--Oracle  DB

select r.loginid 用户名,r.password 密码,r.lastname 姓名,d.departmentname 部门,j.jobtitlename 职位 ,
r.email 邮箱,r.mobile 手机
from HrmResource r,Hrmdepartment d,HrmJobTitles  j 
where r.departmentid=d.id and  r.jobtitle=j.id
and r.status<5