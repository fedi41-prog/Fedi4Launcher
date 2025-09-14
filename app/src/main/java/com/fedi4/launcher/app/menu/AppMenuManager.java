package com.fedi4.launcher.app.menu;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.fedi4.launcher.MainActivity;
import com.fedi4.launcher.app.PatternPadView;

public class AppMenuManager {

    private final String TAG = "AppMenuManager";
    private static AppMenuManager menu;
    private PatternPadView patternPad;
    private MainActivity context;

    private AppMenuItem[] items = new AppMenuItem[9];

    private AppMenuItem currentPoint;

    private AppMenuManager(@NonNull MainActivity context) {
        menu = this;
        this.context = context;
        this.patternPad = context.getPatternPad();

    }

    private void initMenu() {

        for (int i = 0; i < items.length; i++) {

            if (items[i] != null) {

                assignAppIconToPoint(i, items[i].getPackageName());

            }

        }

    }

    private void updateMenu() {



    }

    public AppMenuManager insertItem(AppMenuItem item, int index) {
        items[index] = item;
        return this;
    }
    public AppMenuItem[] getItems() {
        return items;
    }

    public static AppMenuManager getInstance(MainActivity context) {
        if (menu == null) {
            menu = new AppMenuManager(context);
            return menu;
        }
        return menu;
    }

    private void assignAppIconToPoint(int pointIndex, String packageName) {
        Log.d(TAG, "trying to load an icon...");
        try {
            Drawable icon = context.getPackageManager().getApplicationIcon(packageName);
            Log.d(TAG, "first step done...");
            patternPad.setPointIconDrawable(pointIndex, icon);
            Log.d(TAG, "SUCCESS");
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "ERROR, fallback icon");
            // Fallback: eigenes Icon aus res/drawable nehmen
            Drawable fallback = ContextCompat.getDrawable(context, android.R.drawable.ic_delete);
            patternPad.setPointIconDrawable(pointIndex, fallback);
        }
    }

    public static class TouchListener implements PatternPadView.TouchListener {


        @Override
        public void onTouchDetected(int point) {

        }

        @Override
        public void onTouchEnded() {

        }
    }
    private static class AppMenuItem {
        private AppMenuItem[] items = new AppMenuItem[9];
        private String packageName;

        public AppMenuItem(String packageName) {
            this.packageName = packageName;
        }

        public AppMenuItem insertItem(AppMenuItem item, int index) {
            items[index] = item;
            return this;
        }

        public AppMenuItem[] getItems() {
            return items;
        }

        public String getPackageName() {
            return packageName;
        }
    }
}
