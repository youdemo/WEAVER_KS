create or replace view s_online_list as
select operateuserid,ceil((sysdate - to_date(operatedate||' '||operatetime,'yyyy-mm-dd hh24:mi:ss'))*24*60) 
    as logtime from HrmSysMaintenanceLog 
    where id in ( select max(id) as id from HrmSysMaintenanceLog group by operateuserid )  