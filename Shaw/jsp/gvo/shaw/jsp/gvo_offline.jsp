<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.conn.RecordSet"%>
<%@ page import="weaver.general.BaseBean"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.*,weaver.hrm.common.*" %>
<%@ include file="/systeminfo/init_wev8.jsp"%>
<%@ taglib uri="/WEB-INF/weaver.tld" prefix="wea"%>
<%@ taglib uri="/browserTag" prefix="brow"%>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<jsp:useBean id="ResourceComInfo" class="weaver.hrm.resource.ResourceComInfo" scope="page" />
<jsp:useBean id="DepartmentComInfo" class="weaver.hrm.company.DepartmentComInfo" scope="page"/>
<jsp:useBean id="SubCompanyComInfo" class="weaver.hrm.company.SubCompanyComInfo" scope="page"/>
<jsp:useBean id="xssUtil" class="weaver.filter.XssUtil" scope="page" />
<%
	int userid = user.getUID();
	String receiver = Util.null2String(request.getParameter("receiver"));
	String visitor = Util.null2String(request.getParameter("visitor"));
	String company = Util.null2String(request.getParameter("company"));
	String location = Util.null2String(request.getParameter("location"));
	String info = Util.null2String(request.getParameter("idkey")); 
	String resourceid=Util.null2String(request.getParameter("resourceid"));
	
%>
<HTML>
	<HEAD>
		<LINK href="/css/Weaver_wev8.css" type=text/css rel=STYLESHEET>
		<script language="javascript" src="/js/weaver_wev8.js"></script>
		<script type="text/javascript" src="/appres/hrm/js/mfcommon_wev8.js"></script>
		<script language=javascript src="/js/ecology8/hrm/HrmSearchInit_wev8.js"></script>
		<style>
		.checkbox {
			display: none
		}
		</style>
	</head>
	<%
	String imagefilename = "/images/hdMaintenance_wev8.gif";
	String titlename = SystemEnv.getHtmlLabelName(21039,user.getLanguage())
	+ SystemEnv.getHtmlLabelName(480, user.getLanguage())
	+ SystemEnv.getHtmlLabelName(18599, user.getLanguage())
	+ SystemEnv.getHtmlLabelName(352, user.getLanguage());
	String needfav = "1";
	String needhelp = "";
	
	String guard_pageId = "receive_info";
	%>
	<BODY>
		<%@ include file="/systeminfo/TopTitle_wev8.jsp"%>
		<%@ include file="/systeminfo/RightClickMenuConent_wev8.jsp"%>
		<%
		RCMenu += "{强制下线,javascript:getResourceid(this),_TOP} ";
		RCMenuHeight += RCMenuHeightStep;
		%>
		<%@ include file="/systeminfo/RightClickMenu_wev8.jsp"%>
		<FORM id=weaver name=weaver method=post action="" >
			<table id="topTitle" cellpadding="0" cellspacing="0">
				<tr>
					<td>
					</td>
					<td class="rightSearchSpan" style="text-align:right;">
						<input type=button class="e8_btn_top" onclick="getResourceid(this);" value="强制下线">
						<span title="<%=SystemEnv.getHtmlLabelName(23036,user.getLanguage())%>" class="cornerMenu"></span>
					</td>
				</tr>
			</table>
			<wea:layout type="4col" attributes="{'expandAllGroup':'true'}">
			<wea:group context='强制下线'>
				<wea:item>选择账号</wea:item>
				<wea:item>
					<%
						String lastname = "";
						if(resourceid.length()>0) lastname = ResourceComInfo.getLastnameAllStatus(resourceid);
						String subcomstr=SubCompanyComInfo.getRightSubCompany(user.getUID(),"HrmResourceAdd:Add",0);
						String sqlwhere = " (status < 5  )";
						String browserUrl = "/systeminfo/BrowserMain.jsp?url=/hrm/resource/MultiResourceBrowserByRight.jsp?rightStr=HrmResourceAdd:Add&fromHrmStatusChange=HrmResourceHire&sqlwhere="+xssUtil.put(sqlwhere)+"&selectedids=";
						String completeUrl = "/data.jsp?whereClause="+xssUtil.put("(status =0  or status = 2 or status = 3) and t1.subcompanyid1 in("+subcomstr+")");
					%>
					<brow:browser viewType="0" name="resourceid" browserValue='<%=resourceid %>'
             			browserUrl='<%=browserUrl %>'
             			hasInput="true" isSingle="true" hasBrowser = "true" isMustInput='2'
             			completeUrl='<%=completeUrl %>' linkUrl="javascript:openhrm($id$)"
             			browserSpanValue='<%=lastname %>'>     
        			</brow:browser>	
				</wea:item>
			</wea:group>
		</wea:layout>
		</FORM>
		<script type="text/javascript">
			//获取多选浏览按钮
			function getResourceid() {
				var ids = jQuery("#resourceid").val();
				console.log("ids="+ids.length);
				if(ids.length == 0){
					window.top.Dialog.alert("请先选择账号");
					return false;
				}else{
					ids=ids.split(",");
					//console.log("resourceid=" + ids);
					window.top.Dialog.confirm("<%=SystemEnv.getHtmlLabelName(81904,user.getLanguage())%>", function(){
						for (var i = 0; i < ids.length; i++) {
							var id = ids[i];
							forcedOffline(id);
							//console.log("人员ID=" + id);
						}
						window.top.Dialog.alert("所选用户已全部下线");
					});
				}
			}

			//强制下线
			function forcedOffline(id){
				//window.top.Dialog.confirm("<%=SystemEnv.getHtmlLabelName(81904,user.getLanguage())%>", function(){
					jQuery.ajax({
						url:"getdata.jsp?cmd=userOffline&uid="+id,
						type:"post",
						async:false,
					});
				//});
			}
		</script>
		<SCRIPT language="javascript" src="/js/datetime_wev8.js"></script>
		<SCRIPT language="javascript" defer="defer" src="/js/JSDateTime/WdatePicker_wev8.js"></script>
		<script type="text/javascript" src="/js/selectDateTime_wev8.js"></script>
	</BODY>
</HTML>