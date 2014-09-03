package com.aero.control.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.aero.control.R;

/**
 * Created by Alexander Christ on 30.09.13.
 */
public class CustomListPreference extends ListPreference implements OnCheckedChangeListener {


    private Context mContext;
    private Preference mPref;
    private TextView mTitle;
    private TextView mSummary;
    private CheckBox mCheckBox;
    private View mSeperator;

    private String mText;
    private String mName = super.getKey();
    private CharSequence mSummaryPref;
    private SharedPreferences mSharedPreference;

    private Boolean mChecked;
    private Boolean mHideOnBoot;

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setContext(context);
        setLayoutResource(R.layout.preference_enhanced);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public CustomListPreference(Context context) {
        super(context);
        this.setContext(context);
        setLayoutResource(R.layout.preference_enhanced);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * Gets the current hidden state for this preference
     *
     * @return Boolean
     */
    public Boolean isHidden() {

        // If not set, set it to false;
        if (mHideOnBoot == null)
            mHideOnBoot = false;

        return mHideOnBoot;
    }

    /**
     * Sets the checkbox to checked for this preference
     *
     * @param checked Boolean. Decides whether the checkbox
     *                should be checked or not.
     */
    public void setChecked (Boolean checked) {
        this.mChecked = checked;
    }

    /**
     * Gets the current checked state for this preference
     *
     * @return Boolean
     */
    public Boolean isChecked() {

        // If exists, we can mark it checked;
        if (mSharedPreference.getString(getName(), null) != null) {
            setChecked(true);
        }

        // If not set, set it to false;
        if (mChecked == null)
            setChecked(false);

        return mChecked;
    }

    @Override
    public CharSequence getSummary() {
        return mSummaryPref;
    }
    @Override
    public void setSummary(CharSequence value) {
        this.mSummaryPref = value;
    }

    @Override
    public void setKey(String key) {
        setName(key);
    }
    @Override
    public String getKey() {
        return getName();
    }

    /**
     * Sets the name for this preference (most probably filepath)
     *
     * @param name A name for the preference. Necessary for the
     *             set on boot functionality.
     */
    public void setName (String name) {
        this.mName = name;
    }

    /**
     * Gets the name for this preference (most probably filepath)
     *
     * @return String
     */
    public String getName() {
        return mName;
    }


    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mPref = this;

        mTitle = (TextView) view.findViewById(R.id.preference_title);
        mSummary = (TextView) view.findViewById(R.id.preference_summary);

        mTitle.setText(super.getTitle());
        mSummary.setText(mSummaryPref);

        mCheckBox = (CheckBox) view.findViewById(R.id.delete_button);
        mCheckBox.setOnCheckedChangeListener(this);

        mCheckBox.setChecked(isChecked());

        mSeperator = (View) view.findViewById(R.id.separator);

        // Some fragments don't need the new set on boot functionality for each element;
        if (isHidden()) {
            mCheckBox.setVisibility(View.GONE);
            mSeperator.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compButt, boolean checked) {
        SharedPreferences.Editor editor = mSharedPreference.edit();

        // Writes to our shared preferences or deletes the value
        if (checked) {
            editor.putString(this.getName(), super.getValue());
        } else {
            editor.remove(this.getName());
        }
        editor.commit();
    }
}