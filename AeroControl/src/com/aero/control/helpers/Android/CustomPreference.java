package com.aero.control.helpers.Android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import com.aero.control.helpers.Android.Material.CheckBox;
import com.aero.control.helpers.Android.Material.CheckBox.OnCheckListener;
import android.widget.TextView;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.Android.Material.CustomImageButton;
import com.aero.control.helpers.FilePath;
import com.aero.control.helpers.HelpTextHolder;

import java.util.Map;

/**
 * Created by Alexander Christ on 30.09.13.
 */
public class CustomPreference extends Preference implements OnCheckListener {


    private Context mContext;
    private TextView mTitle;
    private TextView mSummary;
    private CheckBox mCheckBox;
    private CustomImageButton mCustomImageButton;

    private String mName = super.getKey();
    private CharSequence mSummaryPref;
    private SharedPreferences mSharedPreference;

    private Boolean mChecked;
    private Boolean mHideOnBoot;
    private String mLookUpDefault;
    private Boolean mClicked;
    private Boolean mShowHelp;

    private String mHelpContent;

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            String key = getKey();
            if (key == null) {
                key = getName();
            }

            // Get the helptext if not already loaded;
            if (mHelpContent == null) {
                mHelpContent = HelpTextHolder.instance(mContext).getText(key);
            }

            AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle(getTitle())
                    .setMessage(mHelpContent)
                    .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    })
                    .create();

            dialog.show();

        }
    };

    // Needed by the generic layout inflater;
    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setContext(context);
        setLayoutResource(R.layout.preference_enhanced);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
        this.setSummary(super.getSummary());
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
     * Sets the small helptext icon left to the main entry visible or invisible
     *
     * @param enable boolean
     */
    public void setHelpEnable(boolean enable) {
        this.mShowHelp = enable;
    }


    /**
     * Gets the current state for the helptext icon.
     *
     * @return Boolean
     */
    public Boolean isHelpEnabled() {

        if (mShowHelp == null) {
            mShowHelp = true;

        }
        return mShowHelp;
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
     * Sets the to summary text to enabled/disabled
     *
     * @param checked Boolean. Handles the summary text
     *                transition.
     */
    public void setClicked (Boolean checked) {
        this.mClicked = checked;

        if (mSummary != null) {
            if (mClicked)
                mSummary.setText(R.string.enabled);
            else
                mSummary.setText(R.string.disabled);
        }

    }

    /**
     * Gets the current clicked(summary) state for this preference
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

        String[] content;
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

        return tmp1;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mTitle = (TextView) view.findViewById(R.id.preference_title);
        mSummary = (TextView) view.findViewById(R.id.preference_summary);

        mTitle.setText(super.getTitle());
        mSummary.setText(mSummaryPref);
        mTitle.setTypeface(FilePath.kitkatFont);
        mSummary.setTypeface(FilePath.kitkatFont);

        mCheckBox = (CheckBox) view.findViewById(R.id.checkbox_pref);
        mCheckBox.setOncheckListener(this);

        mCheckBox.setChecked(isChecked());

        mCustomImageButton = (CustomImageButton) view.findViewById(R.id.info_button);

        View separator_checkbox = (View) view.findViewById(R.id.separator_checkbox);
        View seperator_info = (View) view.findViewById(R.id.separator_info);

        if (isHelpEnabled()) {
            mCustomImageButton.setOnClickListener(mOnClickListener);
        } else {
            mCustomImageButton.setVisibility(View.GONE);
            seperator_info.setVisibility(View.GONE);
        }

        // Some fragments don't need the new set on boot functionality for each element;
        if (isHidden()) {
            mCheckBox.setVisibility(View.GONE);
            separator_checkbox.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCheck(boolean checked) {
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
