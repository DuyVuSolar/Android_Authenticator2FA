package cn.ezandroid.lib.ezfilter.camera;

import android.hardware.camera2.CameraDevice;
import android.util.Size;

import cn.ezandroid.lib.ezfilter.EZFilter;
import cn.ezandroid.lib.ezfilter.core.FBORender;
import cn.ezandroid.lib.ezfilter.core.FilterRender;
import cn.ezandroid.lib.ezfilter.core.environment.IFitView;
import cn.ezandroid.lib.ezfilter.extra.IAdjustable;

/**
 * 摄像头处理构造器
 *
 * @author like
 * @date 2017-09-15
 */
public class Camera2Builder extends EZFilter.Builder {

    private CameraDevice mCameraDevice;
    private Size mPreviewSize;

    private Camera2Input mCamera2Input;
    private CameraBuilder.SurfaceCallback mCallback;

    public Camera2Builder(CameraDevice camera2, Size size, CameraBuilder.SurfaceCallback callback) {
        mCameraDevice = camera2;
        mPreviewSize = size;
        mCallback = callback;
    }

    @Override
    public FBORender getStartPointRender(IFitView view) {
        if (mCamera2Input == null) {
            mCamera2Input = new Camera2Input(view, mCameraDevice, mPreviewSize, mCallback);
        }
        return mCamera2Input;
    }

    @Override
    public float getAspectRatio(IFitView view) {
        return mPreviewSize.getHeight() * 1.0f / mPreviewSize.getWidth();
    }

    @Override
    public Camera2Builder addFilter(FilterRender filterRender) {
        return (Camera2Builder) super.addFilter(filterRender);
    }

    @Override
    public <T extends FilterRender & IAdjustable> Camera2Builder addFilter(T filterRender, float progress) {
        return (Camera2Builder) super.addFilter(filterRender, progress);
    }

    @Override
    public Camera2Builder enableRecord(String outputPath, boolean recordVideo, boolean recordAudio) {
        return (Camera2Builder) super.enableRecord(outputPath, recordVideo, recordAudio);
    }
}
