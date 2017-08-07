<%@ page import="weaver.general.Util" %>
<%@ page import="weaver.general.TimeUtil" %>
<%@ page import="java.util.*" %>
<%@ page import="weaver.general.BaseBean" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ include file="/systeminfo/init_wev8.jsp" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<jsp:useBean id="res" class="weaver.conn.RecordSet" scope="page" />
<jsp:useBean id="log" class="weaver.general.BaseBean" scope="page" />

<%
    //预报废 5
    String ids = Util.null2String(request.getParameter("id"));
    String sql = "";
    String idkey="";
    String tmp_ids=ids+"0";
    out.print("tmp_ids="+tmp_ids);
    if(!"".equals(ids)){
        sql = " update uf_zcxxxx set zczt=0 where id in("+tmp_ids+") ";
        rs.executeSql(sql);
        
        log.writeLog("sql1="+sql);
        idkey="0";
        out.print("sql="+sql);
		
    }else{
        idkey="1";
    }
   
    response.sendRedirect("/gvo/shaw/cpt/jsp/gvo_cptList_2.jsp?idkey="+idkey+" ");
%>


