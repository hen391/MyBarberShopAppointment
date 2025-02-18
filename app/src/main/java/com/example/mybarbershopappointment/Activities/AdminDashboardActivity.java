package com.example.mybarbershopappointment.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mybarbershopappointment.R;

public class AdminDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        Button manageServicesButton = findViewById(R.id.manageServicesButton);
        Button manageScheduleButton = findViewById(R.id.manageScheduleButton);
        Button viewAppointmentsButton = findViewById(R.id.viewAppointmentsButton);

        manageServicesButton.setOnClickListener(v -> startActivity(new Intent(this, AdminManageServicesActivity.class)));
        manageScheduleButton.setOnClickListener(v -> startActivity(new Intent(this, AdminScheduleActivity.class)));
        viewAppointmentsButton.setOnClickListener(v -> startActivity(new Intent(this, AdminViewAppointmentsActivity.class)));
    }
}
