package weaver.interfaces.shaw.util;

import java.util.Iterator;
import java.util.Map;


import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;

public class InsertUtil {

    // 本系统的数据插入操作
    public boolean insert(Map<String, String> mapStr, String table) {
        if (mapStr == null) return false;
        if (mapStr.isEmpty()) return false;

        RecordSet rs = new RecordSet();

        String sql_0 = "insert into " + table + "(";
        StringBuffer sql_1 = new StringBuffer();
        String sql_2 = ") values(";
        StringBuffer sql_3 = new StringBuffer();
        String sql_4 = ")";

        Iterator<String> it = mapStr.keySet().iterator();
        while (it.hasNext()) {
            String tmp_1 = it.next();
            //	String tmp_1_str = Util.null2String(mapStr.get(tmp_1));
            String tmp_1_str = mapStr.get(tmp_1);
            if (tmp_1_str == null) tmp_1_str = "";

            if (tmp_1_str.length() > 0) {
                sql_1.append(tmp_1);
                sql_1.append(",");

                if (tmp_1_str.contains("##")) {
                    sql_3.append(tmp_1_str.replace("##", ""));
                    sql_3.append(",");
                } else {
                    sql_3.append("'");
                    sql_3.append(tmp_1_str);
                    sql_3.append("',");
                }
            }
        }

        String now_sql_1 = sql_1.toString();
        if (now_sql_1.lastIndexOf(",") > 0) {
            now_sql_1 = now_sql_1.substring(0, now_sql_1.length() - 1);
        }

        String now_sql_3 = sql_3.toString();
        if (now_sql_3.lastIndexOf(",") > 0) {
            now_sql_3 = now_sql_3.substring(0, now_sql_3.length() - 1);
        }

        String sql = sql_0 + now_sql_1 + sql_2 + now_sql_3 + sql_4;

        new BaseBean().writeLog("sql = " + sql);

        //return false;
        return rs.executeSql(sql);
    }

    // 外系统的数据插入操作   RecordSetDataSource rsx
    public boolean insert(Map<String, String> mapStr, String table, RecordSetDataSource rsx) {
        if (mapStr == null) return false;
        if (mapStr.isEmpty()) return false;

        String sql_0 = "insert into " + table + "(";
        StringBuffer sql_1 = new StringBuffer();
        String sql_2 = ") values(";
        StringBuffer sql_3 = new StringBuffer();
        String sql_4 = ")";

        Iterator<String> it = mapStr.keySet().iterator();
        while (it.hasNext()) {
            String tmp_1 = it.next();
            String tmp_1_str = Util.null2String(mapStr.get(tmp_1));
            if (tmp_1_str.length() > 0) {
                sql_1.append(tmp_1);
                sql_1.append(",");

                if (tmp_1_str.contains("##")) {
                    sql_3.append(tmp_1_str.replace("##", ""));
                    sql_3.append(",");
                } else {
                    sql_3.append("'");
                    sql_3.append(tmp_1_str);
                    sql_3.append("',");
                }
            }
        }

        String now_sql_1 = sql_1.toString();
        if (now_sql_1.lastIndexOf(",") > 0) {
            now_sql_1 = now_sql_1.substring(0, now_sql_1.length() - 1);
        }

        String now_sql_3 = sql_3.toString();
        if (now_sql_3.lastIndexOf(",") > 0) {
            now_sql_3 = now_sql_3.substring(0, now_sql_3.length() - 1);
        }

        String sql = sql_0 + now_sql_1 + sql_2 + now_sql_3 + sql_4;

        new BaseBean().writeLog("sql = " + sql);

        return rsx.executeSql(sql);
    }
}
