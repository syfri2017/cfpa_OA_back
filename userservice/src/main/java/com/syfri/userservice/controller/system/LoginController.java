package com.syfri.userservice.controller.system;

import com.syfri.common.MessageCache;
import com.syfri.common.Response;
import com.syfri.common.UserToken;
import com.syfri.userservice.model.system.*;
import com.syfri.userservice.service.system.AccountService;
import com.syfri.userservice.utils.Constants;
import com.syfri.userservice.utils.CurrentUserUtil;
import com.syfri.userservice.utils.ImageCodeUtil;
import com.syfri.userservice.utils.JwtUtil;
import io.swagger.annotations.Api;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@Api(value = "登录",tags = "登录API",description = "登录")
@RestController
public class LoginController {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	protected Environment environment;

	@Autowired
	protected AccountService accountService;

	/**
	 * 登录login
	 * by li.xue 2019/1/27 20:03
	 * */
	@PostMapping("login")
	public Response login(@RequestBody AccountVO accountVO){
		Response response = Response.build();
		String password = JwtUtil.md5(accountVO.getPassword() + "-" + Constants.PWD_KEY);
		accountVO.setPassword(password);
		AccountVO tempVO = accountService.doFindByVO(accountVO);

		if(null == tempVO){
			AccountVO tempVO2 = accountService.doFindByVO(new AccountVO(accountVO.getUsername()));
			if(null == tempVO2){
				response.setCode("22222222");
				response.setMessage("user not exist.");
			}else{
				response.setCode("33333333");
				response.setMessage("password is error.");
			}
		}else{
			String token = JwtUtil.md5(tempVO.getPassword() + tempVO.getUsername());
			UserToken userToken = new UserToken();
			userToken.setToken(token);
			userToken.setLastAccessTime(System.currentTimeMillis());
			userToken.setCurrentUser(CurrentUserUtil.setCurrentUser(tempVO));
			MessageCache.putToken(token, userToken);
			response.setData(userToken);
		}
		return response;
	}

	/**
	 * 验证码
	 */
	@GetMapping("/imageCode")
	public void reloadCode(HttpServletRequest request, HttpServletResponse response) throws IOException{

		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");

		OutputStream os = response.getOutputStream();
		Map<String,Object> map = ImageCodeUtil.getImageCode(60,20,os);
		HttpSession session = request.getSession();
		session.setAttribute("code", map.get("strEnsure").toString().toLowerCase());
		session.setAttribute("codeTime", new Date().getTime());
		try{
			ImageIO.write((BufferedImage) map.get("image"), "JPEG", os);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * 退出logout
	 */
	@GetMapping("/logout")
	public Response logout(HttpServletRequest request) {
		MessageCache.removeToken(request);
		return Response.build();
	}

	/**
	 * 查看Session是否有效
	 * by li.xue 2018/12/4 9:38
	 */
	@GetMapping("/getSession")
	public @ResponseBody String getSession(HttpServletRequest request){
		String sessionId = request.getSession().getId();
		Session session;
		try{
//			session = redisSessionDAO.readSession(sessionId);
//			Collection collection = session.getAttributeKeys();
//			if(collection.size() == 0){
//				return "0";
//			}
		}catch(UnknownSessionException e){
			return "0";
		}
		return "1";
	}
}
