package com.palvision.devicemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.content.Context.WIFI_SERVICE;

public class MainActivity extends AppCompatActivity {

    private TextView mBatteryLevelText;
    private static TextView mCheckConnection;
    private ProgressBar mBatteryLevelProgress;
    private BroadcastReceiver mReceiver, mNetworkReceiver;

    Button mTest;
    TextView mDisplayMessage, mDisplaySSID, mDisplaySSIDLevel, mHttpResponse;
    String  mSSID, mSDisplayLevel, mSHttpResponse;
    Context context;

    Integer mHttpState;
    int betteryLevel, wifiLevel;

    WifiConfiguration wifiConfig;
    WifiInfo wInfo;
    String macAddress;
    Integer ipAddress;

    String PAL_BSSID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        context = getApplicationContext();

        PAL_BSSID = "00:26:75:11:9e:66";

        mBatteryLevelText = (TextView) findViewById(R.id.tvBatteryText);
        mCheckConnection = (TextView) findViewById(R.id.tvCheckConnection);

        mBatteryLevelProgress = (ProgressBar) findViewById(R.id.progressBar);
    //    mReceiver = new BatteryBroadcastReceiver();
    //    mNetworkReceiver = new NetworkChangeReceiver();

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
/*
    @Override
    protected void onStart() {

        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        super.onStart();
    }

    public class BatteryBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
                level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                mBatteryLevelText.setText(getString(R.string.battery_level) + " " + level);
                mBatteryLevelProgress.setProgress(level);
        }
    }*/

 /*   public class NetworkChangeReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {

                try {
                    if (internetConnectionAvailable(1000)) {
                        dialog(true);
                        Log.e("keshav", "Online Connect Intenet ");
                    } else {
                        dialog(false);
                        Log.e("keshav", "Conectivity Failure !!! ");
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            private boolean internetConnectionAvailable(int timeOut) {
                InetAddress inetAddress = null;
                try {
                    Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(new Callable<InetAddress>() {
                        @Override
                        public InetAddress call() {
                            try {
                                return InetAddress.getByName("google.com");
                            } catch (UnknownHostException e) {
                                return null;
                            }
                        }
                    });
                    inetAddress = future.get(timeOut, TimeUnit.MILLISECONDS);
                    future.cancel(true);
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                } catch (TimeoutException e) {
                }
                return inetAddress != null && !inetAddress.equals("");
            }
        }*/

/*    public static void dialog(boolean value){

        if(value){
            mCheckConnection.setText("We are back !!!");
            mCheckConnection.setBackgroundColor(Color.GREEN);
            mCheckConnection.setTextColor(Color.WHITE);

            Handler handler = new Handler();
            Runnable delayrunnable = new Runnable() {
                @Override
                public void run() {
                    mCheckConnection.setVisibility(View.GONE);
                }
            };
            handler.postDelayed(delayrunnable, 3000);
        }else {
            mCheckConnection.setVisibility(View.VISIBLE);
            mCheckConnection.setText("Could not Connect to internet");
            mCheckConnection.setBackgroundColor(Color.RED);
            mCheckConnection.setTextColor(Color.WHITE);
        }
    }*/

 /*   public class CallFloorDataApi extends AsyncTask<String, Void, String> {
        String server_response;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("floor", "")
                        .appendQueryParameter("update", "");

                String query = builder.build().getEncodedQuery();
                wr.writeBytes(query);
                Log.e("JSON Input", query);
                wr.flush();
                wr.close();
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //server_response = AppUtils.readStream(urlConnection.getInputStream());
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }*/
/*
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mNetworkReceiver);
    }*/

    public void initUI() {
      //  mTest = (Button) findViewById(R.id.btn_main_test);
        mDisplayMessage = (TextView) findViewById(R.id.tv_main_display);
        mDisplaySSID = (TextView) findViewById(R.id.tv_main_ssid);
        mDisplaySSIDLevel = (TextView) findViewById(R.id.tv_main_level);
        mHttpResponse = (TextView) findViewById(R.id.tv_main_httprespone);
    }


