package org.jenkinsci.plugins.fodupload.models.response.Dast;

import java.util.Date;

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
