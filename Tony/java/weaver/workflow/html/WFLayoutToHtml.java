package weaver.workflow.html;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weaver.docs.category.SecCategoryComInfo;
import weaver.system.SysWFLMonitor;
import weaver.system.code.CodeBuild;
import weaver.system.code.CoderBean;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.datainput.DynamicDataInput;
import weaver.workflow.field.DetailFieldComInfo;
import weaver.workflow.field.FieldComInfo;
import weaver.workflow.field.FieldTypeComInfo;
import weaver.workflow.field.HtmlElement;
import weaver.workflow.field.SpecialFieldInfo;
import weaver.workflow.field.SystemElement;
import weaver.workflow.mode.FieldInfo;
import weaver.workflow.request.RequestDoc;
import weaver.workflow.request.RequestPreAddinoperateManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import weaver.workflow.request.RequestManager;
import weaver.workflow.workflow.WFNodeDtlFieldManager;
import weaver.workflow.workflow.WFNodeFieldManager;
import weaver.workflow.workflow.WfLinkageInfo;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.hrm.resource.ResourceComInfo;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.JSONUtils;
import net.sf.json.xml.XMLSerializer;
import java.io.StringReader;

public class WFLayoutToHtml extends BaseBean {

  private HttpServletRequest request;

  private User user;

  private int iscreate;

  private String wfformhtml;// 解析后的表单

  private String htmlLayout;// 模板

  private StringBuffer jsStr;// 放javascript代码

  private StringBuffer vbsStr;// 放vbs代码

  private Hashtable otherPara_hs;

  private String needcheck;// 必填检查用

  private StringBuffer htmlHiddenElementsb;// 隐藏的input

  private String billtablename;// 单据主表表名
  private int cssfile;
	private String tcss_h;
	private String tcss_e;

private boolean hasRemark = true;
	
	private int htmlParseScheme;
	//签字意见是否存在于模板之中
    private boolean isRemarkInnerMode = false;
    
    
    /**
     * 当签字意见为电子签章模式时的占位符，用于在页面中替换为电子签章
     */
    public static final String HTML_FORMSIGNATURE_PLACEHOLDER = "<WEAVER_FORMSIGNATURE>WEAVER_HTML_FORMSIGNATURE_PLACEHOLDER</WEAVER_FORMSIGNATURE>";
    
  public WFLayoutToHtml() {
    wfformhtml = "";
    htmlLayout = "";
    jsStr = new StringBuffer();
    vbsStr = new StringBuffer();
    iscreate = 0;
    otherPara_hs = new Hashtable();
    needcheck = "";
    htmlHiddenElementsb = new StringBuffer();
    billtablename = "";
    cssfile = 0;
    tcss_h = "<!--tempcss start-->";
	tcss_e = "<!--tempcss end-->";
	htmlParseScheme = 0;
	hasRemark = true;
  }

  /***
   * 被页面调用的解析显示模板的方法。具体事件调用其它方法处理
   * 
   * @return
   */
  public Hashtable analyzeLayout() {
    Hashtable ret_hs = new Hashtable();
    try {
      RecordSet rs = new RecordSet();
      WFNodeFieldManager wFNodeFieldManager = new WFNodeFieldManager();
      int modeid = Util.getIntValue(request.getParameter("modeid"), 0);
      String syspath = "";
      rs.execute("select * from workflow_nodehtmllayout where id=" + modeid);
      if (rs.next()) {
        syspath = Util.null2String(rs.getString("syspath"));
        cssfile = Util.getIntValue(rs.getString("cssfile"), 0);
        htmlParseScheme = Util.getIntValue(rs.getString("htmlparsescheme"), 0);
      }
      htmlLayout = wFNodeFieldManager.readHtmlFile(syspath);// 模板
      // 解析模板，替换字段和节点意见
      analyzeLayoutElement();

      ret_hs.put("wfformhtml", wfformhtml);
    } catch (Exception e) {
      writeLog(e);
    }
    return ret_hs;
  }

  public void analyzeLayoutElement() {
    analyzeFormSplitPage();
    getMainTableElement();
    getNodeRemark();
    getDetailTableElement();
    getCssFile();
  }

