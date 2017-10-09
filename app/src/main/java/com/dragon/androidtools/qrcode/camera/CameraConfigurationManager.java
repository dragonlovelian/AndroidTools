package com.dragon.androidtools.qrcode.camera;

import android.content.Context;
import android.hardware.Camera;
import android.view.Display;
import android.view.WindowManager;

import com.dragon.androidtools.qrcode.open.OpenCamera;

/**
 * Created by dragon on 2017/9/29 0029.
 * 用来处理读取,解析从相机里获取的数据,以及设置相机的参数,和相机的硬件配置
 */
@SuppressWarnings("deprecation")
public final class CameraConfigurationManager {

    private final Context context;
    public CameraConfigurationManager(Context context){
        this.context=context;
    }


    public void initFromCameraParameters(OpenCamera camera){

        Camera.Parameters parameters=camera.getCamera().getParameters();
        WindowManager manager=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display=manager.getDefaultDisplay();


    }

}
