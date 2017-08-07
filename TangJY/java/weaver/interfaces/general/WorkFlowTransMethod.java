//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package weaver.general;

import com.weaver.cssRenderHandler.JsonUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import weaver.conn.RecordSet;
import weaver.cpt.capital.CapitalComInfo;
import weaver.crm.Maint.CustomerInfoComInfo;
import weaver.docs.docs.DocComInfo;
import weaver.docs.docs.DocImageManager;
import weaver.docs.senddoc.DocReceiveUnitComInfo;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.StaticObj;
import weaver.general.Util;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.job.JobTitlesComInfo;
import weaver.hrm.resource.ResourceComInfo;
import weaver.interfaces.workflow.browser.Browser;
import weaver.interfaces.workflow.browser.BrowserBean;
import weaver.proj.Maint.ProjectInfoComInfo;
import weaver.rtx.RTXConfig;
import weaver.system.RequestDefaultComInfo;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.field.BrowserComInfo;
import weaver.workflow.monitor.Monitor;
import weaver.workflow.monitor.MonitorDTO;
import weaver.workflow.request.MailAndMessage;
import weaver.workflow.request.RequestCheckUser;
import weaver.workflow.request.ResourceConditionManager;
import weaver.workflow.request.WFForwardManager;
import weaver.workflow.request.WFLinkInfo;
import weaver.workflow.workflow.WfForceDrawBack;
import weaver.workflow.workflow.WfForceOver;
import weaver.workflow.workflow.WfFunctionManageUtil;
import weaver.workflow.workflow.WorkflowComInfo;
import weaver.workflow.workflow.WorkflowRequestComInfo;

public class WorkFlowTransMethod extends BaseBean {
    private ResourceComInfo rc = null;
    private CustomerInfoComInfo cci = null;
    private RecordSet rs = null;
    private RecordSet RecordSet = null;
    private WorkflowComInfo wf = null;
    private WorkflowComInfo WorkflowComInfo = null;
    private DepartmentComInfo DepartmentComInfo1 = null;
    private JobTitlesComInfo JobTitlesComInfo1 = null;
    private ProjectInfoComInfo ProjectInfoComInfo1 = null;
    private DocComInfo DocComInfo1 = null;
    private BrowserComInfo BrowserComInfo = null;
    private DocImageManager DocImageManager = null;
    private WorkflowRequestComInfo WorkflowRequestComInfo1 = null;
    private CapitalComInfo CapitalComInfo1 = null;
    private RequestDefaultComInfo RequestDefaultComInfo = null;
    private ResourceConditionManager rcm = null;
    private DocReceiveUnitComInfo duc = null;
    public int count = 0;

    public WorkFlowTransMethod() {
        try {
            this.cci = new CustomerInfoComInfo();
            this.rc = new ResourceComInfo();
            this.rs = new RecordSet();
            this.wf = new WorkflowComInfo();
            this.RequestDefaultComInfo = new RequestDefaultComInfo();
            this.DocComInfo1 = new DocComInfo();
            this.ProjectInfoComInfo1 = new ProjectInfoComInfo();
            this.BrowserComInfo = new BrowserComInfo();
            this.RecordSet = new RecordSet();
            this.DepartmentComInfo1 = new DepartmentComInfo();
            this.JobTitlesComInfo1 = new JobTitlesComInfo();
            this.DocImageManager = new DocImageManager();
            this.WorkflowRequestComInfo1 = new WorkflowRequestComInfo();
            this.CapitalComInfo1 = new CapitalComInfo();
            this.rcm = new ResourceConditionManager();
            this.duc = new DocReceiveUnitComInfo();
        } catch (Exception var2) {
            this.writeLog(var2);
        }

    }

    private boolean haveDetailMustAdd(String var1, int var2) {
        boolean var3 = true;
        String var4 = "";
        int var5 = 0;
        int var6 = 0;
        int var7 = 0;
        boolean var8 = false;
        RecordSet var9 = new RecordSet();
        RecordSet var10 = new RecordSet();
        var4 = "select formid,isbill from workflow_base where id=" + var1;
        var9.executeSql(var4);
        if(var9.next()) {
            var6 = var9.getInt(1);
            var5 = var9.getInt(2);
        }

        var4 = "select currentnodeid from workflow_requestbase where requestid=" + var2;
        var9.executeSql(var4);
        if(var9.next()) {
            var7 = var9.getInt(1);
        }

        var4 = "select groupid from workflow_NodeFormGroup where isneed=1 and nodeid=" + var7;
        var9.executeSql(var4);

        while(var9.next()) {
            int var13 = var9.getInt(1);
            if(var6 > 0 && var5 == 0) {
                var4 = "select id from workflow_formdetail where requestid=" + var2 + " and groupid=" + var13;
                var10.executeSql(var4);
                if(!var10.next()) {
                    return false;
                }
            }

            if(var6 < 0 && var5 == 1) {
                int var11 = var13 + 1;
                int var12 = -var6;
                var4 = "select id from formtable_main_" + var12 + "_dt" + var11 + " where mainid=(select id from formtable_main_" + var12 + " where requestid=" + var2 + ")";
                var10.executeSql(var4);
                if(!var10.next()) {
                    return false;
                }
            }
        }

        return var3;
    }

    public String getOpUserResultCheckBox(String var1) {
        String[] var2 = Util.TokenizerString2(var1, "+");
        String var3 = Util.null2String(var2[0]);
        String var4 = Util.null2String(var2[1]);
        RecordSet var5 = new RecordSet();
        var5.executeSql("select distinct userid from workflow_currentoperator where (isremark in (\'0\',\'1\') or (isremark=\'4\' and viewtype=0))  and requestid =" + var3 + " and userid=" + var4);
        return "" + var5.next();
    }

    public String getWFSearchResultName(String var1, String var2) {
        String var3 = "";
        if("1".equals(var2)) {
            var3 = "<a href=\"javaScript:openFullWindowHaveBar(\'/CRM/data/ViewCustomer.jsp?CustomerID=" + var1 + "\')\">" + this.cci.getCustomerInfoname(var1) + "</a>";
        } else {
            var3 = "<a href=\"javaScript:openhrm(" + var1 + ");\" onclick=\'pointerXY(event);\'>" + this.rc.getResourcename(var1) + "</a>";
        }

        return var3;
    }

    public String getWFSearchResultFlowName(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        int var6 = Util.getIntValue(var4[1], 0);
        int var7 = Util.getIntValue(Util.null2String(var4[2]), 0);
        int var8 = Util.getIntValue(Util.null2String(var4[3]), 7);
        int var9 = 0;
        int var10 = 0;
        this.rs.execute("select formid,isbill from workflow_base where id=" + var7);
        if(this.rs.next()) {
            var10 = this.rs.getInt(1);
            var9 = this.rs.getInt(2);
        }

        MailAndMessage var11 = new MailAndMessage();
        String var12 = var11.getTitle(Util.getIntValue(var5, -1), var7, var10, var8, var9);
        if(!var12.equals("")) {
            var1 = var1 + "<B>（" + var12 + "）</B>";
        }

        var3 = var1 + "(" + var5 + ")";
        if(var6 == 1) {
            var3 = "<a href=\"javaScript:openFullWindowHaveBar(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&ismonitor=1\')\">" + var3 + "</a>";
        }

        return var3;
    }

    public String getWFSearchResultUrgencyDegree(String var1, String var2) {
        String var3 = "";
        if("0".equals(var1)) {
            var3 = SystemEnv.getHtmlLabelName(225, Integer.parseInt(var2));
        } else if("1".equals(var1)) {
            var3 = SystemEnv.getHtmlLabelName(15533, Integer.parseInt(var2));
        } else if("2".equals(var1)) {
            var3 = SystemEnv.getHtmlLabelName(2087, Integer.parseInt(var2));
        }

        return var3;
    }

    public String getWFSearchResultCreateTime(String var1, String var2) {
        return var1 + " " + var2;
    }

    public String getWFSearchResultCheckBox(String var1) {
        String[] var2 = Util.TokenizerString2(var1, "+");
        String var3 = Util.null2String(var2[0]);
        String var4 = Util.null2String(var2[1]);
        int var5 = Util.getIntValue(var2[2]);
        int var6 = Util.getIntValue(var2[3]);
        int var7 = Util.getIntValue(var2[4]);
        int var8 = 0;
        int var9 = -1;
        boolean var10 = true;
        String var11;
        if("1".equals(var4)) {
            this.RecordSet.executeSql("select takisremark,isremark,isreminded,preisremark,id,groupdetailid,nodeid from workflow_currentoperator where requestid=" + var5 + " and userid=" + var7 + " and nodeid=" + var6 + " order by isremark,id");

            while(this.RecordSet.next()) {
                var8 = Util.getIntValue(this.RecordSet.getString("id"));
                var9 = Util.getIntValue(this.RecordSet.getString("takisremark"));
            }

            var11 = "select * from workflow_Forward where requestid=" + var5 + " and BeForwardid=" + var8;
            this.RecordSet.executeSql(var11);
            if(this.RecordSet.next()) {
                String var12 = Util.null2String(this.RecordSet.getString("IsFromWFRemark"));
                String var13 = Util.null2String(this.RecordSet.getString("IsSubmitedOpinion"));
                String var14 = Util.null2String(this.RecordSet.getString("IsBeForwardTodo"));
                String var15 = Util.null2String(this.RecordSet.getString("IsBeForwardSubmitAlready"));
                String var16 = Util.null2String(this.RecordSet.getString("IsBeForwardAlready"));
                String var17 = Util.null2String(this.RecordSet.getString("IsBeForwardSubmitNotaries"));
                String var18 = Util.null2String(this.RecordSet.getString("IsBeForward"));
                if(var9 != 2) {
                    if((!"0".equals(var12) || !"1".equals(var13)) && (!"1".equals(var12) || !"1".equals(var15)) && (!"2".equals(var12) || !"1".equals(var17))) {
                        var10 = false;
                    } else {
                        var10 = true;
                    }
                }

                if(var9 == 2) {
                    var10 = true;
                }
            }
        }

        var11 = "false";
        if(var4.equals("0")) {
            if("1".equals(this.getWFMultiSubmit(var3))) {
                var11 = "true";
            }

            if(var11.equals("true")) {
                if(var11.equals("true") && this.haveMustInput(var5)) {
                    var11 = "false";
                } else if(this.haveDetailMustAdd(var3, var5)) {
                    var11 = "true";
                } else {
                    var11 = "false";
                }
            }
        } else if(var4.equals("9")) {
            if("1".equals(this.getWFMultiSubmit(var3))) {
                var11 = "true";
            }
        } else if(var4.equals("1")) {
            if(var10 && "1".equals(this.getWFMultiSubmit(var3))) {
                var11 = "true";
            } else {
                var11 = "false";
            }
        }

        return var11;
    }

