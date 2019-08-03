package com.example.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="bigconf",ignoreUnknownFields = true)
@PropertySource("classpath:config/bigScreenConfig.properties")
public class BigScreenEntity {
	private String rooturl = "";
	private String APPID = "";
	private String APPSECRET = "";
	private String actUrl = "";
	private String actKey = "";
	private String actSecret = "";
	private String mediaId = "";
	
	public String getMediaId() {
		return mediaId;
	}
	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}
	public String getRooturl() {
		return rooturl;
	}
	public void setRooturl(String rooturl) {
		this.rooturl = rooturl;
	}
	public String getAPPID() {
		return APPID;
	}
	public void setAPPID(String aPPID) {
		APPID = aPPID;
	}
	public String getAPPSECRET() {
		return APPSECRET;
	}
	public void setAPPSECRET(String aPPSECRET) {
		APPSECRET = aPPSECRET;
	}
	public String getActUrl() {
		return actUrl;
	}
	public void setActUrl(String actUrl) {
		this.actUrl = actUrl;
	}
	public String getActKey() {
		return actKey;
	}
	public void setActKey(String actKey) {
		this.actKey = actKey;
	}
	public String getActSecret() {
		return actSecret;
	}
	public void setActSecret(String actSecret) {
		this.actSecret = actSecret;
	}
	
}
