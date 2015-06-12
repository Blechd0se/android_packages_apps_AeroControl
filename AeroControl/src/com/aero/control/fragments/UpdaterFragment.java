package com.aero.control.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.Android.CustomListPreference;
import com.aero.control.helpers.Android.CustomPreference;
import com.aero.control.helpers.FilePath;
import com.aero.control.helpers.updateHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Updater Fragment
 */
public class UpdaterFragment extends PlaceHolderFragment {

    private static final String SDPATH = Environment.getExternalStorageDirectory().getPath();

    private static final String timeStamp = new SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
    private static final File BACKUP_PATH = new File(SDPATH + "/com.aero.control/backup/" + timeStamp + "/zImage");
    private static final File IMAGE = new File (FilePath.zImage);
    private static final String AERO_PATH = "/sdcard/com.aero.control/backup";

    private static final updateHelper update = new updateHelper();
    private CustomPreference mBackupKernel;
    private CustomListPreference mRestoreKernel;
    private String mBackup = null;

    private final static String NO_DATA_FOUND = "Unavailable";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.updater_fragment);

        mBackupKernel = (CustomPreference) findPreference("backup_kernel");
        mBackupKernel.setHideOnBoot(true);
        mRestoreKernel = new CustomListPreference(getActivity());
        mRestoreKernel.setName("restore_kernel");
        mRestoreKernel.setTitle(R.string.pref_restore_kernel);
        mRestoreKernel.setDialogTitle(R.string.pref_restore_kernel);
        mRestoreKernel.setHideOnBoot(true);
        this.getPreferenceScreen().addPreference(mRestoreKernel);

        for (String s : FilePath.BACKUPPATH) {
            if (AeroActivity.genHelper.doesExist(s)) {
                mBackup = s;
            }
        }

        // If device doesn't have this kernel path;
        if (AeroActivity.shell.getInfo(FilePath.zImage).equals(NO_DATA_FOUND))
            mBackupKernel.setEnabled(false);

        if (mBackup != null)
            mBackupKernel.setEnabled(true);

        // Check if this model is in the white list;
        if (!mBackupKernel.isEnabled()) {
            if (update.isWhiteListed(Build.MODEL) != null) {
                mBackup = update.isWhiteListed(Build.MODEL);
                mBackupKernel.setEnabled(true);
            }
        }

        if (AeroActivity.shell.getInfo(FilePath.zImage).equals(NO_DATA_FOUND))
            mRestoreKernel.setEnabled(false);

        // Fresh Start, no backup found;
        try {
            mBackupKernel.setSummary(getText(R.string.last_backup_from)+ " " + AeroActivity.shell.getDirInfo(SDPATH + "/com.aero.control/backup/", false)[0]);
            mRestoreKernel.setEnabled(true);
        } catch (NullPointerException e) {
            mBackupKernel.setSummary(getText(R.string.last_backup_from)+ " " + getText(R.string.unavailable));
            mRestoreKernel.setEnabled(false);
        }

        mBackupKernel.setIcon(R.drawable.ic_action_copy);
        mRestoreKernel.setIcon(R.drawable.ic_action_time);


        mRestoreKernel.setEntries(AeroActivity.shell.getDirInfo(SDPATH + File.separator + "/com.aero.control/backup/", false));
        mRestoreKernel.setEntryValues(AeroActivity.shell.getDirInfo(SDPATH + "/com.aero.control/backup/", false));
        mRestoreKernel.setDialogIcon(R.drawable.restore);

        mRestoreKernel.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                final String s = (String) o;

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View layout = inflater.inflate(R.layout.about_screen, null);
                TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

                builder.setTitle(getText(R.string.backup_from) + " " + s);

                aboutText.setText(getText(R.string.restore_from_backup) + " " + s + " ?");
                AeroActivity.shell.remountSystem();

                // Remove it;
                preference.getEditor().remove(preference.getKey()).commit();

                builder.setView(layout)
                        .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                if (mBackup != null)
                                    restoreBoot(s);
                                else
                                    restorezImage(s);

                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // Do nothing
                            }
                        });

                builder.show();

                return true;
            };
        });

        mBackupKernel.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View layout = inflater.inflate(R.layout.about_screen, null);
                TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

                builder.setTitle("Backup");
                builder.setIcon(R.drawable.backup);

                aboutText.setText(R.string.proceed_backup);

                builder.setView(layout)
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                if (mBackup != null)
                                    backupBoot();
                                else
                                    backupzImage();

                                mBackupKernel.setSummary(getText(R.string.last_backup_from) + " " + timeStamp);

                                // Prepare the UI, otherwise it would throw a Exception;
                                mRestoreKernel.setEntries(AeroActivity.shell.getDirInfo(SDPATH + "/com.aero.control/backup/", false));
                                mRestoreKernel.setEntryValues(AeroActivity.shell.getDirInfo(SDPATH + "/com.aero.control/backup/", false));

                                mRestoreKernel.setEnabled(true);

                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // Do nothing
                            }
                        });

                builder.show();

                return true;
            }

            ;
        });
    }

    private void backupzImage() {
        try {
            update.copyFile(IMAGE, BACKUP_PATH, false);
            Toast.makeText(getActivity(), "Backup was successful!", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Log.e("Aero", "A problem occured while saving a backup.", e);
        }
    }

    private void backupBoot() {

        String backuppath = AERO_PATH + "/" + timeStamp;

        // Create file-structure if necessary;
        if (!(AeroActivity.genHelper.doesExist(AERO_PATH))) {
            if (!(new File(AERO_PATH).mkdir()))
                if (!(new File(AERO_PATH).mkdirs()))
                    Log.e("Aero", "Couldn't create file: " + AERO_PATH);
        }
        if (!(AeroActivity.genHelper.doesExist(backuppath))) {
            if (!(new File(backuppath).mkdir()))
                if (!(new File(backuppath).mkdirs()))
                    Log.e("Aero", "Couldn't create file: " + backuppath);
        }

        String[] commands = new String[] {
                "dd if=" + mBackup + " " + "of=" + backuppath + "/boot.img",
                "chmod 777 " + backuppath + "/boot.img"
        };

        AeroActivity.shell.setRootInfo(commands);
    }

    private void restorezImage(String s) {

        // Delete old zImage first, then copy backup;
        String[] commands = new String[] {
                        "rm -f " + FilePath.zImage,
                        "cp " + "/sdcard/com.aero.control/backup/" + s + "/zImage" + " " + FilePath.zImage,
                };

        AeroActivity.shell.setRootInfo(commands);

        Toast.makeText(getActivity(), R.string.need_reboot, Toast.LENGTH_LONG).show();
    }

    private void restoreBoot(String s) {

        String filepath = new File("/sdcard/com.aero.control/backup/" + s + "/boot.img").getPath();

        String[] commands = new String[] {
                "chmod 0777 " + filepath,
                "dd if=" + filepath + " of=" + mBackup
        };

        AeroActivity.shell.setRootInfo(commands);
        Toast.makeText(getActivity(), R.string.need_reboot, Toast.LENGTH_LONG).show();
    }

}
