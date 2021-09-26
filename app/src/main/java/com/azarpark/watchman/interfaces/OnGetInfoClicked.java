package com.azarpark.watchman.interfaces;

import com.azarpark.watchman.models.Place;

public interface OnGetInfoClicked {

    public void pay(int price, Place place);

    public void payAsDebt(Place place);

    public void removeExitRequest(Place place);
}
