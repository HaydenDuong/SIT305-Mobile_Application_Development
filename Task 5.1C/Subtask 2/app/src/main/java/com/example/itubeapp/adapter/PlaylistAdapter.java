package com.example.itubeapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.itubeapp.data.Playlist;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private List<Playlist> playlistList;
    private OnPlaylistClickListener onPlaylistClickListener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    public PlaylistAdapter(List<Playlist> playlistList) {
        this.playlistList = playlistList;
    }

    public void setOnPlaylistClickListener(OnPlaylistClickListener listener) {
        this.onPlaylistClickListener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlistList.get(position);
        holder.textView.setText(playlist.getVideoURL());

        holder.itemView.setOnClickListener(v -> {
            if (onPlaylistClickListener != null) {
                onPlaylistClickListener.onPlaylistClick(playlist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistList != null ? playlistList.size() : 0;
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
