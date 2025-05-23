package com.example.personalizedlearningexperienceapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.data.QuizRepository;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.Locale;

import com.google.android.gms.common.api.CommonStatusCodes;

public class UpgradeAccountFragment extends Fragment {

    private static final String TAG = "UpgradeAccountFragment";
    private NavController navController;
    private Button buttonPurchaseStarter, buttonPurchaseIntermediate, buttonPurchaseAdvanced;
    private PaymentsClient paymentsClient;
    private String selectedTier = "";

    private QuizRepository quizRepository;
    private ExecutorService executorService;
    private int currentUserId;

    // ActivityResultLauncher for handling payment resolution
    private ActivityResultLauncher<IntentSenderRequest> resolvePaymentForResult;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize executor service
        executorService = Executors.newSingleThreadExecutor();

        // Get user ID from shared preferences
        SharedPreferences prefs = requireActivity().getSharedPreferences(SignUpFragment.PREFS_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getInt(SignUpFragment.KEY_USER_ID, SignUpFragment.DEFAULT_USER_ID);

        // Initialize repository
        quizRepository = new QuizRepository(requireActivity().getApplication());

        // Initialize Google Pay
        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build();
        paymentsClient = Wallet.getPaymentsClient(requireActivity(), walletOptions);

        // Initialize the ActivityResultLauncher
        resolvePaymentForResult = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            if (data != null) {
                                PaymentData paymentData = PaymentData.getFromIntent(data);
                                if (paymentData != null) {
                                    handlePaymentSuccess(paymentData);
                                }
                            }
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(requireContext(), "Payment canceled", Toast.LENGTH_SHORT).show();
                            break;
                        case AutoResolveHelper.RESULT_ERROR:
                            Status status = AutoResolveHelper.getStatusFromIntent(data);
                            if (status != null) {
                                Log.e(TAG, "Payment error: " + status.getStatusMessage() + " Code: " + status.getStatusCode());
                                Toast.makeText(requireContext(), "Payment error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                            }
                            break;
                    }
                });

        isReadyToPay();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upgrade_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        // Initialize buttons
        buttonPurchaseStarter = view.findViewById(R.id.buttonPurchaseStarter);
        buttonPurchaseIntermediate = view.findViewById(R.id.buttonPurchaseIntermediate);
        buttonPurchaseAdvanced = view.findViewById(R.id.buttonPurchaseAdvanced);

        // Set button click listeners
        buttonPurchaseStarter.setOnClickListener(v -> {
            selectedTier = "starter";
            requestPayment(4.99);
        });

        buttonPurchaseIntermediate.setOnClickListener(v -> {
            selectedTier = "intermediate";
            requestPayment(9.99);
        });

