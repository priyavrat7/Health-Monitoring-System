package com.example.childhealthcareproject;


import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParentTempActivity extends AppCompatActivity {
    private static final String TAG = "DoctorTempActivity";
    private LineChart tempChart;
    private CollectionReference tempCollection;
    private int color_green, color_red;
    private String babyID, timeUnitKeepDataStr, timeUnitUploadPeriodStr;
    private String prevDate = "";
    private Button recordButton, scanConnectButton;
    private boolean isRecording;
    private long uploadPeriod, keepDataPeriod;
    private BluetoothDevice bleDevice;
    private EditText uploadPeriodEditText, keepDataEditText;
    private TextView accXtextView, accYtextView, accZtextView, tempTextView;

    private static final String IS_RECORDING_KEY = "isRecording";
    private static final String BLE_DEVICE_KEY = "bleDevice";
    private static final String UPLOAD_PERIOD_KEY = "uploadPeriod";
    private static final String KEEP_DATA_PERIOD_KEY = "keepDataPeriod";
    private Spinner timeUnitKeepDataSpinner, timeUnitUploadPeriodSpinner;
    private String[] timeUnitsArray;
    private boolean timeUnitUploadPeriodChosen, timeUnitKeepDataChosen;
    private LinearLayout uploadPeriodLayout, keepDataLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_temp);

        timeUnitUploadPeriodChosen = false;
        timeUnitKeepDataChosen = false;

        // Check if there's a saved instance state
        if (savedInstanceState != null) {
            // Restore countSteps and isRecording from saved state
            isRecording = savedInstanceState.getBoolean(IS_RECORDING_KEY);
            if(savedInstanceState.getParcelable(BLE_DEVICE_KEY) != null){
                bleDevice = savedInstanceState.getParcelable(BLE_DEVICE_KEY);
            }
            uploadPeriod = savedInstanceState.getInt(UPLOAD_PERIOD_KEY);
            keepDataPeriod = savedInstanceState.getInt(KEEP_DATA_PERIOD_KEY);
        } else {
            uploadPeriod = 5; // Default = 5 seconds
            keepDataPeriod = 30; // Default [seconds]
        }

        // Initialize colors
        color_green = ContextCompat.getColor(this, R.color.green);
        color_red = ContextCompat.getColor(this, R.color.red);

        // Initialize time unit spinners for user to choose
        timeUnitKeepDataSpinner = findViewById(R.id.time_unit_keep_data_spinner);
        timeUnitUploadPeriodSpinner = findViewById(R.id.time_unit_upload_period_spinner);
        timeUnitsArray = getResources().getStringArray(R.array.time_units);

        timeUnitUploadPeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = parentView.getItemAtPosition(position).toString();

                // Check if the userInput is in the array
                for (String timeUnitElement : timeUnitsArray) {
                    if ((selectedItem.equals(timeUnitElement)) && (!selectedItem.equals("Select Time Unit"))) {
                        timeUnitUploadPeriodChosen = true;
                        updateRecordButtonVisibility();
                        break; // Break out of the loop once a match is found
                    }
                }

                if(timeUnitUploadPeriodChosen){
                    Toast.makeText(getApplicationContext(), "Selected item: " + selectedItem, Toast.LENGTH_SHORT).show();
                    timeUnitUploadPeriodStr = selectedItem;
                } else {
                    Toast.makeText(getApplicationContext(), "Need to choose time unit for Upload Period!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void
            onNothingSelected(AdapterView<?> parentView) {
                Toast.makeText(getApplicationContext(), "Need to choose time unit for Upload Period!", Toast.LENGTH_SHORT).show();
            }
        });

        timeUnitKeepDataSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = parentView.getItemAtPosition(position).toString();

                // Check if the userInput is in the array
                for (String timeUnitElement : timeUnitsArray) {
                    if ((selectedItem.equals(timeUnitElement)) && (!selectedItem.equals("Select Time Unit"))) {
                        timeUnitKeepDataChosen = true;
                        updateRecordButtonVisibility();
                        break; // Break out of the loop once a match is found
                    }
                }

                if(timeUnitKeepDataChosen){
                    Toast.makeText(getApplicationContext(), "Selected item: " + selectedItem, Toast.LENGTH_SHORT).show();
                    timeUnitKeepDataStr = selectedItem;
                } else {
                    Toast.makeText(getApplicationContext(), "Need to choose time unit for Keep Data Period!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void
            onNothingSelected(AdapterView<?> parentView) {
                Toast.makeText(getApplicationContext(), "Need to choose time unit for Keep Data Period!", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize TextViews
        accXtextView = findViewById(R.id.accX);
        accYtextView = findViewById(R.id.accY);
        accZtextView = findViewById(R.id.accZ);
        tempTextView = findViewById(R.id.tempView);

        // Initialize buttons
        recordButton = findViewById(R.id.startRecordingButton);
        scanConnectButton = findViewById(R.id.scanConnectButton);

        // Initialize Linear Layouts
        uploadPeriodLayout = findViewById(R.id.period_upload_layout);
        keepDataLayout = findViewById(R.id.keep_data_layout);

        // Initialize EditText
        uploadPeriodEditText = findViewById(R.id.upload_period_edit_text);
        uploadPeriodEditText.setText(String.valueOf(uploadPeriod));

        keepDataEditText = findViewById(R.id.keep_data_edit_text);
        keepDataEditText.setText(String.valueOf(keepDataPeriod));

        // Initialize line chart of temperature data
        tempChart = findViewById(R.id.temp_chart);
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 0));
        LineDataSet dataSet = new LineDataSet(entries, "Temperature Data");
        dataSet.setColor(color_green);
        dataSet.setCircleColor(color_green);
        LineData lineData = new LineData(dataSet);
        tempChart.setData(lineData);
        tempChart.getDescription().setEnabled(false);
        tempChart.invalidate(); // Refresh the chart

        // Get babyID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        babyID = currentUser.getUid();
        tempCollection = Utility.getTempCollectionReference(babyID);

        setupFirestoreListener();
        fetchTemperatureData();

        // Get BLE device
        Intent intent = getIntent();
        if(intent.hasExtra("BleDevice")){
            bleDevice = intent.getParcelableExtra("BleDevice");
            Log.d(TAG, "getBLEdevice: done");
        }

        if(bleDevice == null){
            recordButton.setVisibility(View.GONE); // Need to connect BLE device before can click this button
            scanConnectButton.setText("Connect BLE Device");
            uploadPeriodLayout.setVisibility(View.GONE);
            keepDataLayout.setVisibility(View.GONE);
        } else {
            scanConnectButton.setText("Connect Another BLE Device");
            updateRecordButtonVisibility();
            uploadPeriodLayout.setVisibility(View.VISIBLE);
            keepDataLayout.setVisibility(View.VISIBLE);
        }

        isRecording = false;

        scanConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    // Stop services
                    stopBLEservice();
                    stopDataProcessService();
                    isRecording = false;
                }

                Intent intent = new Intent(getApplicationContext(), BLEScanActivity.class);
                startActivity(intent);
                finish();
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    // Stop services
                    stopBLEservice();
                    stopDataProcessService();

                    // Change button color and text to "Start Recording"
                    recordButton.setBackgroundColor(color_green);
                    recordButton.setText("Start Recording");

                    isRecording = false;

                } else {
                    uploadPeriod = getTimeDataFromEditText(uploadPeriodEditText, timeUnitUploadPeriodStr);
                    keepDataPeriod = getTimeDataFromEditText(keepDataEditText, timeUnitKeepDataStr);

                    Log.d(TAG, "uploadPeriod = " + uploadPeriod + ", keepDataPeriod = "+keepDataPeriod);

                    startDataProcessService();
                    startBLEservice();

                    // Change button color and text to "Stop Recording"
                    recordButton.setBackgroundColor(color_red);
                    recordButton.setText("Stop Recording");

                    isRecording = true;
                }
            }
        });

        // Update UI elements based on isRecording
        if (isRecording) {
            // Update UI for recording mode
            recordButton.setBackgroundColor(color_red);
            recordButton.setText("Stop Recording");
        } else {
            // Update UI for non-recording mode
            recordButton.setBackgroundColor(color_green);
            recordButton.setText("Start Recording");
        }
    }

    private void setupFirestoreListener() {
        tempCollection.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Listen failed.", error);
                            return;
                        }

                        if (value != null) {
                            // Data changed, update the chart
                            fetchTemperatureData();
                        }
                    }
                });
    }

    private BroadcastReceiver sensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("sensor_data")) {
                Log.d(TAG, "onReceive: sensor_data");
                // Get acceleration
                float[] values = intent.getFloatArrayExtra("sensor_data_acc");
                accXtextView.setText(String.format("x: %.2f", values[0]));
                accYtextView.setText(String.format("y: %.2f", values[1]));
                accZtextView.setText(String.format("z: %.2f", values[2]));

                // Get temperature
                tempTextView.setText(String.format("Temperature: %.2f oC", intent.getFloatExtra("sensor_data_temp", 0.0f)));
            }
        }
    };

    private void fetchTemperatureData(){
        // Fetch Baby's temperature data
        Query query = tempCollection.orderBy("timestamp", Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> tempDocuments = task.getResult().getDocuments();

                    // Convert documents to a list of your data model
                    List<Float> tempDataList = new ArrayList<>();
                    List<String> timestampTimeList = new ArrayList<>();
                    prevDate = "";
                    for (DocumentSnapshot documentSnapshot : tempDocuments) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            TempFileClass tempData = new TempFileClass(data);
                            tempDataList.add(tempData.mean_temp);

                            String timestampStr = Utility.convTimestampToString(tempData.getTimestamp());

                            // Format the minutes and seconds as MM:ss
                            String[] formattedTime = Utility.convTimestampToDateTime(tempData.getTimestamp());
                            Log.d(TAG, timestampStr + ", date = " + formattedTime[0] + ", hh:mm:ss = "+formattedTime[1]);

                            if(!formattedTime[0]. equals(prevDate)){
                                timestampTimeList.add(formattedTime[0] + "_"  + formattedTime[1]);
                                prevDate = formattedTime[0];
                            } else {
                                timestampTimeList.add(formattedTime[1]);
                            }
                        }
                    }

                    Log.d(TAG, "timestampList: " + timestampTimeList);

                    updateTempChart(tempDataList, timestampTimeList);
                } else {
                    // Handle errors
                }
            }
        });
    }

    private void updateTempChart(List<Float> tempList, List<String> timeList){
        List<Entry> entries = new ArrayList<>();

        // Assuming each entry corresponds to a timestamp or index
        for (int i = 0; i < tempList.size(); i++) {
            entries.add(new Entry(i, tempList.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Temperature Data");

        // Set the color for the line and points
        dataSet.setColor(color_green);
        dataSet.setCircleColor(color_green);

        LineData lineData = new LineData(dataSet);

        // Set x-axis labels
        XAxis xAxis = tempChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(timeList));
        xAxis.setLabelCount(timeList.size());
        xAxis.setLabelRotationAngle(90);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Set x-axis position to bottom
        xAxis.setGranularity(1f); // Set the minimum interval between labels
        xAxis.setGranularityEnabled(true); // Enable granularity
        tempChart.setData(lineData);

        // Refresh the chart
        tempChart.invalidate();
    }

    private void startBLEservice(){

        Intent serviceBleIntent = new Intent(this, BLEDataService.class);
        serviceBleIntent.putExtra("device", bleDevice);
        startService(serviceBleIntent);
    }

    private void startDataProcessService(){
        Intent serviceIntent = new Intent(this, DataProcessService.class);
        Log.d(TAG, "startDataProcessService: uploadPeriod = "+uploadPeriod);
        serviceIntent.putExtra("uploadPeriod", uploadPeriod);
        serviceIntent.putExtra("keepDataPeriod", keepDataPeriod);
        serviceIntent.putExtra("babyID", babyID);
        startService(serviceIntent);
    }

    private void stopBLEservice(){
        stopService(new Intent(this, BLEDataService.class));
    }
    private void stopDataProcessService(){
        stopService(new Intent(this, DataProcessService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("sensor_data");
        LocalBroadcastManager.getInstance(this).registerReceiver(sensorReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sensorReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_RECORDING_KEY, isRecording);
        if(bleDevice != null){
            outState.putParcelable(BLE_DEVICE_KEY, bleDevice);
        }
        outState.putLong(UPLOAD_PERIOD_KEY, uploadPeriod);
        outState.putLong(KEEP_DATA_PERIOD_KEY, keepDataPeriod);
    }

    private void updateRecordButtonVisibility(){
        if((bleDevice == null)||(!timeUnitUploadPeriodChosen)||(!timeUnitKeepDataChosen)){
            recordButton.setVisibility(View.GONE);
        } else {
            recordButton.setVisibility(View.VISIBLE);
        }
    }

     private int getTimeDataFromEditText(EditText timeEditText, String timeUnit){
         int timeCurrentUnit = Integer.parseInt(timeEditText.getText().toString());
         return Utility.convTimeToSeconds(timeCurrentUnit, timeUnit);
     }
}
