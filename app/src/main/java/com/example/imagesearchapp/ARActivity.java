package com.example.imagesearchapp;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;


public class ARActivity extends AppCompatActivity {

    //CameraCaptureSession用変数
    CameraCaptureSession mCaptureSession = null;
    //CaptureRequest用変数
    CaptureRequest mPreviewRequest = null;
    //画面にセットされたTextureView
    TextureView mTextureView;

    //CameraDeviceインスタンス用変数
    CameraDevice mCameraDevice = null;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ar);


        //CameraManagerの取得
        CameraManager mCameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
//利用可能なカメラIDのリストを取得
        String[] cameraIdList = new String[0];
        try {
            cameraIdList = mCameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
//用途に合ったカメラIDを設定
        String mCameraId = null;
        for (String cameraId : cameraIdList) {
            //カメラの向き(インカメラ/アウトカメラ)は以下のロジックで判別可能です。(今回はアウトカメラを使用します)
            CameraCharacteristics characteristics = null;
            try {
                characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            switch (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                case CameraCharacteristics.LENS_FACING_FRONT:
                    //インカメラ
                    break;
                case CameraCharacteristics.LENS_FACING_BACK:
                    //アウトカメラ
                    mCameraId = cameraId;
                    break;
                default:
            }
        }

        //CameraDeviceをオープン
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            mCameraManager.openCamera(mCameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }



    }

    //CameraDevice.StateCallback詳細
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            //接続成功時、CameraDeviceのインスタンスを保持させる
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();	//次フェーズにて説明します。
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            //接続切断時、CameraDeviceをクローズし、CameraDeviceのインスタンスをnullにする
            cameraDevice.close();
            mCameraDevice = null;
        }
        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            //エラー発生時、CameraDeviceをクローズし、CameraDeviceのインスタンスをnullにする
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    //CameraCaptureSession生成関数(前段CameraDevice.StateCallback.onOpened()より呼ばれる)
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            //バッファのサイズをプレビューサイズに設定(画面サイズ等適当な値を入れる)
            texture.setDefaultBufferSize(1080, 1920);

            Surface surface = new Surface(texture);

            // CaptureRequestを生成
            final CaptureRequest.Builder mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // CameraCaptureSessionを生成
            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            //Session設定完了(準備完了)時、プレビュー表示を開始
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // カメラプレビューを開始(TextureViewにカメラの画像が表示され続ける)
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, null, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            //Session設定失敗時
                            Log.e("dwqdfwqe","error");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


}