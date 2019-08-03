package com.example.dao;

import java.util.List;

import com.example.bean.OperationEntity;

public interface KeyWordKpiDao {

	public int addKeyWordKpi(String keyword,String mediaId,int forwardCount ,int centerCount,int provinceCount,int countyCount,int count);	
	public int updateKeyWordKpi(String keyword,String mediaId,int forwardCount ,int centerCount,int provinceCount,int countyCount,int count);	
	public int isHave(String keyword,String mediaId);
	
	public List<OperationEntity> selectKeyWordKpiId(String pTypeIds, String mediaId);
	
}
