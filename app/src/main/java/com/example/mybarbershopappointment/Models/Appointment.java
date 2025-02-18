package com.example.mybarbershopappointment.Models;

import java.io.Serializable;

import com.google.firebase.firestore.DocumentSnapshot;

public class Appointment implements Serializable {
    private transient String id;
    private String date;
    private String time;
    private String service;
    private String userId;

    public Appointment() {}

    public Appointment(String date, String time, String service, String userId) {
        this.date = date;
        this.time = time;
        this.service = service;
        this.userId = userId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
