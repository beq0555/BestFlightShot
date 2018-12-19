package com.websarva.wings.android.bestflightshot;

public class ConditionListItem {
    private int imageId;
    private String craftType;
    private  String departureTime;
    private  String airline;

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

    public  String getAirline() {return airline;}

    public void  setAirline(String airline) {this.airline = airline;}


}
