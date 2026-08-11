package com.caspian.betab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.noties.markwon.Markwon;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final Context context;
    private final List<ChatMessage> messageList;
    private final Markwon markwon;

    public ChatAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.markwon = Markwon.create(context);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);

        if (msg.type == ChatMessage.TYPE_USER) {
            holder.containerUser.setVisibility(View.VISIBLE);
            holder.containerAssistant.setVisibility(View.GONE);
            holder.tvUserText.setText(msg.text);
        } else {
            holder.containerUser.setVisibility(View.GONE);
            holder.containerAssistant.setVisibility(View.VISIBLE);

            String badgeText = "gemini".equalsIgnoreCase(msg.modelService) ? "GEMINI 1.5 PRO" : "CHATGPT 4O";
            holder.tvModelBadge.setText(badgeText);

            markwon.setMarkdown(holder.tvAssistantText, msg.text != null ? msg.text : "");
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout containerUser;
        LinearLayout containerAssistant;
        TextView tvUserText;
        TextView tvAssistantText;
        TextView tvModelBadge;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            containerUser = itemView.findViewById(R.id.container_user_msg);
            containerAssistant = itemView.findViewById(R.id.container_assistant_msg);
            tvUserText = itemView.findViewById(R.id.tv_user_text);
            tvAssistantText = itemView.findViewById(R.id.tv_assistant_text);
            tvModelBadge = itemView.findViewById(R.id.tv_model_badge);
        }
    }
}
