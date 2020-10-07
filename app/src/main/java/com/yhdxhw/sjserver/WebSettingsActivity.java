package com.yhdxhw.sjserver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.codekidlabs.storagechooser.Content;
import com.codekidlabs.storagechooser.StorageChooser;


public class WebSettingsActivity extends AppCompatActivity {

    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);


        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        this.finish();
        return super.onSupportNavigateUp();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        public final static int READ_REQUEST_CODE = 100;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.web_preferences, rootKey);
            //            检测端口是否正确输入
            findPreference("port").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    try {
                        int i = Integer.parseInt((String) newValue);
                        if (i > 65535 || i < 1) {
                            throw new Exception("端口超出范围");
                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "端口必须在1-65535以内", Toast.LENGTH_LONG).show();
                    return false;
                    }
                    return true;
                }
            });
            
//            目录的Summary 显示选择的路径
            String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();

            Preference root_preference = findPreference("root_dir");
            SharedPreferences root_sharedPreferences = root_preference.getPreferenceManager().getSharedPreferences();
            root_preference.setSummary(root_sharedPreferences.getString("root_dir",absolutePath));
        }


        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            switch (preference.getKey()) {
                case "root_dir": {

                    // 开启目录选择器
                    Content content = new Content();
                    content.setSelectLabel("选择");
                    content.setCancelLabel("取消");
                    StorageChooser chooser = new StorageChooser.Builder()
                            .withActivity(getActivity())
                            .allowCustomPath(true)
                            .setType(StorageChooser.DIRECTORY_CHOOSER)
                            .withFragmentManager(getActivity().getFragmentManager())
                            .withContent(content)
                            .skipOverview(true,Environment.getExternalStorageDirectory().getAbsolutePath())
                            .build();


                    chooser.show();

                 // 获取选择的路径
                    chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
                        @Override
                        public void onSelect(String path) {

                            SharedPreferences.Editor edit = preference.getPreferenceManager().getSharedPreferences().edit();
                            edit.putString("root_dir",path);
                            edit.apply();
                       preference.setSummary(path);


                        }
                    });
                    return false;
                }
                }
            return super.onPreferenceTreeClick(preference);
        }


    }

}