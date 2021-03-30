package com.util.camerademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String[] permissions = new String[]{
            Manifest.permission.CAMERA,//相机权限
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


//    private final String filePath =Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+"CameraX/";
    private final String filePath ="CameraX"+getSystermTime();

    private String fileName = getSystermTime()+".jpg";
    private CameraFragment mCameraFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions(permissions);
        findViewById(R.id.takePicture).setOnClickListener(this);

        File folder = new File(Environment.getExternalStorageDirectory()+File.separator+filePath);
        if (!folder.exists()){
            folder.mkdirs();
        }

    }

    /**
     * 相机Fragment
     */

    private void startCameraFragment() {
        mCameraFragment = new CameraFragment();

        Bundle bundle = new Bundle();
        bundle.putString("filePath",filePath);
        bundle.putString("fileName",fileName);
        mCameraFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.frameLayout, mCameraFragment);
        transaction.commit();
    }

    /**
     * 检查应用所需权限
     *
     * @param permissions 应用所需权限
     */
    private void checkPermissions(String[] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) == PackageManager.PERMISSION_GRANTED) {
                startCameraFragment();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 200);

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200){
            startCameraFragment();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.takePicture:
                mCameraFragment.takePicture();
                break;
        }
    }

    private String getSystermTime(){
        SimpleDateFormat sdfTwo =new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String timeStr = sdfTwo.format(System.currentTimeMillis());
        return timeStr;
    }

}