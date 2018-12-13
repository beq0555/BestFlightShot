package com.websarva.wings.android.bestflightshot;

public class SpecialListItem {

    private int imageId;
    private String craftType;
    private  String departureTime;
    private String specialInfo;

    public int getImageId(){
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getCraftType() {
        return craftType;
    }

    public void setCraftType(String craftType) {
        this.craftType = craftType;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getSpecialInfo() {return specialInfo;}

    public void setSpecialInfo(String specialInfo) {this.specialInfo = specialInfo;}
}