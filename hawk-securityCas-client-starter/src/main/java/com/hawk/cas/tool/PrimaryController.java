package com.hawk.cas.tool;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 作为controller的基础类,将service层返回的对象封装为统一格式的json并输出到response.
 */

public class PrimaryController {

	public static Logger logger = LoggerFactory.getLogger(PrimaryController.class);

	protected String SUCCESS_CODE = "0001";
	protected String SUCCESS_MSG = "操作成功";

	/**
	 * 封装分页信息的内部类PageInfo
	 */

	/**
	 * 打印操作成功result信息给调用者
	 * 
	 * @param response
	 * @param objectName
	 * @param object
	 */
	public void printSuccess(HttpServletResponse response, String objectName, Object object) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(objectName, object);
		responseMap.put("code", SUCCESS_CODE);
		responseMap.put("message", SUCCESS_MSG);
		responseMap.put("result", result);
		printWriter(response, JSONObject.toJSONString(responseMap,SerializerFeature.WriteMapNullValue));
	}

	/**
	 * 打印操作成功result信息给调用者(缓存版本)
	 * 
	 * @param response
	 * @param objectName
	 * @param object
	 */
	public void printCacheSuccess(HttpServletResponse response, String objectName, Object object) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(objectName, object);
		responseMap.put("code", SUCCESS_CODE);
		responseMap.put("message", SUCCESS_MSG);
		responseMap.put("result", result);
		printCacheWriter(response, JSONObject.toJSONString(responseMap,SerializerFeature.WriteMapNullValue));
	}

	// public void printSuccessMap(HttpServletResponse response, Map<String,
	// String> resutlMap) {
	// Map<String, Object> responseMap = new HashMap<String, Object>();
	// responseMap.put("code", SUCCESS_CODE);
	// responseMap.put("message", SUCCESS_MSG);
	// responseMap.put("result", resutlMap);
	// Gson gson = new Gson();
	// printWriter(response, gson.toJson(responseMap));
	// }

	public void printSuccessMap(HttpServletResponse response, Map<String, Object> resutlMap) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("code", SUCCESS_CODE);
		responseMap.put("message", SUCCESS_MSG);
		responseMap.put("result", resutlMap);
		printWriter(response, JSONObject.toJSONString(responseMap,SerializerFeature.WriteMapNullValue));
	}

	/**
	 * (缓存版本)
	 * 
	 * @param response
	 * @param resutlMap
	 */
	public void printCacheSuccessMap(HttpServletResponse response, Map<String, Object> resutlMap) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("code", SUCCESS_CODE);
		responseMap.put("message", SUCCESS_MSG);
		responseMap.put("result", resutlMap);
		printCacheWriter(response, JSONObject.toJSONString(responseMap,SerializerFeature.WriteMapNullValue));
	}

	/**
	 * 打印处理失败信息给调用者,处理失败没有result对象
	 * 
	 * @param response
	 * @param errorCode
	 * @param errorMsg
	 */
	public void printError(HttpServletResponse response, String errorCode, String errorMsg) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("code", errorCode);
		responseMap.put("message", errorMsg);
		responseMap.put("result", "");
		printWriter(response, JSONObject.toJSONString(responseMap,SerializerFeature.WriteMapNullValue));
	}

	public void printError(HttpServletResponse response, String errorCode, String errorMsg,
			Map<String, String> result) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("code", errorCode);
		responseMap.put("message", errorMsg);
		responseMap.put("result", result);
		printWriter(response, JSONObject.toJSONString(responseMap,SerializerFeature.WriteMapNullValue));
	}

	/**
	 * 对外打印JSON数据
	 *
	 * @param response
	 * @param data
	 */
	protected void printWriter(HttpServletResponse response, String data) {
		setBasicResponseHeader(response);
		response.setContentType("application/json;charset=utf-8");
		try {
			write(response, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对外打印JSON数据(缓存版本)
	 *
	 * @param response
	 * @param data
	 */
	protected void printCacheWriter(HttpServletResponse response, String data) {
		setCacheResponseHeader(response);
		response.setContentType("application/json;charset=utf-8");
		try {
			write(response, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置不允许缓存的响应头
	 *
	 * @param response
	 */
	protected void setBasicResponseHeader(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "No-cache");
		response.setDateHeader("Expires", 0);
	}

	/**
	 * 设置允许缓存的响应头
	 *
	 * @param response
	 */
	protected void setCacheResponseHeader(HttpServletResponse response) {
		response.setHeader("Cache-Control", "max-age=300");
		// response.setHeader("Pragma", "No-cache");
		response.setDateHeader("Expires", (new Date()).getTime() + 300000);
	}

	/**
	 * 数据打印功能
	 *
	 * @param data
	 * @param response
	 * @throws IOException
	 */
	private void write(HttpServletResponse response, String data) throws IOException {
		PrintWriter printWriter = response.getWriter();
		printWriter.write(data);
		printWriter.flush();
		printWriter.close();
	}

}