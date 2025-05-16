package com.example.lostfoundapp.fragments; // Or your fragment package

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.lostfoundapp.R;

public class HomeFragment extends Fragment {

    private Button buttonCreateAdvert;
    private Button buttonShowAllItems;
    private Button buttonShowOnMap;
    private NavController navController;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize NavController
        navController = Navigation.findNavController(view);

        // Find buttons
        buttonCreateAdvert = view.findViewById(R.id.button_create_advert);
        buttonShowAllItems = view.findViewById(R.id.button_show_all_items);
        buttonShowOnMap = view.findViewById(R.id.button_show_on_map);

        // Set click listeners
        buttonCreateAdvert.setOnClickListener(v -> {
            navController.navigate(R.id.action_homeFragment_to_createAdvertFragment);
        });

        buttonShowAllItems.setOnClickListener(v -> {
            navController.navigate(R.id.action_homeFragment_to_showItemsFragment);
        });

        buttonShowOnMap.setOnClickListener(v -> {
            // We will define action_homeFragment_to_mapFragment in nav_graph.xml next
            navController.navigate(R.id.action_homeFragment_to_mapFragment);
        });
    }
}