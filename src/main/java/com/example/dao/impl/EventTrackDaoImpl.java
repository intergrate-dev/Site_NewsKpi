package com.example.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.example.bean.EventTrackEntity;
import com.example.bean.OperationEntity;
import com.example.dao.EventTrackDao;

/**
 * 事件追踪的数据库访问实现类
 * @author binLee
 *
 */
@Repository
public class EventTrackDaoImpl implements EventTrackDao {

	private final Logger log = LoggerFactory.getLogger(EventTrackDaoImpl.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * 事件追踪表中新增数据
	 * return 更新的数据行数
	 */
	@Override
	public int addEventTrack(EventTrackEntity et) {
		
		String sql = "INSERT INTO bs_event_tracing(" 
				   + "eventId, et_dataType, et_deleteFlag, et_lastModified,"
				   + "et_content, et_count, et_clusterTime,"
				   + "et_emotionAns, et_channelType, et_reportTrend, et_mediaReportsRank, et_apiLastModified) "
				   + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			return jdbcTemplate.update(sql, 
					et.getEventId(), et.getDataType(), et.getDeleteFlag(), et.getLastModifiedTime(),
					et.getContent(), et.getCount(), et.getClusterTime(),
					et.getEmotionAnalysis(), et.getChannelType(), et.getReportTrend(),
					et.getReportsRank(), et.getApiLastModifiedTime());
		} catch (InvalidResultSetAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，无效的结果集合访问异常",e);
			return 0;
		} catch (DataAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，数据访问异常",e);
			return 0;			
		}
	}

	/**
	 * 事件追踪表中更新数据
	 * return 更新的数据行数
	 */
	@Override
	public int updateEventTrack(EventTrackEntity et) {
		String sql = "UPDATE bs_event_tracing SET " 
				   + "et_lastModified = ?, et_content = ?, et_count = ?, et_clusterTime = ? "
				   + "et_emotionAns = ?, et_channelType = ?, et_reportTrend = ?, et_apiLastModified = ? "
				   + "WHERE eventId=? AND et_dataType=? AND et_deleteFlag=0";
		try {
			return jdbcTemplate.update(sql, 
					et.getLastModifiedTime(), et.getContent(), et.getCount(), et.getClusterTime(),
					et.getEmotionAnalysis(), et.getChannelType(), et.getReportTrend(), et.getApiLastModifiedTime(),
					et.getEventId(), et.getDataType());
			
		} catch (InvalidResultSetAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，无效的结果集合访问异常",e);
			return 0;
		} catch (DataAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，数据访问异常",e);
			return 0;			
		}
	}

	/**
	 * 查询某条数据是否存在
	 * return 查询的数据行数
	 */
	@Override
	public int isExist(EventTrackEntity et) {
		
		String sql = "SELECT COUNT(*) FROM bs_event_tracing WHERE "
				   + "eventId=? AND et_dataType=? AND et_deleteFlag=0";
		
		try {
			int resNum = jdbcTemplate.queryForObject(sql, 
				new Object[]{et.getEventId(), et.getDataType()}, Integer.class);
			return resNum;
			
		} catch (InvalidResultSetAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，无效的结果集合访问异常",e);
			return -1;
		} catch (DataAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，数据访问异常",e);
			return -1;			
		}
	}

	/**
	 * 通过事件ID查询该接口类型下的数据ID列表
	 */
	@Override
	public List<Integer> queryEventDataById(int eventId, int dataType) {
		
		String sql = "SELECT et_contentId FROM bs_event_tracing WHERE "
				   + "eventId=? AND et_dataType=? AND et_deleteFlag=0";
		
		List<Integer> conIds = new ArrayList<Integer>();
		try {
			List<Map<String,Object>> rs = new ArrayList<Map<String,Object>>();
			rs = jdbcTemplate.queryForList(sql, eventId, dataType);
			Map<String, Object> map = null;
			if(rs.size()>0){
				for(int i=0; i<rs.size();i++){
					map = rs.get(i);
					if(null != map.get("et_contentId")){
						conIds.add((int)map.get("et_contentId"));
					}
				}
			}
			return conIds;
			
		} catch (InvalidResultSetAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，无效的结果集合访问异常",e);
			return conIds;
		} catch (DataAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，数据访问异常",e);
			return conIds;			
		}
	}

