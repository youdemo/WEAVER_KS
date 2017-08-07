package weaver.interfaces.shaw.gcl;

import weaver.formmode.setup.ModeRightInfo;
import weaver.interfaces.shaw.util.InsertUtil;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by adore on 2017/3/14.
 * 协鑫测试环境
 * 表单建模创建项目时自动生成项目子任务
 * E7
 * <p>
 * Update on 2017/3/21
 * E7流程触发需要FTriggerFlag 默认值为0
 */
public class CreateProjMissionTest implements Action {
    BaseBean log = new BaseBean();//定义写入日志的对象

    public String execute(RequestInfo info) {
        log.writeLog("进入子任务生成CreateProjMissionTest——————");

        String workflowID = info.getWorkflowid();//获取工作流程Workflowid的值，表单建模中为modeId
        String requestid = info.getRequestid();//获取requestid的值，表单建模中为数据 id

        RecordSet rs = new RecordSet();
        RecordSet res = new RecordSet();
        String sql = "";
        String tableName = "";
        String mainID = "";
        String tableNameMission = "";
        String ytlx = "";//业态类型

        sql = " Select tablename From Workflow_bill Where id in (select formid from modeinfo where id= " + workflowID + ")";

        rs.execute(sql);
        if (rs.next()) {
            tableName = Util.null2String(rs.getString("tablename"));
            log.writeLog("tablename=" + tableName);
        }

        if (!"".equals(tableName)) {
            //tableNamedt = tableName + "_dt1";

            // 查询主表
            sql = "select * from " + tableName + " where id=" + requestid;
            rs.execute(sql);
            if (rs.next()) {
                mainID = Util.null2String(rs.getString("ID"));//获取主表中的id，作为明细表中的mainid
                ytlx = Util.null2String(rs.getString("ytlx"));
                log.writeLog("mainID=" + mainID);
            }

            //int num_exist = 0;
            String sql_mission = "select rwmc,fzr,sfkx,ssjd from formtable_main_2790 where sfkx=0 and ssjd=0 and remark=" + ytlx;
            rs.execute(sql_mission);
            while (rs.next()) {
                String miName = Util.null2String(rs.getString("rwmc"));
                String miCharge = Util.null2String(rs.getString("fzr"));
                log.writeLog("miName=" + miName);

                //一阶段只生成一条子任务
                //String sql_0 = " select count(id) as num_cc from formtable_main_2789 where ssxm = " + mainID + " and rwmc = '" + miName + "' and fzr=" + miCharge + " ";
                String sql_0 = " select count(id) as num_cc from formtable_main_2789 where ssxm = " + mainID;
                int num_cc = 0;
                res.executeSql(sql_0);
                if (res.next()) {
                    num_cc = res.getInt("num_cc");
                    log.writeLog("num_cc=" + num_cc);
                }

                if (num_cc == 0) {
                    Map<String, String> mapStr = new HashMap<String, String>();
                    mapStr.put("ssxm", mainID);
                    mapStr.put("rwmc", miName);
                    mapStr.put("fzr", miCharge);
                    mapStr.put("formmodeid", "588");
                    mapStr.put("modedatacreater", miCharge);
                    mapStr.put("FTriggerFlag", "0");

                    tableNameMission = "formtable_main_2789";//一阶段任务表
                    InsertUtil IU = new InsertUtil();
                    IU.insert(mapStr, tableNameMission);


                    String sql_1 = " select id,formmodeid from formtable_main_2789 where ssxm = " + mainID + " and rwmc = '" + miName + "' and fzr=" + miCharge + " ";
                    res.executeSql(sql_1);
                    log.writeLog("sql_1=" + sql_1);
                    if (res.next()) {
                        String dataID = Util.null2String(res.getString("id"));
                        String formmodeid = Util.null2String(res.getString("formmodeid"));
                        log.writeLog("dataID=" + dataID);
                        //新插入数据权限重构
                        ModeRightInfo modeRightInfo = new ModeRightInfo();
                        modeRightInfo.editModeDataShare(Integer.parseInt(miCharge), 588, Integer.parseInt(dataID));
                        //i:人员ID;i1:formmodeid;i2:id
                        modeRightInfo.editModeDataShareForModeField(Integer.parseInt(miCharge), 588, Integer.parseInt(dataID));
                    }

                } else {
                    log.writeLog("___________________任务已存在，不需要生成新的任务。");
                }
            }

        } else {
            log.writeLog("流程信息获取失败!");
            return "-1";
        }
        return SUCCESS;
    }
}
