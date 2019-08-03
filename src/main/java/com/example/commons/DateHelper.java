package com.example.commons;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** 
 * 类说明 ： 
 */
public class DateHelper {
	public enum DateFormat{YMD, YMDHMS, HMS,YEAR, MONTH, DATE, 
		HOUR, MINUTE, SENONDS, 
		YMD_CHINESE,  YMDHMS_ALL_CHINESE, YMDHMS_CHINESE, HMS_CHINESE,
		WEEKDAY, YMDHMSW, YMDW, YMDHMSW_CHINESE, YMDW_CHINESE,YMDHMSW_ALL_CHINESE};
		
	public static final String YMD = "yyyy-MM-dd";
	public static final String YMD_CHINESE = "yyyy年MM月dd日";
	public static final String YMDHMS = "yyyy-MM-dd HH:mm:ss";
	public static final String YMDHMS_CHINESE = "yyyy年MM月dd日 HH:mm:ss";
	public static final String YMDHMS_ALL_CHINESE = "yyyy年MM月dd日HH时mm分ss秒";
	public static final String HMS = "HH:mm:ss";
	public static final String HMS_CHINESE = "HH时mm分ss秒";
	public static final String YEAR = "yyyy";
	public static final String MONTH = "MM";
	public static final String DATE = "dd";
	public static final String HOUR = "HH";
	public static final String MINUTE = "mm";
	public static final String SENONDS = "ss";
	
	public static final String WEEKDAY = "E";
	public static final String YMDHMSW = "yyyy-MM-dd HH:mm:ss E";
	public static final String YMDW = "yyyy-MM-dd E";
	public static final String YMDHMSW_CHINESE = "yyyy年MM月dd日 HH:mm:ss E";
	public static final String YMDW_CHINESE = "yyyy年MM月dd日 E";
	public static final String YMDHMSW_ALL_CHINESE = "yyyy年MM月dd日HH时mm分ss秒 E";
	
	/** 返回 date 加上 value 天后的日期（清除时间信息） */
	public final static Date addDate(Date date, int value)
	{
		return addDate(date, value, true);
	}

	/** 返回 date 加上 value 天后的日期，trimTime 指定是否清除时间信息 */
	public final static Date addDate(Date date, int value, boolean trimTime)
	{
		return addTime(date, Calendar.DATE, value, trimTime);

	}

	/** 返回 date 加上 value 个 field 时间单元后的日期（不清除时间信息） */
	public final static Date addTime(Date date, int field, int value)
	{
		return addTime(date, field, value, false);
	}
	/** 返回 date 加上 value 个 field 时间单元后的日期，trimTime 指定是否去除时间信息 */
	public final static Date addTime(Date date, int field, int value, boolean trimTime)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(field, value);

