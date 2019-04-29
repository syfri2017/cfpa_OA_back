package com.syfri.userservice.controller.system;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.qcloudsms.SmsMultiSender;
import com.github.qcloudsms.SmsMultiSenderResult;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.syfri.baseapi.controller.BaseController;
import com.syfri.baseapi.model.ResultVO;
import com.syfri.baseapi.utils.EConstants;
import com.syfri.userservice.model.system.*;
import com.syfri.userservice.service.system.*;
import com.syfri.baseapi.utils.MathUtil;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import  com.syfri.userservice.config.properties.MsgProperties;
import com.syfri.userservice.config.properties.MailProperties;

import javax.mail.internet.MimeMessage;

@Api(value = "测试",tags = "测试API",description = "测试")
@RestController
@RequestMapping("/test")
public class TestController extends BaseController<UserVO>{

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private CodelistService codelistService;

	@Autowired
	private MsgProperties pMsgProperties;

	@Autowired
	private JavaMailSender jms;
	@Autowired
	private MailProperties mp;

	@RequestMapping(value = "send")
	@ResponseBody
	public Object send(){
		return send("18904047625");
	}

	/**
	 * 发送邮箱验证码
	 * @return
	 */
	@GetMapping("/sendMail")
	public Object sendMail(String mail){
		ResultVO resultVO = ResultVO.build();
		if(!(mail.equals("")||null == mail)) {
			MimeMessage message = jms.createMimeMessage();
			try {
				//true表示需要创建一个multipart message
				MimeMessageHelper helper = new MimeMessageHelper(message, true);
				helper.setFrom(mp.getFrom());
				helper.setTo(mail);
				helper.setSubject(mp.getSubject());
				String randomStr = MathUtil.getCode(6);
				helper.setText(String.format(mp.getText(), randomStr), true);
				jms.send(message);
				resultVO.setCode(EConstants.CODE.SUCCESS);
				//生成的随机数(可以去掉)
				resultVO.setMsg(randomStr.toString());
				System.out.println("html格式邮件发送成功");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("html格式邮件发送失败");
				resultVO.setCode(EConstants.CODE.FAILURE);
				return resultVO;
			}
		}else{
			resultVO.setCode(EConstants.CODE.FAILURE);
			resultVO.setMsg("获取邮箱失败");
		}
		return resultVO;
	}

	/**
	 * 发送短信验证码
	 * @param phone 电话号码
	 * @return
	 */
	@GetMapping("/sendMessage")
	public ResultVO send(String phone){
		ResultVO resultVO = ResultVO.build();
		if(!(phone.equals("")||null == phone)){
			//假设短信模板 id 为 123，模板内容为：测试短信，{1}，{2}，{3}，上学。
			SmsSingleSender sender;
			try {
				sender = new SmsSingleSender(pMsgProperties.getAppId(),pMsgProperties.getAppKey());
				ArrayList<String> params = new ArrayList<String>();
				//6位随机数验证码
				String randomStr=MathUtil.getCode(6);
				params.add(randomStr);
				SmsSingleSenderResult result = sender.sendWithParam("86", phone, pMsgProperties.getTemplId(), params, "", "", "");
				if(result.result==0){
					resultVO.setCode(EConstants.CODE.SUCCESS);
					//生成的随机数(可以去掉)
					resultVO.setMsg( randomStr.toString());
				}else{
					resultVO.setCode(EConstants.CODE.FAILURE);
					resultVO.setMsg( result.errMsg);
				}
			} catch (Exception e) {
				resultVO.setCode(EConstants.CODE.FAILURE);
				resultVO.setMsg(  e.getMessage());
				e.printStackTrace();

				return resultVO;
			}
		}else{

			resultVO.setCode(EConstants.CODE.FAILURE);
			resultVO.setMsg("获取手机号失败");
		}
		return resultVO;
	}

