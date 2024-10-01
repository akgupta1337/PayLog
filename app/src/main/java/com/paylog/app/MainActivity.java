package com.paylog.app;

import static android.content.ContentValues.TAG;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    DatabaseHelper myDB;
    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    AnalysisFragment analysisFragment = new AnalysisFragment();
    ExportFragment exportFragment = new ExportFragment();
    AssistantFragment assistantFragment = new AssistantFragment();
    private final int RC_Notification = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppState.isAppInBackground = false;

//        startService();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        myDB = new DatabaseHelper(this);
        if (!areNotificationsEnabled()) {
            askForNotification();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                    return true;
                } else if (itemId == R.id.analysis) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, analysisFragment).commit();
                    return true;
                } else if (itemId == R.id.export) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, exportFragment).commit();
                    return true;
                } else if (itemId == R.id.assistant){
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, assistantFragment).commit();
                    return true;
                }
                return false;
            }
        });

        // Register activity lifecycle callbacks
        getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(Activity activity) {
                // Log when the app comes to the foreground
//                startService();
            }

            @Override
            public void onActivityPaused(Activity activity) {
                // Log when the app goes to the background
                AppState.isAppInBackground = true;
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {}

            @Override
            public void onActivityStopped(Activity activity) {
                AppState.isAppInBackground = true;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {}
        });



    }

    private void askForNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},RC_Notification);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == RC_Notification){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show();

//                startService();
            }
            else{
                Toast.makeText(this, "Notification Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean areNotificationsEnabled() {
        return NotificationManagerCompat.from(this).areNotificationsEnabled();
    }
    public void startService() {
        Intent serviceIntent = new Intent(this, EmailCheckingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
    }

}