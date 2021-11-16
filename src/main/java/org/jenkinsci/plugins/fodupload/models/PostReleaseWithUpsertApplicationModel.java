package org.jenkinsci.plugins.fodupload.models;

import java.util.ArrayList;
import java.util.List;

public class PostReleaseWithUpsertApplicationModel {
    private String applicationName;
    private String applicationDescription;
    private String applicationType;
    private String releaseName;
    private String releaseDescription;
    private String emailList;
    private Integer ownerId;
    private List<ApplicationAttributeModel> attributes;
    private String businessCriticalityType;
    private String sdlcStatusType;
    private Boolean hasMicroservices;
    private List<String> microservices;
    private String releaseMicroserviceName;
    private List<Integer> userGroupIds;

    public PostReleaseWithUpsertApplicationModel() {
        this.attributes = new ArrayList<>();
        this.microservices = new ArrayList<>();
        this.userGroupIds = new ArrayList<>();
    }

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

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getReleaseDescription() {
        return releaseDescription;
    }

    public void setReleaseDescription(String releaseDescription) {
        this.releaseDescription = releaseDescription;
    }

    public String getEmailList() {
        return emailList;
    }

    public void setEmailList(String emailList) {
        this.emailList = emailList;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public List<ApplicationAttributeModel> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ApplicationAttributeModel> attributes) {
        this.attributes = attributes;
    }

    public String getBusinessCriticalityType() {
        return businessCriticalityType;
    }

    public void setBusinessCriticalityType(String businessCriticalityType) {
        this.businessCriticalityType = businessCriticalityType;
    }

    public String getSdlcStatusType() {
        return sdlcStatusType;
    }

    public void setSdlcStatusType(String sdlcStatusType) {
        this.sdlcStatusType = sdlcStatusType;
    }

    public Boolean getHasMicroservices() {
        return hasMicroservices;
    }

    public void setHasMicroservices(Boolean hasMicroservices) {
        this.hasMicroservices = hasMicroservices;
    }

    public List<String> getMicroservices() {
        return microservices;
    }

    public void setMicroservices(List<String> microservices) {
        this.microservices = microservices;
    }

    public String getReleaseMicroserviceName() {
        return releaseMicroserviceName;
    }

    public void setReleaseMicroserviceName(String releaseMicroserviceName) {
        this.releaseMicroserviceName = releaseMicroserviceName;
    }

    public List<Integer> getUserGroupIds() {
        return userGroupIds;
    }

    public void setUserGroupIds(List<Integer> userGroupIds) {
        this.userGroupIds = userGroupIds;
    }
}

