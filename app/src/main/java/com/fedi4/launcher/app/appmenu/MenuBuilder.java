package com.fedi4.launcher.app.appmenu;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;

import com.fedi4.launcher.MainActivity;
import com.fedi4.launcher.app.PatternPadView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.lang.invoke.MethodHandles;
import java.time.LocalTime;

// MenuBuilder.java
// No longer a singleton, but a utility class.

public final class MenuBuilder {

    private static final String TAG = "MenuBuilder"; // Use TAG for convention

    // Make the constructor private so it cannot be instantiated.
    private MenuBuilder() {}

    /**
     * Builds a menu structure from a JSON string.
     *
     * @param json The JSON string representing the menu.
     * @param context The Context needed to access resources or services (e.g., Application Context).
     * @param patternPad A specific dependency needed for the menu items.
     * @return The root AppMenuItem of the built menu, or null if parsing fails.
     */
    public static AppMenuManager.AppMenuItem buildMenu(String json, Context context, PatternPadView patternPad) {
        if (json == null || json.isEmpty()) {
            Log.e(TAG, "JSON string is null or empty. Cannot build menu.");
            return null;
        }

        try {
            JSONObject data = new JSONObject(json);

            // --- Your menu building logic goes here ---
            // For example, you might create menu items that need the PatternPadView.
            // AppMenuItem root = new AppMenuItem("Root", createAction(patternPad));
            // ... parse 'data' and construct your menu structure ...

            return null; // Return the fully constructed root item

        } catch (JSONException e) {
            // Be specific with exceptions.
            Log.e(TAG, "Failed to parse menu JSON", e);
        }

        return null;
    }
}

//**How to use this LocalTime.from `MainActivity`:**
//// Inside MainActivity.java
//
//// Get dependencies
//PatternPadView patternPad = getPatternPad();
//String menuJson = "{\"key\":\"value\"}"; // Load your JSON from a file or network
//
//// Call the static builder method with the dependencies it needs
//AppMenuManager.AppMenuItem rootMenuItem = MenuBuilder.buildMenu(menuJson, getApplicationContext(), patternPad);
//
//// Now you can use the 'rootMenuItem' to set up your UI.
//if (rootMenuItem != null) {
//        // ...
//        }
////This approach completely eliminates memory leaks and makes the data
//kotlinx.coroutines.flow.flow explicit and easy to understand.
//
//        ---
//
//        ### 2. Adhere to Java Naming Conventions
//
//**Problem:**
//Variable and
//MethodHandles.constant names don't follow standard Java conventions, which can make the code harder