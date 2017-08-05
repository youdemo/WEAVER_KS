package weaver.interfaces.shaw.util;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;

/**
 * Created by adore on 2017/3/20.
 * �Զ����ѡ�����ť��ҳ�б���ʾ
 */
public class MultiBrowserUtil extends BaseBean {
    BaseBean log = new BaseBean();//����д����־�Ķ���

    public String getMultiBrowserValue(String colValues, String browserType) {
        RecordSet rs = new RecordSet();
        /**
         * colValues �ֶε�ֵ
         * browserType ���������,doc:�ĵ�;request:����;resource:������
         */
        String para = "";
        colValues += ",";
        String[] columns = colValues.split(",");
        for (int i = 0; i < columns.length; i++) {
            String col_id = columns[i];
            //System.out.println("--" + columns[i]);
            if ("doc".equals(browserType)) {
                String docName = "";
                String sql = " select id,docsubject,accessorycount from docdetail where id= " + col_id;
                rs.execute(sql);
                if (rs.next()) {
                    docName = Util.null2String(rs.getString("docsubject"));
                    //log.writeLog("docName=" + docName);
                }
                para = para + "<a href=javascript:openFullWindowHaveBar(\"/docs/docs/DocDsp.jsp?id=" + col_id + "\")>" + docName + "</a>&nbsp; ";
            } else if ("request".equals(browserType)) {
                String workflowName = "";
                String sql = " select requestname from workflow_requestbase where requestid= " + col_id;
                rs.execute(sql);
                if (rs.next()) {
                    workflowName = Util.null2String(rs.getString("requestname"));
                    //log.writeLog("workflowName=" + workflowName);
                }
                para = para + "<a href=javascript:openFullWindowHaveBar(\"/workflow/request/ViewRequest.jsp?isrequest=1&requestid=" + col_id + "\")>" + workflowName + "</a>&nbsp; ";
            } else if ("resource".equals(browserType)) {
                String lastname = "";
                String sql = " select lastname from Hrmresource where id= " + col_id;
                rs.execute(sql);
                if (rs.next()) {
                    lastname = Util.null2String(rs.getString("lastname"));
                    //log.writeLog("lastname=" + lastname);
                }
                para = para + "<a href=\'javaScript:openhrm(" + col_id + ");\' onclick=\'pointerXY(event);\'>" + lastname + "</a>&nbsp;";
            }
        }

        return para;
    }
}
