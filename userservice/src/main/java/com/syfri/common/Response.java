package com.syfri.common;

public class Response {

	private String code;
	private String message;
	private Object data;

	public static Response build(){
		Response response =  new Response();
		response.setCode("00000000");
		return response;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
