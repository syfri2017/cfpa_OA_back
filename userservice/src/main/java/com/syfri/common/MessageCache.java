package com.syfri.common;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public final class MessageCache {

	public final static String LOGIN_TOKEN = "XTOKEN";

	private static Map<String,UserToken> userMap = Maps.newConcurrentMap();

	public static UserToken getUserToken(String token){
		if(userMap.containsKey(token)){
			return userMap.get(token);
		}
		return null;

	}

	public static void putToken(String token ,UserToken userToken){
		userMap.put(token,userToken);
	}

	public static void removeToken(String token){
		if(userMap.containsKey(token)) {
			userMap.remove(token);
		}
	}

	public static boolean containsKey(String token){
		return userMap.containsKey(token);
	}

	public static UserToken getUserToken(HttpServletRequest request){
		String accessToken = request.getHeader(MessageCache.LOGIN_TOKEN);
		if (StringUtils.isEmpty(accessToken)) {
			accessToken = request.getParameter(MessageCache.LOGIN_TOKEN);
		}
		if(userMap.containsKey(accessToken)){
			return userMap.get(accessToken);
		}
		return null;
	}

	public static UserToken getUserToken(){
		HttpServletRequest  request = getRequest();
		String accessToken = request.getHeader(MessageCache.LOGIN_TOKEN);
		if (StringUtils.isEmpty(accessToken)) {
			accessToken = request.getParameter(MessageCache.LOGIN_TOKEN);
		}
		if(userMap.containsKey(accessToken)){
			return userMap.get(accessToken);
		}
		return null;
	}


	public static HttpServletRequest getRequest() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if(null==requestAttributes){
			return null;
		}
		return(HttpServletRequest)requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
		//return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();// 获取request
	}

	public static void removeToken(HttpServletRequest request) {
		String token = request.getHeader(MessageCache.LOGIN_TOKEN);
		if (token == null || token.isEmpty()) {
			token = request.getParameter(MessageCache.LOGIN_TOKEN);
		}
		removeToken(token);
	}
}