	/**
	 * 发送群发短信验证码
	 * @return
	 */
	@GetMapping("/sendMultiMessage")
	public ResultVO sendMultiMessage(){
		ResultVO resultVO = ResultVO.build();
		ArrayList<String> phoneNumbers =new ArrayList<String>();
		phoneNumbers.add("15604023161");
		//phoneNumbers.add("15840058633");
		phoneNumbers.add("6666666666");
		//平台限制单次只能发200条
		if(phoneNumbers.size()>0&&phoneNumbers.size()<=200){
			//假设短信模板 id 为 123，模板内容为：测试短信，{1}，{2}，{3}，上学。
			SmsMultiSender msender ;
			try {
				msender = new SmsMultiSender(pMsgProperties.getAppId(),pMsgProperties.getAppKey());
				ArrayList<String> params = new ArrayList<String>();
				//6位随机数验证码
				String randomStr=MathUtil.getCode(6);
				params.add(randomStr);
				SmsMultiSenderResult result = msender.sendWithParam("86", phoneNumbers, pMsgProperties.getTemplId(), params, "", "", "");
				ArrayList<SmsMultiSenderResult.Detail> details=result.details;
				if(result.result==0){
					//全部发送成功
					if(details.size()<=0){
						resultVO.setCode(EConstants.CODE.SUCCESS);
						//生成的随机数(可以去掉)
						resultVO.setMsg( randomStr.toString());
					}else {
						//部分发送成功
						resultVO.setCode(EConstants.CODE.FAILURE);
						resultVO.setMsg(result.details.toString());
						//发送失败错误
						for (SmsMultiSenderResult.Detail d : details) {
							System.out.print("errMsg:\t" + d.errMsg);
							System.out.print("phoneNumber:\t" + d.phoneNumber);
							System.out.println("fee:\t" + d.fee);
						}
					}
				}else{
					//发送失败
					resultVO.setCode(EConstants.CODE.FAILURE);
					resultVO.setMsg( result.errMsg);
				}
			} catch (Exception e) {
				resultVO.setCode(EConstants.CODE.FAILURE);
				resultVO.setMsg(  e.getMessage());
				e.printStackTrace();
				return resultVO;
			}
		}else{

			resultVO.setCode(EConstants.CODE.FAILURE);
			resultVO.setMsg("获取手机号失败");
		}
		return resultVO;
	}
	@Override
	public UserService getBaseService() {
		return this.userService;
	}

	//1
	@RequestMapping("/count")
	@ResponseBody
	public int count(){
		UserVO vo = new UserVO();
		return userService.doSearchCount(vo);
	}

	//2

	@RequestMapping("/findById")
	@ResponseBody
	public UserVO findById(){
		return userService.doFindById("1");
	}

	@RequestMapping("/test")
	public String list2(){
		return "test";
	}


	//3
	@RequestMapping("/findByVO")
	@ResponseBody
	public PageInfo<UserVO> findByVO(){
		UserVO vo = new UserVO();
		vo.setPageNum(1);
		vo.setPageSize(3);
		List<UserVO> list = userService.doSearchListByVO(vo);
//		PageHelper.startPage(2,2);
		PageInfo<UserVO> pageInfo = new PageInfo<>(list);
		return pageInfo;
	}
	@RequestMapping("/findByVO2")
	@ResponseBody
	public List<UserVO> findByVO2(){
		UserVO vo = new UserVO();
		vo.setPageNum(2);
		vo.setPageSize(3);
		List<UserVO> list = userService.doSearchListByVO(vo);
		return list;
	}

	@RequestMapping("/findByVO3")
	@ResponseBody
	public PageInfo<UserVO> findByVO3(){
		UserVO vo = new UserVO();
		PageHelper.startPage(1,1);
		List<UserVO> list = userService.doSearchListByVO(vo);
		PageInfo<UserVO> pageInfo = new PageInfo<UserVO>(list);
		return pageInfo;
	}

	//4
	@RequestMapping("/getPermissionsByUserid")
	@ResponseBody
	public List<String> insertByVO(){
		String userid = "F2B35A68CDEB401A91A593068FDFC3FE";
		List<RoleVO> roleList = roleService.doFindRoleByUserid(userid);
		List<ResourceVO> resourceList = resourceService.doFindResourceByRoleList(roleList);
		List<PermissionVO> permissionList = permissionService.doFindPermissionByResourceList(resourceList);
		List<String> list = new ArrayList<>();
		for(PermissionVO vo : permissionList){
			list.add(vo.getPermissionname());
		}
		return list;
	}

	//5
//	@RequestMapping("/insertByList")
//	public int insertByList(){
//		List<UserVO> list = new ArrayList<UserVO>();
//		UserVO vo1 = new UserVO();
//		vo1.setUsername("aa");
//		vo1.setPassword("1");
//		list.add(vo1);
//		UserVO vo2 = new UserVO();
//		vo2.setUsername("bb");
//		vo2.setPassword("1");
//		list.add(vo2);
//		return userService.doBatchInsertByList(list);
//	}

	//6
	@RequestMapping("/deleteById")
	@ResponseBody
	public int insertByMap(){
		return userService.doDeleteById("11111");
	}

