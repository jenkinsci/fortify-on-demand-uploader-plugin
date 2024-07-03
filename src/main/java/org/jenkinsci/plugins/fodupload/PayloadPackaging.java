package org.jenkinsci.plugins.fodupload;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.Launcher;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.SastJobModel;
import org.jenkinsci.remoting.RoleChecker;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface PayloadPackaging {
    FilePath packagePayload() throws IOException;

    boolean deletePayload() throws IOException, InterruptedException;

    public static PayloadPackaging getInstance(SastJobModel model, String technologyStack, Boolean openSourceAnalysis, String globalSCPath, FilePath workspace, Launcher launcher, PrintStream logger) {
        if (workspace.isRemote()) return new PayloadPackagingRemote(model, technologyStack, openSourceAnalysis, globalSCPath, workspace, launcher, logger);
        else return new PayloadPackagingLocal(model, technologyStack, openSourceAnalysis, globalSCPath, workspace, logger);
    }
}

final class PayloadPackagingImpl {
    static FilePath performPackaging(SastJobModel model, String technologyStack, Boolean openSourceAnalysis, String globalSCPath, FilePath workspace, PrintStream logger) throws IOException {
        logger.println("Starting ScanCentral Packaging for source @ " + model.getSrcLocation());
        FilePath srcLocation = new FilePath(workspace, model.getSrcLocation());
        File payload;

        if (model.getSelectedScanCentralBuildType().equalsIgnoreCase(FodEnums.SelectedScanCentralBuildType.None.toString())) {

            if (ValidationUtils.isScanCentralRecommended(technologyStack)) {
                logger.println("\nFortify recommends using ScanCentral Client to package code for comprehensive scan results.\n");
            }

            // zips the file in a temporary location
            payload = Utils.createZipFile(technologyStack, srcLocation, logger);
            if (payload.length() == 0) {
                boolean deleteSuccess = payload.delete();
                if (!deleteSuccess) {
                    logger.println("Unable to delete empty payload.");
                }

                throw new IOException("Source is empty for given Technology Stack and Language Level.");
            }
        } else {
            File scanCentralPath;
            String scEnv = null;
            String scPath = null;

            try {
                scEnv = System.getenv("FOD_SCANCENTRAL");
                scPath = Utils.isNullOrEmpty(scEnv) ? globalSCPath : scEnv;

                if (Utils.isNullOrEmpty(scPath))
                    throw new IOException(String.format("ScanCentral location not set%nFOD_SCANCENTRAL: %s%nGlobal Config: %s", scEnv, globalSCPath));

                scanCentralPath = new File(scPath);
            } catch (Exception e) {
                throw new IOException(String.format("Failed to retrieve ScanCentral location%nFOD_SCANCENTRAL: %s%nGlobal Config: %s", scEnv, globalSCPath), e);
            }

            logger.println("Scan Central Path : " + scanCentralPath);
            Path scPackPath = packageScanCentral(srcLocation, scanCentralPath, workspace, model, openSourceAnalysis, logger);
            logger.println("Packaged File Output Path : " + scPackPath);

            if (scPackPath != null) {
                payload = new File(scPackPath.toString());

                if (!payload.exists()) throw new IOException("Failed to retrieve ScanCentral package");
            } else {
                throw new IOException("Scan Central package output not found.");
            }
        }

        return new FilePath(payload);
    }

    static boolean deletePayload(FilePath payload) throws IOException, InterruptedException {
        return payload.delete();
    }

