/**
 * HrmService Created on 2005-6-6 9:27:49
 *
 * Copyright(c) 2001-2004 Weaver Software Corp.All rights reserved.
 */
package weaver.soa.hrm;

import org.apache.commons.beanutils.BeanUtils;
import weaver.conn.RecordSet;
import weaver.file.LogMan;
import weaver.file.Prop;
import weaver.general.GCONST;
import weaver.general.Util;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.hrm.finance.SalaryManager;
import weaver.hrm.resource.ResourceComInfo;
import weaver.ldap.LdapUtil;
import weaver.rtx.OrganisationCom;
import ln.LN;
import weaver.system.SysRemindWorkflow;
import weaver.systeminfo.SysMaintenanceLog;
import weaver.systeminfo.SystemEnv;
import weaver.rtx.*;
import java.util.*;


/**
 * Description: HrmService
 * Company: 泛微软件
 *
 * @author xiaofeng.zhang
 * @version 1.0 2005-6-6
 */
public class HrmService {
    private static LogMan log = LogMan.getInstance();
    private boolean isLdap = false;
    private List exp_result;
	private OrganisationCom rtxService=null;

    public HrmService() {
		rtxService=new OrganisationCom();
        String mode = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
        if (mode != null && mode.equals("ldap")) {
            isLdap = true;
        }
    }

