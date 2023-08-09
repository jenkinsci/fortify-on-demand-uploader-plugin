package org.jenkinsci.plugins.fodupload.FodApi;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class ResponseContent implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient InputStream _body;
    private final boolean _isSuccessful;
    private final int _code;
    private final String _message;

    private boolean _bodyParsed = true;
    private String _bodyContent = null;

    public ResponseContent(InputStream body, boolean isSuccessful, int code, String message) {
        _body = body;
        _bodyParsed = false;
        _isSuccessful = isSuccessful;
        _code = code;
        _message = message;
    }

    public void parseBody() throws IOException {
        if (!_bodyParsed) {
            _bodyParsed = true;
            _bodyContent = Utils.getRawBody(_body);
            _body.close();
        }
    }

    public String bodyContent() {
        return _bodyContent;
    }

    public boolean isSuccessful() {
        return _isSuccessful;
    }

    public int code() {
        return _code;
    }

    public String message() {
        return _message;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        _body = null;
    }
}
