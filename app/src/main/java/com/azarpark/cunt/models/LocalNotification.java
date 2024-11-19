package com.azarpark.cunt.models;

public class LocalNotification {

    public String id;
    public int placeID, placeNumber;
    public Type type;

    public LocalNotification(String id, int placeID, int placeNumber, Type type) {
        this.id = id;
        this.placeID = placeID;
        this.placeNumber = placeNumber;
        this.type = type;
    }

    public static enum Type{

        exitRequest,
        freeByUser

    }
}
