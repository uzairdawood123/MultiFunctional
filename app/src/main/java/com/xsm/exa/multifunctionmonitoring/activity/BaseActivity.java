package com.xsm.exa.multifunctionmonitoring.activity;

import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {

    protected static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    /**
     * 权限检查
     * android 6.0 以上需要动态申请权限
     * @param neededPermissions 需要的权限
     */
    protected void initPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            initPermissionDone(ACTION_REQUEST_PERMISSIONS,true); //无权限请求则表示初始化权限完成
            return;
        }
        ArrayList<String> toApplyList = new ArrayList<String>();
        for (String perm : neededPermissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) { //有未申请的权限则开始申请
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), ACTION_REQUEST_PERMISSIONS);
        }
        else { //所有权限通过则表示初始化权限完成
            initPermissionDone(ACTION_REQUEST_PERMISSIONS,true);
        }
    }

    /**
     * 检查能否找到动态链接库，如果找不到，请修改工程配置
     *
     * @param libraries 需要的动态链接库
     * @return 动态库是否存在
     */
    protected boolean checkSoFile(String[] libraries) {
        File dir = new File(getApplicationInfo().nativeLibraryDir);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        List<String> libraryNameList = new ArrayList<>();
        for (File file : files) {
            libraryNameList.add(file.getName());
        }
        boolean exists = true;
        for (String library : libraries) {
            exists &= libraryNameList.contains(library);
        }
        return exists;
    }

//    /**
//     * 权限检查
//     *
//     * @param neededPermissions 需要的权限
//     * @return 是否全部被允许
//     */
//    protected boolean checkPermissions(String[] neededPermissions) {
//        if (neededPermissions == null || neededPermissions.length == 0) {
//            return true;
//        }
//        boolean allGranted = true;
//        for (String neededPermission : neededPermissions) {
//            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
//        }
//        return allGranted;
//    }

    // 此处为android 6.0以上动态授权的回调，用户自行实现。
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAllGranted = true;
        for (int grantResult : grantResults) {
            isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
        }
        initPermissionDone(requestCode, isAllGranted);
    }

    /**
     * 初始化权限完成后的回调
     *
     * @param requestCode  请求码
     * @param isAllGranted 是否全部被同意
     */
    protected abstract void initPermissionDone(int requestCode, boolean isAllGranted);

    protected void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
    protected void showLongToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

}