    public String getWFSearchRstCkBoxForMsg(String var1) {
        String var2 = "false";
        String[] var4 = Util.TokenizerString2(var1, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        String var7 = Util.null2String(var4[2]);
        boolean var3;
        if((!var6.equals("0") || var7.equals("1")) && !var6.equals("5")) {
            var3 = false;
        } else {
            var3 = true;
        }

        if(var5.equals("0")) {
            var2 = "true";
        }

        return var2;
    }

    public List<String> getWFSearchResultOperation(String var1, String var2, String var3) {
        String var4 = "false";
        String var5 = "false";
        String var6 = "false";
        String var7 = "false";
        String var8 = "false";
        String var9 = "false";
        boolean var10 = false;
        boolean var11 = false;
        boolean var12 = true;
        boolean var13 = false;
        boolean var14 = false;
        boolean var15 = false;
        ArrayList var16 = new ArrayList();
        this.getCurrentType(var1);
        String[] var18 = Util.TokenizerString2(var2, "+");
        String var19 = Util.null2String(var18[0]);
        String var20 = Util.null2String(var18[1]);
        String var21 = Util.null2String(var18[2]);
        String var22 = Util.null2String(var18[3]);
        String var23 = Util.null2String(var18[4]);
        String[] var24 = Util.TokenizerString2(var3, "_");
        String var25 = Util.null2String(var24[0]);
        String var26 = Util.null2String(var24[1]);
        String var27 = "";
        String var28 = "";
        boolean var29;
        if((!var20.equals("0") || var21.equals("1")) && !var20.equals("5")) {
            var29 = false;
        } else {
            var29 = true;
        }

        if(var19.equals("0")) {
            var10 = true;
        }

        String var30 = "";
        int var31 = -1;
        RecordSet var32 = new RecordSet();
        RecordSet var33 = new RecordSet();
        int var34 = 0;
        boolean var35 = false;
        int var36 = -1;
        int var37 = -1;
        String var38 = "";
        var33.executeSql("select handleforwardid,takisremark,isremark,isreminded,preisremark,id,groupdetailid,nodeid,(CASE WHEN isremark=9 THEN \'7.5\' ELSE isremark END) orderisremark from workflow_currentoperator where requestid=" + var1 + " and userid in (" + var25 + ") and usertype=" + var26 + " order by orderisremark,id ");

        while(var33.next()) {
            String var39 = Util.null2String(var33.getString("isremark"));
            var34 = Util.getIntValue(Util.null2String(var33.getString("id")));
            var31 = Util.getIntValue(var33.getString("preisremark"), 0);
            var36 = Util.getIntValue(var33.getString("takisremark"), 0);
            var37 = Util.getIntValue(var33.getString("handleforwardid"), -1);
            int var40 = Util.getIntValue(var33.getString("nodeid"));
            if(var39.equals("1") || var39.equals("9")) {
                WFLinkInfo var41 = new WFLinkInfo();
                var38 = var41.getNodeType(var40);
                break;
            }
        }

        var33.executeSql("select * from workflow_requestbase where requestid=" + var1);

        while(var33.next()) {
            var38 = Util.null2String(var33.getString("currentnodetype"));
        }

        WFForwardManager var59 = new WFForwardManager();
        var59.init();
        var59.setWorkflowid(Util.getIntValue(var23, 0));
        var59.setNodeid(Util.getIntValue(var22));
        var59.setRequestid(Util.getIntValue(var1, 0));
        var59.setIsremark(var20);
        var59.setBeForwardid(var34);
        var59.getWFNodeInfo();
        String var60 = Util.null2String(var59.getIsPendingForward());
        String var61 = Util.null2String(var59.getIsBeForwardTodo());
        String var42 = Util.null2String(var59.getIsBeForwardSubmitAlready());
        String var43 = Util.null2String(var59.getIsBeForwardSubmitNotaries());
        String var44 = Util.null2String(var59.getIsFromWFRemark());
        String var45 = Util.null2String(var59.getIsBeForwardAlready());
        String var46 = Util.null2String(var59.getIsAlreadyForward());
        String var47 = Util.null2String(var59.getIsSubmitForward());
        String var48 = Util.null2String(var59.getIsTakingOpinions());
        String var49 = Util.null2String(var59.getIsHandleForward());
        String var50 = Util.null2String(var59.getIsBeForward());
        boolean var51 = false;
        if(var20.equals("1") || var20.equals("9")) {
            if("0".equals(var44) && "1".equals(var61) || "1".equals(var44) && "1".equals(var45) || "2".equals(var44) && "1".equals(var50)) {
                var51 = true;
            }

            if(var20.equals("1") && var51 || var20.equals("9") && var60.equals("1")) {
                var11 = true;
            }
        }

        if(var60.equals("1") && !var20.equals("2") && !var20.equals("4") && var36 != -2) {
            var11 = true;
        }

        if(var46.equals("1") && var36 == -2 && var20.equals("0")) {
            var11 = true;
        }

        if(!"3".equals(var38) && var46.equals("1") && var20.equals("2") && (var31 == 0 || var31 == 8 || var31 == 9 || var31 == 1 && var36 == 2)) {
            var11 = true;
        }

        if("3".equals(var38) && var47.equals("1") && (var20.equals("2") || var20.equals("4")) && (var31 == 0 || var31 == 8 || var31 == 9 || var31 == 1 && var36 == 2)) {
            var11 = true;
        }

        String var53;
        if(var37 < 0 && var36 != 2 && var31 == 1 && var20.equals("2") && var18.length > 5) {
            int var52 = 0;
            var32.executeSql("select isremark,isreminded,preisremark,id,groupdetailid,nodeid from workflow_currentoperator where requestid=" + var1 + " and userid=" + var25 + " and usertype=" + var26 + " order by isremark,id");

            while(var32.next()) {
                var52 = Util.getIntValue(var32.getString("id"));
            }

            var53 = "select * from workflow_Forward where requestid=" + var1 + " and BeForwardid=" + var52;
            var32.executeSql(var53);
            if(var32.next()) {
                var44 = Util.null2String(var32.getString("IsFromWFRemark"));
                var61 = Util.null2String(var32.getString("IsBeForwardTodo"));
                var45 = Util.null2String(var32.getString("IsBeForwardAlready"));
                var50 = Util.null2String(var32.getString("IsBeForward"));
            }

            if("0".equals(var44) && "1".equals(var61) || "1".equals(var44) && "1".equals(var45) || "2".equals(var44) && "1".equals(var50)) {
                var51 = true;
            }

            if(var31 == 1 && var51) {
                var11 = true;
            }
        }

        String var62;
        if(var31 != 1 && !var20.equals("1") && !var20.equals("2") && var18.length > 5) {
            var62 = Util.null2String(var18[5]);
            var53 = Util.null2String(var59.getIsAlreadyForward());
            if((!var62.equals("done") || !var53.equals("1") || "3".equals(var38)) && (!var47.equals("1") || !"3".equals(var38)) && (!var62.equals("doing") || !var60.equals("1")) && !var51) {
                var11 = false;
            } else {
                var11 = true;
            }
        }

        if(var37 < 0 && var36 != 2 && var20.equals("1") && var18.length > 5) {
            if(var51) {
                var11 = true;
            } else {
                var11 = false;
            }
        }

        (new StringBuilder()).append("select * from workflow_nodecustomrcmenu where wfid=").append(var23).append(" and nodeid=").append(var22).toString();
        if(var20.equals("0")) {
            var32.executeSql("select nodeid from workflow_currentoperator c where c.requestid=" + var1 + " and c.userid in(" + var25 + ") and c.usertype=" + var26 + " and c.isremark=\'" + var20 + "\' ");
            var53 = "";
            if(var32.next()) {
                var53 = Util.null2String(var32.getString("nodeid"));
            }

            var62 = "select * from workflow_nodecustomrcmenu where wfid=" + var23 + " and nodeid=" + var53;
            if(!"".equals(var53)) {
                var32.executeSql(var62);
                if(var32.next()) {
                    var27 = Util.null2String(var32.getString("haswfrm"));
                    var28 = Util.null2String(var32.getString("hassmsrm"));
                }
            }
        }

        if("1".equals(var27)) {
            RequestCheckUser var64 = new RequestCheckUser();
            var64.setUserid(Util.getIntValue(var25, 0));
            var64.setWorkflowid(Util.getIntValue(var23));
            var64.setLogintype(var26);

            try {
                var64.checkUser();
                int var54 = var64.getHasright();
                if(var54 == 1) {
                    var13 = true;
                }
            } catch (Exception var58) {
                var13 = false;
            }
        }

        RTXConfig var65 = new RTXConfig();
        String var63 = var65.getPorp(RTXConfig.CUR_SMS_SERVER_IS_VALID);
        boolean var55 = false;
        if(var63 != null && var63.equalsIgnoreCase("true")) {
            var55 = true;
        } else {
            var55 = false;
        }

        User var56 = this.getUser(Util.getIntValue(var25, 0));
        if(var55 && "1".equals(var28) && HrmUserVarify.checkUserRight("CreateSMS:View", var56)) {
            var14 = true;
        }

        String var57 = "";
        var32.executeSql("select t1.ismodifylog,t2.currentstatus from workflow_base t1, workflow_requestbase t2 where t1.id=t2.workflowid and t2.requestid=" + var1);
        if(var32.next()) {
            var57 = var32.getString("isModifyLog");
        }

        if("1".equals(var57)) {
            var15 = true;
        }

        if(var10) {
            var4 = "true";
        }

        if(var11) {
            var5 = "true";
        }

        if(var12) {
            var6 = "true";
        }

        if(var13) {
            var7 = "true";
        }

        if(var14) {
            var8 = "true";
        }

        if(var15) {
            var9 = "true";
        }

        var16.add(var4);
        var16.add(var5);
        var16.add(var6);
        var16.add(var7);
        var16.add(var8);
        var16.add(var9);
        return var16;
    }

    public List<String> getWFAgentBackOperation(String var1, String var2) {
        ArrayList var3 = new ArrayList();
        String[] var4 = var2.split("_");
        String var5 = var4[0];
        String var6 = var4[1];
        if("0".equals(var6)) {
            var3.add("true");
        } else if("1".equals(var6)) {
            User var7 = this.getUser(Util.getIntValue(var5, 0));
            boolean var8 = HrmUserVarify.checkUserRight("WorkflowAgent:All", var7);
            var3.add(var8 + "");
        }

        return var3;
    }

    public String getCurrentType(String var1) {
        String var2 = "";
        this.rs.executeSql("SELECT * FROM workflow_Requestbase WHERE requestid  = \'" + var1 + "\'");
        if(this.rs.next()) {
            var2 = this.rs.getString("currentnodetype");
        }

        return var2;
    }

    public String getWFAgentBackOperationCheckBox(String var1) {
        String var2 = "false";
        String[] var3 = var1.split("_");
        String var4 = var3[0];
        String var5 = var3[1];
        if("0".equals(var5)) {
            var2 = "true";
        } else if("1".equals(var5)) {
            User var6 = this.getUser(Integer.parseInt(var4));
            boolean var7 = HrmUserVarify.checkUserRight("WorkflowAgent:All", var6);
            var2 = var7 + "";
        }

        return var2;
    }

    public List<String> getWfUrgerNewOperation(String var1, String var2) {
        ArrayList var3 = new ArrayList();
        String[] var4 = Util.TokenizerString2(var2, "+");
        int var5 = Util.getIntValue(Util.null2String(var4[0]));
        int var6 = Util.getIntValue(Util.null2String(var4[2]), 0);
        int var7 = Util.getIntValue(Util.null2String(var4[3]), 0);
        String var8 = "false";
        String var9 = "select b.lastoperatedate,b.lastoperatetime,b.creater,b.lastoperator, b.lastoperatortype from workflow_requestbase b where b.requestid = " + var5;
        String var10 = "";
        String var11 = "";
        this.rs.execute(var9);
        if(this.rs.next() && (var6 != this.rs.getInt(4) || var7 != this.rs.getInt(5))) {
            if(!"".equals(Util.null2String(this.rs.getString(1))) && !"".equals(Util.null2String(this.rs.getString(2)))) {
                var10 = this.rs.getString(1) + this.rs.getString(2);
                var9 = "select max(w.viewdate) as viewdate,max(w.viewtime) as viewtime from workflow_requestviewlog w where w.viewer=" + var6 + " and id=" + var5 + " group by id";
                this.rs.execute(var9);
                if(this.rs.next()) {
                    var11 = this.rs.getString(1) + this.rs.getString(2);
                    if(var11.compareTo(var10) < 0) {
                        var8 = "true";
                    }
                } else {
                    var8 = "true";
                }
            } else if(this.rs.getInt(3) != var6) {
                var9 = "select w.viewdate from workflow_requestviewlog w where w.viewer=" + var6 + " and id=" + var5;
                this.rs.execute(var9);
                if(!this.rs.next()) {
                    var8 = "true";
                }
            }
        }

        var3.add(var8);
        return var3;
    }

    public String getWfUrgerNewOperationCheckBox(String var1) {
        String[] var2 = Util.TokenizerString2(var1, "+");
        int var3 = Util.getIntValue(Util.null2String(var2[0]));
        int var4 = Util.getIntValue(Util.null2String(var2[2]), 0);
        int var5 = Util.getIntValue(Util.null2String(var2[3]), 0);
        String var6 = "false";
        String var7 = "select b.lastoperatedate,b.lastoperatetime,b.creater,b.lastoperator, b.lastoperatortype from workflow_requestbase b where b.requestid = " + var3;
        String var8 = "";
        String var9 = "";
        this.rs.execute(var7);
        if(this.rs.next() && (var4 != this.rs.getInt(4) || var5 != this.rs.getInt(5))) {
            if(!"".equals(Util.null2String(this.rs.getString(1))) && !"".equals(Util.null2String(this.rs.getString(2)))) {
                var8 = this.rs.getString(1) + this.rs.getString(2);
                var7 = "select max(w.viewdate) as viewdate,max(w.viewtime) as viewtime from workflow_requestviewlog w where w.viewer=" + var4 + " and id=" + var3 + " group by id";
                this.rs.execute(var7);
                if(this.rs.next()) {
                    var9 = this.rs.getString(1) + this.rs.getString(2);
                    if(var9.compareTo(var8) < 0) {
                        var6 = "true";
                    }
                } else {
                    var6 = "true";
                }
            } else if(this.rs.getInt(3) != var4) {
                var7 = "select w.viewdate from workflow_requestviewlog w where w.viewer=" + var4 + " and id=" + var3;
                this.rs.execute(var7);
                if(!this.rs.next()) {
                    var6 = "true";
                }
            }
        }

        return var6;
    }

    private User getUser(int var1) {
        User var2 = new User();

        try {
            ResourceComInfo var3 = new ResourceComInfo();
            DepartmentComInfo var4 = new DepartmentComInfo();
            var2.setUid(var1);
            var2.setLoginid(var3.getLoginID("" + var1));
            var2.setFirstname(var3.getFirstname("" + var1));
            var2.setLastname(var3.getLastname("" + var1));
            var2.setLogintype("1");
            var2.setSex(var3.getSexs("" + var1));
            var2.setLanguage(7);
            var2.setEmail(var3.getEmail("" + var1));
            var2.setLocationid(var3.getLocationid("" + var1));
            var2.setResourcetype(var3.getResourcetype("" + var1));
            var2.setJobtitle(var3.getJobTitle("" + var1));
            var2.setJoblevel(var3.getJoblevel("" + var1));
            var2.setSeclevel(var3.getSeclevel("" + var1));
            var2.setUserDepartment(Util.getIntValue(var3.getDepartmentID("" + var1), 0));
            var2.setUserSubCompany1(Util.getIntValue(var4.getSubcompanyid1(var2.getUserDepartment() + ""), 0));
            var2.setManagerid(var3.getManagerID("" + var1));
            var2.setAssistantid(var3.getAssistantID("" + var1));
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return var2;
    }

    private boolean haveMustInput(int var1) {
        boolean var2 = false;
        String var3 = "select currentnodeid,workflowid from workflow_requestbase where requestid=" + var1;
        RecordSet var4 = new RecordSet();
        RecordSet var5 = new RecordSet();
        var4.executeSql(var3);
        if(var4.next()) {
            int var6 = var4.getInt(1);
            int var7 = var4.getInt(2);
            var3 = "select ismode,showdes,printdes,toexcel from workflow_flownode where workflowid=" + var7 + " and nodeid=" + var6;
            var4.executeSql(var3);
            if(var4.next()) {
                String var8 = Util.null2String(var4.getString("ismode"));
                int var9 = Util.getIntValue(Util.null2String(var4.getString("showdes")), 0);
                int var10 = 0;
                int var11 = 0;
                int var12 = 0;
                String var13 = "";
                String var14 = "workflow_nodeform";
                var3 = "select formid,isbill from workflow_base where id=" + var7;
                var4.executeSql(var3);
                if(var4.next()) {
                    var12 = var4.getInt(1);
                    var11 = var4.getInt(2);
                }

                if(var8.equals("1") && var9 != 1) {
                    var3 = "select id from workflow_nodemode where isprint=\'0\' and workflowid=" + var7 + " and nodeid=" + var6;
                    var4.executeSql(var3);
                    if(var4.next()) {
                        var10 = var4.getInt("id");
                    } else {
                        var3 = "select id from workflow_formmode where isprint=\'0\' and formid=" + var12 + " and isbill=" + var11;
                        var4.executeSql(var3);
                        if(var4.next()) {
                            var10 = var4.getInt("id");
                        }
                    }
                }

                if(var10 > 0) {
                    var14 = "workflow_modeview";
                }

                String var15;
                String var16;
                if(var11 == 0) {
                    var3 = "select ff.fieldname,ff.fielddbtype from " + var14 + " nf ,workflow_formdict ff where nf.fieldid=ff.id and nf.nodeid=" + var6 + " and nf.ismandatory=1 and nf.fieldid>0 ";
                    var4.executeSql(var3);

                    while(true) {
                        while(var4.next()) {
                            var15 = Util.null2String(var4.getString(2));
                            if(var15.toLowerCase().indexOf("int") <= -1 && var15.toLowerCase().indexOf("float") <= -1 && var15.toLowerCase().indexOf("number") <= -1) {
                                if(var4.getDBType().equals("sqlserver")) {
                                    var16 = " convert(varchar," + var4.getString(1) + ") ";
                                    if(var13.equals("")) {
                                        var13 = var16 + " is null or " + var16 + "=\'\' or " + var16 + "=\' \'";
                                    } else {
                                        var13 = var13 + " or " + var16 + " is null or " + var16 + "=\'\' or " + var16 + "=\' \'";
                                    }
                                } else if(var13.equals("")) {
                                    if(var15.toLowerCase().indexOf("clob") > -1) {
                                        var13 = var4.getString(1) + " is null or dbms_lob.getlength(" + var4.getString(1) + ") = 0";
                                    } else {
                                        var13 = var4.getString(1) + " is null or " + var4.getString(1) + "=\'\' or " + var4.getString(1) + "=\' \'";
                                    }
                                } else if(var15.toLowerCase().indexOf("clob") > -1) {
                                    var13 = var13 + " or (" + var4.getString(1) + " is null or dbms_lob.getlength(" + var4.getString(1) + ") = 0)";
                                } else {
                                    var13 = var13 + " or " + var4.getString(1) + " is null or " + var4.getString(1) + "=\'\' or " + var4.getString(1) + "=\' \'";
                                }
                            } else if(var13.equals("")) {
                                var13 = var4.getString(1) + " is null";
                            } else {
                                var13 = var13 + " or " + var4.getString(1) + " is null";
                            }
                        }

                        if(!var13.equals("")) {
                            var3 = "select requestid from workflow_form where requestid=" + var1 + " and (" + var13 + ")";
                            var4.executeSql(var3);
                            if(var4.next()) {
                                return true;
                            }
                        }

                        var13 = "";
                        var3 = "select ff.fieldname,ff.fielddbtype from " + var14 + " nf ,workflow_formdictdetail ff where nf.fieldid=ff.id and nf.nodeid=" + var6 + " and nf.ismandatory=1 and nf.fieldid>0 ";
                        var4.executeSql(var3);

                        while(true) {
                            while(var4.next()) {
                                var15 = Util.null2String(var4.getString(2));
                                if(var15.toLowerCase().indexOf("int") <= -1 && var15.toLowerCase().indexOf("float") <= -1 && var15.toLowerCase().indexOf("number") <= -1) {
                                    if(var4.getDBType().equals("sqlserver")) {
                                        var16 = " convert(varchar," + var4.getString(1) + ") ";
                                        if(var13.equals("")) {
                                            var13 = var16 + " is null or " + var16 + "=\'\' or " + var16 + "=\' \'";
                                        } else {
                                            var13 = var13 + " or " + var16 + " is null or " + var16 + "=\'\' or " + var16 + "=\' \'";
                                        }
                                    } else if(var13.equals("")) {
                                        if(var15.toLowerCase().indexOf("clob") > -1) {
                                            var13 = var4.getString(1) + " is null or dbms_lob.getlength(" + var4.getString(1) + ") = 0";
                                        } else {
                                            var13 = var4.getString(1) + " is null or " + var4.getString(1) + "=\'\' or " + var4.getString(1) + "=\' \'";
                                        }
                                    } else if(var15.toLowerCase().indexOf("clob") > -1) {
                                        var13 = var13 + " or (" + var4.getString(1) + " is null or dbms_lob.getlength(" + var4.getString(1) + ") = 0)";
                                    } else {
                                        var13 = var13 + " or " + var4.getString(1) + " is null or " + var4.getString(1) + "=\'\' or " + var4.getString(1) + "=\' \'";
                                    }
                                } else if(var13.equals("")) {
                                    var13 = var4.getString(1) + " is null";
                                } else {
                                    var13 = var13 + " or " + var4.getString(1) + " is null";
                                }
                            }

                            if(!var13.equals("")) {
                                var3 = "select requestid from workflow_formdetail where requestid=" + var1 + " and (" + var13 + ")";
                                var4.executeSql(var3);
                                if(var4.next()) {
                                    return true;
                                }
                            }

                            return var2;
                        }
                    }
                } else if(var11 == 1) {
                    var15 = "";
                    var16 = "";
                    var3 = "select tablename,detailkeyfield from workflow_bill where id=" + var12;
                    var4.executeSql(var3);
                    if(var4.next()) {
                        var15 = var4.getString(1);
                        var16 = Util.null2String(var4.getString(2));
                    }

                    if(var16.equals("")) {
                        var16 = "mainid";
                    }

                    var3 = "select ff.fieldname,ff.fielddbtype from " + var14 + " nf ,workflow_billfield ff where (ff.viewtype is null or ff.viewtype=0) and nf.fieldid=ff.id and ff.billid=" + var12 + " and nf.nodeid=" + var6 + " and nf.ismandatory=1 and nf.fieldid>0 ";
                    var4.executeSql(var3);

                    while(true) {
                        String var17;
                        String var18;
                        while(var4.next()) {
                            var17 = Util.null2String(var4.getString(2));
                            if(var17.toLowerCase().indexOf("int") <= -1 && var17.toLowerCase().indexOf("float") <= -1 && var17.toLowerCase().indexOf("number") <= -1) {
                                if(var4.getDBType().equals("sqlserver")) {
                                    var18 = " convert(varchar," + var4.getString(1) + ") ";
                                    if(var13.equals("")) {
                                        var13 = var18 + " is null or " + var18 + "=\'\' or " + var18 + "=\' \'";
                                    } else {
                                        var13 = var13 + " or " + var18 + " is null or " + var18 + "=\'\' or " + var18 + "=\' \'";
                                    }
                                } else if(var13.equals("")) {
                                    if(var17.toLowerCase().indexOf("clob") > -1) {
                                        var13 = var4.getString(1) + " is null or dbms_lob.getlength(" + var4.getString(1) + ") = 0";
                                    } else {
                                        var13 = var4.getString(1) + " is null or " + var4.getString(1) + "=\'\' or " + var4.getString(1) + "=\' \'";
                                    }
                                } else if(var17.toLowerCase().indexOf("clob") > -1) {
                                    var13 = var13 + " or (" + var4.getString(1) + " is null or dbms_lob.getlength(" + var4.getString(1) + ") = 0)";
                                } else {
                                    var13 = var13 + " or " + var4.getString(1) + " is null or " + var4.getString(1) + "=\'\' or " + var4.getString(1) + "=\' \'";
                                }
                            } else if(var13.equals("")) {
                                var13 = var4.getString(1) + " is null";
                            } else {
                                var13 = var13 + " or " + var4.getString(1) + " is null";
                            }
                        }

                        if(!var13.equals("")) {
                            var3 = "select requestid from " + var15 + " where requestid=" + var1 + " and (" + var13 + ")";
                            var4.executeSql(var3);
                            if(var4.next()) {
                                return true;
                            }
                        }

                        var3 = "select tablename from workflow_billdetailtable where billid=" + var12;
                        var5.executeSql(var3);

                        String var19;
                        while(var5.next()) {
                            var17 = var5.getString(1);
                            var13 = "";
                            var3 = "select ff.fieldname,ff.fielddbtype from " + var14 + " nf ,workflow_billfield ff where ff.viewtype=1 and nf.fieldid=ff.id and ff.billid=" + var12 + " and ff.detailtable=\'" + var17 + "\' and nf.nodeid=" + var6 + " and nf.ismandatory=1 and nf.fieldid>0 order by ff.detailtable ";
                            var4.executeSql(var3);

                            while(true) {
                                while(var4.next()) {
                                    var18 = Util.null2String(var4.getString(2));
                                    if(var18.toLowerCase().indexOf("int") <= -1 && var18.toLowerCase().indexOf("float") <= -1 && var18.toLowerCase().indexOf("number") <= -1) {
                                        if(var4.getDBType().equals("sqlserver")) {
                                            var19 = " convert(varchar," + var17 + "." + var4.getString(1) + ") ";
                                            if(var13.equals("")) {
                                                var13 = var19 + " is null or " + var19 + "=\'\' or " + var19 + "=\' \'";
                                            } else {
                                                var13 = var13 + " or " + var19 + " is null or " + var19 + "=\'\' or " + var19 + "=\' \'";
                                            }
                                        } else {
                                            var19 = " " + var17 + "." + var4.getString(1) + " ";
                                            if(var13.equals("")) {
                                                if(var18.toLowerCase().indexOf("clob") > -1) {
                                                    var13 = var19 + " is null or dbms_lob.getlength(" + var19 + ") = 0";
                                                } else {
                                                    var13 = var19 + " is null or " + var19 + "=\'\' or " + var19 + "=\' \'";
                                                }
                                            } else if(var18.toLowerCase().indexOf("clob") > -1) {
                                                var13 = var13 + " or (" + var19 + " is null or dbms_lob.getlength(" + var19 + ") = 0)";
                                            } else {
                                                var13 = var13 + " or " + var19 + " is null or " + var19 + "=\'\' or " + var19 + "=\' \'";
                                            }
                                        }
                                    } else if(var13.equals("")) {
                                        var13 = var4.getString(1) + " is null";
                                    } else {
                                        var13 = var13 + " or " + var4.getString(1) + " is null";
                                    }
                                }

                                if(!var13.equals("")) {
                                    var3 = "select " + var15 + ".requestid from " + var15 + "," + var17 + " where " + var15 + ".id=" + var17 + "." + var16 + " and " + var15 + ".requestid=" + var1 + " and (" + var13 + ")";
                                    var4.executeSql(var3);
                                    if(var4.next()) {
                                        return true;
                                    }
                                }
                                break;
                            }
                        }

                        if(var5.getCounts() < 1) {
                            var3 = "select detailtablename from workflow_bill where id=" + var12;
                            var4.executeSql(var3);
                            if(var4.next()) {
                                var17 = var4.getString(1);
                                var13 = "";
                                var3 = "select ff.fieldname,ff.fielddbtype from " + var14 + " nf , workflow_billfield ff where ff.viewtype=1 and nf.fieldid=ff.id and ff.billid=" + var12 + " and ff.detailtable=\'" + var17 + "\' and nf.nodeid=" + var6 + " and nf.ismandatory=1 and nf.fieldid>0 order by ff.detailtable ";
                                var4.executeSql(var3);

                                while(true) {
                                    while(var4.next()) {
                                        var18 = Util.null2String(var4.getString(2));
                                        if(var18.toLowerCase().indexOf("int") <= -1 && var18.toLowerCase().indexOf("float") <= -1 && var18.toLowerCase().indexOf("number") <= -1) {
                                            if(var4.getDBType().equals("sqlserver")) {
                                                var19 = " convert(varchar," + var17 + "." + var4.getString(1) + ") ";
                                                if(var13.equals("")) {
                                                    var13 = var19 + " is null or " + var19 + "=\'\' or " + var19 + "=\' \'";
                                                } else {
                                                    var13 = var13 + " or " + var19 + " is null or " + var19 + "=\'\' or " + var19 + "=\' \'";
                                                }
                                            } else {
                                                var19 = " " + var17 + "." + var4.getString(1) + " ";
                                                if(var13.equals("")) {
                                                    if(var18.toLowerCase().indexOf("clob") > -1) {
                                                        var13 = var19 + " is null or dbms_lob.getlength(" + var19 + ") = 0";
                                                    } else {
                                                        var13 = var19 + " is null or " + var19 + "=\'\' or " + var19 + "=\' \'";
                                                    }
                                                } else if(var18.toLowerCase().indexOf("clob") > -1) {
                                                    var13 = var13 + " or (" + var19 + " is null or dbms_lob.getlength(" + var19 + ") = 0)";
                                                } else {
                                                    var13 = var13 + " or " + var19 + " is null or " + var19 + "=\'\' or " + var19 + "=\' \'";
                                                }
                                            }
                                        } else if(var13.equals("")) {
                                            var13 = var4.getString(1) + " is null";
                                        } else {
                                            var13 = var13 + " or " + var4.getString(1) + " is null";
                                        }
                                    }

                                    if(!var13.equals("")) {
                                        var3 = "select " + var15 + ".requestid from " + var15 + "," + var17 + " where " + var15 + ".id=" + var17 + "." + var16 + " and " + var15 + ".requestid=" + var1 + " and (" + var13 + ")";
                                        var4.executeSql(var3);
                                        if(var4.next()) {
                                            return true;
                                        }
                                    }

                                    return var2;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        return var2;
    }

    public String getCanMultiSubmitExt(String var1, String var2, String var3) {
        String var4 = "false";
        if(var2.equals("0")) {
            if("1".equals(this.wf.getMultiSubmit(var1))) {
                var4 = "true";
            }

            if(var4.equals("true") && this.haveMustInput(Util.getIntValue(var3))) {
                var4 = "false";
            }
        }

        return var4;
    }

    public String getWfOnlyNewLink(String var1, String var2) {
        return "<a href=javaScript:showModalDialog(\'/workflow/request/ViewRequest.jsp?requestid=" + var2 + "\',\'\',\'dialogTop:0;dialogWidth:\'+(window.screen.availWidth)+\'px;DialogHeight=\'+(window.screen.availHeight)+\'px\')>" + var1 + "</a>";
    }

    public String getWfOnlyViewLink(String var1, String var2) {
        return "<a href=javaScript:showModalDialog(\'/workflow/request/ViewRequest.jsp?isonlyview=1&requestid=" + var2 + "\',\'\',\'dialogTop:0;dialogWidth:\'+(window.screen.availWidth)+\'px;DialogHeight=\'+(window.screen.availHeight)+\'px\')>" + var1 + "</a>";
    }

    public String getWfNewLink(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        String var7 = Util.null2String(var4[2]);
        int var8 = Util.getIntValue(var4[3], 0);
        int var9 = Util.getIntValue(Util.null2String(var4[4]), 7);
        int var10 = 0;
        int var11 = 0;
        this.rs.execute("select formid,isbill from workflow_base where id=" + var6);
        if(this.rs.next()) {
            var11 = this.rs.getInt(1);
            var10 = this.rs.getInt(2);
        }

        MailAndMessage var12 = new MailAndMessage();
        String var13 = var12.getTitle(Util.getIntValue(var5, -1), Util.getIntValue(var6, -1), var11, var9, var10);
        if(!var13.equals("")) {
            var1 = var1 + "<B>（" + var13 + "）</B>";
        }

        boolean var14 = false;
        this.rs.executeSql("select isprocessed from workflow_currentoperator where ((isremark=\'0\' and (isprocessed=\'2\' or isprocessed=\'3\'))  or isremark=\'5\') and requestid = " + var5);
        if(this.rs.next()) {
            var14 = true;
        }

        if(var7.equals("0")) {
            if(var14) {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this);>" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><IMG src=\'/images/BDOut_wev8.gif\' align=absbottom></span>";
            } else {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this);>" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><IMG src=\'/images/BDNew_wev8.gif\' align=absbottom></span>";
            }
        } else if(var7.equals("-1")) {
            if(var14) {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this);>" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><IMG src=\'/images/BDOut_wev8.gif\' align=absbottom></span>";
            } else {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this);>" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><IMG src=\'/images/BDNew2_wev8.gif\' align=absbottom></spna>";
            }
        } else if(var14) {
            var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this);>" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><IMG src=\'/images/BDOut_wev8.gif\' align=absbottom></span>";
        } else {
            var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ")>" + var1 + "</a><span id=\'wflist_" + var5 + "span\'></span>";
        }

        return var3;
    }

    public String getWfNewLinkWithTitle(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        String var7 = Util.null2String(var4[2]);
        int var8 = Util.getIntValue(var4[3], 0);
        int var9 = Util.getIntValue(Util.null2String(var4[4]), 7);
        String var10 = Util.null2String(var4[5]);
        String var11 = Util.null2String(var4[6]);
        String var12 = Util.null2String(var4[7]);
        String var13 = "";
        String var14 = "";
        if(var4.length >= 10) {
            var14 = Util.null2String(var4[9]);
        }

        String var15 = "";
        int var16 = 0;
        int var17 = 0;
        this.rs.execute("select formid,isbill from workflow_base where id=" + var6);
        if(this.rs.next()) {
            var17 = this.rs.getInt(1);
            var16 = this.rs.getInt(2);
        }

        MailAndMessage var18 = new MailAndMessage();
        String var19 = var18.getTitle(Util.getIntValue(var5, -1), Util.getIntValue(var6, -1), var17, var9, var16);
        if(!var19.equals("")) {
            var1 = var1 + "<B>（" + var19 + "）</B>";
        }

        boolean var20 = false;
        boolean var21 = false;
        this.rs.executeSql("select isprocessed, isremark, userid, nodeid from workflow_currentoperator where requestid = " + var5 + " order by receivedate desc, receivetime desc");

        while(this.rs.next()) {
            String var22 = Util.null2String(this.rs.getString("isremark"));
            String var23 = Util.null2String(this.rs.getString("isprocessed"));
            String var24 = Util.null2String(this.rs.getString("userid"));
            if(var22.equals("0") && (var23.equals("2") || var23.equals("3")) || var22.equals("5")) {
                var20 = true;
            }

            if(("8".equals(var11) || "9".equals(var11) || "1".equals(var11)) && var12.equals(var24) && "0".equals(var22) && !var21) {
                int var25 = Util.getIntValue(this.rs.getString("nodeid"), 0);
                if(var25 != 0) {
                    var11 = var22;
                    var10 = "" + var25;
                    var21 = true;
                }
            }

            if(var20 && var21) {
                break;
            }
        }

        if("0".equals(var11)) {
            this.rs.executeSql("select nodetitle from workflow_flownode where workflowid=" + var6 + " and nodeid=" + var10);
            if(this.rs.next()) {
                var15 = Util.null2String(this.rs.getString("nodetitle"));
            }
        }

        if(!"".equals(var15) && !"null".equalsIgnoreCase(var15)) {
            var15 = "（" + var15 + "）";
            var1 = var15 + var1;
        }

        if(var7.equals("0")) {
            if(var20) {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var9) + "\'/></span>";
            } else if("1".equals(var14)) {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ") >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var9) + "\'/></span>";
            } else {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var9) + "\'/></span>";
            }
        } else if(var7.equals("-1")) {
            if(var20) {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var9) + "\'/></span>";
            } else {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDNew2_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(20288, var9) + "\'/></span>";
            }
        } else if(var20) {
            var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var9) + "\'/></span>";
        } else {
            var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ") >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'></span>";
        }

        return var3;
    }

