package com.example.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.example.bean.OperationEntity;
import com.example.bean.SiteNewsEntity;

public interface SiteNewsDao {

	public int addSiteNews(SiteNewsEntity siteNews);
	
	public List<OperationEntity> selectSiteNewsId(String pageTypeIds, String mediaId);
	
	public int updateSiteNews( String id ,String siterm);
	
	public List<JSONObject> isHave(String SN_ID,String siterm,int isorginal);
	
	public List<JSONObject> getAreaNews(String pageTypeID);
	
	/**
	 * 查询站点旧数据
	 * @param siteID
	 * @param oriType
	 * @param taskExeTime
	 * @return
	 */
	public List<SiteNewsEntity> querySiteOldNews(String siteID, int oriType, String taskExeTime);
	
	/**
	 * 清除站点数据
	 * @param siteID   站点ID
	 * @param OriType  类型
	 * @param delNum   删除条数
	 * @return
	 */
	public int deleteNews(String siteID, int oriType, String taskExeTime, String ids);

    List<Integer> queryAllMedias();
}
