package com.example.chatbotapp;

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
        navController.setGraph(R.navigation.main_nav_graph, navArguments);

        appBarConfiguration = new AppBarConfiguration.Builder(R.id.userProfileFragment, R.id.chatFragment, R.id.groupChatFragment).build();
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
                if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != tab.getId()) {
                    Bundle args = new Bundle();
                    args.putString("USER_UID", userUidFromIntent);
                    args.putString("USER_DISPLAY_NAME", userDisplayNameFromIntent);
                    navController.navigate(tab.getId(), args);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getId() == R.id.userProfileFragment) {
                    NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_container);
                    if (navHost != null && navHost.getChildFragmentManager().getFragments().size() > 0) {
                        Fragment currentFragment = navHost.getChildFragmentManager().getFragments().get(0);
                        if (currentFragment instanceof UserProfileFragment) {
                            ((UserProfileFragment) currentFragment).handleProfileTabReselection();
                        }
                    }
                }
            }
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