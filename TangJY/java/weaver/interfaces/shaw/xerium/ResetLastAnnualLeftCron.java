package weaver.interfaces.shaw.xerium;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.schedule.BaseCronJob;

/**
 * Created by adore on 2017/4/6.
 * ��װѵ�����ٻ���ʣ���滻������������Сʱ��
 */
public class ResetLastAnnualLeftCron extends BaseCronJob {
    public void execute() {
        RecordSet rs = new RecordSet();
        BaseBean log = new BaseBean();
        log.writeLog("ResetLastAnnualLeft Start!");
        String sql_1 = " update formtable_main_20 set lastLeft=gsjq  ";//������ٻ���ʣ�ำֵ����һ�������������Сʱ��
        log.writeLog("sql_1=" + sql_1);

        String sql_2 = " update formtable_main_20 set gsjqz=lastLeft,gsjq=nj  ";//����������Сʱ����ֵ������������ʣ��,������ٻ�����ֵ��������ٻ���ʣ��
        log.writeLog("sql_2=" + sql_2);

        if (rs.executeSql(sql_1)) {
            log.writeLog("������ٻ���ʣ�ำֵ����һ�������������Сʱ�� Success!");
        } else {
            log.writeLog("ResetLastAnnualLeft Failed!----0");
        }

        if (rs.executeSql(sql_2)) {
            log.writeLog("����������ʣ��,������ٻ���ʣ��ͬ�� Success!");
        } else {
            log.writeLog("ResetLastAnnualLeft Failed!----1");
        }
    }
}
