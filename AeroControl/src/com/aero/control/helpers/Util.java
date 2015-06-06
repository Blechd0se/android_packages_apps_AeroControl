package com.aero.control.helpers;

import android.content.Context;

import com.aero.control.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Alexander Christ on 25.05.15.
 *
 * Utility methods
 */
public class Util {

    /**
     * Returns a random CharSeqeuence
     * @param context Context, the current Context
     * @return CharSeqeuence
     */
    public static CharSequence getRandomLoadingText(Context context) {

        ArrayList<Integer> randomData = new ArrayList<Integer>();

        randomData.add(R.string.random_programming_flux);
        randomData.add(R.string.random_nsa_loading);
        randomData.add(R.string.random_data_somewhere);
        randomData.add(R.string.random_shovelling_coal);
        randomData.add(R.string.random_testing_patience);
        randomData.add(R.string.random_prepare_awesomeness);
        randomData.add(R.string.random_working_you_know);

        return context.getText(randomData.get(new Random().nextInt(randomData.size())));
    }

}
