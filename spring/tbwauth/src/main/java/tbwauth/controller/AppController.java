package tbwauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import tbwauth.model.AccessToken;
import tbwauth.model.UserInfoResponse;
import tbwauth.service.SsoService;

@Controller
public class AppController {
	@Autowired
	private SsoService ssoService;

	@RequestMapping(value="/loginForm", method=RequestMethod.GET)
	public String loginForm() throws Exception{
		return "loginForm";
	}	
	
//	@RequestMapping(value="/test", method=RequestMethod.GET)
//	public String test() throws Exception{
//		System.out.println("controller called");
//		return "test";
//	}	
//	
//	@RequestMapping(value="/test2", method=RequestMethod.GET)
//	@ResponseBody
//	public String test2() throws Exception{
//		System.out.println("controller called");
//		return "{\"aaa\":\"bbb\"}";
//	}	
	
	@RequestMapping(value="/userInfo", method=RequestMethod.POST)
	@ResponseBody
	public UserInfoResponse userInfoResponse(@RequestParam(value="token") String token,
		@RequestParam(value="clientId") String clientId) {	
		UserInfoResponse userInfoResponse = new UserInfoResponse();

		AccessToken accessToken = ssoService.getAccessToken(token, clientId);
		userInfoResponse.setUserName(accessToken.getUserName());
		
//		userInfoResponse.setResult(false);
//		userInfoResponse.setMessage("not used. use /oauth/check_token uri");
		return userInfoResponse;
	}
}
