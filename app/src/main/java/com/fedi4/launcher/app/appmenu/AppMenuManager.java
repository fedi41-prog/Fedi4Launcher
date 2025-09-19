package com.fedi4.launcher.app.appmenu;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.fedi4.launcher.MainActivity;
import com.fedi4.launcher.R;
import com.fedi4.launcher.app.PatternPadView;

import java.util.ArrayList;
import java.util.List;

public class AppMenuManager {

    private final String TAG = "AppMenuManager";

    private AppMenuItem root = new AppMenuItem("");

    private List<AppMenuItem> stack = new ArrayList<>();

    private List<Integer> pointStack = new ArrayList<>();

    private AppMenuItem lastItem;

    // In AppMenuManager.java
    private MainActivity context; // Instance variable, not static
    private PatternPadView patternPad; // Instance variable

    // In AppMenuManager.java
    private static AppMenuManager menu; // Your existing Singleton instance

    public static synchronized AppMenuManager getInstance(MainActivity context) {
        if (menu == null) {
            menu = new AppMenuManager();
        } else {return menu;}
        // Set the activityContext and patternPadInstance on the 'menu' (the singleton) instance
        menu.context = context;
        if (context != null) { // Null check for context
            menu.patternPad = context.getPatternPad(); // Get and store PatternPadView instance
            if (menu.patternPad == null) {
                throw new RuntimeException("PatternPadView is null in AppMenuManager.getInstance");
            }
        } else {
            throw new RuntimeException("Context is null in AppMenuManager.getInstance");
        }
        return menu;
    }

    private AppMenuManager() {
        menu = this;
    }





    private void renderMenu(int current) {

        AppMenuItem currentItem = stack.get(0);

        AppMenuItem[] it = currentItem.getItems();

        for (int i = 0; i < it.length; i++) {
            if (it[i] != null && i != current) {
                if (!it[i].getPackageName().isEmpty()) {
                    assignAppIconToPoint(i, it[i].getIcon());
                }  else { assignAppIconToPoint(i, "$RESET"); }
            } else if (i == current) {
                assignAppIconToPoint(i, currentItem.getIcon());
            } else {
                assignAppIconToPoint(i, "$RESET");
            }


            if (pointStack.size() > 1) {
                if (i == pointStack.get(1)) {
                    assignAppIconToPoint(i, "$BACK");
                }
            }
        }

    }

    public void setRoot(AppMenuItem root) {
        this.root = root;
        stack = new ArrayList<>();
        stack.add(0, root);
        pointStack = new ArrayList<>();
        pointStack.add(-1);
        renderMenu(-1);
    }

    public AppMenuItem getCurrentItem() {
        return stack.get(0);
    }

    public MainActivity getContext() {
        return context;
    }

    private void assignAppIconToPoint(int pointIndex, @NonNull String packageName) {

        if (packageName.equals("$RESET") || packageName.isEmpty()) {
            patternPad.clearPointIcon(pointIndex);
        } else if (packageName.equals("$BACK")) {
            Drawable icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_revert);
            patternPad.setPointIconDrawable(pointIndex, icon);
        } else if (packageName.startsWith("$CUSTOM:")) {
            Drawable icon = ContextCompat.getDrawable(context, R.drawable.icon_games);
            patternPad.setPointIconDrawable(pointIndex, icon);
        } else {
            try {
                Drawable icon = context.getPackageManager().getApplicationIcon(packageName);
                patternPad.setPointIconDrawable(pointIndex, icon);
            } catch (PackageManager.NameNotFoundException e) {
                // Fallback: eigenes Icon aus res/drawable nehmen
                Drawable fallback = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_close_clear_cancel);
                patternPad.setPointIconDrawable(pointIndex, fallback);
            }
        }
    }

    public static class TouchListener implements PatternPadView.TouchListener {


        @Override
        public void onTouchDetected(int point) {
            AppMenuManager manager = AppMenuManager.getInstance(null);

            AppMenuItem next = manager.getCurrentItem().getItems()[point];


            if (manager.pointStack.size() > 1) {
                if (point == manager.pointStack.get(1)) {
                    manager.stack.remove(0);
                    manager.pointStack.remove(0);
                    manager.renderMenu(point);
                }
            }
            if (next != null) {
                if (next.hasTag("folder")) {
                    manager.stack.add(0, manager.getCurrentItem().getItems()[point]);
                    manager.pointStack.add(0, point);
                    manager.renderMenu(point);
                }
            }

            manager.lastItem = next;



        }

        @Override
        public void onTouchEnded(int point) {
            AppMenuManager manager = AppMenuManager.getInstance(null);
            AppMenuItem cur = manager.getCurrentItem();

            if (point == -1) {
                if (manager.lastItem != null) {
                    if (manager.lastItem.hasTag("executable")) {
                        AppMenuManager.getInstance(null).context.launchApp(manager.lastItem.getPackageName());
                    }
                }
            } else {

                AppMenuItem chosen = manager.getCurrentItem().getItems()[point];
                int lastPoint = manager.pointStack.get(0);
                if (lastPoint == point && cur != null) {
                    if (cur.hasTag("executable")) {
                        AppMenuManager.getInstance(null).context.launchApp(cur.getPackageName());
                    }
                } else if (chosen != null) {
                    if (chosen.hasTag("executable")) {
                        AppMenuManager.getInstance(null).context.launchApp(chosen.getPackageName());
                    }
                }
            }


            manager.setRoot(manager.root);

            manager.renderMenu(-1);
        }
    }
    public static class AppMenuItem {
        private AppMenuItem[] items = new AppMenuItem[9];
        private String packageName;
        private List<String> tags = new ArrayList<>();
        private String icon;

        public AppMenuItem(String packageName) {
            this.packageName = packageName;
            this.icon = packageName;
        }

        public boolean hasTag(String tag) {
            return tags.contains(tag);
        }
        public AppMenuItem addTag(String tag) {
            tags.add(tag);
            return this;
        }

        public AppMenuItem setIcon(String icon) {
            this.icon = icon;
            return this;
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

        public String getIcon() {
            return icon;
        }
    }
}
