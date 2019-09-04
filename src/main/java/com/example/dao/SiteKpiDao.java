package com.example.dao;

import java.util.List;

import com.example.bean.OperationEntity;
import com.example.bean.SiteKpiEntity;

public interface SiteKpiDao {

	public int addSiteKpi(SiteKpiEntity siteKpi);
	
	public List<OperationEntity> selectSiteKpiId();
	
	public int updateSiteKpi( SiteKpiEntity siteKpi);
	
	public int isHave(String ST_ID, String time,int type);
}
