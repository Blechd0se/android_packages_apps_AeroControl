package com.aero.control.adapter;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aero.control.R;

/**
 * Created by ac on 18.09.13.
 */
public class AeroAdapter extends ArrayAdapter<adapterInit> {

    Context context;
    int layoutResourceId;
    adapterInit data[];

    public AeroAdapter(Context context, int layoutResourceId, adapterInit[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    public static class Holder {
        ImageView image;
        TextView header;
        TextView content;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.image = (ImageView) row.findViewById(R.id.imgIcon);
            holder.header = (TextView) row.findViewById(R.id.header);
            holder.content = (TextView) row.findViewById((R.id.content));

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        final adapterInit overview = data[position];
        if (data != null) {


            // To ensure we can use this adapter for different things, make some hooks;
            if (overview.icon != 0)
                holder.image.setImageResource(overview.icon);

            if (!overview.name.equals("A"))
                holder.header.setText(Html.fromHtml("<i><b>" + overview.name + "</b></i>"));


            if (!overview.content.equals("A"))
                holder.content.setText(overview.content);
        } else {
            Log.e("Aero",
                    "No Data found for adapter.");
        }

        return row;
    }

}