    public void getWifiMessage() {


        wifiConfig = new WifiConfiguration();

        wifiConfig.SSID = String.format("\"%s\"", "localhost.localdomain");
        wifiConfig.preSharedKey = String.format("\"%s\"", "nvZhZr69");

        WifiManager wifiManager=(WifiManager)getSystemService(WIFI_SERVICE);
      //  int netId = wifiManager.addNetwork(wifiConfig);

        wInfo = wifiManager.getConnectionInfo();
        macAddress = wInfo.getBSSID();
        ipAddress = wInfo.getIpAddress();

        String mac = wInfo.getBSSID();

        int ip = 1711450304;

        PAL_BSSID = "00:26:75:11:9e:66";

        String Aruna_BSSID = "3c:a0:67:7d:33:f7";
        int Aruna_IP = 1728064010;

        wifiManager.setWifiEnabled(true);

        /*
        if ((PAL_BSSID != macAddress)) {
            wifiManager.disconnect();

        //    wifiManager.setWifiEnabled(false);
            boolean wifiEnabled = wifiManager.isWifiEnabled();
            System.out.print(wifiEnabled);
        }*/

      //   wifiManager.disconnect();
      //  wifiManager.enableNetwork(netId, true);
    //    wifiManager.reconnect();


        System.out.println(wifiConfig.status);

        System.out.println(wifiManager.getConnectionInfo());

        System.out.println(wifiManager.EXTRA_PREVIOUS_WIFI_STATE);


        // Level of current connection
        int rssi = wifiManager.getConnectionInfo().getRssi();
        wifiLevel = WifiManager.calculateSignalLevel(rssi, 5);
        System.out.println("Level is " + wifiLevel + " out of 5");

    //    while (level < 4) {

            rssi = wifiManager.getConnectionInfo().getRssi();
            wifiLevel = WifiManager.calculateSignalLevel(rssi, 5);
            System.out.println("Level is " + wifiLevel + " out of 5");

            if ((wifiLevel > 0) && (wifiLevel <= 3)) {

                rssi = wifiManager.getConnectionInfo().getRssi();
                wifiLevel = WifiManager.calculateSignalLevel(rssi, 5);

                Toast.makeText(getApplicationContext(), "This is Level = " + wifiLevel,
                        Toast.LENGTH_LONG).show();

                mDisplayMessage.setText("Please leave phone on bed");
                mDisplaySSID.setText(wifiConfig.SSID);
                mDisplaySSIDLevel.setText(new String(String.valueOf(wifiLevel)));

             //   break;
            }
     //   }
        sentDataToAPILogic();
    }



    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
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
        if ((ipAddress == wInfo.getIpAddress()) && (macAddress == wInfo.getMacAddress())) {

            if (betteryLevel <= 100) {
                if (wifiLevel <= 2) {
                    new DataToAPI(this).execute(betteryLevel, "Low Bettery", wifiConfig.SSID.toString(), wifiLevel, "In range", mHttpState, macAddress, ipAddress);
                } else {
                    new DataToAPI(this).execute(betteryLevel, "Low Bettery", wifiConfig.SSID.toString(), wifiLevel, "Out Of range", mHttpState, macAddress, ipAddress);
                }
                //   sentDataToApi(level, "low Bettery", wifiConfig.SSID.toString(), wifiLevel, "In range", mHttpState);
            } else if (wifiLevel <= 2) {
                if (betteryLevel > 20 && betteryLevel < 60) {
                    new DataToAPI(this).execute(betteryLevel, "Mid Bettery", wifiConfig.SSID.toString(), wifiLevel, "Out Of range", mHttpState, macAddress, ipAddress);
                } else if (betteryLevel > 60) {
                    new DataToAPI(this).execute(betteryLevel, "Full Bettery", wifiConfig.SSID.toString(), wifiLevel, "Out Of range", mHttpState, macAddress, ipAddress);
                } else {
                    new DataToAPI(this).execute(betteryLevel, "Low Bettery", wifiConfig.SSID.toString(), wifiLevel, "Out Of range", mHttpState, macAddress, ipAddress);
                }
                //  sentDataToApi(level, "mid Bettery", wifiConfig.SSID.toString(), wifiLevel, "out of range", mHttpState);
            } else if (mHttpState == 404) {
                new DataToAPI(this).execute(betteryLevel, "Mid Bettery", wifiConfig.SSID.toString(), wifiLevel, "In range", mHttpState, macAddress, ipAddress);
                // sentDataToApi(level, "mid Bettery", wifiConfig.SSID.toString(), wifiLevel, "In range", mHttpState);
            } else if ((betteryLevel < 20) && (wifiLevel <= 2)) {
                new DataToAPI(this).execute(betteryLevel, "Low Bettery", wifiConfig.SSID.toString(), wifiLevel, "out of range", mHttpState, macAddress, ipAddress);
                // sentDataToApi(level, "low Bettery", wifiConfig.SSID.toString(), wifiLevel, "out of range", mHttpState);
            } else if ((betteryLevel < 20) && (mHttpState == 404)) {
                new DataToAPI(this).execute(betteryLevel, "Low Bettery", wifiConfig.SSID.toString(), wifiLevel, "In range", mHttpState, macAddress, ipAddress);
                //   sentDataToApi(level, "low Bettery", wifiConfig.SSID.toString(), wifiLevel, "In range", mHttpState);
            } else if ((wifiLevel <= 2) && (mHttpState == 404)) {
                new DataToAPI(this).execute(betteryLevel, "Mid Bettery", wifiConfig.SSID.toString(), wifiLevel, "out of range", mHttpState, macAddress, ipAddress);
                //   sentDataToApi(level, "mid Bettery", wifiConfig.SSID.toString(), wifiLevel, "out of range", mHttpState);
            }
        }
    }

    public class DataToAPI  extends AsyncTask{
        private Context context;

        //flag 0 means get and 1 means post.(By default it is get.)
        public DataToAPI(Context context) {
            this.context = context;

        }

        @Override
        protected Object doInBackground(Object[] objects) {
          //  try{
                Integer level = (Integer) objects[0];
                String message = (String)objects[1];
                String SSID = (String)objects[2];
                Integer wifilevel = (Integer) objects[3];
                String wifimessage = (String)objects[4];
                Integer httpresponse = (Integer) objects[5];
                String macAddress = (String) objects[6];
                Integer ipAddress = (Integer) objects[7];

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
                        .appendQueryParameter("ip_address", ipAddress.toString());
                String query = builder.build().getEncodedQuery();
                wr.writeBytes(query);
                Log.e("JSON Input", query);
                wr.flush();
                wr.close();
                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                  //  server_response = AppUtils.readStream(urlConnection.getInputStream());
                }
            }
            catch (MalformedURLException e) {
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
        betteryLevel = -1;
        if (rawlevel >= 0 && scale > 0) {
            betteryLevel = (int) ((rawlevel *100 )/ scale);
        }

        mBatteryLevelText.setText(getString(R.string.battery_level) + " " + betteryLevel);
        mBatteryLevelProgress.setProgress((int) betteryLevel);
    }

    public static class MyNetworkMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // Process the Intent here
            WifiConfiguration wifiConfig = new WifiConfiguration();

            wifiConfig.SSID = String.format("\"%s\"", "localhost.localdomain");
            wifiConfig.preSharedKey = String.format("\"%s\"", "nvZhZr69");

            WifiManager wifiManager=(WifiManager) context.getSystemService(WIFI_SERVICE);
            //  int netId = wifiManager.addNetwork(wifiConfig);

            WifiInfo wInfo = wifiManager.getConnectionInfo();
            String macAddress = wInfo.getBSSID();
            Integer ipAddress = wInfo.getIpAddress();

            String mac = wInfo.getBSSID();

            int ip = 1711450304;

            String PAL_BSSID = "00:26:75:11:9e:66";

            String Aruna_BSSID = "3c:a0:67:7d:33:f7";
            int Aruna_IP = 1728064010;



            if ((PAL_BSSID == macAddress)) {

                wifiManager.setWifiEnabled(true);

            } else if (PAL_BSSID != macAddress){

                wifiManager.disconnect();

                wifiManager.setWifiEnabled(false);

                //    wifiManager.setWifiEnabled(false);
                boolean wifiEnabled = wifiManager.isWifiEnabled();
                System.out.print(wifiEnabled);
            }

        }
    }
}