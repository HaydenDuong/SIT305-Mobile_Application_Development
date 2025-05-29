package com.example.chatbotapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatbotapp.LoginActivity;
import com.example.chatbotapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.activity.OnBackPressedCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class UserProfileFragment extends Fragment {

    private CardView userInfoCard;
    private TextView textViewUsername;
    private TextView textViewEmail;
    private Button buttonChatWithAI;
    private Button buttonGoToRecommendations;
    private Button buttonSignOut;
    private FrameLayout interestsFragmentContainer;

    private String userUid;
    private String userDisplayName;
    private String userEmail;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    // Interface for MainActivity to communicate reselection
    public interface OnProfileTabReselectedListener {
        boolean handleTabReselection();
    }

    private OnBackPressedCallback onBackPressedCallback;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String displayNameFromArgs = null;
        if (getArguments() != null) {
            userUid = getArguments().getString("USER_UID");
            displayNameFromArgs = getArguments().getString("USER_DISPLAY_NAME");
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
            if (userUid == null) { // If UID wasn't in args, get from Firebase
                userUid = currentUser.getUid();
            }

            // Prioritize FirebaseUser's display name if available
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                userDisplayName = currentUser.getDisplayName();
            } else if (displayNameFromArgs != null && !displayNameFromArgs.isEmpty()) {
                // Fallback to display name from arguments
                userDisplayName = displayNameFromArgs;
            } else if (userEmail != null) {
                // Further fallback to email local part
                userDisplayName = userEmail.split("@")[0];
            } else {
                userDisplayName = "User"; // Absolute fallback
            }
        } else {
            // No Firebase user, rely on args or defaults
            userUid = (userUid != null) ? userUid : "UnknownUID";
            userDisplayName = (displayNameFromArgs != null && !displayNameFromArgs.isEmpty()) ? displayNameFromArgs : "Guest";
            userEmail = "No email";
        }

        //if (getArguments() != null) {
        //    userUid = getArguments().getString("USER_UID");
        //    userDisplayName = getArguments().getString("USER_DISPLAY_NAME");
        //}
        //FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //if (currentUser != null) {
        //    userEmail = currentUser.getEmail();
        //    if (userUid == null) userUid = currentUser.getUid(); // Ensure UID is set
        //    if ((userDisplayName == null || userDisplayName.isEmpty()) && userEmail != null) {
        //         userDisplayName = userEmail.split("@")[0];
        //    }
        //}

        // Handle back press when InterestsFragment is shown
        onBackPressedCallback = new OnBackPressedCallback(false) { // Initially disabled
            @Override
            public void handleOnBackPressed() {
                // This is called when back is pressed AND callback is enabled
                if (interestsFragmentContainer.getVisibility() == View.VISIBLE) {
                    getChildFragmentManager().popBackStack();
                    setProfileContentVisibility(View.VISIBLE);
                    interestsFragmentContainer.setVisibility(View.GONE);
                    setEnabled(false); // Disable after handling
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this string resource exists
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        mAuth = FirebaseAuth.getInstance(); // Initialize if using Firebase Auth
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        userInfoCard = view.findViewById(R.id.userInfoCard_profileFrag);
        textViewUsername = view.findViewById(R.id.textViewUsername_profileFrag);
        textViewEmail = view.findViewById(R.id.textViewEmail_profileFrag);
        buttonChatWithAI = view.findViewById(R.id.buttonChatWithAI_profileFrag);
        buttonGoToRecommendations = view.findViewById(R.id.buttonGoToRecommendations);
        buttonSignOut = view.findViewById(R.id.buttonSignOut_profileFrag);
        interestsFragmentContainer = view.findViewById(R.id.interests_fragment_container_profileFrag);

        // Set user details
        if (userDisplayName != null && !userDisplayName.isEmpty()) {
            textViewUsername.setText("Username: " + userDisplayName);
        } else {
            textViewUsername.setText("Username: Not available");
        }
        textViewEmail.setText("Email: " + (userEmail != null ? userEmail : "Not available"));

        // Initial state: Show profile, hide interests container
        setProfileContentVisibility(View.VISIBLE);
        interestsFragmentContainer.setVisibility(View.GONE);

        userInfoCard.setOnClickListener(v -> {
            if (userUid != null) {
                loadInterestsFragment();
                setProfileContentVisibility(View.GONE);
                interestsFragmentContainer.setVisibility(View.VISIBLE);
                onBackPressedCallback.setEnabled(true); // Enable custom back press handling
            } else {
                Toast.makeText(getContext(), "User ID not found.", Toast.LENGTH_SHORT).show();
            }
        });

        final NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_container);

        buttonChatWithAI.setOnClickListener(v -> {
            // Pass arguments to ChatFragment if needed
            Bundle chatArgs = new Bundle();
            chatArgs.putString("USER_UID", userUid);
            chatArgs.putString("USER_DISPLAY_NAME", userDisplayName);
            navController.navigate(R.id.action_userProfileFragment_to_chatFragment, chatArgs);
        });

        buttonGoToRecommendations.setOnClickListener(v -> {
            // Navigate to RecommendationsActivity
            if (userUid != null && !userUid.isEmpty()) {
                Bundle args = new Bundle();
                args.putString("currentUserId", userUid); // Use the userUid from the fragment
                Navigation.findNavController(view).navigate(R.id.action_userProfileFragment_to_recommendationsActivity, args);
            } else {
                Toast.makeText(getContext(), "User ID not available. Cannot fetch recommendations.", Toast.LENGTH_LONG).show();
            }
        });

        buttonSignOut.setOnClickListener(v -> signOut());

        return view;
    }

    private void loadInterestsFragment() {
        InterestsFragment fragment = new InterestsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("USER_UID", userUid);
        fragment.setArguments(bundle);

        // Use getChildFragmentManager for fragments within fragments
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.interests_fragment_container_profileFrag, fragment);
        transaction.addToBackStack(null); // Allows pressing back to remove this InterestsFragment
        transaction.commit();
    }

    private void setProfileContentVisibility(int visibility) {
        userInfoCard.setVisibility(visibility);
        buttonChatWithAI.setVisibility(visibility);
        buttonGoToRecommendations.setVisibility(visibility);
        buttonSignOut.setVisibility(visibility);
    }

    public boolean handleProfileTabReselection() {
        if (interestsFragmentContainer.getVisibility() == View.VISIBLE) {
            getChildFragmentManager().popBackStack();
            setProfileContentVisibility(View.VISIBLE);
            interestsFragmentContainer.setVisibility(View.GONE);
            onBackPressedCallback.setEnabled(false); // Disable custom back press handling
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // It's good practice to remove the callback to avoid memory leaks
        if (onBackPressedCallback != null) {
            onBackPressedCallback.remove();
        }
    }

    private void signOut() {
        // Firebase sign out (if using)
        if (mAuth.getCurrentUser() != null) { // Check if a user is signed in with Firebase
            mAuth.signOut();
        }

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            Toast.makeText(getContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
            // Navigate back to LoginActivity or your desired entry point
            Intent intent = new Intent(getActivity(), LoginActivity.class); // Replace LoginActivity with your actual login screen
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finishAffinity(); // Finish all activities in the task associated with this activity
            }
        });
    }
} 