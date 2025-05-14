package com.example.chatbotapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbotapp.R;
import com.example.chatbotapp.data.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER_MESSAGE = 1;
    private static final int VIEW_TYPE_BOT_MESSAGE = 2;

    private List<ChatMessage> chatMessagesList;
    private String currentUsername;

    public ChatAdapter(List<ChatMessage> chatMessagesList, String username) {
        this.chatMessagesList = chatMessagesList;
        this.currentUsername = username;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessagesList.get(position);
        if (message.getSenderType() == ChatMessage.SenderType.USER) {
            return VIEW_TYPE_USER_MESSAGE;
        } else {
            return VIEW_TYPE_BOT_MESSAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER_MESSAGE) {
            View view = inflater.inflate(R.layout.item_chat_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else { // VIEW_TYPE_BOT_MESSAGE
            View view = inflater.inflate(R.layout.item_chat_message_bot, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessagesList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_USER_MESSAGE) {
            ((UserMessageViewHolder) holder).bind(message, currentUsername);
        } else {
            ((BotMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessagesList == null ? 0 : chatMessagesList.size();
    }

    // ViewHolder for User messages
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessageUser;
        TextView textViewAvatarUser;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessageUser = itemView.findViewById(R.id.textViewMessageUser);
            textViewAvatarUser = itemView.findViewById(R.id.imageViewAvatarUser);
        }

        void bind(ChatMessage message, String username) {
            textViewMessageUser.setText(message.getMessageText());
            if (username != null && !username.isEmpty()) {
                textViewAvatarUser.setText(username.substring(0, 1).toUpperCase());
            } else {
                textViewAvatarUser.setText("User");
            }
        }
    }

    // ViewHolder for Bot messages
    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessageBot;
        // The bot avatar is an ImageView with a placeholder icon in item_chat_message_bot.xml
        // ImageView imageViewAvatarBot;

        public BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessageBot = itemView.findViewById(R.id.textViewMessageBot);
            // imageViewAvatarBot = itemView.findViewById(R.id.imageViewAvatarBot);
        }

        void bind(ChatMessage message) {
            textViewMessageBot.setText(message.getMessageText());
            // Bot avatar icon is static in XML (placeholder), no need to set here unless dynamic
        }
    }

    // Helper method to add a message to the list and notify the adapter
    public void addMessage(ChatMessage message) {
        chatMessagesList.add(message);
        // Notify that an item has been inserted at the last position
        notifyItemInserted(chatMessagesList.size() - 1);
    }
}
