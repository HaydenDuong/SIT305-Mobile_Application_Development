package com.example.unitconverterapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Spinner conversionTypeSpinner, sourceSpinner, destinationSpinner;
    private EditText inputText;
    private Button convertButton;
    private TextView resultText;


    // Function for converting Length unit
    private double convertLength(double value, String fromUnit, String toUnit) {
        switch (fromUnit) {
            case "Inch":
                value *= 2.54; // 1 inch = 2.54 cm
                break;
            case "Foot":
                value *= 30.48; // 1 foot = 30.48 cm
                break;
            case "Yard":
                value *= 91.44; // 1 yard = 91.44 cm
                break;
            case "Mile":
                value *= 160934; // 1 mile = 160934 cm
                break;
        }

        switch (toUnit) {
            case "Centimeter (cm)":
                break;
            case "Kilometer (km)":
                value /= 100000; // 1 km = 100000 cm
                break;
        }

        return value;
    }

    // Function for converting Weight unit
    private double convertWeight(double value, String fromUnit, String toUnit) {
        switch (fromUnit) {
            case "Pound":
                value *= 0.453592; // 1 pound = 0.453592 kg
                break;
            case "Ounce":
                value *= 0.0283495; // 1 ounce = 0.0283495 kg
                break;
            case "Ton":
                value *= 907.185; // 1 ton = 907.185 kg
                break;
        }

        switch (toUnit) {
            case "Kilogram (kg)":
                break;
            case "Gram (g)":
                value *= 1000; // 1 kg = 1000 g
                break;
        }

        return value;
    }

    // Function for converting Temperature unit
    private double convertTemperature(double value, String fromUnit, String toUnit) {
        switch (fromUnit) {
            case "Fahrenheit":
                value = (value - 32) / 1.8; // F to C
                break;
            case "Kelvin":
                value = value - 273.15; // K to C
                break;
        }

        switch (toUnit) {
            case "Celsius":
                break;
            case "Fahrenheit":
                value = (value * 1.8) + 32; // C to F
                break;
            case "Kelvin":
                value = value + 273.15; // C to K
                break;
        }

        return value;
    }

    // Function for display the correct Source and Destination Unit upon the selected "Conversion Type"
    private void updateUnitSpinners(String conversionType) {
        ArrayAdapter<CharSequence> sourceAdapter;
        ArrayAdapter<CharSequence> destinationAdapter;

        switch (conversionType) {
            case "Length":
                sourceAdapter = ArrayAdapter.createFromResource(this, R.array.length_source_unit, android.R.layout.simple_spinner_item);
                destinationAdapter = ArrayAdapter.createFromResource(this, R.array.length_destination_unit, android.R.layout.simple_spinner_item);
                break;

            case "Weight":
                sourceAdapter = ArrayAdapter.createFromResource(this, R.array.weight_source_unit, android.R.layout.simple_spinner_item);
                destinationAdapter = ArrayAdapter.createFromResource(this, R.array.weight_destination_unit, android.R.layout.simple_spinner_item);
                break;

            case "Temperature":
                sourceAdapter = ArrayAdapter.createFromResource(this, R.array.temperature_drop_menu, android.R.layout.simple_spinner_item);
                destinationAdapter = ArrayAdapter.createFromResource(this, R.array.temperature_drop_menu, android.R.layout.simple_spinner_item);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + conversionType);
        }

        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destinationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sourceSpinner.setAdapter(sourceAdapter);
        destinationSpinner.setAdapter(destinationAdapter);
    }

    // Function for calling the corresponding Converting Type Function
    private double convertUnit(double value, String fromUnit, String toUnit, String conversionType) {
        switch (conversionType) {
            case "Length":
                return convertLength(value, fromUnit, toUnit);
            case "Weight":
                return convertWeight(value, fromUnit, toUnit);
            case "Temperature":
                return convertTemperature(value, fromUnit, toUnit);
            default:
                throw new IllegalArgumentException("Invalid conversion type: " + conversionType);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Mapping XML elements from activity_main.xml to theirs Java variables
        conversionTypeSpinner = findViewById(R.id.conversionType);
        sourceSpinner = findViewById(R.id.dropDownSource);
        destinationSpinner = findViewById(R.id.dropDownDestination);
        inputText = findViewById(R.id.inputText);
        convertButton = findViewById(R.id.convertButton);
        resultText = findViewById(R.id.resultText);

        // Populate data for "Type of Conversion" spinner
        ArrayAdapter<CharSequence> conversionTypeAdapter = ArrayAdapter.createFromResource(this, R.array.conversion_types, android.R.layout.simple_spinner_item);
        conversionTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conversionTypeSpinner.setAdapter(conversionTypeAdapter);

        // Handling event during user selection for "Type of Conversion" Drop-down menu
        // Display the corresponding source, and destination units upon the selected conversion type
        conversionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                updateUnitSpinners(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Event-handler for clicking "Convert" button
        convertButton.setOnClickListener(v -> {
            // Checking the input from inputText field
            String inputValueStr = inputText.getText().toString().trim();

            if (inputValueStr.isEmpty()) {
                Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Convert input value into double-type data
                double inputValue = Double.parseDouble(inputValueStr);

                String conversionType = conversionTypeSpinner.getSelectedItem().toString();
                String sourceUnit = sourceSpinner.getSelectedItem().toString();
                String destinationUnit = destinationSpinner.getSelectedItem().toString();

                double result = convertUnit(inputValue, sourceUnit, destinationUnit, conversionType);
                resultText.setText(String.valueOf(result));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });
    }
}