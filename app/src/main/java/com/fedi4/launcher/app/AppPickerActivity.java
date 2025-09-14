package com.fedi4.launcher.app;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.appcompat.app.AppCompatActivity;

import com.fedi4.launcher.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppPickerActivity extends AppCompatActivity {

    PackageManager pm;
    ListView listView;
    List<Map<String, Object>> appItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_picker);
        pm = getPackageManager();
        listView = findViewById(R.id.list_apps);
        loadInstalledApps();

        String[] from = {"icon", "label"};
        int[] to = {R.id.icon, R.id.label};

        SimpleAdapter adapter = new SimpleAdapter(this, appItems, R.layout.item_app, from, to) {
            @Override
            public void setViewImage(android.widget.ImageView v, String value) {
                // do nothing (we override binding below)
            }
        };

        // custom bind to set icon which is a Drawable stored as "iconDrawable"
        listView.setAdapter(new android.widget.BaseAdapter() {
            @Override public int getCount() { return appItems.size(); }
            @Override public Object getItem(int position) { return appItems.get(position); }
            @Override public long getItemId(int position) { return position; }
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_app, parent, false);
                }
                Map<String, Object> item = appItems.get(position);
                android.widget.ImageView iv = convertView.findViewById(R.id.icon);
                android.widget.TextView tv = convertView.findViewById(R.id.label);
                iv.setImageDrawable((Drawable) item.get("iconDrawable"));
                tv.setText((String) item.get("label"));
                return convertView;
            }
        });

        listView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            Map<String, Object> item = appItems.get(position);
            String pkg = (String) item.get("package");
            Intent res = new Intent();
            res.putExtra("package", pkg);
            setResult(RESULT_OK, res);
            finish();
        });
    }

    private void loadInstalledApps() {
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        for (ApplicationInfo ai : apps) {
            if (pm.getLaunchIntentForPackage(ai.packageName) == null) continue; // only launchable apps
            Map<String, Object> map = new HashMap<>();
            map.put("label", pm.getApplicationLabel(ai).toString());
            map.put("package", ai.packageName);
            Drawable icon = pm.getApplicationIcon(ai);
            map.put("iconDrawable", icon);
            appItems.add(map);
        }
        // optional: sort alphabetically
        appItems.sort((a,b) -> ((String)a.get("label")).compareToIgnoreCase((String)b.get("label")));
    }
}
