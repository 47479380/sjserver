package com.yhdxhw.sjserver.ui.Web;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.yhdxhw.sjserver.MainActivity;
import com.yhdxhw.sjserver.R;


import com.yhdxhw.sjserver.WebSettingsActivity;
import com.yhdxhw.sjserver.server.ServerException;
import com.yhdxhw.sjserver.service.WebService;
import com.yhdxhw.sjserver.utils.NetUtils;
import com.yhdxhw.sjserver.utils.ZipUtils;


import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static android.content.Context.BIND_AUTO_CREATE;

public class WebServerFragment extends Fragment {

    private Context mContext;
    private MainActivity activity;
    private Button start_server_btn;
    private TextView network_status_text;
    private TextView network_address_text;
    private TextView tips_text;
    private ImageView network_status_img;
    public static String serverIp;
    private boolean wifiApEnabled;
    private static final String TAG = "WebServerFragment";
    private WebService.ServerBinder binder=null;
    private ServiceConnection connection=new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            binder= (WebService.ServerBinder) service;
            if (binder.isAlive()){
               showServerStart();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("SetTextI18n")
    private void showServerStart(){

        tips_text.setText(R.string.tips_text_open);
        network_address_text.setVisibility(View.VISIBLE);
//        if(isWifiApEnabled()){
//            network_address_text.setText(getResources().getString(R.string.wifi_ap_address_text)+":"+binder.getListeningPort());
//        }else {
//            network_address_text.setText("http://"+(Objects.requireNonNull(NetUtils.getLocalIPAddress())).getHostAddress()+":"+binder.getListeningPort());
//        }
        network_address_text.setText("http://"+serverIp+":"+binder.getListeningPort());
        start_server_btn.setText(R.string.server_close_btn_text);
    }
    private void showServerClose(){
        start_server_btn.setText(R.string.server_start_btn_text);
        network_address_text.setVisibility(View.GONE);
        tips_text.setText(R.string.tips_text_off);
    }
    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_webserver, container, false);
        mContext = this.getContext();
//     让MainActivity持有引用
        activity = (MainActivity) getActivity();
        activity.setWebServerFragment(this);


        Intent intent = new Intent(mContext, WebService.class);
        mContext.bindService(intent, connection, BIND_AUTO_CREATE);
        mContext.startService(intent);
        initView(root);
        start_server_btn.setOnClickListener(v -> {
            if (binder.isAlive()){
               stopServer();
            }else {

                try {
                    binder.startServer();
                } catch (ServerException e) {
                    final AlertDialog.Builder normalDialog =
                            new AlertDialog.Builder(mContext);
                    normalDialog.setTitle("服务器启动出错");
                    normalDialog.setMessage(e.getLocalizedMessage());

                    normalDialog.setNegativeButton("关闭",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    normalDialog.show();
                    return;
                }
//                服务器启动后改变提示文字
               showServerStart();
            }

        });

        return root;
    }

    @Override
    public void onDestroy() {
        mContext.unbindService(connection);
         super.onDestroy();
    }

    private void initView(View root) {
        start_server_btn = root.findViewById(R.id.start_server_btn);
        network_status_text = root.findViewById(R.id.network_status_text);
        network_address_text=root.findViewById(R.id.network_address_text);
        tips_text = root.findViewById(R.id.tips_text);
        network_status_img = root.findViewById(R.id.network_status_img);
    }

    private boolean isWifiApEnabled() {
        return wifiApEnabled;
    }

    public void setWifiApEnabled(boolean wifiApEnabled) {
        this.wifiApEnabled = wifiApEnabled;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


     switch (item.getItemId()){
         case R.id.web_settings:
             this.startActivity(new Intent(mContext, WebSettingsActivity.class));
             break;
         case R.id.re_ui:
             File file = new File(mContext.getFilesDir(), "WebUI");
             AssetManager assets = mContext.getAssets();

             try {
                 ZipUtils.UnZipFolder(assets.open("WebUI.zip"), file.getAbsolutePath());
             } catch (IOException e) {
                 Log.e(TAG, "解压资源出错" + e);
                 Toast.makeText(mContext, "解压资源出错", Toast.LENGTH_SHORT).show();
             }
             break;

     }
        return super.onOptionsItemSelected(item);
    }
    //    当启用wifi时调用
    public void onWifiOpen() {
        serverIp= Objects.requireNonNull(NetUtils.getLocalIPAddress()).getHostAddress();
        network_status_text.setText(R.string.wifi_open);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            network_status_img.getDrawable().setTint(mContext.getColor(R.color.colorPrimary));
        } else {
            network_status_img.getDrawable().setTint(mContext.getResources().getColor(R.color.colorPrimary));
        }
        tips_text.setText(R.string.tips_text_off);
        start_server_btn.setText(R.string.server_start_btn_text);
        start_server_btn.setEnabled(true);
    }

    //    当wifi关闭时调用
    public void onWifiClose() {
        if (binder!=null) stopServer();
        if (isWifiApEnabled()) return;
        network_status_text.setText(R.string.network_close);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            network_status_img.getDrawable().setTint(mContext.getColor(R.color.black));
        } else {
            network_status_img.getDrawable().setTint(mContext.getResources().getColor(R.color.black));
        }
//        tips_text.setText("");

        start_server_btn.setText(R.string.server_start_btn_text);
        start_server_btn.setEnabled(false);
    }

    //    当启用wifi热点时调用
    public void onWifiApOpen() {
        serverIp="192.168.43.1";
        network_status_text.setText(R.string.wifi_ap_open);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            network_status_img.getDrawable().setTint(mContext.getColor(R.color.colorPrimary));
        } else {
            network_status_img.getDrawable().setTint(mContext.getResources().getColor(R.color.colorPrimary));
        }
        tips_text.setText(R.string.tips_text_off);
        start_server_btn.setText(R.string.server_start_btn_text);
        start_server_btn.setEnabled(true);
    }

    //    当关闭wifi热点时调用
    public void onWifiApClose() {

        if (binder!=null) stopServer();

        this.onWifiClose();
    }
    private void stopServer(){
        binder.stopServer();
       showServerClose();
    }


}