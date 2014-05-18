package me.sablednah.MinerBuddyServer;

public enum httpCode {
	CODE_200("200 OK"),
	CODE_400("400 Bad Request"),
	CODE_403("403 Forbidden"),
	CODE_404("404 Not Found"),
	CODE_418("418 I'm a teapot"),
	CODE_500("500 Internal Server Error"),
	CODE_501("501 Not Implemented");
	
	private String code;

	private httpCode(String c) {
		code = c;
	}

	public String getCode() {
		return code;
	}
}
