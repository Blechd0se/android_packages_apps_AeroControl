package com.aero.control;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.aero.control.helpers.Android.CirclePageIndicator;
import com.aero.control.sliderFragments.IntroductionFragment;
import com.aero.control.sliderFragments.PerAppFragment;
import com.aero.control.sliderFragments.SetOnBootFragment;
import com.aero.control.sliderFragments.TutorialFragment;
import com.aero.control.helpers.ZoomOutPageTransformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander Christ on 09.09.14.
 */
public class SplashScreen extends FragmentActivity {

    private static int NUM_PAGES = 4;
    public static final String FIRSTRUN_AERO = "firstrun_aero";
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private CirclePageIndicator mCircleIndicator;
    public Button mSkip;
    private List<android.support.v4.app.Fragment> mFragments = new ArrayList<android.support.v4.app.Fragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mFragments.clear();
        mFragments.add(new IntroductionFragment());
        mFragments.add(new PerAppFragment());
        mFragments.add(new SetOnBootFragment());
        mFragments.add(new TutorialFragment());

        ContextWrapper cw = new ContextWrapper(getBaseContext());
        File firstrun_aero = new File(cw.getFilesDir() + "/" + FIRSTRUN_AERO);

        // First check if its already done;
        if (firstrun_aero.exists()) {
            Intent i = new Intent(this, AeroActivity.class);
            startActivity(i);

            // close this activity
            this.finish();
        }

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mPager.setAdapter(mPagerAdapter);
        mCircleIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mCircleIndicator.setViewPager(mPager);
        mSkip = (Button) findViewById(R.id.splash_button);

        initDefaultSkip();

    }

    public void initDefaultSkip() {

        // Temporarly save the activity;
        final Activity tmp = (Activity)this;

        mSkip.setText(R.string.skip_splash);

        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    FileOutputStream fos = tmp.openFileOutput(SplashScreen.FIRSTRUN_AERO, Context.MODE_PRIVATE);
                    fos.write("1".getBytes());
                    fos.close();
                } catch (IOException e) {
                    Log.e("Aero", "Could not save file(s). ", e);
                } catch (NullPointerException e) {
                    Log.e("Aero", "OpenFileOutput probably was initialized on a null-object.", e);
                }

                Intent i = new Intent(tmp, AeroActivity.class);
                startActivity(i);

                // close this activity
                tmp.finish();
            }
        });

    }

    private class ScreenSlidePagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // Basic Fragment switcher;
        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;

            fragment = mFragments.get(position);

            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}