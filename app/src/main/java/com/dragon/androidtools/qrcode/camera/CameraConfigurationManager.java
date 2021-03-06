package com.dragon.androidtools.qrcode.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.dragon.androidtools.qrcode.activity.PreferencesActivity;
import com.dragon.androidtools.qrcode.open.CameraFacing;
import com.dragon.androidtools.qrcode.open.OpenCamera;
import com.dragon.androidtools.qrcode.utils.CameraConfigurationUtils;

import static java.lang.Boolean.FALSE;

/**
 * Created by dragon on 2017/9/29 0029
 * 用来处理读取,解析从相机里获取的数据,以及设置相机的参数,和相机的硬件配置
 */
@SuppressWarnings("deprecation")
public final class CameraConfigurationManager {
    private static final String TAG="CameraConfiguration";
    private final Context context;
    private int cwNeededRotation;
    private int cwRotationFromDisplayToCamera;
    private Point screenResolution;
    private Point cameraResolution;
    private Point bestPreviewSize;
    private Point previewSizeOnScreen;
    public CameraConfigurationManager(Context context){
        this.context=context;
    }


    /**
     * 屏幕的分辨率和相机分辨率的调整，使相机的预览镜头和屏幕的分辨率对应
     * 和寻找最佳的预览宽高
     * 如果摄像头生成的预览图片宽高比和手机屏幕像素宽高比（准确地说是和相机预览屏幕宽高比）不一样的话，投影的结果肯定就是图片被拉伸。
     * @param camera
     */
    public void initFromCameraParameters(OpenCamera camera){

        Camera.Parameters parameters=camera.getCamera().getParameters();
        WindowManager manager=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display=manager.getDefaultDisplay();

        int displayRotation = display.getRotation();
        int cwRotationFromNaturalToDisplay;
        switch (displayRotation) {
            case Surface.ROTATION_0:
                cwRotationFromNaturalToDisplay = 0;
                break;
            case Surface.ROTATION_90:
                cwRotationFromNaturalToDisplay = 90;
                break;
            case Surface.ROTATION_180:
                cwRotationFromNaturalToDisplay = 180;
                break;
            case Surface.ROTATION_270:
                cwRotationFromNaturalToDisplay = 270;
                break;
            default:
                // Have seen this return incorrect values like -90
                if (displayRotation % 90 == 0) {
                    cwRotationFromNaturalToDisplay = (360 + displayRotation) % 360;
                } else {
                    throw new IllegalArgumentException("Bad rotation: " + displayRotation);
                }
        }
        Log.i(TAG, "Display at: " + cwRotationFromNaturalToDisplay);

        int cwRotationFromNaturalToCamera = camera.getOrientation();
        Log.i(TAG, "Camera at: " + cwRotationFromNaturalToCamera);

        // Still not 100% sure about this. But acts like we need to flip this:
        if (camera.getFacing() == CameraFacing.FRONT) {
            cwRotationFromNaturalToCamera = (360 - cwRotationFromNaturalToCamera) % 360;
            Log.i(TAG, "Front camera overriden to: " + cwRotationFromNaturalToCamera);
        }


        cwRotationFromDisplayToCamera =(360 + cwRotationFromNaturalToCamera - cwRotationFromNaturalToDisplay) % 360;
        Log.i(TAG, "Final display orientation: " + cwRotationFromDisplayToCamera);
        if (camera.getFacing() == CameraFacing.FRONT) {
            Log.i(TAG, "Compensating rotation for front camera");
            cwNeededRotation = (360 - cwRotationFromDisplayToCamera) % 360;
        } else {
            cwNeededRotation = cwRotationFromDisplayToCamera;
        }
        Log.i(TAG, "Clockwise rotation from display to camera: " + cwNeededRotation);

        Point theScreenResolution = new Point();
        display.getSize(theScreenResolution);
        screenResolution = theScreenResolution;
        Log.i(TAG, "Screen resolution in current orientation: " + screenResolution);
        cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, screenResolution);
        Log.i(TAG, "Camera resolution: " + cameraResolution);
        bestPreviewSize = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, screenResolution);
        Log.i(TAG, "Best available preview size: " + bestPreviewSize);

        boolean isScreenPortrait = screenResolution.x < screenResolution.y;
        boolean isPreviewSizePortrait = bestPreviewSize.x < bestPreviewSize.y;

        if (isScreenPortrait == isPreviewSizePortrait) {
            previewSizeOnScreen = bestPreviewSize;
        } else {
            previewSizeOnScreen = new Point(bestPreviewSize.y, bestPreviewSize.x);
        }
        Log.i(TAG, "Preview size on screen: " + previewSizeOnScreen);
    }


    /**
     * 设置相机的一些参数配置
     * @param camera
     * @param safeMode
     */
    public void setDesiredCameraParameters(OpenCamera camera,boolean safeMode){

        Camera theCamera= camera.getCamera();
        Camera.Parameters parameters=theCamera.getParameters();
        if(null==parameters){
            Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

        if(safeMode){
            Log.w(TAG, "In camera config safe mode -- most settings will not be honored");
        }

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(context);
        initializeTorch(parameters,prefs,safeMode);
        CameraConfigurationUtils.setFocus(parameters
                ,prefs.getBoolean(PreferencesActivity.KEY_AUTO_FOCUS,true)
                ,prefs.getBoolean(PreferencesActivity.KEY_DISABLE_CONTINUOUS_FOCUS,true)
                ,safeMode);
        if(!safeMode){
            if(prefs.getBoolean(PreferencesActivity.KEY_INVERT_SCAN,false)){
                CameraConfigurationUtils.setInvertColor(parameters);
            }

            if(!prefs.getBoolean(PreferencesActivity.KEY_DISABLE_BARCODE_SCENE_MODE,true)){
                CameraConfigurationUtils.setBarcodeSceneMode(parameters);
            }
            if(!prefs.getBoolean(PreferencesActivity.KEY_DISABLE_METERING,true)){
                CameraConfigurationUtils.setVideoStabilization(parameters);
                CameraConfigurationUtils.setFocusArea(parameters);
                CameraConfigurationUtils.setMetering(parameters);
            }

        }

        parameters.setPreviewSize(bestPreviewSize.x,bestPreviewSize.y);
        theCamera.setParameters(parameters);
        theCamera.setDisplayOrientation(cwRotationFromDisplayToCamera);

        Camera.Parameters afterParameters=theCamera.getParameters();
        Camera.Size afterSize=afterParameters.getPreviewSize();
        if(null!=afterSize&&(bestPreviewSize.x!=afterSize.width||bestPreviewSize.y!=afterSize.height)){
            Log.w(TAG, "Camera said it supported preview size " + bestPreviewSize.x + 'x' + bestPreviewSize.y +
                    ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);

            bestPreviewSize.x=afterSize.width;
            bestPreviewSize.y=afterSize.height;
        }

    }


    /**
     * 初始化手电筒，也就是闪光灯
     * @param parameters
     * @param prefs
     * @param safeMode
     */
    private void initializeTorch(Camera.Parameters parameters,SharedPreferences prefs,boolean safeMode){
        boolean currentSetting =FrontLightMode.readPref(prefs)==FrontLightMode.ON;
        doSetTorch(parameters,currentSetting,safeMode);


    }


    /**
     * 设置手电筒
     * @param parameters
     * @param newSetting
     * @param safeMode
     */
    private void doSetTorch(Camera.Parameters parameters,boolean newSetting,boolean safeMode){

        CameraConfigurationUtils.setTorch(parameters,newSetting);
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(context);
        if(!safeMode&&!prefs.getBoolean(PreferencesActivity.KEY_DISABLE_EXPOSURE,true)){
            CameraConfigurationUtils.setBestExposure(parameters,newSetting);
        }
    }


    /**
     * 返回相机闪光灯的状态
     * @param camera
     * @return
     */
    public boolean getTorchState(Camera camera){
        if(camera!=null){
            Camera.Parameters parameters=camera.getParameters();
            if(parameters!=null){
                String flashMode=parameters.getFlashMode();
                return flashMode != null &&
                        (Camera.Parameters.FLASH_MODE_ON.equals(flashMode) || Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode));
            }
        }

        return false;
    }




    /** 最好的预览大小*/
    Point getBestPreviewSize() {
        return bestPreviewSize;
    }

    /**屏幕的预览大小*/
    Point getPreviewSizeOnScreen() {
        return previewSizeOnScreen;
    }
    /**相机图像的分辨率*/
    Point getCameraResolution() {
        return cameraResolution;
    }

    /**屏幕分辨率*/
    Point getScreenResolution() {
        return screenResolution;
    }
    /**旋转的最终角度*/
    int getCWNeededRotation() {
        return cwNeededRotation;
    }




}
