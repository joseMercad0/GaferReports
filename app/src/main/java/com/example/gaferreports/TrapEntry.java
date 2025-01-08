package com.example.gaferreports;

import android.os.Parcel;
import android.os.Parcelable;

public class TrapEntry implements Parcelable {
    private String trapType;
    private String poisonType;
    private int poisonAmount;
    private boolean consumption;
    private int consumptionPercentage;
    private boolean replace;
    private int replaceAmount;
    private String replacePoisonType;
    private boolean noAccess;
    private boolean perdido;

    // Constructor
    public TrapEntry(String trapType, String poisonType, int poisonAmount, boolean consumption, int consumptionPercentage, boolean replace, int replaceAmount, String replacePoisonType, boolean noAccess, boolean perdido) {
        this.trapType = trapType;
        this.poisonType = poisonType;
        this.poisonAmount = poisonAmount;
        this.consumption = consumption;
        this.consumptionPercentage = consumptionPercentage;
        this.replace = replace;
        this.replaceAmount = replaceAmount;
        this.replacePoisonType = replacePoisonType;

    }

    // Getter and Setter methods


    public String getTrapType() {
        return trapType;
    }

    public void setTrapType(String trapType) {
        this.trapType = trapType;
    }

    public String getPoisonType() {
        return poisonType;
    }

    public void setPoisonType(String poisonType) {
        this.poisonType = poisonType;
    }

    public int getPoisonAmount() {
        return poisonAmount;
    }

    public void setPoisonAmount(int poisonAmount) {
        this.poisonAmount = poisonAmount;
    }

    public boolean isConsumption() {
        return consumption;
    }

    public void setConsumption(boolean consumption) {
        this.consumption = consumption;
    }

    public int getConsumptionPercentage() {
        return consumptionPercentage;
    }

    public void setConsumptionPercentage(int consumptionPercentage) {
        this.consumptionPercentage = consumptionPercentage;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public int getReplaceAmount() {
        return replaceAmount;
    }

    public void setReplaceAmount(int replaceAmount) {
        this.replaceAmount = replaceAmount;
    }

    public String getReplacePoisonType() {
        return replacePoisonType;
    }

    public void setReplacePoisonType(String replacePoisonType) {
        this.replacePoisonType = replacePoisonType;
    }

    public boolean isNoAccess() {
        return noAccess;
    }

    public void setNoAccess(boolean noAccess) {
        this.noAccess = noAccess;
    }

    public boolean isPerdido() {
        return perdido;
    }

    public void setPerdido(boolean perdido) {
        this.perdido = perdido;
    }


    // Parcelable implementation
    protected TrapEntry(Parcel in) {
        trapType = in.readString();
        poisonType = in.readString();
        poisonAmount = in.readInt();
        consumption = in.readByte() != 0;
        consumptionPercentage = in.readInt();
        replace = in.readByte() != 0;
        replaceAmount = in.readInt();
        replacePoisonType = in.readString();
    }

    public static final Creator<TrapEntry> CREATOR = new Creator<TrapEntry>() {
        @Override
        public TrapEntry createFromParcel(Parcel in) {
            return new TrapEntry(in);
        }

        @Override
        public TrapEntry[] newArray(int size) {
            return new TrapEntry[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trapType);
        dest.writeString(poisonType);
        dest.writeInt(poisonAmount);
        dest.writeByte((byte) (consumption ? 1 : 0));
        dest.writeInt(consumptionPercentage);
        dest.writeByte((byte) (replace ? 1 : 0));
        dest.writeInt(replaceAmount);
        dest.writeString(replacePoisonType);
    }

    // Getters and setters

}
