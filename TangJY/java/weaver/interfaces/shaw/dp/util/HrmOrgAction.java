package weaver.interfaces.shaw.dp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import weaver.interfaces.shaw.dp.util.TmcDBUtil;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.hrm.job.JobTitlesComInfo;
import weaver.hrm.job.JobTitlesTempletComInfo;
import weaver.hrm.resource.ResourceComInfo;

public class HrmOrgAction {

    /*
     *   分部操作
     *   参数   hcb 分部对象参数
     *   原则：  oracle 或 Sqlserver都可以执行的SQL
     */
    public ReturnInfo operSubCompany(HrmSubCompanyBean hcb) {
        TmcDBUtil tdu = new TmcDBUtil();
        BaseBean log = new BaseBean();
        ReturnInfo ri = new ReturnInfo();
        String subCompanyCode = hcb.getSubCompanyCode();
        RecordSet rs = new RecordSet();
        rs.executeSql("select id from HrmSubCompany where subcompanycode = '" + subCompanyCode + "'");
        int id = 0;
        if (rs.next()) {
            id = rs.getInt("id");
        }

        // 查询公司直接上级
        int idOrCode = hcb.getIdOrCode();
        String superID = "";
        if (idOrCode == 0) {
            superID = Util.null2String(hcb.getSuperID());
        } else if (idOrCode == 1) {
            rs.executeSql("select id from HrmSubCompany where subcompanycode = '" + hcb.getSuperCode() + "'");
            if (rs.next()) {
                superID = Util.null2String(rs.getString("id"));
            }
        }
        if ("".equals(superID) || String.valueOf(id).equals(superID)) superID = "0";

        if (id < 1) {  // 不存在，需要新增
            // 把记录插入到分部表
            //		String sql = "insert into hrmsubcompany (subcompanyname,subcompanydesc,subcompanycode,"
            //				+"supsubcomid,companyid,showorder,canceled)" +
            //				"  values ('"+hcb.getSubCompanyName()+"','"+hcb.getSubCompanyDesc()+"','"
            //				+subCompanyCode+"',"+superID+",1,'"+hcb.getOrderBy()+"','"+hcb.getStatus()+"')";

            Map<String, String> mapStr = new HashMap<String, String>();
            mapStr.put("subcompanycode", subCompanyCode);
            mapStr.put("subcompanyname", hcb.getSubCompanyName());
            mapStr.put("subcompanydesc", hcb.getSubCompanyDesc());
            mapStr.put("supsubcomid", superID);
            mapStr.put("companyid", "1");
            mapStr.put("showorder", "" + hcb.getOrderBy());
            mapStr.put("canceled", "" + hcb.getStatus());

            boolean isRun = tdu.insert("HrmSubCompany", mapStr);

            //	boolean isRun = rs.executeSql(sql);
            if (!isRun) {
                ri.setMessage(false, "1002", "分部信息 插入错误！");
                return ri;
            }

            rs.executeSql("select id from hrmsubcompany where subcompanycode='" + subCompanyCode + "'");
            if (rs.next()) {
                id = rs.getInt("id");
            }
            if (id < 1) {
                ri.setMessage(false, "1003", "分部信息 插入后 无记录错误！");
                return ri;
            }
            // 菜单插入操作
            rs.executeSql(" insert into leftmenuconfig (userid,infoid,visible,viewindex,resourceid,resourcetype,locked,lockedbyid,usecustomname,customname,customname_e)  select  " +
                    "	distinct  userid,infoid,visible,viewindex," + id + ",2,locked,lockedbyid,usecustomname,customname,customname_e from leftmenuconfig where resourcetype=1  and resourceid=1");
            rs.executeSql("insert into mainmenuconfig (userid,infoid,visible,viewindex,resourceid,resourcetype,locked,lockedbyid,usecustomname,customname,customname_e)  select  " +
                    "	distinct  userid,infoid,visible,viewindex," + id + ",2,locked,lockedbyid,usecustomname,customname,customname_e from mainmenuconfig where resourcetype=1  and resourceid=1");

        } else { // 已经存在，需要更新
            // 不需要更新的值 记录下来。
            List<String> notList = new ArrayList<String>();
            List<String> list = hcb.getNotUpdate();
            if (list.size() > 0) {
                String sql = "select * from HrmSubCompany where id=" + id;
                rs.executeSql(sql);
                if (rs.next()) {
                    for (String tmp_f : list) {
                        String tmp_f_val = Util.null2String(rs.getString(tmp_f));
                        if (tmp_f_val.length() > 0) {
                            notList.add(tmp_f);
                        }
                    }

                }
            }
            Map<String, String> mapStr = new HashMap<String, String>();
            Map<String, String> whereMap = new HashMap<String, String>();
            whereMap.put("id", "" + id);
            // departmentcode,departmentname,departmentmark,supdepid,subcompanyid1

            if (!notList.contains("subcompanyname".toUpperCase())) {
                mapStr.put("subcompanyname", hcb.getSubCompanyName());
            }
            if (!notList.contains("subcompanydesc".toUpperCase())) {
                mapStr.put("subcompanydesc", hcb.getSubCompanyDesc());
            }
            if (!notList.contains("supsubcomid".toUpperCase())) {
                mapStr.put("supsubcomid", superID);
            }
            if (!notList.contains("showorder".toUpperCase())) {
                mapStr.put("showorder", "" + hcb.getOrderBy());
            }
            if (!notList.contains("canceled".toUpperCase())) {
                mapStr.put("canceled", "" + hcb.getStatus());
            }

            boolean isRun = tdu.update("HrmSubCompany", mapStr, whereMap);

            if (!isRun) {
                ri.setMessage(false, "1001", "分部信息 更新错误！");
                return ri;
            }
        }
        // 自定义信息处理
        String tableName = "HrmSubcompanyDefined";
        Map<String, String> updateMap = hcb.getCusMap();
        Map<String, String> mainMap = new HashMap<String, String>();
        mainMap.put("subcomid", "" + id);
        String message = updateCustom(tableName, mainMap, updateMap);
        ri.setRemark(message);

        // 更新分部缓存
        try {
            SubCompanyComInfo SubCompanyComInfo = new SubCompanyComInfo();
            SubCompanyComInfo.removeCompanyCache();
        } catch (Exception e) {
            e.printStackTrace();
            log.writeLog(e.getMessage());
        }
        return ri;
    }

