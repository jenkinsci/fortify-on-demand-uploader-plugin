package org.jenkinsci.plugins.fodupload;

import com.google.gson.Gson;

public class Json {

    private static Gson instance = new Gson();

    public static Gson getInstance() {
        return instance;
    }
}
