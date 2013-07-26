/*
 * Copyright 2013 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aero.control.helpers;

import java.io.DataOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.aero.control.IOUtils;
import com.aero.control.InstallOptionsCursor;
import com.aero.control.Utils;
import com.aero.control.activities.RequestFileActivity;
import com.aero.control.activities.RequestFileActivity.RequestFileCallback;
import com.aero.control.R;

public class RebootHelper implements RequestFileCallback {

    private Context mContext;
    private RecoveryHelper mRecoveryHelper;
    private String[] mItems;

    public RebootHelper(Context context, RecoveryHelper recoveryHelper) {
        mContext = context;
        mRecoveryHelper = recoveryHelper;
    }

    private void showBackupDialog(final Context context, final String[] items,
            final boolean wipeSystem, final boolean wipeData, final boolean wipeCaches) {

        double checkSpace = 1.0;// ManagerFactory.getPreferencesManager().getSpaceLeft();
        if (checkSpace > 0) {
            double spaceLeft = IOUtils.getSpaceLeft();
            if (spaceLeft < checkSpace) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle(R.string.alert_backup_space_title);
                alert.setMessage(context.getResources().getString(
                        R.string.alert_backup_space_message, checkSpace));

                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();

                        reallyShowBackupDialog(context, items, wipeSystem, wipeData,
                                wipeCaches);
                    }
                });

                alert.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alert.show();
            } else {
                reallyShowBackupDialog(context, items, wipeSystem, wipeData,
                        wipeCaches);
            }
        } else {
            reallyShowBackupDialog(context, items, wipeSystem, wipeData, wipeCaches);
        }
    }

    private void reallyShowBackupDialog(final Context context, final String[] items,
            final boolean wipeSystem, final boolean wipeData, final boolean wipeCaches) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_backup_title);
        View view = LayoutInflater.from(context).inflate(R.layout.backup_dialog,
                (ViewGroup) ((Activity) context).findViewById(R.id.backup_dialog_layout));
        alert.setView(view);

        final CheckBox cbSystem = (CheckBox) view.findViewById(R.id.system);
        final CheckBox cbData = (CheckBox) view.findViewById(R.id.data);
        final CheckBox cbCache = (CheckBox) view.findViewById(R.id.cache);
        final CheckBox cbRecovery = (CheckBox) view.findViewById(R.id.recovery);
        final CheckBox cbBoot = (CheckBox) view.findViewById(R.id.boot);
        final CheckBox cbSecure = (CheckBox) view.findViewById(R.id.androidsecure);
        final CheckBox cbSdext = (CheckBox) view.findViewById(R.id.sdext);
        final EditText input = (EditText) view.findViewById(R.id.backupname);

        input.setText(Utils.getDateAndTime());
        input.selectAll();

        if (mRecoveryHelper.getRecovery().getId() == R.id.twrp) {
            if (!mRecoveryHelper.hasAndroidSecure()) {
                cbSecure.setVisibility(View.GONE);
            }
            if (!mRecoveryHelper.hasSdExt()) {
                cbSdext.setVisibility(View.GONE);
            }
        } else {
            cbSystem.setVisibility(View.GONE);
            cbData.setVisibility(View.GONE);
            cbCache.setVisibility(View.GONE);
            cbRecovery.setVisibility(View.GONE);
            cbBoot.setVisibility(View.GONE);
            cbSecure.setVisibility(View.GONE);
            cbSdext.setVisibility(View.GONE);
        }

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                String text = input.getText().toString();
                text = text.replace(" ", "");

                String backupOptions = null;
                if (mRecoveryHelper.getRecovery().getId() == R.id.twrp) {
                    backupOptions = "";
                    if (cbSystem.isChecked()) {
                        backupOptions += "S";
                    }
                    if (cbData.isChecked()) {
                        backupOptions += "D";
                    }
                    if (cbCache.isChecked()) {
                        backupOptions += "C";
                    }
                    if (cbRecovery.isChecked()) {
                        backupOptions += "R";
                    }
                    if (cbBoot.isChecked()) {
                        backupOptions += "B";
                    }
                    if (cbSecure.isChecked()) {
                        backupOptions += "A";
                    }
                    if (cbSdext.isChecked()) {
                        backupOptions += "E";
                    }

                    if ("".equals(backupOptions)) {
                        return;
                    }
                }

                reboot(context, items, wipeSystem, wipeData, wipeCaches, text, backupOptions);
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    @Override
    public void fileRequested(String filePath) {
        filePath = mRecoveryHelper.getRecoveryFilePath(filePath);
        String[] items = new String[mItems.length + 1];
        for (int i = 0; i < mItems.length; i++) {
            items[i] = mItems[i];
        }
        items[items.length - 1] = filePath;
        mItems = null;
        showRebootDialog(mContext, items);
    }

    public void showRebootDialog(final Context context, final String[] items) {

        if (items == null || items.length == 0) {
            return;
        }

        mContext = context;

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        final InstallOptionsCursor installCursor = new InstallOptionsCursor(context);

        View view = LayoutInflater.from(context).inflate(R.layout.install_dialog,
                (ViewGroup) ((Activity) context).findViewById(R.id.install_dialog_layout));
        alert.setView(view);

        final TextView tvMessage = (TextView) view.findViewById(R.id.message);
        final CheckBox cbBackup = (CheckBox) view.findViewById(R.id.backup);
        final CheckBox cbWipeSystem = (CheckBox) view.findViewById(R.id.wipesystem);
        final CheckBox cbWipeData = (CheckBox) view.findViewById(R.id.wipedata);
        final CheckBox cbWipeCaches = (CheckBox) view.findViewById(R.id.wipecaches);

        if (installCursor.getCount() > 0) {
            alert.setTitle(R.string.alert_reboot_install_title);
        } else {
            alert.setTitle(R.string.alert_reboot_only_install_title);
        }
        cbBackup.setVisibility(installCursor.hasBackup() ? View.VISIBLE : View.GONE);
        cbWipeSystem.setVisibility(installCursor.hasWipeSystem() ? View.VISIBLE : View.GONE);
        cbWipeData.setVisibility(installCursor.hasWipeData() ? View.VISIBLE : View.GONE);
        cbWipeCaches.setVisibility(installCursor.hasWipeCaches() ? View.VISIBLE : View.GONE);
        if (items.length == 1) {
            tvMessage.setText(context.getResources().getString(
                    R.string.alert_reboot_one_message, new Object[] { items[0] }));
        } else {
            tvMessage.setText(context.getResources().getString(
                    R.string.alert_reboot_more_message, new Object[] { items.length }));
        }

        alert.setPositiveButton(R.string.alert_reboot_now, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                if (cbBackup.isChecked()) {
                    showBackupDialog(context, items, cbWipeSystem.isChecked(), cbWipeData.isChecked(),
                            cbWipeCaches.isChecked());
                } else {
                    reboot(context, items, cbWipeSystem.isChecked(), cbWipeData.isChecked(),
                            cbWipeCaches.isChecked(), null, null);
                }
                installCursor.close();

            }
        });

        alert.setNeutralButton(R.string.alert_reboot_add_zip, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                mItems = items;
                RequestFileActivity.setRequestFileCallback(RebootHelper.this);
                Intent intent = new Intent(context, RequestFileActivity.class);
                context.startActivity(intent);
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void reboot(Context context, final String[] items, final boolean wipeSystem,
            final boolean wipeData, final boolean wipeCaches, final String backupFolder,
            final String backupOptions) {

        if (wipeSystem) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle(R.string.alert_wipe_system_title);
            alert.setMessage(R.string.alert_wipe_system_message);

            alert.setPositiveButton(R.string.alert_reboot_now,
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();

                            _reboot(items, wipeSystem, wipeData, wipeCaches, backupFolder,
                                    backupOptions);

                        }
                    });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        } else {
            _reboot(items, wipeSystem, wipeData, wipeCaches, backupFolder, backupOptions);
        }

    }

    private void _reboot(String[] items, boolean wipeSystem, boolean wipeData, boolean wipeCaches,
            String backupFolder, String backupOptions) {

        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());

            os.writeBytes("rm -f /cache/recovery/command\n");
            os.writeBytes("rm -f /cache/recovery/extendedcommand\n");
            os.writeBytes("rm -f /cache/recovery/openrecoveryscript\n");

            String file = mRecoveryHelper.getCommandsFile();

            String[] commands = mRecoveryHelper.getCommands(items, wipeSystem, wipeData,
                    wipeCaches, backupFolder, backupOptions);
            if (commands != null) {
                int size = commands.length, i = 0;
                for (; i < size; i++) {
                    os.writeBytes("echo '" + commands[i] + "' >> /cache/recovery/" + file
                            + "\n");
                }
            }

            os.writeBytes("/system/bin/touch /cache/recovery/boot\n");
            os.writeBytes("reboot recovery\n");

            os.writeBytes("sync\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();

            if (Utils.isSystemApp(mContext)) {
                ((PowerManager) mContext.getSystemService(Activity.POWER_SERVICE))
                        .reboot("recovery");
            } else {
                Runtime.getRuntime().exec("/system/bin/reboot recovery");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}