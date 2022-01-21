package org.jenkinsci.plugins.fodupload.models.response;

import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("UWF_UNWRITTEN_FIELD") //TotalCount is actually written just at runtime.
public class GenericListResponse<T> {
    private List<T> items = new ArrayList<>();
    private int totalCount;

    public int getTotalCount() {
        return totalCount;
    }

    public List<T> getItems() {
        return items;
    }
}
