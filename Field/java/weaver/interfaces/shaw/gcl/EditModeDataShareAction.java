package weaver.interfaces.shaw.gcl;


import weaver.hrm.User;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

/**
 * Created by adore on 2017/3/17.
 * editModeDataShare()��Ȩ��ֻ�ܱ༭һ��������
 * �༭������ٴ��ع�Ȩ��
 */
public class EditModeDataShareAction implements Action {
    BaseBean log = new BaseBean();//����д����־�Ķ���

    public String execute(RequestInfo info) {
        log.writeLog("����Ȩ���ع�EditModeDataShareAction������������");

        String modeId = info.getWorkflowid();//��ȡ����ģ�У�modeId
        String billid = info.getRequestid();//��ȡ����ģ�У�billid
        String creatorid = info.getCreatorid();//
        User user = new User();
        ModeRightInfo modeRightInfo = new ModeRightInfo();

        //int userid = user.getUID();//��ȡ��ǰ��¼�û���ID
        //user.getUID();

        if (!"".equals(billid) && !"".equals(modeId)) {
            modeRightInfo.editModeDataShare(Integer.parseInt(creatorid), Integer.parseInt(modeId), Integer.parseInt(billid));
            log.writeLog("modeId=" + modeId);
            log.writeLog("billid=" + billid);
            log.writeLog("creatorid=" + creatorid);
            log.writeLog("Ȩ���ع�EditModeDataShareAction-------SUCCESS!");
        }

        return SUCCESS;
    }
}
