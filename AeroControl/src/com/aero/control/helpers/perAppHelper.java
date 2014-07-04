package com.aero.control.helpers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Alexander Christ on 18.05.14.
 */
public class perAppHelper {

    private Context mContext;
    private String[] mPackageNames; /* Real package names */
    private String[] mListedApps;
    private String[] mCurrentSelectedPackages;
    private boolean mShowSystemApps;
    private boolean[] mIsChecked;
    private List<ApplicationInfo> mPackages;

    public perAppHelper(Context context) {
        this.mContext = context;
    }

    /**
     * Gets the android packages names
     * e.g. "Aero Control"
     *
     * @return String array = contains all apps
     */
    public final String[] getAllPackageNames() {
        return mListedApps;
    }

    public final void setPackages(List<ApplicationInfo> packages) {

        this.mPackages = packages;
        // Invoke a new scan here if necessary;
        getAllApps(getSystemAppStatus());

    }

    public final List<ApplicationInfo>  getPackages() {
        return mPackages;
    }

    /**
     * Returns false if currently only non-system apps should be shown
     *
     * @return boolean
     */
    public final boolean getSystemAppStatus() {
        return mShowSystemApps;
    }

    public final void setSystemAppStatus(boolean showSystemApps) {
        mShowSystemApps = showSystemApps;

        // If the system state has changed, we need to clear our previous select list;
        mIsChecked = null;
    }

    /**
     * Gets the current State of this
     *
     * @return boolean array with checked state
     */
    public final boolean[] getCheckedState() { return mIsChecked; }

    /**
     * Gets all currently selected packages by the real packages names
     * e.g. "com.aero.control"
     *
     * @return String array = all currently selected packages
     */
    public final String[] getCurrentSelectedPackages() {

        if (mIsChecked == null) {
            return mCurrentSelectedPackages;
        }

        final ArrayList<String> selectedPackages = new ArrayList<String>();

        int i = 0;

        for (final boolean checked : mIsChecked) {

            if (checked)
                selectedPackages.add(mPackageNames[i]);

            i++;
        }
        mCurrentSelectedPackages = selectedPackages.toArray(new String[0]);

        return mCurrentSelectedPackages;
    }

    /**
     * Sets application as checked regarding to their position
     *
     * @param checkedState = true or false
     * @param position = the position where to check
     *
     * @return nothing
     */
    public final void setChecked(boolean checkedState, int position) {

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
    public final void findMatch(final String[] selectedApps) {

        int i = 0;
        for (final String a: mPackageNames) {

            for (final String b : selectedApps) {

                if (a.equals(b))
                    setChecked(true, i);
            }
            i++;
        }

    }

    // Fills our arrays
    public final void getAllApps(boolean showSystemApp) {

        final PackageManager pm = mContext.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(0);
        final ArrayList<String> currentInstalledApps = new ArrayList<String>();
        final ArrayList<String> currentPackages = new ArrayList<String>();

        // Just copy our array;
        if (mPackages == null) {
            // Sort our freshly obtained apps;
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(pm));
            mPackages = packages;
        } else {
            packages = mPackages;
        }

        // We should hold info about what kind of apps this object holds;
        mShowSystemApps = showSystemApp;

        for (final ApplicationInfo packageInfo : packages) {

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
