package com.azarpark.cunt.models;

import android.graphics.Bitmap;

public class DetectionResult {
    private Bitmap sourceImageUri;
    private Bitmap plateImageUri;
    private String plateTag;

    public DetectionResult() {
    }

    public DetectionResult(Bitmap sourceImageUri, Bitmap plateImageUri, String plateTag) {
        this.sourceImageUri = sourceImageUri;
        this.plateImageUri = plateImageUri;
        this.plateTag = plateTag;
    }

    public Bitmap getSourceImageUri() {
        return sourceImageUri;
    }

    public void setSourceImageUri(Bitmap sourceImageUri) {
        this.sourceImageUri = sourceImageUri;
    }

    public Bitmap getPlateImageUri() {
        return plateImageUri;
    }

    public void setPlateImageUri(Bitmap plateImageUri) {
        this.plateImageUri = plateImageUri;
    }

    public String getPlateTag() {
        return plateTag;
    }

    public void setPlateTag(String plateTag) {
        this.plateTag = plateTag;
    }
}
