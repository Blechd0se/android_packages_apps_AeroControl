package com.aero.control.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.aero.control.AeroActivity;
import com.aero.control.R;

import java.util.Map;

/**
 * Created by Alexander Christ on 30.09.13.
 */
public class CustomPreference extends Preference implements OnCheckedChangeListener {


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
    private String mLookUpDefault;
    private Boolean mClicked;

    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setContext(context);
        setLayoutResource(R.layout.preference_enhanced);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public CustomPreference(Context context) {
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
        try {
            if (mSharedPreference.getString(getName(), null) != null) {
                setChecked(true);
            }
        } catch (ClassCastException e) {
            // This is the case if we try to cast a HashMap, since we found it;
            setChecked(true);
        }

        // If not set, set it to false;
        if (mChecked == null)
            setChecked(false);

        return mChecked;
    }

    /**
     * Sets the checkbox to checked for this preference
     *
     * @param checked Boolean. Decides whether the checkbox
     *                should be checked or not.
     */
    public void setClicked (Boolean checked) {
        this.mClicked = checked;

        if (mSummary != null) {
            if (mClicked)
                mSummary.setText("Enabled");
            else
                mSummary.setText("Disabled");
        }

    }

    /**
     * Gets the current checked state for this preference
     *
     * @return Boolean
     */
    public Boolean isClicked() {
        return mClicked;
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

    public void setLookUpDefault(String filepath) {
        this.mLookUpDefault = filepath;
    }


    private String getLookUpDefault(String name) {

        String[] content = null;
        String tmp1 = "";
        String tmp2;
        int switcher;
        int i = 0;

        if (name.equals("rgbValues")) {
            content = AeroActivity.shell.getInfoArray(mLookUpDefault, 0, 0);
            switcher = 1;
        } else if (name.equals("voltage_values")) {
            content = AeroActivity.shell.getInfo(mLookUpDefault, false);
            switcher = 2;
        } else {
            tmp2 = AeroActivity.shell.getInfo(mLookUpDefault);
            return tmp2;
        }

        for (String a : content) {

            switch (switcher) {
                case 1:
                    if (i == 0)
                        tmp1 = tmp1 + a;
                    else
                        tmp1 = tmp1 + " " + a;

                    break;
                case 2:
                    tmp2 = a.split(":")[1].replace(" ", "");

                    if (i == 0)
                        tmp1 = tmp1 + tmp2.replace("mV", "");
                    else
                        tmp1 = tmp1 + " " + tmp2.replace("mV", "");

                    break;
                default:
                    return null;
            }
            i++;
        }

        Log.e("Aero", "Content: " + tmp1);

        return tmp1;
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

        // In order to find the already set voltage value;
        String tmp = null;
        final Map<String,?> allKeys = mSharedPreference.getAll();

        for(Map.Entry<String, ?> entry : allKeys.entrySet()) {

            String value = entry.getValue().toString();
            String key = entry.getKey().toString();

            if (key.equals(this.getName()))
                tmp = value;
        }
        // Check if its a special case;
        if (tmp == null && mLookUpDefault != null)
            tmp = getLookUpDefault(this.getName());

        // Writes to our shared preferences or deletes the value
        if (checked) {
            if (tmp != null)
                editor.putString(this.getName(), tmp);
        } else {
            editor.remove(this.getName());
        }
        editor.commit();
        setChecked(checked);
    }
}
