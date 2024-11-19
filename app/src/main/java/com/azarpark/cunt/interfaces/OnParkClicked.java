package com.azarpark.cunt.interfaces;

import android.graphics.Bitmap;

import com.azarpark.cunt.web_service.bodies.ParkBody;

public interface OnParkClicked {

    public void clicked(ParkBody parkBody, boolean printFactor, Bitmap sourceImageUri, Bitmap plateImageUri);
}
