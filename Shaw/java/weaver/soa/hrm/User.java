package weaver.soa.hrm;


import weaver.general.Util;

/**
 * Description: User
 * Company: ·ºÎ¢Èí¼þ
 *
 * @author xiaofeng.zhang
 * @version 1.0 2005-6-6
 */
public class User {
    private int id=-1;
    private String loginid;//             VARCHAR2(60 BYTE),
    private String lastname;           // VARCHAR2(60 BYTE),
    private String sex;           //   CHAR(1 BYTE),
    private String birthday;//           CHAR(10 BYTE),
    private int nationality=-1;//         INTEGER,
    private int systemlanguage=7;//      INTEGER,
    private String maritalstatus;//MARITALSTATUS       CHAR(1 BYTE),
    private String telephone;//TELEPHONE           VARCHAR2(60 BYTE),
    private String mobile;//MOBILE              VARCHAR2(60 BYTE),
    private String mobilecall;//MOBILECALL          VARCHAR2(60 BYTE),
    private String email;//EMAIL               VARCHAR2(60 BYTE),
    private int locationid=-1;//LOCATIONID          INTEGER,
    private String workroom;//WORKROOM            VARCHAR2(60 BYTE),
    private String homeaddress;//HOMEADDRESS         VARCHAR2(100 BYTE),
    private String resourcetype;//RESOURCETYPE        CHAR(1 BYTE),
    private String startdate;//STARTDATE           CHAR(10 BYTE),
    private String enddate;//ENDDATE             CHAR(10 BYTE),
    private int jobtitle=-1;//JOBTITLE            INTEGER,
    private String jobactivitydesc;//JOBACTIVITYDESC     VARCHAR2(200 BYTE),
    private int joblevel=-1;//JOBLEVEL            INTEGER,
    private int seclevel=10;//SECLEVEL            INTEGER,
    private int departmentid=-1;//DEPARTMENTID        INTEGER,
//    private String subcompanyid1;//SUBCOMPANYID1       INTEGER,
    private int costcenterid=1 ;//COSTCENTERID        INTEGER,
    private int managerid=-1;//MANAGERID           INTEGER,
    private int assistantid=-1;//ASSISTANTID         INTEGER,
    private int bankid1=-1;//BANKID1             INTEGER,
    private String accountid1;//ACCOUNTID1          VARCHAR2(100 BYTE),
//private  String  RESOURCEIMAGEID     INTEGER,
    private int createrid=-1;//CREATERID           INTEGER,
    private String createdate;// CREATEDATE          CHAR(10 BYTE),
    private int lastmodid=-1;//LASTMODID           INTEGER,
    private String lastmoddate;//LASTMODDATE         CHAR(10 BYTE),
    //private String lastlogindate;//LASTLOGINDATE       CHAR(10 BYTE),
//private  String  DATEFIELD1          VARCHAR2(10 BYTE),
//private  String  DATEFIELD2          VARCHAR2(10 BYTE),
//private  String  DATEFIELD3          VARCHAR2(10 BYTE),
//private  String  DATEFIELD4          VARCHAR2(10 BYTE),
//private  String  DATEFIELD5          VARCHAR2(10 BYTE),
//private  String  NUMBERFIELD1        FLOAT(126),
//private  String  NUMBERFIELD2        FLOAT(126),
//private  String  NUMBERFIELD3        FLOAT(126),
//private  String  NUMBERFIELD4        FLOAT(126),
//private  String  NUMBERFIELD5        FLOAT(126),
//private  String  TEXTFIELD1          VARCHAR2(100 BYTE),
//private  String  TEXTFIELD2          VARCHAR2(100 BYTE),
//private  String  TEXTFIELD3          VARCHAR2(100 BYTE),
//private  String  TEXTFIELD4          VARCHAR2(100 BYTE),
//private  String  TEXTFIELD5          VARCHAR2(100 BYTE),
//private  String  TINYINTFIELD1       INTEGER,
//private  String  TINYINTFIELD2       INTEGER,
//private  String  TINYINTFIELD3       INTEGER,
//private  String  TINYINTFIELD4       INTEGER,
//private  String  TINYINTFIELD5       INTEGER,
    private String certificatenum;//CERTIFICATENUM      VARCHAR2(60 BYTE),
    private String nativeplace;//NATIVEPLACE         VARCHAR2(100 BYTE),
    private int educationlevel=-1;//EDUCATIONLEVEL      INTEGER,
    private String bememberdate;//BEMEMBERDATE        CHAR(10 BYTE),
    private String departydate;//BEPARTYDATE         CHAR(10 BYTE),
    private String workcode;//WORKCODE            VARCHAR2(60 BYTE),
    private String regresidentplace;//REGRESIDENTPLACE    VARCHAR2(60 BYTE),
    private String healthinfo;//HEALTHINFO          CHAR(1 BYTE),
    private String residentplace;//RESIDENTPLACE       VARCHAR2(60 BYTE),
    private String policy;//POLICY              VARCHAR2(30 BYTE),
    private String degree;//DEGREE              VARCHAR2(30 BYTE),
    private String height;//HEIGHT              VARCHAR2(10 BYTE),
    private int usekind=-1;//USEKIND             INTEGER,
    private int jobcall=-1;//JOBCALL             INTEGER,
    private String accumfundaccount;//ACCUMFUNDACCOUNT    VARCHAR2(30 BYTE),
    private String birthplace;//BIRTHPLACE          VARCHAR2(60 BYTE),
    private String folk;//FOLK                VARCHAR2(30 BYTE),
    private String residentphone;//RESIDENTPHONE       VARCHAR2(60 BYTE),
    private String residentpostcode;//RESIDENTPOSTCODE    VARCHAR2(60 BYTE),
    private String extphone;//EXTPHONE            VARCHAR2(50 BYTE),
    private int dsporder=-1;//DSPORDER            INTEGER,
//    private String managerstr;//MANAGERSTR          VARCHAR2(200 BYTE),
    private int status=0;//STATUS              INTEGER,
    private String fax;//FAX                 VARCHAR2(60 BYTE),
    private String islabouunion;//ISLABOUUNION        CHAR(1 BYTE),
    private int weight=-1;//WEIGHT              INTEGER,
    private String tempresidentnumber;//TEMPRESIDENTNUMBER  VARCHAR2(60 BYTE),
    private String probationenddate;//PROBATIONENDDATE    CHAR(10 BYTE),
    private String countryid;//COUNTRYID           INTEGER                   DEFAULT 1,
//private  String  PASSWDCHGDATE       CHAR(10 BYTE),
//private  String  NEEDUSB             INTEGER,
//private  String  SERIAL              CHAR(10 BYTE),
    private String account;//ACCOUNT             VARCHAR2(100 BYTE)
    private String password;
    
