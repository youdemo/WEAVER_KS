package weaver.interfaces.shaw.util;

import weaver.workflow.webservices.WorkflowRequestInfo;
import weaver.workflow.webservices.WorkflowServiceImpl;

public class BackFlowUtil {
    /**
     * �����˻ذ�ť
     *
     * @param requestid ����id
     * @param userid    ��ǰҪ�˻ؽڵ������id
     * @return
     */
    public String backFlow(String requestid, String userid) {
        WorkflowServiceImpl ws = new WorkflowServiceImpl();
        WorkflowRequestInfo wri = new WorkflowRequestInfo();
        String result = ws.submitWorkflowRequest(wri, Integer.valueOf(requestid), Integer.valueOf(userid), "reject", "�Զ��˻�");
        return result;
    }
}
