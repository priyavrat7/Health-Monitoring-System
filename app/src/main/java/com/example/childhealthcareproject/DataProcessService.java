package com.example.childhealthcareproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Map;

public class DataProcessService extends Service{
    private boolean writeToCsv = false;
    private static final String TAG = "SensorUpdateService";
    private boolean isRecording = false;

    private boolean firstData = true;

    private boolean notifyFeverCalled, notifyFallCalled;
    private NotificationCompat.Builder notifFeverBuilder, notifFallBuilder;

    private final static String notificationID = "notificationID";
    private float[] sensorData = new float[3];
    private float[] temp_acc = new float[3];
    private float temp, totalTemp, meanTemp;
    private int numDataPointsPerPeriod;
    private float tempFever = 28;
    private float accThresFall = 70;
    private CollectionReference tempCollection, tempDeleteCollection;
    private static final int num_ms_per_sec = 1000;
    private long startTime, currentTime, elapsedTime;
    private long tempUploadPeriod, keepDataPeriod;
    private final int defaultTempUploadPeriod = 1; // seconds
    private final int defaultKeepDataPeriod = 30; // seconds
    private String babyID;
    private Timestamp currentTimestamp;
    private FirebaseFirestore db;
    private boolean deleteDone;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        deleteDone = false;

        // Get upload period and babyID from intent
        if (intent != null){
            if(intent.hasExtra("uploadPeriod")) {
                tempUploadPeriod = intent.getIntExtra(String.valueOf("uploadPeriod"), defaultTempUploadPeriod)*1000; // Convert seconds to ms
                Log.d(TAG, "onStartCommand: tempUploadPeriod = "+tempUploadPeriod);
            }
            if(intent.hasExtra("keepDataPeriod")) {
                keepDataPeriod = intent.getLongExtra(String.valueOf("keepDataPeriod"), defaultKeepDataPeriod)*1000; // Convert seconds to ms
                Log.d(TAG, "onStartCommand: keepDataPeriod = " + keepDataPeriod);
            }
            if(intent.hasExtra("babyID")){
                babyID = intent.getStringExtra("babyID");
            }
        }

        db = FirebaseFirestore.getInstance();

        // Initialize temperature variables
        temp = 0;
        totalTemp = 0;
        meanTemp = 0;
        numDataPointsPerPeriod = 0;

        // Get data from BLEDataService
        IntentFilter filter = new IntentFilter();
        filter.addAction("ble-acc-update");
        filter.addAction("ble-temp_acc-update");
        LocalBroadcastManager.getInstance(this).registerReceiver(bleReceiver, filter);

        // Get CollectionReference to temperature data of the current baby
        tempCollection = Utility.getTempCollectionReference(babyID);

        // Get CollectionReference to delete old temperature data
        tempDeleteCollection = Utility.getTempCollectionReference(babyID);

        // Get system time when service is started
        startTime =  System.currentTimeMillis();
        currentTimestamp = Timestamp.now();
        Log.d(TAG, "subtract: keepDataPeriod = " + keepDataPeriod);
        deleteTempData(Utility.subtractTimestamp(currentTimestamp, keepDataPeriod/1000));

        // Notifications
        notifyFeverCalled = false;
        notifyFallCalled = false;

