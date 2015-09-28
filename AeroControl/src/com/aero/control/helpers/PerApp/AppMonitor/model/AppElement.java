package com.aero.control.helpers.PerApp.AppMonitor.model;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander Christ on 01.06.15.
 * Data model object to store GUI relevant information of AppMonitor
 */
public class AppElement implements Parcelable {

    private String mAppName;
    private String mRealAppName;
    private Drawable mAppDrawable;
    private Long mUsage;

    private ArrayList<AppElementDetail> mAverageData;

    public AppElement(String name, Drawable appDrawable) {
        this.mAppName = name;
        this.mAppDrawable = appDrawable;
        this.mAverageData = new ArrayList<AppElementDetail>();
    }

    public AppElement(Parcel parcel) {
        this.mAppName = parcel.readString();
        this.mAverageData = parcel.readArrayList(new ClassLoader() {
            @Override
            protected Class<?> findClass(String className) throws ClassNotFoundException {
                return super.findClass(className);
            }
        });
        this.mRealAppName = parcel.readString();
    }

    /**
     * Returns the child-data (list) of this AppElement
     * @return List<AppElementDetail>
     */
    public List<AppElementDetail> getChildData() {
        return mAverageData;
    }

    /**
     * Returns the package name of this AppElement (e.g. com.aero.contro)
     * @return String
     */
    public String getName() {
        return mAppName;
    }

    /**
     * Returns the real name of this AppElement (e.g. Aero Control)
     * @return String
     */
    public String getRealName() {
        return mRealAppName;
    }

    /**
     * Allows to set the real name of this AppElement
     * @param realName String, (e.g. Aero Control)
     */
    public void setRealName(String realName) {
        this.mRealAppName = realName;
    }

    /**
     * Returns the drawable that is stored inside this AppElement
     * @return Drawable (usually the appicon)
     */
    public Drawable getImage() {
        return mAppDrawable;
    }

    /**
     * Returns the usage-counter
     * @return Long
     */
    public Long getUsage() {
        return mUsage;
    }

    /**
     * Sets the usage-counter
     * @param usage Long, usually the TimeUsage in ms
     */
    public void setUsage(Long usage) {
        this.mUsage = usage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mAppName);
        parcel.writeList(mAverageData);
        parcel.writeString(mRealAppName);
    }

    public static final Parcelable.Creator<AppElement> CREATOR =
            new Parcelable.Creator<AppElement>(){

                @Override
                public AppElement createFromParcel(Parcel source) {
                    return new AppElement(source);
                }

                @Override
                public AppElement[] newArray(int size) {
                    return new AppElement[size];
                }
            };

}
