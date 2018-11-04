package com.example.flashlight;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

public class FlashlightManager {
    private static final String TAG = FlashlightManager.class.getSimpleName();

    private FlashlightControlActivity context;
    private CameraManager.TorchCallback torchCallback;

    private boolean flashLightStatus;

    public FlashlightManager(FlashlightControlActivity context) {
        this.context = context;
    }

    public void updateFlashLightStatus() {
        Log.d(TAG, "updateFlashLightStatus");
        if (!context.getHasCameraPermission())
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerTorchStatusCallback();
        } else {
            Camera camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            String flashMode = parameters.getFlashMode();
            flashLightStatus = flashMode.equals("Torch");
            camera.stopPreview();
        }
    }

    public void unregisterCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager manager = (CameraManager) context.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
            manager.unregisterTorchCallback(torchCallback);
            torchCallback = null;
        }
    }

    public void registerTorchStatusCallback() {
        if (torchCallback != null)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            torchCallback = new CameraManager.TorchCallback() {
                @Override
                public void onTorchModeUnavailable(String cameraId) {
                    super.onTorchModeUnavailable(cameraId);
                }

                @Override
                public void onTorchModeChanged(String cameraId, boolean enabled) {
                    super.onTorchModeChanged(cameraId, enabled);
                    Log.d(TAG, "Flashlight status callback: " + (enabled ? "true" : "false"));
                    flashLightStatus = enabled;
                    context.setButtonVisualStatus();
                }
            };

            CameraManager manager = (CameraManager) context.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
            manager.registerTorchCallback(torchCallback, null);
        }
    }

    private void flashLightOff() {
        Log.d(TAG, "Turn flashlight OFF");
        if (torchCallback == null)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Camera camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            camera.stopPreview();
        }
    }

    private void flashLightOn() {
        Log.d(TAG, "Turn flashlight ON");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, true);
            } catch (CameraAccessException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Camera camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
        }

    }

    public void toggleFlashlight() {
        if (flashLightStatus)
            flashLightOff();
        else
            flashLightOn();
    }

    public boolean getFlashLightStatus() {
        return flashLightStatus;
    }
}
