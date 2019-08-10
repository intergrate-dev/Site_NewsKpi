package com.example.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.example.bean.LiangHuiKpiTrendEntity;
import com.example.bean.LiangHuiNewsEntity;
import com.example.bean.OperationEntity;
import com.example.dao.LiangHuiNewsDao;

/**
 * 事件追踪的数据库访问实现类
 * @author binLee
 *
 */
@Repository
public class LiangHuiNewsDaoImpl implements  LiangHuiNewsDao{
	private final Logger log = LoggerFactory.getLogger(SiteNewsDaoImpl.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	

	@Override
	public int addLiangHuiNews(LiangHuiNewsEntity et) {
		try {
			return jdbcTemplate
					.update("insert into bs_lianghhuinews("
							+ "tid,title,pubdate,source,channel,author,dataType,content,location,keyWord,siteId,lastModified)"
							+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
							et.getTid(),et.getTitle(),et.getPubdate(),et.getSource(),
							et.getChannel(),et.getAuthor(),et.getDataType(),
							et.getContent(),et.getLocation(),et.getKeyWord(),
							et.getSiteId(),et.getLastModified());
		} catch (InvalidResultSetAccessException e) {
			log.error("数据库错误",e);
			return 0;
		} catch (DataAccessException e) {
			log.error("数据库错误",e);
			return 0;			
		}
	}

	@Override
	public int isExist(LiangHuiNewsEntity et) {
		String sql = "SELECT COUNT(*) FROM bs_lianghhuinews WHERE tid = ? and"
				   + " keyWord=? AND siteId=? ";
		
		try {
			int resNum = jdbcTemplate.queryForObject(sql, 
				new Object[]{et.getTid(),et.getKeyWord(), et.getSiteId()}, Integer.class);
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

	@Override
	public List<OperationEntity> queryLiangHuiByConfig(int pageId) {
		//JSONArray res  = new JSONArray();
				List<OperationEntity> list = null;
				try{     //and MEDIA_ID = 15
						 list = jdbcTemplate.query("SELECT  * from  ucoperation where  pageTypeID = ? "
						 		+ " and SYS_DELETEFLAG  = 0 and IsReFlash = 1"
						 		+ " ORDER BY media_id DESC", new Object[]{pageId}, new RowMapper<OperationEntity>() {		             
				            @Override
				            public OperationEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
				            	OperationEntity stu = new OperationEntity();
				            	stu.setSITE_ID(rs.getString("SITE_ID"));
				            	stu.setSITE_NAME(rs.getString("SITE_NAME"));
				            	stu.setSITE_TYPE(rs.getString("SITE_TYPE"));
				            	stu.setPageTypeID(rs.getString("pageTypeID"));
				            	stu.setMEDIA_ID(rs.getString("MEDIA_ID"));
				            	
				            	stu.setExtfileds(rs.getString("extfileds"));
				                stu.setConfigure(rs.getString("Configure"));
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
	public List<String> queryChannelId() {
		List<String> list = null;
		try{//where MEDIA_ID = ? 
			list = jdbcTemplate.query("SELECT  channelId from  bs_lhchannelid ", new Object[]{}, new RowMapper<String>() {				             
		            @Override
		            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
		            	//JSONObject stu = new JSONObject();
		            	//stu.put("channelId",rs.getString("channelId"));
		            	//list.add(rs.getString("channelId"));
		                return rs.getString("channelId");
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
	public int addLiangHuiKpiTrend(LiangHuiKpiTrendEntity et) {
		try {
			return jdbcTemplate
					.update("insert into bs_lianghuikpi("
							+ "keyWord,datatype,sourcetype,datatrend,sourcerank,origrank,lastModified,deleteFlag,countType)  "
							+ " VALUES (?,?,?,?,?,?,?,?,?)",
							et.getKeyWord(),et.getDatatype(),et.getSourcetype(),et.getDatatrend(),
							et.getSourcerank(),et.getOrigrank(),et.getLastModified(),
							et.getDeleteFlag(),et.getCountType());
		} catch (InvalidResultSetAccessException e) {
			log.error("数据库错误",e);
			return 0;
		} catch (DataAccessException e) {
			log.error("数据库错误",e);
			return 0;			
		}
	}

	@Override
	public int isExistKpi(LiangHuiKpiTrendEntity et) {
		String sql = "SELECT COUNT(*) FROM bs_lianghuikpi WHERE keyWord = ? and"
				   + " countType=? AND deleteFlag=0 ";
		
		try {
			int resNum = jdbcTemplate.queryForObject(sql, 
				new Object[]{et.getKeyWord(), et.getCountType()}, Integer.class);
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

	@Override
	public int updateLHKpi(LiangHuiKpiTrendEntity et) {
		try {
			return jdbcTemplate
					.update("UPDATE bs_lianghuikpi set datatype=? ,sourcetype=? ,datatrend=? ,"
							+ "sourcerank=? ,origrank=? ,lastModified=? where keyWord=? and countType=? and deleteFlag = 0",
							et.getDatatype(),et.getSourcetype(),et.getDatatrend(),
							et.getSourcerank(),et.getOrigrank(),et.getLastModified(),
							et.getKeyWord(),et.getCountType());
		} catch (InvalidResultSetAccessException e) {
			log.error("数据库错误",e);
			return 0;
		} catch (DataAccessException e) {
			log.error("数据库错误",e);
			return 0;			
		}
	}

}
