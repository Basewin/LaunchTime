package com.quaap.launchtime.components;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * Copyright (C) 2017   Tom Kliethermes
 *
 * This file is part of LaunchTime and is is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
public class SpecialIconStore {


    public static String makeSafeName(String name) {

        try {
            byte[] inbytes = name.getBytes("UTF-8");

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] shabytes = md.digest(inbytes);

            StringBuilder hexString = new StringBuilder();
            for (int i=0;i<shabytes.length;i++) {
                String hex=Integer.toHexString(0xff & shabytes[i]);
                if(hex.length()==1) hexString.append('0');
                hexString.append(hex);
            }

            //Log.d("Icon", "name " + name + " => " + hexString);
            return hexString.toString() + ".png";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //return "icon-" + name.replaceAll("(\\.\\.|[\\\\//$&():=#])+", "_");
    }

    public static void saveBitmap(Context context, ComponentName cname, Bitmap bitmap, IconType iconType) {

        String name = cname.getPackageName() + ":" + cname.getClassName();
        try  {
            String fname = makeSafeName(name) + "." + iconType.name();
            FileOutputStream fos = context.openFileOutput(fname, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fos);
            fos.close();
            Log.d("SpecialIconStore", "Saved icon " + fname);
        } catch (IOException e) {
            Log.e("SpecialIconStore", e.getMessage(), e);
        }
    }

    public static  Bitmap loadBitmap(Context context, ComponentName cname, IconType iconType) {

        String name = cname.getPackageName() + ":" + cname.getClassName();


        Bitmap bitmap = null;

//            for (String fn: context.fileList()) {
//                Log.d("SpecialIconStore", " I see file " + fn);
//            }
        bitmap = loadBitmap(context, makeSafeName(name) + "." + iconType.name());
        if (bitmap == null) {
            bitmap = loadBitmap(context, makeSafeName(name));
        }
        if (bitmap == null) {
            bitmap = loadBitmap(context, makeSafeName(cname.getClassName()));
        }


        return bitmap;
    }

    private static Bitmap loadBitmap(Context context, String name) {
        Bitmap bitmap = null;
        try {
            if (fileExists(context, name)) {
                FileInputStream fis = context.openFileInput(name);
                bitmap = BitmapFactory.decodeStream(fis);
                fis.close();
            }
        } catch (IOException e) {
            Log.e("SpecialIconStore", e.getMessage(), e);
        }
        return bitmap;
    }


    public static boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }


    public enum IconType {
        Cached, Shortcut, Custom
    }



}
