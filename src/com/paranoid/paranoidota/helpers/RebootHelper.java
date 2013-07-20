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

package com.paranoid.paranoidota.helpers;

import java.io.DataOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.paranoid.paranoidota.IOUtils;
import com.paranoid.paranoidota.InstallOptionsCursor;
import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;

public class RebootHelper {

    private Context mContext;
    private RecoveryHelper mRecoveryHelper;
    private int mSelectedBackup;

    public RebootHelper(Context context, RecoveryHelper recoveryHelper) {
        mContext = context;
        mRecoveryHelper = recoveryHelper;
    }

    public void showBackupDialog(Context context) {
        showBackupDialog(context, null, false, false, false);
    }

    public void showRestoreDialog(final Context context) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_restore_title);

        final String backupFolder = mRecoveryHelper.getBackupDir(false);
        final String[] backups = mRecoveryHelper.getBackupList();
        mSelectedBackup = backups.length > 0 ? 0 : -1;

        alert.setSingleChoiceItems(backups, mSelectedBackup, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                mSelectedBackup = which;
            }
        });

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                if (mSelectedBackup >= 0) {
                    reboot(context, null, false, false, false, null, null, backupFolder
                            + backups[mSelectedBackup]);
                }
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();

    }

    public void simpleReboot(Context context) {
        reboot(context, null, false, false, false, null, null, null, true);
    }

    public void simpleReboot(Context context, boolean wipeData, boolean wipeCaches) {
        reboot(context, null, false, wipeData, wipeCaches, null, null, null, false);
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

                reboot(context, items, wipeSystem, wipeData, wipeCaches, text, backupOptions, null);
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public void showRebootDialog(final Context context, final String[] items) {

        if (items == null || items.length == 0) {
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        final InstallOptionsCursor cursor = new InstallOptionsCursor(context);

        if (cursor.getCount() > 0) {
            alert.setTitle(R.string.alert_reboot_install_title);
            alert.setMultiChoiceItems(cursor, cursor.getIsCheckedColumn(), cursor.getLabelColumn(),
                    new DialogInterface.OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            cursor.setOption(which, isChecked);
                        }

                    });
        } else {
            alert.setTitle(R.string.alert_reboot_only_install_title);
            alert.setMessage(R.string.alert_reboot_message);
        }

        alert.setPositiveButton(R.string.alert_reboot_now, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                if (cursor.isBackup()) {
                    showBackupDialog(context, null, cursor.isWipeSystem(), cursor.isWipeData(),
                            cursor.isWipeCaches());
                } else {
                    reboot(context, items, cursor.isWipeSystem(), cursor.isWipeData(),
                            cursor.isWipeCaches(), null, null, null);
                }

            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void reboot(Context context, String[] items, boolean wipeSystem, boolean wipeData, boolean wipeCaches,
            String backupFolder, String backupOptions, String restore) {
        reboot(context, items, wipeSystem, wipeData, wipeCaches, backupFolder, backupOptions,
                restore, false);
    }

    private void reboot(Context context, final String[] items, final boolean wipeSystem,
            final boolean wipeData, final boolean wipeCaches, final String backupFolder,
            final String backupOptions, final String restore, final boolean skipCommands) {

        if (wipeSystem) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle(R.string.alert_wipe_system_title);
            alert.setMessage(R.string.alert_wipe_system_message);

            alert.setPositiveButton(R.string.alert_reboot_now,
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();

                            _reboot(items, wipeSystem, wipeData, wipeCaches, backupFolder,
                                    backupOptions, restore, skipCommands);

                        }
                    });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        } else {
            _reboot(items, wipeSystem, wipeData, wipeCaches, backupFolder, backupOptions, restore,
                    skipCommands);
        }

    }

    private void _reboot(String[] items, boolean wipeSystem, boolean wipeData, boolean wipeCaches,
            String backupFolder, String backupOptions, String restore, boolean skipCommands) {

        try {

            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());

            os.writeBytes("rm -f /cache/recovery/command\n");
            os.writeBytes("rm -f /cache/recovery/extendedcommand\n");
            os.writeBytes("rm -f /cache/recovery/openrecoveryscript\n");

            if (!skipCommands) {

                String file = mRecoveryHelper.getCommandsFile();

                String[] commands = mRecoveryHelper.getCommands(items, wipeSystem, wipeData,
                        wipeCaches, backupFolder, backupOptions, restore);
                if (commands != null) {
                    int size = commands.length, i = 0;
                    for (; i < size; i++) {
                        os.writeBytes("echo '" + commands[i] + "' >> /cache/recovery/" + file
                                + "\n");
                    }
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