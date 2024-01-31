package com.azarpark.watchman.interfaces;

import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Place;

public interface OnGetInfoClicked {

    public void pay(int price, Place place);

    public void payAsDebt(Place place);

    public void removeExitRequest(Place place);

    public void charge(PlateType plateType, String tag1, String tag2, String tag3, String tag4, boolean hasMobile);

    public void buyDiscount(PlateType plateType, String tag1, String tag2, String tag3, String tag4, boolean hasMobile);

    public void print(int parkCount, String startTime, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID, int debt, int balance, String printDescription, int printCommand, String carToken);

    public void newPark(Place place);
}
