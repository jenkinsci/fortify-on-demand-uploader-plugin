package org.jenkinsci.plugins.fod.schema;

public enum ScanStatus {
	NONE(0,"N/A")
	,IN_PROGRESS(1,"In Progress")
	,COMPLETED(2,"Completed")
	,CANCELLED(3,"Canceled")
	,WAITING(4,"Waiting")
	;
	
	private Integer id;
	private String name;

	private ScanStatus(Integer id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public static String getName(int id)
	{
		String name = null;
		
		for( ScanStatus opt : values() )
		{
			if( opt.getId() == id )
			{
				name = opt.getName();
			}
		}
		return name;
	}
}
