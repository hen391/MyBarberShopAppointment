package com.example.mybarbershopappointment.Repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.mybarbershopappointment.Models.Schedule;
import com.google.firebase.firestore.*;
import java.util.*;

public class ScheduleRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference scheduleRef = db.collection("schedules");

    public LiveData<List<Schedule>> getSchedule() {
        MutableLiveData<List<Schedule>> scheduleLiveData = new MutableLiveData<>();
        scheduleRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Schedule> scheduleList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Schedule schedule = document.toObject(Schedule.class);
                    schedule.setId(document.getId()); // Ensure Schedule class has getId() and setId()
                    scheduleList.add(schedule);
                }
                scheduleLiveData.postValue(scheduleList);
            }
        });
        return scheduleLiveData;
    }

    public void saveSchedule(Schedule schedule) {
        if (schedule != null && schedule.getId() != null && !schedule.getId().isEmpty()) {
            scheduleRef.document(schedule.getId()).set(schedule);
        } else {
            scheduleRef.add(schedule).addOnSuccessListener(documentReference -> {
                schedule.setId(documentReference.getId());
                scheduleRef.document(documentReference.getId()).set(schedule);
            });
        }
    }

    public void saveDefaultScheduleForWeek() {
        String[] days = {"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};
        for (String day : days) {
            Schedule defaultSchedule = Schedule.getDefaultSchedule(day);
            defaultSchedule.setId(day); // Set default ID for each day
            scheduleRef.document(day).set(defaultSchedule);
        }
    }
}
