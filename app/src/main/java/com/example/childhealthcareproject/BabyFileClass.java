package com.example.childhealthcareproject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

public class BabyFileClass implements Parcelable {
    String babyFirstName, babyLastName, motherName, fatherName, dateOfBirth, gender, phoneNo, address, email;
    float weight, height;


    public BabyFileClass() {

    }

    public BabyFileClass(String babyFirstName, String babyLastName, String motherName, String fatherName, String dateOfBirth, String gender, String phoneNo, String address, String email, float weight, float height) {
        this.babyFirstName = babyFirstName;
        this.babyLastName = babyLastName;
        this.motherName = motherName;
        this.fatherName = fatherName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNo = phoneNo;
        this.address = address;
        this.email = email;
        this.weight = weight;
        this.height = height;
    }
    public BabyFileClass(Map<String, Object> data){
        this.babyFirstName = data.get("babyFirstName").toString();
        this.babyLastName = data.get("babyLastName").toString();
        this.motherName = data.get("motherName").toString();
        this.fatherName = data.get("fatherName").toString();
        this.dateOfBirth = data.get("dateOfBirth").toString();
        this.gender = data.get("gender").toString();
        this.phoneNo = data.get("phoneNo").toString();
        this.address = data.get("address").toString();
        this.email = data.get("email").toString();
        this.weight = Utility.convObjToFloat(data.get("weight"));
        this.height = Utility.convObjToFloat(data.get("height"));
    }

    protected BabyFileClass(Parcel in) {
        babyFirstName = in.readString();
        babyLastName = in.readString();
        motherName = in.readString();
        fatherName = in.readString();
        dateOfBirth = in.readString();
        gender = in.readString();
        phoneNo = in.readString();
        address = in.readString();
        email = in.readString();
        weight = in.readFloat();
        height = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(babyFirstName);
        dest.writeString(babyLastName);
        dest.writeString(motherName);
        dest.writeString(fatherName);
        dest.writeString(dateOfBirth);
        dest.writeString(gender);
        dest.writeString(phoneNo);
        dest.writeString(address);
        dest.writeString(email);
        dest.writeFloat(weight);
        dest.writeFloat(height);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BabyFileClass> CREATOR = new Creator<BabyFileClass>() {
        @Override
        public BabyFileClass createFromParcel(Parcel in) {
            return new BabyFileClass(in);
        }

        @Override
        public BabyFileClass[] newArray(int size) {
            return new BabyFileClass[size];
        }
    };

    public String getBabyFirstName() {
        return babyFirstName;
    }

    public void setBabyFirstName(String babyFirstName) {
        this.babyFirstName = babyFirstName;
    }

    public String getBabyLastName() {
        return babyLastName;
    }

    public void setBabyLastName(String babyLastName) {
        this.babyLastName = babyLastName;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
