package ir.logicbase.mojmessenger.adapter;

import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.bundle.BundleChat;
import ir.logicbase.mojmessenger.bundle.BundleContact;
import ir.logicbase.mojmessenger.util.Time;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * Created by Mahdi on 5/3/2017.
 * Populating chat list recyclerView
 */

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.ViewHolder> {

    private ArrayList<BundleChat> chatList;
    private ChatListListener listener;

    public interface ChatListListener {
        void onClick(BundleContact contact);
    }

    public AdapterChatList(ArrayList<BundleChat> chatList) {
        this.chatList = chatList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;

        ViewHolder(CardView v) {
            super(v);
            this.cardView = v;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_chat_list, parent, false);
        return new ViewHolder(cv);
    }

    public void setListener(ChatListListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        CircleImageView imViewChatPic = (CircleImageView) cardView.findViewById(R.id.imView_chat_pic);
        TextView txtViewChatTitle = (TextView) cardView.findViewById(R.id.txtView_chat_title);
        TextView txtViewChatContent = (TextView) cardView.findViewById(R.id.txtView_chat_content);
        TextView txtViewChatTime = (TextView) cardView.findViewById(R.id.txtView_chat_time);
        TextView txtViewChatCount = (TextView) cardView.findViewById(R.id.txtView_chat_count);

        String url = "http://vvcexpl.com/wordpress/wp-content/uploads/2013/09/profile-default-male.png1";
        Picasso.with(cardView.getContext())
                .load(url)
                .placeholder(R.drawable.default_person_pic)
                .error(R.drawable.default_person_pic)
                .into(imViewChatPic);
        txtViewChatTitle.setText(chatList.get(position).getContact().getName());
        txtViewChatContent.setText(chatList.get(position).getLastMsg());
        txtViewChatTime.setText(Time.getChatListMessageLabel(chatList.get(position).getTime()));
        if (chatList.get(position).getCount() == 0) {
            txtViewChatCount.setVisibility(View.INVISIBLE);
        } else {
            txtViewChatCount.setText(String.valueOf(chatList.get(position).getCount()));
        }

        /*
        Change Fonts
         */
        Typeface typeface = TypefaceManager.get(cardView.getContext(), cardView.getContext().getString(R.string.font_iran_sans));
        txtViewChatTitle.setTypeface(typeface);
        txtViewChatContent.setTypeface(typeface);
        txtViewChatTime.setTypeface(typeface);

        cardView.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPosition = holder.getAdapterPosition();
                listener.onClick(chatList.get(adapterPosition).getContact());
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}
