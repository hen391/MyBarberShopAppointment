package com.example.mybarbershopappointment.Models;

public class Schedule {
    private String id;
    private String date;
    private String openTime;
    private String closeTime;
    private boolean isDefault;
    private String dayOfWeek;

    public Schedule() {}

    public Schedule(String id, String date, String openTime, String closeTime, boolean isDefault, String dayOfWeek) {
        this.id = id;
        this.date = date;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isDefault = isDefault;
        this.dayOfWeek = dayOfWeek;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }

    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    // פונקציות עזר
    public boolean isOverlapping(Schedule other) {
        return this.date.equals(other.date) &&
                !(this.closeTime.compareTo(other.openTime) <= 0 || this.openTime.compareTo(other.closeTime) >= 0);
    }

    public String getFormattedSchedule() {
        return String.format("%s (%s): %s - %s%s", date, dayOfWeek, openTime, closeTime, isDefault ? " (Default)" : "");
    }

    public static Schedule getDefaultSchedule(String dayOfWeek) {
        switch (dayOfWeek.toLowerCase()) {
            case "friday":
                return new Schedule(null, null, "08:00", "15:00", true, dayOfWeek);
            case "saturday":
                return new Schedule(null, null, "00:00", "00:00", true, dayOfWeek);
            default:
                return new Schedule(null, null, "09:00", "18:00", true, dayOfWeek);
        }
    }
}