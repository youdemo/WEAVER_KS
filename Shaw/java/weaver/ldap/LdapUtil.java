/**
 * LdapUtil Created on 2005-6-16 14:24:56
 * <p>
 * Copyright(c) 2001-2004 Weaver Software Corp.All rights reserved.
 */
package weaver.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import ln.LN;

import org.apache.commons.beanutils.BeanUtils;

import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.Util;
import weaver.hrm.resource.ResourceComInfo;
import weaver.soa.hrm.HrmService;
import weaver.soa.hrm.User;

/**
 * Description: LdapUtil
 * Company: 泛微软件
 *
 * @author xiaofeng.zhang
 * @version 1.0 2005-6-16
 */

/**
 * ldap 操作工具类
 */
public class LdapUtil extends BaseBean {
    private static LdapUtil util = new LdapUtil();

    public static String DOMAIN = "ldap.domain";

    public static final String FACTORY_CLASS = "ldap.factoryclass";

    public static String PROVIDER_URL = "ldap.provider";

    public static String PRINCIPAL = "ldap.principal";

    public static String CREDENTIALS = "ldap.credentials";
    public static String logindomain = "ldap.logindomain";

    public static final String TYPE = "ldap.type";
    private String type = "";

    private LdapUtil() {
        type = Prop.getPropValue(GCONST.getConfigFile(), TYPE);
    }

    public static LdapUtil getInstance() {
        return util;
    }

    /**
     * 从ldap服务器中导出用户
     * @return
     */
    public List export() {
        try {

            if (type.equals("ad"))
                return export("(&(objectCategory=person)(objectClass=user))");
            else
                return export("objectclass=person");
        } catch (Exception e) {
            writeLog(e);
            return null;
        }
    }

    public List exportByTime(String time) {
        try {

            if (type.equals("ad"))
                return export("(&(objectCategory=person)(objectClass=user)(whenchanged>=" + time + "))");
            else
                return export("(&(objectclass=person)(modifytimestamp>=" + time + "))");
        } catch (Exception e) {
            writeLog(e);
            return null;
        }
    }

    public List export(String filter) {
        try {

            List l = new ArrayList();

            for (int j = 0; j < 1000000; j++) {
                String bfDOMAIN = "ldap";
                PROVIDER_URL = "ldap";
                PRINCIPAL = "ldap";
                CREDENTIALS = "ldap";
                logindomain = "ldap";
                if (j > 0) {
                    bfDOMAIN = bfDOMAIN + j;
                    PROVIDER_URL = PROVIDER_URL + j;
                    PRINCIPAL = PRINCIPAL + j;
                    CREDENTIALS = CREDENTIALS + j;
                    logindomain = logindomain + j;
                }
                bfDOMAIN = bfDOMAIN + ".domain";
                PROVIDER_URL = PROVIDER_URL + ".provider";
                PRINCIPAL = PRINCIPAL + ".principal";
                CREDENTIALS = CREDENTIALS + ".credentials";
                logindomain = logindomain + ".logindomain";
                String pv1 = new String(Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), bfDOMAIN)).getBytes("ISO8859-1"));

                String dc = "";

                if (Util.null2String(pv1).equals("")) {
                    break;
                } else {


                    dc = Prop.getPropValue(GCONST.getConfigFile(), logindomain);
                }


