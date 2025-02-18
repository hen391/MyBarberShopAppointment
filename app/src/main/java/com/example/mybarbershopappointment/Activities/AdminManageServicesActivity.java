package com.example.mybarbershopappointment.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.example.mybarbershopappointment.R;
import com.google.firebase.firestore.*;
import java.util.*;

public class AdminManageServicesActivity extends AppCompatActivity {
    private EditText serviceNameInput, serviceDurationInput;
    private Button addServiceButton;
    private ListView servicesListView;
    private FirebaseFirestore db;
    private ArrayAdapter<String> servicesAdapter;
    private List<String> servicesList = new ArrayList<>();
    private Map<String, String> serviceIds = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_services);

        // 📌 הפעלת כפתור החזרה
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        serviceNameInput = findViewById(R.id.serviceNameInput);
        serviceDurationInput = findViewById(R.id.serviceDurationInput);
        addServiceButton = findViewById(R.id.addServiceButton);
        servicesListView = findViewById(R.id.servicesListView);
        db = FirebaseFirestore.getInstance();

        servicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, servicesList);
        servicesListView.setAdapter(servicesAdapter);
        servicesListView.setOnItemClickListener((parent, view, position, id) -> showEditDialog(position));
        addServiceButton.setOnClickListener(v -> {
            addService();
            // ✅ מנקה את השדות לאחר הוספת שירות
            serviceNameInput.setText("");
            serviceDurationInput.setText("");
        });
        loadServices();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addService() {
        String name = serviceNameInput.getText().toString().trim();
        String durationStr = serviceDurationInput.getText().toString().trim();
        if (name.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> service = new HashMap<>();
        service.put("name", name);
        service.put("duration", Long.parseLong(durationStr));

        db.collection("services").add(service).addOnSuccessListener(docRef -> {
            Toast.makeText(this, "Service added successfully!", Toast.LENGTH_SHORT).show();
            loadServices();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to add service", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadServices() {
        db.collection("services").get().addOnCompleteListener(task -> {
            servicesList.clear();
            serviceIds.clear();
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                for (DocumentSnapshot doc : task.getResult()) {
                    String name = doc.getString("name");
                    Long duration = doc.getLong("duration");
                    if (name != null && duration != null) {
                        String serviceInfo = name + " - Duration: " + duration + " minutes";
                        servicesList.add(serviceInfo);
                        serviceIds.put(serviceInfo, doc.getId());
                    }
                }
            } else {
                servicesList.add("No services available.");
            }
            servicesAdapter.notifyDataSetChanged();
        });
    }

    private void showEditDialog(int position) {
        String selectedService = servicesList.get(position);
        String serviceId = serviceIds.get(selectedService);
        if (serviceId == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_service, null);
        EditText editName = dialogView.findViewById(R.id.editServiceName);
        EditText editDuration = dialogView.findViewById(R.id.editServiceDuration);

        db.collection("services").document(serviceId).get().addOnSuccessListener(doc -> {
            editName.setText(doc.getString("name"));
            editDuration.setText(doc.contains("duration") ? String.valueOf(doc.getLong("duration")) : "");
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Edit Service")
                .setPositiveButton("Update", (d, which) -> {
                    updateService(serviceId, editName, editDuration);
                    d.dismiss();
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .setNeutralButton("Delete", (d, which) -> {
                    deleteService(serviceId);
                    d.dismiss();
                })
                .create();
        dialog.show();
    }


    private void updateService(String id, EditText nameInput, EditText durationInput) {
        String newName = nameInput.getText().toString().trim();
        String newDuration = durationInput.getText().toString().trim();
        if (!newName.isEmpty() && !newDuration.isEmpty()) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", newName);
            updates.put("duration", Long.parseLong(newDuration));
            db.collection("services").document(id).update(updates)
                    .addOnSuccessListener(aVoid -> loadServices())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void deleteService(String id) {
        db.collection("services").document(id).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Service deleted", Toast.LENGTH_SHORT).show();
            loadServices();
        });
    }

}
