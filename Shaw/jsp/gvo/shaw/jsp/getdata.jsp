<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="weaver.common.StringUtil"%>
<%@page import="weaver.general.Util"%>
<%@page import="weaver.conn.RecordSet"%>
<%@page import="weaver.hrm.passwordprotection.domain.HrmResource"%>
<%@page import="weaver.systeminfo.SystemEnv"%>
<%@page import="weaver.hrm.passwordprotection.manager.HrmPasswordProtectionSetManager"%>
<%@page import="weaver.hrm.passwordprotection.manager.HrmResourceManager"%>
<%@page import="weaver.hrm.passwordprotection.manager.HrmResourceManagerManager"%>
<%@page import="weaver.hrm.common.Constants"%>
<%@page import="weaver.common.MessageUtil"%>
<%@page import="weaver.hrm.passwordprotection.domain.HrmPasswordProtectionQuestion"%>
<%@page import="weaver.hrm.passwordprotection.manager.HrmPasswordProtectionQuestionManager"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Enumeration"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="weaver.common.DateUtil"%>
<%@page import="weaver.hrm.HrmUserVarify"%>
<%@page import="weaver.hrm.common.Tools"%>
<%@page import="weaver.hrm.common.AjaxManager"%>
<%@page import="ln.LN"%>
<%@page import="java.util.List"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="weaver.hrm.User"%>
<%@page import="weaver.file.Prop"%>
<%@page import="weaver.general.GCONST"%>
<%@page import="weaver.ldap.LdapUtil"%>
<%@page import="java.util.Random"%>


<%!
public static boolean ifEqlTarget(String val, String target) {
	if(val == null || val.equals("")) {
		return false;
	}
	if(!val.equals(target)) {
		return false;
	}
	return true;
}

