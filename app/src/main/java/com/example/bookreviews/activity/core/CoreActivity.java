package com.example.bookreviews.activity.core;

import android.os.Bundle;
import android.view.View;

import com.example.bookreviews.R;
import com.example.bookreviews.StaticClass;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Objects;

public class CoreActivity extends AppCompatActivity {

    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);
        navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_search, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        if(Objects.equals(getIntent().getStringExtra(StaticClass.TO),
                StaticClass.PROFILE_FRAGMENT)){
            navView.setSelectedItemId(R.id.navigation_profile);
        }
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
