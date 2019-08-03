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

import com.example.bean.TokenEntity;
import com.example.dao.GetTokenDao;
@Repository
public class GetTokenDaoImpl implements GetTokenDao{
	private final Logger log = LoggerFactory.getLogger(GetTokenDaoImpl.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public List<TokenEntity> getToken(String soure) {

		try{
			//获取对应接口授权
			List<TokenEntity> list = jdbcTemplate.query("select * from uc_token where TK_SOURE = ?", new Object[]{soure}, new RowMapper<TokenEntity>() {
	            //映射每行数据  
	            @Override
	            public TokenEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
	            	TokenEntity stu = new TokenEntity();
	                stu.setTK_TOKEN(rs.getString("TK_TOKEN"));
	                stu.setTK_EXPIRETIME(rs.getString("TK_EXPIRETIME"));
	                stu.setTK_SOURE(rs.getString("TK_SOURE"));
	                return stu;
	            }
	
	        });
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
	public int updateToken(TokenEntity token) {
		try{
			return jdbcTemplate.update("UPDATE uc_token SET TK_TOKEN = ? , TK_EXPIRETIME = ?  where TK_SOURE= ?",
					token.getTK_TOKEN(),token.getTK_EXPIRETIME(), token.getTK_SOURE());
		}catch (InvalidResultSetAccessException e){
		    return 0;
		}catch (DataAccessException e){
			 return 0;
		}
	}

	@Override
	public int insertToken(TokenEntity token) {
			try{
				return jdbcTemplate.update("insert into uc_token(TK_TOKEN, TK_EXPIRETIME,TK_SOURE) values(?,?,?)",
						token.getTK_TOKEN(),token.getTK_EXPIRETIME(), token.getTK_SOURE());
			}catch (InvalidResultSetAccessException e){
			    return 0;
			}catch (DataAccessException e){
				 return 0;
			}
	}

}
