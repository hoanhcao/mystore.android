package com.hevaisoi.android.model;

/**
 * Created by ERP on 10/25/2016.
 */

public class ClothModel {
    public int Id;
    public String Code;
    public String ImgUrl;
    public Number Price;
    private boolean IsNew;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getCode() {
        return Code;
    }

    public Number getPrice() {
        return Price;
    }

    public String getImgUrl() {
        return ImgUrl;
    }

    public boolean isNew() {
        return IsNew;
    }
}
