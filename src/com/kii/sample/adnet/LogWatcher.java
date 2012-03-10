package com.kii.sample.adnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class LogWatcher implements Runnable {
    private static final String TAG = "KiiAdnetSample";
    private static final Pattern networkPattern = Pattern.compile("nid=\"(.*)\" name=\"(.*)\"");
    private static final Pattern pingPattern = Pattern.compile("\\?nid=(.*)&type");
    
    private boolean runnable = false;
    private Handler handler;
    private Runtime runtime = Runtime.getRuntime();
    private Process proc;
    private WeakReference<TextView> impressionTextRef;
    private WeakReference<TextView> clickTextRef;
    private WeakReference<TableLayout> tableRef;
    
    private int impression = 0;
    private int click = 0;
    private Map<String, NetworkInfo> networks = new TreeMap<String, NetworkInfo>();
    
    public LogWatcher(Handler handler, TextView impressionText, TextView clickText, TableLayout table) {
        this.handler = handler;
        this.impressionTextRef = new WeakReference<TextView>(impressionText);
        this.clickTextRef = new WeakReference<TextView>(clickText);
        this.tableRef = new WeakReference<TableLayout>(table);
    }
    
    public void start() {
        runnable = true;
        try {
            runtime.exec("logcat -c");
        } catch (IOException e) {
            Log.e(TAG, "failed to clear locat", e);
        }
        // wait 10 second
        handler.postDelayed(this, 10000); 
    }
    
    public void stop() {
        runnable = false;
    }
    @Override
    public void run() {
        try {
            proc = runtime.exec("logcat -d");
            InputStream is = proc.getInputStream();
            List<String> lines = readLines(is);
            runtime.exec("logcat -c");
            readLog(lines);
            // update UI
            handler.post(new ShowRunnable(impressionTextRef.get(), impression, clickTextRef.get(), click, tableRef.get(), networks));
            
            Log.d(TAG, "Impression:" + impression);
            Log.d(TAG, "Click:" + click);
        } catch (IOException e) {
            Log.e(TAG, "failed to get locat", e);
        }
        if (runnable) {
            handler.postDelayed(this, 3000);
        }
    }
    
    private List<String> readLines(InputStream in) throws IOException {
        List<String> result = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(in), 8192);
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
            return result;
        } finally {
            in.close();
            if (br != null) br.close();
        }
    }
    private void readLog(List<String> lines) {
        for (String line : lines) {
            if (line.contains("Pinging")) {
                if (line.contains("exmet")) {
                    ++impression;
                    String nid = getNid(line);
                    if (nid != null) {
                        NetworkInfo networkInfo = networks.get(nid);
                        if (networkInfo == null) {
                            networkInfo = new NetworkInfo();
                            networks.put(nid, networkInfo);
                        }
                        networkInfo.countImpression();
                    }
                } else if (line.contains("exclick")) {
                    ++click;
                    String nid = getNid(line);
                    if (nid != null) {
                        NetworkInfo networkInfo = networks.get(nid);
                        if (networkInfo == null) {
                            networkInfo = new NetworkInfo();
                            networks.put(nid, networkInfo);
                        }
                        networkInfo.countClick();
                    }
                }
            } else if (line.contains("network nid")) {
                parseNetworkLine(line);
            }
        }
    }
    
    private String getNid(String line) {
        Matcher m = pingPattern.matcher(line);
        if (m.find()) {
            return m.group(1); 
        }
        return null;
    }

	private void parseNetworkLine(String line) {
        Matcher m = networkPattern.matcher(line);
        if (m.find()) {
            String nid = m.group(1);
            String name = m.group(2);
            NetworkInfo info = new NetworkInfo();
            info.setName(name);
            networks.put(nid, info);
            Log.d(TAG, "parse nid=" + nid + " name=" + name);
        }
        
    }

	private static class ShowRunnable implements Runnable {
        private WeakReference<TextView> impressionRef;
        private int impressionCount;
        private WeakReference<TextView> clickRef;
        private int clickCount;
        private WeakReference<TableLayout> tableRef;
        private Map<String, NetworkInfo> networks;
        
        public ShowRunnable(TextView impressionText, int impressionCount, 
                TextView clickText, int clickCount,
                TableLayout table, Map<String, NetworkInfo> networks) {
            this.impressionRef = new WeakReference<TextView>(impressionText);
            this.impressionCount = impressionCount;
            this.clickRef = new WeakReference<TextView>(clickText);
            this.clickCount = clickCount;
            this.tableRef = new WeakReference<TableLayout>(table);
            this.networks = networks;
        }
        @Override
        public void run() {
            TextView text = impressionRef.get();
            if (text != null) {
                text.setText("Impressions:" + impressionCount);
            }
            text = clickRef.get();
            if (text != null) {
                text.setText(" Clicks:" + clickCount);
            }
            TableLayout table = tableRef.get();
            if (table != null) {
                updateTable(table);
            }
        }
        
        private void updateTable(TableLayout table) {
            table.removeAllViews();
            Context context = table.getContext();

            // set header
            TableRow headerRow = new TableRow(context);
            table.addView(headerRow);
            
            TextView text = new TextView(context);
            text.setText("Name");
            headerRow.addView(text);
            
            text = new TextView(context);
            text.setText("Impression");
            headerRow.addView(text);
            
            text = new TextView(context);
            text.setText("Click");
            headerRow.addView(text);
            
            Set<String> keys = networks.keySet();
            for (String key : keys) {
                NetworkInfo networkInfo = networks.get(key);
                TableRow row = new TableRow(context);
                table.addView(row);
                
                text = new TextView(context);
                text.setText(networkInfo.getName());
                row.addView(text);
                
                text = new TextView(context);
                text.setGravity(Gravity.RIGHT);
                text.setText(String.valueOf(networkInfo.getImpression()));
                row.addView(text);
                
                text = new TextView(context);
                text.setGravity(Gravity.RIGHT);
                text.setText(String.valueOf(networkInfo.getClick()));
                row.addView(text);
            }
        }
    }
}
