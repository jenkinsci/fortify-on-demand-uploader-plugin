package org.jenkinsci.plugins.fodupload.models.response;

import java.util.ArrayList;
import java.util.List;

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
