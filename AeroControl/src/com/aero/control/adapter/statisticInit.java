package com.aero.control.adapter;

/**
 * Created by Alexander Christ on 13.02.14.
 * Statistic Init. Determines which Parameter StatisticAdapter has.
 */
public class statisticInit {

    public String mFrequency;
    public String mTimeInState;
    public String mPercentage;

    public statisticInit() {
        super();
    }

    public statisticInit(String frequency, String timeInState, String percentage) {
        super();
        this.mFrequency = frequency;
        this.mTimeInState = timeInState;
        this.mPercentage = percentage;
    }

}