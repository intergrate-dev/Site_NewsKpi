package com.example.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.util.FastJsonConvertUtil;
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
import com.example.bean.OperationEntity;
import com.example.bean.SiteNewsEntity;
import com.example.dao.SiteNewsDao;

@Repository
public class SiteNewsDaoImpl implements SiteNewsDao {
    private final Logger log = LoggerFactory.getLogger(SiteNewsDaoImpl.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<OperationEntity> selectSiteNewsId(String pageTypeIds, String mediaId) {
        List<OperationEntity> list = null;
        try {
            //and  11,14,15,16,17,18,20,23,24,39,45,49,64,65
            String sql = "SELECT  * from  ucoperation  where  pageTypeID in "
                    + "( "+ pageTypeIds + ") and SYS_DELETEFLAG  = 0 and IsReFlash = 1 ";
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
    public List<JSONObject> isHave(String SN_ID, String siteTerm, int isorginal) {

        List<JSONObject> list = null;
        try {
            list = jdbcTemplate.query("SELECT COUNT(id) as sums from bs_sitenews "
                            + "where SN_ID = ?  and  SN_SITETERM = ? and SN_ORIGINAL = ? ",
                    new Object[]{SN_ID, siteTerm, isorginal}, new RowMapper<JSONObject>() {
                        @Override
                        public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                            JSONObject stu = new JSONObject();
                            stu.put("sums", rs.getInt("sums"));
                            //stu.put("siteterm", rs.getString("SN_SITETERM"));
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
    public int addSiteNews(SiteNewsEntity siteNews) {
        log.info("==================================================================== siteNews: " + FastJsonConvertUtil.convertObjectToJSON(siteNews).toString()
                + " =======================================================");
        try {
            return jdbcTemplate
                    .update("insert into bs_sitenews("
                                    + "SYS_DELETEFLAG,SN_LASTMODIFIED,"
                                    + "SN_ID,SN_TITLE,SN_PUBDATE,SN_LOCATION,SN_SUMMARY,SN_KEYWORDS,"
                                    + "SN_SOURCE,SN_SOURCEID,SN_CHANNEL,"
                                    + "SN_CHANNELID,SN_PRESSCOUNT,SN_VISITCOUNT,SN_REBACKCOUNT,SN_LIKECOUNT,"
                                    + "SN_FORWARDCOUNT,SN_DATATYPE,SN_AUTHOR,"
                                    + "SN_ORIGINAL,SN_ISHOW,SN_PAGETYPEID,SN_DOCUMENT,SN_SITETERM,SN_ORIGINALPICURL)  "
                                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            siteNews.getSYS_DELETEFLAG(), siteNews.getSN_LASTMODIFIED(), siteNews.getSN_ID(), siteNews.getSN_TITLE(),
                            siteNews.getSN_PUBDATE(), siteNews.getSN_LOCATION(), siteNews.getSN_SUMMARY(),
                            siteNews.getSN_KEYWORDS(), siteNews.getSN_SOURCE(), siteNews.getSN_SOURCEID(),
                            siteNews.getSN_CHANNEL(), siteNews.getSN_CHANNELID(), siteNews.getSN_PRESSCOUNT(), siteNews.getSN_VISITCOUNT(),
                            siteNews.getSN_REBACKCOUNT(), siteNews.getSN_LIKECOUNT(), siteNews.getSN_FORWARDCOUNT(),
                            siteNews.getSN_DATATYPE(), siteNews.getSN_AUTHOR(),
                            siteNews.getSN_ORIGINAL(), siteNews.getSN_ISHOW(), siteNews.getSN_PAGETYPEID(),
                            siteNews.getSN_DOCUMENT(), siteNews.getSN_SITETERM(), siteNews.getSN_ORIGINALPICURL());
        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return 0;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return 0;
        }
    }

    @Override
    public List<JSONObject> getAreaNews(String maidaid) {
        //JSONArray res  = new JSONArray();
        List<JSONObject> list = null;
        try {
            list = jdbcTemplate.query("SELECT  * from  ucareanews where MEDIA_ID = ? ", new Object[]{maidaid}, new RowMapper<JSONObject>() {
                @Override
                public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                    JSONObject stu = new JSONObject();
                    stu.put("mediaId", rs.getString("MEDIA_ID"));
                    stu.put("keyWord", rs.getString("qxKeywords"));
                    stu.put("outWord", rs.getString("qxOutwords"));
                    stu.put("qxName", rs.getString("qxName"));

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
    public int updateSiteNews(String id, String siterm) {
        try {
            return jdbcTemplate.update("update bs_sitenews SET SN_SITETERM = ?  where SN_ID = ? ", siterm, id);
        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return 0;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return 0;
        }
    }

	/*@Override
	public int deleteNews(String siteID, int oriType, String taskExeTime, int delNum) {
		String sql = "DELETE FROM bs_sitenews WHERE SYS_DELETEFLAG=0 "
				   + "AND SN_ORIGINAL=? AND SN_SITETERM=? "
				   + "AND SN_LASTMODIFIED<? ORDER BY id ASC";
		
		try {
			if(delNum==-1){
				return jdbcTemplate.update(sql, oriType, siteID, taskExeTime);
			}else{
				sql += " LIMIT ?";
				return jdbcTemplate.update(sql, oriType, siteID, taskExeTime, delNum);
			}
			
		} catch (InvalidResultSetAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，无效的结果集合访问异常",e);
			return -1;
		} catch (DataAccessException e) {
			log.info("执行的sql语句：", sql);
			log.error("数据库错误，数据访问异常",e);
			return -1;			
		}
	}*/

    @Override
    public int deleteNews(String siteID, int oriType, String taskExeTime, String ids) {
        String sql = "DELETE FROM bs_sitenews WHERE SYS_DELETEFLAG=0 "
                + "AND SN_ORIGINAL=? AND SN_SITETERM=? "
                + "AND SN_LASTMODIFIED<?";

        if (!StringUtils.isBlank(ids)) {
            sql += " AND id in (" + ids + ")";
        }

        try {
            return jdbcTemplate.update(sql, oriType, siteID, taskExeTime);
        } catch (InvalidResultSetAccessException e) {
            log.info("执行的sql语句：", sql);
            log.error("数据库错误，无效的结果集合访问异常", e);
            return -1;
        } catch (DataAccessException e) {
            log.info("执行的sql语句：", sql);
            log.error("数据库错误，数据访问异常", e);
            return -1;
        }
    }

    @Override
    public List<Integer> queryAllMedias() {
        String sql = "SELECT pageType, SITE_NAME, MEDIA_ID from ucoperation GROUP BY MEDIA_ID";
        List<String> list = null;
        List<Integer> result = new ArrayList<>();
        try {
            list = jdbcTemplate.query(sql, new Object[]{}, new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return rs.getString("MEDIA_ID");
                        }
                    });
        } catch (InvalidResultSetAccessException e) {
            log.error("数据库错误", e);
            return null;
        } catch (DataAccessException e) {
            log.error("数据库错误", e);
            return null;
        }
        Collections.sort(list, new Collator() {
            @Override
            public int compare(String source, String target) {
                return Integer.parseInt(source) > Integer.parseInt(target) ? 1 : -1;
                //return source > target ? 1 : -1;
            }

            @Override
            public CollationKey getCollationKey(String source) {
                return null;
            }

            @Override
            public int hashCode() {
                return 0;
            }
        });
        for (String s : list) {
            result.add(Integer.parseInt(s));
        }
        return result;
    }

    @Override
    public List<SiteNewsEntity> querySiteOldNews(String siteID, int oriType, String taskExeTime) {

        String sql = "SELECT id,SN_LASTMODIFIED,SN_ID,SN_TITLE FROM bs_sitenews "
                + "WHERE SYS_DELETEFLAG=0 AND SN_ORIGINAL=? AND SN_SITETERM=? "
                + "AND SN_LASTMODIFIED<?";

        List<SiteNewsEntity> list = null;
        try {
            list = jdbcTemplate.query(sql, new Object[]{oriType, siteID, taskExeTime},
                    new RowMapper<SiteNewsEntity>() {
                        @Override
                        public SiteNewsEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
                            SiteNewsEntity sn = new SiteNewsEntity();
                            sn.setId(rs.getLong("id"));
                            sn.setSN_LASTMODIFIED(rs.getString("SN_LASTMODIFIED"));
                            sn.setSN_ID(rs.getString("SN_ID"));
                            sn.setSN_TITLE(rs.getString("SN_TITLE"));
                            return sn;
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
