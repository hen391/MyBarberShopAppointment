package com.example.mybarbershopappointment.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mybarbershopappointment.R;
import com.example.mybarbershopappointment.Models.Appointment;
import com.example.mybarbershopappointment.Adapters.AppointmentsAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminViewAppointmentsActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyMessage;
    private List<Appointment> appointmentList;
    private AppointmentsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_appointments);

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerViewAppointments);
        progressBar = findViewById(R.id.progressBar);
        emptyMessage = findViewById(R.id.textViewEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();
        adapter = new AppointmentsAdapter(this, appointmentList, true);
        recyclerView.setAdapter(adapter);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            loadAppointmentsForDate(selectedDate);
        });
    }

    private void loadAppointmentsForDate(String date) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d("AdminView", "Loading appointments for date: " + date);
        db.collection("appointments")
                .whereEqualTo("date", date)
                .get().addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        appointmentList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Appointment appointment = document.toObject(Appointment.class);
                                Log.d("AdminView", "Fetched appointment: " + appointment);
                                appointmentList.add(appointment);
                            } catch (Exception e) {
                                Log.e("AdminView", "Error parsing document", e);
                            }
                        }
                        runOnUiThread(() -> {
                            Log.d("AdminView", "Appointments count: " + appointmentList.size());
                            adapter.setAppointments(appointmentList);
                            recyclerView.setAdapter(adapter);  // Force set adapter again
                            adapter.notifyDataSetChanged();    // Force update
                            if (!appointmentList.isEmpty()) {
                                recyclerView.setVisibility(View.VISIBLE);
                                emptyMessage.setVisibility(View.GONE);
                            } else {
                                recyclerView.setVisibility(View.GONE);
                                emptyMessage.setText("אין תורים להצגה");
                                emptyMessage.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        Log.e("AdminView", "Error loading appointments", task.getException());
                        runOnUiThread(() -> {
                            emptyMessage.setText("שגיאה בטעינת התורים");
                            emptyMessage.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        });
                    }
                });
    }
}
