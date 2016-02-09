package org.jenkinsci.plugins.fod;

public enum AuthCredentialType {
	CLIENT_CREDENTIALS(0,"client_credentials"),PASSWORD(1,"password");
	
	private int id;
	private String name;
	
	private AuthCredentialType(int id, String name) {
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
		
		for( AuthCredentialType type : values() )
		{
			if( type.getId() == typeId )
			{
				name = type.getName();
			}
		}
		return name;
	}

}
