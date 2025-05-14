package com.example.lostfoundapp.adapters; // Or your adapter package

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lostfoundapp.R;
import com.example.lostfoundapp.data.Item;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class ItemAdapter extends ListAdapter<Item, ItemAdapter.ItemViewHolder> {

    private OnItemClickListener listener;

    public ItemAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            // Use Objects.equals for null-safe comparisons
            return Objects.equals(oldItem.getName(), newItem.getName()) &&
                    Objects.equals(oldItem.getType(), newItem.getType()) &&
                    Objects.equals(oldItem.getDescription(), newItem.getDescription()) && // Also check description
                    Objects.equals(oldItem.getDate(), newItem.getDate()) &&
                    Objects.equals(oldItem.getLocation(), newItem.getLocation());
        }
    };

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_advert, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item currentItem = getItem(position);
        holder.textViewName.setText(currentItem.getName());
        holder.textViewType.setText(currentItem.getType().toUpperCase(Locale.ROOT));
        holder.textViewLocation.setText(currentItem.getLocation());

        if (currentItem.getDate() != null) {
            // Simple date formatting, could be more elaborate (e.g., "2 days ago")
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.textViewDate.setText(sdf.format(new Date(currentItem.getDate())));
        } else {
            holder.textViewDate.setText("N/A");
        }
    }

    public Item getItemAt(int position) {
        return getItem(position);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private TextView textViewType;
        private TextView textViewDate;
        private TextView textViewLocation;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textView_item_name);
            textViewType = itemView.findViewById(R.id.textView_item_type);
            textViewDate = itemView.findViewById(R.id.textView_item_date);
            textViewLocation = itemView.findViewById(R.id.textView_item_location);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
