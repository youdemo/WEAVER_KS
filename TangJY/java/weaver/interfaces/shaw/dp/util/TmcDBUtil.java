package weaver.interfaces.shaw.dp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;

public class TmcDBUtil {
	BaseBean log = new BaseBean();
	
	public boolean insert(String table,Map<String,String> mapStr){
		if(mapStr == null || mapStr.isEmpty()) return false;
		if(table == null || "".equals(table)) return false;

		RecordSet rs = new RecordSet();
		
		String sql_0 = "insert into "+table+"(";
		StringBuffer sql_1 = new StringBuffer();
		String sql_2 = ") values(";
		StringBuffer sql_3 = new StringBuffer();
		String sql_4 = ")";
		
		Iterator<String> it = mapStr.keySet().iterator();
		while(it.hasNext()){
			String tmp_1 = it.next();
			String tmp_1_str = mapStr.get(tmp_1);
			if(tmp_1_str == null) tmp_1_str = "";
			
			if(tmp_1_str.length() > 0){
				sql_1.append(tmp_1);sql_1.append(",");
				
				if(tmp_1_str.contains("##")){
					sql_3.append(tmp_1_str.replace("##", ""));sql_3.append(",");
				}else{
					sql_3.append("'");sql_3.append(tmp_1_str);sql_3.append("',");
				}
			}
		}
		
		String now_sql_1 = sql_1.toString();
		if(now_sql_1.lastIndexOf(",")>0){
			now_sql_1 = now_sql_1.substring(0,now_sql_1.length()-1);
		}
		
		String now_sql_3 = sql_3.toString();
		if(now_sql_3.lastIndexOf(",")>0){
			now_sql_3 = now_sql_3.substring(0,now_sql_3.length()-1);
		}
		
		String sql = sql_0 + now_sql_1 + sql_2 + now_sql_3 + sql_4;
		log.writeLog("insert(sql) = " + sql);
		return rs.executeSql(sql);
	}
	
	// 内容不存在新增，存在新增
	public boolean addOrUpdate(String table,Map<String,String> mapStr,Map<String,String> whereMap){
		if(mapStr == null || mapStr.isEmpty()) return false;
		if(table == null || "".equals(table)) return false;
		if(whereMap == null|| whereMap.isEmpty()) return false;
		// 判断是否存在
		String whereSql = " where 1=1 ";
		Iterator<String> itx = whereMap.keySet().iterator();
		while(itx.hasNext()){
			String tmp_1 = itx.next();	
			String tmp_1_str = whereMap.get(tmp_1);
			
			whereSql = whereSql + " and " + tmp_1 + " ='" + tmp_1_str + "' ";
		}
		
		RecordSet rs = new RecordSet();
		String sql = "select count(*) as ct from " + table + whereSql;
		int ct = 0 ;
		boolean isRun = rs.executeSql(sql);
		if(!isRun){
			log.writeLog("sql = "+ sql +" 执行错误！表不存在或字段不存在！");
			return isRun;
		}
		if(rs.next()){
			ct = rs.getInt("ct");		
		}
		if(ct == 0){
			isRun = update(table,mapStr,whereMap);
		}else if(ct > 1){
			isRun = false;
			log.writeLog("系统存在多个对象，本次操作不进行任何更新！");
		}else{
			isRun = insert(table,mapStr);
		}
		
		return isRun;
	}
	
	public boolean update(String table,Map<String,String> mapStr,Map<String,String> whereMap){
		
		return update(table, mapStr, whereMap,true);
	}
	
	// isUpdate 值为空或null是否更新   true 更新   false 不更新
	public boolean update(String table,Map<String,String> mapStr,Map<String,String> whereMap,boolean isUpdate){
		if(mapStr == null || mapStr.isEmpty()) return false;
		if(table == null || "".equals(table)) return false;
		if(whereMap == null|| whereMap.isEmpty()) return false;
		RecordSet rs = new RecordSet();
			
		StringBuffer buff = new StringBuffer();
		buff.append("update ");buff.append(table);buff.append(" set ");
			
		Iterator<String> it = mapStr.keySet().iterator();
		String flag = "";
		while(it.hasNext()){
			String tmp_1 = it.next();	
			String tmp_1_str = mapStr.get(tmp_1);
			if(tmp_1_str == null) tmp_1_str = "";
			
			if(isUpdate){
				if(tmp_1_str.length() == 0){
					buff.append(flag);buff.append(tmp_1);buff.append("=null ");					
					flag = ",";
				}else{
					buff.append(flag);buff.append(tmp_1);buff.append("=");
					
					if(tmp_1_str.contains("##")){
						buff.append(tmp_1_str.replace("##", ""));
					}else{
						buff.append("'");buff.append(tmp_1_str);buff.append("'");
					}
					flag = ",";
				}
			}else{
				if(tmp_1_str.length() > 0){
					buff.append(flag);buff.append(tmp_1);buff.append("=");
					
					if(tmp_1_str.contains("##")){
						buff.append(tmp_1_str.replace("##", ""));
					}else{
						buff.append("'");buff.append(tmp_1_str);buff.append("'");
					}
					flag = ",";
				}
			}
		}
		String whereSql = " where 1=1 ";
		Iterator<String> itx = whereMap.keySet().iterator();
		while(itx.hasNext()){
			String tmp_1 = itx.next();	
			String tmp_1_str = whereMap.get(tmp_1);
			
			whereSql = whereSql + " and " + tmp_1 + " ='" + tmp_1_str + "' ";
		}
		
		String sql = buff.toString() + whereSql;
		
		log.writeLog("update(sql) = " + sql);
		return rs.executeSql(sql);
	}
	
	//  表单建模
	public boolean insertGs(int creater,int modeid,int m_billid){
		ModeRightInfo ModeRightInfo = new ModeRightInfo();
		ModeRightInfo.editModeDataShare(creater,modeid,m_billid);//新建的时候添加共享
		return true;
	}
	
	// 流水编码   表名  uf_tmcCode【numF，字母标示；codeF，编码标示；yearM，年月；nextN，下一个编号】
	
	// yearM 格式  201605 
	public String getNowCode(String code,String flag,String yearM){
		RecordSet rs = new RecordSet();
		String sql = "select * from uf_tmcCode where codef='"+code
				+"' and numf='"+flag+"' and yearm='"+yearM+"'";
		rs.executeSql(sql);
		int now = 0;
		if(rs.next()){
			now = rs.getInt("nextN");
		}
		int nexNow = now + 1;
		if(now == 0){
			now = 1;nexNow = 2;
			sql = "insert into uf_tmcCode(codeF,numf,yearm,nextN) values('"
					+code+"','"+flag+"','"+yearM+"',"+nexNow+")";
		}else{
			sql = "update uf_tmcCode set nextN="+nexNow+"where codef='"+code
					+"' and numf='"+flag+"' and yearm='"+yearM+"'";
		}
		rs.executeSql(sql);
		
		String all_code = code+yearM+String.format("%04d", now);
		
		return all_code;
	}
	
	public String getNowCode(String code,String flag){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		String yearM = sdf.format(new Date());
		return getNowCode(code,flag,yearM);
	}
}
