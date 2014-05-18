package me.sablednah.MinerBuddyServer;

public enum contentType {
	ICON("text/plain"), 
	JPEG("image/jpeg"), 
	GIF("image/gif"), 
	ZIP("application/x-zip-compressed"), 
	HTML("text/html"), 
	TEXT("text/plain");

	private String content;

	private contentType(String c) {
		content = c;
	}

	public String getType() {
		return content;
	}
}
