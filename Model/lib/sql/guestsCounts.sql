---------------------------------------------------------------------------------------
-- GUESTS COUNTS -- parameter:  maxdate: eg: '01-APR-17' (April 1 2017)
-- run:  @guestsCounts 01-MAY-17   where 01-MAY-17 is the cutoff date (upper limit)
-----------------------------------------------------------------------------------------

prompt GUESTS -MIN DATE
---------------------------------------------------------------------------------------
select min(first_access)  from userlogins5.users where is_guest = 1;

-----------------------------------------------------------------------------------------
prompt GUESTS -MAX DATE
---------------------------------------------------------------------------------------
select max(first_access)  from userlogins5.users where is_guest = 1;

prompt ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``
-----------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------
prompt GUESTS STATS  with first access < '&1'
-----------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------
prompt ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``

-----------------------------------------------------------------------------------------
prompt TOTAL GUEST COUNT
---------------------------------------------------------------------------------------
select count(*) from userlogins5.users 
where is_guest = 1
and first_access < '&1';
-----------------------------------------------------------------------------------------
prompt GUEST COUNT WITH strategies
---------------------------------------------------------------------------------------
select count(*) from userlogins5.users 
where is_guest = 1
and first_access < '&1'
and user_id in 
(select user_id from userlogins5.strategies
  where is_deleted = 0
);
-----------------------------------------------------------------------------------------
prompt GUEST COUNT WITH steps but NO STRATEGIES
---------------------------------------------------------------------------------------
select count(*) from userlogins5.users 
where is_guest = 1
and first_access < '&1'
and user_id in 
(select user_id from userlogins5.steps
  where is_deleted = 0
    and strategy_id is NULL
);


prompt ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``

-----------------------------------------------------------------------------------------
prompt   GUEST count   STRATEGY count
prompt
prompt   (this number of guests with these many strategies)
prompt   (interesting GUESTS probably have a strategy count >2 and <30)
prompt
prompt
---------------------------------------------------------------------------------------
select count(*), stratcount
from
(
select sr.user_id, count(strategy_id) as stratcount
from userlogins5.users u, userlogins5.strategies sr
where sr.is_deleted = 0
  and u.user_id = sr.user_id
  and u.is_guest = 1
  and first_access < '&1'
group by sr.user_id
order by stratcount desc  
)
group by stratcount
order by stratcount;



-----------------------------------------------------------------------------------------
prompt   STRATEGY count    STEP count
prompt 
prompt  (this number of strategies have this number of steps)
prompt
---------------------------------------------------------------------------------------
select count(*),stepcount
  from
    (
    select st.user_id,st.strategy_id, count(*) as stepcount
    from userlogins5.users u,userlogins5.steps st
    where st.strategy_id is not NULL
      and st.is_deleted = 0 
      and u.user_id = st.user_id
      and u.is_guest = 1
      and first_access < '&1'
    group by st.user_id,st.strategy_id
    order by stepcount desc, st.user_id, st.strategy_id
   )
 group by stepcount
 order by stepcount;
-----------------------------------------------------------------------------------------
prompt 
prompt  same as above but now ONLY THOSE users with a strategy count >2 and <30
prompt
prompt   STRATEGY count     STEP count
prompt  (This many strategies with these many steps)
---------------------------------------------------------------------------------------
select count(*), stepcount
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
            and uin.first_access < '&1'
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


prompt ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``


-----------------------------------------------------------------------------------------
prompt QUESTIONS in strategies
prompt
prompt   STEP count    QUESTION
prompt  (this number of steps with this question)
prompt
---------------------------------------------------------------------------------------

select questionCount || '	' ||  question_name
from
(
select question_name,count(*) as questionCount
  from userlogins5.users u , userlogins5.steps st
where u.user_id = st.user_id
  and u.is_guest = 1
  and first_access < '&1'
  and st.is_deleted = 0
  and st.strategy_id is not NULL
group by st.question_name
order by questionCount desc,question_name
);
-----------------------------------------------------------------------------------------
prompt 
prompt QUESTIONS in steps that are NOT In strategies (web services)
prompt
prompt   STEP count    QUESTION
prompt  (this number of steps with this question)
prompt
prompt
---------------------------------------------------------------------------------------

select questionCount || '	' ||  question_name
from
(
select question_name,count(*) as questionCount
  from userlogins5.users u , userlogins5.steps st
where u.user_id = st.user_id
  and u.is_guest = 1
  and first_access < '&1'
  and st.is_deleted = 0
  and st.strategy_id is NULL
group by st.question_name
order by  questionCount desc,question_name
);
prompt ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``
