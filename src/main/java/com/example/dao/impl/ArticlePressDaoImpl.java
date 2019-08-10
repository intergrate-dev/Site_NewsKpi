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

import com.example.bean.ArticleNewsKpiEntity;
import com.example.bean.OperationEntity;
import com.example.dao.ArticlePressDao;

@Repository
public class ArticlePressDaoImpl implements ArticlePressDao {
    private final Logger log = LoggerFactory
            .getLogger(ArticlePressDaoImpl.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * @param pageTypeIds
     * @param mediaId
     * @author xls
     */
    @Override
    public List<OperationEntity> selectArticlePressId(String pageTypeIds, String mediaId) {
        List<OperationEntity> list = null;
        try {
            // 获取微信站点传播分析数据的相关页面配置
            String sql = "SELECT  * from  ucoperation  where  pageTypeID in "
                    + "( " + pageTypeIds + ") and SYS_DELETEFLAG  = 0 and IsReFlash = 1 ";
            if (!StringUtils.isEmpty(mediaId)) {
                sql = sql.concat("and MEDIA_ID = ").concat(mediaId);
            }
            sql = sql.concat(" ORDER BY media_id DESC");
            list = jdbcTemplate.query(sql, new Object[]{}, new RowMapper<OperationEntity>() {
                @Override
                public OperationEntity mapRow(ResultSet rs,
                                              int rowNum) throws SQLException {
                    OperationEntity stu = new OperationEntity();
                    stu.setSITE_ID(rs.getString("SITE_ID"));
                    stu.setSITE_NAME(rs.getString("SITE_NAME"));
                    stu.setSITE_TYPE(rs.getString("SITE_TYPE"));
                    stu.setPageTypeID(rs
                            .getString("pageTypeID"));
                    stu.setMEDIA_ID(rs.getString("MEDIA_ID"));
                    stu.setUSER_BEHAVIOR_ID(rs
                            .getString("USER_BEHAVIOR_ID"));
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
    public int isHave(String mediaId, String id, String keyword) {
        try {
            String sql = "SELECT COUNT(*) from bs_articlepress where mediaId = ? and id = ? ";
            if (keyword != null) {
                sql = sql + " and keyword = ? ";
                int upId = jdbcTemplate.queryForObject(sql, new Object[]{mediaId,
                        id, keyword}, Integer.class);
                return upId;
            } else {
                int upId = jdbcTemplate.queryForObject(sql, new Object[]{mediaId,
                        id}, Integer.class);
                return upId;
            }

        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return -1;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return -1;
        }
    }

    @Override
    public int getcount(String siteId) {
        String sql = "SELECT COUNT(*) from bs_articlepress where siteId = ?  ";
        try {
            int upId = jdbcTemplate.queryForObject(sql, new Object[]{siteId
            }, Integer.class);
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
    public int addArticlePress(ArticleNewsKpiEntity articlePress) {
        try {
            return jdbcTemplate
                    .update("insert into bs_articlepress(id,title,pubtime,source,channel,forwardMediaCount,"
                                    + "forwardCount,forwardMediaList,forwardNewsList,forwardKpiTrend,"
                                    + "pressTypes,pressDistribution,siteId,creatTime,keyword,mediaId) "
                                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            articlePress.getId(), articlePress.getTitle(),
                            articlePress.getPubtime(),
                            articlePress.getSource(),
                            articlePress.getChannel(),
                            articlePress.getForwardMediaCount(),
                            articlePress.getForwardCount(),
                            articlePress.getForwardMediaList(),
                            articlePress.getForwardNewsList(),
                            articlePress.getForwardKpiTrend(),
                            articlePress.getPressTypes(),
                            articlePress.getPressDistribution(),
                            articlePress.getSiteId(),
                            articlePress.getCreatTime(),
                            articlePress.getKeyword(),
                            articlePress.getMediaId());
        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return 0;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return 0;
        }
    }

    @Override
    public int updateArticlePress(ArticleNewsKpiEntity articlePress) {
        String keyword = articlePress.getKeyword();
        try {
            if (keyword != null && !"".equals(keyword) && !"无需配置".equals(keyword)) {
                return jdbcTemplate
                        .update("update bs_articlePress SET forwardMediaCount = ?,forwardCount = ?,"
                                        + "pressTypes = ?,pressDistribution = ? where id = ? and siteId = ? and keyword = ?",
                                articlePress.getForwardMediaCount(),
                                articlePress.getForwardCount(),
                                articlePress.getPressTypes(),
                                articlePress.getPressDistribution(),
                                articlePress.getId(), articlePress.getSiteId(), articlePress.getKeyword());
            } else {
                return jdbcTemplate
                        .update("update bs_articlePress SET forwardMediaCount = ?,forwardCount = ?,"
                                        + "pressTypes = ?,pressDistribution = ? where id = ? and siteId = ? ",
                                articlePress.getForwardMediaCount(),
                                articlePress.getForwardCount(),
                                articlePress.getPressTypes(),
                                articlePress.getPressDistribution(),
                                articlePress.getId(), articlePress.getSiteId());
            }

        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return 0;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return 0;
        }
    }

    @Override
    public int deleteArticlePress(String mediaId, String id, int type, String keyword) {
        try {
            if (type == 0) {
                return jdbcTemplate.update(
                        "DELETE FROM bs_articlePress  where mediaId = ? and id = ? ", mediaId, id);
            } else {
                int sum = jdbcTemplate
                        .queryForObject("select MIN(t.SYS_DOCUMENTID) from (select  SYS_DOCUMENTID from bs_articlePress where  mediaId = ? and keyword = ? order by SYS_DOCUMENTID desc limit 0,5) t",
                                new Object[]{mediaId, keyword}, Integer.class);

                return jdbcTemplate.update(
                        "DELETE FROM bs_articlePress  where mediaId = ?  and keyword = ? and SYS_DOCUMENTID < ?", mediaId, keyword, sum);
            }

        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return 0;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return 0;
        }
    }
}
