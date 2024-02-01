package com.xsm.lib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DBOpenHelper extends BaseOpenHelper {


    public static final String KEY_USER = "name";
    public static final String KEY_PHONE = "phone";

    public DBOpenHelper(Context context, String fileName, String[] tbs,
                        boolean islog) {
        super(context, fileName, tbs, islog);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String getTableDefine(String tb) {
        // TODO Auto-generated method stub
        String sql = KEY_USER+" VARCHAR(20),";
        sql += KEY_PHONE+" VARCHAR(20),";
        return sql;
    }

    @Override
    protected Element exportElement(Cursor cursor, Document document, String filename) {
        return null;
    }

    @Override
    protected ContentValues loadElement(Element element, String filename) {
        return null;
    }

    public Boolean addItem(String table, UserInfoItem item) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.KEY_USER, item.getUserName());
        values.put(DBOpenHelper.KEY_PHONE, item.getPhoneNum());
        return (insert(table,values)>=0);
    }

    public Boolean removeItem(String table, UserInfoItem item) {
        return (delete(table,DBOpenHelper.KEY_USER+"=?", new String[]{item.getUserName()})>=0);
    }

    public void removeAll(String table) {
        clearTable(table);
    }

    /**
     * byte数组转换为十六进制的字符串
     * @param b
     * @return
     */
    private static String conver16HexStr(byte [] b)
    {
        StringBuffer result = new StringBuffer();
        for(int i = 0;i<b.length;i++)
        {
            if((b[i]&0xff)<0x10)
                result.append("0");
            result.append(Long.toString(b[i]&0xff, 16));
        }
        return result.toString().toUpperCase();
    }

    /**
     * 十六进制的字符串转换为byte数组
     * @param hex16Str
     * @return
     */
    private static byte[] conver16HexToByte(String hex16Str)
    {
        char [] c = hex16Str.toUpperCase().toCharArray();
        byte [] b = new byte[c.length/2];
        for(int i = 0;i<b.length;i++)
        {
            int pos = i * 2;
            b[i] = (byte)("0123456789ABCDEF".indexOf(c[pos]) << 4 | "0123456789ABCDEF".indexOf(c[pos+1]));
        }
        return b;
    }

}
