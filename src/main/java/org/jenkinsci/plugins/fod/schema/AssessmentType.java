package org.jenkinsci.plugins.fod.schema;

public enum AssessmentType {
	
	STATIC_ANALYSIS(58,"Static Source Code Analysis")
	,MOBILE_PREMIUM(104,"Mobile Premium")
	,STATIC_EXPRESS(105,"Static Express")
	,DYNAMIC_PREMIUM(141,"Dynamic Premium Assessment")
	,STATIC_ASSESSMENT(170,"Static Assessment")
	;
	
	private int id;
	private String name;
	
	private AssessmentType(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	 public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public static String getTypeName(int typeId)
	{
		String name = null;
		
		for( AssessmentType type : values() )
		{
			if( type.getId() == typeId )
			{
				name = type.getName();
			}
		}
		return name;
	}

}