	//7
//	@RequestMapping("updateByVO")
//	public int updateByVO(){
//		UserVO vo = userService.doFindById("1");
//		vo = userService.getPasswordEncry(vo);
//		UserVO vo2 = userService.doFindById("2");
//		vo2 = userService.getPasswordEncry(vo2);
//		userService.doUpdateByVO(vo2);
//		return userService.doUpdateByVO(vo);
//	}

	//8
//	@RequestMapping("/updateByList")
//	public int updateByList(){
//		List<UserVO> list = new ArrayList<UserVO>();
//		UserVO vo1 = new UserVO();
//		vo1.setPkid("2");
//		vo1.setUsername("lucy22");
//		vo1.setPassword("1");
//		list.add(vo1);
//		UserVO vo2 = new UserVO();
//		vo2.setPkid("3");
//		vo2.setUsername("smith33");
//		vo2.setPassword("1");
//		list.add(vo2);
//		return userService.doBatchUpdateByList(list);
//	}
	//9
//	@RequestMapping("doFindByVO")
//	public UserVO doFindByVO(){
//		UserVO userVO = new UserVO();
//		userVO.setUsername("aaa");
//		return userService.doFindByVO(userVO);
//	}
	//10
	@RequestMapping("getRoleByUserid")
	@ResponseBody
	public List<RoleVO> getUserRoleByVO(){
		return roleService.doFindRoleByUserid("1");
	}

	//11
//	@RequestMapping("getUsers")
//	@RequiresPermissions("user:list")
//	public List<UserVO> getUsers(){
//		UserVO vo = new UserVO();
//		AccountVO accountVO = new AccountVO();
//		accountVO.setUsername("lucy222");
//		vo.setAccountVO(accountVO);
//		List<UserVO> lists = userService.doFindUserRoles(vo);
//		return lists;
//	}

	//12
	@RequestMapping("insertUser")
	@ResponseBody
	public UserVO insertUser(){
		UserVO vo = new UserVO();
		vo.setUsername("111");
		vo.setPassword("1");
		vo.setRealname("系统管理员");

		List<RoleVO> roles = new ArrayList<RoleVO>();
		RoleVO role = new RoleVO();
		role.setRolename("normal");
		role.setRoleid("2");
		roles.add(role);

		RoleVO role1 = new RoleVO();
		role1.setRolename("admin");
		role1.setRoleid("1");
		roles.add(role1);
		vo.setRoles(roles);
		return userService.doInsertUserRoles(vo);
	}

	//13
	@RequestMapping("updateUser")
	@ResponseBody
	@RequiresPermissions("user:edit")
	public UserVO updateUser(){
		UserVO user = new UserVO();
		user.setPkid("3");
		UserVO vo = userService.doFindUserRoles(user).get(0);
		vo.setRealname("20180103");
		List<RoleVO> roles = new ArrayList<>();
		RoleVO role = new RoleVO();
		role.setRolename("normal");
		role.setRoleid("2");
		roles.add(role);
		vo.setRoles(roles);
		return userService.doUpdateUserRoles(vo);
	}

	//14
//	@RequestMapping("deleteUser")
//	@ResponseBody
//	@RequiresPermissions("user:delete")
//	public void deleteUser(){
//		userService.doDeleteUserRoles("3");
//	}

	//15
	@RequestMapping("getRoles")
	@ResponseBody
	@RequiresPermissions("role:list")
	public List<RoleVO> getRoles(){
		RoleVO vo = new RoleVO();
		vo.setRolename("admin");
		List<RoleVO> lists = roleService.doFindRoleResources(vo);
		return lists;
	}

	//16
	@RequestMapping("insertRole")
	@ResponseBody
	@RequiresPermissions("role:add")
	public RoleVO insertRole(){
		RoleVO vo = new RoleVO();
		vo.setRolename("角色3");

		List<ResourceVO> list = new ArrayList<>();
		ResourceVO rp = new ResourceVO();
		rp.setResourceid("1");
		list.add(rp);
		ResourceVO rp2 = new ResourceVO();
		rp2.setResourceid("2");
		list.add(rp2);
		vo.setResources(list);

		return roleService.doInsertRoleResources(vo);
	}

