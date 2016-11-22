package org.jenkinsci.plugins.fodupload.models.response;

public class TenantEntitlementDTO {
    private int entitlementId;
    private int unitsPurchased;
    private int unitsConsumed;
    private String startDate;
    private String endDate;
    private TenantEntitlementExtendedPropertiesDTO extendedProperties;

    public int getEntitlementId() {
        return entitlementId;
    }

    public int getUnitsPurchased() {
        return unitsPurchased;
    }

    public int getUnitsConsumed() {
        return unitsConsumed;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public TenantEntitlementExtendedPropertiesDTO getExtendedProperties() {
        return extendedProperties;
    }
}
