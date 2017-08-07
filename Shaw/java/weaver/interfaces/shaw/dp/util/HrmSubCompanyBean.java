package weaver.interfaces.shaw.dp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weaver.general.Util;

public class HrmSubCompanyBean {
	
	// �ֲ�����  ���ֱ�ʾ
	private String subCompanyCode;
	// �ֲ����
	private String subCompanyName;
	// �ֲ�ȫ��
	private String subCompanyDesc;
	// �ϼ���ϵ      0ͨ��ID  1��ͨ������
	private int idOrCode;
	// �ϼ�id   ���û���ϼ�����Ϊ0 
	private String superID;
	// �ϼ�Ψһ����   ���û���ϼ�����Ϊ�� 
	private String superCode;
	// ״̬   1 ���   0����
	private int status = 0;
	// ����
	private int orderBy = 0;
	// �Զ����ֶ�  
	private Map<String,String> cusMap;
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
	
	public int getIdOrCode() {
		return idOrCode;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setIdOrCode(int idOrCode) {
		this.idOrCode = idOrCode;
	}

	public String getSuperCode() {
		return Util.null2String(superCode);
	}

	public void setSuperCode(String superCode) {
		this.superCode = superCode;
	}

	public String getSubCompanyCode() {
		return Util.null2String(subCompanyCode);
	}
	
	public void setSubCompanyCode(String subCompanyCode) {
		this.subCompanyCode = subCompanyCode;
	}
	
	public String getSubCompanyName() {
		return Util.null2String(subCompanyName);
	}
	
	public void setSubCompanyName(String subCompanyName) {
		this.subCompanyName = subCompanyName;
	}
	public String getSubCompanyDesc() {
		return Util.null2String(subCompanyDesc);
	}
	
	public void setSubCompanyDesc(String subCompanyDesc) {
		this.subCompanyDesc = subCompanyDesc;
	}
	
	public String getSuperID() {
		return Util.null2String(superID);
	}
	
	public void setSuperID(String superID) {
		this.superID = superID;
	}
	
	public int getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(int orderBy) {
		this.orderBy = orderBy;
	}
	public Map<String, String> getCusMap() {
		return cusMap;
	}
	
	public void setCusMap(Map<String, String> cusMap) {
		this.cusMap = cusMap;
	}	
	
	public void addCusMap(String key,String value){
		if(cusMap == null)  cusMap = new HashMap<String, String>();
		cusMap.put(key,value);
	}
}
