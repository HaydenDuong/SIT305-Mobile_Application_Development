package com.example.chatbotapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private String userUidFromIntent; // Store for tab navigation
    private String userDisplayNameFromIntent; // Store for tab navigation
    private int lastSelectedFragmentTabPosition = 0; // To remember the last fragment tab

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userUidFromIntent = getIntent().getStringExtra("USER_UID");
        userDisplayNameFromIntent = getIntent().getStringExtra("USER_DISPLAY_NAME");

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_container);
        navController = navHostFragment.getNavController();

        Bundle navArguments = new Bundle();
        navArguments.putString("USER_UID", userUidFromIntent);
        navArguments.putString("USER_DISPLAY_NAME", userDisplayNameFromIntent);
        // Set graph only if it hasn't been set, to preserve state on config changes if nav controller persists
        if (navController.getCurrentDestination() == null) {
             navController.setGraph(R.navigation.main_nav_graph, navArguments);
        }

        // Define top-level destinations for AppBarConfiguration (only those that are fragments in this host)
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.userProfileFragment, R.id.chatFragment).build(); 
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        setupTabLayout(tabLayout);
    }

    private void setupTabLayout(TabLayout tabLayout) {
        tabLayout.addTab(tabLayout.newTab().setText("Profile").setId(R.id.userProfileFragment));
        tabLayout.addTab(tabLayout.newTab().setText("AI Chat").setId(R.id.chatFragment));
        tabLayout.addTab(tabLayout.newTab().setText("Recommendations").setId(R.id.recommendations_tab_tag_id)); // Use new ID

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int selectedTabId = tab.getId();
                if (selectedTabId == R.id.recommendations_tab_tag_id) {
                    Intent intent = new Intent(MainActivity.this, RecommendationsActivity.class);
                    intent.putExtra("currentUserId", userUidFromIntent); // Pass UID if needed
                    startActivity(intent);
                    // After launching, re-select the previously active fragment tab
                    // to avoid the Recommendations tab looking active while an activity is on top.
                    if (tabLayout.getTabAt(lastSelectedFragmentTabPosition) != null) {
                        tabLayout.getTabAt(lastSelectedFragmentTabPosition).select();
                    }
                } else {
                    // Handle navigation for fragment tabs
                    if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != selectedTabId) {
                        Bundle args = new Bundle();
                        args.putString("USER_UID", userUidFromIntent);
                        args.putString("USER_DISPLAY_NAME", userDisplayNameFromIntent);
                        navController.navigate(selectedTabId, args);
                        // Update last selected fragment tab position
                        if(tab.getPosition() < 2) { // Assuming first two tabs are fragments
                           lastSelectedFragmentTabPosition = tab.getPosition();
                        }
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                 if (tab.getId() == R.id.recommendations_tab_tag_id) {
                    Intent intent = new Intent(MainActivity.this, RecommendationsActivity.class);
                    intent.putExtra("currentUserId", userUidFromIntent);
                    startActivity(intent);
                } else if (tab.getId() == R.id.userProfileFragment) {
                    NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_container);
                    if (navHost != null && navHost.getChildFragmentManager().getFragments().size() > 0) {
                        Fragment currentFragment = navHost.getChildFragmentManager().getFragments().get(0);
                        if (currentFragment instanceof UserProfileFragment) {
                            ((UserProfileFragment) currentFragment).handleProfileTabReselection();
                        }
                    }
                }
                // You might want to add reselection logic for AI Chat tab if needed
            }
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean foundTab = false;
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                // Only match fragment tabs with destinations in this NavController
                if (tab != null && (tab.getId() == R.id.userProfileFragment || tab.getId() == R.id.chatFragment) && tab.getId() == destination.getId()) {
                    if (!tab.isSelected()) {
                        tab.select();
                    }
                    if(i < 2) { // Update last selected for fragment tabs
                        lastSelectedFragmentTabPosition = i;
                    }
                    foundTab = true;
                    break;
                }
            }
            // If the destination is not one of the main fragment tabs (e.g. after launching RecommendationsActivity and coming back)
            // ensure the correct fragment tab is selected based on lastSelectedFragmentTabPosition
            if (!foundTab && tabLayout.getTabCount() > lastSelectedFragmentTabPosition) {
                 TabLayout.Tab lastFragmentTab = tabLayout.getTabAt(lastSelectedFragmentTabPosition);
                 if(lastFragmentTab != null && !lastFragmentTab.isSelected()) {
                     // Only select if current destination is one of the fragment destinations
                     if (destination.getId() == R.id.userProfileFragment || destination.getId() == R.id.chatFragment) {
                          lastFragmentTab.select();
                     }
                 }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
} 