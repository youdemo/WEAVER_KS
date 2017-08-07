package weaver.login;

import weaver.general.*;
import weaver.conn.*;

import javax.servlet.*;
import javax.servlet.http.*;

import weaver.hrm.*;
import weaver.hrm.resource.ResourceComInfo;

import java.text.SimpleDateFormat;
import java.util.*;

import weaver.general.StaticObj;
import weaver.systeminfo.SysMaintenanceLog;

import java.sql.Timestamp;

import ln.LN;
import weaver.file.Prop;
import weaver.ldap.LdapUtil;
import org.w3c.dom.Document;

import HT.HTSrvAPI;

import weaver.hrm.settings.RemindSettings;
import weaver.hrm.settings.ChgPasswdReminder;
import weaver.hrm.settings.HrmSettingsComInfo;
import weaver.usb.UsbKeyProxy;
import weaver.sms.SMSManager;
import weaver.general.*;

/**
 * Title:        wahaha project
 * Description:  for wahaha product
 * Copyright:    Copyright (c) 1995
 * Company:      weaver
 *
 * @author liuyu
 * @version 1.0
 */

public class VerifyLogin extends BaseBean {

    public String getUserCheck(HttpServletRequest request, HttpServletResponse response, String login_id, String user_password, String serial, String username, String rnd, String login_type, String login_file,String validateCode,String messages,String languid,boolean ismutilangua) throws Exception {
    	StaticObj staticobj = null; 
        staticobj = StaticObj.getInstance(); 
        String frommail=Util.null2String(request.getParameter("frommail"));//��Ի���Ҫ���ʼ�ֱ�ӵ�½�������ǰ�ȫ�ԣ��ڲ�ʹ�ã�
        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        RecordSet rs2 = new RecordSet();
        char separator = Util.getSeparator();
        String message = "";
        boolean MOREACCOUNTLANDING = GCONST.getMOREACCOUNTLANDING();
        Calendar today = Calendar.getInstance();
        String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
        String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);


        try {

            String isLicense = (String) staticobj.getObject("isLicense");
            //���� 2004-9-16 , TD1096 , License���������Ʋ�������
            LN ckLicense = new LN();
            message=ckLicense.CkLicense(currentdate);
            if (!message.equals("1")) {
                return "19";
            } else {
                staticobj.putObject("isLicense", "true");
            }

			//����û�������start
			String concurrentFlag = Util.null2String(ckLicense.getConcurrentFlag());
			int hrmnumber = Util.getIntValue(ckLicense.getHrmnum());
			if("1".equals(concurrentFlag)){
				LicenseCheckLogin  lchl = new LicenseCheckLogin();
				if(lchl.getLicUserCheck(login_id,hrmnumber)){
					recordFefuseLogin(login_id); //�ܾ���½��¼
					return "26";
				}
			}
            //����û�������end
            
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
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();

                String mode = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
                String usbserver = Prop.getPropValue(GCONST.getConfigFile(), "usbserver.ip");

                int needvalidate=settings.getNeedvalidate();//0: ��,1: ��
                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }
                
                /***���ADͬ�������Ȩ����Ա���ܵ�¼***/
                String ldapmark="";
                boolean isloginlpad = true;
                if (mode != null && mode.equals("ldap")){
                	rs.executeSql("select * from HrmResourceManager where loginid = '"+login_id+"'");
                	if(rs.next()) isloginlpad = false;
                }

                if (mode != null && mode.equals("ldap") && isloginlpad) {
                    LdapUtil util = LdapUtil.getInstance();
                    rs.executeSql("select ldapmark from HrmResource where account='" + login_id + "'");
                	if(rs.next()){
                		ldapmark=rs.getString("ldapmark");	
                	}
                	boolean flag = util.authentic(login_id, user_password,ldapmark);  
                    
                    rs.executeSql("select * from HrmResource where account='" + login_id + "'");
                    
                    if (flag) {
                        if (!rs.next())
                            return "17";
                        else {
							int tmpid = rs.getInt("id");
                            int needdynapass_usr = rs.getInt("needdynapass");
                            int needdynapass_sys = settings.getNeeddynapass();
                            int dynapasslen = settings.getDynapasslen();
                            String dypadcon = Util.null2String(settings.getDypadcon());
                            int passwordstateip = 1;
                            if(needdynapass_sys==1) {
                            	String sql = "select password,passwordstate from HrmResource where account='"+login_id+"'";
                            	rs2.executeSql(sql);
                            	if(rs2.next()) passwordstateip = rs2.getInt("passwordstate");
                            }
                            boolean ipaddress = checkIpSeg(request, login_id, passwordstateip);//������β���
                            if (needdynapass_usr == 1 && needdynapass_sys == 1 && dynapasslen > 0 && ipaddress) {
                                String dynapass = "";
                                if(dypadcon.equals("0")){
                                	dynapass = Util.passwordBuilderNo(dynapasslen);
                                }else if (dypadcon.equals("1")){
                                	dynapass = Util.passwordBuilderEn(dynapasslen);
                                }else if (dypadcon.equals("2")){
                                	 dynapass = Util.passwordBuilder(dynapasslen);
                                }
                                SMSManager sm = new SMSManager();
                                String mobileno = rs.getString("mobile");
                                boolean sendflag = sm.sendSMS(mobileno, "����"+currentdate+" "+currenttime+"��¼ϵͳ�Ķ�̬����Ϊ��" + dynapass);
                                if (sendflag) {
                                    rs.executeSql("update hrmpassword set password='" + Util.getEncrypt(dynapass) + "' where id=" + tmpid);
                                    return "101";
                                }
                            }
                        }
                    } else {
                        if (!rs.next())
                            return "17";
                        else {
							int tmpid = rs.getInt("id");
                            int needdynapass_usr = rs.getInt("needdynapass");
                            int needdynapass_sys = settings.getNeeddynapass();
                            int dynapasslen = settings.getDynapasslen();
                            if (needdynapass_usr == 1 && needdynapass_sys == 1 && dynapasslen > 0) {
                                rs1.executeSql("select * from hrmpassword where id=" + tmpid + " and password='" + Util.getEncrypt(user_password)+"'");
                            if (rs1.next())
                                rs1.executeSql("update hrmpassword set password='' where id=" + tmpid);
                            else
                                return "16";
                            }else
                                return "16";

                        }


                    }
                } else {

                    String[] loginCheck = checkUserPass(request,login_id,user_password,messages);
                    if (frommail.equals("1")) loginCheck=checkUserPassM(login_id,user_password);
                    if(loginCheck[0].equals("-2"))
                        return "55";
                    else if(loginCheck[0].equals("-1"))
                        return "17";
                    else if(loginCheck[1].equals("0"))
                            return "16";
                    else if(loginCheck[1].equals("101"))
                            return "101";
                    else if(loginCheck[1].equals("57"))
                        return "57";
                        else if(loginCheck[0].equals("0")){
                                rs.executeSql("select * from HrmResource where loginid='"+login_id+"'");
                                rs.next();
                            }
                            else{
                                rs.executeSql("select * from HrmResourceManager where loginid='"+login_id+"'");
                                rs.next();
                            }
                    /*
                    if (rs.next()) {

                        if (rs.getString(1).equals("0")) {
                            return "16";
                        }
                    }else{
                         return "17";
                    }*/
                }
                
                //���ڼ���ظ���¼
                ArrayList onlineuserids = null;
                onlineuserids = (ArrayList) staticobj.getObject("onlineuserids");
                if (onlineuserids != null) {
                    if (onlineuserids.indexOf("" + rs.getInt("id")) != -1) {
                        String sqltmp = "";
                        if (rs1.getDBType().equals("oracle")) {
                            sqltmp = "select * from (select * from SysMaintenanceLog where relatedid = " +rs.getInt("id") + " and operatetype='6' and operateitem='60' order by id desc ) where rownum=1 ";
                         }else if(rs.getDBType().equals("db2")){
                            sqltmp = "select * from SysMaintenanceLog where relatedid = "+rs.getInt("id") +" and operatetype='6' and operateitem='60' order by id desc fetch first 1 rows only ";
                        } else {
                            sqltmp = "select top 1 * from SysMaintenanceLog where relatedid = " + rs.getInt("id") + " and operatetype='6' and operateitem='60' order by id desc";
                        }
                       
                        rs1.executeSql(sqltmp);
                        if (rs1.next())
                            message = rs1.getString("clientaddress") + " " + rs1.getString("operatedate") + " " + rs1.getString("operatetime");
                    }
                }

