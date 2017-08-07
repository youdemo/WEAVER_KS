package weaver.workflow.request;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

/**
 * Created by adore on 2016/9/30.
 * ����������ת������Ϣ��ʾ
 */
public class CallBackTestAction extends BaseBean implements Action {
    private final static String joinstring = "|";

    public String execute(RequestInfo request) {
        String requestid = request.getRequestid();
        RecordSet rs = new RecordSet();
        String tablename = request.getRequestManager().getBillTableName();
        String src = request.getRequestManager().getSrc();
        String workflowid = request.getWorkflowid();
        String sql = "";
        //��ȡ���õ�����
        String projectfieldname = Util.null2String(getPropValue("SAPAjax", "sapprj319fieldname"));//��Ŀ���
        String ztfieldname = Util.null2String(getPropValue("SAPAjax", "zt319fieldname"));//�ͻ�
        String sysNum = "";
        String zt = "";
        if (!" ".equals(tablename)) {

            sql = "select * from " + tablename + " where requestid = " + requestid;
            //new BaseBean().writeLog("sql1---------" + sql);
            rs.executeSql(sql);
            if (rs.next()) {
                sysNum = Util.null2String(rs.getString("sysNum"));
            }

            if (sysNum.equals("")) {
                //�����쳣 ���ش�����Ϣ
                request.getRequestManager().setMessageid(System.currentTimeMillis() + "");
                request.getRequestManager().setMessagecontent("��Ŀ����Ϊ�գ����޸ı������ύ��");
            }
        }
        return SUCCESS;
    }
}
