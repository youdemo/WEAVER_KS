package weaver.interfaces.shaw.dp.org;

import weaver.general.BaseBean;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.interfaces.shaw.dp.org.DpSyncOrg;

/**
 * Created by adore on 2017/4/21.
 * 德鹏组织结构同步定时任务
 */
public class SyncOrgCronJob extends BaseCronJob {
    public void execute() {
        BaseBean log = new BaseBean();

        DpSyncOrg dso = new DpSyncOrg();
        String result = dso.syncOrg();
        log.writeLog("SyncOrg  Start!");
        log.writeLog("result=" + result);
        log.writeLog("SyncOrg  Success!");
    }
}