                String startdate = rs.getString("startdate");
                String enddate = rs.getString("enddate");
/*

				if((currentdate.compareTo(startdate) < 0 && !startdate.equals("")) ||
				   (currentdate.compareTo(enddate) >0 && !enddate.equals("")))  return "17" ;
*/
                int status = Util.getIntValue(rs.getString("status"));
                if (status != 0 && status != 1 && status != 2 && status != 3) {
                    return "17";
                }

                User user = new User();

                user.setUid(rs.getInt("id"));
                user.setLoginid(login_id);
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setAliasname(rs.getString("aliasname"));
                user.setTitle(rs.getString("title"));
                user.setTitlelocation(rs.getString("titlelocation"));
                user.setSex(rs.getString("sex"));
                user.setPwd(user_password);
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
                //xiaofeng
                HrmSettingsComInfo sci=new HrmSettingsComInfo();

                /**����Ƿ�����usb���β��Կ�ʼ**/
				CheckIpNetWork checkipnetwork = new CheckIpNetWork();
				String clientIP = Util.getIpAddr(request);
				boolean checktmp = checkipnetwork.checkIpSeg(clientIP);//true��ʾ������֮��,false��ʾ������֮��
				/**����Ƿ�����usb���β��Խ���**/
				
                int needusb = rs.getInt("needusb");
                String usbType = sci.getUsbType();
                String userUsbType=Util.null2String(rs.getString("userUsbType")); //ָ��usbkey��֤����
                
                if(!userUsbType.equals("")){
                	usbType=userUsbType;
                }
                	
                if (needusb==1) {                   
                    if(checktmp) {
	                	if("1".equals(usbType)){
		                    String serialNo = rs.getString("serial");
		                    // System.out.println(serial);
		                    byte[] bts = Base64.decode(serial);
		                    String serial1 = new String(bts, "ISO8859_1");
		
		                    //TD4290
		                    //modified by hubo,2006-05-09
		                    long firmcode = Util.getIntValue(sci.getFirmcode());
		                    long usercode = Util.getIntValue(sci.getUsercode());
		                    /*rs1.executeSql("SELECT firmcode,usercode FROM HrmOtherSettings");
		                    if(rs1.next()){
		                        firmcode = Long.parseLong(rs1.getString("firmcode"));
		                        usercode = Long.parseLong(rs1.getString("usercode"));
		                    }*/
		                    String serialNo1=null;
		                    if(usbserver!=null&&!usbserver.equals("")) {
		                     UsbKeyProxy proxy=new UsbKeyProxy(usbserver);
		                     serialNo1=proxy.decrypt(firmcode, usercode, Long.parseLong(rnd), serial1);
		                    }else
		                     serialNo1= AuthenticUtil.decrypt(firmcode, usercode, Long.parseLong(rnd), serial1);
		
		                    if (serial.equals("0"))
		                        return "45";
		                    else if (serial.equals("1") || serial.equals(serialNo))
		                        return "46";
		                    else if (serialNo.equals(serialNo1)) {
		                        user.setNeedusb(needusb);
		                        user.setSerial(serialNo);
		                    } else if (serialNo1.equals("0")) {
		                        return "48";
		                    } else
		                        return "47";
	                	}else if("3".equals(usbType)){ //��ʯkey��֤
	                		String tokenAuthKey=Util.null2String(request.getParameter("tokenAuthKey"));
	                		String tokenKey=Util.null2String(rs.getString("tokenKey"));
	                		if(tokenKey.equals(""))
	                			return "120"; //δ������
	                		else{
	                			TokenJSCX token=new TokenJSCX();
	                			boolean isTokenAuthKeyPass=false;
	                			
	                			RecordSet recordSet=new RecordSet();  
	                			String sql="select * from tokenJscx WHERE tokenKey='"+tokenKey+"'";
	                			recordSet.execute(sql);
	                			if(recordSet.next()){
		                			if(tokenKey.startsWith("1"))
		                			    isTokenAuthKeyPass=token.checkDLKey(tokenKey,tokenAuthKey);
		                			else if(tokenKey.startsWith("2"))
		                			    isTokenAuthKeyPass=token.checkDLKey(tokenKey,tokenAuthKey);
		                			else if(tokenKey.startsWith("3"))
		                				isTokenAuthKeyPass=token.checkKey(tokenKey,tokenAuthKey); 
		                			
		                			if(!isTokenAuthKeyPass)
		                				return "122"; //��֤��ͨ�� 
	                			}else
	                				return "120";     //����δ���г�ʼ������
	                		}
	                	}else{
	                		String username1 = Util.null2String(rs.getString("account"));
	                		String serialNo = rs.getString("serial");
	                		//user.setNeedusb(needusb);
	                		//user.setSerial(serialNo);
	                		HTSrvAPI htsrv = new HTSrvAPI();
	                		String sharv = "";
	                		sharv = htsrv.HTSrvSHA1(rnd, rnd.length());
	
	                		sharv = sharv + "04040404";
	                		String ServerEncData = htsrv.HTSrvCrypt(0, serialNo, 0, sharv);
	                		//System.out.println("rnd = " + rnd);
	                		//System.out.println("sharv = " + sharv);
	                		//System.out.println("serialNo = " + serialNo);
	                		//System.out.println("ServerEncData = " + ServerEncData);
	                		if(serial.equals("0")){
	                			return "45";
	                		}else if(!username1.equals(username)){
	                			return "17";
	                		}else if(!ServerEncData.equals(serial)){
	                			return "16";
	                		}
							user.setNeedusb(needusb);
		                    user.setSerial(serialNo);
	                	}
                    } else {
                    	user.setNeedusb(0);
                    }
                } else {   //û������key
                	int needusbnetwork = Util.getIntValue(sci.getNeedusbnetwork());
                	
                	boolean isSysadmin = false;
									rs1.executeSql("select count(loginid) from HrmResourceManager where loginid = '"+login_id+"'");
									if(rs1.next() && rs1.getInt(1)>0){
										isSysadmin = true;
									}
					
									if(needusbnetwork == 1 && !isSysadmin) {   //�������β���
	                	if(checktmp) {  //��������β���֮�⣬������key��½
	                		return "45";
	                	} else {  //���β���֮�ڣ��Ͳ���Ҫkey������½
	                		user.setNeedusb(0);
	                	}
                	} else {
                		user.setNeedusb(0);
                	}
                }

