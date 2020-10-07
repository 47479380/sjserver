package com.yhdxhw.sjserver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.yhdxhw.sjserver.BroadcastReceiver.WifiApBroadcast;
import com.yhdxhw.sjserver.BroadcastReceiver.WifiBroadcast;
import com.yhdxhw.sjserver.server.HttpServer;
import com.yhdxhw.sjserver.ui.Web.WebServerFragment;
import com.yhdxhw.sjserver.utils.ZipUtils;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;
import android.view.Menu;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private WebServerFragment webServerFragment;

    private WifiApBroadcast wifiApBroadcast;
    private WifiBroadcast wifiBroadcast;
    private AppBarConfiguration mAppBarConfiguration;
    private static final String TAG = "MainActivity";
    private long exitTime = 0;

    public WebServerFragment getWebServerFragment() {
        return webServerFragment;
    }

    public void setWebServerFragment(WebServerFragment webServerFragment) {
        this.webServerFragment = webServerFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        verifyStoragePermissions(this);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        UnzipWebUi();

//        监听网络变化广播

        wifiApBroadcast = new WifiApBroadcast(this);
        this.registerReceiver(wifiApBroadcast, new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED"));
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        wifiBroadcast = new WifiBroadcast(this);
        this.registerReceiver(wifiBroadcast, filter);

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(wifiApBroadcast);
        unregisterReceiver(wifiBroadcast);
        super.onDestroy();
}

    /**
     * 检查手机上的WebUI资源和assets上的是否一致
     * @return 一致返回true 否则返回false
     */
    public boolean checkWebUIVersion(){
        AssetManager assets = getAssets();
        try {
            String hash= IOUtils.toString(new File(new File(this.getFilesDir(), "WebUI"),"WebUI.hash").toURI(),"utf-8");
            if (hash.equals(IOUtils.toString(assets.open("WebUI.hash")))){
                return true;
            }
        } catch (IOException ignored) {
        }
            return false;
    }

    /**
     * 更新WebUI资源的版本号
     */
    public void updateWebUIVersionFile(){
        final File webHashFile = new File(new File(this.getFilesDir(), "WebUI"), "WebUI.hash");
        try {
            IOUtils.copy(getAssets().open("WebUI.hash"), new FileOutputStream(webHashFile));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 解压WebUI资源
     */
    @SuppressLint("CommitPrefEdits")
    private void UnzipWebUi() {
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("解压资源中");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        File webUIFile = new File(this.getFilesDir(), "WebUI");
        HttpServer.staticPath = webUIFile.getAbsolutePath();
        AssetManager assets = getAssets();
        if (webUIFile.exists()) {
            if (checkWebUIVersion()){
                return;
            }else {
                FileUtils.deleteQuietly(webUIFile);;
            }
        }
        progressDialog.show();
        try {
            ZipUtils.UnZipFolder(assets.open("WebUI.zip"), webUIFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "解压资源出错" + e);
            Toast.makeText(this, "解压资源出错", Toast.LENGTH_SHORT).show();
            return;
        }finally {
            progressDialog.dismiss();
        }

       updateWebUIVersionFile();
        Toast.makeText(this, "解压资源成功", Toast.LENGTH_SHORT).show();
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.MANAGE_EXTERNAL_STORAGE"
    };


    public static void verifyStoragePermissions(Activity activity) {

        try {
            if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){

            }
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.MANAGE_EXTERNAL_STORAGE");
            int permission1 = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED&&permission1!=PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



}
