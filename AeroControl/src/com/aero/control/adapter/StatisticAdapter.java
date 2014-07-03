package com.aero.control.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aero.control.R;

/**
 * Created by ac on 18.09.13.
 */
public class StatisticAdapter extends ArrayAdapter<statisticInit> {

    private Context context;
    private int layoutResourceId;
    private statisticInit data[];
    private final static Typeface font = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
    private int mIndex = 0;

    // Color Code, redundant but necessary
    public static final String[] color_code = {
            "#009688", /* Turquoise */
            "#ff5722", /* Orange */
            "#8bc34a", /* Midnight Blue */
            "#03a9f4", /* Nephritis */
            "#e51c23", /* Monza */
            "#00bcd4", /* Wisteria */
            "#607d8b", /* Peter River */
            "#e91e63", /* Pomegrante */
    };

    public StatisticAdapter(Context context, int layoutResourceId, statisticInit[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    public static class Holder {
        TextView frequency;
        TextView timeInState;
        TextView percentage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder;

        if (mIndex == 8)
            mIndex = 0;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.frequency = (TextView) row.findViewById(R.id.frequency);
            holder.timeInState = (TextView) row.findViewById(R.id.timeInState);
            holder.percentage = (TextView) row.findViewById((R.id.percentage));

            holder.frequency.setTypeface(font);
            holder.timeInState.setTypeface(font);
            holder.percentage.setTypeface(font);

            holder.frequency.setTextColor(Color.parseColor(color_code[mIndex]));
            holder.timeInState.setTextColor(Color.parseColor(color_code[mIndex]));
            holder.percentage.setTextColor(Color.parseColor(color_code[mIndex]));

            mIndex++;

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        final statisticInit overview = data[position];
        if(overview == null)
            return row;

        if (data != null) {

            if(overview.mFrequency != null)
                holder.frequency.setText(overview.mFrequency);

            if(overview.mTimeInState != null)
                holder.timeInState.setText(overview.mTimeInState);

            if(overview.mPercentage != null)
                holder.percentage.setText(overview.mPercentage);

        } else {
            Log.e("Aero",
                    "No Data found for adapter.");
        }

        /* Small animation effect */
        int delay = (position * 200);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        animation.setStartOffset(delay);
        row.setAnimation(animation);

        return row;
    }

}
