package com.mediarecorder;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
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

  private static final String TAG = "MediaRecorder";
  private Notification foregroundNotification;
  private MediarecorderModule recorder;
  private Activity activityContext;
  private Context recorderContext;
  private final IBinder mBinder = new LocalBinder();
  private CompletableFuture<Void> futureForeground;

  private boolean isServiceStarted = false;

  //private CompletableFuture<Void> future;
  public ForegroundService() {
    Log.d(TAG, "[ForegroundService] - Without parameters");
  }

  public ForegroundService(CompletableFuture<Void> futureForeground) {
    Log.d(TAG, "[ForegroundService] - With parameters");
    this.futureForeground = futureForeground;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    //super.onCreate();
    if (this.futureForeground == null) {
      Log.d(TAG, "The future for foreground is null");
    } else {
      Log.d(TAG, "The future for foreground is ready");
    }
    Log.d(TAG, "onStartCommand method");

    try {
      /*ASK NOTIFICATION PERMISSION */
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
          "my_channel_id",
          "My Channel",
          NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager notificationManager = getSystemService(
          NotificationManager.class
        );
        notificationManager.createNotificationChannel(channel);
      }

      /*ASK NOTIFICATION PERMISSION */

      this.foregroundNotification =
        new NotificationCompat.Builder(this, "my_channel_id")
          .setContentTitle("React-native mediaRecorder")
          .setContentText("Is capturing your screen")
          .setPriority(Notification.PRIORITY_MIN)
          .setOngoing(true)
          .build();
      Log.d(TAG, "Notification is build, now starting foreground");

      this.startForeground(
          1,
          foregroundNotification,
          ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        );
      Log.d(TAG, "Foreground started !");
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionAsString = sw.toString();
      Log.d(TAG, "Foreground face an issue : ");
      Log.d(TAG, exceptionAsString);
    }
    return START_NOT_STICKY;
  }

  @Override
  public void onCreate() {
    Log.d(TAG, "onCreate ");
  }

  @Override
  public void onStart(Intent intent, int startId) {}

  @Override
  public void onDestroy() {
    super.onDestroy();
    try {
      Log.d(TAG, "ERROR ON SERVICE DESTROY");
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionAsString = sw.toString();
      Log.d(TAG, exceptionAsString);
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
