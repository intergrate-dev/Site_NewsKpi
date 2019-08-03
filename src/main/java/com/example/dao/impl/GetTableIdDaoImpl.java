package com.example.dao.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.bean.GetTableIdEntity;
import com.example.dao.GetTableIdDao;
@Repository
public class GetTableIdDaoImpl implements GetTableIdDao{
	private final Logger log = LoggerFactory.getLogger(GetTableIdDaoImpl.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<GetTableIdEntity> getTableIdList(String tableName) {
		try{
				//获取定时表的E5配置信息
				List<GetTableIdEntity> list = jdbcTemplate.query("select d.DOCLIBTABLE as DOCLIBTABLE,d.DOCLIBID as DOCLIBID,d.FOLDERID as FOLDERID,e.E5VALUE as E5VALUE from e5id e,dom_doclibs d where e.E5IDENTIFIER = d.DOCLIBTABLE and d.DOCLIBTABLE = ?", new Object[]{tableName}, new BeanPropertyRowMapper(GetTableIdEntity.class));
				if(list!=null && list.size()>0){
					return list;
				}else{
					return null;
				}
		}catch (InvalidResultSetAccessException e){
			log.error("数据库错误",e);
		    return null;
		}catch (DataAccessException e){
			log.error("数据库错误",e);
			 return null;
		}
	}

	@Override
	public int updateTableId(String tableName, int tableId) {
		try{
			//修改定制表的id增长后数值，
			int upId = jdbcTemplate.update("UPDATE e5id SET E5VALUE = ? WHERE E5IDENTIFIER=?",
					tableId, tableName);
			return upId;
		}catch (InvalidResultSetAccessException e){
		    return 0;
		}catch (DataAccessException e){
			 return 0;
		}
	}

	@Override
	public int selectMaxId(String tableName) {
		try{
			//查询表里的最大id值
			int upId = jdbcTemplate.queryForObject("SELECT MAX(SYS_DOCUMENTID) as MAXID FROM "+tableName, Integer.class);
			return upId;
		}catch (InvalidResultSetAccessException e){
		    return 0;
		}catch (DataAccessException e){
			 return 0;
		}
	}

}
