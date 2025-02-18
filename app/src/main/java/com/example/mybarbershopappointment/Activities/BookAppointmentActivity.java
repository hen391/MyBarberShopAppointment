package com.example.mybarbershopappointment.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mybarbershopappointment.Models.Appointment;
import com.example.mybarbershopappointment.R;
import com.example.mybarbershopappointment.ViewModels.BookAppointmentViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class BookAppointmentActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private Spinner timeSlotSpinner, serviceSpinner;
    private Button bookButton;
    private BookAppointmentViewModel viewModel;
    private String selectedDate;
    private ProgressBar progressBar;
    private Map<String, Integer> serviceDurations = new HashMap<>();
    private boolean isEditMode;
    private boolean lockService;
    private Appointment currentAppointment;

    private String selectedService;

    private static final TimeZone ISRAEL_TZ = TimeZone.getTimeZone("Asia/Jerusalem");
    private static final Map<Integer, String[]> defaultSchedule = new HashMap<>() {{
        put(Calendar.SUNDAY, new String[]{"09:00", "18:00"});
        put(Calendar.MONDAY, new String[]{"09:00", "18:00"});
        put(Calendar.TUESDAY, new String[]{"09:00", "18:00"});
        put(Calendar.WEDNESDAY, new String[]{"09:00", "18:00"});
        put(Calendar.THURSDAY, new String[]{"09:00", "18:00"});
        put(Calendar.FRIDAY, new String[]{"08:00", "15:00"});
        put(Calendar.SATURDAY, new String[]{"00:00", "00:00"});
    }};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        calendarView = findViewById(R.id.calendarView);
        timeSlotSpinner = findViewById(R.id.spinnerTimeSlots);
        serviceSpinner = findViewById(R.id.spinnerService);
        bookButton = findViewById(R.id.buttonBook);
        progressBar = findViewById(R.id.progressBar);

        viewModel = new ViewModelProvider(this).get(BookAppointmentViewModel.class);

        if (getIntent().hasExtra("is_edit")) {
            isEditMode = getIntent().getBooleanExtra("is_edit", false);
            if (getIntent().hasExtra("selected_service")) {
                selectedService = getIntent().getStringExtra("selected_service");
            }
            lockService = getIntent().getBooleanExtra("lock_service", false);
            currentAppointment = (Appointment) getIntent().getSerializableExtra("edit_appointment");
        }

        calendarView.setMinDate(System.currentTimeMillis() - 1000);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
            calendar.set(year, month, dayOfMonth);
            selectedDate = String.format("%04d-%d-%d", year, month + 1, dayOfMonth);
            loadAvailableTimeSlots(calendar);
        });

        loadServiceOptions();
        bookButton.setOnClickListener(v -> bookAppointment());
    }

    private void loadServiceOptions() {
        FirebaseFirestore.getInstance().collection("services").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> services = new ArrayList<>();
                    for (var doc : querySnapshot) {
                        String serviceName = doc.getString("name");
                        int duration = doc.getLong("duration").intValue();
                        services.add(serviceName);
                        serviceDurations.put(serviceName, duration);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, services);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    serviceSpinner.setAdapter(adapter);

                    if (isEditMode && selectedService != null) {
                        int position = services.indexOf(selectedService);
                        if (position != -1) {
                            serviceSpinner.setSelection(position);
                            serviceSpinner.setEnabled(false);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load services", Toast.LENGTH_SHORT).show());
    }

    private void bookAppointment() {
        String service = isEditMode && lockService ? currentAppointment.getService() : serviceSpinner.getSelectedItem().toString();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> appointment = new HashMap<>();
        appointment.put("date", selectedDate);
        appointment.put("time", timeSlotSpinner.getSelectedItem().toString());
        appointment.put("service", service);
        appointment.put("userId", userId);

        if (isEditMode && currentAppointment != null) {
            FirebaseFirestore.getInstance().collection("appointments")
                    .whereEqualTo("date", currentAppointment.getDate())
                    .whereEqualTo("time", currentAppointment.getTime())
                    .whereEqualTo("service", currentAppointment.getService())
                    .whereEqualTo("userId", userId)
                    .get().addOnSuccessListener(querySnapshot -> {
                        for (var doc : querySnapshot) {
                            doc.getReference().delete();
                        }
                        saveNewAppointment(appointment);
                    }).addOnFailureListener(e -> Log.e("Firestore", "Error deleting appointment", e));
        } else {
            saveNewAppointment(appointment);
        }
    }

    private void saveNewAppointment(Map<String, Object> appointment) {
        FirebaseFirestore.getInstance().collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Appointment saved successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, UserDashboardActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save appointment", Toast.LENGTH_SHORT).show());
    }

    private void loadAvailableTimeSlots(Calendar selectedDay) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("schedules").document(selectedDate).get()
                .addOnSuccessListener(document -> {
                    Log.d("FirestoreData", "Document found: " + document.getId());
                    if (document.exists()) {
                        String openTime = document.getString("openTime");
                        String closeTime = document.getString("closeTime");
                        List<String> allSlots = generateTimeSlotsForDay(openTime, closeTime);
                        allSlots.removeIf(slot -> !isWithinWorkingHours(slot, closeTime));
                        loadAppointments(selectedDay, allSlots);
                    } else {
                        Log.d("FirestoreData", "No document found with ID: " + selectedDate);
                        List<String> allSlots = generateTimeSlotsForDay(defaultSchedule.get(selectedDay.get(Calendar.DAY_OF_WEEK))[0], defaultSchedule.get(selectedDay.get(Calendar.DAY_OF_WEEK))[1]);
                        allSlots.removeIf(slot -> !isWithinWorkingHours(slot, defaultSchedule.get(selectedDay.get(Calendar.DAY_OF_WEEK))[1]));
                        loadAppointments(selectedDay, allSlots);
                    }
                }).addOnFailureListener(e -> Log.e("FirestoreError", "Failed to load schedule", e));
    }
    private void loadAppointments(Calendar selectedDay, List<String> allSlots) {
        FirebaseFirestore.getInstance().collection("appointments").whereEqualTo("date", selectedDate).get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreData", "Appointments found: " + querySnapshot.size());
                    for (var doc : querySnapshot) {
                        String time = doc.getString("time");
                        String service = doc.getString("service");
                        int duration = serviceDurations.getOrDefault(service, 30);
                        blockAllConflictingSlots(allSlots, time, duration);
                    }
                    updateTimeSlotSpinner(selectedDay, allSlots);
                });
    }

    private void updateTimeSlotSpinner(Calendar selectedDay, List<String> allSlots) {
        Calendar now = Calendar.getInstance(ISRAEL_TZ);
        allSlots.removeIf(slot -> {
            String[] parts = slot.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return selectedDay.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    selectedDay.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
                    (hour < now.get(Calendar.HOUR_OF_DAY) || (hour == now.get(Calendar.HOUR_OF_DAY) && minute < now.get(Calendar.MINUTE)));
        });
        if (allSlots.isEmpty()) {
            Toast.makeText(this, "No available time slots", Toast.LENGTH_SHORT).show();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSlotSpinner.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
    }

    private boolean isWithinWorkingHours(String slot, String closeTime) {
        int slotHour = Integer.parseInt(slot.split(":")[0]);
        int slotMinute = Integer.parseInt(slot.split(":")[1]);
        int closeHour = Integer.parseInt(closeTime.split(":")[0]);
        int closeMinute = Integer.parseInt(closeTime.split(":")[1]);
        int duration = serviceDurations.getOrDefault(serviceSpinner.getSelectedItem().toString(), 30);
        int endHour = slotHour + duration / 60;
        int endMinute = slotMinute + duration % 60;
        return (endHour < closeHour) || (endHour == closeHour && endMinute <= closeMinute);
    }

    private void blockAllConflictingSlots(List<String> slots, String startTime, int duration) {
        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int startMinute = Integer.parseInt(startTime.split(":")[1]);
        int endHour = startHour + duration / 60;
        int endMinute = startMinute + duration % 60;

        Iterator<String> iterator = slots.iterator();
        while (iterator.hasNext()) {
            String slot = iterator.next();
            int slotHour = Integer.parseInt(slot.split(":")[0]);
            int slotMinute = Integer.parseInt(slot.split(":")[1]);
            if ((slotHour + slotMinute / 60.0 + serviceDurations.getOrDefault(serviceSpinner.getSelectedItem().toString(), 30) / 60.0) > (startHour + startMinute / 60.0) &&
                    (slotHour + slotMinute / 60.0) < (endHour + endMinute / 60.0)) {
                iterator.remove();
            }
        }
    }
    private List<String> generateTimeSlotsForDay(String openTime, String closeTime) {
        List<String> slots = new ArrayList<>();
        int start = Integer.parseInt(openTime.split(":")[0]);
        int end = Integer.parseInt(closeTime.split(":")[0]);
        for (int hour = start; hour < end; hour++) {
            slots.add(String.format("%02d:00", hour));
            slots.add(String.format("%02d:30", hour));
        }
        return slots;
    }

    public static void deleteAppointment(Appointment appointment) {
        FirebaseFirestore.getInstance().collection("appointments")
                .whereEqualTo("date", appointment.getDate())
                .whereEqualTo("time", appointment.getTime())
                .whereEqualTo("service", appointment.getService())
                .whereEqualTo("userId", appointment.getUserId())
                .get().addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot) {
                        doc.getReference().delete().addOnSuccessListener(aVoid -> Log.d("Firestore", "Appointment deleted successfully from DB"));
                    }
                }).addOnFailureListener(e -> Log.e("Firestore", "Error deleting appointment", e));
    }
}

