package weaver.interfaces.shaw.xerium;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

/**
 * Created by adore on 2017/4/6.
 * �޸����ݺ���µ�����ٻ���ʣ�������������ʣ��
 * ��Ϊ������̫�࣬������action���棬��ֹ��ͻ
 */
public class UpdateLeaveLeftAction implements Action {
    BaseBean log = new BaseBean();//����д����־�Ķ���

    public String execute(RequestInfo info) {
        log.writeLog("�������ʣ�����UpdateLeaveLeftAction������������");

        String workflowID = info.getWorkflowid();//��ȡ��������Workflowid��ֵ������ģ��ΪmodeId
        String requestid = info.getRequestid();//��ȡrequestid��ֵ������ģ��Ϊ���� id

        RecordSet rs = new RecordSet();
        RecordSet res = new RecordSet();
        String sql = "";
        String tableName = "";
        String mainID = "";//����id
        String annual = "";//������ٻ���
        String lastAnnual = "";//�������ʣ��

        sql = " Select tablename From Workflow_bill Where id in (select formid from modeinfo where id= " + workflowID + ")";

        rs.execute(sql);
        if (rs.next()) {
            tableName = Util.null2String(rs.getString("tablename"));
            //log.writeLog("tablename=" + tableName);
        }

        if (!"".equals(tableName)) {
            //tableNamedt = tableName + "_dt1";

            // ��ѯ����
            sql = "select * from " + tableName + " where id=" + requestid;
            rs.execute(sql);
            if (rs.next()) {
                mainID = Util.null2String(rs.getString("ID"));//��ȡ�����е�id����Ϊ��ϸ���е�mainid
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
            log.writeLog("������Ϣ��ȡʧ��!");
            return "-1";
        }
        return SUCCESS;
    }
}
