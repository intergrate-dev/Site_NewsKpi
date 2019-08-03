package com.example.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.example.bean.EventTrackEntity;
import com.example.bean.OperationEntity;

public interface EventTrackDao {
	
	public int addEventTrack(EventTrackEntity et);
	
	public int updateEventTrack(EventTrackEntity et);
	
	public int deleteEventTrack(int eventId, int dataType, String taskExeTime);
	
	public int isExist(EventTrackEntity et);
	
	public int addEventIDs(OperationEntity  Entity);
	
	public List<Integer> queryEventDataById(int eventId, int dataType);
	
	public String queryEventIdsByConfig(String pageTypeID);
	
	public List<OperationEntity> queryEventIdsByMcode(String pageTypeID);
	
	public JSONObject queryReportRankByEventId(int eventId);

}
