package com.example.smartpillboxapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> data = new MutableLiveData<>();
    private final MutableLiveData<int[]> numPillsLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateCalendar = new MutableLiveData<>();


    public LiveData<String> getData() {
        return data;
    }

    public void updateData(String newData) {
        data.setValue(newData);
    }
    public LiveData<int[]> getNumPills(){
        return numPillsLiveData;
    }
    public void setNumPills(Integer[] numPills){
        if (numPills == null) {
            return;
        }

        int[] primitiveNumPills = new int[numPills.length];
        for (int i = 0; i < numPills.length; i++) {
            primitiveNumPills[i] = (numPills[i] != null) ? numPills[i] : Integer.MIN_VALUE;
        }

        numPillsLiveData.setValue(primitiveNumPills);
    }

    public LiveData<Boolean> getUpdateCalendar() {
        return updateCalendar;
    }

    public void triggerCalendarUpdate() {
        updateCalendar.setValue(true); // Trigger the update
    }

    public void resetCalendarUpdate() {
        updateCalendar.setValue(false); // Reset the trigger
    }
}

