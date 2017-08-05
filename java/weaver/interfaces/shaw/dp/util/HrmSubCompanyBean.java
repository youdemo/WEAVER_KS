package weaver.interfaces.shaw.dp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weaver.general.Util;

public class HrmSubCompanyBean {
	
	// 分部编码  区分标示
	private String subCompanyCode;
	// 分部简称
	private String subCompanyName;
	// 分部全称
	private String subCompanyDesc;
	// 上级关系      0通过ID  1是通过编码
	private int idOrCode;
	// 上级id   如果没有上级补充为0 
	private String superID;
	// 上级唯一编码   如果没有上级编码为空 
	private String superCode;
	// 状态   1 封存   0正常
	private int status = 0;
	// 排序
	private int orderBy = 0;
	// 自定义字段  
	private Map<String,String> cusMap;
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
