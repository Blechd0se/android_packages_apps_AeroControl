package com.aero.control.helpers.Android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aero.control.R;

/**
 * Created by Alexander Christ on 21.12.14.
 */
public class AboutDialog extends DialogFragment {

    private String mTitle;
    private Context mContext;
    private boolean mPayPalIcons = false;

    private String mNegativeText;
    private String mPositiveText;
    private String mNeutralText;
    private int mIcon;
    private View mLayout;

    public void setContext(Context context) {
        this.mContext = context;
    }

    public void setTitle(int i){
        this.mTitle = mContext.getText(i).toString();
    }

    public void setView(View v) {
        this.mLayout = v;
    }

    public void setIcon(int i) {
        this.mIcon = i;
    }

    public void setPayPalIcons(boolean b) {
        this.mPayPalIcons = b;
    }

    public void setPositiveButton(int i) {
        this.mPositiveText = mContext.getText(i).toString();
    }

    public void setNeutralButton(int i) {
        this.mNeutralText = mContext.getText(i).toString();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(mTitle)
                .setIcon(mIcon)
                .setView(mLayout)
                .setPositiveButton(mPositiveText, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=46VQEKBETN36U");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                }).setNeutralButton(mNeutralText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Uri uri = Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=quarx%40yandex%2eru&lc=DE&no_note=0&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                }).create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                if (mPayPalIcons) {
                    Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button neutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

                    Drawable drawable = mContext.getResources().getDrawable(
                            R.drawable.paypal);

                    // set the bounds to place the drawable a bit right
                    positive.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                    neutral.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

                    positive.setCompoundDrawablePadding(5);
                    neutral.setCompoundDrawablePadding(5);
                }
            }
        });

        return dialog;
    }
}
