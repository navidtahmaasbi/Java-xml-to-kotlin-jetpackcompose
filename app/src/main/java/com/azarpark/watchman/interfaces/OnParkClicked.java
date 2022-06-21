package com.azarpark.watchman.interfaces;

import com.azarpark.watchman.web_service.bodies.ParkBody;

public interface OnParkClicked {

    public void clicked(ParkBody parkBody, boolean printFactor);
}
