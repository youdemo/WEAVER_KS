package weaver.workflow.request;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

/**
 * Created by adore on 2016/9/30.
 * 测试流程流转错误信息提示
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
        //读取配置的设置
        String projectfieldname = Util.null2String(getPropValue("SAPAjax", "sapprj319fieldname"));//项目编号
        String ztfieldname = Util.null2String(getPropValue("SAPAjax", "zt319fieldname"));//客户
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
                //调用异常 返回错误信息
                request.getRequestManager().setMessageid(System.currentTimeMillis() + "");
                request.getRequestManager().setMessagecontent("项目名称为空，请修改表单重新提交！");
            }
        }
        return SUCCESS;
    }
}
