package com.fedi4.launcher.app;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.fedi4.launcher.MainActivity;

public class AppMenuManager {

    private final String TAG = "AppMenuManager";
    private static AppMenuManager menu;
    private PatternPadView patternPad;
    private static MainActivity context;

    private AppMenuItem root = new AppMenuItem("");
    private AppMenuItem currentItem = root;

    private AppMenuManager(@NonNull MainActivity context) {
        menu = this;
        this.context = context;
        this.patternPad = context.getPatternPad();

        renderMenu(-1);

    }

    private void renderMenu(int notResetIcon) {

        Log.d(TAG, "rendering Menu...");

        AppMenuItem[] it = currentItem.getItems();

        for (int i = 0; i < it.length; i++) {
            Log.d(TAG, "item" + i);
            if (it[i] != null) {
                if (!it[i].getPackageName().isEmpty()) {
                    Log.d(TAG, "setting icon for " + it[i].getPackageName());
                    assignAppIconToPoint(i, it[i].getPackageName());
                }
            } else if (i != notResetIcon){
                assignAppIconToPoint(i, "$RESET");
            }

        }

    }

    public void setRoot(AppMenuItem root) {
        this.root = root;
        currentItem = root;
        renderMenu(-1);
    }

    public void updateItems(AppMenuItem root) {
        currentItem = root;
    }

    public AppMenuItem getCurrentItem() {
        return currentItem;
    }

    public static AppMenuManager getInstance(MainActivity context) {
        if (menu == null) {
            menu = new AppMenuManager(context);
            return menu;
        }
        return menu;
    }

    public static MainActivity getContext() {
        return context;
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
            AppMenuManager manager = AppMenuManager.getInstance(AppMenuManager.getContext());

            AppMenuItem next = manager.getCurrentItem().getItems()[point];

            if (next != null) {
                manager.updateItems(manager.getCurrentItem().getItems()[point]);
            }

            manager.renderMenu(point);



        }

        @Override
        public void onTouchEnded() {
            AppMenuManager manager = AppMenuManager.getInstance(AppMenuManager.getContext());

            AppMenuManager.getContext().launchApp(manager.getCurrentItem().getPackageName());

            manager.updateItems(manager.root);

            manager.renderMenu(-1);
        }
    }
    public static class AppMenuItem {
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
