package org.jenkinsci.plugins.fod;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.jenkinsci.plugins.fod.schema.Release;
import org.jenkinsci.plugins.fod.schema.Scan;
import org.jenkinsci.plugins.fod.schema.ScanSnapshot;
import org.junit.Before;
import org.junit.Test;

public class FoDAPITest {
	private static final String CLASS_NAME = FoDAPITest.class.getName();
	private static Logger log = LogManager.getLogManager().getLogger(FoDAPITest.class.getName());

	private String clientId = "";
	private String clientSecret = "";
	private String zipLocation = "eightball.zip";
	private String applicationName = "eightball";
	private Long applicationId = 0l;
	private String releaseName = "1.0";
	private Long releaseId = 0l;
	private Long scanId = 0l;
	
	// name=Static Assessment / id=170
	// name=Static Express / id=105
	private Long assessmentTypeId = 105l; // 105 = Static/Express
	private String technologyStack = "JAVA/J2EE";
	private String languageLevel = "1.5";
	
	private String FOD_BASE_URL = "https://www.hpfod.com";
	
	private FoDAPI api = new FoDAPI(FOD_BASE_URL);
	
	@Before
	public void setUp() {
		final String METHOD_NAME = CLASS_NAME+".setUp()";
		
		if( !api.isLoggedIn() )
		{
			try {
				boolean authSuccess = api.authorize(clientId, clientSecret);
				System.out.println(METHOD_NAME+": authSuccess="+authSuccess);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void testConnect() {
        boolean success = false;
		
		String[] args = new String[] {"www.hpfod.com","443"};
		try {
			// source: http://stackoverflow.com/questions/21442547/java-ssl-dh-keypair-generation-prime-size-error
			if( args.length < 2 ){ System.out.println ("Usage: tohost port [fromaddr [fromport]]"); return; }
	        Socket sock = SSLSocketFactory.getDefault().createSocket();
	        if( args.length > 2 )
	            sock.bind (new InetSocketAddress (args[2], args.length>3? Integer.parseInt(args[3]): 0));
	        sock.connect (new InetSocketAddress (args[0], Integer.parseInt(args[1])));
	        System.out.println (sock.getInetAddress().getHostName() + " = " + sock.getInetAddress().getHostAddress());
	        ((SSLSocket)sock).startHandshake();
	        System.out.println ("connect okay " + ((SSLSocket)sock).getSession().getCipherSuite());
	        success = true;
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		}
		assertTrue(success);
	}
	
	@Test
	public void testAuthorizeApiKey() throws IOException {
		final String METHOD_NAME = CLASS_NAME+".testAuthorizeApiKey";
		boolean authSuccess = false;
		authSuccess = api.authorize(clientId, clientSecret);
		System.out.println(METHOD_NAME+": authSuccess="+authSuccess);
		assertTrue(authSuccess);
	}

	@Test
	public void testIsLoggedIn() throws IOException {
		final String METHOD_NAME = CLASS_NAME+".testIsLoggedIn";
		boolean authSuccess = false;
		authSuccess = api.authorize(clientId,clientSecret);
		System.out.println(METHOD_NAME+": authSuccess: "+authSuccess);
		System.out.println(METHOD_NAME+": loggedIn: "+api.isLoggedIn());
		assertTrue(authSuccess);
	}

	@Test
	public void testGetApplicationList() throws IOException {
		final String METHOD_NAME = CLASS_NAME+".testGetApplicationList";
		
		Map<String,String> appList = api.getApplicationList();
		assertNotNull(appList);
		assertFalse(appList.isEmpty());
		for( Entry<String,String> entry : appList.entrySet() )
		{
			System.out.println(METHOD_NAME+": name="+entry.getKey()+" / id=" + entry.getValue() );
		}
	}

	@Test
	public void testGetReleaseListByAppId() throws IOException {
		final String METHOD_NAME = CLASS_NAME+".testGetReleaseListByAppId";
		
		List<Release> releaseList = api.getReleaseList(this.applicationId);
		assertNotNull(releaseList);
		assertTrue(!releaseList.isEmpty());
		for( Release release : releaseList )
		{
			System.out.println(METHOD_NAME+": name="+release.getReleaseName()+" / id=" + release.getReleaseId() );
		}
	}
	
	@Test
	public void testGetReleaseIdByAppNameRelName() throws IOException
	{
		final String METHOD_NAME = CLASS_NAME+".testGetReleaseIdByAppNameRelName";
		
		Long actualReleaseId = api.getReleaseId(applicationName,releaseName);
		assertNotNull(releaseId);
		System.out.println(METHOD_NAME+": releaseId = "+actualReleaseId);
		assertTrue(releaseId.equals(actualReleaseId));
	}

	@Test
	public void testGetReleaseByAppNameRelName() throws IOException
	{
		final String METHOD_NAME = CLASS_NAME+".testGetReleaseByAppNameRelName";
		
		Release release = api.getRelease(applicationName,releaseName);
		assertNotNull(release);
		assertNotNull(release.getApplicationId());
		System.out.println(METHOD_NAME+": release.applicationId = "+release.getApplicationId());
		System.out.println(METHOD_NAME+": release.applicationName = "+release.getApplicationName());
		System.out.println(METHOD_NAME+": release.releaseId = "+release.getReleaseId());
		System.out.println(METHOD_NAME+": release.releaseName = "+release.getReleaseName());
		System.out.println(METHOD_NAME+": release.staticScanStatus = "+release.getStaticScanStatus());
		System.out.println(METHOD_NAME+": release.staticScanStatusId = "+release.getStaticScanStatusId());
		System.out.println(METHOD_NAME+": release.staticScanDate = "+release.getStaticScanDate());
		System.out.println(METHOD_NAME+": release.isPassed = "+release.getIsPassed());
		System.out.println(METHOD_NAME+": release.passFailReasonId = "+release.getPassFailReasonId());
	}

	@Test
	public void testGetReleaseByRelId() throws IOException
	{
		final String METHOD_NAME = CLASS_NAME+".testGetReleaseByRelId";
		
		Release release = api.getRelease(releaseId);
		assertNotNull(release);
		assertNotNull(release.getApplicationId());
		System.out.println(METHOD_NAME+": release.applicationId = "+release.getApplicationId());
		System.out.println(METHOD_NAME+": release.applicationName = "+release.getApplicationName());
		System.out.println(METHOD_NAME+": release.releaseId = "+release.getReleaseId());
		System.out.println(METHOD_NAME+": release.releaseName = "+release.getReleaseName());
		System.out.println(METHOD_NAME+": release.staticScanStatus = "+release.getStaticScanStatus());
		System.out.println(METHOD_NAME+": release.staticScanStatusId = "+release.getStaticScanStatusId());
		System.out.println(METHOD_NAME+": release.staticScanDate = "+release.getStaticScanDate());
		System.out.println(METHOD_NAME+": release.isPassed = "+release.getIsPassed());
		System.out.println(METHOD_NAME+": release.passFailReasonId = "+release.getPassFailReasonId());
	}
	
	@Test
	public void testGetAllReleases() throws IOException
	{
		final String METHOD_NAME = CLASS_NAME+".testGetAllReleases";
		
		List<Release> releaseList = api.getReleaseList();
		assertNotNull(releaseList);
		assertFalse(releaseList.isEmpty());
		for(int i=0; i<releaseList.size(); ++i) {
			Release release = releaseList.get(i);
			assertNotNull(release.getApplicationId());
			System.out.println(METHOD_NAME+": release["+i+"].applicationId = "+release.getApplicationId());
			System.out.println(METHOD_NAME+": release["+i+"].applicationName = "+release.getApplicationName());
			System.out.println(METHOD_NAME+": release["+i+"].releaseId = "+release.getReleaseId());
			System.out.println(METHOD_NAME+": release["+i+"].releaseName = "+release.getReleaseName());
			System.out.println(METHOD_NAME+": release["+i+"].staticScanStatus = "+release.getStaticScanStatus());
			System.out.println(METHOD_NAME+": release["+i+"].staticScanStatusId = "+release.getStaticScanStatusId());
			System.out.println(METHOD_NAME+": release["+i+"].staticScanDate = "+release.getStaticScanDate());
			System.out.println(METHOD_NAME+": release["+i+"].isPassed = "+release.getIsPassed());
			System.out.println(METHOD_NAME+": release["+i+"].passFailReasonId = "+release.getPassFailReasonId());
		}
	}

	@Test
	public void testGetAssessmentTypeList() throws IOException {
		final String METHOD_NAME = CLASS_NAME+".testGetAssessmentTypeList";
		
		Map<String,String> asmtTypeList = api.getAssessmentTypeList();
		assertNotNull(asmtTypeList);
		for( Entry<String,String> entry : asmtTypeList.entrySet() )
		{
			System.out.println(METHOD_NAME+": name="+entry.getKey()+" / id=" + entry.getValue() );
		}
	}

	@Test
	public void testGetScanSnapshotList() throws IOException {
		final String METHOD_NAME = CLASS_NAME+".testGetScanList";
		
		List<ScanSnapshot> scanList = api.getScanSnapshotList();
		assertNotNull(scanList);
		assertFalse(scanList.isEmpty());
		for( ScanSnapshot entry : scanList )
		{
			System.out.println(METHOD_NAME+": projectVersionId="+entry.getProjectVersionId()+" , dynamicScanId=" + entry.getDynamicScanId()+" , staticScanId=" + entry.getStaticScanId()+" , mobileScanId=" + entry.getMobileScanId()+" , historyRollupId=" + entry.getHistoryRollupId() );
		}
	}

	@Test
	public void testGetLatestScan2() throws IOException {
		final String METHOD_NAME = CLASS_NAME+".testGetLatestScan2";
		
		List<ScanSnapshot> scanList = api.getScanSnapshotList(releaseId);
		List<Long> staticScanIds = new LinkedList<Long>();
		
		assertNotNull(scanList);
		for( ScanSnapshot entry : scanList )
		{
			System.out.println(METHOD_NAME+": projectVersionId="+entry.getProjectVersionId()+" , releaseId="+entry.getReleaseId()+" , dynamicScanId=" + entry.getDynamicScanId()+" , staticScanId=" + entry.getStaticScanId()+" , mobileScanId=" + entry.getMobileScanId()+" , historyRollupId=" + entry.getHistoryRollupId() );
			Long staticScanId = entry.getStaticScanId();
			if( null != staticScanId && 0 < staticScanId )
			{
				staticScanIds.add(staticScanId);
			}
		}
		
		Long maxScanId = 0l;
		for( Long scanId : staticScanIds )
		{
			System.out.println(METHOD_NAME+": examining scan ID "+scanId);
			if( scanId > maxScanId )
			{
				maxScanId = scanId;
			}
		}
		System.out.println(METHOD_NAME+": max static scan ID = "+maxScanId);
		assertTrue( 0 < maxScanId );
	}

	@Test
	public void testGetScan() throws IOException {
		final String METHOD_NAME = CLASS_NAME+".testGetScan";
		
		Scan scan = api.getScan(releaseId, scanId);
		assertNotNull(scan);
		assertNotNull(scan.getScanId());
		System.out.println(METHOD_NAME+": scan.releaseId = "+scan.getReleaseId());
		System.out.println(METHOD_NAME+": scan.scanId = "+scan.getScanId());
		System.out.println(METHOD_NAME+": scan.scanTypeId = "+scan.getScanTypeId());
		System.out.println(METHOD_NAME+": scan.starRating = "+scan.getStarRating());
		System.out.println(METHOD_NAME+": scan.totalIssues = "+scan.getTotalIssues());
		System.out.println(METHOD_NAME+": scan.isFalsePositiveChallenge = "+scan.getIsFalsePositiveChallenge());
		System.out.println(METHOD_NAME+": scan.isRemediationScan = "+scan.getIsRemediationScan());
	}

//	@Test
//	public void testSendData() {
//		fail("Not yet implemented");
//	}
//
	
	// https://www.hpfod.com/api/v1/release/93328/scan/?assessmentTypeId=105&technologyStack=JAVA/J2EE&languageLevel=1.5&fragNo=-1&len=423&offset=0
	@Test
	public void testUploadFile_RelId() throws IOException {
		final String METHOD_NAME = CLASS_NAME+".testUploadFile_RelId";
		
		UploadRequest req = new UploadRequest();
		req.setReleaseId(releaseId);
		req.setAssessmentTypeId(assessmentTypeId);
		req.setTechnologyStack(technologyStack);
		req.setLanguageLevel(languageLevel);
		req.setZipLocation(zipLocation);
		UploadStatus status = null;
		//status = api.uploadFile(req);
		assertNotNull(status);
		System.out.println(METHOD_NAME+": HTTP status code: "+status.getHttpStatusCode());
		System.out.println(METHOD_NAME+": POST failed: "+status.isSendPostFailed());
		System.out.println(METHOD_NAME+": Error message: "+status.getErrorMessage());
		System.out.println(METHOD_NAME+": Bytes sent: "+status.getBytesSent());
		assertFalse(status.isSendPostFailed());
		assertTrue(status.isUploadSucceeded());
	}

	@Test
	public void testUploadFile_RelName() throws IOException {
		final String METHOD_NAME = CLASS_NAME+".testUploadFile_RelName";
		
		UploadRequest req = new UploadRequest();
		req.setApplicationName(applicationName);
		req.setReleaseName(releaseName);
		req.setAssessmentTypeId(assessmentTypeId);
		req.setTechnologyStack(technologyStack);
		req.setLanguageLevel(languageLevel);
		req.setZipLocation(zipLocation);
		UploadStatus status = null;
		//status = api.uploadFile(req);
		assertNotNull(status);
		System.out.println(METHOD_NAME+": HTTP status code: "+status.getHttpStatusCode());
		System.out.println(METHOD_NAME+": POST failed: "+status.isSendPostFailed());
		System.out.println(METHOD_NAME+": Error message: "+status.getErrorMessage());
		System.out.println(METHOD_NAME+": Bytes sent: "+status.getBytesSent());
		assertFalse(status.isSendPostFailed());
		assertTrue(status.isUploadSucceeded());
	}
}
