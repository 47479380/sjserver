package com.yhdxhw.sjserver.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import com.yhdxhw.sjserver.MainActivity;

public class WifiBroadcast extends BroadcastReceiver {
    private MainActivity mainActivity;

    public WifiBroadcast(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private static final String TAG = "WifiBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (Objects.equals(intent.getAction(), WifiManager.WIFI_STATE_CHANGED_ACTION)) {
//            switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WIFI_STATE_UNKNOWN)) {
//                case WIFI_STATE_DISABLED: {
//
//                    mainActivity.getWebServerFragment().onWifiClose();
////                    Toast.makeText(context, "WiFi 已关闭", Toast.LENGTH_SHORT).show();
//                    break;
//                }
//              /*  case WIFI_STATE_DISABLING: {
//                    Toast.makeText(context, "WiFi 正在关闭", Toast.LENGTH_SHORT).show();
//                    break;
//                }
//                case WIFI_STATE_ENABLED: {
//
//                    break;
//                }
//                case WIFI_STATE_ENABLING: {
//                    Toast.makeText(context, "WiFi 正在启动", Toast.LENGTH_SHORT).show();
//                    break;
//                }
//                case WIFI_STATE_UNKNOWN: {
//                    Toast.makeText(context, "WiFi 未知状态", Toast.LENGTH_SHORT).show();
//                    break;
//                }*/
//            }
//        }
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected = state== NetworkInfo.State.CONNECTED;//当然，这边可以更精确的确定状态
                  Log.d(TAG,String.valueOf(isConnected));
                if(isConnected){
                    mainActivity.getWebServerFragment().onWifiOpen();
                }else{
                    mainActivity.getWebServerFragment().onWifiClose();
                }
            }
        }
    }
}
