package org.jenkinsci.plugins.fodupload;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexFileFilter implements FileFilter, Serializable {
    private static final long serialVersionUID = 1L;
    private final Pattern filePattern;

    /**
     * Constructor for filtering file names
     *
     * @param filePattern file extensions to filter
     */
    public RegexFileFilter(Pattern filePattern) {
        this.filePattern = filePattern;
    }

    @Override
    public boolean accept(File file) {
        Matcher m = filePattern.matcher(file.getName());
        return m.matches();
    }
}
