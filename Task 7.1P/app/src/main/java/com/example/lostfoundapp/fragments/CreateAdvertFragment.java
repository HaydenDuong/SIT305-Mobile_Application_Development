package com.example.lostfoundapp.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.text.TextUtils;
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

    private Calendar selectedDateCalendar;

    public CreateAdvertFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewModel - scope it to the Activity to share if needed, or Fragment for non-shared
        itemViewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
        selectedDateCalendar = Calendar.getInstance(); // Initialize with current date
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_advert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        radioGroupType = view.findViewById(R.id.radioGroup_type);
        editTextName = view.findViewById(R.id.editText_name);
        editTextPhone = view.findViewById(R.id.editText_phone);
        editTextDescription = view.findViewById(R.id.editText_description);
        buttonDatePicker = view.findViewById(R.id.button_date_picker);
        textViewSelectedDate = view.findViewById(R.id.textView_selected_date);
        editTextLocation = view.findViewById(R.id.editText_location);
        buttonSave = view.findViewById(R.id.button_save);

        updateDateInView(); // Show current date initially or "No date selected" if preferred

        buttonDatePicker.setOnClickListener(v -> showDatePickerDialog());
        buttonSave.setOnClickListener(v -> saveAdvert());
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

        Item newItem = new Item(type, name, phone, description, dateTimestamp, location);
        itemViewModel.insert(newItem);

        Toast.makeText(getContext(), "Advert Saved", Toast.LENGTH_SHORT).show();
        // Navigate back to the previous screen (HomeFragment or ShowItemsFragment)
        navController.popBackStack();
    }
}
