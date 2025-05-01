package com.example.itubeapp.activities;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.itubeapp.R;
import com.example.itubeapp.fragment.HomeFragment;
import com.example.itubeapp.fragment.PlaylistFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Retrieve login userId from LoginFragment through Intent
        userId = getIntent().getIntExtra("userId", -1);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set event-handler for bottom NavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_playlist) {
                selectedFragment = new PlaylistFragment();
            }
            if (selectedFragment != null) {
                Bundle bundle = new Bundle();
                bundle.putInt("userId", userId);
                selectedFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Display HomeFragment
        if (savedInstanceState == null) {
            HomeFragment homeFragment = new HomeFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("userId", userId);
            homeFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        }

    }
}