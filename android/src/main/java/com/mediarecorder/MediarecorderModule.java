package com.mediarecorder;

import android.Manifest;
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
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.hardware.display.DisplayManager;
import android.icu.text.DateFormat;
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
import androidx.core.app.ActivityCompat;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
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
  public boolean isRunning;

  private static String[] PERMISSIONS_STORAGE = {
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
  };

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(
      Activity activity,
      int requestCode,
      int resultCode,
      Intent data
    ) {
      //UP Start foreground on top UP
      if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
        if (initRecordingPromise != null) {
          if (resultCode == Activity.RESULT_OK) {
            //ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            try {
              mediaProjection =
                mediaProjectionManager.getMediaProjection(resultCode, data);
            } catch (Exception e) {
              catchError(e);
            }
            if (mediaProjection != null) {
              initScreenRecording();
            } else {
              initRecordingPromise.reject("");
            }
          } else {
            initRecordingPromise.reject("");
          }
        }
      }
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
    this.recorder = new MediaRecorder();
    this.isRunning = false;

    try {
      Intent intent = new Intent(
        getReactApplicationContext(),
        ForegroundService.class
      );
      Log.d("info", "Intent created");
      ComponentName componentName = getReactApplicationContext()
        .startService(intent);
      Log.d("info", "Service started");
      if (componentName == null) {
        initRecordingPromise.reject(
          "initMediaRecorder",
          "ForegroundService: Service failed to start, component is null."
        );
      }
    } catch (Exception e) {
      catchError(e);
      initRecordingPromise.reject(
        "initMediaRecorder",
        "ForegroundService: Foreground service failed to start. Exception error"
      );
    }

    if (
      ActivityCompat.checkSelfPermission(
        getCurrentActivity(),
        Manifest.permission.RECORD_AUDIO
      ) !=
      PackageManager.PERMISSION_GRANTED
    ) {
      ActivityCompat.requestPermissions(
        getCurrentActivity(),
        new String[] { Manifest.permission.RECORD_AUDIO },
        0
      );
    }
    if (
      ActivityCompat.checkSelfPermission(
        getCurrentActivity(),
        Manifest.permission.CAMERA
      ) !=
      PackageManager.PERMISSION_GRANTED
    ) {
      ActivityCompat.requestPermissions(
        getCurrentActivity(),
        new String[] { Manifest.permission.CAMERA },
        0
      );
    }

    if (
      ActivityCompat.checkSelfPermission(
        getCurrentActivity(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
      ) !=
      PackageManager.PERMISSION_GRANTED
    ) {
      ActivityCompat.requestPermissions(
        getCurrentActivity(),
        PERMISSIONS_STORAGE,
        1
      );
    }
    /*ASK SCREEN RECORD PERMISSION */
    Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
    getCurrentActivity()
      .startActivityForResult(captureIntent, SCREEN_RECORD_REQUEST_CODE);
  }

  private void initScreenRecording() {
    if (this.recorder != null) {
      /* Action listeners */
      this.recorder.setOnInfoListener(
          (MediaRecorder mr, int what, int extra) -> {
            int[] message = { what, extra };
            String messageString = Arrays.toString(message);
            this.sendMessage("info", messageString);
          }
        );
      this.recorder.setOnErrorListener(
          (MediaRecorder mr, int what, int extra) -> {
            int[] message = { what, extra };
            String messageString = Arrays.toString(message);
            this.sendMessage("error", messageString);
          }
        );

      try {
        this.recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        this.recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        this.recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        this.recorder.setVideoSize(this.width, this.height);
        //this.sendMessage("info", this.height);
        //this.sendMessage("info", this.width);
        //this.recorder.setVideoSize(1280, 960);

        this.recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        this.recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        this.recorder.setVideoEncodingBitRate(3000000);
        this.recorder.setVideoFrameRate(16);

        /* Set recording file */
        File mOutputFile = this.getOutputFile();
        this.sendMessage("info", mOutputFile.toString());
        mOutputFile.getParentFile().mkdirs();
        /* Set recording file */

        this.recorder.setOutputFile(mOutputFile.getAbsolutePath());

        this.recorder.prepare();
        this.sendMessage("info", "Prepare !");
        try {
          int screenDensity = 1000000;
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
        } catch (Exception e) {
          StringWriter sw = new StringWriter();
          e.printStackTrace(new PrintWriter(sw));
          String exceptionAsString = sw.toString();
          throw new Exception(exceptionAsString);
        }
        this.initRecordingPromise.resolve(1);
      } catch (Exception e) {
        this.sendMessage("error", "Enable to prepare media recorder");
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        this.sendMessage("error", exceptionAsString);
      }
    }
  }

  @ReactMethod
  public void start(Promise promise) {
    if (!this.isRunning) {
      try {
        if (this.recorder != null) {
          this.recorder.start();
          this.isRunning = true;
          this.sendMessage("info", "Record start");
          promise.resolve(1);
        } else {
          this.sendMessage("error", "Prepare mediaRecorder before launch");
          promise.reject("");
        }
      } catch (Exception e) {
        this.sendMessage("error", "Can't start media recorder");
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        this.sendMessage("error", exceptionAsString);
        promise.reject("");
      }
    }
  }

  @ReactMethod
  public void stop(Promise promise) {
    if (this.isRunning) {
      try {
        if (this.recorder != null) {
          Intent intent = new Intent(
            getReactApplicationContext(),
            ForegroundService.class
          );
          Log.d("info", "Intent created");
          getReactApplicationContext().stopService(intent);

          this.recorder.stop();
          this.isRunning = false;
          this.sendMessage("info", "Record stop");
          promise.resolve(1);
        } else {
          this.sendMessage("error", "Prepare mediaRecorder before launch");
          promise.reject("");
        }
      } catch (Exception e) {
        this.sendMessage("error", "Can't stop media recorder");
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        this.sendMessage("error", exceptionAsString);
        promise.reject("");
      }
    }
  }

  @ReactMethod
  public void release() {
    this.recorder.release();
    this.recorder = null;
  }

  private File getOutputFile() {
    String pattern = "yyyyMMdd_HHmmss";
    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
    String date = dateFormat.format(new Date());

    return new File(
      "/storage/emulated/0/" + //Need to replace by something more consistent, but many devices have this path
      "/Download/" +
      "RECORDING_" +
      date +
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
      this.sendMessage("error", "Cannot acces screen size");
    }
  }

  /*
   * Listeners callbacks
   */
  public void sendMessage(String type, Object message) {
    //Dispatch an event by using eventManager :
    this.reactContext.getJSModule(
        DeviceEventManagerModule.RCTDeviceEventEmitter.class
      )
      .emit(type, message);
  }

  public void catchError(Exception e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    String exceptionAsString = sw.toString();
    this.sendMessage("error", exceptionAsString);
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
