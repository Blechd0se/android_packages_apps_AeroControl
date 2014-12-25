package com.aero.control;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;


import com.aero.control.helpers.Android.CirclePageIndicator;
import com.aero.control.sliderFragments.IntroductionFragment;
import com.aero.control.sliderFragments.PerAppFragment;
import com.aero.control.sliderFragments.SetOnBootFragment;
import com.aero.control.sliderFragments.TutorialFragment;
import com.aero.control.helpers.ZoomOutPageTransformer;

import java.io.File;

/**
 * Created by Alexander Christ on 09.09.14.
 */
public class SplashScreen extends FragmentActivity {

    private static int NUM_PAGES = 4;
    public static final String FIRSTRUN_AERO = "firstrun_aero";
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private CirclePageIndicator mCircleIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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

    }

    private class ScreenSlidePagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // Basic Fragment switcher;
        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;

            switch (position) {
                case 0:
                    fragment = new IntroductionFragment();
                    break;
                case 1:
                    fragment = new PerAppFragment();
                    break;
                case 2:
                    fragment = new SetOnBootFragment();
                    break;
                case 3:
                    fragment = new TutorialFragment();
                    break;
                default:
                    fragment = new IntroductionFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}