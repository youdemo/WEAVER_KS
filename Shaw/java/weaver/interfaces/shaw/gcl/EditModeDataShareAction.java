package weaver.interfaces.shaw.gcl;


import weaver.hrm.User;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

/**
 * Created by adore on 2017/3/17.
 * editModeDataShare()赋权限只能编辑一次问题解决
 * 编辑保存后再次重构权限
 */
public class EditModeDataShareAction implements Action {
    BaseBean log = new BaseBean();//定义写入日志的对象

    public String execute(RequestInfo info) {
        log.writeLog("进入权限重构EditModeDataShareAction——————");

        String modeId = info.getWorkflowid();//获取表单建模中：modeId
        String billid = info.getRequestid();//获取表单建模中：billid
        String creatorid = info.getCreatorid();//
        User user = new User();
        ModeRightInfo modeRightInfo = new ModeRightInfo();

        //int userid = user.getUID();//获取当前登录用户的ID
        //user.getUID();

        if (!"".equals(billid) && !"".equals(modeId)) {
            modeRightInfo.editModeDataShare(Integer.parseInt(creatorid), Integer.parseInt(modeId), Integer.parseInt(billid));
            log.writeLog("modeId=" + modeId);
            log.writeLog("billid=" + billid);
            log.writeLog("creatorid=" + creatorid);
            log.writeLog("权限重构EditModeDataShareAction-------SUCCESS!");
        }

        return SUCCESS;
    }
}
