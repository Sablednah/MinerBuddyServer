package me.sablednah.MinerBuddyServer;

public class CommandArchtype {
	private String name;
	private int arguments;
	private String console;
	private String pwd;
	
	public CommandArchtype(String n, int a, String c, String p) {
		this.setName(n);
		this.setArguments(a);
		this.setConsole(c);
		this.setPwd(p);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the arguments
	 */
	public int getArguments() {
		return arguments;
	}

	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(int arguments) {
		this.arguments = arguments;
	}

	/**
	 * @return the console
	 */
	public String getConsole() {
		return console;
	}

	/**
	 * @param console the console to set
	 */
	public void setConsole(String console) {
		this.console = console;
	}

    /**
     * @return the pwd
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * @param pwd the pwd to set
     */
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
