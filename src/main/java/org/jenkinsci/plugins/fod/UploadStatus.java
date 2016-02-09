package org.jenkinsci.plugins.fod;

public class UploadStatus {
	private boolean uploadSucceeded = false;
	private boolean sendPostFailed = false;
	private long bytesSent = 0;
	private String errorMessage = null;
	private Integer httpStatusCode = null;
	
	public boolean isUploadSucceeded() {
		return uploadSucceeded;
	}
	public void setUploadSucceeded(boolean uploadSucceeded) {
		this.uploadSucceeded = uploadSucceeded;
	}
	public boolean isSendPostFailed() {
		return sendPostFailed;
	}
	public void setSendPostFailed(boolean sendPostFailed) {
		this.sendPostFailed = sendPostFailed;
	}
	public long getBytesSent() {
		return bytesSent;
	}
	public void setBytesSent(long bytesSent) {
		this.bytesSent = bytesSent;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public Integer getHttpStatusCode() {
		return httpStatusCode;
	}
	public void setHttpStatusCode(Integer httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
}
