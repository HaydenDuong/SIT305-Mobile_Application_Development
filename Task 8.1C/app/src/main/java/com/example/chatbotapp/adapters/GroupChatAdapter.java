package com.example.chatbotapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbotapp.data.GroupMessage;
import com.example.chatbotapp.R;

import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<GroupMessage> messageList;
    private String currentUserId;

    public GroupChatAdapter(List<GroupMessage> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        GroupMessage message = messageList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else { // VIEW_TYPE_MESSAGE_RECEIVED
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupMessage message = messageList.get(position);
        String formattedTimestamp = formatTimestamp(message.getTimestamp());

        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
            ((SentMessageHolder) holder).bind(message, formattedTimestamp);
        } else {
            ((ReceivedMessageHolder) holder).bind(message, formattedTimestamp);
        }
    }

    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size();
    }

    public void updateMessages(List<GroupMessage> newMessages) {
        this.messageList.clear();
        this.messageList.addAll(newMessages);
        notifyDataSetChanged(); // Consider more efficient updates like DiffUtil for larger lists
    }

    public void addMessage(GroupMessage message) {
        this.messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }
    
    private String formatTimestamp(String isoTimestamp) {
        // Input format from Neo4j: e.g., 2023-10-27T10:30:00.123Z or similar with Z or timezone offset
        // We want to display it in a user-friendly local time format, e.g., "HH:mm" or "MMM dd, HH:mm"
        try {
            // Adjusting for Neo4j DateTime format which may or may not have milliseconds or 'Z'
            // Common ISO 8601 format often includes 'Z' for UTC or an offset like +00:00
            // Sometimes it might be just T between date and time.
            // Example: "2024-03-17T08:05:17.298351Z"
            // Or from backend: "2024-03-17T15:30:53.738011+00:00"
            
            // Try parsing with Z for UTC, then fallback
            SimpleDateFormat sdfInput;
            if (isoTimestamp.endsWith("Z")) {
                sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
                sdfInput.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else if (isoTimestamp.contains("+")) { // Contains timezone offset like +00:00
                 // Handle potential for SSSSSS or SSS, be more flexible
                if (isoTimestamp.substring(isoTimestamp.lastIndexOf('.') + 1, isoTimestamp.lastIndexOf('+')).length() > 3) {
                    sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault());
                } else {
                    sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
                }
            } else { // No Z and no offset, assume it might be local time already or needs UTC assumption
                // This case is less specific, if your backend always sends UTC, this fallback might be less accurate
                 if (isoTimestamp.contains(".")) {
                    sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                } else {
                    sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                }
                 // If timestamps without Z or offset are actually UTC, setTimeZone(TimeZone.getTimeZone("UTC")) here.
            }

            Date date = sdfInput.parse(isoTimestamp);
            
            // Format to a user-friendly local time
            SimpleDateFormat sdfOutput = new SimpleDateFormat("HH:mm", Locale.getDefault());
            sdfOutput.setTimeZone(TimeZone.getDefault()); // Convert to device's local timezone
            return sdfOutput.format(date);
        } catch (ParseException e) {
            // Log.e("GroupChatAdapter", "Error parsing timestamp: " + isoTimestamp, e);
            // Fallback: return a portion of the original string if parsing fails
            if (isoTimestamp != null && isoTimestamp.length() > 16 && isoTimestamp.contains("T")) {
                return isoTimestamp.substring(isoTimestamp.indexOf('T') + 1, isoTimestamp.indexOf('T') + 6);
            }
            return isoTimestamp; // Or return original / empty string
        }
    }


    // ViewHolder for sent messages
    private static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timestampText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textViewMessageText);
            timestampText = itemView.findViewById(R.id.textViewMessageTimestamp);
        }

        void bind(GroupMessage message, String formattedTime) {
            messageText.setText(message.getText());
            timestampText.setText(formattedTime);
        }
    }

    // ViewHolder for received messages
    private static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timestampText, senderIdText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textViewMessageText);
            timestampText = itemView.findViewById(R.id.textViewMessageTimestamp);
            senderIdText = itemView.findViewById(R.id.textViewSenderId);
        }

        void bind(GroupMessage message, String formattedTime) {
            messageText.setText(message.getText());
            timestampText.setText(formattedTime);
            senderIdText.setText(message.getSenderId()); // Display sender's ID
        }
    }
} 