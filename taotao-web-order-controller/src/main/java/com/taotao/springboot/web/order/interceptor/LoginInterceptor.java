package com.taotao.springboot.web.order.interceptor;

import com.taotao.springboot.sso.domain.pojo.TbUser;
import com.taotao.springboot.sso.domain.result.TaotaoResult;
import com.taotao.springboot.sso.export.UserResource;
import com.taotao.springboot.web.order.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 判断用户是否登录拦截器
 * <p>Title: LoginInterceptor</p>
 * <p>Description: </p>
 * <p>Company: www.itcast.cn</p> 
 * @version 1.0
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
	
	@Value("${TOKEN_KEY}")
	private String TOKEN_KEY;

	@Value("${SSO_URL}")
	private String SSO_URL;
	
	@Autowired
	private UserResource userResource;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 在Handler前，先执行此方法
		// #1 从Cookie中，获取TOKEN令牌信息
		String token = CookieUtils.getCookieValue(request, TOKEN_KEY);
		// #2 若TOKEN令牌为空，则跳转至sso登录页面
		if (StringUtils.isBlank(token)) {
			String requestURL = request.getRequestURL().toString();
			// 注意：将当前请求的url作为参数传递，sso登录成功后跳转回请求的页面
			response.sendRedirect(SSO_URL + "/page/login?url=" + requestURL);
			return false;
		}
		// #3 若成功获取TOKEN令牌，则调用sso系统服务判断用户是否登录
		TaotaoResult taotaoResult = userResource.getUserByToken(token);
		// #4 若用户未登录，则跳转至sso登录页面
		if (taotaoResult.getStatus() != 200) {
			String requestURL = request.getRequestURL().toString();
			response.sendRedirect(SSO_URL + "/page/login?url=" + requestURL);
			return false;
		}
		// #5 若用户已登录，则放行，并将用户信息放入Request
		TbUser user = (TbUser) taotaoResult.getData();
		request.setAttribute("user", user);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// 在Handler执行后，ModelAndView返回前

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// 在ModelAndView返回后，常用于清理资源
	}

}
