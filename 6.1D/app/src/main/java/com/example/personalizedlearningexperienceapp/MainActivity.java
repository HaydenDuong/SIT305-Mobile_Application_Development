package com.example.personalizedlearningexperienceapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
// Import NavHostFragment
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

// Import Toolbar
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
        setSupportActionBar(toolbar); // Set the toolbar as the support action bar

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Define top-level destinations (optional, if you don't want Up button on these)
            // For example, if loginFragment is the very first screen:
            appBarConfiguration = new AppBarConfiguration.Builder(R.id.loginFragment).build();
            // If you have a drawer layout, you'd pass it to Builder:
            // appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
            //        .setOpenableLayout(drawerLayout) // If you have a DrawerLayout
            //        .build();


            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        } else {
            throw new IllegalStateException("NavHostFragment not found. Check your activity_main.xml layout.");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Ensure navController and appBarConfiguration are not null
        return navController != null && appBarConfiguration != null &&
                (NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp());
    }
}
