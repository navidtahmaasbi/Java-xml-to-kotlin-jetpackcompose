package com.azarpark.watchman.models;

import android.net.Uri;

public class DetectionResult {
    private Uri sourceImageUri;
    private Uri plateImageUri;
    private String plateTag;

    public DetectionResult() {
    }

    public DetectionResult(Uri sourceImageUri, Uri plateImageUri, String plateTag) {
        this.sourceImageUri = sourceImageUri;
        this.plateImageUri = plateImageUri;
        this.plateTag = plateTag;
    }

    public Uri getSourceImageUri() {
        return sourceImageUri;
    }

    public void setSourceImageUri(Uri sourceImageUri) {
        this.sourceImageUri = sourceImageUri;
    }

    public Uri getPlateImageUri() {
        return plateImageUri;
    }

    public void setPlateImageUri(Uri plateImageUri) {
        this.plateImageUri = plateImageUri;
    }

    public String getPlateTag() {
        return plateTag;
    }

    public void setPlateTag(String plateTag) {
        this.plateTag = plateTag;
    }
}