                user.setLoginip(Util.getIpAddr(request));
                // request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
                request.getSession(true).setAttribute("weaver_user@bean", user);
                //request.getSession(true).setAttribute("moniter", new OnLineMonitor("" + user.getUID(),user.getLoginip()));

                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
                Util.setCookie(response, "languageidweaver", languageidweaver, 172800);
                if(MOREACCOUNTLANDING){
                //���ʺŵ�½
                if (user.getUID() != 1) {  //is not sysadmin
                    List accounts = this.getAccountsById(user.getUID());
                    request.getSession(true).setAttribute("accounts", accounts);
                	}
                	Util.setCookie(response, "loginfileweaver", login_file, 172800);
                	Util.setCookie(response, "loginidweaver", login_id, 172800);
                }
                char separater = Util.getSeparator();
                rs.execute("HrmResource_UpdateLoginDate", rs.getString("id") + separater + currentdate);
                //�������USB�����ʱ��Ա��¼�����ظ���¼������ by sjh 54096
				if("".equals(usbType) || usbType==null){
					SysMaintenanceLog log = new SysMaintenanceLog();
					log.resetParameter();
					log.setRelatedId(rs.getInt("id"));
					log.setRelatedName((rs.getString("firstname") + " " + rs.getString("lastname")).trim());
					log.setOperateType("6");
					log.setOperateDesc("");
					log.setOperateItem("60");
					log.setOperateUserid(rs.getInt("id"));
					log.setClientAddress(Util.getIpAddr(request));
					log.setSysLogInfo();
                }
                //����Ƿ��пͻ���ϵ�ƻ�
                /* �����޸� 2004��05��08 ��¼���ͻ���ϵ�ƻ���ǰ��ѡȡ���еļ�¼��������Ҫ�����ڸ���Ϊѡ������ */
                char flag = 2;
                String sql = "";
                rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "0");
                sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + rs.getInt("id");
                rs1.executeSql(sql);
                if (rs1.next()) {
                    if (Util.getIntValue(rs1.getString(1), 0) > 0)
                        rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "1");

                } else {
                    return "15";
                }
            }

            if (login_type.equals("2")) {
                //add by sean.yang 2006-02-09 for TD3609
            	//edit by chujun 2009-03-05 TD10057
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();
                int needvalidate=settings.getNeedvalidate();//0: ��,1: ��
                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }
                rs.execute("CRM_CustomerInfo_SByLoginID", login_id);

                if (rs.next()) {
                	if (rs.getString("deleted").equals("1")) {
                        return "17";
                    }
                    if (!rs.getString("PortalPassword").equals(user_password)) {
                        return "16";
                    }

                    if (!rs.getString("PortalStatus").equals("2")) return "17";

                    User user = new User();

                    user.setUid(rs.getInt("id"));
                    user.setLoginid(login_id);
					user.setPwd(user_password);
                    user.setFirstname(rs.getString("name"));
                    //user.setLanguage(Util.getIntValue("7", 0));
					//Editted by zhaolg
                    String languageidweaver = rs.getString("language");
                    if(!languid.equalsIgnoreCase(languageidweaver) && ismutilangua){
                    	rs2.execute("update CRM_CustomerInfo set  language = "+languid+" where id ="+rs.getInt("id"));
                    	languageidweaver = languid;
                    }
                    user.setLanguage(Util.getIntValue(languageidweaver, 0));
                    user.setUserDepartment(Util.getIntValue(rs.getString("department"), 0));
                    user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
                    user.setManagerid(rs.getString("manager"));
                    user.setCountryid(rs.getString("country"));
                    user.setEmail(rs.getString("email"));
                    user.setAgent(Util.getIntValue(rs.getString("agent"), 0));
                    user.setType(Util.getIntValue(rs.getString("type"), 0));
                    user.setParentid(Util.getIntValue(rs.getString("parentid"), 0));
                    user.setProvince(Util.getIntValue(rs.getString("province"), 0));
                    user.setCity(Util.getIntValue(rs.getString("city"), 0));
                    user.setLogintype("2");
                    user.setSeclevel(rs.getString("seclevel"));
                    user.setLoginip(Util.getIpAddr(request));
                    request.getSession(true).setAttribute("weaver_user@bean", user);
                    //request.getSession(true).setAttribute("moniter", new OnLineMonitor());

                    Util.setCookie(response, "loginfileweaver", login_file, 172800);
                    Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
                    Util.setCookie(response, "languageidweaver", languageidweaver, 172800);

                    String para = "" + rs.getInt("id") + separator + currentdate + separator + currenttime + separator + Util.getIpAddr(request);
                    rs.executeProc("CRM_LoginLog_Insert", para);

                    //����Ƿ��пͻ���ϵ�ƻ�
                    /* �����޸� 2004��05��08 ��¼���ͻ���ϵ�ƻ���ǰ��ѡȡ���еļ�¼��������Ҫ�����ڸ���Ϊѡ������ */
                    char flag = 2;
                    String sql = "";
                    rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "1" + flag + "0");
                    sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and agentid =" + rs.getInt("id");
                    rs1.executeSql(sql);
                    if (rs1.next()) {
                        if (Util.getIntValue(rs1.getString(1), 0) > 0)
                            rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "1" + flag + "1");
                    }
                } else {
                    return "15";
                }
            }
        } catch (Exception e) {
            writeLog(e);
            throw e;
        }
        return message;
    }

    public String getUserCheck(HttpServletRequest request, HttpServletResponse response, String login_id, String user_password, String login_type, String login_file,String validateCode,String messages,String languid,boolean ismutilangua) throws Exception {
    	StaticObj staticobj = null;
        staticobj = StaticObj.getInstance();
        String frommail=Util.null2String(request.getParameter("frommail"));//��Ի���Ҫ���ʼ�ֱ�ӵ�½�������ǰ�ȫ�ԣ��ڲ�ʹ�ã�
        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        RecordSet rs2 = new RecordSet();
        char separator = Util.getSeparator();
        String message = "";

        Calendar today = Calendar.getInstance();
        String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
        String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);

        Document doc;
        try {

            String isLicense = (String) staticobj.getObject("isLicense");
            //���� 2004-9-16 , TD1096 , License���������Ʋ�������
            
            LN ckLicense = new LN();
            message=ckLicense.CkLicense(currentdate);
            if (!message.equals("1")) {
                return "19";
            } else {
                staticobj.putObject("isLicense", "true");
            }
            
            //����û�������start
			String concurrentFlag = Util.null2String(ckLicense.getConcurrentFlag());
			int hrmnumber = Util.getIntValue(ckLicense.getHrmnum());
			if("1".equals(concurrentFlag)){
				LicenseCheckLogin  lchl = new LicenseCheckLogin();
				if(lchl.getLicUserCheck(login_id,hrmnumber)){
					recordFefuseLogin(login_id); //�ܾ���½��¼
					return "26";
				}
			}
            //����û�������end

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
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();

                String mode = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
                String usbserver = Prop.getPropValue(GCONST.getConfigFile(), "usbserver.ip");
                int needvalidate=settings.getNeedvalidate();//0: ��,1: ��
                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }
                /***���ADͬ�������Ȩ����Ա���ܵ�¼***/
                String ldapmark="";
                boolean isloginlpad = true;
                if (mode != null && mode.equals("ldap")){
                	rs.executeSql("select * from HrmResourceManager where loginid = '"+login_id+"'");
                	if(rs.next()) isloginlpad = false;
                }

                if (mode != null && mode.equals("ldap") && isloginlpad) {
                	rs.executeSql("select ldapmark from HrmResource where account='" + login_id + "'");
                	if(rs.next()){
                		ldapmark=rs.getString("ldapmark");	
                	}
                    LdapUtil util = LdapUtil.getInstance();
                    boolean flag = util.authentic(login_id, user_password,ldapmark);
                    rs.executeSql("select * from HrmResource where account='" + login_id + "'");
                    if (flag) {
                        if (!rs.next())
                            return "17";
                        else {
							int tmpid = rs.getInt("id");
                            int needdynapass_usr = rs.getInt("needdynapass");
                            int needdynapass_sys = settings.getNeeddynapass();
                            int dynapasslen = settings.getDynapasslen();
                            String dypadcon = Util.null2String(settings.getDypadcon());
                            int passwordstateip = 1;
                            if(needdynapass_sys==1) {
                            	String sql = "select password,passwordstate from HrmResource where account='"+login_id+"'";
                            	rs2.executeSql(sql);
                            	if(rs2.next()) passwordstateip = rs2.getInt("passwordstate");
                            }
                            boolean ipaddress = checkIpSeg(request, login_id, passwordstateip);//������β���
                            if (needdynapass_usr == 1 && needdynapass_sys == 1 && dynapasslen > 0 && ipaddress) {
                            	String dynapass = "";
                                if(dypadcon.equals("0")){
                                	dynapass = Util.passwordBuilderNo(dynapasslen);
                                }else if (dypadcon.equals("1")){
                                	dynapass = Util.passwordBuilderEn(dynapasslen);
                                }else if (dypadcon.equals("2")){
                                	 dynapass = Util.passwordBuilder(dynapasslen);
                                }
                                SMSManager sm = new SMSManager();
                                String mobileno = rs.getString("mobile");
                                boolean sendflag = sm.sendSMS(mobileno, "����"+currentdate+" "+currenttime+"��¼ϵͳ�Ķ�̬����Ϊ��" + dynapass);
                                if (sendflag) {
                                    rs.executeSql("update hrmpassword set password='" + Util.getEncrypt(dynapass) + "' where id=" + tmpid);
                                    return "101";
                                }
                            }
                        }
                    } else {
                        if (!rs.next())
                            return "17";
                        else {
							int tmpid = rs.getInt("id");
                            int needdynapass_usr = rs.getInt("needdynapass");
                            int needdynapass_sys = settings.getNeeddynapass();
                            int dynapasslen = settings.getDynapasslen();
                            if (needdynapass_usr == 1 && needdynapass_sys == 1 && dynapasslen > 0) {
                                rs1.executeSql("select * from hrmpassword where id=" + tmpid + " and password='" + Util.getEncrypt(user_password)+"'");
                            if (rs1.next())
                                rs1.executeSql("update hrmpassword set password='' where id=" + tmpid);
                            else
                                return "16";
                            }else
                                return "16";

                        }

                    }
                } else {

                    String[] loginCheck = checkUserPass(request,login_id,user_password,messages);
                    if (frommail.equals("1")) loginCheck = checkUserPassM(login_id,user_password);
                    if(loginCheck[0].equals("-2"))
                        return "55";
                    else if(loginCheck[0].equals("-1"))
                        return "17";
                    else if(loginCheck[1].equals("0"))
                            return "16";
                    else if(loginCheck[1].equals("101"))
                            return "101";
                    else if(loginCheck[1].equals("57"))
                        return "57";
                        else if(loginCheck[0].equals("0")){
                                rs.executeSql("select * from HrmResource where loginid='"+login_id+"'");
                                rs.next();
                            }
                            else{
                                rs.executeSql("select * from HrmResourceManager where loginid='"+login_id+"'");
                                rs.next();
                            }

                    /*
                    if (rs.next()) {

                        if (rs.getString(1).equals("0")) {
                            return "16";
                        }
                    }else
                         return "17";
                    */
                }
                
                //���ڼ���ظ���¼
                ArrayList onlineuserids = null;
                onlineuserids = (ArrayList) staticobj.getObject("onlineuserids");
                if (onlineuserids != null) {
                    if (onlineuserids.indexOf("" + rs.getInt("id")) != -1) {
                        String sqltmp = "";
                        if (rs1.getDBType().equals("oracle")) {
                            sqltmp = "select * from (select * from SysMaintenanceLog where relatedid = " + rs.getInt("id") + " and operatetype='6' and operateitem='60' order by id desc ) where rownum=1 ";
                        }else if(rs.getDBType().equals("db2")){
                            sqltmp = "select * from SysMaintenanceLog where relatedid = "+rs.getInt("id") +" and operatetype='6' and operateitem='60' order by id desc fetch first 1 rows only ";
                        } else {
                            sqltmp = "select top 1 * from SysMaintenanceLog where relatedid = " + rs.getInt("id") + " and operatetype='6' and operateitem='60' order by id desc";
                        }

                        rs1.executeSql(sqltmp);
                        if (rs1.next())
                            message = rs1.getString("clientaddress") + " " + rs1.getString("operatedate") + " " + rs1.getString("operatetime");
                    }
                }
                
                String startdate = rs.getString("startdate");
                String enddate = rs.getString("enddate");
