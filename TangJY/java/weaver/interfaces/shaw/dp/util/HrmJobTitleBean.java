package weaver.interfaces.shaw.dp.util;

import java.util.ArrayList;
import java.util.List;

import weaver.general.Util;

public class HrmJobTitleBean {

	// 岗位编码
	private String jobtitlecode;
	// 岗位全称
	private String jobtitlemark;
	// 岗位简称
	private String jobtitlename;
	// 岗位描述
	private String jobtitleremark;
	// 职位描述   中文判断 无编码
	private String jobactivityName;
	// 职位组   描述判断 无编码
	private String jobGroupName;
	// 岗位所属部门  0通过ID  1是通过编码
	private int deptIdOrCode;
	// 岗位所属部门ID
	private String jobdepartmentid;
	// 岗位所属部门Code
	private String jobdepartmentCode;
	// 上级岗位Code
	private String superJobCode;
	// 如果系统存在这个值,就不更新组合
	private List<String> notUpdate;
	
	// 如果系统存在这个值,就不更新组合
	public List<String> getNotUpdate(){
		if(notUpdate == null) {
			notUpdate = new ArrayList<String>();
		}
		return  notUpdate;
	}
	// 如果系统存在这个值,就不更新组合
	public void addNotUpdate(String info){
		if(info == null || info.length() < 1) return;
		if(notUpdate == null) {
			notUpdate = new ArrayList<String>();
		}
		notUpdate.add(info.toUpperCase());
	}
	

	public String getJobactivityName() {
		return Util.null2String(jobactivityName);
	}

	public void setJobactivityName(String jobactivityName) {
		this.jobactivityName = jobactivityName;
	}

	public String getJobGroupName() {
		return Util.null2String(jobGroupName);
	}



	public void setJobGroupName(String jobGroupName) {
		this.jobGroupName = jobGroupName;
	}

	public String getSuperJobCode() {
		return Util.null2String(superJobCode);
	}

	public void setSuperJobCode(String superJobCode) {
		this.superJobCode = superJobCode;
	}
	
	public String getJobtitlecode() {
		return Util.null2String(jobtitlecode);
	}

	public void setJobtitlecode(String jobtitlecode) {
		this.jobtitlecode = jobtitlecode;
	}

	public String getJobtitlemark() {
		return Util.null2String(jobtitlemark);
	}

	public void setJobtitlemark(String jobtitlemark) {
		this.jobtitlemark = jobtitlemark;
	}

	public String getJobtitlename() {
		return Util.null2String(jobtitlename);
	}

	public void setJobtitlename(String jobtitlename) {
		this.jobtitlename = jobtitlename;
	}

	public String getJobtitleremark() {
		return Util.null2String(jobtitleremark);
	}

	public void setJobtitleremark(String jobtitleremark) {
		this.jobtitleremark = jobtitleremark;
	}

	public int getDeptIdOrCode() {
		return deptIdOrCode;
	}

	public void setDeptIdOrCode(int deptIdOrCode) {
		this.deptIdOrCode = deptIdOrCode;
	}

	public String getJobdepartmentid() {
		return Util.null2String(jobdepartmentid);
	}

	public void setJobdepartmentid(String jobdepartmentid) {
		this.jobdepartmentid = jobdepartmentid;
	}

	public String getJobdepartmentCode() {
		return Util.null2String(jobdepartmentCode);
	}

	public void setJobdepartmentCode(String jobdepartmentCode) {
		this.jobdepartmentCode = jobdepartmentCode;
	}
}
