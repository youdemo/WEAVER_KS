-- 一级部门 oracle
create or replace function htkj_supdepid_dept(v_dept int)
return int
as
       v_supdepid int;
       v_i int;
       v_sup_dept int;
       v_tmp_dept int;
begin
       v_i:=0;
       v_tmp_dept:=v_dept;
       while v_i<=5 loop
             select supdepid into v_supdepid from HrmDepartment where id=v_tmp_dept and nvl(canceled,0)<>1;
             if v_supdepid=0 then
                v_sup_dept:=v_tmp_dept; 
                v_i:=10;
             else
                    v_tmp_dept:=v_supdepid;
                    v_i:=v_i+1;
             end if;
       
       end loop;
return v_sup_dept;
end;