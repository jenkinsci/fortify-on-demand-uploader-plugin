package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class GetTenantEntitlementResponse {
    private List<TenantEntitlementDTO> tenantEntitlements;
    private int entitlementTypeId;
    private String entitlementType;
    private int subscriptionTypeId;
    private String subscriptionType;

    public List<TenantEntitlementDTO> getTenantEntitlements() { return tenantEntitlements; }
    public int getEntitlementTypeId() { return entitlementTypeId; }
    public int getSubscriptionTypeId() { return subscriptionTypeId; }
    public String getEntitlementType() { return entitlementType; }
    public String getSubscriptionType() { return subscriptionType; }
}
