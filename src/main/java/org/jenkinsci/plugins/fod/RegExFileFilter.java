package org.jenkinsci.plugins.fod;
import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExFileFilter implements FileFilter, Serializable{

	private static final long serialVersionUID = 1L;

	private final Pattern filePattern;
	
	public RegExFileFilter(Pattern filePattern) {
		this.filePattern = filePattern;
	}
	
	
	@Override
	public boolean accept(File pathname) {
		
		boolean matches = false;

		Matcher m = filePattern.matcher(pathname.getName());
		
		matches = m.matches();

		return matches;
	}
	
	

}
