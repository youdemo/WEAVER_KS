<%@ page import="weaver.general.Util" %>
<%@ page import="weaver.general.TimeUtil" %>
<%@ page import="java.util.*" %>
<%@ page import="weaver.general.BaseBean" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<jsp:useBean id="res" class="weaver.conn.RecordSet" scope="page" />
<jsp:useBean id="log" class="weaver.general.BaseBean" scope="page" />

<%
    String logTime = Util.null2String(request.getParameter("logTime"));
	String empids = Util.null2String(request.getParameter("empids"));
	String onlineIds = Util.null2String(request.getParameter("onlineIds"));
    String sql = " select operateuserid,logtime from s_online_list where logtime>= "+logTime+" and operateuserid in("+empids+") ";
	rs.executeSql(sql);
	//out.print("sql="+sql);
	while(rs.next()) {
		String onlineId = Util.null2String(rs.getString("operateuserid"));
		onlineIds+=onlineId+",";
    }
    onlineIds+="0";
	out.print(onlineIds);
%>


