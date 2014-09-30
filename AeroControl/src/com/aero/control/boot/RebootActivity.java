package com.aero.control.boot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aero.control.R;

/**
 * Created by root on 30.11.13.
 */
public class RebootActivity extends Activity {

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();

            // Just reuse aboutScreen, because its Linear and has a TextView
            View layout = inflater.inflate(R.layout.about_screen, null);
            TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

            builder.setCancelable(false);
            builder.setIcon(R.drawable.warning);
            builder.setTitle(R.string.reboot_notifier_header);
            aboutText.setText(getText(R.string.reboot_notifier));
            aboutText.setTextSize(14);

            builder.setView(layout)
                    .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            finish();
                        }
                    });
            builder.show();
        }

}
