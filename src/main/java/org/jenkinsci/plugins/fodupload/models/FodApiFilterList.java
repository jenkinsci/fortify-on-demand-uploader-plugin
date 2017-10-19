package org.jenkinsci.plugins.fodupload.models;

import java.util.*;

public class FodApiFilterList {
    private Map<String, String> filters;

    public FodApiFilterList() {
        this.filters = new HashMap<>();
    }

    public FodApiFilterList addFilter(String key, String value) {
        this.filters.put(key, value);
        return this;
    }

    public FodApiFilterList addFilter(String key, int value) {
        this.filters.put(key, Integer.toString(value));
        return this;
    }

    public FodApiFilterList addFilter(String key, boolean value) {
        this.filters.put(key, Boolean.toString(value));
        return this;
    }

    public FodApiFilterList removeFilter(String key) {
        if (filters.containsKey(key))
            filters.remove(key);
        return this;
    }

    public String toString() {

        Iterator<Map.Entry<String, String>> iterator = filters.entrySet().iterator();

        List<String> list = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, String> filter = iterator.next();
            list.add(filter.getKey() + ":" + filter.getValue());
            iterator.remove();
        }

        return String.join("+", list);
    }
}
