package com.example.smartpillboxapp;

public class Pill {
    private String pillName;
    private String pillAmount;
    private String pillTime;

    public Pill(String pillName, String pillAmount, String pillTime){
        this.pillName = pillName;
        this.pillAmount = pillAmount;
        this.pillTime = pillTime;
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
}