/*

				if((currentdate.compareTo(startdate) < 0 && !startdate.equals("")) ||
				   (currentdate.compareTo(enddate) >0 && !enddate.equals("")))  return "17" ;
*/
                int status = rs.getInt("status");
                if (status != 0 && status != 1 && status != 2 && status != 3) {
                    return "17";
                }
                User user = new User();
                
                user.setUid(rs.getInt("id"));
                user.setLoginid(login_id);
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setAliasname(rs.getString("aliasname"));
                user.setTitle(rs.getString("title"));
                user.setTitlelocation(rs.getString("titlelocation"));
                user.setSex(rs.getString("sex"));
				user.setPwd(user_password);
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
                // request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
                request.getSession(true).setAttribute("weaver_user@bean", user);
                //request.getSession(true).setAttribute("moniter", new OnLineMonitor("" + user.getUID(),user.getLoginip()));
//              ���ʺŵ�½
                if (user.getUID() != 1) {  //is not sysadmin
                    List accounts = this.getAccountsById(user.getUID());
                    request.getSession(true).setAttribute("accounts", accounts);
                }
                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", login_id, 172800);
                
                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
                Util.setCookie(response, "languageidweaver", languageidweaver, 172800);

                char separater = Util.getSeparator();
                rs.execute("HrmResource_UpdateLoginDate", rs.getString("id") + separater + currentdate);

                SysMaintenanceLog log = new SysMaintenanceLog();
                log.resetParameter();
                log.setRelatedId(rs.getInt("id"));
                log.setRelatedName((rs.getString("firstname") + " " + rs.getString("lastname")).trim());
                log.setOperateType("6");
                log.setOperateDesc("");
                log.setOperateItem("60");
                log.setOperateUserid(rs.getInt("id"));
                log.setClientAddress(Util.getIpAddr(request));
                log.setSysLogInfo();

                //����Ƿ��пͻ���ϵ�ƻ�
