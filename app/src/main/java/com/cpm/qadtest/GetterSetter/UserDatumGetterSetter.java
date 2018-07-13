package com.cpm.qadtest.GetterSetter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserDatumGetterSetter {
    @SerializedName("User_Data")
    @Expose
    private List<UserDatum> userData = null;

    public List<UserDatum> getUserData() {
        return userData;
    }

    public void setUserData(List<UserDatum> userData) {
        this.userData = userData;
    }
}
