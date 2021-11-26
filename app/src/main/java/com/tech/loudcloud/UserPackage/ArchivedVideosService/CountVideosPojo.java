package com.tech.loudcloud.UserPackage.ArchivedVideosService;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CountVideosPojo {

    @SerializedName("number")
    @Expose
    private Integer number;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

}