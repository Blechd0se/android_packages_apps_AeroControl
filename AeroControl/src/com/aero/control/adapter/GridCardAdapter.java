package com.aero.control.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.aero.control.helpers.Android.Material.CardBox;

import java.util.List;

/**
 * Created by Alexander Christ on 18.09.13.
 */
public class GridCardAdapter extends ArrayAdapter<CardBox> {

    private List<CardBox> data;

    public GridCardAdapter(Context context, int layoutResourceId, List<CardBox> data) {
        super(context, layoutResourceId, data);
        this.data = data;
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

        return data.get(position);
    }

}
