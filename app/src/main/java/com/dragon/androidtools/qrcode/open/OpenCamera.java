package com.dragon.androidtools.qrcode.open;

import android.hardware.Camera;

/**
 * Created by dragon on 2017/9/29 0029.
 * 代表一个打开相机和他的元数据，比如摄像头的前后，方向
 */

public final class OpenCamera {

    private final int index;
    private final Camera camera;
    private final CameraFacing facing;
    private final int orientation;

    public OpenCamera(int index,Camera camera,CameraFacing facing,int orientation){
        this.index=index;
        this.camera=camera;
        this.facing=facing;
        this.orientation=orientation;

    }

    public Camera getCamera(){
        return camera;
    }

    /**
     * 返回设置摄像头是前置还是后置的enum
     * @return
     */
    public CameraFacing getFacing(){
        return facing;
    }

    public int getOrientation(){
        return orientation;
    }


    @Override
    public String toString() {
        return "Camera #" + index + " : " + facing + ',' + orientation;
    }




}
