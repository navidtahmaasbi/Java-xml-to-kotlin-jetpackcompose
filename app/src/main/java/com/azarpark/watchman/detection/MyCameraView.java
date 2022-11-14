package com.azarpark.watchman.detection;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.widget.Toast;

import org.opencv.android.JavaCameraView;

import java.util.List;

public class MyCameraView extends JavaCameraView {
    public MyCameraView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setFocusMode(Context context, int i) {

        Camera.Parameters parameters = this.mCamera.getParameters();
        this.mCamera.cancelAutoFocus();
        this.mCamera.autoFocus(new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean z, Camera camera) {
            }
        });
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();

        if (i != 0) {
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        if (i != 4) {
                            if (i == 5) {
                                if (supportedFocusModes.contains("macro")) {
                                    parameters.setFocusMode("macro");
                                } else {
                                    Toast.makeText(context, "Macro Mode is not supported", 0).show();
                                }
                            }
                        } else if (supportedFocusModes.contains("infinity")) {
                            parameters.setFocusMode("infinity");
                        } else {
                            Toast.makeText(context, "Infinity Mode is not supported", 0).show();
                        }
                    } else if (supportedFocusModes.contains("fixed")) {
                        parameters.setFocusMode("fixed");
                    } else {
                        Toast.makeText(context, "Fixed Mode is not supported", 0).show();
                    }
                } else if (supportedFocusModes.contains("edof")) {
                    parameters.setFocusMode("edof");
                } else {
                    Toast.makeText(context, "EDOF Mode is not supported", 0).show();
                }
            } else if (supportedFocusModes.contains("continuous-video")) {
                parameters.setFocusMode("continuous-video");
            } else {
                Toast.makeText(context, "Continuous Mode is not supported", 0).show();
            }
        } else if (supportedFocusModes.contains("auto")) {
            parameters.setFocusMode("auto");
        } else {
            Toast.makeText(context, "Auto Mode is not supported", 0).show();
        }
        this.mCamera.setParameters(parameters);
    }

    public void setzoom(Context context, int i) {
        if (this.mCamera != null) {
            Camera.Parameters parameters = this.mCamera.getParameters();
            int maxZoom = parameters.getMaxZoom();
            int zoom = parameters.getZoom();
            if (i == 1 && zoom < maxZoom - 8) {
                parameters.setZoom(zoom + 8);
            }
            if (i == -1 && zoom > 4) {
                parameters.setZoom(zoom - 4);
            }
            this.mCamera.setParameters(parameters);
        }
    }

    public void setFlashMode(Context context, int i, String flashMode) {
        selectedFlashMode = flashMode;
        Camera.Parameters parameters = this.mCamera.getParameters();
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes.contains(selectedFlashMode))
            parameters.setFlashMode(selectedFlashMode);
        else
            Toast.makeText(context, selectedFlashMode + " Mode not supported", Toast.LENGTH_SHORT).show();
        this.mCamera.setParameters(parameters);
//        public static String Torch = "torch",
//                redEye = "red-eye",
//                on = "on",
//                off = "off",
//                auto = "auto";


//        if (this.mCamera != null) {
//            if (i != 0) {
//                if (i != 1) {
//                    if (i != 2) {
//                        if (i != 3) {
//                            if (i == 4) {
//                                if (supportedFlashModes.contains("torch")) {
//                                    parameters.setFlashMode("torch");
//                                } else {
//                                    Toast.makeText(context, "Torch Mode not supported", 0).show();
//                                }
//                            }
//                        } else if (supportedFlashModes.contains("red-eye")) {
//                            parameters.setFlashMode("red-eye");
//                        } else {
//                            Toast.makeText(context, "Red Eye Mode not supported", 0).show();
//                        }
//                    } else if (supportedFlashModes.contains("on")) {
//                        parameters.setFlashMode("on");
//                    } else {
//                        Toast.makeText(context, "On Mode not supported", 0).show();
//                    }
//                } else if (supportedFlashModes.contains("off")) {
//                    parameters.setFlashMode("off");
//                } else {
//                    Toast.makeText(context, "Off Mode not supported", 0).show();
//                }
//            } else if (supportedFlashModes.contains("auto")) {
//                parameters.setFlashMode("auto");
//            } else {
//                Toast.makeText(context, "Auto Mode not supported", 0).show();
//            }
//
//        }
    }

    public static final String Torch = "torch",
            RedEye = "red-eye",
            On = "on",
            Off = "off",
            Auto = "auto";

    public String selectedFlashMode = Off;


}