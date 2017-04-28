package ir.logicbase.mojmessenger.conversation;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.database.message.MessageEntity;
import ir.logicbase.mojmessenger.util.PrefManager;
import ir.logicbase.mojmessenger.util.Time;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * Created by Mahdi on 6/13/2017.
 * Populating Conversation list recyclerView
 */

class AdapterConversationList extends RecyclerView.Adapter<AdapterConversationList.ViewHolder> {

    private List<MessageEntity> dataSource;
    private ConversationListListener listener;
    private TextView emptyView;

    private static final int LAYOUT_TYPE_MSG_IN = 1;
    private static final int LAYOUT_TYPE_MSG_OUT = 2;

    AdapterConversationList(TextView emptyView) {
        this(emptyView, new ArrayList<>());
    }

    AdapterConversationList(TextView emptyView, List<MessageEntity> dataSource) {
        this.dataSource = dataSource;
        this.emptyView = emptyView;
        if (dataSource.size() == 0) {
            this.emptyView.setVisibility(View.VISIBLE);
        }
    }

    interface ConversationListListener {

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
        CardView cv = null;
        switch (viewType) {
            case LAYOUT_TYPE_MSG_IN:
                cv = (CardView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_msg_pv_in_conversation_list, parent, false);
                break;
            case LAYOUT_TYPE_MSG_OUT:
                cv = (CardView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_msg_out_conversation_list, parent, false);
        }
        return new ViewHolder(cv);
    }

    void setListener(ConversationListListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        TextView timestamp = cardView.findViewById(R.id.txtView_conversation_timestamp);
        TextView message = cardView.findViewById(R.id.txtView_conversation_message);
        final Context context = holder.cardView.getContext();
        Typeface typeface = TypefaceManager.get(context, context.getString(R.string.font_iran_sans));
        timestamp.setText(Time.getConversationMessageLabel(dataSource.get(position).getTimestamp()));
        message.setText(dataSource.get(position).getContent());
        message.setTypeface(typeface);
        int fontSize = new PrefManager(context).getSettingsPrefConversationFontSize();
        message.setTextSize(TypedValue.COMPLEX_UNIT_SP,fontSize);
        if (getItemViewType(position) == LAYOUT_TYPE_MSG_OUT) {
            ImageView msgStatus = cardView.findViewById(R.id.imView_conversation_msg_status);
            if (dataSource.get(position).getSync() == 0) {
                msgStatus.setImageResource(R.drawable.ic_time_wait);
            } else if (dataSource.get(position).getSync() == 1) {
                msgStatus.setImageResource(R.drawable.ic_one_tick);
            }
            if (dataSource.get(position).getSeen() == 1) {
                msgStatus.setImageResource(R.drawable.ic_two_tick);
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (dataSource.get(position).getIsYours() == 1) {
            return LAYOUT_TYPE_MSG_OUT;
        } else {
            return LAYOUT_TYPE_MSG_IN;
        }
    }

    void addItem(MessageEntity item, RecyclerView recyclerView) {
        this.dataSource.add(item);
        this.emptyView.setVisibility(View.GONE);
        int position = dataSource.indexOf(item);
        notifyItemChanged(position);
        int lastPosition = getItemCount() - 1;
        recyclerView.smoothScrollToPosition(lastPosition);
    }

    void swapDataSource(List<MessageEntity> dataSource) {
        this.dataSource = dataSource;
        if (dataSource.size() == 0) {
            this.emptyView.setVisibility(View.VISIBLE);
        } else {
            this.emptyView.setVisibility(View.GONE);
        }
        notifyDataSetChanged();
    }

    void updateItem(MessageEntity item) {
        for (MessageEntity entity : dataSource) {
            if (entity.getId() == item.getId()) {
                int position = dataSource.indexOf(entity);
                dataSource.set(position, item);
                notifyItemChanged(position);
            }
        }
    }

}
