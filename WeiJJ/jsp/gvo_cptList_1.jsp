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
<%
	int userid = user.getUID();
	String receiver = Util.null2String(request.getParameter("receiver"));
	String visitor = Util.null2String(request.getParameter("visitor"));
	String company = Util.null2String(request.getParameter("company"));
	String location = Util.null2String(request.getParameter("location"));
	String info = Util.null2String(request.getParameter("idkey")); 
	
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
		
		<script language="JavaScript">
	<%if(info!=null && !"".equals(info)){

		if("0".equals(info)){%>
			top.Dialog.alert("更新成功！")
		<%}

		else if("1".equals(info)){%>
			top.Dialog.alert("更新失败！")
		<%}
	}%>
	</script>
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
		RCMenu += "{更新状态,javascript:doEdit(this),_TOP} ";
		RCMenuHeight += RCMenuHeightStep;
		%>
		<%@ include file="/systeminfo/RightClickMenu_wev8.jsp"%>
		<FORM id=weaver name=weaver method=post action="" >
			<table id="topTitle" cellpadding="0" cellspacing="0">
				<tr>
					<td>
					</td>
					<td class="rightSearchSpan" style="text-align:right;">
						<input type=button class="e8_btn_top" onclick="doEdit(this);" value="更新状态">
						<span title="<%=SystemEnv.getHtmlLabelName(23036,user.getLanguage())%>" class="cornerMenu"></span>
					</td>
				</tr>
			</table>
			
		</FORM>
		<%
		//待装 0
		String backfields = " id,zcbh,zcmc,(select lbmc from uf_zcfl where id = substr(zclx,instr(zclx,'_',-1)+1)) zclx,zcguanly,sybgr,"
		+"(select selectname from workflow_selectitem where fieldid=38627 and selectvalue=zczt) zczt, "
		+" (select kfmc from uf_zckxx where id = substr(ejk,instr(ejk,'_',-1)+1)) ejk ";
		String fromSql  = " from uf_zcxxxx  ";
		String sqlWhere = " zczt=0 and sfddhs=1";

		String orderby = " id " ;
		String tableString = "";
		String operateString= "";
		if(userid>1){
			sqlWhere += " and zcguanly="+userid;
		}
		//out.println("select "+ backfields + fromSql + "where"+ sqlWhere);
		//out.println(sqlWhere);
		//out.println("userid="+userid);
		tableString =" <table tabletype=\"checkbox\" pagesize=\""+ PageIdConst.getPageSize(guard_pageId,user.getUID(),PageIdConst.HRM)+"\" pageId=\""+guard_pageId+"\">"+
			"	   <sql backfields=\""+backfields+"\" sqlform=\""+fromSql+"\" sqlwhere=\""+Util.toHtmlForSplitPage(sqlWhere)+"\"  sqlorderby=\""+orderby+"\"  sqlprimarykey=\"id\" sqlsortway=\"asc\" sqlisdistinct=\"false\"/>"+
			operateString +
			"			<head>";
				tableString+="<col width=\"13%\" labelid=\"-10257\" text=\"资产编号\" column=\"zcbh\" orderkey=\"zcbh\" />"+
				"		<col width=\"13%\" labelid=\"18939\" text=\"资产名称\" column=\"zcmc\" orderkey=\"zcmc\" />"+
				"		<col width=\"13%\" labelid=\"-10263\" text=\"资产类型\" column=\"zclx\" orderkey=\"zclx\" />"+
				"		<col width=\"13%\" labelid=\"-10258\" text=\"所属库（二级）\" column=\"ejk\" orderkey=\"ejk\" />"+
				"		<col width=\"13%\" labelid=\"18939\" text=\"管理部门资产管理员\" column=\"zcguanly\" orderkey=\"zcguanly\" transmethod=\"weaver.hrm.resource.ResourceComInfo.getResourcename\" />"+
				"		<col width=\"13%\" labelid=\"-10263\" text=\"使用保管人\" column=\"sybgr\" orderkey=\"sybgr\" transmethod=\"weaver.hrm.resource.ResourceComInfo.getResourcename\" />"+
				"		<col width=\"13%\" labelid=\"-10258\" text=\"资产状态\" column=\"zczt\" orderkey=\"zczt\" />"+
			"			</head>"+
		" </table>";
		%>
		<wea:SplitPageTag isShowTopInfo="false" tableString="<%=tableString%>" mode="run"/>
		<script type="text/javascript">
		
			function doEdit(){
				
				var ids = _xtable_CheckedCheckboxId();
				//alert("ids="+ids);
				
				if(ids == ""){
					window.top.Dialog.alert("<%=SystemEnv.getHtmlLabelName(19689,user.getLanguage())%>");
					return false;
				}
				
				Dialog.confirm("确认更新资产状态？", function (){
						weaver.action="/gvo/shaw/cpt/jsp/cpt_update_1.jsp?id="+ids;
						weaver.submit();
					}, function () {}, 320, 90,false);
			}
			
		</script>
		<SCRIPT language="javascript" src="/js/datetime_wev8.js"></script>
		<SCRIPT language="javascript" defer="defer" src="/js/JSDateTime/WdatePicker_wev8.js"></script>
		<script type="text/javascript" src="/js/selectDateTime_wev8.js"></script>
	</BODY>
</HTML>