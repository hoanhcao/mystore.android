package com.hevaisoi.android.model;

/**
 * Created by ERP on 11/1/2016.
 */

public class ProductTypeModel {

    public int getTypeId() {
        return TypeId;
    }

    public void setTypeId(int typeId) {
        TypeId = typeId;
    }

    public String getTypeDesc() {
        return TypeDesc;
    }

    public void setTypeDesc(String typeDesc) {
        TypeDesc = typeDesc;
    }

    public int getTotalProduct() {
        return TotalProduct;
    }

    public void setTotalProduct(int totalProduct) {
        TotalProduct = totalProduct;
    }

    public int TypeId;

    public String getTypeUrl() {
        return TypeUrl;
    }

    public void setTypeUrl(String typeUrl) {
        TypeUrl = typeUrl;
    }

    public int getTypeOrder() {
        return TypeOrder;
    }

    public void setTypeOrder(int typeOrder) {
        TypeOrder = typeOrder;
    }

    public String TypeUrl;
    public int TypeOrder;
    public String TypeDesc;
    public int TotalProduct;

   /* public ProductTypeModel(int typeId, String typeDesc, int totalProduct) {
        TypeId = typeId;
        TypeDesc = typeDesc;
        TotalProduct = totalProduct;
    }*/

}