    /*
     *   部门操作
     *   参数   hdb 部门对象参数
     *   原则：  oracle 或 Sqlserver都可以执行的SQL
     */
    public ReturnInfo operDept(HrmDepartmentBean hdb) {
        TmcDBUtil tdu = new TmcDBUtil();
        BaseBean log = new BaseBean();
        ReturnInfo ri = new ReturnInfo();
        String departmentcode = hdb.getDepartmentcode();
        RecordSet rs = new RecordSet();
        String sql = "";
        sql = "select id from HrmDepartment where departmentcode='" + departmentcode + "' ";
        rs.executeSql(sql);
        int id = 0;
        if (rs.next()) {
            id = rs.getInt("id");
        }

        // 获取直属分部
        int comIdOrCode = hdb.getComIdOrCode();
        String subComID = "";
        if (comIdOrCode == 0) {
            subComID = Util.null2String(hdb.getSubcompanyid1());
        } else if (comIdOrCode == 1) {
            sql = "select id from hrmsubcompany where subcompanycode='" + hdb.getSubcompanyCode() + "'";
            rs.executeSql(sql);
            if (rs.next()) {
                subComID = Util.null2String(rs.getString("id"));
            }
        }
        if ("".equals(subComID)) {
            ri.setMessage(false, "1100", "部门信息 分部未查询到,终止！");
            return ri;
        }

        // 查询公司直接部门
        int idOrCode = hdb.getIdOrCode();
        String superID = "";
        if (idOrCode == 0) {
            superID = Util.null2String(hdb.getSuperID());
        } else {
            sql = "select id from HrmDepartment where departmentcode = '" + hdb.getSuperCode() + "'";
            rs.executeSql(sql);
            if (rs.next()) {
                superID = Util.null2String(rs.getString("id"));
            }
        }
        if ("".equals(superID) || String.valueOf(id).equals(superID)) superID = "0";

        if (id < 1) {
            Map<String, String> mapStr = new HashMap<String, String>();
            mapStr.put("departmentcode", departmentcode);
            mapStr.put("departmentname", hdb.getDepartmentname());
            mapStr.put("departmentmark", hdb.getDepartmentark());
            mapStr.put("supdepid", superID);
            mapStr.put("subcompanyid1", subComID);
            mapStr.put("canceled", "" + hdb.getStatus());
            mapStr.put("showorder", "" + hdb.getOrderBy());

            boolean isRun = tdu.insert("HrmDepartment", mapStr);
            if (!isRun) {
                ri.setMessage(false, "1101", "部门信息 插入操作错误！");
                return ri;
            }

            rs.executeSql("select id from HrmDepartment where departmentcode='" + departmentcode + "' ");
            if (rs.next()) {
                id = rs.getInt("id");
            }
            if (id < 1) {
                ri.setMessage(false, "1103", "部门信息 插入后 无记录错误！");
                return ri;
            }

        } else { // 更新部门信息
            // 不需要更新的值 记录下来。
            List<String> notList = new ArrayList<String>();
            List<String> list = hdb.getNotUpdate();
            if (list.size() > 0) {
                sql = "select * from HrmDepartment where id=" + id;
                rs.executeSql(sql);
                if (rs.next()) {
                    for (String tmp_f : list) {
                        String tmp_f_val = Util.null2String(rs.getString(tmp_f));
                        if (tmp_f_val.length() > 0) {
                            notList.add(tmp_f);
                        }
                    }

                }
            }

            Map<String, String> whereMap = new HashMap<String, String>();
            whereMap.put("id", "" + id);
            // departmentcode,departmentname,departmentmark,supdepid,subcompanyid1
            Map<String, String> mapStr = new HashMap<String, String>();

            if (!notList.contains("departmentname".toUpperCase())) {
                mapStr.put("departmentname", hdb.getDepartmentname());
            }
            if (!notList.contains("departmentmark".toUpperCase())) {
                mapStr.put("departmentmark", hdb.getDepartmentark());
            }
            if (!notList.contains("supdepid".toUpperCase())) {
                mapStr.put("supdepid", superID);
            }
            if (!notList.contains("subcompanyid1".toUpperCase())) {
                mapStr.put("subcompanyid1", subComID);
            }
            if (!notList.contains("canceled".toUpperCase())) {
                mapStr.put("canceled", "" + hdb.getStatus());
            }
            if (!notList.contains("showorder".toUpperCase())) {
                mapStr.put("showorder", "" + hdb.getOrderBy());
            }

            boolean isRun = tdu.update("HrmDepartment", mapStr, whereMap);
            if (!isRun) {
                ri.setMessage(false, "1102", "部门信息 更新错误！");
                return ri;
            }
        }
        // 自定义信息处理
        String tableName = "HrmDepartmentDefined";
        Map<String, String> updateMap = hdb.getCusMap();
        Map<String, String> mainMap = new HashMap<String, String>();
        mainMap.put("deptid", "" + id);
        String message = updateCustom(tableName, mainMap, updateMap);
        ri.setRemark(message);

        try {
            DepartmentComInfo DepartmentComInfo = new DepartmentComInfo();
            DepartmentComInfo.removeCompanyCache();
        } catch (Exception e) {
            e.printStackTrace();
            log.writeLog(e.getMessage());
        }

        return ri;
    }

