package com.hawk.cas.client.security;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.hawk.cas.client.handler.AjaxAccessDeniedHandler;
import com.hawk.cas.tool.PrimaryController;

/**
 * Used by the <code>ExceptionTranslationFilter</code> to commence
 * authentication via the JA-SIG Central Authentication Service (CAS).
 * <p>
 * The user's browser will be redirected to the JA-SIG CAS enterprise-wide login
 * page. This page is specified by the <code>loginUrl</code> property. Once
 * login is complete, the CAS login page will redirect to the page indicated by
 * the <code>service</code> property. The <code>service</code> is a HTTP URL
 * belonging to the current application. The <code>service</code> URL is
 * monitored by the {@link CasAuthenticationFilter}, which will validate the CAS
 * login was successful.
 *
 * @author Ben Alex
 * @author Scott Battaglia
 * @author Zhoulc & Lucio Zhou
 * @date 2018年12月28日 上午11:23:10
 * @email 34405161@qq.com, 13811858856@163.com
 */
public class HawkCasAuthenticationEntryPoint implements AuthenticationEntryPoint, InitializingBean {
	public static Logger logger = LoggerFactory.getLogger(HawkCasAuthenticationEntryPoint.class);
	// ~ Instance fields
	// ================================================================================================
	private ServiceProperties serviceProperties;
	private static PrimaryController outputController = new PrimaryController();
	private String loginUrl;

	/**
	 * Determines whether the Service URL should include the session id for the
	 * specific user. As of CAS 3.0.5, the session id will automatically be
	 * stripped. However, older versions of CAS (i.e. CAS 2), do not automatically
	 * strip the session identifier (this is a bug on the part of the older server
	 * implementations), so an option to disable the session encoding is provided
	 * for backwards compatibility.
	 *
	 * By default, encoding is enabled.
	 */
	private boolean encodeServiceUrlWithSessionId = true;

	// ~ Methods
	// ========================================================================================================

	public void afterPropertiesSet() throws Exception {
		Assert.hasLength(this.loginUrl, "loginUrl must be specified");
		Assert.notNull(this.serviceProperties, "serviceProperties must be specified");
		Assert.notNull(this.serviceProperties.getService(), "serviceProperties.getService() cannot be null.");
	}

	@Override
	public final void commence(final HttpServletRequest servletRequest, final HttpServletResponse response,
			final AuthenticationException authenticationException) throws IOException, ServletException {

		final String urlEncodedService = createServiceUrl(servletRequest, response);
		final String redirectUrl = createRedirectUrl(urlEncodedService);
		final boolean isAjax = AjaxAccessDeniedHandler.isAjax(servletRequest);
//		Enumeration headerNames = servletRequest.getHeaderNames();
//		while (headerNames.hasMoreElements()) {
//	        String key = (String) headerNames.nextElement();
//	        String value = servletRequest.getHeader(key);
//	        logger.info("请求中携带的header内容--{}:{}", key,value);
//	    }
//		logger.info("当前request是否为Ajax的判断为:{}", isAjax);
		if (isAjax) {

			Map<String, String> resultMap = new HashMap<>();

			resultMap.put("redirectUrl", redirectUrl);
			outputController.printError(response, "0000", "您尚未登录或当前的会话已经过期,将自动跳转到登录系统进行验证", resultMap);
			return;
		}

		response.sendRedirect(redirectUrl);
	}

	/**
	 * Constructs a new Service Url. The default implementation relies on the CAS
	 * client to do the bulk of the work.
	 * 
	 * @param request  the HttpServletRequest
	 * @param response the HttpServlet Response
	 * @return the constructed service url. CANNOT be NULL.
	 */
	@SuppressWarnings("deprecation")
	protected String createServiceUrl(final HttpServletRequest request, final HttpServletResponse response) {
		return CommonUtils.constructServiceUrl(request, response, this.serviceProperties.getService(), null,
				this.serviceProperties.getArtifactParameter(), this.encodeServiceUrlWithSessionId);
	}

	/**
	 * Constructs the Url for Redirection to the CAS server. Default implementation
	 * relies on the CAS client to do the bulk of the work.
	 *
	 * @param serviceUrl the service url that should be included.
	 * @return the redirect url. CANNOT be NULL.
	 */
	protected String createRedirectUrl(final String serviceUrl) {
		return CommonUtils.constructRedirectUrl(this.loginUrl, this.serviceProperties.getServiceParameter(), serviceUrl,
				this.serviceProperties.isSendRenew(), false);
	}

	/**
	 * The enterprise-wide CAS login URL. Usually something like
	 * <code>https://www.mycompany.com/cas/login</code>.
	 *
	 * @return the enterprise-wide CAS login URL
	 */
	public final String getLoginUrl() {
		return this.loginUrl;
	}

	public final ServiceProperties getServiceProperties() {
		return this.serviceProperties;
	}

	public final void setLoginUrl(final String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public final void setServiceProperties(final ServiceProperties serviceProperties) {
		this.serviceProperties = serviceProperties;
	}

	/**
	 * Sets whether to encode the service url with the session id or not.
	 *
	 * @param encodeServiceUrlWithSessionId whether to encode the service url with
	 *                                      the session id or not.
	 */
	public final void setEncodeServiceUrlWithSessionId(final boolean encodeServiceUrlWithSessionId) {
		this.encodeServiceUrlWithSessionId = encodeServiceUrlWithSessionId;
	}

	/**
	 * Sets whether to encode the service url with the session id or not.
	 * 
	 * @return whether to encode the service url with the session id or not.
	 *
	 */
	protected boolean getEncodeServiceUrlWithSessionId() {
		return this.encodeServiceUrlWithSessionId;
	}

}
