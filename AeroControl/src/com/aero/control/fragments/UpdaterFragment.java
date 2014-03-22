package com.aero.control.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;
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
public class UpdaterFragment extends PreferenceFragment {

    private static final String sdpath = Environment.getExternalStorageDirectory().getPath();

    public static final String timeStamp = new SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
    public static final String zImage = "/system/bootstrap/2nd-boot/zImage";
    public static final File BACKUP_PATH = new File(sdpath + "/com.aero.control/" + timeStamp + "/zImage");
    public static final File IMAGE = new File (zImage);

    private static final updateHelper update = new updateHelper();

    public static Fragment newInstance(Context context) {
        UpdaterFragment f = new UpdaterFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.updater_fragment);

        PreferenceScreen root = this.getPreferenceScreen();

        final Preference backup_kernel = root.findPreference("backup_kernel");
        final Preference updater_kernel = root.findPreference("updater_kernel");
        final ListPreference restore_kernel = (ListPreference) root.findPreference("restore_kernel");

        // Disable them all;
        updater_kernel.setEnabled(false);

        // If device doesn't have this kernel path;
        if (AeroActivity.shell.getInfo(zImage).equals("Unavailable"))
            backup_kernel.setEnabled(false);

        if (AeroActivity.shell.getInfo(zImage).equals("Unavailable"))
            restore_kernel.setEnabled(false);

        // Fresh Start, no backup found;
        try {
            backup_kernel.setSummary(getText(R.string.last_backup_from)+ " " + AeroActivity.shell.getDirInfo(sdpath + "/com.aero.control/", false)[0]);
            restore_kernel.setEnabled(true);
        } catch (NullPointerException e) {
            backup_kernel.setSummary(getText(R.string.last_backup_from)+ " " + getText(R.string.unavailable));
            restore_kernel.setEnabled(false);
        }

        backup_kernel.setIcon(R.drawable.ic_action_copy);
        updater_kernel.setIcon(R.drawable.ic_action_download);
        restore_kernel.setIcon(R.drawable.ic_action_time);


        restore_kernel.setEntries(AeroActivity.shell.getDirInfo(sdpath + File.separator + "/com.aero.control/", false));
        restore_kernel.setEntryValues(AeroActivity.shell.getDirInfo(sdpath + "/com.aero.control/", false));
        restore_kernel.setDialogIcon(R.drawable.restore_dark);

        restore_kernel.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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

                builder.setView(layout)
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                // Delete old zImage first, then copy backup;
                                String[] commands = new String[]
                                        {
                                                "rm -f " + zImage,
                                                "cp " + "/sdcard/com.aero.control/" + s + "/zImage" + " " + zImage,
                                        };

                                AeroActivity.shell.setRootInfo(commands);

                                Toast.makeText(getActivity(), R.string.need_reboot, Toast.LENGTH_LONG).show();


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

        backup_kernel.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View layout = inflater.inflate(R.layout.about_screen, null);
                TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

                builder.setTitle("Backup");
                builder.setIcon(R.drawable.backup_dark);

                aboutText.setText(R.string.proceed_backup);

                builder.setView(layout)
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                try {
                                    update.copyFile(IMAGE, BACKUP_PATH, false);
                                    Toast.makeText(getActivity(), "Backup was successful!", Toast.LENGTH_LONG).show();

                                    backup_kernel.setSummary(getText(R.string.last_backup_from) + " " + timeStamp);

                                    // Prepare the UI, otherwise it would throw a Exception;
                                    restore_kernel.setEntries(AeroActivity.shell.getDirInfo(sdpath + "/com.aero.control/", false));
                                    restore_kernel.setEntryValues(AeroActivity.shell.getDirInfo(sdpath + "/com.aero.control/", false));

                                    restore_kernel.setEnabled(true);

                                } catch (IOException e) {
                                    Log.e("Aero", "A problem occured while saving a backup.", e);
                                }
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
}
