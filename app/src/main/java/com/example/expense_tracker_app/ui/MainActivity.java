package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding b;

    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new StatsFragment()).commit();

        b.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_stats) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new StatsFragment()).commit();
                return true;
            }
            return false;
        });

        b.fabAdd.setOnClickListener(v ->
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new AddTransactionFragment())
                        .addToBackStack(null).commit()
        );
    }
}