        // Notification Channel
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    notificationID,
                    "notification_channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel);
        }

        initializeFeverNotification ();
        initializeFallNotification ();

        return START_STICKY;
    }

    private void initializeFeverNotification (){
        // Notification Build
        notifFeverBuilder = new NotificationCompat.Builder(this, notificationID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("DANGER!")
                .setContentText("Baby is having a fever!");

        // Intent to send notification to TrackStepActivity
        Intent notificationIntent = new Intent(this, ParentActivity.class);
        Log.d(TAG, "Create contentIntent");
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        notifFeverBuilder.setContentIntent(contentIntent);
    }

    private void initializeFallNotification (){
        // Notification Build
        notifFallBuilder = new NotificationCompat.Builder(this, notificationID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("DANGER!")
                .setContentText("Baby fell!");

        // Intent to send notification to TrackStepActivity
        Intent notificationIntent = new Intent(this, ParentActivity.class);
        Log.d(TAG, "Create contentIntent");
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        notifFallBuilder.setContentIntent(contentIntent);
    }

    private final BroadcastReceiver bleReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("ble-acc-update")) {
                if(intent.hasExtra("acc_data")){
                    if(intent.getFloatArrayExtra("acc_data") != null){
                        sensorData = intent.getFloatArrayExtra("acc_data");
                        if(firstData == true){
                            Log.d(TAG, "This is the first data");

                            // Get baseline data for calibration
                            temp_acc[0] = sensorData[0];
                            temp_acc[1] = sensorData[1];
                            temp_acc[2] = sensorData[2];

                            firstData = false;
                        }
                        // In this calibration, we did some scaling
                        for (int i = 0; i < 3; i++){
                            if(temp_acc[i] != 0){
//                                sensorData[i] = sensorData[i]/ temp_acc[i] - 1;
                                sensorData[i] = sensorData[i] - temp_acc[i];
                            }
                        }


                        // Process the real-time data update
                        if (sensorData != null && sensorData.length == 3) {
                            Log.d(TAG, "Acceleration: " + sensorData[0] + ", " + sensorData[1] + ", " + sensorData[2]);
                            detectFall(sensorData);
                        }
                    } else {
                        Log.d(TAG, "onReceive: acc_data is NULL");
                    }

                    updateAccDataToActivity(sensorData);
                }
            } else if (intent.getAction().equals("ble-temp_acc-update")){
                if(intent.hasExtra("temp_data")){
                    temp = intent.getFloatExtra("temp_data", 0.0f);
                    Log.d(TAG, "onReceive: temp= " + temp + "oC");

                    totalTemp += temp;
                    numDataPointsPerPeriod += 1;

                    if(temp >= tempFever){
                        notifyFever();
                    }

                    // Get elapsed time to only send data after specific period of time
                    currentTimestamp = Timestamp.now();
                    currentTime = System.currentTimeMillis();
                    elapsedTime = currentTime - startTime;
                    Log.d(TAG, "startTime = " + startTime +", currentTime = " + currentTime);

                    if((elapsedTime > tempUploadPeriod) && (deleteDone)){
                        Log.d(TAG, "onReceive: Upload temp data to Firebase");

                        // Find mean temperature over the period
                        meanTemp = totalTemp/numDataPointsPerPeriod;

                        totalTemp = 0; // reset total temperature
                        numDataPointsPerPeriod = 0; // reset counting number of data points per period

                        startTime = currentTime; // Reset start time to count for next period of time to upload
                        // Upload temperature data to Firebase
                        TempFileClass tempFile = new TempFileClass();
                        tempFile.setCurrent_temp(temp);
                        tempFile.setMean_temp(meanTemp);

                        // Create new temps document and push to cloud
                        tempFile.setTimestamp(currentTimestamp);
                        String timestamp = Utility.convTimestampToString(currentTimestamp);

                        DocumentReference tempDoc = tempCollection.document(timestamp);
                        tempDoc.set(tempFile).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Utility.showToast(DataProcessService.this, "Temp data is uploaded successfully");
                                    Timestamp cutoffTimestamp = Utility.subtractTimestamp(currentTimestamp, keepDataPeriod/1000);
                                    Log.d(TAG, "keepDataPeriod = " + keepDataPeriod);
                                    deleteTempData(cutoffTimestamp);
                                } else{
                                    Utility.showToast(DataProcessService.this, "Temp data failed to upload");
                                }
                            }
                        });
                    } else {

                    }
                }
            }

        }
    };

    private void notifyFever(){
        Log.d(TAG, "Fever at "+ temp + " oC");

        // Check if notification has already been called
        if (!notifyFeverCalled) {
            // Build the notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notifFeverBuilder.build());

            // Set notifyGoalCalled to true to prevent repeated notifications
            notifyFeverCalled = true;
        }
    }

    private void notifyFall(){
        Log.d(TAG, "Baby fell!");

        // Check if notification has already been called
        if (!notifyFallCalled) {
            // Build the notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notifFallBuilder.build());

            // Set notifyGoalCalled to true to prevent repeated notifications
            notifyFallCalled = true;
        }
    }


    private void detectFall(float[] acc){
        float totalMag = (float) Math.sqrt(Math.pow(acc[0], 2) + Math.pow(acc[1], 2) + Math.pow(acc[2], 2));
        if (totalMag >= accThresFall){
            notifyFall();
        }
    }

    private void updateAccDataToActivity(float[] data){
        // Send data to TrackStepActivity
        Intent intentToActivity = new Intent("sensor_data");
        intentToActivity.putExtra("sensor_data_acc", data);
        intentToActivity.putExtra("sensor_data_temp", temp);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intentToActivity);
    }

    private void deleteTempData(Timestamp cutoffTimestamp){
        String cutoffTimestampStr = Utility.convTimestampToString(cutoffTimestamp);
        Log.d(TAG, "deleteTempData: cutoffTimestamp = " + cutoffTimestampStr);
        // Query documents with a timestamp older than the cutoff
        Query query = tempDeleteCollection.whereLessThan("timestamp", cutoffTimestamp);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: deleteTempData");
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, "onComplete: task.getResult()");
                        // Delete each document
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, "onComplete: after getData");
                        if (data != null) {
                            TempFileClass tempData = new TempFileClass(data);
                            // Format the minutes and seconds as MM:ss
                            String timestampStr = Utility.convTimestampToString(tempData.getTimestamp());
                            Log.d(TAG, "Deleting document with timestamp: " + timestampStr);
                            batch.delete(document.getReference());
                        } else {
                            Log.d(TAG, "onComplete: delete data is null");
                        }
                    }

                    // Commit the batch
                    batch.commit()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Documents successfully deleted
                                    Log.d(TAG, "Documents successfully deleted!");
                                    if(!deleteDone){
                                        deleteDone = true;
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle any errors that may occur
                                    Log.w(TAG, "Error deleting documents", e);
                                }
                            });
                } else {
                    // Handle errors
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bleReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(bleReceiver);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        // Stop the service
        stopSelf();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
