/*************************************************************************
 
 Copyright 2012 Kii Corporation
 http://kii.com
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 *************************************************************************/

package com.kii.sample.adnet;

import java.util.Locale;

import com.kii.ad.KiiAdNetLayout;
import com.kii.ad.KiiAdNetTargeting;
import com.kii.ad.core.KiiAdNetManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class KiiAdNetSampleActivity extends Activity {
    
    private static final String APP_ID = "7deef15e";
    private static final String APP_KEY = "90d72af7fe46d72ffaa26721862c6629";
    private static final String PREF_NAME_APP_INFO = "appInfo";
    private static final String PREF_KEY_APPID = "appId";
    private static final String PREF_KEY_APPKEY = "appKey";
    private static final int ID_APPID = 100;
    private static final int ID_APPKEY = 101;

    private String appId;
    private String appKey;
    private Handler handler = new Handler();
    private LogWatcher logWatcher = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get scaledDensity
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        float scaledDensity = metrics.scaledDensity;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView text = new TextView(this);
        layout.addView(text);
        text.setText("This app is KiiAdNet Viewer");

        // get appId and appKey
        SharedPreferences pref = getSharedPreferences(PREF_NAME_APP_INFO, MODE_PRIVATE);
        appId = pref.getString(PREF_KEY_APPID, "");
        appKey = pref.getString(PREF_KEY_APPKEY, "");
        if (appId == null || appId.length() == 0 ||
            appKey == null || appKey.length() == 0) {
            appId = APP_ID;
            appKey = APP_KEY;
        }

        text = new TextView(this);
        layout.addView(text);
        text.setText("App ID:" + appId);

        text = new TextView(this);
        layout.addView(text);
        text.setText("App Key:" + appKey);

        text = new TextView(this);
        layout.addView(text);
        text.setText("Your country:" + Locale.getDefault().getCountry());

        Button b = new Button(this);
        b.setText("Setting");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingDialog();
            }
        });
        layout.addView(b);

        // for debug
        KiiAdNetManager.setConfigExpireTimeout(1);

        KiiAdNetTargeting.setTestMode(true);
        KiiAdNetLayout adLayout = new KiiAdNetLayout(this, appId, appKey);
        layout.addView(adLayout);
        adLayout.setMaxHeight((int)(scaledDensity * 52));
        adLayout.setMaxWidth((int)(scaledDensity * 320));

        // for metrics table
        ScrollView scroll = new ScrollView(this);
        TableLayout table = new TableLayout(this);
        scroll.addView(table);
        layout.addView(scroll);

        setContentView(layout);

        // log watcher
        logWatcher = new LogWatcher(handler, table);
        logWatcher.start();
        Thread th = new Thread(logWatcher);
        th.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logWatcher != null) logWatcher.stop();
    }


    /**
     * for change appId / appKey
     */
    private void showSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Setting");
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        TextView text = new TextView(this);
        text.setText("App ID");
        layout.addView(text);
        
        EditText edit = new EditText(this);
        edit.setText(appId);
        edit.setInputType(InputType.TYPE_CLASS_TEXT);
        edit.setId(ID_APPID);
        layout.addView(edit);
        
        text = new TextView(this);
        text.setText("App Key");
        layout.addView(text);
        
        edit = new EditText(this);
        edit.setText(appKey);
        edit.setInputType(InputType.TYPE_CLASS_TEXT);
        edit.setId(ID_APPKEY);
        layout.addView(edit);
        
        builder.setView(layout);
        
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    // get view
                    EditText edit = (EditText)layout.findViewById(ID_APPID);
                    appId = edit.getText().toString();
                    edit = (EditText)layout.findViewById(ID_APPKEY);
                    appKey = edit.getText().toString();
                    
                    SharedPreferences pref = getSharedPreferences(PREF_NAME_APP_INFO, MODE_PRIVATE);
                    Editor editor = pref.edit();
                    editor.putString(PREF_KEY_APPID, appId);
                    editor.putString(PREF_KEY_APPKEY, appKey);
                    editor.commit();
                    
                    Toast.makeText(KiiAdNetSampleActivity.this, "Please restart application", Toast.LENGTH_LONG).show();
                }
            }
        };
        builder.setPositiveButton("OK", listener);
        builder.setNegativeButton("Cancel", listener);
        
        builder.create().show();
    }
}