                for (int i = 0; i < 1000000; i++) {
                    String sp = "";
                    if (i > 0)
                        sp = "." + i;
                    String rnDOMAIN = bfDOMAIN + sp;
                    //System.out.println("load="+rnDOMAIN);
                    String pv = new String(Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), rnDOMAIN)).getBytes("ISO8859-1"));
                    //System.out.println("pv="+pv);
                    if (!Util.null2String(pv).equals("")) {
                        DOMAIN = rnDOMAIN;
                        try {
                            l.addAll((export(new String(Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), DOMAIN)).getBytes("ISO8859-1")), filter, j, dc)));
                        } catch (Exception e) {
                            writeLog("Ldap load error: DOMAIN=" + pv);
                            writeLog("Ldap load error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }//第二个循环的括号


            }//第一个循环的括号
            return l;
        } catch (Exception e) {
            writeLog(e);
            e.printStackTrace();
            return null;
        }


    }

    public List export(String domain, String filter, int j, String dc) throws Exception {


        domain = changeStr(domain);
        InitialDirContext context = (InitialDirContext) getInitialContext();
        Properties prop = Prop.loadTemplateProp("ldap");
        //获取配置的禁用状态值
        String isUac = Util.null2String(Prop.getPropValue("ldap_uac", "isUac"));
        String uacValue = Util.null2String(Prop.getPropValue("ldap_uac", "uacValue"));
        if (uacValue.equals("")) {
            uacValue = "514,66050";
        }
        SearchControls sc = new SearchControls();
        sc.setSearchScope(2);
        String domain_new = buildDomain(domain);//处理domain中的特殊字符
        NamingEnumeration enumeration = context.search(domain_new, filter, sc);
        writeLog("========================export [" + domain + "] begin==========================\n");
        HrmService service = new HrmService();
        try {
            service.setExp_result(new ArrayList());
            while (enumeration.hasMoreElements()) {
                LN l = new LN();
                if (l.CkHrmnum() >= 0) {  //reach the max hrm number
                    writeLog("--已到最大license数，无法导入---");
                    break;
                }
                SearchResult result = (SearchResult) enumeration.nextElement();
                Attributes attributes = result.getAttributes();

                Enumeration keys = prop.keys();
                User u = new User();
                boolean userAccountflag = true;
                u.setLdapmark(j + "");//用于标示第几个ldap域
                u.setLdap_domainName(dc);//用于域名
                int tmp = 0;
                String subcompanyname = "";
                String departmentname = "";
                String jobtitlename = "";
                String managername = "";

                //add by shaw 2017/3/29
                String tempresidentnumber = "";

                while (keys.hasMoreElements()) {
                    tmp++;
                    String col = (String) keys.nextElement();
                    writeLog("col====:" + col);
                    String attr = prop.getProperty(col);
                    writeLog("attr====:" + attr+"|"+col);
                    if (attr.indexOf("$") == 0) { //use the value from ldap
                        attr = attr.substring(1);

                        if (attr.equalsIgnoreCase("userpassword"))   //userpassword is a forbidden attribute
                            continue;
                        Attribute att = null;
                        try {
                            att = attributes.get(attr);
                        } catch (Exception e) {
                            //do nothing
                            writeLog("Ldap load error: " + e.getMessage());
                        }
                        if (att == null)
                            continue;
                        String val = (String) att.get();
                        writeLog("val====:" + val);

                        if (attr.equalsIgnoreCase("userAccountControl")) {
                            if (isUac.equals("1") && uacValue.indexOf(val) > -1) {//检查禁止帐户状态
                                userAccountflag = false;
                                break;
                            }
                        }

                        if (val == null)
                            continue;
                        if (col.equalsIgnoreCase("subcompanyid1")) {    //分部
                            subcompanyname = val;
                            writeLog("得到分部:名称，" + subcompanyname);
                        }
                        if (col.equalsIgnoreCase("departmentid")) {    //部门
                            departmentname = val;
                            val = "0";
                            writeLog("得到部门:名称，" + departmentname);
                        }
                        if (col.equalsIgnoreCase("jobtitle")) {    //岗位
                            jobtitlename = val;
                            val = "0";
                            writeLog("得到岗位:名称，" + jobtitlename);
                        }
                        if (col.equalsIgnoreCase("managerid")) {    //上级
                            managername = val;
                            val = "0";
                            writeLog("得到直接上级:" + managername);
                        }
                        if (col.equalsIgnoreCase("seclevel")) {
                            writeLog("安全级别值:" + val);
                        }

                        if (col.equalsIgnoreCase("tempresidentnumber")) {    //dept code
                            tempresidentnumber = val;
                            val = "0";
                            writeLog("get tempresidentnumber val:" + tempresidentnumber);
                        }

                        BeanUtils.setProperty(u, col, val);
                    } else { //use defaulst value from ldap.properties
                        if (col.equalsIgnoreCase("subcompanyid1")) {    //分部
                            subcompanyname = attr;
                        }
                        if (col.equalsIgnoreCase("departmentid")) {    //部门
                            departmentname = attr;
                            attr = "0";
                        }
                        if (col.equalsIgnoreCase("jobtitle")) {    //岗位
                            jobtitlename = attr;
                            attr = "0";
                            writeLog("得到岗位:名称，" + attr);
                        }
                        if (col.equalsIgnoreCase("managerid")) {    //上级
                            managername = attr;
                            attr = "0";
                            writeLog("得到直接上级:" + attr);
                        }

                        /* add by shaw 2017/3/29 dept code  */
                        if (col.equalsIgnoreCase("tempresidentnumber")) {    //dept code
                            tempresidentnumber = attr;
                            attr = "0";
                            writeLog("tempresidentnumber:attr=" + attr);
                        }

                        BeanUtils.setProperty(u, col, attr);
                    }
                }
                writeLog("userAccountflag:" + userAccountflag + ",name:" + u.getLastname() + ",accout:" + u.getAccount());
                if (userAccountflag) {
                    //System.out.println("add user="+u.getLastname());
                    service.addUser(u, departmentname, jobtitlename, subcompanyname, managername,tempresidentnumber);
                    writeLog("updating emp:"+tempresidentnumber);
                } else {
                    writeLog("禁用AD帐号(不同步)：account=" + Util.null2String(u.getAccount()) + ", lastname=" + Util.null2String(u.getLastname()) + " AD帐户");
                }
            }
        } catch (Exception e) {

            writeLog(e.getMessage());
            writeLog(e.getLocalizedMessage());

        }

        writeLog("========================export [" + domain + "] end==========================\n");
        ResourceComInfo rci = new ResourceComInfo();
        rci.removeResourceCache();
        context.close();
        return service.getExp_result();
    }

    /**
     * get accounts from ldap
     *
     * @param filter
     * @return account list
     * @throws Exception
     */
    public List getAccounts(String filter) throws Exception {
        //获取配置的禁用状态值
        String isUac = Util.null2String(Prop.getPropValue("ldap_uac", "isUac"));
        String uacValue = Util.null2String(Prop.getPropValue("ldap_uac", "uacValue"));
        InitialDirContext context = (InitialDirContext) getInitialContext();
        Properties prop = Prop.loadTemplateProp("ldap");
        Enumeration keys = prop.keys();
        String fieldname = "account";
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.equals("account")) {
                fieldname = prop.getProperty(key);
                fieldname = fieldname.substring(1);
                break;
            }
        }
        if (!filter.equals("*"))
            filter = "*" + filter + "*";
        filter = fieldname + "=" + filter;

        //if (type.equals("ad"))
        //filter = "(&(objectCategory=person)(objectClass=user)("+filter+"))";
        //  else
        //filter = "(&(objectCategory=person)("+filter+"))";
        if (type.equalsIgnoreCase("ad")) {
            filter = "(&(objectCategory=person)(objectClass=user)(" + filter + "))";
        } else if (type.equalsIgnoreCase("OpenLDAP")) {

        } else {
            filter = "(&(objectCategory=person)(" + filter + "))";
        }

        SearchControls sc = new SearchControls();
        sc.setSearchScope(2);
        ArrayList list = new ArrayList();
        String bfDOMAIN = "ldap.domain";
        for (int i = 0; i < 1000000; i++) {
            String sp = "";
            if (i > 0)
                sp = "." + i;
            String rnDOMAIN = bfDOMAIN + sp;
            String pv = Prop.getPropValue(GCONST.getConfigFile(), rnDOMAIN);
            pv = changeStr(pv);
            if (!Util.null2String(pv).equals("")) {
                DOMAIN = rnDOMAIN;
                try {
                    NamingEnumeration iter = context.search(new String(Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), DOMAIN)).getBytes("ISO8859-1")), filter, sc);
                    while (iter.hasMoreElements()) {
                        SearchResult result = (SearchResult) iter.nextElement();
                        Attributes attributes = result.getAttributes();
                        String account = (String) attributes.get(fieldname).get();

                        String userAccountControl = (String) attributes.get("userAccountControl").get();
                        if (isUac.equals("1") && uacValue.indexOf(userAccountControl) > -1) {//检查禁止帐户状态
                            continue;
                        }
                        //在ldap.domain中配置的ou如果有包含关系，去除重复账号
                        if (!list.contains(account)) {
                            list.add(account);
                        }
                    }
                } catch (Exception e) {
                    writeLog("Ldap getAccounts error: DOMAIN=" + DOMAIN);
                    System.out.println("Ldap getAccounts error: DOMAIN=" + DOMAIN);
                }
            } else {
                break;
            }
        }

        context.close();
        Collections.sort(list, new Comparator() {
            public int compare(Object o, Object o1) {
                return ((String) o).compareTo((String) o1);  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        return list;
    }

    public List getAccounts(String filter, String ldapmark) throws Exception {
        //获取配置的禁用状态值
        String isUac = Util.null2String(Prop.getPropValue("ldap_uac", "isUac"));
        String uacValue = Util.null2String(Prop.getPropValue("ldap_uac", "uacValue"));
        InitialDirContext context = (InitialDirContext) getInitialContext();
        Properties prop = Prop.loadTemplateProp("ldap");
        Enumeration keys = prop.keys();
        String fieldname = "account";
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.equals("account")) {
                fieldname = prop.getProperty(key);
                fieldname = fieldname.substring(1);
                break;
            }
        }
        if (!filter.equals("*"))
            filter = "*" + filter + "*";
        filter = fieldname + "=" + filter;

        //if (type.equals("ad"))
        //filter = "(&(objectCategory=person)(objectClass=user)("+filter+"))";
        //  else
        //filter = "(&(objectCategory=person)("+filter+"))";
        if (type.equalsIgnoreCase("ad")) {
            filter = "(&(objectCategory=person)(objectClass=user)(" + filter + "))";
        } else if (type.equalsIgnoreCase("OpenLDAP")) {

        } else {
            filter = "(&(objectCategory=person)(" + filter + "))";
        }

        SearchControls sc = new SearchControls();
        sc.setSearchScope(2);
        ArrayList list = new ArrayList();
        if (ldapmark.equals("0")) {
            ldapmark = "";
        }
        String bfDOMAIN = "ldap" + ldapmark + ".domain";
        for (int i = 0; i < 1000000; i++) {
            String sp = "";
            if (i > 0)
                sp = "." + i;
            String rnDOMAIN = bfDOMAIN + sp;
            String pv = Prop.getPropValue(GCONST.getConfigFile(), rnDOMAIN);
            pv = changeStr(pv);
            if (!Util.null2String(pv).equals("")) {
                DOMAIN = rnDOMAIN;
                try {
                    NamingEnumeration iter = context.search(new String(Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), DOMAIN)).getBytes("ISO8859-1")), filter, sc);
                    while (iter.hasMoreElements()) {
                        SearchResult result = (SearchResult) iter.nextElement();
                        Attributes attributes = result.getAttributes();
                        String account = (String) attributes.get(fieldname).get();

                        String userAccountControl = (String) attributes.get("userAccountControl").get();
                        if (isUac.equals("1") && uacValue.indexOf(userAccountControl) > -1) {//检查禁止帐户状态
                            continue;
                        }
                        //在ldap.domain中配置的ou如果有包含关系，去除重复账号
                        if (!list.contains(account)) {
                            list.add(account);
                        }
                    }
                } catch (Exception e) {
                    writeLog("Ldap getAccounts error: DOMAIN=" + DOMAIN);
                }
            } else {
                break;
            }
        }

        context.close();
        Collections.sort(list, new Comparator() {
            public int compare(Object o, Object o1) {
                return ((String) o).compareTo((String) o1);  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        return list;
    }

    /**
     * ldap login verify
     *
     * @param account
     * @param password
     * @return success or not
     */

    public boolean authentic(String account, String password) {
        boolean checkStatus = false;
        String bfDOMAIN = "ldap.domain";
        for (int i = 0; i < 1000000; i++) {
            String sp = "";
            if (i > 0)
                sp = "." + i;
            String rnDOMAIN = bfDOMAIN + sp;
            String pv = Prop.getPropValue(GCONST.getConfigFile(), rnDOMAIN);
            pv = changeStr(pv);
            if (!Util.null2String(pv).equals("")) {
                DOMAIN = rnDOMAIN;
                if (type.equals("ad"))
                    checkStatus = authentic4AD(account, password);
                else
                    checkStatus = authentic4iPlanet(account, password);
                if (checkStatus) break;
            } else {
                break;
            }
        }
//      if (type.equals("ad"))
//      return checkStatus;//authentic4AD(account,password);
//        else
//      return authentic4iPlanet(account,password);
        return checkStatus;
    }

    public boolean authentic(String account, String password, String ldapmark) {

        boolean checkStatus = false;
        if (account.indexOf("\\") != -1) {
            account = account.substring(account.indexOf("\\") + 1);
        }
        String bfDOMAIN = "ldap";
        PROVIDER_URL = "ldap";
        PRINCIPAL = "ldap";
        CREDENTIALS = "ldap";
        if (ldapmark.equals("") || ldapmark.equals("0")) {
            bfDOMAIN = "ldap.domain";
            PROVIDER_URL = "ldap.provider";
            PRINCIPAL = "ldap.principal";
            CREDENTIALS = "ldap.credentials";

        } else {

            bfDOMAIN = "ldap" + ldapmark + ".domain";
            PROVIDER_URL = "ldap" + ldapmark + ".provider";
            PRINCIPAL = "ldap" + ldapmark + ".principal";
            CREDENTIALS = "ldap" + ldapmark + ".credentials";

        }

        for (int i = 0; i < 1000000; i++) {
            String sp = "";
            if (i > 0)
                sp = "." + i;
            String rnDOMAIN = bfDOMAIN + sp;
            String pv = Prop.getPropValue(GCONST.getConfigFile(), rnDOMAIN);
            pv = changeStr(pv);
            if (!Util.null2String(pv).equals("")) {
                DOMAIN = rnDOMAIN;
                if (type.equals("ad"))
                    checkStatus = authentic4AD(account, password);
                else
                    checkStatus = authentic4iPlanet(account, password);
                if (checkStatus) break;
            } else {
                break;
            }
        }
//      if (type.equals("ad"))
//      return checkStatus;//authentic4AD(account,password);
//        else
//      return authentic4iPlanet(account,password);
        return checkStatus;
    }

    private InitialContext getInitialContext() throws Exception {

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, Prop.getPropValue(GCONST.getConfigFile(), FACTORY_CLASS));
        env.put(Context.PROVIDER_URL, Prop.getPropValue(GCONST.getConfigFile(), PROVIDER_URL));
        env.put(Context.REFERRAL, "follow");

        String principal = new String(Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), PRINCIPAL)).getBytes("ISO8859-1"));
        principal = buildPrincipal(principal);//处理账号中的特殊字符
        if (type.equals("ad")) {
            String domain = new String(Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), DOMAIN)).getBytes("ISO8859-1"));
            domain = changeStr(domain);
            //System.out.println("domain="+domain);
            String[] arr_baseDN = Util.TokenizerString2(domain, ",");
            String baseDN = "";
            for (int i = 0; i < arr_baseDN.length; i++) {
                if (arr_baseDN[i].indexOf("dc=") < 0)
                    continue;
                if (!baseDN.equals(""))
                    baseDN = baseDN + "," + arr_baseDN[i];
                else
                    baseDN += arr_baseDN[i];
            }
            baseDN = buildDomain(baseDN);//处理domain中的特殊字符
            if (principal.toUpperCase().indexOf("CN=") < 0 && principal.toUpperCase().indexOf(",OU=") < 0 && principal.toUpperCase().indexOf(",DC=") < 0) {  //原来的配置，默认在users组下
                env.put(Context.SECURITY_PRINCIPAL, "cn=" + principal + ",cn=users," + baseDN);
            } else {
                env.put(Context.SECURITY_PRINCIPAL, principal);
            }
        } else
            env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, Prop.getPropValue(GCONST.getConfigFile(), CREDENTIALS));
        InitialDirContext initialContext = new InitialDirContext(env);
        return initialContext;
    }

    public boolean authentic4AD(String account, String password) {
        try {
            InitialDirContext initialContext = (InitialDirContext) getInitialContext();
            SearchControls sc = new SearchControls();
            sc.setSearchScope(2);
            String domain = new String(Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), DOMAIN)).getBytes("ISO8859-1"));
            domain = changeStr(domain);
            domain = buildDomain(domain);
            /*String[] arr_baseDN = Util.TokenizerString2(domain, ",");
            String baseDN = "";
            for (int i = 0; i < arr_baseDN.length; i++) {
                if(arr_baseDN[i].indexOf("dc")<0)
                continue;
                if (!baseDN.equals(""))
                    baseDN = "." + baseDN + arr_baseDN[i].substring(arr_baseDN[i].indexOf("=") + 1);
                else
                    baseDN += baseDN + arr_baseDN[i];
            }
            String userprincipalname = account + "@" + baseDN;*/
            NamingEnumeration iter = initialContext.search(domain, "(&(samaccountname=" + account + ")(objectclass=user))", sc);
            if (iter.hasMoreElements()) {
                SearchResult result = (SearchResult) iter.nextElement();
                Attributes attributes = result.getAttributes();
                Attribute att = attributes.get("distinguishedname");
                String dn = (String) att.get();
                Hashtable env = new Hashtable();
                env.put(Context.INITIAL_CONTEXT_FACTORY, Prop.getPropValue(GCONST.getConfigFile(), LdapUtil.FACTORY_CLASS));
                env.put(Context.PROVIDER_URL, Prop.getPropValue(GCONST.getConfigFile(), LdapUtil.PROVIDER_URL));
                env.put(Context.SECURITY_PRINCIPAL, dn);
                env.put(Context.SECURITY_CREDENTIALS, password);
                env.put(Context.REFERRAL, "follow");
                //dn = buildPrincipal(dn);
                InitialDirContext initialContext1 = new InitialDirContext(env);
                //initialContext1.search(dn, null);
                initialContext1.close();

            } else {
                initialContext.close();
                return false;
            }
            initialContext.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean authentic4iPlanet(String account, String password) {
        try {
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, Prop.getPropValue(GCONST.getConfigFile(), FACTORY_CLASS));
            env.put(Context.PROVIDER_URL, Prop.getPropValue(GCONST.getConfigFile(), PROVIDER_URL));
            env.put(Context.SECURITY_PRINCIPAL, "uid=" + account + "," + new String(Util.null2String(changeStr(Prop.getPropValue(GCONST.getConfigFile(), DOMAIN))).getBytes("ISO8859-1")));
            env.put(Context.SECURITY_CREDENTIALS, password);
            env.put(Context.REFERRAL, "follow");

            InitialDirContext initialContext = new InitialDirContext(env);
            initialContext.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 全部转换成小写，全角转半角
     *
     * @str string
     * @return newString
     */
    public static String changeStr(String str) {
        str = str.trim();
        str = str.toLowerCase();
        char c[] = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    /**
     * 全角转半角,判断有没有包含特殊字符  ：/  \ # +  ; " , <>  = 空格
     * @param str
     * @return
     */
    public static boolean CheckDomain(String str) {
        String str1 = str;
        boolean bl = false;
        char c[] = str1.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);
            }
        }
        str1 = new String(c);
        bl = str1.matches(".*[\\x2f\\x5c\\x23\\x2b\\x3b\\x22\\x2c\\x3c\\x3e\\x3d\\x20\\x60].*");
        return bl;

    }

    /**
     * 处理domain中的特殊字符
     * @param domain
     * @return
     */
    public String buildDomain(String domain) {
        String result = procSpecialChar(domain);
        result = result.replaceAll("ou\\\\=", "ou=").replaceAll("dc\\\\=", "dc=");
        result = result.replaceAll("\\\\,ou=", ",ou=").replaceAll("\\\\,dc=", ",dc=");

        result = result.replaceAll("OU\\\\=", "OU=").replaceAll("DC\\\\=", "DC=");
        result = result.replaceAll("\\\\,OU=", ",OU=").replaceAll("\\\\,DC=", ",DC=");

        result = result.replaceAll("\\\"", "\\\\\\\"");//处理双引号


        return result;
    }

    /**
     * 处理账号（dn）中的特殊字符
     * @param principal
     * @return
     */
    public String buildPrincipal(String principal) {
        String result = procSpecialChar(principal);
        result = result.replaceAll("cn\\\\=", "cn=").replaceAll("ou\\\\=", "ou=").replaceAll("dc\\\\=", "dc=");
        result = result.replaceAll("\\\\,cn=", ",cn=").replaceAll("\\\\,ou=", ",ou=").replaceAll("\\\\,dc=", ",dc=");

        result = result.replaceAll("CN\\\\=", "CN=").replaceAll("OU\\\\=", "OU=").replaceAll("DC\\\\=", "DC=");
        result = result.replaceAll("\\\\,CN=", ",CN=").replaceAll("\\\\,OU=", ",OU=").replaceAll("\\\\,DC=", ",DC=");

        result = result.replaceAll("\\\"", "\\\\\\\"");//处理双引号

        return result;
    }

    /**
     * 处理特殊字符
     * @param principal
     * @return
     */
    private String procSpecialChar(String principal) {
        // 特殊字符列表
        // 34 35 39 43 44 47 59 60 61 62 92
        //  "  #  '  +  ,  /  ;  <  =  >  \
        char[] ccArray = new char[]{'"', '#', '\'', '+', ',', '/', ';', '<', '=', '>', '\\'};
        String ccStr = new String(ccArray);

        StringBuilder sb = new StringBuilder();
        principal = principal.replaceAll("\\\\", "\\\\\\\\");
        char[] domainCharArray = principal.toCharArray();
        for (int i = 0; i < domainCharArray.length; i++) {
            String cs = new String(new char[]{domainCharArray[i]});
            if (ccStr.indexOf(cs) > -1) {
                sb.append("\\");
            }

            sb.append(domainCharArray[i]);
        }

        String result = sb.toString();
        return result;
    }

    public Map<String, String> getDomainName() {
        Map<String, String> domainmap = new HashMap<String, String>();
        String field = "ldap.logindomain";
        domainmap.put("0", Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), field)));
        String temp = "ldap";
        for (int i = 1; i < 100; i++) {
            temp = temp + i + ".logindomain";
            String temp1 = Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), temp));
            if (!temp1.equals("")) {
                domainmap.put(i + "", Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), temp)));
            }
        }

        return domainmap;

    }


    /**
     * check account from ldap
     *
     * @param filter
     * @return account list
     * @throws Exception
     */
    public boolean checkAccount(String filter, String ldapmark) throws Exception {
        //获取配置的禁用状态值
        String isUac = Util.null2String(Prop.getPropValue("ldap_uac", "isUac"));
        String uacValue = Util.null2String(Prop.getPropValue("ldap_uac", "uacValue"));
        InitialDirContext context = (InitialDirContext) getInitialContext(ldapmark);
        Properties prop = Prop.loadTemplateProp("ldap");
        Enumeration keys = prop.keys();
        String fieldname = "account";
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.equals("account")) {
                fieldname = prop.getProperty(key);
                fieldname = fieldname.substring(1);
                break;
            }
        }
        //if (!filter.equals("*"))
        //filter = "*" + filter + "*";
        filter = fieldname + "=" + filter;

        //if (type.equals("ad"))
        //filter = "(&(objectCategory=person)(objectClass=user)("+filter+"))";
        //  else
        //filter = "(&(objectCategory=person)("+filter+"))";
        if (type.equalsIgnoreCase("ad")) {
            filter = "(&(objectCategory=person)(objectClass=user)(" + filter + "))";
        } else if (type.equalsIgnoreCase("OpenLDAP")) {

        } else {
            filter = "(&(objectCategory=person)(" + filter + "))";
        }

        SearchControls sc = new SearchControls();
        sc.setSearchScope(2);
        // ArrayList list = new ArrayList();
        boolean ll = false;
        String bfDOMAIN = "ldap";
        if (!ldapmark.equals("") && !ldapmark.equals("0")) {
            bfDOMAIN += ldapmark;
        }
        bfDOMAIN += ".domain";
        for (int i = 0; i < 10000; i++) {
            String sp = "";
            if (i > 0)
                sp = "." + i;
            String rnDOMAIN = bfDOMAIN + sp;
            String pv = Prop.getPropValue(GCONST.getConfigFile(), rnDOMAIN);
            pv = changeStr(pv);
            if (!Util.null2String(pv).equals("")) {
                DOMAIN = rnDOMAIN;
                try {
                    NamingEnumeration iter = context.search(new String(Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), DOMAIN)).getBytes("ISO8859-1")), filter, sc);
                    while (iter.hasMoreElements()) {
                        SearchResult result = (SearchResult) iter.nextElement();
                        Attributes attributes = result.getAttributes();
                        String account = (String) attributes.get(fieldname).get();

                        String userAccountControl = (String) attributes.get("userAccountControl").get();
                        if (isUac.equals("1") && uacValue.indexOf(userAccountControl) > -1) {//检查禁止帐户状态
                            continue;
                        }
                        ll = true;
                        break;
                        // list.add(account);
                    }
                } catch (Exception e) {
                    writeLog("Ldap getAccounts error: DOMAIN=" + DOMAIN);
                    System.out.println("Ldap getAccounts error: DOMAIN=" + DOMAIN);
                }
            } else {
                break;
            }
            if (ll) {
                break;
            }
        }

        context.close();

        return ll;
    }

    private InitialContext getInitialContext(String ldapmark) throws Exception {
        if (ldapmark.equals("0")) {
            ldapmark = "";
        }
        String provider_url = "ldap" + ldapmark + ".provider";

        String principaltemp = "ldap" + ldapmark + ".principal";

        String credentials = "ldap" + ldapmark + ".credentials";
        String domaintemp = "ldap" + ldapmark + ".domain";

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, Prop.getPropValue(GCONST.getConfigFile(), FACTORY_CLASS));
        env.put(Context.PROVIDER_URL, Prop.getPropValue(GCONST.getConfigFile(), provider_url));
        env.put(Context.REFERRAL, "follow");

        String principal = new String(Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), principaltemp)).getBytes("ISO8859-1"));
        principal = buildPrincipal(principal);//处理账号中的特殊字符
        if (type.equals("ad")) {
            String domain = new String(Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), domaintemp)).getBytes("ISO8859-1"));
            domain = changeStr(domain);
            //System.out.println("domain="+domain);
            String[] arr_baseDN = Util.TokenizerString2(domain, ",");
            String baseDN = "";
            for (int i = 0; i < arr_baseDN.length; i++) {
                if (arr_baseDN[i].indexOf("dc=") < 0)
                    continue;
                if (!baseDN.equals(""))
                    baseDN = baseDN + "," + arr_baseDN[i];
                else
                    baseDN += arr_baseDN[i];
            }
            baseDN = buildDomain(baseDN);//处理domain中的特殊字符
            if (principal.toUpperCase().indexOf("CN=") < 0 && principal.toUpperCase().indexOf(",OU=") < 0 && principal.toUpperCase().indexOf(",DC=") < 0) {  //原来的配置，默认在users组下
                env.put(Context.SECURITY_PRINCIPAL, "cn=" + principal + ",cn=users," + baseDN);
            } else {
                env.put(Context.SECURITY_PRINCIPAL, principal);
            }
        } else
            env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, Prop.getPropValue(GCONST.getConfigFile(), credentials));
        InitialDirContext initialContext = new InitialDirContext(env);
        return initialContext;
    }


}