/* �����޸� 2004��05��08 ��¼���ͻ���ϵ�ƻ���ǰ��ѡȡ���еļ�¼��������Ҫ�����ڸ���Ϊѡ������ */
                char flag = 2;
                String sql = "";
                rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "0");
                sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + rs.getInt("id");
                rs1.executeSql(sql);
                if (rs1.next()) {
                    if (Util.getIntValue(rs1.getString(1), 0) > 0)
                        rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "1");
                } else {
                  return "15";
                }
            }

            if (login_type.equals("2")) {
                //add by sean.yang 2006-02-09 for TD3609
            	//edit by chujun 2009-03-05 TD10057
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();
                int needvalidate=settings.getNeedvalidate();//0: ��,1: ��
                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }

                rs.execute("CRM_CustomerInfo_SByLoginID", login_id);

                if (rs.next()) {
                	if (rs.getString("deleted").equals("1")) {
                        return "17";
                    }
                    if (!rs.getString("PortalPassword").equals(user_password)) {
                        return "16";
                    }

                    if (!rs.getString("PortalStatus").equals("2")) return "17";
                    User user = new User();
                    
                    user.setUid(rs.getInt("id"));
                    user.setLoginid(login_id);
					user.setPwd(user_password);
                    user.setFirstname(rs.getString("name"));
                    // user.setLanguage(Util.getIntValue("7", 0));
				    //Editted by zhaolg
                    String languageidweaver = rs.getString("language");
                    if(!languid.equalsIgnoreCase(languageidweaver) && ismutilangua){
                    	rs2.execute("update CRM_CustomerInfo set  language = "+languid+" where id ="+rs.getInt("id"));
                    	languageidweaver = languid;
                    }
                    user.setLanguage(Util.getIntValue(languageidweaver, 0));
                    user.setUserDepartment(Util.getIntValue(rs.getString("department"), 0));
                    user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
                    user.setManagerid(rs.getString("manager"));
                    user.setCountryid(rs.getString("country"));
                    user.setEmail(rs.getString("email"));
                    user.setAgent(Util.getIntValue(rs.getString("agent"), 0));
                    user.setType(Util.getIntValue(rs.getString("type"), 0));
                    user.setParentid(Util.getIntValue(rs.getString("parentid"), 0));
                    user.setProvince(Util.getIntValue(rs.getString("province"), 0));
                    user.setCity(Util.getIntValue(rs.getString("city"), 0));
                    user.setLogintype("2");
                    user.setSeclevel(rs.getString("seclevel"));
                    user.setLoginip(Util.getIpAddr(request));
                    request.getSession(true).setAttribute("weaver_user@bean", user);
                    //request.getSession(true).setAttribute("moniter", new OnLineMonitor());

                    Util.setCookie(response, "loginfileweaver", login_file, 172800);
                    Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
                    Util.setCookie(response, "languageidweaver", languageidweaver, 172800);

                    String para = "" + rs.getInt("id") + separator + currentdate + separator + currenttime + separator + Util.getIpAddr(request);
                    rs.executeProc("CRM_LoginLog_Insert", para);

                    //����Ƿ��пͻ���ϵ�ƻ�
/* �����޸� 2004��05��08 ��¼���ͻ���ϵ�ƻ���ǰ��ѡȡ���еļ�¼��������Ҫ�����ڸ���Ϊѡ������ */
                    char flag = 2;
                    String sql = "";
                    rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "1" + flag + "0");
                    sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and agentid =" + rs.getInt("id");
                    rs1.executeSql(sql);
                    if (rs1.next()) {
                        if (Util.getIntValue(rs1.getString(1), 0) > 0)
                            rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "1" + flag + "1");
                    }
                } else {
                    return "15";
                }
            }
        } catch (Exception e) {
            writeLog(e);
            throw e;
        }
        return message;
    }
    public String getElementUserCheck(HttpServletRequest request, HttpServletResponse response, String login_id, String user_password, String login_type, String login_file,String validateCode,String messages,String languid,boolean ismutilangua) throws Exception {
    	StaticObj staticobj = null;
        staticobj = StaticObj.getInstance();
        String frommail=Util.null2String(request.getParameter("frommail"));//��Ի���Ҫ���ʼ�ֱ�ӵ�½�������ǰ�ȫ�ԣ��ڲ�ʹ�ã�
        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        RecordSet rs2 = new RecordSet();
        char separator = Util.getSeparator();
        String message = "";

        Calendar today = Calendar.getInstance();
        String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
        String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);

        Document doc;
        try 
        {
            String isLicense = (String) staticobj.getObject("isLicense");
            //���� 2004-9-16 , TD1096 , License���������Ʋ�������
            
            LN ckLicense = new LN();
            message=ckLicense.CkLicense(currentdate);
            if (!message.equals("1")) {
                return "19";
            } else {
                staticobj.putObject("isLicense", "true");
            }
            
            //����û�������start
			String concurrentFlag = Util.null2String(ckLicense.getConcurrentFlag());
			int hrmnumber = Util.getIntValue(ckLicense.getHrmnum());
			if("1".equals(concurrentFlag)){
				LicenseCheckLogin  lchl = new LicenseCheckLogin();
				if(lchl.getLicUserCheck(login_id,hrmnumber)){
					recordFefuseLogin(login_id); //�ܾ���½��¼
					return "26";
				}
			}
            //����û�������end

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
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();

                String mode = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
                String usbserver = Prop.getPropValue(GCONST.getConfigFile(), "usbserver.ip");
                int needvalidate=settings.getNeedvalidate();//0: ��,1: ��
                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }
                /***���ADͬ�������Ȩ����Ա���ܵ�¼***/
                String ldapmark="";
                boolean isloginlpad = true;
                if (mode != null && mode.equals("ldap")){
                	rs.executeSql("select * from HrmResourceManager where loginid = '"+login_id+"'");
                	if(rs.next()) isloginlpad = false;
                }

                if (mode != null && mode.equals("ldap") && isloginlpad) {
                    LdapUtil util = LdapUtil.getInstance();
                    rs.executeSql("select ldapmark from HrmResource where account='" + login_id + "'");
                	if(rs.next()){
                		ldapmark=rs.getString("ldapmark");	
                	}
                    boolean flag = util.authentic(login_id, user_password,ldapmark);
                    rs.executeSql("select * from HrmResource where account='" + login_id + "'");
                    if (flag) {
                        if (!rs.next())
                            return "17";
                        else {
							int tmpid = rs.getInt("id");
                            int needdynapass_usr = rs.getInt("needdynapass");
                            int needdynapass_sys = settings.getNeeddynapass();
                            int dynapasslen = settings.getDynapasslen();
                            String dypadcon = Util.null2String(settings.getDypadcon());
                            int passwordstateip = 1;
                            if(needdynapass_sys==1) {
                            	String sql = "select password,passwordstate from HrmResource where account='"+login_id+"'";
                            	rs2.executeSql(sql);
                            	if(rs2.next()) passwordstateip = rs2.getInt("passwordstate");
                            }
                            boolean ipaddress = checkIpSeg(request, login_id, passwordstateip);//������β���
                            if (needdynapass_usr == 1 && needdynapass_sys == 1 && dynapasslen > 0 && ipaddress) {
                            	String dynapass = "";
                                if(dypadcon.equals("0")){
                                	dynapass = Util.passwordBuilderNo(dynapasslen);
                                }else if (dypadcon.equals("1")){
                                	dynapass = Util.passwordBuilderEn(dynapasslen);
                                }else if (dypadcon.equals("2")){
                                	 dynapass = Util.passwordBuilder(dynapasslen);
                                }
                                SMSManager sm = new SMSManager();
                                String mobileno = rs.getString("mobile");
                                boolean sendflag = sm.sendSMS(mobileno, "����"+currentdate+" "+currenttime+"��¼ϵͳ�Ķ�̬����Ϊ��" + dynapass);
                                if (sendflag) {
                                    rs.executeSql("update hrmpassword set password='" + Util.getEncrypt(dynapass) + "' where id=" + tmpid);
                                    return "101";
                                }
                            }
                        }
                    } else {
                        if (!rs.next())
                            return "17";
                        else {
							int tmpid = rs.getInt("id");
                            int needdynapass_usr = rs.getInt("needdynapass");
                            int needdynapass_sys = settings.getNeeddynapass();
                            int dynapasslen = settings.getDynapasslen();
                            if (needdynapass_usr == 1 && needdynapass_sys == 1 && dynapasslen > 0) {
                                rs1.executeSql("select * from hrmpassword where id=" + tmpid + " and password='" + Util.getEncrypt(user_password)+"'");
                            if (rs1.next())
                                rs1.executeSql("update hrmpassword set password='' where id=" + tmpid);
                            else
                                return "16";
                            }else
                                return "16";

                        }

                    }
                } else {

                    String[] loginCheck = checkUserPass(request,login_id,user_password,messages);
                    if (frommail.equals("1")) loginCheck = checkUserPassM(login_id,user_password);
                    if(loginCheck[0].equals("-2"))
                        return "55";
                    else if(loginCheck[0].equals("-1"))
                        return "17";
                    else if(loginCheck[1].equals("0"))
                            return "16";
                    else if(loginCheck[1].equals("101"))
                            return "101";
                    else if(loginCheck[1].equals("57"))
                        return "57";
                    else if(loginCheck[0].equals("0")){
                            rs.executeSql("select * from HrmResource where loginid='"+login_id+"'");
                            rs.next();
                    }
                    else{
                        rs.executeSql("select * from HrmResourceManager where loginid='"+login_id+"'");
                        rs.next();
                    }

                    /*
                    if (rs.next()) {

                        if (rs.getString(1).equals("0")) {
                            return "16";
                        }
                    }else
                         return "17";
                    */
                }
                
                
                
                String startdate = rs.getString("startdate");
                String enddate = rs.getString("enddate");
/*

				if((currentdate.compareTo(startdate) < 0 && !startdate.equals("")) ||
				   (currentdate.compareTo(enddate) >0 && !enddate.equals("")))  return "17" ;
*/
                int status = rs.getInt("status");
                if (status != 0 && status != 1 && status != 2 && status != 3) {
                    return "17";
                }
                User user = new User();
                
                user.setUid(rs.getInt("id"));
                user.setLoginid(login_id);

                user.setLoginip(Util.getIpAddr(request));
                // request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
                request.getSession(true).setAttribute("weaver_user@bean", user);
//              ���ʺŵ�½
                if (user.getUID() != 1) {  //is not sysadmin
                    List accounts = this.getAccountsById(user.getUID());
                    request.getSession(true).setAttribute("accounts", accounts);
                }
                //����Ƿ��пͻ���ϵ�ƻ�
