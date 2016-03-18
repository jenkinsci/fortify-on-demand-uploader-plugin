package org.jenkinsci.plugins.fod;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.jenkinsci.plugins.fod.schema.AssessmentType;
import org.jenkinsci.plugins.fod.schema.Release;
import org.jenkinsci.plugins.fod.schema.ScanStatus;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

/*
 * @author ryancblack
 * @author Michael.A.Marshall
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
	private static final Long DEFAULT_POLLING_INTERVAL = 1l;

	private String filePattern;
	private String assessmentTypeId;
	private String applicationName;
	private String releaseName;
	private String technologyStack;
	private String languageLevel;
	private boolean runSonatypeScan;
	private boolean isExpressScan;
	private boolean isExpressAudit;
	private boolean doSkipFortifyResults;
	private boolean doPrettyLogOutput;
	private boolean includeAllFiles;
	

	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public FodBuilder(String filePattern
			,String assessmentTypeId
			,String applicationName
			,String releaseName
			,String technologyStack
			,String languageLevel
			,boolean runSonatypeScan
			,boolean isExpressScan
			,boolean isExpressAudit
			,boolean doSkipFortifyResults
			,boolean doPrettyLogOutput
			,boolean includeAllFiles)
	{
		this.filePattern = filePattern;
		this.assessmentTypeId = assessmentTypeId;
		this.applicationName = applicationName;
		this.releaseName = releaseName;
		this.technologyStack = technologyStack;
		this.languageLevel = languageLevel;
		this.runSonatypeScan = runSonatypeScan;
		this.isExpressScan = isExpressScan;
		this.isExpressAudit = isExpressAudit;
		this.doSkipFortifyResults = doSkipFortifyResults;
		this.doPrettyLogOutput = doPrettyLogOutput;
		this.includeAllFiles = includeAllFiles;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getFilePattern()
	{
		return filePattern;
	}
	public String getAssessmentTypeId()
	{
		return assessmentTypeId;
	}

	public String getApplicationName()
	{
		return applicationName;
	}

	public String getReleaseName()
	{
		return releaseName;
	}

	public String getTechnologyStack()
	{
		return technologyStack;
	}

	public String getLanguageLevel()
	{
		return languageLevel;
	}

	public boolean getRunSonatypeScan()
	{
		return runSonatypeScan;
	}
	public boolean getIsExpressScan()
	{
		return isExpressScan;
	}
	public boolean getIsExpressAudit()
	{
		return isExpressAudit;
	}
	public boolean doSkipFortifyReults()
	{
		return doSkipFortifyResults;
	}
	public boolean doPrettyLogOutput()
	{
		return doPrettyLogOutput;		
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
	
	/** Takes technologyStack and returns a correct regular expression for files needed by that type
	 * @param technologyStack
	 * @return regular expression string of files by language
	 */
	private static String getFileExpressionPatternString(String technologyStack){
		
		String constantFiles = "|.*\\.html|.*\\.htm|.*\\.js|.*\\.xml|.*\\.xsd|.*\\.xmi|.*\\.wsdd|.*\\.config|.*\\.settings|.*\\.cpx|.*\\.xcfg|.*\\.cscfg|.*\\.cscdef|.*\\.wadcfg|.*\\.appxmanifest"
							 + "|.*\\.wsdl|.*\\.plist|.*\\.properties|.*\\.ini|.*\\.sql|.*\\.pks|.*\\.pkh|.*\\.pkb";
		
		
		if (technologyStack.equalsIgnoreCase(".NET")){
			
			return ".*\\.dll|.*\\.pdb|.*\\.cs|.*\\.aspx|.*\\.asp|.*\\.vb|.*\\.vbproj|.*\\.csproj|.*\\.sln" + constantFiles;
		}
		if (technologyStack.equalsIgnoreCase("JAVA/J2EE")){
			
			return ".*\\.java|.*\\.class|.*\\.ear|.*\\.war|.*\\.jar|.*\\.jsp|.*\\.tag|.*\\.tagx|.*\\.tld|.*\\.jspx|.*\\.xhtml|.*\\.faces|.*\\.jsff|.*\\.properties" + constantFiles;
		}
		if (technologyStack.equalsIgnoreCase("Python")){
			
			return ".*\\.py" + constantFiles;
		}
		if (technologyStack.equalsIgnoreCase("Ruby")){
			
			return ".*\\.rb|.*\\.erb" + constantFiles;
		}
		if (technologyStack.equalsIgnoreCase("Objective-C")){
			
			return ".*"; //returning all as mobile applications are typically small and we must build them to assess.
		}
		if (technologyStack.equalsIgnoreCase("ASP")){
			
			return ".*\\.asp" + constantFiles;
		}
		if (technologyStack.equalsIgnoreCase("PHP")){
			
			return ".*\\.php" + constantFiles;
		}
		if (technologyStack.equalsIgnoreCase("VB6")){
			
			return ".*\\.vbs|.*\\.bas|.*\\.frm|.*\\.ctl|.*\\.cls" + constantFiles;
		}
		if (technologyStack.equalsIgnoreCase("VBScript")){
			
			return ".*\\.vbscript" + constantFiles;
		}
		if (technologyStack.equalsIgnoreCase("Android")){
			
			return ".*\\.java|.*\\.class|.*\\.ear|.*\\.war|.*\\.jar|.*\\.jsp|.*\\.tag|.*\\.tagx|.*\\.tld|.*\\.jspx|.*\\.xhtml|.*\\.faces|.*\\.jsff|.*\\.properties|.*\\.apk" + constantFiles;			
			// APK is not normally used for Static analysis but we are collecting in the event it is useful
		}
		if (technologyStack.equalsIgnoreCase("Other")){
			
			return ".*";
		}
		if (technologyStack.equalsIgnoreCase("XML/HTML")){
			
			return ".*\\.xml|.*\\.xsd|.*\\.xmi|.*\\.wsdd|.*\\.config|.*\\.cpx|.*\\.xcfg" + constantFiles;
		}
		if (technologyStack.contains("SQL")){
			
			return ".*\\.sql|.*\\.pks|.*\\.pkh|.*\\.pkb" + constantFiles;
		}
		if (technologyStack.equalsIgnoreCase("ABAP")){
			
			return ".*\\.abap" + constantFiles;
		}
		if (technologyStack.equalsIgnoreCase("CFML")){
			
			return ".*\\.cfm|.*\\.cfml|.*\\.cfc" + constantFiles;
		}
		
		return ".*";
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
	//	logger.println(METHOD_NAME+": filePattern = \""+filePattern+"\"");		
			
		if (assessmentTypeId == null || assessmentTypeId.trim().isEmpty())
		{
			logger.println(METHOD_NAME+": No valid static assessment types are available, please contact your Technical Account Manager for assistance.");
			build.setResult(Result.FAILURE);

		}
		logger.println(METHOD_NAME+": pollingInterval = "+pollingInterval);
		logger.println(METHOD_NAME+": assessmentType = "+assessmentTypeId);
		logger.println(METHOD_NAME+": applicationName = "+applicationName);
		logger.println(METHOD_NAME+": releaseName = "+releaseName);
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
					
					FileOutputStream fos = null;
					
					try {
						fos = new FileOutputStream(tmpZipFile);

						String localFilePatterns = null;
						if (includeAllFiles)
						{
							localFilePatterns = ".*";
						} else 
						{
							localFilePatterns = getFileExpressionPatternString(technologyStack);
						}
						logger.println(METHOD_NAME+": effective file patterns = \""+localFilePatterns+"\"");
						final Pattern p = Pattern.compile(localFilePatterns, Pattern.CASE_INSENSITIVE);
						
						FileFilter filter = new FileFilter() 
						
						{
							//			private final String CLASS_NAME = FodBuilder.CLASS_NAME+".anon(FileFilter)";

							private final Pattern filePattern = p;

							@Override
							public boolean accept(File pathname) {
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
						
					} finally 
						{
							if (fos != null)
							{
								try {
									fos.flush();
								} catch (IOException e){
	
								}
								try {
									fos.close();
								} catch (IOException e){
									
								}
							}
						}
					
					logger.println(METHOD_NAME+": compressed workspace contents.");
					
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
				
				if( status.isUploadSucceeded() && !doSkipFortifyResults)
				{
					Long releaseId = api.getReleaseId(applicationName, releaseName);
					Release release = null;
					
					DateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
					Date startTime = new Date();
					
					logger.println(METHOD_NAME+": build will await Fortify scan results for: "+ applicationName + " - " + releaseName);
					logger.println(METHOD_NAME+": polling start: "+ df.format(startTime.getTime()));
					
					Long maxErrorAttempts = 12l;
					Long attempts = 0l;
					Long errorStatePollingWait = 60l; // Time in minutes * maxErrorAttempts = max error state period, applies to portal outages, or anything but authentication issues.
					
					Long pollingWait = null;
					
					try
					{
						if( null != pollingInterval && !pollingInterval.isEmpty() )
						{
							pollingWait = Long.parseLong(pollingInterval);
							logger.println(METHOD_NAME+": Effective polling interval = "+pollingWait+" minutes.");
						}
						if( null == pollingWait || 1 > pollingWait ) // minimum 1 minutes between polling
						{
							pollingWait = DEFAULT_POLLING_INTERVAL;
							logger.println(METHOD_NAME+": Effective polling interval = "+pollingWait+" minutes.");
						}
						
					}
					catch( NumberFormatException nfe )
					{
					//	nfe.printStackTrace();
						pollingWait = DEFAULT_POLLING_INTERVAL;
						logger.println(METHOD_NAME+": Effective polling interval = "+pollingWait+" minutes.");
					}
						
					Long originalPollingWait = pollingWait;  // save desired polling rate for reset upon successful retry with FoD
					
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
						try {
							
							Date dateobj = new Date();
							String pollingTimestamp = df.format(dateobj.getTime());
							
							
							if(!api.isLoggedIn())								
								{
									logger.println(METHOD_NAME+" "+pollingTimestamp+": Session stale, re-authenticating...");
									try {
										authSuccess = api.authorize(clientId, clientSecret);
									} catch (IOException e) 
									{
										logger.println(METHOD_NAME+" "+pollingTimestamp+": Issue re-authenticating to Fortify on Demand, will retry "+(maxErrorAttempts - attempts)+" times.");
										attempts++;
									}
								}
							
							logger.println(METHOD_NAME+" "+pollingTimestamp+": Polling Fortify on Demand for assessment status.");
							try{ 
								release = api.getRelease(releaseId); //may see a 503 during maintenance etc. need to be able to withstand a longer outage in this case
								
								if (!pollingWait.equals(originalPollingWait))
									{
										
										logger.println(METHOD_NAME+" "+pollingTimestamp+": Resetting poll time after retry extension to every " + originalPollingWait + " minute(s).");
									}
									
								pollingWait = originalPollingWait;   // reset polling wait since call didn't throw an exception, needed if retrying
								
							} catch (IOException e)
								{
									attempts++;
									pollingWait = errorStatePollingWait; // set to longer error state wait to give time for the FoD service to recover
									logger.println(METHOD_NAME+" "+pollingTimestamp+": Issue reading status from HPE Fortify on Demand, will retry "+(maxErrorAttempts - attempts)+" additional times, every " + 
									pollingWait + " minute(s), until resolved.");																							
								}
							
							if (release != null)
							{							
								
								continueLoop = 
										((ScanStatus.IN_PROGRESS.getId().intValue() 
												== release.getStaticScanStatusId().intValue() 
												|| ScanStatus.WAITING.getId().intValue() 
												== release.getStaticScanStatusId().intValue()
												) && (attempts < maxErrorAttempts));
								
								if (ScanStatus.WAITING.getId().intValue() == release.getStaticScanStatusId().intValue())
								{
									logger.println(METHOD_NAME+" "+pollingTimestamp+": Assessment is paused with a question from Fortify on Demand,"
											+ " please contact your Technical Account Manager. Polling will continue.");
								}
								
							}
							else
							{
								attempts++;
								logger.println(METHOD_NAME+": Error retrieving assessment status information from Fortify on Demand after "+attempts+ " attempts! Will retry "+(maxErrorAttempts - attempts)+ " more time(s), every " + 
									pollingWait + " minute(s), until resolved.");	
								continueLoop = false;
							}
						} catch (Exception e) 
							{							
								logger.println(METHOD_NAME+"Exception: "+e.getMessage());
								attempts++;
								continueLoop = false;
							}
					} while( continueLoop );

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
					
				//	logger.println(METHOD_NAME+": passFailReasonId: "+release.getPassFailReasonId()+(passFailReasonStr==null?"":" ("+passFailReasonStr+")"));
					
					if( ScanStatus.IN_PROGRESS.getId().equals(release.getStaticScanStatusId().intValue()) )
					{
						logger.println("Timed out while waiting for scan to complete. Marking build as unstable.");
						build.setResult(Result.UNSTABLE);
					}
					else if( ScanStatus.CANCELLED.getId().equals(release.getStaticScanStatusId().intValue()) )
					{
						logger.println("Scan was cancelled after uploading. Marking build as unstable. Please contact your Technical Account Manager for details.");
						build.setResult(Result.UNSTABLE);
					}
					else if( ScanStatus.COMPLETED.getId().equals(release.getStaticScanStatusId().intValue()) )
					{
						try {
							
							if(doPrettyLogOutput)
							{
								if( !release.getIsPassed() )
								{	
									logger.println("-------------------------------------------------------------------------------");
									logger.println("       Fortify on Demand Assessment Results");
									logger.println("-------------------------------------------------------------------------------");
									logger.println(" ");
									logger.println(String.format("Star Rating: %d out of 5 with %d total issue(s).",release.getRating(), release.getIssueCount()));
									logger.println(" ");
									logger.println(String.format("Critical: %d", release.getCritical()));
									logger.println(String.format("High:     %d", release.getHigh()));
									logger.println(String.format("Medium:   %d", release.getMedium()));
									logger.println(String.format("Low:      %d", release.getLow()));
									logger.println(" ");
									logger.println("For application status details see the customer portal: "  + fodUrl + "/Releases/" + release.getReleaseId() + "/Overview");
									logger.println(" ");
									logger.println("Scan failed established policy check, marking build as unstable.");
									logger.println(" ");
									logger.println("-------------------------------------------------------------------------------");
									
									build.setResult(Result.UNSTABLE);
								}
								else
								{
									logger.println("-------------------------------------------------------------------------------");
									logger.println("       Fortify on Demand Assessment Results");
									logger.println("-------------------------------------------------------------------------------");
									logger.println(" ");
									logger.println(String.format("Star Rating: %d out of 5 with %d total issue(s).",release.getRating(), release.getIssueCount()));
									logger.println(" ");
									logger.println(String.format("Critical: %d", release.getCritical()));
									logger.println(String.format("High:     %d", release.getHigh()));
									logger.println(String.format("Medium:   %d", release.getMedium()));
									logger.println(String.format("Low:      %d", release.getLow()));
									logger.println(" ");
									logger.println("For application status details see the customer portal: "  + fodUrl + "/Releases/" + release.getReleaseId() + "/Overview");
									logger.println(" ");
									logger.println("Scan passed established policy check, marking build as stable.");
									logger.println(" ");
									logger.println("-------------------------------------------------------------------------------");
									
									build.setResult(Result.SUCCESS);
								}								
							} 
							else								
							{
								if( !release.getIsPassed() )
								{	
									
									logger.println(METHOD_NAME+" Star Rating: " + release.getRating());
									logger.println(METHOD_NAME+" Issue Count: " + release.getIssueCount());
									logger.println(METHOD_NAME+" Policy Check: Failed");
								
									build.setResult(Result.UNSTABLE);
								}
								else
								{
									logger.println(METHOD_NAME+" Star Rating: " + release.getRating());
									logger.println(METHOD_NAME+" Issue Count: " + release.getIssueCount());
									logger.println(METHOD_NAME+" Policy Check: Passed");
									
									build.setResult(Result.SUCCESS);
								}															
							}							
						} 
						catch (Exception e) 
						{
							logger.println(e.getMessage());
						}
					}
					else if( ScanStatus.WAITING.getId().equals(release.getStaticScanStatusId().intValue()) )
					{
						logger.println("Scan status is 'WAITING'. Please contact your Technical Account Manager for details."); 
						//this should loop until the scan completes after a question from the FoD team or the scan is ultimately canceled.
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
					logger.println(METHOD_NAME+": HTTP Status: " + status.getHttpStatusCode());
					logger.println(METHOD_NAME+": Upload Error: " + status.getErrorMessage());
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
						logger.println(METHOD_NAME+": upload file deletion failed! See Jenkins server log for details");
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
		private static final String TS_RUBY_KEY = "Ruby";
		private static final String TS_PYTHON_KEY = "Python";
		private static final String TS_OBJECTIVEC_KEY = "Objective-C";
		private static final String TS_ABAP_KEY = "ABAP";
		private static final String TS_ASP_KEY = "ASP";
		private static final String TS_CFML_KEY = "CFML";
		private static final String TS_COBOL_KEY = "COBOL";
		private static final String TS_ANDROID_KEY = "Android";
		private static final String TS_PHP_KEY = "PHP";
		private static final String TS_PLSQL_TSQL_KEY = "PL/SQL & T-SQL";
		private static final String TS_VB6_KEY = "VB6";
		private static final String TS_VBSCRIPT_KEY = "VBScript";
		private static final String TS_XML_HTML_KEY = "XML/HTML";

		
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

		private transient Long releaseListRetentionTime = 2l*60*1000;
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
				returnValue = FormValidation.ok();
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
					tmpFodUrl.getPath();
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
		
		public ListBoxModel doFillAssessmentTypeIdItems() throws IOException
		{
			final String METHOD_NAME = CLASS_NAME+".doFillAssessmentTypeItems()";
			PrintStream out = null;
			out = System.out;
			
			ListBoxModel items = new ListBoxModel();
			
		//	out.println(METHOD_NAME+": getting FoD API object reference");
			FoDAPI api = getApi();
			if( null != api )
			{
				ensureLogin(api);
				
				if( api.isLoggedIn() )
				{
						out.println(METHOD_NAME+": getting assessment type list");
						Map<String,String> assessmentTypeList = new TreeMap<String, String>(api.getAssessmentTypeListWithRetry());
						
						if( null != assessmentTypeList && !assessmentTypeList.isEmpty() )
						{
							out.println(METHOD_NAME+": assessmentTypeList.size = "+assessmentTypeList.size());
							for( Map.Entry<String, String> item : assessmentTypeList.entrySet())
							{
								out.println(METHOD_NAME+": adding assessment type \""+item.getKey()+"\" to menu");

								items.add(new Option(item.getKey(),item.getValue(),false));							
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
						Collections.sort(appNameList, Collator.getInstance());
						
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

			items.add(new Option(TS_DOTNET_KEY, TS_DOTNET_KEY,false));
			items.add(new Option(TS_ABAP_KEY, TS_ABAP_KEY, false));
			items.add(new Option(TS_ASP_KEY, TS_ASP_KEY, false));
			items.add(new Option(TS_ANDROID_KEY, TS_ANDROID_KEY, false));
			items.add(new Option(TS_CFML_KEY, TS_ABAP_KEY, false));
			items.add(new Option(TS_COBOL_KEY, TS_COBOL_KEY, false));
			items.add(new Option(TS_JAVA_KEY, TS_JAVA_KEY,false));
			items.add(new Option(TS_OBJECTIVEC_KEY, TS_OBJECTIVEC_KEY, false));
			items.add(new Option(TS_PHP_KEY, TS_PHP_KEY, false));
			items.add(new Option(TS_PLSQL_TSQL_KEY, TS_PLSQL_TSQL_KEY, false));
			items.add(new Option(TS_PYTHON_KEY, TS_PYTHON_KEY,false));
			items.add(new Option(TS_RUBY_KEY, TS_RUBY_KEY,false));				
			items.add(new Option(TS_VB6_KEY, TS_VB6_KEY, false));
			items.add(new Option(TS_VBSCRIPT_KEY, TS_VBSCRIPT_KEY, false));
			items.add(new Option(TS_XML_HTML_KEY, TS_XML_HTML_KEY, false));

			return items;
		}
		
		

		public ListBoxModel doFillLanguageLevelItems(@QueryParameter("technologyStack") String technologyStack) {
			ListBoxModel items = new ListBoxModel();
					
			if( TS_JAVA_KEY.equalsIgnoreCase(technologyStack))
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
				items.add(new Option("4.6.1", "4.6.1",true));
			}
			else if (TS_PYTHON_KEY.equalsIgnoreCase(technologyStack))
			{
				items.add(new Option("Standard Python", "Standard Python", false));
				items.add(new Option("Django", "Django", false));
				
			}
			else
			{
				items.add(new Option("N/A",null,false)); //support for no language level, must be null for correct API call
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
			return "HPE Fortify on Demand Upload";
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
			
		//	out.println(METHOD_NAME+": releaseListCache = "+releaseListCache);
			
			boolean returnValue = 
					null == releaseListCache
					|| releaseListCache.isEmpty()
					|| null == releaseListRetrieveTime
					|| null == releaseListRetentionTime
					|| (releaseListRetrieveTime+releaseListRetentionTime < System.currentTimeMillis());
			
		//	out.println(METHOD_NAME+": releaseListCacheStale = "+returnValue);
			return returnValue;
		}
	} // end class DescriptorImpl
}