		if(trimTime)
		{
        		c.set(Calendar.HOUR, 0);
        		c.set(Calendar.MINUTE, 0);
        		c.set(Calendar.SECOND, 0);
        		c.set(Calendar.MILLISECOND, 0);
		}
		return c.getTime();
	}
	
	public final static Timestamp getAfter1Min(){
		Calendar c=Calendar.getInstance();  
		c.add(Calendar.MINUTE, 1); 
		Timestamp ts = new Timestamp(c.getTime().getTime());
		return ts;
	}
	
	/**
	 * 获取年月日时分秒时间戳
	 * @return
	 */
	public static String getSysTime(){
		Date dNow = new Date();
		//保存目录名称
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		//保存文件名称后缀
		SimpleDateFormat formatT = new SimpleDateFormat("HHmmss");
		
		StringBuilder sb = new StringBuilder();
		//当前日期
		sb.append(format.format(dNow));
		//当前时分秒
		sb.append(formatT.format(dNow));
		return sb.toString();
	}
	
	/**
	 * 获取年月日时分秒时间戳
	 * @return
	 */
	public static String getSysDate(){
		Date dNow = new Date();
		//保存目录名称
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		
		StringBuilder sb = new StringBuilder();
		//当前日期
		sb.append(format.format(dNow));
		return sb.toString();
	}
	
	public static String getSysDateTime() {
		return new Timestamp(System.currentTimeMillis()).toString().substring(0,19);
	}
	
	/**
	 * 获取日期
	 * @param dateTime
	 * @return
	 */
	public static String getSubDate(String dateTime){
		if(dateTime == null || dateTime.length() < 10)
			return "";
		
		return dateTime.substring(0, 10);
	}
	
	/**
	 * 获取日期
	 * @return
	 */
	public static String getSubDate(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(new Date());
	}
	
	/**
	 * 获取指定格式的日期
	 * @return
	 */
	public static String getFormat(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return format.format(new Date());
	}
	
	public static String getCdateFormat2Str(String fmt){
		SimpleDateFormat format = new SimpleDateFormat(fmt);
		return format.format(new Date());
	}
	
	/**
	 * 转换指定格式的日期
	 * @return
	 * @throws ParseException 
	 */
	public static Date covertFormat(String time) throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.parse(time);
	}
	/**
	 * 获取小时
	 * @param dateTime
	 * @return
	 */
	public static String getSubHour(String dateTime){
		if(dateTime == null || dateTime.length() < 19)
			return "";
		
		return dateTime.substring(11, 13);
	}
	
	/**
	 * 获取分钟
	 * @param dateTime
	 * @return
	 */
	public static String getSubMin(String dateTime){
		if(dateTime == null || dateTime.length() < 19)
			return "";
		
		return dateTime.substring(14, 16);
	}
	
	/**
	 * 
	 * @param date
	 * @param format
	 * @return 格式化日期
	 * @throws Exception
	 */
	public static String parse2String(Date date, DateFormat format){
		SimpleDateFormat sdf  = new SimpleDateFormat();
		switch(format){
			case YMD:sdf.applyPattern(YMD);break;
			case YMDHMS:sdf.applyPattern(YMDHMS);break;
			case HMS:sdf.applyPattern(HMS);break;
			case YEAR:sdf.applyPattern(YEAR);break;
			case MONTH:sdf.applyPattern(MONTH);break;
			case DATE:sdf.applyPattern(DATE);break;
			case HOUR:sdf.applyPattern(HOUR);break;
			case MINUTE:sdf.applyPattern(MINUTE);break;
			case SENONDS:sdf.applyPattern(SENONDS);break;
			case YMD_CHINESE:sdf.applyPattern(YMD_CHINESE);break;
			case YMDHMS_ALL_CHINESE:sdf.applyPattern(YMDHMS_ALL_CHINESE);break;
			case YMDHMS_CHINESE:sdf.applyPattern(YMDHMS_CHINESE);break;
			case WEEKDAY:sdf.applyPattern(WEEKDAY);break;
			case YMDHMSW:sdf.applyPattern(YMDHMSW);break;
			case YMDW:sdf.applyPattern(YMDW);break;
			case YMDHMSW_CHINESE:sdf.applyPattern(YMDHMSW_CHINESE);break;
			case YMDW_CHINESE:sdf.applyPattern(YMDW_CHINESE);break;
			case YMDHMSW_ALL_CHINESE:sdf.applyPattern(YMDHMSW_ALL_CHINESE);break;
			default:
				sdf.applyPattern(YMDHMS);break;
		}
		
		return sdf.format(date);
	}
	
	/**
	 * 获取当前日期
	 * @return
	 */
	public static String getCurrentDate(){
		
		return parse2String(new Date(), DateFormat.YMD);
	}
	
	/** 
	   * 得到几天前的时间 
	   * @param d 某个日期
	   * @param day 1~前一天数据
	   * @return 
	   */  
	  public static Date getDateBefore(Date d,int day){  
	   Calendar now =Calendar.getInstance();  
	   now.setTime(d);  
	   now.set(Calendar.DATE,now.get(Calendar.DATE)-day);  
	   return now.getTime();  
	  }  
	    
	  /** 
	   * 得到几天后的时间 
	   * @param d 
	   * @param day 
	   * @return 
	   */  
	  public static Date getDateAfter(Date d,int day){  
	   Calendar now =Calendar.getInstance();  
	   now.setTime(d);  
	   now.set(Calendar.DATE,now.get(Calendar.DATE)+day);  
	   return now.getTime();  
	  }
	  
	/**
	 * 
	 * @param str format yyyy-MM-dd
	 * @return
	 * @throws ParseException 
	 */
	public static Date string2Date(String str){
		SimpleDateFormat sdf = new SimpleDateFormat(YMD);
		
		try {
			return sdf.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 将字符串日期转换为指定日期格式
	 * @param str
	 * @param fmt
	 * @return
	 */
	public static Date string2Date(String str,String fmt){
		SimpleDateFormat sdf = new SimpleDateFormat(fmt);
		
		try {
			return sdf.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param str format yyyy-MM-dd
	 * @return
	 * @throws ParseException 
	 */
	public static String date2String(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat(YMD);
		return sdf.format(date);
	}
	
	/**
	 * 将date格式日期转换为指定格式
	 * @param date
	 * @param fmt
	 * @return
	 */
	public static String date2String(Date date,String fmt){
		SimpleDateFormat sdf = new SimpleDateFormat(fmt);
		return sdf.format(date);
	}
	
	/**
	 * 时间戳转为日期时间
	 * @param ts
	 * @return
	 * @throws ParseException
	 */
	public static Date timeStamp2Date(long ts) throws ParseException{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.parse(sdf.format((new Date(ts))));
	}
}