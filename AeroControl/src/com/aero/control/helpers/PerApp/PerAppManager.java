package com.aero.control.helpers.PerApp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.aero.control.R;
import com.aero.control.adapter.AeroData;
import com.aero.control.adapter.PerAppAdapter;

import java.util.List;

/**
 * Created by Alexander Christ on 05.10.14.
 */
public class PerAppManager extends LinearLayout implements PerAppListener {

    Context mContext;
    private ListView mListView;
    private perAppHelper mPerApp;
    private PerAppAdapter mAdapter;

    public PerAppManager(Context context, AttributeSet attrs, perAppHelper perApp) {
        super(context, attrs);

        this.mContext = context;
        this.mPerApp = perApp;

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.file_folder, this);

        mListView = (ListView) view.findViewById(R.id.list);

        setItemList();
    }

    public void setItemList(){

        List<AeroData> mData = mPerApp.getFullPackages();

        mAdapter = new PerAppAdapter(mContext,
                R.layout.perapp_row, mData, mPerApp.getCheckedState());

        mAdapter.setPerAppListener(this);

        mListView.setAdapter(mAdapter);
    }

    @Override
    public void OnAppItemClicked(int position, boolean isChecked) {
        mPerApp.setChecked(isChecked, position);
    }
}