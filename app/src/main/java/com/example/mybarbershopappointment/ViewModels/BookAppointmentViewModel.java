package com.example.mybarbershopappointment.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.mybarbershopappointment.Repositories.AppointmentRepository;

public class BookAppointmentViewModel extends ViewModel {
    private final AppointmentRepository repository = new AppointmentRepository();
    private final MutableLiveData<Boolean> appointmentSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> getAppointmentSuccess() {
        return appointmentSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void bookAppointment(String userId, String date, String time, String service) {
        repository.bookAppointment(userId, date, time, service).observeForever(result -> {
            if (Boolean.TRUE.equals(result)) {
                appointmentSuccess.postValue(true);
            } else {
                appointmentSuccess.postValue(false);
                errorMessage.postValue("Failed to book appointment. Please try again.");
            }
        });
    }

}
