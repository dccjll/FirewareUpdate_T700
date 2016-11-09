package com.dsm.t700.update;

import java.util.Date;

public class FirewareVersion {
	private Integer id;
	private String item2;
	private String appUrl;
	private String appName;
	private String item1;
	private Date appRefreshtime;
	private String remark;
	private String appState;
	private String appVersion;
	private String appType;

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getItem2() {
		return item2;
	}

	public void setItem2(String item2) {
		this.item2 = item2;
	}

	public String getAppUrl() {
		return appUrl;
	}

	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getItem1() {
		return item1;
	}

	public void setItem1(String item1) {
		this.item1 = item1;
	}

	public Date getAppRefreshtime() {
		return appRefreshtime;
	}

	public void setAppRefreshtime(Date appRefreshtime) {
		this.appRefreshtime = appRefreshtime;
	}

	public String getAppState() {
		return appState;
	}

	public void setAppState(String appState) {
		this.appState = appState;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	@Override
	public String toString() {
		return "FirewareVersion{" +
				"id=" + id +
				", item2='" + item2 + '\'' +
				", appUrl='" + appUrl + '\'' +
				", appName='" + appName + '\'' +
				", item1='" + item1 + '\'' +
				", appRefreshtime=" + appRefreshtime +
				", remark='" + remark + '\'' +
				", appState='" + appState + '\'' +
				", appVersion='" + appVersion + '\'' +
				", appType='" + appType + '\'' +
				'}';
	}
}
