package com.aero.control.fragments;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.Android.CustomListPreference;
import com.aero.control.helpers.Android.CustomPreference;
import com.aero.control.helpers.Android.Material.Slider;
import com.aero.control.helpers.FilePath;;
import com.aero.control.helpers.PreferenceHandler;
import com.aero.control.helpers.Shell;

import java.io.File;

/**
 * Created by ac on 16.09.13.
 */
public class GPUFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private String[] mColorValues;
    private PreferenceCategory PrefCat;
    private PreferenceScreen root;
    private AlertDialog mColorDialog;

    private GPUGovernorFragment mGPUGovernorFragment;

    private CustomPreference mGPUControl, mSweep2wake, mDoubletap2Wake, mColorControl;
    private CustomListPreference mGPUControlFrequencies, mGPUGovernor, mDisplayControl;

    private String mGPUFile;
    private String mGPUFreq;
    private String mGPUGov;

    private Shell mShell;

    private final static String NO_DATA_FOUND = "Unavailable";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        boolean checkGpuControl;
        boolean checkmSweep2wake;
        boolean checkDoubletap2wake;

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.gpu_fragment);

        root = this.getPreferenceScreen();
        final PreferenceCategory gpuCategory = (PreferenceCategory) findPreference("gpu_settings");

        // If there are already some entries, kill them all (with fire)
        if (PrefCat != null)
            root.removePreference(PrefCat);

        // Set up gpu control;
        mGPUControl = new CustomPreference(getActivity());
        mGPUControl.setName("gpu_control_enable");
        mGPUControl.setTitle(R.string.pref_gpu_control_enable);
        mGPUControl.setSummary(R.string.pref_gpu_control_enable_sum);
        mGPUControl.setLookUpDefault(FilePath.GPU_CONTROL_ACTIVE);
        mGPUControl.setOrder(5);
        gpuCategory.addPreference(mGPUControl);

        // Set up sweeptowake;
        mSweep2wake = new CustomPreference(getActivity());
        mSweep2wake.setName("sweeptowake");
        mSweep2wake.setTitle(R.string.pref_sweeptowake);
        mSweep2wake.setSummary(R.string.pref_sweeptowake_sum);
        mSweep2wake.setLookUpDefault(FilePath.SWEEP2WAKE);
        mSweep2wake.setOrder(10);
        gpuCategory.addPreference(mSweep2wake);

        // Set up doubletaptowake;
        mDoubletap2Wake = new CustomPreference(getActivity());
        mDoubletap2Wake.setName("doubletaptowake");
        mDoubletap2Wake.setTitle(R.string.pref_doubletaptowake);
        mDoubletap2Wake.setSummary(R.string.pref_doubletaptowake_sum);
        mDoubletap2Wake.setLookUpDefault(FilePath.DOUBLETAP2WAKE);
        mDoubletap2Wake.setOrder(15);
        gpuCategory.addPreference(mDoubletap2Wake);

        mGPUControlFrequencies = new CustomListPreference(getActivity());
        mGPUControlFrequencies.setName("gpu_max_freq");
        mGPUControlFrequencies.setTitle(R.string.pref_gpu_max_freq);
        mGPUControlFrequencies.setDialogTitle(R.string.pref_gpu_max_freq);
        mGPUControlFrequencies.setSummary(R.string.pref_gpu_max_freq_sum);
        mGPUControlFrequencies.setOrder(20);
        gpuCategory.addPreference(mGPUControlFrequencies);


        mGPUGovernor = new CustomListPreference(getActivity());
        mGPUGovernor.setName("set_gpu_governor");
        mGPUGovernor.setTitle("GPU Governor");
        mGPUGovernor.setDialogTitle("GPU Governor");
        mGPUGovernor.setSummary("GPU Governor");
        mGPUGovernor.setOrder(25);
        gpuCategory.addPreference(mGPUGovernor);


        mDisplayControl = new CustomListPreference(getActivity());
        mDisplayControl.setName("display_control");
        mDisplayControl.setTitle(R.string.pref_display_color);
        mDisplayControl.setDialogTitle(R.string.pref_display_color);
        mDisplayControl.setSummary(R.string.pref_display_color_sum);
        mDisplayControl.setOrder(30);
        gpuCategory.addPreference(mDisplayControl);


        mColorControl = (CustomPreference)root.findPreference("rgbValues");
        mColorControl.setOrder(40);
        mColorControl.setLookUpDefault(FilePath.COLOR_CONTROL);
        mGPUGovernor.setOnPreferenceChangeListener(this);
        mGPUGovernor.setOrder(45);
        mGPUControlFrequencies.setOnPreferenceChangeListener(this);
        mGPUControlFrequencies.setOrder(21);

        /* Find correct gpu path */
        for (String a : FilePath.GPU_FILES) {
            if (AeroActivity.genHelper.doesExist(a)) {
                mGPUFile = a;
                break;
            }
        }

        if(!(AeroActivity.genHelper.doesExist(FilePath.SWEEP2WAKE)))
            gpuCategory.removePreference(mSweep2wake);

        if(!(AeroActivity.genHelper.doesExist(FilePath.DOUBLETAP2WAKE)))
            gpuCategory.removePreference(mDoubletap2Wake);

        if(!(AeroActivity.genHelper.doesExist(FilePath.GPU_CONTROL_ACTIVE)))
                gpuCategory.removePreference(mGPUControl);

        if (mGPUFile == null)
            gpuCategory.removePreference(mGPUControlFrequencies);

        if (!(AeroActivity.genHelper.doesExist(FilePath.COLOR_CONTROL)))
            gpuCategory.removePreference(mColorControl);

        if (AeroActivity.shell.getInfo(FilePath.DISPLAY_COLOR).equals(NO_DATA_FOUND))
            gpuCategory.removePreference(mDisplayControl);

        final CustomPreference gpu_gov_settings = (CustomPreference)root.findPreference("gpu_gov_settings");
        if (AeroActivity.genHelper.doesExist(("/sys/module/msm_kgsl_core/parameters"))) {
            gpu_gov_settings.setOrder(35);
            gpu_gov_settings.setHideOnBoot(true);

            gpu_gov_settings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    if (mGPUGovernorFragment == null)
                        mGPUGovernorFragment = new GPUGovernorFragment();

                    AeroActivity.mHandler.postDelayed(new Runnable()  {
                        @Override
                        public void run() {
                            getFragmentManager()
                                    .beginTransaction()
                                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                    .replace(R.id.content_frame, mGPUGovernorFragment)
                                    .addToBackStack("GPU Governor")
                                    .commit();
                            mGPUGovernorFragment.setTitle();
                        }
                    },AeroActivity.genHelper.getDefaultDelay());
                    return true;
                }
            });
        } else {
            gpuCategory.removePreference(gpu_gov_settings);
        }

        // Get our strings;
        CharSequence[] display_entries = {
                getText(R.string.defy_red_colors),
                getText(R.string.defy_green_colors),
                getText(R.string.defy_energy_saver)
        };
        CharSequence[] display_values = {"31", "9", "0"};
        mDisplayControl.setEntries(display_entries);
        mDisplayControl.setEntryValues(display_values);

        for (String s : FilePath.GPU_FREQ_ARRAY) {
            if (AeroActivity.genHelper.doesExist(s))
                mGPUFreq = s;
        }

        // Just throw in our frequencies;
        if (mGPUFreq != null) {
            mGPUControlFrequencies.setEntries(AeroActivity.shell.getInfoArray(mGPUFreq, 1, 0));
            mGPUControlFrequencies.setEntryValues(AeroActivity.shell.getInfoArray(mGPUFreq, 0, 0));
        } else {
            mGPUControlFrequencies.setEntries(R.array.gpu_frequency_list);
            mGPUControlFrequencies.setEntryValues(R.array.gpu_frequency_list_values);
        }


        for (String s: FilePath.GPU_GOV_ARRAY) {
            if (AeroActivity.genHelper.doesExist(s))
                mGPUGov = s;
        }

        if (mGPUGov != null) {

            String tmp;
            if (AeroActivity.genHelper.doesExist(mGPUGov + "available_governors")) {
                tmp = mGPUGov + "available_governors";
            } else {
                tmp = mGPUGov + "governor";
            }

            mGPUGovernor.setEntries(AeroActivity.shell.getInfoArray(tmp, 0, 0));
            mGPUGovernor.setEntryValues(AeroActivity.shell.getInfoArray(tmp, 0, 0));
            mGPUGovernor.setValue(AeroActivity.shell.getInfo(mGPUGov + "governor"));
            mGPUGovernor.setSummary(AeroActivity.shell.getInfo(mGPUGov + "governor"));
            mGPUGovernor.setDialogIcon(R.drawable.device_old);
        } else {
            gpuCategory.removePreference(mGPUGovernor);
        }

        try  {

            if (mGPUFile != null) {
                mGPUControlFrequencies.setValue(AeroActivity.shell.getInfoArray(mGPUFile, 0, 0)[0]);
                mGPUControlFrequencies.setSummary(AeroActivity.shell.toMHz((AeroActivity.shell.getInfoArray(mGPUFile, 0, 0)[0].substring(0,
                        AeroActivity.shell.getInfoArray(mGPUFile, 0, 0)[0].length() - 3))));
            }

            // Check if enabled or not;
            if (AeroActivity.shell.getInfo(FilePath.GPU_CONTROL_ACTIVE).equals("1")) {
                checkGpuControl = true;
                mGPUControl.setSummary(R.string.enabled);
            } else {
                checkGpuControl = false;
                mGPUControl.setSummary(R.string.disabled);
            }

            // Check if enabled or not;
            if (AeroActivity.shell.getInfo(FilePath.SWEEP2WAKE).equals("1")) {
                checkmSweep2wake = true;
                mSweep2wake.setSummary(R.string.enabled);
            } else {
                checkmSweep2wake = false;
                mSweep2wake.setSummary(R.string.disabled);
            }

            if (AeroActivity.shell.getInfo(FilePath.DOUBLETAP2WAKE).equals("1")) {
                checkDoubletap2wake = true;
                mDoubletap2Wake.setSummary(R.string.enabled);
            } else {
                checkDoubletap2wake = false;
                mDoubletap2Wake.setSummary(R.string.disabled);
            }

            mSweep2wake.setClicked(checkmSweep2wake);
            mDoubletap2Wake.setClicked(checkDoubletap2wake);
            mGPUControl.setClicked(checkGpuControl);

        } catch (ArrayIndexOutOfBoundsException e) {
            /*
             * If the folder is missing, disable this feature completely;
             */
            mGPUControlFrequencies.setSummary(NO_DATA_FOUND);
            mGPUControlFrequencies.setEnabled(false);
            mGPUControl.setEnabled(false);

            Toast.makeText(getActivity(), "GPU Control is not supported with your kernel.", Toast.LENGTH_LONG).show();
        }
        mGPUControlFrequencies.setDialogIcon(R.drawable.gpu);

        if (!(gpuCategory.getPreferenceCount() > 0)) {
            gpuCategory.setTitle(R.string.nogpu_data);
        }
    }

    private void showColorControl(final SharedPreferences.Editor editor, final CustomPreference cusPref) {

        if (mShell == null) {
            mShell = new Shell("su", true);
        }

        mColorValues = AeroActivity.shell.getInfoArray(FilePath.COLOR_CONTROL, 0, 0);

        if (mColorValues[0].equals(NO_DATA_FOUND)) {
            Toast.makeText(getActivity(), R.string.no_data_found, Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.flower);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.gpu_color_control, null);

        final Slider redValues = (Slider)layout.findViewById(R.id.redValues);
        final Slider greenValues = (Slider)layout.findViewById(R.id.greenValues);
        final Slider blueValues = (Slider)layout.findViewById(R.id.blueValues);

        final EditText redValue = (EditText)layout.findViewById(R.id.redValue);
        final EditText greenValue = (EditText)layout.findViewById(R.id.greenValue);
        final EditText blueValue = (EditText)layout.findViewById(R.id.blueValue);

        redValues.setProgress(Integer.parseInt(mColorValues[0]));
        greenValues.setProgress(Integer.parseInt(mColorValues[1]));
        blueValues.setProgress(Integer.parseInt(mColorValues[2]));

        redValue.setText(mColorValues[0]);
        greenValue.setText(mColorValues[1]);
        blueValue.setText(mColorValues[2]);

        redValue.setEnabled(true);
        greenValue.setEnabled(true);
        blueValue.setEnabled(true);

        redValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int i = Integer.parseInt(s.toString());
                    if (i <= 255 && i >= 0) {
                        redValues.setProgress(i);
                        setColorValues(redValue, greenValue, blueValue, cusPref, editor);
                    } else {
                        redValue.setText("" + 255);
                    }
                } catch (NumberFormatException e) {}
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        greenValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int i = Integer.parseInt(s.toString());
                    if (i <= 255 && i >= 0) {
                        greenValues.setProgress(i);
                        setColorValues(redValue, greenValue, blueValue, cusPref, editor);
                    } else {
                        greenValue.setText("" + 255);
                    }
                } catch (NumberFormatException e) {}
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        blueValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int i = Integer.parseInt(s.toString());
                    if (i <= 255 && i >= 0) {
                        blueValues.setProgress(i);
                        setColorValues(redValue, greenValue, blueValue, cusPref, editor);
                    } else {
                        blueValue.setText("" + 255);
                    }
                } catch (NumberFormatException e) {}
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        redValues.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                redValue.setText("" + value);
                setColorValues(redValue, greenValue, blueValue, cusPref, editor);
            }
        });
        greenValues.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                greenValue.setText("" + value);
                setColorValues(redValue, greenValue, blueValue, cusPref, editor);
            }
        });
        blueValues.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                blueValue.setText("" + value);
                setColorValues(redValue, greenValue, blueValue, cusPref, editor);
            }
        });

        builder.setTitle(R.string.pref_display_color);
        builder.setView(layout);

        // Use the builder to create a new instance, which we can dismiss later;
        mColorDialog = builder.create();
        mColorDialog.show();
    }


    private void setColorValues(EditText redValue, EditText greenValue, EditText blueValue,
                                CustomPreference cusPref, SharedPreferences.Editor editor) {

        int red = Integer.parseInt(redValue.getText().toString());
        int green = Integer.parseInt(greenValue.getText().toString());
        int blue = Integer.parseInt(blueValue.getText().toString());

        if (red > 255 || blue > 255 || green > 255 ) {
            Toast.makeText(getActivity(), "The values are out of range!", Toast.LENGTH_LONG).show();
            return;
        } else if (red < 10 && blue < 10 && green < 10) {
            Toast.makeText(getActivity(), "Those values are pretty low, are you sure?", Toast.LENGTH_LONG).show();
            return;
        }

        String rgbValues = redValue.getText() + " " + greenValue.getText() + " " + blueValue.getText();

        mShell.addCommand("echo " + rgbValues + " > " + FilePath.COLOR_CONTROL);

        if (new File(FilePath.COLOR_CONTROL_BIT).exists())
            mShell.addCommand("echo 1 > " + FilePath.COLOR_CONTROL_BIT);

        mShell.runInteractive();

        if (cusPref.isChecked())
            editor.putString(cusPref.getName(), rgbValues).commit();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mColorDialog != null) {
            // In order to make the shell work correctly, dismiss here;
            mColorDialog.dismiss();
        }
        if (mShell != null) {
            mShell.closeInteractive();
            mShell = null;
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        CustomPreference cusPref = null;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();

        if (preference == mSweep2wake) {

            mSweep2wake.setClicked(!mSweep2wake.isClicked());

            if (mSweep2wake.isClicked())
                AeroActivity.shell.setRootInfo("1", FilePath.SWEEP2WAKE);
            else
                AeroActivity.shell.setRootInfo("0", FilePath.SWEEP2WAKE);

            cusPref = (CustomPreference)preference;

        } else if (preference == mDoubletap2Wake) {

            mDoubletap2Wake.setClicked(!mDoubletap2Wake.isClicked());

            if (mDoubletap2Wake.isClicked())
                AeroActivity.shell.setRootInfo("1", FilePath.DOUBLETAP2WAKE);
            else
                AeroActivity.shell.setRootInfo("0", FilePath.DOUBLETAP2WAKE);

            cusPref = (CustomPreference)preference;

        } else if (preference == mColorControl) {

            cusPref = (CustomPreference)preference;

            showColorControl(editor, cusPref);

        } else if (preference == mGPUControl) {

            mGPUControl.setClicked(!mGPUControl.isClicked());

            if (mGPUControl.isClicked())
                AeroActivity.shell.setRootInfo("1", FilePath.GPU_CONTROL_ACTIVE);
            else
                AeroActivity.shell.setRootInfo("0", FilePath.GPU_CONTROL_ACTIVE);

            cusPref = (CustomPreference)preference;
        }

        // If its checked, we want to save it;
        if (cusPref != null) {
            if (cusPref.isChecked()) {
                if (cusPref.isClicked() != null) {
                    String state = cusPref.isClicked() ? "1" : "0";
                    editor.putString(cusPref.getName(), state).commit();
                }
            }
        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String a = (String) newValue;
        String newSummary = "";
        String path = "";

        if (preference == mGPUControlFrequencies) {

            path = mGPUFile;
            newSummary = AeroActivity.shell.toMHz((a.substring(0, a.length() - 3)));

        } else if (preference == mGPUGovernor) {
            // If there are already some entries, kill them all (with fire)
            if (PrefCat != null)
                root.removePreference(PrefCat);

            if (mGPUGov == null) {
                for (String s: FilePath.GPU_GOV_ARRAY) {
                    if (AeroActivity.genHelper.doesExist(s))
                        mGPUGov = s;
                }
            }

            path = mGPUGov + "governor";
            newSummary = a;

        }  else if (preference == mDisplayControl) {

            // Get Permissions first, then execute;
            final String[] commands = new String[]
                    {
                            "chmod 0664 " + FilePath.DISPLAY_COLOR,
                            "echo " + a + " > " + FilePath.DISPLAY_COLOR,
                    };
            AeroActivity.shell.setRootInfo(commands);

            Toast.makeText(getActivity(), "Turn your display off/on :)", Toast.LENGTH_LONG).show();

            // Return earlier, since we executed an array;
            return true;
        }

        AeroActivity.shell.setRootInfo(a, path);

        if(!newSummary.equals(""))
            preference.setSummary(newSummary);

        return true;
    }


    // Create our options menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mGPUGov == null) {
            for (String s: FilePath.GPU_GOV_ARRAY) {
                if (AeroActivity.genHelper.doesExist(s))
                    mGPUGov = s;
            }
        }

        if (AeroActivity.genHelper.doesExist(mGPUGov)) {
            inflater.inflate(R.menu.cpu_menu, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_governor_settings:

                String[] completeParamterList = null;
                try {
                    completeParamterList = AeroActivity.shell.getDirInfo(mGPUGov + AeroActivity.shell.getInfo(mGPUGov + "governor"), true);
                } catch (NullPointerException e) {
                    Toast.makeText(getActivity(), "Looks like there are no parameter for this governor?", Toast.LENGTH_LONG).show();
                    Log.e("Aero", "Couldn't find any parameters for this governor!", e);
                    return true;
                } finally {
                    if (completeParamterList == null) {
                        Toast.makeText(getActivity(), "Looks like there are no parameter for this governor?", Toast.LENGTH_LONG).show();
                        Log.e("Aero", "We found no parameters for this governor, maybe because it has none?");
                        return true;
                    }
                }

                // If there are already some entries, kill them all (with fire)
                if (PrefCat != null)
                    root.removePreference(PrefCat);

                PrefCat = new PreferenceCategory(getActivity());
                PrefCat.setTitle(R.string.perf_gpu_gov_settings);
                root.addPreference(PrefCat);

                try {

                    PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

                    h.genPrefFromDictionary(completeParamterList, mGPUGov + AeroActivity.shell.getInfo(mGPUGov + "governor"));

                } catch (NullPointerException e) {
                    Log.e("Aero", "I couldn't get any files!", e);
                }


                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
