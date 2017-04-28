package ir.logicbase.mojmessenger.settings.preference;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.util.TypefaceManager;

public class CustomIntentPreference extends Preference {

    public CustomIntentPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.item_preference_intent);
    }

    public CustomIntentPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.item_preference_intent);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView title = (TextView) holder.findViewById(android.R.id.title);
        TextView summary = (TextView) holder.findViewById(android.R.id.summary);
        Typeface typeface = TypefaceManager.get(getContext(), getContext().getString(R.string.font_iran_sans));
        title.setTypeface(typeface);
        summary.setTypeface(typeface);
    }
}
