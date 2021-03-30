package com.util.camerademo;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.MEDIA_MOUNTED;

/**
 * 相机控制Fragment
 * create By zhph777@gmail.com
 */
public class CameraFragment extends Fragment {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private String filePath;
    private String fileName;
    private File file;

    private ImageAnalysis imageAnalysis;
    private ImageCapture imageCapture;
    private boolean switchCamera = false;

    private int cameraID = CameraSelector.LENS_FACING_BACK;

    /**
     * 创建一个线程池
     * 2个线程
     * 最大3个线程
     * 空闲线程等待时间为0
     * Unit单位
     * 队列
     */
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            2, 3, 0,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(2));


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        filePath = bundle.getString("filePath");
        fileName = bundle.getString("fileName");
        switchCamera = bundle.getBoolean("switchCamera");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        previewView = view.findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(view.getContext()); //获取cameraProvider

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            file = new File(Environment.getExternalStorageDirectory(), filePath + "/" + fileName);
        }

        initCamera(view);
        return view;
    }


    /**
     * 初始化相机 绑定Preview
     *
     * @param view
     */
    private void initCamera(View view) {
        //检查检查 CameraProvider 可用性
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bindPreview(view, cameraProvider);
                }
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(view.getContext()));
    }

    /**
     * 选择相机并绑定生命周期和实例
     *
     * @param cameraProvider
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void bindPreview(View view, ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview
                .Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cameraID)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(view.getDisplay().getRotation())
                        .build();

        cameraProvider.unbindAll();  //绑定前解除所有绑定,防止cameraProvider绑定Lifecycle发生异常
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, imageCapture, preview);
    }

    /**
     * 图片分析
     */
    private void startAn() {
        imageAnalysis.setAnalyzer(threadPoolExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();


            }
        });
    }

    /**
     * 切换相机ID
     *
     * @return
     */
    private int switchCamera() {

        switch (cameraID){
            case CameraSelector.LENS_FACING_BACK:
                cameraID = CameraSelector.LENS_FACING_FRONT;
                break;
            case CameraSelector.LENS_FACING_FRONT:
                cameraID = CameraSelector.LENS_FACING_BACK;
                break;
        }

        return cameraID;
    }


    /**
     * 拍照
     */
    public void takePicture() {
        Log.i("CameraX", file.getPath());
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions
                        .Builder(file)
                        .build();

        imageCapture.takePicture(outputFileOptions, threadPoolExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.i("CameraX", "保存成功");
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.i("CameraX", "保存失败:" + exception);
                    }
                });
    }

}