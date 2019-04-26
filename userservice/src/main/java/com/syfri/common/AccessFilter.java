package com.syfri.common;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebFilter(filterName = "accessFilter", urlPatterns = "/*")
public class AccessFilter implements Filter {

	private static Logger logger = LoggerFactory.getLogger(AccessFilter.class);

	private static long validPeriod;

	@Value("${user.token.period: 1800000}")
	public void setValidPeriod(Long validPeriod){
		AccessFilter.validPeriod = validPeriod;
	}

	//不需要进行过滤的url
	private Set<String> urlPatterns = null;

	public AccessFilter(){

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("===============AccessFilter init=============");
		urlPatterns = new HashSet<>();
		urlPatterns.add("/login");
		urlPatterns.add("/imageCode");
		urlPatterns.add("/signin/sendMessage");
		urlPatterns.add("/signin/sendMailEng");
		urlPatterns.add("/signin/getUsernameByMail");
		urlPatterns.add("/signin/findByMail");
		urlPatterns.add("/signin/findByUsername");
		urlPatterns.add("/signin/insertByVO");
		urlPatterns.add("/signin/updateByVO");
        urlPatterns.add("/signin/findByUnscid/");
        urlPatterns.add("/signin/getMailNum/");
        urlPatterns.add("/signin/sendMail");
        urlPatterns.add("/zwjbxx/doSearchListByVO");
        urlPatterns.add("/zgjbxx/doSearchHbListByVO");
        urlPatterns.add("/zgjbxx/doSearchHbMKListByVO");
        urlPatterns.add("/zgjbxx/doSearchDataListByVO");
		urlPatterns.add("/doExp");
		urlPatterns.add("api-docs");
		urlPatterns.add("static");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse) response;
		handleCrossDomain(req, resp);

		if(req.getMethod().equals("OPTIONS")){
			chain.doFilter(req, resp);
		}else{
			// 获得用户请求的URI
			String path = req.getRequestURI();
			logger.info(path);

			// 登陆请求无需过滤
			if(isMatches(path) || path.equals("/") || path.equals("/login")){
				chain.doFilter(req, resp);
			}else if(path.endsWith(".html")){
				req.getRequestDispatcher("/index.html").forward(req, resp);
			}else{
				boolean isAuth = false;
				Response result = Response.build();
				result.setCode("11111111");

				// 从head中获取token，如果head中的token为null，则从request中获取请求
				String accessToken = req.getHeader(MessageCache.LOGIN_TOKEN);
				if (StringUtils.isEmpty(accessToken)) {
					accessToken = req.getParameter(MessageCache.LOGIN_TOKEN);
				}

				// 判断对应Token是否存在
				if (StringUtils.isNotEmpty(accessToken) && MessageCache.containsKey(accessToken)) {
					UserToken loginToken = MessageCache.getUserToken(accessToken);
					if(loginToken != null){
						long lastAccessTime = loginToken.getLastAccessTime();
						long currentTime = System.currentTimeMillis();

						// 判断是否过期
						if ((currentTime-lastAccessTime) > validPeriod) {
							MessageCache.removeToken(accessToken); //若登录token超时，则模拟退出后台登录
						} else {
							loginToken.setLastAccessTime(currentTime);
							MessageCache.putToken(accessToken,loginToken);
							isAuth = true;
						}
						// 登录校验通过
						if (isAuth) {
							chain.doFilter(request, response);
						} else {
							result.setCode("44444444");
							result.setMessage("登录超时，请重新登录");
							resp.setContentType("text/html");
							resp.getWriter().print(JSON.toJSONString(result));
						}
					}else{
						result.setCode("55555555");
						result.setMessage("登录异常");
						resp.setContentType("text/html");
						resp.getWriter().print(JSON.toJSONString(result));
					}

				}else{
					result.setCode("55555555");
					result.setMessage("登录异常");
					resp.setContentType("text/html");
					resp.getWriter().print(JSON.toJSONString(result));
				}
			}
		}
	}

	@Override
	public void destroy() {
		logger.info("===============AccessFilter destroy=============");
	}


	//处理跨域
	private void handleCrossDomain(HttpServletRequest req,HttpServletResponse resp){
		resp.setContentType("text/json;charset=UTF-8");
		resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, OPTIONS");
		resp.setHeader("Access-Control-Max-Age", "0");
		//res.setHeader("Access-Control-Allow-Headers", "Origin, No-Cache, X-Requested-With, "
		//		+ "If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type, X-E4M-With,userId,token");
		resp.setHeader("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, X-E4M-With, Authorization,"
				+ " IPS, XTOKEN, PATHNAME, PATHURL");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("XDomainRequestAllowed","1");

	}

	/**判断其请求是否在不需要拦截的名单中
	 * by li.xue 2019/1/29
	 * */
	private boolean isMatches(String path){
		boolean result = false;
		for(String pattern :urlPatterns){
			if(path.indexOf(pattern) > -1){
				result = true;
				break;
			}
		}
		return result;
	}
}