	//17
	@RequestMapping("updateRole")
	@ResponseBody
	@RequiresPermissions("role:edit")
	public RoleVO updateRole() throws Exception {
		RoleVO vo = new RoleVO();
		vo.setRoleid("A1AEACA6D7DD4131A8D7F9ADF1A5DAB0");
		vo.setRoleinfo("111weadsadadewq1");

		List<ResourceVO> list = new ArrayList<>();
		ResourceVO p1 = new ResourceVO();
		p1.setResourceid("1");
		list.add(p1);
		ResourceVO p2 = new ResourceVO();
		p2.setResourceid("2");
		p2.setReserve1(null);
		list.add(p2);
		vo.setResources(list);

		return roleService.doUpdateRoleResources(vo);
	}

	//18
	@RequestMapping("deleteRole")
	@ResponseBody
	@RequiresPermissions("role:delete")
	public void deleteRole(){
//		roleService.doDeleteRole("A1AEACA6D7DD4131A8D7F9ADF1A5DAB0");
	}

	//19
	@RequestMapping("getPermission")
	@ResponseBody
	@RequiresPermissions("permission:list")
	public List<PermissionVO> getPermission(){
		PermissionVO vo = new PermissionVO();
		vo.setPermissionid("1");
		return permissionService.doSearchListByVO(vo);
	}

	//20
	@RequestMapping("getPermissions")
	@ResponseBody
	@RequiresPermissions("permission:list")
	public List<PermissionVO> getPermissions(){
		return permissionService.doSearchListByVO(null);
	}

	//21
	@RequestMapping("insertPermission")
	@ResponseBody
	@RequiresPermissions("permission:add")
	public int insertPermission(){
		PermissionVO vo = new PermissionVO();
		vo.setPermissionname("test");
		return permissionService.doInsertByVO(vo);
	}

	//22
	@RequestMapping("updatePermission")
	@ResponseBody
	@RequiresPermissions("permission:edit")
	public int updatePermission(){
		PermissionVO vo = new PermissionVO();
		vo.setPermissioninfo("test");
		vo.setPermissionid("C642000235894C129742E44F2A7BEAE8");
		return permissionService.doUpdateByVO(vo);
	}

	//23
	@RequestMapping("deletePermission")
	@ResponseBody
	@RequiresPermissions("permission:delete")
	public int deletePermission(){
		return permissionService.doDeleteById("2E85A37D74314CBDAFA0AC0148F41BA1");
	}

	//24
	@RequestMapping("getResource")
	@ResponseBody
	@RequiresPermissions("resource:list")
	public List<ResourceVO> getResource(){
		ResourceVO vo = new ResourceVO();
		vo.setResourceid("2");
		return resourceService.doFindResourcePermissions(vo);
	}

	//25
	@RequestMapping("getResources")
	@ResponseBody
	@RequiresPermissions("resource:list")
	public List<ResourceVO> getResources(){
		return resourceService.doSearchListByVO(null);
	}

	//26
	@RequestMapping("insertResource")
	@ResponseBody
	@RequiresPermissions("resource:add")
	public ResourceVO insertResource(){
		ResourceVO vo = new ResourceVO();
		vo.setResourcename("/test/test");
		vo.setResourceinfo("权限管理");
		vo.setParentid("-1");
		vo.setSeqno(3);

		List<PermissionVO> permissions = new ArrayList<>();
		PermissionVO permission = new PermissionVO();
		permission.setPermissionid("1");
		permissions.add(permission);
		vo.setPermissions(permissions);

		return resourceService.doInsertResourcePermissions(vo);
	}

	//27
	@RequestMapping("updateResource")
	@ResponseBody
	@RequiresPermissions("resource:edit")
	public ResourceVO updateResource(){
		ResourceVO vo = new ResourceVO();
		vo.setResourceinfo("权限管理测试2");
		vo.setResourceid("5EB53C0711D343FA884992B3ABC68936");

		List<PermissionVO> permissions = new ArrayList<>();
		PermissionVO permission1 = new PermissionVO();
		permission1.setPermissionid("1");
		permissions.add(permission1);
		vo.setPermissions(permissions);

		PermissionVO permission2 = new PermissionVO();
		permission2.setPermissionid("3");
		permissions.add(permission2);

		return resourceService.doUpdateResourcePermissions(vo);
	}

	//28
	@RequestMapping("deleteResource")
	@ResponseBody
	@RequiresPermissions("resource:delete")
	public void deleteResource(){
		resourceService.doDeleteResourcePermissions("5EB53C0711D343FA884992B3ABC68936");
	}

	//29
	@RequestMapping("findType")
	@ResponseBody
	public List<CodelistVO> findType(){
		CodelistVO vo = new CodelistVO();
		vo.setCodetype("CA01");
		return codelistService.doSearchListByVO(vo);
	}

}
