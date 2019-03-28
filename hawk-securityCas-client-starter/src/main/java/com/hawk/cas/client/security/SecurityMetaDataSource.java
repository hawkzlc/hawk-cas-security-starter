package com.hawk.cas.client.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import com.hawk.cas.client.entity.Resource;
import com.hawk.cas.client.service.ResourceService;

import javax.annotation.PostConstruct;
import java.util.*;
/**
 * 
 * @author Zhoulc & Lucio Zhou
 * @date 2018年12月28日 上午11:23:10
 * @email 34405161@qq.com, 13811858856@163.com
 */
@Component
public class SecurityMetaDataSource implements FilterInvocationSecurityMetadataSource {

	public static Logger logger = LoggerFactory.getLogger(SecurityMetaDataSource.class);
	// 为了避免与url中的字符串冲突,采用uuid伪动态生成分隔符
	public static final String RESOURCE_SEPARATOR = UUID.randomUUID().toString();
	// 当resource中定义的method不在标准的http方法中时,指定默认的请求方法
	public static final String DEFAULT_METHOD = "GET";

	@Autowired
	private ResourceService resourceService;

	private LinkedHashMap<String, Collection<ConfigAttribute>> metaData;

	@PostConstruct
	private void loadSecurityMetaData() {
		List<Resource> list = resourceService.getAll();
		metaData = new LinkedHashMap<>();
		if (null == list) {
			return;
		}
		for (Resource resource : list) {
			List<ConfigAttribute> attributes = new ArrayList<>();
			if (resource.getRoles().size() > 0) {
				for (String role : resource.getRoles()) {
					attributes.add(new SecurityConfig(role));
				}
			}
			// 此处将resource的url和method进行拼装,适用于restful接口的权限控制
			metaData.put(resource.getUrl() + RESOURCE_SEPARATOR + resource.getResMethod(), attributes);
		}
	}

	// 1.从数据库(或其他存储中)获取全部资源(url)的<url,Collection<ConfigAttribute>>的完整集合
	// 2.当用户登录后并访问相应url时,获取该用户的全部可用权限(角色)
	// 3.从步骤1的完整列表中检索到当前访问url对应的权限(角色)集,然后用步骤2中获取到的用户权限进行匹配
	// 4.将匹配结果打包Collection<ConfigAttribute>,供org.springframework.security.access.vote.RoleVoter类的vote()方法使用
	// 5.created on
	// 2018-10-24,新增功能:对所有未匹配到权限配置的url.都配置默认角色AUTH_ADMIN_Hawkzlc,这样可实现2个目的,一个是避免出现功能权限遗漏配置导致权限泄露,
	// 另一个是对顶级管理员可直接放开新增的功能点访问权限
	@Override
	public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
		FilterInvocation invocation = (FilterInvocation) object;
		if (metaData == null) {
			return new ArrayList<>(0);
		}
		// String requestUrl = invocation.getRequestUrl();
		// logger.info("请求Url：{}", requestUrl);
		Iterator<Map.Entry<String, Collection<ConfigAttribute>>> iterator = metaData.entrySet().iterator();
		Collection<ConfigAttribute> rs = new ArrayList<>();
		while (iterator.hasNext()) {
			Map.Entry<String, Collection<ConfigAttribute>> next = iterator.next();
			String resourceKey = next.getKey();
			String[] strings = resourceKey.split(RESOURCE_SEPARATOR);
			Collection<ConfigAttribute> value = next.getValue();
			RequestMatcher requestMatcher = new AntPathRequestMatcher(strings[0],
					HttpMethod.resolve(strings[1]) == null ? DEFAULT_METHOD : strings[1]);
			if (requestMatcher.matches(invocation.getRequest())) {
				rs = value;
				break;
			}
		}
		if (rs.size() == 0) {

			rs.add(new SecurityConfig("AUTH_ADMIN_Hawkzlc"));

		}
		logger.info("拦截认证权限为：{}", rs);
		return rs;
	}

	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		logger.info("invoke getAllConfigAttributes ");
		loadSecurityMetaData();
		logger.info("初始化元数据");
		Collection<Collection<ConfigAttribute>> values = metaData.values();
		Collection<ConfigAttribute> all = new ArrayList<>();
		for (Collection<ConfigAttribute> each : values) {
			each.forEach(configAttribute -> {
				all.add(configAttribute);
			});
		}
		return all;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}
}
