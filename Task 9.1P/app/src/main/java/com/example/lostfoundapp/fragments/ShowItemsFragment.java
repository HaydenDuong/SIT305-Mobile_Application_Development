package com.example.lostfoundapp.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.Toast;
import com.example.lostfoundapp.R;
import com.example.lostfoundapp.adapters.ItemAdapter;
import com.example.lostfoundapp.data.Item;
import com.example.lostfoundapp.data.ItemViewModel;

public class ShowItemsFragment extends Fragment {

    private ItemViewModel itemViewModel;
    private ItemAdapter lostItemsAdapter;
    private ItemAdapter foundItemsAdapter;
    private NavController navController;

    public ShowItemsFragment() { /* ... */ }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemViewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_show_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Setup Lost Items RecyclerView
        RecyclerView lostRecyclerView = view.findViewById(R.id.recyclerView_lost_items);
        lostRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lostRecyclerView.setHasFixedSize(true); // If item size doesn't change
        lostItemsAdapter = new ItemAdapter();
        lostRecyclerView.setAdapter(lostItemsAdapter);

        // Setup Found Items RecyclerView
        RecyclerView foundRecyclerView = view.findViewById(R.id.recyclerView_found_items);
        foundRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        foundRecyclerView.setHasFixedSize(true); // If item size doesn't change
        foundItemsAdapter = new ItemAdapter();
        foundRecyclerView.setAdapter(foundItemsAdapter);

        // Observe Lost Items
        itemViewModel.getItemsByType("Lost").observe(getViewLifecycleOwner(), items -> {
            Log.d("ShowItemsFragment", "Lost items updated: " + (items != null ? items.size() : "null")); // DEBUG LOG
            if (items != null) { // Good practice to check for null, though LiveData usually emits non-null
                lostItemsAdapter.submitList(items);
                Log.d("ShowItemsFragment", "Lost items submitted to adapter. Count: " + items.size()); // DEBUG LOG
                if (items.isEmpty()) {
                    Log.d("ShowItemsFragment", "Lost items list is empty."); // DEBUG LOG
                } else {
                    for (Item item : items) {
                        Log.d("ShowItemsFragment", "Lost Item: " + item.getName()); // DEBUG LOG
                    }
                }
            } else {
                 Log.d("ShowItemsFragment", "Lost items list is NULL"); // DEBUG LOG
            }
        });

        // Observe Found Items
        itemViewModel.getItemsByType("Found").observe(getViewLifecycleOwner(), items -> {
            Log.d("ShowItemsFragment", "Found items updated: " + (items != null ? items.size() : "null")); // DEBUG LOG
             if (items != null) {
                foundItemsAdapter.submitList(items);
                Log.d("ShowItemsFragment", "Found items submitted to adapter. Count: " + items.size()); // DEBUG LOG
                if (items.isEmpty()) {
                    Log.d("ShowItemsFragment", "Found items list is empty."); // DEBUG LOG
                } else {
                    for (Item item : items) {
                        Log.d("ShowItemsFragment", "Found Item: " + item.getName()); // DEBUG LOG
                    }
                }
            } else {
                Log.d("ShowItemsFragment", "Found items list is NULL"); // DEBUG LOG
            }
        });

        // Click listener for lost items
        lostItemsAdapter.setOnItemClickListener(item -> {
            navigateToDetail(item);
        });

        // Click listener for found items
        foundItemsAdapter.setOnItemClickListener(item -> {
            navigateToDetail(item);
        });
    }

    private void navigateToDetail(Item item) {
        Bundle bundle = new Bundle();
        bundle.putInt("itemId", item.getId()); // Use a consistent key, e.g., "itemId"

        // Ensure you have this action ID in your nav_graph.xml leading to ItemDetailFragment
        navController.navigate(R.id.action_showItemsFragment_to_itemDetailFragment, bundle);
    }
}