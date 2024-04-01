package org.jenkinsci.plugins.fodupload.FodApi;

import hudson.FilePath;
import hudson.Launcher;
import hudson.ProxyConfiguration;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import jenkins.security.MasterToSlaveCallable;
import okhttp3.*;
import org.jenkinsci.plugins.fodupload.models.response.PatchDastFileUploadResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface DastScanPayloadUpload {
    PatchDastFileUploadResponse performUpload() throws IOException;
}

