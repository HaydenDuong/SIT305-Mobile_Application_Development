package com.example.chatbotapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_container);
        navController = navHostFragment.getNavController();

        // Retrieve extras from LoginActivity and set them as arguments for the graph
        // This makes them available to the startDestination (UserProfileFragment)
        Bundle navArguments = new Bundle();
        navArguments.putString("USER_UID", getIntent().getStringExtra("USER_UID"));
        navArguments.putString("USER_DISPLAY_NAME", getIntent().getStringExtra("USER_DISPLAY_NAME"));
        navController.setGraph(R.navigation.main_nav_graph, navArguments);

        // Define top-level destinations for AppBarConfiguration if you don't want Up button for these
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.userProfileFragment, R.id.chatFragment, R.id.groupChatFragment)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        setupTabLayout(tabLayout);
    }

    private void setupTabLayout(TabLayout tabLayout) {
        tabLayout.addTab(tabLayout.newTab().setText("Profile").setId(R.id.userProfileFragment));
        tabLayout.addTab(tabLayout.newTab().setText("AI Chat").setId(R.id.chatFragment));
        tabLayout.addTab(tabLayout.newTab().setText("Group Chat").setId(R.id.groupChatFragment));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Check current destination to avoid navigating to the same fragment
                if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != tab.getId()) {
                    navController.navigate(tab.getId());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // Update selected tab when NavController destination changes
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null && tab.getId() == destination.getId()) {
                    if (!tab.isSelected()) {
                        tab.select();
                    }
                    break;
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
} 