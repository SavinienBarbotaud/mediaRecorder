package com.mediarecorder;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.NotificationCompat;
import com.mediarecorder.MediarecorderModule;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;

public class ForegroundService extends Service {

  private static final String TAG = "ForegroundService";
  private Notification foregroundNotification;
  private MediarecorderModule recorder;
  private Activity activityContext;
  private Context recorderContext;
  private final IBinder mBinder = new LocalBinder();

  //private CompletableFuture<Void> future;

  public ForegroundService() {}

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "onCreate method");
    this.foregroundNotification =
      new NotificationCompat.Builder(this)
        .setContentTitle("React-native mediaRecorder")
        .setContentText("Is capturing your screen")
        .setPriority(Notification.PRIORITY_MIN)
        .build();
    Log.d("Debug", "Notification is build, now starting foreground");
    try {
      startForeground(
        1,
        foregroundNotification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
      );
      Log.d("Info", "foreground started");
    } catch (Exception e) {}
  }

  @Override
  public void onStart(Intent intent, int startId) {}

  @Override
  public void onDestroy() {
    super.onDestroy();
    try {
      this.recorder.sendEvent("info", "onStop service");
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionAsString = sw.toString();
      this.recorder.sendEvent("error", "onStop service : " + exceptionAsString);
    }
  }

  /**
   * Class used for the client Binder.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {

    ForegroundService getService() {
      // Return this instance of LocalService so clients can call public methods
      return ForegroundService.this;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }
}
