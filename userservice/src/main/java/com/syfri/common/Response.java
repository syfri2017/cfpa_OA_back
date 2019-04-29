package com.syfri.common;

import java.io.Serializable;

public class Response  implements Serializable {

	private static final long serialVersionUID = 1L;

	public final static String RESSUCCESS = "00000000";
	public final static String RESFAIL = "11111111";

	private String code = RESSUCCESS;
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
