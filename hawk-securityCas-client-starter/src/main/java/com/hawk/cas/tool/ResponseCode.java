package com.hawk.cas.tool;
public enum ResponseCode {

	OPERATION_SUCCESS("1", "操作成功"), OPERATION_FAIL("0", "操作失败");

	ResponseCode(String status, String msg) {
		this.status = status;
		this.msg = msg;
	}

	private String status;
	private String msg;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}