        buttonPurchaseAdvanced.setOnClickListener(v -> {
            selectedTier = "advanced";
            requestPayment(14.99);
        });
    }

    private void isReadyToPay() {
        try {
            JSONObject isReadyToPayJson = new JSONObject()
                    .put("apiVersion", 2)
                    .put("apiVersionMinor", 0)
                    .put("allowedPaymentMethods", new JSONArray()
                            .put(new JSONObject()
                                    .put("type", "CARD")
                                    .put("parameters", new JSONObject()
                                            .put("allowedAuthMethods", new JSONArray()
                                                    .put("PAN_ONLY")
                                                    .put("CRYPTOGRAM_3DS"))
                                            .put("allowedCardNetworks", new JSONArray()
                                                    .put("AMEX")
                                                    .put("DISCOVER")
                                                    .put("MASTERCARD")
                                                    .put("VISA")))));

            IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString());

            if (request == null) {
                Log.e(TAG, "IsReadyToPayRequest is null after creation.");
                disablePaymentButtons();
                return;
            }

            Task<Boolean> task = paymentsClient.isReadyToPay(request);
            task.addOnCompleteListener(requireActivity(),
                    task1 -> {
                        try {
                            boolean result = task1.getResult(ApiException.class);
                            if (!result) {
                                Log.w(TAG, "Google Pay is not available.");
                                disablePaymentButtons();
                            } else {
                                Log.i(TAG, "Google Pay is available.");
                            }
                        } catch (ApiException exception) {
                            Log.e(TAG, "isReadyToPay failed: " + exception.getMessage() + " Status code: " + exception.getStatusCode(), exception);
                            disablePaymentButtons();
                        }
                    });
        } catch (JSONException e) {
            Log.e(TAG, "Error creating isReadyToPayJson: " + e.getMessage(), e);
            disablePaymentButtons();
        }
    }

    private void disablePaymentButtons() {
        if (buttonPurchaseStarter != null) buttonPurchaseStarter.setEnabled(false);
        if (buttonPurchaseIntermediate != null) buttonPurchaseIntermediate.setEnabled(false);
        if (buttonPurchaseAdvanced != null) buttonPurchaseAdvanced.setEnabled(false);

        Toast.makeText(requireContext(), "Google Pay is not available on this device", Toast.LENGTH_LONG).show();
    }

    private void requestPayment(double price) {
        try {
            JSONObject paymentDataRequestJson = createPaymentDataRequest(price);
            PaymentDataRequest request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString());

            if (request != null) {
                Task<PaymentData> paymentDataTask = paymentsClient.loadPaymentData(request);

                paymentDataTask.addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        handlePaymentSuccess(task.getResult());
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Status status = apiException.getStatus();
                            if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED && status.hasResolution()) {
                                try {
                                    IntentSenderRequest intentSenderRequest =
                                            new IntentSenderRequest.Builder(status.getResolution().getIntentSender())
                                                    .build();
                                    resolvePaymentForResult.launch(intentSenderRequest);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error launching payment resolution: " + e.getMessage(), e);
                                    Toast.makeText(requireContext(), "Payment error.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "Payment failed with unresolvable error: " + apiException.getMessage() + " Code: " + status.getStatusCode(), apiException);
                                Toast.makeText(requireContext(), "Payment failed: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e(TAG, "Payment failed with non-API exception: " + (exception != null ? exception.getMessage() : "Unknown error"), exception);
                            Toast.makeText(requireContext(), "Payment failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Log.e(TAG, "PaymentDataRequest is null.");
                Toast.makeText(requireContext(), "Error processing payment request.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error creating payment request JSON: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error processing payment.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        Log.i(TAG, "Payment successful. Updating user tier.");
        updateUserTier();
    }

    private JSONObject createPaymentDataRequest(double price) throws JSONException {
        JSONObject paymentDataRequest = new JSONObject();
        paymentDataRequest.put("apiVersion", 2);
        paymentDataRequest.put("apiVersionMinor", 0);

        JSONObject merchantInfo = new JSONObject();
        merchantInfo.put("merchantName", "Personalized Learning Experience"); 
        paymentDataRequest.put("merchantInfo", merchantInfo);

        JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");
        JSONObject cardParameters = new JSONObject();
        cardParameters.put("allowedAuthMethods", new JSONArray().put("PAN_ONLY").put("CRYPTOGRAM_3DS"));
        cardParameters.put("allowedCardNetworks", new JSONArray().put("AMEX").put("DISCOVER").put("MASTERCARD").put("VISA"));
        cardPaymentMethod.put("parameters", cardParameters);

        JSONObject tokenizationSpec = new JSONObject();
        tokenizationSpec.put("type", "PAYMENT_GATEWAY");
        JSONObject tokenizationParams = new JSONObject();
        tokenizationParams.put("gateway", "example"); // Placeholder for test environment
        tokenizationParams.put("gatewayMerchantId", "exampleGatewayMerchantId"); // Placeholder
        tokenizationSpec.put("parameters", tokenizationParams);
        cardPaymentMethod.put("tokenizationSpecification", tokenizationSpec);

        JSONArray allowedPaymentMethods = new JSONArray();
        allowedPaymentMethods.put(cardPaymentMethod);
        paymentDataRequest.put("allowedPaymentMethods", allowedPaymentMethods);

        JSONObject transactionInfo = new JSONObject();
        transactionInfo.put("totalPrice", String.format(Locale.US, "%.2f", price));
        transactionInfo.put("totalPriceStatus", "FINAL");
        transactionInfo.put("currencyCode", "USD");
        paymentDataRequest.put("transactionInfo", transactionInfo);
        
        try {
            Log.d(TAG, "PaymentDataRequest JSON: " + paymentDataRequest.toString(2)); // toString(2) for pretty print
        } catch (JSONException e) {
            Log.e(TAG, "Error pretty printing JSON for PaymentDataRequest", e);
        }

        return paymentDataRequest;
    }

    private void updateUserTier() {
        // Update user tier in database
        if (currentUserId != SignUpFragment.DEFAULT_USER_ID && !selectedTier.isEmpty()) {
            quizRepository.updateUserTier(String.valueOf(currentUserId), selectedTier);

            Toast.makeText(requireContext(),
                    "Payment successful! Your account has been upgraded to " + selectedTier,
                    Toast.LENGTH_LONG).show();

            // Navigate back to profile
            navController.navigateUp();
        } else {
            Toast.makeText(requireContext(), "Error updating account", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}