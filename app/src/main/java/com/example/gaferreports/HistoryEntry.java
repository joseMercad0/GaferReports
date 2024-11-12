package com.example.gaferreports;

public class HistoryEntry {
    private String date;
    private String trapType;
    private String poisonType;
    private int poisonAmount;
    private boolean consumption;
    private int consumptionPercentage;
    private boolean replace;
    private int replaceAmount;
    private String replacePoisonType;
    private boolean noAccess;
    private String previousDate;

    public HistoryEntry() {
        // Constructor vacío necesario para Firebase
    }

    public HistoryEntry(String date, String trapType, String poisonType, int poisonAmount, boolean consumption, int consumptionPercentage, boolean replace, int replaceAmount, String replacePoisonType, boolean noAccess, String previousDate) {
        this.date = date;
        this.trapType = trapType;
        this.poisonType = poisonType;
        this.poisonAmount = poisonAmount;
        this.consumption = consumption;
        this.consumptionPercentage = consumptionPercentage;
        this.replace = replace;
        this.replaceAmount = replaceAmount;
        this.replacePoisonType = replacePoisonType;
        this.noAccess = noAccess;
        this.previousDate = previousDate;

    }

    // Getters y setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

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

    public String getPreviousDate() {
        return previousDate;
    }

    public void setPreviousDate(String previousDate) {
        this.previousDate = previousDate;
    }

    public boolean isNoAccess() {
        return noAccess;
    }

    public void setNoAccess(boolean noAccess) {
        this.noAccess = noAccess;
    }

}
