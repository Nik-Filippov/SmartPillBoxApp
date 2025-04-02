package com.example.smartpillboxapp;

import android.util.Log;

public class Pill{
    private String pillName;
    private String pillAmount;
    private String pillTime;
    private String pillRecurrence;

    public Pill(String pillName, String pillAmount, String pillTime, String pillRecurrence){
        this.pillName = pillName;
        this.pillAmount = pillAmount;
        this.pillTime = pillTime;
        this.pillRecurrence = pillRecurrence;
    }

    public String getPillName(){
        return pillName;
    }

    public String getPillAmount(){
        return pillAmount;
    }

    public String getPillTime(){
        return pillTime;
    }
    public String getPillRecurrence(){
        return pillRecurrence;
    }

    public String getName() {
        return pillName;
    }
}
