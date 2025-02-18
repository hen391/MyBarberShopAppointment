package com.example.mybarbershopappointment.Repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.mybarbershopappointment.Models.Appointment;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Boolean> bookAppointment(String userId, String date, String time, String service) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        db.collection("appointments").document(date + "_" + time)
                .set(new Appointment(userId, service, date, time))
                .addOnSuccessListener(aVoid -> result.postValue(true))
                .addOnFailureListener(e -> result.postValue(false));
        return result;
    }

    public LiveData<Boolean> cancelAppointment(String date, String time) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        db.collection("appointments").document(date + "_" + time)
                .delete()
                .addOnSuccessListener(aVoid -> result.postValue(true))
                .addOnFailureListener(e -> result.postValue(false));
        return result;
    }

    public LiveData<List<Appointment>> getUserAppointments(String userId) {
        MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
        db.collection("appointments").whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Appointment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(Appointment.class));
                    }
                    appointments.postValue(list);
                })
                .addOnFailureListener(e -> appointments.postValue(null));
        return appointments;
    }

    public LiveData<Boolean> deleteAppointment(String appointmentId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        db.collection("appointments").document(appointmentId)
                .delete()
                .addOnSuccessListener(aVoid -> result.postValue(true))
                .addOnFailureListener(e -> result.postValue(false));
        return result;
    }
}
