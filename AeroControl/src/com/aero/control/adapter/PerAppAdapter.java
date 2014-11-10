package com.aero.control.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import android.widget.ImageView;
import android.widget.TextView;

import com.aero.control.R;
import com.aero.control.helpers.PerApp.PerAppListener;

import java.util.List;

/**
 * Created by Alexander Christ on 05.10.13.
 */
public class PerAppAdapter extends ArrayAdapter<AeroData> {

    private Context context;
    private int layoutResourceId;
    private PerAppListener mPerAppListener;
    private List<AeroData> data;
    private final static Typeface font = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
    private boolean[] mCheckedState;

    public PerAppAdapter(Context context, int layoutResourceId, List<AeroData> data, boolean[] checkedState) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.mCheckedState = checkedState;
    }

    public static class Holder {
        ImageView header;
        TextView content;
        CheckBox check;
    }

    public void setPerAppListener(PerAppListener perAppListener) {
        this.mPerAppListener = perAppListener;
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
            holder.header = (ImageView) row.findViewById(R.id.rowfolder);
            holder.content = (TextView) row.findViewById((R.id.rowtext));
            holder.check = (CheckBox) row.findViewById(R.id.rowcheck);
            holder.content.setTypeface(font);

            holder.check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox cb = (CheckBox) view;
                    int row_id = (Integer) cb.getTag();
                    data.get(row_id).isChecked = cb.isChecked();
                    mPerAppListener.OnAppItemClicked(row_id, holder.check.isChecked());
                }
            });

            row.setTag(holder);
            row.setTag(R.id.rowcheck, holder.check);
        } else {
            holder = (Holder) row.getTag();
        }

        final AeroData overview = data.get(position);

        if (data != null) {

            holder.header.setImageDrawable(overview.image);

            holder.content.setText(overview.name);

            // Set the tag before checking for existing data;
            holder.check.setTag(position);
            int row_id = (Integer) holder.check.getTag();

            if (mCheckedState != null) {
                data.get(row_id).isChecked = mCheckedState[row_id];
            }
            holder.check.setTag(row_id);
            holder.check.setChecked(overview.isChecked);

        } else {
            Log.e("Aero",
                    "No Data found for adapter.");
        }

        return row;
    }

}
