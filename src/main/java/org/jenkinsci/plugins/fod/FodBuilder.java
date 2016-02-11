package org.jenkinsci.plugins.fod;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import jenkins.util.VirtualFile;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.fod.schema.AssessmentType;
import org.jenkinsci.plugins.fod.schema.PassFailReason;
import org.jenkinsci.plugins.fod.schema.Release;
import org.jenkinsci.plugins.fod.schema.ScanStatus;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */
public class FodBuilder extends Recorder implements SimpleBuildStep
{

	protected static final String CLASS_NAME = FodBuilder.class.getName();

	private static final String DEFAULT_FOD_BASE_URL = "https://www.hpfod.com";

	private static Logger log = LogManager.getLogManager().getLogger(FodBuilder.class.getCanonicalName());
	
    // Thread local variable containing each thread's taskListener. for use during a build only.
    private static final ThreadLocal<TaskListener> taskListener =
        new ThreadLocal<TaskListener>();

    /**
     * Default time between queries to FoD API for scan status, in minutes.
     */
	private static final Long DEFAULT_POLLING_INTERVAL = 5l;

    private String filePatterns;
	private String applicationName;
	private String releaseName;
	private Long assessmentTypeId;
	private String technologyStack;
	private String languageLevel;
	private Boolean runSonatypeScan;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public FodBuilder(String filePatterns
    		,String applicationName
    		,String releaseName
    		,Long assessmentTypeId
    		,String technologyStack
    		,String languageLevel
    		,Boolean runSonatypeScan)
    {
        this.filePatterns = filePatterns;
        this.applicationName = applicationName;
        this.releaseName = releaseName;
        this.assessmentTypeId = assessmentTypeId;
        this.technologyStack = technologyStack;
        this.languageLevel = languageLevel;
        this.runSonatypeScan = runSonatypeScan;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getFilePatterns()
    {
        return filePatterns;
    }

	public String getApplicationName()
	{
		return applicationName;
	}

	public String getReleaseName()
	{
		return releaseName;
	}

	public Long getAssessmentTypeId()
	{
		return assessmentTypeId;
	}

	public String getTechnologyStack()
	{
		return technologyStack;
	}

	public String getLanguageLevel()
	{
		return languageLevel;
	}

	public Boolean getRunSonatypeScan()
	{
		return runSonatypeScan;
	}
	
	protected static TaskListener getTaskListener()
	{
		return taskListener.get();
	}
	
	protected static void setTaskListener(TaskListener listener)
	{
		taskListener.set(listener);
	}
	
	protected static void clearTaskListener()
	{
		taskListener.remove();
	}
	
	protected static PrintStream getLogger()
	{
		TaskListener listener = getTaskListener();
		PrintStream returnVal = null;
		if( null != listener ) {
			returnVal = listener.getLogger();
		} else {
			returnVal = System.out;
		}
		return returnVal;
	}

	public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener)
	{
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a build.
		final String METHOD_NAME = CLASS_NAME+".perform";
		setTaskListener(listener);

        // This also shows how you can consult the global configuration of the builder
		DescriptorImpl descriptor = this.getDescriptor();
		String fodUrl = descriptor.getFodUrl();
		String clientId = descriptor.getClientId();
		String clientSecret = descriptor.getClientSecret();
		String pollingInterval = descriptor.getPollingInterval();
		String tenantCode = descriptor.getTenantCode();
		String username = descriptor.getUsername();
		String password = descriptor.getPassword();
    	String proxyHost = descriptor.getProxyHost();
    	String proxyPort = descriptor.getProxyPort();
    	String proxyUser = descriptor.getProxyUser();
    	String proxyPassword = descriptor.getProxyPassword();
    	String ntWorkstation = descriptor.getNtWorkstation();
    	String ntDomain = descriptor.getNtDomain();
		
		final PrintStream logger = listener.getLogger();
    	
    	logger.println(METHOD_NAME+": foDUrl = "+fodUrl);
		logger.println(METHOD_NAME+": clientId = "+clientId);
		logger.println(METHOD_NAME+": clientSecret = "+clientSecret);
		logger.println(METHOD_NAME+": pollingInterval = "+pollingInterval);
        logger.println(METHOD_NAME+": proxyHost = "+proxyHost);
        logger.println(METHOD_NAME+": proxyPort = "+proxyPort);
        logger.println(METHOD_NAME+": proxyUser = "+proxyUser);
        logger.println(METHOD_NAME+": proxyPassword = "+proxyPassword);
        logger.println(METHOD_NAME+": ntWorkstation = "+ntWorkstation);
        logger.println(METHOD_NAME+": ntDomain = "+ntDomain);
        
        logger.println(METHOD_NAME+": filePatterns = \""+filePatterns+"\"");
        logger.println(METHOD_NAME+": applicationName = "+applicationName);
        logger.println(METHOD_NAME+": releaseName = "+releaseName);
        logger.println(METHOD_NAME+": assessmentTypeId = "+assessmentTypeId);
        logger.println(METHOD_NAME+": technologyStack = "+technologyStack);
        logger.println(METHOD_NAME+": languageLevel = "+languageLevel);
               
        logger.println(METHOD_NAME+": Attempting to authorize ...");
        boolean authSuccess = false;
        try
        {
        	
	        String baseURL = DEFAULT_FOD_BASE_URL;
	        FoDAPI api = new FoDAPI(baseURL);
	        authSuccess = api.authorize(clientId,clientSecret);
	        
	        if( authSuccess )
	        {
	            logger.println(METHOD_NAME+": Auth success!");
	        }
	        else
	        {
	            logger.println(METHOD_NAME+": Auth failed!");
	        }
	        
	        if(authSuccess)
	        {
	        	//FilePath zipFile = workspace.child(zipLocation);
	        	String prefix = "fodupload";
				String suffix = ".zip";
				logger.println(METHOD_NAME+": workspace = "+workspace.toString());
				File uploadFile = null;
				
				try {
//					logger.println(METHOD_NAME+": creating temp file ...");
//					FilePath tmpZipFile = workspace.createTempFile(prefix,suffix);
//					
//					logger.println(METHOD_NAME+": tmpZipFile.URI = "+tmpZipFile.toURI());
//					logger.println(METHOD_NAME+": zipping up workspace contents ...");
//					workspace.zip(tmpZipFile);
					
					String tmpDir = System.getProperty("java.io.tmpdir");
					File dir = new File(tmpDir);
					File tmpZipFile = File.createTempFile(prefix, suffix, dir);
					
					FileOutputStream fos = new FileOutputStream(tmpZipFile);
					
					String localFilePatterns = null;
					if( null == filePatterns || filePatterns.isEmpty() )
					{
						localFilePatterns = ".*";
					}
					else
					{
						localFilePatterns = filePatterns;
					}
					final Pattern p = Pattern.compile(localFilePatterns, Pattern.CASE_INSENSITIVE);
					
					FileFilter filter = new FileFilter()
					{
						private final String CLASS_NAME = FodBuilder.CLASS_NAME+".anon(FileFilter)";
						
						private final Pattern filePattern = p;
						
						@Override
						public boolean accept(File pathname)
						{
							final String METHOD_NAME = CLASS_NAME+".accept";
							logger.println(METHOD_NAME+": pathname.path = "+pathname.getPath());
							logger.println(METHOD_NAME+": pathname.name = "+pathname.getName());
							
							boolean matches = false;
							
//							matches = !(pathname.getName().endsWith(".zip")
//									&& pathname.getName().contains("fodupload"));
							
							Matcher m = filePattern.matcher(pathname.getName());
							matches = m.matches();
							logger.println(METHOD_NAME+": pathname accepted : "+matches);
							
							return matches;
						}
					};
					workspace.zip(fos, filter);
					
					fos.flush();
					fos.close();
					
					logger.println(METHOD_NAME+": zipped up workspace contents.");
					
					File localTmpZipFile = tmpZipFile;
					
//					logger.println(METHOD_NAME+": tmpZipFile.isRemote = "+tmpZipFile.isRemote());
//					File localTmpZipFile = null;
//					if( tmpZipFile.isRemote() )
//					{
//						localTmpZipFile = copyToLocalTmp(tmpZipFile);
//					}
//					else
//					{
//						localTmpZipFile = new File(tmpZipFile.toURI());
//					}
					logger.println(METHOD_NAME+": localTmpZipFile = "+localTmpZipFile);
					logger.println(METHOD_NAME+": localTmpZipFile.exists = "+localTmpZipFile.exists());
					
					if( null != localTmpZipFile && localTmpZipFile.exists() )
					{
						uploadFile = localTmpZipFile;
					}
					// build.getRootDir()
					
					//boolean exists = zipFile.exists();
					//logger.println(METHOD_NAME+": zipFile.exists = "+exists);
					
					//File f = new File(zipFile.toURI());
					
					//workspace.zip(os, filter);
					//zipFile.zip(out, scanner)

				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	
				//TODO set global parameters on FoDAPI object instead
	        	UploadRequest req = new UploadRequest();
	        	req.setClientId(clientId);
	        	req.setClientSecret(clientSecret);
	        	req.setPollingInterval(pollingInterval);
	        	req.setTenantCode(tenantCode);
	        	req.setUsername(username);
	        	req.setPassword(password);
	        	req.setProxyHost(proxyHost);
	        	req.setProxyPort(proxyPort);
	        	req.setProxyUser(proxyUser);
	        	req.setProxyPassword(proxyPassword);
	        	req.setNtWorkstation(ntWorkstation);
	        	req.setNtDomain(ntDomain);
	        	req.setUploadZip(uploadFile);
	        	req.setApplicationName(applicationName);
	        	req.setReleaseName(releaseName);
	        	req.setAssessmentTypeId(assessmentTypeId);
	        	req.setTechnologyStack(technologyStack);
	        	req.setLanguageLevel(languageLevel);
	        	req.setRunSonatypeScan(runSonatypeScan);
	        	
				UploadStatus status = api.uploadFile(req);
				logger.println(METHOD_NAME+": HTTP status code: "+status.getHttpStatusCode());
				logger.println(METHOD_NAME+": POST failed: "+status.isSendPostFailed());
				logger.println(METHOD_NAME+": Error message: "+status.getErrorMessage());
				logger.println(METHOD_NAME+": Bytes sent: "+status.getBytesSent());
				
				if( status.isUploadSucceeded() )
				{
					Long releaseId = api.getReleaseId(applicationName, releaseName);
					Release release = null;
					
					//FIXME make configurable based on timeout in hours and pollingInterval in minutes
					Long maxAttempts = 3l;
					Long attempts = 0l;
					
					Long pollingWait = null;
					try
					{
						if( null != pollingInterval && !pollingInterval.isEmpty() )
						{
							pollingWait = Long.parseLong(pollingInterval);
						}
					}
					catch( NumberFormatException nfe )
					{
						nfe.printStackTrace();
					}
					
					if( null == pollingWait || 0 < pollingWait )
					{
						pollingWait = DEFAULT_POLLING_INTERVAL;
					}
					
					boolean continueLoop = true;
					do
					{
						try
						{
							//FIXME make configurable
							Thread.sleep(pollingWait*60*1000l);
						}
						catch (InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						release = api.getRelease(releaseId);
						
						++attempts;
						
						logger.println(METHOD_NAME+": scan status ID: "+release.getStaticScanStatusId().intValue());
						logger.println(METHOD_NAME+": attempts: "+attempts);
						
						continueLoop = 
								(ScanStatus.IN_PROGRESS.getId().intValue() 
										== release.getStaticScanStatusId().intValue() )
								&& ( attempts < maxAttempts);
						
						logger.println(METHOD_NAME+": continueLoop: "+continueLoop);
					} while( continueLoop );
					
					logger.println(METHOD_NAME+": scan status ID: "+release.getStaticScanStatusId());
					logger.println(METHOD_NAME+": scan status: "+release.getStaticScanStatus());
					logger.println(METHOD_NAME+": isPassed: "+release.getIsPassed());
					String passFailReasonId = release.getPassFailReasonId();
					String passFailReasonStr = null;
					if( null != passFailReasonId && !passFailReasonId.isEmpty() )
					{
						try
						{
							Integer passFailReasonIdInt = Integer.parseInt(passFailReasonStr);
							
							if( null != passFailReasonIdInt )
							{
								passFailReasonStr = AssessmentType.getTypeName(passFailReasonIdInt);
							}
						} catch(NumberFormatException nfe) {}
					}
					
					logger.println(METHOD_NAME+": passFailReasonId: "+release.getPassFailReasonId()+(passFailReasonStr==null?"":" ("+passFailReasonStr+")"));
					
					if( ScanStatus.IN_PROGRESS.getId().equals(release.getStaticScanStatusId().intValue()) )
					{
						logger.println("Timed out while waiting for scan to complete. Marking build as unstable.");
						build.setResult(Result.UNSTABLE);
					}
					else if( ScanStatus.CANCELLED.getId().equals(release.getStaticScanStatusId().intValue()) )
					{
						logger.println("Scan was cancelled after uploading. Marking build as unstable.");
						build.setResult(Result.UNSTABLE);
					}
					else if( ScanStatus.COMPLETED.getId().equals(release.getStaticScanStatusId().intValue()) )
					{
						if( !release.getIsPassed() )
						{
							logger.println("Scan failed policy check! Marking build as unstable.");
							build.setResult(Result.UNSTABLE);
						}
						else
						{
							logger.println("Scan completed and passed policy check!");
						}
					}
					else if( ScanStatus.WAITING.getId().equals(release.getStaticScanStatusId().intValue()) )
					{
						logger.println("Scan status is 'WAITING'. Response to this status not yet implemented in plugin!");
						build.setResult(Result.UNSTABLE);
					}
					else
					{
						logger.println("Scan status ID of "+release.getStaticScanStatusId().intValue()+" unrecognized. Response to this status not yet implemented in plugin!");
						build.setResult(Result.UNSTABLE);
					}
				}
				else
				{
					logger.println(METHOD_NAME+": upload failure!");
					build.setResult(Result.UNSTABLE);
				}
		        
		        if( null != uploadFile && uploadFile.exists() )
		        {
		        	try 
		        	{
		        		boolean fileDeleted = uploadFile.delete();
		        		if( fileDeleted )
		        		{
			        		logger.println(METHOD_NAME+": upload file "+uploadFile.getName()+" deleted");
		        		}
		        		else 
		        		{
			        		logger.println(METHOD_NAME+": upload file deletion failed!");
		        		}
		        	}
		        	catch( RuntimeException rte )
		        	{
		        		logger.println(METHOD_NAME+": upload file deletion failed! see Jenkins server log for details");
		        		rte.printStackTrace(logger);
		        		rte.printStackTrace();
		        	}
		        }
		        else
		        {
	        		logger.println(METHOD_NAME+": upload file does not exist - not attempting to delete");
		        }
	        }
	        else
	        {
				logger.println(METHOD_NAME+": auth failure!");
	        }
        }
        catch (RuntimeException rte)
        {
        	rte.printStackTrace();
        	logger.println(rte.toString());
			build.setResult(Result.FAILURE);
        }
        
        catch (IOException ioe)
        {
        	ioe.printStackTrace();
        	logger.println(ioe.toString());
			build.setResult(Result.FAILURE);
		}

    	clearTaskListener();
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
	}

    /**
     * Descriptor for {@link FodBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/fod/FodBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		private static final String CLASS_NAME = DescriptorImpl.class.getName();
    	
    	private static Logger log = LogManager.getLogManager().getLogger(DescriptorImpl.class.getName());
    	
        private static final String TS_DOTNET_KEY = ".NET";
		private static final String TS_JAVA_KEY = "JAVA/J2EE";
		
		/**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String clientId;
        private String clientSecret;
        private String fodUrl;
		private String pollingInterval;
        private String tenantCode;
        private String username;
        private String password;
    	private String proxyHost;
    	private String proxyPort;
    	private String proxyUser;
    	private String proxyPassword;
    	private String ntWorkstation;
    	private String ntDomain;
    	
    	private transient FoDAPI api;

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }
        
        /**
         * Performs on-the-fly validation of the form field 'password'.
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
//        public FormValidation doCheckPassword(@QueryParameter String value)
//                throws IOException, ServletException {
//            if (value.length() == 0)
//                return FormValidation.error("Please set a password");
//            if (value.length() < 4)
//                return FormValidation.warning("Invalid password");
//            return FormValidation.ok();
//        }

		/**
         * Performs on-the-fly validation of the form field 'zipLocation'.
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
        public FormValidation doCheckZipLocation(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a zip location");
            if (value.length() < 4)
                return FormValidation.warning("Invalid zip location");
            return FormValidation.ok();
        }
        
		public ListBoxModel doFillApplicationNameItems()
		{
			final String METHOD_NAME = CLASS_NAME+".doFillApplicationNameItems()";
			PrintStream out = null;
			//out = FodBuilder.getLogger();
			out = System.out;
			
			ListBoxModel items = new ListBoxModel();
			
			out.println(METHOD_NAME+": getting FoD API object reference");
			FoDAPI api = getApi();
			if( null != api )
			{
				ensureLogin(api);
				
				if( api.isLoggedIn() )
				{
					try
					{
						out.println(METHOD_NAME+": getting application list");
						Map<String,String> appList = api.getApplicationList();
						
						if( null != appList && !appList.isEmpty() )
						{
							out.println(METHOD_NAME+": appList.size = "+appList.size());
							for( String appName : appList.keySet() )
							{
								out.println(METHOD_NAME+": adding application \""+appName+"\" to menu");
								items.add(new Option(appName,appName,false));
							}
						}
						else
						{
							out.println(METHOD_NAME+": application list empty or null");
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					out.println(METHOD_NAME+": secondary login check failed");
				}
			} else {
				out.println(METHOD_NAME+": getApi() returned null");
			}
			
			return items;
		}

		public ListBoxModel doFillReleaseNameItems(@QueryParameter("applicationName") String applicationName)
		{
			final String METHOD_NAME = CLASS_NAME+".doFillReleaseNameItems()";
			PrintStream out = null;
			//out = FodBuilder.getLogger();
			out = System.out;
			
			ListBoxModel items = new ListBoxModel();
			
			out.println(METHOD_NAME+": applicationName = "+applicationName);
			
			if( null != applicationName && !applicationName.isEmpty() )
			{
				out.println(METHOD_NAME+": getting FoD API object reference");
				FoDAPI api = getApi();
				if( null != api )
				{
					ensureLogin(api);
					
					if( api.isLoggedIn() )
					{
						try
						{
							out.println(METHOD_NAME+": getting release list for applicationName = "+applicationName);
							List<Release> relList = api.getReleaseList(applicationName);
							
							if( null != relList && !relList.isEmpty() )
							{
								out.println(METHOD_NAME+": relList.size = "+relList.size());
								for( Release release : relList )
								{
									String releaseName = release.getReleaseName();
									out.println(METHOD_NAME+": adding release \""+releaseName+"\" to menu");
									items.add(new Option(releaseName,releaseName,false));
								}
							}
							else
							{
								out.println(METHOD_NAME+": release list empty or null");
							}
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						out.println(METHOD_NAME+": secondary login check failed");
					}
				} else {
					out.println(METHOD_NAME+": getApi() returned null");
				}
			} else {
				out.println(METHOD_NAME+": applicationName is null. doing nothing.");
			}
			
			return items;
		}

		protected void ensureLogin(FoDAPI api)
		{
			final String METHOD_NAME = CLASS_NAME+".ensureLogin";
			PrintStream out = getLogger();
			
			if( !api.isLoggedIn() )
			{
				// timeout must be configured, or else a hung call here blocks everything
				synchronized(this)
				{
					if( !api.isLoggedIn() )
					{
						try
						{
							out.println(METHOD_NAME+": logging in to FoD API");
							api.authorize(this.getClientId(), this.getClientSecret());
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}

		public ListBoxModel doFillAssessmentTypeIdItems() {
			ListBoxModel items = new ListBoxModel();
			
			// 
			//TODO populate dynamically ... how to get config values?
//			for (...) {
//				items.add(new Option(name,id,selected));
//			}
//			Jenkins.getInstance().getGlobalNodeProperties()
//		       .get(EnvironmentVariablesNodeProperty.class).getEnvVars().get(name);
			
//			items.add(new Option("Static Express", "105",false));
//			items.add(new Option("Static Assessment", "170",false));
			
			items.add(new Option("Express Scan", "105", false));
			items.add(new Option("Standard Scan", "170", false));
			return items;
		}

		public ListBoxModel doFillTechnologyStackItems() {
			ListBoxModel items = new ListBoxModel();
			
			// only supporting Java, initially, but only one option 
			//  causes it to be selected automatically, which means 
			//  language levels will not be filled correctly
			items.add(new Option(TS_JAVA_KEY, TS_JAVA_KEY,false));
			items.add(new Option(TS_DOTNET_KEY, TS_DOTNET_KEY,false));
			
			return items;
		}

		public ListBoxModel doFillLanguageLevelItems(@QueryParameter("technologyStack") String technologyStack) {
			ListBoxModel items = new ListBoxModel();
			
			// log is null reference?!
			//log.info("doFillLanguageLevelItems: technologyStack = "+technologyStack);
			//System.out.println("doFillLanguageLevelItems: technologyStack = "+technologyStack);
			
			if( TS_JAVA_KEY.equalsIgnoreCase(technologyStack)
					|| "".equals(technologyStack)
					|| null == technologyStack)
			{
				items.add(new Option("1.2", "1.2",false));
				items.add(new Option("1.3", "1.3",false));
				items.add(new Option("1.4", "1.4",false));
				items.add(new Option("1.5", "1.5",false));
				items.add(new Option("1.6", "1.6",false));
				items.add(new Option("1.7", "1.7",false));
				items.add(new Option("1.8", "1.8",false));
			}
			else if( TS_DOTNET_KEY.equalsIgnoreCase(technologyStack) )
			{
				items.add(new Option("1.0", "1.0",false));
				items.add(new Option("1.1", "1.1",false));
				items.add(new Option("2.0", "2.0",false));
				items.add(new Option("3.0", "3.0",false));
				items.add(new Option("3.5", "3.5",false));
				items.add(new Option("4.0", "4.0",false));
				items.add(new Option("4.5", "4.5",false));
				items.add(new Option("4.5.1", "4.5.1",false));
				items.add(new Option("4.5.2", "4.5.2",false));
				items.add(new Option("4.6", "4.6",false));
				items.add(new Option("4.6.1", "4.6.1",false));
			}
			
			return items;
		}

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            //return aClass.isAssignableFrom(MavenModule.class) ||  aClass.isAssignableFrom(FreeStyleProject.class);
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Fortify on Demand Upload";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            //useFrench = formData.getBoolean("useFrench");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            clientId = formData.getString("clientId");
            clientSecret = formData.getString("clientSecret");
            fodUrl = formData.getString("fodUrl");
            pollingInterval = formData.getString("pollingInterval");
            proxyHost = formData.getString("proxyHost");
            proxyPort = formData.getString("proxyPort");
            proxyUser = formData.getString("proxyUser");
            proxyPassword = formData.getString("proxyPassword");
            ntWorkstation = formData.getString("ntWorkstation");
            ntDomain = formData.getString("ntDomain");
            return super.configure(req,formData);
        }

		public String getClientId() {
			return clientId;
		}

		public String getClientSecret() {
			return clientSecret;
		}

		public String getFodUrl() {
			return fodUrl;
		}
        
        public String getPollingInterval() {
			return pollingInterval;
		}

	    public String getTenantCode() {
	        return tenantCode;
	    }

	    public String getUsername() {
	        return username;
	    }

	    public String getPassword() {
	        return password;
	    }
	    
	    public String getProxyHost() {
			return proxyHost;
		}

		public String getProxyPort() {
			return proxyPort;
		}

		public String getProxyUser() {
			return proxyUser;
		}

		public String getProxyPassword() {
			return proxyPassword;
		}

		public String getNtWorkstation() {
			return ntWorkstation;
		}

		public String getNtDomain() {
			return ntDomain;
		}

//        /**
//         * This method returns true if the global configuration says we should use proxy.
//         *
//         * The method name is bit awkward because global.jelly calls this method to determine
//         * the initial state of the checkbox by the naming convention.
//         */
//        public boolean getUseProxy() {
//            return useProxy;
//        }
		
		/**
		 * Retrieves a reference to the FoDAPI class, creating this object if necessary.
		 * @return
		 */
		protected FoDAPI getApi() {
			if( null == api)
			{
				synchronized(this)
				{
					if( null == api )
					{
						String configFodUrl = getFodUrl();
						
						if( null != configFodUrl && !configFodUrl.isEmpty() )
						{
							api = new FoDAPI(configFodUrl);
						}
						else
						{
							api = new FoDAPI(DEFAULT_FOD_BASE_URL);
						}
					}
				}
			}
			
			return this.api;
		}
    }
    
    /**
     * Copies file from remote worker to local temp dir.
     * Shamelessly stolen from Jenkins F360 plugin.
     * 
     * @param file A file, assumed to be remote.
     * @return local file
     * @throws IOException
     * @throws InterruptedException
     */
	private File copyToLocalTmp(FilePath file) throws IOException, InterruptedException {
		UUID uuid = UUID.randomUUID();
		String tmp = System.getProperty("java.io.tmpdir");
		String s = System.getProperty("file.separator");
		File tmpFile = new File(tmp + s + uuid + s + file.getName());
		FilePath tmpFP = new FilePath(tmpFile);
		file.copyTo(tmpFP);
		return tmpFile;
	}
}