    /*
     *   岗位操作
     *   参数   hjt 岗位对象参数
     *   原则：  oracle 或 Sqlserver都可以执行的SQL
     */
    public ReturnInfo operJobtitle(HrmJobTitleBean hjt) {
        TmcDBUtil tdu = new TmcDBUtil();
        BaseBean log = new BaseBean();
        ReturnInfo ri = new ReturnInfo();
        String jobtitlecode = hjt.getJobtitlecode();
        RecordSet rs = new RecordSet();
        String sql = "";
        sql = "select id from HrmJobTitles where jobtitlecode='" + jobtitlecode + "' ";
        rs.executeSql(sql);
        int id = 0;
        if (rs.next()) {
            id = rs.getInt("id");
        }

        // 获取岗位所属部门
        int deptIdOrCode = hjt.getDeptIdOrCode();
        String deptId = "";
        if (deptIdOrCode == 0) {
            deptId = Util.null2String(hjt.getJobdepartmentid());
        } else if (deptIdOrCode == 1) {
            sql = "select id from HrmDepartment where departmentcode='" + hjt.getJobdepartmentCode() + "'";
            rs.executeSql(sql);
            if (rs.next()) {
                deptId = Util.null2String(rs.getString("id"));
            }
        }
        if ("".equals(deptId) || String.valueOf(id).equals(deptId)) deptId = "0";

        // 所属职位的模板的ID   HrmJobGroups
        String jobGroups = hjt.getJobGroupName();
        sql = "select count(*) as ct from HrmJobGroups where jobgroupname='" + jobGroups + "'";
        rs.executeSql(sql);
        int flag_1 = 0;
        if (rs.next()) {
            flag_1 = rs.getInt("ct");
        }
        if (flag_1 < 1) {
            sql = "insert into HrmJobGroups(jobgroupname,jobgroupremark) values('" + jobGroups + "','" + jobGroups + "')";
            rs.executeSql(sql);
        }
        sql = "select id from HrmJobGroups where jobgroupname='" + jobGroups + "'";
        rs.executeSql(sql);
        String groupID = "";
        if (rs.next()) {
            groupID = Util.null2String(rs.getString("id"));
        }
        if ("".equals(groupID)) groupID = "11";    // 11为待定

        // 所属职位的ID   HrmJobActivities
        String jobAct = hjt.getJobactivityName();
        sql = "select count(*) as ct from HrmJobActivities where jobactivityname='" + jobAct + "'";
        rs.executeSql(sql);
        flag_1 = 0;
        if (rs.next()) {
            flag_1 = rs.getInt("ct");
        }
        if (flag_1 < 1) {
            sql = "insert into HrmJobActivities(jobactivityname,jobactivitymark,jobgroupid) values('"
                    + jobAct + "','" + jobAct + "'," + groupID + ")";
            rs.executeSql(sql);
        }
        sql = "select id from HrmJobActivities where jobactivityname='" + jobAct + "'";
        rs.executeSql(sql);
        String jobActID = "";
        if (rs.next()) {
            jobActID = Util.null2String(rs.getString("id"));
        }
        if ("".equals(jobActID)) jobActID = "14";   // 14 为待定

        // select jobactivityid,jobtitlename,jobtitlemark,jobtitlecode,jobdepartmentid,outkey from HrmJobTitles
        if (id < 1) {
            Map<String, String> mapStr = new HashMap<String, String>();
            mapStr.put("jobtitlecode", jobtitlecode);
            mapStr.put("jobtitlename", hjt.getJobtitlename());
            mapStr.put("jobtitleremark", hjt.getJobtitleremark());
            mapStr.put("jobtitlemark", hjt.getJobtitlemark());
            mapStr.put("jobactivityid", jobActID);
            mapStr.put("jobdepartmentid", deptId);
            mapStr.put("outkey", hjt.getSuperJobCode());

            boolean isRun = tdu.insert("HrmJobTitles", mapStr);
            if (!isRun) {
                ri.setMessage(false, "1201", "岗位信息 插入操作错误！");
                return ri;
            }

            rs.executeSql("select id from HrmJobTitles where jobtitlecode='" + jobtitlecode + "' ");
            if (rs.next()) {
                id = rs.getInt("id");
            }
            if (id < 1) {
                ri.setMessage(false, "1203", "岗位信息 插入后 无记录错误！");
                return ri;
            }

        } else {// 更新岗位信息
            // 不需要更新的值 记录下来。
            List<String> notList = new ArrayList<String>();
            List<String> list = hjt.getNotUpdate();
            if (list.size() > 0) {
                sql = "select * from HrmJobTitles where id=" + id;
                rs.executeSql(sql);
                if (rs.next()) {
                    for (String tmp_f : list) {
                        String tmp_f_val = Util.null2String(rs.getString(tmp_f));
                        if (tmp_f_val.length() > 0) {
                            notList.add(tmp_f);
                        }
                    }

                }
            }

            Map<String, String> whereMap = new HashMap<String, String>();
            whereMap.put("id", "" + id);
            Map<String, String> mapStr = new HashMap<String, String>();

            if (!notList.contains("jobtitlename".toUpperCase())) {
                mapStr.put("jobtitlename", hjt.getJobtitlename());
            }
            if (!notList.contains("jobtitleremark".toUpperCase())) {
                mapStr.put("jobtitleremark", hjt.getJobtitleremark());
            }
            if (!notList.contains("jobtitlemark".toUpperCase())) {
                mapStr.put("jobtitlemark", hjt.getJobtitlemark());
            }
            if (!notList.contains("jobactivityid".toUpperCase())) {
                mapStr.put("jobactivityid", jobActID);
            }
            if (!notList.contains("jobdepartmentid".toUpperCase())) {
                mapStr.put("jobdepartmentid", deptId);
            }
            if (!notList.contains("outkey".toUpperCase())) {
                mapStr.put("outkey", hjt.getSuperJobCode());
            }

            boolean isRun = tdu.update("HrmJobTitles", mapStr, whereMap);
            if (!isRun) {
                ri.setMessage(false, "1202", "岗位信息 更新错误！");
                return ri;
            }
        }

        try {
            JobTitlesTempletComInfo JobTitlesTempletComInfo = new JobTitlesTempletComInfo();
            JobTitlesTempletComInfo.removeJobTitlesTempletCache();

            JobTitlesComInfo JobTitlesComInfo = new JobTitlesComInfo();
            JobTitlesComInfo.removeJobTitlesCache();
        } catch (Exception e) {
            e.printStackTrace();
            log.writeLog(e.getMessage());
        }

        return ri;
    }

