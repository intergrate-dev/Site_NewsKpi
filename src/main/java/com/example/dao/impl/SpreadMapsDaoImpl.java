package com.example.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.bean.OperationEntity;
import com.example.bean.SpreadMapEntity;
import com.example.dao.SpreadMapDao;

@Repository
public class SpreadMapsDaoImpl implements SpreadMapDao {
    private final Logger log = LoggerFactory.getLogger(SpreadMapsDaoImpl.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<OperationEntity> selectSiteId(String pageTypeIds, String mediaId) {
        List<OperationEntity> list = null;
        try {
            String sql = "SELECT  * from  ucoperation  where  pageTypeID in "
                    + "( " + pageTypeIds + ") and SYS_DELETEFLAG  = 0 and IsReFlash = 1 ";
            if (!StringUtils.isEmpty(mediaId)) {
                sql = sql.concat("and MEDIA_ID = ").concat(mediaId);
            }
            sql = sql.concat(" ORDER BY media_id DESC");
            list = jdbcTemplate.query(sql, new Object[]{}, new RowMapper<OperationEntity>() {
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

        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return null;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return null;
        }
        return list;
    }

	/*@Override
	public int isHave(String ST_ID, String time,int type) {
			
		try{
			int upId = jdbcTemplate.queryForObject("SELECT COUNT(*) from ucspreadMap where ST_ID = ? and ST_HOURSITE = ? and ST_COUNTTYPE = ?",new Object[]{ST_ID,time,type}, Integer.class);
			return upId;
		}catch (InvalidResultSetAccessException e){
			log.error("数据库错误",e);
		    return -1;
		}catch (DataAccessException e){
			log.error("数据库错误",e);
			 return -1;
		}
	}*/

    @Override
    public int addSpreadMap(SpreadMapEntity spreadMap) {
        try {
            return jdbcTemplate
                    .update("insert into ucspreadmap("
                                    + "SYS_DOCUMENTID, SYS_DOCLIBID, SYS_FOLDERID,SYS_DELETEFLAG,"
                                    + "MP_SITEID,MP_AREA,MP_COUNT,MP_MADIAID,MP_CREATETIME,MP_SOURCE) "
                                    + "VALUES (?,?,?,?,?,?,?,?,?,?)",
                            spreadMap.getSYS_DOCUMENTID(), spreadMap.getSYS_DOCLIBID(), spreadMap.getSYS_FOLDERID(),
                            spreadMap.getSYS_DELETEFLAG(), spreadMap.getMP_SITEID(), spreadMap.getMP_AREA(),
                            spreadMap.getMP_COUNT(), spreadMap.getMP_MADIAID(),
                            spreadMap.getMP_CREATETIME(), spreadMap.getMP_SOURCE());
        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return 0;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return 0;
        }
    }

    @Override
    public int delectSpreadMap(String mediaid, String SITE_ID) {
        try {
            return jdbcTemplate.update("DELETE  from  ucspreadMap  where MP_SITEID = ? and MP_MADIAID = ?",
                    SITE_ID, mediaid);
        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return 0;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return 0;
        }
    }

}
