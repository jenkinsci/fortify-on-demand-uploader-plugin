package org.jenkinsci.plugins.fod.schema;

import java.util.Date;

public class Application {

    private long applicationId;
    private String applicationName;
    private String applicationDescription;
    private Date applicationCreatedDate;
    private int businessCriticalityTypeId;
    private String businessCriticalityType;
    private String emailList;
    private long applicationTypeId;
    private String applicationType;

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getApplicationDescription() {
		return applicationDescription;
	}

	public void setApplicationDescription(String applicationDescription) {
		this.applicationDescription = applicationDescription;
	}

	public Date getApplicationCreatedDate() {
		return applicationCreatedDate;
	}

	public void setApplicationCreatedDate(Date applicationCreatedDate) {
		this.applicationCreatedDate = applicationCreatedDate;
	}

	public int getBusinessCriticalityTypeId() {
		return businessCriticalityTypeId;
	}

	public void setBusinessCriticalityTypeId(int businessCriticalityTypeId) {
		this.businessCriticalityTypeId = businessCriticalityTypeId;
	}

	public String getBusinessCriticalityType() {
		return businessCriticalityType;
	}

	public void setBusinessCriticalityType(String businessCriticalityType) {
		this.businessCriticalityType = businessCriticalityType;
	}

	public String getEmailList() {
		return emailList;
	}

	public void setEmailList(String emailList) {
		this.emailList = emailList;
	}

	public long getApplicationTypeId() {
		return applicationTypeId;
	}

	public void setApplicationTypeId(long applicationTypeId) {
		this.applicationTypeId = applicationTypeId;
	}

	public String getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}

	public void setApplicationId(long applicationId) {
		this.applicationId = applicationId;
	}

}