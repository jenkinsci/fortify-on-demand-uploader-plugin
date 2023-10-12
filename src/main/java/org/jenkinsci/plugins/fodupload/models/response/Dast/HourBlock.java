package org.jenkinsci.plugins.fodupload.models.response.Dast;

public class HourBlock {
    private int hour;
    private boolean checked;

    // Constructors, getters, and setters

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