  /**
   * 解析表单分tab页 如果有分tab的情况，则必须在流程表单的最开始出设置第一个tab标签，解析时，从第一个标签的位置开始设置tab页
   */
  public void analyzeFormSplitPage() {
    // <div class="formSplitPage"
    wfformhtml = htmlLayout;
    int pos = htmlLayout.toLowerCase().indexOf("formsplitpage");
    if (pos > -1) {// 需要分tab页
      try {
        StringBuffer tabHead_sb = new StringBuffer();// 流程页面内tab的头，最后加在模板文件前面
        tabHead_sb.append("\n").append("<table id=\"tabHeadDiv\" name=\"tabHeadDiv\" class=\"tabHeadDiv\" cellspacing=\"0\"><tr>");
        int tabCount = 0;
        String content1 = "";
        String content2 = "";
        while (pos > -1) {
          int lastDiv1 = htmlLayout.toLowerCase().lastIndexOf("<div", pos);// 找到的formSplitPage之前的第一个<div
          int firstDiv2 = htmlLayout.toLowerCase().indexOf("</div>", pos);// 找到的formSplitPage之后的第一个</div>
          content1 = wfformhtml.substring(0, lastDiv1);
          content2 = wfformhtml.substring(firstDiv2 + 6);
          String nameStr = wfformhtml.substring(lastDiv1 + 4, firstDiv2);
          String divStr = "";
          if (tabCount > 0) {// 不是第一个tab，需要终结前面的div
            divStr = "</div>\n";
          }
          divStr += "<div id=\"formsplitdiv" + tabCount + "\" name=\"formsplitdiv" + tabCount + "\" class=\"formSplitDiv\" ";
          String classStr = "In";
          if (tabCount > 0) {
            divStr += " style=\"display:none\" ";// 不是第一个，就隐藏掉
            classStr = "Out";// 不是第一个，就设置未选中class
          }
          divStr += " >";
          wfformhtml = content1 + divStr + content2;
          htmlLayout = wfformhtml;
          // 处理tab页名称
          int spanindex1 = nameStr.toLowerCase().indexOf(">");
          String name = "";
          if (spanindex1 > -1) {
            name = nameStr.substring(spanindex1 + 1);
          }
          if ("".equals(name)) {
            name = (SystemEnv.getHtmlLabelName(23825, user.getLanguage()) + "&nbsp;" + (tabCount + 1));
          }
          byte[] b = name.getBytes();
          tabHead_sb.append("<td id=\"formsplitspan" + tabCount + "\" name=\"formsplitspan" + tabCount + "\" class=\"formSplitSpan" + classStr + "\" style=\"width:" + (b.length * 12 + 10) + "px\" onclick=\"javascript:changeFormSplitPage(" + tabCount + ");\">&nbsp;&nbsp;&nbsp;" + name + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>");
          // tabHead_sb.append("<span name=\"borderformsplitspan\" class=\"borderFormSplitSpan\" ></span>");
          tabCount++;
          pos = htmlLayout.toLowerCase().indexOf("formsplitpage");
        }
        tabHead_sb.append("<td name=\"lastformsplitspan\" class=\"lastFormSplitSpan\" >&nbsp;</td>");
        tabHead_sb.append("</tr></table>").append("\n").append(wfformhtml).append("</div>");
        wfformhtml = tabHead_sb.toString();
        htmlLayout = wfformhtml;
      } catch (Exception e) {
        writeLog(e);
      }
    }
  }

  /**
   * 处理主子段的字段转化，同时处理明细字段的显示名转化
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void getMainTableElement() {
    try {
      String sql = "";
      RecordSet rs = new RecordSet();
      FieldComInfo fieldComInfo = new FieldComInfo();
      FieldTypeComInfo fieldTypeComInfo = new FieldTypeComInfo();
      ResourceComInfo resourceComInfo = new ResourceComInfo();
      WfLinkageInfo wfLinkageInfo = new WfLinkageInfo();
      SystemElement systemElement = new SystemElement();
      int requestid = Util.getIntValue(request.getParameter("requestid"), 0);
      int billid = Util.getIntValue(request.getParameter("billid"), 0);
      int workflowid = Util.getIntValue(request.getParameter("workflowid"), 0);
      int nodeid = Util.getIntValue(request.getParameter("nodeid"), 0);
      int nodetype = Util.getIntValue(request.getParameter("nodetype"), 0);// 创建页面正好不传过去，所以是0
      int isbill = Util.getIntValue(request.getParameter("isbill"), 0);
      int formid = Util.getIntValue(request.getParameter("formid"), 0);
      int isremark = Util.getIntValue(request.getParameter("isremark"), 0);
      int isprint = Util.getIntValue(request.getParameter("isprint"), 0);
      String isCanModify = Util.null2String(request.getParameter("IsCanModify"));
      
      boolean isurger = Util.null2String(request.getParameter("isurger")).equalsIgnoreCase("true");
      boolean wfmonitor = Util.null2String(request.getParameter("wfmonitor")).equalsIgnoreCase("true");
        
      String canDelAcc = "";
      String smsAlertsType = "";
      String chatsAlertType = "";
      rs.executeSql("select candelacc,smsAlertsType,chatsAlertType from workflow_base where id=" + workflowid);
      if (rs.next()) {
        canDelAcc = Util.null2String(rs.getString("candelacc"));
        smsAlertsType = Util.null2String(rs.getString("smsAlertsType"));
        chatsAlertType = Util.null2String(rs.getString("chatsAlertType"));
      }

      // 获取当前请求的流程类型中所设置的附件保存目录
      String docCategory = "";
      //rs.executeSql("select b.docCategory from workflow_requestbase a,workflow_base b where a.workflowid=b.id and a.requestid=" + requestid);
      //if (rs.next()) {
      //  docCategory = rs.getString("docCategory");
      //}
		RequestManager requestManager = new RequestManager();
		int uploadType = 0;
		String selectedfieldid = "";			
		String result = requestManager.getUpLoadTypeForSelect(workflowid);
		if(!result.equals("")){
			selectedfieldid = result.substring(0,result.indexOf(","));
			uploadType = Integer.valueOf(result.substring(result.indexOf(",")+1)).intValue();
		}
		boolean isCanuse = requestManager.hasUsedType(workflowid);
		if(selectedfieldid.equals("") || selectedfieldid.equals("0")){
			isCanuse = false;
		}
		if(isCanuse&&uploadType==1){
			String fieldName="";//字段名称
			String fieldValue="";//字段的值
			String tableName="workflow_form";
			if(isbill==0){//如果不为单据，即为表单
				sql=" select fieldName,fieldHtmlType,type from workflow_formdict where id="+selectedfieldid;
			}else{//否则为单据
				rs.executeSql(" select tableName from workflow_bill where id="+formid);
				if(rs.next()){
					tableName=rs.getString(1);
				}
				sql=" select fieldName,fieldHtmlType,type from workflow_billfield where (viewtype is null or viewtype<>1) and id= "+selectedfieldid;
			}
			rs.executeSql(sql);
			if(rs.next()){
				fieldName=rs.getString(1);
			}
			
			if(fieldName!=null&&!(fieldName.trim().equals(""))
			 &&tableName!=null&&!(tableName.trim().equals(""))){
				//rs.executeSql(" select "+fieldName+" from workflow_form where requestid= "+requestId);						
				rs.executeSql(" select "+fieldName+" from "+tableName+" where requestid= "+requestid);			
				if(rs.next()){
					fieldValue=Util.null2String(rs.getString(1));
				}
			}			
			SecCategoryComInfo secCategoryComInfo = new SecCategoryComInfo();
			Map secMaxUploads = new HashMap();//封装选择目录的信息
            Map secCategorys = new HashMap();
			int maxUploadImageSize = 5;				
			char flag = Util.getSeparator();
			rs.executeProc("workflow_SelectItemSelectByid", ""+selectedfieldid+flag+isbill);
			while(rs.next()){
				String tmpselectvalue = Util.null2String(rs.getString("selectvalue"));
				String tmpselectname = Util.toScreen(rs.getString("selectname"),user.getLanguage());
				String isdefault = Util.null2String(rs.getString("isdefault"));
				String selectedStr = "";
				String tdocCategory = Util.null2String(rs.getString("docCategory"));
				
				int tsecid = Util.getIntValue(tdocCategory.substring(tdocCategory.lastIndexOf(",")+1),-1);
				String tMaxUploadFileSize = ""+Util.getIntValue(secCategoryComInfo.getMaxUploadFileSize(""+tsecid),-1);
				secMaxUploads.put(tmpselectvalue,tMaxUploadFileSize);
                secCategorys.put(tmpselectvalue,tdocCategory);		                    
				if(!"".equals(tdocCategory)&&(("y".equals(isdefault)&&fieldValue.trim().equals(""))||tmpselectvalue.equals(fieldValue))){
					maxUploadImageSize = Util.getIntValue(tMaxUploadFileSize,5);    				
    				docCategory= tdocCategory;
				}
			}
			if(secMaxUploads.size()>0){
				otherPara_hs.put("maxUploadImageSize", ""+maxUploadImageSize);
				otherPara_hs.put("secMaxUploads", secMaxUploads);
                otherPara_hs.put("secCategorys", secCategorys);
			}				
			
		}else{
			rs.executeSql("select b.docCategory from workflow_requestbase a,workflow_base b where a.workflowid=b.id and a.requestid="+requestid );
			if (rs.next()) {
				docCategory= rs.getString("docCategory");
			}				
		}      
      
      int currentnodetype = -1;
      rs.executeProc("workflow_Requestbase_SByID", "" + requestid);
      if (rs.next()) {
        currentnodetype = Util.getIntValue(rs.getString("currentnodetype"), 0);
      }
      otherPara_hs.put("workflowid", "" + workflowid);
      otherPara_hs.put("isremark", "" + isremark);
      otherPara_hs.put("nodeid", "" + nodeid);
      otherPara_hs.put("isbill", "" + isbill);
      otherPara_hs.put("nodetype", "" + nodetype);
      otherPara_hs.put("canDelAcc", canDelAcc);
      otherPara_hs.put("docCategory", docCategory);
      otherPara_hs.put("iscreate", "" + iscreate);
      otherPara_hs.put("isprint", "" + isprint);
      
      otherPara_hs.put("wfmonitor", "" + wfmonitor);
      otherPara_hs.put("isurger", "" + isurger);

      ArrayList fieldidList = new ArrayList();// 表单的所有主表字段列表
      ArrayList detailFieldidList = new ArrayList();// 表单的所有明细表字段列表
      ArrayList fieldhtmltypeList = new ArrayList(); // 字段的htmltype队列
      ArrayList fieldtypeList = new ArrayList(); // 字段的type队列
      ArrayList fielddbtypeList = new ArrayList(); // 字段的数据库字段类型队列
      Hashtable fieldname_hs = new Hashtable();// 表字段在数据库的字段名字
      Hashtable isview_hs = new Hashtable();// 是否显示
      Hashtable isedit_hs = new Hashtable();// 是否可编辑
      Hashtable ismand_hs = new Hashtable();// 是否必填
      Hashtable fieldlabel_hs = new Hashtable();// 字段的显示名。这个显示名现在只用在temptitle里，用于必填提示
      Hashtable fieldvalue_hs = new Hashtable();// 字段的值。如果是创建流程时，则为空
      if (isbill == 0) {
        sql = "select nf.*, ff.isdetail, fl.fieldlable, ff.groupid, '' as fieldname from workflow_nodeform nf left join workflow_formfield ff on nf.fieldid=ff.fieldid and ff.formid=" + formid + " left join workflow_fieldlable fl on fl.fieldid=nf.fieldid and fl.formid=" + formid + " and fl.langurageid=" + user.getLanguage() + " where nf.nodeid=" + nodeid + " order by nf.orderid, ff.fieldorder";
      } else if (isbill == 1) {
        // 单据的明细字段信息先拿出来，具体分组以后再查数据库
        sql = "select nf.*, bf.viewtype as isdetail, bf.fieldlabel as fieldlable, detailtable as groupid, bf.fieldname, bf.fieldhtmltype, bf.type, bf.fielddbtype,bf.textheight,bf.imgheight,bf.imgwidth from workflow_nodeform nf left join workflow_billfield bf on nf.fieldid=bf.id and bf.billid=" + formid + " where nf.nodeid=" + nodeid + " order by nf.orderid, bf.dsporder";
      }
      rs.execute(sql);
      while (rs.next()) {
        int isdetail_tmp = Util.getIntValue(rs.getString("isdetail"), 0);
        int fieldid_tmp = Util.getIntValue(rs.getString("fieldid"), 0);
        int isview_tmp = Util.getIntValue(rs.getString("isview"), 0);
        int isedit_tmp = Util.getIntValue(rs.getString("isedit"), 0);
        int ismand_tmp = Util.getIntValue(rs.getString("ismandatory"), 0);
        String fieldname = Util.null2String(rs.getString("fieldname"));
        if (isbill == 0) {
          fieldname = fieldComInfo.getFieldname("" + fieldid_tmp);
        }
        String labelName_tmp = "";
        if (isbill == 0) {
          labelName_tmp = Util.null2String(rs.getString("fieldlable"));
        } else {
          int labelid_tmp = Util.getIntValue(rs.getString("fieldlable"));
          labelName_tmp = SystemEnv.getHtmlLabelName(labelid_tmp, user.getLanguage());
        }
        // 流程名称、紧急程度、短信情况特殊处理
        if (fieldid_tmp == -1) {
          labelName_tmp = SystemEnv.getHtmlLabelName(21192, user.getLanguage());
        } else if (fieldid_tmp == -2) {
          labelName_tmp = SystemEnv.getHtmlLabelName(15534, user.getLanguage());
        } else if (fieldid_tmp == -3) {
          labelName_tmp = SystemEnv.getHtmlLabelName(17586, user.getLanguage());
        }else if(fieldid_tmp == -4){
			labelName_tmp = SystemEnv.getHtmlLabelName(17614, user.getLanguage());
		}else if(fieldid_tmp == -5){
			labelName_tmp = SystemEnv.getHtmlLabelName(32812, user.getLanguage());
		}
        labelName_tmp = Util.toScreenForWorkflow(labelName_tmp);
        if (isdetail_tmp == 0) {
          fieldidList.add("" + fieldid_tmp);
          int htmltype_tmp = 0;
          int type_tmp = 0;
          String dbtype_tmp = "";
          if (isbill == 0) {
            htmltype_tmp = Util.getIntValue(fieldComInfo.getFieldhtmltype("" + fieldid_tmp), 0);
            type_tmp = Util.getIntValue(fieldComInfo.getFieldType("" + fieldid_tmp), 0);
            dbtype_tmp = Util.null2String(fieldComInfo.getFielddbtype("" + fieldid_tmp));
          } else {
            htmltype_tmp = Util.getIntValue(rs.getString("fieldhtmltype"), 0);
            type_tmp = Util.getIntValue(rs.getString("type"), 0);
            dbtype_tmp = Util.null2String(rs.getString("fielddbtype"));
          }
          fieldhtmltypeList.add("" + htmltype_tmp);
          fieldtypeList.add("" + type_tmp);
          fielddbtypeList.add(dbtype_tmp);
          if (htmltype_tmp == 6) {
            if (isbill == 0) {
              otherPara_hs.put("fieldimgwidth" + fieldid_tmp, "" + fieldComInfo.getImgWidth("" + fieldid_tmp));
              otherPara_hs.put("fieldimgheight" + fieldid_tmp, "" + fieldComInfo.getImgHeight("" + fieldid_tmp));
              otherPara_hs.put("fieldimgnum" + fieldid_tmp, "" + fieldComInfo.getImgNumPreRow("" + fieldid_tmp));
            } else {
              otherPara_hs.put("fieldimgwidth" + fieldid_tmp, "" + Util.getIntValue(rs.getString("imgwidth"), 0));
              otherPara_hs.put("fieldimgheight" + fieldid_tmp, "" + Util.getIntValue(rs.getString("imgheight"), 0));
              otherPara_hs.put("fieldimgnum" + fieldid_tmp, "" + Util.getIntValue(rs.getString("textheight"), 0));
            }
          }
        } else {
          detailFieldidList.add("" + fieldid_tmp);
        }
        fieldname_hs.put("fieldname" + fieldid_tmp, fieldname);
        isview_hs.put("isview" + fieldid_tmp, "" + isview_tmp);
        isedit_hs.put("isedit" + fieldid_tmp, "" + isedit_tmp);
        ismand_hs.put("ismand" + fieldid_tmp, "" + ismand_tmp);
        fieldlabel_hs.put("fieldlabel" + fieldid_tmp, labelName_tmp);
      }
      // 把主、明细字段队列也放进去，在单个实现页面字段的JAVA类中，需要判断其他字段的主或明细属性
      otherPara_hs.put("fieldidList", fieldidList);
      otherPara_hs.put("detailFieldidList", detailFieldidList);
      otherPara_hs.put("fieldtypeList", fieldtypeList);
      otherPara_hs.put("isedit_hs", isedit_hs);

      Hashtable inoperatefield_hs = new Hashtable();
      String prjid = Util.null2String(request.getParameter("prjid"));
      String reqid = Util.null2String(request.getParameter("reqid"));
      String docid = Util.null2String(request.getParameter("docid"));
      String hrmid = Util.null2String(request.getParameter("hrmid"));
      String crmid = Util.null2String(request.getParameter("crmid"));
      if (iscreate == 1) {
        // 获取节点前附加操作
        RequestPreAddinoperateManager requestPreAddM = new RequestPreAddinoperateManager();
        requestPreAddM.setCreater(user.getUID());
        requestPreAddM.setOptor(user.getUID());
        requestPreAddM.setWorkflowid(workflowid);
        requestPreAddM.setNodeid(nodeid);
        Hashtable getPreAddRule_hs = requestPreAddM.getPreAddRule();
        inoperatefield_hs = (Hashtable) getPreAddRule_hs.get("inoperatefield_hs");
        fieldvalue_hs = (Hashtable) getPreAddRule_hs.get("inoperatevalue_hs");
      } else {
        // 查每个字段的值
        if (isbill == 0) {
          rs.executeProc("workflow_FieldValue_Select", "" + requestid); // 从workflow_form表中查
          rs.next();
          for (int i = 0; i < fieldidList.size(); i++) {
            int fieldid_tmp = Util.getIntValue((String) fieldidList.get(i));
            if (fieldid_tmp <= 0) {
              continue;
            }
            String fieldname = Util.null2String((String) fieldname_hs.get("fieldname" + fieldid_tmp));
            fieldvalue_hs.put("inoperatevalue" + fieldid_tmp, Util.null2String(rs.getString(fieldname)));// 这里为了和节点前附加操作的一样，所以用“inoperatevalue”作为Key
          }
        } else {
          rs.executeSql("select tablename from workflow_bill where id = " + formid);// 查询工作流单据表的信息
          rs.next();
          billtablename = rs.getString("tablename");
          rs.executeSql("select * from " + billtablename + " where id = " + billid);
          if (rs.next()) {
            for (int i = 0; i < fieldidList.size(); i++) {
              int fieldid_tmp = Util.getIntValue((String) fieldidList.get(i));
              if (fieldid_tmp <= 0) {
                continue;
              }
              String fieldname = Util.null2String((String) fieldname_hs.get("fieldname" + fieldid_tmp));
              fieldvalue_hs.put("inoperatevalue" + fieldid_tmp, Util.null2String(rs.getString(fieldname)));
            }
          }
        }
        rs.execute("select requestname, requestlevel, messagetype,chatstype from workflow_requestbase where requestid=" + requestid);
        if (rs.next()) {
          String requestname_tmp = Util.null2String(rs.getString("requestname"));
          fieldvalue_hs.put("inoperatevalue-1", requestname_tmp);
          int requestlevel_tmp = Util.getIntValue(rs.getString("requestlevel"), 0);
          fieldvalue_hs.put("inoperatevalue-2", "" + requestlevel_tmp);
          int messagetype_tmp = Util.getIntValue(rs.getString("messagetype"), 0);
          fieldvalue_hs.put("inoperatevalue-3", "" + messagetype_tmp);
          int chatstype_tmp = Util.getIntValue(rs.getString("chatstype"), 0);
          fieldvalue_hs.put("inoperatevalue-5", "" + chatstype_tmp);
        }
      }

      SpecialFieldInfo specialFieldInfo = new SpecialFieldInfo();
      RequestDoc requestDoc = new RequestDoc();
      HashMap specialfield = specialFieldInfo.getFormSpecialField();// 特殊字段的字段信息
      otherPara_hs.put("specialfield", specialfield);

      int creater = Util.getIntValue(request.getParameter("creater"), 0);
      int creatertype = Util.getIntValue(request.getParameter("creatertype"), 0);
      String currentdate = Util.null2String(request.getParameter("currentdate"));
      String currenttime = Util.null2String(request.getParameter("currenttime"));

      // CodeBuild cbuild = new CodeBuild(formid);
			CodeBuild cbuild = new CodeBuild(formid,""+isbill,workflowid,creater,creatertype);
      CoderBean cb = cbuild.getFlowCBuild();
      String isUse = cb.getUserUse(); // 是否使用流程编号
      String fieldCode = Util.null2String(cb.getCodeFieldId());
      ArrayList memberList = cb.getMemberList();
      boolean hasHistoryCode = cbuild.hasHistoryCode(rs, workflowid);
      int fieldIdSelect = -1;
      int departmentFieldId = -1;
      int subCompanyFieldId = -1;
      int supSubCompanyFieldId = -1;
      int yearFieldId = -1;
      int yearFieldHtmlType = 0;
      int monthFieldId = -1;
      int dateFieldId = -1;

      for (int i = 0; i < memberList.size(); i++) {
        String[] codeMembers = (String[]) memberList.get(i);
        String codeMemberName = codeMembers[0];
        String codeMemberValue = codeMembers[1];
        if ("22755".equals(codeMemberName)) {
          fieldIdSelect = Util.getIntValue(codeMemberValue, -1);
        } else if ("22753".equals(codeMemberName)) {
          supSubCompanyFieldId = Util.getIntValue(codeMemberValue, -1);
        } else if ("141".equals(codeMemberName)) {
          subCompanyFieldId = Util.getIntValue(codeMemberValue, -1);
        } else if ("124".equals(codeMemberName)) {
          departmentFieldId = Util.getIntValue(codeMemberValue, -1);
        } else if ("445".equals(codeMemberName)) {
          yearFieldId = Util.getIntValue(codeMemberValue, -1);
        } else if ("6076".equals(codeMemberName)) {
          monthFieldId = Util.getIntValue(codeMemberValue, -1);
        } else if ("390".equals(codeMemberName) || "16889".equals(codeMemberName)) {
          dateFieldId = Util.getIntValue(codeMemberValue, -1);
        }
      }

      String codeFields = Util.null2String(cbuild.haveCode());
      otherPara_hs.put("codeFields", codeFields);

      otherPara_hs.put("isUse", isUse);
      otherPara_hs.put("fieldCode", fieldCode);
      otherPara_hs.put("hasHistoryCode", String.valueOf(hasHistoryCode));

      ArrayList flowDocs = requestDoc.getDocFiled("" + workflowid); // 得到流程建文挡的发文号字段
      String codeField = "";
      String flowCat="";
      if (flowDocs != null && flowDocs.size() > 0) {
        codeField = "" + flowDocs.get(0);
        flowCat=""+flowDocs.get(3);	//取得流程中“显示目录”字段ID	
      }
      otherPara_hs.put("codeField", codeField);
      int keywordismand = 0;
      int keywordisedit = 0;
      int titleFieldId = 0;
      int keywordFieldId = 0;
      String workflowname = "";
      int messageType = 0;
      int chatsType = 0;
      rs.execute("select titleFieldId,keywordFieldId,workflowname,messageType,chatsType  from workflow_base where id=" + workflowid);
      if (rs.next()) {
        titleFieldId = Util.getIntValue(rs.getString("titleFieldId"), 0);
        keywordFieldId = Util.getIntValue(rs.getString("keywordFieldId"), 0);
        workflowname = Util.null2String(rs.getString("workflowname"));
        messageType = Util.getIntValue(rs.getString("messageType"), 0);
        chatsType = Util.getIntValue(rs.getString("chatsType"), 0);
      }
      otherPara_hs.put("titleFieldId", "" + titleFieldId);
      otherPara_hs.put("keywordFieldId", "" + keywordFieldId);

      int defaultName = Util.getIntValue(request.getParameter("defaultName"), 0);

      HttpSession session = (HttpSession) request.getSession(false);
      User user = (User) request.getSession(true).getAttribute("weaver_user@bean");
      int isaffirmancebody = Util.getIntValue((String) session.getAttribute(user.getUID() + "_" + requestid + "isaffirmance"), 0);// 是否需要提交确认
      int reEditbody = Util.getIntValue((String) session.getAttribute(user.getUID() + "_" + requestid + "reEdit"), 0);// 是否需要提交确认
      int isviewonly = 0;
      if (isremark != 0 || nodetype == 3 || isprint == 1) {
        isviewonly = 1;
      }
        if(null!=isCanModify && "true".equals(isCanModify)){
            isviewonly = 0;
        }
      int mustNoEdit = 0;
      if (isaffirmancebody == 1 && reEditbody == 0) {
        mustNoEdit = 1;
        isviewonly = 0;
      }
      otherPara_hs.put("isviewonly", "" + isviewonly);
      otherPara_hs.put("mustNoEdit", "" + mustNoEdit);
      // 获得触发字段名
      DynamicDataInput ddi = new DynamicDataInput("" + workflowid);
      String trrigerfield = ddi.GetEntryTriggerFieldName();
      ArrayList selfieldsadd = wfLinkageInfo.getSelectField(workflowid, nodeid, 0);
      ArrayList changefieldsadd = wfLinkageInfo.getChangeField(workflowid, nodeid, 0);
      otherPara_hs.put("trrigerfield", trrigerfield);
      otherPara_hs.put("selfieldsadd", selfieldsadd);
      otherPara_hs.put("changefieldsadd", changefieldsadd);

      getFieldAttr();// 取出字段取值属性，放到otherPara_hs里，同时拼必要的页面JS方法
      // 在这里先把明细字段的标签都处理掉
      for (int i = 0; i < detailFieldidList.size(); i++) {
        int fieldid_tmp = Util.getIntValue((String) detailFieldidList.get(i));
        // 替换字段显示名。这里如果找到就替换，如果找不到就不处理
        String fieldlabel_tmp = Util.null2String((String) fieldlabel_hs.get("fieldlabel" + fieldid_tmp));
        String content1 = "";
        String content2 = "";
        int pos = htmlLayout.toLowerCase().indexOf("$label" + fieldid_tmp + "$");
        while (pos > -1) {
          content1 = wfformhtml.substring(0, pos);
          content2 = wfformhtml.substring(pos + 1);
          int pos1 = content1.lastIndexOf("<");
          int pos2 = content2.indexOf(">");
          if (pos1 > -1) {
            content1 = content1.substring(0, pos1);
          }
          if (pos2 > -1) {
            content2 = content2.substring(pos2 + 1);
          }
          wfformhtml = content1 + fieldlabel_tmp + content2;
          htmlLayout = wfformhtml;
          pos = htmlLayout.toLowerCase().indexOf("$label" + fieldid_tmp + "$");
        }
      }
      // 为防字段循环顺序问题，把找被代理人的方法提前
      int beagenter = user.getUID();
	  String beagenter2 = ""+user.getUID();
      // 获得被代理人
      rs.executeSql("select agentorbyagentid from workflow_currentoperator where usertype=0 and isremark='0' and requestid=" + requestid + " and userid=" + user.getUID() + " and nodeid=" + nodeid + " order by id desc");
      if (rs.next()) {
        int tembeagenter = rs.getInt(1);
        if (tembeagenter > 0) {
          beagenter = tembeagenter;
          beagenter2 =""+tembeagenter;
        }
      }
	  int body_isagent=Util.getIntValue((String)session.getAttribute(workflowid+"isagent"+user.getUID()),0);
      //QC51102
	  if(requestid <= 0) {
      	  	//获得被代理人
			//int body_isagent=Util.getIntValue((String)session.getAttribute(workflowid+"isagent"+user.getUID()),0);
			if(body_isagent==1){
			    beagenter=Util.getIntValue((String)session.getAttribute(workflowid+"beagenter"+user.getUID()),0);
				beagenter2=""+Util.getIntValue((String)session.getAttribute(workflowid+"beagenter"+user.getUID()),0);
			}
      }
      session.removeAttribute("beagenter_" + user.getUID());
      session.setAttribute("beagenter_" + user.getUID(), "" + beagenter);
      otherPara_hs.put("beagenter", "" + beagenter);
      otherPara_hs.put("httprequest", request);
		otherPara_hs.put("requestid", ""+requestid);
		otherPara_hs.put("userid", ""+user.getUID());
      ArrayList fckfieldidList = new ArrayList();
      String newfromdate = "a";
      String newenddate = "b";
      // 开始根据字段id队列循环解析
      HtmlElement object = null;
      for (int i = 0; i < fieldidList.size(); i++) {
        int fieldid_tmp = Util.getIntValue((String) fieldidList.get(i));
        String fieldname_tmp = Util.null2String((String) fieldname_hs.get("fieldname" + fieldid_tmp));
        int fieldhtmltype_tmp = Util.getIntValue((String) fieldhtmltypeList.get(i), 0);
        int type_tmp = Util.getIntValue((String) fieldtypeList.get(i));
        int groupid_tmp = 0;
        int isview_tmp = Util.getIntValue((String) isview_hs.get("isview" + fieldid_tmp), 0);
        int isedit_tmp = Util.getIntValue((String) isedit_hs.get("isedit" + fieldid_tmp), 0);
        int ismand_tmp = Util.getIntValue((String) ismand_hs.get("ismand" + fieldid_tmp), 0);
        if (mustNoEdit == 1) {
          isedit_tmp = 0;
          ismand_tmp = 0;
        }
        String fielddbtype_tmp = Util.null2String((String) fielddbtypeList.get(i));

        int tmpmanagerid = 0;
        if (fieldname_tmp.equals("manager") && currentnodetype != 3) {// manager字段
          tmpmanagerid = Util.getIntValue(resourceComInfo.getManagerID("" + beagenter), 0);
          if (isview_tmp == 0) {
            htmlHiddenElementsb.append("\n").append("<input type=\"hidden\" id=\"field" + fieldid_tmp + "\" name=\"field" + fieldid_tmp + "\" value=\"" + tmpmanagerid + "\">").append("\n");
          }
          isedit_tmp = 0;
          ismand_tmp = 0;
          // continue;
        }

        otherPara_hs.put("fielddbtype", fielddbtype_tmp);
        if (fieldname_tmp.equals("begindate")) {
          newfromdate = "field" + fieldid_tmp; // 开始日期,主要为开始日期不大于结束日期进行比较
        }
        if (fieldname_tmp.equals("enddate")) {
          newenddate = "field" + fieldid_tmp; // 结束日期,主要为开始日期不大于结束日期进行比较
        }
        if (fieldid_tmp == keywordFieldId) {
          keywordismand = ismand_tmp;
          keywordisedit = isedit_tmp;
        }
        if (("" + yearFieldId).equals("" + fieldid_tmp)) {
          yearFieldHtmlType = fieldhtmltype_tmp;
        }

        int isdetail_tmp = 0;
        int fieldlength_tmp = 0;
        int fieldheight_tmp = 4;
        if (fieldhtmltype_tmp == 1 && type_tmp == 1) {// 单文本中的文本
          if ((fielddbtype_tmp.toLowerCase()).indexOf("varchar") > -1) {
            fieldlength_tmp = Util.getIntValue(fielddbtype_tmp.substring(fielddbtype_tmp.indexOf("(") + 1, fielddbtype_tmp.length() - 1));
          }
        } else if (fieldhtmltype_tmp == 2) {// 多行文本框
          if (isbill == 0) {
            rs.execute("select textheight from workflow_formdict where id=" + fieldid_tmp);
          } else {
            rs.execute("select textheight from workflow_billfield where id=" + fieldid_tmp + " and billid=" + formid);
          }
          if (rs.next()) {
            fieldheight_tmp = Util.getIntValue(rs.getString("textheight"), 4);
            otherPara_hs.put("fieldheight", "" + fieldheight_tmp);
          }
        }

        // 记录必填标志
        if (ismand_tmp == 1 && !codeField.equals("" + fieldid_tmp)) {
          if (fieldid_tmp > 0) {
            needcheck += (",field" + fieldid_tmp);
          } else {
            if (fieldid_tmp == -1) {
              needcheck += (",requestname");
            }
          }
        } else if (fieldid_tmp == -1 && isedit_tmp == 1) {
          needcheck += (",requestname");
          ismand_tmp = 1;
        }

        String fieldlabel_tmp = Util.null2String((String) fieldlabel_hs.get("fieldlabel" + fieldid_tmp));
        String fieldvalue_tmp = Util.null2String((String) fieldvalue_hs.get("inoperatevalue" + fieldid_tmp));
        String inoperatefield_tmp = Util.null2String((String) inoperatefield_hs.get("inoperatefield" + fieldid_tmp));
        if (!"1".equals(inoperatefield_tmp) && iscreate == 1) {// 没有设置节点前附加操作
                                                               // ，并且是创建时
          if (fieldhtmltype_tmp == 3) {
            if ((type_tmp == 8 || type_tmp == 135) && !prjid.equals("")) { // 浏览按钮为项目,从前面的参数中获得项目默认值
              fieldvalue_tmp = "" + Util.getIntValue(prjid, 0);
            } else if ((type_tmp == 9 || type_tmp == 37) && !docid.equals("")) { // 浏览按钮为文档,从前面的参数中获得文档默认值
              fieldvalue_tmp = "" + Util.getIntValue(docid, 0);
            } else if ((type_tmp == 1 || type_tmp == 17 || type_tmp == 165 || type_tmp == 166) && !hrmid.equals("") && body_isagent!=1) { // 浏览按钮为人,从前面的参数中获得人默认值
              fieldvalue_tmp = "" + Util.getIntValue(hrmid, 0);
            }else if ((type_tmp == 1 || type_tmp == 17 || type_tmp == 165 || type_tmp == 166) && !hrmid.equals("") && body_isagent==1) { // 代理，浏览按钮为人,从前面的参数中获得人默认值
              fieldvalue_tmp = "" + Util.getIntValue(beagenter2, 0);
            } else if ((type_tmp == 7 || type_tmp == 18) && !crmid.equals("")) { // 浏览按钮为CRM,从前面的参数中获得CRM默认值
              fieldvalue_tmp = "" + Util.getIntValue(crmid, 0);
            } else if ((type_tmp == 16 || type_tmp == 152 || type_tmp == 171) && !reqid.equals("")) { // 浏览按钮为REQ,从前面的参数中获得REQ默认值
              fieldvalue_tmp = "" + Util.getIntValue(reqid, 0);
            } else if ((type_tmp == 4 || type_tmp == 57 || type_tmp == 167 || type_tmp == 168) && !hrmid.equals("") && body_isagent!=1) { // 浏览按钮为部门,从前面的参数中获得人默认值(由人力资源的部门得到部门默认值)
              fieldvalue_tmp = "" + Util.getIntValue(resourceComInfo.getDepartmentID(hrmid), 0);
            }else if ((type_tmp == 4 || type_tmp == 57 || type_tmp == 167 || type_tmp == 168) && !hrmid.equals("") && body_isagent==1) { // 代理，浏览按钮为部门,从前面的参数中获得人默认值(由人力资源的部门得到部门默认值)
              fieldvalue_tmp = "" + Util.getIntValue(resourceComInfo.getDepartmentID(beagenter2), 0);
            } else if (type_tmp == 24 && !hrmid.equals("")) { // 浏览按钮为职务,从前面的参数中获得人默认值(由人力资源的职务得到职务默认值)
              fieldvalue_tmp = "" + Util.getIntValue(resourceComInfo.getJobTitle(hrmid), 0);
            } else if (type_tmp == 32 && !hrmid.equals("")) { // 浏览按钮为职务,从前面的参数中获得人默认值(由人力资源的职务得到职务默认值)
              fieldvalue_tmp = "" + Util.getIntValue(request.getParameter("TrainPlanId"), 0);
            } else if ((type_tmp == 164 || type_tmp == 169 || type_tmp == 170) && !hrmid.equals("") && body_isagent!=1 ) { // 浏览按钮为分部,从前面的参数中获得人默认值(由人力资源的分部得到分部默认值)
              fieldvalue_tmp = "" + Util.getIntValue(resourceComInfo.getSubCompanyID(hrmid), 0);
            }else if ((type_tmp == 164 || type_tmp == 169 || type_tmp == 170) && !hrmid.equals("") && body_isagent==1 ) { // 代理，浏览按钮为分部,从前面的参数中获得人默认值(由人力资源的分部得到分部默认值)
              fieldvalue_tmp = "" + Util.getIntValue(resourceComInfo.getSubCompanyID(beagenter2), 0);
            } else if (type_tmp == 2) {// 日期
              fieldvalue_tmp = TimeUtil.getCurrentDateString();
            } else if (type_tmp == 19) {// 时间
              fieldvalue_tmp = TimeUtil.getCurrentTimeString().substring(11, 16);
            }
          }
        }

        //qc 67594 yl
//          if (fieldname_tmp.equals("manager") && currentnodetype != 3) {
//              fieldvalue_tmp = "" + fieldvalue_tmp;
//          }
          boolean mgrflg = false;
          if(requestid>0){
              String sssqq = "select isremark,isreminded,preisremark,id,groupdetailid,nodeid from workflow_currentoperator where requestid=" + requestid + " and userid=" + user.getUID() + " and usertype=0" + " order by isremark,id";
              rs.executeSql(sssqq);
              while (rs.next()) {
                  isremark = Util.getIntValue(rs.getString("isremark"), -1);
                  if (isremark == 1 || isremark == 5 || isremark == 7 || isremark == 9 || (isremark == 0 && currentnodetype != 3)) {
                      mgrflg = true;
                      break;
                  }
              }
          }
          if("manager".equals(fieldname_tmp)){
              //非创建流程的情况
              if(requestid > 0){
                  if(mgrflg){
                      if(beagenter!= user.getUID()){
                    	  if(isremark!=1&&isremark!=8&&isremark!=9){
                          fieldvalue_tmp = resourceComInfo.getManagerID(String.valueOf(beagenter));
                    	                                            }
                    	  }else{
                    	  if(isremark!=1&&isremark!=8&&isremark!=9){	  
                          fieldvalue_tmp = resourceComInfo.getManagerID(String.valueOf(user.getUID()));
                    	                                             }
                    	  }
                  }
                  //创建流程时候拿当前操作者的直接上级(代理时还是要考虑的)
              } else {
					if(beagenter!= user.getUID()){
						if(isremark!=1&&isremark!=8&&isremark!=9){
                          fieldvalue_tmp = resourceComInfo.getManagerID(String.valueOf(beagenter));
						                                         }
						}else{
							if(isremark!=1&&isremark!=8&&isremark!=9){
                          fieldvalue_tmp = resourceComInfo.getManagerID(String.valueOf(user.getUID()));
							                                       }
							}
              }
          }
          //qc 67594 yl end

          if(flowCat.equals("" + fieldid_tmp)){
        	htmlHiddenElementsb.append("\n").append("<input type=\"hidden\" id=\"oldfield"+fieldid_tmp+"\" name=\"oldfield"+fieldid_tmp+"\" value=\"" + fieldvalue_tmp + "\">" + "\n");
        }
        
        String content1 = "";
        String content2 = "";
        // 替换字段显示名。这里如果找到就替换，如果找不到就不处理
        int pos = htmlLayout.toLowerCase().indexOf("$label" + fieldid_tmp + "$");
        while (pos > -1) {
          content1 = wfformhtml.substring(0, pos);
          content2 = wfformhtml.substring(pos + 1);
          int pos1 = content1.lastIndexOf("<");
          int pos2 = content2.indexOf(">");
          if (pos1 > -1) {
            content1 = content1.substring(0, pos1);
          }
          if (pos2 > -1) {
            content2 = content2.substring(pos2 + 1);
          }
          wfformhtml = content1 + fieldlabel_tmp + content2;
          htmlLayout = wfformhtml;
          pos = htmlLayout.toLowerCase().indexOf("$label" + fieldid_tmp + "$");
        }
        // 替换字段
        pos = htmlLayout.toLowerCase().indexOf("$field" + fieldid_tmp + "$");
        if (pos > -1) {
          content1 = wfformhtml.substring(0, pos);
          content2 = wfformhtml.substring(pos + 1);
          int pos1 = content1.lastIndexOf("<");
          int pos2 = content2.indexOf(">");
          if (pos1 > -1) {
            content1 = content1.substring(0, pos1);
          }
          if (pos2 > -1) {
            content2 = content2.substring(pos2 + 1);
          }
          isview_tmp = 1;
          if (fieldid_tmp > 0) {
            String inputStr_tmp = "";
            if (fieldhtmltype_tmp == 2 && type_tmp == 2 && (isedit_tmp == 0 || isviewonly == 1)) {// 有Fck字段，并且显示且不能编辑
              fckfieldidList.add("FCKiframe" + fieldid_tmp);
              inputStr_tmp = "<input type='hidden' id='FCKiframefieldid' value='FCKiframe"+fieldid_tmp+"'/>";
            }
            try {
              object = (HtmlElement) Class.forName(fieldTypeComInfo.getClassname("" + fieldhtmltype_tmp)).newInstance();
              Hashtable ret_hs = object.getHtmlElementString(fieldid_tmp, fieldname_tmp, type_tmp, fieldlabel_tmp, fieldlength_tmp, isdetail_tmp, groupid_tmp, fieldvalue_tmp, isviewonly, 1, isedit_tmp, ismand_tmp, user, otherPara_hs);
              inputStr_tmp += Util.null2String((String) ret_hs.get("inputStr"));

              wfformhtml = content1 + inputStr_tmp + content2;
              htmlLayout = wfformhtml;
              String jsStr_t = Util.null2String((String) ret_hs.get("jsStr"));
              jsStr.append("\n").append(jsStr_t).append("\n");
            } catch (Exception e) {
              writeLog(e);
            }
          } else {//流程标题、紧急程度、短信提醒、签字意见4个特殊字段
            Hashtable ret_hs = new Hashtable();
            systemElement.setIsviewonly(isviewonly);
            systemElement.setIsview(isview_tmp);
            systemElement.setIsedit(isedit_tmp);
            systemElement.setIsmand(ismand_tmp);
            systemElement.setLanguageid(user.getLanguage());
            systemElement.setOtherPara(otherPara_hs);
            systemElement.setFieldvalue(fieldvalue_tmp);
            if (fieldid_tmp == -1) {
              if (isview_tmp == 0 && isviewonly == 0) {// 保证流程标题不空。如果不显示，就存默认值
                defaultName = 1;
              }
              if (defaultName == 1 && iscreate == 1 && body_isagent !=1) {
                fieldvalue_tmp = Util.toScreenToEdit(workflowname + "-" + user.getLastname() + "-" + TimeUtil.getCurrentDateString(), user.getLanguage());
                systemElement.setFieldvalue(fieldvalue_tmp);
              }
			  if (defaultName == 1 && iscreate == 1 && body_isagent ==1) {	
				String usernameagent="";
				usernameagent=resourceComInfo.getLastname(beagenter2);
                fieldvalue_tmp = Util.toScreenToEdit(workflowname + "-" + usernameagent + "-" + TimeUtil.getCurrentDateString(), user.getLanguage());
                systemElement.setFieldvalue(fieldvalue_tmp);
              }
              ret_hs = systemElement.getRequestName();
            } else if (fieldid_tmp == -2) {
              ret_hs = systemElement.getRequestLevel();
            } else if (fieldid_tmp == -3) {
			  if("".equals(fieldvalue_tmp))systemElement.setFieldvalue(smsAlertsType);
              ret_hs = systemElement.getMessageType(messageType);
            }else if(fieldid_tmp == -4){
				boolean IsBeForwardCanSubmitOpinion = "true".equals(session.getAttribute(user.getUID()+"_"+requestid+"IsBeForwardCanSubmitOpinion"))?true:false;
				boolean IsCanSubmit = "true".equals(session.getAttribute(user.getUID()+"_"+requestid+"IsCanSubmit"))?true:false;
				boolean issignView = false;
				
				if (isremark == 1 || isremark == 9 ||isremark==7) {
					issignView = true;
				}else if(isremark == 0 && IsCanSubmit){
					issignView = true;
				}
				if (Util.getIntValue(request.getParameter("isremark"), 0) == 2) {
					issignView = false;
				}
				
				if((isviewonly!=1 || issignView) && (IsBeForwardCanSubmitOpinion || iscreate==1)){
					
					otherPara_hs.put("hasRemark", "1");
					RecordSet rrs = new RecordSet();
					
					String isFormSignature = null;
					rrs.executeSql("select isFormSignature from workflow_flownode where workflowId="+workflowid+" and nodeId="+nodeid);
					if(rrs.next()){
						isFormSignature = Util.null2String(rrs.getString("isFormSignature"));
					}
					
					int isUseWebRevision_t = Util.getIntValue(new weaver.general.BaseBean().getPropValue("weaver_iWebRevision","isUseWebRevision"), 0);
					if(isUseWebRevision_t != 1){
						isFormSignature = "";
					}
					
					if("1".equals(isFormSignature)){
						ret_hs = new Hashtable();
						ret_hs.put("inputStr", WFLayoutToHtml.HTML_FORMSIGNATURE_PLACEHOLDER);
					} else {
						rrs.execute("select issignmustinput from workflow_flownode where nodeid="+nodeid);
						if(rrs.next()){
							int issignmustinput_ = Util.getIntValue(rrs.getString("issignmustinput"));
							if(issignmustinput_ == 1){
								ismand_tmp = 1;
							}
						}
						String annexdocids = "" ;
						String signdocids="";
						String signworkflowids="";
						rrs.executeProc("workflow_RequestLog_SBUser", ""+requestid+Util.getSeparator()+""+user.getUID()+Util.getSeparator()+""+0+Util.getSeparator()+"1");
						if(rrs.next()){
							fieldvalue_tmp = Util.null2String(rrs.getString("remark"));
							annexdocids = Util.null2String(rrs.getString("annexdocids"));
							signdocids = Util.null2String(rrs.getString("signdocids"));
							signworkflowids = Util.null2String(rrs.getString("signworkflowids"));
						}
						boolean isSuccess  = rrs.executeProc("sysPhrase_selectByHrmId",""+user.getUID());
						String workflowPhrases[] = new String[rrs.getCounts()];
						String workflowPhrasesContent[] = new String[rrs.getCounts()];
						int x = 0;
						if (isSuccess) {
							while (rrs.next()){
								workflowPhrases[x] = Util.null2String(rrs.getString("phraseShort"));
								workflowPhrasesContent[x] = Util.toHtml(Util.null2String(rrs.getString("phrasedesc")));
								x++;
							}
						}
						String isannexupload_edit="";
						String isSignDoc_edit="";
						String isSignWorkflow_edit="";
						String annexdocCategory_edit = "";
						rrs.execute("select isannexupload,isSignDoc,isSignWorkflow, annexdocCategory from workflow_base where id="+workflowid);
						if(rrs.next()){
							isannexupload_edit=Util.null2String(rrs.getString("isannexupload"));
							isSignDoc_edit=Util.null2String(rrs.getString("isSignDoc"));
							isSignWorkflow_edit=Util.null2String(rrs.getString("isSignWorkflow"));
							annexdocCategory_edit=Util.null2String(rrs.getString("annexdocCategory"));
						}
						otherPara_hs.put("annexdocids", annexdocids);
						otherPara_hs.put("signdocids", signdocids);
						otherPara_hs.put("signworkflowids", signworkflowids);
						otherPara_hs.put("workflowPhrases", workflowPhrases);
						otherPara_hs.put("workflowPhrasesContent", workflowPhrasesContent);
						otherPara_hs.put("isannexupload_edit", isannexupload_edit);
						otherPara_hs.put("isSignDoc_edit", isSignDoc_edit);
						otherPara_hs.put("isSignWorkflow_edit", isSignWorkflow_edit);
						otherPara_hs.put("annexdocCategory_edit", annexdocCategory_edit);
						systemElement.setIsedit(1);
						systemElement.setIsmand(ismand_tmp);
						systemElement.setFieldvalue(fieldvalue_tmp);
						systemElement.setOtherPara(otherPara_hs);
						ret_hs = systemElement.getRemark();
					}
				}
            }else if (fieldid_tmp == -5) {
			  if("".equals(fieldvalue_tmp))systemElement.setFieldvalue(chatsAlertType);
              ret_hs = systemElement.getChatsType(chatsType);
            }
            String inputStr_tmp = Util.null2String((String) ret_hs.get("inputStr"));
            wfformhtml = content1 + inputStr_tmp + content2;
            htmlLayout = wfformhtml;
          }
        } else {
          // 如果是3个系统字段，则需要放一个隐藏的input
          if (fieldid_tmp == -1) {
            if (iscreate == 1 && "".equals(fieldvalue_tmp)) {
              fieldvalue_tmp = Util.null2String(workflowname + "-" + user.getLastname() + "-" + TimeUtil.getCurrentDateString());
            }
            htmlHiddenElementsb.append("\n").append("<input type=\"hidden\" id=\"requestname\" name=\"requestname\" value=\"" + Util.toScreenToEdit(fieldvalue_tmp, user.getLanguage()) + "\">" + "\n");
          } else if (fieldid_tmp == -2) {
            htmlHiddenElementsb.append("\n").append("<input type=\"hidden\" id=\"requestlevel\" name=\"requestlevel\" value=\"" + fieldvalue_tmp + "\">" + "\n");
          }else if(fieldid_tmp == -4){
			hasRemark = false;
		  } 
		  else if (fieldid_tmp == -3) {
            htmlHiddenElementsb.append("\n").append("<input type=\"hidden\" id=\"messageType\" name=\"messageType\" value=\"" + fieldvalue_tmp + "\">" + "\n");
          }  else if (fieldid_tmp == -5) {
            htmlHiddenElementsb.append("\n").append("<input type=\"hidden\" id=\"chatsType\" name=\"chatsType\" value=\"" + fieldvalue_tmp + "\">" + "\n");
          }else {
            //htmlHiddenElementsb.append("\n").append("<input type=\"hidden\" id=\"field" + fieldid_tmp + "\" name=\"field" + fieldid_tmp + "\" value=\"" + fieldvalue_tmp + "\">" + "\n");
			if(fieldhtmltype_tmp==2 && type_tmp==2){//多行文本框处理
				htmlHiddenElementsb.append("\n").append("<input type=\"hidden\" id=\"field" + fieldid_tmp + "\" name=\"field" + fieldid_tmp + "\" value=\"" + Util.toHtmltextarea(Util.encodeAnd(fieldvalue_tmp)) + "\">" + "\n");
			} else {
				htmlHiddenElementsb.append("\n").append("<input type=\"hidden\" id=\"field" + fieldid_tmp + "\" name=\"field" + fieldid_tmp + "\" value=\"" + fieldvalue_tmp + "\">" + "\n");
			}
          }
        }
      }
      // 拼JS方法
      jsStr.append("\n");
      jsStr.append("function checktimeok(){<!-- 结束日期不能小于开始日期 -->").append("\n");
      jsStr.append("\tif (\"" + newenddate + "\"!=\"b\" && \"" + newfromdate + "\"!=\"a\" && document.frmmain." + newenddate + ".value!=\"\"){").append("\n");
      jsStr.append("\t\tYearFrom=document.frmmain." + newfromdate + ".value.substring(0,4);").append("\n");
      jsStr.append("\t\tMonthFrom=document.frmmain." + newfromdate + ".value.substring(5,7);").append("\n");
      jsStr.append("\t\tDayFrom=document.frmmain." + newfromdate + ".value.substring(8,10);").append("\n");
      jsStr.append("\t\tYearTo=document.frmmain." + newenddate + ".value.substring(0,4);").append("\n");
      jsStr.append("\t\tMonthTo=document.frmmain." + newenddate + ".value.substring(5,7);").append("\n");
      jsStr.append("\t\tDayTo=document.frmmain." + newenddate + ".value.substring(8,10);").append("\n");
      jsStr.append("\t\tif(!DateCompare(YearFrom, MonthFrom, DayFrom,YearTo, MonthTo,DayTo )){").append("\n");
      jsStr.append("\t\t\twindow.alert(\"" + SystemEnv.getHtmlLabelName(15273, user.getLanguage()) + "\");").append("\n");
      jsStr.append("\t\t\treturn false;").append("\n");
      jsStr.append("\t\t}").append("\n");
      jsStr.append("\t}").append("\n");
      jsStr.append("\t\treturn true;").append("\n");
      jsStr.append("}").append("\n");

      jsStr.append("\n");
      jsStr.append("function changeKeyword(){").append("\n");
      if (titleFieldId > 0 && keywordFieldId > 0) {
        jsStr.append("\tvar titleObj=$G(\"field" + titleFieldId + "\");").append("\n");
        jsStr.append("\tvar keywordObj=$G(\"field" + keywordFieldId + "\");").append("\n");
        jsStr.append("\tif(titleObj!=null&&keywordObj!=null){").append("\n");
        jsStr.append("\t\t$G(\"workflowKeywordIframe\").src=\"/docs/sendDoc/WorkflowKeywordIframe.jsp?operation=UpdateKeywordData&docTitle=\"+titleObj.value+\"&docKeyword=\"+keywordObj.value;").append("\n");
        jsStr.append("\t}").append("\n");
      } else if (titleFieldId == -3 && keywordFieldId > 0) {
        jsStr.append("\tvar titleObj=$G(\"requestname\");").append("\n");
        jsStr.append("\tvar keywordObj=$G(\"field" + keywordFieldId + "\");").append("\n");
        jsStr.append("\tif(titleObj!=null&&keywordObj!=null){").append("\n");
        jsStr.append("\t$G(\"workflowKeywordIframe\").src=\"/docs/sendDoc/WorkflowKeywordIframe.jsp?operation=UpdateKeywordData&docTitle=\"+titleObj.value+\"&docKeyword=\"+keywordObj.value;").append("\n");
        jsStr.append("\t}").append("\n");
      }
      jsStr.append("}").append("\n");

      jsStr.append("\n");
      jsStr.append("function updateKeywordData(strKeyword){").append("\n");
      if (keywordFieldId > 0) {
        jsStr.append("\tvar keywordObj=$G(\"field" + keywordFieldId + "\");").append("\n");
        jsStr.append("\tvar keywordismand=" + keywordismand + ";").append("\n");
        jsStr.append("\tvar keywordisedit=" + keywordisedit + ";").append("\n");
        jsStr.append("\tif(keywordObj!=null){").append("\n");
        jsStr.append("\t\tif(keywordisedit==1){").append("\n");
        jsStr.append("\t\t\tkeywordObj.value=strKeyword;").append("\n");
        jsStr.append("\t\t\tif(keywordismand==1){").append("\n");
        jsStr.append("\t\t\t\tcheckinput('field" + keywordFieldId + "','field" + keywordFieldId + "span');").append("\n");
        jsStr.append("\t\t\t}").append("\n");
        jsStr.append("\t\t}else{").append("\n");
        jsStr.append("\t\t\tkeywordObj.value=strKeyword;").append("\n");
        jsStr.append("\t\t\tfield" + keywordFieldId + "span.innerHTML=strKeyword;").append("\n");
        jsStr.append("\t\t}").append("\n");
        jsStr.append("\t}").append("\n");
      }
      jsStr.append("}").append("\n");

      jsStr.append("\n");
      if (iscreate == 1 && titleFieldId == -3 && keywordFieldId > 0) {
        jsStr.append("changeKeyword();").append("\n");
      }

      jsStr.append("\n");
      jsStr.append("function onShowKeyword(isbodymand){").append("\n");
      if (keywordFieldId > 0) {
		char setSeparator = Util.getSeparator();
    	char setSeparator_temp = Util.getSeparator_temp();
        jsStr.append("\tvar keywordObj=$G(\"field" + keywordFieldId + "\");").append("\n");
		 jsStr.append("\tvar getSeparator= \""+setSeparator+"\";").append("\n");
        jsStr.append("\tvar getSeparator_temp= \""+ setSeparator_temp +"\";").append("\n");
        jsStr.append("\tif(keywordObj!=null){").append("\n");
        jsStr.append("\t\tstrKeyword=keywordObj.value;").append("\n");
		 jsStr.append("\t\tstrKeyword=strKeyword.replace(/%/g,getSeparator);").append("\n");
        jsStr.append("\t\tstrKeyword=strKeyword.replace(/\"/g,getSeparator_temp);").append("\n");
        jsStr.append("\t\ttempUrl=escape(\"/docs/sendDoc/WorkflowKeywordBrowserMulti.jsp?strKeyword=\"+strKeyword);").append("\n");
        jsStr.append("\t\ttempUrl=tempUrl.replace(/%A0/g,'%20');").append("\n");
        jsStr.append("\t\treturnKeyword=window.showModalDialog(\"/systeminfo/BrowserMain.jsp?url=\"+tempUrl);").append("\n");

        jsStr.append("\t\tif(typeof(returnKeyword)!=\"undefined\"){").append("\n");
        jsStr.append("\t\t\tkeywordObj.value=returnKeyword;").append("\n");
        jsStr.append("\t\t\tif(isbodymand==1){").append("\n");
        jsStr.append("\t\t\t\tcheckinput('field" + keywordFieldId + "','field" + keywordFieldId + "span');").append("\n");
        jsStr.append("\t\t\t}").append("\n");
        jsStr.append("\t\t}").append("\n");
        jsStr.append("\t}").append("\n");
      }
      jsStr.append("}").append("\n");

      ArrayList currentdateList = Util.TokenizerString(currentdate, "-");
      int departmentId = Util.getIntValue(resourceComInfo.getDepartmentID("" + creater), -1);
      DepartmentComInfo departmentComInfo = new DepartmentComInfo();
      int subCompanyId = Util.getIntValue(departmentComInfo.getSubcompanyid1("" + departmentId), -1);
      SubCompanyComInfo subCompanyComInfo = new SubCompanyComInfo();
      int supSubCompanyId = Util.getIntValue(subCompanyComInfo.getSupsubcomid("" + subCompanyId), -1);
      if (supSubCompanyId <= 0) {
        supSubCompanyId = subCompanyId;// 若上级分部为空，则认为上级分部为分部
      }

      jsStr.append("var workflowId=" + workflowid + ";\n");
      jsStr.append("var formId=" + formid + ";\n");
      jsStr.append("var isBill=" + isbill + ";\n");
      jsStr.append("var yearId=-1;\n");
      jsStr.append("var monthId=-1;\n");
      jsStr.append("var dateId=-1;\n");
      jsStr.append("var fieldId=-1;\n");
      jsStr.append("var fieldValue=-1;\n");
      jsStr.append("var supSubCompanyId=-1;\n");
      jsStr.append("var subCompanyId=-1;\n");
      jsStr.append("var departmentId=-1;\n");
      jsStr.append("var recordId=-1;\n");

      jsStr.append("var yearFieldValue=-1;\n");
      jsStr.append("var yearFieldHtmlType=-1;\n");
      jsStr.append("var monthFieldValue=-1;\n");
      jsStr.append("var dateFieldValue=-1;\n");

      jsStr.append("function initDataForWorkflowCode(){\n");
      jsStr.append("\tyearId=-1;\n");
      jsStr.append("\tmonthId=-1;\n");
      jsStr.append("\tdateId=-1;\n");
      jsStr.append("\tfieldId=-1;\n");
      jsStr.append("\tfieldValue=-1;\n");
      jsStr.append("\tsupSubCompanyId=-1;\n");
      jsStr.append("\tsubCompanyId=-1;\n");
      jsStr.append("\tdepartmentId=-1;\n");
      jsStr.append("\trecordId=-1;\n");

      jsStr.append("\tyearFieldValue=-1;\n");
      jsStr.append("\tyearFieldHtmlType=" + yearFieldHtmlType + ";\n");
      jsStr.append("\tmonthFieldValue=-1;\n");
      jsStr.append("\tdateFieldValue=-1;\n");

      jsStr.append("\tif($G(\"field" + yearFieldId + "\")!=null){\n");
      jsStr.append("\t\tif(yearFieldHtmlType==5){//年份为下拉框\n");
      jsStr.append("\t\t\ttry{\n");
      jsStr.append("\t\t\t\tobjYear=$G(\"field" + yearFieldId + "\");\n");
      jsStr.append("\t\t\t\tyearId=objYear.options[objYear.selectedIndex].text;\n");
      jsStr.append("\t\t\t}catch(e){\n");
      jsStr.append("\t\t\t}\n");
      jsStr.append("\t\t}else{\n");
      jsStr.append("\t\t\tyearFieldValue=$G(\"field" + yearFieldId + "\").value;\n");
      jsStr.append("\t\t\tif(yearFieldValue.indexOf(\"-\")>0){\n");
      jsStr.append("\t\t\t\tvar yearFieldValueArray = yearFieldValue.split(\"-\") ;\n");
      jsStr.append("\t\t\t\tif(yearFieldValueArray.length>=1){\n");
      jsStr.append("\t\t\t\t\tyearId=yearFieldValueArray[0];\n");
      jsStr.append("\t\t\t\t}\n");
      jsStr.append("\t\t\t}else{\n");
      jsStr.append("\t\t\t\tyearId=yearFieldValue;\n");
      jsStr.append("\t\t\t}\n");
      jsStr.append("\t\t}\n");
      jsStr.append("\t}\n");

      jsStr.append("\tif($G(\"field" + monthFieldId + "\")!=null){\n");
      jsStr.append("\t\tmonthFieldValue=$G(\"field" + monthFieldId + "\").value;\n");
      jsStr.append("\t\tif(monthFieldValue.indexOf(\"-\")>0){\n");
      jsStr.append("\t\t\tvar monthFieldValueArray = monthFieldValue.split(\"-\") ;\n");
      jsStr.append("\t\t\tif(monthFieldValueArray.length>=2){\n");
      jsStr.append("\t\t\t\tyearId=monthFieldValueArray[0];\n");
      jsStr.append("\t\t\t\tmonthId=monthFieldValueArray[1];\n");
      jsStr.append("\t\t\t}\n");
      jsStr.append("\t\t}\n");
      jsStr.append("\t}\n");

      jsStr.append("\tif($G(\"field" + dateFieldId + "\")!=null){\n");
      jsStr.append("\t\tdateFieldValue=$G(\"field" + dateFieldId + "\").value;\n");
      jsStr.append("\t\tif(dateFieldValue.indexOf(\"-\")>0){\n");
      jsStr.append("\t\t\tvar dateFieldValueArray = dateFieldValue.split(\"-\") ;\n");
      jsStr.append("\t\t\tif(dateFieldValueArray.length>=3){\n");
      jsStr.append("\t\t\t\tyearId=dateFieldValueArray[0];\n");
      jsStr.append("\t\t\t\tmonthId=dateFieldValueArray[1];\n");
      jsStr.append("\t\t\t\tdateId=dateFieldValueArray[2];\n");
      jsStr.append("\t\t\t}\n");
      jsStr.append("\t\t}\n");
      jsStr.append("\t}\n");

      if (currentdateList.size() >= 1) {
        jsStr.append("\tif(yearId==\"\"||yearId<=0){\n");
        jsStr.append("\t\tyearId=" + (String) currentdateList.get(0) + ";\n");
        jsStr.append("\t}\n");
      }

      if (currentdateList.size() >= 2) {
        jsStr.append("\tif(monthId==\"\"||monthId<=0){\n");
        jsStr.append("\t\tmonthId=" + (String) currentdateList.get(1) + ";\n");
        jsStr.append("\t}\n");
      }

      if (currentdateList.size() >= 3) {
        jsStr.append("\tif(dateId==\"\"||dateId<=0){\n");
        jsStr.append("\t\tdateId=" + (String) currentdateList.get(2) + ";\n");
        jsStr.append("\t}\n");
      }

      jsStr.append("\tif($G(\"field" + fieldIdSelect + "\")!=null){\n");
      jsStr.append("\t\tfieldId=" + fieldIdSelect + ";\n");
      jsStr.append("\t\tfieldValue=$G(\"field" + fieldIdSelect + "\").value;\n");
      jsStr.append("\t}\n");

      jsStr.append("\tif($G(\"field" + supSubCompanyFieldId + "\")!=null){\n");
      jsStr.append("\t\tsupSubCompanyId=$G(\"field" + supSubCompanyFieldId + "\").value;\n");
      jsStr.append("\t}\n");
      jsStr.append("\tif(supSubCompanyId==\"\"||supSubCompanyId<=0){\n");
      jsStr.append("\t\tsupSubCompanyId=" + supSubCompanyId + ";\n");
      jsStr.append("\t}\n");

      jsStr.append("\tif($G(\"field" + subCompanyFieldId + "\")!=null){\n");
      jsStr.append("\t\tsubCompanyId=$G(\"field" + subCompanyFieldId + "\").value;\n");
      jsStr.append("\t}\n");
      jsStr.append("\tif(subCompanyId==\"\"||subCompanyId<=0){\n");
      jsStr.append("\t\tsubCompanyId=" + subCompanyId + ";\n");
      jsStr.append("\t}\n");

      jsStr.append("\tif($G(\"field" + departmentFieldId + "\")!=null){\n");
      jsStr.append("\t\tdepartmentId=$G(\"field" + departmentFieldId + "\").value;\n");
      jsStr.append("\t}\n");
      jsStr.append("\tif(departmentId==\"\"||departmentId<=0){\n");
      jsStr.append("\t\tdepartmentId=" + departmentId + ";\n");
      jsStr.append("\t}\n");
      jsStr.append("}\n");

      jsStr.append("function onCreateCodeAgain(ismand){\n");
      jsStr.append("\tif($G(\"field" + fieldCode + "\")!=null&&$G(\"field" + fieldCode + "span\")!=null){\n");
      jsStr.append("\t\tinitDataForWorkflowCode();\n");
      jsStr.append("\t\t$G(\"workflowKeywordIframe\").src=\"/workflow/request/WorkflowCodeIframe.jsp?operation=CreateCodeAgain&requestId=" + requestid + "&workflowId=\"+workflowId+\"&formId=\"+formId+\"&isBill=\"+isBill+\"&yearId=\"+yearId+\"&monthId=\"+monthId+\"&dateId=\"+dateId+\"&fieldId=\"+fieldId+\"&fieldValue=\"+fieldValue+\"&supSubCompanyId=\"+supSubCompanyId+\"&subCompanyId=\"+subCompanyId+\"&departmentId=\"+departmentId+\"&recordId=\"+recordId+\"&ismand=\"+ismand;\n");
      jsStr.append("\t}\n");
      jsStr.append("}\n");

      jsStr.append("function onCreateCodeAgainReturn(newCode,ismand){\n");
      jsStr.append("\tif(typeof(newCode)!=\"undefined\"&&newCode!=\"\"){\n");
      jsStr.append("\t\t$G(\"field" + fieldCode + "\").value=newCode;\n");
      jsStr.append("\t\t$G(\"field" + fieldCode + "span\").innerHTML='';\n");
      jsStr.append("\t\tif(parent.document.getElementById(\"requestmarkSpan\")!=null){\n");
      jsStr.append("\t\t\tjQuery(parent.document.getElementById(\"requestmarkSpan\")).text(newCode);\n");
      jsStr.append("\t\t}\n");
      jsStr.append("\t}\n");
      jsStr.append("}\n");

      jsStr.append("function onChooseReservedCode(ismand){\n");
      jsStr.append("\tif($G(\"field" + fieldCode + "\")!=null&&$G(\"field" + fieldCode + "span\")!=null){\n");
      jsStr.append("\t\tinitDataForWorkflowCode();\n");
      jsStr.append("\t\turl=escape(\"/workflow/workflow/showChooseReservedCodeOperate.jsp?workflowId=\"+workflowId+\"&formId=\"+formId+\"&isBill=\"+isBill+\"&yearId=\"+yearId+\"&monthId=\"+monthId+\"&dateId=\"+dateId+\"&fieldId=\"+fieldId+\"&fieldValue=\"+fieldValue+\"&supSubCompanyId=\"+supSubCompanyId+\"&subCompanyId=\"+subCompanyId+\"&departmentId=\"+departmentId+\"&recordId=\"+recordId);\n");
      jsStr.append("\t\tcon = window.showModalDialog(\"/systeminfo/BrowserMain.jsp?url=\"+url);\n");

      jsStr.append("\t\tif(typeof(con)!=\"undefined\"&&con!=\"\"){\n");
      jsStr.append("\t\t\t$G(\"workflowKeywordIframe\").src=\"/workflow/request/WorkflowCodeIframe.jsp?operation=chooseReservedCode&requestId=" + requestid + "&workflowId=\"+workflowId+\"&formId=\"+formId+\"&isBill=\"+isBill+\"&codeSeqReservedIdAndCode=\"+con+\"&ismand=\"+ismand;\n");
      jsStr.append("\t\t}\n");
      jsStr.append("\t}\n");
      jsStr.append("}\n");

      jsStr.append("function onNewReservedCode(ismand){\n");
      jsStr.append("\tinitDataForWorkflowCode();\n");
      jsStr.append("\turl=escape(\"/workflow/workflow/showNewReservedCodeOperate.jsp?workflowId=\"+workflowId+\"&formId=\"+formId+\"&isBill=\"+isBill+\"&yearId=\"+yearId+\"&monthId=\"+monthId+\"&dateId=\"+dateId+\"&fieldId=\"+fieldId+\"&fieldValue=\"+fieldValue+\"&supSubCompanyId=\"+supSubCompanyId+\"&subCompanyId=\"+subCompanyId+\"&departmentId=\"+departmentId+\"&recordId=\"+recordId);\n");
      jsStr.append("\tcon = window.showModalDialog(\"/systeminfo/BrowserMain.jsp?url=\"+url);\n");
      jsStr.append("}\n");

      jsStr.append("\n");
      jsStr.append("function showfieldpop(){").append("\n");
      if (fieldidList.size() < 1) {
        jsStr.append("\talert(\"" + SystemEnv.getHtmlLabelName(22577, user.getLanguage()) + "\");").append("\n");
      }
      jsStr.append("}").append("\n");

      jsStr.append("\n");
      jsStr.append("").append("function dyniframesize(){").append("").append("\n");
      jsStr.append("\t").append("var dyniframe;").append("").append("\n");
      for (int i = 0; i < fckfieldidList.size(); i++) {
        jsStr.append("\t").append("if($G){").append("").append("\n");
        jsStr.append("\t\t").append("dyniframe = $G(\"" + fckfieldidList.get(i) + "\");").append("").append("\n");
        jsStr.append("\t\t").append("if (dyniframe && !window.opera){").append("").append("\n");
        jsStr.append("\t\t\t").append("if (dyniframe.contentDocument && dyniframe.contentDocument.body.offsetHeight){").append("").append("\n");
        jsStr.append("\t\t\t\t").append("dyniframe.height = dyniframe.contentDocument.body.offsetHeight+20;").append("").append("\n");
        jsStr.append("\t\t\t").append("}else if (dyniframe.Document && dyniframe.Document.body.scrollHeight){//如果用户的浏览器是IE").append("").append("\n");
        jsStr.append("\t\t\t\t").append("dyniframe.Document.body.bgColor=\"transparent\";").append("").append("\n");
        jsStr.append("\t\t\t\t").append("dyniframe.height = dyniframe.Document.body.scrollHeight+20;").append("").append("\n");
        jsStr.append("\t\t\t").append("}").append("").append("\n");
        jsStr.append("\t\t").append("}").append("").append("\n");
        jsStr.append("\t").append("}").append("").append("\n");
      }
      jsStr.append("").append("}").append("\n");

      jsStr.append("").append("if(window.addEventListener){").append("\n");
      jsStr.append("\t").append("window.addEventListener(\"load\",dyniframesize,false);").append("\n");
      jsStr.append("").append("}else if(window.attachEvent){").append("\n");
      jsStr.append("\t").append("window.attachEvent(\"onload\",dyniframesize);").append("\n");
      jsStr.append("").append("}else{").append("\n");
      jsStr.append("\t").append("window.onload=dyniframesize;").append("\n");
      jsStr.append("").append("}").append("\n");
      // System.out.println(wfformhtml);//开发中测试用
    } catch (Exception e) {
      writeLog(e);
    }
  }

  // 获得节点签字意见，放到模板上指定位置
  public void getNodeRemark() {
    try {
      int workflowid = Util.getIntValue(request.getParameter("workflowid"), 0);
      int nodeid = Util.getIntValue(request.getParameter("nodeid"), 0);
      int requestid = Util.getIntValue(request.getParameter("requestid"), 0);
      
	  boolean wfmonitor_1 = Util.null2String(request.getParameter("wfmonitor")).equalsIgnoreCase("true");
      String isintervenor = Util.null2String(request.getParameter("isintervenor"));
      int intervenorright = 0;
	  if(isintervenor.equals("1") || wfmonitor_1){
	      SysWFLMonitor syswflmonitor = new SysWFLMonitor();
		  intervenorright = syswflmonitor.getWFInterventorRightBymonitor(user.getUID(), requestid);
	  }
	  if(wfmonitor_1){
		  intervenorright = 1;
	  }
	  List signcanviewnodeids = getSigntCanViewNodeIds(user, workflowid, requestid, Util.null2String(request.getParameter("isurger")), intervenorright);
      
      RecordSet rs = new RecordSet();
      ArrayList nodeidList = new ArrayList();// nodeid队列，这样可以释放rs对象。
      //String sql = "select fn.nodeid from workflow_flownode fn where fn.workflowid=" + workflowid + " order by fn.nodetype, fn.nodeid";
      //String sql = "select nodeid from workflow_flownode,workflow_nodebase where (IsFreeNode is null or IsFreeNode!='1') and nodeid=id and workflowid="+workflowid+" order by nodetype,nodeid";
      String sql = "select b.nodeid from workflow_nodebase a left join workflow_flownode b on a.id = b.nodeid where b.workflowid="+workflowid+" and ((a.IsFreeNode is null or a.IsFreeNode = '') or a.requestid="+requestid+") order by b.nodetype,b.nodeid";
      rs.execute(sql);
      while (rs.next()) {
        int nodeid_tmp = Util.getIntValue(rs.getString("nodeid"), 0);
        if (nodeid_tmp > 0) {
          nodeidList.add("" + nodeid_tmp);
        }
      }
      nodeidList.add("" + 999999999);  //自由流转的默认id
      Hashtable hasnoderemark_hs = new Hashtable();
      Hashtable noderemark_hs = new Hashtable();
      FieldInfo fieldInfo = new FieldInfo();
      fieldInfo.setRequestid(requestid);
      fieldInfo.setUser(user);
      for (int i = 0; i < nodeidList.size(); i++) {
        int nodeid_tmp = Util.getIntValue((String) nodeidList.get(i), 0);
        String content1 = "";
        String content2 = "";
        // 循环替换节点显示意见。这里如果找到就替换，如果找不到就不处理
        int pos = htmlLayout.toLowerCase().indexOf("$node" + nodeid_tmp + "$");
        while (pos > -1) {
          isRemarkInnerMode = true;
          content1 = wfformhtml.substring(0, pos);
          content2 = wfformhtml.substring(pos + 1);
          int pos1 = content1.lastIndexOf("<");
          int pos2 = content2.indexOf(">");
          if (pos1 > -1) {
            content1 = content1.substring(0, pos1);
          }
          if (pos2 > -1) {
            content2 = content2.substring(pos2 + 1);
          }
          String nodemark = "";
          // 要获得一次节点信息的数据库查询量很大，所以用Hashtable暂存。如果取过，就拿缓存里的。
          int hasnoderemark_tmp = Util.getIntValue((String) hasnoderemark_hs.get("node" + nodeid_tmp), 0);
          if (hasnoderemark_tmp == 0) {
            if(nodeid_tmp == 999999999){  //自由流转
        		nodemark = fieldInfo.GetfreeNodeRemark(workflowid, nodeid, 2);
        	}else{
			    //是否有权限查看
			 if (signcanviewnodeids.indexOf(nodeid_tmp + "") != -1) {
        			nodemark = fieldInfo.GetNodeRemark(workflowid, nodeid_tmp, nodeid, 2);
        		}
        	}
            //去掉多余分隔符
            nodemark = nodemark.replace(String.valueOf(FieldInfo.getNodeSeparator()), "").replace(String.valueOf(Util.getSeparator()),"");
            hasnoderemark_hs.put("node" + nodeid_tmp, "1");
            noderemark_hs.put("node" + nodeid_tmp, nodemark);
          } else {
            nodemark = Util.null2String((String) noderemark_hs.get("node" + nodeid_tmp));
          }

          String nodeRemar_tmp = nodemark;
          if (nodeRemar_tmp.indexOf("<br>") == 0) {
            nodeRemar_tmp = nodeRemar_tmp.substring(4, nodeRemar_tmp.length());
          }
          if (nodeRemar_tmp.endsWith("<br>")) {
            nodeRemar_tmp = nodeRemar_tmp.substring(0, nodeRemar_tmp.length() - 4);
          }
          wfformhtml = content1 + nodeRemar_tmp + content2;
          htmlLayout = wfformhtml;
          pos = htmlLayout.toLowerCase().indexOf("$node" + nodeid_tmp + "$");
        }
      }
    } catch (Exception e) {
      writeLog(e);
    }
  }

  /**
   * 处理明细字段的转化。不需要处理字段显示名
   */
  public void getDetailTableElement() {
    try {
	  String useNew = Util.null2String(new weaver.general.BaseBean().getPropValue("workflow_htmlNew","useNew"));
      String sql = "";
      RecordSet rs_oldDetail = new RecordSet();// 保留，只用于查已有的明细数据
      RecordSet rs_group = new RecordSet();
      RecordSet rs_sap = new RecordSet();
      RecordSet rs = new RecordSet();
      RecordSet rs_tmp = new RecordSet();
      DetailFieldComInfo detailFieldComInfo = new DetailFieldComInfo();
      FieldTypeComInfo fieldTypeComInfo = new FieldTypeComInfo();
      WfLinkageInfo wfLinkageInfo = new WfLinkageInfo();
      ResourceComInfo resourceComInfo = new ResourceComInfo();
      int requestid = Util.getIntValue(request.getParameter("requestid"), 0);
      int billid = Util.getIntValue(request.getParameter("billid"), 0);
      int workflowid = Util.getIntValue(request.getParameter("workflowid"), 0);
      int nodeid = Util.getIntValue(request.getParameter("nodeid"), 0);
      int nodetype = Util.getIntValue(request.getParameter("nodetype"), 0);
      int isbill = Util.getIntValue(request.getParameter("isbill"), 0);
      int formid = Util.getIntValue(request.getParameter("formid"), 0);
      int isremark = Util.getIntValue(request.getParameter("isremark"), 0);
      int isviewonly = Util.getIntValue((String) otherPara_hs.get("isviewonly"), 0);
      int mustNoEdit = Util.getIntValue((String) otherPara_hs.get("mustNoEdit"), 0);
      String prjid = Util.null2String(request.getParameter("prjid"));
      String reqid = Util.null2String(request.getParameter("reqid"));
      String docid = Util.null2String(request.getParameter("docid"));
      String hrmid = Util.null2String(request.getParameter("hrmid"));
      String crmid = Util.null2String(request.getParameter("crmid"));
      if ("".equals(hrmid) && "1".equals(this.user.getLogintype())) {
        hrmid = "" + this.user.getUID();
      } else if ("".equals(crmid) && "2".equals(this.user.getLogintype())) {
        crmid = "" + this.user.getUID();
      }
      int isprint = Util.getIntValue(request.getParameter("isprint"), 0);
      HttpSession session = (HttpSession) request.getSession(false);
      int _intervenorright = Util.getIntValue((String) session.getAttribute(user.getUID() + "_" + requestid + "intervenorright"), 0);
      //html解析方案
      //int htmlParseScheme = 0;
      
      String dlbodychangattrstr = "";
      HtmlElement object = null;
	  
      int isaffirmancebody = Util.getIntValue((String) session.getAttribute(user.getUID() + "_" + requestid + "isaffirmance"), 0);// 是否需要提交确认
      int reEditbody = Util.getIntValue((String) session.getAttribute(user.getUID() + "_" + requestid + "reEdit"), 0);// 是否需要提交确认

      DynamicDataInput ddidetail = new DynamicDataInput(workflowid + "");
      String trrigerdetailfield = ddidetail.GetEntryTriggerDetailFieldName();
      ArrayList seldefieldsadd = wfLinkageInfo.getSelectField(workflowid, nodeid, 1);
      ArrayList changedefieldsadd = wfLinkageInfo.getChangeField(workflowid, nodeid, 1);
      otherPara_hs.put("seldefieldsadd", seldefieldsadd);
      otherPara_hs.put("changedefieldsadd", changedefieldsadd);
      otherPara_hs.put("trrigerdetailfield", trrigerdetailfield);

      Hashtable detailField2Groupid_hs = new Hashtable();
      // 把单据的明细字段表明映射为groupid
      if (isbill == 1) {
        int cx = 0;
        sql = "select tablename from Workflow_billdetailtable where billid=" + formid + " order by orderid";
        rs_tmp.execute(sql);
        while (rs_tmp.next()) {
          String tablename_tmp = Util.null2String(rs_tmp.getString("tablename"));
          if (!"".equals(tablename_tmp)) {
            detailField2Groupid_hs.put(tablename_tmp, "" + cx);
            cx++;
          }
        }
      }

      Hashtable detailFieldid_hs = new Hashtable();// 表单的所有明细表字段
      Hashtable fieldhtmltype_hs = new Hashtable(); // 字段的htmltype队列
      Hashtable fieldtype_hs = new Hashtable(); // 字段的type队列
      Hashtable fielddbtype_hs = new Hashtable(); // 字段的数据库字段类型队列
      Hashtable fieldname_hs = new Hashtable();// 表字段在数据库的字段名字
      Hashtable isview_hs = new Hashtable();// 是否显示
      Hashtable isedit_hs = new Hashtable();// 是否可编辑
      Hashtable ismand_hs = new Hashtable();// 是否必填
      Hashtable fieldlabel_hs = new Hashtable();// 字段显示名
      if (isbill == 0) {
        sql = "select nf.*, fl.fieldlable, ff.groupid, '' as fieldname from workflow_nodeform nf left join workflow_formfield ff on nf.fieldid=ff.fieldid and ff.formid=" + formid + " left join workflow_fieldlable fl on fl.fieldid=nf.fieldid and fl.formid=" + formid + " and fl.langurageid=" + user.getLanguage() + " where nf.nodeid=" + nodeid + " and ff.isdetail='1' order by nf.orderid, ff.fieldorder";
      } else if (isbill == 1) {
        // 单据的明细字段信息先拿出来，具体分组以后再查数据库
        sql = "select nf.*, bf.fieldlabel as fieldlable, detailtable as groupid, bf.fieldname, bf.fieldhtmltype, bf.type, bf.fielddbtype from workflow_nodeform nf left join workflow_billfield bf on nf.fieldid=bf.id and bf.billid=" + formid + " where nf.nodeid=" + nodeid + " and bf.viewtype=1 order by nf.orderid, bf.dsporder";
      }
      rs_tmp.execute(sql);
      while (rs_tmp.next()) {
        int fieldid_tmp = Util.getIntValue(rs_tmp.getString("fieldid"), 0);
        int isview_tmp = Util.getIntValue(rs_tmp.getString("isview"), 0);
        int isedit_tmp = Util.getIntValue(rs_tmp.getString("isedit"), 0);
        int ismand_tmp = Util.getIntValue(rs_tmp.getString("ismandatory"), 0);
        String fieldname = Util.null2String(rs_tmp.getString("fieldname"));
        if (isbill == 0) {
          fieldname = detailFieldComInfo.getFieldname("" + fieldid_tmp);
        }
        String labelName_tmp = "";
        if (isbill == 0) {
          labelName_tmp = Util.null2String(rs_tmp.getString("fieldlable"));
        } else {
          int labelid_tmp = Util.getIntValue(rs_tmp.getString("fieldlable"));
          labelName_tmp = SystemEnv.getHtmlLabelName(labelid_tmp, user.getLanguage());
        }
        labelName_tmp = Util.toScreenForWorkflow(labelName_tmp);
        String groupid_tmp = "";
        if (isbill == 0) {
          groupid_tmp = "" + Util.getIntValue(rs_tmp.getString("groupid"), 0);
        } else {
          String groupname_tmp = "" + Util.null2String(rs_tmp.getString("groupid"));
          groupid_tmp = "" + Util.getIntValue((String) detailField2Groupid_hs.get(groupname_tmp), -1);
        }
        ArrayList detailFieldidList = (ArrayList) detailFieldid_hs.get("detailfieldList_" + groupid_tmp);
        if (detailFieldidList == null) {
          detailFieldidList = new ArrayList();
        }
        detailFieldidList.add("" + fieldid_tmp);
        detailFieldid_hs.put("detailfieldList_" + groupid_tmp, detailFieldidList);
        int htmltype_tmp = 0;
        int type_tmp = 0;
        String dbtype_tmp = "";
        if (isbill == 0) {
          htmltype_tmp = Util.getIntValue(detailFieldComInfo.getFieldhtmltype("" + fieldid_tmp), 0);
          type_tmp = Util.getIntValue(detailFieldComInfo.getFieldType("" + fieldid_tmp), 0);
          dbtype_tmp = Util.null2String(detailFieldComInfo.getFielddbtype("" + fieldid_tmp));
        } else {
          htmltype_tmp = Util.getIntValue(rs_tmp.getString("fieldhtmltype"), 0);
          type_tmp = Util.getIntValue(rs_tmp.getString("type"), 0);
          dbtype_tmp = Util.null2String(rs_tmp.getString("fielddbtype"));
        }
        fieldhtmltype_hs.put("fieldhtmltype" + fieldid_tmp, "" + htmltype_tmp);
        fieldtype_hs.put("fieldtype" + fieldid_tmp, "" + type_tmp);
        fielddbtype_hs.put("fielddbtype" + fieldid_tmp, dbtype_tmp);
        fieldname_hs.put("fieldname" + fieldid_tmp, fieldname);
        isview_hs.put("isview" + fieldid_tmp, "" + isview_tmp);
        isedit_hs.put("isedit" + fieldid_tmp, "" + isedit_tmp);
        ismand_hs.put("ismand" + fieldid_tmp, "" + ismand_tmp);
        fieldlabel_hs.put("fieldlabel" + fieldid_tmp, labelName_tmp);
      }
      otherPara_hs.put("detailFieldid_hs", detailFieldid_hs);
      // 获取节点前附加操作
      Hashtable inoperatefield_hs = new Hashtable();
      Hashtable fieldvalue_hs = new Hashtable();// 节点前附加操作的值
      RequestPreAddinoperateManager requestPreAddM = new RequestPreAddinoperateManager();
      requestPreAddM.setCreater(user.getUID());
      requestPreAddM.setOptor(user.getUID());
      requestPreAddM.setWorkflowid(workflowid);
      requestPreAddM.setNodeid(nodeid);
      Hashtable getPreAddRule_hs = requestPreAddM.getPreAddRule();
      inoperatefield_hs = (Hashtable) getPreAddRule_hs.get("inoperatefield_hs");
      fieldvalue_hs = (Hashtable) getPreAddRule_hs.get("inoperatevalue_hs");

      // 明细字段间操作的数据
      ArrayList colCalAry = new ArrayList();
      String rowCalItemStr1, colCalItemStr1, mainCalStr1;
      rowCalItemStr1 = new String("");
      colCalItemStr1 = new String("");
      mainCalStr1 = new String("");
      rs_tmp.executeProc("Workflow_formdetailinfo_Sel", formid + "");
      while (rs_tmp.next()) {
        rowCalItemStr1 = Util.null2String(rs_tmp.getString("rowCalStr"));
        colCalItemStr1 = Util.null2String(rs_tmp.getString("colCalStr"));
        mainCalStr1 = Util.null2String(rs_tmp.getString("mainCalStr"));
        // System.out.println("rowCalItemStr1 = " + rowCalItemStr1);
      }
      StringTokenizer stk = new StringTokenizer(colCalItemStr1, ";");
      while (stk.hasMoreTokens()) {
        colCalAry.add(stk.nextToken());
      }

      if (isbill == 0) {
        sql = "select distinct groupid from workflow_formfield where formid=" + formid + " and isdetail='1' order by groupid";
      } else {
        sql = "select tablename as groupid, title from Workflow_billdetailtable where billid=" + formid + " order by orderid";
      }
      rs_group.execute(sql);
      int groupCount = 0;
      int derecorderindex = 0;
      String addrowstrtmp = "";
      String detailFieldNotShow = ""; // 保存不显示的明细字段id

      while (rs_group.next()) {
        String submitdtlid = "";
        StringBuffer addJsSb = new StringBuffer();
        StringBuffer delJsSb = new StringBuffer();
        StringBuffer addSAPJsSb = new StringBuffer();//zzl
        
        String addJSExt = "";
        int groupid_tmp = -1;
        if (isbill == 0) {
          groupid_tmp = Util.getIntValue(rs_group.getString("groupid"), 0);
        } else {
          groupid_tmp = groupCount;
        }
        String hiddenElementStr = "";
        String addJsStr = "";
        WFNodeDtlFieldManager wFNodeDtlFieldManager = new WFNodeDtlFieldManager();
        wFNodeDtlFieldManager.resetParameter();
        wFNodeDtlFieldManager.setNodeid(nodeid);
        wFNodeDtlFieldManager.setGroupid(groupid_tmp);
        wFNodeDtlFieldManager.selectWfNodeDtlField();
        String dtladd = wFNodeDtlFieldManager.getIsadd();// 其实这个条件意义不大，因为添加按钮根据页面本身的来，不做任何解析
        String dtldelete = wFNodeDtlFieldManager.getIsdelete();// 这个条件的意义在于，已有的明细字段，在最前面是不是有checkbox
        String dtledit = wFNodeDtlFieldManager.getIsedit();
        String isprintnulldetail = wFNodeDtlFieldManager.getIshide();
        String dtldefault = wFNodeDtlFieldManager.getIsdefault();
        String dtlneed = wFNodeDtlFieldManager.getIsneed();

        String groupName_tmp = "";
        if (isbill == 0) {
          groupName_tmp = "" + Util.getIntValue(rs_group.getString("groupid"), 0);
        } else {
          groupName_tmp = "" + Util.null2String(rs_group.getString("groupid"));;
        }
        boolean shouldHidden = false;
        if (_intervenorright == 1) {
          shouldHidden = true;
        }
        if (isprint == 1 && !"1".equals(isprintnulldetail)) {
          if (isbill == 1) {
            if (billtablename.indexOf("formtable_main_") == 0 && groupName_tmp.indexOf("formtable_main_") == 0) {// 新表单
              rs.executeSql("select b.* from " + billtablename + " a," + groupName_tmp + " b where a.id=b.mainid and a.requestid =" + requestid + " order by b.id");
            } else if (Util.getIntValue("" + formid) < 0) { // 数据中心模块创建的明细报表
              rs.executeSql("select b.* from " + billtablename + " a," + groupName_tmp + " b where a.id=b.mainid and a.requestid =" + requestid + " order by b.inputid");
            } else {
              rs.executeSql("select b.* from " + billtablename + " a," + groupName_tmp + " b where a.id=b.mainid and a.requestid =" + requestid);
            }
            if (rs.getCounts() <= 0) {
              shouldHidden = true;
            }
          } else {
            rs.executeSql(" select * from Workflow_formdetail where requestid =" + requestid + " and groupId=" + groupName_tmp + "  order by id");
            if (rs.getCounts() <= 0) {
              shouldHidden = true;
            }
          }
        }
        if (shouldHidden == true) {
          // 明细字段不显示，包括标题栏。这里用JS方法把它隐藏掉
          jsStr.append("\n");
          jsStr.append("").append("function doHiddenDetail" + groupCount + "(){").append("\n");
          jsStr.append("\t").append("try{").append("\n");
          jsStr.append("\t\t").append("jQuery(\"#table" + groupCount + "button\").hide();").append("\n");
          jsStr.append("\t\t").append("jQuery(\"#div" + groupCount + "button\").hide();").append("\n");
          jsStr.append("\t\t").append("jQuery(\"#oTable" + groupCount + "\").hide();").append("\n");
          jsStr.append("\t").append("}catch(e){}").append("\n");
          jsStr.append("").append("}").append("\n");

          // jsStr.append("\t").append("window.attachEvent(\"onload\", doHiddenDetail"+groupCount+");").append("\n");

          jsStr.append("\t").append("if (window.addEventListener){").append("\n");
          jsStr.append("\t").append("    window.addEventListener(\"load\", doHiddenDetail" + groupCount + ", false);").append("\n");
          jsStr.append("\t").append("}else if (window.attachEvent){").append("\n");
          jsStr.append("\t").append("    window.attachEvent(\"onload\", doHiddenDetail" + groupCount + ");").append("\n");
          jsStr.append("\t").append("}else{").append("\n");
          jsStr.append("\t").append("    window.onload=doHiddenDetail" + groupCount + ";").append("\n");
          jsStr.append("\t").append("}").append("\n");

          groupCount++;
          continue;
        }

        if (isviewonly == 1 || !"1".equals(dtladd) || mustNoEdit == 1) {// 把添加按钮删掉
          int pos_tmp = htmlLayout.toLowerCase().indexOf("$addbutton" + groupid_tmp + "$");
          while (pos_tmp > -1) {
            String content1_tmp = wfformhtml.substring(0, pos_tmp);
            String content2_tmp = wfformhtml.substring(pos_tmp + 1);
            int pos1 = content1_tmp.lastIndexOf("<");

            int pos2 = content2_tmp.toLowerCase().indexOf("<button");
            int pos3 = content2_tmp.toLowerCase().indexOf("</button>");
            if (pos1 > -1) {
              content1_tmp = content1_tmp.substring(0, pos1);
            }
            if (pos3 > -1 && (pos2 == -1 || pos3 < pos2)) {
              content2_tmp = content2_tmp.substring(pos3 + 9);
            }
            wfformhtml = content1_tmp + "" + content2_tmp;
            htmlLayout = wfformhtml;
            pos_tmp = htmlLayout.toLowerCase().indexOf("$addbutton" + groupid_tmp + "$");
          }
        }
        if (isviewonly == 1 || mustNoEdit == 1) {// 把删除按钮删掉
          int pos_tmp = htmlLayout.toLowerCase().indexOf("$delbutton" + groupid_tmp + "$");
          while (pos_tmp > -1) {
            String content1_tmp = wfformhtml.substring(0, pos_tmp);
            String content2_tmp = wfformhtml.substring(pos_tmp + 1);
            int pos1 = content1_tmp.lastIndexOf("<");

            int pos2 = content2_tmp.toLowerCase().indexOf("<button");
            int pos3 = content2_tmp.toLowerCase().indexOf("</button>");
            if (pos1 > -1) {
              content1_tmp = content1_tmp.substring(0, pos1);
            }
            if (pos3 > -1 && (pos2 == -1 || pos3 < pos2)) {
              content2_tmp = content2_tmp.substring(pos3 + 9);
            }
            wfformhtml = content1_tmp + "" + content2_tmp;
            htmlLayout = wfformhtml;
            pos_tmp = htmlLayout.toLowerCase().indexOf("$delbutton" + groupid_tmp + "$");
          }
        }
        
        if (isviewonly == 1 || mustNoEdit == 1) {// 把SAP多选浏览按钮删除-------------zzl
            int pos_tmp = htmlLayout.toLowerCase().indexOf("$sapmulbutton" + groupid_tmp + "$");
            while (pos_tmp > -1) {
              String content1_tmp = wfformhtml.substring(0, pos_tmp);
              String content2_tmp = wfformhtml.substring(pos_tmp + 1);
              int pos1 = content1_tmp.lastIndexOf("<");

              int pos2 = content2_tmp.toLowerCase().indexOf("<button");
              int pos3 = content2_tmp.toLowerCase().indexOf("</button>");
              if (pos1 > -1) {
                content1_tmp = content1_tmp.substring(0, pos1);
              }
              if (pos3 > -1 && (pos2 == -1 || pos3 < pos2)) {
                content2_tmp = content2_tmp.substring(pos3 + 9);
              }
              wfformhtml = content1_tmp + "" + content2_tmp;
              htmlLayout = wfformhtml;
              pos_tmp = htmlLayout.toLowerCase().indexOf("$sapmulbutton" + groupid_tmp + "$");
            }
          }
        
        // 去掉删除按钮的isdel()方法
        String str1 = "if(isdel()){deleterow" + groupid_tmp + "(" + groupid_tmp + ");}";
        int pos_temp = wfformhtml.toLowerCase().indexOf(str1);
        if (pos_temp > -1) {
          wfformhtml = wfformhtml.substring(0, pos_temp) + "deleteRow" + groupid_tmp + "(" + groupid_tmp + ");" + wfformhtml.substring(pos_temp + str1.length());
        }
        ArrayList detailFieldList = (ArrayList) detailFieldid_hs.get("detailfieldList_" + groupid_tmp);
        // 先拼新建、删除行的Js方法的头
        addJsSb.append("\n").append("function addRow" + groupid_tmp + "(groupid){").append("\n");
		addJsSb.append("\n").append("var maxHeight=0;\nvar oCells = [];\nrowProcessing = true;").append("\n");
        delJsSb.append("\n").append("function deleteRow" + groupid_tmp + "(groupid,isfromsap){").append("\n");
        //------------------zzl生成多选浏览按钮脚本-------------------------------------
        addSAPJsSb.append("\n").append("function addSapRow" + groupid_tmp + "(groupid){").append("\n");
        			//添加一行
        			rs_sap.execute("select browsermark  from sap_multiBrowser where mxformid='"+formid+"' and mxformname='"+groupName_tmp+"'");
        			if(rs_sap.next()){
	        			String browsermark=rs_sap.getString("browsermark");
	        			//addSAPJsSb.append("addRow"+groupid_tmp+"(groupid);");
	        			//http://localhost:8081/systeminfo/BrowserMain.jsp?url=/integration/sapSingleBrowser.jsp?type=browser.181|11182_1
	        			//打开sap多选浏览按钮
	        			addSAPJsSb.append("window.showModalDialog(\"/systeminfo/BrowserMain.jsp?url=/integration/sapSingleBrowser.jsp?type="+browsermark+"|\"+groupid, window, \"dialogWidth=550px;dialogHeight=550px\");");
        			}else{
						addSAPJsSb.append("alert('当前明细表没有配置集成多选浏览按钮!')");
					}
        			
        addSAPJsSb.append("\n").append("}");
        addSAPJsSb.append("\n");
        //------------------zzl生成多选浏览按钮脚本-------------------------------------
        
        int nodesnum = 0;// 填到下面的2个hidden的input里
        String needcheck_tmp = "";// addrow里面的新增的字段的必填控制
        boolean defshowsum = false;// 是否需要合计
        // 用eweaver的方式，把每个明细字段都做在一个table里，然后只要这个table里面的tr加1行就行了
        addJsSb.append("\n").append("var initDetailfields=\"\";").append("\n");
        if (detailFieldList != null && detailFieldList.size() > 0) {
          // 这里取出已有的明细字段的值
          if (iscreate == 0) {
            if (isbill == 0) {
              rs_oldDetail.executeSql("select * from Workflow_formdetail where requestid =" + requestid + "  and groupId=" + groupid_tmp + " order by id");
            } else {
              if (billtablename.indexOf("formtable_main_") == 0 && groupName_tmp.indexOf("formtable_main_") == 0) {// 新表单
                rs_oldDetail.executeSql("select b.* from " + billtablename + " a," + groupName_tmp + " b where a.id=b.mainid and a.requestid =" + requestid + " order by b.id");
              } else if (formid < 0) { // 数据中心模块创建的明细报表
                rs_oldDetail.executeSql("select b.* from " + billtablename + " a," + groupName_tmp + " b where a.id=b.mainid and a.requestid =" + requestid + " order by b.inputid");
              } else {
                rs_oldDetail.executeSql("select b.* from " + billtablename + " a," + groupName_tmp + " b where a.id=b.mainid and a.requestid =" + requestid);
              }
            }
          }
          int pos = -1;
          int lastPos = -1;
          // 这里先循环一次，找出哪个字段才是第一个明细字段
          int firstDetailFieldid = 0;
          //最后一个，用于删除html生成的行
          int lastDetailFieldId = 0;
          //更改html模式明细的显示模式，需要记录明细显示顺序
          List detailFieldOrder = new ArrayList();
		  HashMap detailFieldOrderMap = new HashMap();//存放字段ID与位置的对应信息
          for (int i = 0; i < detailFieldList.size(); i++) {
            int fieldid_tmp = Util.getIntValue((String) detailFieldList.get(i), 0);
            int pos_tmp = htmlLayout.toLowerCase().indexOf("$field" + fieldid_tmp + "$");
            if (pos_tmp > -1) {
              detailFieldOrder.add(new Integer(pos_tmp));
			  detailFieldOrderMap.put(new Integer(pos_tmp),"" + fieldid_tmp);
              if (pos == -1) {
                pos = pos_tmp;
                firstDetailFieldid = fieldid_tmp;
              } else {
                if (pos_tmp < pos) {
                  pos = pos_tmp;
                  firstDetailFieldid = fieldid_tmp;
                }
              }
              //查找最后一个明细字段
              if (lastPos == -1) {
            	  lastPos = pos_tmp;
            	  lastDetailFieldId = fieldid_tmp;
                } else {
                  if (pos_tmp > lastPos) {
                	  lastPos = pos_tmp;
                	  lastDetailFieldId = fieldid_tmp;
                }
              }
            }
            if (defshowsum == false) {
              if (colCalAry.indexOf("detailfield_" + fieldid_tmp) > -1) {
                defshowsum = true;
              }
            }
          }
          otherPara_hs.put("firstDetailFieldid", "" + firstDetailFieldid);
          // 这里拼方法的开始内容
          addJsSb.append("\t").append("var rowindex = parseInt($G(\"indexnum" + groupid_tmp + "\").value);").append("\n");
          addJsSb.append("\t").append("var curindex = parseInt($G(\"nodesnum" + groupid_tmp + "\").value);").append("\n");
          addJsSb.append("\t").append("if($G('submitdtlid" + groupid_tmp + "').value==''){").append("\n");
          addJsSb.append("\t\t").append("$G('submitdtlid" + groupid_tmp + "').value=rowindex;").append("\n");
          addJsSb.append("\t").append("}else{").append("\n");
          addJsSb.append("\t\t").append("$G('submitdtlid" + groupid_tmp + "').value+=\",\"+rowindex;").append("\n");
          addJsSb.append("\t").append("}").append("\n");
          addJsSb.append("\t").append("var oRow;").append("\n");
          addJsSb.append("\t").append("var oCell;").append("\n");
          addJsSb.append("\t").append("var oDiv;").append("\n");
          addJsSb.append("\t").append("var sHtml;").append("\n");

          delJsSb.append("\t").append("try{").append("\n");
          delJsSb.append("var flag = false;\n");
          delJsSb.append("\tvar ids = document.getElementsByName(\"check_node_\"+groupid);\n");
          delJsSb.append("\tfor(i=0; i<ids.length; i++) {\n");
          delJsSb.append("\t\tif(ids[i].checked==true) {\n");
          delJsSb.append("\t\t\tflag = true;\n");
          delJsSb.append("\t\t\tbreak;\n");
          delJsSb.append("\t\t}\n");
          delJsSb.append("\t}\n");
          
          //SAP带值函数内部调用deleterow方法不提示
          delJsSb.append("\tif(isfromsap){flag=true;}\n");
          
          delJsSb.append("    if(flag) {\n");
          delJsSb.append("\t\tif(isfromsap || isdel()){\n");
          //模板解析方案1生成的脚本，获取最外层Table，然后添加行，使之能够对齐
          if (htmlParseScheme == 1) {
        	  delJsSb.append("\t\t").append("var oTable=$G('oTable' + groupid);").append("\n");
          } else {
              delJsSb.append("\t\t").append("var oTable=$G('detailFieldTable" + firstDetailFieldid + "');").append("\n");
          }
          delJsSb.append("\t\t").append("var len = document.forms[0].elements.length;").append("\n");
          delJsSb.append("\t\t").append("var curindex=parseInt($G(\"nodesnum\"+groupid).value);").append("\n");
          delJsSb.append("\t\t").append("var i=0;").append("\n");
		  if(htmlParseScheme == 1){
			  delJsSb.append("\t").append("var thead = jQuery('#oTable"+groupCount+"').find('tr.detailtitle').size();\nif(thead==null||thead==undefined||thead==0)thead=1;").append("\n");
			  delJsSb.append("\t\t").append("var rowsum1 = thead-1;").append("\n");
		  }else{
			delJsSb.append("\t\t").append("var rowsum1 = 0;").append("\n");
		  }
          delJsSb.append("\t\t").append("var objname = \"check_node_\"+groupid;").append("\n");
          delJsSb.append("\t\t").append("for(i=len-1; i >= 0;i--) {").append("\n");
          delJsSb.append("\t\t\t").append("if (document.forms[0].elements[i].name==objname){").append("\n");
          delJsSb.append("\t\t\t\t").append("rowsum1 += 1;").append("\n");
          delJsSb.append("\t\t\t").append("}").append("\n");
          delJsSb.append("\t\t").append("}").append("\n");
          delJsSb.append("\t\t").append("for(i=len-1; i>=0; i--) {").append("\n");
          delJsSb.append("\t\t\t").append("if(document.forms[0].elements[i].name==objname){").append("\n");
          // delJsSb.append("\t\t\t\t").append("rowsum1--;").append("\n");
          delJsSb.append("\t\t\t\t").append("if(document.forms[0].elements[i].checked==true){").append("\n");

          delJsSb.append("\t\t\t\t\t").append("var nodecheckObj;").append("\n");
          delJsSb.append("\t\t\t\t\t\t").append("var delid;").append("\n");
          delJsSb.append("\t\t\t\t\t").append("try{").append("\n");
          delJsSb.append("\t\t\t\t\t\t").append("for(var cc=0; cc<jQuery(oTable.rows[rowsum1].cells[0]).children().eq(0).children().eq(0).children().length; cc++){").append("\n");

          delJsSb.append("\t\t\t\t\t\t\t").append("if(jQuery(oTable.rows[rowsum1].cells[0]).children().eq(0).children().eq(0).children()[cc].tagName==\"INPUT\"){").append("\n");

          delJsSb.append("\t\t\t\t\t\t\t\t").append("nodecheckObj = jQuery(oTable.rows[rowsum1].cells[0]).children().eq(0).children().eq(0).children()[cc];").append("\n");

          delJsSb.append("\t\t\t\t\t\t").append("delid = nodecheckObj.value;").append("\n");

          delJsSb.append("\t\t\t\t\t\t\t").append("}").append("\n");
          delJsSb.append("\t\t\t\t\t\t").append("}").append("\n");
          delJsSb.append("\t\t\t\t\t").append("}catch(e){}").append("\n");
          delJsSb.append("\t\t\t\t\t").append("//记录被删除的旧记录 id串").append("\n");
          delJsSb.append("\t\t\t\t\t").append("if(jQuery(oTable.rows[rowsum1].cells[0]).children().length>0 && jQuery(jQuery(oTable.rows[rowsum1].cells[0]).children()[0]).children().length>1){").append("\n");
          delJsSb.append("\t\t\t\t\t\t").append("if($G(\"deldtlid\"+groupid).value!=''){").append("\n");
          delJsSb.append("\t\t\t\t\t\t\t").append("//老明细").append("\n");
          delJsSb.append("\t\t\t\t\t\t\t").append("$G(\"deldtlid\"+groupid).value+=\",\"+jQuery(oTable.rows[rowsum1].cells[0].children[0]).children()[1].value;").append("\n");
          delJsSb.append("\t\t\t\t\t\t").append("}else{").append("\n");
          delJsSb.append("\t\t\t\t\t\t\t").append("//新明细").append("\n");
          delJsSb.append("\t\t\t\t\t\t\t").append("$G(\"deldtlid\"+groupid).value=jQuery(oTable.rows[rowsum1].cells[0]).children().eq(0).children()[1].value;").append("\n");
          delJsSb.append("\t\t\t\t\t\t").append("}").append("\n");
          delJsSb.append("\t\t\t\t\t").append("}").append("\n");
          delJsSb.append("\t\t\t\t\t").append("//从提交序号串中删除被删除的行").append("\n");
          delJsSb.append("\t\t\t\t\t").append("var submitdtlidArray=$G(\"submitdtlid\"+groupid).value.split(',');").append("\n");
          delJsSb.append("\t\t\t\t\t").append("$G(\"submitdtlid\"+groupid).value=\"\";").append("\n");
          delJsSb.append("\t\t\t\t\t").append("var k;").append("\n");
          delJsSb.append("\t\t\t\t\t").append("for(k=0; k<submitdtlidArray.length; k++){").append("\n");
          delJsSb.append("\t\t\t\t\t\t").append("if(submitdtlidArray[k]!=delid){").append("\n");
          delJsSb.append("\t\t\t\t\t\t\t").append("if($G(\"submitdtlid\"+groupid).value==''){").append("\n");
          delJsSb.append("\t\t\t\t\t\t\t\t").append("$G(\"submitdtlid\"+groupid).value = submitdtlidArray[k];").append("\n");
          delJsSb.append("\t\t\t\t\t\t\t").append("}else{").append("\n");
          delJsSb.append("\t\t\t\t\t\t\t\t").append("$G(\"submitdtlid\"+groupid).value += \",\"+submitdtlidArray[k];").append("\n");
          delJsSb.append("\t\t\t\t\t\t\t").append("}").append("\n");
          delJsSb.append("\t\t\t\t\t\t").append("}").append("\n");
          delJsSb.append("\t\t\t\t\t").append("}").append("\n");
          
          //QC49273
          ArrayList detailRealFieldOrder = new ArrayList();//节点模板字段显示顺序数组
          //明细列的值（包括合计）
          List detailTableValues = new ArrayList();
          int orderpos = -1;
          //用于计算明细列初始位置
          String orderHtmlLayout = htmlLayout;
          
          if (htmlParseScheme == 1) {
	          //addrow从字段循环内放到外面
	          addJsSb.append("\t").append("").append("\n");
			  addJsSb.append("\t").append("var thead = jQuery('#oTable"+groupCount+"').find('tr.detailtitle').size();\nif(thead==null||thead==undefined||thead==0)thead=1;").append("\n");
	          addJsSb.append("\t").append("oRow = $G('oTable" + groupCount + "').insertRow(curindex+thead);").append("\n");
	          
	          //显示顺序排序
	          Collections.sort(detailFieldOrder);
			  
	          //1、先获取节点模板中设置的字段按显示顺序依次放入数组
	          for (int i = 0; i < detailFieldOrder.size(); i++) {
	          	  detailRealFieldOrder.add((String)detailFieldOrderMap.get(detailFieldOrder.get(i)));
	          }
	          //2、获取节点模板中未设置的字段放入数组
	          for (int i = 0; i < detailFieldList.size(); i++) {
	          	  if(!detailRealFieldOrder.contains((String)detailFieldList.get(i))) {
	          	  	  detailRealFieldOrder.add(detailFieldList.get(i));
	          	  }
	          }
          }
          
          pos = -1;
          String content1 = "";
          String contentStart = "";
          String contentEnd = "";
          String content2 = "";
          boolean detailTabRowFirstCol = true;
          //QC49273 节点模板字段显示顺序数组循环
          List iteratorList = null;
          if (htmlParseScheme == 1) {
        	  iteratorList = detailRealFieldOrder;
          } else {
        	  iteratorList = detailFieldList;
          }
          for (int i = 0; i < iteratorList.size(); i++) {
            int fieldid_tmp = Util.getIntValue((String) iteratorList.get(i), 0);
            String fieldname_tmp = Util.null2String((String) fieldname_hs.get("fieldname" + fieldid_tmp));
            int fieldhtmltype_tmp = Util.getIntValue((String) fieldhtmltype_hs.get("fieldhtmltype" + fieldid_tmp), 0);
            int type_tmp = Util.getIntValue((String) fieldtype_hs.get("fieldtype" + fieldid_tmp), 0);
            String fielddbtype_tmp = Util.null2String((String) fielddbtype_hs.get("fielddbtype" + fieldid_tmp));
            int isview_tmp = Util.getIntValue((String) isview_hs.get("isview" + fieldid_tmp), 0);
            int isedit_tmp = Util.getIntValue((String) isedit_hs.get("isedit" + fieldid_tmp), 0);
            int ismand_tmp = Util.getIntValue((String) ismand_hs.get("ismand" + fieldid_tmp), 0);
            if (mustNoEdit == 1) {
              isedit_tmp = 0;
              ismand_tmp = 0;
            }
            String fieldlabel_tmp = Util.null2String((String) fieldlabel_hs.get("fieldlabel" + fieldid_tmp));
            int fieldlength_tmp = 0;
            int fieldheight_tmp = 4;
            if (fieldhtmltype_tmp == 1 && type_tmp == 1) {// 单文本中的文本
              if ((fielddbtype_tmp.toLowerCase()).indexOf("varchar") > -1) {
                fieldlength_tmp = Util.getIntValue(fielddbtype_tmp.substring(fielddbtype_tmp.indexOf("(") + 1, fielddbtype_tmp.length() - 1));
              }
            } else if (fieldhtmltype_tmp == 2) {// 多行文本框
              if (isbill == 0) {
                rs_tmp.execute("select textheight from workflow_formdictdetail where id=" + fieldid_tmp);
              } else {
                rs_tmp.execute("select textheight from workflow_billfield where id=" + fieldid_tmp + " and billid=" + formid);
              }
              if (rs_tmp.next()) {
                fieldheight_tmp = Util.getIntValue(rs_tmp.getString("textheight"), 4);
                otherPara_hs.put("fieldheight", "" + fieldheight_tmp);
              }
            }
            otherPara_hs.put("fielddbtype", fielddbtype_tmp);
            pos = htmlLayout.toLowerCase().indexOf("$field" + fieldid_tmp + "$");
            
            if (htmlParseScheme == 1) {
            	orderpos = orderHtmlLayout.toLowerCase().indexOf("$field" + fieldid_tmp + "$");
            }
            if (pos > -1) {
              content1 = wfformhtml.substring(0, pos);
              content2 = wfformhtml.substring(pos + 1);
              int pos1 = content1.lastIndexOf("<");
              if (htmlParseScheme == 1) {
	              int pos2 = content2.toLowerCase().indexOf("</td>");
	              if (firstDetailFieldid == fieldid_tmp) {
	            	  pos1 = content1.toLowerCase().lastIndexOf("<tr");
	              }
	              
	              if (lastDetailFieldId == fieldid_tmp) {
	            	  pos2 = content2.toLowerCase().indexOf("</tr>");
	              }
	              
	              if (pos1 > -1) {
	                content1 = content1.substring(0, pos1);
	            		if (firstDetailFieldid == fieldid_tmp) {
	            			contentStart = content1;
	              }
	              }
	              if (pos2 > -1) {
	                content2 = content2.substring(pos2 + 5);
	                if (lastDetailFieldId == fieldid_tmp) {
	                	contentEnd = content2;
	        		}
	                
	              }
              } else {
              int pos2 = content2.indexOf(">");
              if (pos1 > -1) {
                content1 = content1.substring(0, pos1);
              }
              if (pos2 > -1) {
                content2 = content2.substring(pos2 + 1);
                  }
              }
              // 这里拼字段
              String inputStr_tmp = "";
              
              if (htmlParseScheme != 1) {
            	  inputStr_tmp = "\n<table class=\"ListStyle\" id=\"detailFieldTable"+fieldid_tmp+"\" name=\"detailFieldTable"+fieldid_tmp+"\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"wfDetailFieldTable\" style=\"margin:0px;padding:0px;margin-bottom:4px!important;\">\n";
                  inputStr_tmp += "<tbody>\n";
                  inputStr_tmp += "<tr height=\"0\" class=\"wfDetailTopRow\" style=\"display: none;\"><td class=\"wfDetailTopCell\"  style=\"margin:0px;padding:0px;heigth:0px;\">" + "</td></tr>\n";
              }
              
              rs_oldDetail.beforFirst();
              boolean isttLight = false;
              submitdtlid = "";
              derecorderindex = 0;
              Map detailCellInfoBean = new HashMap();
              List detailFieldValues = new ArrayList();
              
              // 这里取以前已经有的明细字段的值
              while (rs_oldDetail.next()) {
                String fieldvalue_tmp = Util.null2String(rs_oldDetail.getString(fieldname_tmp));
                isttLight = !isttLight;
                
                if (htmlParseScheme == 1) {
                	inputStr_tmp = "";
                }
                
                if(isttLight == true){
                	if (htmlParseScheme == 1) {
                		inputStr_tmp += "<td class=\"detailfield\">";
                	} else {
                		inputStr_tmp += "<tr class=\"detailfield\"><td class=\"detailfield\">";
                	}
				}else{
					if (htmlParseScheme == 1) {
						inputStr_tmp += "<td class=\"detailfield\">";
					} else {
						inputStr_tmp += "<tr class=\"detailfield\"><td class=\"detailfield\">";
					}
				}
                if (ismand_tmp == 1 && fieldhtmltype_tmp != 4) {
                  needcheck += ",field" + fieldid_tmp + "_" + derecorderindex + "";
                }
                if (firstDetailFieldid == fieldid_tmp) {
                  inputStr_tmp += "<div id=\"firstFielddiv" + fieldid_tmp + "\"><span name=\"detailCheckSpan" + groupid_tmp + "\"><input type=\"checkbox\" name=\"check_node_" + groupid_tmp + "\" value=\"" + derecorderindex + "\" ";
                  if (isviewonly == 1 || !"1".equals(dtldelete)) {
                    inputStr_tmp += " disabled ";
                  }
                  inputStr_tmp += ">&nbsp;&nbsp;</span>";
                  inputStr_tmp += "<input type=\"hidden\" name=\"dtl_id_" + groupid_tmp + "_" + derecorderindex + "\" value=\"" + rs_oldDetail.getString("id") + "\">";
                //因Firefox和Chrome两浏览器下边，span标签不支持width样式，故使每行表头与表单中
                  //第一个字段数据紧挨在一起。现去掉width样式，并在表头后边加上三个空格转义符。
                  inputStr_tmp += "<span name=\"detailIndexSpan" + groupid_tmp + "\" style=\"padding-top:2px;\">" + (derecorderindex + 1) + "&nbsp;&nbsp;&nbsp;</span>";
                }
                try {
                  int isedit_value = isedit_tmp;
                  if (!"1".equals(dtledit)) {
                    isedit_value = 0;
                  }
                  otherPara_hs.put("derecorderindex", "" + derecorderindex);
                  object = (HtmlElement) Class.forName(fieldTypeComInfo.getClassname("" + fieldhtmltype_tmp)).newInstance();
                  Hashtable ret_hs = object.getHtmlElementString(fieldid_tmp, fieldname_tmp, type_tmp, fieldlabel_tmp, fieldlength_tmp, 1, groupid_tmp, fieldvalue_tmp, isviewonly, 1, isedit_value, ismand_tmp, user, otherPara_hs);
                  inputStr_tmp += Util.null2String((String) ret_hs.get("inputStr"));
                  if (firstDetailFieldid == fieldid_tmp) {
                    inputStr_tmp += "</div>";
                  }
                  addJsStr += Util.null2String((String) ret_hs.get("addJsStr"));
                  hiddenElementStr += Util.null2String((String) ret_hs.get("hiddenElementStr"));
                  // retStr += inputStr_tmp;
                  if (htmlParseScheme != 1) {
                  	wfformhtml = content1 + inputStr_tmp + content2;
                  	htmlLayout = wfformhtml;
                  }
                  String jsStr_t = Util.null2String((String) ret_hs.get("jsStr"));
                  jsStr.append("\n").append(jsStr_t).append("\n");
                } catch (Exception e) {
                  writeLog(e);
                }
                
                if (htmlParseScheme == 1) {
	                inputStr_tmp += "</td>\n";
	                detailFieldValues.add(inputStr_tmp);
                } else {
                	inputStr_tmp += "&nbsp;</td></tr>\n";
                }
                if ("".equals(submitdtlid)) {
                  submitdtlid = "" + derecorderindex;
                } else {
                  submitdtlid += "," + derecorderindex;
                }
                derecorderindex++;
              }

              nodesnum = derecorderindex;
              if (htmlParseScheme == 1) {
            	  inputStr_tmp = "";
              } else {
            	  inputStr_tmp += "\n</tbody>\n";
              }
              // 需要合计的话，在这里多加一个<tfoot> Start
              if (defshowsum) {
            	  if (htmlParseScheme != 1) {
            		  inputStr_tmp += "\n<tfoot>\n";
            		  inputStr_tmp += "<tr class=\"header\">\n";
            	  }
                if (firstDetailFieldid == fieldid_tmp) {
                  inputStr_tmp += "<td>" + SystemEnv.getHtmlLabelName(358, user.getLanguage()) + "\n";
                } else {
                  inputStr_tmp += "<td>\n";
                }
                inputStr_tmp += "<span id=\"sum" + fieldid_tmp + "\" style=\"color:#ff0000\"></span>\n";
                inputStr_tmp += "<input type=\"hidden\" id=\"sumvalue" + fieldid_tmp + "\" name=\"sumvalue" + fieldid_tmp + "\">";
                inputStr_tmp += "</td>\n";
                if (htmlParseScheme != 1) {
                    inputStr_tmp += "</tr>\n";
                    inputStr_tmp += "\n</tfoot>\n";
                }
              }
              if (htmlParseScheme == 1) {
	              detailCellInfoBean.put("cellValues", detailFieldValues);
	              detailCellInfoBean.put("cellSum", inputStr_tmp);
	              //detailFieldValues.add(detailCellInfoBean);
	              //将列值放入列表（按照显示位置放入），等待所有列都处理完毕后，统一做显示处理
	              int tempOrder = detailFieldOrder.indexOf(new Integer(orderpos));
	              if (tempOrder != -1) {
	            	  if (detailTableValues.size() <= tempOrder) {
	            		  for (int k=detailTableValues.size(); k<=tempOrder; k++) {
	            			  detailTableValues.add(null);
	            		  }
	            	  }
	            	  detailTableValues.set(tempOrder, detailCellInfoBean);
	              } else {
	            	  detailTableValues.add(detailCellInfoBean);
	              }
              }
              // 需要合计的话，在这里多加一个</tfoot> End
              if (htmlParseScheme != 1) {
            	  inputStr_tmp += "</table>";
            	  wfformhtml = content1 + inputStr_tmp + content2;
              } else {
            	  wfformhtml = content1 + content2;
              }
              htmlLayout = wfformhtml;

              if (isviewonly == 0) {// 不是仅查看的情况下才去拼JS方法。方法头那里不去控制了，下面的控制下，节约速度
                if (ismand_tmp == 1 && fieldhtmltype_tmp != 4) {
                  needcheck_tmp += ",field" + fieldid_tmp + "_\"+rowindex+\"";
                }
                // 这里拼JS方法
                // addRow
                if (htmlParseScheme != 1) {
                	addJsSb.append("\t").append("").append("\n");
                    addJsSb.append("\t").append("oRow = detailFieldTable" + fieldid_tmp + ".insertRow(curindex+1);").append("\n");
                }
                addJsSb.append("\t").append("oCell = oRow.insertCell(-1);").append("\n");
				addJsSb.append("\t").append("oCells.push(oCell)").append("\n");
                addJsSb.append("\t").append("oCell.className = \"detailfield\";").append("\n");
                addJsSb.append("\t").append("oDiv = document.createElement(\"div\");").append("\n");
                addJsSb.append("\t").append("sHtml=\"\";").append("\n");
                if (firstDetailFieldid == fieldid_tmp) {
                  addJsSb.append("\t").append("sHtml += \"<span name='detailCheckSpan" + groupid_tmp + "'><input type='checkbox' name='check_node_" + groupid_tmp + "' value='\"+rowindex+\"'>&nbsp;&nbsp;</span>\";").append("\n");
                  addJsSb.append("\t").append("sHtml += \"<input type='hidden' name='dtl_id_" + groupid_tmp + "' value=''>\";").append("\n");
                  addJsSb.append("\t").append("sHtml += \"<span name='detailIndexSpan" + groupid_tmp + "' style='width:20px;padding-top:2px;'>\"+(curindex+1)+\"</span>\";").append("\n");
                }
                // addJsSb.append("\t").append("sHtml += \""+fieldid_tmp+"+\"+curindex;").append("\n");//该句为开发调试使用

                // 这里获得字段用于AddRow的sHtml的值
                try {	   // 为防字段循环顺序问题，把找被代理人的方法提前
     int agenttype = -1; //流程创建代理预设
	 String dt_beagenter = ""+user.getUID();
      // 获得被代理人
      rs.executeSql("select agentorbyagentid,agenttype from workflow_currentoperator where usertype=0 and isremark='0' and requestid=" + requestid + " and userid=" + user.getUID() + " and nodeid=" + nodeid + " order by id desc");
      if (rs.next()) {
        int tembeagenter = rs.getInt(1);
		 agenttype = rs.getInt(2);
        if (tembeagenter > 0) {
          dt_beagenter =""+tembeagenter;
        }
      }
	  int body_isagent=Util.getIntValue((String)session.getAttribute(workflowid+"isagent"+user.getUID()),0);
			if(body_isagent==1){				dt_beagenter=""+Util.getIntValue((String)session.getAttribute(workflowid+"beagenter"+user.getUID()),0);
			
      }
                  String fieldvalue_tmp = "";
                  String inoperatefield_tmp = Util.null2String((String) inoperatefield_hs.get("inoperatefield" + fieldid_tmp));
                  if ("1".equals(inoperatefield_tmp)) {
                    fieldvalue_tmp = Util.null2String((String) fieldvalue_hs.get("inoperatevalue" + fieldid_tmp));
                  } else {// 没有设置节点前附加操作
                    if (fieldhtmltype_tmp == 3) {
                      if ((type_tmp == 8 || type_tmp == 135) && !prjid.equals("")) { // 浏览按钮为项目,从前面的参数中获得项目默认值
                        fieldvalue_tmp = "" + Util.getIntValue(prjid, 0);
                      } else if ((type_tmp == 9 || type_tmp == 37) && !docid.equals("")) { // 浏览按钮为文档,从前面的参数中获得文档默认值
                        fieldvalue_tmp = "" + Util.getIntValue(docid, 0);
                      } else if ((type_tmp == 1 || type_tmp == 17 || type_tmp == 165 || type_tmp == 166) && !hrmid.equals("") && body_isagent ==1 && agenttype != 0) { //代理， 浏览按钮为人,从前面的参数中获得人默认值
                        fieldvalue_tmp = "" + Util.getIntValue(dt_beagenter, 0);
                      } else if ((type_tmp == 1 || type_tmp == 17 || type_tmp == 165 || type_tmp == 166) && !hrmid.equals("")) { // 浏览按钮为人,从前面的参数中获得人默认值
                        fieldvalue_tmp = "" + Util.getIntValue(hrmid, 0);
                      }else if ((type_tmp == 7 || type_tmp == 18) && !crmid.equals("")) { // 浏览按钮为CRM,从前面的参数中获得CRM默认值
                        fieldvalue_tmp = "" + Util.getIntValue(crmid, 0);
                      } else if ((type_tmp == 16 || type_tmp == 152 || type_tmp == 171) && !reqid.equals("")) { // 浏览按钮为REQ,从前面的参数中获得REQ默认值
                        fieldvalue_tmp = "" + Util.getIntValue(reqid, 0);
                      }  else if ((type_tmp == 4 || type_tmp == 57 || type_tmp == 167 || type_tmp == 168) && !hrmid.equals("") && body_isagent ==1 && agenttype != 0) { //代理， 浏览按钮为部门,从前面的参数中获得人默认值(由人力资源的部门得到部门默认值)
                        fieldvalue_tmp = "" + Util.getIntValue(resourceComInfo.getDepartmentID(dt_beagenter), 0);
                      } else if ((type_tmp == 4 || type_tmp == 57 || type_tmp == 167 || type_tmp == 168) && !hrmid.equals("")) { // 浏览按钮为部门,从前面的参数中获得人默认值(由人力资源的部门得到部门默认值)
                        fieldvalue_tmp = "" + Util.getIntValue(resourceComInfo.getDepartmentID(hrmid), 0);
                      } else if (type_tmp == 24 && !hrmid.equals("")) { // 浏览按钮为职务,从前面的参数中获得人默认值(由人力资源的职务得到职务默认值)
                        fieldvalue_tmp = "" + Util.getIntValue(resourceComInfo.getJobTitle(hrmid), 0);
                      } else if (type_tmp == 32 && !hrmid.equals("")) { // 浏览按钮为职务,从前面的参数中获得人默认值(由人力资源的职务得到职务默认值)
                        fieldvalue_tmp = "" + Util.getIntValue(request.getParameter("TrainPlanId"), 0);
                      } else if ((type_tmp == 164 || type_tmp == 169 || type_tmp == 170) && !hrmid.equals("") && body_isagent==1 && agenttype != 0 ) { // 代理新建，浏览按钮为分部,从前面的参数中获得人默认值(由人力资源的分部得到分部默认值)
                        fieldvalue_tmp = "" + Util.getIntValue(resourceComInfo.getSubCompanyID(dt_beagenter), 0);
                      } else if ((type_tmp == 164 || type_tmp == 169 || type_tmp == 170) && !hrmid.equals("") ) { // 浏览按钮为分部,从前面的参数中获得人默认值(由人力资源的分部得到分部默认值)
                        fieldvalue_tmp = "" + Util.getIntValue(resourceComInfo.getSubCompanyID(hrmid), 0);
                      } else if (type_tmp == 2) {// 日期
                        fieldvalue_tmp = TimeUtil.getCurrentDateString();
                      } else if (type_tmp == 19) {// 时间
                        fieldvalue_tmp = TimeUtil.getCurrentTimeString().substring(11, 16);
                      }
                    }
                  }
                  int isedit_value = isedit_tmp;
                  otherPara_hs.put("derecorderindex", "\"+rowindex+\"");
                  object = (HtmlElement) Class.forName(fieldTypeComInfo.getClassname("" + fieldhtmltype_tmp)).newInstance();
                  // 这里的isviewonly肯定是0
                  Hashtable ret_hs = object.getHtmlElementString(fieldid_tmp, fieldname_tmp, type_tmp, fieldlabel_tmp, fieldlength_tmp, 1, groupid_tmp, fieldvalue_tmp, 0, 1, isedit_value, ismand_tmp, user, otherPara_hs);
                  String addRowElement = Util.null2String((String) ret_hs.get("inputStr"));
                  addJsSb.append("\t").append("sHtml += \"" + addRowElement + "\";").append("\n");
                  addJSExt += Util.null2String((String) ret_hs.get("jsStr"));// 类似SQL初始化之类的JS操作
                  addJsStr += Util.null2String((String) ret_hs.get("addJsStr"));
                  hiddenElementStr += Util.null2String((String) ret_hs.get("hiddenElementStr"));
                } catch (Exception e) {
                  writeLog(e);
                }
                // addJsSb.append("\t").append("sHtml += \"&nbsp;\";").append("\n");
                addJsSb.append("\t").append("sHtml += \"&nbsp;\";").append("\n");
                addJsSb.append("\t").append("oDiv.innerHTML = sHtml;").append("\n");
                addJsSb.append("\t").append("oCell.appendChild(oDiv);").append("\n");

                if (trrigerdetailfield.indexOf("field" + fieldid_tmp) >= 0) {
                  addJsSb.append("\t").append("initDetailfields+=\"field" + fieldid_tmp + "_\"+rowindex+\",\"").append("\n");
                }

                // deleteRow
                if (htmlParseScheme != 1) {
                	delJsSb.append("\t\t\t\t\t").append("detailFieldTable" + fieldid_tmp + ".deleteRow(rowsum1);").append("\n");
                }
              }
            } else {
              // System.out.println("fieldid_tmp:"+fieldid_tmp+"		fieldname_tmp:"+fieldname_tmp);
              // 这里拼字段
              String inputStr_tmp = "\n<table class=\"ListStyle\" id=\"detailFieldTable" + fieldid_tmp + "\" name=\"detailFieldTable" + fieldid_tmp + "\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" >\n";
              inputStr_tmp += "<tbody>\n";
              inputStr_tmp += "<tr height=\"0\"><td>" + "</td></tr>\n";
              rs_oldDetail.beforFirst();
              boolean isttLight = false;
              submitdtlid = "";
              derecorderindex = 0;
              // 这里取以前已经有的明细字段的值
              while (rs_oldDetail.next()) {
                String fieldvalue_tmp = Util.null2String(rs_oldDetail.getString(fieldname_tmp));
                // System.out.println("fieldid_tmp:"+fieldid_tmp+"		fieldname_tmp:"+fieldname_tmp+"		fieldvalue_tmp:"+fieldvalue_tmp);
                // System.out.println("firstDetailFieldid:"+firstDetailFieldid+"	fieldid_tmp:"+fieldid_tmp);
                // if(firstDetailFieldid == fieldid_tmp){
                htmlHiddenElementsb.append("\n").append("<input type=\"hidden\" id=\"field" + fieldid_tmp + "_" + derecorderindex + "\" name=\"field" + fieldid_tmp + "_" + derecorderindex + "\" value=\"" + fieldvalue_tmp + "\" isShow=\"0\">" + "\n");
                if (firstDetailFieldid == 0 && detailTabRowFirstCol) {
                	htmlHiddenElementsb.append("\n").append("<input type=\"hidden\" name=\"dtl_id_" + groupid_tmp + "_" + derecorderindex + "\" value=\"" + rs_oldDetail.getString("id") + "\">");
                }
                detailFieldNotShow += "," + fieldid_tmp;
                // }
                if ("".equals(submitdtlid)) {
                  submitdtlid = "" + derecorderindex;
                } else {
                  submitdtlid += "," + derecorderindex;
                }
                derecorderindex++;
              }
            }
            detailTabRowFirstCol = false;
          }
          if(!"".equals(detailFieldNotShow)) {
        	  detailFieldNotShow = detailFieldNotShow.substring(1);
          }
          
          //外层循环行，内层循环列
          String inputStr_tmp = "";
          if (htmlParseScheme == 1 && detailTableValues.size() > 0) {
        	  List tempFieldValues = (List)((Map)detailTableValues.get(0)).get("cellValues");
        	  for (int i=0; i<tempFieldValues.size(); i++) {
        		  inputStr_tmp += "<TR>";
        		  
        		  for (int j=0; j<detailTableValues.size(); j++) {
        			  List temList = (List)((Map)detailTableValues.get(j)).get("cellValues");
        			  inputStr_tmp += temList.get(i);
        		  }
        		  
        		  inputStr_tmp += "</TR>";
        	  }
        	  String footString = "\n<tfoot>\n";
        	  footString += "<tr class=\"header\">\n";

        	  boolean hasDetailSum = false;
        	  //明细合计
        	  for (int j=0; j<detailTableValues.size(); j++) {
        		  String sumString = (String)((Map)detailTableValues.get(j)).get("cellSum");
        		  if (sumString != null && !"".equals(sumString)) {
        			  footString += sumString;
        			  hasDetailSum = true;
        			  continue;
        		  } 
        		  footString += "<td></td>";
        	  }
        	  footString += "</tr>\n\n</tfoot>\n";
        	  
        	  if (hasDetailSum) {
        		  inputStr_tmp += footString;
        	  }
        	  wfformhtml = contentStart + inputStr_tmp + contentEnd;
              htmlLayout = wfformhtml;
            //删除移到循环外
              if (isviewonly == 0) {
            	  delJsSb.append("\t\t\t\t\t").append("oTable" + groupCount + ".deleteRow(rowsum1);").append("\n");
              }
          }
        }
		if("1".equals(useNew)){
			//第一次修正
			addJsSb.append("\t").append("window.setTimeout(function(){checkHeight(oCells); },100);\n\t");
		}
        addJsSb.append("\t").append("datainputd(initDetailfields);").append("\n");
        addJsSb.append("\t").append("$G(\"indexnum" + groupid_tmp + "\").value = rowindex*1 + 1;").append("\n");
        addJsSb.append("\t").append("$G(\"nodesnum" + groupid_tmp + "\").value = curindex*1 + 1;").append("\n");
        addJsSb.append("\t").append(addJsStr).append("\n");
        // 必填控制
        if (!"".equals(needcheck_tmp)) {
          addJsSb.append("\t").append("$G(\"needcheck\").value += \"," + needcheck_tmp + "\";").append("\n");
        }

        addJsSb.append("\t").append("try{").append("\n");
        addJsSb.append("\t\t").append(addJSExt).append("\n");
        addJsSb.append("\t").append("}catch(e){}").append("\n");
        addJsSb.append("\t").append("try{").append("\n");
        addJsSb.append("\t\t").append("calSum(groupid);").append("\n");
        addJsSb.append("\t").append("}catch(e){}").append("\n");
        //if("1".equals(useNew)){
        	//第二次修正
		//	addJsSb.append("\t").append("window.setTimeout(function(){checkHeight(oCells); },100);\n\t");
		//}
        //第二次修正放在字段数据后执行
        addJsSb.append("}").append("\n");// addRow终结

        delJsSb.append("\t\t\t\t\t").append("curindex--;").append("\n");
        delJsSb.append("\t\t\t\t").append("}").append("\n");
        delJsSb.append("\t\t\t\t").append("rowsum1--;").append("\n");
        delJsSb.append("\t\t\t").append("}").append("\n");
        delJsSb.append("\t\t").append("}").append("\n");
        delJsSb.append("\t\t").append("$G(\"nodesnum\"+groupid).value=curindex;").append("\n");
        delJsSb.append("\t\t\t").append("calSum(groupid);").append("\n");
        // delJsSb.append("\t").append("}catch(e){alert(e);}").append("\n");//开发调试时使用。正式使用用下面那句
        delJsSb.append("}\n");
        delJsSb.append("}else{\n");
        delJsSb.append("        alert('" + SystemEnv.getHtmlLabelName(22686, user.getLanguage()) + "');\n");
        delJsSb.append("\t\treturn;\n");
        delJsSb.append("    }");
        delJsSb.append("\t").append("}catch(e){}").append("\n");
        // 修改序号 Start
        delJsSb.append("\t").append("try{").append("\n");
        delJsSb.append("\t\t").append("var indexNum = jQuery(\"span[name='detailIndexSpan" + groupid_tmp + "']\").length;").append("\n");
        delJsSb.append("\t\t").append("for(var k=1; k<=indexNum; k++){").append("\n");
        delJsSb.append("\t\t\t").append("jQuery(\"span[name='detailIndexSpan" + groupid_tmp + "']\").get(k-1).innerHTML = k;").append("\n");
        delJsSb.append("\t\t").append("}").append("\n");
        delJsSb.append("\t").append("}catch(e){}").append("\n");
        // 修改序号 End

        delJsSb.append("}").append("\n");// deleteRow终结
        jsStr.append(addJsSb.toString()).append("\n").append(delJsSb.toString()).append("\n");
        //抽取高度修正方法
        jsStr.append("\t").append("function checkHeight(oCells){var maxHeight = 0;\n\tfor(var _i=0;_i<oCells.length;_i++){var height = jQuery(oCells[_i]).height();if(height>maxHeight)maxHeight = height;}if(oCells[0]){oCells[0].___maxH=maxHeight+5;}for(var _i=0;_i<oCells.length;_i++){\n\tjQuery(oCells[_i]).height(maxHeight+5);\n\t}\n\twindow.setTimeout(function(){rowProcessing = false},100); };\n\t");
        
        jsStr.append(addSAPJsSb);//zzl--添加sap多选浏览按钮触发的js方法
        
        
        // 先这样写，等处理编辑页面时再修改
        htmlHiddenElementsb.append("<input type=\"hidden\" id=\"nodesnum" + groupid_tmp + "\" name=\"nodesnum" + groupid_tmp + "\" value=\"" + nodesnum + "\">").append("\n");
        htmlHiddenElementsb.append("<input type=\"hidden\" id=\"rowneed" + groupid_tmp + "\" name=\"rowneed" + groupid_tmp + "\" value=\"" + dtlneed + "\">").append("\n");
        htmlHiddenElementsb.append("<input type=\"hidden\" id=\"indexnum" + groupid_tmp + "\" name=\"indexnum" + groupid_tmp + "\" value=\"" + derecorderindex + "\">").append("\n");
        htmlHiddenElementsb.append("<input type=\"hidden\" id=\"submitdtlid" + groupid_tmp + "\" name=\"submitdtlid" + groupid_tmp + "\" value=\"" + submitdtlid + "\">").append("\n");
        htmlHiddenElementsb.append("<input type=\"hidden\" id=\"deldtlid" + groupid_tmp + "\" name=\"deldtlid" + groupid_tmp + "\" value=\"\">").append("\n");
        htmlHiddenElementsb.append("<input type=\"hidden\" id=\"colcalnames\" name=\"colcalnames\" value=\"\">").append("\n");
        htmlHiddenElementsb.append(hiddenElementStr).append("\n");

        if (dtldefault.equals("1") && nodesnum < 1 && !(isaffirmancebody == 1 && reEditbody == 0 && nodetype==0)) {
          addrowstrtmp += "addRow" + groupid_tmp + "(0);";
          rs_tmp.execute("select defaultrows from workflow_NodeFormGroup where nodeid="+nodeid+" and groupid="+groupid_tmp);
          rs_tmp.next();
          int defaultrows = Util.getIntValue(rs_tmp.getString("defaultrows"),0);
          for(int k=0;k<(defaultrows-1);k++){
        	  addrowstrtmp += "addRow" + groupid_tmp + "(0);";
      	  }
        }

        groupCount++;
      }

      jsStr.append("jQuery(document).ready(function (){ ");
      jsStr.append(" try{");
      jsStr.append("  " + addrowstrtmp + "  ");
      jsStr.append(" }catch(e){}");
      jsStr.append("}); ");
      

      // 这里拼所有明细字段组公用的JS
      ArrayList rowCalAry = new ArrayList();
      ArrayList rowCalSignAry = new ArrayList();
      ArrayList mainCalAry = new ArrayList();
      ArrayList tmpAry = null;

      StringTokenizer stk2 = new StringTokenizer(rowCalItemStr1, ";");

      ArrayList newRowCalArray = new ArrayList();

      while (stk2.hasMoreTokens()) {
        rowCalAry.add(stk2.nextToken(";"));
      }
      stk2 = new StringTokenizer(mainCalStr1, ";");
      while (stk2.hasMoreTokens()) {
        mainCalAry.add(stk2.nextToken(";"));
      }
      // jsStr.append("\t").append("").append("\n");

      jsStr.append("").append("rowindex = " + derecorderindex + ";").append("\n");
      jsStr.append("").append("curindex = " + derecorderindex + ";").append("\n");

      jsStr.append("").append("function calSumPrice(){").append("\n");
      jsStr.append("\t").append("try{ ").append("\n");
      jsStr.append("\t").append("var datalength = 2;").append("\n");
      jsStr.append("\t").append("var temv1; var tempi = arguments[0] ;").append("\n");
      jsStr.append("\t").append("var reP;var oldvalue;var toPvalue;").append("\n");
      
      String temStr = "";
      ArrayList detailFieldNotShowList = Util.TokenizerString(detailFieldNotShow, ",");
      for (int i = 0; i < rowCalAry.size(); i++) {
        temStr = "";
        String calExp = (String) rowCalAry.get(i);
        ArrayList calExpList = DynamicDataInput.FormatString(calExp);
        jsStr.append("\t").append("try{").append("\n");
        jsStr.append("\t\t").append("var i='';").append("\n");
        jsStr.append("\t\t").append("try{").append("\n");

        jsStr.append("\t\t\t").append("var evt = getEvent();").append("\n");
        jsStr.append("\t\t\t").append("var nowobj=(evt.srcElement ? evt.srcElement : evt.target).name.toString();").append("\n");
        jsStr.append("\t\t\t").append("if(nowobj.indexOf('_')>-1){").append("\n");
        jsStr.append("\t\t\t\t").append("i=nowobj.substr(nowobj.indexOf('_')+1);").append("\n");
        jsStr.append("\t\t\t").append("}").append("\n");
        jsStr.append("\t\t").append("}catch(e){").append("\n");
        jsStr.append("\t\t\t").append("if(tempi != undefined && tempi != null && tempi != \"\") {").append("\n");
        jsStr.append("\t\t\t").append("i = tempi ;").append("\n");
        jsStr.append("\t\t\t").append("}else {").append("\n");
        jsStr.append("\t\t\t").append("i = rowindexAll;").append("\n");
        jsStr.append("\t\t\t").append("}").append("\n");
        jsStr.append("\t\t\t").append("if(i<0){i=0;}").append("\n");
        jsStr.append("\t\t").append("}").append("\n");
        
        jsStr.append("\t\t").append("try{").append("\n");
        jsStr.append("\t\t\t").append("if(i.indexOf(\"lable\")>-1){").append("\n");
        jsStr.append("\t\t\t\t").append("i = i.substr(i.indexOf(\"_\")+1);").append("\n");
        jsStr.append("\t\t\t").append("}").append("\n");
        jsStr.append("\t\t").append("}catch(e){}").append("\n");
        jsStr.append("\t\t").append("var iscalc = true;").append("\n");
        for (int j = 0; j < calExpList.size(); j++) {
          String fieldidStr = "";
          calExp = (String) calExpList.get(j);
          String targetStr = "";        
          if (calExp.indexOf("innerHTML") > 0) {
            targetStr = calExp.substring(0, calExp.indexOf("innerHTML") - 1);
            jsStr.append("\t\t").append("if(" + targetStr + "){").append("\n");
            jsStr.append("\t\t").append(" datalength = " + calExp.substring(0, calExp.indexOf("innerHTML") - 9) + ").getAttribute(\"datalength\");").append("\n");
            jsStr.append("\t\t").append(" if(datalength == null || datalength == '') datalength = 2").append("\n");
            jsStr.append("\t\t").append("if(" + calExp.substring(0, calExp.indexOf("innerHTML") - 9) + ").getAttribute(\"datavaluetype\")=='5'){").append("\n");
            jsStr.append("\t\t").append("oldvalue = "+calExp.substring(0, calExp.indexOf("innerHTML") - 9) + ").value;").append("\n");
            //jsStr.append("\t\t").append("if(oldvalue.indexOf(\".\")<0)").append("\n");
            //jsStr.append("\t\t").append(" reP = /(\\d{1,3})(?=(\\d{3})+($))/g;").append("\n");
            //jsStr.append("\t\t").append("else reP = /(\\d{1,3})(?=(\\d{3})+(\\.))/g;").append("\n");
            //jsStr.append("\t\t").append("toPvalue = oldvalue.replace(reP,\"$1,\");").append("\n");
            jsStr.append("\t\t").append(targetStr+".innerHTML=changeToThousandsVal(oldvalue);").append("\n");
            jsStr.append("\t\t").append("}else if(datalength!='2'){" + calExp.substring(0, calExp.indexOf("=")) + "=toPrecision(" + calExp.substring(0, calExp.indexOf("innerHTML") - 9) + ").value,datalength);").append("\n");
            jsStr.append("\t\t").append("}else if(" + calExp.substring(0, calExp.indexOf("innerHTML") - 9) + ").getAttribute(\"datatype\")=='int' && " + calExp.substring(0, calExp.indexOf("innerHTML") - 9) + ").getAttribute(\"datavaluetype\")!='5'){" + calExp.substring(0, calExp.indexOf("=")) + "=toPrecision(" + calExp.substring(0, calExp.indexOf("innerHTML") - 9) + ").value,0);").append("\n");
            jsStr.append("\t\t").append("}else{ " + calExp.substring(0, calExp.indexOf("=")) + "=toPrecision(" + calExp.substring(0, calExp.indexOf("innerHTML") - 9) + ").value,datalength);}}").append("\n");
             //add by liaodong for qc43068 in 2013-11-22 start
            String str =calExp.substring(0, calExp.indexOf("="));
            String resultNum=str.substring(0,str.indexOf("innerHTML"));
    		String resultNumStr = resultNum.substring(0, resultNum.indexOf("span"))+"\")";
    		//如果是金额千分位与金额转换的需要去掉_lable
    		resultNumStr=resultNumStr.replace("_lable", "");
            jsStr.append("\t\t").append("try{");
             jsStr.append("\t\t").append(" if("+resultNumStr+"){");
              jsStr.append("\t\t").append(" var numType="+resultNumStr+".getAttribute(\"datatype\"); ");
              jsStr.append("\t\t").append(" if(numType=='float'||numType=='int'){");
              //经验证viewtype0未编辑1为必填 只读并没有此属性
                   jsStr.append("\t\t").append(" var numviewtype="+resultNumStr+".getAttribute(\"viewtype\"); ");
                   jsStr.append("\t\t").append(" if(numviewtype==1||numviewtype==0){");
				   jsStr.append("\t\t").append(" var fielddbtype="+resultNumStr+".getAttribute(\"fieldtype\");");
                   jsStr.append("\t\t").append("if(fielddbtype != 4){"); //金额转换的不去掉重复的
                   jsStr.append("\t\t").append(""+calExp.substring(0, calExp.indexOf("=")) +"='';");
				   jsStr.append("\t\t").append(" }");
                   jsStr.append("\t\t").append(" }");
                   jsStr.append("\t\t").append("if("+resultNumStr+".value==''&&numviewtype==1){");
                   jsStr.append("\t\t").append(""+resultNum+"innerHTML=\"<img src='/images/BacoError.gif' align=absmiddle>\";");
                   jsStr.append("\t\t").append("}");
              jsStr.append("\t\t").append(" }");
             jsStr.append("\t\t").append(" } ");
            jsStr.append("\t\t").append("}catch(e){}");            
            //end
		  } else {
			  boolean iscalc = true; // 有隐藏字段不计算
			  for(int ii = 0 ; ii < detailFieldNotShowList.size(); ii++) {
				  String detailField = (String) detailFieldNotShowList.get(ii);
				  if(calExp.indexOf("$G(\"field" + detailField + "_\"+i") > -1) {
					  iscalc = false;
					  break;
				  }
			  }
			  if(iscalc) {
				  jsStr.append("\t\t").append("iscalc = true;").append("\n");
			  }else {
				  jsStr.append("\t\t").append("iscalc = false;").append("\n");
			  }
            if (calExp.indexOf("value") > 0) {
              targetStr = calExp.substring(0, calExp.indexOf("value") - 1);
              jsStr.append("\t\t").append("if(" + targetStr + "){").append("\n");
              jsStr.append("\t\t").append(" datalength = " + targetStr + ".getAttribute(\"datalength\");").append("\n");
              jsStr.append("\t\t").append(" if(datalength == null || datalength == '') datalength = 2").append("\n");
              if (calExp.indexOf("=") != calExp.length()-1) {
            	  //如果除数为零的时候需要将Infinity去掉光标移至错误字段
            	  jsStr.append("\t\t").append("try{").append("\n");	  
            	  jsStr.append("\t\t").append("if(iscalc) {").append("\n");
            	  jsStr.append("\t\t").append(calExp + "; ").append("\n");
            	  jsStr.append("\t\t").append("}").append("\n");
            	  try{
             		 if(calExp.indexOf("=")>=0){
             			 String[] calSplitSign=calExp.split("=");
             			 String rightequalsmark = calSplitSign[0].replace(".value", "");
             			 String leftequalsmark = calSplitSign[1].replace(".replace(/,/g,\"\"))", "").replace("parse_Float(", "").replace(".value", ""); 
             			 if(leftequalsmark.indexOf("/")>=0){
             				  String leftdivide  =leftequalsmark.split("/")[0];
             				  String rightdivide =leftequalsmark.split("/")[1];
             				  jsStr.append("\t\t").append("if("+rightequalsmark+".value == \"Infinity\" || "+rightequalsmark+".value == \"-Infinity\" || "+rightequalsmark+".value == \"NaN\"    ){").append("\n");
             				  jsStr.append("\t\t").append(rightequalsmark+".value='';").append("\n");
             				  String spanObj = rightequalsmark.replace(")", "+\"span\")");
             				  jsStr.append("\t\t").append("if("+rightequalsmark+".viewtype == 1){").append("\n");
             				  jsStr.append("\t\t").append(spanObj+".innerHTML=\"<img src=/images/BacoError.gif align=absmiddle>\";").append("\n");
             				  jsStr.append("\t\t").append("}else{").append("\n");
             				  jsStr.append("\t\t").append(spanObj+".innerHTML='';").append("\n");
             				  jsStr.append("\t\t").append("}").append("\n");
             				  jsStr.append("\t\t").append("return;").append("\n");
             				  jsStr.append("\t\t").append("}");
             			 }
             		 } 
             	  }catch(Exception e){}
            	  jsStr.append("\t\t").append("}catch(e){").append("\n");
            	  jsStr.append("\t\t").append("}").append("\n");
              }
              jsStr.append("\t\t").append("if(" + calExp.substring(0, calExp.indexOf("value") - 1) + ".datatype=='int') " 
               + calExp.substring(0, calExp.indexOf("=")) + "=toPrecision(" + calExp.substring(0, calExp.indexOf("=")) + ",0);else {"
               + "if("+targetStr+".getAttribute('datavaluetype') == 5){"
               +"oldvalue = toPrecision(" + calExp.substring(0, calExp.indexOf("=")) + ",datalength);"
               //+"if(oldvalue.indexOf(\".\")<0){reP = /(\\d{1,3})(?=(\\d{3})+($))/g;}else{reP = /(\\d{1,3})(?=(\\d{3})+(\\.))/g;}"
               //+"toPvalue = oldvalue.replace(reP,\"$1,\");"
               + calExp.substring(0, calExp.indexOf("=")) + "=changeToThousandsVal(oldvalue);}else{"
               + calExp.substring(0, calExp.indexOf("=")) + "=toPrecision(" + calExp.substring(0, calExp.indexOf("=")) + ",datalength);}}}").append("\n");
            }
          }
          try {
            fieldidStr = targetStr.substring(targetStr.indexOf("field") + 5, targetStr.indexOf("_"));
          } catch (Exception e) {
            fieldidStr = "";
          }
          if (!"".equals(fieldidStr)) {
            jsStr.append("\t").append("try{").append("\n");
            jsStr.append("\t\t").append("if($G(\"field" + fieldidStr + "_\"+i).getAttribute(\"fieldtype\")=='4'){").append("\n");
            jsStr.append("\t\t").append("getNumber2(\"" + fieldidStr + "_\"+i);").append("\n");
            jsStr.append("\t\t").append("}else {");
            jsStr.append("\t\t").append("getNumber(\"" + fieldidStr + "_\"+i);").append("\n");
            jsStr.append("\t\t").append("}");
            jsStr.append("\t\t").append("if(!isNaN($G(\"field_lable" + fieldidStr + "_\"+i).value)){");
            jsStr.append("\t\t").append("numberToChinese(\"" + fieldidStr + "_\"+i);").append("\n");
            jsStr.append("\t\t").append("}");
            jsStr.append("\t\t").append("checkinput3(\"field_lable" + fieldidStr + "_\"+i,\"field" + fieldidStr + "_\"+i+\"span\",$G(\"field" + fieldidStr + "_\"+i).getAttribute(\'viewtype\'));").append("\n");
            jsStr.append("\t").append("}catch(e){}").append("\n");
          }
        }
        jsStr.append("\t").append("}catch(e){}").append("\n");
      }
      jsStr.append("\t").append("}catch(e){}").append("\n");
      jsStr.append("").append("}").append("\n");
      
     
      jsStr.append("").append("function calMainField(obj){").append("\n");
      jsStr.append("\t").append("try{").append("\n");
      jsStr.append("\t").append("var datalength = 2;").append("\n");
      jsStr.append("\t").append("var rows=0;").append("\n");
      for (int i = 0; i < groupCount; i++) {
        jsStr.append("\t").append("var temprow=parseInt($G('indexnum" + i + "').value);").append("\n");
        jsStr.append("\t").append("if(temprow>rows) rows=temprow;").append("\n");
      }
      jsStr.append("\t").append("if(rowindex<rows){").append("\n");
      jsStr.append("\t\t").append("rowindex=rows;").append("\n");
      jsStr.append("\t").append("}").append("\n");
      jsStr.append("\t").append("var iscalmain = true;").append("\n");
      for (int i = 0; i < mainCalAry.size(); i++) {
        String str2 = mainCalAry.get(i).toString();
        int idx = str2.indexOf("=");
        String str3 = str2.substring(0, idx);
        str3 = str3.substring(str3.indexOf("_") + 1);
        String str4 = str2.substring(idx);
        str4 = str4.substring(str4.indexOf("_") + 1);
        
        jsStr.append("\t").append("var sum=0;").append("\n");
        jsStr.append("\t").append("var temStr;").append("\n");
        jsStr.append("\t").append("var reP;var oldvalue;var toPvalue;").append("\n");
        jsStr.append("\t").append("for(i=0; i<rowindex; i++){").append("\n");

        jsStr.append("\t\t").append("try{").append("\n");
        jsStr.append("\t\t\t").append("temStr=$G(\"field" + str4 + "_\"+i).value;").append("\n");
        jsStr.append("\t\t\t").append("temStr = temStr.replace(/,/g,\"\");").append("\n");
        jsStr.append("\t\t\t").append("if(temStr+\"\"!=\"\"){").append("\n");
        jsStr.append("\t\t\t\t").append("sum+=temStr*1;").append("\n");
        jsStr.append("\t\t\t").append("}").append("\n");
        jsStr.append("\t\t").append("}catch(e){;}").append("\n");
        jsStr.append("\t").append("}").append("\n");
        
        jsStr.append("\t\tif(window.console)console.log('rowindex = '+rowindex+' sum = '+sum);");
        
        boolean iscalmain = true; // 明细合计字段隐藏不计算列规则
		  for(int ii = 0 ; ii < detailFieldNotShowList.size(); ii++) {
			  String detailField = (String) detailFieldNotShowList.get(ii);
			  if(detailField.equals(str4)) {
				  iscalmain = false;
				  break;
			  }
		  }
		  if(iscalmain) {
			  jsStr.append("\t\t").append("iscalmain = true;").append("\n");
		  }else {
			  jsStr.append("\t\t").append("iscalmain = false;").append("\n");
		  }
		jsStr.append("\t\t").append("if(iscalmain) {").append("\n");
        jsStr.append("\t\t").append("if($G(\"field" + str3 + "\")){").append("\n");
        jsStr.append("\t\t").append("datalength = $G(\"field" + str3 + "\").getAttribute(\"datalength\");").append("\n");
        jsStr.append("\t\t").append(" if(datalength == null || datalength == '') datalength = 2").append("\n");
        jsStr.append("\t\t").append("if($G(\"field" + str3 + "\").datatype+''==\"int\")").append("\n");
        jsStr.append("\t\t\t").append("$G(\"field" + str3 + "\").value=toPrecision(sum,0);").append("\n");
        jsStr.append("\t\t").append("else{").append("\n");
        jsStr.append("\t\t").append("if($G(\"field" + str3 + "\").getAttribute(\"datavaluetype\")=='5'){").append("\n");
        jsStr.append("\t\t\t\t\t").append("oldvalue = $G(\"field" + str3 + "\").value;").append("\n");
        //jsStr.append("\t\t\t\t\t").append("if(oldvalue.indexOf(\".\")<0)").append("\n");
        //jsStr.append("\t\t\t\t\t").append(" reP = /(\\d{1,3})(?=(\\d{3})+($))/g;").append("\n");
        //jsStr.append("\t\t\t\t\t").append("else reP = /(\\d{1,3})(?=(\\d{3})+(\\.))/g;").append("\n");
        //jsStr.append("\t\t\t\t\t").append("toPvalue = oldvalue.replace(reP,\"$1,\");").append("\n");
        jsStr.append("\t\t\t").append("$G(\"field" + str3 + "\").value=changeToThousandsVal(toPrecision(sum,datalength));");
        jsStr.append("\t\t").append("if($G(\"field" + str3 + "\").getAttribute(\"datavaluetype\")!='5'){").append("\n");
        jsStr.append("\t\t\t").append("jQuery($G(\"field" + str3 + "\")).trigger(\"blur\");").append("\n");
        jsStr.append("\t\t").append("}").append("\n");
        jsStr.append("\t\t\t").append("jQuery($G(\"field" + str3 + "\")).trigger(\"change\");").append("\n");
        jsStr.append("\t\t").append("}else{").append("\n");
        jsStr.append("\t\t\t").append("$G(\"field" + str3 + "\").value=toPrecision(sum,datalength);}}").append("\n");
        jsStr.append("\t\t").append("}").append("\n");
        jsStr.append("\t\t").append("if($G(\"field" + str3 + "span\")){").append("\n");
        jsStr.append("\t\t\t").append("if($G(\"field" + str3 + "\")&&$G(\"field" + str3 + "\").type==\"text\"){").append("\n");
        jsStr.append("\t\t\t\t").append("$G(\"field" + str3 + "span\").innerHTML=\"\";").append("\n");
        jsStr.append("\t\t\t").append("}else{").append("\n");
        jsStr.append("\t\t\t\t").append("if($G(\"field" + str3 + "\").getAttribute(\"datatype\")==\"int\" && $G(\"field" + str3 + "\").getAttribute(\"datavaluetype\")!=\"5\"){").append("\n");
        jsStr.append("\t\t\t\t\t").append("$G(\"field" + str3 + "span\").innerHTML=toPrecision(sum,0);").append("\n");
        jsStr.append("\t\t\t\t").append("}else if(datalength!=\"2\"){").append("\n");
        jsStr.append("\t\t\t\t\t").append("$G(\"field" + str3 + "span\").innerHTML=toPrecision(sum,datalength);").append("\n");
        jsStr.append("\t\t\t\t").append("}else if($G(\"field" + str3 + "\").getAttribute(\"datatype\")!=\"int\" && $G(\"field" + str3 + "\").getAttribute(\"datavaluetype\")!=\"5\"){").append("\n");
        jsStr.append("\t\t\t\t\t").append("$G(\"field" + str3 + "span\").innerHTML=toPrecision(sum,datalength);").append("\n");
        jsStr.append("\t\t\t\t\t").append("try{").append("\n");
        jsStr.append("\t\t\t\t\t").append("if($G(\"field" + str3 + "\").getAttribute(\"filedtype\")){").append("\n");
        jsStr.append("\t\t\t\t\t").append("  var filedtype=$G(\"field" + str3 + "\").getAttribute(\"filedtype\")").append("\n");
        jsStr.append("\t\t\t\t\t").append("  if(filedtype == 4){").append("\n");
        jsStr.append("\t\t\t\t\t").append("$G(\"field" + str3 + "span\").innerHTML=\"\";").append("\n");
        jsStr.append("\t\t\t\t\t").append("$G(\"field_lable" + str3 + "\").value=toPrecision(sum,datalength);").append("\n");
             jsStr.append("\t\t\t\t\t").append("numberToFormat(\"" + str3 + "\")").append("\n");
        jsStr.append("\t\t\t\t\t").append("}").append("\n");
        jsStr.append("\t\t\t\t\t").append("}").append("\n");
        jsStr.append("\t\t\t\t\t").append("}catch(e){}").append("\n");
        jsStr.append("\t\t\t\t").append("}else{").append("\n");
        jsStr.append("\t\t\t\t\t").append("oldvalue = $G(\"field" + str3 + "\").value;").append("\n");
        //jsStr.append("\t\t\t\t\t").append("if(oldvalue.indexOf(\".\")<0)").append("\n");
        //jsStr.append("\t\t\t\t\t").append(" reP = /(\\d{1,3})(?=(\\d{3})+($))/g;").append("\n");
        //jsStr.append("\t\t\t\t\t").append("else reP = /(\\d{1,3})(?=(\\d{3})+(\\.))/g;").append("\n");
        //jsStr.append("\t\t\t\t\t").append("toPvalue = oldvalue.replace(reP,\"$1,\");").append("\n");
        jsStr.append("\t\t\t\t\t").append("$G(\"field" + str3 + "span\").innerHTML=changeToThousandsVal(toPrecision(sum,datalength));").append("\n");
        jsStr.append("\t\t\t\t").append("}").append("\n");
        jsStr.append("\t\t\t").append("}").append("\n");
        jsStr.append("\t\t").append("}").append("\n");
        jsStr.append("\t\t").append("}").append("\n");
        
        
        jsStr.append("\t\t\t\t\t\t").append("var filedtype=$GetEle(\"field"+str3+"\").getAttribute(\"filedtype\");").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("var _filedvalue=$GetEle(\"field"+str3+"\").value;").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("var _viewtype=$GetEle(\"field"+str3+"\").getAttribute(\"viewtype\");").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("if((_filedvalue==null||_filedvalue=='')&&_viewtype==1){").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("	if(filedtype == '4'){").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("			jQuery(\"#field_lable"+str3+"span\").html('<img align=\"absMiddle\" src=\"/images/BacoError.gif\"/>');").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("	}else{");
        jsStr.append("\t\t\t\t\t\t").append("			jQuery(\"#field"+str3+"span\").html(\"<img align='absMiddle' src='/images/BacoError.gif' />\");").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("	}").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("	}else if((_filedvalue==null||_filedvalue=='')&&_viewtype==0){").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("	if(filedtype == '4'){").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("			jQuery(\"#field_lable"+str3+"span\").html('');").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("	}else{");
        jsStr.append("\t\t\t\t\t\t").append("			jQuery(\"#field"+str3+"span\").html(\"\");").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("	}").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("}else if(_filedvalue != null && _filedvalue != '') {").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("    if (filedtype == '4') {").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("        if (jQuery(\"#field_lable"+str3+"span\")) {").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("            jQuery(\"#field_lable"+str3+"span\").html(\"\");").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("        }").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("    }else {").append("\n");
        //jsStr.append("\t\t\t\t\t\t").append("        jQuery(\"#field"+str3+"span\").html(\"\");").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("    }").append("\n");
        jsStr.append("\t\t\t\t\t\t").append("}").append("\n");
        jsStr.append("\t\tif(window.console)console.log('field"+str3+" : value = '+$GetEle(\"field"+str3+"\").value+' ');");
      }
      jsStr.append("\t").append("}catch(e){}").append("\n");
      jsStr.append("").append("}").append("\n");

      jsStr.append("").append("function calSum(obj){").append("\n");
	  jsStr.append("\t").append("enableAllmenu();").append("\n");
      jsStr.append("\t").append("try{").append("\n");
      jsStr.append("\t").append("calSumPrice(arguments[2]);").append("\n");
      jsStr.append("\t").append("var rows=0;").append("\n");
      jsStr.append("\t").append("var temprow=parseInt($G('indexnum'+obj).value);").append("\n");
      jsStr.append("\t").append("if(temprow>rows){rows=temprow;}").append("\n");
      
      jsStr.append("\t").append("if(rowindex<rows){").append("\n");
      jsStr.append("\t\t").append("rowindex=rows;").append("\n");
      jsStr.append("\t").append("}").append("\n");
      jsStr.append("\t").append("var sum=0;").append("\n");
      jsStr.append("\t").append("var temStr;").append("\n");
	  jsStr.append("\t").append("var datavaluetype;");

      for (int i = 0; i < colCalAry.size(); i++) {
        String str = colCalAry.get(i).toString();
        str = str.substring(str.indexOf("_") + 1);

        jsStr.append("\t").append("sum=0;").append("\n");
		jsStr.append("\t").append("datavaluetype='';").append("\n");
        jsStr.append("\t").append("for(i=0; i<rowindex; i++){").append("\n");
        jsStr.append("\t\t").append("try{").append("\n");
        jsStr.append("\t\t\t").append("temStr=$G(\"field" + str + "_\"+i).value;").append("\n");
		jsStr.append("\t\t\t").append("temStr=temStr.replace(/,/g,\"\");").append("\n");
        jsStr.append("\t\t\t").append("if(temStr+\"\"!=\"\"){").append("\n");
        jsStr.append("\t\t\t\t").append("sum+=temStr*1;").append("\n");
        jsStr.append("\t\t\t").append("}").append("\n");
		jsStr.append("\t\t\t").append("if($G(\"field" + str + "_\"+i).getAttribute(\"datavaluetype\")){").append("\n");
        jsStr.append("\t\t\t").append("datavaluetype = $G(\"field" + str + "_\"+i).getAttribute(\"datavaluetype\");").append("\n");   
        jsStr.append("\t\t\t").append("}").append("\n");
        jsStr.append("\t\t").append("}catch(e){}").append("\n");
        jsStr.append("\t").append("}").append("\n");
        jsStr.append("\t\tif(window.console)console.log('rowindex = '+rowindex+'  sum = '+sum);");
		if (isbill == 0) {
        	  rs.executeSql("select fielddbtype from workflow_formdictdetail where id=" + str);
        } else {
        	  rs.executeSql("select fielddbtype from workflow_billfield where id=" + str);
        }
         int decimaldigits_t =2;
    	if("oracle".equals(rs.getDBType())){
    		 if(rs.next()){
    	        	String fielddbtypeStr=rs.getString("fielddbtype");
    	        	if(fielddbtypeStr.indexOf("number")>=0){
    	        		int digitsIndex = fielddbtypeStr.indexOf(",");
        				if(digitsIndex > -1){
        					decimaldigits_t = Util.getIntValue(fielddbtypeStr.substring(digitsIndex+1, fielddbtypeStr.length()-1), 2);
        				}else{
        					decimaldigits_t = 2;
        				}
    	        	}else{
    	        		if(fielddbtypeStr.equals("integer")){
    	        			decimaldigits_t = 0;
    	        		}
    	        	}
    	        }
    	}else{
    		 if(rs.next()){
 	        	String fielddbtypeStr=rs.getString("fielddbtype");
 	        	if(fielddbtypeStr.indexOf("decimal")>=0){
 	        		int digitsIndex = fielddbtypeStr.indexOf(",");
     				if(digitsIndex > -1){
     					decimaldigits_t = Util.getIntValue(fielddbtypeStr.substring(digitsIndex+1, fielddbtypeStr.length()-1), 2);
     				}else{
     					decimaldigits_t = 2;
     				}
 	        	}else{
 	        		if(fielddbtypeStr.equals("int")){
 	        			decimaldigits_t = 0;
 	        		}
 	        	}
 	        }
    	}
	    jsStr.append("\t\t\t").append("var decimalNumber="+decimaldigits_t+"").append("\n");
        jsStr.append("\t\t").append("if($G(\"sum" + str + "\")){").append("\n");
        jsStr.append("\t\t").append("try{");
		jsStr.append("\t\t").append("if(datavaluetype == '5'||datavaluetype == 5){").append("\n");
        jsStr.append("\t\t\t\t\t").append("oldvalue = toPrecision(sum,decimalNumber);").append("\n");
        //jsStr.append("\t\t\t\t\t").append("if(oldvalue.indexOf(\".\")<0)").append("\n");
        //jsStr.append("\t\t\t\t\t").append(" reP = /(\\d{1,3})(?=(\\d{3})+($))/g;").append("\n");
        //jsStr.append("\t\t\t\t\t").append("else reP = /(\\d{1,3})(?=(\\d{3})+(\\.))/g;").append("\n");
        //jsStr.append("\t\t\t\t\t").append("toPvalue = oldvalue.replace(reP,\"$1,\");").append("\n");
        jsStr.append("\t\t").append("$G(\"sum" + str + "\").innerHTML=changeToThousandsVal(oldvalue);").append("\n");
        //jsStr.append("\t\tif(window.console)console.log('"+str+" : oldvalue = '+oldvalue+' ');");
        jsStr.append("\t\t").append("}else{").append("\n");
        jsStr.append("\t\t").append("$G(\"sum" + str + "\").innerHTML=toPrecision(sum,decimalNumber) ;").append("\n");
        jsStr.append("\t").append("}").append("\n");
        jsStr.append("\t").append("}catch(e){ }").append("\n");
        
        jsStr.append("\t").append("}").append("\n");
        jsStr.append("\t").append("if($G(\"sumvalue" + str + "\")){").append("\n");
		jsStr.append("\t\t").append("if(datavaluetype == '5'||datavaluetype == 5){").append("\n");
        jsStr.append("\t\t\t\t\t").append("oldvalue = toPrecision(sum,decimalNumber);").append("\n");
        //jsStr.append("\t\t\t\t\t").append("if(oldvalue.indexOf(\".\")<0)").append("\n");
        //jsStr.append("\t\t\t\t\t").append(" reP = /(\\d{1,3})(?=(\\d{3})+($))/g;").append("\n");
        //jsStr.append("\t\t\t\t\t").append("else reP = /(\\d{1,3})(?=(\\d{3})+(\\.))/g;").append("\n");
        //jsStr.append("\t\t\t\t\t").append("toPvalue = oldvalue.replace(reP,\"$1,\");").append("\n");
        //jsStr.append("\t\tif(window.console)console.log('"+str+"-->oldvalue = '+oldvalue+'  ');");
        jsStr.append("\t\t").append("$G(\"sumvalue" + str + "\").value=changeToThousandsVal(oldvalue);").append("\n");
        jsStr.append("\t\t").append("}else{").append("\n");
        jsStr.append("\t\t").append("$G(\"sumvalue" + str + "\").value=toPrecision(sum,decimalNumber);").append("\n");
		jsStr.append("\t\t").append("}").append("\n");
        jsStr.append("\t").append("}").append("\n");
      }
      
      jsStr.append("\t").append("}catch(e){}").append("\n");
      
      jsStr.append("\t").append("calMainField(obj);").append("\n");
      jsStr.append("\t").append("displayAllmenu();").append("\n");
      jsStr.append("").append("}").append("\n");

      if (iscreate == 0) {
        for (int i = 0; i < groupCount; i++) {
          //jsStr.append("").append("calSum(" + i + ");").append("\n");
		  jsStr.append("").append("jQuery(document).ready(function(){calSum(" + i + ");});").append("\n");
        }
        jsStr.append("").append("setTimeout(\"doTriggerDetailInit()\",1000);").append("\n");
        jsStr.append("").append("function doTriggerDetailInit(){").append("\n");
        jsStr.append("\t").append("try{").append("\n");
        jsStr.append("\t").append("var tempS = \"" + trrigerdetailfield + "\";").append("\n");
        jsStr.append("\t").append("var tempA = \"\";").append("\n");
        jsStr.append("\t").append("if(tempS.length>0){").append("\n");
        jsStr.append("\t\t").append("tempA = tempS.split(\",\");").append("\n");
        jsStr.append("\t\t").append("for(var i=0;i<tempA.length;i++){").append("\n");
        jsStr.append("\t\t\t").append("datainputd(tempA[i]);").append("\n");
        jsStr.append("\t\t").append("}").append("\n");
        jsStr.append("\t").append("}").append("\n");
        jsStr.append("\t").append("}catch(e){}").append("\n");
        jsStr.append("").append("}").append("\n");
      }
    } catch (Exception e) {
      writeLog(e);
    }
  }

    public void getFieldAttr() {
        String currentdate = Util.null2String(request.getParameter("currentdate"));
	    int workflowid = 0;
	    try {
	    	ResourceComInfo resourceComInfo = new ResourceComInfo();
	    	int requestid = Util.getIntValue(request.getParameter("requestid"), 0);
	    	workflowid = Util.getIntValue(request.getParameter("workflowid"), 0);
		  
	    	int isbill = Util.getIntValue(request.getParameter("isbill"), 0);
	    	int formid = Util.getIntValue(request.getParameter("formid"), 0);
	      
	    	int nodeid = Util.getIntValue(request.getParameter("nodeid"), 0);
	    	int creater = user.getUID();
	    	int credept = Util.getIntValue(resourceComInfo.getDepartmentID("" + creater), 0);
	    	int currentuser = user.getUID();
	    	int currentdept = Util.getIntValue(resourceComInfo.getDepartmentID("" + currentuser), 0);
	    	if (iscreate == 0) {
	    		HttpSession session = (HttpSession) request.getSession(false);
	    		creater = Util.getIntValue((String) session.getAttribute(user.getUID() + "_" + requestid + "creater"), 0);
	    		credept = Util.getIntValue(resourceComInfo.getDepartmentID("" + creater), 0);
	    	}
	    	
	    	Map fieldattrKv = getFieldAttr(user, workflowid, isbill, formid, requestid, nodeid, iscreate, creater, currentdate);
	    	htmlHiddenElementsb.append(fieldattrKv.get("htmlHiddenElementsb"));
	    	jsStr.append(fieldattrKv.get("jsStr"));
	    	otherPara_hs.putAll((Map)fieldattrKv.get("otherPara_hs"));
	    } catch (Exception e) {
			writeLog(e);
		}
    }
    
    public static Map getFieldAttr(User user, int workflowid, int isbill, int formid, int requestid, int nodeid, int iscreate, int creater2, String currentdate) {
	  
    	StringBuffer htmlHiddenElementsb = new StringBuffer();
    	StringBuffer jsStr = new StringBuffer();
    	Map otherPara_hs = new HashMap();
  
  
    	Map result = new HashMap();
    	result.put("htmlHiddenElementsb", htmlHiddenElementsb);
    	result.put("jsStr", jsStr);
    	result.put("otherPara_hs", otherPara_hs);
	  
	    ArrayList sqlfieldidList = new ArrayList();
	    ArrayList sqlcontentList = new ArrayList();
	    ArrayList attrfieldidList = new ArrayList();
	    ArrayList attrcontentList = new ArrayList();
	    ArrayList datefieldidList = new ArrayList();
		ArrayList datecontentList = new ArrayList();
		ArrayList othertypeList = new ArrayList();
		ArrayList allcalfieldidList = new ArrayList();
		Hashtable transtype_hs = new Hashtable();
		ArrayList sapfieldidList = new ArrayList();
	    RecordSet rs = new RecordSet();
	    //String currentdate = Util.null2String(request.getParameter("currentdate"));
	    //int workflowid = 0;
	    try {
	      ResourceComInfo resourceComInfo = new ResourceComInfo();
//	      int requestid = Util.getIntValue(request.getParameter("requestid"), 0);
//	      workflowid = Util.getIntValue(request.getParameter("workflowid"), 0);
//	      int nodeid = Util.getIntValue(request.getParameter("nodeid"), 0);
	      int creater = user.getUID();
	      int credept = Util.getIntValue(resourceComInfo.getDepartmentID("" + creater), 0);
	      int currentuser = user.getUID();
	      int currentdept = Util.getIntValue(resourceComInfo.getDepartmentID("" + currentuser), 0);
	      if (iscreate == 0) {
//	        HttpSession session = (HttpSession) request.getSession(false);
	        creater = creater2;//Util.getIntValue((String) session.getAttribute(user.getUID() + "_" + requestid + "creater"), 0);
	        credept = Util.getIntValue(resourceComInfo.getDepartmentID("" + creater), 0);
	      }
	      ArrayList paraList = new ArrayList();
	      paraList.add("currentuser");
	      paraList.add("currentdept");
	      paraList.add("wfcreater");
	      paraList.add("wfcredept");
	      paraList.add("currentdate");
	      ArrayList valueList = new ArrayList();
	      valueList.add("" + currentuser);
	      valueList.add("" + currentdept);
	      valueList.add("" + creater);
	      valueList.add("" + credept);
	      valueList.add(""+currentdate);
	      // 获得字段SQL属性
	      rs.execute("select * from workflow_nodefieldattr where nodeid=" + nodeid);
	      while(rs.next()){
	    	  	int fieldattrid = Util.getIntValue(rs.getString("id"), 0);
				int fieldid_tmp = Util.getIntValue(rs.getString("fieldid"), 0);
				String sqlcontent_tmp = Util.null2String(rs.getString("attrcontent")).trim();
				int caltype_tmp = Util.getIntValue(rs.getString("caltype"), 0);
				int othertype_tmp = Util.getIntValue(rs.getString("othertype"), 0);
				int transtype_tmp = Util.getIntValue(rs.getString("transtype"), 0);
				int index = sqlcontent_tmp.indexOf("doFieldSQL(\"");
				allcalfieldidList.add(""+fieldid_tmp);
				transtype_hs.put("transtype"+fieldid_tmp, ""+transtype_tmp);
	            //add by liaodong for qc52958  in 20130912
				if(caltype_tmp == 0){ //如果计算公式为默认值的时候判断计算类型出现的原因直接复制的语句
					if(sqlcontent_tmp.indexOf("doFieldSQL(\"") >=0){
						caltype_tmp = 1;
					}else if(sqlcontent_tmp.indexOf("doFieldMath(\"") >=0){
						caltype_tmp = 2;
					}else  if(sqlcontent_tmp.indexOf("doFieldDate(\"") >=0){
						caltype_tmp = 3;
					}else if(sqlcontent_tmp.indexOf("doFieldSAP(\"") >=0){
						caltype_tmp = 4;
					}
				}
				//end
				if(caltype_tmp == 1){
					if(index < 0){
						continue;
					}
					sqlcontent_tmp = analyzeSql(sqlcontent_tmp, paraList, valueList);
					sqlfieldidList.add(""+fieldid_tmp);
					sqlcontentList.add(sqlcontent_tmp);
					sqlcontent_tmp = sqlcontent_tmp.substring(index+12);
					index = sqlcontent_tmp.lastIndexOf("\")");
					if(index > -1){
						sqlcontent_tmp = sqlcontent_tmp.substring(0, index);
						sqlcontent_tmp = sqlcontent_tmp.trim();
					}
					sqlcontent_tmp = sqlcontent_tmp.trim();
					htmlHiddenElementsb.append("\n\t<input type=\"hidden\" id=\"fieldsql"+fieldid_tmp+"\" name=\"fieldsql"+fieldid_tmp+"\" value=\"").append(sqlcontent_tmp.replaceAll("\"", "&quot;")).append("\">");
				}else if(caltype_tmp == 2){
					index = sqlcontent_tmp.indexOf("doFieldMath(\"");
					if(index < 0){
						continue;
					}
					sqlcontent_tmp = sqlcontent_tmp.substring(index+13);
					index = sqlcontent_tmp.lastIndexOf("\")");
					if(index > -1){
						sqlcontent_tmp = sqlcontent_tmp.substring(0, index);
						sqlcontent_tmp = sqlcontent_tmp.trim();
					}
					sqlcontent_tmp = sqlcontent_tmp.trim();
					attrfieldidList.add(""+fieldid_tmp);
					attrcontentList.add(sqlcontent_tmp);
				}else if(caltype_tmp == 3){
					index = sqlcontent_tmp.indexOf("doFieldDate(\"");
					if(index < 0){
						continue;
					}
					htmlHiddenElementsb.append("\n\t<input type=\"hidden\" id=\"caltype"+fieldid_tmp+"\" name=\"caltype"+fieldid_tmp+"\" value=\"").append(""+caltype_tmp).append("\">");
					htmlHiddenElementsb.append("\n\t<input type=\"hidden\" id=\"ohtertype"+fieldid_tmp+"\" name=\"ohtertype"+fieldid_tmp+"\" value=\"").append(""+othertype_tmp).append("\">");
					sqlcontent_tmp = sqlcontent_tmp.substring(index+13);
					index = sqlcontent_tmp.lastIndexOf("\")");
					if(index > -1){
						sqlcontent_tmp = sqlcontent_tmp.substring(0, index);
						sqlcontent_tmp = sqlcontent_tmp.trim();
					}
					sqlcontent_tmp = sqlcontent_tmp.trim();
					datefieldidList.add(""+fieldid_tmp);
					datecontentList.add(sqlcontent_tmp);
					othertypeList.add(""+othertype_tmp);
				}else if(caltype_tmp == 4){
					
					index = sqlcontent_tmp.indexOf("doFieldSAP(\"");
					if(index < 0){
						continue;
					}
					htmlHiddenElementsb.append("\n\t<input type=\"hidden\" id=\"caltype"+fieldid_tmp+"\" name=\"caltype"+fieldid_tmp+"\" value=\"").append(""+caltype_tmp).append("\">");
					htmlHiddenElementsb.append("\n\t<input type=\"hidden\" id=\"ohtertype"+fieldid_tmp+"\" name=\"ohtertype"+fieldid_tmp+"\" value=\"").append(""+othertype_tmp).append("\">");
					sapfieldidList.add(fieldid_tmp + "-" + fieldattrid);
					othertypeList.add(""+othertype_tmp);
				}
			}
		}catch(Exception e){
//			writeLog(e);
			e.printStackTrace();
		}
	    otherPara_hs.put("sqlfieldidList", sqlfieldidList);
	    otherPara_hs.put("sqlcontentList", sqlcontentList);
	    otherPara_hs.put("attrfieldidList", attrfieldidList);
	    otherPara_hs.put("attrcontentList", attrcontentList);
	    otherPara_hs.put("datefieldidList", datefieldidList);
		otherPara_hs.put("datecontentList", datecontentList);
		otherPara_hs.put("transtype_hs", transtype_hs);
		otherPara_hs.put("sapfieldidList", sapfieldidList);
	    ArrayList fieldidList = (ArrayList) otherPara_hs.get("fieldidList");
	    ArrayList fieldtypeList = (ArrayList) otherPara_hs.get("fieldtypeList");
	    Hashtable isedit_hs = (Hashtable) otherPara_hs.get("isedit_hs");

		if(sapfieldidList.size() > 0){
			jsStr.append("function doSAPField(fieldattrid,obj){").append("\n");
			//读取SAP联动配置开关
			String saptriggerflag = Util.null2String(new BaseBean().getPropValue("SAPTrigger","SAPTriggerFlag"));
			if(saptriggerflag.equals("1") || saptriggerflag.equalsIgnoreCase("y")){
				jsStr.append("\tvar isdetail = 0;");
				jsStr.append("\tvar rowindex = 0;");
				jsStr.append("\tif(obj){\n");
				jsStr.append("\t\tvar id = jQuery(obj).attr('id');");
				jsStr.append("\t\tif(id.indexOf('_') > 0){");
				jsStr.append("\t\t\tisdetail = 1;");
				jsStr.append("\t\t\trowindex = id.substring(id.indexOf('_') + 1);");
				jsStr.append("\t\t}");
				jsStr.append("\t}\n");
				jsStr.append("\t").append("jQuery.getScript('/workflow/request/GetSAPData.jsp?step=1&isdetail='+isdetail+'&rowindex='+rowindex+'&fieldattrid='+fieldattrid+'&workflowid="+workflowid+"')");
			}
			jsStr.append("\n}");
			
		}
	    // 数字数值型字段之间的计算、赋值操作
	    boolean isoracle = (rs.getDBType()).equals("oracle") ;
//		int isbill = Util.getIntValue(request.getParameter("isbill"), 0);
	    for (int i = 0; i < attrfieldidList.size(); i++) {
	      String attrfieldid_tmp = Util.null2String((String) attrfieldidList.get(i));
	      String attrcontent_tmp = Util.null2String((String) attrcontentList.get(i));
	      jsStr.append("\n");
	      jsStr.append("").append("function doMathFieldAttr" + attrfieldid_tmp + "(){").append("\n");
	      String attrcontent_js = "";
	      int indexFieldHead = attrcontent_tmp.indexOf("$");
	      while (indexFieldHead > -1) {
	        attrcontent_js += attrcontent_tmp.substring(0, indexFieldHead);
	        attrcontent_tmp = attrcontent_tmp.substring(indexFieldHead + 1);
	        int indexFieldTail = attrcontent_tmp.indexOf("$");
	        String cFieldid = attrcontent_tmp;
	        if(indexFieldTail > -1){
	        	cFieldid = attrcontent_tmp.substring(0, indexFieldTail);
	        }
	        int type_tmp = 0;
	        if (fieldidList != null && fieldtypeList != null && fieldidList.indexOf("" + cFieldid) > -1) {
	          type_tmp = Util.getIntValue((String) fieldtypeList.get(fieldidList.indexOf("" + cFieldid)));
	        }
	        if (type_tmp == 5) {// 金额千分位
	          attrcontent_js += ("($G(\"field" + cFieldid + "\").value.replace(/,/g,\"\"))*1.0");
	        } else {
	          attrcontent_js += ("$G(\"field" + cFieldid + "\").value*1.0");
	        }
	        attrcontent_tmp = attrcontent_tmp.substring(indexFieldTail + 1);
	        indexFieldHead = attrcontent_tmp.indexOf("$");
	      }
	      attrcontent_js += attrcontent_tmp;
	      if (isbill == 0) {
	      	  rs.executeSql("select fielddbtype from workflow_formdict where id=" + attrfieldid_tmp);
	      } else {
	      	  rs.executeSql("select fielddbtype from workflow_billfield where id=" + attrfieldid_tmp);
	      }
	      int decimalnum = 2;
	      try {
		      String strFielddbtype = "";
		      if(rs.next()) {
		      	  strFielddbtype = Util.null2String(rs.getString("fielddbtype"));
		      	  if(isoracle) {
		      	  	  if(strFielddbtype.toLowerCase().indexOf("number(") > -1) 
		      	  	  	  decimalnum = Util.getIntValue(strFielddbtype.substring(strFielddbtype.indexOf(",") + 1, strFielddbtype.indexOf(")")), 2);
		      	  } else {
		      	  	  if(strFielddbtype.toLowerCase().indexOf("decimal(") > -1) 
		      	  	  	  decimalnum = Util.getIntValue(strFielddbtype.substring(strFielddbtype.indexOf(",") + 1, strFielddbtype.indexOf(")")), 2);
		      	  }
				  if(strFielddbtype.toLowerCase().indexOf("int") > -1) decimalnum = 0;
		      }
	      } catch(Exception ex) {}
	      //attrcontent_js = "(doFormatNumber("+attrcontent_js+"," + decimalnum + "))";
	      int type_tmp = 0;
	      if (fieldidList != null && fieldtypeList != null && fieldidList.indexOf("" + attrfieldid_tmp) > -1) {
	        type_tmp = Util.getIntValue((String) fieldtypeList.get(fieldidList.indexOf("" + attrfieldid_tmp)));
	      }
	      int isedit_tmp = 0;
	      if (isedit_hs != null) {
	        isedit_tmp = Util.getIntValue((String) isedit_hs.get("isedit" + attrfieldid_tmp));
	      }
	      if (type_tmp == 4) {// 金额转换
	          if (isedit_tmp == 1) {
	            jsStr.append("").append("try{").append("\n");
	            jsStr.append("\t").append("if($G(\"field_lable" + attrfieldid_tmp + "\")){").append("\n");
	            jsStr.append("\t\t").append("var numberTemp = ").append(attrcontent_js).append(";\n");
	    		  jsStr.append("\t\t").append("var numberStr = toFix(numberTemp,"+decimalnum+")").append(";\n");
	            jsStr.append("\t\t").append("$G(\"field_lable" + attrfieldid_tmp + "\").value = numberStr").append(";\n");
	            jsStr.append("\t").append("}").append("\n");
	            jsStr.append("").append("}catch(e){}").append("\n");
	            jsStr.append("").append("try{").append("\n");
	            jsStr.append("\t").append("if($G(\"field" + attrfieldid_tmp + "\")){").append("\n");
	            jsStr.append("\t\t").append("$G(\"field" + attrfieldid_tmp + "\").value = numberStr").append(";\n");
	            jsStr.append("\t").append("}").append("\n");
	            jsStr.append("").append("}catch(e){}").append("\n");
	            jsStr.append("").append("try{").append("\n");
	            jsStr.append("\t").append("numberToFormat('" + attrfieldid_tmp + "');").append(";\n");
	            jsStr.append("").append("}catch(e){}").append("\n");
	          } else if (isedit_tmp == 0) {
	            jsStr.append("").append("try{").append("\n");
	            jsStr.append("\t").append("if($G(\"field" + attrfieldid_tmp + "\")){").append("\n");
	            jsStr.append("\t\t").append("$G(\"field" + attrfieldid_tmp + "\").value = ").append(attrcontent_js).append(";\n");
	            jsStr.append("\t\t").append("$G(\"field" + attrfieldid_tmp + "span\").innerHTML = ").append("milfloatFormat($G(\"field" + attrfieldid_tmp + "\").value);").append(";\n");
	            jsStr.append("\t\t").append("$G(\"field" + attrfieldid_tmp + "ncspan\").innerHTML = ").append("numberChangeToChinese($G(\"field" + attrfieldid_tmp + "\").value);").append(";\n");
	            jsStr.append("\t").append("}").append("\n");
	            jsStr.append("").append("}catch(e){}").append("\n");
	          }
	          jsStr.append("").append("try{").append("\n");
	          jsStr.append("\t").append("doformat" + attrfieldid_tmp + "();").append("\n");
	          jsStr.append("").append("}catch(e){}").append("\n");
	        } else if (type_tmp == 5) {// 金额千分位
	          jsStr.append("").append("try{").append("\n");
	          jsStr.append("\t").append("if($G(\"field" + attrfieldid_tmp + "\")){").append("\n");
	          jsStr.append("\t\t").append("var numberTemp = ").append(attrcontent_js).append(";\n");
	  		jsStr.append("\t\t").append("var numberStr = toFix(numberTemp,"+decimalnum+")").append(";\n");
	          jsStr.append("\t\t").append("$G(\"field" + attrfieldid_tmp + "\").value = numberStr").append(";\n");
	          jsStr.append("\t").append("}else{").append("\n");
	          jsStr.append("\t\t").append("$G(\"field" + attrfieldid_tmp + "span\").innerHTML = numberStr").append("\n");
	          jsStr.append("\t").append("}").append("\n");
	          jsStr.append("").append("}catch(e){}").append("\n");
	          jsStr.append("").append("changeToThousands('field" + attrfieldid_tmp + "');").append("\n");
	          jsStr.append("").append("try{").append("\n");
	          jsStr.append("\t").append("if($G(\"field" + attrfieldid_tmp + "\").type==\"hidden\"){").append("\n");
	          jsStr.append("\t\t").append("$G(\"field" + attrfieldid_tmp + "span\").innerHTML = ").append("$G(\"field" + attrfieldid_tmp + "\").value;").append(";\n");
	          jsStr.append("\t").append("}").append("\n");
	          jsStr.append("").append("}catch(e){}").append("\n");
	          jsStr.append("").append("try{").append("\n");
	          jsStr.append("\t").append("doformat" + attrfieldid_tmp + "();").append("\n");
	          jsStr.append("").append("}catch(e){}").append("\n");
	      } else {
	        jsStr.append("").append("try{").append("\n");
	        jsStr.append("\t").append("if($G(\"field" + attrfieldid_tmp + "\")){").append("\n");
	        jsStr.append("\t\t").append("var numberTemp = ").append(attrcontent_js).append(";\n");
			jsStr.append("\t\t").append("var numberStr = toFix(numberTemp,"+decimalnum+")").append(";\n");
	        jsStr.append("\t\t").append("$G(\"field" + attrfieldid_tmp + "\").value = numberStr").append(";\n");
	        jsStr.append("\t").append("}else{").append("\n");
	        jsStr.append("\t\t").append("$G(\"field" + attrfieldid_tmp + "span\").innerHTML = numberStr").append("\n");
	        jsStr.append("\t").append("}").append("\n");
	        jsStr.append("").append("}catch(e){}").append("\n");
	        jsStr.append("").append("try{").append("\n");
	        jsStr.append("\t").append("if($G(\"field" + attrfieldid_tmp + "\").type==\"hidden\"){").append("\n");
	        jsStr.append("\t\t").append("$G(\"field" + attrfieldid_tmp + "span\").innerHTML = numberStr").append(";\n");
	        jsStr.append("\t").append("}").append("\n");
	        jsStr.append("").append("}catch(e){}").append("\n");
	        jsStr.append("").append("try{").append("\n");
	        jsStr.append("\t").append("doformat" + attrfieldid_tmp + "();").append("\n");
	        jsStr.append("").append("}catch(e){}").append("\n");
	      }
	      jsStr.append("").append("}").append("\n");
	    }

	  //拼 日期计算的JS方法
		//先把能参与日期计算的字段的类型都查出来
		ArrayList datefieldList = new ArrayList();
		ArrayList timefieldList = new ArrayList();
		ArrayList inputfieldList = new ArrayList();
		Hashtable candatefieldtype_hs = new Hashtable();
		Hashtable isdetail_hs = new Hashtable();
//		int isbill = Util.getIntValue(request.getParameter("isbill"), 0);
//		int formid = Util.getIntValue(request.getParameter("formid"), 0);
		String sql = "";
		if(isbill == 0){
			sql = "select id, fieldhtmltype, type, 0 as viewtype from workflow_formdict fd where ( (fieldhtmltype='1' and type in (2,3,4,5) ) or (fieldhtmltype='3' and type in (2, 19) ) ) and exists (select id from workflow_formfield ff where ff.fieldid=fd.id and ff.formid="+formid+")";
			sql += " union ";
			sql += "select id, fieldhtmltype, type, 1 as viewtype from workflow_formdictdetail fdd where ( (fieldhtmltype='1' and type in (2,3,4,5) ) or (fieldhtmltype='3' and type in (2, 19) ) ) and exists (select id from workflow_formfield ff where ff.fieldid=fdd.id and ff.formid="+formid+")";
		}else{
			sql = "select id, fieldhtmltype, type, viewtype from workflow_billfield where ( (fieldhtmltype='1' and type in (2,3,4,5) ) or (fieldhtmltype='3' and type in (2, 19) ) ) and billid="+formid;
		}
		rs.execute(sql);
		while(rs.next()){
			int fieldhtmltype_tmp = Util.getIntValue(rs.getString("fieldhtmltype"), 0);
			int viewtype_tmp = Util.getIntValue(rs.getString("viewtype"), 0);
			int type = Util.getIntValue(rs.getString("type"), 0);
			int id_tmp = Util.getIntValue(rs.getString("id"), 0);
			isdetail_hs.put("viewtype"+id_tmp, ""+viewtype_tmp);
			if(fieldhtmltype_tmp == 1){
				inputfieldList.add(""+id_tmp);
			}else if(fieldhtmltype_tmp == 3){
				if(type == 2){
					datefieldList.add(""+id_tmp);
				}else if(type == 19){
					timefieldList.add(""+id_tmp);
				}
			}
		}
		//单个字段的方法，用于初步解析运算公式
		for(int i=0; i<datefieldidList.size(); i++){
			try{
				int datefieldid_tmp = Util.getIntValue((String)datefieldidList.get(i), 0);
				String datecontent_tmp = Util.null2String((String)datecontentList.get(i));
				String datecontent_s = datecontent_tmp;
				int viewtype_tmp = Util.getIntValue((String)isdetail_hs.get("viewtype"+datefieldid_tmp));
				String retDateContent = "";
				int othertype_t = Util.getIntValue((String)othertypeList.get(i), 0);
				jsStr.append("").append("function doFieldDate"+datefieldid_tmp+"(detailrow_t){").append("\n");
				
				//jsStr.append("").append("alert(\"datefieldid_tmp = "+datefieldid_tmp+"\");").append("\n");
				//jsStr.append("").append("alert(\"datecontent_tmp = "+datecontent_tmp+"\");").append("\n");
				if(viewtype_tmp == 1){
					jsStr.append("\t").append("var isdetail_t = \"_\"+detailrow_t;").append("\n");
				}else{
					jsStr.append("\t").append("var isdetail_t = \"\";").append("\n");
				}
				int index = datecontent_tmp.indexOf("$");
				while(index > -1){
					int index0 = datecontent_tmp.indexOf("$", index+1);
					String field1 = datecontent_tmp.substring(index+1, index0);
					boolean isinput = inputfieldList.contains(""+field1);
					boolean isdate = datefieldList.contains(""+field1);
					boolean istime = timefieldList.contains(""+field1);
					String mark_t = datecontent_tmp.substring(0, index).trim();
					if("+".equals(mark_t)){
						retDateContent += (" + \"+\" + ");
					}else if("-".equals(mark_t)){
						retDateContent += (" + \"-\" + ");
					}

					if(("datetime").equals(field1)){
						retDateContent += "  +\"$datetime$\"+  ";
					}else if("currentdate".equals(field1)){
						retDateContent += "  \"$date$"+currentdate+"$date$\"  ";
					}else{
						String fieldtypeStr = "";
						if(isinput == true){
							fieldtypeStr = "$input$";
						}else if(isdate == true){
							fieldtypeStr = "$date$";
						}else if(istime == true){
							fieldtypeStr = "$time$";
						}
						retDateContent += "\""+fieldtypeStr+"\"+document.getElementById(\"field"+field1+"\"+isdetail_t).value+\""+fieldtypeStr+"\"";
					}
					datecontent_tmp = datecontent_tmp.substring(index0+1);
					index = datecontent_tmp.indexOf("$");
				}
				//最后结尾如果有数据，要特殊处理
				String[] tailStr = datecontent_tmp.split("");
				Pattern pa = Pattern.compile("\\d{1}");
				if(tailStr != null){
					int ishead = -1;
					for(int j=0; j<tailStr.length; j++){
						String s = tailStr[j].trim();
						Matcher matcher = pa.matcher(s);
						if("+".equals(s)){
							if(ishead == 0){
								retDateContent += "$input$\"";
							}
							retDateContent += (" + \"+\" + ");
							ishead = 1;
						}else if("-".equals(s)){
							if(ishead == 0){
								retDateContent += "$input$\"";
							}
							retDateContent += (" + \"-\" + ");
							ishead = 1;
						}else if(matcher.find()){
							if(ishead == 1){
								retDateContent += ("\"$input$"+s);
								ishead = 0;
							}else{
								retDateContent += s;
							}
						}
					}
					if(ishead == 0){
						retDateContent += "$input$\"";
					}
				}
				jsStr.append("\t").append("var para = \"othertype="+othertype_t+"&datecontent=\";").append("\n");
				jsStr.append("\t").append("var datecontent_t = "+retDateContent+";").append("\n");
				jsStr.append("\t").append("datecontent_t = datecontent_t.replace(/\\+/g, \"%2B\");").append("\n");
				jsStr.append("\t").append("datecontent_t = datecontent_t.replace(new RegExp(\"%\",\"gm\"), \"%25\");").append("\n");
				jsStr.append("\t").append("para = para + escape(datecontent_t);").append("\n");
				jsStr.append("\t").append("doFieldDateAjax(para, \""+datefieldid_tmp+"\", isdetail_t);").append("\n");
				jsStr.append("").append("}").append("\n");
			}catch(Exception e){
				//writeLog(e);
				e.printStackTrace();
			}
		}
		try{
			//构建doformat函数
				//writeLog("allcalfieldidList = " + allcalfieldidList.size());
				for(int c=0; c<allcalfieldidList.size(); c++){
					String fieldid_t = Util.null2String((String)allcalfieldidList.get(c));
					int transtype_t = Util.getIntValue((String)transtype_hs.get("transtype"+fieldid_t));
					//writeLog("transtype_t " + transtype_t);
					//writeLog("fieldid_t " + fieldid_t);
					String jsStr_t = "";
					if(transtype_t == 1){
							
						FieldTransInterface obj = (FieldTransInterface)Class.forName("weaver.workflow.html.MoneyTrans4FieldTrans").newInstance();
						jsStr_t = obj.getTransJs(fieldid_t);
					}else if(transtype_t == 2){
							
						FieldTransInterface obj = (FieldTransInterface)Class.forName("weaver.workflow.html.MoneySpilt4FieldTrans").newInstance();
						jsStr_t = obj.getTransJs(fieldid_t);
					}
					//writeLog("jsStr_t = " + jsStr_t);
					
					jsStr.append("\n").append(jsStr_t).append("\n");
				}
			}catch(Exception e){
//				writeLog(e);
				e.printStackTrace();
			}
			
			return result;
  }
	/**
	 * 加上临时CSS关联
	 * @param swfformhtml
	 * @param realfilename
	 * @return
	 */
	public String addTempCss(String swfformhtml, String realfilename){
		try{
			if(!"".equals(realfilename)){
				String cssFileStr = tcss_h + "\n<link type=\"text/css\" rel=\"stylesheet\" href=\"/css/crmcss/"+realfilename+"\" >\n" + tcss_e;
				swfformhtml = swfformhtml + cssFileStr;
			}
		}catch(Exception e){
			writeLog(e);
		}
		return swfformhtml;
	}

	/**
	 * 删除临时css关联
	 * @param swfformhtml
	 * @return
	 */
	public String deleteTempCss(String swfformhtml){
		String wfformhtml_t = swfformhtml;
		try{
			int index_1 = wfformhtml_t.indexOf(tcss_h);
			int index_2 = wfformhtml_t.indexOf(tcss_e);
			if(index_1>-1 && index_2>index_1){
				swfformhtml = wfformhtml_t.substring(0, index_1) + wfformhtml_t.substring(index_2+tcss_e.length());
			}
		}catch(Exception e){
			writeLog(e);
		}
		return swfformhtml;
	}
	/**
	 * 如果关联了CSS文件，把这个文件拼在已经组织好的Html文件的头部
	 */
	public void getCssFile(){
		try{
			String wfformhtml_t = wfformhtml;
			wfformhtml = deleteTempCss(wfformhtml_t);
			String realfilename = "";
			String cssFileStr = "";
			if(cssfile > 0){

				RecordSet rs = new RecordSet();
				rs.execute("select * from workflow_crmcssfile where id="+cssfile);
				if(rs.next()){
					realfilename = Util.null2String(rs.getString("realfilename"));



				}
			}
			if("".equals(realfilename)){//<LINK href="/css/Weaver.css" type=text/css rel=STYLESHEET>
				realfilename = "lanlv.css";
			}
			cssFileStr = "\n<link type=\"text/css\" rel=\"stylesheet\" href=\"/css/crmcss/"+realfilename+"\" >\n";
			wfformhtml = cssFileStr + wfformhtml;
		}catch(Exception e){
			writeLog(e);
		}
	}

  /**
   * 
   * @param sql
   * @param paraList
   *          currentuser,currentdept,creater,credept
   * @return
   */
  public static String analyzeSql(String sql, ArrayList paraList, ArrayList valueList) {
    String sqlStr = sql;
    try {
      for (int i = 0; i < paraList.size(); i++) {
        String para = Util.null2String((String) paraList.get(i));
        String value = Util.null2String((String) valueList.get(i));
        sqlStr = sqlStr.replace("$" + para + "$", value);
      }
    } catch (Exception e) {
//      writeLog(e);
    	e.printStackTrace();
      sqlStr = sql;
    }
    return sqlStr;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public int getIscreate() {
    return iscreate;
  }

  public void setIscreate(int iscreate) {
    this.iscreate = iscreate;
  }

  public StringBuffer getJsStr() {
    return jsStr;
  }

  public void setJsStr(StringBuffer jsStr) {
    this.jsStr = jsStr;
  }

  public StringBuffer getVbsStr() {
    return vbsStr;
  }

  public void setVbsStr(StringBuffer vbsStr) {
    this.vbsStr = vbsStr;
  }

  public Hashtable getOtherPara_hs() {
    return otherPara_hs;
  }

  public void setOtherPara_hs(Hashtable otherPara_hs) {
    this.otherPara_hs = otherPara_hs;
  }

  public String getNeedcheck() {
    return needcheck;
  }

  public void setNeedcheck(String needcheck) {
    this.needcheck = needcheck;
  }

  public StringBuffer getHtmlHiddenElementsb() {
    return htmlHiddenElementsb;
  }

  public void setHtmlHiddenElementsb(StringBuffer htmlHiddenElementsb) {
    this.htmlHiddenElementsb = htmlHiddenElementsb;
  }

  public static Map parseSAPInvokeInfo(String configstr) throws Exception {
		Map sapInfo = new HashMap();
		BaseBean basebean = new BaseBean();
		try{
			XMLSerializer xmlser = new XMLSerializer();
			JSONObject jsonObj = JSONObject.fromObject(configstr);
			String configXml = xmlser.write(jsonObj);
			SAXBuilder builder = new SAXBuilder();
			StringReader reader = new StringReader(configXml);
			Document doc = builder.build(reader);
			Element root = doc.getRootElement();
			
			//函数名
			String functionName = root.getChild("FunctionName").getText().trim().toUpperCase();
			sapInfo.put("FunctionName", functionName);
			
			//输入参数
			Element inputParamsE = root.getChild("InputParams");
			if(inputParamsE != null){			
				List inputParamList = new ArrayList();
				sapInfo.put("InputParams", inputParamList);
				List inputParamListE = inputParamsE.getChildren("e");
				if(inputParamListE != null){
					for(int i = 0; i<inputParamListE.size(); i++){
						Element e = (Element)inputParamListE.get(i);
						String SAPParamName = e.getChildText("SAPParamName").trim().toUpperCase();
						String FromOAField = e.getChildText("FromOAField").trim();
						Map oneparam = new HashMap();
						oneparam.put("SAPParamName", SAPParamName);
						oneparam.put("FromOAField", FromOAField);
						inputParamList.add(oneparam);
					}
				}
			}
			
			//返回参数
			Element outputParamsE = root.getChild("OutputParams");
			if(outputParamsE != null){			
				List outputParamList = new ArrayList();
				sapInfo.put("OutputParams", outputParamList);
				List outputParamListE = outputParamsE.getChildren("e");
				if(outputParamListE != null){
					for(int i = 0; i<outputParamListE.size(); i++){
						Element e = (Element)outputParamListE.get(i);
						String SAPParamName = e.getChildText("SAPParamName").trim().toUpperCase();
						String TOOAField = e.getChildText("TOOAField").trim();
						Map oneparam = new HashMap();
						oneparam.put("SAPParamName", SAPParamName);
						oneparam.put("TOOAField", TOOAField);
						outputParamList.add(oneparam);
					}
				}
			}
			
			//输入结构
			Element inputStructsE = root.getChild("InputStructs");
			if(inputStructsE != null){
				List inputStructList = new ArrayList();
				sapInfo.put("InputStructs", inputStructList);
				List inputStructListE = inputStructsE.getChildren("e");
				if(inputStructListE != null){
					for(int i = 0; i<inputStructListE.size(); i++){
						Element structE = (Element)inputStructListE.get(i);
						String structName = structE.getChildText("StructName").trim().toUpperCase();
						Map onestructMap = new HashMap();
						onestructMap.put("StructName", structName);
						List structFieldsE = structE.getChild("Fields").getChildren("e");
						List structFieldList = new ArrayList();
						for(int j = 0; j<structFieldsE.size(); j++){
							Element e = (Element)structFieldsE.get(j);
							String SAPFieldName = e.getChildText("SAPFieldName").trim().toUpperCase();
							String FromOAField = e.getChildText("FromOAField").trim();
							Map onefieldmap = new HashMap();
							onefieldmap.put("SAPFieldName", SAPFieldName);
							onefieldmap.put("FromOAField", FromOAField);
							structFieldList.add(onefieldmap);
						}
						onestructMap.put("Fields", structFieldList);
						inputStructList.add(onestructMap);
					}
				}
			}
			
			//返回结构
			Element outputStructsE = root.getChild("OutputStructs");
			if(outputStructsE != null){
				List outputStructList = new ArrayList();
				sapInfo.put("OutputStructs", outputStructList);
				List outputStructListE = outputStructsE.getChildren("e");
				if(outputStructListE != null){
					for(int i = 0; i<outputStructListE.size(); i++){
						Element structE = (Element)outputStructListE.get(i);
						String structName = structE.getChildText("StructName").trim().toUpperCase();
						Map onestructMap = new HashMap();
						onestructMap.put("StructName", structName);
						List structFieldsE = structE.getChild("Fields").getChildren("e");
						List structFieldList = new ArrayList();
						for(int j = 0; j<structFieldsE.size(); j++){
							Element e = (Element)structFieldsE.get(j);
							String SAPFieldName = e.getChildText("SAPFieldName").trim().toUpperCase();
							String TOOAField = e.getChildText("TOOAField").trim();
							Map onefieldmap = new HashMap();
							onefieldmap.put("SAPFieldName", SAPFieldName);
							onefieldmap.put("TOOAField", TOOAField);
							structFieldList.add(onefieldmap);
						}
						onestructMap.put("Fields", structFieldList);
						outputStructList.add(onestructMap);
					}
				}
			}
			
			//输入表
			Element inputTablesE = root.getChild("InputTables");
			if(inputTablesE != null){
				List inputTableList = new ArrayList();
				sapInfo.put("InputTables", inputTableList);
				List inputTableListE = inputTablesE.getChildren("e");
				if(inputTableListE != null){
					for(int i = 0; i<inputTableListE.size(); i++){
						Element TableE = (Element)inputTableListE.get(i);
						String TableName = TableE.getChildText("TableName").trim().toUpperCase();
						Map oneTableMap = new HashMap();
						oneTableMap.put("TableName", TableName);
						List TableFieldsE = TableE.getChild("Fields").getChildren("e");
						List TableFieldList = new ArrayList();
						for(int j = 0; j<TableFieldsE.size(); j++){
							Element e = (Element)TableFieldsE.get(j);
							String SAPFieldName = e.getChildText("SAPFieldName").trim().toUpperCase();
							String FromOAField = e.getChildText("FromOAField").trim();
							Map onefieldmap = new HashMap();
							onefieldmap.put("SAPFieldName", SAPFieldName);
							onefieldmap.put("FromOAField", FromOAField);
							TableFieldList.add(onefieldmap);
						}
						oneTableMap.put("Fields", TableFieldList);
						inputTableList.add(oneTableMap);
					}
				}
			}
			
			//返回表
			Element outputTablesE = root.getChild("OutputTables");
			if(outputTablesE != null){
				List outputTableList = new ArrayList();
				sapInfo.put("OutputTables", outputTableList);
				List outputTableListE = outputTablesE.getChildren("e");
				if(outputTableListE != null){
					for(int i = 0; i<outputTableListE.size(); i++){
						Element TableE = (Element)outputTableListE.get(i);
						String TableName = TableE.getChildText("TableName").trim().toUpperCase();
						Map oneTableMap = new HashMap();
						oneTableMap.put("TableName", TableName);
						List TableFieldsE = TableE.getChild("Fields").getChildren("e");
						List TableFieldList = new ArrayList();
						for(int j = 0; j<TableFieldsE.size(); j++){
							Element e = (Element)TableFieldsE.get(j);
							String SAPFieldName = e.getChildText("SAPFieldName").trim().toUpperCase();
							String TOOAField = e.getChildText("TOOAField").trim();
							Map onefieldmap = new HashMap();
							onefieldmap.put("SAPFieldName", SAPFieldName);
							onefieldmap.put("TOOAField", TOOAField);
							TableFieldList.add(onefieldmap);
						}
						oneTableMap.put("Fields", TableFieldList);
						outputTableList.add(oneTableMap);
					}
				}
			}
			
			
			//System.out.println(sapInfo);
			reader.close();
			
		}catch(Exception e){
			//System.out.println(e);
			//e.printStackTrace();
			basebean.writeLog("解析SAP配置信息时出错："+e);
			throw e;
		}
		return sapInfo;
	}

    /**
     * 获取当前用户能够查看的节点签字意见
     * @param user 查看用户
     * @param workflowid 工作流id
     * @param requestid 请求id
     * @param isurger 督办人可查看
     * @param intervenorright 流程监控
     * @return 可查看节点id集合
     */
    private List getSigntCanViewNodeIds(User user, int workflowid, int requestid, String isurger, int intervenorright) {
    	List canViewIds = new ArrayList();
    	RecordSet scvnrs = new RecordSet();
    	RecordSet rsn = new RecordSet();
    	RecordSet rsn2 = new RecordSet();
    	
    	String singleViewLogIds = "-1";
    	char procflag=Util.getSeparator();
    	
    	scvnrs.executeSql("select nodeid from workflow_currentoperator where requestid=" + requestid + " and userid=" + user.getUID() + " order by receivedate desc ,receivetime desc");

    	if(scvnrs.next()){
    		String viewNodeId = scvnrs.getString("nodeid");
    		rsn.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowid + " and nodeid="+viewNodeId);
    		if(rsn.next()){
    			singleViewLogIds = rsn.getString("viewnodeids");
    		}
    		
    		if("-1".equals(singleViewLogIds)){ //全部查看
    			rsn2.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowid+" and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid="+requestid+"))");
	    		while(rsn2.next()){
		    		String tempNodeId = rsn2.getString("nodeid");
		    		if(!canViewIds.contains(tempNodeId)){
		    			canViewIds.add(tempNodeId);
		    		}
	    		}
    		} else if(singleViewLogIds != null && !"".equals(singleViewLogIds)){//查看部分
    			String tempidstrs[] = Util.TokenizerString2(singleViewLogIds, ",");
	    		for(int i=0;i<tempidstrs.length;i++){
		    		if(!canViewIds.contains(tempidstrs[i])){
		    			canViewIds.add(tempidstrs[i]);
		    		}
	    		}
    		}
    	}
    	
    	if(isurger.trim().equals("true") || intervenorright>0){
    		rsn2.executeSql("select nodeid from workflow_flownode where workflowid=" + workflowid);
    		while(rsn2.next()){
	    		String tempNodeId = rsn2.getString("nodeid");
	    		if(!canViewIds.contains(tempNodeId)){
	    			canViewIds.add(tempNodeId);
	    		}
    		}
    	}
    	
    	return canViewIds;
    }
  
	public boolean getIsRemarkInnerMode() {
		return isRemarkInnerMode;
	}

	public boolean getHasRemark(){
		return this.hasRemark;
	}
}
