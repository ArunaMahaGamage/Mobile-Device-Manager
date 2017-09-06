package com.palvision.devicemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView mBatteryLevelText;
    private static TextView mCheckConnection;
    private ProgressBar mBatteryLevelProgress;


    TextView mDisplayMessage, mDisplaySSID, mDisplaySSIDLevel, mHttpResponse;
    Context context;

    Integer mHttpState;
    int mBetteryLevel, mWifiLevel;

    WifiConfiguration mWifiConfig;
    WifiInfo mWInfo;
    String mMacAddress;
    Integer mIpAddress;

    String PAL_BSSID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        PAL_BSSID = "00:26:75:11:9e:66";

        initUI();

        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {

                testRequest();
                handler.postDelayed(this, 10000);
            }
        };

        handler.postDelayed(r, 5000);

    }

    public void initUI() {
        mDisplayMessage = (TextView) findViewById(R.id.tv_main_display);
        mDisplaySSID = (TextView) findViewById(R.id.tv_main_ssid);
        mDisplaySSIDLevel = (TextView) findViewById(R.id.tv_main_level);
        mHttpResponse = (TextView) findViewById(R.id.tv_main_httprespone);

        mBatteryLevelText = (TextView) findViewById(R.id.tvBatteryText);
        mCheckConnection = (TextView) findViewById(R.id.tvCheckConnection);
        mBatteryLevelProgress = (ProgressBar) findViewById(R.id.progressBar);
    }

    public void getWifiMessage() {
        mWifiConfig = new WifiConfiguration();
        mWifiConfig.SSID = String.format("\"%s\"", "PAL");
        mWifiConfig.preSharedKey = String.format("\"%s\"", "newoffice");
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        mWInfo = wifiManager.getConnectionInfo();
        mMacAddress = mWInfo.getBSSID();
        mIpAddress = mWInfo.getIpAddress();

        PAL_BSSID = "00:26:75:11:9e:66";
        // Level of current connection
        int rssi = wifiManager.getConnectionInfo().getRssi();
        mWifiLevel = WifiManager.calculateSignalLevel(rssi, 5);

        rssi = wifiManager.getConnectionInfo().getRssi();
        mWifiLevel = WifiManager.calculateSignalLevel(rssi, 5);

        Toast.makeText(getApplicationContext(), "WIFI Level = " + mWifiLevel,
                Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), "SSID = " + mWInfo.getSSID(),
                Toast.LENGTH_LONG).show();

        if (mWifiLevel <= 3) {
            mDisplayMessage.setText("");
            mDisplaySSID.setText(mWInfo.getSSID());
            mDisplaySSIDLevel.setText(new String(String.valueOf(mWifiLevel)));
        }
        if ((mWifiLevel <= 2)) {
            Toast.makeText(getApplicationContext(), "Please leave phone on bed",
                    Toast.LENGTH_LONG).show();
            mDisplayMessage.setText("Please leave phone on bed");
        } else if (mWifiLevel == 0) {
            mDisplayMessage.setText("");
            mDisplaySSID.setText("");
            mDisplaySSIDLevel.setText("");
        }

        sentDataToAPILogic();
    }

    public synchronized void testRequest() {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "http://119.73.222.42:8081/pvsioncms/api/devicemanager";
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        mHttpResponse.setText("Response: " + "True");
                        mHttpState = 200;
                        getWifiMessage();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mHttpResponse.setText("Response: " + "False");
                mHttpState = 404;
                getWifiMessage();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        betteryLevel();
    }

    public void sentDataToAPILogic() {

        if (mBetteryLevel <= 20) {
            if (mWifiLevel <= 2) {
                new DataToAPI(this).execute(mBetteryLevel, "Low Bettery", mWInfo.getSSID(), mWifiLevel, "In range", mHttpState, mMacAddress, mIpAddress);
            } else {
                new DataToAPI(this).execute(mBetteryLevel, "Low Bettery", mWInfo.getSSID(), mWifiLevel, "Out Of range", mHttpState, mMacAddress, mIpAddress);
            }
        } else if (mWifiLevel <= 2) {
            if (mBetteryLevel > 20 && mBetteryLevel < 60) {
                new DataToAPI(this).execute(mBetteryLevel, "Mid Bettery", mWInfo.getSSID(), mWifiLevel, "Out Of range", mHttpState, mMacAddress, mIpAddress);
            } else if (mBetteryLevel > 60) {
                new DataToAPI(this).execute(mBetteryLevel, "Full Bettery", mWInfo.getSSID(), mWifiLevel, "Out Of range", mHttpState, mMacAddress, mIpAddress);
            } else {
                new DataToAPI(this).execute(mBetteryLevel, "Low Bettery", mWInfo.getSSID(), mWifiLevel, "Out Of range", mHttpState, mMacAddress, mIpAddress);
            }
        } else if (mHttpState == 404) {
            new DataToAPI(this).execute(mBetteryLevel, "Mid Bettery", mWInfo.getSSID(), mWifiLevel, "In range", mHttpState, mMacAddress, mIpAddress);
        } else if ((mBetteryLevel < 20) && (mWifiLevel <= 2)) {
            new DataToAPI(this).execute(mBetteryLevel, "Low Bettery", mWInfo.getSSID(), mWifiLevel, "out of range", mHttpState, mMacAddress, mIpAddress);
        } else if ((mBetteryLevel < 20) && (mHttpState == 404)) {
            new DataToAPI(this).execute(mBetteryLevel, "Low Bettery", mWInfo.getSSID(), mWifiLevel, "In range", mHttpState, mMacAddress, mIpAddress);
        } else if ((mWifiLevel <= 2) && (mHttpState == 404)) {
            new DataToAPI(this).execute(mBetteryLevel, "Mid Bettery", mWInfo.getSSID(), mWifiLevel, "out of range", mHttpState, mMacAddress, mIpAddress);
        }

    }

    public class DataToAPI extends AsyncTask {
        private Context context;
        public DataToAPI(Context context) {
            this.context = context;

        }

        @Override
        protected Object doInBackground(Object[] objects) {

            Integer level = (Integer) objects[0];
            String message = (String) objects[1];
            String SSID = (String) objects[2];
            Integer wifilevel = (Integer) objects[3];
            String wifimessage = (String) objects[4];
            Integer httpresponse = (Integer) objects[5];
            String macAddress = (String) objects[6];
            Integer mIpAddress = (Integer) objects[7];

            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL("http://119.73.222.42:8081/pvsioncms/api/devicemanager/store");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("betterystate", level.toString())
                        .appendQueryParameter("message", message)
                        .appendQueryParameter("ssid", SSID)
                        .appendQueryParameter("wifilevel", wifilevel.toString())
                        .appendQueryParameter("wifimessage", wifimessage)
                        .appendQueryParameter("httpresponse", httpresponse.toString())
                        .appendQueryParameter("mac_address", macAddress)
                        .appendQueryParameter("ip_address", mIpAddress.toString());
                String query = builder.build().getEncodedQuery();
                wr.writeBytes(query);
                Log.e("JSON Input", query);
                wr.flush();
                wr.close();
                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void betteryLevel() {
        Intent batteryIntent = context.getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int rawlevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        double scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        mBetteryLevel = -1;
        if (rawlevel >= 0 && scale > 0) {
            mBetteryLevel = (int) ((rawlevel * 100) / scale);
        }

        mBatteryLevelText.setText(getString(R.string.battery_level) + " " + mBetteryLevel);
        mBatteryLevelProgress.setProgress((int) mBetteryLevel);
    }

    public static class MyNetworkMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // Process the Intent here
            WifiConfiguration wifiConfig = new WifiConfiguration();

            wifiConfig.SSID = String.format("\"%s\"", "PAL");
            wifiConfig.preSharedKey = String.format("\"%s\"", "newoffice");

            WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);

            wifiManager.setWifiEnabled(true);

            int rssi = wifiManager.getConnectionInfo().getRssi();
            int mWifiLevel = WifiManager.calculateSignalLevel(rssi, 5);

            WifiInfo wInfo = wifiManager.getConnectionInfo();
            String APMacAddress = wInfo.getBSSID();
            Integer mIpAddress = wInfo.getIpAddress();

            String PAL_BSSID = "00:26:75:11:9e:66";
            wifiManager.setWifiEnabled(true);

            if ((PAL_BSSID.equals(APMacAddress))) {
                wifiManager.setWifiEnabled(true);
                wifiManager.reconnect();
            } else {
                wifiManager.disconnect();
            }
        }
    }
}