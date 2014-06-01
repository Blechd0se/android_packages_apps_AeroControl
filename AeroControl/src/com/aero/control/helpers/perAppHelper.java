package com.aero.control.helpers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander Christ on 18.05.14.
 */
public class perAppHelper {

    private Context mContext;
    private String[] mPackageNames; /* Real package names */
    private String[] mListedApps;
    private String[] mCurrentSelectedPackages;
    private String[] mCurrentSelectedPackagesByName;
    private boolean mShowSystemApps;
    private boolean[] mIsChecked;

    public perAppHelper(Context context) {
        this.mContext = context;
    }

    /**
     * Gets the android packages names
     * e.g. "Aero Control"
     *
     * @return String array = contains all apps
     */
    public String[] getAllPackageNames() {
        return mListedApps;
    }

    /**
     * Returns false if currently only non-system apps should be shown
     *
     * @return boolean
     */
    public boolean getSystemAppStatus() {
        return mShowSystemApps;
    }

    public void setSystemAppStatus(boolean showSystemApps) {
        mShowSystemApps = showSystemApps;

        // If the system state has changed, we need to clear our previous select list;
        mIsChecked = null;
    }

    /**
     * Gets the current State of this
     *
     * @return boolean array with checked state
     */
    public boolean[] getCheckedState() { return mIsChecked; }

    /**
     * Gets all currently selected packages by the real packages names
     * e.g. "com.aero.control"
     *
     * @return String array = all currently selected packages
     */
    public String[] getCurrentSelectedPackages() {

        ArrayList<String> selectedPackages = new ArrayList<String>();

        int i = 0;

        for (boolean checked : mIsChecked) {

            if (checked)
                selectedPackages.add(mPackageNames[i]);

            i++;
        }
        mCurrentSelectedPackages = selectedPackages.toArray(new String[0]);

        return mCurrentSelectedPackages;
    }

    /**
     * Gets all currently selected packages by their packages names
     * e.g. "Aero Control"
     *
     * @return String array = all currently selected package names
     */
    public String[] getCurrentSelectedPackagesByName() {

        ArrayList<String> selectedPackages = new ArrayList<String>();

        int i = 0;

        if (mIsChecked == null)
            return null;

        for (boolean checked : mIsChecked) {

            if (checked)
                selectedPackages.add(mListedApps[i]);

            i++;
        }
        mCurrentSelectedPackagesByName = selectedPackages.toArray(new String[0]);

        return mCurrentSelectedPackagesByName;
    }

    /**
     * Sets application as checked regarding to their position
     *
     * @param checkedState = true or false
     * @param position = the position where to check
     *
     * @return nothing
     */
    public void setChecked(boolean checkedState, int position) {

        if (mIsChecked == null)
            mIsChecked = new boolean[mPackageNames.length];

        mIsChecked[position] = checkedState;
    }

    /**
     * Finds all the matches for currently selected apps and sets them checked;
     *
     * @param selectedApps = the currently selected apps
     *
     * @return nothing
     */
    public void findMatch(String[] selectedApps) {

        int i = 0;
        for (String a: mPackageNames) {

            for (String b : selectedApps) {

                if (a.equals(b))
                    setChecked(true, i);
            }
            i++;
        }

    }

    // Fills our arrays
    public void getAllApps(boolean showSystemApp) {

        final PackageManager pm = mContext.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<String> currentInstalledApps = new ArrayList<String>();
        ArrayList<String> currentPackages = new ArrayList<String>();

        // We should hold info about what kind of apps this object holds;
        mShowSystemApps = showSystemApp;

        for (ApplicationInfo packageInfo : packages) {

            // If no interest in system apps;
            if (!showSystemApp) {
                if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    continue;
                }
            }

            currentInstalledApps.add(packageInfo.loadLabel(mContext.getPackageManager()).toString());
            currentPackages.add(packageInfo.packageName);
        }

        mListedApps = currentInstalledApps.toArray(new String[0]);
        mPackageNames = currentPackages.toArray(new String[0]);
    }
}
