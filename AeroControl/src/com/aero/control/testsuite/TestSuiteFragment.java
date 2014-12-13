package com.aero.control.testsuite;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.TextView;

import com.aero.control.R;

/**
 * Created by Alexander Christ on 16.08.14.
 */
public class TestSuiteFragment extends PreferenceFragment {

    private PreferenceScreen root;
    private final static int mNumProcessors = Runtime.getRuntime().availableProcessors();
    private static final String LOG_TAG = PreferenceFragment.class.getName();
    private double mStartTime, mTargetTime;
    private double mMFlops = 0;
    private int mProgress;
    private ActionBar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.testsuite_fragment);
        root = this.getPreferenceScreen();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActionBar = getActivity().getActionBar();
            mActionBar.setTitle(getText(R.string.slider_testsuite_settings));
        } else {
            TextView mActionBarTitle = (TextView) getActivity().findViewById(getResources().getIdentifier("action_bar_title", "id", "android"));
            mActionBarTitle.setText(R.string.slider_testsuite_settings);
        }

        // Load our custom preferences;
        loadSettings();
    }

    public void loadSettings() {

        final PreferenceCategory TestSuiteCat =
                (PreferenceCategory) findPreference("testsuite_settings");

        Preference lpPreference = (Preference) root.findPreference("linpack_test");


        lpPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                new RunBenchmark().execute();

                return false;
            }
        });
        TestSuiteCat.addPreference(lpPreference);
    }

    /*
     * Configures how many threads should be started
     */
    public final void setUpBenchmark(int numThreads) {

        mMFlops = 0;

        Runnable[] runWorker = new Runnable[numThreads];

        for (int i = 0; i < numThreads; i++) {

            final Linpack lp = new Linpack();
            lp.resetBenchmark();
            warmUp(lp);
            lp.resetBenchmark();

            runWorker[i] = new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        runTest(lp);
                    }
                }
            };
        }
        mStartTime = System.currentTimeMillis();
        // 5 Second Benchmark
        mTargetTime = (mStartTime + 5000);

        for (int j = 0; j < numThreads; j++) {
            Thread mWorker = new Thread(runWorker[j]);
            mWorker.start();
            Log.e(LOG_TAG, "Running now!");
        }
    }

    public final void warmUp(Linpack lp) {

        // WarumUp the hardware;
        for (int i = 0; i < 50; i++)
            lp.run_benchmark();
    }

    /*
     * Does the real benchmarking via recursive calls
     */
    public final void runTest(Linpack lp) {

        if ((System.currentTimeMillis()) < mTargetTime) {
            lp.run_benchmark();
            runTest(lp);
        } else {
            Log.e(LOG_TAG, "Stopped the test");
            gatherResults(lp);
        }
    }

    public final void gatherResults(Linpack lp) {

        mMFlops += lp.getMFlops();
        // Completed another run;
        mProgress++;

        Log.e(LOG_TAG, "Average MFLop-Counter: " + mMFlops +
                " Time Passed:" + lp.getTimePassed());
    }

    private class RunBenchmark extends AsyncTask<Void, Integer, Void> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "Running Linpack","Burning your CPUs...", false);
            progressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.spinner_animation));
            mProgress = 0;
        }

        @Override
        protected Void doInBackground(Void... params) {
            while(mProgress < mNumProcessors){
                publishProgress(mProgress);
                setUpBenchmark(mNumProcessors);
                /*
                 * While the first loop starts up the benchmark, the latter one
                 * takes care of a simple lock. As long as we keep spinning no more
                 * threads are created. Overheat should be negligible, since the cpu
                 * just jumps around.
                 */
                while (mProgress < mNumProcessors) {}
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Result");
            builder.setMessage("Great! \nYou have achieved; \n" + mMFlops + " MFlops");
            builder.show();
            mMFlops = 0;

            progressDialog.dismiss();
        };
    }
}