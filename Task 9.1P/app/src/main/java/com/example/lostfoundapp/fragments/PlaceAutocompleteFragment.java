package com.example.lostfoundapp.fragments;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.lostfoundapp.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import java.util.Arrays;
import java.util.List;

public class PlaceAutocompleteFragment extends Fragment {

    private static final String TAG = "PlaceAutocomplete";
    private NavController navController;

    public PlaceAutocompleteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Places.isInitialized()) {
            try {
                ApplicationInfo app = requireActivity().getPackageManager().getApplicationInfo(requireActivity().getPackageName(), PackageManager.GET_META_DATA);
                Bundle metaData = app.metaData;
                String apiKey = metaData.getString("com.google.android.geo.API_KEY");

                if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_API_KEY") && !apiKey.equals("YOUR_API_KEY_PLACEHOLDER")) {
                    Places.initialize(requireActivity().getApplicationContext(), apiKey);
                    Log.i(TAG, "Places SDK initialized successfully.");
                } else {
                    Log.e(TAG, "API key not found, is a placeholder, or is empty in AndroidManifest.xml. Places SDK cannot be initialized.");
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Failed to load meta-data for API key: " + e.getMessage());
            } catch (NullPointerException e) {
                Log.e(TAG, "Failed to initialize Places SDK. Bundle or API Key was null: " + e.getMessage());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_place_autocomplete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        if (!Places.isInitialized()) {
            Toast.makeText(getContext(), "Places Autocomplete is unavailable. Please check API key configuration.", Toast.LENGTH_LONG).show();
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.placeAutocompleteFragment) {
                navController.popBackStack();
            }
            return;
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment_container);

        if (autocompleteFragment != null) {
            List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            autocompleteFragment.setPlaceFields(placeFields);

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    Log.i(TAG, "Place: " + place.getName() + ", ID: " + place.getId() + ", Address: " + place.getAddress() + ", LatLng: " + place.getLatLng());
                    
                    String placeName = place.getName();
                    String placeAddress = place.getAddress();
                    Double placeLat = null;
                    Double placeLng = null;

                    if (place.getLatLng() != null) {
                        placeLat = place.getLatLng().latitude;
                        placeLng = place.getLatLng().longitude;
                    }

                    Bundle result = new Bundle();
                    result.putString("placeName", placeName);
                    result.putString("placeAddress", placeAddress);
                    if (placeLat != null) result.putDouble("placeLat", placeLat);
                    if (placeLng != null) result.putDouble("placeLng", placeLng);
                    
                    getParentFragmentManager().setFragmentResult("requestKey_place", result);
                    if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.placeAutocompleteFragment) {
                        navController.popBackStack();
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.e(TAG, "An error occurred during place selection: " + status.getStatusMessage() + " | Code: " + status.getStatusCode());
                    String statusMessage = status.getStatusMessage();

                    if (status.getStatusCode() == com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR) {
                        Toast.makeText(getContext(), "Network error. Please check your connection.", Toast.LENGTH_LONG).show();
                    } else if (status.getStatusCode() == com.google.android.gms.common.api.CommonStatusCodes.API_NOT_CONNECTED &&
                               statusMessage != null && statusMessage.contains("Places Write access is not enabled")) {
                         Toast.makeText(getContext(), "Error with Places API. Please check configuration.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Error selecting place: " + (statusMessage != null ? statusMessage : "Unknown error"), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Log.e(TAG, "AutocompleteSupportFragment not found in the layout.");
            Toast.makeText(getContext(), "Error: Autocomplete feature is not available.", Toast.LENGTH_LONG).show();
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.placeAutocompleteFragment) {
                navController.popBackStack();
            }
        }
    }
}
