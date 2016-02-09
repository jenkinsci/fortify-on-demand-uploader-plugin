package org.jenkinsci.plugins.fod;

public class AuthTokenRequest {

	private String scope;
	private String grantType;
	private AuthPrincipal principal;
	
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getGrantType() {
		return grantType;
	}
	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}
	public AuthPrincipal getPrincipal() {
		return principal;
	}
	public void setPrincipal(AuthPrincipal principal) {
		this.principal = principal;
	}
}
