package com.azarpark.watchman.interfaces;

import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;

public interface OnParkClicked {

    public void clicked(ParkBody parkBody, boolean printFactor);
}