    /*
     *   人员操作
     *   参数   hrb 人员对象参数
     *   原则：  oracle 或 Sqlserver都可以执行的SQL
     */
    public ReturnInfo operResource(HrmResourceBean hrb) {
        TmcDBUtil tdu = new TmcDBUtil();
        BaseBean log = new BaseBean();
        ReturnInfo ri = new ReturnInfo();
        String workcode = hrb.getWorkcode();
        RecordSet rs = new RecordSet();
        String sql = "";
        sql = "select id from hrmresource where workcode='" + workcode + "' ";
        rs.executeSql(sql);
        int id = 0;
        if (rs.next()) {
            id = rs.getInt("id");
        }

        // 岗位ID
        int jobFlag = hrb.getJobIdOrCode();
        String jobTitleID = "";
        if (jobFlag == 0) {
            jobTitleID = Util.null2String(hrb.getJobtitle());
        } else if (jobFlag == 1) {
            sql = "select id from HrmJobTitles where jobtitlecode='" + hrb.getJobtitleCode() + "'";
            rs.executeSql(sql);
            if (rs.next()) {
                jobTitleID = Util.null2String(rs.getString("id"));
            }
        }
        if ("".equals(jobTitleID)) {
            ri.setMessage(false, "2200", "人员的岗位不存在!");
            return ri;
        }

        // 获取部门ID
        int deptFlag = hrb.getDeptIdOrCode();
        String deptID = "";
        if (deptFlag == 0) {
            deptID = Util.null2String(hrb.getDepartmentid());
        } else if (deptFlag == 1) {
            sql = "select id from HrmDepartment where departmentcode='" + hrb.getDepartmentCode() + "'";
            rs.executeSql(sql);
            if (rs.next()) {
                deptID = Util.null2String(rs.getString("id"));
            }
        }
        if ("".equals(deptID)) {
            ri.setMessage(false, "2201", "人员的部门不存在!");
            return ri;
        }

        String comID = "";
        sql = "select subcompanyid1 from HrmDepartment where id=" + deptID;
        rs.executeSql(sql);
        if (rs.next()) {
            comID = Util.null2String(rs.getString("subcompanyid1"));
        }

        if ("".equals(comID)) {
            ri.setMessage(false, "2202", "分部的部门不存在!");
            return ri;
        }

        // 获取人员上级
        int managerFlag = hrb.getManagerIdOrCode();
        String managerID = "";
        if (managerFlag == 0) {
            managerID = Util.null2String(hrb.getManagerid());
        } else if (managerFlag == 1) {
            sql = "select id from hrmresource where workcode='" + hrb.getWorkcode() + "'";
            rs.executeSql(sql);
            if (rs.next()) {
                managerID = Util.null2String(rs.getString("id"));
            }
        } else if (managerFlag == 2) {
            // 待通过岗位处理
            managerID = "@@";
        }
        if ("".equals(managerID)) {
            ri.setRemark(ri.getRemark() + ";直接上级不存在！");
        }

        // 获取次账号所属主账号
        int belongFlag = hrb.getBelongIdOrCode();
        String belongID = "";
        if (belongFlag >= 0) {
            if (belongFlag == 0) {
                belongID = Util.null2String(hrb.getBelongto());
            } else if (belongFlag == 1) {
                sql = "select id from hrmresource where workcode='" + hrb.getBelongtoCode() + "'";
                rs.executeSql(sql);
                if (rs.next()) {
                    belongID = Util.null2String(rs.getString("id"));
                }
            }

            if (String.valueOf(id).equals(belongID)) belongID = "";

            if ("".equals(belongID)) {
                ri.setRemark(ri.getRemark() + ";次账号归属主账号不存在！");
            }
        }

        // 岗位信息判断上级
        if ("@@".equals(managerID)) {
            String tmp_1 = "";
            sql = "select outkey from HrmJobTitles where id=" + jobTitleID;
            rs.executeSql(sql);
            if (rs.next()) {
                tmp_1 = Util.null2String(rs.getString("outkey"));
            }
            //	log.writeLog("tmp_1 = " + tmp_1);
            if (!"".equals(tmp_1)) {
                managerID = "";
                int tmp_managerID = 0;
                if (id > 0) {
                    sql = "select managerid from hrmresource where id=" + id;
                    rs.executeSql(sql);
                    if (rs.next()) {
                        tmp_managerID = rs.getInt("managerid");
                    }
                }
                int tmp_2_x = 0;
                // 跑2边 肯定都是有值的。
                while ("".equals(managerID)) {
                    // 查询处理上级   如果已经存在看里面是否存在，如果存在就还是之前的；如果不存在,就需要
                    sql = "select h.id,jt.outkey from hrmresource h join HrmJobTitles jt on h.jobtitle=jt.id "
                            + "  where jt.jobtitlecode='" + tmp_1 + "' and h.status in(0,1,2,3,4) order by h.id ";
                    rs.executeSql(sql);
                    while (rs.next()) {
                        int ss_xx = rs.getInt("id");
                        if (tmp_2_x < 1) tmp_2_x = ss_xx;
                        if (tmp_managerID == ss_xx) {
                            managerID = String.valueOf(tmp_managerID);
                            break;
                        }
                        tmp_1 = Util.null2String(rs.getString("outkey"));

                        //		log.writeLog("ss_xx = " + ss_xx + " ; tmp_2_x = " + tmp_2_x + " ; tmp_managerID = " + tmp_managerID);
                    }

                    if ("".equals(managerID)) {
                        if (tmp_2_x > 0) managerID = String.valueOf(tmp_2_x);
                        break;
                    }
                    if ("".equals(tmp_1)) break;
                }
            } else {
                managerID = "";
            }
        }
        if (String.valueOf(id).equals(managerID)) managerID = "";

        if ("".equals(managerID)) {
            ri.setRemark(ri.getRemark() + ";直接上级不存在！");
        }

        if (id < 1) {
            Map<String, String> mapStr = new HashMap<String, String>();
            int ss_yy = hrb.getAccounttype();
            if (ss_yy == 0) {
                mapStr.put("loginid", hrb.getLoginid());
            }
            mapStr.put("workcode", workcode);
            mapStr.put("loginid", hrb.getLoginid());
            mapStr.put("status", hrb.getStatus());
            mapStr.put("lastname", hrb.getLastname());
            mapStr.put("sex", hrb.getSexNew());
            mapStr.put("birthday", hrb.getBirthday());
            mapStr.put("seclevel", "" + hrb.getSeclevel());
            mapStr.put("jobtitle", jobTitleID);
            mapStr.put("departmentid", deptID);
            mapStr.put("subcompanyid1", comID);

            mapStr.put("managerid", managerID);
            mapStr.put("nationality", hrb.getNationalityNew());
            mapStr.put("systemlanguage", hrb.getSystemlanguage());
            mapStr.put("password", hrb.getPasswordNew());
            mapStr.put("maritalstatus", hrb.getMaritalstatus());
            mapStr.put("telephone", hrb.getTelephone());
            mapStr.put("mobile", hrb.getMobile());
            mapStr.put("mobilecall", hrb.getMobilecall());
            mapStr.put("email", hrb.getEmail());
            mapStr.put("dsporder", "" + hrb.getDsporder());
            mapStr.put("createrid", hrb.getCreaterid());
            mapStr.put("createdate", hrb.getCreatedate());
            mapStr.put("accounttype", "" + hrb.getAccounttype());
            mapStr.put("belongto", belongID);
            mapStr.put("locationid", hrb.getLocationidNew());
            mapStr.put("workroom", hrb.getWorkroom());
            mapStr.put("homeaddress", hrb.getHomeaddress());
            mapStr.put("startdate", hrb.getStartdate());
            mapStr.put("enddate", hrb.getEnddate());
            mapStr.put("datefield1", hrb.getDatefield1());
            mapStr.put("datefield2", hrb.getDatefield2());
            mapStr.put("datefield3", hrb.getDatefield3());
            mapStr.put("datefield4", hrb.getDatefield4());
            mapStr.put("datefield5", hrb.getDatefield5());
            mapStr.put("numberfield1", hrb.getNumberfield1());
            mapStr.put("numberfield2", hrb.getNumberfield2());
            mapStr.put("numberfield3", hrb.getNumberfield3());
            mapStr.put("numberfield4", hrb.getNumberfield4());
            mapStr.put("numberfield5", hrb.getNumberfield5());
            mapStr.put("textfield1", hrb.getTextfield1());
            mapStr.put("textfield2", hrb.getTextfield2());
            mapStr.put("textfield3", hrb.getTextfield3());
            mapStr.put("textfield4", hrb.getTextfield4());
            mapStr.put("textfield5", hrb.getTextfield5());
            mapStr.put("tinyintfield1", hrb.getTinyintfield1());
            mapStr.put("tinyintfield2", hrb.getTinyintfield2());
            mapStr.put("tinyintfield3", hrb.getTinyintfield3());
            mapStr.put("tinyintfield4", hrb.getTinyintfield4());
            mapStr.put("tinyintfield5", hrb.getTinyintfield5());
            mapStr.put("jobactivitydesc", hrb.getJobactivitydesc());
            mapStr.put("certificatenum", hrb.getCertificatenum());
            mapStr.put("nativeplace", hrb.getNativeplace());
            mapStr.put("educationlevel", hrb.getEducationlevelNew());
            mapStr.put("regresidentplace", hrb.getRegresidentplace());
            mapStr.put("healthinfo", hrb.getHealthinfoNew());
            mapStr.put("policy", hrb.getPolicy());
            mapStr.put("degree", hrb.getDegree());
            mapStr.put("height", hrb.getHeight());
            mapStr.put("jobcall", hrb.getJobcallNew());
            mapStr.put("accumfundaccount", hrb.getAccumfundaccount());
            mapStr.put("birthplace", hrb.getBirthday());
            mapStr.put("folk", hrb.getFolk());
            mapStr.put("extphone", hrb.getExtphone());
            mapStr.put("fax", hrb.getFax());
            mapStr.put("weight", hrb.getWeight());
            mapStr.put("tempresidentnumber", hrb.getTempresidentnumber());
            mapStr.put("probationenddate", hrb.getProbationenddate());
            mapStr.put("bankid1", hrb.getBankid1());
            mapStr.put("accountid1", hrb.getAccountid1());

            int s = 0;
            sql = "select max(id) as maxid from hrmresource";
            rs.executeSql(sql);
            if (rs.next()) {
                s = rs.getInt("maxid");
            }
            if (s < 2) s = 2;
            else s = s + 1;

            mapStr.put("id", String.valueOf(s));

            boolean isRun = tdu.insert("hrmresource", mapStr);
            if (!isRun) {
                ri.setMessage(false, "2220", "人员信息 插入操作错误！");
                return ri;
            }

            rs.executeSql("select id from hrmresource where workcode='" + workcode + "' ");
            if (rs.next()) {
                id = rs.getInt("id");
            }
            if (id < 1) {
                ri.setMessage(false, "2221", "人员信息 插入后 无记录错误！");
                return ri;
            }

        } else {// 更新人员信息
            // 不需要更新的值 记录下来。
            List<String> notList = new ArrayList<String>();
            List<String> list = hrb.getNotUpdate();
            if (list.size() > 0) {
                sql = "select * from hrmresource where id=" + id;
                rs.executeSql(sql);
                if (rs.next()) {
                    for (String tmp_f : list) {
                        String tmp_f_val = Util.null2String(rs.getString(tmp_f));
                        if (tmp_f_val.length() > 0) {
                            notList.add(tmp_f);
                        }
                    }

                }
            }
            Map<String, String> whereMap = new HashMap<String, String>();
            whereMap.put("id", "" + id);
            Map<String, String> mapStr = new HashMap<String, String>();
            int ss_yy = hrb.getAccounttype();
            if (ss_yy == 0) {
                if (!notList.contains("loginid".toUpperCase())) {
                    mapStr.put("loginid", hrb.getLoginid());
                }
            }
            if (!notList.contains("status".toUpperCase())) {
                mapStr.put("status", hrb.getStatus());
            }
            if (!notList.contains("lastname".toUpperCase())) {
                mapStr.put("lastname", hrb.getLastname());
            }
            if (!notList.contains("sex".toUpperCase())) {
                mapStr.put("sex", hrb.getSexNew());
            }
            if (!notList.contains("birthday".toUpperCase())) {
                mapStr.put("birthday", hrb.getBirthday());
            }
            if (!notList.contains("seclevel".toUpperCase())) {
                mapStr.put("seclevel", "" + hrb.getSeclevel());
            }
            if (!notList.contains("jobtitle".toUpperCase())) {
                mapStr.put("jobtitle", jobTitleID);
            }
            if (!notList.contains("departmentid".toUpperCase())) {
                mapStr.put("departmentid", deptID);
            }
            if (!notList.contains("subcompanyid1".toUpperCase())) {
                mapStr.put("subcompanyid1", comID);
            }
            if (!notList.contains("managerid".toUpperCase())) {
                mapStr.put("managerid", managerID);
            }
            if (!notList.contains("nationality".toUpperCase())) {
                mapStr.put("nationality", hrb.getNationalityNew());
            }
            if (!notList.contains("systemlanguage".toUpperCase())) {
                mapStr.put("systemlanguage", hrb.getSystemlanguage());
            }
            if (!notList.contains("password".toUpperCase())) {
                mapStr.put("password", hrb.getPasswordNew());
            }
            if (!notList.contains("maritalstatus".toUpperCase())) {
                mapStr.put("maritalstatus", hrb.getMaritalstatus());
            }
            if (!notList.contains("telephone".toUpperCase())) {
                mapStr.put("telephone", hrb.getTelephone());
            }
            if (!notList.contains("mobile".toUpperCase())) {
                mapStr.put("mobile", hrb.getMobile());
            }
            if (!notList.contains("mobilecall".toUpperCase())) {
                mapStr.put("mobilecall", hrb.getMobilecall());
            }
            if (!notList.contains("email".toUpperCase())) {
                mapStr.put("email", hrb.getEmail());
            }
            if (!notList.contains("dsporder".toUpperCase())) {
                mapStr.put("dsporder", "" + hrb.getDsporder());
            }
            if (!notList.contains("createrid".toUpperCase())) {
                mapStr.put("createrid", hrb.getCreaterid());
            }
            if (!notList.contains("createdate".toUpperCase())) {
                mapStr.put("createdate", hrb.getCreatedate());
            }
            if (!notList.contains("accounttype".toUpperCase())) {
                mapStr.put("accounttype", "" + hrb.getAccounttype());
            }
            if (!notList.contains("belongto".toUpperCase())) {
                mapStr.put("belongto", belongID);
            }
            if (!notList.contains("locationid".toUpperCase())) {
                mapStr.put("locationid", hrb.getLocationidNew());
            }
            if (!notList.contains("workroom".toUpperCase())) {
                mapStr.put("workroom", hrb.getWorkroom());
            }
            if (!notList.contains("homeaddress".toUpperCase())) {
                mapStr.put("homeaddress", hrb.getHomeaddress());
            }
            if (!notList.contains("startdate".toUpperCase())) {
                mapStr.put("startdate", hrb.getStartdate());
            }
            if (!notList.contains("enddate".toUpperCase())) {
                mapStr.put("enddate", hrb.getEnddate());
            }
            if (!notList.contains("datefield1".toUpperCase())) {
                mapStr.put("datefield1", hrb.getDatefield1());
            }
            if (!notList.contains("datefield2".toUpperCase())) {
                mapStr.put("datefield2", hrb.getDatefield2());
            }
            if (!notList.contains("datefield3".toUpperCase())) {
                mapStr.put("datefield3", hrb.getDatefield3());
            }
            if (!notList.contains("datefield4".toUpperCase())) {
                mapStr.put("datefield4", hrb.getDatefield4());
            }
            if (!notList.contains("datefield5".toUpperCase())) {
                mapStr.put("datefield5", hrb.getDatefield5());
            }
            if (!notList.contains("numberfield1".toUpperCase())) {
                mapStr.put("numberfield1", hrb.getNumberfield1());
            }
            if (!notList.contains("numberfield2".toUpperCase())) {
                mapStr.put("numberfield2", hrb.getNumberfield2());
            }
            if (!notList.contains("numberfield3".toUpperCase())) {
                mapStr.put("numberfield3", hrb.getNumberfield3());
            }
            if (!notList.contains("numberfield4".toUpperCase())) {
                mapStr.put("numberfield4", hrb.getNumberfield4());
            }
            if (!notList.contains("numberfield5".toUpperCase())) {
                mapStr.put("numberfield5", hrb.getNumberfield5());
            }
            if (!notList.contains("textfield1".toUpperCase())) {
                mapStr.put("textfield1", hrb.getTextfield1());
            }
            if (!notList.contains("textfield2".toUpperCase())) {
                mapStr.put("textfield2", hrb.getTextfield2());
            }
            if (!notList.contains("textfield3".toUpperCase())) {
                mapStr.put("textfield3", hrb.getTextfield3());
            }
            if (!notList.contains("textfield4".toUpperCase())) {
                mapStr.put("textfield4", hrb.getTextfield4());
            }
            if (!notList.contains("textfield5".toUpperCase())) {
                mapStr.put("textfield5", hrb.getTextfield5());
            }
            if (!notList.contains("tinyintfield1".toUpperCase())) {
                mapStr.put("tinyintfield1", hrb.getTinyintfield1());
            }
            if (!notList.contains("tinyintfield2".toUpperCase())) {
                mapStr.put("tinyintfield2", hrb.getTinyintfield2());
            }
            if (!notList.contains("tinyintfield3".toUpperCase())) {
                mapStr.put("tinyintfield3", hrb.getTinyintfield3());
            }
            if (!notList.contains("tinyintfield4".toUpperCase())) {
                mapStr.put("tinyintfield4", hrb.getTinyintfield4());
            }
            if (!notList.contains("tinyintfield5".toUpperCase())) {
                mapStr.put("tinyintfield5", hrb.getTinyintfield5());
            }
            if (!notList.contains("jobactivitydesc".toUpperCase())) {
                mapStr.put("jobactivitydesc", hrb.getJobactivitydesc());
            }
            if (!notList.contains("certificatenum".toUpperCase())) {
                mapStr.put("certificatenum", hrb.getCertificatenum());
            }
            if (!notList.contains("nativeplace".toUpperCase())) {
                mapStr.put("nativeplace", hrb.getNativeplace());
            }
            if (!notList.contains("educationlevel".toUpperCase())) {
                mapStr.put("educationlevel", hrb.getEducationlevelNew());
            }
            if (!notList.contains("regresidentplace".toUpperCase())) {
                mapStr.put("regresidentplace", hrb.getRegresidentplace());
            }
            if (!notList.contains("healthinfo".toUpperCase())) {
                mapStr.put("healthinfo", hrb.getHealthinfoNew());
            }
            if (!notList.contains("policy".toUpperCase())) {
                mapStr.put("policy", hrb.getPolicy());
            }
            if (!notList.contains("degree".toUpperCase())) {
                mapStr.put("degree", hrb.getDegree());
            }
            if (!notList.contains("height".toUpperCase())) {
                mapStr.put("height", hrb.getHeight());
            }
            if (!notList.contains("jobcall".toUpperCase())) {
                mapStr.put("jobcall", hrb.getJobcallNew());
            }
            if (!notList.contains("accumfundaccount".toUpperCase())) {
                mapStr.put("accumfundaccount", hrb.getAccumfundaccount());
            }
            if (!notList.contains("birthplace".toUpperCase())) {
                mapStr.put("birthplace", hrb.getBirthday());
            }
            if (!notList.contains("folk".toUpperCase())) {
                mapStr.put("folk", hrb.getFolk());
            }
            if (!notList.contains("extphone".toUpperCase())) {
                mapStr.put("extphone", hrb.getExtphone());
            }
            if (!notList.contains("fax".toUpperCase())) {
                mapStr.put("fax", hrb.getFax());
            }
            if (!notList.contains("weight".toUpperCase())) {
                mapStr.put("weight", hrb.getWeight());
            }
            if (!notList.contains("tempresidentnumber".toUpperCase())) {
                mapStr.put("tempresidentnumber", hrb.getTempresidentnumber());
            }
            if (!notList.contains("probationenddate".toUpperCase())) {
                mapStr.put("probationenddate", hrb.getProbationenddate());
            }
            if (!notList.contains("bankid1".toUpperCase())) {
                mapStr.put("bankid1", hrb.getBankid1());
            }
            if (!notList.contains("accountid1".toUpperCase())) {
                mapStr.put("accountid1", hrb.getAccountid1());
            }

            boolean isRun = tdu.update("hrmresource", mapStr, whereMap);
            if (!isRun) {
                ri.setMessage(false, "2222", "人员信息 更新错误！");
                return ri;
            }
        }

        int currentid = 0;
        // 处理系统最大需要问题
        sql = "select indexdesc,currentid from SequenceIndex where indexdesc='resourceid'";
        rs.executeSql(sql);
        if (rs.next()) {
            currentid = rs.getInt("currentid");
        }
        if (currentid != id) {
            rs.executeSql("update SequenceIndex set currentid=" + id + " where indexdesc='resourceid'");
        }

        // 自定义信息处理
        String tableName = "cus_fielddata";
        Map<String, String> updateMap = hrb.getCusMap();
        Map<String, String> mainMap = new HashMap<String, String>();
        mainMap.put("id", "" + id);
        mainMap.put("scope", "HrmCustomFieldByInfoType");
        mainMap.put("scopeid", "-1");
        String message = updateCustom(tableName, mainMap, updateMap);
        ri.setRemark(message);

        try {
            ResourceComInfo ResourceComInfo = new ResourceComInfo();
            ResourceComInfo.addResourceInfoCache("" + id);
        } catch (Exception e) {
            e.printStackTrace();
            log.writeLog(e.getMessage());
        }

        return ri;
    }

