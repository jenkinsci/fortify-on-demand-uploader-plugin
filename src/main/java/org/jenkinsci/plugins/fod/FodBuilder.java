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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Date;
import java.util.LinkedList;
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
	private Boolean isExpressScan;
	private Boolean isExpressAudit;
	private Boolean doSkipFortifyResults;
	

	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public FodBuilder(String filePatterns
			,String applicationName
			,String releaseName
			,Long assessmentTypeId
			,String technologyStack
			,String languageLevel
			,Boolean runSonatypeScan
			,Boolean isExpressScan
			,Boolean isExpressAudit
			,Boolean doSkipFortifyResults)
	{
		this.filePatterns = filePatterns;
		this.applicationName = applicationName;
		this.releaseName = releaseName;
		this.assessmentTypeId = assessmentTypeId;
		this.technologyStack = technologyStack;
		this.languageLevel = languageLevel;
		this.runSonatypeScan = runSonatypeScan;
		this.isExpressScan = isExpressScan;
		this.isExpressAudit = isExpressAudit;
		this.doSkipFortifyResults = doSkipFortifyResults;
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
	public Boolean getIsExpressScan()
	{
		return isExpressScan;
	}
	public Boolean getIsExpressAudit()
	{
		return isExpressAudit;
	}
	public Boolean doSkipFortifyReults()
	{
		return doSkipFortifyResults;
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
		
		final PrintStream logger = listener.getLogger();
		
		logger.println(METHOD_NAME+": foDUrl = "+fodUrl);
		logger.println(METHOD_NAME+": pollingInterval = "+pollingInterval);	
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
			
			String baseUrl = null;
			if( null != fodUrl && !fodUrl.isEmpty() )
			{
				baseUrl = fodUrl;
			}
			else
			{
				baseUrl = FoDAPI.PUBLIC_FOD_BASE_URL;
			}
			
			FoDAPI api = new FoDAPI();
			api.setBaseUrl(baseUrl);
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
				String prefix = "fodupload";
				String suffix = ".zip";
				logger.println(METHOD_NAME+": workspace = "+workspace);
				File uploadFile = null;
				
				try
				{
					
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
			//			private final String CLASS_NAME = FodBuilder.CLASS_NAME+".anon(FileFilter)";
						
						private final Pattern filePattern = p;
						
						@Override
						public boolean accept(File pathname)
						{
			//				final String METHOD_NAME = CLASS_NAME+".accept";
			//				logger.println(METHOD_NAME+": pathname.path = "+pathname.getPath());
			//				logger.println(METHOD_NAME+": pathname.name = "+pathname.getName());
							
							boolean matches = false;
							
							Matcher m = filePattern.matcher(pathname.getName());
							matches = m.matches();
			//				logger.println(METHOD_NAME+": pathname accepted : "+matches);
							
							return matches;
						}
					};
					workspace.zip(fos, filter);
					
					fos.flush();
					fos.close();
					
					logger.println(METHOD_NAME+": zipped up workspace contents.");
					
					File localTmpZipFile = tmpZipFile;
					
					logger.println(METHOD_NAME+": localTmpZipFile = "+localTmpZipFile);
					logger.println(METHOD_NAME+": localTmpZipFile.exists = "+localTmpZipFile.exists());
					
					if( null != localTmpZipFile && localTmpZipFile.exists() )
					{
						uploadFile = localTmpZipFile;
					}

				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//TODO set global parameters on FoDAPI object instead
				UploadRequest req = new UploadRequest();
				req.setUploadZip(uploadFile);
				req.setApplicationName(applicationName);
				req.setReleaseName(releaseName);
				req.setAssessmentTypeId(assessmentTypeId);
				req.setTechnologyStack(technologyStack);
				req.setLanguageLevel(languageLevel);
				req.setRunSonatypeScan(runSonatypeScan);
				req.setIsExpressScan(isExpressScan);
				req.setIsExpressAudit(isExpressAudit);
				
				
				UploadStatus status = api.uploadFile(req);
				logger.println(METHOD_NAME+": HTTP status code: "+status.getHttpStatusCode());
				logger.println(METHOD_NAME+": POST failed: "+status.isSendPostFailed());
				logger.println(METHOD_NAME+": Error message: "+status.getErrorMessage());
				logger.println(METHOD_NAME+": Bytes sent: "+status.getBytesSent());
				
				if( status.isUploadSucceeded() && !doSkipFortifyResults)
				{
					Long releaseId = api.getReleaseId(applicationName, releaseName);
					Release release = null;
					
					logger.println(METHOD_NAME+": build will await Fortify scan results for: "+ applicationName + " - " + releaseName);
					
					//FIXME make configurable based on timeout in hours and pollingInterval in minutes
					// for now, we'll allow it to poll forever until it receives a response from FoD (complete, cancelled, etc.)
					
			//		Long maxAttempts = 3l;
			//		Long attempts = 0l;
					
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
						
				//		++attempts;
						
				//		logger.println(METHOD_NAME+": scan status ID: "+release.getStaticScanStatusId().intValue());
				//		logger.println(METHOD_NAME+": attempts: "+attempts);
						
						continueLoop = 
								(ScanStatus.IN_PROGRESS.getId().intValue() 
										== release.getStaticScanStatusId().intValue() );
						// TODO Implement max waiting period in line with SLO and/or user preference
						// currently until we see another response aside from "in progress" we'll keep checking FoD
						//		&& ( attempts < maxAttempts); 
						
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
						logger.println("Scan status is 'WAITING'. Please contact your Technical Account Manager for details."); 
						//this should loop until the scan completes after a question from the FoD team or the scan is ultimately canceled.
					//	build.setResult(Result.UNSTABLE);
					}
					else
					{
						logger.println("Scan status ID of "+release.getStaticScanStatusId().intValue()+" unrecognized. Response to this status not yet implemented in plugin!");
						build.setResult(Result.UNSTABLE);
					}
				}
				else if (!status.isUploadSucceeded())
				{
					logger.println(METHOD_NAME+": upload failure!");
					build.setResult(Result.UNSTABLE);
				}
				else
				{
					logger.println(METHOD_NAME+": continuing Jenkins build without awaiting Fortify assessment results.");
					
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
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

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
		
		private transient FoDAPI api;
		private transient Object apiLockMonitor = new Object();

		private transient Long releaseListRetentionTime = 5l*60*1000;
		private transient List<Release> releaseListCache = null;
		private transient Long releaseListRetrieveTime = null;
		private transient Object releaseListLockMonitor = new Object();

		/**
		 * In order to load the persisted global configuration, you have to 
		 * call load() in the constructor.
		 */
		public DescriptorImpl() {
			load();
		}
		
		public FormValidation doCheckClientId(@QueryParameter String value)
				throws IOException, ServletException
		{
			FormValidation returnValue = null;
			if (value.length() == 0)
			{
				returnValue = FormValidation.error("Please set an API key");
			}
			else
			{
				//TODO get less verbose regex working
				String clientIdRegex = "........-....-....-....-............";
				Pattern p = Pattern.compile(clientIdRegex);
				Matcher m = p.matcher(value);
				if( m.matches() )
				{
					returnValue = FormValidation.ok();
				}
				else
				{
					returnValue = FormValidation.error("Invalid API key");
				}
			}
			return returnValue;
		}
		
		public FormValidation doCheckClientSecret(@QueryParameter String value)
				throws IOException, ServletException
		{
			FormValidation returnValue = null;
			if (value.length() == 0)
			{
				returnValue = FormValidation.error("Please set a secret key");
			}
			else
			{
				if( 40 == value.length() )
				{
					returnValue = FormValidation.ok();
				}
				else
				{
					returnValue = FormValidation.error("Invalid secret key");
				}
			}
			return returnValue;
		}


		/**
		 * Performs on-the-fly validation of the form field 'fodUrl'.
		 *
		 * @param value
		 *	  This parameter receives the value that the user has typed.
		 * @return
		 *	  Indicates the outcome of the validation. This is sent to the browser.
		 *	  <p>
		 *	  Note that returning {@link FormValidation#error(String)} does not
		 *	  prevent the form from being saved. It just means that a message
		 *	  will be displayed to the user. 
		 */
		public FormValidation doCheckFodUrl(@QueryParameter String value)
				throws IOException, ServletException
		{
			FormValidation returnValue = null;
			if (value.length() == 0)
			{
				returnValue = FormValidation.ok();
			}
			else
			{
				try
				{
					URL tmpFodUrl = new URL(value);
					tmpFodUrl.getPath(); // can't remember if URL immediately parsed ==> force parse
				}
				catch (MalformedURLException e)
				{
					returnValue = FormValidation.error("Invalid URL");
				}
				if( null == returnValue )
				{
					returnValue = FormValidation.ok();
				}
			}
			return returnValue;
		}
		
		public ListBoxModel doFillApplicationNameItems()
		{
			final String METHOD_NAME = CLASS_NAME+".doFillApplicationNameItems()";
			PrintStream out = null;
			out = System.out;
			
			ListBoxModel items = new ListBoxModel();
			
			out.println(METHOD_NAME+": getting FoD API object reference");
			FoDAPI api = getApi();
			if( null != api )
			{
				ensureLogin(api);
				
				if( api.isLoggedIn() )
				{
						out.println(METHOD_NAME+": getting application list");
						List<String> appNameList = this.getApplicationNameList(api);
						
						if( null != appNameList && !appNameList.isEmpty() )
						{
							out.println(METHOD_NAME+": appNameList.size = "+appNameList.size());
							for( String appName : appNameList )
							{
								out.println(METHOD_NAME+": adding application \""+appName+"\" to menu");
								items.add(new Option(appName,appName,false));
							}
						}
						else
						{
							out.println(METHOD_NAME+": application list empty or null");
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
						out.println(METHOD_NAME+": getting release list for applicationName = "+applicationName);

						List<String> releaseNameList = this.getReleaseNameList(api, applicationName);

						if (null != releaseNameList && !releaseNameList.isEmpty())
						{
							out.println(METHOD_NAME+": relList.size = "+releaseNameList.size());
							for (String releaseName : releaseNameList)
							{
								out.println(METHOD_NAME+": adding release \""+releaseName+"\" to menu");
								items.add(new Option(releaseName,releaseName,false));
							}
						}
						else
						{
							out.println(METHOD_NAME+": release list empty or null");
						}
					}
					else
					{
						out.println(METHOD_NAME+": secondary login check failed");
					}
				}
				else
				{
					out.println(METHOD_NAME+": getApi() returned null");
				}
			}
			else
			{
				out.println(METHOD_NAME+": applicationName is null. doing nothing.");
			}
			
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
		public boolean configure(StaplerRequest req, JSONObject formData)
				throws FormException
		{
			final String METHOD_NAME = CLASS_NAME+".configure";
			PrintStream out = getLogger();
			
			// To persist global configuration information,
			// set that to properties and call save().
			//useFrench = formData.getBoolean("useFrench");
			// ^Can also use req.bindJSON(this, formData);
			//  (easier when there are many fields; need set* methods for this, like setUseFrench)
			
			String newClientId = formData.getString("clientId");
			if( null != newClientId && !newClientId.isEmpty() && !newClientId.equals(this.clientId) )
			{
				this.clientId = newClientId;
				if( null != this.api )
				{
					this.api.setPrincipal(this.clientId, this.clientSecret);
				}
				this.releaseListRetrieveTime = 0l;
				out.println(METHOD_NAME+": clientId = "+this.clientId);
			}
			
			String newClientSecret = formData.getString("clientSecret");
			if( null != newClientSecret && !newClientSecret.isEmpty() && !newClientSecret.equals(this.clientSecret) )
			{
				this.clientSecret = newClientSecret;
				if( null != this.api )
				{
					this.api.setPrincipal(this.clientSecret, this.clientSecret);
				}
				this.releaseListRetrieveTime = 0l;
			//	out.println(METHOD_NAME+": clientSecret = "+this.clientSecret);
			}
			
			
			String newFodUrl = formData.getString("fodUrl");
			if( null != newFodUrl && !newFodUrl.isEmpty() && !newFodUrl.equals(this.fodUrl))
			{
				this.fodUrl = newFodUrl;
				
				if( null != api )
				{
					this.api.setBaseUrl(this.fodUrl);
				}
				this.releaseListRetrieveTime = 0l;
				out.println(METHOD_NAME+": fodUrl = "+this.fodUrl);
			}
			
			String newPollingInterval = formData.getString("pollingInterval");
			if( null != pollingInterval && !newPollingInterval.isEmpty() && !newPollingInterval.equals(this.pollingInterval) )
			{
				this.pollingInterval = newPollingInterval;
				out.println(METHOD_NAME+": pollingInterval = "+this.pollingInterval);
			}
			
			out.println(METHOD_NAME+": calling save");
			save();
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

//		/**
//		 * This method returns true if the global configuration says we should use proxy.
//		 *
//		 * The method name is bit awkward because global.jelly calls this method to determine
//		 * the initial state of the checkbox by the naming convention.
//		 */
//		public boolean getUseProxy() {
//			return useProxy;
//		}
		
		/**
		 * Retrieves a reference to the FoDAPI class, creating this object if necessary.
		 * @return
		 */
		protected FoDAPI getApi() {
			final String METHOD_NAME = CLASS_NAME+".getApi()";
			PrintStream out = null;
			out = FodBuilder.getLogger();
			if( null == out ){
				out = System.out;
			}
				synchronized(this.apiLockMonitor)
				{
					if( null == api )
					{
						String configFodUrl = getFodUrl();
						out.println(METHOD_NAME+": fodUrl = "+configFodUrl);
						
						api = new FoDAPI();
						if( null != configFodUrl && !configFodUrl.isEmpty() )
						{
							api.setBaseUrl(configFodUrl);
						}
						else
						{
							out.println(METHOD_NAME+": using default FoD URL : "+FoDAPI.PUBLIC_FOD_BASE_URL);
							api.setBaseUrl(FoDAPI.PUBLIC_FOD_BASE_URL);
						}
					}
				}

			return this.api;
		}
		
		
		protected void ensureLogin(FoDAPI api)
		{
			final String METHOD_NAME = CLASS_NAME+".ensureLogin";
			PrintStream out = getLogger();
			
				// timeout must be configured, or else a hung call here blocks everything
				synchronized(this.apiLockMonitor)
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

		protected List<String> getApplicationNameList(FoDAPI api)
		{
			List<String> appNameList = null;
			
			refreshReleaseListCache(api);
			
			if( null != this.releaseListCache && !this.releaseListCache.isEmpty() )
			{
				appNameList = new LinkedList<String>();
				for( Release release : this.releaseListCache )
				{
					String appName = release.getApplicationName();
					if( null != appName && !appName.isEmpty() 
							&& !appNameList.contains(appName) )
					{
						appNameList.add(appName);
					}
				}
			}
			
			return appNameList;
		}
		
		/**
		 * Get list of release names, using cached list of names
		 * @param api
		 * @param applicationName
		 * @return
		 */
		protected List<String> getReleaseNameList(FoDAPI api, String applicationName)
		{
			List<String> releaseNameList = null;
			
			refreshReleaseListCache(api);
			
			if( null != this.releaseListCache && !this.releaseListCache.isEmpty() )
			{
				releaseNameList = new LinkedList<String>();
				for( Release release : this.releaseListCache )
				{
					String appName = release.getApplicationName();
					String relName = release.getReleaseName();
					if( null != relName && !relName.isEmpty() 
							&& null != appName && appName.equals(applicationName) )
					{
						releaseNameList.add(relName);
					}
				}
			}
			
			return releaseNameList;
		}

		protected void refreshReleaseListCache(FoDAPI api)
		{
				synchronized(this.releaseListLockMonitor)
				{
					if( this.isReleaseListCacheStale() )
					{
						List<Release> releases = getReleasesWithRetry(api, 5);
						
						if( null != releases && !releases.isEmpty() )
						{
							this.releaseListCache = releases;
							this.releaseListRetrieveTime = System.currentTimeMillis();
						}
					}
				}
		}
		
		protected List<Release> getReleasesWithRetry(FoDAPI api, int maxAttempts)
		{
			final String METHOD_NAME = CLASS_NAME + ".getReleasesWithRetry";
			PrintStream out = getLogger();
			
			List<Release> releases = null;
			int attempts = 0;
			
			out.println(METHOD_NAME+": attempting to retrieve new list of application / release names");
			out.println(METHOD_NAME+": maxAttempts = "+maxAttempts);
			
			while( (null == releases || releases.isEmpty()) && (attempts < maxAttempts) )
			{
				out.println(METHOD_NAME+": calling FoDAPI.getReleaseList() ; attempt number "+attempts);
				try
				{
					releases = api.getReleaseList();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				++attempts;
			}
			
			if( null == releases || releases.isEmpty() )
			{
				out.println(METHOD_NAME+": failed to retrieve release list");
			}
			else
			{
				out.println(METHOD_NAME+": retrieved list of "+releases.size()+" releases from FoD");
			}
			
			return releases;
		}
		
		protected boolean isReleaseListCacheStale()
		{
			final String METHOD_NAME = CLASS_NAME+".isReleaseListCacheStale";
			PrintStream out = getLogger();
			
			out.println(METHOD_NAME+": releaseListCache = "+releaseListCache);
			
			boolean returnValue = 
					null == releaseListCache
					|| releaseListCache.isEmpty()
					|| null == releaseListRetrieveTime
					|| null == releaseListRetentionTime
					|| (releaseListRetrieveTime+releaseListRetentionTime < System.currentTimeMillis());
			
			out.println(METHOD_NAME+": releaseListCacheStale = "+returnValue);
			return returnValue;
		}
	} // end class DescriptorImpl
}
