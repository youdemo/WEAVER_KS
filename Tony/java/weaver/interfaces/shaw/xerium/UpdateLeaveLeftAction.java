package weaver.interfaces.shaw.xerium;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

/**
 * Created by adore on 2017/4/6.
 * 修改数据后更新当年年假基数剩余和年假上年结余剩余
 * 因为触发器太多，所以用action代替，防止冲突
 */
public class UpdateLeaveLeftAction implements Action {
    BaseBean log = new BaseBean();//定义写入日志的对象

    public String execute(RequestInfo info) {
        log.writeLog("进入假期剩余更新UpdateLeaveLeftAction——————");

        String workflowID = info.getWorkflowid();//获取工作流程Workflowid的值，表单建模中为modeId
        String requestid = info.getRequestid();//获取requestid的值，表单建模中为数据 id

        RecordSet rs = new RecordSet();
        RecordSet res = new RecordSet();
        String sql = "";
        String tableName = "";
        String mainID = "";//主表id
        String annual = "";//当年年假基数
        String lastAnnual = "";//上年年假剩余

        sql = " Select tablename From Workflow_bill Where id in (select formid from modeinfo where id= " + workflowID + ")";

        rs.execute(sql);
        if (rs.next()) {
            tableName = Util.null2String(rs.getString("tablename"));
            //log.writeLog("tablename=" + tableName);
        }

        if (!"".equals(tableName)) {
            //tableNamedt = tableName + "_dt1";

            // 查询主表
            sql = "select * from " + tableName + " where id=" + requestid;
            rs.execute(sql);
            if (rs.next()) {
                mainID = Util.null2String(rs.getString("ID"));//获取主表中的id，作为明细表中的mainid
                annual = Util.null2String(rs.getString("nj"));
                lastAnnual = Util.null2String(rs.getString("lastLeft"));
                //log.writeLog("mainID=" + mainID);
                if ("".equals(annual)) {
                    annual = "0";
                }

                if ("".equals(lastAnnual)) {
                    lastAnnual = "0";
                }

                String sql_update = " update " + tableName + " set gsjq = " + annual + ",gsjqz=" + lastAnnual + " where id= " + mainID;
                log.writeLog("sql_update=" + sql_update);

                res.executeSql(sql_update);
            }

        } else {
            log.writeLog("流程信息获取失败!");
            return "-1";
        }
        return SUCCESS;
    }
}