    // 更新自定义表    tableName:自定义表      mainMap:判断字段    updateMap：需要更新的自定义字段
    private String updateCustom(String tableName, Map<String, String> mainMap, Map<String, String> updateMap) {
        BaseBean log = new BaseBean();
        log.writeLog("updateCustom(Start) : " + tableName);
        // 无明细字段
        if (updateMap == null || mainMap.size() < 1)
            return "";
        if (mainMap == null || mainMap.size() < 1)
            return "主表判断字段为空";

        String message = "";

        // 判断字段拼接
        String where = " where 1=1 ";
        // 组合内容拼接
        String sum_key = "";
        String sum_val = "";
        String flme = "";
        Iterator<String> it = mainMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = mainMap.get(key);

            where = where + " and " + key + "='" + value + "'";
            sum_key = sum_key + flme + key;
            sum_val = sum_val + flme + "'" + value + "'";
            flme = ",";
        }

        RecordSet rs = new RecordSet();
        // 判断值是否存在，不存在话插入
        String sql = "select count(*) as ct from " + tableName + where;
        rs.executeSql(sql);
        int flag = 0;
        if (rs.next()) {
            flag = rs.getInt("ct");
        }
        if (flag == 0) {
            sql = "insert into " + tableName + "(" + sum_key + ") values(" + sum_val + ")";
            rs.executeSql(sql);
            log.writeLog("updateCustom(sql1) : " + sql);
        }

        // 所有字段更新
        Iterator<String> itx = updateMap.keySet().iterator();
        while (itx.hasNext()) {
            String key = itx.next();
            String value = updateMap.get(key);

            sql = "update " + tableName + " set " + key + "='" + value + "' " + where;
            log.writeLog("updateCustom(sql2) : " + sql);
            boolean isRun = rs.executeSql(sql);
            if (!isRun) {
                message = message + ";字段:" + key + ",值为:" + value + " 信息更新失败";
            }
        }

        return message;
    }

    public HrmResourceBean getHrmByID(String hrmID) {
        HrmResourceBean hrb = new HrmResourceBean();
        String sql = "select * from hrmresource where id=" + hrmID;
        RecordSet rs = new RecordSet();
        rs.executeSql(sql);
        while (rs.next()) {
            String tmp_name = Util.null2String(rs.getString("lastname"));
            String tmp_loginid = Util.null2String(rs.getString("loginid"));
            String tmp_ = Util.null2String(rs.getString("loginid"));
        }

        return null;
    }
}
