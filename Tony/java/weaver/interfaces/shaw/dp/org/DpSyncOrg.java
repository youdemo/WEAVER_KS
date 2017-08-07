package weaver.interfaces.shaw.dp.org;

import weaver.conn.RecordSetDataSource;
import weaver.general.Util;
import weaver.interfaces.shaw.dp.util.*;

/**
 * Created by adore on 2017/4/21.
 * 德鹏组织结构同步
 */
public class DpSyncOrg {
    public String syncOrg() {
        // 执行类
        HrmOrgAction hoa = new HrmOrgAction();
        RecordSetDataSource rsds = new RecordSetDataSource("HR");
        String org_result = "";
        String sql = "";
        sql = " select companycode,companyname,companydesc,supercode,subcompanyname,showorder,createtime from DP_company ";
        rsds.executeSql(sql);
        while (rsds.next()) {
            String companycode = Util.null2String(rsds.getString("companycode"));
            String companyname = Util.null2String(rsds.getString("companyname"));
            String companydesc = Util.null2String(rsds.getString("companydesc"));
            String supercode = Util.null2String(rsds.getString("supercode"));
            //String subcompanyname = Util.null2String(rsds.getString("subcompanyname"));
            int showorder = rsds.getInt("showorder");
            String createtime = Util.null2String(rsds.getString("createtime"));
            /**
             单个分部同步
             */
            // 分部类
            HrmSubCompanyBean hsb = new HrmSubCompanyBean();
            hsb.setSubCompanyCode(companycode);
            hsb.setSubCompanyName(companyname);
            hsb.setSubCompanyDesc(companyname);
            // 上级的操作方式     0 是通过id获取  1是通过code获取
            hsb.setIdOrCode(1);
            hsb.setSuperID("");
            hsb.setSuperCode(supercode);
            //排序字段
            hsb.setOrderBy(showorder);
            // 状态    0正常  1封装
            hsb.setStatus(0);
            // 自定义内容    例如:tt1、tt2是自定义的字段名     TEst1、TEst2 是自定义字段的值       HrmSubcompanyDefined记录
            //hsb.addCusMap("tt1", "TEst1");
            //hsb.addCusMap("tt2", "TEst2");
            // 执行结果  可以直接打印result 查看直接结果
            ReturnInfo result = hoa.operSubCompany(hsb);

            org_result += result.getRemark();

            //
            if (result.isTure()) {
                System.out.println("执行成功！警告内容：" + result.getRemark());
            } else {
                System.out.println("执行失败！失败详细：" + result.getRemark());
            }
        }

        sql = " select departmentcode,departmentname,departmentmark,supdepcode,companycode,showorder,createtime from DP_Department ";
        rsds.executeSql(sql);
        while (rsds.next()) {
            String departmentcode = Util.null2String(rsds.getString("departmentcode"));
            String departmentname = Util.null2String(rsds.getString("departmentname"));
            String departmentmark = Util.null2String(rsds.getString("departmentmark"));
            String supdepcode = Util.null2String(rsds.getString("supdepcode"));
            String companycode = Util.null2String(rsds.getString("companycode"));
            int showorder = rsds.getInt("showorder");
            String createtime = Util.null2String(rsds.getString("createtime"));
            /**
             单个部门同步
             */
            // 部门类
            HrmDepartmentBean hdb = new HrmDepartmentBean();
            hdb.setDepartmentcode(departmentcode);
            hdb.setDepartmentname(departmentname);
            hdb.setDepartmentark(departmentmark);
            // 分部的获取操作方式     0 是通过id获取  1是通过code获取
            hdb.setComIdOrCode(1);
            hdb.setSubcompanyid1("");
            hdb.setSubcompanyCode(companycode);
            // 上级的操作方式     0 是通过id获取  1是通过code获取
            hdb.setIdOrCode(1);
            hdb.setSuperID("");
            hdb.setSuperCode(supdepcode);
            //排序字段
            hdb.setOrderBy(showorder);
            // 状态    0正常  1封装
            hdb.setStatus(0);
            // 自定义内容    例如:t1、t2是自定义的字段名     123、456 是自定义字段的值       HrmDepartmentDefined记录
            //hdb.addCusMap("t1", "123");
            //hdb.addCusMap("t2", "456");
            // 执行结果  可以直接打印result 查看直接结果
            ReturnInfo result = hoa.operDept(hdb);

            org_result += result.getRemark();

            if (result.isTure()) {
                System.out.println("执行成功！警告内容：" + result.getRemark());
            } else {
                System.out.println("执行失败！失败详细：" + result.getRemark());
            }
        }
        sql = " select jobtitlecode,jobtitlename,jobtitlemark,jobActivities,createtime from DP_JobTitles2 ";
        rsds.executeSql(sql);
        while (rsds.next()) {
            String jobtitlecode = Util.null2String(rsds.getString("jobtitlecode"));
            String jobtitlename = Util.null2String(rsds.getString("jobtitlename"));
            String jobtitlemark = Util.null2String(rsds.getString("jobtitlemark"));
            String jobActivities = Util.null2String(rsds.getString("jobActivities"));
            String createtime = Util.null2String(rsds.getString("createtime"));
            /**
             单个岗位同步
             */
            // 岗位类
            HrmJobTitleBean hjt = new HrmJobTitleBean();
            hjt.setJobtitlecode(jobtitlecode);
            hjt.setJobtitlename(jobtitlename);
            hjt.setJobtitlemark(jobtitlemark);
            hjt.setJobtitleremark(jobtitlemark);
            // 所属部门  0 是通过id获取  1是通过code获取
            hjt.setDeptIdOrCode(1);
            hjt.setJobdepartmentid("");
            hjt.setJobdepartmentCode("");
            hjt.setSuperJobCode("");
            // 职位 直接通过字段去查询，没有就添加，有就直接获取
            hjt.setJobactivityName("TEST");
            // 职位 直接通过字段去查询，没有就添加，有就直接获取
            //hjt.setJobGroupName("TEST");
            // 执行结果  可以直接打印result 查看直接结果
            ReturnInfo result = hoa.operJobtitle(hjt);

            org_result += result.getRemark();

            if (result.isTure()) {
                System.out.println("执行成功！警告内容：" + result.getRemark());
            } else {
                System.out.println("执行失败！失败详细：" + result.getRemark());
            }
        }

        sql = " select workcode,name,loginid,mangercode,mobile,seclevel,telephone,email,certificatenum,sex,departmentcode,jobtitlecode"
                + " ,birthday,maritalstatus,nativeplace,educationlevel,status,extphone,residentpostcode,tempresidentnumber,fax,folk,location,workroom"
                + " ,createtime from DP_HrmResource ";
        rsds.executeSql(sql);
        while (rsds.next()) {
            String workcode = Util.null2String(rsds.getString("workcode"));
            String name = Util.null2String(rsds.getString("name"));
            String loginid = Util.null2String(rsds.getString("loginid"));
            String mobile = Util.null2String(rsds.getString("mobile"));
            int seclevel = rsds.getInt("seclevel");
            String mangercode = Util.null2String(rsds.getString("mangercode"));
            String telephone = Util.null2String(rsds.getString("telephone"));
            String email = Util.null2String(rsds.getString("email"));
            String certificatenum = Util.null2String(rsds.getString("certificatenum"));
            String sex = Util.null2String(rsds.getString("sex"));
            String departmentcode = Util.null2String(rsds.getString("departmentcode"));
            String jobtitlecode = Util.null2String(rsds.getString("jobtitlecode"));
            String birthday = Util.null2String(rsds.getString("birthday"));
            String maritalstatus = Util.null2String(rsds.getString("maritalstatus"));
            String nativeplace = Util.null2String(rsds.getString("nativeplace"));
            String educationlevel = Util.null2String(rsds.getString("educationlevel"));
            String status = Util.null2String(rsds.getString("status"));
            String extphone = Util.null2String(rsds.getString("extphone"));
            String residentpostcode = Util.null2String(rsds.getString("residentpostcode"));
            String tempresidentnumber = Util.null2String(rsds.getString("tempresidentnumber"));
            String fax = Util.null2String(rsds.getString("fax"));
            String folk = Util.null2String(rsds.getString("folk"));
            String location = Util.null2String(rsds.getString("location"));
            String workroom = Util.null2String(rsds.getString("workroom"));
            String createtime = Util.null2String(rsds.getString("createtime"));
            /**
             单个人员同步
             */
            if ("DP001892".equals(workcode)) {
                name = "adler";
                loginid = "adler";
            }

            if ("DP003296".equals(workcode)) {
                name = "Richard";
                loginid = "Richard";
            }

            if ("DP002022".equals(workcode)) {
                name = "tony";
                loginid = "tony";
            }

            if ("DP000004".equals(workcode)) {
                name = "tommy";
                loginid = "tommy";
            }
            // 人员信息类
            HrmResourceBean hrb = new HrmResourceBean();
            hrb.setWorkcode(workcode);
            hrb.setLoginid(loginid);
            hrb.setLastname(name);
            //hrb.setPassword("1234");
            // 所属分部   部门所对应的分部   省略
            // 所属部门  0 是通过id获取  1是通过code获取
            hrb.setDeptIdOrCode(1);
            hrb.setDepartmentid("");
            hrb.setDepartmentCode(departmentcode);
            // 所属岗位  0 是通过id获取  1是通过code获取
            hrb.setJobIdOrCode(1);
            hrb.setJobtitle("");
            hrb.setJobtitleCode(jobtitlecode);
            // 上级领导  0 是通过id获取  1是通过code获取      2是通过岗位获取
            //hrb.setManagerIdOrCode(1);
            //hrb.setManagerid("");
            //hrb.setManagerCode(mangercode);
            //hrb.setSeclevel(10);
            hrb.setBirthday(birthday);
            hrb.setCertificatenum(certificatenum);
            hrb.setMobile(mobile);
            hrb.setEducationlevel(educationlevel);
            hrb.setTelephone(telephone);
            hrb.setEmail(email);
            hrb.setSex(sex);
            hrb.setMaritalstatus(maritalstatus);
            hrb.setNationality(nativeplace);
            hrb.setStatus(status);
            hrb.setExtphone(extphone);
            hrb.setRegresidentplace(residentpostcode);
            hrb.setTempresidentnumber(tempresidentnumber);
            hrb.setFax(fax);
            hrb.setFolk(folk);
            hrb.setLocationid(location);
            hrb.setWorkroom(workroom);
            //不需更新的字段
            hrb.addNotUpdate("seclevel");
            hrb.addNotUpdate("managerid");
            hrb.addNotUpdate("password");
            hrb.addNotUpdate("mobile");
            hrb.addNotUpdate("telephone");
            hrb.addNotUpdate("email");

            /**
             *  当然可以下还有 都可以设置  在HrmResourceBean中，可以set值
             */

            // 执行结果  可以直接打印result 查看直接结果
            ReturnInfo result = hoa.operResource(hrb);
            org_result += result.getRemark();
            if (result.isTure()) {
                System.out.println("执行成功！警告内容：" + result.getRemark());
            } else {
                System.out.println("执行失败！失败详细：" + result.getRemark());
            }
        }
        return "执行成功！";
    }
}
