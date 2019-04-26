package com.syfri.common;

import com.syfri.userservice.model.system.CurrentUser;

public class UserToken {

	/*用户token.*/
	private String token;

	/*上次登录时间.*/
	private long lastAccessTime;

	/*当前登录用户.*/
	private CurrentUser currentUser;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public CurrentUser getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(CurrentUser currentUser) {
		this.currentUser = currentUser;
	}
}
