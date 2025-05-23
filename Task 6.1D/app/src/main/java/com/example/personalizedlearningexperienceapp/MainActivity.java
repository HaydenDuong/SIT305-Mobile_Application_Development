package com.example.personalizedlearningexperienceapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration; // Make it a member

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            appBarConfiguration = new AppBarConfiguration.Builder(R.id.loginFragment).build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        } else {
            throw new IllegalStateException("NavHostFragment not found. Check your activity_main.xml layout.");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && appBarConfiguration != null && (NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp());
    }
}
