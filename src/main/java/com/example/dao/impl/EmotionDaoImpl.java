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

import com.example.bean.EmotionEntity;
import com.example.bean.OperationEntity;
import com.example.dao.EmotionDao;

@Repository
public class EmotionDaoImpl implements EmotionDao {
    private final Logger log = LoggerFactory.getLogger(EmotionDaoImpl.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public int addEmotion(EmotionEntity emotion) {
        try {
            return jdbcTemplate
                    .update("INSERT INTO bs_emotion (siteItem,emotion,mediaId,createTime) VALUES(?,?,?,?);",
                            emotion.getSiteItem(), emotion.getEmotion(), emotion.getMediaId(), emotion.getCreateTime());
        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return 0;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return 0;
        }
    }

    @Override
    public int updateEmotion(String siteItem, String mediaId, String emotion) {
        try {
            return jdbcTemplate.update("UPDATE bs_emotion SET emotion = ? where siteItem = ? and mediaId = ? ", emotion, siteItem, mediaId);
        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return 0;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return 0;
        }
    }

    @Override
    public int isHave(String siteItem, String mediaId) {
        String sql = "SELECT COUNT(*) as sums from bs_emotion where siteItem = ? and mediaId = ?";

        try {
            int upId = jdbcTemplate.queryForObject(sql, new Object[]{siteItem, mediaId}, Integer.class);
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
    public List<OperationEntity> selectEmotionId(String pageTypeIds, String mediaId) {
        List<OperationEntity> list = null;
        try {
            //and  11,14,15,16,17,18,20,23,24,39,45,49,64,65
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

}
