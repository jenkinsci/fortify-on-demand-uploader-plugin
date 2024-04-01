package org.jenkinsci.plugins.fodupload.models.response.Dast;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


import java.util.Date;
@SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
public class WebServiceManifestFile {
    public String getWebServiceFileType() {
        return webServiceFileType;
    }

    public String getFilename() {
        return filename;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public String getFileId() {
        return fileId;
    }

    public String webServiceFileType;
    public String filename;
    public Date dateCreated;
    public String fileId;
}
