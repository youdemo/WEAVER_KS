package weaver.login;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;

import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.StaticObj;
import weaver.general.Util;
import weaver.hrm.OnLineMonitor;
import weaver.hrm.User;
import weaver.hrm.settings.ChgPasswdReminder;
import weaver.hrm.settings.RemindSettings;
import weaver.ldap.LdapUtil;
import weaver.sms.SMSManager;
import ln.LN;
import weaver.systeminfo.SysMaintenanceLog;

public class VerifyLoginDomain extends BaseBean{

	public String getUserCheck(HttpServletRequest request, HttpServletResponse response, String domainname, String username, String login_type, String login_file, String messages, String languid, boolean ismutilangua) throws Exception {
		String message = "";
		StaticObj staticobj = null;
		staticobj = StaticObj.getInstance();
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();

		Calendar today = Calendar.getInstance();
		String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
		String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);

		try {
			String isLicense = (String) staticobj.getObject("isLicense");
			//国生 2004-9-16 , TD1096 , License的日期限制不起作用
			LN ckLicense = new LN();
			message = ckLicense.CkLicense(currentdate);
			if (!message.equals("1")) {
				return "19";
			} else {
				staticobj.putObject("isLicense", "true");
			}

            //检测用户并发数start
			String concurrentFlag = Util.null2String(ckLicense.getConcurrentFlag());
			int hrmnumber = Util.getIntValue(ckLicense.getHrmnum());
			if("1".equals(concurrentFlag)){
				LicenseCheckLogin  lchl = new LicenseCheckLogin();
				if(lchl.getLicUserCheck(username,hrmnumber)){
					return "26";
				}
			}
            //检测用户并发数end

			message = "";

			String software = (String) staticobj.getObject("software");
			String portal = "n";
			String multilanguage = "n";
			if (software == null) {
				rs.executeSql("select * from license");
				if (rs.next()) {
					software = rs.getString("software");
					if (software.equals("")) software = "ALL";
					staticobj.putObject("software", software);
					portal = rs.getString("portal");
					if (portal.equals("")) portal = "n";
					staticobj.putObject("portal", portal);
					multilanguage = rs.getString("multilanguage");
					if (multilanguage.equals("")) multilanguage = "n";
					staticobj.putObject("multilanguage", multilanguage);
				}
			}

			if (login_type.equals("1")) {
				//判断域名是否相同
				String domain_system = "";
				try{
					domain_system = Util.null2String(this.getPropValue("ldap", "domain"));
				}catch(Exception e){
					domain_system = "";
				}
				domainname = trimAllSpace(domainname);
				username = username.trim();
				if(!domainname.equalsIgnoreCase(domain_system)){
					return "61";
				}
				String mode = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
				//sql要处理大小写通配。Oarcle和SQL Server下是一样的
				if (mode != null && mode.equals("ldap")){
					username=domainname+"\\"+username;
					rs.executeSql("select * from HrmResource where upper(account)=upper('" + username + "')");
				}else{
					rs.executeSql("select * from HrmResource where upper(loginid)=upper('" + username + "')");
				}
				if (!rs.next()){
					return "17";
				}
				int tmpid = rs.getInt("id");
				String startdate = rs.getString("startdate");
				String enddate = rs.getString("enddate");
				int status = rs.getInt("status");
				if (status != 0 && status != 1 && status != 2 && status != 3) {
					return "17";
				}
				User user = new User();

				user.setUid(tmpid);
				user.setLoginid(username);
				user.setFirstname(rs.getString("firstname"));
				user.setLastname(rs.getString("lastname"));
				user.setAliasname(rs.getString("aliasname"));
				user.setTitle(rs.getString("title"));
				user.setTitlelocation(rs.getString("titlelocation"));
				user.setSex(rs.getString("sex"));
				//user.setPwd(user_password);//登陆BBS时使用，现在也不需要密码
                String languageidweaver = rs.getString("systemlanguage");
                if(!languid.equalsIgnoreCase(languageidweaver) && ismutilangua){
                	rs2.execute("update hrmresource set  systemlanguage = "+languid+" where id ="+rs.getInt("id"));
                	languageidweaver = languid;
                }
                user.setLanguage(Util.getIntValue(languageidweaver, 0));

				user.setTelephone(rs.getString("telephone"));
				user.setMobile(rs.getString("mobile"));
				user.setMobilecall(rs.getString("mobilecall"));
				user.setEmail(rs.getString("email"));
				user.setCountryid(rs.getString("countryid"));
				user.setLocationid(rs.getString("locationid"));
				user.setResourcetype(rs.getString("resourcetype"));
				user.setStartdate(startdate);
				user.setEnddate(enddate);
				user.setContractdate(rs.getString("contractdate"));
				user.setJobtitle(rs.getString("jobtitle"));
				user.setJobgroup(rs.getString("jobgroup"));
				user.setJobactivity(rs.getString("jobactivity"));
				user.setJoblevel(rs.getString("joblevel"));
				user.setSeclevel(rs.getString("seclevel"));
				user.setUserDepartment(Util.getIntValue(rs.getString("departmentid"), 0));
				user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
				user.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"), 0));
				user.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"), 0));
				user.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"), 0));
				user.setManagerid(rs.getString("managerid"));
				user.setAssistantid(rs.getString("assistantid"));
				user.setPurchaselimit(rs.getString("purchaselimit"));
				user.setCurrencyid(rs.getString("currencyid"));
				user.setLastlogindate(currentdate);
				user.setLogintype("1");
				user.setAccount(rs.getString("account"));

				user.setLoginip(Util.getIpAddr(request));
				request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
				request.getSession(true).setAttribute("weaver_user@bean", user);
				//request.getSession(true).setAttribute("moniter", new OnLineMonitor(""+user.getUID(), user.getLoginip()));
//			  多帐号登陆
				if(user.getUID() != 1){	//is not sysadmin
					List accounts = this.getAccountsById(tmpid);
					request.getSession(true).setAttribute("accounts", accounts);
				}
				Util.setCookie(response, "loginfileweaver", login_file, 172800);
				Util.setCookie(response, "loginidweaver", username, 172800);
				
				Util.setCookie(response, "loginfileweaver", login_file, 172800);
				Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
				Util.setCookie(response, "languageidweaver", languageidweaver, 172800);

				char separater = Util.getSeparator();
				rs.execute("HrmResource_UpdateLoginDate", ""+tmpid + separater + currentdate);

				SysMaintenanceLog log = new SysMaintenanceLog();
				log.resetParameter();
				log.setRelatedId(tmpid);
				log.setRelatedName((rs.getString("firstname") + " " + rs.getString("lastname")).trim());
				log.setOperateType("6");
				log.setOperateDesc("");
				log.setOperateItem("60");
				log.setOperateUserid(tmpid);
				log.setClientAddress(Util.getIpAddr(request));
				log.setSysLogInfo();

				//检查是否有客户联系计划
/* 刘煜修改 2004－05－08 登录检查客户联系计划以前是选取所有的记录，并不需要，现在更改为选择总数 */
				char flag = 2;
				String sql = "";
				rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + tmpid + flag + "0" + flag + "0");
				sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + tmpid;
				rs1.executeSql(sql);
				if (rs1.next()) {
					if (Util.getIntValue(rs1.getString(1), 0) > 0)
						rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + tmpid + flag + "0" + flag + "1");
				} else {
				  return "15";
				}
			}else{
				return "17";
			}
		} catch (Exception e) {
			writeLog(e);
			throw e;
		}
		return message;
	}

	public List getAccountsById(int id) {
		RecordSet rs = new RecordSet();
		List l = new ArrayList();
		String sql = "select id,loginid,subcompanyid1,departmentid,jobtitle,accounttype,belongto from hrmresource where id=" + id +" and status<4";

		rs.executeSql(sql);
		if (rs.next()) {
			String accounttype = rs.getString("accounttype");
			if (accounttype == null || accounttype.equals("") || accounttype.equals("0")) { //the logging account is a major account
				Account major_account = new Account();
				major_account.setId(rs.getInt("id"));
				major_account.setSubcompanyid(rs.getInt("subcompanyid1"));
				major_account.setDepartmentid(rs.getInt("departmentid"));
				major_account.setJobtitleid(rs.getInt("jobtitle"));
				major_account.setType(0);
				major_account.setAccount(rs.getString("loginid"));
				l.add(major_account);
				sql = "select id,loginid,subcompanyid1,departmentid,jobtitle,accounttype,belongto from hrmresource where accounttype=1 and belongto=" + rs.getInt("id")+" and status<4";
				rs.executeSql(sql);
				while (rs.next()) {
					Account accessory_account = new Account();
					accessory_account.setId(rs.getInt("id"));
					accessory_account.setSubcompanyid(rs.getInt("subcompanyid1"));
					accessory_account.setDepartmentid(rs.getInt("departmentid"));
					accessory_account.setJobtitleid(rs.getInt("jobtitle"));
					accessory_account.setType(1);
					accessory_account.setAccount(rs.getString("loginid"));
					l.add(accessory_account);
				}
				return l;
			} else {   //the logging account is a accessory account
				String major_id = rs.getString("belongto");
				sql = "select id,loginid,subcompanyid1,departmentid,jobtitle,accounttype,belongto from hrmresource where id=" + major_id+" and status<4";
				rs.executeSql(sql);
				if (rs.next()) {
					Account major_account = new Account();
					major_account.setId(rs.getInt("id"));
					major_account.setSubcompanyid(rs.getInt("subcompanyid1"));
					major_account.setDepartmentid(rs.getInt("departmentid"));
					major_account.setJobtitleid(rs.getInt("jobtitle"));
					major_account.setType(0);
					major_account.setAccount(rs.getString("loginid"));
					l.add(major_account);
					sql = "select id,loginid,subcompanyid1,departmentid,jobtitle,accounttype,belongto from hrmresource where belongto=" + major_id+" and status<4";
					rs.executeSql(sql);
					while (rs.next()) {
						Account accessory_account = new Account();
						accessory_account.setId(rs.getInt("id"));
						accessory_account.setSubcompanyid(rs.getInt("subcompanyid1"));
						accessory_account.setDepartmentid(rs.getInt("departmentid"));
						accessory_account.setJobtitleid(rs.getInt("jobtitle"));
						accessory_account.setType(1);
						accessory_account.setAccount(rs.getString("loginid"));
						l.add(accessory_account);
					}
					return l;
				} else
					return null;
			}

		} else
			return null;
	}	

    public String trimAllSpace(String s){
    	//writeLog("s = " + s);
    	String r = "";
    	s = Util.null2String(s).trim();
    	try{
    		int l = s.length();
            for(int i=1; i<=l; i++){
            	String t = s.substring(0, 1);
            	t = t.trim();
            	//writeLog("t" + i + " = " + t);
            	s = s.substring(1);
            	//writeLog("t" + i + " boolean = " + ("".equals(t)));
            	if(!"".equals(t)){
            		r += t;
            	}
            }
    		r = r.replaceAll("\\s", "");
    		r = r.replaceAll("\\t", "");
    	}catch(Exception e){
    		writeLog(e);
    		r = s;
    	}
    	return r;
    }


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