private static String checkLoginIdMsg(String id, String resourceid, boolean needJson,String type,int languageid){
		RecordSet RS = new RecordSet();
		StringBuffer sql = new StringBuffer("select id,lastname,loginid,{fEmail},mobile,(select COUNT(id) from hrm_protection_question where user_id = {tName}.id and delflag = 0) as qCount from {tName} where loginid = '")
		.append(StringUtil.vString(id)).append("' ");
		if(StringUtil.isNotNull(resourceid)){
			sql.append(" and id != ").append(resourceid);
		}
		String message = "";
		String _sql = StringUtil.replace(sql.toString(), "{fEmail}", "email");
		_sql = StringUtil.replace(_sql, "{tName}", "HrmResource");
		RS.executeSql(_sql + (needJson ? " and (accounttype = 0 or accounttype is null) " : ""));
		StringBuffer result = new StringBuffer();
		HrmResource resource = new HrmResource();
		int qCount = 0;
		if(RS.next()){
			resource.setId(RS.getInt(1));
			resource.setLastname(StringUtil.vString(RS.getString(2)));
			resource.setLoginid(StringUtil.vString(RS.getString(3)));
			resource.setEmail(StringUtil.vString(RS.getString(4)));
			resource.setMobile(StringUtil.vString(RS.getString(5)));
			qCount = RS.getInt("qCount");
		}
		if(resource.getId().intValue() == 0){
			_sql = StringUtil.replace(sql.toString(), "{fEmail}", "'' as email");
			_sql = StringUtil.replace(_sql, "{tName}", "HrmResourceManager");
			RS.executeSql(_sql);
			if(RS.next()){
				resource.setId(RS.getInt(1));
				resource.setLastname(StringUtil.vString(RS.getString(2)));
				resource.setLoginid(StringUtil.vString(RS.getString(3)));
				resource.setEmail(StringUtil.vString(RS.getString(4)));
				resource.setMobile(StringUtil.vString(RS.getString(5)));
				qCount = RS.getInt("qCount");
			}
		}
		int rid = Util.getIntValue(resource.getId()+"",0);
		int typeid = Util.getIntValue(type,0);
		String email = StringUtil.vString(resource.getEmail());
		String mobile = StringUtil.vString(resource.getMobile());
		
		String ret = "0";
		String mode=Prop.getPropValue(GCONST.getConfigFile() , "authentic");
		if(mode.equals("ldap")){
			RecordSet rs = new RecordSet();
			rs.executeSql("select isADAccount from hrmresource where id="+rid);
			if(rs.next() &&  "1".equals(Util.null2String(rs.getString("isADAccount")))){
				ret = "1";
			}
		} 
	if(!ret.equals("1")){
		if(rid == 0){
			message = SystemEnv.getHtmlLabelName(127829, languageid);
		} else {
			if(typeid == 0 && mobile == ""){
				message = SystemEnv.getHtmlLabelName(81618, languageid);
			}else if(typeid == 1 && qCount == 0){
				message = SystemEnv.getHtmlLabelName(125970, languageid);
			}else if(typeid == 2 && email == ""){
				message = SystemEnv.getHtmlLabelName(125971, languageid);
			}
		}
	}else{
		message = SystemEnv.getHtmlLabelNames("33268,126690", languageid);
	}
		if("".equals(message)){
			message = rid+"";
		}else{
			//统一返回的错误消息，以防恶意猜测攻击。
			if(!message.equals(SystemEnv.getHtmlLabelName(127829, languageid))){
				//System.out.println(message);
				message = SystemEnv.getHtmlLabelName(127829, languageid);
			}
		}
		
		return message;
	}

	private static String getReceiverByLoginid(String loginid,String type){
		RecordSet RS = new RecordSet();
		String receiver = "";
		String sql = "select * from HrmResource where loginid='"+loginid+"' and (accounttype = 0 or accounttype is null)";
		RS.executeSql(sql);
		String mobile="",email="";
		if(RS.next()){
			mobile = RS.getString("mobile");
			email = RS.getString("email");
		}
		if("sendSMS".equals(type)){
			receiver = mobile;
		}else if("sendEmail".equals(type)){
			receiver = email;
		}
		return receiver;
	}
	
	public static String getData(String str, String param){
		RecordSet RS = new RecordSet();
		String result = "";
		str = StringUtil.vString(str);
		param = StringUtil.vString(param);
		String[] params = param.split(";");
		if(params == null || params.length != 2) return "";
		String cmd = StringUtil.vString(params[0]);
		String data = StringUtil.vString(params[1]);
		if(cmd.equals("getHrmChoiceImage")){
			String[] dataArray = StringUtil.split(data,"+");
			StringBuffer sb = new StringBuffer();
			for(String _d : dataArray){
				sb.append(_d);
			}
			String[] allDate = sb.toString().split(",");
			if(allDate.length == 2){
				String currentdate = DateUtil.getCurrentDate();
				if((currentdate.compareTo(dataArray[0])>=0 || StringUtil.isNull(dataArray[0])) && (currentdate.compareTo(dataArray[1])<=0 || StringUtil.isNull(dataArray[1]))){
					result = "<img src='/images/BacoCheck.gif'>";
				}
			}
			if(HrmUserVarify.isUserOnline(str)) {
				result += "<img src='/images/State_LoggedOn.gif'>";
			}
		} else if(cmd.equals("getTResourceName")){
			RS.executeSql("select 1 from HrmResourceManager where loginid = '"+str+"'");
			if(RS.next()) result = "HrmResourceManager";
			
			result = Tools.vString(result, data);
		} else if(cmd.equals("getAccountType")){
			RS.executeSql("select accounttype from HrmResource where id = "+str);
			if(RS.next()) result = RS.getString(1);
			
			result = Tools.vString(result, data);
		} else if(cmd.equals("getLnScCount")){
			result = getLnScResult(data);
		}
		return result;
	}
	
	private static String getLnScResult(String param){
		RecordSet RS = new RecordSet();
		final int F_Y = 0;
		final int F_N = 1;
		int type = F_N;
		int count = 0;
		LN license = new LN();
		license.InLicense();
		type = StringUtil.parseToInt(license.getScType(), F_N);
		count = StringUtil.parseToInt(license.getScCount(), 0);
		count = type == F_Y ? (count < 0 ? 0 : count) : 0;
		
		String result = "";
		if(param.equals("ct")){
			result = String.valueOf(count);
		} else if(param.equals("mf")){
			int allSubCompany = 0;
			RS.executeSql("select COUNT(id) from HrmSubCompany where supsubcomid = 0 and (canceled is null or canceled != '1')");
			if(RS.next()) 
				allSubCompany = RS.getInt(1);
			result = String.valueOf(count == 0 || allSubCompany < count);
		}
		return result;
	}
	
	private static String checkLoginId(String id, String resourceid, boolean needJson){
		RecordSet RS = new RecordSet();
		StringBuffer sql = new StringBuffer("select id,lastname,loginid,{fEmail},mobile,(select COUNT(id) from hrm_protection_question where user_id = {tName}.id and delflag = 0) as qCount from {tName} where loginid = '")
		.append(StringUtil.vString(id)).append("' ");
		if(StringUtil.isNotNull(resourceid)){
			sql.append(" and id != ").append(resourceid);
		}
		String _sql = StringUtil.replace(sql.toString(), "{fEmail}", "email");
		_sql = StringUtil.replace(_sql, "{tName}", "HrmResource");
		RS.executeSql(_sql + (needJson ? " and (accounttype = 0 or accounttype is null) " : ""));
		StringBuffer result = new StringBuffer();
		HrmResource resource = new HrmResource();
		int qCount = 0;
		if(RS.next()){
			resource.setId(RS.getInt(1));
			resource.setLastname(StringUtil.vString(RS.getString(2)));
			resource.setLoginid(StringUtil.vString(RS.getString(3)));
			resource.setEmail(StringUtil.vString(RS.getString(4)));
			resource.setMobile(StringUtil.vString(RS.getString(5)));
			qCount = RS.getInt("qCount");
		}
		if(resource.getId().intValue() == 0){
			_sql = StringUtil.replace(sql.toString(), "{fEmail}", "'' as email");
			_sql = StringUtil.replace(_sql, "{tName}", "HrmResourceManager");
			RS.executeSql(_sql);
			if(RS.next()){
				resource.setId(RS.getInt(1));
				resource.setLastname(StringUtil.vString(RS.getString(2)));
				resource.setLoginid(StringUtil.vString(RS.getString(3)));
				resource.setEmail(StringUtil.vString(RS.getString(4)));
				resource.setMobile(StringUtil.vString(RS.getString(5)));
				qCount = RS.getInt("qCount");
			}
		}
		if(needJson){
			JSONObject obj = new JSONObject();
			try {
				obj.put("id", resource.getId());
			} catch (JSONException e) {}
			
			result.append(obj.toString());
		} else {
			result.append(resource.getId().intValue() != 0 ? "1" : "0");
		}
		return result.toString();
	}
	
 %>
