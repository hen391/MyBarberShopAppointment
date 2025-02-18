package com.example.mybarbershopappointment.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mybarbershopappointment.R;

public class UserDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        Button viewAppointmentsButton = findViewById(R.id.viewAppointmentsButton);
        Button bookAppointmentButton = findViewById(R.id.bookAppointmentButton);

        viewAppointmentsButton.setOnClickListener(v -> startActivity(new Intent(this, AppointmentsActivity.class)));
        bookAppointmentButton.setOnClickListener(v -> startActivity(new Intent(this, BookAppointmentActivity.class)));
    }
}
