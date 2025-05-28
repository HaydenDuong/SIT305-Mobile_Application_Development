package com.example.chatbotapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfileFragment extends Fragment {

    private CardView userInfoCard;
    private TextView textViewUsername;
    private TextView textViewEmail;
    private Button buttonChatWithAI;
    private Button buttonChatWithGroup;
    private Button buttonSignOut;
    private FrameLayout interestsFragmentContainer;

    private View mainProfileContent; // To group card and buttons for easier visibility toggling

    private String userUid;
    private String userDisplayName;
    private String userEmail;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve arguments passed from MainActivity (or NavController)
        if (getArguments() != null) {
            userUid = getArguments().getString("USER_UID");
            userDisplayName = getArguments().getString("USER_DISPLAY_NAME");
        }
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
            if (userUid == null) userUid = currentUser.getUid(); // Ensure UID is set
            if ((userDisplayName == null || userDisplayName.isEmpty()) && userEmail != null) {
                 userDisplayName = userEmail.split("@")[0];
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        userInfoCard = view.findViewById(R.id.userInfoCard_profileFrag);
        textViewUsername = view.findViewById(R.id.textViewUsername_profileFrag);
        textViewEmail = view.findViewById(R.id.textViewEmail_profileFrag);
        buttonChatWithAI = view.findViewById(R.id.buttonChatWithAI_profileFrag);
        buttonChatWithGroup = view.findViewById(R.id.buttonChatWithGroup_profileFrag);
        buttonSignOut = view.findViewById(R.id.buttonSignOut_profileFrag);
        interestsFragmentContainer = view.findViewById(R.id.interests_fragment_container_profileFrag);

        // Grouping main content for easier show/hide
        // In fragment_user_profile.xml, you might want to wrap userInfoCard and the three buttons
        // in a LinearLayout or some other ViewGroup and give it an ID to simplify this.
        // For now, I'll handle them individually.

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

        buttonChatWithGroup.setOnClickListener(v -> {
             // Pass arguments to GroupChatFragment if needed
            Bundle groupArgs = new Bundle();
            groupArgs.putString("USER_UID", userUid);
            navController.navigate(R.id.action_userProfileFragment_to_groupChatFragment, groupArgs);
            // Toast.makeText(getContext(), "Group chat feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        buttonSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

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
        buttonChatWithGroup.setVisibility(visibility);
        buttonSignOut.setVisibility(visibility);
    }
    
    // Handle back press from InterestsFragment
    // This needs to be coordinated with MainActivity's onBackPressed if it also handles back stack for main navigation
    // For fragments loaded with getChildFragmentManager, the child fragment manager handles its own back stack.
    // So, when InterestsFragment is shown, a physical back press should pop it from child back stack first.
} 