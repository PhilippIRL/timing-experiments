package de.ppluss.timingexperiments;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Vibrator;

public class WorkerThread extends Thread {

    private final Context context;

    public boolean shouldWork = false, singleRun = false;
    public int mode = 0;
    public int fireTime = 0, waitTime = 0;

    private Vibrator vibrator;
    private CameraManager cameraManager = null;
    private String cameraId;

    public WorkerThread(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        super.run();

        try {
            for(;;) {

                work();

                if(singleRun) {
                    singleRun = false;
                }

                while(!shouldWork && !singleRun) {
                    sleep(10);
                }

            }
        } catch (InterruptedException ignored) {}

    }

    public void stopEverything() {
        if(vibrator != null) {
            vibrator.cancel();
        }
        if(cameraManager != null) {
            try {
                cameraManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException ignored) {}
        }
    }

    private void work() throws InterruptedException {
        if(shouldWork || singleRun) fire();
        if(shouldWork) wait_();
    }

    private void fire() throws InterruptedException {

        if(fireTime == 0) {
            return;
        }

        if(mode == 0) {
            if(vibrator == null) {
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }
            vibrator.vibrate(fireTime);
            sleep(fireTime);
            vibrator.cancel();
        } else if (mode == 1) {
            try {
                if(cameraManager == null) {
                    cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                    cameraId = cameraManager.getCameraIdList()[0];
                }
                cameraManager.setTorchMode(cameraId, true);
                sleep(fireTime);
                cameraManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException | NullPointerException ignored) {}
        }

    }

    private void wait_() throws InterruptedException {
        if(waitTime != 0) {
            sleep(waitTime);
        }
    }

}
