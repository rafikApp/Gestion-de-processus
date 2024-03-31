package server;

import java.io.Serializable;


//pour quelle puisse etre envoyée dans les streams du serveur (envoyés dans les sockets)
public class Command implements Serializable {
	private String command;
	private String result;
	private String error;
	private boolean isExecuted;
	private String parameter;
	private String invokerId;

	public Command(String command, String parameter) {
		this.command = command;
		isExecuted = false;
		this.parameter = parameter;
	}

	public void setInvokerId(String invokerId) {
		this.invokerId = invokerId;
	}

	public String getInvokerId() {
		return invokerId;
	}

	public void setExecuted() {
		this.isExecuted = true;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean isExecuted() {
		return isExecuted;
	}

	public String getParameter() {
		return parameter;
	}

}
