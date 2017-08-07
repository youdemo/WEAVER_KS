package weaver.interfaces.shaw.xerium;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.schedule.BaseCronJob;

/**
 * Created by adore on 2017/4/6.
 * 年底把当年年假基数剩余替换到年假上年结余小时数
 */
public class ResetLastAnnualLeftCron extends BaseCronJob {
    public void execute() {
        RecordSet rs = new RecordSet();
        BaseBean log = new BaseBean();
        log.writeLog("ResetLastAnnualLeft Start!");
        String sql_1 = " update formtable_main_20 set lastLeft=gsjq  ";//当年年假基数剩余赋值给下一年的年假上年结余小时数
        log.writeLog("sql_1=" + sql_1);

        String sql_2 = " update formtable_main_20 set gsjqz=lastLeft,gsjq=nj  ";//年假上年结余小时数赋值给年假上年结余剩余,当年年假基数赋值给当年年假基数剩余
        log.writeLog("sql_2=" + sql_2);

        if (rs.executeSql(sql_1)) {
            log.writeLog("当年年假基数剩余赋值给下一年的年假上年结余小时数 Success!");
        } else {
            log.writeLog("ResetLastAnnualLeft Failed!----0");
        }

        if (rs.executeSql(sql_2)) {
            log.writeLog("年假上年结余剩余,当年年假基数剩余同步 Success!");
        } else {
            log.writeLog("ResetLastAnnualLeft Failed!----1");
        }
    }
}
