package org.jenkinsci.plugins.fod.schema;

public enum PassFailReason
{
	UNASSESSED(1,"Unassessed")
		,OVERRIDE(2,"Override")
		,GRACE_PERIOD(3,"GracePeriod")
		,SCAN_FREQUENCY(14,"ScanFrequency");
	
	private Integer id;
	private String name;

	private PassFailReason(Integer id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
