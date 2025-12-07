package com.example.childhealthcareproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DoctorTempActivity extends AppCompatActivity {
    private static final String TAG = "DoctorTempActivity";
    private LineChart tempChart;
    private CollectionReference tempCollection;
    private int color_green, color_red;
    private String babyID;
    private String prevDate = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_temp);

        // Initialize colors
        color_green = ContextCompat.getColor(this, R.color.green);
        color_red = ContextCompat.getColor(this, R.color.red);

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
        Intent intent = getIntent();
        babyID = intent.getStringExtra("babyID");
        tempCollection = Utility.getTempCollectionReference(babyID);

        setupFirestoreListener();
        fetchTemperatureData();
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
}
