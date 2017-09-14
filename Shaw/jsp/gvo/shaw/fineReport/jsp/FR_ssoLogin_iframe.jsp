<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ page import="weaver.general.Util"%>
<%@ page import="java.util.*,weaver.hrm.appdetach.*"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ include file="/systeminfo/init_wev8.jsp" %>
<%@ taglib uri="/WEB-INF/weaver.tld" prefix="wea"%>
<%@ taglib uri="/browserTag" prefix="brow"%>
<jsp:useBean id="RecordSet" class="weaver.conn.RecordSet" scope="page" />
<jsp:useBean id="ResourceComInfo" class="weaver.hrm.resource.ResourceComInfo" scope="page" />

<HTML>
	<HEAD>
		<LINK href="/css/Weaver_wev8.css" type=text/css rel=STYLESHEET>
		<script type="text/javascript" src="/appres/hrm/js/mfcommon_wev8.js"></script>
		<script language=javascript src="/js/ecology8/hrm/HrmSearchInit_wev8.js"></script>
		<style>
		.checkbox {
			display: none
		}
		</style>
	</head>
	<%
	String imagefilename = "/images/hdReport_wev8.gif";
	String titlename =SystemEnv.getHtmlLabelName(20536,user.getLanguage());
	String needfav ="1";
	String needhelp ="";
	%>
	<BODY>
		<div id="tabDiv">
			<span class="toggleLeft" id="toggleLeft" title="<%=SystemEnv.getHtmlLabelName(32814,user.getLanguage()) %>"><%=SystemEnv.getHtmlLabelName(20536,user.getLanguage()) %></span>
		</div>
		<div id="dialog">
			<div id='colShow'></div>
		</div>
		<%@ include file="/systeminfo/TopTitle_wev8.jsp" %>
		<%@ include file="/systeminfo/RightClickMenuConent_wev8.jsp" %>
		<%
		RCMenu += "{" + SystemEnv.getHtmlLabelName(527, user.getLanguage())+ ",javascript:onBtnSearchClick(),_self} ";
		RCMenuHeight += RCMenuHeightStep;//添加鼠标右键菜单：527为数据库中HtmlLableIndex表中对应的id，可通过id值获取显示的内容
		%>
		<%@ include file="/systeminfo/RightClickMenu_wev8.jsp" %>
		<%
		//FR单点登录用
		String nodeSql ="select loginid,password from HrmResource where id =  " + user.getUID();  
		RecordSet.executeSql(nodeSql);  
		String userpasswordstr = "";
		String loginidstr = "";
		if (RecordSet.next()){  
			userpasswordstr = RecordSet.getString("password");       
			loginidstr = RecordSet.getString("loginid");  
		}  
		%>
		<script languange="javascript">
			jQuery(document).ready(function () {
				var username = cjkEncode("<%=loginidstr%>");
				var password = "<%=userpasswordstr%>";
				password = password.toUpperCase();
				var scr = document.createElement("iframe"); //创建iframe      
				scr.src = " http://10.211.55.5:8075/WebReport/ReportServer?op=fs_load&cmd=sso&fr_username=" + username +
					"&fr_password=" + password; //将报表验证用户名密码的地址指向此iframe        
				if (scr.attachEvent) { //判断是否为ie浏览器  
					scr.attachEvent("onload", function () { //如果为ie浏览器则页面加载完成后立即执行
						//window.location = " http://localhost:8075/WebReport/ReportServer?op=fs"; //直接跳转到数据决策系统(用IP地址)  
						window.location = " http://10.211.55.5:8075/WebReport/ReportServer?op=fs"; //直接跳转到数据决策系统  
					});
				} else {
					scr.onload = function () { //其他浏览器则重新加载onload事件    
						/*跳转到指定登录成功页面,index.jsp   
						 var f = document.getElementById("login");   
						 f.submit();  */
						//window.location = " http://localhost:8075/WebReport/ReportServer?op=fs"; //直接跳转到数据决策系统(用IP地址)
						window.location = " http://10.211.55.5:8075/WebReport/ReportServer?op=fs"; //直接跳转到数据决策系统   						
					};
				}
	
				document.getElementsByTagName("head")[0].appendChild(scr); //将iframe标签嵌入到head中 
			});
	
			//cjkEncode方法的实现代码，放在网页head中或者用户自己的js文件中
			function cjkEncode(text) {
				if (text == null) {
					return "";
				}
				var newText = "";
				for (var i = 0; i < text.length; i++) {
					var code = text.charCodeAt(i);
					if (code >= 128 || code == 91 || code == 93) {  //91 is "[", 93 is "]".       
						newText += "[" + code.toString(16) + "]";
					} else {
						newText += text.charAt(i);
					}
				}
				return newText;
			}
		</script>
	<SCRIPT language="javascript" src="/js/datetime_wev8.js"></script>
	<SCRIPT language="javascript" defer="defer" src="/js/JSDateTime/WdatePicker_wev8.js"></script>
	<script type="text/javascript" src="/js/selectDateTime_wev8.js"></script>
</BODY>
</HTML>