package com.fedi4.launcher.app.appmenu;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class JsonStorage {

    public static void save(Context context, String jsonString) {
        try {
            FileOutputStream fos = context.openFileOutput("save.json", Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String load(Context context) {
        try {
            FileInputStream fis = context.openFileInput("save.json");
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            return new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
