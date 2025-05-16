package com.example.lostfoundapp.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.lostfoundapp.R;
import com.example.lostfoundapp.data.Item;
import com.example.lostfoundapp.data.ItemViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import androidx.fragment.app.FragmentResultListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateAdvertFragment extends Fragment {

    private ItemViewModel itemViewModel;
    private NavController navController;

    private RadioGroup radioGroupType;
    private EditText editTextName;
    private EditText editTextPhone;
    private EditText editTextDescription;
    private Button buttonDatePicker;
    private TextView textViewSelectedDate;
    private EditText editTextLocation;
    private Button buttonSave;
    private Button buttonGetCurrentLocation;
    private Calendar selectedDateCalendar;
    private Double currentLatitude = null;
    private Double currentLongitude = null;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public CreateAdvertFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemViewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
        selectedDateCalendar = Calendar.getInstance(); // Initialize with current date
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize the permission Launcher
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(getContext(), "Location permission denied. Cannot get current location.", Toast.LENGTH_LONG).show();
            }
        });

        // Listen for result from PlaceAutocompleteFragment
        getParentFragmentManager().setFragmentResultListener("requestKey_place", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                String placeName = bundle.getString("placeName");
                String placeAddress = bundle.getString("placeAddress");
                currentLatitude = bundle.containsKey("placeLat") ? bundle.getDouble("placeLat") : null;
                currentLongitude = bundle.containsKey("placeLng") ? bundle.getDouble("placeLng") : null;

                if (placeName != null) {
                    editTextLocation.setText(placeName);
                    Toast.makeText(getContext(), "Location selected: " + placeName, Toast.LENGTH_SHORT).show();
                }
                Log.d("PlaceSelection", "Name: " + placeName + ", Address: " + placeAddress + ", Lat: " + currentLatitude + ", Lng: " + currentLongitude);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_advert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        navController = Navigation.findNavController(view);
        radioGroupType = view.findViewById(R.id.radioGroup_type);
        editTextName = view.findViewById(R.id.editText_name);
        editTextPhone = view.findViewById(R.id.editText_phone);
        editTextDescription = view.findViewById(R.id.editText_description);
        buttonDatePicker = view.findViewById(R.id.button_date_picker);
        textViewSelectedDate = view.findViewById(R.id.textView_selected_date);
        editTextLocation = view.findViewById(R.id.editText_location);
        buttonSave = view.findViewById(R.id.button_save);
        buttonGetCurrentLocation = view.findViewById(R.id.button_get_current_location);

        updateDateInView(); // Show current date initially or "No date selected" if preferred

        // Setup Event-Click Listener
        buttonDatePicker.setOnClickListener(v -> showDatePickerDialog());
        buttonSave.setOnClickListener(v -> saveAdvert());
        buttonGetCurrentLocation.setOnClickListener(v -> requestLocationPermissionOrGetLocation());
        editTextLocation.setOnClickListener(v -> {
            navController.navigate(R.id.action_createAdvertFragment_to_placeAutocompleteFragment);
        });
        // Alternatively, the whole TextInputLayout can be clickable by implementing the following:
        // TextInputLayout locationInputLayout = view.findViewById(R.id.textInputLayout_location);
        // locationInputLayout.setOnClickListener(v -> navController.navigate(R.id.action_createAdvertFragment_to_placeAutocompleteFragment));
    }

    // Method for handling permission request or fetch location
    private void requestLocationPermissionOrGetLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    // A placeholder method for fetching location
    private void fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location permission is not granted.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Fetching current location...", Toast.LENGTH_SHORT).show();

        // Used for request a high accuracy location
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken())
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        String locationText = "Lat: " + String.format(Locale.getDefault(), "%.4f", currentLatitude) +
                                ", Long: " + String.format(Locale.getDefault(), "%.4f", currentLongitude);
                        editTextLocation.setText(locationText); // Update the location EditText
                        Toast.makeText(getContext(), "Location fetched: " + locationText, Toast.LENGTH_LONG).show();
                    } else {
                        currentLatitude = null;
                        currentLongitude = null;
                        editTextLocation.setText(""); // Clear if location is null
                        Toast.makeText(getContext(), "Failed to get current location. Last known location might also be null.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(requireActivity(),e -> {
                    currentLatitude = null;
                    currentLongitude = null;
                    editTextLocation.setText(""); // Clear on failure
                    Toast.makeText(getContext(), "Failed to get current location: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("FetchLocation", "Error getting location", e);
                });
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, dayOfMonth) -> {
            selectedDateCalendar.set(Calendar.YEAR, year);
            selectedDateCalendar.set(Calendar.MONTH, month);
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateInView();
        };

        new DatePickerDialog(requireContext(), dateSetListener,
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void updateDateInView() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        textViewSelectedDate.setText(sdf.format(selectedDateCalendar.getTime()));
    }

    private void saveAdvert() {
        String type = radioGroupType.getCheckedRadioButtonId() == R.id.radioButton_lost ? "Lost" : "Found";
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        Long dateTimestamp = selectedDateCalendar.getTimeInMillis(); // Get timestamp

        // Basic Validation
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(description) || TextUtils.isEmpty(location)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // currentLatitude and currentLongitude are updated by either fetchCurrentLocation()
        // or the FragmentResultListener for place autocomplete
        Item newItem = new Item(type, name, phone, description, dateTimestamp, location, currentLatitude, currentLongitude);
        itemViewModel.insert(newItem);

        Toast.makeText(getContext(), "Advert Saved", Toast.LENGTH_SHORT).show();
        navController.popBackStack();
    }
}
