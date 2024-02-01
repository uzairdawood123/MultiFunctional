package com.xsm.lib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import com.xsm.lib.util.UserLog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public abstract class BaseOpenHelper extends SQLiteOpenHelper {
    protected UserLog DEBUG = null;

    public static final String KEY_ID = "_id";
    public static final String KEY_TIME = "_time";

    /**
     * 数据库版本,可以把版本号更改掉 但必须 >=1
     */
    private static final int version = 1;

    private final static boolean OPEN_AHEAD = false;
    private String[] tbs=null;
    private File databaseFile = null;
    /**
     * 数据库是否已经初始化，如果未初始化，则会自动导入初始化数据
     */
    private boolean isInit = false;
    /**
     * 如果想额外的增加一个字段（需求）
     * 可以把版本号更改掉 但必须 >=1
     * 更改版本号之后 会根据版本号判断是不是上次创建的时候 （目前的版本号和传入的版本号是否一致 ）
     * 如果不是会执行 onUpgrade（） 方法
     * @param context 上下文
     * @param fileName 操作的数据库文件名,包含路径:
     * 1.如果这个值设为null,则数据库在memory中创建，否则数据库创建文件
     * 2.如果路径不存在，则会自动创建，
     * 3.如果不存在数据库文件，则会自动生成
     * @param tbs 数据表列表，初始化时会生成所有数据表
     * @param islog 是否启用调试信息
     */
    public BaseOpenHelper(Context context, String fileName,String[] tbs, boolean islog) {
        super(context, fileName, null, version);
        DEBUG = new UserLog(this.getClass().getSimpleName(), islog);
        this.tbs = (tbs==null)? new String[0]: tbs;
        if(fileName!=null) {
            databaseFile = new File(fileName);
            if(!databaseFile.exists()) { //数据库不存在
                databaseFile.getParentFile().mkdirs(); //如果目录不存在则创建目录
                isInit=false;
            }
            else isInit=true;
        }
        else isInit=false;
        if(Build.VERSION.SDK_INT >= 11&&OPEN_AHEAD){
            this.getWritableDatabase().enableWriteAheadLogging();
        }
        else this.getReadableDatabase(); //触发生成数据库
    }

    /**
     * 数据库是否已经初始化
     * @return
     */
    public boolean isInit() {return this.isInit;}

    /**
     * 可用于重载，生成不同的表结构
     * @param tb 当前创建的表的表名
     * @return
     */
    protected abstract String getTableDefine(String tb);

    /**
     * 用游标的当前位置数据新建一个文件标签
     * @param cursor 数据游标
     * @param document
     * @param filename 数据输出的文件名
     * @return
     */
    protected abstract Element exportElement(Cursor cursor, Document document, String filename);

    /**
     * 插入操作
     * @param element 需要生成数据的标签
     * @param filename 保存初始化数据的文件名
     * @return
     */
    protected abstract ContentValues loadElement(Element element, String filename);

    /**
     * 取得数据库文件
     * @return
     */
    public File getDatabaseFile() {
        return databaseFile;
    }

    /**
     * 初始化内存数据库
     * @param tb 数据表名
     * @param filename 保存初始化数据的文件名
     * @return
     */
    public boolean loadFormXml(String tb, String filename) {
        try {
            DEBUG.log("loadFormXml to "+tb+" form "+filename);
            File file = new File(filename);
            if(!file.exists()) return false; //文件不存在
            DEBUG.log("find file ok");
            //导入xml解码器
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = db.parse(new FileInputStream(file));
            DEBUG.log("decode xml file ok");
            //解码节点
            NodeList nodeList = document.getChildNodes();
            for(int cnt = 0; cnt<nodeList.getLength(); cnt++) {
                Node topNode = nodeList.item(cnt);
                if(topNode.getNodeType()==Node.ELEMENT_NODE && topNode.getNodeName().trim().equals(tb)) { //父节点正确
                    NodeList childList = topNode.getChildNodes();
                    for (int i = 0; i < childList.getLength(); i++) { //取出每一个数据项
                        Node node = childList.item(i);
                        if(node.getNodeType() == Node.ELEMENT_NODE) { //节点类型正确
                            ContentValues values = this.loadElement((Element)node, filename);
                            if(values!=null) this.insert(tb, values);
                        }
                        //	else DEBUG.log("childNode type error<"+node.getNodeType()+"> = "+node.getNodeName());
                    }
                }
                else {
                    //	DEBUG.log("topNode type error<"+topNode.getNodeType()+"> = "+topNode.getNodeName());
                }
            }
            return true;
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 将数据导出到xml文件中
     * @param tb
     * @param filename
     * @param selection 选择器，比如"id=? and oid=?"等等，"?"则在selectionArgs中定义值
     * @param selectionArgs 条件占位符的值定义
     * @return
     */
    public boolean exportToXml(String tb, String filename, String selection, String[] selectionArgs) {
        boolean sta = false;
        Cursor cursor=null;
        try {
            //新建一个xml文档对像
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = db.newDocument();
            Element root = document.createElement(tb);
            document.appendChild(root);
            //添加节点内容
            cursor = this.query(tb,null,selection, selectionArgs, null);
            while (cursor.moveToNext()) {
                Element element = this.exportElement(cursor, document, filename);
                if(element!=null) root.appendChild(element);
            }
            DEBUG.log("appendChild to document ok.");
            //保存数据到文件
            File file = new File(filename);
            file.getParentFile().mkdirs(); //生成目录
            //配置xml头
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");// 设置输出采用的编码方式
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");// 是否自动添加额外的空白
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");// 是否忽略XML声明
            //写入到文件
            DOMSource source = new DOMSource(document);
            //  PrintWriter pw = new PrintWriter(new FileOutputStream(file));
            transformer.transform(source, new StreamResult(new FileOutputStream(file)));
            DEBUG.log("save xml file ok.");
            sta = true;
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally{
            if(cursor!=null) cursor.close();
        }
        return sta;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String start_sql = " (";
        start_sql += KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,";
        String end_sql = KEY_TIME+" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP";
        end_sql += ")";
        for(int i=0; i<tbs.length; i++) {
            String sql = getTableDefine(tbs[i]);
            if(sql==null) sql="";
            db.execSQL("CREATE TABLE "+ tbs[i] + start_sql+sql+end_sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS person");
        onCreate(db);
    }

    /**
     * 删除操作
     * @param tb 数据表名
     * @param selection 选择器，比如"id=? and oid=?"等等，"?"则在selectionArgs中定义值
     * @param selectionArgs 条件占位符的值定义
     * @return
     */
    public int delete(String tb, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tb, selection, selectionArgs);
    }

    /**
     * 插入操作
     * @param tb 数据表名
     * @param values 需要插入的值
     * @return
     */
    public long insert(String tb, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        long newBookId=db.insert(tb, null, values);//返回添加的行
        return newBookId;
    }

    /**
     * 查找操作
     * @param tb 数据表名
     * @param columns 需要返回的列
     * @param selection 选择器，比如"id=? and oid=?"等等，"?"则在selectionArgs中定义值
     * @param selectionArgs 条件占位符的值定义
     * @param sortOrder 排序方式["asc","desc"]，为null表示使用默认排序方式
     * @return
     */
    public Cursor query(String tb, String[] columns, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(tb, columns, selection,selectionArgs, null, null, sortOrder);
        return cursor;
    }

    /**
     * 更新操作
     * @param tb 数据表名
     * @param values 需要更新的值
     * @param selection 选择器，比如"id=? and oid=?"等等，"?"则在selectionArgs中定义值
     * @param selectionArgs 条件占位符的值定义
     * @return
     */
    public int update(String tb, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        int updateRows = db.update(tb, values, selection, selectionArgs);
        return updateRows;
    }

    /**
     * 是否存在某个列表
     * @param tableName 数据表名
     * @return
     * @throws SQLException
     */
    public boolean isTableExist(String tableName)throws SQLException {
        boolean result = false;
        if (tableName == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select count(1) as c from sqlite_master where type ='table' and name ='" + tableName.trim() + "'";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }

            if(cursor!=null) cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 删除某个列表
     * @param table 数据表名
     * @throws SQLException
     */
    public void deleteTable(String table) throws SQLException
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE "+table);
    }

    /**
     * 清空某个列表
     * @param table 数据表名
     * @throws SQLException
     */
    public void clearTable(String table) throws SQLException
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+table);
        db.execSQL("VACUUM");
    }
}
