package com.example.mybarbershopappointment.Activities;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mybarbershopappointment.Models.Schedule;
import com.example.mybarbershopappointment.R;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class AdminScheduleActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private Button manageScheduleButton, resetScheduleButton, closedButton;
    private EditText openTimeInput, closeTimeInput;
    private FirebaseFirestore db;
    private CollectionReference scheduleRef;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_schedule);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        calendarView = findViewById(R.id.calendarView);
        manageScheduleButton = findViewById(R.id.buttonManageSchedule);
        resetScheduleButton = findViewById(R.id.buttonResetToDefault);
        closedButton = findViewById(R.id.buttonClosed);
        openTimeInput = findViewById(R.id.editTextOpenTime);
        closeTimeInput = findViewById(R.id.editTextCloseTime);

        db = FirebaseFirestore.getInstance();
        scheduleRef = db.collection("schedules");

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            updateHoursBasedOnDay(selectedDate);
            loadScheduleForDate(selectedDate);
        });

        manageScheduleButton.setOnClickListener(v -> {
            if (isFutureDate(selectedDate)) {
                checkForConflictingAppointments(selectedDate, () -> {
                    saveCustomSchedule(selectedDate);
                    loadScheduleForDate(selectedDate);
                });
            } else {
                Toast.makeText(this, "Cannot edit past dates", Toast.LENGTH_SHORT).show();
            }
        });
        resetScheduleButton.setOnClickListener(v -> {
            if (isFutureDate(selectedDate)) {
                deleteSchedule(selectedDate);
                loadScheduleForDate(selectedDate);
            } else {
                Toast.makeText(this, "Cannot reset past schedules", Toast.LENGTH_SHORT).show();
            }
        });
        closedButton.setOnClickListener(v -> {
            if (isFutureDate(selectedDate)) {
                db.collection("appointments").whereEqualTo("date", selectedDate).get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                Toast.makeText(this, "Cannot close barbershop on a day with existing appointments", Toast.LENGTH_LONG).show();
                            } else {
                                setBarbershopClosed(selectedDate);
                                loadScheduleForDate(selectedDate);
                            }
                        });
            } else {
                Toast.makeText(this, "Cannot reset past schedules", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkForConflictingAppointments(String date, Runnable onSuccess) {
        String newOpenTime = openTimeInput.getText().toString();
        String newCloseTime = closeTimeInput.getText().toString();

        db.collection("appointments").whereEqualTo("date", date).get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String appointmentTime = doc.getString("time");
                        if (!isWithinNewSchedule(appointmentTime, newOpenTime, newCloseTime)) {
                            Toast.makeText(this, "Existing appointments conflict with new schedule", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    onSuccess.run();
                });
    }

    private boolean isWithinNewSchedule(String appointmentTime, String openTime, String closeTime) {
        return !(openTime.equals("00:00") && closeTime.equals("00:00")) && appointmentTime.compareTo(openTime) >= 0 && appointmentTime.compareTo(closeTime) <= 0;
    }

    private boolean isFutureDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date selected = sdf.parse(date);
            Date today = new Date();
            return selected != null && !selected.before(today);
        } catch (Exception e) {
            return false;
        }
    }
    private void loadScheduleForDate(String date) {
        scheduleRef.document(date).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                Schedule schedule = document.toObject(Schedule.class);
                openTimeInput.setText(schedule.getOpenTime());
                closeTimeInput.setText(schedule.getCloseTime());
            } else {
                updateHoursBasedOnDay(date);
            }
        });
    }
    private void updateHoursBasedOnDay(String date) {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date selected = sdf.parse(date);
            calendar.setTime(selected);
            calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.FRIDAY:
                openTimeInput.setText("08:00");
                closeTimeInput.setText("15:00");
                break;
            case Calendar.SATURDAY:
                openTimeInput.setText("00:00");
                closeTimeInput.setText("00:00");
                break;
            default:
                openTimeInput.setText("09:00");
                closeTimeInput.setText("18:00");
        }
    }
    private void saveCustomSchedule(String date) {
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("date", date);
        schedule.put("openTime", openTimeInput.getText().toString());
        schedule.put("closeTime", closeTimeInput.getText().toString());
        scheduleRef.document(date).set(schedule);
        Toast.makeText(this, "New schedule saved", Toast.LENGTH_SHORT).show();

    }

    private void deleteSchedule(String date) {
        scheduleRef.document(date).delete().addOnSuccessListener(aVoid -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            updateHoursBasedOnDay(date);
            Toast.makeText(this, "Schedule reset to default based on day", Toast.LENGTH_SHORT).show();
        });
    }

    private void setBarbershopClosed(String date) {
        openTimeInput.setText("00:00");
        closeTimeInput.setText("00:00");
        saveCustomSchedule(date);
        Toast.makeText(this, "Barbershop set as closed for this day", Toast.LENGTH_SHORT).show();
    }
}
