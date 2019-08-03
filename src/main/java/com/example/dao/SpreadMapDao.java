package com.example.dao;

import java.util.List;

import com.example.bean.OperationEntity;
import com.example.bean.SpreadMapEntity;

public interface SpreadMapDao {

	public int addSpreadMap(SpreadMapEntity spreadMap);
	
	public List<OperationEntity> selectSiteId(String pageTypeIds, String mediaId);
	
	public int delectSpreadMap(String mediaid,String SITE_ID);
	
	//public int isHave(String ST_ID, String time,int type);
}