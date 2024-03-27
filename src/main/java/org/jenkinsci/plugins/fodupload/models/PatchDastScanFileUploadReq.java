package org.jenkinsci.plugins.fodupload.models;


public class PatchDastScanFileUploadReq {

public String releaseId;

public String fileName;
public FodEnums.DastScanFileTypes dastFileType;

public byte[] content;

}
