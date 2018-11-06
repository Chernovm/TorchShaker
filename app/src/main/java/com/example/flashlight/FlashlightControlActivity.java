package com.example.flashlight;

import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.widget.Toast;

import com.squareup.seismic.ShakeDetector;

public class FlashlightControlActivity extends AppCompatActivity implements ShakeDetector.Listener {

    private static final int CAMERA_REQUEST = 127;
    private static final String TAG = FlashlightControlActivity.class.getSimpleName();

    private Button buttonToggle;
    private boolean hasCameraFlash;
    private boolean hasCameraPermission;

    private FlashlightManager flashlightManager;
    private ShakeDetector sd;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flashlightManager = new FlashlightManager(this);
        buttonToggle = (Button) findViewById(R.id.buttonToggle);

        hasCameraFlash = getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        requestCameraPermission();

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sd = new ShakeDetector(this);
        sd.setSensitivity(ShakeDetector.SENSITIVITY_LIGHT);
        sd.start(sensorManager);

        buttonToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "OnClick");
                toggleLedCommand();
            }
        });
    }

    private void toggleLedCommand() {
        if (hasCameraFlash) {
            if (!hasCameraPermission)
                requestCameraPermission();
            else
                flashlightManager.toggleFlashlight();
        } else {
            Toast.makeText(FlashlightControlActivity.this, "No flash available on your device",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        updateUi();
        flashlightManager.updateFlashLightStatus();
        super.onStart();
    }

    @Override
    protected void onStop() {
        flashlightManager.unregisterCallback();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        sd.stop();
        super.onDestroy();
    }

    public void requestCameraPermission() {
        if (!hasCameraPermission) {
            ActivityCompat.requestPermissions(FlashlightControlActivity.this,
                    new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case CAMERA_REQUEST :
                if (grantResults.length > 0  &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasCameraPermission = true;
                    updateUi();
                    flashlightManager.updateFlashLightStatus();
                } else {
                    Toast.makeText(FlashlightControlActivity.this, "Permission Denied for the Camera",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void updateUi() {
        if (buttonToggle != null)
            buttonToggle.setEnabled(hasCameraPermission);

        updateButtonAppearance();
    }

    public void updateButtonAppearance() {
        boolean status = flashlightManager.getFlashLightStatus();
        Log.d(TAG, "Update UI " + (status ? "true" : "false"));

        if (status) {
            buttonToggle.setText(R.string.disable_text);
            buttonToggle.setBackgroundColor(getResources().getColor(R.color.colorInactive));
        }
        else {
            buttonToggle.setText(R.string.enable_text);
            buttonToggle.setBackgroundColor(getResources().getColor(R.color.colorActive));
        }
    }

    public boolean getHasCameraPermission() {
        return hasCameraPermission;
    }

    @Override
    public void hearShake() {
        if (timer != null)
            return;

        timer = new CountDownTimer((flashlightManager.getFlashLightStatus() ? 750 : 1500), 150) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                timer = null;
            }

        }.start();

        toggleLedCommand();
        Log.d(TAG, "Don't shake me, bro!");
    }
}
