package com.example.itubeapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.itubeapp.R;
import com.example.itubeapp.data.DatabaseClient;
import com.example.itubeapp.data.Playlist;

public class HomeFragment extends Fragment {

    private int userId;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Get userId from Arguments
        if (getArguments() != null) {
            userId = getArguments().getInt("userId", -1);
        }

        EditText videoUrlEditText = view.findViewById(R.id.videoUrlEditText);
        Button playButton = view.findViewById(R.id.playButton);
        Button addToPlaylistButton = view.findViewById(R.id.addToPlaylistButton);

        playButton.setOnClickListener(v -> {
            String videoUrl = videoUrlEditText.getText().toString().trim();
            if (videoUrl.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a video URL", Toast.LENGTH_SHORT).show();
                return;
            }
            // Chuyển sang PlayVideoFragment
            PlayVideoFragment playVideoFragment = new PlayVideoFragment();
            Bundle bundle = new Bundle();
            bundle.putString("videoUrl", videoUrl);
            playVideoFragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, playVideoFragment)
                    .addToBackStack(null)
                    .commit();
        });

        addToPlaylistButton.setOnClickListener(v -> {
            String videoUrl = videoUrlEditText.getText().toString().trim();
            if (videoUrl.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a video URL", Toast.LENGTH_SHORT).show();
                return;
            }
            if (userId == -1) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            Playlist playlist = new Playlist(userId, videoUrl);
            new Thread(() -> {
                try {
                    DatabaseClient.getInstance(getContext()).getAppDatabase()
                            .playlistDao()
                            .insertPlaylist(playlist);
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Added to playlist!", Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });

        return view;
    }
}