package com.example.childhealthcareproject;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utility {
    static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    static CollectionReference getTempCollectionReference(String babyID){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        return FirebaseFirestore.getInstance().collection("Babies")
//                .document(currentUser.getUid()).collection("temps");
        return FirebaseFirestore.getInstance().collection("Babies")
                .document(babyID).collection("temps");
    }

    static CollectionReference getNoteCollectionReference(String babyID){
        return FirebaseFirestore.getInstance().collection("Babies")
                .document(babyID).collection("Notes");
    }

    // Method to convert input date string to the desired format
    static String formatDateString(String inputDate) {
        // Specify input and output date formats
        SimpleDateFormat inputFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            // Parse the input date string
            Date date = inputFormat.parse(inputDate);

            // Format the date to the desired output format
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Return the input string if parsing fails
        return inputDate;
    }

    static String convTimestampToString(Timestamp timestamp) {
        Date timeDate = timestamp.toDate();
        // Format the timestamp as a string
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return sdf.format(timeDate);
    }

    static String[] convTimestampToDateTime(Timestamp timestamp){
        Date timestampDate = timestamp.toDate();
        // Convert the Date to a Calendar for better date manipulation
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestampDate);

        // Extract time from the Calendar object
        int month = calendar.get(Calendar.MONTH) + 1; // Adding 1 to convert to 1-based month
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        // Format the month and day in the desired format
        String formattedDate = new SimpleDateFormat("MMM dd, yyyy").format(calendar.getTime());

        // Format the minutes and seconds as MM:ss
        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

        String[] result = new String[2];
        result[0] = formattedDate;
        result[1] = formattedTime;
        return result;
    }

    static float convObjToFloat(Object obj){
        String str = obj.toString();
        return Float.parseFloat(str);
    }

    static Timestamp subtractTimestamp(Timestamp initialTimestamp, long subtractTimeSecs){
        // Convert Firestore Timestamp to Date
        Date originalDate = initialTimestamp.toDate();

        // Subtract 30 seconds
        long newTimeInMillis = originalDate.getTime() - (subtractTimeSecs * 1000);

        // Create a new Date with the subtracted time
        Date newDate = new Date(newTimeInMillis);

        // Convert Date back to Firestore Timestamp
        Timestamp newTimestamp = new Timestamp(newDate);
        return newTimestamp;
    }

    static int convTimeToSeconds(int time, String time_unit){
        if(time_unit.equals("minutes")){
            return time*60;
        } else if(time_unit.equals("hours")){
            return time*60*60;
        } else if(time_unit.equals("days")){
            return time*60*60*24;
        } else {
            return time;
        }
    }
}
