package ir.logicbase.mojmessenger.contact;

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
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.util.Time;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * Created by Mahdi on 5/4/2017.
 * this adapter will be used in both online and all contact sections
 */

class AdapterContactList extends RecyclerView.Adapter<AdapterContactList.ViewHolder> {

    private List<ContactEntity> dataSource;
    private ContactListListener listener;
    private TextView emptyView;
    private boolean isOnlineAdapter;

    interface ContactListListener {
        void onClick(ContactEntity contact);
    }

    AdapterContactList(TextView emptyView, boolean isOnlineAdapter) {
        this(emptyView, new ArrayList<>());
        this.isOnlineAdapter = isOnlineAdapter;
    }

    AdapterContactList(TextView emptyView, List<ContactEntity> dataSource) {
        Collections.sort(dataSource);
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
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_contact_list, parent, false);
        return new ViewHolder(cv);
    }

    void setListener(ContactListListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        CircleImageView imViewContactPic = cardView.findViewById(R.id.imView_contact_pic);
        TextView txtViewContactName = cardView.findViewById(R.id.txtView_contact_name);
        TextView txtViewContactLastSeen = cardView.findViewById(R.id.txtView_contact_last_seen);
        TextView txtViewContactLastSeenLabel = cardView.findViewById(R.id.txtView_contact_last_seen_label);

        Picasso.with(cardView.getContext())
                .load(dataSource.get(position).getPic())
                .placeholder(R.drawable.default_person_pic)
                .error(R.drawable.default_person_pic)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(imViewContactPic);

        txtViewContactName.setText(dataSource.get(position).getName());
        String lastSeenLabel;
        if (dataSource.get(position).isOnline()) {
            lastSeenLabel = "آنلاین";
        } else {
            lastSeenLabel = Time.getLastSeenLabel(dataSource.get(position).getLastSeen());
        }
        txtViewContactLastSeen.setText(lastSeenLabel);

        // Change Fonts
        Typeface typeface = TypefaceManager.get(cardView.getContext(), cardView.getContext().getString(R.string.font_iran_sans));
        txtViewContactName.setTypeface(typeface);
        txtViewContactLastSeen.setTypeface(typeface);
        txtViewContactLastSeenLabel.setTypeface(typeface);

        cardView.setOnClickListener(v -> {
            if (listener != null) {
                int position1 = holder.getAdapterPosition();
                listener.onClick(dataSource.get(position1));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    void swapDataSource(List<ContactEntity> dataSource) {
        Collections.sort(dataSource);
        this.dataSource = dataSource;
        if (dataSource.size() == 0) {
            this.emptyView.setVisibility(View.VISIBLE);
        } else {
            this.emptyView.setVisibility(View.GONE);
        }
        notifyDataSetChanged();
    }

    void updateDataSource(List<ContactEntity> dataSource) {
        for (ContactEntity contact: dataSource) {
            updateItem(contact);
        }
    }

    void updateItem(ContactEntity contact) {
        boolean isExistInList = false;
        for (ContactEntity entity : dataSource) {
            if (entity.getPhone().equals(contact.getPhone())) {
                isExistInList = true;
                int position = dataSource.indexOf(entity);
                if (isOnlineAdapter && !contact.isOnline()) {
                    dataSource.remove(position);
                    notifyItemChanged(position);
                    if (dataSource.size() == 0) {
                        emptyView.setVisibility(View.VISIBLE);
                    }
                } else {
                    dataSource.set(position, contact);
                    notifyItemChanged(position);
                }
                break;
            }
        }
        if (!isExistInList) {
            dataSource.add(contact);
            Collections.sort(dataSource);
            notifyDataSetChanged();
            this.emptyView.setVisibility(View.GONE);
        }
    }
}
