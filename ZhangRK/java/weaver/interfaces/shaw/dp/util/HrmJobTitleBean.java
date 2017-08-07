package weaver.interfaces.shaw.dp.util;

import java.util.ArrayList;
import java.util.List;

import weaver.general.Util;

public class HrmJobTitleBean {

	// ��λ����
	private String jobtitlecode;
	// ��λȫ��
	private String jobtitlemark;
	// ��λ���
	private String jobtitlename;
	// ��λ����
	private String jobtitleremark;
	// ְλ����   �����ж� �ޱ���
	private String jobactivityName;
	// ְλ��   �����ж� �ޱ���
	private String jobGroupName;
	// ��λ��������  0ͨ��ID  1��ͨ������
	private int deptIdOrCode;
	// ��λ��������ID
	private String jobdepartmentid;
	// ��λ��������Code
	private String jobdepartmentCode;
	// �ϼ���λCode
	private String superJobCode;
	// ���ϵͳ�������ֵ,�Ͳ��������
	private List<String> notUpdate;
	
	// ���ϵͳ�������ֵ,�Ͳ��������
	public List<String> getNotUpdate(){
		if(notUpdate == null) {
			notUpdate = new ArrayList<String>();
		}
		return  notUpdate;
	}
	// ���ϵͳ�������ֵ,�Ͳ��������
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
