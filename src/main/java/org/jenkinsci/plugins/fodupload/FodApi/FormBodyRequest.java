package org.jenkinsci.plugins.fodupload.FodApi;

import java.util.*;

public class FormBodyRequest extends HttpRequest {
    private final Hashtable<String, String> _body = new Hashtable<>();

    public FormBodyRequest(String url, Verb verb) {
        super(url, verb);
    }

    public FormBodyRequest addValue(String key, String value) {
        if (_body.containsKey(key)) _body.replace(key, value);
        else _body.put(key, value);

        return this;
    }

    public Set<Map.Entry<String, String>> body(){
        return _body.entrySet();
    }
}
