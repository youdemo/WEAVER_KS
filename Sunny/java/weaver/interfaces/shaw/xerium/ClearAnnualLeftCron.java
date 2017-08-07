package weaver.interfaces.shaw.xerium;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.schedule.BaseCronJob;


/**
 * Created by adore on 2017/4/6.
 * ��ʱ����������ʣ��
 */
public class ClearAnnualLeftCron extends BaseCronJob {
    public void execute() {
        RecordSet rs = new RecordSet();
        BaseBean log = new BaseBean();
        log.writeLog("ClearAnnualLeftStart!");
        String sql = " update formtable_main_20 set gsjqz=0,lastLeft=0  ";
        log.writeLog("sql=" + sql);
        if (rs.executeSql(sql)) {
            log.writeLog("ClearAnnualLeftSuccess!");
        } else {
            log.writeLog("ClearAnnualLeftFailed!");
        }
    }
}
