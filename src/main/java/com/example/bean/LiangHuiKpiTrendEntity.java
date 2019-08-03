package com.example.bean;


/**
 * 事件追踪实体类
 * @author binLee
 *
 */
public class LiangHuiKpiTrendEntity {
	
	private String keyWord;	
	private String datatype;
	private String sourcetype;	
	private String datatrend;
	private String sourcerank;	
	private String origrank;
	private String lastModified;
	private int deleteFlag;
	private int countType;
	
	
	public int getCountType() {
		return countType;
	}
	public void setCountType(int countType) {
		this.countType = countType;
	}
	public String getKeyWord() {
		return keyWord;
	}
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	public String getDatatype() {
		return datatype;
	}
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	public String getSourcetype() {
		return sourcetype;
	}
	public void setSourcetype(String sourcetype) {
		this.sourcetype = sourcetype;
	}
	public String getDatatrend() {
		return datatrend;
	}
	public void setDatatrend(String datatrend) {
		this.datatrend = datatrend;
	}
	public String getSourcerank() {
		return sourcerank;
	}
	public void setSourcerank(String sourcerank) {
		this.sourcerank = sourcerank;
	}
	public String getOrigrank() {
		return origrank;
	}
	public void setOrigrank(String origrank) {
		this.origrank = origrank;
	}
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	public int getDeleteFlag() {
		return deleteFlag;
	}
	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}  
	
}
