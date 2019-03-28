package com.hawk.cas.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * 
 * @author Zhoulc & Lucio Zhou
 * @date 2018年12月28日 上午11:23:10
 * @email 34405161@qq.com, 13811858856@163.com
 */
@ConfigurationProperties
public class CasProperties {
	@Value("${cas.server.host.url}")
	private String casServerUrl;

	@Value("${cas.server.host.login_url}")
	private String casServerLoginUrl;

	@Value("${cas.server.host.logout_url}")
	private String casServerLogoutUrl;

	@Value("${app.server.host.url}")
	private String appServerUrl;

	@Value("${app.login.url}")
	private String appLoginUrl;

	@Value("${app.logout.url}")
	private String appLogoutUrl;

	@Value("${app.security.ignore}")
	private String[] urlIgnore;

	public String[] getUrlIgnore() {
		return urlIgnore;
	}

	public void setUrlIgnore(String urlIgnore) {
		String[] temp = urlIgnore.split(",");
		if (temp.length < 1) {
			String[] defaultUrls = { "/static/**", "/**/*.js", "/**/*.css" };
			this.urlIgnore = defaultUrls;
		} else {
			this.urlIgnore = temp;
		}
	}

	public String getCasServerUrl() {
		return casServerUrl;
	}

	public void setCasServerUrl(String casServerUrl) {
		this.casServerUrl = casServerUrl;
	}

	public String getCasServerLoginUrl() {
		return casServerLoginUrl;
	}

	public void setCasServerLoginUrl(String casServerLoginUrl) {
		this.casServerLoginUrl = casServerLoginUrl;
	}

	public String getCasServerLogoutUrl() {
		return casServerLogoutUrl;
	}

	public void setCasServerLogoutUrl(String casServerLogoutUrl) {
		this.casServerLogoutUrl = casServerLogoutUrl;
	}

	public String getAppServerUrl() {
		return appServerUrl;
	}

	public void setAppServerUrl(String appServerUrl) {
		this.appServerUrl = appServerUrl;
	}

	public String getAppLoginUrl() {
		return appLoginUrl;
	}

	public void setAppLoginUrl(String appLoginUrl) {
		this.appLoginUrl = appLoginUrl;
	}

	public String getAppLogoutUrl() {
		return appLogoutUrl;
	}

	public void setAppLogoutUrl(String appLogoutUrl) {
		this.appLogoutUrl = appLogoutUrl;
	}
}
