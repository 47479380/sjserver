package com.yhdxhw.sjserver.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yhdxhw.sjserver.MainActivity;

public class WifiApBroadcast extends BroadcastReceiver {
    private static final String TAG = "WifiApBroadcast";
    private MainActivity mainActivity;

    public WifiApBroadcast(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {


        String action = intent.getAction();
        if("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)){
            //便携式热点的状态为：10---正在关闭；11---已关闭；12---正在开启；13---已开启
            int state = intent.getIntExtra("wifi_state",  0);
            Log.e(TAG,"热点开关状态：state= "+String.valueOf(state));
            if(state == 13){

                mainActivity.getWebServerFragment().setWifiApEnabled(true);
                mainActivity.getWebServerFragment().onWifiApOpen();

                Log.e(TAG,"热点已开启");
            }else if(state == 11){
                mainActivity.getWebServerFragment().setWifiApEnabled(false);
                mainActivity.getWebServerFragment().onWifiApClose();
                Log.e(TAG,"热点已关闭");
            }else if(state == 10){
                Log.e(TAG,"热点正在关闭");
            }else if(state == 12){
                Log.e(TAG,"热点正在开启");
            }
        }
    }
}
