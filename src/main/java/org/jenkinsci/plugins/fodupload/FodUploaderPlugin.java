package org.jenkinsci.plugins.fodupload;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

public class FodUploaderPlugin extends Recorder implements SimpleBuildStep {
    private static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();

    private JobModel model;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    // Entry point when building
    @DataBoundConstructor
    public FodUploaderPlugin(String bsiUrl,
                             boolean runOpenSourceAnalysis,
                             boolean isExpressScan,
                             boolean isExpressAudit,
                             boolean includeAllFiles,
                             boolean excludeThirdParty,
                             boolean isRemediationScan,
                             boolean isBundledAssessment,
                             boolean purchaseEntitlements,
                             int entitlementPreference) throws URISyntaxException {

        model = new JobModel(bsiUrl,
                runOpenSourceAnalysis,
                isExpressAudit,
                isExpressScan,
                includeAllFiles,
                excludeThirdParty,
                isRemediationScan,
                isBundledAssessment,
                purchaseEntitlements,
                entitlementPreference);
    }

    // logic run during a build
    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace,
                        @Nonnull Launcher launcher, @Nonnull TaskListener listener) {
        try {
            final PrintStream logger = listener.getLogger();
            taskListener.set(listener);

            Result currentResult = build.getResult();
            if (Result.FAILURE.equals(currentResult)
                    || Result.ABORTED.equals(currentResult)
                    || Result.UNSTABLE.equals(currentResult)) {

                logger.println("Error: Build Failed or Unstable.  Halting with Fortify on Demand upload.");
                return;
            }

            // Load api settings
            FodApi api = getDescriptor().createFodApi();

            if (api == null) {
                logger.println("Error: Failed to Authenticate with Fortify API.");
                build.setResult(Result.UNSTABLE);
                return;
            } else {
                api.authenticate();

                if (model == null) {
                    logger.println("Unexpected Error");
                    build.setResult(Result.FAILURE);
                    return;
                }

                if (model.validate(logger)) { // Add all validation here
                    logger.println("Starting FoD Upload.");

                    // zips the file in a temporary location
                    File payload = Utils.createZipFile(model.getBsiUrl().getTechnologyStack(), workspace, logger);
                    if (payload.length() == 0) {
                        if (payload != null) {
                            boolean deleteSuccess = payload.delete();
                            if (!deleteSuccess) {
                                logger.println("Unable to delete empty payload.");
                            }
                        }
                        logger.println("Source is empty for given Technology Stack and Language Level.");
                        build.setResult(Result.FAILURE);
                        return;
                    }

                    model.setPayload(payload);
                    boolean success = api.getStaticScanController().startStaticScan(model);
                    boolean deleted = payload.delete();

                    if (success && deleted) {
                        logger.println("Scan Uploaded Successfully.");
                    }

                    // Success could be true then set to false from polling.
                    api.retireToken();
                    build.setResult(success && deleted ? Result.SUCCESS : Result.UNSTABLE);
                } else {
                    build.setResult(Result.UNSTABLE);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            build.setResult(Result.UNSTABLE);
        }
    }

    /**
     * Gets the out for the build console output
     *
     * @return Task Listener object
     */
    public static PrintStream getLogger() {
        return taskListener.get().getLogger();
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static final class DescriptorImpl extends FodDescriptor {
        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        // Entry point when accessing global configuration
        public DescriptorImpl() {
            super();
            load();
        }
    }

    // NOTE: The following Getters are used to return saved values in the config.jelly. Intellij
    // marks them unused, but they actually are used.
    // These getters are also named in the following format: Get<JellyField>.
    @SuppressWarnings("unused")
    public String getBsiUrl() {
        return model.getBsiUrl().ORIGINAL_VALUE;
    }

    @SuppressWarnings("unused")
    public boolean getRunOpenSourceAnalysis() {
        return model.isRunOpenSourceAnalysis();
    }

    @SuppressWarnings("unused")
    public boolean getIsExpressScan() {
        return model.isExpressScan();
    }

    @SuppressWarnings("unused")
    public boolean getIsExpressAudit() {
        return model.isExpressAudit();
    }

    @SuppressWarnings("unused")
    public boolean getIncludeAllFiles() {
        return model.isIncludeAllFiles();
    }

    @SuppressWarnings("unused")
    public boolean getExcludeThirdParty() {
        return model.isExcludeThirdParty();
    }

    @SuppressWarnings("unused")
    public boolean getIsRemediationScan() {
        return model.isRemediationScan();
    }

    @SuppressWarnings("unused")
    public int getEntitlementPreference() {
        return model.getEntitlementPreference();
    }

    @SuppressWarnings("unused")
    public boolean getIsBundledAssessment() {
        return model.isBundledAssessment();
    }

    @SuppressWarnings("unused")
    public boolean getPurchaseEntitlements() {
        return model.isPurchaseEntitlements();
    }
}


