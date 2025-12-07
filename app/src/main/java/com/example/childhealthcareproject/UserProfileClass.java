package com.example.childhealthcareproject;

import android.util.Log;

import java.util.ArrayList;

public class UserProfileClass {

    String emailAddress, userType, userId;

    //ArrayList<String> babyIDs;

    public UserProfileClass(String emailAddress, String userType, String userId) {
        this.emailAddress = emailAddress;
        this.userType = userType;
        this.userId = userId;
        //this.babyIDs = new ArrayList<>();
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }
/*
    public void addBabyID(String babyID) {
        this.babyIDs.add(babyID);
    }

    public void removeBabyID(String babyID) {
        boolean success = false;
        success = this.babyIDs.remove(babyID);
        if(!success) {
            Log.d("UserProfileClass", "BabyID removal is unsuccessful");
        }
    }
*/
}
