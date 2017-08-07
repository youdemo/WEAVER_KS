package weaver.interfaces.shaw.xerium;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

/**
 * Created by adore on 2017/6/19.
 * QTS流程退回
 * 当四种Type的经理都选择了‘It should be other claim’并且退回的时候，流程才会流转到Asia Controller。
 */
public class QtsReturnTypeWorkflowAction implements Action {
    public String execute(RequestInfo info) {

        BaseBean log = new BaseBean();
        RecordSet rs = new RecordSet();
        RecordSet res = new RecordSet();

        log.writeLog("进入QTS退回QtsReturnTypeWorkflowAction——————");

        String sql = "";
        String tableName = "";
        String requestid = info.getRequestid();
        String workflowid = info.getWorkflowid();
        sql = "Select tablename From Workflow_bill Where id in (" + "Select formid From workflow_base Where id=" + workflowid + ")";
        //new BaseBean().writeLog("sql---------" + sql);
        rs.executeSql(sql);
        if (rs.next()) {
            tableName = Util.null2String(rs.getString("tablename"));
        }
        if (!" ".equals(tableName)) {

            sql = "select * from " + tableName + " where requestid = " + requestid;
            rs.executeSql(sql);
            if (rs.next()) {
                String claimType = Util.null2String(rs.getString("claimType"));
                String sql_insert = " insert into formtable_main_39(reqid,claimType) values(" + requestid + "," + claimType + ")";
                res.executeSql(sql_insert);
            } else {
                log.writeLog("工作流信息获取错误!");
                return "-1";
            }

            String sql_check = " select COUNT(*) as num_check from(\n" +
                    "select reqid,claimType from formtable_main_39 group by reqid,claimType having reqid=" + requestid + "\n" +
                    ") a ";
            log.writeLog("sql_check=" + sql_check);
            res.executeSql(sql_check);
            if (res.next()) {
                int num_check = 0;
                num_check = res.getInt("num_check");

                if (num_check == 4) {
                    String sql_update = " update " + tableName + " set rejReason =0,claimType=4 where requestid=" + requestid;
                    log.writeLog("sql_update=" + sql_update);
                    rs.executeSql(sql_update);
                }
            }

        } else {
            log.writeLog("流程信息表读取错误");
            return "-1";
        }
        return SUCCESS;

    }

}