    private static Path packageScanCentral(FilePath srcLocation, File scanCentralLocation, FilePath outputLocation, SastJobModel job, Boolean openSourceAnalysis, PrintStream logger) throws IOException {
        BufferedReader stdInputVersion = null, stdInput = null;
        String scexec = SystemUtils.IS_OS_WINDOWS ? "scancentral.bat" : "scancentral";

        try {
            //version check
            logger.println("Checking ScanCentralVersion");
            String scanCentralbatLocation = scanCentralLocation.toPath().resolve(scexec).toString();

            List<String> scanCentralVersionCommandList = new ArrayList<>();

            scanCentralVersionCommandList.add(scanCentralbatLocation);
            scanCentralVersionCommandList.add("--version");

            Process pVersion = runProcessBuilder(scanCentralVersionCommandList, scanCentralLocation);

            stdInputVersion = new BufferedReader(new InputStreamReader(pVersion.getInputStream(), StandardCharsets.UTF_8));

            String versionLine = null;
            String scanCentralVersion = null;
            StringBuilder scoutput = new StringBuilder();

            while ((versionLine = stdInputVersion.readLine()) != null) {
                scoutput.append("\n");
                scoutput.append(versionLine);
                logger.println(versionLine);
                if (versionLine.contains("version")) {
                    Pattern versionPattern = Pattern.compile("(version: *?)(.*)");
                    Matcher m = versionPattern.matcher(versionLine);

                    if (m.find()) {
                        scanCentralVersion = m.group(2).trim();

                        ComparableVersion minScanCentralVersion = new ComparableVersion("21.1.2.0002");
                        ComparableVersion userScanCentralVersion = new ComparableVersion(scanCentralVersion);

                        if (userScanCentralVersion.compareTo(minScanCentralVersion) < 0) {
                            throw new IOException(String.format("ScanCentral client version used is outdated. Update to the latest version provided on Tools page%nScanCentral Output: ", scoutput.toString()));
                        }
                        break;
                    }
                }
            }

            if (versionLine != null && versionLine.contains("version")) {
                Path outputZipFolderPath = Paths.get(String.valueOf(outputLocation)).resolve("output.zip");
                FodEnums.SelectedScanCentralBuildType buildType = FodEnums.SelectedScanCentralBuildType.valueOf(job.getSelectedScanCentralBuildType());

                if (buildType == FodEnums.SelectedScanCentralBuildType.Gradle) {
                    logger.println("Giving permission to gradlew");
                    int permissionsExitCode = givePermissionsToGradle(srcLocation, logger);
                    logger.println("Finished Giving Permissions : " + permissionsExitCode);
                    if (permissionsExitCode != 0) {
                        throw new IOException("Errors giving permissions to gradle : " + permissionsExitCode);
                    }
                }
                List<String> scanCentralPackageCommandList = new ArrayList<>();

                scanCentralPackageCommandList.add(scanCentralbatLocation);
                scanCentralPackageCommandList.add("package");

                if (Utils.traceLogging()) scanCentralPackageCommandList.add("-debug");

                String extraParams = System.getenv("FOD_SC_EP");

                if (!Utils.isNullOrEmpty(extraParams)) {
                    logger.println("Including additional ScanCentral arguments set in environment variable FOD_SC_EP '" + extraParams + "'");
                    scanCentralPackageCommandList.add(extraParams);
                }

                if (openSourceAnalysis) {
                    ComparableVersion minScanCentralOpenSourceSupportVersion = new ComparableVersion("22.1.2");
                    ComparableVersion oldVersionScanCentralOpenSourceSupportVersionone = new ComparableVersion("21.1.5");
                    ComparableVersion userScanCentralOpenSourceSupportVersion = new ComparableVersion(scanCentralVersion.substring(0, 6));
                    if (userScanCentralOpenSourceSupportVersion.compareTo(minScanCentralOpenSourceSupportVersion) < 0 && userScanCentralOpenSourceSupportVersion.compareTo(oldVersionScanCentralOpenSourceSupportVersionone) != 0) {
                        logger.println("Warning message : If you are submitting Debricked OSS scan. Scan might fail due to to missing required dependency files");
                    } else {
                        scanCentralPackageCommandList.add("--oss");
                    }
                }

                scanCentralPackageCommandList.add("-bt");

                switch (buildType) {
                    case Gradle:
                        scanCentralPackageCommandList.add("gradle");
                        if (job.getScanCentralSkipBuild()) scanCentralPackageCommandList.add("--skipBuild");
                        if (!Utils.isNullOrEmpty(job.getScanCentralBuildCommand())) {
                            scanCentralPackageCommandList.add("--build-command");
                            scanCentralPackageCommandList.add(job.getScanCentralBuildCommand());
                        }
                        if (!Utils.isNullOrEmpty(job.getScanCentralBuildFile())) {
                            scanCentralPackageCommandList.add("--build-file");
                            scanCentralPackageCommandList.add("\"" + job.getScanCentralBuildFile() + "\"");
                        }
                        break;
                    case Maven:
                        scanCentralPackageCommandList.add("mvn");
                        if (job.getScanCentralSkipBuild()) scanCentralPackageCommandList.add("--skipBuild");
                        if (!Utils.isNullOrEmpty(job.getScanCentralBuildCommand())) {
                            scanCentralPackageCommandList.add("--build-command");
                            scanCentralPackageCommandList.add(job.getScanCentralBuildCommand());
                        }
                        if (!Utils.isNullOrEmpty(job.getScanCentralBuildFile())) {
                            scanCentralPackageCommandList.add("--build-file");
                            scanCentralPackageCommandList.add("\"" + job.getScanCentralBuildFile() + "\"");
                        }
                        break;
                    case MSBuild:
                        scanCentralPackageCommandList.add("msbuild");
                        if (!Utils.isNullOrEmpty(job.getScanCentralBuildCommand())) {
                            scanCentralPackageCommandList.add("--build-command");
                            scanCentralPackageCommandList.add(transformMsBuildCommand(job.getScanCentralBuildCommand()));
                        }
                        if (!Utils.isNullOrEmpty(job.getScanCentralBuildFile())) {
                            scanCentralPackageCommandList.add("--build-file");
                            scanCentralPackageCommandList.add("\"" + job.getScanCentralBuildFile() + "\"");
                        } else {
                            throw new IOException("Build File is a required field for msbuild build type. Please fill in the .sln file name in the current source folder ");
                        }
                        break;
                    case DotNet:
                        scanCentralPackageCommandList.add("dotnet");
                        if (!Utils.isNullOrEmpty(job.getScanCentralBuildCommand())) {
                            scanCentralPackageCommandList.add("--build-command");
                            scanCentralPackageCommandList.add(transformMsBuildCommand(job.getScanCentralBuildCommand()));
                        }
                        if (!Utils.isNullOrEmpty(job.getScanCentralBuildFile())) {
                            scanCentralPackageCommandList.add("--build-file");
                            scanCentralPackageCommandList.add("\"" + job.getScanCentralBuildFile() + "\"");
                        } else {
                            throw new IOException("Build File is a required field for msbuild build type. Please fill in the .sln file name in the current source folder ");
                        }
                        break;
                    case Python:
                        scanCentralPackageCommandList.add("none");
                        if (!Utils.isNullOrEmpty(job.getScanCentralVirtualEnv())) {
                            scanCentralPackageCommandList.add("--python-virtual-env");
                            scanCentralPackageCommandList.add(job.getScanCentralVirtualEnv());
                        }
                        if (!Utils.isNullOrEmpty(job.getScanCentralRequirementFile())) {
                            scanCentralPackageCommandList.add("--python-requirements");
                            scanCentralPackageCommandList.add(job.getScanCentralRequirementFile());
                        }
                        if (!Utils.isNullOrEmpty(job.getScanCentralBuildToolVersion())) {
                            scanCentralPackageCommandList.add("--python-version");
                            scanCentralPackageCommandList.add(job.getScanCentralBuildToolVersion());
                        }
                        break;
                    case PHP:
                        scanCentralPackageCommandList.add("none");
                        if (!Utils.isNullOrEmpty(job.getScanCentralBuildToolVersion())) {
                            scanCentralPackageCommandList.add("--php-version");
                            scanCentralPackageCommandList.add(job.getScanCentralBuildToolVersion());
                        }
                        break;
                    case Go:
                        scanCentralPackageCommandList.add("none");
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid ScanCentral build type: " + buildType);
                }

                if(!Utils.isNullOrEmpty(job.getScanCentralExcludeFiles())){
                    ComparableVersion minScanCentralExcludeFilesSupportVersion = new ComparableVersion("22.2.0");
                    ComparableVersion userScanCentralVersion = new ComparableVersion(scanCentralVersion.substring(0, 6));
                    if (userScanCentralVersion.compareTo(minScanCentralExcludeFilesSupportVersion) < 0 ) {
                        logger.println("Warning message : Scan Central Client version used does not support file exclusion. Please download and use Scan Central 22.2 or above to get the functionality.");
                    } else {
                        scanCentralPackageCommandList.add("--exclude");
                        scanCentralPackageCommandList.add("\"" + job.getScanCentralExcludeFiles()+ "\"");
                    }
                }

                scanCentralPackageCommandList.add("-o");
                scanCentralPackageCommandList.add("\"" + outputZipFolderPath.toString() + "\"");

                logger.println("Packaging ScanCentral\n" + String.join(" ", scanCentralPackageCommandList));

                // Is getRemote() correct?
                Process scanCentralProcess = runProcessBuilder(scanCentralPackageCommandList, new File(srcLocation.getRemote()));

                stdInput = new BufferedReader(new InputStreamReader(scanCentralProcess.getInputStream(), StandardCharsets.UTF_8));
                String s = null;

                while ((s = stdInput.readLine()) != null) {
                    logger.println(s);
                }

                int exitCode = scanCentralProcess.waitFor();

                if (exitCode != 0) throw new IOException("Errors executing Scan Central. Exiting with errorcode : " + exitCode);

                return outputZipFolderPath;
            } else throw new IOException(String.format("ScanCentral not found or invalid version%nScanCentral Output: ", scoutput.toString()));
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            throw new IOException(String.format("Failed executing scan central : %s", e.getMessage()), e);
        } finally {
            try {
                if (stdInputVersion != null) {
                    stdInputVersion.close();
                }
                if (stdInput != null) {
                    stdInput.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int givePermissionsToGradle(FilePath srcLocation, PrintStream logger) throws IOException {
        if (!SystemUtils.IS_OS_WINDOWS) {
            BufferedReader stdInput = null;
            List<String> linuxPermissionsList = new ArrayList<>();

            linuxPermissionsList.add("chmod");
            linuxPermissionsList.add("u+x");
            linuxPermissionsList.add("gradlew");

            try {
                // Is getRemote() correct?
                Process gradlePermissionsProcess = runProcessBuilder(linuxPermissionsList, new File(srcLocation.getRemote()));

                stdInput = new BufferedReader(new InputStreamReader(gradlePermissionsProcess.getInputStream(), StandardCharsets.UTF_8));
                String s = null;

                while ((s = stdInput.readLine()) != null) {
                    logger.println(s);
                }

                return gradlePermissionsProcess.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                throw new IOException("Failed to assign executable permissions to gradle file");
            } finally {
                if (stdInput != null) stdInput.close();
            }
        }
        return 0;
    }

    private static String transformMsBuildCommand(String cmd) {
        if (!Utils.isNullOrEmpty(cmd)) {
            String[] arrOfCmds = cmd.split(" ");
            StringBuilder transformedCommands = new StringBuilder();
            for (String command : arrOfCmds) {
                if (command.charAt(0) == '-') {
                    command = '/' + command.substring(1);
                }
                transformedCommands.append(command).append(" ");
            }
            return transformedCommands.substring(0, transformedCommands.length() - 1);
        }
        return null;
    }

    private static Process runProcessBuilder(List<String> cmdList, File directoryLocation) throws IOException {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmdList);

            pb.directory(directoryLocation);
            Process p = pb.start();
            System.out.println(pb.redirectErrorStream());
            pb.redirectErrorStream(true);
            return p;
        } catch (IOException e) {
            throw e;
        }
    }
}

final class PayloadPackagingLocal implements PayloadPackaging {
    private PrintStream _logger;
    private SastJobModel _model;
    private String _technologyStack;
    private Boolean _openSourceAnalysis;
    private FilePath _payload;
    private FilePath _workspace;
    private String _globalSCPath;

    PayloadPackagingLocal(SastJobModel model, String technologyStack, Boolean openSourceAnalysis, String globalSCPath, FilePath workspace, PrintStream logger) {
        _logger = logger;
        _model = model;
        _technologyStack = technologyStack;
        _openSourceAnalysis = openSourceAnalysis;
        _workspace = workspace;
        _globalSCPath = globalSCPath;
    }

    @Override
    public FilePath packagePayload() throws IOException {
        _payload = PayloadPackagingImpl.performPackaging(_model, _technologyStack, _openSourceAnalysis, _globalSCPath, _workspace, _logger);
        return _payload;
    }

    @Override
    public boolean deletePayload() throws IOException, InterruptedException {
        return PayloadPackagingImpl.deletePayload(_payload);
    }
}

final class PayloadPackagingRemote extends MasterToSlaveCallable<FilePath, IOException> implements PayloadPackaging {
    private static final long serialVersionUID = 1L;
    private transient VirtualChannel _channel;
    private RemoteOutputStream _logger;
    private SastJobModel _model;
    private String _technologyStack;
    private Boolean _openSourceAnalysis;
    private FilePath _payload;
    private FilePath _workspace;
    private String _globalSCPath;

    PayloadPackagingRemote(SastJobModel model, String technologyStack, Boolean openSourceAnalysis, String globalSCPath, FilePath workspace, Launcher launcher, PrintStream logger) {
        _model = model;
        _technologyStack = technologyStack;
        _openSourceAnalysis = openSourceAnalysis;
        _workspace = workspace;
        _globalSCPath = globalSCPath;

        _channel = launcher.getChannel();
        if (_channel == null) {
            throw new IllegalStateException("Launcher doesn't support remoting but it is required");
        }

        _logger = new RemoteOutputStream(logger);
    }

    @Override
    public FilePath call() throws IOException {
        PrintStream logger = new PrintStream(_logger, true, StandardCharsets.UTF_8.name());

        _payload = PayloadPackagingImpl.performPackaging(_model, _technologyStack, _openSourceAnalysis, _globalSCPath, _workspace, logger);
        return _payload;
    }

    @Override
    public FilePath packagePayload() throws IOException {
        try {
            _payload = _channel.call(this);
            return _payload;
        } catch (InterruptedException e) {
            throw new IOException("PayloadPackagingRemote failed", e);
        }
    }

    @Override
    public boolean deletePayload() throws IOException, InterruptedException {
        try {
            return _payload.act(new RemotePayloadCleanup(_logger));
        } catch (InterruptedException e) {
            throw new IOException("RemotePayloadCleanup failed", e);
        }
    }
}

@SuppressFBWarnings("SE_NO_SERIALVERSIONID")
class RemotePayloadCleanup implements FilePath.FileCallable<Boolean> {
    private RemoteOutputStream _logger;

    RemotePayloadCleanup(RemoteOutputStream logger) {
        _logger = logger;
    }

    @Override
    public Boolean invoke(File file, VirtualChannel virtualChannel) throws IOException, InterruptedException {
        PrintStream logger = new PrintStream(_logger, true, StandardCharsets.UTF_8.name());

        logger.println("Deleting remote file " + file.getAbsolutePath());
        Boolean res = file.delete();

        logger.close();
        return res;
    }

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {

    }
}