    private String ldapmark;
    private String ldap_domainName;


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = Util.getEncrypt(password);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLoginid() {
        return loginid;
    }

    public void setLoginid(String loginid) {
        this.loginid = loginid;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = "0";
        if(sex.equalsIgnoreCase("ÄÐ") )
        this.sex = "0";
        if(sex.equalsIgnoreCase("Å®") )
        this.sex = "1";
        if(sex.equalsIgnoreCase("male") )
        this.sex = "0";
        if(sex.equalsIgnoreCase("female") )
        this.sex = "1";
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getNationality() {
        return nationality;
    }

    public void setNationality(int nationality) {
        this.nationality = nationality;
    }

    public int getSystemlanguage() {
        return systemlanguage;
    }

    public void setSystemlanguage(int systemlanguage) {
        this.systemlanguage = systemlanguage;
    }

    public String getMaritalstatus() {
        return maritalstatus;
    }

    public void setMaritalstatus(String maritalstatus) {
        this.maritalstatus = maritalstatus;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobilecall() {
        return mobilecall;
    }

    public void setMobilecall(String mobilecall) {
        this.mobilecall = mobilecall;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getLocationid() {
        return locationid;
    }

    public void setLocationid(int locationid) {
        this.locationid = locationid;
    }

    public String getWorkroom() {
        return workroom;
    }

    public void setWorkroom(String workroom) {
        this.workroom = workroom;
    }

    public String getHomeaddress() {
        return homeaddress;
    }

    public void setHomeaddress(String homeaddress) {
        this.homeaddress = homeaddress;
    }

    public String getResourcetype() {
        return resourcetype;
    }

    public void setResourcetype(String resourcetype) {
        this.resourcetype = resourcetype;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public int getJobtitle() {
        return jobtitle;
    }

    public void setJobtitle(int jobtitle) {
        this.jobtitle = jobtitle;
    }

    public String getJobactivitydesc() {
        return jobactivitydesc;
    }

    public void setJobactivitydesc(String jobactivitydesc) {
        this.jobactivitydesc = jobactivitydesc;
    }

    public int getJoblevel() {
        return joblevel;
    }

    public void setJoblevel(int joblevel) {
        this.joblevel = joblevel;
    }

    public int getSeclevel() {
        return seclevel;
    }

    public void setSeclevel(int seclevel) {
        this.seclevel = seclevel;
    }

    public int getDepartmentid() {
        return departmentid;
    }

    public void setDepartmentid(int departmentid) {
        this.departmentid = departmentid;
    }

    public int getManagerid() {
        return managerid;
    }

    public void setManagerid(int managerid) {
        this.managerid = managerid;
    }

    public int getAssistantid() {
        return assistantid;
    }

    public void setAssistantid(int assistantid) {
        this.assistantid = assistantid;
    }

    public int getBankid1() {
        return bankid1;
    }

    public void setBankid1(int bankid1) {
        this.bankid1 = bankid1;
    }

    public String getAccountid1() {
        return accountid1;
    }

    public void setAccountid1(String accountid1) {
        this.accountid1 = accountid1;
    }

    public int getCreaterid() {
        return createrid;
    }

    public void setCreaterid(int createrid) {
        this.createrid = createrid;
    }

    public String getCreatedate() {
        return createdate;
    }

    public void setCreatedate(String createdate) {
        this.createdate = createdate;
    }

    public int getLastmodid() {
        return lastmodid;
    }

    public void setLastmodid(int lastmodid) {
        this.lastmodid = lastmodid;
    }

    public String getLastmoddate() {
        return lastmoddate;
    }

    public void setLastmoddate(String lastmoddate) {
        this.lastmoddate = lastmoddate;
    }

    public String getCertificatenum() {
        return certificatenum;
    }

    public void setCertificatenum(String certificatenum) {
        this.certificatenum = certificatenum;
    }

    public String getNativeplace() {
        return nativeplace;
    }

    public void setNativeplace(String nativeplace) {
        this.nativeplace = nativeplace;
    }

    public int getEducationlevel() {
        return educationlevel;
    }

    public void setEducationlevel(int educationlevel) {
        this.educationlevel = educationlevel;
    }

    public String getBememberdate() {
        return bememberdate;
    }

    public void setBememberdate(String bememberdate) {
        this.bememberdate = bememberdate;
    }

    public String getDepartydate() {
        return departydate;
    }

    public void setDepartydate(String departydate) {
        this.departydate = departydate;
    }

    public String getWorkcode() {
        return workcode;
    }

    public void setWorkcode(String workcode) {
        this.workcode = workcode;
    }

    public String getRegresidentplace() {
        return regresidentplace;
    }

    public void setRegresidentplace(String regresidentplace) {
        this.regresidentplace = regresidentplace;
    }

    public String getHealthinfo() {
        return healthinfo;
    }

    public void setHealthinfo(String healthinfo) {
        this.healthinfo = healthinfo;
    }

    public String getResidentplace() {
        return residentplace;
    }

    public void setResidentplace(String residentplace) {
        this.residentplace = residentplace;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public int getUsekind() {
        return usekind;
    }

    public void setUsekind(int usekind) {
        this.usekind = usekind;
    }

    public int getJobcall() {
        return jobcall;
    }

    public void setJobcall(int jobcall) {
        this.jobcall = jobcall;
    }

    public String getAccumfundaccount() {
        return accumfundaccount;
    }

    public void setAccumfundaccount(String accumfundaccount) {
        this.accumfundaccount = accumfundaccount;
    }

    public String getBirthplace() {
        return birthplace;
    }

    public void setBirthplace(String birthplace) {
        this.birthplace = birthplace;
    }

    public String getFolk() {
        return folk;
    }

    public void setFolk(String folk) {
        this.folk = folk;
    }

    public String getResidentphone() {
        return residentphone;
    }

    public void setResidentphone(String residentphone) {
        this.residentphone = residentphone;
    }

    public String getResidentpostcode() {
        return residentpostcode;
    }

    public void setResidentpostcode(String residentpostcode) {
        this.residentpostcode = residentpostcode;
    }

    public String getExtphone() {
        return extphone;
    }

    public void setExtphone(String extphone) {
        this.extphone = extphone;
    }

    public int getDsporder() {
        return dsporder;
    }

    public void setDsporder(int dsporder) {
        this.dsporder = dsporder;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getIslabouunion() {
        return islabouunion;
    }

    public void setIslabouunion(String islabouunion) {
        this.islabouunion = islabouunion;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getTempresidentnumber() {
        return tempresidentnumber;
    }

    public void setTempresidentnumber(String tempresidentnumber) {
        this.tempresidentnumber = tempresidentnumber;
    }

    public String getProbationenddate() {
        return probationenddate;
    }

    public void setProbationenddate(String probationenddate) {
        this.probationenddate = probationenddate;
    }

    public String getCountryid() {
        return countryid;
    }

    public void setCountryid(String countryid) {
        this.countryid = countryid;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getCostcenterid() {
        return costcenterid;
    }

    public void setCostcenterid(int costcenterid) {
        this.costcenterid = costcenterid;
    }



	public String getLdapmark() {
		return ldapmark;
	}

	public void setLdapmark(String ldapmark) {
		this.ldapmark = ldapmark;
	}

	public String getLdap_domainName() {
		return ldap_domainName;
	}

	public void setLdap_domainName(String ldap_domainName) {
		this.ldap_domainName = ldap_domainName;
	}
}
