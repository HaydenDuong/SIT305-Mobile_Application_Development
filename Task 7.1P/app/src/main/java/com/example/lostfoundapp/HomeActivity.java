package com.example.lostfoundapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class HomeActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Setup the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar); // Assuming your Toolbar ID is @id/toolbar
        setSupportActionBar(toolbar);

        // Get the NavHostFragment
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment instanceof NavHostFragment) {
            navController = ((NavHostFragment) navHostFragment).getNavController();

            // Initialize AppBarConfiguration.
            appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

            // Setup ActionBar with NavController
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        } else {
            // Handle the case where the NavHostFragment is not found - this would be an error
            throw new IllegalStateException("NavHostFragment not found in the layout. Check activity_home.xml and R.id.nav_host_fragment");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Ensure navController is initialized before using it
        if (navController == null) {
            // This might happen if NavHostFragment wasn't found, though the throw above should prevent it.
            return super.onSupportNavigateUp();
        }
        // Let NavigationUI handle the Up button, providing the AppBarConfiguration
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}