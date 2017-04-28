package ir.logicbase.mojmessenger.adapter;

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

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.bundle.BundleMessage;
import ir.logicbase.mojmessenger.util.PrefManager;
import ir.logicbase.mojmessenger.util.Time;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * Created by Mahdi on 6/13/2017.
 * Populating Conversation list recyclerView
 */

public class AdapterConversationList extends RecyclerView.Adapter<AdapterConversationList.ViewHolder> {

    private ArrayList<BundleMessage> conversationList;
    private ConversationListListener listener;
    private static final int LAYOUT_TYPE_MSG_IN = 1;
    private static final int LAYOUT_TYPE_MSG_OUT = 2;
    private TextView emptyView;

    public AdapterConversationList(TextView emptyView) {
        this(emptyView, new ArrayList<>());
    }

    public AdapterConversationList(TextView emptyView, ArrayList<BundleMessage> conversationList) {
        this.conversationList = conversationList;
        this.emptyView = emptyView;
        if (conversationList.size() == 0) {
            this.emptyView.setVisibility(View.VISIBLE);
        }
    }

    public interface ConversationListListener {

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;

        public ViewHolder(CardView v) {
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

    public void setListener(ConversationListListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        TextView timestamp = (TextView) cardView.findViewById(R.id.txtView_conversation_timestamp);
        TextView message = (TextView) cardView.findViewById(R.id.txtView_conversation_message);
        final Context context = holder.cardView.getContext();
        Typeface typeface = TypefaceManager.get(context, context.getString(R.string.font_iran_sans));
        timestamp.setText(Time.getConversationMessageLabel(conversationList.get(position).getTimestamp()));
        message.setText(conversationList.get(position).getMsgContent());
        message.setTypeface(typeface);
        int fontSize = new PrefManager(context).getSettingsPrefConversationFontSize();
        message.setTextSize(TypedValue.COMPLEX_UNIT_SP,fontSize);
        if (getItemViewType(position) == LAYOUT_TYPE_MSG_OUT) {
            ImageView msgStatus = (ImageView) cardView.findViewById(R.id.imView_conversation_msg_status);
            if (!conversationList.get(position).isSync()) {
                msgStatus.setImageResource(R.drawable.ic_time_wait);
            } else if (conversationList.get(position).isSync()) {
                msgStatus.setImageResource(R.drawable.ic_one_tick);
            }
            if (conversationList.get(position).isSeen()) {
                msgStatus.setImageResource(R.drawable.ic_two_tick);
            }
        }
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (conversationList.get(position).isYours()) {
            return LAYOUT_TYPE_MSG_OUT;
        } else {
            return LAYOUT_TYPE_MSG_IN;
        }
    }

    public void addItem(BundleMessage item) {
        this.conversationList.add(item);
        this.emptyView.setVisibility(View.GONE);
    }
}
