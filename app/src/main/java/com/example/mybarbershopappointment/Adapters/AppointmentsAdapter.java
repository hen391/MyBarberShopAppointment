package com.example.mybarbershopappointment.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mybarbershopappointment.Activities.BookAppointmentActivity;
import com.example.mybarbershopappointment.Models.Appointment;
import com.example.mybarbershopappointment.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.List;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.AppointmentViewHolder> {
    private Context context;
    private List<Appointment> appointmentList;
    private boolean isAdmin;

    public AppointmentsAdapter(Context context, List<Appointment> appointmentList, boolean isAdmin) {
        this.context = context;
        this.appointmentList = appointmentList;
        this.isAdmin = isAdmin;
    }
    public void setAppointments(List<Appointment> appointments) {
        this.appointmentList = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        if (appointment == null) return;
        if (!isAdmin) {
            holder.textViewDetails.setText("Date: " + appointment.getDate() + "\nHour: " + appointment.getTime() + "\nService: " + appointment.getService());
            Button editButton = holder.itemView.findViewById(R.id.buttonEditAppointment);
            Button cancelButton = holder.itemView.findViewById(R.id.buttonCancelAppointment);

            // Check if appointment date/time is in the past
            if (isPastAppointment(appointment)) {
                editButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
            } else {
                editButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);

                editButton.setOnClickListener(v -> {
                    Intent intent = new Intent(context, BookAppointmentActivity.class);
                    intent.putExtra("edit_appointment", appointment);
                    intent.putExtra("is_edit", true);
                    intent.putExtra("selected_service", appointment.getService());
                    intent.putExtra("lock_service", true);
                    context.startActivity(intent);
                });

                cancelButton.setOnClickListener(v -> {
                    FirebaseFirestore.getInstance().collection("appointments")
                            .whereEqualTo("userId", appointment.getUserId())
                            .whereEqualTo("date", appointment.getDate())
                            .whereEqualTo("time", appointment.getTime())
                            .whereEqualTo("service", appointment.getService())
                            .get().addOnSuccessListener(querySnapshot -> {
                                for (var doc : querySnapshot) {
                                    doc.getReference().delete().addOnSuccessListener(aVoid -> {
                                        appointmentList.remove(position);
                                        notifyItemRemoved(position);
                                        Toast.makeText(context, "Appointment deleted from database successfully.", Toast.LENGTH_SHORT).show();
                                    }).addOnFailureListener(e -> Toast.makeText(context, "Failed to delete appointment from database.", Toast.LENGTH_SHORT).show());
                                }
                            }).addOnFailureListener(e -> Toast.makeText(context, "Error finding appointment in database.", Toast.LENGTH_SHORT).show());
                });
            }
        } else {
            FirebaseFirestore.getInstance().collection("users").document(appointment.getUserId())
                    .get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            holder.textViewDetails.setText("Date: " + appointment.getDate() + "\nHour: " + appointment.getTime() + "\nService: " + appointment.getService() + "\nUser: " + fullName);
                        }
                    });
            holder.itemView.findViewById(R.id.buttonEditAppointment).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.buttonCancelAppointment).setVisibility(View.GONE);
        }
    }

    private boolean isPastAppointment(Appointment appointment) {
        Calendar now = Calendar.getInstance();
        Calendar appointmentTime = Calendar.getInstance();
        String[] dateParts = appointment.getDate().split("-");
        String[] timeParts = appointment.getTime().split(":");
        appointmentTime.set(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[2]),
                Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
        return now.after(appointmentTime);
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDetails;
        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDetails = itemView.findViewById(R.id.textViewAppointmentDetails);
        }
    }
}
