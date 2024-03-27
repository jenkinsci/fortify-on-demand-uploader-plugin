package org.jenkinsci.plugins.fodupload.models.response.Dast;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class BlackoutEntry {
    private String day;
    private List<HourBlock> hourBlocks;

    // Constructors, getters, and setters

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<HourBlock> getHourBlocks() {
        return hourBlocks;
    }

    public void setHourBlocks(List<HourBlock> hourBlocks) {
        this.hourBlocks = hourBlocks;
    }
}
