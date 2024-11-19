package com.azarpark.cunt.utils;


import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public class Imagecompressor {
    public static File compressImage(Context context, File imageFile) throws IOException {
        return new Compressor(context)
                .setQuality(70)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(context.getFilesDir() + Constants.IMAGES_DIRECTORY)
                .compressToFile(imageFile);
    }

}
