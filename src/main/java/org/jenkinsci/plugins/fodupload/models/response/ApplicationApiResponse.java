package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class ApplicationApiResponse {
    private int applicationId;
    private String applicationName;
    private String applicationDescription;
    private String applicationCreatedDate;
    private int businessCriticalityTypeId;
    private String businessCriticalityType;
    private String emailList;
    private int applicationTypeId;
    private String applicationType;
    private Boolean hasMicroservices;
    private List<ApplicationAttributeExtended> attributes;

    public int getApplicationId() {
        return applicationId;
    };
    public String getApplicationName() {
        return applicationName;
    };
    public String getApplicationDescription() {
        return applicationDescription;
    };
    public String getApplicationCreatedDate() {
        return applicationCreatedDate;
    };
    public int getBusinessCriticalityTypeId() {
        return businessCriticalityTypeId;
    };
    public String getBusinessCriticalityType() {
        return businessCriticalityType;
    };
    public String getEmailList() {
        return emailList;
    };
    public int getApplicationTypeId() {
        return applicationTypeId;
    };
    public String getApplicationType() {
        return applicationType;
    };
    public Boolean getHasMicroservices() {
        return hasMicroservices;
    };

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<ApplicationAttributeExtended> getAttributes() {
        return attributes;
    };
}
@SuppressFBWarnings("EI_EXPOSE_REP")
class ApplicationAttributeExtended {
    private String name;
    private int id;
    private String value;
    
    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    public String getName() {
        return name;
    };
    public int getId() {
        return id;
    };
    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    public String getValue() {
        return value;
    };
}