package com.aero.control.helpers.Android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.view.View;
import com.aero.control.helpers.Android.Material.CheckBox;
import com.aero.control.helpers.Android.Material.CheckBox.OnCheckListener;
import android.widget.TextView;

import com.aero.control.R;
import com.aero.control.helpers.FilePath;

/**
 * Created by Alexander Christ on 30.09.13.
 */
public class CustomTextPreference extends EditTextPreference implements OnCheckListener {


    private Context mContext;
    private TextView mTitle;
    private TextView mSummary;

    private String mText;
    private String mName;
    private CharSequence mSummaryPref;
    private SharedPreferences mSharedPreference;

    private Boolean mChecked;
    private Boolean mHideOnBoot;

    public CustomTextPreference(Context context) {
        super(context);
        this.setContext(context);
        setLayoutResource(R.layout.preference_enhanced);
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * Sets the checkbox visible or invisible.
     *
     * @param checked Boolean. Decides whether the checkbox
     *                should be visible or not.
     */
    public void setHideOnBoot (Boolean checked) {
        this.mHideOnBoot = checked;
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
            mChecked = false;

        return mChecked;
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

    /**
     * Sets a new title for this preference.
     *
     * @param title Sets this String as the new title
     */
    public void setPrefText(String title) {
        this.mText = title;
        if (mTitle != null)
            this.mTitle.setText(title);
    }

    /**
     * Sets a new summary for this preference.
     *
     * @param summary Sets this CharSequence as the new summary
     */
    public void setPrefSummary(CharSequence summary) {
        this.mSummaryPref = summary;
        if (mSummary != null)
            this.mSummary.setText(summary);
    }

    /**
     * Gets the current set summary text view content from this preference
     *
     * @return CharSequence
     */
    public CharSequence getPrefSummary() {
        return mSummaryPref;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);

        mTitle = (TextView) view.findViewById(R.id.preference_title);
        mSummary = (TextView) view.findViewById(R.id.preference_summary);

        mTitle.setText(mText);
        mSummary.setText(mSummaryPref);
        mTitle.setTypeface(FilePath.kitkatFont);
        mSummary.setTypeface(FilePath.kitkatFont);

        CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox_pref);
        checkbox.setOncheckListener(this);

        if (isChecked() != null)
            checkbox.setChecked(isChecked());

        View seperator = (View) view.findViewById(R.id.separator);

        // Some fragments don't need the new set on boot functionality for each element;
        if (isHidden()) {
            checkbox.setVisibility(View.GONE);
            seperator.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCheck(boolean checked) {
        SharedPreferences.Editor editor = mSharedPreference.edit();

        // Update the checked state;
        setChecked(checked);

        // Writes to our shared preferences or deletes the value
        if (checked) {
            editor.putString(this.getName(), this.getPrefSummary().toString());
        } else {
            editor.remove(this.getName());
        }
        editor.commit();
    }
}
