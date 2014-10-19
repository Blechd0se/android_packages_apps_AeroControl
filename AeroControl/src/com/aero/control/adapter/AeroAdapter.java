package com.aero.control.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aero.control.R;

import java.util.List;

/**
 * Created by ac on 18.09.13.
 */
public class AeroAdapter extends ArrayAdapter<AeroData> {

    private Context context;
    private int layoutResourceId;
    private List<AeroData> data;
    private final static Typeface font = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    public AeroAdapter(Context context, int layoutResourceId, List<AeroData> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    public static class Holder {
        TextView header;
        TextView content;
    }

    @Override
    public void clear() {
        super.clear();
        this.data.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, null);

            holder = new Holder();
            holder.header = (TextView) row.findViewById(R.id.header);
            holder.content = (TextView) row.findViewById((R.id.content));

            holder.header.setTypeface(font);
            holder.content.setTypeface(font);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        final AeroData overview = data.get(position);
        if (data != null) {

            // To ensure we can use this adapter for different things, make some hooks;
            if (!overview.name.equals("A"))
                holder.header.setText(overview.name);

            if (!overview.content.equals("A"))
                holder.content.setText(overview.content);

        } else {
            Log.e("Aero",
                    "No Data found for adapter.");
        }

        return row;
    }

}
