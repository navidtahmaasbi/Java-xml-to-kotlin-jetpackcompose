package com.azarpark.watchman.interfaces;

import android.graphics.Bitmap;
import android.net.Uri;

import com.azarpark.watchman.web_service.bodies.ParkBody;

public interface OnParkClicked {

    public void clicked(ParkBody parkBody, boolean printFactor, Bitmap sourceImageUri, Bitmap plateImageUri);
}
