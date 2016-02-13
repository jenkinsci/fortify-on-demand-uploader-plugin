package org.jenkinsci.plugins.fod;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import hudson.model.Descriptor.FormException;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.easymock.EasyMock;
import org.jenkinsci.plugins.fod.FodBuilder.DescriptorImpl;
import org.jenkinsci.plugins.fod.schema.Release;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;

public class FodBuilderTest {

	private static final String CLASS_NAME = FodBuilderTest.class.getName();

	private String clientId = "";
	private String clientSecret = "";
	
	@Before
	public void setUp() {
		final String METHOD_NAME = CLASS_NAME+".setUp()";
	}
	
	
	@Test
	public void testFodBuilder() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTenantCode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUsername() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPassword() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetZipLocation() {
		fail("Not yet implemented");
	}

	@Test
	public void testPerformRunOfQQFilePathLauncherTaskListener() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDescriptor() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testGetApi() {
		
	}

	@Test
	public void testConfigure() throws Exception {
		final String METHOD_NAME = CLASS_NAME+"testGetApplicationNameList_empty";
		
		DescriptorImpl descriptor = EasyMock.partialMockBuilder(DescriptorImpl.class)
				.addMockedMethod("getApi")
				.addMockedMethod("save")
				.createMock();
		FoDAPI fodApi = EasyMock.niceMock(FoDAPI.class);
		StaplerRequest staplerRequest = EasyMock.niceMock(StaplerRequest.class);
		//JSONObject is final, can't create proxy or anonymous child
		JSONObject formData = new JSONObject();
		formData.put("clientId", "abcdef");
		formData.put("clientSecret","123456");
		formData.put("fodUrl", "http://localhost:8080");
		formData.put("pollingInterval", "5");
		descriptor.save();
		replay(descriptor);
		boolean success = descriptor.configure(staplerRequest, formData);
		verify(descriptor);
	}

	/*
	@Test
	public void testGetApplicationList_single() throws Exception {
		final String METHOD_NAME = CLASS_NAME+"testGetApplicationNameList_one";
		
		DescriptorImpl descriptor = EasyMock.partialMockBuilder(DescriptorImpl.class)
				.addMockedMethod("getApi")
				.addMockedMethod("save")
				.addMockedMethod("ensureLogin")
				.createMock();
		FoDAPI fodApi = EasyMock.partialMockBuilder(FoDAPI.class)
				.addMockedMethod("isLoggedIn")
				.addMockedMethod("getApplicationList")
				.createMock();
		System.out.println("fodApi = "+fodApi);
		
		expect(descriptor.getApi()).andReturn(fodApi);
		descriptor.ensureLogin(fodApi);
		expect(fodApi.isLoggedIn()).andReturn(true).anyTimes(); // returns false, for some reason
		String applicationName = "eightball";
		String applicationId = "234567";
		Map<String, String> appList = new LinkedHashMap<String,String>();
		appList.put(applicationName, applicationId);
		expect(fodApi.getApplicationList()).andReturn(appList);
		replay(descriptor);
		
		ListBoxModel model = descriptor.doFillApplicationNameItems();
		verify(descriptor);
		
		assertNotNull( model );
		assertTrue( appList.size() == model.size() );
		assertTrue( applicationName.equals(model.get(0).name) );
	}
	*/
	
	@Test
	public void testGetReleasesWithRetry() throws IOException, NoSuchMethodException, SecurityException
	{
		DescriptorImpl descriptor = partialMockBuilder(DescriptorImpl.class)
				.addMockedMethod("load")
				.addMockedMethod("getApi")
				.createMock();
		
		Method relListMethod = FoDAPI.class.getMethod("getReleaseList");
		
		FoDAPI api = partialMockBuilder(FoDAPI.class)
				//.addMockedMethod("getReleaseList")
				.addMockedMethod(relListMethod)
				.withConstructor()
				.createMock();
		
		api.setBaseUrl("https://www.hpfod.com");
		//api.setPrincipal(clientId, clientSecret);
		int maxAttempts = 5;
		api.authorize(clientId, clientSecret);
		expect(api.getReleaseList()).andThrow(new IOException("oh noes!"));
		expect(api.getReleaseList()).andReturn(null);
		expect(api.getReleaseList()).andReturn(null);
		expect(api.getReleaseList()).andReturn(new LinkedList<Release>());
		List<Release> mockReleaseList = new LinkedList<Release>();
		Release release = new Release();
		release.setApplicationName("eightball");
		release.setReleaseName("1.0");
		mockReleaseList.add(release);
		expect(api.getReleaseList()).andReturn(mockReleaseList);
		
		replay(descriptor);
		replay(api);
		List<Release> releaseList = descriptor.getReleasesWithRetry(api, maxAttempts);
		verify(descriptor);
		verify(api);
		
		assertNotNull(releaseList);
		assertFalse(releaseList.isEmpty());
		assertTrue(mockReleaseList.size() == releaseList.size());
	}
}
