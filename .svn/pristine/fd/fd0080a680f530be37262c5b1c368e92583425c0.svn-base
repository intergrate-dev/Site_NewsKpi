package com.example.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.example.bean.LiangHuiKpiTrendEntity;
import com.example.bean.LiangHuiNewsEntity;
import com.example.bean.OperationEntity;

public interface LiangHuiNewsDao {
	
	public int addLiangHuiNews(LiangHuiNewsEntity et);
	
	//public int updateLiangHuiNews(LiangHuiNewsEntity et);
	
	//public int deleteLiangHuiNews(int eventId, int dataType, String taskExeTime);
	
	public int isExist(LiangHuiNewsEntity et);
	
	public List<OperationEntity> queryLiangHuiByConfig(int pageId);
	
	public List<String> queryChannelId();
	
	public int addLiangHuiKpiTrend(LiangHuiKpiTrendEntity et );
	public int isExistKpi(LiangHuiKpiTrendEntity et);
	public int updateLHKpi(LiangHuiKpiTrendEntity et);
}
