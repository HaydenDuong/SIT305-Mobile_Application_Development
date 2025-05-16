package com.example.lostfoundapp.fragments; // Your fragment package

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.lostfoundapp.R;
import com.example.lostfoundapp.data.Item;
import com.example.lostfoundapp.data.ItemViewModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ItemDetailFragment extends Fragment {

    private ItemViewModel itemViewModel;
    private NavController navController;

    // TextViews for displaying item details
    private TextView textViewName;
    private TextView textViewType;
    private TextView textViewDate;
    private TextView textViewLocation;
    private TextView textViewDescription;
    private TextView textViewPhone;
    private Button buttonRemove;

    private int currentItemId = -1; // Initialize with a default/invalid value
    private Item currentItemForDeletion; // To hold the item for deletion

    public ItemDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemViewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);

        if (getArguments() != null) {
            // Retrieve itemId from the bundle using the key "itemId"
            currentItemId = getArguments().getInt("itemId", -1); // -1 is a default if key not found
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Initialize views
        textViewName = view.findViewById(R.id.textView_detail_name);
        textViewType = view.findViewById(R.id.textView_detail_type);
        textViewDate = view.findViewById(R.id.textView_detail_date); // Make sure this ID exists in your layout
        textViewLocation = view.findViewById(R.id.textView_detail_location); // Make sure this ID exists
        textViewDescription = view.findViewById(R.id.textView_detail_description);
        textViewPhone = view.findViewById(R.id.textView_detail_phone); // Make sure this ID exists
        buttonRemove = view.findViewById(R.id.button_remove);

        if (currentItemId != -1 && currentItemId != 0) { // Check if itemId is valid (0 is default for autoGen PK if not set)
            itemViewModel.getItemById(currentItemId).observe(getViewLifecycleOwner(), item -> {
                if (item != null) {
                    this.currentItemForDeletion = item; // Store for deletion
                    populateUI(item);
                } else {
                    Toast.makeText(getContext(), "Item not found.", Toast.LENGTH_SHORT).show();
                    navController.popBackStack();
                }
            });
        } else {
            Toast.makeText(getContext(), "Invalid Item ID received.", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
        }

        buttonRemove.setOnClickListener(v -> {
            if (this.currentItemForDeletion != null) {
                showRemoveConfirmationDialog();
            } else {
                Toast.makeText(getContext(), "Cannot remove, item not loaded.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(Item item) {
        // Set the main title of the screen if desired (using Activity's ActionBar)
        // ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(item.getName());

        textViewName.setText(item.getName()); // This is your main centered title now
        textViewType.setText("Type: " + item.getType()); // Adding "Type: " prefix for clarity

        if (item.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            textViewDate.setText("Date: " + sdf.format(new Date(item.getDate()))); // Adding "Date: "
        } else {
            textViewDate.setText("Date: N/A");
        }

        textViewLocation.setText("Location: " + item.getLocation()); // Adding "Location: "
        textViewPhone.setText("Phone: " + item.getPhoneNumber()); // Adding "Phone: "
        textViewDescription.setText("Description:\n" + item.getDescription()); // Adding "Description: " and newline
    }

    private void showRemoveConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Advert")
                .setMessage("Are you sure you want to remove '" + currentItemForDeletion.getName() + "'?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    itemViewModel.delete(this.currentItemForDeletion);
                    Toast.makeText(getContext(), "Advert removed", Toast.LENGTH_SHORT).show();
                    navController.popBackStack();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}