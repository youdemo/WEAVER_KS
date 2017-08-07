package weaver.interfaces.shaw.util;

import weaver.workflow.webservices.WorkflowRequestInfo;
import weaver.workflow.webservices.WorkflowServiceImpl;

public class BackFlowUtil {
    /**
     * 调用退回按钮
     *
     * @param requestid 流程id
     * @param userid    当前要退回节点操作者id
     * @return
     */
    public String backFlow(String requestid, String userid) {
        WorkflowServiceImpl ws = new WorkflowServiceImpl();
        WorkflowRequestInfo wri = new WorkflowRequestInfo();
        String result = ws.submitWorkflowRequest(wri, Integer.valueOf(requestid), Integer.valueOf(userid), "reject", "自动退回");
        return result;
    }
}