    public String getWfNewLinkWithTitle2(String var1, String var2) throws Exception {
        return this.getWfNewLinkWithTitle2(var1, var2, 2);
    }

    public String getWfNewLinkWithTitle2(String var1, String var2, int var3) throws Exception {
        DepartmentComInfo var4 = new DepartmentComInfo();
        JobTitlesComInfo var5 = new JobTitlesComInfo();
        String var6 = "";
        String[] var7 = Util.TokenizerString2(var2, "+");
        String var8 = Util.null2String(var7[0]);
        String var9 = Util.null2String(var7[1]);
        String var10 = Util.null2String(var7[2]);
        int var11 = Util.getIntValue(var7[3], 0);
        int var12 = Util.getIntValue(Util.null2String(var7[4]), 7);
        String var13 = Util.null2String(var7[5]);
        String var14 = Util.null2String(var7[6]);
        String var15 = Util.null2String(var7[7]);
        String var16 = Util.null2String(var7[11]);
        this.rc.getResourcename(var16);
        String var18 = this.rc.getDepartmentID(var16);
        String var19 = var4.getDepartmentname(var18);
        String var20 = var5.getJobTitlesname(this.rc.getJobTitle(var16));
        String var21 = "";
        String var22 = "";
        if(var7.length >= 10) {
            var22 = Util.null2String(var7[9]);
        }

        String var23 = "";
        int var24 = 0;
        int var25 = 0;
        this.rs.execute("select formid,isbill from workflow_base where id=" + var9);
        if(this.rs.next()) {
            var25 = this.rs.getInt(1);
            var24 = this.rs.getInt(2);
        }

        MailAndMessage var26 = new MailAndMessage();
        String var27 = var26.getTitle(Util.getIntValue(var8, -1), Util.getIntValue(var9, -1), var25, var12, var24);
        if(!var27.equals("")) {
            var1 = var1 + "<B>（" + var27 + "）</B>";
        }

        boolean var28 = false;
        boolean var29 = false;
        this.rs.executeSql("select isprocessed, isremark, userid, nodeid from workflow_currentoperator where requestid = " + var8 + " order by receivedate desc, receivetime desc");

        while(this.rs.next()) {
            String var30 = Util.null2String(this.rs.getString("isremark"));
            String var31 = Util.null2String(this.rs.getString("isprocessed"));
            String var32 = Util.null2String(this.rs.getString("userid"));
            if(var30.equals("0") && (var31.equals("2") || var31.equals("3")) || var30.equals("5")) {
                var28 = true;
            }

            if(("8".equals(var14) || "9".equals(var14) || "1".equals(var14)) && var15.equals(var32) && "0".equals(var30) && !var29) {
                int var33 = Util.getIntValue(this.rs.getString("nodeid"), 0);
                if(var33 != 0) {
                    var14 = var30;
                    var13 = "" + var33;
                    var29 = true;
                }
            }

            if(var28 && var29) {
                break;
            }
        }

        if("0".equals(var14)) {
            this.rs.executeSql("select nodetitle from workflow_flownode where workflowid=" + var9 + " and nodeid=" + var13);
            if(this.rs.next()) {
                var23 = Util.null2String(this.rs.getString("nodetitle"));
            }
        }

        if(!"".equals(var23) && !"null".equalsIgnoreCase(var23)) {
            var23 = "（" + var23 + "）";
            var1 = var23 + var1;
        }

        if(var3 == 2) {
            if(!var15.equals(var16)) {
                this.rs.executeSql("select * from workflow_currentoperator where userid=" + var16 + " and workflowid=" + var9 + " and nodeid=" + var13 + " and requestid=" + var8);
                if(this.rs.next()) {
                    if(var10.equals("0")) {
                        if(var28) {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                        } else if("1".equals(var22)) {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
                        } else {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
                        }
                    } else if(var10.equals("-1")) {
                        if(var28) {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                        } else {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew2_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(20288, var12) + "\'/></span>";
                        }
                    } else if(var28) {
                        var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                    } else {
                        var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'></span>";
                    }
                }
            } else if(var10.equals("0")) {
                if(var28) {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                } else {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
                }
            } else if(var10.equals("-1")) {
                if(var28) {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                } else {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew2_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(20288, var12) + "\'/></span>";
                }
            } else if(var28) {
                var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
            } else {
                var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'></span>";
            }
        }

        if(var3 == 1) {
            if(!var15.equals(var16)) {
                this.rs.executeSql("select * from workflow_currentoperator where userid=" + var16 + " and workflowid=" + var9 + " and nodeid=" + var13 + " and requestid=" + var8);
                if(this.rs.next()) {
                    if(var10.equals("0")) {
                        if(var28) {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this);  target=\'_self\'>" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                        } else if("1".equals(var22)) {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ")  target=\'_self\'>" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
                        } else {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this);  target=\'_self\'>" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
                        }
                    } else if(var10.equals("-1")) {
                        if(var28) {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); target=\'_self\'>" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                        } else {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); target=\'_self\'>" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew2_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(20288, var12) + "\'/></span>";
                        }
                    } else if(var28) {
                        var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); target=\'_self\'>" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                    } else {
                        var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var16 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") target=\'_self\'>" + var1 + "</a><span id=\'wflist_" + var8 + "span\'></span>";
                    }
                }
            } else if(var10.equals("0")) {
                if(var28) {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); target=\'_self\'>" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                } else {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); target=\'_self\'>" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
                }
            } else if(var10.equals("-1")) {
                if(var28) {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); target=\'_self\' >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                } else {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); target=\'_self\'>" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew2_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(20288, var12) + "\'/></span>";
                }
            } else if(var28) {
                var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); target=\'_self\'>" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
            } else {
                var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var19 + "/" + var20 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") target=\'_self\'>" + var1 + "</a><span id=\'wflist_" + var8 + "span\'></span>";
            }
        }

        return var6;
    }

    public String getWfShareLinkWithTitle(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        String var7 = Util.null2String(var4[2]);
        int var8 = Util.getIntValue(var4[3], 0);
        int var9 = Util.getIntValue(Util.null2String(var4[4]), 7);
        String var10 = Util.null2String(var4[5]);
        String var11 = Util.null2String(var4[6]);
        String var12 = Util.null2String(var4[7]);
        String var13 = "";
        String var14 = "";
        String var15 = "";
        if(var4.length >= 12) {
            var14 = Util.null2String(var4[9]);
            var15 = Util.null2String(var4[11]);
        }

        String var16 = "";
        int var17 = 0;
        int var18 = 0;
        this.rs.execute("select formid,isbill from workflow_base where id=" + var6);
        if(this.rs.next()) {
            var18 = this.rs.getInt(1);
            var17 = this.rs.getInt(2);
        }

        MailAndMessage var19 = new MailAndMessage();
        String var20 = var19.getTitle(Util.getIntValue(var5, -1), Util.getIntValue(var6, -1), var18, var9, var17);
        if(!var20.equals("")) {
            var1 = var1 + "<B>（" + var20 + "）</B>";
        }

        boolean var21 = false;
        boolean var22 = false;
        this.rs.executeSql("select isprocessed, isremark, userid, nodeid from workflow_currentoperator where requestid = " + var5 + " order by receivedate desc, receivetime desc");

        String var23;
        String var25;
        while(this.rs.next()) {
            var23 = Util.null2String(this.rs.getString("isremark"));
            String var24 = Util.null2String(this.rs.getString("isprocessed"));
            var25 = Util.null2String(this.rs.getString("userid"));
            if(var23.equals("0") && (var24.equals("2") || var24.equals("3")) || var23.equals("5")) {
                var21 = true;
            }

            if(("8".equals(var11) || "9".equals(var11) || "1".equals(var11)) && var12.equals(var25) && "0".equals(var23) && !var22) {
                int var26 = Util.getIntValue(this.rs.getString("nodeid"), 0);
                if(var26 != 0) {
                    var11 = var23;
                    var10 = "" + var26;
                    var22 = true;
                }
            }

            if(var21 && var22) {
                break;
            }
        }

        if("0".equals(var11)) {
            this.rs.executeSql("select nodetitle from workflow_flownode where workflowid=" + var6 + " and nodeid=" + var10);
            if(this.rs.next()) {
                var16 = Util.null2String(this.rs.getString("nodetitle"));
            }
        }

        if(!"".equals(var16) && !"null".equalsIgnoreCase(var16)) {
            var16 = "（" + var16 + "）";
            var1 = var16 + var1;
        }

        var23 = "";
        if(!"".equals(var15)) {
            this.rs.executeSql("select requestid from workflow_currentoperator where id in(" + var15 + ")");

            while(this.rs.next()) {
                if("".equals(var23)) {
                    var23 = Util.null2String(this.rs.getString("requestid"));
                } else {
                    var23 = var23 + "," + Util.null2String(this.rs.getString("requestid"));
                }
            }
        }

        boolean var27 = false;
        if(!"".equals(var23)) {
            var25 = "," + var23 + ",";
            if(var25.indexOf(var5) > -1) {
                var27 = true;
            }
        }

        if(var27) {
            var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ") >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'></span>";
        } else if(var7.equals("0")) {
            if(var21) {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var9) + "\'/></span>";
            } else if("1".equals(var14)) {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ") >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var9) + "\'/></span>";
            } else {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var9) + "\'/></span>";
            }
        } else if(var7.equals("-1")) {
            if(var21) {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var9) + "\'/></span>";
            } else {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDNew2_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(20288, var9) + "\'/></span>";
            }
        } else if(var21) {
            var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ");doReadIt(" + var5 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var9) + "\'/></span>";
        } else {
            var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ") >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'></span>";
        }

        return var3;
    }

    public String getWfShareLinkWithTitle2(String var1, String var2) throws Exception {
        String var3 = "";
        ResourceComInfo var4 = new ResourceComInfo();
        DepartmentComInfo var5 = new DepartmentComInfo();
        JobTitlesComInfo var6 = new JobTitlesComInfo();
        String[] var7 = Util.TokenizerString2(var2, "+");
        String var8 = Util.null2String(var7[0]);
        String var9 = Util.null2String(var7[1]);
        String var10 = Util.null2String(var7[2]);
        int var11 = Util.getIntValue(var7[3], 0);
        int var12 = Util.getIntValue(Util.null2String(var7[4]), 7);
        String var13 = Util.null2String(var7[5]);
        String var14 = Util.null2String(var7[6]);
        String var15 = Util.null2String(var7[7]);
        String var16 = "";
        String var17 = "";
        String var18 = "";
        String var19 = "";
        if(var7.length >= 12) {
            var17 = Util.null2String(var7[9]);
            var18 = Util.null2String(var7[11]);
            var19 = Util.null2String(var7[12]);
        }

        var4.getResourcename(var19);
        String var21 = var4.getDepartmentID(var19);
        String var22 = var5.getDepartmentname(var21);
        String var23 = var6.getJobTitlesname(var4.getJobTitle(var19));
        String var24 = "";
        int var25 = 0;
        int var26 = 0;
        this.rs.execute("select formid,isbill from workflow_base where id=" + var9);
        if(this.rs.next()) {
            var26 = this.rs.getInt(1);
            var25 = this.rs.getInt(2);
        }

        MailAndMessage var27 = new MailAndMessage();
        String var28 = var27.getTitle(Util.getIntValue(var8, -1), Util.getIntValue(var9, -1), var26, var12, var25);
        if(!var28.equals("")) {
            var1 = var1 + "<B>（" + var28 + "）</B>";
        }

        boolean var29 = false;
        boolean var30 = false;
        this.rs.executeSql("select isprocessed, isremark, userid, nodeid from workflow_currentoperator where requestid = " + var8 + " order by receivedate desc, receivetime desc");

        String var31;
        String var33;
        while(this.rs.next()) {
            var31 = Util.null2String(this.rs.getString("isremark"));
            String var32 = Util.null2String(this.rs.getString("isprocessed"));
            var33 = Util.null2String(this.rs.getString("userid"));
            if(var31.equals("0") && (var32.equals("2") || var32.equals("3")) || var31.equals("5")) {
                var29 = true;
            }

            if(("8".equals(var14) || "9".equals(var14) || "1".equals(var14)) && var15.equals(var33) && "0".equals(var31) && !var30) {
                int var34 = Util.getIntValue(this.rs.getString("nodeid"), 0);
                if(var34 != 0) {
                    var14 = var31;
                    var13 = "" + var34;
                    var30 = true;
                }
            }

            if(var29 && var30) {
                break;
            }
        }

        if("0".equals(var14)) {
            this.rs.executeSql("select nodetitle from workflow_flownode where workflowid=" + var9 + " and nodeid=" + var13);
            if(this.rs.next()) {
                var24 = Util.null2String(this.rs.getString("nodetitle"));
            }
        }

        if(!"".equals(var24) && !"null".equalsIgnoreCase(var24)) {
            var24 = "（" + var24 + "）";
            var1 = var24 + var1;
        }

        var31 = "";
        if(!"".equals(var18)) {
            this.rs.executeSql("select requestid from workflow_currentoperator where id in(" + var18 + ")");

            while(this.rs.next()) {
                if("".equals(var31)) {
                    var31 = Util.null2String(this.rs.getString("requestid"));
                } else {
                    var31 = var31 + "," + Util.null2String(this.rs.getString("requestid"));
                }
            }
        }

        boolean var35 = false;
        if(!"".equals(var31)) {
            var33 = "," + var31 + ",";
            if(var33.indexOf(var8) > -1) {
                var35 = true;
            }
        }

        if(!var15.equals(var19)) {
            if(var35) {
                var3 = "<span></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var19 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'></span>";
            } else if(var10.equals("0")) {
                if(var29) {
                    var3 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var19 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                } else {
                    var3 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var19 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
                }
            } else if(var10.equals("-1")) {
                if(var29) {
                    var3 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var19 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                } else {
                    var3 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var19 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew2_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(20288, var12) + "\'/></span>";
                }
            } else if(var29) {
                var3 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var19 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
            } else {
                var3 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var19 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'></span>";
            }
        } else if(var35) {
            var3 = "<span></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&isovertime=" + var11 + "\'," + var8 + ") >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'></span>";
        } else if(var10.equals("0")) {
            if(var29) {
                var3 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
            } else if("1".equals(var17)) {
                var3 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
            } else {
                var3 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
            }
        } else if(var10.equals("-1")) {
            if(var29) {
                var3 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
            } else {
                var3 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew2_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(20288, var12) + "\'/></span>";
            }
        } else if(var29) {
            var3 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
        } else {
            var3 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var22 + "/" + var23 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var15 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'></span>";
        }

        return var3;
    }

    public String getWfMainSubPic(String var1, String var2) throws Exception {
        ResourceComInfo var3 = new ResourceComInfo();
        DepartmentComInfo var4 = new DepartmentComInfo();
        JobTitlesComInfo var5 = new JobTitlesComInfo();
        String var6 = "";
        String[] var7 = Util.TokenizerString2(var2, "+");
        String var8 = Util.null2String(var7[0]);
        String var9 = Util.null2String(var7[1]);
        String var10 = Util.null2String(var7[2]);
        int var11 = Util.getIntValue(var7[3], 0);
        int var12 = Util.getIntValue(Util.null2String(var7[4]), 7);
        String var13 = Util.null2String(var7[5]);
        String var14 = Util.null2String(var7[6]);
        String var15 = Util.null2String(var7[7]);
        String var16 = Util.null2String(var7[11]);
        String[] var17 = null;
        ArrayList var18 = new ArrayList();
        boolean var19 = false;
        int var20;
        if(!"".equals(var16)) {
            var17 = var16.split(",");

            for(var20 = 0; var20 < var17.length; ++var20) {
                int var39 = Util.getIntValue(var17[var20]);
                var18.add(var39 + "");
            }
        }

        for(var20 = 0; var20 < var18.size(); ++var20) {
            String var21 = Util.null2String((String)var18.get(var20));
            var3.getResourcename((String)var18.get(var20));
            String var23 = var3.getDepartmentID((String)var18.get(var20));
            String var24 = var4.getDepartmentname(var23);
            String var25 = var5.getJobTitlesname(var3.getJobTitle((String)var18.get(var20)));
            String var26 = "";
            String var27 = "";
            if(var7.length >= 10) {
                var27 = Util.null2String(var7[9]);
            }

            String var28 = "";
            int var29 = 0;
            int var30 = 0;
            this.rs.execute("select formid,isbill from workflow_base where id=" + var9);
            if(this.rs.next()) {
                var30 = this.rs.getInt(1);
                var29 = this.rs.getInt(2);
            }

            MailAndMessage var31 = new MailAndMessage();
            String var32 = var31.getTitle(Util.getIntValue(var8, -1), Util.getIntValue(var9, -1), var30, var12, var29);
            if(!var32.equals("")) {
                var1 = var1 + "<B>（" + var32 + "）</B>";
            }

            boolean var33 = false;
            boolean var34 = false;
            this.rs.executeSql("select isprocessed, isremark, userid, nodeid from workflow_currentoperator where requestid = " + var8 + " order by receivedate desc, receivetime desc");

            while(this.rs.next()) {
                String var35 = Util.null2String(this.rs.getString("isremark"));
                String var36 = Util.null2String(this.rs.getString("isprocessed"));
                String var37 = Util.null2String(this.rs.getString("userid"));
                if(var35.equals("0") && (var36.equals("2") || var36.equals("3")) || var35.equals("5")) {
                    var33 = true;
                }

                if(("8".equals(var14) || "9".equals(var14) || "1".equals(var14)) && var15.equals(var37) && "0".equals(var35) && !var34) {
                    int var38 = Util.getIntValue(this.rs.getString("nodeid"), 0);
                    if(var38 != 0) {
                        var14 = var35;
                        var13 = "" + var38;
                        var34 = true;
                    }
                }

                if(var33 && var34) {
                    break;
                }
            }

            if("0".equals(var14)) {
                this.rs.executeSql("select nodetitle from workflow_flownode where workflowid=" + var9 + " and nodeid=" + var13);
                if(this.rs.next()) {
                    var28 = Util.null2String(this.rs.getString("nodetitle"));
                }
            }

            if(!"".equals(var28) && !"null".equalsIgnoreCase(var28)) {
                var28 = "（" + var28 + "）";
                var1 = var28 + var1;
            }

            if(!var15.equals(var21)) {
                this.rs.executeSql("select * from workflow_currentoperator where userid=" + var21 + " and workflowid=" + var9 + " and nodeid=" + var13 + " and requestid=" + var8);
                if(this.rs.next()) {
                    if(var10.equals("0")) {
                        if(var33) {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                        } else if("1".equals(var27)) {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
                        } else {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
                        }
                    } else if(var10.equals("-1")) {
                        if(var33) {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                        } else {
                            var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew2_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(20288, var12) + "\'/></span>";
                        }
                    } else if(var33) {
                        var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                    } else {
                        var6 = "<span><img src=\'/images/ecology8/subwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'></span>";
                    }
                }
            } else if(var10.equals("0")) {
                if(var33) {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                } else if("1".equals(var27)) {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
                } else {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var12) + "\'/></span>";
                }
            } else if(var10.equals("-1")) {
                if(var33) {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
                } else {
                    var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDNew2_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(20288, var12) + "\'/></span>";
                }
            } else if(var33) {
                var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ");doReadIt(" + var8 + ",\"\",this); >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'><img src=\'/images/ecology8/statusicon/BDOut_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19081, var12) + "\'/></span>";
            } else {
                var6 = "<span><img src=\'/images/ecology8/mainwf_wev8.png\' title=\'" + var24 + "/" + var25 + "\' /></span><a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var8 + "&f_weaver_belongto_userid=" + var21 + "&f_weaver_belongto_usertype=0&isovertime=" + var11 + "\'," + var8 + ") >" + var1 + "</a><span id=\'wflist_" + var8 + "span\'></span>";
            }
        }

        return var6;
    }

    public String getContentNewLinkWithTitle(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[1]);
        String var6 = Util.null2String(var4[2]);
        int var7 = Util.getIntValue(var4[3], 0);
        int var8 = Util.getIntValue(Util.null2String(var4[4]), 7);
        String var9 = Util.null2String(var4[5]);
        String var10 = Util.null2String(var4[6]);
        String var11 = Util.null2String(var4[7]);
        String var12 = "";
        this.rs.executeSql("select docids from workflow_requestbase where requestid=" + var1);
        if(this.rs.next()) {
            var12 = Util.null2String(this.rs.getString(1));
        }

        if(!var12.equals("") && !var12.equals("0")) {
            if(var12.indexOf(",") == -1) {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?viewdoc=1&isworkflowdoc=1&seeflowdoc=1&requestid=" + var1 + "&isovertime=" + var7 + "\'," + var1 + ") >" + this.DocComInfo1.getDocname(var12) + "</a>";
            } else {
                this.rs.executeSql("select flowDocField from workflow_createdoc where workflowid=" + var5 + " order by id desc");
                if(this.rs.next()) {
                    int var13 = Util.getIntValue(this.rs.getString(1), -1);
                    if(var13 > 0) {
                        int var14 = 0;
                        int var15 = 0;
                        this.rs.execute("select formid,isbill from workflow_base where id=" + var5);
                        if(this.rs.next()) {
                            var15 = this.rs.getInt(1);
                            var14 = this.rs.getInt(2);
                        }

                        String var16;
                        if(var14 == 1) {
                            this.rs.executeSql("select fieldname from workflow_billfield where id=" + var13 + " and billid=" + var15);
                            if(this.rs.next()) {
                                var16 = Util.null2String(this.rs.getString(1));
                                if(!var16.equals("")) {
                                    if(var15 < 0) {
                                        this.rs.executeSql("select " + var16 + " from formtable_main_" + var15 * -1 + " where requestid=" + var1);
                                        if(this.rs.next()) {
                                            var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?viewdoc=1&isworkflowdoc=1&seeflowdoc=1&requestid=" + var1 + "&isovertime=" + var7 + "\'," + var1 + ") >" + this.DocComInfo1.getDocname("" + Util.getIntValue(this.rs.getString(1), 0)) + "</a>";
                                        }
                                    } else {
                                        this.rs.executeSql("select tablename from workflow_bill where id=" + var15);
                                        if(this.rs.next()) {
                                            String var17 = Util.null2String(this.rs.getString(1));
                                            if(!var17.equals("")) {
                                                this.rs.executeSql("select " + var16 + " from " + var17 + " where requestid=" + var1);
                                                if(this.rs.next()) {
                                                    var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?viewdoc=1&isworkflowdoc=1&seeflowdoc=1&requestid=" + var1 + "&isovertime=" + var7 + "\'," + var1 + ") >" + this.DocComInfo1.getDocname("" + Util.getIntValue(this.rs.getString(1), 0)) + "</a>";
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            this.rs.executeSql("select fieldname from workflow_formdict where id=" + var13);
                            if(this.rs.next()) {
                                var16 = Util.null2String(this.rs.getString(1));
                                if(!var16.equals("")) {
                                    this.rs.executeSql("select " + var16 + " from workflow_form where requestid=" + var1);
                                    if(this.rs.next()) {
                                        var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?viewdoc=1&isworkflowdoc=1&seeflowdoc=1&requestid=" + var1 + "&isovertime=" + var7 + "\'," + var1 + ") >" + this.DocComInfo1.getDocname("" + Util.getIntValue(this.rs.getString(1), 0)) + "</a>";
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return var3;
    }

    public String getWfNewLinkWithTitleExt(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        String var7 = Util.null2String(var4[2]);
        int var8 = Util.getIntValue(var4[3], 0);
        int var9 = Util.getIntValue(Util.null2String(var4[4]), 7);
        String var10 = Util.null2String(var4[5]);
        String var11 = Util.null2String(var4[6]);
        String var12 = Util.null2String(var4[7]);
        String var13 = "";
        int var14 = 0;
        int var15 = 0;
        this.rs.execute("select formid,isbill from workflow_base where id=" + var6);
        if(this.rs.next()) {
            var15 = this.rs.getInt(1);
            var14 = this.rs.getInt(2);
        }

        MailAndMessage var16 = new MailAndMessage();
        String var17 = var16.getTitle(Util.getIntValue(var5, -1), Util.getIntValue(var6, -1), var15, var9, var14);
        if(!var17.equals("")) {
            var1 = var1 + "<B>（" + var17 + "）</B>";
        }

        boolean var18 = false;
        boolean var19 = false;
        this.rs.executeSql("select isprocessed, isremark, userid, nodeid from workflow_currentoperator where requestid = " + var5 + " order by receivedate desc, receivetime desc");

        while(this.rs.next()) {
            String var20 = Util.null2String(this.rs.getString("isremark"));
            String var21 = Util.null2String(this.rs.getString("isprocessed"));
            String var22 = Util.null2String(this.rs.getString("userid"));
            if(var20.equals("0") && (var21.equals("2") || var21.equals("3")) || var20.equals("5")) {
                var18 = true;
            }

            if(("8".equals(var11) || "9".equals(var11) || "1".equals(var11)) && var12.equals(var22) && "0".equals(var20) && !var19) {
                int var23 = Util.getIntValue(this.rs.getString("nodeid"), 0);
                if(var23 != 0) {
                    var11 = var20;
                    var10 = "" + var23;
                    var19 = true;
                }
            }

            if(var18 && var19) {
                break;
            }
        }

        if("0".equals(var11)) {
            this.rs.executeSql("select nodetitle from workflow_flownode where workflowid=" + var6 + " and nodeid=" + var10);
            if(this.rs.next()) {
                var13 = Util.null2String(this.rs.getString("nodetitle"));
            }
        }

        if(!"".equals(var13) && !"null".equalsIgnoreCase(var13)) {
            var13 = "（" + var13 + "）";
            var1 = var13 + var1;
        }

        if(var7.equals("0")) {
            var3 = "<a href=javaScript:openWfToTab(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\',\'" + Util.toHtmlForCpt(var1) + "\')  >" + var1 + "</a>";
        } else if(var7.equals("-1")) {
            var3 = "<a href=javaScript:openWfToTab(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\',\'" + Util.toHtmlForCpt(var1) + "\')  >" + var1 + "</a>";
        } else {
            var3 = "<a href=javaScript:openWfToTab(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\',\'" + Util.toHtmlForCpt(var1) + "\')  >" + var1 + "</a>";
        }

        return var3;
    }

    public String getWfViewTypeExt(String var1, String var2) {
        boolean var3 = false;
        this.rs.executeSql("select isprocessed from workflow_currentoperator where ((isremark=\'0\' and (isprocessed=\'2\' or isprocessed=\'3\'))  or isremark=\'5\') and requestid = " + var2);
        if(this.rs.next()) {
            var3 = true;
        }

        String var4 = "";
        if(var1.equals("0")) {
            if(var3) {
                var4 = "<IMG src=\'/images/BDOut_wev8.gif\' align=absbottom>";
            } else {
                var4 = "<IMG src=\'/images/BDNew_wev8.gif\' align=absbottom>";
            }
        } else if(var1.equals("-1")) {
            if(var3) {
                var4 = "<IMG src=\'/images/BDOut_wev8.gif\' align=absbottom>";
            } else {
                var4 = "<IMG src=\'/images/BDNew2_wev8.gif\' align=absbottom>";
            }
        } else if(var3) {
            var4 = "<IMG src=\'/images/BDOut_wev8.gif\' align=absbottom>";
        } else {
            var4 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp";
        }

        return var4;
    }

    public String getWfViewTypeExtIncludeAgent(String var1, String var2) {
        String[] var3 = Util.TokenizerString2(var2, "+");
        String var4 = Util.null2String(var3[0]);
        String var5 = Util.null2String(var3[1]);
        String var6 = Util.null2String(var3[2]);
        boolean var7 = false;
        this.rs.executeSql("select isprocessed from workflow_currentoperator where ((isremark=\'0\' and (isprocessed=\'2\' or isprocessed=\'3\'))  or isremark=\'5\') and requestid = " + var4);
        if(this.rs.next()) {
            var7 = true;
        }

        String var8 = "";
        if(var1.equals("0")) {
            if(var7) {
                var8 = "<IMG src=\'/images/BDOut_wev8.gif\' align=absbottom>";
            } else if("1".equals(var6)) {
                var8 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp";
            } else {
                var8 = "<IMG src=\'/images/BDNew_wev8.gif\' align=absbottom>";
            }
        } else if(var1.equals("-1")) {
            if(var7) {
                var8 = "<IMG src=\'/images/BDOut_wev8.gif\' align=absbottom>";
            } else {
                var8 = "<IMG src=\'/images/BDNew2_wev8.gif\' align=absbottom>";
            }
        } else if(var7) {
            var8 = "<IMG src=\'/images/BDOut_wev8.gif\' align=absbottom>";
        } else {
            var8 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp";
        }

        return var8;
    }

    public String getUnOperators(String var1, String var2) {
        String[] var3 = Util.TokenizerString2(var2, "+");
        String var4 = Util.null2String(var3[0]);
        String var5 = Util.null2String(var3[1]);
        String var6 = Util.null2String(var3[2]);
        String var7 = "<div id=\'" + var6 + var1 + "div\'>";
        String var8 = var6 + var1 + "div";
        var7 = var7 + "<span style=\'cursor:hand;text-decoration: underline\' onClick=showallreceived(\'" + var1 + "\',\'" + var8 + "\') >" + SystemEnv.getHtmlLabelName(89, Util.getIntValue(var4)) + "</span>";
        var7 = var7 + "</div>";
        String var9 = "";

        try {
            var9 = this.RequestDefaultComInfo.getShowoperator("" + var5);
        } catch (Exception var11) {
            ;
        }

        if(var9.equals("1")) {
            var7 = "";
            this.rs.executeSql("select distinct userid,usertype,agenttype,agentorbyagentid from workflow_currentoperator where (isremark in (\'0\',\'1\',\'5\',\'7\',\'8\',\'9\') or (isremark=\'4\' and viewtype=0))  and requestid = " + var1);

            while(this.rs.next()) {
                if(var7.equals("")) {
                    if(this.rs.getInt("usertype") == 0) {
                        if(this.rs.getInt("agenttype") == 2) {
                            var7 = var7 + this.rc.getResourcename(this.rs.getString("agentorbyagentid")) + "->" + this.rc.getResourcename(this.rs.getString("userid"));
                        } else {
                            var7 = var7 + this.rc.getResourcename(this.rs.getString("userid"));
                        }
                    } else {
                        var7 = var7 + this.cci.getCustomerInfoname(this.rs.getString("userid"));
                    }
                } else if(this.rs.getInt("usertype") == 0) {
                    if(this.rs.getInt("agenttype") == 2) {
                        var7 = var7 + "," + this.rc.getResourcename(this.rs.getString("agentorbyagentid")) + "->" + this.rc.getResourcename(this.rs.getString("userid"));
                    } else {
                        var7 = var7 + "," + this.rc.getResourcename(this.rs.getString("userid"));
                    }
                } else {
                    var7 = var7 + "," + this.cci.getCustomerInfoname(this.rs.getString("userid"));
                }
            }
        }

        return var7;
    }

    public String getUnOperators2(String var1, String var2) {
        String[] var3 = Util.TokenizerString2(var2, "+");
        String var4 = Util.null2String(var3[0]);
        String var5 = Util.null2String(var3[1]);
        String var6 = "<div id=\'" + var5 + var1 + "div\'>";
        String var7 = var5 + var1 + "div";
        var6 = var6 + "<span style=\'cursor:hand;text-decoration: underline\' onClick=showallreceived(\'" + var1 + "\',\'" + var7 + "\') >" + SystemEnv.getHtmlLabelName(89, Util.getIntValue(var4)) + "</span>";
        var6 = var6 + "</div>";
        String var8 = "";

        try {
            var8 = this.RequestDefaultComInfo.getShowoperator("" + var5);
        } catch (Exception var10) {
            ;
        }

        if(var8.equals("1")) {
            var6 = "";
            this.rs.executeSql("select distinct userid,usertype,agenttype,agentorbyagentid from workflow_currentoperator where (isremark in (\'0\',\'1\',\'5\',\'7\',\'8\',\'9\') or (isremark=\'4\' and viewtype=0))  and requestid = " + var1);

            while(this.rs.next()) {
                if(var6.equals("")) {
                    if(this.rs.getInt("usertype") == 0) {
                        if(this.rs.getInt("agenttype") == 2) {
                            var6 = var6 + this.rc.getResourcename(this.rs.getString("agentorbyagentid")) + "->" + this.rc.getResourcename(this.rs.getString("userid"));
                        } else {
                            var6 = var6 + this.rc.getResourcename(this.rs.getString("userid"));
                        }
                    } else {
                        var6 = var6 + this.cci.getCustomerInfoname(this.rs.getString("userid"));
                    }
                } else if(this.rs.getInt("usertype") == 0) {
                    if(this.rs.getInt("agenttype") == 2) {
                        var6 = var6 + "," + this.rc.getResourcename(this.rs.getString("agentorbyagentid")) + "->" + this.rc.getResourcename(this.rs.getString("userid"));
                    } else {
                        var6 = var6 + "," + this.rc.getResourcename(this.rs.getString("userid"));
                    }
                } else {
                    var6 = var6 + "," + this.cci.getCustomerInfoname(this.rs.getString("userid"));
                }
            }
        }

        return var6;
    }

    public String getMUnOperators(String var1, String var2) {
        String[] var3 = Util.TokenizerString2(var2, "+");
        String var4 = Util.null2String(var3[0]);
        String var5 = Util.null2String(var3[1]);
        String var6 = "<div id=\'" + var1 + "div\'>";
        String var7 = var1 + "div";
        var6 = var6 + "<span style=\'cursor:hand;text-decoration: underline\' onClick=showallreceived(\'" + var1 + "\',\'" + var7 + "\') >" + SystemEnv.getHtmlLabelName(89, Util.getIntValue(var4)) + "</span>";
        var6 = var6 + "</div>";
        String var8 = "";

        try {
            var8 = this.RequestDefaultComInfo.getShowoperator("" + var5);
        } catch (Exception var10) {
            ;
        }

        if(var8.equals("1")) {
            var6 = "";
            this.rs.executeSql("select distinct userid,usertype,agenttype,agentorbyagentid from workflow_currentoperator where (isremark in (\'0\',\'1\',\'5\',\'7\',\'8\',\'9\') or (isremark=\'4\' and viewtype=0))  and requestid = " + var1);

            while(this.rs.next()) {
                if(var6.equals("")) {
                    if(this.rs.getInt("usertype") == 0) {
                        if(this.rs.getInt("agenttype") == 2) {
                            var6 = var6 + this.rc.getResourcename(this.rs.getString("agentorbyagentid")) + "->" + this.rc.getResourcename(this.rs.getString("userid"));
                        } else {
                            var6 = var6 + this.rc.getResourcename(this.rs.getString("userid"));
                        }
                    } else {
                        var6 = var6 + this.cci.getCustomerInfoname(this.rs.getString("userid"));
                    }
                } else if(this.rs.getInt("usertype") == 0) {
                    if(this.rs.getInt("agenttype") == 2) {
                        var6 = var6 + "," + this.rc.getResourcename(this.rs.getString("agentorbyagentid")) + "->" + this.rc.getResourcename(this.rs.getString("userid"));
                    } else {
                        var6 = var6 + "," + this.rc.getResourcename(this.rs.getString("userid"));
                    }
                } else {
                    var6 = var6 + "," + this.cci.getCustomerInfoname(this.rs.getString("userid"));
                }
            }
        }

        return var6;
    }

    public String getUnOptInRep(String var1) {
        String var2 = "";
        this.rs.executeSql("select distinct userid,usertype,agenttype,agentorbyagentid from workflow_currentoperator where (isremark in (\'0\',\'1\',\'5\',\'7\',\'8\',\'9\') or (isremark=\'4\' and viewtype=0))  and requestid = " + var1);
        String var3 = "";
        String var4 = "";

        while(this.rs.next()) {
            if(var2.equals("")) {
                if(this.rs.getInt("usertype") == 0) {
                    if(this.rs.getInt("agenttype") == 2) {
                        var3 = this.rs.getString("agentorbyagentid");
                        var4 = this.rc.getResourcename(var3);
                        var2 = var2 + "<a href=\"javaScript:openhrm(" + var3 + ");\" onclick=\'pointerXY(event);\'>" + var4 + "</a>->";
                        var2 = var2 + "<a href=\"javaScript:openhrm(" + this.rs.getString("userid") + ");\" onclick=\'pointerXY(event);\'>" + this.rc.getResourcename("userid") + "</a>";
                    } else {
                        var3 = this.rs.getString("userid");
                        var4 = this.rc.getResourcename(var3);
                        var2 = var2 + "<a href=\"javaScript:openhrm(" + var3 + ");\" onclick=\'pointerXY(event);\'>" + var4 + "</a>";
                    }
                } else {
                    var3 = this.rs.getString("userid");
                    var4 = this.cci.getCustomerInfoname(var3);
                    var2 = var2 + "<a href=\"javaScript:openhrm(" + var3 + ");\" onclick=\'pointerXY(event);\'>" + var4 + "</a>";
                }
            } else if(this.rs.getInt("usertype") == 0) {
                if(this.rs.getInt("agenttype") == 2) {
                    var3 = this.rs.getString("agentorbyagentid");
                    var4 = this.rc.getResourcename(var3);
                    var2 = var2 + ",<a href=\"javaScript:openhrm(" + var3 + ");\" onclick=\'pointerXY(event);\'>" + var4 + "</a>->";
                    var2 = var2 + "<a href=\"javaScript:openhrm(" + this.rs.getString("userid") + ");\" onclick=\'pointerXY(event);\'>" + this.rc.getResourcename("userid") + "</a>";
                } else {
                    var3 = this.rs.getString("userid");
                    var4 = this.rc.getResourcename(var3);
                    var2 = var2 + ",<a href=\"javaScript:openhrm(" + var3 + ");\" onclick=\'pointerXY(event);\'>" + var4 + "</a>";
                }
            } else {
                var3 = this.rs.getString("userid");
                var4 = this.cci.getCustomerInfoname(var3);
                var2 = var2 + ",<a href=\"javaScript:openhrm(" + var3 + ");\" onclick=\'pointerXY(event);\'>" + var4 + "</a>";
            }
        }

        return var2;
    }

    public String getUnOptOutPutExcel(String var1) {
        String var2 = "";
        this.rs.executeSql("select distinct userid,usertype,agenttype,agentorbyagentid from workflow_currentoperator where (isremark in (\'0\',\'1\',\'5\',\'7\',\'8\',\'9\') or (isremark=\'4\' and viewtype=0))  and requestid = " + var1);
        String var3 = "";
        String var4 = "";

        while(this.rs.next()) {
            if(var2.equals("")) {
                if(this.rs.getInt("usertype") == 0) {
                    if(this.rs.getInt("agenttype") == 2) {
                        var3 = this.rs.getString("agentorbyagentid");
                        var4 = this.rc.getResourcename(var3);
                        var2 = var2 + var4 + "->";
                        var2 = var2 + this.rc.getResourcename("userid");
                    } else {
                        var3 = this.rs.getString("userid");
                        var4 = this.rc.getResourcename(var3);
                        var2 = var2 + var4;
                    }
                } else {
                    var3 = this.rs.getString("userid");
                    var4 = this.cci.getCustomerInfoname(var3);
                    var2 = var2 + var4;
                }
            } else if(this.rs.getInt("usertype") == 0) {
                if(this.rs.getInt("agenttype") == 2) {
                    var3 = this.rs.getString("agentorbyagentid");
                    var4 = this.rc.getResourcename(var3);
                    var2 = var2 + "," + var4 + "->";
                    var2 = var2 + this.rc.getResourcename("userid");
                } else {
                    var3 = this.rs.getString("userid");
                    var4 = this.rc.getResourcename(var3);
                    var2 = var2 + "," + var4;
                }
            } else {
                var3 = this.rs.getString("userid");
                var4 = this.cci.getCustomerInfoname(var3);
                var2 = var2 + "," + var4;
            }
        }

        return var2;
    }

    public String getUnOperatorsExt(String var1, String var2) {
        String[] var3 = Util.TokenizerString2(var2, "+");
        String var4 = Util.null2String(var3[0]);
        String var5 = Util.null2String(var3[1]);
        String var6 = "<div id=\'" + var1 + "div\'>";
        String var7 = var1 + "div";
        var6 = var6 + "<span style=\'cursor:hand;color: blue; text-decoration: underline\' onClick=showallreceived(\'" + var1 + "\',\'" + var7 + "\',this) >" + SystemEnv.getHtmlLabelName(89, Util.getIntValue(var4)) + "</span>";
        var6 = var6 + "</div>";
        String var8 = "";

        try {
            var8 = this.RequestDefaultComInfo.getShowoperator("" + var5);
        } catch (Exception var10) {
            ;
        }

        if(var8.equals("1")) {
            var6 = "";
            this.rs.executeSql("select distinct userid,usertype,agenttype,agentorbyagentid from workflow_currentoperator where (isremark in (\'0\',\'1\') or (isremark=\'4\' and viewtype=0))  and requestid = " + var1);

            while(this.rs.next()) {
                if(var6.equals("")) {
                    if(this.rs.getInt("usertype") == 0) {
                        if(this.rs.getInt("agenttype") == 2) {
                            var6 = var6 + this.rc.getResourcename(this.rs.getString("agentorbyagentid")) + "->" + this.rc.getResourcename(this.rs.getString("userid"));
                        } else {
                            var6 = var6 + this.rc.getResourcename(this.rs.getString("userid"));
                        }
                    } else {
                        var6 = var6 + this.cci.getCustomerInfoname(this.rs.getString("userid"));
                    }
                } else if(this.rs.getInt("usertype") == 0) {
                    if(this.rs.getInt("agenttype") == 2) {
                        var6 = var6 + "," + this.rc.getResourcename(this.rs.getString("agentorbyagentid")) + "->" + this.rc.getResourcename(this.rs.getString("userid"));
                    } else {
                        var6 = var6 + "," + this.rc.getResourcename(this.rs.getString("userid"));
                    }
                } else {
                    var6 = var6 + "," + this.cci.getCustomerInfoname(this.rs.getString("userid"));
                }
            }
        }

        return var6;
    }

    public String getWfMonLink(String var1, String var2) {
        String var3 = "<a href=\'/workflow/request/ViewRequest.jsp?ismonitor=y&requestid=" + var2 + "\' target=\'_newworks\'>" + var1 + "</a>";
        return var3;
    }

    public String getFlowPendingLink(String var1, String var2) {
        String var3 = "<a href=\'/workflow/request/ViewRequest.jsp?isfromflowreport=1&reportid=-2&requestid=" + var2 + "\' target=\'_newworks\'>" + var1 + "</a>";
        return var3;
    }

    public String getFlowDocLink(String var1, String var2) {
        String var3 = "<a href=\'/workflow/request/ViewRequest.jsp?isfromflowreport=1&reportid=-6&requestid=" + var2 + "\' target=\'_newworks\'>" + var1 + "</a>";
        return var3;
    }

    public String getCurrentNode(String var1) {
        String var2 = "";
        this.rs.executeSql("select nodename from workflow_nodebase where id= " + var1);
        if(this.rs.next()) {
            var2 = this.rs.getString("nodename");
        }

        return var2;
    }

    /** @deprecated */
    @Deprecated
    public List<String> getWFMonitorListOperation(String var1, String var2) {
        String var3 = "false";
        String var4 = "false";
        String var5 = "false";
        String var6 = "false";
        String var7 = "false";
        String var8 = "false";
        String var9 = "false";
        ArrayList var10 = new ArrayList();
        String[] var11 = Util.TokenizerString2(var2, "+");
        String var12 = "";
        byte var13 = 0;
        boolean var14 = false;
        String var15 = Util.null2String(var11[0]);
        int var16 = Integer.parseInt(Util.null2String(var11[1]));
        int var17 = Integer.parseInt(Util.null2String(var11[2]));
        int var18 = Integer.parseInt(Util.null2String(var11[3]));
        int var19 = Util.getIntValue(Util.null2String(var11[4]), -1);
        int var20 = Util.getIntValue(Util.null2String(var11[5]), -1);
        int var21 = Util.getIntValue(Util.null2String(var11[6]), -1);
        boolean var22 = false;
        boolean var23 = false;
        boolean var24 = false;
        this.rs.executeSql("select isForceDrawBack,isForceOver,issooperator from workflow_monitor_bound where monitorhrmid=" + var16 + " and EXISTS(select 1 from workflow_requestbase where currentnodetype!=\'3\' and workflowid=workflow_monitor_bound.workflowid and requestid=" + var15 + ")");

        while(this.rs.next()) {
            if(!var22) {
                var22 = Util.getIntValue(this.rs.getString("isForceDrawBack")) == 1;
            }

            if(!var23) {
                var23 = Util.getIntValue(this.rs.getString("isForceOver")) == 1;
            }

            if(!var24) {
                var24 = Util.getIntValue(this.rs.getString("issooperator")) == 1;
            }
        }

        this.rs.executeSql("select workflowid from workflow_requestbase where requestid=" + var15);
        if(this.rs.next()) {
            var12 = this.rs.getString("workflowid");
        }

        WfFunctionManageUtil var25 = new WfFunctionManageUtil();
        User var26 = new User();
        var26.setUid(var16);
        WfFunctionManageUtil var27 = new WfFunctionManageUtil();
        boolean var28 = var27.haveStopright(var19, var20, var26, "" + var21, -1, var24);
        boolean var29 = var27.haveCancelright(var19, var20, var26, "" + var21, -1, var24);
        boolean var30 = var27.haveRestartright(var19, var20, var26, "" + var21, -1, var24);
        var28 = var28 && (var13 != 158 && var13 != 156 || !var14);
        var29 = var29 && (var13 != 158 && var13 != 156 || !var14);
        var30 = var30 && (var13 != 158 && var13 != 156 || !var14);
        String var31 = var12 + "+" + var16 + "+" + var17;
        boolean var32 = this.getWFMonitorCheckBox(var31).equals("true");
        if(var32) {
            var3 = "true";
        }

        if(var28) {
            var4 = "true";
        }

        if(var29) {
            var5 = "true";
        }

        if(var30) {
            var6 = "true";
        }

        if(!var30 || (var13 == 158 || var13 == 156) && var14) {
            HashMap var33 = var25.wfFunctionMonitorByNodeid(var12, var16 + "");
            String var34 = (String)var33.get("ov");
            String var35 = (String)var33.get("rb");
            WfForceOver var36 = new WfForceOver();
            if(var23 && "1".equals(var34) && !var36.isOver(Integer.parseInt(var15))) {
                var7 = "true";
            }

            boolean var37 = true;
            boolean var38 = false;
            this.rs.executeSql("select userid,usertype from workflow_currentoperator where requestid = " + var15 + " and isremark = \'2\' order by operatedate desc ,operatetime desc");
            if(this.rs.next()) {
                int var41 = this.rs.getInt("userid");
                int var42 = this.rs.getInt("usertype");
            }

            int var39 = 0;
            this.rs.executeSql("select count(distinct nodeid) as nodecounts from workflow_currentoperator where requestid=" + var15);
            if(this.rs.next()) {
                var39 = this.rs.getInt("nodecounts");
            }

            new WfForceDrawBack();
            if(var39 > 1 && var22 && !"0".equals(var35)) {
                var8 = "true";
            }

            if(GCONST.getWorkflowIntervenorByMonitor()) {
                this.rs.executeSql("select workflowid from workflow_monitor_bound where monitorhrmid=" + var16 + " and isintervenor=\'1\' and EXISTS(select 1 from workflow_requestbase where currentnodetype!=\'3\' and workflowid=workflow_monitor_bound.workflowid and requestid=" + var15 + ")");
                if(this.rs.next()) {
                    var9 = "true";
                }
            }
        }

        var10.add(var3);
        var10.add(var4);
        var10.add(var5);
        var10.add(var6);
        var10.add(var7);
        var10.add(var8);
        var10.add(var9);
        return var10;
    }

    public String getMonitorLink(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = "";
        byte var6 = 0;
        boolean var7 = false;
        String var8 = Util.null2String(var4[0]);
        int var9 = Integer.parseInt(Util.null2String(var4[1]));
        int var10 = Integer.parseInt(Util.null2String(var4[2]));
        int var11 = Integer.parseInt(Util.null2String(var4[3]));
        int var12 = Util.getIntValue(Util.null2String(var4[4]), -1);
        int var13 = Util.getIntValue(Util.null2String(var4[5]), -1);
        int var14 = Util.getIntValue(Util.null2String(var4[6]), -1);
        var3 = "";
        boolean var15 = true;
        boolean var16 = false;
        boolean var17 = false;
        boolean var18 = false;
        this.rs.executeSql("select workflowid from workflow_requestbase where requestid=" + var8);
        if(this.rs.next()) {
            var5 = this.rs.getString("workflowid");
        }

        Monitor var19 = new Monitor();
        MonitorDTO var20 = var19.getMonitorInfo(var9 + "", var13 + "", var5);
        var16 = var20.getIsforcedrawback();
        var17 = var20.getIsforceover();
        var18 = var20.getIssooperator();
        WfFunctionManageUtil var21 = new WfFunctionManageUtil();
        User var22 = new User();
        var22.setUid(var9);
        WfFunctionManageUtil var23 = new WfFunctionManageUtil();
        boolean var24 = var23.haveStopright(var12, var13, var22, "" + var14, -1, var18);
        boolean var25 = var23.haveCancelright(var12, var13, var22, "" + var14, -1, var18);
        boolean var26 = var23.haveRestartright(var12, var13, var22, "" + var14, -1, var18);
        var24 = var24 && (var6 != 158 && var6 != 156 || !var7);
        var25 = var25 && (var6 != 158 && var6 != 156 || !var7);
        var26 = var26 && (var6 != 158 && var6 != 156 || !var7);
        if(var24) {
            if(var15) {
                var3 = var3 + "<table><tr><td height=20><a href=\"#\" onclick=\"javaScript:doMonitorRequestSignle(\'" + var8 + "\',\'stop\')\">" + SystemEnv.getHtmlLabelName(20387, var11) + "</a>";
                var15 = false;
            } else {
                var3 = var3 + "</td></tr><tr><td height=20><a href=\"#\" onclick=\"javaScript:doMonitorRequestSignle(\'" + var8 + "\',\'stop\')\">" + SystemEnv.getHtmlLabelName(20387, var11) + "</a>";
                var15 = false;
            }
        }

        if(var25) {
            if(var15) {
                var3 = var3 + "<table><tr><td height=20><a href=\"#\" onclick=\"javaScript:doMonitorRequestSignle(\'" + var8 + "\',\'cancel\')\">" + SystemEnv.getHtmlLabelName(16210, var11) + "</a>";
                var15 = false;
            } else {
                var3 = var3 + "</td></tr><tr><td height=20><a href=\"#\" onclick=\"javaScript:doMonitorRequestSignle(\'" + var8 + "\',\'cancel\')\">" + SystemEnv.getHtmlLabelName(16210, var11) + "</a>";
                var15 = false;
            }
        }

        if(var26) {
            if(var15) {
                var3 = var3 + "<table><tr><td height=20><a href=\"#\" onclick=\"javaScript:doMonitorRequestSignle(\'" + var8 + "\',\'restart\')\">" + SystemEnv.getHtmlLabelName(18095, var11) + "</a>";
                var15 = false;
            } else {
                var3 = var3 + "</td></tr><tr><td height=20><a href=\"#\" onclick=\"javaScript:doMonitorRequestSignle(\'" + var8 + "\',\'restart\')\">" + SystemEnv.getHtmlLabelName(18095, var11) + "</a>";
                var15 = false;
            }
        }

        if(!var26 || (var6 == 158 || var6 == 156) && var7) {
            HashMap var27 = var21.wfFunctionManageAsMonitor(Integer.parseInt(var1));
            String var28 = (String)var27.get("ov");
            String var29 = (String)var27.get("rb");
            WfForceOver var30 = new WfForceOver();
            if(var17 && "1".equals(var28) && !var30.isOver(Integer.parseInt(var8))) {
                var3 = var3 + "<table><tr><td height=20><a href=\"#\" onclick=\"javaScript:doMonitorRequestSignle(\'" + var8 + "\',\'ovm\')\">" + SystemEnv.getHtmlLabelName(18360, var11) + "</a>";
                var15 = false;
            }

            int var31 = 1;
            int var32 = 0;
            this.rs.executeSql("select userid,usertype from workflow_currentoperator where requestid = " + var8 + " and isremark = \'2\' order by operatedate desc ,operatetime desc");
            if(this.rs.next()) {
                var31 = this.rs.getInt("userid");
                var32 = this.rs.getInt("usertype");
            }

            int var33 = 0;
            this.rs.executeSql("select count(distinct nodeid) as nodecounts from workflow_currentoperator where requestid=" + var8);
            if(this.rs.next()) {
                var33 = this.rs.getInt("nodecounts");
            }

            WfForceDrawBack var34 = new WfForceDrawBack();
            if(var33 > 1 && var16 && !"0".equals(var29) && var34.isHavePurview(Integer.parseInt(var8), var9, var10, var31, var32)) {
                if(var15) {
                    var3 = var3 + "<table><tr><td height=20><a href=\"#\" onclick=\"javaScript:doMonitorRequestSignle(\'" + var8 + "\',\'rbm\')\">" + SystemEnv.getHtmlLabelName(18359, var11) + "</a>";
                    var15 = false;
                } else {
                    var3 = var3 + "</td></tr><tr><td height=20><a href=\"#\" onclick=\"javaScript:doMonitorRequestSignle(\'" + var8 + "\',\'rbm\')\">" + SystemEnv.getHtmlLabelName(18359, var11) + "</a>";
                    var15 = false;
                }
            }

            if(GCONST.getWorkflowIntervenorByMonitor() && var20.getIsintervenor() && var14 != 3) {
                if(var15) {
                    var3 = var3 + "<table><tr><td height=20><a href=\"#\" onclick=\"javaScript:openFullWindowHaveBar(\'/workflow/request/ViewRequest.jsp?isintervenor=1&requestid=" + var8 + "\')\">" + SystemEnv.getHtmlLabelName(18913, var11) + "</a>";
                    var15 = false;
                } else {
                    var3 = var3 + "</td></tr><tr><td height=20><a href=\"#\" onclick=\"javaScript:openFullWindowHaveBar(\'/workflow/request/ViewRequest.jsp?isintervenor=1&requestid=" + var8 + "\')\">" + SystemEnv.getHtmlLabelName(18913, var11) + "</a>";
                    var15 = false;
                }
            }
        }

        if(!var15) {
            var3 = var3 + "</td></tr></table>";
        }

        return var3;
    }

    public String getSubWFLink(String var1, String var2) {
        int var3 = 7;
        if(var2 != null && !var2.equals("")) {
            var3 = Integer.parseInt(var2);
        }

        String var4 = "";
        this.rs.executeSql("select requestid from workflow_requestbase where mainRequestId=" + var1);
        if(this.rs.next()) {
            var4 = "<a href=\"/workflow/search/SubWFSearchResult.jsp?mainRequestId=" + var1 + "\" target=\'_new\'>" + SystemEnv.getHtmlLabelName(361, var3) + "</a>";
        }

        return var4;
    }

    public String getSubWFLinkNew(String var1, String var2) {
        int var3 = 7;
        if(var2 != null && !var2.equals("")) {
            var3 = Integer.parseInt(var2);
        }

        String var4 = "<a href=\"/workflow/search/SubWFSearchResult.jsp?isNew=2New&mainRequestId=" + var1 + "\">" + SystemEnv.getHtmlLabelName(361, var3) + "</a>";
        return var4;
    }

    public String getCancleMoniter(String var1, String var2) {
        String[] var3 = Util.TokenizerString2(var2, "+");
        String var4 = Util.null2String(var3[0]);
        String var5 = Util.null2String(var3[1]);
        String var6 = Util.null2String(var3[2]);
        int var7 = 7;
        if(var5 != null && !var5.equals("")) {
            var7 = Integer.parseInt(var5);
        }

        int var8 = 0;
        if(var3.length > 3) {
            var8 = Util.getIntValue(Util.null2String(var3[3]), -1);
        }

        int var9 = 0;
        if(var3.length > 4) {
            var9 = Util.getIntValue(Util.null2String(var3[4]), -1);
        }

        String var10 = "";
        if(var3.length > 5) {
            var10 = Util.null2String(var3[5]);
        }

        String var11 = "";
        if(var3.length > 6) {
            var11 = Util.null2String(var3[6]);
        }

        String var12 = "";
        if(var6.equals("1")) {
            var12 = "<a href=\"/system/systemmonitor/workflow/systemMonitorOperation.jsp?monitorhrmid=" + var4 + "&actionKey=delflow&flowid=" + var1 + "&subcompanyid=" + var11 + "&typeid=" + var10 + "\">" + SystemEnv.getHtmlLabelName(201, var7) + SystemEnv.getHtmlLabelName(665, var7) + "</a>";
        } else if(var9 == 1) {
            if(var8 > 0) {
                var12 = "<a href=\"/system/systemmonitor/workflow/systemMonitorOperation.jsp?monitorhrmid=" + var4 + "&actionKey=delflow&flowid=" + var1 + "&subcompanyid=" + var11 + "&typeid=" + var10 + "\">" + SystemEnv.getHtmlLabelName(201, var7) + SystemEnv.getHtmlLabelName(665, var7) + "</a>";
            } else {
                var12 = SystemEnv.getHtmlLabelName(201, var7) + SystemEnv.getHtmlLabelName(665, var7);
            }
        } else {
            var12 = "<a href=\"/system/systemmonitor/workflow/systemMonitorOperation.jsp?monitorhrmid=" + var4 + "&actionKey=delflow&flowid=" + var1 + "&subcompanyid=" + var11 + "&typeid=" + var10 + "\">" + SystemEnv.getHtmlLabelName(201, var7) + SystemEnv.getHtmlLabelName(665, var7) + "</a>";
        }

        return var12;
    }

    public String getSubWFReceiver(String var1) {
        String var2 = "";
        int var3 = 0;
        int var4 = 0;
        boolean var5 = false;
        String var6 = "0";
        boolean var7 = false;
        boolean var8 = false;
        this.rs.executeSql(" select workflowid from workflow_requestbase where requestid=" + var1);
        if(this.rs.next()) {
            var3 = this.rs.getInt(1);
        }

        this.rs.executeSql("select nodeId from workflow_flownode where workflowid=" + var3 + " and nodeType=\'0\'");
        if(this.rs.next()) {
            var4 = this.rs.getInt(1);
        }

        this.rs.executeSql("select destnodeid from workflow_nodelink where wfrequestid is null and workflowId=" + var3 + " and EXISTS(select 1 from workflow_nodebase b where workflow_nodelink.destnodeid=b.id and (b.IsFreeNode is null or b.IsFreeNode!=\'1\')) and nodeId=" + var4);

        while(this.rs.next()) {
            int var9 = this.rs.getInt(1);
            var6 = var6 + "," + var9;
        }

        this.rs.executeSql(" select userId,userType from workflow_currentoperator where requestid=" + var1 + "   and nodeId in(" + var6 + ") ");

        while(this.rs.next()) {
            int var10 = this.rs.getInt(1);
            int var11 = this.rs.getInt(2);
            if(var11 == 0) {
                var2 = var2 + " <a href=\"javaScript:openhrm(" + var10 + ")\" onclick=\'pointerXY(event);\'>" + this.rc.getResourcename(String.valueOf(var10)) + "</a>";
            } else if(var11 == 1) {
                var2 = var2 + "<a href=\"javaScript:openFullWindowHaveBar(\'/CRM/data/ViewCustomer.jsp?CustomerID=" + var10 + "\')\">" + this.cci.getCustomerInfoname(String.valueOf(var10)) + "</a>";
            }
        }

        return var2;
    }

    public String getSubWFRequestDescription(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        boolean var7 = false;
        this.rs.executeSql("select requestId from workflow_currentoperator where requestid=" + var5 + " and userId=" + var6);
        if(this.rs.next()) {
            var7 = true;
        }

        if(var7) {
            var3 = "<a href=javaScript:openFullWindowHaveBar(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "\')>" + var1 + "</a>";
        } else {
            var3 = var1;
        }

        return var3;
    }

    public String getOthers(String var1, String var2) {
        String var3 = "";
        String var4 = "";
        String[] var5 = Util.TokenizerString2(var2, "+");
        String var6 = Util.null2String(var5[1]);
        String var7 = Util.null2String(var5[2]);
        String var8 = Util.null2String(var5[3]);
        int var9 = Util.getIntValue(Util.null2String(var5[4]));
        String var10 = Util.null2String(var5[0]);
        char var11 = Util.getSeparator();
        String var12 = Util.null2String(var5[5]);
        String var13 = Util.null2String(var5[6]);

        try {
            if(var8.equals("118")) {
                var3 = "<a href=\'/meeting/report/MeetingRoomPlan.jsp\' target=\'_blank\'>" + SystemEnv.getHtmlLabelName(2193, var9) + "</a>";
            }

            if(!var7.equals("1") && !var7.equals("2")) {
                String var15;
                String var16;
                String var21;
                String var22;
                if(var7.equals("3")) {
                    if(!var8.equals("2") && !var8.equals("19")) {
                        if(!var1.equals("")) {
                            this.BrowserComInfo.getBrowserurl(var8);
                            var15 = this.BrowserComInfo.getLinkurl(var8);
                            var16 = "";
                            String var17 = "";
                            ArrayList var18 = Util.TokenizerString(var1, ",");
                            int var19;
                            if(!var8.equals("8") && !var8.equals("135")) {
                                if(!var8.equals("1") && !var8.equals("17")) {
                                    if(!var8.equals("7") && !var8.equals("18")) {
                                        if(!var8.equals("4") && !var8.equals("57")) {
                                            if(!var8.equals("9") && !var8.equals("37")) {
                                                if(var8.equals("23")) {
                                                    for(var19 = 0; var19 < var18.size(); ++var19) {
                                                        if(!var15.equals("")) {
                                                            var16 = var16 + "<a href=\'" + var15 + var18.get(var19) + "\' target=\'_new\'>" + this.CapitalComInfo1.getCapitalname((String)var18.get(var19)) + "</a>&nbsp";
                                                        } else {
                                                            var16 = var16 + this.CapitalComInfo1.getCapitalname((String)var18.get(var19)) + " ";
                                                        }
                                                    }
                                                } else if(!var8.equals("16") && !var8.equals("152") && !var8.equals("171")) {
                                                    if(var8.equals("141")) {
                                                        var16 = var16 + this.rcm.getFormShowName(var1, var9);
                                                    } else if(var8.equals("142")) {
                                                        for(var19 = 0; var19 < var18.size(); ++var19) {
                                                            if(!var15.equals("")) {
                                                                var16 = var16 + "<a href=\'" + var15 + var18.get(var19) + "\' target=\'_new\'>" + this.duc.getReceiveUnitName("" + var18.get(var19)) + "</a>&nbsp";
                                                            } else {
                                                                var16 = var16 + this.duc.getReceiveUnitName("" + var18.get(var19)) + " ";
                                                            }
                                                        }
                                                    } else {
                                                        String var24;
                                                        String var39;
                                                        if(var8.equals("161")) {
                                                            var16 = "";
                                                            var39 = "";
                                                            var17 = var1;

                                                            try {
                                                                Browser var36 = (Browser)StaticObj.getServiceByFullname(var13, Browser.class);
                                                                BrowserBean var38 = var36.searchById(var17);
                                                                var22 = Util.null2String(var38.getDescription());
                                                                String var40 = Util.null2String(var38.getName());
                                                                var24 = Util.null2String(var38.getHref());
                                                                if(var24.equals("")) {
                                                                    var16 = "<a title=\'" + var22 + "\'>" + var40 + "</a>&nbsp";
                                                                } else {
                                                                    var16 = "<a title=\'" + var22 + "\' href=\'" + var24 + "\' target=\'_blank\'>" + var40 + "</a>&nbsp";
                                                                }
                                                            } catch (Exception var27) {
                                                                ;
                                                            }
                                                        } else if(var8.equals("162")) {
                                                            var16 = "";
                                                            var17 = var1;

                                                            try {
                                                                Browser var41 = (Browser)StaticObj.getServiceByFullname(var13, Browser.class);
                                                                ArrayList var35 = Util.TokenizerString(var17, ",");

                                                                for(int var37 = 0; var37 < var35.size(); ++var37) {
                                                                    var22 = (String)var35.get(var37);
                                                                    BrowserBean var23 = var41.searchById(var22);
                                                                    var24 = Util.null2String(var23.getName());
                                                                    String var25 = Util.null2String(var23.getDescription());
                                                                    String var26 = Util.null2String(var23.getHref());
                                                                    if(var26.equals("")) {
                                                                        var16 = var16 + "<a title=\'" + var25 + "\'>" + var24 + "</a>&nbsp";
                                                                    } else {
                                                                        var16 = var16 + "<a title=\'" + var25 + "\' href=\'" + var26 + "\' target=\'_blank\'>" + var24 + "</a>&nbsp";
                                                                    }
                                                                }
                                                            } catch (Exception var28) {
                                                                ;
                                                            }
                                                        } else if(!var8.equals("224") && !var8.equals("225") && !var8.equals("226") && !var8.equals("227")) {
                                                            var39 = this.BrowserComInfo.getBrowsertablename(var8);
                                                            String var34 = this.BrowserComInfo.getBrowsercolumname(var8);
                                                            var21 = this.BrowserComInfo.getBrowserkeycolumname(var8);
                                                            var1 = this.deleteFirstAndEndchar(var1, ",");
                                                            if(var1.indexOf(",") != -1) {
                                                                var4 = "select " + var21 + "," + var34 + " from " + var39 + " where " + var21 + " in( " + var1 + ")";
                                                            } else {
                                                                var4 = "select " + var21 + "," + var34 + " from " + var39 + " where " + var21 + "=" + var1;
                                                            }

                                                            this.RecordSet.executeSql(var4);

                                                            while(this.RecordSet.next()) {
                                                                if(!var15.equals("")) {
                                                                    var16 = var16 + "<a href=\'" + var15 + this.RecordSet.getString(1) + "\' target=\'_new\'>" + Util.toScreen(this.RecordSet.getString(2), var9) + "</a>&nbsp";
                                                                } else {
                                                                    var16 = var16 + Util.toScreen(this.RecordSet.getString(2), var9) + " ";
                                                                }
                                                            }
                                                        } else {
                                                            var16 = var1;
                                                        }
                                                    }
                                                } else {
                                                    for(var19 = 0; var19 < var18.size(); ++var19) {
                                                        if(var19 > 0) {
                                                            var16 = var16 + "<br>";
                                                        }

                                                        if(!var15.equals("")) {
                                                            byte var20 = 0;
                                                            int var33 = var20 + 1;
                                                            var16 = var16 + "<a href=\'" + var15 + var18.get(var19) + "&wflinkno=" + var33 + "\' target=\'_new\'>" + this.WorkflowRequestComInfo1.getRequestName((String)var18.get(var19)) + "</a>";
                                                        } else {
                                                            var16 = var16 + this.WorkflowRequestComInfo1.getRequestName((String)var18.get(var19));
                                                        }
                                                    }
                                                }
                                            } else {
                                                for(var19 = 0; var19 < var18.size(); ++var19) {
                                                    if(var19 > 0) {
                                                        var16 = var16 + "<br>";
                                                    }

                                                    if(!var15.equals("")) {
                                                        var16 = var16 + "<a href=\'" + var15 + var18.get(var19) + "&requestid=" + var10 + "\' target=\'_new\'>" + this.DocComInfo1.getDocname((String)var18.get(var19)) + "</a>";
                                                    } else {
                                                        var16 = var16 + this.DocComInfo1.getDocname((String)var18.get(var19));
                                                    }
                                                }
                                            }
                                        } else {
                                            for(var19 = 0; var19 < var18.size(); ++var19) {
                                                if(!var15.equals("")) {
                                                    var16 = var16 + "<a href=\'" + var15 + var18.get(var19) + "\' target=\'_new\'>" + this.DepartmentComInfo1.getDepartmentname((String)var18.get(var19)) + "</a>&nbsp";
                                                } else {
                                                    var16 = var16 + this.DepartmentComInfo1.getDepartmentname((String)var18.get(var19)) + " ";
                                                }
                                            }
                                        }
                                    } else {
                                        for(var19 = 0; var19 < var18.size(); ++var19) {
                                            if(!var15.equals("")) {
                                                var16 = var16 + "<a href=\'" + var15 + var18.get(var19) + "\' target=\'_new\'>" + this.cci.getCustomerInfoname((String)var18.get(var19)) + "</a>&nbsp";
                                            } else {
                                                var16 = var16 + this.cci.getCustomerInfoname((String)var18.get(var19)) + " ";
                                            }
                                        }
                                    }
                                } else {
                                    for(var19 = 0; var19 < var18.size(); ++var19) {
                                        if(!var15.equals("")) {
                                            if("/hrm/resource/HrmResource.jsp?id=".equals(var15)) {
                                                var16 = var16 + "<a href=\'javaScript:openhrm(" + var18.get(var19) + ");\' onclick=\'pointerXY(event);\'>" + this.rc.getResourcename((String)var18.get(var19)) + "</a>&nbsp";
                                            } else {
                                                var16 = var16 + "<a href=\'" + var15 + var18.get(var19) + "\' target=\'_new\'>" + this.rc.getResourcename((String)var18.get(var19)) + "</a>&nbsp";
                                            }
                                        } else {
                                            var16 = var16 + this.rc.getResourcename((String)var18.get(var19)) + " ";
                                        }
                                    }
                                }
                            } else {
                                for(var19 = 0; var19 < var18.size(); ++var19) {
                                    if(!var15.equals("")) {
                                        var16 = var16 + "<a href=\'" + var15 + var18.get(var19) + "\' target=\'_new\'>" + this.ProjectInfoComInfo1.getProjectInfoname((String)var18.get(var19)) + "</a>&nbsp";
                                    } else {
                                        var16 = var16 + this.ProjectInfoComInfo1.getProjectInfoname((String)var18.get(var19)) + " ";
                                    }
                                }
                            }

                            var3 = var16;
                        }
                    } else {
                        var3 = var1;
                    }
                } else if(var7.equals("4")) {
                    var3 = var1;
                } else if(var7.equals("5")) {
                    this.rs.executeProc("workflow_SelectItemSelectByid", "" + var6 + var11 + var12);

                    while(this.rs.next()) {
                        String var14 = Util.null2String(this.rs.getString("selectvalue"));
                        var15 = Util.toScreen(this.rs.getString("selectname"), var9);
                        if(var1.equals(var14)) {
                            var3 = var15;
                        }
                    }
                } else if(var7.equals("6") && !var1.equals("")) {
                    var4 = "select id,docsubject,accessorycount from docdetail where id in(" + var1 + ") order by id asc";
                    int var30 = -1;
                    this.RecordSet.executeSql(var4);

                    while(true) {
                        while(this.RecordSet.next()) {
                            ++var30;
                            if(var30 > 0) {
                                var3 = var3 + "<br>";
                            }

                            var15 = Util.null2String(this.RecordSet.getString(1));
                            var16 = Util.toScreen(this.RecordSet.getString(2), var9);
                            int var31 = this.RecordSet.getInt(3);
                            this.DocImageManager.resetParameter();
                            this.DocImageManager.setDocid(Integer.parseInt(var15));
                            this.DocImageManager.selectDocImageInfo();
                            String var32 = "";
                            long var44 = 0L;
                            var21 = "";
                            var22 = "";
                            boolean var42 = false;
                            if(this.DocImageManager.next()) {
                                var32 = this.DocImageManager.getImagefileid();
                                long var10000 = (long)this.DocImageManager.getImageFileSize(Util.getIntValue(var32));
                                var21 = this.DocImageManager.getImagefilename();
                                var22 = var21.substring(var21.lastIndexOf(".") + 1).toLowerCase();
                                int var43 = this.DocImageManager.getVersionId();
                            }

                            if(var31 == 1 && (var22.equalsIgnoreCase("xls") || var22.equalsIgnoreCase("doc") || var22.equalsIgnoreCase("pdf"))) {
                                var3 = var3 + "<a href=\"javascript:openFullWindowHaveBar(\'/docs/docs/DocDspExt.jsp?id=" + var15 + "&imagefileId=" + var32 + "&isFromAccessory=true\')\">" + var21 + "</a> ";
                            } else {
                                var3 = var3 + "<a href=\"javascript:openFullWindowHaveBar(\'/docs/docs/DocDsp.jsp?id=" + var15 + " \')\">" + var16 + "</a> ";
                            }
                        }

                        return var3;
                    }
                }
            } else if(var7.equals("1") && var8.equals("4")) {
                var3 = Util.milfloatFormat(var1);
            } else {
                var3 = Util.toHtmlSearch(var1);
            }
        } catch (Exception var29) {
            var3 = "";
        }

        return var3;
    }

    public String getWfNewLinkByUrger(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        int var5 = Util.getIntValue(Util.null2String(var4[0]));
        int var6 = Util.getIntValue(Util.null2String(var4[1]));
        int var7 = Util.getIntValue(Util.null2String(var4[2]), 0);
        int var8 = Util.getIntValue(Util.null2String(var4[3]), 0);
        int var9 = Util.getIntValue(Util.null2String(var4[4]), 7);
        int var10 = 0;
        int var11 = 0;
        boolean var12 = false;
        String var13 = "select b.lastoperatedate,b.lastoperatetime,b.creater,b.lastoperator, b.lastoperatortype from workflow_requestbase b where b.requestid = " + var5;
        String var14 = "";
        String var15 = "";
        this.rs.execute(var13);
        if(this.rs.next() && (var7 != this.rs.getInt(4) || var8 != this.rs.getInt(5))) {
            if(!"".equals(Util.null2String(this.rs.getString(1))) && !"".equals(Util.null2String(this.rs.getString(2)))) {
                var14 = this.rs.getString(1) + this.rs.getString(2);
                var13 = "select max(w.viewdate) as viewdate,max(w.viewtime) as viewtime from workflow_requestviewlog w where w.viewer=" + var7 + " and id=" + var5 + " group by id";
                this.rs.execute(var13);
                if(this.rs.next()) {
                    var15 = this.rs.getString(1) + this.rs.getString(2);
                    if(var15.compareTo(var14) < 0) {
                        var12 = true;
                    }
                } else {
                    var12 = true;
                }
            } else if(this.rs.getInt(3) != var7) {
                var13 = "select w.viewdate from workflow_requestviewlog w where w.viewer=" + var7 + " and id=" + var5;
                this.rs.execute(var13);
                if(!this.rs.next()) {
                    var12 = true;
                }
            }
        }

        this.rs.execute("select formid,isbill from workflow_base where id=" + var6);
        if(this.rs.next()) {
            var11 = this.rs.getInt(1);
            var10 = this.rs.getInt(2);
        }

        MailAndMessage var16 = new MailAndMessage();
        String var17 = var16.getTitle(var5, var6, var11, var9, var10);
        if(!var17.equals("")) {
            var1 = var1 + "<B>（" + var17 + "）</B>";
        }

        if(var12) {
            var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&urger=1\'," + var5 + ")>" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><img src=\'/images/ecology8/statusicon/BDNew_wev8.png\' title=\'" + SystemEnv.getHtmlLabelName(19154, var9) + "\'/></span>";
        } else {
            var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&urger=1\'," + var5 + ")>" + var1 + "</a><span id=\'wflist_" + var5 + "span\'></span>";
        }

        return var3;
    }

    public String getWfNewLinkByUrgerExt(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        int var5 = Util.getIntValue(Util.null2String(var4[0]));
        int var6 = Util.getIntValue(Util.null2String(var4[1]));
        int var7 = Util.getIntValue(Util.null2String(var4[4]), 7);
        int var8 = 0;
        int var9 = 0;
        this.rs.execute("select formid,isbill from workflow_base where id=" + var6);
        if(this.rs.next()) {
            var9 = this.rs.getInt(1);
            var8 = this.rs.getInt(2);
        }

        MailAndMessage var10 = new MailAndMessage();
        String var11 = var10.getTitle(var5, var6, var9, var7, var8);
        if(!var11.equals("")) {
            var1 = var1 + "<B>（" + var11 + "）</B>";
        }

        var3 = "<a href=javaScript:openWfToTab(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&urger=1\',\'" + Util.toHtmlForCpt(var1) + "\')>" + var1 + "</a>";
        return var3;
    }

    public String getWfNewLinkImageExt(String var1, String var2) {
        String var3 = "";
        String var4 = "select a.viewdate,a.viewtime from workflow_requestviewlog a  where a.id=" + var1 + " and a.viewer=" + var2 + " order by a.viewdate desc,a.viewtime desc";
        String var5 = "select b.operator, b.operatedate,b.operatetime from workflow_requestlog b  where b.requestid=" + var1 + " order by b.operatedate desc ,b.operatetime desc";
        String var6 = "";
        String var7 = "";
        int var8 = -1;
        this.rs.execute(var4);
        if(this.rs.next()) {
            var6 = this.rs.getString(1) + this.rs.getString(2);
        }

        this.rs.execute(var5);
        if(this.rs.next()) {
            var8 = this.rs.getInt(1);
            var7 = this.rs.getString(2) + this.rs.getString(3);
        }

        if(!(var8 + "").equals(var2)) {
            int var9 = var6.compareTo(var7);
            if(var9 < 0) {
                var3 = "<IMG src=\'/images/BDNew_wev8.gif\' align=absbottom>";
            } else {
                var3 = "";
            }
        } else {
            var3 = "";
        }

        return var3;
    }

    public String getViewWorkflow(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        int var7 = Util.getIntValue(var4[2], 7);
        String var8 = Util.null2String(var4[3]);
        int var9 = 0;
        if(var4.length > 4) {
            var9 = Util.getIntValue(Util.null2String(var4[4]), -1);
        }

        int var10 = 0;
        if(var4.length > 5) {
            var10 = Util.getIntValue(Util.null2String(var4[5]), -1);
        }

        String var11 = "";
        if(!var8.equals("1") && var10 == 1 && var9 <= 0) {
            var11 = " disabled ";
        }

        StringBuffer var12 = new StringBuffer();
        if(Util.getIntValue(var1, 0) > 0) {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisview(this,").append(var5).append(",").append(var6).append(",0)\'").append(" name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' checked />");
        } else {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisview(this,").append(var5).append(",").append(var6).append(",1)\'").append(" name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' />");
        }

        return var12.toString();
    }

    public String getEditWorkflow(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        int var7 = Util.getIntValue(var4[2], 7);
        String var8 = Util.null2String(var4[3]);
        int var9 = 0;
        if(var4.length > 4) {
            var9 = Util.getIntValue(Util.null2String(var4[4]), -1);
        }

        int var10 = 0;
        if(var4.length > 5) {
            var10 = Util.getIntValue(Util.null2String(var4[5]), -1);
        }

        String var11 = "";
        if(!var8.equals("1") && var10 == 1 && var9 <= 0) {
            var11 = " disabled ";
        }

        StringBuffer var12 = new StringBuffer();
        if(Util.getIntValue(var1, 0) > 0) {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisedit(this,").append(var5).append(",").append(var6).append(",0)\'").append(" name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' checked />");
        } else {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisedit(this,").append(var5).append(",").append(var6).append(",1)\'").append(" name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' />");
        }

        return var12.toString();
    }

    public String getIntervenorWorkflow(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        int var7 = Util.getIntValue(var4[2], 7);
        String var8 = Util.null2String(var4[3]);
        int var9 = 0;
        if(var4.length > 4) {
            var9 = Util.getIntValue(Util.null2String(var4[4]), -1);
        }

        int var10 = 0;
        if(var4.length > 5) {
            var10 = Util.getIntValue(Util.null2String(var4[5]), -1);
        }

        String var11 = "";
        if(!var8.equals("1") && var10 == 1 && var9 <= 0) {
            var11 = " disabled ";
        }

        StringBuffer var12 = new StringBuffer();
        if(Util.getIntValue(var1, 0) > 0) {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisIntervenor(this," + var5 + "," + var6 + ",0)\' name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' checked />");
        } else {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisIntervenor(this," + var5 + "," + var6 + ",1)\' name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' />");
        }

        return var12.toString();
    }

    public String getDelWorkflow(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        int var7 = Util.getIntValue(var4[2], 7);
        String var8 = Util.null2String(var4[3]);
        int var9 = 0;
        if(var4.length > 4) {
            var9 = Util.getIntValue(Util.null2String(var4[4]), -1);
        }

        int var10 = 0;
        if(var4.length > 5) {
            var10 = Util.getIntValue(Util.null2String(var4[5]), -1);
        }

        String var11 = "";
        if(!var8.equals("1") && var10 == 1 && var9 <= 0) {
            var11 = " disabled ";
        }

        StringBuffer var12 = new StringBuffer();
        if(Util.getIntValue(var1, 0) > 0) {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisDel(this," + var5 + "," + var6 + ",0)\' name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' checked />");
        } else {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisDel(this," + var5 + "," + var6 + ",1)\' name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' />");
        }

        return var12.toString();
    }

    public String getFOWorkflow(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        int var7 = Util.getIntValue(var4[2], 7);
        String var8 = Util.null2String(var4[3]);
        int var9 = 0;
        if(var4.length > 4) {
            var9 = Util.getIntValue(Util.null2String(var4[4]), -1);
        }

        int var10 = 0;
        if(var4.length > 5) {
            var10 = Util.getIntValue(Util.null2String(var4[5]), -1);
        }

        String var11 = "";
        if(!var8.equals("1") && var10 == 1 && var9 <= 0) {
            var11 = " disabled ";
        }

        StringBuffer var12 = new StringBuffer();
        if(Util.getIntValue(var1, 0) > 0) {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisFO(this," + var5 + "," + var6 + ",0)\' name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' checked />");
        } else {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisFO(this," + var5 + "," + var6 + ",1)\' name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' />");
        }

        return var12.toString();
    }

    public String getSOWorkflow(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        int var7 = Util.getIntValue(var4[2], 7);
        String var8 = Util.null2String(var4[3]);
        int var9 = 0;
        if(var4.length > 4) {
            var9 = Util.getIntValue(Util.null2String(var4[4]), -1);
        }

        int var10 = 0;
        if(var4.length > 5) {
            var10 = Util.getIntValue(Util.null2String(var4[5]), -1);
        }

        String var11 = "";
        if(!var8.equals("1") && var10 == 1 && var9 <= 0) {
            var11 = " disabled ";
        }

        StringBuffer var12 = new StringBuffer();
        if(Util.getIntValue(var1, 0) > 0) {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisSO(this," + var5 + "," + var6 + ",0)\' name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' checked />");
        } else {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisSO(this," + var5 + "," + var6 + ",1)\' name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' />");
        }

        return var12.toString();
    }

    public String getFBWorkflow(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        int var7 = Util.getIntValue(var4[2], 7);
        String var8 = Util.null2String(var4[3]);
        int var9 = 0;
        if(var4.length > 4) {
            var9 = Util.getIntValue(Util.null2String(var4[4]), -1);
        }

        int var10 = 0;
        if(var4.length > 5) {
            var10 = Util.getIntValue(Util.null2String(var4[5]), -1);
        }

        String var11 = "";
        if(!var8.equals("1") && var10 == 1 && var9 <= 0) {
            var11 = " disabled ";
        }

        StringBuffer var12 = new StringBuffer();
        if(Util.getIntValue(var1, 0) > 0) {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisFB(this," + var5 + "," + var6 + ",0)\' name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' checked />");
        } else {
            var12.append("<input type=\'checkbox\' tzCheckbox=\'true\'  class=\'InputStyle\' onclick=\'setisFB(this," + var5 + "," + var6 + ",1)\' name=\'vradio_" + var6 + "\' " + var11).append("value=\'");
            var12.append("\' />");
        }

        return var12.toString();
    }

    /** @deprecated */
    @Deprecated
    public String getWFMonitorCheckBox(String var1) {
        String[] var2 = Util.TokenizerString2(var1, "+");
        int var3 = Util.getIntValue(var2[0]);
        int var4 = Util.getIntValue(var2[1]);
        int var5 = Util.getIntValue(var2[2], 1);
        String var6 = "false";
        if(var5 == 1) {
            this.rs.executeSql("select workflowid from workflow_monitor_bound where (isdelete=\'1\' or isforceover=\'1\') and workflowid=" + var3 + " and monitorhrmid=" + var4);
            if(this.rs.next()) {
                var6 = "true";
            }
        }

        return var6;
    }

    public String getUnOperatorsForStat(String var1) {
        String var2 = "";
        this.rs.executeSql("select distinct userid,usertype,agenttype,agentorbyagentid from workflow_currentoperator where (isremark in (\'0\',\'1\') or (isremark=\'4\' and viewtype=0))  and requestid = " + var1);

        while(this.rs.next()) {
            if(var2.equals("")) {
                if(this.rs.getInt("usertype") == 0) {
                    if(this.rs.getInt("agenttype") == 2) {
                        var2 = var2 + this.rc.getResourcename(this.rs.getString("agentorbyagentid")) + "->" + this.rc.getResourcename(this.rs.getString("userid"));
                    } else {
                        var2 = var2 + this.rc.getResourcename(this.rs.getString("userid"));
                    }
                } else {
                    var2 = var2 + this.cci.getCustomerInfoname(this.rs.getString("userid"));
                }
            } else if(this.rs.getInt("usertype") == 0) {
                if(this.rs.getInt("agenttype") == 2) {
                    var2 = var2 + "," + this.rc.getResourcename(this.rs.getString("agentorbyagentid")) + "->" + this.rc.getResourcename(this.rs.getString("userid"));
                } else {
                    var2 = var2 + "," + this.rc.getResourcename(this.rs.getString("userid"));
                }
            } else {
                var2 = var2 + this.cci.getCustomerInfoname(this.rs.getString("userid"));
            }
        }

        return var2;
    }

    public String deleteFirstAndEndchar(String var1, String var2) {
        boolean var3 = false;
        if(var1.substring(0, var2.length()).equals(var2)) {
            var1 = var1.substring(var2.length());
            var3 = true;
        }

        if(var1.substring(var1.length() - var2.length()).equals(var2)) {
            var1 = var1.substring(0, var1.length() - var2.length());
            var3 = true;
        }

        return var3?this.deleteFirstAndEndchar(var1, var2):var1;
    }

    public String getIsmultiprintStr(String var1, String var2) {
        String var3 = "";

        try {
            int var4 = Util.getIntValue(var2, 7);
            if("1".equals(var1)) {
                var3 = SystemEnv.getHtmlLabelName(27046, var4);
            } else {
                var3 = SystemEnv.getHtmlLabelName(27045, var4);
            }
        } catch (Exception var5) {
            var3 = SystemEnv.getHtmlLabelName(27045, 7);
        }

        return var3;
    }

    public String getWfNewLinkWithTitleNoAdditional(String var1, String var2) {
        String var3 = "";
        String[] var4 = Util.TokenizerString2(var2, "+");
        String var5 = Util.null2String(var4[0]);
        String var6 = Util.null2String(var4[1]);
        String var7 = Util.null2String(var4[2]);
        int var8 = Util.getIntValue(var4[3], 0);
        int var9 = Util.getIntValue(Util.null2String(var4[4]), 7);
        String var10 = Util.null2String(var4[5]);
        String var11 = Util.null2String(var4[6]);
        String var12 = Util.null2String(var4[7]);
        String var13 = "";
        if(var4.length >= 11) {
            var13 = Util.null2String(var4[10]);
        }

        String var14 = "";
        String var15 = "";
        if(var4.length >= 10) {
            var15 = Util.null2String(var4[9]);
        }

        boolean var16 = false;
        if(var11.equals("0") && (var13.equals("2") || var13.equals("3")) || var11.equals("5")) {
            var16 = true;
        }

        if(var7.equals("0")) {
            if(var16) {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ") >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><IMG src=\'/images/BDOut_wev8.gif\' align=absbottom></span>";
            } else if("1".equals(var15)) {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ") >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'></span>";
            } else {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ") >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><IMG src=\'/images/BDNew_wev8.gif\' align=absbottom></span>";
            }
        } else if(var7.equals("-1")) {
            if(var16) {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ") >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><IMG src=\'/images/BDOut_wev8.gif\' align=absbottom></span>";
            } else {
                var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ") >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><IMG src=\'/images/BDNew2_wev8.gif\' align=absbottom></span>";
            }
        } else if(var16) {
            var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ") >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'><IMG src=\'/images/BDOut_wev8.gif\' align=absbottom></span>";
        } else {
            var3 = "<a href=javaScript:openFullWindowHaveBarForWFList(\'/workflow/request/ViewRequest.jsp?requestid=" + var5 + "&isovertime=" + var8 + "\'," + var5 + ") >" + var1 + "</a><span id=\'wflist_" + var5 + "span\'></span>";
        }

        return var3;
    }

    public String getInterfaceChecBox(String var1) {
        var1 = Util.null2String(var1);
        return var1.equals("1")?"false":"true";
    }

    public String getInterfaceDetailChecBox(String var1) {
        return "true";
    }

    public String getDeployDesc(String var1) {
        var1 = Util.null2String(var1);
        return var1.equals("1")?"已部署":"<span class=\'noDeploy\'>未部署</span>";
    }

    public String getClosedDesc(String var1) {
        var1 = Util.null2String(var1);
        return var1.equals("1")?"封存":"正常";
    }

    public String getTypeDesc(String var1, String var2) {
        var1 = Util.null2String(var1);
        var2 = Util.null2String(var2);
        return var1.equals("0")?"出口":(var2.equals("0")?"节点后":"节点前");
    }

    public List<String> getInterfaceOperate(String var1, String var2, String var3) {
        ArrayList var4 = new ArrayList();
        if(var2.equals("1")) {
            var4.add("false");
            var4.add("true");
        } else {
            var4.add("true");
            var4.add("false");
        }

        if(var3.equals("1")) {
            var4.add("false");
            var4.add("true");
        } else {
            var4.add("true");
            var4.add("false");
        }

        return var4;
    }

    public List<String> getInterfaceDetailOperate(String var1) {
        ArrayList var2 = new ArrayList();
        var2.add("true");
        return var2;
    }

    public List getshowTransOperate(String var1) {
        ArrayList var2 = new ArrayList();
        var2.add("true");
        return var2;
    }

    public String getBrowserPerson(String var1, String var2) {
        String[] var3 = Util.TokenizerString2(var2, "+");
        String var4 = Util.null2String(var3[1]);
        String var5 = "";

        try {
            var5 = this.RequestDefaultComInfo.getShowoperator("" + var4);
        } catch (Exception var10) {
            ;
        }

        ArrayList var6 = new ArrayList();
        if(var5.equals("1")) {
            this.rs.executeSql("select distinct userid,usertype,agenttype,agentorbyagentid from workflow_currentoperator where (isremark in (\'0\',\'1\',\'5\',\'7\',\'8\',\'9\') or (isremark=\'4\' and viewtype=0))  and requestid = " + var1);

            while(this.rs.next()) {
                if(this.rs.getInt("usertype") == 0) {
                    String var7 = this.rs.getString("userid");
                    String var8 = this.rc.getResourcename(var7);
                    HashMap var9 = new HashMap();
                    var9.put("browserValue", var7);
                    var9.put("browserSpanValue", var8);
                    var6.add(var9);
                }
            }
        }

        return JsonUtils.list2json(var6);
    }

    public String getWFMultiSubmit(String var1) {
        String var2 = "";
        this.rs.executeSql("select multiSubmit from WORKFLOW_BASE WHERE ID = " + var1);

        while(this.rs.next()) {
            var2 = Util.null2String(this.rs.getString("multiSubmit"));
        }

        return var2;
    }
}