	@Override
	public int deleteEventTrack(int eventId, int dataType, String taskExeTime) {
		String sql = "DELETE FROM bs_event_tracing WHERE "
				   + "eventId=? AND et_dataType=? AND et_lastModified<?";
		try {
			return jdbcTemplate.update(sql, eventId, dataType, taskExeTime);
			
		} catch (InvalidResultSetAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，无效的结果集合访问异常",e);
			return -1;
		} catch (DataAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，数据访问异常",e);
			return -1;			
		}
	}
	@Override
	public int addEventIDs(OperationEntity Entity) {

		String sql = "UPDATE ucoperation SET " 
				   + "extfileds = ? "
				   + "WHERE pageTypeID=? AND MEDIA_ID=?";
		try {
			return jdbcTemplate.update(sql,Entity.getExtfileds(),Entity.getPageTypeID(),Entity.getMEDIA_ID());
			
		} catch (InvalidResultSetAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，无效的结果集合访问异常",e);
			return 0;
		} catch (DataAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，数据访问异常",e);
			return 0;			
		}
	
	}
	/**
	 * 通过配置表中的页面类型查询所有需要同步数据的事件id
	 */
	@Override
	public String queryEventIdsByConfig(String pageID) {
		String sql = "SELECT Configure,pageTypeID,extfileds,MEDIA_ID  FROM ucoperation WHERE "
				   + "SYS_DELETEFLAG=0 AND pageTypeID";
		if(pageID.contains(",")){
			sql = sql + "  in (?) ;";
		}else{
			sql = sql + " = ?;";
		}
		
		String eventIds = "";
		try {
			List<Map<String,Object>> rs = new ArrayList<Map<String,Object>>();
			rs = jdbcTemplate.queryForList(sql, pageID);
			Map<String, Object> map = null;
			if(rs.size()>0){
				for(int i=0; i<rs.size();i++){
					map = rs.get(i);
					String pageTypeID = String.valueOf(map.get("pageTypeID"));
					if("80".equals(pageTypeID)){
						String mcode = String.valueOf(map.get("Configure"));
						if(null!=mcode){
						  //String evenid = 
						}
					}else{
						if(null!=map.get("Configure")){
							eventIds += String.valueOf(map.get("Configure")) + ",";
						}
					}
					}
					
			}
			return eventIds;			
		} catch (InvalidResultSetAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，无效的结果集合访问异常",e);
			return eventIds;
		} catch (DataAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，数据访问异常",e);
			return eventIds;			
		}
	}
	@Override
	public List<OperationEntity> queryEventIdsByMcode(String pageID) {
		String sql = "SELECT Configure,pageTypeID,extfileds,MEDIA_ID  FROM ucoperation WHERE "
				   + "SYS_DELETEFLAG=0 AND pageTypeID";
		if(pageID.contains(",")){
			sql = sql + "  in ("+pageID+") ;";
		}else{
			sql = sql + " = "+pageID+";";
		}
		List<OperationEntity> list = null;
		try{	//获取微信站点传播分析数据的相关页面配置
				 list = jdbcTemplate.query(sql, new Object[]{}, new RowMapper<OperationEntity>() {
		             
		            @Override
		            public OperationEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
		            	OperationEntity stu = new OperationEntity();
		            	stu.setPageTypeID(rs.getString("pageTypeID"));
		            	stu.setExtfileds(rs.getString("extfileds"));
		                stu.setConfigure(rs.getString("Configure"));
		                stu.setMEDIA_ID(rs.getString("MEDIA_ID"));
		                return stu;
		            }
		        });
				
		}catch (InvalidResultSetAccessException e){
			log.error("数据库错误",e);
		    return null;
		}catch (DataAccessException e){
			log.error("数据库错误",e);
			 return null;
		}
		return list;
	}
	@Override
	public JSONObject queryReportRankByEventId(int eventId) {
		
		String sql = "SELECT et_mediaReportsRank FROM bs_event_tracing WHERE "
				   + "eventId=? AND et_dataType=7 AND et_deleteFlag=0 ORDER BY id desc";
		
		JSONObject rankJson = new JSONObject();
		try {
			List<Map<String,Object>> rs = new ArrayList<Map<String,Object>>();
			rs = jdbcTemplate.queryForList(sql, eventId);
			Map<String, Object> map = null;
			if(rs.size()>0){
				for(int i=0; i<rs.size();i++){
					map = rs.get(i);
					if(null != map.get("et_mediaReportsRank")){
						String rankResult = (String)map.get("et_mediaReportsRank");
						if(!StringUtils.isBlank(rankResult)){
							rankJson = JSONObject.parseObject(rankResult);
							if(null!=rankJson && rankJson.size()>0){
								break;
							}
						}
					}
				}
			}
			return rankJson;
			
		} catch (InvalidResultSetAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，无效的结果集合访问异常",e);
			return rankJson;
		} catch (DataAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，数据访问异常",e);
			return rankJson;			
		}
	}


}
