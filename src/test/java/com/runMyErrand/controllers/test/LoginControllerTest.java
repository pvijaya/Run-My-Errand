package com.runMyErrand.controllers.test;
import static org.junit.Assert.assertEquals;

import org.junit.runner.RunWith;
import org.junit.Assert;
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mysql.jdbc.AssertionFailedException;
import com.runMyErrand.controllers.DashboardController;
import com.runMyErrand.controllers.LoginController;
import com.runMyErrand.model.Login;
import com.runMyErrand.model.UserInfo;
import com.runMyErrand.services.UserServices;
@RunWith(MockitoJUnitRunner.class)
public class LoginControllerTest {
	@InjectMocks
	static
	LoginController logincon;
	@Mock
	UserServices userserv;
	static LoginController stLoginController=null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		stLoginController=new LoginController();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		stLoginController=null;
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLogin() {
		//
		System.out.println("inside test case 1");
		assert stLoginController.login()!=null:"The Object is not null";
		ModelAndView mav =logincon.login();
		assert mav!=null :"returning object passes";
		
	}
	
	@Test
	public void testsignup()
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = auth.getName();
		UserInfo user = (UserInfo)UserServices.selectUser(username);
		System.out.println("inside test signup ");
		ModelAndView model = new ModelAndView("signin");
		assertEquals("shah_tejas92@yahoo.co.in","abcd");
	}
		/*ModelAndView model = new ModelAndView("signin");
		String success = UserServices.addUser(user, "aa");
		when(userserv.addUser(user, null)).thenReturn(model);
				
	}
	
	public void testloginfailed()
	{
		ModelAndView model = new ModelAndView("signin");
		model.addObject("error", "Invalid Username Or Password");
		assert model!=null :"checking for login null";
		
	}*/
}
