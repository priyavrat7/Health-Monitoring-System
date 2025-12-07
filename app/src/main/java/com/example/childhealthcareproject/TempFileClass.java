package com.example.childhealthcareproject;

import com.google.firebase.Timestamp;

import java.sql.Time;
import java.util.Map;

public class TempFileClass {
    Float current_temp, mean_temp;
    Timestamp timestamp;
    public TempFileClass() {
    }
    public TempFileClass(Map<String, Object> data) {
        this.current_temp = Utility.convObjToFloat(data.get("current_temp"));
        this.mean_temp = Utility.convObjToFloat(data.get("mean_temp"));
        this.timestamp = (Timestamp) data.get("timestamp");
    }

    public Float getMean_temp() {
        return mean_temp;
    }

    public void setMean_temp(Float mean_temp) {
        this.mean_temp = mean_temp;
    }



    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Float getCurrent_temp() {
        return current_temp;
    }

    public void setCurrent_temp(Float current_temp) {
        this.current_temp = current_temp;
    }
}
