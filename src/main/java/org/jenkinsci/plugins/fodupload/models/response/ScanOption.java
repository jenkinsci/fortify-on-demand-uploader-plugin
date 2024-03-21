package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class ScanOption {
    private Integer id;
    private String name;
    private String lastSelectedOption;
    private List<LookupItemsModel> options;

    public ScanOption(Integer id, String name, String lastSelectedOption, List<LookupItemsModel> options) {
        this.id = id;
        this.name = name;
        this.lastSelectedOption = lastSelectedOption;
        this.options = options;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastSelectedOption() {
        return lastSelectedOption;
    }

    public List<LookupItemsModel> getOptions() {
        return options;
    }
}
