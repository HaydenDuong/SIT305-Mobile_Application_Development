package com.example.personalizedlearningexperienceapp.fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.viewmodels.ProfileViewModel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.IOException;
import java.io.OutputStream;


public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private NavController navController;
    private TextView textViewUsername, textViewEmail, textViewAccountTier;
    private TextView textViewTotalQuestions, textViewCorrectAnswers, textViewIncorrectAnswers;
    private TextView textViewTotalQuestionsLabel;
    private Button buttonUpgradeAccount, buttonShareProfile;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Initialize UI elements
        textViewUsername = view.findViewById(R.id.textViewUsername);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewAccountTier = view.findViewById(R.id.textViewAccountTier);
        textViewTotalQuestionsLabel = view.findViewById(R.id.textViewTotalQuestionsLabel);
        textViewTotalQuestions = view.findViewById(R.id.textViewTotalQuestions);
        textViewCorrectAnswers = view.findViewById(R.id.textViewCorrectAnswers);
        textViewIncorrectAnswers = view.findViewById(R.id.textViewIncorrectAnswers);
        buttonUpgradeAccount = view.findViewById(R.id.buttonUpgradeAccount);
        buttonShareProfile = view.findViewById(R.id.buttonShareProfile);

        // Make entire Total Questions card clickable
        CardView cardViewTotalQuestions = view.findViewById(R.id.cardViewTotalQuestions);
        cardViewTotalQuestions.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.profileFragment) {
                navController.navigate(R.id.action_profileFragment_to_historyFragment);
            }
        });

        buttonUpgradeAccount.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.profileFragment) {
                navController.navigate(R.id.action_profileFragment_to_upgradeAccountFragment);
            }
        });

        buttonShareProfile.setOnClickListener(v -> {
            showQrCodeDialog();
        });

        profileViewModel.loadProfileData();

        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        profileViewModel.loadProfileData();
    }

    private void observeViewModel() {
        profileViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                textViewUsername.setText(user.getUsername());
                textViewEmail.setText(user.getEmail());
            } else {
                textViewUsername.setText("N/A");
                textViewEmail.setText("N/A");
            }
        });

        profileViewModel.getAccountTierLiveData().observe(getViewLifecycleOwner(), tier -> {
            textViewAccountTier.setText(tier != null ? tier : "N/A");
        });

        profileViewModel.getTotalQuestionsAnsweredLiveData().observe(getViewLifecycleOwner(), total -> {
            textViewTotalQuestions.setText(total != null ? String.valueOf(total) : "0");
        });

        profileViewModel.getCorrectAnswersLiveData().observe(getViewLifecycleOwner(), correct -> {
            textViewCorrectAnswers.setText(correct != null ? String.valueOf(correct) : "0");
        });

        profileViewModel.getIncorrectAnswersLiveData().observe(getViewLifecycleOwner(), incorrect -> {
            textViewIncorrectAnswers.setText(incorrect != null ? String.valueOf(incorrect) : "0");
        });
    }

    private void showQrCodeDialog() {
        // Get profile data for QR
        String username = textViewUsername.getText().toString();
        String stats = "Total: " + textViewTotalQuestions.getText() +
                ", Correct: " + textViewCorrectAnswers.getText() +
                ", Incorrect: " + textViewIncorrectAnswers.getText();

        // Create QR content
        String qrContent = "PROFILE\n" +
                "Username: " + username + "\n" +
                "Stats: " + stats + "\n" +
                "App: Personalized Learning Experience";

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_qr_code, null);
        builder.setView(dialogView);

        // Initialize dialog views
        TextView textViewQrUsername = dialogView.findViewById(R.id.textViewQrUsername);
        TextView textViewQrStats = dialogView.findViewById(R.id.textViewQrStats);
        ImageView imageViewQrCode = dialogView.findViewById(R.id.imageViewQrCode);
        Button buttonSaveQr = dialogView.findViewById(R.id.buttonSaveQr);
        Button buttonCloseQr = dialogView.findViewById(R.id.buttonCloseQr);

        // Set text
        textViewQrUsername.setText("Username: " + username);
        textViewQrStats.setText(stats);

        // Generate QR Code
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    500, 500
            );

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageViewQrCode.setImageBitmap(bitmap);

            // Create and show the dialog
            AlertDialog dialog = builder.create();

            // Set save button click listener
            buttonSaveQr.setOnClickListener(v -> {
                saveQrCodeToGallery(bitmap);
            });

            // Set close button click listener
            buttonCloseQr.setOnClickListener(v -> {
                dialog.dismiss();
            });

            dialog.show();

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQrCodeToGallery(Bitmap qrBitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "profile_qr_code_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PersonalizedLearning");

            ContentResolver resolver = requireContext().getContentResolver();
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            try {
                if (uri != null) {
                    try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                        qrBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        Toast.makeText(getContext(), "QR code saved to gallery", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to save QR code", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            String savedImagePath = MediaStore.Images.Media.insertImage(
                    requireContext().getContentResolver(),
                    qrBitmap,
                    "Profile QR Code",
                    "QR Code for profile sharing"
            );

            if (savedImagePath != null) {
                Toast.makeText(getContext(), "QR code saved to gallery", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to save QR code", Toast.LENGTH_SHORT).show();
            }
        }
    }
}