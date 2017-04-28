package ir.logicbase.mojmessenger.chat;

import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.database.chat.ChatEntity;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.util.Time;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * Created by Mahdi on 5/3/2017.
 * Populating chat list recyclerView
 */

class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.ViewHolder> {

    private List<ChatEntity> dataSource;
    private ChatListListener listener;
    private TextView emptyView;

    interface ChatListListener {
        void onClick(ContactEntity contact);
    }

    AdapterChatList(TextView emptyView) {
        this(emptyView, new ArrayList<>());
    }

    AdapterChatList(TextView emptyView, List<ChatEntity> dataSource) {
        this.dataSource = dataSource;
        this.emptyView = emptyView;
        if (dataSource.size() == 0) {
            this.emptyView.setVisibility(View.VISIBLE);
        }
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

    void setListener(ChatListListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        CircleImageView imViewChatPic = cardView.findViewById(R.id.imView_chat_pic);
        TextView txtViewChatTitle = cardView.findViewById(R.id.txtView_chat_title);
        TextView txtViewChatContent = cardView.findViewById(R.id.txtView_chat_content);
        TextView txtViewChatTime = cardView.findViewById(R.id.txtView_chat_time);
        TextView txtViewChatCount = cardView.findViewById(R.id.txtView_chat_count);

        Picasso.with(cardView.getContext())
                .load(dataSource.get(position).getContactEntity().getPic())
                .placeholder(R.drawable.default_person_pic)
                .error(R.drawable.default_person_pic)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(imViewChatPic);
        txtViewChatTitle.setText(dataSource.get(position).getContactEntity().getName());
        txtViewChatContent.setText(dataSource.get(position).getLastMsg());
        txtViewChatTime.setText(Time.getChatListMessageLabel(dataSource.get(position).getTime()));
        if (dataSource.get(position).getCount() == 0) {
            txtViewChatCount.setVisibility(View.INVISIBLE);
        } else {
            txtViewChatCount.setText(String.valueOf(dataSource.get(position).getCount()));
        }


        // Change Fonts
        Typeface typeface = TypefaceManager.get(cardView.getContext(), cardView.getContext().getString(R.string.font_iran_sans));
        txtViewChatTitle.setTypeface(typeface);
        txtViewChatContent.setTypeface(typeface);
        txtViewChatTime.setTypeface(typeface);

        cardView.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPosition = holder.getAdapterPosition();
                listener.onClick(dataSource.get(adapterPosition).getContactEntity());
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    void swapDataSource(List<ChatEntity> dataSource) {
        this.dataSource = dataSource;
        if (dataSource.size() == 0) {
            this.emptyView.setVisibility(View.VISIBLE);
        } else {
            this.emptyView.setVisibility(View.GONE);
        }
        notifyDataSetChanged();
    }

    void updateContact(ContactEntity contact) {
        for (ChatEntity entity : dataSource) {
            if (entity.getContactEntity().getPhone().equals(contact.getPhone())) {
                int position = dataSource.indexOf(entity);
                ContactEntity c = entity.getContactEntity();
                c.setOnline(contact.isOnline());
                c.setLastSeen(contact.getLastSeen());
                c.setPic(contact.getPic());
                entity.setContactEntity(c);
                dataSource.set(position, entity);
                notifyItemChanged(position);
                break;
            }
        }
    }

    void updateChat(ChatEntity chat) {
        boolean isExistInList = false;
        for (ChatEntity entity : dataSource) {
            if (entity.getContactEntity().getId() == chat.getContactEntity().getId()) {
                isExistInList = true;
                int position = dataSource.indexOf(entity);
                dataSource.set(position, chat);
                notifyItemChanged(position);
                break;
            }
        }
        if (!isExistInList) {
            dataSource.add(chat);
            int position = dataSource.indexOf(chat);
            notifyItemChanged(position);
            this.emptyView.setVisibility(View.GONE);
        }
    }
}
