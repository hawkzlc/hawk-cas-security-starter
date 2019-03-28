package com.hawk.cas.client.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.hawk.cas.client.entity.User;
import com.hawk.cas.client.service.UserService;

/**
 * 
 * @author Zhoulc & Lucio Zhou
 * @date 2018年12月28日 上午11:23:10
 * @email 34405161@qq.com, 13811858856@163.com
 */
@SuppressWarnings("all")
@Component
public class UserDetailsServiceImpl implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {
	public static Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
	@Autowired
	private UserService userService;

	/**
	 * 实现AuthenticationUserDetailsService的方法， 用于获取cas
	 * server返回的用户信息，再根据用户关键信息加载出用户在当前系统的权限
	 * 
	 * @param token
	 * @return
	 * @throws UsernameNotFoundException
	 */
	@Override
	public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
		String name = token.getName();
//		logger.info("获得的用户名：{}", name);
//		logger.info("token：{}", token);
//		logger.info("token-getAssertion.getAttributes：{}", token.getAssertion().getAttributes());
//		logger.info("token-getAssertion.getPrincipal.getAttributes：{}",
//				token.getAssertion().getPrincipal().getAttributes());
		User user = userService.findByUsername(name);

		return user;
	}
}
