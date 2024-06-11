package com.example.mobile_term;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private FragmentProfile fragmentProfile = new FragmentProfile();
    private FragmentDaily fragmentDaily = new FragmentDaily();
    private FragmentCalender fragmentCalender = new FragmentCalender();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayout, fragmentDaily).commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());
    }

    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener{
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            int id = menuItem.getItemId();

            if (id == R.id.profile) transaction.replace(R.id.frameLayout, fragmentProfile).commitAllowingStateLoss();
            else if (id == R.id.daily) transaction.replace(R.id.frameLayout, fragmentDaily).commitAllowingStateLoss();
            else if (id == R.id.calender) transaction.replace(R.id.frameLayout, fragmentCalender).commitAllowingStateLoss();

            return true;
        }
    }
}