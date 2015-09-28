package com.aero.control.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aero.control.R;
import com.aero.control.helpers.PerApp.AppMonitor.model.AppElement;
import com.aero.control.helpers.Util;

import java.util.List;

/**
 * Created by Alexander Christ on 03.05.15.
 */
public class AppDataAdapter extends ArrayAdapter<AppElement> {

    private Context context;
    private int layoutResourceId;
    private List<AppElement> data;
    private final static Typeface font = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    public AppDataAdapter(Context context, int layoutResourceId, List<AppElement> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    public static class Holder {
        ImageView image;
        TextView text;
        TextView textTime;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final Holder holder;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, null);

            holder = new Holder();
            holder.image = (ImageView) row.findViewById(R.id.rowimage);
            holder.text = (TextView) row.findViewById((R.id.rowtext));
            holder.textTime = (TextView) row.findViewById(R.id.rowtime);
            holder.text.setTypeface(font);
            holder.textTime.setTypeface(font);


            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        final AppElement overview = data.get(position);

        if (data != null) {

            holder.text.setText(overview.getRealName() + "\n" + overview.getName());
            holder.textTime.setText(Util.getFormatedTimeString(overview.getUsage()));
            holder.image.setImageDrawable(overview.getImage());
            holder.image.setTag(position);

        } else {
            Log.e("Aero",
                    "No Data found for adapter.");
        }

        return row;
    }

}
