package com.example.lostfoundapp.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.lostfoundapp.R;
import com.example.lostfoundapp.data.Item;
import com.example.lostfoundapp.data.ItemViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";
    private MapView mapView;
    private GoogleMap googleMap;
    private ItemViewModel itemViewModel;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemViewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState); // Important for MapView lifecycle
        mapView.getMapAsync(this); // This will call onMapReady when the map is available
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        Log.d(TAG, "Map is ready.");
        // TODO: Configure map settings if needed (e.g., UI settings, map type)
        // googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Observe items from ViewModel and add markers
        itemViewModel.getAllItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null && googleMap != null) {
                Log.d(TAG, "Received " + items.size() + " items to display on map.");
                googleMap.clear(); // Clear existing markers
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                boolean hasValidLocation = false;

                for (Item item : items) {
                    if (item.getLatitude() != null && item.getLongitude() != null) {
                        LatLng itemLocation = new LatLng(item.getLatitude(), item.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(itemLocation)
                                .title(item.getName());

                        if ("Lost".equalsIgnoreCase(item.getType())) {
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        } else if ("Found".equalsIgnoreCase(item.getType())) {
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        } else {
                            // Default marker color if type is neither Lost nor Found (or null)
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        }
                        googleMap.addMarker(markerOptions);
                        boundsBuilder.include(itemLocation);
                        hasValidLocation = true;
                    } else {
                        Log.w(TAG, "Item '" + item.getName() + "' has no location data, not showing on map.");
                    }
                }

                if (hasValidLocation) {
                    try {
                        LatLngBounds bounds = boundsBuilder.build();
                        // Adjust padding as needed, e.g., 100 pixels
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                    } catch (IllegalStateException e) {
                        // This can happen if no locations were added to boundsBuilder (e.g., only one item with location)
                        // Fallback to moving to the first item or a default location
                        Log.e(TAG, "Error building bounds, possibly only one marker or no markers: " + e.getMessage());
                        if (!items.isEmpty() && items.get(0).getLatitude() != null && items.get(0).getLongitude() != null) {
                            LatLng firstItemLoc = new LatLng(items.get(0).getLatitude(), items.get(0).getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstItemLoc, 10f)); // Zoom level 10
                        }
                        // else handle case with no items with locations at all - maybe show a default location like city center
                    }
                } else {
                    Log.d(TAG, "No items with valid locations to display on map.");
                    // Optionally move camera to a default location or show a message
                }
            }
        });
    }

    // --- MapView Lifecycle Forwarding ---
    // It's crucial to forward lifecycle events to the MapView.
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() { // Use onDestroyView for fragment views
        super.onDestroyView();
        if (mapView != null) {
            // According to Google's documentation, MapView.onDestroy() should be called.
            // However, ensure this doesn't cause issues with NavComponent Bstack.
            // If map state is lost on back navigation, review if map needs to be cleared or if
            // the MapView instance itself is being prematurely destroyed.
            mapView.onDestroy();
        }
        googleMap = null; // Release map reference
        mapView = null; // Release mapView reference
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
}