/* �����޸� 2004��05��08 ��¼���ͻ���ϵ�ƻ���ǰ��ѡȡ���еļ�¼��������Ҫ�����ڸ���Ϊѡ������ */
                char flag = 2;
                String sql = "";
                sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + rs.getInt("id");
                rs1.executeSql(sql);
                if (rs1.next()) 
                {
                    if (Util.getIntValue(rs1.getString(1), 0) > 0)
                        rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "1");
                } 
                else 
                {
                  return "15";
                }
            }

            if (login_type.equals("2")) {
                //add by sean.yang 2006-02-09 for TD3609
            	//edit by chujun 2009-03-05 TD10057
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();
                int needvalidate=settings.getNeedvalidate();//0: ��,1: ��
                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }

                rs.execute("CRM_CustomerInfo_SByLoginID", login_id);

                if (rs.next()) {
                	if (rs.getString("deleted").equals("1")) {
                        return "17";
                    }
                    if (!rs.getString("PortalPassword").equals(user_password)) {
                        return "16";
                    }

                    if (!rs.getString("PortalStatus").equals("2")) return "17";
                    User user = new User();
                    
                    user.setUid(rs.getInt("id"));
                    user.setLoginid(login_id);
					
                    request.getSession(true).setAttribute("weaver_user@bean", user);
                } 
                else 
                {
                    return "15";
                }
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
    
    public String shiftIdentity(HttpServletRequest request, HttpServletResponse response, int id) throws Exception {
        StaticObj staticobj = null;
        staticobj = StaticObj.getInstance();

        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        char separator = Util.getSeparator();
        String message = "";
        boolean MOREACCOUNTLANDING = GCONST.getMOREACCOUNTLANDING();
        Calendar today = Calendar.getInstance();
        String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
        String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);
        rs.executeProc("HrmResource_SelectByID",""+id);
        rs.next();
        try {


            ArrayList onlineuserids = null;
            onlineuserids = (ArrayList) staticobj.getObject("onlineuserids");
            if (onlineuserids != null) {
                if (onlineuserids.indexOf("" + id) != -1) {
                    String sqltmp = "";
                    if (rs1.getDBType().equals("oracle")) {
                        sqltmp = "select * from (select * from SysMaintenanceLog where relatedid = " + rs.getInt("id") + " and operatetype='6' and operateitem='60' order by id desc ) where rownum=1 ";
                    } else {
                        sqltmp = "select top 1 * from SysMaintenanceLog where relatedid = " + rs.getInt("id") + " and operatetype='6' and operateitem='60' order by id desc";
                    }

                    rs1.executeSql(sqltmp);
                    if (rs1.next())
                        message = rs1.getString("clientaddress") + " " + rs1.getString("operatedate") + " " + rs1.getString("operatetime");
                }
            }

            String startdate = rs.getString("startdate");
            String enddate = rs.getString("enddate");
/*

				if((currentdate.compareTo(startdate) < 0 && !startdate.equals("")) ||
				   (currentdate.compareTo(enddate) >0 && !enddate.equals("")))  return "17" ;
*/
            int status = Util.getIntValue(rs.getString("status"));
            if (status != 0 && status != 1 && status != 2 && status != 3) {
                return "17";
            }
            User user = new User();

            user.setUid(rs.getInt("id"));
            user.setLoginid(rs.getString("loginid"));
            user.setFirstname(rs.getString("firstname"));
            user.setLastname(rs.getString("lastname"));
            user.setAliasname(rs.getString("aliasname"));
            user.setTitle(rs.getString("title"));
            user.setTitlelocation(rs.getString("titlelocation"));
            user.setSex(rs.getString("sex"));
            user.setLanguage(Util.getIntValue(rs.getString("systemlanguage"), 0));
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

            user.setLoginip(Util.getIpAddr(request));
            // request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
            request.getSession(true).setAttribute("weaver_user@bean", user);
            //request.getSession(true).setAttribute("moniter", new OnLineMonitor("" + user.getUID(),user.getLoginip()));
            if(MOREACCOUNTLANDING){
            //���ʺŵ�½
            if (user.getUID() != 1) {
                List accounts = this.getAccountsById(user.getUID());
                request.getSession(true).setAttribute("accounts", accounts);
            }
            	Util.setCookie(response, "loginfileweaver", "/login/Login.jsp?logintype=1", 172800);
            	Util.setCookie(response, "loginidweaver", user.getLoginid(), 172800);
            }
            char separater = Util.getSeparator();
            rs.execute("HrmResource_UpdateLoginDate", rs.getString("id") + separater + currentdate);

            SysMaintenanceLog log = new SysMaintenanceLog();
            log.resetParameter();
            log.setRelatedId(rs.getInt("id"));
            log.setRelatedName((rs.getString("firstname") + " " + rs.getString("lastname")).trim());
            log.setOperateType("6");
            log.setOperateDesc("");
            log.setOperateItem("60");
            log.setOperateUserid(rs.getInt("id"));
            log.setClientAddress(Util.getIpAddr(request));
            log.setSysLogInfo();

            //����Ƿ��пͻ���ϵ�ƻ�
            /* �����޸� 2004��05��08 ��¼���ͻ���ϵ�ƻ���ǰ��ѡȡ���еļ�¼��������Ҫ�����ڸ���Ϊѡ������ */
            char flag = 2;
            String sql = "";
            rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "0");
            sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + rs.getInt("id");
            rs1.executeSql(sql);
            if (rs1.next()) {
                if (Util.getIntValue(rs1.getString(1), 0) > 0)
                    rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "1");
            }
        }


        catch (Exception e) {
            writeLog(e);
            throw e;
        }
        return message;
    }
    
    /*
     * ���ڰ�ȫ���ǰ�ԭ���ô洢����HrmResource_SByLoginIDPass��Ϊ�˷���������
     * returnValue[0]
     * 0����ͨ�û���1��ϵͳ����Ա
     * returnValue[1]
     * 0: �û����ڵ����ܲ��ԣ�1���û����붼��ȷ
    */
