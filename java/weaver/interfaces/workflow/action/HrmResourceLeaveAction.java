package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

/**
 * Created by adore on 2017/4/1.
 */

public class HrmResourceLeaveAction extends BaseBean implements Action {

    public String execute(RequestInfo requestInfo) {
        String requestid = Util.null2String(requestInfo.getRequestid());
        this.writeLog("--->do action on requestid :" + requestid);
        String select_sql = "select * from formtable_main_3 where requestid = '" + requestid + "'";
        RecordSet rs = new RecordSet();
        RecordSet res = new RecordSet();
        double hours = 0.0D;
        String applicant = "";
        String leaveType = "";
        if (rs.executeSql(select_sql)) {
            while (rs.next()) {
                applicant = Util.null2String(rs.getString("Applicant"));
                hours = Util.getDoubleValue(rs.getString("LeavingTime"), 0.0D);
                leaveType = Util.null2String(rs.getString("leavingType"));
            }
        }

        if (!"0".equals(leaveType) && !"9".equals(leaveType)) {
            return "1";
        } else {
            if (!"".equals(applicant)) {
                double jbtx = 0.0D;
                double njbalance = 0.0D;
                //double gsbalance = 0.0D;
                //add by shaw 2017/4/1
                //double nj = 0.0D;
                double lastLeft = 0.0D;
                String dataID = "";

                String sqlString = " select id,name,isnull(gdjq,0) as gdjq,isnull(gsjq,0) as gsjq,isnull(gsjqz,0) as gsjqz "
                        + " from formtable_main_20 where name = '" + applicant + "'";
                rs.executeSql(sqlString);
                if (rs.next()) {
                    dataID = Util.null2String(rs.getString("id"));
                    jbtx = Util.getDoubleValue(rs.getString("jbtx"), 0.0D);
                    njbalance = Util.getDoubleValue(rs.getString("gdjq"), 0.0D);
                    //gsbalance = Util.getDoubleValue(rs.getString("gsjq"), 0.0D);

                    //nj = rs.getDouble("gsjq");
                    lastLeft = rs.getDouble("gsjqz");
                }

                String update_sql = "";
                if ("0".equals(leaveType)) {
                    if (lastLeft >= hours) {
                        String sql_update = " update formtable_main_20 set gsjqz= gsjqz-" + hours + " where id=" + dataID;
                        this.writeLog("sql_update1=" + sql_update);
                        res.executeSql(sql_update);
                    } else {
                        String sql_update = " update formtable_main_20 set gsjqz=0,gsjq=gsjq-" + hours + "+" + lastLeft + " where id=" + dataID;
                        this.writeLog("sql_update2=" + sql_update);
                        res.executeSql(sql_update);
                    }
                } else if ("9".equals(leaveType)) {
                    update_sql = "Update formtable_main_20 set jbtx = ISNULL(jbtx,0.0) - " + hours + " WHERE name = '" + applicant + "'";
                    this.writeLog("update_sql:" + update_sql);
                    rs.executeSql(update_sql);
                }
            }

            return "1";
        }
    }
}
