package com.example.childhealthcareproject;

import android.util.Log;

import java.util.ArrayList;

public class UserDoctorClass {

    String emailAddress, userType, userId;
    boolean isDoctorRequested, isDoctorApproved;

    public UserDoctorClass(String emailAddress, boolean isDoctorRequested, boolean isDoctorApproved, String userId, String userType) {
        this.emailAddress = emailAddress;
        this.userType = userType;
        this.userId = userId;
        this.isDoctorRequested = isDoctorRequested;
        this.isDoctorApproved = isDoctorApproved;
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

    public void setRequestStatus(boolean doctorRequest){this.isDoctorRequested = doctorRequest;}
    public boolean getRequestStatus(){return isDoctorRequested;}

    public void setApprovalStatus(boolean approvalStatus){this.isDoctorApproved = approvalStatus;}

    public boolean getApprovalStatus(){return isDoctorApproved;}

}
