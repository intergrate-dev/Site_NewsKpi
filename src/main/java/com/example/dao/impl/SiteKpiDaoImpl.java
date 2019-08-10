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

import com.example.bean.OperationEntity;
import com.example.bean.SiteKpiEntity;
import com.example.dao.SiteKpiDao;
@Repository
public class SiteKpiDaoImpl implements SiteKpiDao {
	private final Logger log = LoggerFactory.getLogger(SiteKpiDaoImpl.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	/**
	 * @author xls
	 */
	@Override
	public List<OperationEntity> selectSiteKpiId() {
		//JSONArray res  = new JSONArray(); MEDIA_ID = 15  AND 
		List<OperationEntity> list = null;
		try{	//获取微信站点传播分析数据的相关页面配置
				 list = jdbcTemplate.query("SELECT  * from  ucoperation where pageTypeID IN (10,11,18,20,23,24,64,65) and IsReFlash = 1 ", new Object[]{}, new RowMapper<OperationEntity>() {
		             
		            @Override
		            public OperationEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
		            	OperationEntity stu = new OperationEntity();
		            	stu.setSITE_ID(rs.getString("SITE_ID"));
		            	stu.setSITE_NAME(rs.getString("SITE_NAME"));
		            	stu.setSITE_TYPE(rs.getString("SITE_TYPE"));
		            	stu.setPageTypeID(rs.getString("pageTypeID"));
		            	stu.setMEDIA_ID(rs.getString("MEDIA_ID"));
		            	stu.setUSER_BEHAVIOR_ID(rs.getString("USER_BEHAVIOR_ID"));
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
	public int isHave(String ST_ID, String time,int type) {
		String sql = "";
		if(type==0 || type==2 || type==3){
			sql="SELECT COUNT(*) from bs_sitekpi where ST_ID = ? and ST_DAYSITE = ? and ST_COUNTTYPE = ?";
		}else if(type==1){
			sql="SELECT COUNT(*) from bs_sitekpi where ST_ID = ? and ST_HOURSITE = ? and ST_COUNTTYPE = ?";
		}else{
			 return -1;
		}			
		try{
			int upId = jdbcTemplate.queryForObject(sql,new Object[]{ST_ID,time,type}, Integer.class);
			return upId;
		}catch (InvalidResultSetAccessException e){
			log.error("数据库错误",e);
		    return -1;
		}catch (DataAccessException e){
			log.error("数据库错误",e);
			 return -1;
		}
	}

	@Override
	public int addSiteKpi(SiteKpiEntity siteKpi) {
		try {
			return jdbcTemplate
					.update("insert into bs_sitekpi("
							+ "SYS_DELETEFLAG,ST_ID,ST_NAME,ST_TYPE,"
							+ "ST_PRESSCOUNT,ST_FORWARDCOUNT,ST_ORIGINALCOUNT,"
							+ "ST_FORWARDMEDIACOUNT,ST_REBACKCOUNT,ST_VISITCOUNT,"
							+ "ST_LIKECOUNT,ST_FORWARDBYICCOUNT,ST_READCOUNT,ST_COUNTTYPE,"
							+ "ST_HOURSITE,ST_DAYSITE,ST_CREATETIME,ST_UPDATETIME)  "
							+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
							siteKpi.getSYS_DELETEFLAG(),siteKpi.getST_ID(),siteKpi.getST_NAME(),
							siteKpi.getST_TYPE(),siteKpi.getST_PRESSCOUNT(),siteKpi.getST_FORWARDCOUNT(),
							siteKpi.getST_ORIGINALCOUNT(),siteKpi.getST_FORWARDMEDIACOUNT(),
							siteKpi.getST_REBACKCOUNT(),siteKpi.getST_VISITCOUNT(),siteKpi.getST_LIKECOUNT(),
							siteKpi.getST_FORWARDBYICCOUNT(),siteKpi.getST_READCOUNT(),
							siteKpi.getST_COUNTTYPE(),siteKpi.getST_HOURSITE(),siteKpi.getST_DAYSITE(),
							siteKpi.getST_CREATETIME(),siteKpi.getST_UPDATETIME());
		} catch (InvalidResultSetAccessException e) {
			log.error("数据库错误",e);
			return 0;
		} catch (DataAccessException e) {
			log.error("数据库错误",e);
			return 0;			
		}
	}

	@Override
	public int updateSiteKpi(SiteKpiEntity siteKpi) {
		
		try{
			if(siteKpi.getST_COUNTTYPE()==1){
				return jdbcTemplate.update("update bs_sitekpi SET ST_PRESSCOUNT = ? , ST_FORWARDCOUNT = ? , "
						+ "ST_ORIGINALCOUNT = ? , ST_FORWARDMEDIACOUNT = ? , ST_REBACKCOUNT = ? ,ST_VISITCOUNT=? ,"
						+ "ST_LIKECOUNT = ?, ST_FORWARDBYICCOUNT=?, ST_READCOUNT=?, ST_UPDATETIME = ? "
						+ " where ST_ID = ? and ST_HOURSITE = ? and ST_COUNTTYPE = ?",
						siteKpi.getST_PRESSCOUNT(),siteKpi.getST_FORWARDCOUNT(),siteKpi.getST_ORIGINALCOUNT(),
						siteKpi.getST_FORWARDMEDIACOUNT(),siteKpi.getST_REBACKCOUNT(),siteKpi.getST_VISITCOUNT(),
						siteKpi.getST_LIKECOUNT(),siteKpi.getST_FORWARDBYICCOUNT(),siteKpi.getST_READCOUNT(),
						siteKpi.getST_UPDATETIME(),siteKpi.getST_ID(),siteKpi.getST_HOURSITE(),siteKpi.getST_COUNTTYPE());
			}else{
				return jdbcTemplate.update("update bs_sitekpi SET ST_PRESSCOUNT = ? , ST_FORWARDCOUNT = ? , "
						+ "ST_ORIGINALCOUNT = ? , ST_FORWARDMEDIACOUNT = ? , ST_REBACKCOUNT = ? ,ST_VISITCOUNT=? ,"
						+ "ST_LIKECOUNT = ?, ST_FORWARDBYICCOUNT=?, ST_READCOUNT=?, ST_UPDATETIME = ?"
						+ " where ST_ID = ? and ST_DAYSITE = ? and ST_COUNTTYPE = ?",
						siteKpi.getST_PRESSCOUNT(),siteKpi.getST_FORWARDCOUNT(),siteKpi.getST_ORIGINALCOUNT(),
						siteKpi.getST_FORWARDMEDIACOUNT(),siteKpi.getST_REBACKCOUNT(),siteKpi.getST_VISITCOUNT(),
						siteKpi.getST_LIKECOUNT(),siteKpi.getST_FORWARDBYICCOUNT(),siteKpi.getST_READCOUNT(),
						siteKpi.getST_UPDATETIME(),siteKpi.getST_ID(),siteKpi.getST_DAYSITE(), siteKpi.getST_COUNTTYPE());
			}

			
		}catch (InvalidResultSetAccessException e){
			log.error("数据库错误",e);
		    return 0;
		}catch (DataAccessException e){
			log.error("数据库错误",e);
			 return 0;
		}
	}

}
