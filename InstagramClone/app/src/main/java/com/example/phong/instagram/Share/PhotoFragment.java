package com.example.phong.instagram.Share;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.phong.instagram.Profile.AccountSettingActivity;
import com.example.phong.instagram.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by phong on 8/12/2017.
 */

public class PhotoFragment extends Fragment implements SurfaceHolder.Callback {

    private static final int PHOTO_FRAGMENT_NUMBER = 1;

    Camera camera;
    SurfaceView mSurfaceView;
    ImageView imgTakePhoto, imgFlashMode, imgChangeCamera,back;
    SurfaceHolder mSurfaceHolder;
    Camera.PictureCallback jpegCallBack;
    Camera.ShutterCallback mShutterCallback;
    private static  final int FOCUS_AREA_SIZE= 300;
    private boolean isFlashOn;
    Camera.Parameters params;
    public static int currentCameraId = 0;
    Animation rotate;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        mSurfaceView = (SurfaceView) view.findViewById(R.id.surfaceview);
        imgTakePhoto = (ImageView) view.findViewById(R.id.btnSave);
        imgFlashMode = (ImageView) view.findViewById(R.id.flashMode);
        imgChangeCamera = (ImageView) view.findViewById(R.id.changeCamera);
        back = (ImageView) view.findViewById(R.id.backPhoto);
        rotate = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        getCamera();
        Button();
        focusMode();
        changeCamera();
        settingImage();

        return view;
    }

    private void Button() {
        imgFlashMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFlashOn) {
                    imgFlashMode.setImageResource(R.drawable.ic_flashoff);
                    turnOffFlash();
                    Toast.makeText(getActivity(), "turn off", Toast.LENGTH_SHORT).show();
                } else {
                    imgFlashMode.setImageResource(R.drawable.ic_flashon);
                    turnOnFlash();
                    Toast.makeText(getActivity(), "turn on", Toast.LENGTH_SHORT).show();
                }
            }
        });
        imgTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraImage();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    //===============================switch camera==================================
    private void changeCamera() {
        if (Camera.getNumberOfCameras() == 1) {
            imgChangeCamera.setVisibility(View.INVISIBLE);
        } else {
            imgChangeCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    camera.stopPreview();
                    camera.release();

                    if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        imgFlashMode.setEnabled(false);
                        mSurfaceView.setEnabled(false);
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        imgChangeCamera.startAnimation(rotate);
                    } else {
                        mSurfaceView.setEnabled(true);
                        imgFlashMode.setEnabled(true);
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                        imgChangeCamera.startAnimation(rotate);
                    }
                    camera = Camera.open(currentCameraId);
                    Camera.Parameters parameters;
                    parameters = camera.getParameters();
                    if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        parameters.setRotation(90);
                    }else if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
                        parameters.setRotation(270);
                    }

                    List<Camera.Size> allPreviewSizes = parameters.getSupportedPreviewSizes();
                    Camera.Size previewSize = allPreviewSizes.get(0); // get top size
                    for (int i = 0; i < allPreviewSizes.size(); i++) {
                        if (allPreviewSizes.get(i).width > previewSize.width)
                            previewSize = allPreviewSizes.get(i);
                    }
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                    camera.setParameters(parameters);
                    setCameraDisplayOrientation(getActivity(), currentCameraId, camera);
                    try {

                        camera.setPreviewDisplay(mSurfaceHolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camera.startPreview();
                }
            });
        }
    }

    // set DisplayOrientation
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    //============================ setting flash =============================
    private void getCamera() {
        try {
            camera = Camera.open();
            params = camera.getParameters();
        } catch (Exception e) {

        }
    }

    private void turnOnFlash() {
        if (!isFlashOn) {
            params = camera.getParameters();
            List<String> flashModes = params.getSupportedFlashModes();
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            }
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;
        }

    }

    private void turnOffFlash() {

        if (isFlashOn) {
            params = camera.getParameters();
            List<String> flashModes = params.getSupportedFlashModes();
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = false;
        }
    }

    //=============================create ontouch focus =================
    private void focusMode(){
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    focusOnTouch(event);
                }
                return true;
            }
        });

    }

    private void focusOnTouch(MotionEvent event) {
        if (camera != null ) {

            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0){
                Log.i(TAG,"fancy !");
                Rect rect = calculateFocusArea(event.getX(), event.getY());

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);

                camera.setParameters(parameters);
                camera.autoFocus(mAutoFocusTakePictureCallback);
            }else {
                camera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                // do something...
                Log.i("tap_to_focus","success!");
            } else {
                // do something...
                Log.i("tap_to_focus","fail!");
            }
        }
    };

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / mSurfaceView.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / mSurfaceView.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper)+focusAreaSize/2>1000){
            if (touchCoordinateInCameraReper>0){
                result = 1000 - focusAreaSize/2;
            } else {
                result = -1000 + focusAreaSize/2;
            }
        } else{
            result = touchCoordinateInCameraReper - focusAreaSize/2;
        }
        return result;
    }

    // ======================setting image==========================
    private void settingImage() {
        jpegCallBack = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream fileOutputStream = null;
                File fileImage = getFile();
                if (!fileImage.exists() && !fileImage.mkdirs()) {
                    return;
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                String date = simpleDateFormat.format(new Date());
                String photoFile = "image" + date + ".jpg";
                String fileName = fileImage.getAbsolutePath() + "/" + photoFile;
                File picFile = new File(fileName);
                try {
                    fileOutputStream = new FileOutputStream(picFile);
                    fileOutputStream.write(data);
                    fileOutputStream.close();

                } catch (FileNotFoundException e) {

                } catch (IOException ex) {

                } finally {

                }
                if (isRootTask()) {
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmaps), fileName);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }else {
                    Intent intent = new Intent(getActivity(), AccountSettingActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmaps), fileName);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile));
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }

                refreshCamera();
                refreshGallery(picFile);

            }
        };
    }

    private boolean isRootTask() {
        if (((ShareActivity) getActivity()).getTask() == 0) {
            return true;
        } else {
            return false;
        }
    }


    private File getFile() {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(file, "CameraInsta");
    }

    private void cameraImage() {
        camera.takePicture(null, null, jpegCallBack);
    }


    private void refreshGallery(File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        getActivity().sendBroadcast(intent);
    }

    private void refreshCamera() {
        if (mSurfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {

        }
        try {
            camera.setPreviewDisplay(mSurfaceHolder);
            camera.startPreview();
        } catch (Exception e) {

        }
    }

    //=====================create surface===========================
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();

        } catch (RuntimeException e) {

        }
        Camera.Parameters parameters;
        parameters = camera.getParameters();
        parameters.setPreviewFrameRate(30);
        parameters.setPreviewFpsRange(15000, 30000);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setRotation(90);
        List<Camera.Size> allPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size previewSize = allPreviewSizes.get(0); // get top size
        for (int i = 0; i < allPreviewSizes.size(); i++) {
            if (allPreviewSizes.get(i).width > previewSize.width)
                previewSize = allPreviewSizes.get(i);
        }
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        camera.setParameters(parameters);
        setCameraDisplayOrientation(getActivity(), currentCameraId, camera);
        try {
            camera.setPreviewDisplay(mSurfaceHolder);
            camera.startPreview();
        } catch (Exception e) {

        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}
