package com.hawk.cas.client.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import com.hawk.cas.client.handler.AjaxAccessDeniedHandler;
import com.hawk.cas.client.security.HawkCasAuthenticationEntryPoint;
import com.hawk.cas.client.security.LoginSuccessHandler;
import com.hawk.cas.client.security.SecurityMetaDataSource;
import com.hawk.cas.client.security.UserDetailsServiceImpl;

/**
 * 
 * @author Zhoulc & Lucio Zhou
 * @date 2018年12月28日 上午11:23:10
 * @email 34405161@qq.com, 13811858856@163.com
 */
@Configuration
@EnableConfigurationProperties(CasProperties.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	Logger log = LoggerFactory.getLogger(SecurityConfig.class);
	@Resource
	private CasProperties casProperties;
	@Autowired
	private SecurityMetaDataSource securityMetaDataSource;
	@Autowired
	AjaxAccessDeniedHandler ajaxAccessDeniedHandler;

	@Override
	public void configure(WebSecurity web) throws Exception {
		log.info("casProperties.getUrlIgnore: {}", Arrays.toString(casProperties.getUrlIgnore()));
		web.ignoring().antMatchers(casProperties.getUrlIgnore());
	}

	/** 定义认证用户信息获取来源，密码校验规则等 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		super.configure(auth);
		auth.authenticationProvider(casAuthenticationProvider());
	}

	/** 定义安全策略 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()// 配置安全策略
//				.antMatchers("/", "/hello").permitAll()// 定义/请求不需要验证
				.anyRequest().authenticated()// 其余的所有请求都需要验证
				.and().logout().permitAll()// 定义logout不需要验证
				.and().formLogin().successHandler(loginSuccessHandler());// 使用form表单登录,此处与cas整合,登录交由cas-server来实现,并完成回调处理

		http.exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint()).and()
				.addFilter(casAuthenticationFilter()).addFilterBefore(casLogoutFilter(), LogoutFilter.class)
				.addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class);
		http.exceptionHandling().accessDeniedHandler(ajaxAccessDeniedHandler); // 无权访问 JSON 格式的数据
		http.csrf().disable(); // 禁用CSRF,如果采用thymeleaf渲染页面的方式则可以开启csrf防御功能,否则在前后端分离的开发模式下建议关闭csrf
		/**
		 * FilterSecurityInterceptor本身属于过滤器，不能在外面定义为@Bean，
		 * 如果定义在外面，则这个过滤器会被独立加载到webContext中，导致请求会一直被这个过滤器拦截
		 * 加入到Springsecurity的过滤器链中，才会使它完整的生效
		 */
		http.addFilterBefore(filterSecurityInterceptor(), FilterSecurityInterceptor.class);

	}

	@Bean
	public LoginSuccessHandler loginSuccessHandler() {
		return new LoginSuccessHandler();
	}

	/**
	 * 注意：这里不能加@Bean注解
	 * 
	 * @return
	 * @throws Exception
	 */
//    @Bean
	public FilterSecurityInterceptor filterSecurityInterceptor() throws Exception {
		FilterSecurityInterceptor filterSecurityInterceptor = new FilterSecurityInterceptor();
		filterSecurityInterceptor.setSecurityMetadataSource(securityMetaDataSource);
		filterSecurityInterceptor.setAuthenticationManager(authenticationManager());
		filterSecurityInterceptor.setAccessDecisionManager(affirmativeBased());
		return filterSecurityInterceptor;
	}

	/**
	 * 认证的入口,改用自定义class:HawkCasAuthenticationEntryPoint,以实现对会话超时/首次访问等ajax请求返回json格式数据,而非302重定向
	 */
	@Bean
	public HawkCasAuthenticationEntryPoint casAuthenticationEntryPoint() {
		HawkCasAuthenticationEntryPoint casAuthenticationEntryPoint = new HawkCasAuthenticationEntryPoint();
		casAuthenticationEntryPoint.setLoginUrl(casProperties.getCasServerLoginUrl());
		casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
		return casAuthenticationEntryPoint;
	}

	/** 指定service相关信息 */
	@Bean
	public ServiceProperties serviceProperties() {
		ServiceProperties serviceProperties = new ServiceProperties();
		serviceProperties.setService(casProperties.getAppServerUrl() + casProperties.getAppLoginUrl());
		serviceProperties.setAuthenticateAllArtifacts(true);
		return serviceProperties;
	}

	/** CAS认证过滤器 */
	@Bean
	public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
//		casAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
		casAuthenticationFilter.setAuthenticationManager(authenticationManager());
		casAuthenticationFilter.setFilterProcessesUrl(casProperties.getAppLoginUrl());
		return casAuthenticationFilter;
	}

	/** cas 认证 Provider */
	@Bean
	public CasAuthenticationProvider casAuthenticationProvider() {
		CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
		casAuthenticationProvider.setAuthenticationUserDetailsService(customUserDetailsService());
		// casAuthenticationProvider.setUserDetailsService(customUserDetailsService());
		// //这里只是接口类型，实现的接口不一样，都可以的。
		casAuthenticationProvider.setServiceProperties(serviceProperties());
		casAuthenticationProvider.setTicketValidator(cas30ServiceTicketValidator());
		casAuthenticationProvider.setKey("casAuthenticationProviderKey");
		return casAuthenticationProvider;
	}

	/*
	 * @Bean public UserDetailsService customUserDetailsService(){ return new
	 * CustomUserDetailsService(); }
	 */

	/** 用户自定义的AuthenticationUserDetailsService */
	@Bean
	public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> customUserDetailsService() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	public Cas30ServiceTicketValidator cas30ServiceTicketValidator() {
		return new Cas30ServiceTicketValidator(casProperties.getCasServerUrl());
	}

	/** 单点登出过滤器 */
	@Bean
	public SingleSignOutFilter singleSignOutFilter() {
		SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
		singleSignOutFilter.setCasServerUrlPrefix(casProperties.getCasServerUrl());
		singleSignOutFilter.setIgnoreInitConfiguration(true);
		return singleSignOutFilter;
	}

	/** 请求单点退出过滤器 */
	@Bean
	public LogoutFilter casLogoutFilter() {
		LogoutFilter logoutFilter = new LogoutFilter(casProperties.getCasServerLogoutUrl(),
				new SecurityContextLogoutHandler());
		logoutFilter.setFilterProcessesUrl(casProperties.getAppLogoutUrl());
		return logoutFilter;
	}

	/**
	 * 定义决策管理器，这里可直接使用内置的AffirmativeBased选举器，
	 * 如果需要，可自定义，继承AbstractAccessDecisionManager，实现decide方法即可
	 * 
	 * @return
	 */
	@Bean
	public AccessDecisionManager affirmativeBased() {
		List<AccessDecisionVoter<? extends Object>> voters = new ArrayList<>();
		voters.add(roleVoter());
		log.info("正在创建决策管理器");
		return new AffirmativeBased(voters);
	}

	/**
	 * 定义选举器
	 * 
	 * @return
	 */
	@Bean
	public RoleVoter roleVoter() {
		// 这里使用角色选举器
		RoleVoter voter = new RoleVoter();
		log.info("正在创建选举器");
		voter.setRolePrefix("AUTH_");
		log.info("已将角色选举器的前缀修改为AUTH_");
		return voter;
	}
}
