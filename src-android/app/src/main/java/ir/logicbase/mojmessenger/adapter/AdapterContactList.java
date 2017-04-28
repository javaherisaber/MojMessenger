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
import ir.logicbase.mojmessenger.bundle.BundleContact;
import ir.logicbase.mojmessenger.util.Time;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * Created by Mahdi on 5/4/2017.
 * this adapter will be used in both online and all contact sections
 */

public class AdapterContactList extends RecyclerView.Adapter<AdapterContactList.ViewHolder> {

    private ArrayList<BundleContact> contactList;
    private ContactListListener listener;

    public interface ContactListListener{
        void onClick(BundleContact contact);
    }

    public AdapterContactList() {
        this(new ArrayList<>());
    }

    public AdapterContactList(ArrayList<BundleContact> contactList) {
        this.contactList = contactList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

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

    public void setListener(ContactListListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        CircleImageView imViewContactPic = (CircleImageView) cardView.findViewById(R.id.imView_contact_pic);
        TextView txtViewContactName = (TextView) cardView.findViewById(R.id.txtView_contact_name);
        TextView txtViewContactLastSeen = (TextView) cardView.findViewById(R.id.txtView_contact_last_seen);
        TextView txtViewContactLastSeenLabel = (TextView) cardView.findViewById(R.id.txtView_contact_last_seen_label);

        String url = "http://enadcity.org/enadcity/wp-content/uploads/2017/02/profile-pictures.png2";
        Picasso.with(cardView.getContext())
                .load(url)
                .placeholder(R.drawable.default_person_pic)
                .error(R.drawable.default_person_pic)
                .into(imViewContactPic);
        txtViewContactName.setText(contactList.get(position).getName());
        String lastSeenLabel = Time.getLastSeenLabel(contactList.get(position).getLastSeen());
        txtViewContactLastSeen.setText(lastSeenLabel);

        /*
        Change Fonts
         */
        Typeface typeface = TypefaceManager.get(cardView.getContext(), cardView.getContext().getString(R.string.font_iran_sans));
        txtViewContactName.setTypeface(typeface);
        txtViewContactLastSeen.setTypeface(typeface);
        txtViewContactLastSeenLabel.setTypeface(typeface);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    int position = holder.getAdapterPosition();
                    listener.onClick(contactList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void swapDataSource(ArrayList<BundleContact> data){
        this.contactList = data;
        notifyDataSetChanged();
    }
}
