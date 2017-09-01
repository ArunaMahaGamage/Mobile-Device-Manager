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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private TextView mBatteryLevelText;
    private static TextView mCheckConnection;
    private ProgressBar mBatteryLevelProgress;
    private BroadcastReceiver mReceiver, mNetworkReceiver;

    Button mTest;
    TextView mDisplayMessage, mDisplaySSID, mDisplaySSIDLevel, mHttpResponse;
    String  mSSID, mSDisplayLevel, mSHttpResponse;
    Context context;

    Boolean b, mHttpState;
    int level, wifiLevel;

    WifiConfiguration wifiConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        mBatteryLevelText = (TextView) findViewById(R.id.tvBatteryText);
        mCheckConnection = (TextView) findViewById(R.id.tvCheckConnection);

        mBatteryLevelProgress = (ProgressBar) findViewById(R.id.progressBar);
        mReceiver = new BatteryBroadcastReceiver();
        mNetworkReceiver = new NetworkChangeReceiver();

        final Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                b = isOnline();
                System.out.println(b);

                initUI();

                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(r, 1000);

    }

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
    @Override
    protected void onStop() {
        unregisterReceiver(mReceiver);
//        unregisterReceiver(mNetworkReceiver);
        super.onStop();
    }

    public class BatteryBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
                level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                mBatteryLevelText.setText(getString(R.string.battery_level) + " " + level);
                mBatteryLevelProgress.setProgress(level);
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {
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
        }

    public static void dialog(boolean value){

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
    }

    public class CallFloorDataApi extends AsyncTask<String, Void, String> {
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mNetworkReceiver);
    }

    public void initUI() {
      //  mTest = (Button) findViewById(R.id.btn_main_test);
        mDisplayMessage = (TextView) findViewById(R.id.tv_main_display);
        mDisplaySSID = (TextView) findViewById(R.id.tv_main_ssid);
        mDisplaySSIDLevel = (TextView) findViewById(R.id.tv_main_level);
        mHttpResponse = (TextView) findViewById(R.id.tv_main_httprespone);


        showSSID();
        testRequest();

       /* mTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWifiMessage();
            }
        });*/
    }

    public void showSSID() {
        wifiConfig = new WifiConfiguration();

        wifiConfig.SSID = String.format("\"%s\"", "PAL");
        wifiConfig.preSharedKey = String.format("\"%s\"", "P@l12345");

        WifiManager wifiManager=(WifiManager)getSystemService(WIFI_SERVICE);
        int netId = wifiManager.addNetwork(wifiConfig);
      //  wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
     //   wifiManager.reconnect();

        System.out.println(wifiConfig.status);

        System.out.println(wifiManager.getConnectionInfo());

        System.out.println(wifiManager.EXTRA_PREVIOUS_WIFI_STATE);

        // Level of current connection
        int rssi = wifiManager.getConnectionInfo().getRssi();
        int level = WifiManager.calculateSignalLevel(rssi, 5);
        System.out.println("Level is " + level + " out of 5");


        mDisplaySSID.setText(wifiConfig.SSID);
        mDisplaySSIDLevel.setText(new String(String.valueOf(level)));
    }

    public void getWifiMessage() {


        WifiConfiguration wifiConfig = new WifiConfiguration();

        wifiConfig.SSID = String.format("\"%s\"", "PAL");
        wifiConfig.preSharedKey = String.format("\"%s\"", "P@l12345");

        WifiManager wifiManager=(WifiManager)getSystemService(WIFI_SERVICE);
        int netId = wifiManager.addNetwork(wifiConfig);
     //   wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
     //   wifiManager.reconnect();

        System.out.println(wifiConfig.status);

        System.out.println(wifiManager.getConnectionInfo());

        System.out.println(wifiManager.EXTRA_PREVIOUS_WIFI_STATE);


        // Level of current connection
        int rssi = wifiManager.getConnectionInfo().getRssi();
        wifiLevel = WifiManager.calculateSignalLevel(rssi, 5);
        System.out.println("Level is " + level + " out of 5");

        while (level < 4) {

            rssi = wifiManager.getConnectionInfo().getRssi();
            level = WifiManager.calculateSignalLevel(rssi, 5);
            System.out.println("Level is " + level + " out of 5");

            if ((level > 0) && (level <= 2)) {

                rssi = wifiManager.getConnectionInfo().getRssi();
                level = WifiManager.calculateSignalLevel(rssi, 5);

                Toast.makeText(getApplicationContext(), "This is Level = " + level,
                        Toast.LENGTH_LONG).show();

                Toast.makeText(getApplicationContext(), "This is Level = " + b,
                        Toast.LENGTH_LONG).show();

                mDisplayMessage.setText("Please leave phone on bed");
                mDisplaySSID.setText(wifiConfig.SSID);
                mDisplaySSIDLevel.setText(new String(String.valueOf(level)));

                break;
            }


        }
    }



    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void testRequest() {
        String url = "http://my-json-feed";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        mHttpResponse.setText("Response: " + "True");
                        mHttpState = true;
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        mHttpResponse.setText("Response: " + "False");
                        mHttpState = false;
                    }
                });

// Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    public void sentDataToAPILogic() {
        if (level < 20) {
            sentDataToApi(level, "low Bettery", wifiConfig.SSID.toString(), wifiLevel, "In range", mHttpState);
        } else if (wifiLevel > 2) {
            sentDataToApi(level, "mid Bettery", wifiConfig.SSID.toString(), wifiLevel, "out of range", mHttpState);
        } else if (mHttpState == false) {
            sentDataToApi(level, "mid Bettery", wifiConfig.SSID.toString(), wifiLevel, "In range", mHttpState);
        } else if ((level < 20) && (wifiLevel > 2)) {
            sentDataToApi(level, "low Bettery", wifiConfig.SSID.toString(), wifiLevel, "out of range", mHttpState);
        } else if ((level < 20) && (mHttpState == false)) {
            sentDataToApi(level, "low Bettery", wifiConfig.SSID.toString(), wifiLevel, "In range", mHttpState);
        } else if ((wifiLevel > 2) && (mHttpState == false)) {
            sentDataToApi(level, "mid Bettery", wifiConfig.SSID.toString(), wifiLevel, "out of range", mHttpState);
        }
    }

    public void sentDataToApi(int betteryLevel,String batteryLevelMessage, String ssid, int wifiLevel, String wifiMessage, Boolean httpState) {
        String url = "http://my-json-feed";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(context, "This is my Toast message!",
                                Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

// Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

}