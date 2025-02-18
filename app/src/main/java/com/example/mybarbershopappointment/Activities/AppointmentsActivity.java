package com.example.mybarbershopappointment.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mybarbershopappointment.R;
import com.example.mybarbershopappointment.Models.Appointment;
import com.example.mybarbershopappointment.Adapters.AppointmentsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class AppointmentsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyMessage;
    private List<Appointment> appointmentList;
    private AppointmentsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        recyclerView = findViewById(R.id.recyclerViewAppointments);
        progressBar = findViewById(R.id.progressBar);
        emptyMessage = findViewById(R.id.textViewEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();

        loadUserAppointments();
    }

    private void loadUserAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("appointments")
                .whereEqualTo("userId", userId)
                .orderBy("userId", Query.Direction.ASCENDING)
                .orderBy("date", Query.Direction.ASCENDING)
                .orderBy("time", Query.Direction.ASCENDING)
                .get().addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        appointmentList.clear();
                        for (var document : task.getResult()) {
                            try {
                                Appointment appointment = document.toObject(Appointment.class);
                                appointmentList.add(appointment);
                            } catch (Exception e) {
                                Log.e("AppointmentsActivity", "Error parsing document", e);
                            }
                        }
                        adapter = new AppointmentsAdapter(this, appointmentList, false);
                        recyclerView.setAdapter(adapter);

                        if (!appointmentList.isEmpty()) {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyMessage.setVisibility(View.GONE);
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            emptyMessage.setText("No appointments to display");
                            emptyMessage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("AppointmentsActivity", "Error loading appointments", task.getException());
                        emptyMessage.setText("Error loading appointments");
                        emptyMessage.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });
    }
}