    /**
     * add user, if the user has existed, update the user. according to loginid or account to judge if the user has existed.
     *
     * @param u
     * @return success
     */
    public boolean addUser(User u) {
        try {
            //judge license
            LN l = new LN();
            
            if (l.CkHrmnum() >= 0) {  //reach the max hrm number
            	log.writeLog(this.getClass().getName(), "超出license用户数许可。");
               return false;
            }
            u.setId(-1);  //avoid dirty value
            RecordSet rs = new RecordSet();
            DepartmentComInfo deptcominfo = new DepartmentComInfo();
            ResourceComInfo resourcecominfo = new ResourceComInfo();
            String logid = BeanUtils.getSimpleProperty(u, "loginid");
            logid = this.plusQuoter(logid);//处理引号
            BeanUtils.setProperty(u, "loginid", logid);//回写loginid
            String lastname = BeanUtils.getSimpleProperty(u, "lastname");
            String lastname_new = this.plusQuoter(lastname);//处理引号
            BeanUtils.setProperty(u, "lastname", lastname_new);//回写lastname
           
           
          
           /*
            if (isLdap) {//workcode con't repeat
                rs.executeSql("select id from HrmResource where workcode='" + workcode +"'");
                if(rs.next()){
                    u.setId(rs.getInt(1));
                }
                if (rs.getCounts() > 0)
                {
                	rs.executeSql("select id from HrmResource where account='" + dc+"\\"+acc+ "' and workcode='" + workcode +"'" );
                	if(rs.getColCounts()>0){
                      return updateUser(u);
                	}
                	else
                	{
                		 log.writeLog(this.getClass().getName(), "account= "  + dc+"\\"+acc+ "'" + "......workcode has existed. sync ignore !\n");
                    	 return false;
                	}
                }  
            }*/
            //==========================
            String dc=u.getLdap_domainName();//域名
			int ldap_mark=Util.getIntValue(u.getLdapmark(),0);//ldap第ldap_mark个标记
            
            //modify by ds 人员导入可以导入登陆账号为空的人员
            /*
            if (isLdap && (u.getAccount() == null || u.getAccount().equals("")))  //account is must for ldap athentic
                return false;
            if (!isLdap && (logid == null || logid.equals("")))  //loginid is must for database athentic
                return false;*/
            if (!isLdap && logid != null && !logid.equals("")) {//judge if the entry has been exported or not

                rs.executeSql("select id from HrmResource where loginid='" + logid + "'");
                if(rs.next()){
                    u.setId(rs.getInt(1));
                }
                if (rs.getCounts() > 0)
                    return updateUser(u);
            }
            String acc = BeanUtils.getSimpleProperty(u, "account");
            acc = this.plusQuoter(acc);//处理引号
            BeanUtils.setProperty(u, "account", acc);//回写account
          
          
            if (isLdap && acc != null && !acc.equals("")) {//judge if the entry has been exported or not
            	rs.executeSql("select id from HrmResource where account='" + dc+"\\"+acc+ "'");
               // rs.executeSql("select id from HrmResource where account='" + acc + "'");
                if(rs.next()){
                    u.setId(rs.getInt(1));
                }
                if (rs.getCounts() > 0)
                    return updateUser(u);
            }

           //如果OA里没有的用户，不需要把ad里用户同步过来
            String synctype=Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), "ldap.synctype"));
            if(!"all".equalsIgnoreCase(synctype)&&!"".equals(synctype)){
            	return false;
            }
            //get user id
            rs.executeProc("HrmResourceMaxId_Get", "");
            rs.next();
            int id = rs.getInt(1);
            u.setId(id);
            Map attrs = BeanUtils.describe(u);
            Set cols = attrs.keySet();

            String sql_cols = "";
            String sql_vals = "";
            Class c = User.class.getDeclaredField("account").getType();
            c.getName();
            for (Iterator iter = cols.iterator(); iter.hasNext();) {
                String col = (String) iter.next();
                String val = (String) attrs.get(col);
                if (!col.equalsIgnoreCase("class") && val != null && !val.equals("") && !val.equals("-1")) {
                    if (sql_cols.equals("")) {
                        sql_cols += col;
                        if (User.class.getDeclaredField(col).getType() .getName() .equals("java.lang.String"))
                            sql_vals += "'" + val + "'";
                        else
                            sql_vals += val;
                    } else {
                        sql_cols = sql_cols + "," + col;
                        
                        if (isLdap&& col.equals("account")){
                          	 sql_vals = sql_vals + "," + "'" + dc+"\\"+val+ "'";
                          	
                          }else{
   	                        if (User.class.getDeclaredField(col).getType() .getName() .equals("java.lang.String"))
   	                            sql_vals = sql_vals + "," + "'" + val + "'";
   	                        else
   	                            sql_vals = sql_vals + "," + val;
                          }
                    }
                }
            }

         
            String managerid = (String) attrs.get("managerid");
            String managerstr = "";
            if (managerid != null && !managerid.equals("") && !managerid.equals("-1")) {
                String sql = "select managerstr from HrmResource where id = " + Util.getIntValue(managerid);
                rs.executeSql(sql);

                while (rs.next()) {
                    managerstr += rs.getString("managerstr");
                    managerstr += managerid + ",";

                    break;
                }

            }
            if (!managerstr.equals("")) {
                sql_cols = sql_cols + ",managerstr";
                sql_vals = sql_vals + "," + "'" + managerstr + "'";
            }
            String departmentid = (String) attrs.get("departmentid");
            String subcompanyid1 = "";
            if (departmentid != null && !departmentid.equals("") && !departmentid.equals("-1")) {
                subcompanyid1 = deptcominfo.getSubcompanyid1(departmentid);
            }
            if (!subcompanyid1.equals("")) {
                sql_cols = sql_cols + ",subcompanyid1";
                sql_vals = sql_vals + "," + subcompanyid1;
            }
            String sql = "insert into HrmResource (" + sql_cols + ") values (" + sql_vals + ")";
            if (l.CkHrmnum() < 0) {  //hasn't reach the max hrm number
                if (isLdap)
                	sql = "insert into HrmResource (" + sql_cols + ",lloginid) values (" + sql_vals + ",'" + Util.getEncrypt(dc+"\\"+u.getAccount())+ "')";
            }
          
            boolean flag = rs.executeSql(sql);
            BeanUtils.setProperty(u, "lastname", lastname);//回写lastname
            if (flag) {
                log.writeLog(this.getClass().getName(), "Adding User " + u.getLastname() + "...... successful!\n");
                ExportResult exportResult = new ExportResult();
                if (isLdap){
                	  exportResult.setAccount(dc+"\\"+u.getAccount());
                  }else{
                	  exportResult.setAccount(u.getAccount());    	
                  }
               
                exportResult.setLastname(u.getLastname());
                exportResult.setOperation("82");     //新建
                exportResult.setStatus("15242");    //成功
                exportResult.setDepartment(u.getDepartmentid());
                exportResult.setJobtitle(u.getJobtitle());
                exp_result.add(exportResult);
            }

            else {
                log.writeLog(this.getClass().getName(), "Adding User " + u.getLastname() + "...... fail!\n");
                ExportResult exportResult = new ExportResult();
                if (isLdap){
                	  exportResult.setAccount(dc+"\\"+u.getAccount());
                  }else{
              	  exportResult.setAccount(u.getAccount());    	
                  }
                exportResult.setLastname(u.getLastname());
                exportResult.setOperation("1421");
                exportResult.setStatus("498");
                exp_result.add(exportResult);
                return false;
            }
            char separator = Util.getSeparator();
            Calendar todaycal = Calendar.getInstance();
            String today = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-" +
                    Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-" +
                    Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);
            String userpara = "" + 1 + separator + today;
            rs.executeProc("HrmResource_CreateInfo", "" + id + separator + userpara + separator + userpara);

            resourcecominfo.addResourceInfoCache("" + id);
            SalaryManager salaryManager = new SalaryManager();
            salaryManager.initResourceSalary("" + id);

            /*
             * 导入人员默认显示顺序为人员ID
             * */
            String taxissql = ("update HrmResource set dsporder = " + id + " where id = "+id );
            rs.executeSql(taxissql);

            String para = "" + id + separator + managerid + separator + departmentid + separator + subcompanyid1 + separator + "0" + separator + managerstr;
            rs.executeProc("HrmResource_Trigger_Insert", para);

            //调用存储过程，处理人员共享信息TD9096
            //TD8636删除了触发器
            String seclevel = u.getSeclevel()+"";
			String p_para = "" + id + separator + departmentid + separator + subcompanyid1 + separator + managerid + separator + seclevel + separator + managerstr + separator + "0" + separator + "0" + separator + "0" + separator + "0" + separator + "0" + separator + "0";
			rs.executeProc("HrmResourceShare", p_para);
            String sql_1 = ("insert into HrmInfoStatus (itemid,hrmid,status) values(1," + id + ",1)");
            rs.executeSql(sql_1);
            String sql_2 = ("insert into HrmInfoStatus (itemid,hrmid) values(2," + id + ")");
            rs.executeSql(sql_2);
            String sql_3 = ("insert into HrmInfoStatus (itemid,hrmid) values(3," + id + ")");
            rs.executeSql(sql_3);

            String sql_10 = ("insert into HrmInfoStatus (itemid,hrmid) values(10," + id + ")");
            rs.executeSql(sql_10);

            String name = u.getLastname();

            String CurrentUser = "" + 1;     //sysadmin
            String CurrentUserName = "" + resourcecominfo.getResourcename(String.valueOf(1));

            String SWFAccepter = "";
            String SWFTitle = "";
            String SWFRemark = "";
            String SWFSubmiter = "";
            String Subject = "";
            Subject = SystemEnv.getHtmlLabelName(15670, 7);
            Subject += ":" + name;

            String thesql = "select hrmid from HrmInfoMaintenance where id<4 or id = 10";
            rs.executeSql(thesql);

            String members = "";
            while (rs.next()) {
                if (1 != Util.getIntValue(rs.getString("hrmid")))
                    members += "," + rs.getString("hrmid");
            }
            if (!members.equals("")) {
                members = members.substring(1);

                SWFAccepter = members;
                SWFTitle = SystemEnv.getHtmlLabelName(15670, 7);
                SWFTitle += ":" + name;
                SWFTitle += "-" + CurrentUserName;
                SWFTitle += "-" + today;
                SWFRemark = "<a href=/hrm/employee/EmployeeManage.jsp?hrmid=" + id + ">" + Util.fromScreen2(Subject, 7) + "</a>";
                SWFSubmiter = CurrentUser;
                SysRemindWorkflow sysRemindWorkflow = new SysRemindWorkflow();
                sysRemindWorkflow.setPrjSysRemind(SWFTitle, 0, Util.getIntValue(SWFSubmiter), SWFAccepter, SWFRemark);
            }
            SysMaintenanceLog sysMaintenanceLog = new SysMaintenanceLog();
            sysMaintenanceLog.resetParameter();
            sysMaintenanceLog.setRelatedId(id);
            sysMaintenanceLog.setRelatedName(u.getLastname());
            sysMaintenanceLog.setOperateItem("29");
            sysMaintenanceLog.setOperateUserid(1);
            sysMaintenanceLog.setClientAddress("rpc");
            sysMaintenanceLog.setOperateType("1");
            sysMaintenanceLog.setOperateDesc("HrmResourceBasicInfo_Insert");
            sysMaintenanceLog.setSysLogInfo();


			//把人员插入RTX里
            if(!"".equals(logid)) {
            	rtxService.addUser(id);
            }
        } catch (Exception e) {
        	log.writeLog( "错误："+e);
            return false;
        }
        return true;
    }
    
    
    public boolean addUser(User u,String departmentnametemp,String jobtitlename,String subcompanyname,String managertempname,String tempresidentnumber ) {
        try {
            //judge license
            LN l = new LN();
            
            if (l.CkHrmnum() >= 0) {  //reach the max hrm number
            	log.writeLog(this.getClass().getName(), "超出license用户数许可。");
               return false;
            }
            u.setId(-1);  //avoid dirty value
            RecordSet rs = new RecordSet();
            DepartmentComInfo deptcominfo = new DepartmentComInfo();
            ResourceComInfo resourcecominfo = new ResourceComInfo();
            String logid = BeanUtils.getSimpleProperty(u, "loginid");
            logid = this.plusQuoter(logid);//处理引号
            BeanUtils.setProperty(u, "loginid", logid);//回写loginid
            String lastname = BeanUtils.getSimpleProperty(u, "lastname");
            String lastname_new = this.plusQuoter(lastname);//处理引号
            BeanUtils.setProperty(u, "lastname", lastname_new);//回写lastname
           
            //部门，岗位转换,直接上级
            String manageidtemp=this.getmanageridBydomain(managertempname);
            BeanUtils.setProperty(u, "managerid",manageidtemp );//回写直接上级id
            String subidtemp=this.getsubidBysubName(subcompanyname);
            String deptidtemp=this.getdepidByDeptName(departmentnametemp, subidtemp);
            BeanUtils.setProperty(u, "departmentid",deptidtemp );//回写部门
            BeanUtils.setProperty(u, "jobtitle", this.getJobtitleidByName(jobtitlename, deptidtemp));//回写岗位

            //add by shaw 2017/3/29 tempresidentnumber
            BeanUtils.setProperty(u, "tempresidentnumber", tempresidentnumber);//回写dept code
            log.writeLog("tempresidentnumber="+tempresidentnumber);
             
           /*
            if (isLdap) {//workcode con't repeat
                rs.executeSql("select id from HrmResource where workcode='" + workcode +"'");
                if(rs.next()){
                    u.setId(rs.getInt(1));
                }
                if (rs.getCounts() > 0)
                {
                	rs.executeSql("select id from HrmResource where account='" + dc+"\\"+acc+ "' and workcode='" + workcode +"'" );
                	if(rs.getColCounts()>0){
                      return updateUser(u);
                	}
                	else
                	{
                		 log.writeLog(this.getClass().getName(), "account= "  + dc+"\\"+acc+ "'" + "......workcode has existed. sync ignore !\n");
                    	 return false;
                	}
                }  
            }*/
            //==========================
            String dc=u.getLdap_domainName();//域名
			int ldap_mark=Util.getIntValue(u.getLdapmark(),0);//ldap第ldap_mark个标记
            
            //modify by ds 人员导入可以导入登陆账号为空的人员
            /*
            if (isLdap && (u.getAccount() == null || u.getAccount().equals("")))  //account is must for ldap athentic
                return false;
            if (!isLdap && (logid == null || logid.equals("")))  //loginid is must for database athentic
                return false;*/
            if (!isLdap && logid != null && !logid.equals("")) {//judge if the entry has been exported or not

                rs.executeSql("select id from HrmResource where loginid='" + logid + "'");
                if(rs.next()){
                    u.setId(rs.getInt(1));
                }
                if (rs.getCounts() > 0)
                    return updateUser(u);
            }
            String acc = BeanUtils.getSimpleProperty(u, "account");
            acc = this.plusQuoter(acc);//处理引号
            BeanUtils.setProperty(u, "account", acc);//回写account
         
            if (isLdap && acc != null && !acc.equals("")) {//judge if the entry has been exported or not
            	rs.executeSql("select id from HrmResource where account='" + dc+"\\"+acc+ "'");
               // rs.executeSql("select id from HrmResource where account='" + acc + "'");
                if(rs.next()){
                    u.setId(rs.getInt(1));
                }
                if (rs.getCounts() > 0)
                    return updateUser(u);
            }

           //如果OA里没有的用户，不需要把ad里用户同步过来
            String synctype=Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), "ldap.synctype"));
            if(!"all".equalsIgnoreCase(synctype)&&!"".equals(synctype)){
            	return false;
            }
            //get user id
            rs.executeProc("HrmResourceMaxId_Get", "");
            rs.next();
            int id = rs.getInt(1);
            u.setId(id);
           
            
            Map attrs = BeanUtils.describe(u);
            Set cols = attrs.keySet();

            String sql_cols = "";
            String sql_vals = "";
            Class c = User.class.getDeclaredField("account").getType();
            c.getName();
            for (Iterator iter = cols.iterator(); iter.hasNext();) {
                String col = (String) iter.next();
                String val = (String) attrs.get(col);
                if (!col.equalsIgnoreCase("class") && val != null && !val.equals("") && !val.equals("-1")&&!("seclevel").equalsIgnoreCase(col)) {
                    if (sql_cols.equals("")) {
                        sql_cols += col;
                        if (User.class.getDeclaredField(col).getType() .getName() .equals("java.lang.String"))
                            sql_vals += "'" + val + "'";
                        else
                            sql_vals += val;
                    } else {
                        sql_cols = sql_cols + "," + col;
                        
                        if (isLdap&& col.equals("account")){
                          	 sql_vals = sql_vals + "," + "'" + dc+"\\"+val+ "'";
                          	
                          }else{
   	                        if (User.class.getDeclaredField(col).getType() .getName() .equals("java.lang.String"))
   	                            sql_vals = sql_vals + "," + "'" + val + "'";
   	                        else
   	                            sql_vals = sql_vals + "," + val;
                          }
                    }
                }
            }

          
            String managerid = (String) attrs.get("managerid");
            String managerstr = "";
            if (managerid != null && !managerid.equals("") && !managerid.equals("-1")) {
                String sql = "select managerstr from HrmResource where id = " + Util.getIntValue(managerid);
                rs.executeSql(sql);

                while (rs.next()) {
                    managerstr += rs.getString("managerstr");
                    managerstr += managerid + ",";

                    break;
                }

            }
            if (!managerstr.equals("")) {
                sql_cols = sql_cols + ",managerstr";
                sql_vals = sql_vals + "," + "'" + managerstr + "'";
            }
            
            
            String departmentid = (String) attrs.get("departmentid");
            String subcompanyid1 = "";
            if (departmentid != null && !departmentid.equals("") && !departmentid.equals("-1")) {
                subcompanyid1 = deptcominfo.getSubcompanyid1(departmentid);
            }
            if (!subcompanyid1.equals("")) {
                sql_cols = sql_cols + ",subcompanyid1";
                sql_vals = sql_vals + "," + subcompanyid1;
            }
            //add by shaw 2017/3/29
            if (!tempresidentnumber.equals("")) {
                sql_cols = sql_cols + ",tempresidentnumber";
                sql_vals = sql_vals + "," + tempresidentnumber;
            }
            String sql = "insert into HrmResource (" + sql_cols + ") values (" + sql_vals + ")";
            if (l.CkHrmnum() < 0) {  //hasn't reach the max hrm number
                if (isLdap)
                	sql = "insert into HrmResource (" + sql_cols + ",lloginid) values (" + sql_vals + ",'" + Util.getEncrypt(dc+"\\"+u.getAccount())+ "')";
            }
            
            boolean flag = rs.executeSql(sql);
            BeanUtils.setProperty(u, "lastname", lastname);//回写lastname
            if (flag) {
                log.writeLog(this.getClass().getName(), "Adding User " + u.getLastname() + "...... successful!\n");
                ExportResult exportResult = new ExportResult();
                if (isLdap){
                	  exportResult.setAccount(dc+"\\"+u.getAccount());
                  }else{
                	  exportResult.setAccount(u.getAccount());    	
                  }
               
                exportResult.setLastname(u.getLastname());
                exportResult.setOperation("82");     //新建
                exportResult.setStatus("15242");    //成功
                exportResult.setDepartment(u.getDepartmentid());
                exportResult.setJobtitle(u.getJobtitle());
                exp_result.add(exportResult);
            } else {
                log.writeLog(this.getClass().getName(), "Adding User " + u.getLastname() + "...... fail!\n");
                ExportResult exportResult = new ExportResult();
                if (isLdap){
                	  exportResult.setAccount(dc+"\\"+u.getAccount());
                  }else{
              	  exportResult.setAccount(u.getAccount());    	
                  }
                exportResult.setLastname(u.getLastname());
                exportResult.setOperation("1421");
                exportResult.setStatus("498");
                exp_result.add(exportResult);
                return false;
            }
            char separator = Util.getSeparator();
            Calendar todaycal = Calendar.getInstance();
            String today = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-" +
                    Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-" +
                    Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);
            String userpara = "" + 1 + separator + today;
            rs.executeProc("HrmResource_CreateInfo", "" + id + separator + userpara + separator + userpara);

            resourcecominfo.addResourceInfoCache("" + id);
            SalaryManager salaryManager = new SalaryManager();
            salaryManager.initResourceSalary("" + id);

            /*
             * 导入人员默认显示顺序为人员ID
             * */
            String taxissql = ("update HrmResource set dsporder = " + id + " where id = "+id );
            rs.executeSql(taxissql);

            String para = "" + id + separator + managerid + separator + departmentid + separator + subcompanyid1 + separator + "0" + separator + managerstr;
            rs.executeProc("HrmResource_Trigger_Insert", para);

            //调用存储过程，处理人员共享信息TD9096
            //TD8636删除了触发器
            String seclevel = u.getSeclevel()+"";
			String p_para = "" + id + separator + departmentid + separator + subcompanyid1 + separator + managerid + separator + seclevel + separator + managerstr + separator + "0" + separator + "0" + separator + "0" + separator + "0" + separator + "0" + separator + "0";
			rs.executeProc("HrmResourceShare", p_para);
            String sql_1 = ("insert into HrmInfoStatus (itemid,hrmid,status) values(1," + id + ",1)");
            rs.executeSql(sql_1);
            String sql_2 = ("insert into HrmInfoStatus (itemid,hrmid) values(2," + id + ")");
            rs.executeSql(sql_2);
            String sql_3 = ("insert into HrmInfoStatus (itemid,hrmid) values(3," + id + ")");
            rs.executeSql(sql_3);

            String sql_10 = ("insert into HrmInfoStatus (itemid,hrmid) values(10," + id + ")");
            rs.executeSql(sql_10);

            String name = u.getLastname();

            String CurrentUser = "" + 1;     //sysadmin
            String CurrentUserName = "" + resourcecominfo.getResourcename(String.valueOf(1));

            String SWFAccepter = "";
            String SWFTitle = "";
            String SWFRemark = "";
            String SWFSubmiter = "";
            String Subject = "";
            Subject = SystemEnv.getHtmlLabelName(15670, 7);
            Subject += ":" + name;

            String thesql = "select hrmid from HrmInfoMaintenance where id<4 or id = 10";
            rs.executeSql(thesql);

            String members = "";
            while (rs.next()) {
                if (1 != Util.getIntValue(rs.getString("hrmid")))
                    members += "," + rs.getString("hrmid");
            }
            if (!members.equals("")) {
                members = members.substring(1);

                SWFAccepter = members;
                SWFTitle = SystemEnv.getHtmlLabelName(15670, 7);
                SWFTitle += ":" + name;
                SWFTitle += "-" + CurrentUserName;
                SWFTitle += "-" + today;
                SWFRemark = "<a href=/hrm/employee/EmployeeManage.jsp?hrmid=" + id + ">" + Util.fromScreen2(Subject, 7) + "</a>";
                SWFSubmiter = CurrentUser;
                SysRemindWorkflow sysRemindWorkflow = new SysRemindWorkflow();
                sysRemindWorkflow.setPrjSysRemind(SWFTitle, 0, Util.getIntValue(SWFSubmiter), SWFAccepter, SWFRemark);
            }
            SysMaintenanceLog sysMaintenanceLog = new SysMaintenanceLog();
            sysMaintenanceLog.resetParameter();
            sysMaintenanceLog.setRelatedId(id);
            sysMaintenanceLog.setRelatedName(u.getLastname());
            sysMaintenanceLog.setOperateItem("29");
            sysMaintenanceLog.setOperateUserid(1);
            sysMaintenanceLog.setClientAddress("rpc");
            sysMaintenanceLog.setOperateType("1");
            sysMaintenanceLog.setOperateDesc("HrmResourceBasicInfo_Insert");
            sysMaintenanceLog.setSysLogInfo();


			//把人员插入RTX里
            if(!"".equals(logid)) {
            	rtxService.addUser(id);
            }
        } catch (Exception e) {
        	log.writeLog( "错误："+e);
            return false;
        }
        return true;
    }

    /**
     * update user according to id or loginid or account
     * add tempresidentnumber by shaw 2017/3/29
     * @param u
     * @return success
     */
    public boolean updateUser(User u) {

        try {
            LN l = new LN();
            if (l.CkHrmnum() >= 0) {  //reach the max hrm number
                return false;
            }
            String id = "" + u.getId();
            RecordSet rs = new RecordSet();
            DepartmentComInfo deptcominfo = new DepartmentComInfo();
            ResourceComInfo resourcecominfo = new ResourceComInfo();
            OrganisationCom organisationcom = new OrganisationCom();
            if (isLdap) {
                if (u.getAccount() == null || u.getAccount().equals(""))
                    return false;
                while (resourcecominfo.next()) {
                    if (resourcecominfo.getAccount().equals(u.getAccount())) {
                        id = resourcecominfo.getResourceid();
                        break;
                    }
                }


            } else {
                if ((id == null || id.equals("")) && u.getLoginid() != null && !u.getLoginid() .equals("")) {
                    while (resourcecominfo.next()) {
                        if (resourcecominfo.getLoginID().equals(u.getLoginid())) {
                            id = resourcecominfo.getResourceid();
                            break;
                        }
                    }

                }
            }

            if (id == null || id.equals(""))
                return false;

            Map attrs = BeanUtils.describe(u);
            Set cols = attrs.keySet();

            String sql_set = "";
            for (Iterator iter = cols.iterator(); iter.hasNext();) {
                String col = (String) iter.next();
                String val = (String) attrs.get(col);
                if("id".equalsIgnoreCase(col)){
                	continue;
                }
                
                if (!col.equalsIgnoreCase("class")&&!col.equalsIgnoreCase("loginid")&&!col.equalsIgnoreCase("account")&&!col.equalsIgnoreCase("systemlanguage")&&!col.equalsIgnoreCase("costcenterid")&&!col.equalsIgnoreCase("status") && val != null && !val.equals("") && !val.equals("-1")&&!("seclevel").equalsIgnoreCase(col)) {
                    if (sql_set.equals("")) {

                        if (User.class.getDeclaredField(col).getType() .getName() .equals("java.lang.String")){
                        	
                            if(val==null)
                            {
                         	   sql_set = col + "=''";
                            }
                            else{
                         	   sql_set = col + "='" + val + "'";
                            }
                        }
                           
                        else{
                        	 if(val==null)
                             {
                        		 sql_set = col + "=''";
                             }
                             else{
                            	 sql_set = col + "=" + val;
                             }
                     }
                    } else
                    if (User.class.getDeclaredField(col).getType() .getName() .equals("java.lang.String")){
                    	 if(val==null)
                         {
                			 sql_set = sql_set + "," + col + "=''";
                         }
                         else{
                        	 sql_set = sql_set + "," + col + "='" + val + "'";
                         }
                    }
                    else
                    {
                    	 if(val==null)
                         {
                    		 sql_set = sql_set + "," + col + "=''";
                         }
                         else{
                        	 sql_set = sql_set + "," + col + "=" + val;
                         }
                    }
                }
            }
            char separator = Util.getSeparator();
            String oldmanagerstr = "";
    		String olddepartmentid = "";
    		String oldmanagerid = "";
    		String oldsubcompanyid1 = "";
    		String oldseclevel = "";
    		//add by shaw 2017/3/29
            String oldtempresidentnumber = "";
            String sql = "select * from HrmResource where id = " + id;
            rs.executeSql(sql);
            while (rs.next()) {
                oldmanagerstr = rs.getString("managerstr");
        		olddepartmentid = rs.getString("departmentid");
        		oldmanagerid = rs.getString("managerid");
        		oldsubcompanyid1 = rs.getString("subcompanyid1");
        		oldseclevel = rs.getString("seclevel");
        		//add by shaw 2017/3/29
                oldtempresidentnumber = rs.getString("tempresidentnumber");
            }

            String managerid = (String) attrs.get("managerid");
             //String managerid = (String) attrs.get("managerid");
            String managerstr = "";
            if (managerid != null && !managerid.equals("-1") && !managerid.equals("")) {
                sql = "select managerstr from HrmResource where id = " + Util.getIntValue(managerid);
                rs.executeSql(sql);

                while (rs.next()) {
                    managerstr += rs.getString("managerstr");
                    managerstr += managerid + ",";
                    break;
                }
                if (!managerstr.equals("")) {
                    sql_set = sql_set + ",managerstr='" + managerstr + "'";
                    sql = "update HrmResource_Trigger set managerstr ='" + managerstr + "'  where id=" + id;
                    rs.executeSql(sql);
                }
            }
            String departmentid = (String) attrs.get("departmentid");
           
            log.writeLog("部门id＝＝＝"+departmentid);
            String subcompanyid1 = "";
            if (departmentid != null &&!departmentid.equals("-1") && !departmentid.equals("")) {
                subcompanyid1 = deptcominfo.getSubcompanyid1(departmentid);
                if (!subcompanyid1.equals("")) {
                    sql_set = sql_set + ",subcompanyid1=" + subcompanyid1;
                    sql = "update HrmResource_Trigger set subcompanyid1 =" + subcompanyid1 + "  WHERE id =" + id;
                    log.writeLog("updateCompany＝＝＝"+sql);
                    rs.executeSql(sql);
                }
            }
            //by cyril on 2008-09-19 for td:9361 AD中是没有部门和机构的
            else {
            	departmentid = olddepartmentid;
            	subcompanyid1 = oldsubcompanyid1;
            }
            //end by cyril on 2008-09-19 for td:9361

            //add by shaw 2017/3/29
            String tempresidentnumber = (String) attrs.get("tempresidentnumber");
            //String tempresidentnumber = "12345";
            log.writeLog("tempresidentnumber="+tempresidentnumber);
            if (!tempresidentnumber.equals("")) {
                sql_set = sql_set + ",tempresidentnumber=" + tempresidentnumber;
            }
            
            sql = "update HrmResource set " + sql_set + "  where id=" + id;
            log.writeLog("update emp sql:"+sql);
            if (l.CkHrmnum() < 0) {  //hasn't reach the max hrm number
                if (isLdap)
                    sql = "update HrmResource set " + sql_set  + "  where id=" + id;
            }
            boolean flag = rs.executeSql(sql);
            String lastname = BeanUtils.getSimpleProperty(u, "lastname");
            lastname = this.minusQuoter(lastname);
            BeanUtils.setProperty(u, "lastname", lastname);//回写lastname
            if (flag) {
                //调用存储过程，处理人员共享信息TD9096
                //TD8636删除了触发器
                //String seclevel = u.getSeclevel()+"";
            	String seclevel = oldseclevel;
    			String p_para = "" + id + separator + departmentid + separator + subcompanyid1 + separator + managerid + separator + seclevel + separator + managerstr + separator + olddepartmentid + separator + oldsubcompanyid1 + separator + oldmanagerid + separator + oldseclevel + separator + oldmanagerstr + separator + "1";
    			rs.executeProc("HrmResourceShare", p_para);

                log.writeLog(this.getClass().getName(), "Updating User " + u.getLastname() + "...... successful!\n");
                ExportResult exportResult = new ExportResult();
                exportResult.setAccount(u.getAccount());
                exportResult.setLastname(u.getLastname());
                exportResult.setOperation("17744");
                exportResult.setStatus("15242");
                exportResult.setDepartment(u.getDepartmentid());
                exportResult.setJobtitle(u.getJobtitle());
                exp_result.add(exportResult);
            } else {
                log.writeLog(this.getClass().getName(), "Updating User " + u.getLastname() + "...... fail!\n");
                ExportResult exportResult = new ExportResult();
                exportResult.setAccount(u.getAccount());
                exportResult.setLastname(u.getLastname());
                exportResult.setOperation("17744");
                exportResult.setStatus("498");
                exp_result.add(exportResult);
                return false;
            }
            if (!managerstr.equals("")) {
            sql = "select id,managerstr from HrmResource where managerstr like '" + oldmanagerstr + id + ",%'";
            rs.executeSql(sql);
            while (rs.next()) {
                String nowmanagerstr = Util.null2String(rs.getString("managerstr"));
                String resourceid = rs.getString("id");
                nowmanagerstr = Util.StringReplaceOnce(nowmanagerstr, oldmanagerstr, managerstr);
                String para = resourceid + separator + nowmanagerstr;
                rs.executeProc("HrmResource_UpdateManagerStr", para);
            }
            }
            //同步RTX端的用户信息.
            organisationcom.editUser(Integer.parseInt(id));

            // 改为自进行修正
            resourcecominfo.updateResourceInfoCache(id);
            SysMaintenanceLog sysMaintenanceLog = new SysMaintenanceLog();
            sysMaintenanceLog.resetParameter();
            sysMaintenanceLog.setRelatedId(Util.getIntValue(id));
            sysMaintenanceLog.setRelatedName(u.getLastname());
            sysMaintenanceLog.setOperateItem("29");
            sysMaintenanceLog.setOperateUserid(1);
            sysMaintenanceLog.setClientAddress("rpc");
            sysMaintenanceLog.setOperateType("2");
            sysMaintenanceLog.setOperateDesc("HrmResourceBasicInfo_Update");
            sysMaintenanceLog.setSysLogInfo();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public SimpleUser getUserById(String id) throws Exception {
        ResourceComInfo resourcecominfo = new ResourceComInfo();
        resourcecominfo.setTofirstRow();
        SimpleUser u = null;
        while (resourcecominfo.next()) {
            if (resourcecominfo.getResourceid().equals(id)) {
                u = new SimpleUser();
                u.setAccount(resourcecominfo.getAccount());
                u.setDepartmentid(resourcecominfo.getDepartmentID());
                u.setId(resourcecominfo.getResourceid());
                u.setLastname(resourcecominfo.getLastname());
                u.setLoginid(resourcecominfo.getLoginID());
                u.setManagerid(resourcecominfo.getManagerID());
                u.setSex(resourcecominfo.getSex());
                u.setStatus(resourcecominfo.getStatus());
            }
        }
        return u;
    }

    public SimpleUser getUserByAccount(String account) throws Exception {
        ResourceComInfo resourcecominfo = new ResourceComInfo();
        resourcecominfo.setTofirstRow();
        SimpleUser u = null;
        while (resourcecominfo.next()) {
            if (resourcecominfo.getAccount().equals(account)) {
                u = new SimpleUser();
                u.setAccount(resourcecominfo.getAccount());
                u.setDepartmentid(resourcecominfo.getDepartmentID());
                u.setId(resourcecominfo.getResourceid());
                u.setLastname(resourcecominfo.getLastname());
                u.setLoginid(resourcecominfo.getLoginID());
                u.setSex(resourcecominfo.getSex());
                u.setStatus(resourcecominfo.getStatus());
                u.setManagerid(resourcecominfo.getManagerID());
            }
        }
        return u;
    }

    public SimpleUser getUserByDeptId(String deptid) throws Exception {
        ResourceComInfo resourcecominfo = new ResourceComInfo();
        resourcecominfo.setTofirstRow();
        SimpleUser u = null;
        while (resourcecominfo.next()) {
            if (resourcecominfo.getDepartmentID().equals(deptid)) {
                u = new SimpleUser();
                u.setAccount(resourcecominfo.getAccount());
                u.setDepartmentid(resourcecominfo.getDepartmentID());
                u.setId(resourcecominfo.getResourceid());
                u.setLastname(resourcecominfo.getLastname());
                u.setLoginid(resourcecominfo.getLoginID());
                u.setSex(resourcecominfo.getSex());
                u.setStatus(resourcecominfo.getStatus());
                u.setManagerid(resourcecominfo.getManagerID());
            }
        }
        return u;
    }

    public SimpleUser[] getUsers() throws Exception {
        ResourceComInfo resourcecominfo = new ResourceComInfo();
        List l_users = new ArrayList();
        resourcecominfo.setTofirstRow();
        while (resourcecominfo.next()) {
            SimpleUser u = new SimpleUser();
            u.setAccount(resourcecominfo.getAccount());
            u.setDepartmentid(resourcecominfo.getDepartmentID());
            u.setId(resourcecominfo.getResourceid());
            u.setLastname(resourcecominfo.getLastname());
            u.setLoginid(resourcecominfo.getLoginID());
            u.setSex(resourcecominfo.getSex());
            u.setStatus(resourcecominfo.getStatus());
            u.setManagerid(resourcecominfo.getManagerID());
            l_users.add(u);
        }

        SimpleUser[] users = new SimpleUser[l_users.size()];
        l_users.toArray(users);
        return users;
    }

    public SimpleDepartment[] getDepartments() throws Exception {
        DepartmentComInfo deptcominfo = new DepartmentComInfo();
        List l_depts = new ArrayList();
        deptcominfo.setTofirstRow();
        while (deptcominfo.next()) {
            SimpleDepartment dept = new SimpleDepartment();
            dept.setId(deptcominfo.getDepartmentid());
            dept.setName(deptcominfo.getDepartmentmark());
            dept.setDescription(deptcominfo.getDepartmentname());
            dept.setSubcompany1(deptcominfo.getSubcompanyid1());
            dept.setSupdeptid(deptcominfo.getDepartmentsupdepid());
            dept.setParentid(deptcominfo.getDepartmentsupdepid());
            l_depts.add(dept);
        }

        SimpleDepartment[] depts = new SimpleDepartment[l_depts.size()];
        l_depts.toArray(depts);
        return depts;
    }

    public SimpleSubCompany[] getSubcompanys() throws Exception {
        SubCompanyComInfo subcominfo = new SubCompanyComInfo();
        List l_subs = new ArrayList();
        subcominfo.setTofirstRow();
        while (subcominfo.next()) {
            SimpleSubCompany sub = new SimpleSubCompany();
            sub.setId(subcominfo.getSubCompanyid());
            sub.setName(subcominfo.getSubCompanyname());
            sub.setDescription(subcominfo.getSubCompanydesc());
            sub.setParentid(subcominfo.getSupsubcomid());
            l_subs.add(sub);
        }

        SimpleSubCompany[] subs = new SimpleSubCompany[l_subs.size()];
        l_subs.toArray(subs);
        return subs;
    }

    public List getExp_result() {
        return exp_result;
    }

    public void setExp_result(List exp_result) {
        this.exp_result = exp_result;
    }

    public ExportResult[] exportLdap() {
        List result = LdapUtil.getInstance().export();
        return (ExportResult[]) result.toArray(new ExportResult[result.size() ]);
    }
    public ExportResult[] exportLdapByTime(String time) {
        List result = LdapUtil.getInstance().exportByTime(time);
        return (ExportResult[]) result.toArray(new ExportResult[result.size() ]);
    }
    
    /**
     * 处理引号，1个单引号变成2个单引号
     * @param s
     * @return
     */
    private String plusQuoter(String s){
    	String result = Util.null2String(s);
    	if(result.indexOf("'")>-1){
    		result = result.replaceAll("'", "''");
    	}
    	
    	return result;
    }
    /**
     * 处理引号，2个单引号变成1个单引号
     * @param s
     * @return
     */
    private String minusQuoter(String s){
    	String result = Util.null2String(s);
    	if(result.indexOf("''")>-1){
    		result = result.replaceAll("''", "'");
    	}
    	
    	return result;
    }
    
    /**
     * 由岗位名称得到岗位id,没有新建
     * 
     * @param jobtitleName
     * @param jobdepartmentid
     * @param jobactivityid
     * @return
     */
    public String getJobtitleidByName(String jobtitleName,String jobdepartmentid){
    	String returnstr  = "";
    	
    	RecordSet rs = new RecordSet();
    	RecordSet rs2 = new RecordSet();
    	if(jobtitleName.equals("")){
    		return "";
    	}
    	String sql = "select id from HrmJobTitles where jobtitlename='"+jobtitleName+"' and jobdepartmentid='"+jobdepartmentid+"'";
        rs.executeSql(sql);
        if(rs.next()){
        	returnstr = Util.null2String(rs.getString("id"));
        }
        
    	return returnstr;
    }
    
    
	 public  String getsubidBysubName(String subName){
		  
	    	RecordSet rs=new RecordSet();
	    
	    	String subid="";
	    	log.writeLog("分部名称＝＝＝"+subName);
			rs.execute("select id from hrmsubcompany where  subcompanyname='"+subName+"'");
			log.writeLog("分部名称＝＝＝"+"select id from hrmsubcompany where  subcompanyname='"+subName+"'");
			if(rs.next())
			{
				subid= Util.null2String(rs.getString("id"));
			}
			log.writeLog("分部id＝＝＝"+subid);
	    	return subid;
	    	
	    }
	 
	 public  String getdepidByDeptName(String deptName,String subcompanyid1){
		 RecordSet rs=new RecordSet();
		 
			String depid="";
			rs.execute("select id from hrmdepartment where departmentname='" + deptName + "' and subcompanyid1='"+subcompanyid1+"'");
			log.writeLog("查找部门＝＝＝"+"select id from hrmdepartment where departmentname='" + deptName + "' and subcompanyid1='"+subcompanyid1+"'");
			log.writeLog("部门名称＝＝＝"+deptName+",分部id,"+subcompanyid1);
			if(rs.next())
			{
				depid=Util.null2String(rs.getString("id"));	
			}
			
			log.writeLog("部门id＝＝＝"+depid);
			return depid;
		}
	 

	 public  String getmanageridBydomain(String dm){
		 log.writeLog("上级标志＝＝＝"+dm);
		  int cn=dm.indexOf("CN=");
		 if(cn<0){
			 return "";
		 }
		 String name="";
		 String domainname="";
		  String temp=dm.substring(cn+3);
		  name=temp.substring(0,temp.indexOf(","));
		  
		 /* int dc=temp.indexOf("DC=");
		  if(cn<0){
				 return "";
			 }
		  temp=temp.substring(dc+3);
		  domainname=temp.substring(0,temp.indexOf(","));
		  
		  String managername=domainname+"\\"+name;
		  */
	    	RecordSet rs=new RecordSet();
	    
	    	String hrmid="";
	    	
			rs.execute("select id from hrmresource where  lastname='"+name+"'");
			log.writeLog("查找上级＝＝＝"+"select id from hrmresource where  lastname='"+name+"'");
			if(rs.next())
			{
				hrmid= Util.null2String(rs.getString("id"));
			}

	    	return hrmid;
	    	
	    }
}
