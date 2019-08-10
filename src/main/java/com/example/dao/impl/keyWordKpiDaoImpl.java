package com.example.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.example.dao.KeyWordKpiDao;

@Repository
public class keyWordKpiDaoImpl implements KeyWordKpiDao {
    private final Logger log = LoggerFactory.getLogger(keyWordKpiDaoImpl.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int isHave(String keyword, String mediaId) {
        String sql = "SELECT COUNT(*) from bs_keywordkpi where keyWord = ? and mediaId = ?";

        try {
            int upId = jdbcTemplate.queryForObject(sql, new Object[]{keyword, mediaId}, Integer.class);
            return upId;
        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return -1;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return -1;
        }
    }

    @Override
    public List<OperationEntity> selectKeyWordKpiId(String pageTypeIds, String mediaId) {
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

    @Override
    public int addKeyWordKpi(String keyword, String mediaId, int forwardCount, int centerCount, int provinceCount, int countyCount, int count) {
        try {
            Date date = new Date();
            DateFormat format = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            String time = format.format(date);
            return jdbcTemplate
                    .update("INSERT INTO bs_keywordkpi (keyWord,forwardCount,centralMedia,provincialMedia,countyMedia,count,mediaId,createTime) VALUES (?,?,?,?,?,?,?,?);",
                            keyword, forwardCount, centerCount, provinceCount, countyCount, count, mediaId, time);
        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return 0;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return 0;
        }
    }

    @Override
    public int updateKeyWordKpi(String keyword, String mediaId, int forwardCount, int centerCount, int provinceCount, int countyCount, int count) {
        try {
            return jdbcTemplate.update("UPDATE bs_keywordkpi set  forwardCount = ?,centralMedia = ?,provincialMedia = ?,countyMedia = ?,count = ? where keyWord = ? and mediaId = ?",
                    forwardCount, centerCount, provinceCount, countyCount, count, keyword, mediaId);
        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return 0;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return 0;
        }
    }

}
