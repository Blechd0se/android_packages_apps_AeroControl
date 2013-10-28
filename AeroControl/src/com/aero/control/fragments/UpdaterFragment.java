package com.aero.control.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.control.R;
import com.aero.control.helpers.shellHelper;
import com.aero.control.helpers.updateHelpers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Updater Fragment
 */
public class UpdaterFragment extends PreferenceFragment {

    public static String timeStamp = new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime());
    public static File BACKUP_PATH = new File ("/sdcard/com.aero.control/" + timeStamp + "/zImage");
    public static File IMAGE = new File ("/system/bootmenu/2nd-boot/zImage");

    updateHelpers update = new updateHelpers();
    shellHelper shell = new shellHelper();

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
        final Preference restore_kernel = root.findPreference("restore_kernel");

        // Disable them all;
        //backup_kernel.setEnabled(false);
        updater_kernel.setEnabled(false);
        restore_kernel.setEnabled(false);

        backup_kernel.setSummary("Last Backup from: " + shell.getDirInfo("/sdcard/com.aero.control/", false)[0]);

        backup_kernel.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View layout = inflater.inflate(R.layout.about_screen, null);
                TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

                builder.setTitle("Backup");

                aboutText.setText("Proceed with backup? This will backup your current kernel.");

                builder.setView(layout)
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                try {
                                    update.copyFile(IMAGE, BACKUP_PATH , timeStamp);
                                    Toast.makeText(getActivity(), "Backup was successful!", Toast.LENGTH_LONG).show();

                                    backup_kernel.setSummary("Last Backup from: " + timeStamp);

                                } catch (IOException e) {
                                    Log.e("Aero", "A problem occured while saving a backup", e);
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
