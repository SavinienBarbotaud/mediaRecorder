package com.mediarecorder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.hardware.display.DisplayManager;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.mediarecorder.ForegroundService;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ReactModule(name = MediarecorderModule.NAME)
public class MediarecorderModule extends ReactContextBaseJavaModule {

  private static final int SCREEN_RECORD_REQUEST_CODE = 101;
  private static final int NOTIFICATION_REQUEST_CODE = 102;
  public static final String NAME = "Mediarecorder";
  private MediaRecorder recorder;
  private ReactApplicationContext reactContext;
  public int width;
  public int height;
  private MediaProjectionManager mediaProjectionManager;
  private MediaProjection mediaProjection;
  private NotificationManager notificationManager;
  private Promise initRecordingPromise; //resolved on init

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(
      Activity activity,
      int requestCode,
      int resultCode,
      Intent data
    ) {
      try {
        Intent intent = new Intent(
          getReactApplicationContext(),
          ForegroundService.class
        );
        Log.d("info", "Intent created");
        ComponentName componentName = getReactApplicationContext()
          .startService(intent);
        Log.d("info", "Service started");
        /*if (componentName == null) {
          initRecordingPromise.reject(
            "initMediaRecorder",
            "ForegroundService: Foreground service failed to start."
          );
        }*/
      } catch (Exception e) {
        catchError(e);
        /*initRecordingPromise.reject(
          "initMediaRecorder",
          "ForegroundService: Foreground service failed to start."
        );*/
      }
      initRecordingPromise.resolve(1);
      //UP Start foreground on top UP
      /*
      if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
        if (initRecordingPromise != null) {
          if (resultCode == Activity.RESULT_OK) {
            //ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            try {
              //mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            } catch (Exception e) {
              catchError(e);
            }
            sendEvent("info", "COUCOU5");
            initRecordingPromise.resolve(1);
            if (mediaProjection != null) {
                initScreenRecording();
                initRecordingPromise.resolve(1);
              } else {
                initRecordingPromise.reject("");
              }
          } else {
            initRecordingPromise.reject("");
          }
        }
      }*/
    }
  };

  public MediarecorderModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.recorder = null;
    this.notificationManager =
      (NotificationManager) reactContext.getSystemService(
        Context.NOTIFICATION_SERVICE
      );
    this.mediaProjectionManager =
      (MediaProjectionManager) reactContext.getSystemService(
        Context.MEDIA_PROJECTION_SERVICE
      );
    this.getScreenSize();
    this.reactContext.addActivityEventListener(mActivityEventListener);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    promise.resolve(a * b);
  }

  @ReactMethod
  public void initMediaRecorder(Promise promise) {
    if (getCurrentActivity() == null) {
      promise.reject("NO_ACTIVITY", "Activity is not available.");
      return;
    }

    this.initRecordingPromise = promise;
    /*ASK SCREEN RECORD PERMISSION */
    Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
    getCurrentActivity()
      .startActivityForResult(captureIntent, SCREEN_RECORD_REQUEST_CODE);



    this.sendEvent(
        "info",
        "Notification state : " +
        this.notificationManager.areNotificationsEnabled()
      );
  }

  private void initScreenRecording() {
    if (this.recorder == null) {
      this.recorder = new MediaRecorder();

      /* Action listeners */
      this.recorder.setOnInfoListener(
          (MediaRecorder mr, int what, int extra) -> {
            int[] message = { what, extra };
            this.sendEvent("info", message);
          }
        );
      this.recorder.setOnErrorListener(
          (MediaRecorder mr, int what, int extra) -> {
            int[] message = { what, extra };
            this.sendEvent("error", message);
          }
        );

      try {
        int screenDensity = 320;
        Surface surface = this.recorder.getSurface();
        mediaProjection.createVirtualDisplay(
          "ScreenCapture",
          this.width,
          this.height,
          screenDensity,
          DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
          surface,
          null,
          null
        );

        this.recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        this.recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        this.recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        this.recorder.setVideoSize(this.height, this.width);

        this.recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        this.recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        this.recorder.setVideoFrameRate(20);
        this.recorder.setOrientationHint(0);

        /* Set recording file */
        File mOutputFile = this.getOutputFile();
        this.sendEvent("info", mOutputFile.toString());
        mOutputFile.getParentFile().mkdirs();
        this.sendEvent("info", "MKDIR path");
        /* Set recording file */

        this.recorder.setOutputFile(mOutputFile.getAbsolutePath());
        this.recorder.prepare();
      } catch (Exception e) {
        this.sendEvent("error", "Enable to prepare media recorder");
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        this.sendEvent("error", exceptionAsString);
      }
    }
  }

  @ReactMethod
  public void start(Promise promise) {
    this.recorder.start();
    this.sendEvent("info", "Record start");
    promise.resolve(1);
  }

  @ReactMethod
  public void stop(Promise promise) {
    this.recorder.stop();
    this.sendEvent("info", "Record stop");
    promise.resolve(1);
  }

  @ReactMethod
  public void release() {
    this.recorder.release();
    this.recorder = null;
  }

  private File getOutputFile() {
    String pattern = "yyyy-MM-dd";
    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
    return new File(
      Environment.getExternalStorageDirectory().getAbsolutePath().toString() +
      "/Download/" +
      "RECORDING_" +
      dateFormat.format(new Date()) +
      ".mp4"
    );
  }

  public void getScreenSize() {
    DisplayMetrics metrics = new DisplayMetrics();
    WindowManager windowManager = (WindowManager) this.reactContext.getSystemService(
        Context.WINDOW_SERVICE
      );
    if (windowManager != null) {
      Display display = windowManager.getDefaultDisplay();
      display.getMetrics(metrics);
      this.width = metrics.widthPixels;
      this.height = metrics.heightPixels;
    } else {
      this.sendEvent("error", "Cannot acces screen size");
    }
  }

  /*
   * Listeners callbacks
   */
  public void sendEvent(String type, Object message) {
    //Dispatch an event by using eventManager :
    /*
     * .emit("onevent", params);
     */
    this.reactContext.getJSModule(
        DeviceEventManagerModule.RCTDeviceEventEmitter.class
      )
      .emit(type, message);
  }

  public void catchError(Exception e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    String exceptionAsString = sw.toString();
    sendEvent("error", exceptionAsString);
  }

  private boolean isMyServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) this.reactContext.getSystemService(
        Context.ACTIVITY_SERVICE
      );
    for (RunningServiceInfo service : manager.getRunningServices(
      Integer.MAX_VALUE
    )) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }
}
