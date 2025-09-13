package com.fedi4.launcher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private PatternPadView patternPad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        patternPad = findViewById(R.id.patternPad);

        patternPad.setPatternListener((pattern, mappingMode) -> {
            // Hier Beispiel: 0-1-2 öffnet YouTube
            switch (pattern) {
                case "0-1-2":
                    launchApp("com.openai.chatgpt");
                    break;
                case "3-4-5":
                    launchApp("com.sec.android.app.camera");
                    break;
                default:
                    Toast.makeText(this, "Pattern not mapped", Toast.LENGTH_SHORT).show();
            }
        });

        // Example call in onCreate after patternPad is initialized
        assignAppIconToPoint(0, "com.openai.chatgpt"); // Test with Chrome

        listAllInstalledApps();
    }

    private void launchApp(String packageName) {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "App not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void assignAppIconToPoint(int pointIndex, String packageName) {
        Log.d(TAG, "trying to load an icon...");
        try {
            Drawable icon = getPackageManager().getApplicationIcon(packageName);
            Log.d(TAG, "first step done...");
            patternPad.setPointIconDrawable(pointIndex, icon);
            Log.d(TAG, "SUCCESS");
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "ERROR, fallback icon");
            // Fallback: eigenes Icon aus res/drawable nehmen
            Drawable fallback = ContextCompat.getDrawable(this, android.R.drawable.ic_delete);
            patternPad.setPointIconDrawable(pointIndex, fallback);
        }
    }



    public void listAllInstalledApps() {
        PackageManager pm = getPackageManager();
        // Die '0' als Flag bedeutet, dass keine zusätzlichen Informationen pro Paket benötigt werden,
        // was den Vorgang beschleunigt.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        // Alternativ, wenn Sie nur Paket-Objekte und nicht unbedingt ApplicationInfo wollen:
        // List<PackageInfo> packages = pm.getInstalledPackages(0);


        Log.d(TAG, "--- Start Liste installierter Apps ---");
        for (ApplicationInfo appInfo : packages) {
            // appInfo.packageName enthält den Paketnamen
            Log.d(TAG, "App Name: " + appInfo.loadLabel(pm).toString() + ", Paketname: " + appInfo.packageName);

            // Wenn Sie pm.getInstalledPackages(0) verwendet haben, wäre es:
            // for (PackageInfo packageInfo : packages) {
            //     Log.d(TAG, "Paketname: " + packageInfo.packageName);
            // }
        }
        Log.d(TAG, "--- Ende Liste installierter Apps ---");
    }

}
