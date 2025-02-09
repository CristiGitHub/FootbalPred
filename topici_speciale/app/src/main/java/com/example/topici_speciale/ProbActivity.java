package com.example.topici_speciale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ProbActivity extends AppCompatActivity {

    private Button checkButton;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prob);
        checkButton = findViewById(R.id.button);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);



        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), FirstActivity.class));
                        return true;
                    case R.id.menu:
                        startActivity(new Intent(getApplicationContext(), SecondActivity.class));
                        return true;
                    default:
                        return false;
                }
            }
        });
    }


}
