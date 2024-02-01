package com.xsm.lib.db;

import android.widget.CheckBox;

public class UserInfoItem {

    /**
     * 用于实现选中对像的清除选中功能
     */
    private CheckBox checkBox = null;
    /**
     * 用户名
     */
    private String userName = "";
    /**
     * 电话号码
     */
    private String phoneNum = "";
    /**
     * 保存当前是否选中的状态
     * 之所以不用checkBox来判定是为了更高效及跨线程时访问UI出错的问题
     */
    private Boolean isSelect = false;

    public UserInfoItem(String name, String phone) {
        userName = name;
        phoneNum = phone;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(CheckBox checkBox) {
        this.checkBox = checkBox;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public Boolean getSelect() {
        return isSelect;
    }

    public void setSelect(Boolean select) {
        isSelect = select;
    }

}
