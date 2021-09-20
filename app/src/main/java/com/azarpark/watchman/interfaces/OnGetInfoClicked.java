package com.azarpark.watchman.interfaces;

import com.azarpark.watchman.models.Place;

public interface OnGetInfoClicked {

    public void pay(int price, int placeID);

    public void payAsDebt(Place place);
}
