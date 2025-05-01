package com.example.itubeapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.itubeapp.R;
import com.example.itubeapp.adapter.PlaylistAdapter;
import com.example.itubeapp.data.DatabaseClient;
import com.example.itubeapp.data.Playlist;

import java.util.List;

public class PlaylistFragment extends Fragment {

    private int userId;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        if (getArguments() != null) {
            userId = getArguments().getInt("userId", -1);
        }

        RecyclerView playlistRecyclerView = view.findViewById(R.id.playlistRecyclerView);
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Lấy danh sách playlist từ database
        new Thread(() -> {
            List<Playlist> playlists = DatabaseClient.getInstance(getContext()).getAppDatabase()
                    .playlistDao()
                    .getPlaylistsForUser(userId);
            getActivity().runOnUiThread(() -> {
                PlaylistAdapter adapter = new PlaylistAdapter(playlists);
                adapter.setOnPlaylistClickListener(playlist -> {
                    // Chuyển sang PlayVideoFragment
                    PlayVideoFragment playVideoFragment = new PlayVideoFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("videoUrl", playlist.getVideoURL());
                    playVideoFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, playVideoFragment)
                            .addToBackStack(null)
                            .commit();
                });
                playlistRecyclerView.setAdapter(adapter);
            });
        }).start();

        return view;
    }
}