<%
	request.setCharacterEncoding("UTF-8");
	response.setContentType("text/html; charset=UTF-8");
	response.setHeader("Cache-Control", "no-cache");
	java.io.PrintWriter pout = response.getWriter();
	try{
		StringBuffer result = new StringBuffer();
		String id = StringUtil.getURLDecode(request.getParameter("id"));
		String cmd = StringUtil.getURLDecode(request.getParameter("cmd"));
		if(cmd.equalsIgnoreCase("forgotPasswordCheckMsg")){
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			String type = StringUtil.getURLDecode(request.getParameter("type"));
			int languageid = Util.getIntValue(StringUtil.getURLDecode(request.getParameter("languageid")), 7) ;
			result.append(checkLoginIdMsg(loginid, null, true,type,languageid));
		}else if(cmd.equalsIgnoreCase("checkValicateCode")){
		
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			String validatecode = StringUtil.getURLDecode(request.getParameter("validatecode"));
            String validateRand="";
            validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
            if(!validateRand.toLowerCase().equals(validatecode.trim().toLowerCase())){
				result.append("");
            }else{
				result.append(new Random().nextInt()+"");
            }
            
		}else if(cmd.equalsIgnoreCase("checkSMSCode")){
		
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			String validatecode = StringUtil.getURLDecode(request.getParameter("validatecode"));
			String phoneCodeInp = StringUtil.getURLDecode(request.getParameter("phoneCode"));
			
            String validateRand="";
            validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
            String phoneCode="";
            phoneCode=Util.null2String((String)request.getSession(true).getAttribute("phoneCode"));
            if(!validateRand.toLowerCase().equals(validatecode.trim().toLowerCase())){
				result.append("");
            }else{
	            if("".equals(phoneCodeInp.trim().toLowerCase())){
						result.append("");
	            }else{
		            if(!phoneCode.toLowerCase().equals(phoneCodeInp.trim().toLowerCase())){
						result.append("");
		            }else{
						result.append(new Random().nextInt()+"");
		            }
	            }
            }
            
		}else if(cmd.equalsIgnoreCase("checkEmailCode")){
		
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			String validatecode = StringUtil.getURLDecode(request.getParameter("validatecode"));
			String emailCodeInp = StringUtil.getURLDecode(request.getParameter("emailCode"));
			
            String validateRand="";
            validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
            String emailCode="";
            emailCode=Util.null2String((String)request.getSession(true).getAttribute("emailCode"));
            if(!validateRand.toLowerCase().equals(validatecode.trim().toLowerCase())){
				result.append("");
            }else{
	            if("".equals(emailCodeInp.trim().toLowerCase())){
						result.append("");
	            }else{
		            if(!emailCode.toLowerCase().equals(emailCodeInp.trim().toLowerCase())){
						result.append("");
		            }else{
						result.append(new Random().nextInt()+"");
		            }
	            }
            }
            
		}else if(cmd.equalsIgnoreCase("invalidateSMSCode")){
		
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			String validatecode = StringUtil.getURLDecode(request.getParameter("validatecode"));
			
            String validateRand="";
            validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
            if(!validateRand.toLowerCase().equals(validatecode.trim().toLowerCase())){
				result.append("");
            }else{
			   request.getSession(true).removeAttribute("phoneCode");
				result.append(new Random().nextInt()+"");
            }
            
		}else if(cmd.equalsIgnoreCase("invalidateEmailCode")){
		
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			String validatecode = StringUtil.getURLDecode(request.getParameter("validatecode"));
			
            String validateRand="";
            validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
            if(!validateRand.toLowerCase().equals(validatecode.trim().toLowerCase())){
				result.append("");
            }else{
			   request.getSession(true).removeAttribute("emailCode");
				result.append(new Random().nextInt()+"");
            }
            
		}else if(cmd.equalsIgnoreCase("sendSMS")){
			String content = StringUtil.getURLDecode(request.getParameter("content"));
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			String validatecode = StringUtil.getURLDecode(request.getParameter("validatecode"));
			String receiver = getReceiverByLoginid(loginid,"sendSMS");
//			if(StringUtil.isNotNull(receiver)) receiver = StringUtil.decode(receiver);
			String newPassword = "";
			boolean isChange = false;
             String validateRand="";
             validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
             if(!validateRand.toLowerCase().equals(validatecode.trim().toLowerCase())){
					result.append("");
             }else{
				HrmPasswordProtectionSetManager manager = new HrmPasswordProtectionSetManager();
				if(StringUtil.isNotNull(id) && !id.equals("0")){
					newPassword = manager.getRandomPassword();
					content = StringUtil.replace(Constants.PASSWORD_MESSSAGE, "{pswd}", newPassword);
					isChange = true;
				}
				String phone = "";
				boolean bool = MessageUtil.sendSMS(receiver, content);
				if(bool && isChange){
					manager.changePassword(id, loginid, newPassword);
					if(receiver.length() - 4 > 0){
						phone = receiver.substring(0, receiver.length() - 4);
					}
					phone += "****";
				}
				if(!phone.equals("")){
					try {
					   request.getSession(true).removeAttribute("validateRand");
					}catch (Exception e) {
				  	}	
				}
				result.append(phone);
             }
		}else if(cmd.equalsIgnoreCase("sendSMSCode")){
			String content = StringUtil.getURLDecode(request.getParameter("content"));
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			String validatecode = StringUtil.getURLDecode(request.getParameter("validatecode"));
			String receiver = getReceiverByLoginid(loginid,"sendSMS");
//			if(StringUtil.isNotNull(receiver)) receiver = StringUtil.decode(receiver);
			String newPassword = "";
			boolean isChange = false;
             String validateRand="";
             validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
             if(!validateRand.toLowerCase().equals(validatecode.trim().toLowerCase())){
					result.append("");
             }else{
				HrmPasswordProtectionSetManager manager = new HrmPasswordProtectionSetManager();
				if(StringUtil.isNotNull(id) && !id.equals("0")){
					newPassword = manager.getRandomPassword();
					content = StringUtil.replace(Constants.PHONECODE_MESSSAGE, "{pswd}", newPassword);
					isChange = true;
				}
				String phone = "";
				boolean bool = MessageUtil.sendSMS(receiver, content);
				if(bool && isChange){
					//manager.changePassword(id, loginid, newPassword);
					if(receiver.length() - 4 > 0){
						phone = receiver.substring(0, receiver.length() - 4);
					}
					phone += "****";
				}
				if(!phone.equals("")){
					try {
					   request.getSession(true).removeAttribute("phoneCode");
					   if(!"".equals(newPassword)){
							// 将手机验证码存入session
							request.getSession(true).setAttribute("phoneCode", newPassword);
					   }
					}catch (Exception e) {
				  	}	
				}
				result.append(phone);
             }
		} else if(cmd.equalsIgnoreCase("sendEmailCode")){
			String subject = StringUtil.getURLDecode(request.getParameter("subject"));
			String content = StringUtil.getURLDecode(request.getParameter("content"));
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			String validatecode = StringUtil.getURLDecode(request.getParameter("validatecode"));
			String receiver = getReceiverByLoginid(loginid,"sendEmail");
			if(StringUtil.isNull(subject)) subject = "E-cology密码找回";
			String newPassword = "";
			boolean isChange = false;
             String validateRand="";
             validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
             if(!validateRand.toLowerCase().equals(validatecode.trim().toLowerCase())){
					result.append("");
             }else{
				HrmPasswordProtectionSetManager manager = new HrmPasswordProtectionSetManager();
				if(StringUtil.isNotNull(id) && !id.equals("0")){
					newPassword = manager.getRandomPassword();
					content = StringUtil.replace(Constants.EMAILCODE_MESSSAGE, "{pswd}", newPassword);
					isChange = true;
				}
				boolean bool = MessageUtil.sendEmail(receiver, subject, content);
				String email = "";
				if(bool && isChange) {
					//manager.changePassword(id, loginid, newPassword);
					if(receiver.length() - 4 > 0){
						email = receiver.substring(0, receiver.length() - 4);
					}
					email += "****";
				}
				if(!email.equals("")){
					try {
					   request.getSession(true).removeAttribute("emailCode");
					   if(!"".equals(newPassword)){
							// 将邮箱验证码存入session
							request.getSession(true).setAttribute("emailCode", newPassword);
					   }
					}catch (Exception e) {
				  	}	
				}
				result.append(email);
             }
		}else if(cmd.equalsIgnoreCase("sendEmail")){
			String subject = StringUtil.getURLDecode(request.getParameter("subject"));
			String content = StringUtil.getURLDecode(request.getParameter("content"));
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			String validatecode = StringUtil.getURLDecode(request.getParameter("validatecode"));
			String receiver = getReceiverByLoginid(loginid,"sendEmail");
			if(StringUtil.isNull(subject)) subject = "E-cology密码找回";
			String newPassword = "";
			boolean isChange = false;
             String validateRand="";
             validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
             if(!validateRand.toLowerCase().equals(validatecode.trim().toLowerCase())){
					result.append("");
             }else{
				HrmPasswordProtectionSetManager manager = new HrmPasswordProtectionSetManager();
				if(StringUtil.isNotNull(id) && !id.equals("0")){
					newPassword = manager.getRandomPassword();
					content = StringUtil.replace(Constants.PASSWORD_MESSSAGE, "{pswd}", newPassword);
					isChange = true;
				}
				boolean bool = MessageUtil.sendEmail(receiver, subject, content);
				String email = "";
				if(bool && isChange) {
					manager.changePassword(id, loginid, newPassword);
					if(receiver.length() - 4 > 0){
						email = receiver.substring(0, receiver.length() - 4);
					}
					email += "****";
				}
				if(!email.equals("")){
					try {
					   request.getSession(true).removeAttribute("validateRand");
					}catch (Exception e) {
				  	}	
				}
				result.append(email);
             }
		} else if(cmd.equalsIgnoreCase("verifyQuestion")){
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			String qid = StringUtil.getURLDecode(request.getParameter("qid"));
			String answer = StringUtil.getURLDecode(request.getParameter("answer"));
			HrmPasswordProtectionQuestionManager manager = new HrmPasswordProtectionQuestionManager();
			Map<String, Comparable> map = new HashMap<String, Comparable>();
			map.put("sql_userId", "and t.user_id in (select id from "+getData(loginid, "getTResourceName;HrmResource")+" where loginid = '"+loginid+"') ");
			map.put("id", qid);
			map.put("answer", answer);
			List<HrmPasswordProtectionQuestion> list = manager.find(map);
			if(list != null && list.size() > 0&&((HrmPasswordProtectionQuestion)list.get(0)).getUserId()>0){
				request.getSession(true).setAttribute("validateLoginid",loginid);
			}
//			result.append(list != null && list.size() > 0);
			result.append((list != null && list.size() > 0)?((HrmPasswordProtectionQuestion)list.get(0)).getUserId():("false"));
		}else if(cmd.equalsIgnoreCase("forgotPasswordCheck")){
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			result.append(checkLoginId(loginid, null, true));
		}else if(cmd.equalsIgnoreCase("saveNewPassword")){
			RecordSet rs = new RecordSet();
			String loginid = StringUtil.getURLDecode(request.getParameter("loginid"));
			String type = StringUtil.getURLDecode(request.getParameter("type"));
			
    		if("".equals(type)){
			    String validateLoginid=Util.null2String((String)request.getSession(true).getAttribute("validateLoginid"));
			  	try {
					   request.getSession(true).removeAttribute("validateLoginid");
					}catch (Exception e) {}	
			    if(validateLoginid.length()==0 || !validateLoginid.toLowerCase().equals(loginid.trim().toLowerCase())){
			    	response.sendRedirect("/Refresh.jsp?loginfile=/login/Login.jsp?logintype=1");
						return;
			    }
    		}
			String newPassword = StringUtil.getURLDecode(request.getParameter("newpswd"));
			rs.executeSql("select id from "+AjaxManager.getData(loginid, "getTResourceName;HrmResource")+" where loginid='"+loginid+"'");
			rs.next();
			String userid = StringUtil.vString(rs.getString("id"),"0");
			String qid = StringUtil.vString(request.getParameter("qid"),"0");
    		if(!"".equals(type)){
				new HrmPasswordProtectionSetManager().changePassword(null, loginid, newPassword);
			}else{
				rs.executeSql("select 1 from hrm_protection_question where user_id="+userid+" and id in("+qid+")");
				if(rs.next()){
					new HrmPasswordProtectionSetManager().changePassword(id, loginid, newPassword);
				}else{
					response.sendRedirect("/hrm/password/forgotPassword.jsp");
				}
			}
		} else if(cmd.equalsIgnoreCase("ppset")){
			User user = HrmUserVarify.getUser (request , response) ;
			if(user == null){
				response.sendRedirect("/login/Login.jsp");
				return ;
			}else if(!id.equals(""+user.getUID())){
	    	response.sendRedirect("/Refresh.jsp?loginfile=/login/Login.jsp?logintype=1");
				return;
	    }
			HrmPasswordProtectionSetManager manager = new HrmPasswordProtectionSetManager();
			String checked = StringUtil.getURLDecode(request.getParameter("checked"));
			manager.set(StringUtil.parseToLong(id), Boolean.valueOf(checked));
		} else if(cmd.equalsIgnoreCase("insertQuestion")){
			User user = HrmUserVarify.getUser (request , response) ;
			if(user == null){
				response.sendRedirect("/login/Login.jsp");
				return ;
			}else{
				long userid = StringUtil.parseToLong(user.getUID()+"");
				HrmPasswordProtectionQuestion bean = null;
				Map<String, HrmPasswordProtectionQuestion> qmap = new LinkedHashMap<String, HrmPasswordProtectionQuestion>();
				Enumeration enu = request.getParameterNames();
				int maxSize = 0;
				String indexs = "";
				while(enu.hasMoreElements()){  
					String paraName = StringUtil.vString(enu.nextElement());  
					if(paraName.equalsIgnoreCase("userid") || paraName.equalsIgnoreCase("cmd")) continue;
					String[] params = paraName.split("_"); 
					if(params == null || params.length != 2) continue;
					String key = "q"+params[1];
					if(qmap.containsKey(key)){
						bean = qmap.get(key);
					} else {
						bean = new HrmPasswordProtectionQuestion();
						qmap.put(key, bean);
						maxSize++;
						indexs += (indexs.length()==0?"":",") +params[1];
					}
					if(params[0].equalsIgnoreCase("question")){
						bean.setQuestion(StringUtil.getURLDecode(request.getParameter(paraName)));
					} else if(params[0].equalsIgnoreCase("answer")){
						bean.setAnswer(StringUtil.getURLDecode(request.getParameter(paraName)));
					}
				}
				HrmPasswordProtectionQuestionManager manager = new HrmPasswordProtectionQuestionManager();
				Map<String, Long> map = new HashMap<String, Long>();
				map.put("userId", userid);
				manager.delete(map);
				
				String[] indexArray = indexs.split(",");
				int[] iArray = new int[indexArray.length];
				for(int i=0; i<indexArray.length; i++){
					iArray[i] = StringUtil.parseToInt(indexArray[i]);
				}
				Arrays.sort(iArray);
				for(int i=0; i<iArray.length; i++){
					if(qmap.containsKey("q"+iArray[i])){
						bean = (HrmPasswordProtectionQuestion)qmap.get("q"+iArray[i]);
						bean.setUserId(userid);
						manager.insert(bean);
					}
				}
			}
		}else if(cmd.equalsIgnoreCase("verifyPswd")){
			session.setAttribute("verifyPswd",null);
			JSONObject obj = new JSONObject();
			User user = HrmUserVarify.getUser (request , response) ;
			if(user == null){
				obj.put("result", "false");
				result.append(obj.toString());
			}else{
				boolean isExsit = true;
				RecordSet rs = new RecordSet();
				String isADAccount = "";
		        String isADAccountSql = "select isADAccount from HrmResource where id = "+id;
		        rs.executeSql(isADAccountSql);
		        if(rs.next()) {
		        	isADAccount = rs.getString("isADAccount");
		        }
		        String isUseLdap = Prop.getPropValue(GCONST.getConfigFile(), "authentic");    
	            if (ifEqlTarget(isUseLdap, "ldap") && ifEqlTarget(isADAccount, "1") && !"1".equals(id)) {
	            	LdapUtil util = LdapUtil.getInstance();
	            	isExsit = util.authentic(user.getLoginid(), request.getParameter("pswd"));
	            }else{
	            	String pswd = Util.getEncrypt(StringUtil.getURLDecode(request.getParameter("pswd")));
					Map<String, Comparable> map = new HashMap<String, Comparable>();
					map.put("id", id);
					map.put("password", pswd);
					isExsit = new HrmResourceManager().get(map) != null;
					if(!isExsit){
						isExsit = new HrmResourceManagerManager().get(map) != null;
					}
	            }
				try {
					if(isExsit){
						session.setAttribute("verifyPswd",user);
					}
					obj.put("result", String.valueOf(isExsit));
				} catch (JSONException e) {}
				result.append(obj.toString());
			}
		}else if(cmd.equalsIgnoreCase("verifyIsADAccount")){
			String ret = "0";
			String mode=Prop.getPropValue(GCONST.getConfigFile() , "authentic");
			if(mode.equals("ldap")){
				RecordSet rs = new RecordSet();
				rs.executeSql("select isADAccount from hrmresource where id="+id);
				if(rs.next() &&  "1".equals(Util.null2String(rs.getString("isADAccount")))){
					ret = "1";
				}
			}  
			result.append(ret);
		}else{
			result.append(weaver.hrm.common.AjaxManager.getData(request, application));
		}
		pout.print(result.toString());
		
	} catch (Exception e) {
		pout.print(e.toString());
	}
%>
