package org.jenkinsci.plugins.fod.fodupload;


import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
 * The class is marked as public so that it can be accessed from views.
 *
 * <p>
 * See {@code src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly}
 * for the actual HTML fragment for the configuration screen.
 */
@Extension // This indicates to Jenkins that this is an implementation of an extension point.
public final class DescriptorImpl extends BuildStepDescriptor<Builder> {
    /**
     * To persist global configuration information,
     * simply store it in a field and call save().
     *
     * <p>
     * If you don't want fields to be persisted, use {@code transient}.
     */
    private boolean useFrench;

    /**
     * In order to load the persisted global configuration, you have to
     * call load() in the constructor.
     */
    public DescriptorImpl() {
        load();
    }

    /**
     * Performs on-the-fly validation of the form field 'name'.
     *
     * @param value
     *      This parameter receives the value that the user has typed.
     * @return
     *      Indicates the outcome of the validation. This is sent to the browser.
     *      <p>
     *      Note that returning {@link FormValidation#error(String)} does not
     *      prevent the form from being saved. It just means that a message
     *      will be displayed to the user.
     */
    public FormValidation doCheckName(@QueryParameter String value)
            throws IOException, ServletException {
        if (value.length() == 0)
            return FormValidation.error("Please set a name");
        if (value.length() < 4)
            return FormValidation.warning("Isn't the name too short?");
        return FormValidation.ok();
    }

    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        // Indicates that this builder can be used with all kinds of project types
        return true;
    }

    /**
     * This human readable name is used in the configuration screen.
     */
    public String getDisplayName() {
        return "Say hello world";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        // To persist global configuration information,
        // set that to properties and call save().
        useFrench = formData.getBoolean("useFrench");
        // ^Can also use req.bindJSON(this, formData);
        //  (easier when there are many fields; need set* methods for this, like setUseFrench)
        save();
        return super.configure(req,formData);
    }

    /**
     * This method returns true if the global configuration says we should speak French.
     *
     * The method name is bit awkward because global.jelly calls this method to determine
     * the initial state of the checkbox by the naming convention.
     */
    public boolean getUseFrench() {
        return useFrench;
    }
}