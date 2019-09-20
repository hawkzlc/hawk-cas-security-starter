package com.hawk.cas.tool;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;

public class ReturnData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// 响应业务状态码
	@ApiModelProperty(value = "返回报文中的操作结果状态码,1:成功,0:失败", example = "1")
	private String code;

	// 响应信息
	@ApiModelProperty(value = "返回报文中的操作结果描述信息", example = "操作成功")
	private String msg;

	// 响应数据
	@ApiModelProperty(value = "返回报文中的操作结果数据", example = "字符串结果集")
	private Object data;

	private ReturnData(String code, String msg, Object data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}

	public static ReturnData error(String code, String msg, Object data) {
		return new ReturnData(code, msg, data);
	}

	public static ReturnData error(String code, String msg) {
		return new ReturnData(code, msg, null);
	}

	public static ReturnData success(Object data) {
		return new ReturnData(ResponseCode.OPERATION_SUCCESS.getStatus(), ResponseCode.OPERATION_SUCCESS.getMsg(),
				data);
	}

	public static ReturnData success() {
		return new ReturnData(ResponseCode.OPERATION_SUCCESS.getStatus(), ResponseCode.OPERATION_SUCCESS.getMsg(),
				null);
	}

	public static ReturnData fail(Object data) {
		return new ReturnData(ResponseCode.OPERATION_FAIL.getStatus(), ResponseCode.OPERATION_FAIL.getMsg(), data);
	}

	public static ReturnData fail() {
		return new ReturnData(ResponseCode.OPERATION_FAIL.getStatus(), ResponseCode.OPERATION_FAIL.getMsg(), null);
	}

	public ReturnData() {
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "ReturnData{" + "status=" + code + ", msg='" + msg + '\'' + ", data=" + data + '}';
	}
}