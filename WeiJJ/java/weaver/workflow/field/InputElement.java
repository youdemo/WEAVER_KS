package weaver.workflow.field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
 
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.system.code.CodeBuild;
import weaver.systeminfo.SystemEnv;


public class InputElement extends BaseBean implements HtmlElement{



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * fieldid：字段id
	 * fieldname：字段的数据库名
	 * type：字段小类型，文本、整数、浮点....、人力资源、文档....浏览框
	 * fieldlabel：字段显示名称，在这里可以直接用，不是数据库标签id
	 * textlength：字段长度。在单行文本框中，代表该文本框可是输入最大的字符数（不是中文字数，所以要除以2）；在多行文本框中，代表文本框的高度。
	 * isdetail：是否明细字段，1、是；0、不是；该参数的识别优先级高于groupid
	 * groupid：明细字段组id，如果是-1，则代表是用在addrow里
	 * fieldvalue：值。传过来前认为没做过任何处理。即使是创建流程，也可能因为节点前附加操作而有值
	 * isviewonly：是否仅查看，1、是；0、不是。如果是1，则认为是viewrequest，在isview==0的时候不要hidden的input。该参数的识别优先级高于isedit和ismand
	 * isview：字段是否显示，1、显示；0、不显示。该参数的识别优先级高于isviewonly、isedit和ismand
	 * isedit：是否可编辑，1、可编辑；0、不可编辑。该参书的识别优先及高于ismand
	 * ismand：是否必填，1、是；0、否
	 * otherPara_hs：其他参数，为实现个别字段的特殊功能而设，每种字段所需要的各不相同，用Hashtable存放。
	 */
	public Hashtable getHtmlElementString(int fieldid, String fieldname, int type, String fieldlabel, int textlength, int isdetail, int groupid, String fieldvalue, int isviewonly, int isview, int isedit, int ismand, User user, Hashtable otherPara_hs) {
		// TODO Auto-generated method stub
		Hashtable ret_hs = new Hashtable();
		String inputStr = "";
		String jsStr = "";
		try{
            int languageid=user.getLanguage();
			String fielddbtype = Util.null2String((String)otherPara_hs.get("fielddbtype"));
			if(isdetail == 0){//主字段
				HashMap specialfield = (HashMap)otherPara_hs.get("specialfield");
				if(specialfield == null){
					specialfield = new HashMap();
				}
				//int codeFields = Util.getIntValue((String)otherPara_hs.get("codeFields"), 0);//编码字段
				int codeField = Util.getIntValue((String)otherPara_hs.get("codeField"), 0);//创建文档字段
				//String codeFields = Util.null2String((String)otherPara_hs.get("codeFields"));
				int titleFieldId = Util.getIntValue((String)otherPara_hs.get("titleFieldId"), 0);
				int keywordFieldId = Util.getIntValue((String)otherPara_hs.get("keywordFieldId"), 0);
				String trrigerfield = Util.null2String((String)otherPara_hs.get("trrigerfield"));
				ArrayList changefieldsadd = (ArrayList)otherPara_hs.get("changefieldsadd");
				
				int iscreate = Util.getIntValue((String)otherPara_hs.get("iscreate"), 0);
				String isUse = Util.null2String((String)otherPara_hs.get("isUse"));
				boolean hasHistoryCode=Util.null2String((String)otherPara_hs.get("hasHistoryCode")).equals("true");
				String fieldCode = Util.null2String((String)otherPara_hs.get("fieldCode"));				
			
				String ismandStr = "";
				if(isviewonly==0 && isview==1 && isedit==1 && ismand==1 && "".equals(fieldvalue)){
					ismandStr = "<img src='/images/BacoError.gif' align='absmiddle'>";
				}
				String sqlAttrStr = "";
				String sqlAttrStr1 = "";
				String sqlfieldids = "";
				ArrayList sqlfieldidList = (ArrayList)otherPara_hs.get("sqlfieldidList");
				ArrayList sqlcontentList = (ArrayList)otherPara_hs.get("sqlcontentList");
				if(sqlfieldidList!=null && sqlfieldidList.size()>0){
					for(int i=0; i<sqlfieldidList.size(); i++){
						String sqlfieldid_tmp = Util.null2String((String)sqlfieldidList.get(i)).trim();
						String sqlcontent_tmp = Util.null2String((String)sqlcontentList.get(i)).trim();
						if(!"".equals(sqlcontent_tmp)){
							if(sqlcontent_tmp.indexOf("$"+fieldid+"$") > -1){
								sqlfieldids += (sqlfieldid_tmp+",");
							}
						}
						if(sqlfieldid_tmp.equals(""+fieldid)){
							jsStr += "function getFieldValueAjax"+fieldid+"(){"+"\n";
							jsStr += "initFieldValue(\""+fieldid+"\");"+"\n";
							jsStr += "}"+"\n";
							//jsStr += "\twindow.attachEvent(\"onload\", getFieldValueAjax"+fieldid+");"+"\n";
							
							jsStr += "\t" + "if (window.addEventListener){"+"\n";
							jsStr += "\t" + "    window.addEventListener(\"load\", getFieldValueAjax"+fieldid+", false);"+"\n";
							jsStr += "\t" + "}else if (window.attachEvent){"+"\n";
							jsStr += "\t" + "    window.attachEvent(\"onload\", getFieldValueAjax"+fieldid+");"+"\n";
							jsStr += "\t" + "}else{"+"\n";
							jsStr += "\t" + "    window.onload=getFieldValueAjax"+fieldid+";"+"\n";
							jsStr += "\t" + "}"+"\n";
						}
					}
					if(sqlfieldids.length() > 0){
						sqlAttrStr = "doSqlFieldAjax(this,'"+sqlfieldids.substring(0, sqlfieldids.length()-1)+"');";
						sqlAttrStr1 = "doSqlFieldAjax(this,'"+sqlfieldids.substring(0, sqlfieldids.length()-1)+"')";
					}
				}
				//数字数值型字段之间的计算、赋值操作
				ArrayList attrfieldidList = (ArrayList)otherPara_hs.get("attrfieldidList");
				ArrayList attrcontentList = (ArrayList)otherPara_hs.get("attrcontentList");
				if(attrfieldidList!=null && attrfieldidList.size()>0){
					for(int i=0; i<attrfieldidList.size(); i++){
						String attrfieldid_tmp = Util.null2String((String)attrfieldidList.get(i));
						String attrcontent_tmp = Util.null2String((String)attrcontentList.get(i));
						if(attrcontent_tmp.indexOf("$"+fieldid+"$") > -1){//作为主动字段，启动onchange事件
							sqlAttrStr += "doMathFieldAttr"+attrfieldid_tmp+"();";
							if("".equals(sqlAttrStr1)){
								sqlAttrStr1 = "doMathFieldAttr"+attrfieldid_tmp+"()";
							}else{
								sqlAttrStr1 += ";doMathFieldAttr"+attrfieldid_tmp+"()";
							}
						}
					}
				}
				//日期计算
				String datefieldids = "";
				ArrayList datefieldidList = (ArrayList)otherPara_hs.get("datefieldidList");
				ArrayList datecontentList = (ArrayList)otherPara_hs.get("datecontentList");
				if(datefieldidList!=null && datefieldidList.size()>0){
					for(int i=0; i<datefieldidList.size(); i++){
						String datefieldid_tmp = Util.null2String((String)datefieldidList.get(i)).trim();
						String datecontent_tmp = Util.null2String((String)datecontentList.get(i)).trim();
						if(!"".equals(datecontent_tmp)){
							if(datecontent_tmp.indexOf("$"+fieldid+"$") > -1){
								datefieldids += ("doFieldDate"+datefieldid_tmp+"(-1);");
							}
						}
						if(datefieldid_tmp.equals(""+fieldid)){
							jsStr += "function getFieldDateAjax"+fieldid+"(){"+"\n";
							jsStr += "doFieldDate"+fieldid+"(-1);"+"\n";
							jsStr += "}"+"\n";
							jsStr += "\twindow.attachEvent(\"onload\", getFieldDateAjax"+fieldid+");"+"\n";
						}
					}
					if(datefieldids.length() > 0){
						sqlAttrStr = datefieldids;
						sqlAttrStr1 = datefieldids.substring(0, datefieldids.length()-1);
					}
				}
				
				//SAP取值
				ArrayList sapfieldidList = (ArrayList)otherPara_hs.get("sapfieldidList");
				if(sapfieldidList != null && sapfieldidList.size() > 0){
					for(int i = 0; i<sapfieldidList.size(); i++){
						String attrfieldid_tmp = Util.null2String((String)sapfieldidList.get(i));
						String fieldidtmp = attrfieldid_tmp.substring(0,attrfieldid_tmp.indexOf("-"));
						String attridtmp = attrfieldid_tmp.substring(attrfieldid_tmp.indexOf("-") + 1);
						if(("" + fieldid).equals(fieldidtmp)){
							sqlAttrStr += "doSAPField('"+attridtmp+"',this);";
							if("".equals(sqlAttrStr1)){
								sqlAttrStr1 = "doSAPField('"+attridtmp+"',this)";
							}else{
								sqlAttrStr1 += ";doSAPField('"+attridtmp+"',this)";
							}
						}
					}
				}
				
				if(!"".equals(sqlAttrStr1)){
					//sqlAttrStr1 = (" onpropertychange=\"" + sqlAttrStr1 + "\" ");
				}
				int decimaldigits_t = 2;
				if(isview == 1){
				   if(type == 3){
						int digitsIndex = fielddbtype.indexOf(",");
						if(digitsIndex > -1){
							decimaldigits_t = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1), 2);
						}else{
							decimaldigits_t = 2;
						}
					}
					if(isviewonly == 0){
						if(isedit == 1){//可编辑
							if(type == 1){//文本
								if(fieldid!=codeField){
									if(keywordFieldId>0 && keywordFieldId==fieldid){
										inputStr += "<button type=button class=\"Browser\" onclick=\"onShowKeyword(field"+fieldid+".getAttribute(\'viewtype\'))\" title=\""+SystemEnv.getHtmlLabelName(21517, languageid)+"\"></button>";
									}
									
									String styleWidth="width:90%";
									if(iscreate==0&&"1".equals(isUse)&&!hasHistoryCode&&fieldCode.equals(""+fieldid)){//启用新版流程编号
										styleWidth="width:60%";
									}
									inputStr += "<input datatype=\"text\" viewtype=\""+ismand+"\" type=\"text\" class=\"Inputstyle\" temptitle=\""+Util.toScreen(fieldlabel,languageid)+"\" id=\"field"+fieldid+"\" name=\"field"+fieldid+"\" style=\""+styleWidth+"\" onChange=\"checkinput2('field"+fieldid+"','field"+fieldid+"span',this.getAttribute(\'viewtype\'));checkLength('field"+fieldid+"','"+textlength+"','"+Util.toScreen(fieldlabel,languageid)+"','"+SystemEnv.getHtmlLabelName(20246, languageid)+"','"+SystemEnv.getHtmlLabelName(20247, languageid)+"')";
									if(titleFieldId>0 && keywordFieldId>0 && titleFieldId==fieldid){
										inputStr += ";changeKeyword()";
									}
									
									//start_td20002
									if(iscreate==0 && "1".equals(isUse) && !hasHistoryCode&&fieldCode.equals(""+fieldid)){//启用新版流程编号
										inputStr += ";onChangeCode('"+ismand+"')";
									}
									//end_td20002
									inputStr += ";"+sqlAttrStr+"\"";
									//inputStr += " onpropertychange=\"if (event.propertyName == 'value') {checkLength('field"+fieldid+"','"+textlength+"','"+Util.toScreen(fieldlabel,languageid)+"','"+SystemEnv.getHtmlLabelName(20246, languageid)+"','"+SystemEnv.getHtmlLabelName(20247, languageid)+"')}\" ";
									if(trrigerfield.indexOf("field"+fieldid)>=0){
										inputStr += " onBlur=\"datainput('field"+fieldid+"')\" ";
									}
									inputStr += " value=\""+Util.toScreenForWorkflow(fieldvalue)+"\" >";
									
									//流程编号  开始
									if(iscreate==0&&"1".equals(isUse)&&!hasHistoryCode&&fieldCode.equals(""+fieldid)){//启用新版流程编号
										inputStr += "<A href=\"#\" onclick=\"onCreateCodeAgain('"+ismand+"');return false;\">"+SystemEnv.getHtmlLabelName(22784,languageid)+"</a>";
										inputStr += "&nbsp;&nbsp;&nbsp;&nbsp";
										inputStr += "<A href=\"#\" onclick=\"onChooseReservedCode('"+ismand+"');return false;\">"+SystemEnv.getHtmlLabelName(22785,languageid)+"</a>";
										inputStr += "&nbsp;&nbsp;&nbsp;&nbsp";
										inputStr += "<A href=\"#\" onclick=\"onNewReservedCode('"+ismand+"');return false;\">"+SystemEnv.getHtmlLabelName(22783,languageid)+"</a>";
									}
									//流程编号  结束
									
								}
							}else if(type == 2){//整数
								/**
								 * 单行文本-整数  
								 * 再此行(inputStr)添加 onkeyup onafterpaste style="ime-mode:disabled"
								 * ypc  2012-09-04 添加
								 */
								inputStr += "<input datatype=\"int\"  onafterpaste=\"if(isNaN(value))execCommand('undo')\"  style=\"ime-mode:disabled\" viewtype=\""+ismand+"\" type=\"text\" class=\"Inputstyle\" temptitle=\""+Util.toScreen(fieldlabel, languageid)+"\" id=\"field"+fieldid+"\" name=\"field"+fieldid+"\" style=\"width:70%\" onKeyPress=\"ItemCount_KeyPress()\" ";
								if(trrigerfield.indexOf("field"+fieldid)>=0){
									inputStr += " onBlur=\"checkcount1(this);checkItemScale(this,'" + SystemEnv.getHtmlLabelName(31181,languageid).replace("12","9") + "',-999999999,999999999);checkinput2('field"+fieldid+"','field"+fieldid+"span',this.getAttribute(\'viewtype\'));datainput('field"+fieldid+"')\" ";
								}else{
									inputStr += " onBlur=\"checkcount1(this);checkItemScale(this,'" + SystemEnv.getHtmlLabelName(31181,languageid).replace("12","9") + "',-999999999,999999999);checkinput2('field"+fieldid+"','field"+fieldid+"span',this.getAttribute(\'viewtype\'))\" ";
								}
								inputStr += " value=\""+fieldvalue+"\"  onchange=\""+sqlAttrStr+"\"  onpropertychange=\""+sqlAttrStr+"\"   _listener=\""+sqlAttrStr+"\">";
							}else if(type==3 || type==5){//浮点数 || 金额千分位
								if(type == 3){
									int digitsIndex = fielddbtype.indexOf(",");
									if(digitsIndex > -1){
										decimaldigits_t = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1), 2);
									}else{
										decimaldigits_t = 2;
									}
									fieldvalue = Util.toDecimalDigits(fieldvalue,decimaldigits_t);
								}
							 
								/**
								 * 单行文本-浮点数 || 金额千分位
								 * 再此行(inputStr)添加 onkeyup onafterpaste style="ime-mode:disabled"
								 * ypc  2012-09-04 添加
								 */
								inputStr += "<input datalength='"+decimaldigits_t+"' datatype=\"float\"  style=\"ime-mode:disabled\"  onafterpaste=\"if(isNaN(value))execCommand('undo')\" viewtype=\""+ismand+"\" type=\"text\" class=\"Inputstyle\" temptitle=\""+Util.toScreen(fieldlabel, languageid)+"\" id=\"field"+fieldid+"\" name=\"field"+fieldid+"\" style=\"width:70%\" onKeyPress=\"ItemDecimal_KeyPress('field"+fieldid+"',15,"+decimaldigits_t+")\" ";
								if(type == 5){
									fieldvalue = Util.toDecimalDigits(fieldvalue,2);

									inputStr += " datavaluetype='5'  onfocus=\"changeToNormalFormat('field"+fieldid+"')\" ";
								}
								inputStr += " datalength="+decimaldigits_t+" onBlur=\"checkFloat(this);checkinput2('field"+fieldid+"','field"+fieldid+"span',this.getAttribute(\'viewtype\'))";
								if(trrigerfield.indexOf("field"+fieldid)>=0){
									inputStr += ";datainput('field"+fieldid+"')";
								}
								if(type == 5){
								inputStr += ";changeToThousands('field"+fieldid+"')";
								}
								inputStr += "\" ";

								inputStr += " value=\""+fieldvalue+"\"  onchange=\""+sqlAttrStr+"\"  onpropertychange=\""+sqlAttrStr+"\" _listener=\""+sqlAttrStr+"\">";
							}else if(type == 4){//金额
                                 if(type == 4){
									int digitsIndex = fielddbtype.indexOf(",");
									if(digitsIndex > -1){
										decimaldigits_t = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1), 2);
									}else{
										decimaldigits_t = 2;
									}
									fieldvalue = Util.toDecimalDigits(fieldvalue,decimaldigits_t);
								}
								inputStr += "<table cols=\"2\" id=\"field"+fieldid+"_tab\" width=\"100%\">";
								inputStr += "<tr><td>";
								if(isedit == 1){
									/**
									 * 把onKeyPress=\"ItemNum_KeyPress('field_lable"+fieldid+"')\"
									 * 换成onKeyPress
									 * 再此行(inputStr)添加 onkeydown  onkeyup onafterpaste style="ime-mode:disabled"
									 * ypc  2012-09-04 添加
									 */
									inputStr += "<input datatype=\"float\"  onKeyPress=\"ItemDecimal_KeyPress('field_lable"+fieldid+"',15,"+decimaldigits_t+")\" style=\"ime-mode:disabled;width:70%\"  onafterpaste=\"if(isNaN(value))execCommand('undo')\" type=\"text\" class=\"Inputstyle\" id=\"field_lable"+fieldid+"\" name=\"field_lable"+fieldid+"\" temptitle=\""+Util.toScreen(fieldlabel, languageid)+"\" onfocus=\"FormatToNumber('"+fieldid+"')\" ";
									if(trrigerfield.indexOf("field"+fieldid)>=0){
										inputStr += " onBlur=\"checkFloat(this);numberToFormat('"+fieldid+"');checkinput2('field_lable"+fieldid+"','field_lable"+fieldid+"span',field"+fieldid+".getAttribute(\'viewtype\'));datainput('field_lable"+fieldid+"')\"";
									}else{
										inputStr += " onBlur=\"checkFloat(this);numberToFormat('"+fieldid+"');checkinput2('field_lable"+fieldid+"','field_lable"+fieldid+"span',field"+fieldid+".getAttribute(\'viewtype\'))\"";
									}
									inputStr += " onpropertychange=\""+sqlAttrStr1+"\">";
									inputStr += "<span id=\"field_lable"+fieldid+"span\">"+ismandStr+"</span>";
									inputStr += "<span id=\"field"+fieldid+"span\" style=\"word-break:break-all;word-wrap:break-word\"></span>";
									//update by liaodong for qc82290  in 20131016 start
									inputStr += "<input datatype=\"float\" filedtype=\"4\" datalength=\"2\" viewtype=\""+ismand+"\" temptitle=\""+Util.toScreen(fieldlabel, languageid)+"\" type=\"hidden\" class=\"Inputstyle\" id=\"field"+fieldid+"\" name=\"field"+fieldid+"\" value=\""+fieldvalue+"\" >";
								    //end
								}
							}
							if(type != 4){
								inputStr += "<span id=\"field"+fieldid+"span\" style=\"word-break:break-all;word-wrap:break-word\">"+ismandStr+"</span>";
							}
							if(type == 4){
								inputStr += "</td></tr>";
								inputStr += "<tr><td>";
								inputStr += "<input type=\"text\" class=\"Inputstyle\" style=\"width:70%\" id=\"field_chinglish"+fieldid+"\" name=\"field_chinglish"+fieldid+"\" readOnly=\"true\">";
								inputStr += "</td></tr>";
								inputStr += "</table>";
								if(!"".equals(fieldvalue)){
									inputStr += "<script language=\"javascript\">";
									inputStr += "$G(\"field_lable\"+"+fieldid+").value = milfloatFormat(floatFormat("+fieldvalue+"));";
									inputStr += "$G(\"field_chinglish\"+"+fieldid+").value = numberChangeToChinese("+fieldvalue+");";
									inputStr += "</script>";
								}
							}
						}else{
							inputStr += "<input ";
							if(type ==1){
								inputStr += " datatype=\"text\" ";
							}else if(type == 2){//整数
								inputStr += " datatype=\"int\" ";
							}else if(type==3 || type==5){//浮点数
								int digitsIndex = fielddbtype.indexOf(",");
								if(digitsIndex > -1){
									decimaldigits_t = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1), 2);
								}else{
									decimaldigits_t = 2;
								}
								inputStr += " datatype=\"float\" datalength='"+decimaldigits_t+"' ";
								fieldvalue = Util.toDecimalDigits(fieldvalue,decimaldigits_t);
								if(type == 5){
									inputStr +=" datavaluetype='5' ";
								}
							}else if(type == 4){//金额
								inputStr += " datatype=\"float\" ";
							}
							if(type==4){
								//update by liaodong for qc82290  in 20131016 start
								inputStr += " type=\"hidden\"  datalength=\"2\" id=\"field"+fieldid+"\" name=\"field"+fieldid+"\" value=\""+Util.toScreenForWorkflow(fieldvalue)+"\"  onpropertychange=\"checkLength4Read('field"+fieldid+"','field"+fieldid+"span','"+textlength+"','"+Util.toScreen(fieldlabel,languageid)+"','"+SystemEnv.getHtmlLabelName(20246,languageid)+"','"+SystemEnv.getHtmlLabelName(20247,languageid)+"');numberToFormatForReadOnly('"+fieldid+"');"+sqlAttrStr1+"\" onchange=\""+sqlAttrStr+"\" _listener=\"numberToFormatForReadOnly('"+fieldid+"');" + sqlAttrStr  + "\">";
							    //end 
							}else{
								inputStr += " type=\"hidden\" id=\"field"+fieldid+"\" name=\"field"+fieldid+"\" value=\""+Util.toScreenForWorkflow(fieldvalue)+"\"  onpropertychange=\"checkLength4Read('field"+fieldid+"','field"+fieldid+"span','"+textlength+"','"+Util.toScreen(fieldlabel,languageid)+"','"+SystemEnv.getHtmlLabelName(20246,languageid)+"','"+SystemEnv.getHtmlLabelName(20247,languageid)+"');"+sqlAttrStr1+"\" onchange=\""+sqlAttrStr+"\" _listener=\"" + sqlAttrStr + "\">";
							}
							if(type == 4){
								inputStr += "<table id=\"field"+fieldid+"_tab\">\n";
								inputStr += "\t<tr><td>\n";
							}
							if(type == 4){
								inputStr += "<span id=\"field"+fieldid+"span\" style=\"word-break:break-all;word-wrap:break-word\"></span>";
								inputStr += "\t</td></tr>\n";
								inputStr += "\t<tr><td>\n";
								inputStr += "<span id=\"field"+fieldid+"ncspan\"></span>";
								inputStr += "\t</td></tr>\n";
								inputStr += "</table>\n";
								jsStr += "function changeJine2Chinese"+fieldid+"(){\n";
								jsStr += "\ttry{\n";
								jsStr += "\t\tvar vjine = milfloatFormat(floatFormat("+fieldvalue+"));\n";
								jsStr += "\t\t$G(\"field"+fieldid+"span\").innerHTML = vjine;\n";
								jsStr += "\t}catch(e){}\n";
								jsStr += "\ttry{\n";
								jsStr += "\t\tvar cjine = numberChangeToChinese("+fieldvalue+");\n";
								jsStr += "\t\t$G(\"field"+fieldid+"ncspan\").innerHTML = cjine;\n";
								jsStr += "\t}catch(e){}\n";
								jsStr += "}\n";
								//jsStr += "window.attachEvent(\"onload\", changeJine2Chinese"+fieldid+");\n";
								
								jsStr += "\t" + "if (window.addEventListener){"+"\n";
								jsStr += "\t" + "    window.addEventListener(\"load\", changeJine2Chinese"+fieldid+", false);"+"\n";
								jsStr += "\t" + "}else if (window.attachEvent){"+"\n";
								jsStr += "\t" + "    window.attachEvent(\"onload\", changeJine2Chinese"+fieldid+");"+"\n";
								jsStr += "\t" + "}else{"+"\n";
								jsStr += "\t" + "    window.onload=changeJine2Chinese"+fieldid+";"+"\n";
								jsStr += "\t" + "}"+"\n";
							}else{
								inputStr += "<span id=\"field"+fieldid+"span\" style=\"word-break:break-all;word-wrap:break-word\">"+Util.toScreenForWorkflow(fieldvalue)+"</span>";
							}
						}
						if(changefieldsadd.indexOf(""+fieldid)>=0){
							inputStr += "<input type=\"hidden\" id=\"oldfieldview"+fieldid+"\" name=\"oldfieldview"+fieldid+"\" value=\""+(isview+isedit+ismand)+"\" >";
						}
					}else{
						String  datavaluetype="";
						if(type == 5){
							datavaluetype=" datatype=\"float\" datavaluetype='5' datalength='' ";
							fieldvalue = Util.toDecimalDigits(fieldvalue,2);
						}else if(type == 3){
							int digitsIndex = fielddbtype.indexOf(",");
							if(digitsIndex > -1){
								decimaldigits_t = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1), 2);
							}else{
								decimaldigits_t = 2;
							}
							datavaluetype=" datatype=\"float\" datalength='"+decimaldigits_t+"' ";
							fieldvalue = Util.toDecimalDigits(fieldvalue,decimaldigits_t);
						}else if(type == 2){
							datavaluetype=" datatype=\"int\" ";
						}
						if(type==4){
							inputStr += "<input type=\"hidden\" "+datavaluetype+" id=\"field"+fieldid+"\" name=\"field"+fieldid+"\" value=\""+fieldvalue+"\"  onpropertychange=\"numberToFormatForReadOnly('"+fieldid+"');\"  _listener=\"numberToFormatForReadOnly('"+fieldid+"');\">";
						}else{
							inputStr += "<input type=\"hidden\" "+datavaluetype+" id=\"field"+fieldid+"\" name=\"field"+fieldid+"\" value=\""+fieldvalue+"\" >";
						}
						if(type == 4){
							inputStr += "<table id=\"field"+fieldid+"_tab\">\n";
							inputStr += "\t<tr><td>\n";
						}
						if(type == 4){
							inputStr += "<span id=\"field"+fieldid+"span\" style=\"word-break:break-all;word-wrap:break-word\"></span>";
							inputStr += "\t</td></tr>\n";
							inputStr += "\t<tr><td>\n";
							inputStr += "<span id=\"field"+fieldid+"ncspan\"></span>";
							inputStr += "\t</td></tr>\n";
							inputStr += "</table>\n";
							jsStr += "function changeJine2Chinese"+fieldid+"(){\n";
							jsStr += "\ttry{\n";
							jsStr += "\t\tvar vjine = milfloatFormat(floatFormat("+fieldvalue+"));\n";
							jsStr += "\t\t$G(\"field"+fieldid+"span\").innerHTML = vjine;\n";
							jsStr += "\t}catch(e){}\n";
							jsStr += "\ttry{\n";
							jsStr += "\t\tvar cjine = numberChangeToChinese("+fieldvalue+");\n";
							jsStr += "\t\t$G(\"field"+fieldid+"ncspan\").innerHTML = cjine;\n";
							jsStr += "\t}catch(e){}\n";
							jsStr += "}\n";
							//jsStr += "window.attachEvent(\"onload\", changeJine2Chinese"+fieldid+");\n";
							jsStr += "\t" + "if (window.addEventListener){"+"\n";
							jsStr += "\t" + "    window.addEventListener(\"load\", changeJine2Chinese"+fieldid+", false);"+"\n";
							jsStr += "\t" + "}else if (window.attachEvent){"+"\n";
							jsStr += "\t" + "    window.attachEvent(\"onload\", changeJine2Chinese"+fieldid+");"+"\n";
							jsStr += "\t" + "}else{"+"\n";
							jsStr += "\t" + "    window.onload=changeJine2Chinese"+fieldid+";"+"\n";
							jsStr += "\t" + "}"+"\n";
						}else{
							
							
			     String toPvalue = "";
							if(fieldvalue.matches("-*\\d+\\.?\\d*") && type == 5){
								NumberFormat formatter = new DecimalFormat("###,###.##");   
								toPvalue = formatter.format(Double.parseDouble(fieldvalue))+""; 
								toPvalue = Util.toDecimalDigits(toPvalue,2);
								fieldvalue = toPvalue;
							}else{
								toPvalue = fieldvalue;
							}
							fieldvalue = toPvalue;
							inputStr += "<span id=\"field"+fieldid+"span\" style=\"word-break:break-all;word-wrap:break-word\">"+Util.toScreenForWorkflow(fieldvalue)+"</span>";
						}
					}
				}else{
					//if(isviewonly == 0){
						inputStr += "<input type=\"hidden\" datalength=2 id=\"field"+fieldid+"\" name=\"field"+fieldid+"\" value=\""+Util.toScreenForWorkflow(fieldvalue)+"\" >";
					//}
				}
			}else{
				
			 
				//明细字段
				String derecorderindex = Util.null2String((String)otherPara_hs.get("derecorderindex"));
				String jsSpileStr = "\"";
				if("\"+rowindex+\"".equals(derecorderindex)){
					jsSpileStr = "\\\"";
				}
				String trrigerdetailfield = Util.null2String((String)otherPara_hs.get("trrigerdetailfield"));
				ArrayList changedefieldsadd = (ArrayList)otherPara_hs.get("changedefieldsadd");
				int firstDetailFieldid = Util.getIntValue((String)otherPara_hs.get("firstDetailFieldid"), 0);
				int inputWidthExt = 0;
				if(firstDetailFieldid == fieldid){
					inputWidthExt = 10;
				}
				String ismandStr = "";
				if(isviewonly==0 && isview==1 && isedit==1 && ismand==1 && "".equals(fieldvalue)){
					ismandStr = "<img src='/images/BacoError.gif' align='absmiddle'>";
				}
				String sqlAttrStr = "";
				String sqlAttrStr1 = "";
				String sqlfieldids = "";
				ArrayList sqlfieldidList = (ArrayList)otherPara_hs.get("sqlfieldidList");
				ArrayList sqlcontentList = (ArrayList)otherPara_hs.get("sqlcontentList");
				if(sqlfieldidList!=null && sqlfieldidList.size()>0){
					for(int i=0; i<sqlfieldidList.size(); i++){
						String sqlfieldid_tmp = Util.null2String((String)sqlfieldidList.get(i)).trim();
						String sqlcontent_tmp = Util.null2String((String)sqlcontentList.get(i)).trim();
						if(!"".equals(sqlcontent_tmp)){
							if(sqlcontent_tmp.indexOf("$"+fieldid+"$") > -1){
								sqlfieldids += (sqlfieldid_tmp+",");
							}
						}
						if(sqlfieldid_tmp.equals(""+fieldid)){
							if("\"+rowindex+\"".equals(derecorderindex)){
								jsStr += "initFieldValue(\""+fieldid+"_\"+rowindex);"+"\n";
							}else{
								jsStr += "function getFieldValueAjaxDetail"+fieldid+"_"+derecorderindex+"(){"+"\n";
								jsStr += "\t"+"initFieldValue(\""+fieldid+"_"+derecorderindex+"\");"+"\n";
								jsStr += "}"+"\n";
								//jsStr += "\twindow.attachEvent(\"onload\", getFieldValueAjaxDetail"+fieldid+"_"+derecorderindex+");"+"\n";
								
								jsStr += "\t" + "if (window.addEventListener){"+"\n";
								jsStr += "\t" + "    window.addEventListener(\"load\", getFieldValueAjaxDetail"+fieldid+"_"+derecorderindex+", false);"+"\n";
								jsStr += "\t" + "}else if (window.attachEvent){"+"\n";
								jsStr += "\t" + "    window.attachEvent(\"onload\", getFieldValueAjaxDetail"+fieldid+"_"+derecorderindex+");"+"\n";
								jsStr += "\t" + "}else{"+"\n";
								jsStr += "\t" + "    window.onload=getFieldValueAjaxDetail"+fieldid+"_"+derecorderindex+";"+"\n";
								jsStr += "\t" + "}"+"\n";
								
							}
						}
					}
					if(sqlfieldids.length() > 0){
						sqlAttrStr = ";doSqlFieldAjax(this,'"+sqlfieldids.substring(0, sqlfieldids.length()-1)+"')";
						sqlAttrStr1 = " onchange="+jsSpileStr+"doSqlFieldAjax(this,'"+sqlfieldids.substring(0, sqlfieldids.length()-1)+"')"+jsSpileStr+" ";
						sqlAttrStr1 +=" onpropertychange="+jsSpileStr+"doSqlFieldAjax(this,'"+sqlfieldids.substring(0, sqlfieldids.length()-1)+"')"+jsSpileStr+""; 
						sqlAttrStr1 +=" _listener="+jsSpileStr+"doSqlFieldAjax(this,'"+sqlfieldids.substring(0, sqlfieldids.length()-1)+"')"+jsSpileStr+""; 
					}
				}
				//日期计算
				String datefieldids = "";
				ArrayList datefieldidList = (ArrayList)otherPara_hs.get("datefieldidList");
				ArrayList datecontentList = (ArrayList)otherPara_hs.get("datecontentList");
				if(datefieldidList!=null && datefieldidList.size()>0){
					for(int i=0; i<datefieldidList.size(); i++){
						String datefieldid_tmp = Util.null2String((String)datefieldidList.get(i)).trim();
						String datecontent_tmp = Util.null2String((String)datecontentList.get(i)).trim();
						if(!"".equals(datecontent_tmp)){
							if(datecontent_tmp.indexOf("$"+fieldid+"$") > -1){
								datefieldids += ("doFieldDate"+datefieldid_tmp+"("+derecorderindex+");");
							}
						}
						if(datefieldid_tmp.equals(""+fieldid)){
							if("\"+rowindex+\"".equals(derecorderindex)){
								jsStr += "eval(\"doFieldDate"+fieldid+"(\"+rowindex+\");\");"+"\n";
							}else{
								jsStr += "function getFieldDateAjaxDetail"+fieldid+"_"+derecorderindex+"(){"+"\n";
								jsStr += "\t"+"doFieldDate"+fieldid+"("+derecorderindex+");"+"\n";
								jsStr += "}"+"\n";
								jsStr += "\twindow.attachEvent(\"onload\", getFieldDateAjaxDetail"+fieldid+"_"+derecorderindex+");"+"\n";
							}
						}
					}
					if(datefieldids.length() > 0){
						sqlAttrStr = ";"+datefieldids;
						sqlAttrStr1 = " onchange="+jsSpileStr+datefieldids+jsSpileStr+" ";
						sqlAttrStr1 +=" onpropertychange="+jsSpileStr+datefieldids+jsSpileStr+""; 
						sqlAttrStr1 +=" _listener="+jsSpileStr+datefieldids+jsSpileStr+"";
					}
				}
				
				ArrayList sapfieldidList = (ArrayList)otherPara_hs.get("sapfieldidList");
				if(sapfieldidList != null && sapfieldidList.size() > 0){
					for(int i = 0; i<sapfieldidList.size(); i++){
						String attrfieldid_tmp = Util.null2String((String)sapfieldidList.get(i));
						String fieldidtmp = attrfieldid_tmp.substring(0,attrfieldid_tmp.indexOf("-"));
						String attridtmp = attrfieldid_tmp.substring(attrfieldid_tmp.indexOf("-") + 1);
						if(("" + fieldid).equals(fieldidtmp)){
							sqlAttrStr += ";doSAPField('"+attridtmp+"',this);";
							sqlAttrStr1 = " onchange="+jsSpileStr+"doSAPField('"+attridtmp+"',this)"+jsSpileStr+" ";
							sqlAttrStr1 +=  " onpropertychange="+jsSpileStr+"doSAPField('"+attridtmp+"',this)"+jsSpileStr+" ";
							sqlAttrStr1 +=  " _listener="+jsSpileStr+"doSAPField('"+attridtmp+"',this)"+jsSpileStr+" ";
						}
					}
				}

				String trrigerdetailStr = "";
				if (trrigerdetailfield.indexOf("field"+fieldid) >= 0){
					trrigerdetailStr = "datainputd('field"+fieldid+"_"+derecorderindex+"')";
				}
				if(type == 1){//文本
					if(isedit==1 && isviewonly==0){
					  //加上字段联运的js方法。
						inputStr += "<input class='inputstyle' viewtype='"+ismand+"' datatype='text' type='text' temptitle='"+fieldlabel+"' name='field"+fieldid+"_"+derecorderindex+"' id='field"+fieldid+"_"+derecorderindex+"' style='width:"+(80-inputWidthExt)+"%' value=" + jsSpileStr + ""+Util.toScreenForWorkflow(fieldvalue) + jsSpileStr + " onChange="+jsSpileStr+"checkinput2('field"+fieldid+"_"+derecorderindex+"','field"+fieldid+"_"+derecorderindex+"span',this.getAttribute(\'viewtype\'));"+trrigerdetailStr+";checkLength('field"+fieldid+"_"+derecorderindex+"','"+textlength+"','"+fieldlabel+"','"+SystemEnv.getHtmlLabelName(20246,languageid)+"','"+SystemEnv.getHtmlLabelName(20247,languageid)+"')"+sqlAttrStr+jsSpileStr+">";
						inputStr += "<span id='field"+fieldid+"_"+derecorderindex+"span'>"+ismandStr+"</span>";
						if(changedefieldsadd.indexOf(""+fieldid)>=0){
							inputStr += "<input type='hidden' name='oldfieldview"+fieldid+"_"+derecorderindex+"' value=" + jsSpileStr + ""+(isview+isedit+ismand)+ jsSpileStr + " />";
						}
					}else{
						inputStr += "<span id='field"+fieldid+"_"+derecorderindex+"span'>"+Util.toScreenForWorkflow(fieldvalue)+"</span>";
						//if(isviewonly == 0){
							inputStr += "<input type='hidden'  datatype='text' class='Inputstyle' id='field"+fieldid+"_"+derecorderindex+"' name='field"+fieldid+"_"+derecorderindex+"'  value=" + jsSpileStr + ""+Util.toScreenForWorkflow(fieldvalue)+"" + jsSpileStr + ">";
						//}
					}
				}else if(type == 2){//整型
					if(isedit==1 && isviewonly==0){
						inputStr += "<input class='inputstyle' viewtype='"+ismand+"' datatype='int' temptitle='"+fieldlabel+"' type='text' name='field"+fieldid+"_"+derecorderindex+"' id='field"+fieldid+"_"+derecorderindex+"' style='width:60%' value='"+fieldvalue+"' onKeyPress='ItemCount_KeyPress()' onChange="+jsSpileStr+"checkcount1(this);checkItemScale(this,'" + SystemEnv.getHtmlLabelName(31181,languageid).replace("12","9") + "',-999999999,999999999);checkinput2('field"+fieldid+"_"+derecorderindex+"','field"+fieldid+"_"+derecorderindex+"span',this.getAttribute(\'viewtype\'));calSum("+groupid+")"+sqlAttrStr+";"+trrigerdetailStr+jsSpileStr+">";
						inputStr += "<span id='field"+fieldid+"_"+derecorderindex+"span'>"+ismandStr+"</span>";
						if(changedefieldsadd.indexOf(""+fieldid)>=0){
							inputStr += "<input type='hidden' name='oldfieldview"+fieldid+"_"+derecorderindex+"' value='"+(isview+isedit+ismand)+"' />";
						}
					}else{
						inputStr += "<span id='field"+fieldid+"_"+derecorderindex+"span'>"+fieldvalue+"</span>";
						//if(isviewonly == 0){
							inputStr += "<input type='hidden' datatype='int' class='Inputstyle' id='field"+fieldid+"_"+derecorderindex+"' name='field"+fieldid+"_"+derecorderindex+"'  onpropertychange="+jsSpileStr+"checkLength4Read('field"+fieldid+"_"+derecorderindex+"','field"+fieldid+"_"+derecorderindex+"span','"+textlength+"','"+Util.toScreen(fieldlabel,languageid)+"','"+SystemEnv.getHtmlLabelName(20246,languageid)+"','"+SystemEnv.getHtmlLabelName(20247,languageid)+"');"+jsSpileStr+" value='"+fieldvalue+"'>";
						//}
					}
				}else if(type==3 || type==5){//浮点型 || 金额千分位
					int decimaldigits_t = 2;
					if(type == 3){
						int digitsIndex = fielddbtype.indexOf(",");
						if(digitsIndex > -1){
							decimaldigits_t = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1), 2);
						}else{
							decimaldigits_t = 2;
						}
						fieldvalue = Util.toDecimalDigits(fieldvalue,decimaldigits_t);
					}
			 
					
					String  datavaluetype=" datalength='"+decimaldigits_t+"' ";
					if(type == 5){
						datavaluetype="datavaluetype='5'";
						fieldvalue = Util.toDecimalDigits(fieldvalue,2);
					}
					if(isedit==1 && isviewonly==0){
						inputStr += "<input class='inputstyle' viewtype='"+ismand+"' "+datavaluetype+" valuetype='5' datatype='float' temptitle='"+fieldlabel+"' type='text' name='field"+fieldid+"_"+derecorderindex+"' id='field"+fieldid+"_"+derecorderindex+"' style='width:90%' value='"+fieldvalue+"' onKeyPress=" + jsSpileStr + "ItemDecimal_KeyPress(this.name, 15, " + decimaldigits_t + ")" + jsSpileStr + " ";
						if(type == 5){
							inputStr += " onfocus='changeToNormalFormat(this.name)' onblur='changeToThousands(this.name);calSum("+groupid+")' ";
						}else{
							inputStr += " onblur='calSum("+groupid+")' ";
						}
						inputStr += " onChange="+jsSpileStr+"checkFloat(this);checkinput2('field"+fieldid+"_"+derecorderindex+"','field"+fieldid+"_"+derecorderindex+"span',this.getAttribute(\'viewtype\'));calSum("+groupid+")"+sqlAttrStr+";"+trrigerdetailStr+jsSpileStr+">";
						inputStr += "<span id='field"+fieldid+"_"+derecorderindex+"span'>"+ismandStr+"</span>";
						if(changedefieldsadd.indexOf(""+fieldid)>=0){
							inputStr += "<input "+datavaluetype+" type='hidden' name='oldfieldview"+fieldid+"_"+derecorderindex+"' value='"+(isview+isedit+ismand)+"' />";
						}
					}else{
							String toPvalue = "";
					 
						
						if(fieldvalue.matches("-*\\d+\\.?\\d*") && type == 5){
							NumberFormat formatter = new DecimalFormat("###,###.##");   
							toPvalue = formatter.format(Double.parseDouble(fieldvalue))+""; 
							toPvalue = Util.toDecimalDigits(toPvalue,2);
						}else{
							toPvalue = fieldvalue;
							toPvalue = Util.toDecimalDigits(fieldvalue,decimaldigits_t);
						}
						fieldvalue = toPvalue; 
						
						
						
						inputStr += "<span id='field"+fieldid+"_"+derecorderindex+"span'>"+fieldvalue+"</span>";
						//if(isviewonly == 0){
							inputStr += "<input type='hidden' "+datavaluetype+" datatype='float' class='Inputstyle' id='field"+fieldid+"_"+derecorderindex+"' name='field"+fieldid+"_"+derecorderindex+"'  onpropertychange="+jsSpileStr+"checkLength4Read('field"+fieldid+"_"+derecorderindex+"','field"+fieldid+"_"+derecorderindex+"span','"+textlength+"','"+Util.toScreen(fieldlabel,languageid)+"','"+SystemEnv.getHtmlLabelName(20246,languageid)+"','"+SystemEnv.getHtmlLabelName(20247,languageid)+"');"+jsSpileStr+" value='"+fieldvalue+"'>";
						//}
					}
				}else if(type == 4){//金额转换
                    int decimaldigits_t = 2;
					if(type == 4 ){
						int digitsIndex = fielddbtype.indexOf(",");
						if(digitsIndex > -1){
							decimaldigits_t = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1), 2);
						}else{
							decimaldigits_t = 2;
						}
					}
					if(isedit==1 && isviewonly==0){
						//add by liaodong for qc43068 in 2013-11-22  datatype='float'
						inputStr += "<input class='inputstyle'   temptitle='"+fieldlabel+"' value='"+fieldvalue+"' datatype='float' datetype='float' type='text' id='field_lable"+fieldid+"_"+derecorderindex+"' name='field_lable"+fieldid+"_"+derecorderindex+"' style='width:60%' onKeyPress="+jsSpileStr+"ItemDecimal_KeyPress('field_lable"+fieldid+"_"+derecorderindex+"',15,"+decimaldigits_t+")"+jsSpileStr+"  onfocus="+jsSpileStr+"getNumber('"+fieldid+"_"+derecorderindex+"')"+jsSpileStr+" onBlur="+jsSpileStr+"numberToChinese('"+fieldid+"_"+derecorderindex+"');checkinput3(field_lable"+fieldid+"_"+derecorderindex+",field"+fieldid+"_"+derecorderindex+"span,field"+fieldid+"_"+derecorderindex+".getAttribute(\'viewtype\'));calSum("+groupid+");"+trrigerdetailStr+jsSpileStr+" "+sqlAttrStr1+"><span id='field"+fieldid+"_"+derecorderindex+"span'>";
						inputStr += ismandStr + "</span>";
						//add by liaodong for qc43068 in 2013-11-22  datatype='float'
						inputStr += "<input class='inputstyle'  datatype='float' datetype='float'  fieldtype='4' datalength='2' type='hidden' viewtype='"+ismand+"' temptitle='"+fieldlabel+"' value='"+fieldvalue+"' name='field"+fieldid+"_"+derecorderindex+"'>";
						if(changedefieldsadd.indexOf(""+fieldid)>=0){
							inputStr += "<input type='hidden'  name='oldfieldview"+fieldid+"_"+derecorderindex+"' value='"+(isview+isedit+ismand)+"' />";
						}
					}else{
						//add by liaodong for qc43068 in 2013-11-22  datatype='float'
						inputStr += "<input class='inputstyle'  style='width:60%' value='"+fieldvalue+"' datatype='float' datetype='float' 'type=text' _printflag='1' disabled='true' id='field_lable"+fieldid+"_"+derecorderindex+"' name='field_lable"+fieldid+"_"+derecorderindex+"'>";
						inputStr += "<span id='field"+fieldid+"_"+derecorderindex+"span'></span>";
						//if(isviewonly == 0){
							inputStr += "<input type='hidden' datalength=2 class='Inputstyle' datatype='float' datetype='float'  fieldtype='4' datalength='2' id='field"+fieldid+"_"+derecorderindex+"' name='field"+fieldid+"_"+derecorderindex+"'  onpropertychange="+jsSpileStr+"checkLength4Read('field"+fieldid+"_"+derecorderindex+"','field"+fieldid+"_"+derecorderindex+"span','"+textlength+"','"+Util.toScreen(fieldlabel,languageid)+"','"+SystemEnv.getHtmlLabelName(20246,languageid)+"','"+SystemEnv.getHtmlLabelName(20247,languageid)+"');"+jsSpileStr+" value='"+fieldvalue+"'>";
						//}
					}
					if(!"\"+rowindex+\"".equals(derecorderindex)){
						inputStr += "\n<script language=\"javascript\">";
						inputStr += "\ntry{";
						inputStr += "\n\t$G(\"field_lable"+fieldid+"_\"+"+derecorderindex+").value  = numberChangeToChinese(\""+fieldvalue+"\");";
						inputStr += "\n}catch(e){}";
						inputStr += "\n</script>";
					}
				}
			}
		}catch(Exception e){
			inputStr = "";
			writeLog(e);
		}
		ret_hs.put("jsStr", jsStr);
		ret_hs.put("inputStr", inputStr);
		return ret_hs;
	}

}
