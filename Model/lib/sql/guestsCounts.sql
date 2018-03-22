-----------------------------------------------------------------------------------
-- GUESTS COUNTS
-----------------------------------------------------------------------------------------
prompt TOTAL GUEST COUNT
---------------------------------------------------------------------------------------
select count(*) from userlogins5.users where is_guest = 1;

-----------------------------------------------------------------------------------------
prompt GUESTS -MIN DATE
---------------------------------------------------------------------------------------
select min(first_access)  from userlogins5.users where is_guest = 1;

-----------------------------------------------------------------------------------------
prompt GUESTS -MAX DATE
---------------------------------------------------------------------------------------
select max(first_access)  from userlogins5.users where is_guest = 1;


-----------------------------------------------------------------------------------------
prompt #GUESTS WITH steps
---------------------------------------------------------------------------------------
select count(*) from userlogins5.users 
where is_guest = 1
and user_id in 
(select user_id from userlogins5.steps
  where is_deleted = 0
);

-----------------------------------------------------------------------------------------
prompt #GUESTS WITH strategies
---------------------------------------------------------------------------------------
select count(*) from userlogins5.users 
where is_guest = 1
and user_id in 
(select user_id from userlogins5.strategies
  where is_deleted = 0
);

-----------------------------------------------------------------------------------------
prompt #GUESTS (created AFTER 01-JAN-2016) with STEPs
---------------------------------------------------------------------------------------
select count(*) from userlogins5.users 
where is_guest = 1
and first_access > '01-JAN-16'
and user_id in 
(select user_id from userlogins5.steps
  where is_deleted = 0
);



-----------------------------------------------------------------------------------------
prompt #GUESTS  (created AFTER 01-JAN-2016) with Strategies
---------------------------------------------------------------------------------------
select count(*) from userlogins5.users 
where is_guest = 1
and first_access > '01-JAN-16'
and user_id in 
(select user_id from userlogins5.strategies
  where is_deleted = 0
);


prompt ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


-----------------------------------------------------------------------------------------
prompt GUEST USERS: how much they work
---------------------------------------------------------------------------------------

-----------------------------------------------------------------------------------------
prompt
prompt GUEST USERS: strategy count -- users count
prompt
prompt INTERESTING GUEST USERS probably have a strategy count >2 and <30
prompt
prompt
---------------------------------------------------------------------------------------
select stratcount, count(*) from
(
select sr.user_id, count(strategy_id) as stratcount
from userlogins5.users u, userlogins5.strategies sr
where sr.is_deleted = 0
  and u.user_id = sr.user_id
  and u.is_guest = 1
  and first_access > '01-JAN-16'
group by sr.user_id
order by stratcount desc  
)
group by stratcount
order by stratcount;


-----------------------------------------------------------------------------------------
prompt 
prompt GUEST USERS with steps: WHICH QUESTIONS -- step count
prompt
prompt 
prompt
prompt
---------------------------------------------------------------------------------------

select question_name || '	' || questionCount
from
(
select question_name,count(*) as questionCount
from userlogins5.users u , userlogins5.steps st
where u.user_id = st.user_id
and u.is_guest = 1
and u.first_access > '01-JAN-16'
and st.is_deleted = 0
group by st.question_name
order by  questionCount desc,question_name
);

-----------------------------------------------------------------------------------------
prompt 
prompt GUEST USERS with strategies: WHICH QUESTIONS -- step count
prompt
prompt 
prompt
prompt
---------------------------------------------------------------------------------------

select question_name || '	' || questionCount
from
(
select question_name,count(*) as questionCount
from userlogins5.users u , userlogins5.steps st
where u.user_id = st.user_id
and u.is_guest = 1
and u.first_access > '01-JAN-16'
and st.is_deleted = 0
and st.strategy_id is not NULL
group by st.question_name
order by questionCount desc,question_name
);


-----------------------------------------------------------------------------------------
prompt 
prompt GUEST USERS with strategies: how many steps in these strategies?
prompt
prompt 
prompt
prompt
---------------------------------------------------------------------------------------


select stepcount,count(*)
  from
    (
    select st.user_id,st.strategy_id, count(*) as stepcount
    from userlogins5.users u,userlogins5.steps st
    where st.strategy_id is not NULL
      and st.is_deleted = 0 
      and u.user_id = st.user_id
      and u.is_guest = 1
      and first_access > '01-JAN-16'
    group by st.user_id,st.strategy_id
    order by stepcount desc, st.user_id, st.strategy_id
   )
 group by  stepcount
 order by stepcount;


-----------------------------------------------------------------------------------------
prompt 
prompt GUEST USERS with strategies
prompt   (ONLY THOSE users WITH MORE THAN ONE strategy AND FEWER THAN 30 strategies):
prompt       how many steps in these strategies?
prompt 
prompt
prompt
---------------------------------------------------------------------------------------

select stepcount,count(*)
from
(
select sr.user_id, uu.strategycount, st.strategy_id, sr.create_time, sr.name, sr.root_step_id, count(st.step_id) as stepcount
 from userlogins5.strategies sr, 
      userlogins5.steps st,
      (  select srin.user_id,count(*) as strategycount 
           from userlogins5.strategies srin, userlogins5.users uin
          where srin.is_deleted = 0
            and srin.user_id = uin.user_id
            and uin.is_guest = 1
            and uin.first_access > '01-JAN-16'
          group by srin.user_id
      ) uu
where sr.user_id = uu.user_id 
  and st.strategy_id = sr.strategy_id 
  and st.is_deleted = 0 and sr.is_deleted = 0 
  and st.user_id = sr.user_id 
  and st.project_id = sr.project_id 
  and uu.strategycount > 1 and uu.strategycount < 30  --INTERESTING GUEST USERS
group by sr.user_id, uu.strategycount, st.strategy_id, sr.create_time, sr.name, sr.root_step_id
order by stepcount desc, sr.create_time desc --uu.strategycount desc, sr.user_id, sr.create_time
)
group by  stepcount
order by stepcount;


prompt ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
