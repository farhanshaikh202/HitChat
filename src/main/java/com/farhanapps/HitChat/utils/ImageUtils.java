package com.farhanapps.HitChat.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.support.v4.graphics.BitmapCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by farhan on 23-04-2016.
 */
public class ImageUtils {

    public static File BitmapToFileCatch(Context context,Bitmap bitmap,String filename) throws IOException{
        //create a file to write bitmap data
        File f = new File(context.getCacheDir(), filename);
        f.createNewFile();

//Convert bitmap to byte array
        Bitmap mbitmap = bitmap;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mbitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();

        return f;
    }

    public static Bitmap reduceImage(String filepath) {
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filepath),50,50);
    }


    public static void write( String data,String fileName) {

        File root = Environment.getExternalStorageDirectory();
        File outDir = new File(root.getAbsolutePath());
        if (!outDir.isDirectory()) {
            outDir.mkdir();
        }
        try {
            if (!outDir.isDirectory()) {
                throw new IOException(
                        "Unable to create directory EZ_time_tracker. Maybe the SD card is mounted?");
            }
            File outputFile = new File(outDir, fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(data);

            writer.close();
        } catch (IOException e) {
            Log.w("eztt", e.getMessage(), e);

        }

    }
}
