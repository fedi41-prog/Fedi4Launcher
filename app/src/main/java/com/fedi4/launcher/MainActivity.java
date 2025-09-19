package com.fedi4.launcher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fedi4.launcher.app.PatternPadView;
import com.fedi4.launcher.app.appmenu.AppMenuManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private PatternPadView patternPad;

    private AppMenuManager menuManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        patternPad = findViewById(R.id.patternPad);
        menuManager = AppMenuManager.getInstance(this);

        patternPad.setTouchListener(new AppMenuManager.TouchListener());

        // Example call in onCreate after patternPad is initialized
        listAllInstalledApps();

        initMenu();
    }

    public void launchApp(String packageName) {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "App not installed", Toast.LENGTH_SHORT).show();
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

    public PatternPadView getPatternPad() {
        return patternPad;
    }


    private void initMenu() {

        menuManager.setRoot(
                new AppMenuManager.AppMenuItem("")
                        .insertItem(
                                new AppMenuManager.AppMenuItem("com.android.settings")
                                        .insertItem(
                                                new AppMenuManager.AppMenuItem("com.chess").addTag("executable").addTag("folder")
                                                        .insertItem(new AppMenuManager.AppMenuItem("com.openai.chatgpt").addTag("executable"), 0)
                                                        .insertItem(new AppMenuManager.AppMenuItem("de.digionline.lernsax").addTag("executable"), 2)
                                                , 1
                                        )
                                        .addTag("folder")
                                , 4
                        )
                        .insertItem(
                                new AppMenuManager.AppMenuItem("games")
                                        .insertItem(new AppMenuManager.AppMenuItem("com.zeptolab.evopop").addTag("executable"), 4)
                                        .insertItem(new AppMenuManager.AppMenuItem("air.com.midjiwan.polytopia").addTag("executable"), 1)
                                        .addTag("folder")
                                        .setIcon("$CUSTOM:icon_games.jpg")
                                ,5
                        )
        ); //TODO: replace with json parsing function

    }
}