public String[] checkUserPass(HttpServletRequest request, String loginid, String pass,String messages){
    	String ClientIP = Util.getIpAddr(request);
        String[] returnValue = new String[2];
        returnValue[0]="-1"; //0����ͨ�û���1��ϵͳ����Ա
        returnValue[1]="-1"; //0: �û����ڵ����ܲ��ԣ�1���û����붼��ȷ
        
        if (loginid.indexOf(";")>-1||loginid.indexOf("--")>-1||loginid.indexOf(" ")>-1||loginid.indexOf("'")>-1) {
            writeLog("illegal sql statement input loginid:" + loginid);
            returnValue[0]="-2";
            return returnValue;
        }
        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        RecordSet rs2 = new RecordSet();
        String sql = "";
        String idTemp ="0";
        String passwordTemp ="";
        sql = "select id,needdynapass,mobile,passwordstate from HrmResource where loginid='"+loginid+"' and (accounttype is null  or accounttype=0)";
        rs.executeSql(sql);
        if(rs.next()&&(Util.getIntValue(rs.getString(1),0)>0)) {
            idTemp = rs.getString(1);
            returnValue[0] = "0";
            returnValue[1] = "0";
            int needdynapass = rs.getInt(2);
            if(needdynapass!=0&&needdynapass!=1){
            	needdynapass= 1;
            }
            HrmSettingsComInfo sci=new HrmSettingsComInfo();
            int needdynapass_sys = Util.getIntValue(sci.getNeeddynapass());
            if(needdynapass_sys==1){
            	int passwordstatenow = rs.getInt(4);
            	if(passwordstatenow==0 || passwordstatenow==2){
            		needdynapass= 1;
            	}
            }
            String mobile = rs.getString(3);
            /*
             * ϵͳ��Ա�����п��ܲ���needdynapass,passwordstate���ֶ�ΪNUll����hrmpassword����Ҳû��
             * ���ID��Loginid����Щ�ֶ�ΪNull���ڱ���û�м�¼������ֵ�½��֤�����̬�������
             * */
            if(needdynapass==1){
            	rs1.executeSql("select id from hrmpassword where id='"+idTemp+"'") ;
            	if(rs1.next()) ;
            	else{                 
                    rs1.executeSql("insert into hrmpassword(id,loginid) values("+idTemp+",'"+loginid+"')") ;
                   }
            }
            sql = "select password,passwordstate from HrmResource where id= " + idTemp;
            rs.executeSql(sql);
            if (rs.next()) {
                passwordTemp = Util.null2String(rs.getString(1));
                if (needdynapass != 1) {
                    if (passwordTemp.equals(Util.getEncrypt(pass)))
                        returnValue[1] = "1";
                } else {
                    if (passwordTemp.equals(Util.getEncrypt(pass))&&!"101".equals(messages)&&!"57".equals(messages)) 
                    {
                    	ChgPasswdReminder reminder=new ChgPasswdReminder();
                        RemindSettings settings=reminder.getRemindSettings();
                    	
                    	
                            int dynapasslen = Util.getIntValue(sci.getDynapasslen());
                            //��̬��������
                            String dypadcon = Util.null2String(sci.getDypadcon());
                            boolean ipaddress= false; 
                            int passwordstateip = 1;
                            if(needdynapass_sys==1) {
                            	sql = "select password,passwordstate from HrmResource where loginid='"+loginid+"'";
                            	rs2.executeSql(sql);
                            	if(rs2.next()) passwordstateip = rs2.getInt("passwordstate");
                            }
                            ipaddress = checkIpSeg(request, loginid, passwordstateip);//������β���
                            if ((passwordstateip==0 || passwordstateip ==2) && dynapasslen > 0&&ipaddress) {
                                SMSManager sm = new SMSManager();
                                String dynapass = "";
                                if(dypadcon.equals("0")){
                                	dynapass = Util.passwordBuilderNo(dynapasslen);
                                }else if (dypadcon.equals("1")){
                                	dynapass = Util.passwordBuilderEn(dynapasslen);
                                }else if (dypadcon.equals("2")){
                                	 dynapass = Util.passwordBuilder(dynapasslen);
                                }
                                //String dynapass = Util.passwordBuilder(dynapasslen);
                                String currentdatetime=TimeUtil.getCurrentTimeString();
                                boolean flag = sm.sendSMS(mobile, "����"+currentdatetime+"��¼ϵͳ�Ķ�̬����Ϊ��" + dynapass+",IP��"+ClientIP);
                                if (flag){
                                    rs.executeSql("update hrmpassword set password='"+Util.getEncrypt(dynapass)+"' where id=" + idTemp);
                                    returnValue[1] = "101"; //return login page and relogin by dynamic password
                                }
                                else
                                    returnValue[1] = "1";
                            } else
                                returnValue[1] = "1";
                    } else if("101".equals(messages)||"57".equals(messages)){
                        rs.executeSql("select password from hrmpassword where id=" + idTemp +" and password='"+Util.getEncrypt(pass)+"'");
                        if (rs.next()) {
                            rs.executeSql("update hrmpassword set password='' where id=" + idTemp);
                            returnValue[1] = "1";
                        }
                        else
                        {
                        	returnValue[1] = "57";
                        }
                    }
                }
            }
        } else{
        	rs.executeProc("SystemSet_Select","");//fix TD4867 ����Ȩʱ��systemadmin�����ʺŲ��ܵ�½
        	rs.next();
        	String detachable= Util.null2String(rs.getString("detachable"));
            sql = "select count(id),id from HrmResourceManager where loginid='"+loginid+"' group by id";
            rs.executeSql(sql);
            if(rs.next()&&(Util.getIntValue(rs.getString(1),0)>0)){
            	if (!detachable.equals("1") && !loginid.equalsIgnoreCase("sysadmin")){
            		returnValue[0]="-1";
            		returnValue[1] = "0";
            		return returnValue;
            	}
            	
                idTemp = rs.getString(2);
                returnValue[0]="1";
                returnValue[1] = "0";
                
                sql = "select password from HrmResourceManager where id= " + idTemp;
                rs.executeSql(sql);
                if(rs.next()){
                    passwordTemp = Util.null2String(rs.getString(1));
                    if(passwordTemp.equals(Util.getEncrypt(pass)))
                        returnValue[1] = "1";
                }
            }
        }
        return returnValue;
    }
	
	/**
	 * ������β���
	 * @param request
	 * @param loginid
	 * @return
	 */
	public boolean checkIpSeg(HttpServletRequest request, String loginid, int passwordstateip) {
		String ClientIP = Util.getIpAddr(request);
		//System.out.println("ClientIP="+ClientIP);
		boolean ipaddress = true;
        
        HrmSettingsComInfo sci=new HrmSettingsComInfo();
        int needdynapass_sys = Util.getIntValue(sci.getNeeddynapass());
        if(needdynapass_sys==1) {
        	RecordSet rs = new RecordSet();
			/**
	         * 0,����.
	         * 1��ֹͣ��Ĭ�ϣ�.
	         * 2�����β���.
	         * */
	
	        String inceptipaddress ="";
	        String endipaddress = "";
	        String sql = "select * from HrmnetworkSegStr";
	        rs.executeSql(sql);
	        /*
	         * �ѻ�õ�Ip��ַת��Ϊ16�������������õ����β���������бȽ��ж��Ƿ�ʹ�ö�̬����
	         * */
	        while(rs.next()){
                //inceptipaddress =rs.getString("inceptipaddress");
                inceptipaddress =rs.getString("inceptipaddress");
                endipaddress = rs.getString("endipaddress");
                //System.out.println("inceptipaddress="+inceptipaddress+"   endipaddress="+endipaddress);
                long ip1=IpUtils.ip2number(inceptipaddress);
        		long iIp2=IpUtils.ip2number(endipaddress);
        		
        		String ip3=ClientIP;
        		long iIp3=IpUtils.ip2number(ip3);
        		//System.out.println("ip1="+ip1+"   iIp2="+iIp2+"   iIp3="+iIp3);
        		//System.out.println("ip3:"+iIp3);
        		if(passwordstateip==2){
        			if(iIp3>=ip1&&iIp3<=iIp2){
        				ipaddress = false;
        				//System.out.println("ipaddress = false");
        				break;
        			}
        			else if(iIp3<=ip1||iIp3>=iIp2){
        				ipaddress = true;
        			}
        		}else if(passwordstateip==0){
        			ipaddress = true;
        			//System.out.println("0");
        		}
	        }
        }
        //System.out.println("ipaddress="+ipaddress);
        return ipaddress;
	}
    /*
     * ���ڰ�ȫ���ǰ�ԭ���ô洢����HrmResource_SByLoginIDPass��Ϊ�˷���������
     * returnValue[0]
     * 0����ͨ�û���1��ϵͳ����Ա
     * returnValue[1]
     * 0: �û����ڵ����ܲ��ԣ�1���û����붼��ȷ
    */
    public String[] checkUserPassM(String loginid, String pass){
        String[] returnValue = new String[2];
        returnValue[0]="-1"; //0����ͨ�û���1��ϵͳ����Ա
        returnValue[1]="-1"; //0: �û����ڵ����ܲ��ԣ�1���û����붼��ȷ
        if (loginid.indexOf(";")>-1||loginid.indexOf("--")>-1||loginid.indexOf(" ")>-1||loginid.indexOf("'")>-1) {
            writeLog("illegal sql statement input loginid:" + loginid);
            returnValue[0]="-2";
            return returnValue;
        }
        RecordSet rs = new RecordSet();
        String sql = "";
        String idTemp ="0";
        String passwordTemp ="";
        sql = "select count(id),id from HrmResource where loginid='"+loginid+"' group by id";
        rs.executeSql(sql);
        if(rs.next()&&(Util.getIntValue(rs.getString(1),0)>0)){
            idTemp = rs.getString(2);
            returnValue[0]="0";
            returnValue[1] = "0";
            sql = "select password from HrmResource where id= " + idTemp;
            rs.executeSql(sql);
            if(rs.next()){
                passwordTemp = Util.null2String(rs.getString(1));
                if(passwordTemp.equals(pass))
                        returnValue[1] = "1";
            }
        } 
        return returnValue;
    }
    
    public String getUserCheckByDactylogram(HttpServletRequest request, HttpServletResponse response, String login_id, String user_password, String login_type, String login_file,String validateCode,String messages,String languid,boolean ismutilangua) throws Exception {
    	StaticObj staticobj = null;
        staticobj = StaticObj.getInstance();
        String frommail=Util.null2String(request.getParameter("frommail"));//��Ի���Ҫ���ʼ�ֱ�ӵ�½�������ǰ�ȫ�ԣ��ڲ�ʹ�ã�
        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        RecordSet rs2 = new RecordSet();
        char separator = Util.getSeparator();
        String message = "";

        Calendar today = Calendar.getInstance();
        String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
        String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);

        Document doc;
        try {

            String isLicense = (String) staticobj.getObject("isLicense");
            //���� 2004-9-16 , TD1096 , License���������Ʋ�������
            
            LN ckLicense = new LN();
            message=ckLicense.CkLicense(currentdate);
            if (!message.equals("1")) {
                return "19";
            } else {
                staticobj.putObject("isLicense", "true");
            }
            
            //����û�������start
			String concurrentFlag = Util.null2String(ckLicense.getConcurrentFlag());
			int hrmnumber = Util.getIntValue(ckLicense.getHrmnum());
			if("1".equals(concurrentFlag)){
				LicenseCheckLogin  lchl = new LicenseCheckLogin();
				if(lchl.getLicUserCheck(login_id,hrmnumber)){
					recordFefuseLogin(login_id); //�ܾ���½��¼
					return "26";
				}
			}
            //����û�������end

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
            	//edit by chujun 2009-03-05 TD10057
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();
                int needvalidate=settings.getNeedvalidate();//0: ��,1: ��
                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }

            rs.executeSql("select * from HrmResource where loginid='" + login_id + "'");
            if(rs.next()){
                
                String startdate = rs.getString("startdate");
                String enddate = rs.getString("enddate");
                
                int status = rs.getInt("status");
                if (status != 0 && status != 1 && status != 2 && status != 3) {
                    return "17";
                }
                User user = new User();

                user.setUid(rs.getInt("id"));
                user.setLoginid(login_id);
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setAliasname(rs.getString("aliasname"));
                user.setTitle(rs.getString("title"));
                user.setTitlelocation(rs.getString("titlelocation"));
                user.setSex(rs.getString("sex"));
				//user.setPwd(user_password);
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
                // request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
                request.getSession(true).setAttribute("weaver_user@bean", user);
                //request.getSession(true).setAttribute("moniter", new OnLineMonitor("" + user.getUID(),user.getLoginip()));

                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", login_id, 172800);
                
                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
                Util.setCookie(response, "languageidweaver", languageidweaver, 172800);
                
                //Util.setCookie(response, "dactylogram", rs.getString("dactylogram"), 172800);
                //Util.setCookie(response, "assistantdactylogram", rs.getString("assistantdactylogram"), 172800);

                char separater = Util.getSeparator();
                rs.execute("HrmResource_UpdateLoginDate", rs.getString("id") + separater + currentdate);

                SysMaintenanceLog log = new SysMaintenanceLog();
                log.resetParameter();
                log.setRelatedId(rs.getInt("id"));
                log.setRelatedName((rs.getString("firstname") + " " + rs.getString("lastname")).trim());
                log.setOperateType("6");
                log.setOperateDesc("");
                log.setOperateItem("60");
                log.setOperateUserid(rs.getInt("id"));
                log.setClientAddress(Util.getIpAddr(request));
                log.setSysLogInfo();

                //����Ƿ��пͻ���ϵ�ƻ�
/* �����޸� 2004��05��08 ��¼���ͻ���ϵ�ƻ���ǰ��ѡȡ���еļ�¼��������Ҫ�����ڸ���Ϊѡ������ */
                char flag = 2;
                String sql = "";
                rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "0");
                sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + rs.getInt("id");
                rs1.executeSql(sql);
                if (rs1.next()) {
                    if (Util.getIntValue(rs1.getString(1), 0) > 0)
                        rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "1");
                } else {
                  return "15";
                }
              }else{
              	rs.executeSql("select * from HrmResourceManager where loginid='" + login_id + "'");
              	rs.next();

                String startdate = rs.getString("startdate");
                String enddate = rs.getString("enddate");

                int status = rs.getInt("status");
                if (status != 0 && status != 1 && status != 2 && status != 3) {
                    return "17";
                }
                User user = new User();

                user.setUid(rs.getInt("id"));
                user.setLoginid(login_id);
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setAliasname(rs.getString("aliasname"));
                user.setTitle(rs.getString("title"));
                user.setTitlelocation(rs.getString("titlelocation"));
                user.setSex(rs.getString("sex"));
				//user.setPwd(user_password);
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
                // request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
                request.getSession(true).setAttribute("weaver_user@bean", user);
                //request.getSession(true).setAttribute("moniter", new OnLineMonitor("" + user.getUID(),user.getLoginip()));

                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", login_id, 172800);
                
                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
                Util.setCookie(response, "languageidweaver", languageidweaver, 172800);
                
                //Util.setCookie(response, "dactylogram", rs.getString("dactylogram"), 172800);
                //Util.setCookie(response, "assistantdactylogram", rs.getString("assistantdactylogram"), 172800);

                char separater = Util.getSeparator();
                rs.execute("HrmResource_UpdateLoginDate", rs.getString("id") + separater + currentdate);

                SysMaintenanceLog log = new SysMaintenanceLog();
                log.resetParameter();
                log.setRelatedId(rs.getInt("id"));
                log.setRelatedName((rs.getString("firstname") + " " + rs.getString("lastname")).trim());
                log.setOperateType("6");
                log.setOperateDesc("");
                log.setOperateItem("60");
                log.setOperateUserid(rs.getInt("id"));
                log.setClientAddress(Util.getIpAddr(request));
                log.setSysLogInfo();

                //����Ƿ��пͻ���ϵ�ƻ�
/* �����޸� 2004��05��08 ��¼���ͻ���ϵ�ƻ���ǰ��ѡȡ���еļ�¼��������Ҫ�����ڸ���Ϊѡ������ */
                char flag = 2;
                String sql = "";
                rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "0");
                sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + rs.getInt("id");
                rs1.executeSql(sql);
                if (rs1.next()) {
                    if (Util.getIntValue(rs1.getString(1), 0) > 0)
                        rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "1");
                } else {
                  return "15";
                }
              }
            }

        } catch (Exception e) {
            writeLog(e);
            throw e;
        }
        return message;
    }
    public static void checkLicenseInfo() {
		RecordSet rs = new RecordSet();
		StaticObj staticobj = StaticObj.getInstance();
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
	}

	/**
     * �ܾ���½��¼
     * @param loginid
     */
    public void recordFefuseLogin(String loginid){
    	
    	SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
    	Calendar calendar=Calendar.getInstance();
    	String currentdate=dateFormat.format(calendar.getTime());
    	int currentYear=calendar.get(Calendar.YEAR);
    	int currentMonth=calendar.get(Calendar.MONTH)+1;
    	int currentHour=calendar.get(Calendar.HOUR_OF_DAY);
    	
    	String sql="select id from HrmRefuseCount where refuse_date='"+currentdate+"' and refuse_hour="+currentHour+" and refuse_loginid='"+loginid+"'";
    	RecordSet rs=new RecordSet();
    	rs.execute(sql);
    	if(!rs.next()){
    		sql="insert into HrmRefuseCount(refuse_date,refuse_year,refuse_month,refuse_hour,refuse_loginid)" +
    			"values('"+currentdate+"',"+currentYear+","+currentMonth+","+currentHour+",'"+loginid+"')";
    		rs.execute(sql);
    	}	
    }

}
