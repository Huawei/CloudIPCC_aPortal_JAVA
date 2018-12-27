CREATE OR REPLACE FUNCTION F_UTL_GETSKILLNAMEFORPORTAL
(
  i_Type              INT,
  i_SkillID           INT,
  i_Time_Skill        VARCHAR2,
  i_SubCCNo           INT,
  i_VDN               INT
)RETURN VARCHAR2
AS
  v_Time_Skill        VARCHAR2(200);
BEGIN

  IF (i_SkillID IS NULL) OR (i_SubCCNo IS NULL) OR (i_VDN IS NULL) THEN
    RETURN i_Time_Skill;
  END IF;

  -- if the last record is 99999, return iSkillName
  IF i_Type = 0 OR i_SkillID = 99999 THEN
    v_Time_Skill := i_Time_Skill;
  ELSE
    v_Time_Skill :=  i_Time_Skill ;
  END IF;

  RETURN v_Time_Skill;

END F_UTL_GETSKILLNAMEFORPORTAL;
/
CREATE OR REPLACE FUNCTION F_CONSOLE_AGENTHELP
(
  i_AgentNo         VARCHAR2,   -- the string of agent id
  i_WorkGroup       VARCHAR2,   -- the string of workgroup
  i_Flag            NUMBER,     -- query type(0:agentid associated with workgroup, 1:by agent id, 2:by work group)
  i_VDNUserName     VARCHAR2,
  i_VDN    NUMBER
) RETURN INTEGER
AS
  v_AgentNo         VARCHAR2(200);
  v_WorkGroup       VARCHAR2(200);
  v_Num             INTEGER;
BEGIN

  -- delete the temporary data
  DELETE FROM T_TMP_AGENTWORKGROUP WHERE USERNAME = i_VDNUserName;
  DELETE FROM T_TMP_AGENTID WHERE USERNAME = i_VDNUserName;
  COMMIT;

  -- if input parameter is null, then return
  IF (i_AgentNo IS NOT NULL OR i_WorkGroup IS NOT NULL) THEN
      -- check if the input parameter is multi-selected
       v_AgentNo := SUBSTR(i_AgentNo, 1, 50);
       v_WorkGroup := SUBSTR(i_WorkGroup, 1, 50);
  END IF;



  IF i_Flag = 1 THEN
    -- only select agent id
    IF i_AgentNo <> '0|ALL|0|0' THEN

      v_Num := F_UTL_SPLITAGENT(i_AgentNo, i_VDNUserName);
      IF v_Num <> 0 THEN
        RETURN 1;
      END IF;

    ELSE
      -- insert all the agentid which the user can query into the temporary table
      INSERT INTO T_TMP_AGENTID(
                  AGENTID,
                  AGENTNAME,
                  WORKGROUPID,
                  AGENTWORKGROUP,
                  SUBCCNO,
                  VDN,
                  USERNAME)
           SELECT DISTINCT
                  A.AGENTID,
                  NVL(A.NAME, ' '),
                  A.WORKGROUPID,
                  A.AGENTWORKGROUP,
                  A.SUBCCNO,
                  A.VDN,
                  i_VDNUserName
             FROM TAGENTINFO A, V_RPT_USERINFO B
            WHERE A.SUBCCNO  = B.SUBCCNO
              AND A.VDN      = B.VDN
              AND B.USERNAME = i_VDNUserName
              AND A.VDN = i_VDN;
        
    END IF;

  END IF;

  IF i_Flag = 2 THEN
    IF i_WorkGroup <> '0|ALL|0|0' THEN

      --split work group
      v_Num := F_UTL_SPLITGROUP(i_WorkGroup, i_VDNUserName);
      IF v_Num <> 0 THEN
        RETURN 1;
      END IF;

      -- insert all the agent id of the query workgroup into the table T_TMP_AGENTID
      -- replace workgroup id -1 which means no workgroup by 88888
      INSERT INTO T_TMP_AGENTID(
                  AGENTID,
                  AGENTNAME,
                  WORKGROUPID,
                  AGENTWORKGROUP,
                  SUBCCNO,
                  VDN,
                  USERNAME)
           SELECT DISTINCT
                  A.AGENTID,
                  NVL(A.NAME, ' '),
                  DECODE(B.WORKGROUPID, -1, 88888, B.WORKGROUPID),
                  B.AGENTWORKGROUP,
                  A.SUBCCNO,
                  A.VDN,
                  i_VDNUserName
             FROM TAGENTINFO A, T_TMP_AGENTWORKGROUP B, V_RPT_USERINFO C
            WHERE A.SUBCCNO = B.SUBCCNO
              AND A.VDN = B.VDN
              AND B.WORKGROUPID = DECODE(NVL(A.WORKGROUPID, -1), 0, -1, -1, -1, A.WORKGROUPID)
              AND B.SUBCCNO = C.SUBCCNO
              AND B.VDN = C.VDN
              AND B.USERNAME = C.USERNAME
              AND B.USERNAME = i_VDNUserName;
    ELSE
      -- insert all the agent id into temporary table, 88888 represent no work group
      INSERT INTO T_TMP_AGENTID(
                  AGENTID,
                  AGENTNAME,
                  WORKGROUPID,
                  AGENTWORKGROUP,
                  SUBCCNO,
                  VDN,
                  USERNAME)
           SELECT DISTINCT
                  A.AGENTID,
                  NVL(A.NAME, ' '),
                  NVL(C.WORKGROUPID, 88888),
                  NVL(C.WORKGROUP, F_UTL_GETLOCALEDESC('noworkgroup')),
                  A.SUBCCNO,
                  A.VDN,
                  i_VDNUserName
             FROM TAGENTINFO A, V_RPT_USERINFO B,TWORKGROUP C
            WHERE A.SUBCCNO  = B.SUBCCNO
              AND A.VDN      = B.VDN
              AND A.WORKGROUPID = C.WORKGROUPID(+)
              /*begin modified by li,anzhou,lKF56491,2012-2-2 20:23:49*/
              -- add outer join for associated condition,add the record for no workgroup
              -- AND A.SUBCCNO  = C.SUBCCNO
              -- AND A.VDN      = C.VDN
              AND A.SUBCCNO  = C.SUBCCNO(+)
              AND A.VDN      = C.VDN(+)
              /*end modified by li,anzhou,lKF56491,2012-2-2 20:23:49*/
              AND B.USERNAME = i_VDNUserName;
    END IF;

  END IF;

  IF i_Flag = 0 THEN

    -- case 1: agent id is multi-selected, split the agent id
    IF (v_AgentNo <> '0|ALL|0|0' AND v_WorkGroup <> '0|ALL|0|0')
       OR (v_AgentNo <> '0|ALL|0|0' AND v_WorkGroup = '0|ALL|0|0') THEN

      v_Num := F_UTL_SPLITAGENT(i_AgentNo, i_VDNUserName);
      IF v_Num <> 0 THEN
        RETURN 1;
      END IF;

    END IF;

    -- case 2: 1.if workgroup and agent id are default value 0|ALL|0|0, only split the agent id
    --         2.if selected work group include 0|ALL|0|0, insert all the agent id
    IF (v_AgentNo = '0|ALL|0|0' AND (v_WorkGroup = '0|ALL|0|0' OR INSTR(i_WorkGroup, '0|ALL|0|0') > 0)) THEN

      -- insert all the agent id into temporary table, replace the id -1 means no workgroup by 88888
      INSERT INTO T_TMP_AGENTID(
                  AGENTID,
                  AGENTNAME,
                  WORKGROUPID,
                  AGENTWORKGROUP,
                  SUBCCNO,
                  VDN,
                  USERNAME)
           SELECT DISTINCT
                  A.AGENTID,
                  NVL(A.NAME, ' '),
                  NVL(C.WORKGROUPID, 88888),
                  NVL(C.WORKGROUP, F_UTL_GETLOCALEDESC('noworkgroup')),
                  A.SUBCCNO,
                  A.VDN,
                  i_VDNUserName
             FROM TAGENTINFO A, V_RPT_USERINFO B, TWORKGROUP C
            WHERE A.SUBCCNO  = B.SUBCCNO
              AND A.VDN      = B.VDN
              AND A.WORKGROUPID = C.WORKGROUPID(+)
              /*begin modified by li,anzhou,lKF56491,2012-2-2 20:23:49*/
              -- add outer join for associated condition,add the record for no workgroup
              -- AND A.SUBCCNO  = C.SUBCCNO
              -- AND A.VDN      = C.VDN
              AND A.SUBCCNO  = C.SUBCCNO(+)
              AND A.VDN      = C.VDN(+)
              /*end modified by li,anzhou,lKF56491,2012-2-2 20:23:49*/
              AND B.USERNAME = i_VDNUserName
              AND A.Vdn = i_VDN;

    END IF;

    -- case 3: workgroup is multi-selected and not include 0|ALL|0|0, agent id is default,then split the workgroup
    IF (v_AgentNo = '0|ALL|0|0' AND (v_WorkGroup <> '0|ALL|0|0' AND INSTR(i_WorkGroup, '0|ALL|0|0') = 0)) THEN

      --split work group
      v_Num := F_UTL_SPLITGROUP(i_WorkGroup, i_VDNUserName);
      IF v_Num <> 0 THEN
        RETURN 1;
      END IF;

      -- insert all the agent id of the workgroup into the table T_TMP_AGENTID, replace the workgroup id -1 by 88888
      INSERT INTO T_TMP_AGENTID(
                  AGENTID,
                  AGENTNAME,
                  WORKGROUPID,
                  AGENTWORKGROUP,
                  SUBCCNO,
                  VDN,
                  USERNAME)
           SELECT DISTINCT
                  A.AGENTID,
                  NVL(A.NAME, ' '),
                  DECODE(B.WORKGROUPID, -1, 88888, B.WORKGROUPID),
                  B.AGENTWORKGROUP,
                  A.SUBCCNO,
                  A.VDN,
                  i_VDNUserName
             FROM TAGENTINFO A, T_TMP_AGENTWORKGROUP B, V_RPT_USERINFO C
            WHERE A.SUBCCNO = B.SUBCCNO
              AND A.VDN = B.VDN
              -- if workgroup id in the tagentinfo is null or 0, replace it by -1
              AND B.WORKGROUPID = DECODE(NVL(A.WORKGROUPID, -1), 0, -1, -1, -1, A.WORKGROUPID)
              AND B.SUBCCNO = C.SUBCCNO
              AND B.VDN = C.VDN
              AND B.USERNAME = C.USERNAME
              AND B.USERNAME = i_VDNUserName;

    END IF;

  END IF;

  COMMIT;
  RETURN 0;

EXCEPTION
  WHEN OTHERS THEN
    ROLLBACK;
    RETURN 1;

END F_CONSOLE_AGENTHELP;
/

CREATE OR REPLACE FUNCTION F_CONSOLE_SKILLHELP

(
  i_SkillID         VARCHAR2,
  i_VDNUserName     VARCHAR2,
  i_VDN             NUMBER
) RETURN INTEGER
AS
  v_SkillID         VARCHAR2(65);
  v_tSkillID        VARCHAR2(32767);
  v_Pos1            INT;
  v_Pos2            INT;
  v_Time            INT;
  v_Char            VARCHAR2(100);
  v_Loc1            INT;
  v_Loc2            INT;
  v_Loc3            INT;
  v_Loc4            INT;
BEGIN

  -- check null value
  IF (i_SkillID IS NULL) THEN
    RETURN 1;
  END IF;

  -- check whether the condition is single-choise or multi-choise
  v_SkillID := SUBSTR(i_SkillID, 1, 10);

  IF v_SkillID = '0|ALL|0|0' THEN

    INSERT INTO T_TMP_SKILLHELP(
                SKILLID,
                SKILLNAME,
                SUBCCNO,
                VDN,
                USERNAME)
         SELECT DISTINCT
                A.SKILLGROUPID,
                A.SKILLGROUPNAME,
                A.SUBCCNO,
                A.VDN,
                i_VDNUserName
           FROM TSKILLGROUP A, V_RPT_USERINFO B
          WHERE A.SUBCCNO = B.SUBCCNO
            AND A.VDN = B.VDN
            AND A.VDN = i_VDN
            AND A.MEDIATYPE = 5   -- add by lianzhou,lKF56491,2012-12-12
            AND B.USERNAME = i_VDNUserName;
  ELSE

    v_tSkillID := i_SkillID;
    -- split string by ','
    v_tSkillID := ',' || v_tSkillID || ',';
    v_Time := 1;
    v_Pos1 := 1;
    v_Pos2 := 1;

    WHILE v_Pos2 <> 0 LOOP

      v_Pos1 := v_Pos2;
      -- split string by ','
      v_Pos2 := INSTR(v_tSkillID, ',', v_Time + 1);

      IF v_Pos2 - v_Pos1 - 1 > 0 THEN

        v_Char := SUBSTR(v_tSkillID, v_Pos1 + 1, v_Pos2 - v_Pos1 - 1);
        v_Loc1 := INSTR(v_Char, '|', 1);
        v_Loc2 := INSTR(v_Char, '|', v_Loc1 + 1);
        v_Loc3 := INSTR(v_Char, '|', v_Loc2 + 1);
        v_Loc4 := LENGTH(v_Char);

        INSERT INTO T_TMP_SKILLHELP(
                    SKILLID,
                    SKILLNAME,
                    SUBCCNO,
                    VDN,
                    USERNAME)
             VALUES(SUBSTR(v_Char, 1, v_Loc1 - 1),
                    SUBSTR(v_Char, v_Loc1 + 1, v_Loc2 - v_Loc1 -1),
                    SUBSTR(v_Char, v_Loc2 + 1, v_Loc3 - v_Loc2 -1),
                    SUBSTR(v_Char, v_Loc3 + 1, v_Loc4 - v_Loc3),
                    i_VDNUserName);
      END IF;

      v_Time := v_Pos2 + 1;
    END LOOP;

  END IF;

  UPDATE T_TMP_SKILLHELP A
     SET A.SKILLNAME = NVL((SELECT B.SKILLGROUPNAME
                             FROM TSKILLGROUP B
                            WHERE A.SKILLID = B.SKILLGROUPID
                                  AND A.SUBCCNO = B.SUBCCNO
                                  AND A.VDN = B.VDN),
                           F_UTL_GETLOCALEDESC('unknow'))
   WHERE A.USERNAME = i_VDNUserName;

  COMMIT;
  RETURN 0;

EXCEPTION
  WHEN OTHERS THEN
    ROLLBACK;
    RETURN 1;

END;
/

CREATE OR REPLACE PROCEDURE P_CONSOLE_QUERYRECORDINFO
(
    o_cursor            OUT  cms_package.out_cursor,
    i_recordingID       IN   VARCHAR2,
    i_callerno          IN   VARCHAR2,
    i_calleeno          IN   VARCHAR2,
    i_visitDate         IN   VARCHAR2,
    i_beginDate         IN   VARCHAR2,
    i_endDate           IN   VARCHAR2,
    i_agentIds          IN   VARCHAR2,
    i_serviceTypeIds    IN   VARCHAR2,
    i_callTypeId        IN   NUMBER,
    i_mediaType         IN   NUMBER,
    i_phoneNo           IN   VARCHAR2,
    i_servDurationMin   IN   NUMBER,
    i_servDurationMax   IN   NUMBER,
    i_ccId              IN   NUMBER,
    i_vdnId             IN   NUMBER,
    i_takePercent       IN   NUMBER,
    i_skillIds          IN   VARCHAR2,
    i_callIds           IN   VARCHAR2,
    i_pageSize          IN OUT  INTEGER,
    io_pageNo           IN OUT INTEGER,
    o_totalCount        OUT INTEGER
)
AS
    v_partIdBegin     NUMBER;
    v_partIdEnd       NUMBER;
    v_partId          NUMBER;
    v_tempPageSize    NUMBER := 0;
    v_rowNumFirst     NUMBER;
    v_rowNumLast      NUMBER;
    v_sql             LONG;
  v_count_sql       LONG;
  v_condition_sql   LONG;
    v_temp_sql        LONG;
    v_common_sql      LONG;
    v_visitDate       VARCHAR2(100);
    v_beginDate       VARCHAR2(100);
    v_endDate         VARCHAR2(100);
    v_year            NUMBER;
    v_month           NUMBER;
    v_beginDate_product  NUMBER;
    v_endDate_product    NUMBER;
    v_beginDate_search   DATE;
    v_beginDate_search_str VARCHAR2(100);
    v_endDate_search     DATE;
    v_endDate_search_str VARCHAR2(100);
    v_countNum           NUMBER;
    v_sn                 NUMBER;
    v_sum                NUMBER;
    v_firstNum           NUMBER;
    v_lastNum            NUMBER;
    v_i                  NUMBER;
    v_dbtimezone         VARCHAR2(100);
    v_dbtimezone_module  VARCHAR2(8);
    v_dbtimezone_hour    NUMBER;
    v_dbtimezone_minute  NUMBER;
    v_dbtimezone_cumulate_day NUMBER;
    v_i_beginDate        DATE;
    v_i_endDate          DATE;
    v_i_visitDate        DATE;
    v_callId_sort        VARCHAR2(32);
    v_agentId_sort       NUMBER;
    v_begintime_sort     DATE;
    v_begintime_sort_char VARCHAR2(100);
    v_mounthStr          VARCHAR2(100);
    v_recordingIDNUM     NUMBER;

    CURSOR v_cursor_temp IS SELECT SN, MONTH, BEGINTIME, ENDTIME, COUNTNUM, FIRSTNUM, LASTNUM FROM T_CMS_TEMPORARY_QUERY_CONTROL;
    CURSOR v_cursor_sort IS SELECT CALLID, AGENTID, BEGINTIME FROM T_CMS_TEMPORARY_SORT;

BEGIN

  EXECUTE IMMEDIATE 'TRUNCATE TABLE T_CMS_TEMPORARY_QUERY_CONTROL';
    EXECUTE IMMEDIATE 'TRUNCATE TABLE T_CMS_TEMPORARY_TRECORDINFO';
    EXECUTE IMMEDIATE 'TRUNCATE TABLE T_CMS_TEMPORARY_SORT';

    select to_date(i_beginDate,'yyyy-mm-dd hh24:mi:ss') into v_i_beginDate from dual;
    select to_date(i_endDate,'yyyy-mm-dd hh24:mi:ss') into v_i_endDate from dual;
    select to_date(i_visitDate,'yyyy-mm-dd hh24:mi:ss') into v_i_visitDate from dual;
    SELECT COUNT(1) INTO v_i FROM T_DBTIMEZONE;

    IF v_i = 1 THEN
        SELECT DBTZONE INTO v_dbtimezone FROM T_DBTIMEZONE;
    END IF;

    IF v_dbtimezone IS NOT NULL AND SUBSTR(v_dbtimezone, 1, 1) IN ('+','-')
        AND SUBSTR(v_dbtimezone, 2, 2) BETWEEN '00' AND '14'
        AND SUBSTR(v_dbtimezone, 5, 2) BETWEEN '00' AND '59' THEN
      v_dbtimezone_module := SUBSTR(v_dbtimezone, 1, 1);
      v_dbtimezone_hour := TO_NUMBER(SUBSTR(v_dbtimezone, 2, 2));
      v_dbtimezone_minute := TO_NUMBER(SUBSTR(v_dbtimezone, 5, 2));
      v_dbtimezone_cumulate_day := TO_NUMBER(v_dbtimezone_hour/24 + v_dbtimezone_minute/24/60);
    END IF;

    v_tempPageSize := i_pageSize;
    IF v_tempPageSize IS NULL OR v_tempPageSize <= 0 THEN
        v_tempPageSize := 1;
    END IF;

    IF io_pageNo IS NULL OR io_pageNo <= 0 THEN
        io_pageNo := 1;
    END IF;

    v_rowNumFirst := v_tempPageSize * (io_pageNo - 1) + 1;
    v_rowNumLast := v_tempPageSize * io_pageNo;

  IF i_recordingID IS NOT NULL AND LENGTH(i_recordingID) > 0 THEN
        SELECT TO_CHAR(v_i_visitDate, 'YYYY-MM-DD HH24:mi:ss') INTO v_visitDate FROM DUAL;
        v_partIdBegin := TO_NUMBER(TO_CHAR(v_i_visitDate, 'MM'));
        v_partIdEnd := v_partIdBegin;

        v_partId := v_partIdEnd;
        SELECT INSTR(i_recordingID,'\',1,3) INTO v_recordingIDNUM FROM DUAL;
        v_mounthStr := SUBSTR(i_recordingID,v_recordingIDNUM+5,2);

    IF v_mounthStr IS NOT NULL AND SUBSTR(v_mounthStr,1,1) ='0' THEN
        v_mounthStr := SUBSTR(v_mounthStr, 2, 1);
    END IF ;

    v_count_sql := 'SELECT R.CALLID, R.FILENAME FROM TRECORDINFO';

    v_temp_sql := 'SELECT ROWNUM AS ROWNO, SUBSTR(R.FILENAME,1,1) || ''c'' || R.CALLCENTERID || ''v'' || R.VIRTUALCALLCENTERID || ''a'' || R.AGENTID
              || ''d'' ||SUBSTR(R.FILENAME,INSTR(REPLACE(R.FILENAME,''/'',''\''),''\'',1,3)+1,8)
            || ''t'' ||SUBSTR(R.FILENAME,INSTR(REPLACE(R.FILENAME,''/'',''\''),''\'',1,5)+1,INSTR(R.FILENAME,''.'',1,1)-INSTR(REPLACE(R.FILENAME,''/'',''\''),''\'',1,5)-1) AS RECORD_ID,
            R.CALLID, R.CALLERNO, R.CALLEENO, R.AGENTID, R.CALLCENTERID, R.VIRTUALCALLCENTERID, R.BEGINTIME, R.ENDTIME,
            R.FILENAME, R.CALLTYPE, R.SERVICENO, R.VISITTIME, R.VISITFLAG, R.MEDIATYPE, R.RECORDFORMAT, R.CURRENTSKILLID
          FROM TRECORDINFO';

    v_condition_sql := v_mounthStr || ' R WHERE REPLACE(R.FILENAME,''/'',''\'') = ''' || i_recordingID || '''';

    IF LENGTH(i_phoneNo) > 0 THEN
       v_condition_sql := v_condition_sql || ' AND (R.CALLERNO LIKE ''%' || i_phoneNo|| '%'' OR R.CALLEENO LIKE ''%' || i_phoneNo || '%'')';
    END IF;

    IF LENGTH(i_agentIds) > 0 THEN
       v_condition_sql := v_condition_sql || ' AND R.AGENTID IN (' || i_agentIds || ')';
    END IF;

    IF LENGTH(i_serviceTypeIds) > 0 THEN
       v_condition_sql := v_condition_sql || ' AND R.SERVICENO IN(' || i_serviceTypeIds || ')';
    END IF;

    IF LENGTH(i_skillIds) > 0 THEN
       v_condition_sql := v_condition_sql || ' AND R.CURRENTSKILLID IN(' || i_skillIds || ')';
    END IF;

    IF i_callTypeId > -1 THEN
       v_condition_sql := v_condition_sql || ' AND R.CALLTYPE = ' || i_callTypeId;
    END IF;
    
    IF LENGTH(i_callerno) > 0 THEN
       v_condition_sql := v_condition_sql || ' AND R.CALLERNO =' || i_callerno;
    END IF;
    
    IF LENGTH(i_calleeno) > 0 THEN
       v_condition_sql := v_condition_sql || ' AND R.CALLEENO =' || i_calleeno;
    END IF;

    IF i_mediaType > 0 THEN
       v_condition_sql := v_condition_sql || ' AND R.MEDIATYPE = ' || i_mediaType;
    END IF;

    IF i_servDurationMin >= 0 THEN
       v_condition_sql := v_condition_sql || ' AND ROUND((R.ENDTIME - R.BEGINTIME)*3600*24) >= ' || i_servDurationMin;
    END IF;

    IF i_servDurationMax >= 0 THEN
       v_condition_sql := v_condition_sql || ' AND ROUND((R.ENDTIME - R.BEGINTIME)*3600*24) <= ' || i_servDurationMax;
    END IF;

    IF i_callIds IS NOT NULL AND LENGTH(i_callIds) > 0 THEN
       v_condition_sql := v_condition_sql || ' AND R.CALLID IN (''' || i_callIds || ''')';
    END IF;

    IF v_i_beginDate IS NOT NULL THEN
      SELECT TO_CHAR(v_i_beginDate,'YYYY-MM-DD HH24:mi:ss') INTO v_beginDate FROM DUAL;
      v_condition_sql := v_condition_sql || ' AND R.BEGINTIME >= TO_DATE(''' || v_beginDate|| ''',''YYYY-MM-DD HH24:mi:ss'')';
    END IF;

    IF v_i_endDate IS NOT NULL THEN
       SELECT TO_CHAR(v_i_endDate,'YYYY-MM-DD HH24:mi:ss') INTO v_endDate FROM DUAL;
       v_condition_sql := v_condition_sql || ' AND R.BEGINTIME <= TO_DATE(''' || v_endDate || ''',''YYYY-MM-DD HH24:mi:ss'')';
    END IF;

    v_condition_sql := v_condition_sql || ' AND R.CALLCENTERID = ' || i_ccId || ' AND R.VIRTUALCALLCENTERID = ' || i_vdnId;
    v_count_sql := v_count_sql || v_condition_sql;
    v_temp_sql := v_temp_sql || v_condition_sql;
    EXECUTE IMMEDIATE 'SELECT COUNT(1) FROM (' || v_count_sql || ')' INTO o_totalCount;

    v_tempPageSize := i_pageSize;
    IF v_tempPageSize IS NULL OR v_tempPageSize <= 0 THEN
      v_tempPageSize := 1;
    END IF;

    IF io_pageNo IS NULL OR io_pageNo <= 0 THEN
      io_pageNo := 1;
    END IF;

    v_rowNumFirst := v_tempPageSize * (io_pageNo - 1) + 1;
    v_rowNumLast := v_tempPageSize * io_pageNo;

    IF i_takePercent > 0 AND i_takePercent < 100 THEN
      o_totalCount := TRUNC(o_totalCount * i_takePercent / 100);
      v_tempPageSize := i_pageSize;

      IF v_tempPageSize IS NULL OR v_tempPageSize <= 0 THEN
        v_tempPageSize := 1;
      END IF;

      IF io_pageNo IS NULL OR io_pageNo <= 0 THEN
        io_pageNo := 1;
      END IF;

      v_rowNumFirst := v_tempPageSize * (io_pageNo - 1) + 1;
      v_rowNumLast := v_tempPageSize * io_pageNo;
      IF v_rowNumLast > o_totalCount THEN
        v_rowNumLast := o_totalCount;
      END IF;
    END IF;

    v_sql := 'SELECT T.CALLID AS callId,
          T.RECORD_ID AS recordId,
          ROUND((T.ENDTIME - T.BEGINTIME)*3600*24) AS serverTime,
          T.CALLCENTERID AS ccId,
          T.VIRTUALCALLCENTERID AS vdnId,
          T.AGENTID AS agentId,
          T.SERVICENO AS serviceTypeId,
          T.CALLTYPE AS callTypeId,';

    IF v_dbtimezone_module = '+' THEN
      v_sql := v_sql || '(T.BEGINTIME - ' || v_dbtimezone_cumulate_day || ') AS beginDate,';
      v_sql := v_sql || '(T.ENDTIME - ' || v_dbtimezone_cumulate_day || ') AS endDate,';
      v_sql := v_sql || '(T.VISITTIME - '|| v_dbtimezone_cumulate_day || ') AS visitTime,';
    ELSIF v_dbtimezone_module = '-' THEN
      v_sql := v_sql || '(T.BEGINTIME + ' || v_dbtimezone_cumulate_day || ') AS beginDate,';
      v_sql := v_sql || '(T.ENDTIME + ' || v_dbtimezone_cumulate_day || ') AS endDate,';
      v_sql := v_sql || '(T.VISITTIME + ' || v_dbtimezone_cumulate_day || ') AS visitTime,';
    ELSE
      v_sql := v_sql || 'T.BEGINTIME AS beginDate, T.ENDTIME AS endDate, T.VISITTIME AS visitTime,';
    END IF;

    v_sql := v_sql || 'T.FILENAME AS fileName,
        T.CALLERNO AS callerNo,
        T.FILENAME AS filename,
        T.CALLEENO AS calleeNo,
        T.CURRENTSKILLID AS currentSkillId,
        T.RECORDFORMAT AS recordFormat,
        T.MEDIATYPE AS mediaType,
        T.VISITFLAG AS visitFlag FROM (' || v_temp_sql || ') T WHERE T.ROWNO >= ' || v_rowNumFirst || ' AND T.ROWNO <= ' || v_rowNumLast;

    OPEN o_cursor FOR v_sql;

  ELSE
    v_year := TO_CHAR(v_i_beginDate,'yyyy');
    v_month := TO_CHAR(v_i_beginDate,'MM');
    v_beginDate_product := v_year*12 + v_month - 1;

    v_year := TO_CHAR(v_i_endDate,'yyyy');
    v_month := TO_CHAR(v_i_endDate,'MM');
    v_endDate_product := v_year*12 + v_month -1;

    v_i := v_endDate_product;

    WHILE v_i >= v_beginDate_product LOOP
      v_year := trunc(v_i/12);
      v_month := v_i - (v_year*12)+1;
      v_beginDate_search := TO_DATE(v_year || '-' || v_month || '-01 00:00:00','YYYY-MM-DD HH24:mi:ss');

      IF v_beginDate_search < v_i_beginDate THEN
        v_beginDate_search := v_i_beginDate;
      END IF;

      v_endDate_search := LAST_DAY(TO_DATE(TO_CHAR(v_beginDate_search,'YYYY-MM-DD')||' 23:59:59','YYYY-MM-DD HH24:mi:ss'));

      IF v_endDate_search > v_i_endDate THEN
         v_endDate_search := v_i_endDate;
      END IF;

      v_beginDate_search_str := TO_CHAR(v_beginDate_search, 'YYYY-MM-DD HH24:mi:ss');
      v_endDate_search_str := TO_CHAR(v_endDate_search, 'YYYY-MM-DD HH24:mi:ss');

      v_count_sql := 'SELECT COUNT(1) FROM TRECORDINFO' || v_month || ' R WHERE R.CALLCENTERID = ' || i_ccId;

      v_common_sql := '';

      IF LENGTH(i_agentIds) > 0 THEN
        v_common_sql := v_common_sql || ' AND R.AGENTID IN(' || i_agentIds || ')';
      END IF;

      IF LENGTH(i_phoneNo) > 0 THEN
         v_common_sql := v_common_sql || ' AND (R.CALLERNO LIKE ''%' || i_phoneNo || '%'' OR R.CALLEENO LIKE ''%' || i_phoneNo || '%'')';
      END IF;
      
      IF LENGTH(i_callerno) > 0 THEN
       v_common_sql := v_common_sql || ' AND R.CALLERNO =' || i_callerno;
      END IF;
    
      IF LENGTH(i_calleeno) > 0 THEN
         v_common_sql := v_common_sql || ' AND R.CALLEENO =''' || i_calleeno || '''';
      END IF;

      IF LENGTH(i_serviceTypeIds)>0 THEN
         v_common_sql := v_common_sql || ' AND R.SERVICENO IN(' || i_serviceTypeIds || ')';
      END IF;

      IF LENGTH(i_skillIds)>0 THEN
         v_common_sql := v_common_sql || ' AND R.CURRENTSKILLID IN(' || i_skillIds || ')';
      END IF;

      IF i_callTypeId > -1 THEN
         v_common_sql := v_common_sql || ' AND R.CALLTYPE = ' || i_callTypeId;
      END IF;

      IF i_mediaType > 0 THEN
         v_common_sql := v_common_sql || ' AND R.MEDIATYPE = ' || i_mediaType;
      END IF;

      IF i_servDurationMin >= 0 THEN
         v_common_sql := v_common_sql || ' AND ROUND((R.ENDTIME - R.BEGINTIME)*3600*24) >= ' || i_servDurationMin;
      END IF;

      IF i_servDurationMax >= 0 THEN
         v_common_sql := v_common_sql || ' AND ROUND((R.ENDTIME - R.BEGINTIME)*3600*24) <= ' || i_servDurationMax;
      END IF;

      IF i_callIds IS NOT NULL AND LENGTH(i_callIds) > 0 THEN
         v_common_sql := v_common_sql || ' AND R.CALLID IN (''' || i_callIds || ''')';
      END IF;

      IF v_common_sql IS NOT NULL AND LENGTH(v_common_sql) > 0 THEN
        v_count_sql := v_count_sql || v_common_sql;
      END IF;

      v_count_sql := v_count_sql || ' AND R.BEGINTIME >= TO_DATE(''' || v_beginDate_search_str || ''',''YYYY-MM-DD HH24:mi:ss'')'
          || ' AND R.BEGINTIME <= TO_DATE(''' || v_endDate_search_str || ''',''YYYY-MM-DD HH24:mi:ss'') AND R.VIRTUALCALLCENTERID = ' || i_vdnId;
      EXECUTE IMMEDIATE v_count_sql INTO v_countNum;

      v_sn := v_endDate_product - v_i + 1;

      IF v_countNum > 0 THEN

        EXECUTE IMMEDIATE 'SELECT SUM(COUNTNUM) FROM T_CMS_TEMPORARY_QUERY_CONTROL WHERE SN < ' || v_sn INTO v_sum;

        IF v_sum IS NOT NULL THEN
          v_firstNum := v_sum + 1;
          v_lastNum := v_firstNum + v_countNum - 1;
        ELSE
          v_firstNum := 1;
          v_lastNum := v_countNum;
        END IF;
      ELSE
        v_firstNum := 0;
        v_lastNum := 0;
      END IF;

      INSERT INTO T_CMS_TEMPORARY_QUERY_CONTROL(SN,MONTH,BEGINTIME,ENDTIME,COUNTNUM,FIRSTNUM,LASTNUM)
        VALUES(v_sn,v_month,v_beginDate_search,v_endDate_search,v_countNum,v_firstNum,v_lastNum);
      COMMIT;
      v_i := v_i - 1;
    END LOOP;


    EXECUTE IMMEDIATE 'SELECT SUM(COUNTNUM) FROM T_CMS_TEMPORARY_QUERY_CONTROL' INTO o_totalCount;
    v_tempPageSize := i_pageSize;

    IF v_tempPageSize IS NULL OR v_tempPageSize <= 0 THEN
      v_tempPageSize := 1;
    END IF;

        IF io_pageNo IS NULL OR io_pageNo <= 0 THEN
            io_pageNo := 1;
        END IF;

    v_rowNumFirst := v_tempPageSize * (io_pageNo - 1) + 1;
    v_rowNumLast := v_tempPageSize * io_pageNo;

    IF i_takePercent > 0 AND i_takePercent < 100 THEN

      o_totalCount := TRUNC(o_totalCount * i_takePercent / 100);
      v_tempPageSize := i_pageSize;

            IF v_tempPageSize IS NULL OR v_tempPageSize <= 0 THEN
                v_tempPageSize := 1;
            END IF;

      IF io_pageNo IS NULL OR io_pageNo <= 0 THEN
        io_pageNo := 1;
      END IF;

            v_rowNumFirst := v_tempPageSize * (io_pageNo - 1) + 1;
            v_rowNumLast := v_tempPageSize * io_pageNo;

            IF v_rowNumLast > o_totalCount THEN
                v_rowNumLast := o_totalCount;
            END IF;
        END IF;

        IF o_totalCount <= 1000000 THEN

      EXECUTE IMMEDIATE 'DELETE FROM T_CMS_TEMPORARY_QUERY_CONTROL WHERE FIRSTNUM > '||v_rowNumLast||' OR LASTNUM < '||v_rowNumFirst;
      COMMIT;
      OPEN v_cursor_temp;

      LOOP
        FETCH v_cursor_temp INTO v_sn, v_month, v_beginDate_search, v_endDate_search,v_countNum,v_firstNum, v_lastNum;
        EXIT WHEN v_cursor_temp%NOTFOUND;

        v_beginDate_search_str := to_char(v_beginDate_search,'YYYY-MM-DD HH24:mi:ss');
        v_endDate_search_str := to_char(v_endDate_search,'YYYY-MM-DD HH24:mi:ss');

        v_temp_sql := 'SELECT ROW_NUMBER() OVER(ORDER BY R.BEGINTIME DESC) ROWNO, R.CALLID, R.AGENTID, R.BEGINTIME FROM TRECORDINFO' || v_month
        || ' R WHERE R.CALLCENTERID = ' || i_ccId;
        v_temp_sql := v_temp_sql || v_common_sql;
        v_temp_sql := v_temp_sql || ' AND R.BEGINTIME >= TO_DATE(''' || v_beginDate_search_str || ''',''YYYY-MM-DD HH24:mi:ss'')'
          || ' AND R.BEGINTIME <= TO_DATE(''' || v_endDate_search_str || ''',''YYYY-MM-DD HH24:mi:ss'')' || ' AND R.VIRTUALCALLCENTERID = ' || i_vdnId;

        IF v_rowNumLast <= v_lastNum THEN
          v_lastNum := v_rowNumLast - v_firstNum + 1;
        ELSE
          v_lastNum := v_lastNum - v_firstNum + 1;
        END IF;

                IF v_rowNumFirst >= v_firstNum THEN
          v_firstNum := v_rowNumFirst - v_firstNum + 1;
        ELSE
          v_firstNum := 1;
        END IF;

        v_temp_sql := 'INSERT INTO T_CMS_TEMPORARY_SORT SELECT CALLID,AGENTID,BEGINTIME
          FROM (' || v_temp_sql || ') Q WHERE Q.ROWNO >=' || v_firstNum || ' AND Q.ROWNO <= ' || v_lastNum;

        EXECUTE IMMEDIATE v_temp_sql;

        OPEN v_cursor_sort;
        LOOP
          FETCH v_cursor_sort INTO v_callId_sort, v_agentId_sort, v_begintime_sort;
          EXIT WHEN v_cursor_sort%NOTFOUND;

          SELECT TO_CHAR(v_begintime_sort,'YYYY-MM-DD HH24:mi:ss') INTO v_begintime_sort_char FROM DUAL;

          v_temp_sql := 'INSERT INTO T_CMS_TEMPORARY_TRECORDINFO';
          v_temp_sql := v_temp_sql || ' SELECT SUBSTR(R.FILENAME,1,1) || ''c'' || R.CALLCENTERID || ''v'' || R.VIRTUALCALLCENTERID
              || ''a'' || R.AGENTID || ''d'' || SUBSTR(R.FILENAME,INSTR(REPLACE(R.FILENAME,''/'',''\''),''\'',1,3)+1,8)
              ||''t'' || SUBSTR(R.FILENAME,INSTR(REPLACE(R.FILENAME,''/'',''\''),''\'',1,5)+1,INSTR(R.FILENAME,''.'',1,1)-INSTR(REPLACE(R.FILENAME,''/'',''\''),''\'',1,5)-1) AS RECORD_ID,';

          v_temp_sql := v_temp_sql || 'CALLID,CALLERNO,CALLEENO,AGENTID,CALLCENTERID,VIRTUALCALLCENTERID,BEGINTIME,
              ENDTIME,FILENAME,CALLTYPE,SERVICENO,VISITTIME,VISITFLAG,MEDIATYPE,MODNO,TRKNO,SERVICEID,SERVICEINFO,
              CALLINFO,STOPREASON,LOCATIONID,RECORDFORMAT,USERWANTEDSKILLID,CURRENTSKILLID,CUSTLEVEL
            FROM TRECORDINFO' || v_month || ' R WHERE R.CALLID = ''' || v_callId_sort || ''' AND R.AGENTID=' || v_agentId_sort
              || ' AND R.BEGINTIME = TO_DATE(''' || v_begintime_sort_char || ''',''YYYY-MM-DD HH24:mi:ss'')';

          EXECUTE IMMEDIATE v_temp_sql;
          COMMIT;
        END LOOP;
        CLOSE v_cursor_sort;
      END LOOP;

      CLOSE v_cursor_temp;
    END IF;

    v_sql := 'SELECT Q.CALLID AS callId,
                Q.RECORD_ID AS recordId,
                ROUND((Q.ENDTIME - Q.BEGINTIME)*3600*24) AS serverTime,
                Q.CALLCENTERID AS ccId,
                Q.VIRTUALCALLCENTERID AS vdnId,
                Q.AGENTID AS agentId,
                Q.SERVICENO AS serviceTypeId,
                Q.CALLTYPE AS callTypeId,';

    
    v_sql := v_sql || 'Q.BEGINTIME AS beginDate, Q.ENDTIME AS endDate, Q.VISITTIME AS visitTime,';
    

    v_sql := v_sql || 'Q.FILENAME AS fileName,
                Q.CALLERNO AS callerNo,
                Q.CALLEENO AS calleeNo,
                Q.CURRENTSKILLID AS currentSkillId,
                Q.RECORDFORMAT AS recordFormat,
        Q.MEDIATYPE AS mediaType,
                Q.VISITFLAG AS visitFlag FROM T_CMS_TEMPORARY_TRECORDINFO Q ORDER BY BEGINTIME DESC';

        OPEN o_cursor FOR v_sql;
    END IF;

END P_CONSOLE_QUERYRECORDINFO;
/



CREATE OR REPLACE PROCEDURE P_CONSOLE_AGENTCALLOUTBRIEF
/*
  Description   : Agent Group Outbound Profile Report
  Author        : Zhou Jiasheng zKF56494
  Date          : 2012-5-9
  Version       :
  Caller        :
  Callee        :
                  F_UTL_AGENTHELP
                  F_UTL_CALCULATEQUERYTIME
                  F_UTL_GETAGENTNAME
                  F_UTL_FORMATTIME
                  F_Rpt_AgentCallOutBrief
                  F_Rpt_AgentCallOutBrief_P
                  F_Rpt_AgentCallOutBrief_D
                  F_Rpt_AgentCallOutBrief_M
                  F_Rpt_AgentCallOutBrief_W

  Comments      :
  History       :
    1.  Date         : 2012-5-9
        Author       : WangHuan KF55753
        Modification : Modification of code standardization
*/
(
  c_Cursor           OUT    xpp_DB170.T_RetDataSet,     -- cursor return value
  i_StartDate               VARCHAR2,                   -- query begin time
  i_EndDate                 VARCHAR2,                   -- query end time
  i_TimeUnit                NUMBER,                     -- static time unit
  i_Flag                    NUMBER,                     -- query type(1:by agent id, 2:by work group, 0:by agent id associated with work group)
  i_AgentWorkGroup          VARCHAR2,                   -- agent group
  i_AgentID                 VARCHAR2,                   -- agent id
  i_VDNUserName             VARCHAR2,                   -- rpt user
  i_RptType                 INT,                         -- rpt type(0:real-time, 1:interval, 2:daily, 3:monthly, 4:weekly)
  i_VDN                     INT
)
AS
  v_UserName                VARCHAR2(60);
  v_StartDate               DATE;                       -- query begin time
  v_EndDate                 DATE;                       -- query end time
  v_StartTime               DATE;                       -- query begin time for real-time daylog
  v_EndTime                 DATE;                       -- query end time for real-time daylog
  v_StartTime_p             DATE;                       -- query begin time for interval daylog
  v_EndTime_p               DATE;                       -- query end time for interval daylog
  v_Ret                     INT;                        -- Returns 0 on success, failure to return 1
  v_Num                     INT;
  v_ErrCode                 INT;
  v_ErrMsg                  VARCHAR2(200);
  ex_User                   EXCEPTION;                  -- user exception
  ex_InPut                  EXCEPTION;                  -- input exception
  ex_Range                  EXCEPTION;                  -- range exception
  ex_Internal               EXCEPTION;
BEGIN

  -- check input time
  v_Ret := F_UTL_VALIDATEDATE(i_StartDate, i_EndDate, i_RptType, 1);
  IF v_Ret <> 0 THEN
    RAISE ex_Range;
  END IF;

  -- Format time type
  v_Ret := F_UTL_FORMATDATE(i_StartDate, i_RptType, i_TimeUnit, v_StartDate);
  IF v_Ret = 1 THEN
    RAISE ex_Range;
  END IF;

  v_Ret := F_UTL_FORMATDATE(i_EndDate, i_RptType, i_TimeUnit, v_EndDate);
  IF v_Ret = 1 THEN
    RAISE ex_Range;
  END IF;

  -- check the user privilege
  v_Num := F_UTL_VALIDATEUSER(i_VDNUserName, v_UserName);
  IF v_Num = 0 THEN
    RAISE ex_User;
  END IF;

  -- split agent group and agent id for operated agent
  v_Ret := F_CONSOLE_AGENTHELP(i_AgentID, i_AgentWorkGroup, i_Flag, v_UserName,i_VDN);
  IF v_Ret <> 0 THEN
    RAISE ex_InPut;
  END IF;

  IF i_RptType = 1 THEN
    -- calculate begin time and end time
    v_Ret := F_UTL_CALCULATEQUERYTIME(v_StartDate, v_EndDate,
                                      v_StartTime, v_EndTime, v_StartTime_p, v_EndTime_p);
    IF v_Ret = 1 THEN
      RAISE ex_Internal;
    END IF;

    IF (v_StartTime IS NOT NULL) OR (v_EndTime IS NOT NULL) THEN
      v_Ret := F_RPT_AGENTCALLOUTBRIEF(v_StartTime, v_EndTime, i_TimeUnit, i_Flag, v_UserName);
      IF v_Ret = 1 THEN
        RAISE ex_Internal;
      END IF;
    END IF;

    IF (v_StartTime_p IS NOT NULL) OR (v_EndTime_p IS NOT NULL) THEN
      v_Ret := F_RPT_AGENTCALLOUTBRIEF_P(v_StartTime_p, v_EndTime_p, i_TimeUnit, i_Flag, v_UserName);
      IF v_Ret = 1 THEN
        RAISE ex_Internal;
      END IF;
    END IF;

    UPDATE T_TMP_AGENTCALLOUTBRIEF
       SET STATTIME = F_UTL_UPDATEQUERYTIME(v_StartDate, v_EndDate, i_TimeUnit)
     WHERE i_TimeUnit = 0
       AND SID IN (8, 9)
       AND VDNUSERNAME = v_UserName;

  ELSIF i_RptType = 2 THEN
    v_Ret := F_RPT_AGENTCALLOUTBRIEF_D(v_StartDate, v_EndDate, i_Flag, v_UserName);

  ELSIF i_RptType = 3 THEN
    v_Ret := F_RPT_AGENTCALLOUTBRIEF_M(v_StartDate, v_EndDate, i_Flag, v_UserName);

  ELSIF i_RptType = 4 THEN
    v_Ret := F_RPT_AGENTCALLOUTBRIEF_W(v_StartDate, v_EndDate, i_Flag, v_UserName);
  END IF;

  IF v_Ret = 1 THEN
    RAISE ex_Internal;
  END IF;


  -- cursor return value
  OPEN c_Cursor FOR
    SELECT DECODE(AGENTID_GROUPID, '99999', '--', AGENTID_GROUPID) AS AGENTID,                   -- agent id / work group id
           F_UTL_GETAGENTNAME(i_Flag, AGENTID_GROUPID, AGENTNAME_WORKGROUP, SUBCCNO, VDN) AS AGENTNAME, -- agent name / work group name
           NVL(STATTIME, '') AS TIMESEGMENT,                                                         -- time segment
           NVL(SUM(OUTAGENTOCCUPYNUM), 0) AS OUTBOUNDCALLS,                                            -- outbound calls
           NVL(SUM(OUTAGENTCALLSUCCNUM), 0) AS OUTBOUNDANSWERED,                                          -- outbound answered
           NVL(SUM(OUTAGENTOCCUPYNUM) - SUM(OUTAGENTCALLSUCCNUM), 0) AS OUTBOUNDABANDONED,                 -- outbound abandoned
           TO_CHAR(SUM(OUTAGENTCALLSUCCNUM) * 100.0 / SUM(OUTAGENTOCCUPYNUM), '990.99') || '%' AS OUTBOUNDANSWERRATE,  -- outbound answer rate
           F_UTL_FORMATTIME(NVL(SUM(OUTAGENTCALLSUCCTIME), 0)) AS OUTBOUNDTALKTIME,                       -- outbound talk time
           F_UTL_FORMATTIME(DECODE(NVL(SUM(OUTAGENTCALLSUCCNUM), 0), 0, 0,
               NVL(SUM(OUTAGENTCALLSUCCTIME), 0) * 1.0 / SUM(OUTAGENTCALLSUCCNUM))) AS AVGOUTBOUNDTALKTIME,  -- avg. outbound talk time
           F_UTL_FORMATTIME(NVL(MAX(OUTMAXAGENTCALLTIME), 0)) AS MAXOUTBOUNDTALKTIME,                        -- max. outbound talk time
           F_UTL_FORMATTIME(DECODE(NVL(MIN(OUTMINAGENTCALLTIME), 0), 86400, 0, MIN(OUTMINAGENTCALLTIME)))  AS MINOUTBOUNDTALKTIME   -- min. outbound talk time
      FROM T_TMP_AGENTCALLOUTBRIEF
     WHERE VDNUSERNAME = v_UserName
     GROUP BY DECODE(SID , 9, 8, SID), SUBCCNO, VDN, AGENTID_GROUPID, AGENTNAME_WORKGROUP, STATTIME
    HAVING SUM(OUTAGENTOCCUPYNUM) <> 0
     ORDER BY DECODE(SID , 9, 8, SID), SUBCCNO, VDN, AGENTID_GROUPID, AGENTNAME_WORKGROUP, STATTIME;

  --delete data from temporary table
  DELETE FROM T_TMP_AGENTWORKGROUP WHERE USERNAME = v_UserName;
  DELETE FROM T_TMP_AGENTID WHERE USERNAME = v_UserName;
  DELETE FROM T_TMP_AGENTCALLOUTBRIEF WHERE VDNUSERNAME = v_UserName;
  COMMIT;

  RETURN;

EXCEPTION
  WHEN ex_User THEN
    OPEN c_Cursor FOR
      SELECT '-30001 Access Not Authorized' AS A1, DECODE(i_Flag, 2, '-30001 Access Not Authorized', '0') AS A2,
             '0' AS A3, '0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7, '0' AS A8, '0' AS A9, '0' AS A10, '0' AS A11
        FROM DUAL;

  WHEN ex_InPut THEN
    OPEN c_Cursor FOR
      SELECT '-30002 Input Parameter Error' AS A1, DECODE(i_Flag, 2, '-30002 Input Parameter Error', '0') AS A2,
             '0' AS A3,'0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7, '0' AS A8, '0' AS A9, '0' AS A10, '0' AS A11
        FROM DUAL;

  WHEN ex_Range THEN
    OPEN c_Cursor FOR
      SELECT '-30003 Input Time Error' AS A1, DECODE(i_Flag, 2, '-30003 Input Time Error', '0') AS A2,
             '0' AS A3,'0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7, '0' AS A8, '0' AS A9, '0' AS A10, '0' AS A11
        FROM DUAL;

  WHEN ex_Internal THEN
    OPEN c_Cursor FOR
      SELECT '-30009 Internal Error' AS A1, DECODE(i_Flag, 2, '-30009 Internal Error', '0') AS A2,
             '0' AS A3,'0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7, '0' AS A8, '0' AS A9, '0' AS A10, '0' AS A11
        FROM DUAL;

  WHEN OTHERS THEN
    v_ErrCode := SQLCODE;
    v_ErrMsg := SUBSTR(SQLERRM, 1, 200);
      OPEN c_Cursor FOR
        SELECT TO_CHAR(v_ErrCode) || ' ' || v_ErrMsg AS A1, DECODE(i_Flag, 2, TO_CHAR(v_ErrCode) || ' ' || v_ErrMsg, '0') AS A2,
               '0' AS A3,'0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7, '0' AS A8, '0' AS A9, '0' AS A10, '0' AS A11
          FROM DUAL;

END P_CONSOLE_AGENTCALLOUTBRIEF;
/

CREATE OR REPLACE PROCEDURE P_CONSOLE_AGENTTRAFFIC
/*
  Description   : Agent Traffic Report
  Author        : shaocan sKF55755
  Date          : 2012-05-08
  Version       :
  Callee        : F_Rpt_AgentTraffic
                  F_Rpt_AgentTraffic_P
                  F_Rpt_AgentTraffic_D
                  F_Rpt_AgentTraffic_M
                  F_Rpt_AgentTraffic_W
  History       :
    1.  Date         : 2012-05-08
        Author       : shaocan sKF55755
        Modification :
    2.  Date         : 2013-04-18
        Author       : zhaoling_00165355
        Modification : add an item of answer in SLA rate
 */
(
  c_Cursor       OUT    xpp_DB170.T_RetDataSet,            -- cursor return value
  i_StartDate           VARCHAR2,                          -- query begin time
  i_EndDate             VARCHAR2,                          -- query end time
  i_TimeUnit            INT,                            -- static time unit
  i_Flag                INT,                            -- query type(1:by agent id, 2:by work group, 0:by agent id associate with work group)
  i_AgentGroup          VARCHAR2,                          -- work group
  i_AgentID             VARCHAR2,                          -- agent id
  i_SkillID             VARCHAR2,                          -- skill
  i_CCID                VARCHAR2,                          -- subccno
  i_VDNUserName         VARCHAR2,                          -- rpt user
  i_Type                INT,                               -- query type(0:by time, 1:by skill)
  i_RptType             INT,                              -- rpt type(0:real-time, 1:interval, 2:daily, 3:monthly, 4:weekly)
  i_VDN                 INT
)
AS
  v_UserName            VARCHAR2(60);
  v_StartDate           DATE;                              -- query begin time
  v_EndDate             DATE;                              -- query end time
  v_StartTime           DATE;                              -- query begin time for real-time daylog
  v_EndTime             DATE;                              -- query end time for real-time daylog
  v_StartTime_p         DATE;                              -- query begin time for interval daylog
  v_EndTime_p           DATE;                              -- query end time for interval daylog
  v_Ret                 INT;                               -- return value
  v_Num                 INT;
  v_ErrCode             INT;                               -- error code
  v_ErrMsg              VARCHAR2(200);                     -- error message
  ex_User               EXCEPTION;                         -- user exception
  ex_InPut              EXCEPTION;                         -- input exception
  ex_Range              EXCEPTION;                         -- range exception
  ex_Internal           EXCEPTION;
BEGIN

  -- check input time
  v_Ret := F_UTL_VALIDATEDATE(i_StartDate, i_EndDate, i_RptType, 1);
  IF v_Ret <> 0 THEN
    RAISE ex_Range;
  END IF;

  -- Format time type
  v_Ret := F_UTL_FORMATDATE(i_StartDate, i_RptType, i_TimeUnit, v_StartDate);
  IF v_Ret = 1 THEN
    RAISE ex_Range;
  END IF;

  v_Ret := F_UTL_FORMATDATE(i_EndDate, i_RptType, i_TimeUnit, v_EndDate);
  IF v_Ret = 1 THEN
    RAISE ex_Range;
  END IF;

  -- check the user privilege
  v_Num := F_UTL_VALIDATEUSER(i_VDNUserName, v_UserName);
  IF v_Num = 0 THEN
    RAISE ex_User;
  END IF;

  -- split query condition of agent group and agent id
  IF i_Type = 0 THEN
    v_Ret := F_CONSOLE_AGENTHELP(i_AgentID, i_AgentGroup, i_Flag, v_UserName,i_VDN);
  ELSIF i_Type = 1 THEN
    v_Ret := F_UTL_AGENTSKILLHELP(i_SkillID, v_UserName);

  END IF;

  IF v_Ret <> 0 THEN
    RAISE ex_InPut;
  END IF;

  IF i_RptType = 1 THEN  -- interval

    -- calculate begin time and end time
    v_Ret := F_UTL_CALCULATEQUERYTIME(v_StartDate, v_EndDate,
                                      v_StartTime, v_EndTime, v_StartTime_p, v_EndTime_p);
    IF v_Ret = 1 THEN
      RAISE ex_Internal;
    END IF;

    IF (v_StartTime IS NOT NULL) OR (v_EndTime IS NOT NULL) THEN
      v_Ret := F_RPT_AGENTTRAFFIC(v_StartTime, v_EndTime, i_TimeUnit, i_Flag, v_UserName);
      IF v_Ret = 1 THEN
        RAISE ex_Internal;
      END IF;
    END IF;

    IF (v_StartTime_p IS NOT NULL) OR (v_EndTime_p IS NOT NULL) THEN
      v_Ret := F_RPT_AGENTTRAFFIC_P(v_StartTime_p, v_EndTime_p, i_TimeUnit, i_Flag, v_UserName);
      IF v_Ret = 1 THEN
        RAISE ex_Internal;
      END IF;
    END IF;

    UPDATE T_TMP_AGENTTRAFFIC
       SET TIME_SKILL = F_UTL_UPDATEQUERYTIME(v_StartDate, v_EndDate, i_TimeUnit)
     WHERE i_TimeUnit = 0
       AND SID IN (8, 9)
       AND VDNUSERNAME = v_UserName;

  ELSIF i_RptType = 2 THEN  -- daily
    v_Ret := F_RPT_AGENTTRAFFIC_D(v_StartDate, v_EndDate, i_Flag, v_UserName, i_Type);

  ELSIF i_RptType = 3 THEN  -- month
    v_Ret := F_RPT_AGENTTRAFFIC_M(v_StartDate, v_EndDate, i_Flag, v_UserName, i_Type);

  ELSIF i_RptType = 4 THEN  -- week 
    v_Ret := F_RPT_AGENTTRAFFIC_W(v_StartDate, v_EndDate, i_Flag, v_UserName, i_Type);
  END IF;

  IF v_Ret = 1 THEN
    RAISE ex_Internal;
  END IF;

  -- cursor return value
  OPEN c_Cursor FOR
    SELECT DECODE(AGENTID_GROUPID, '99999', '--', AGENTID_GROUPID) AS AGENT_ID,             -- agent id / work group id
           F_UTL_GETAGENTNAME(i_Flag, AGENTID_GROUPID, AGENTNAME_WORKGROUP, SUBCCNO, VDN) AS AGENT_NAME,     -- i_Flag :0,agent name / 2,work group name
           F_UTL_GETSKILLNAMEFORPORTAL(i_Type, SkillID, TIME_SKILL, SUBCCNO, VDN)  AS I_TYPE,      -- i_Type : 0:time segment, 1:skill
           NVL(SUM(AGENTOCCUPYNUM), 0) AS OFFERED_CALLS,                                   -- offered calls
           NVL(SUM(AGENTCALLSUCCNUM), 0) AS ANSWERED_CALLS,                                 -- answered calls
           NVL(SUM(AGENTOCCUPYNUM - AGENTCALLSUCCNUM), 0) AS LOST_CALLS,                -- lost calls
           NVL(SUM(NOACKNUM - ABANDONINAGENTNUM - AGENTABORTANSNUM - AGENTNOACKNUM), 0) AS ABORT_IN_RING,  -- Abort in ring
           TO_CHAR(DECODE(NVL(SUM(AGENTOCCUPYNUM), 0), 0, 0,
               NVL(SUM(AGENTCALLSUCCNUM), 0) * 100.0 / SUM(AGENTOCCUPYNUM)),'990.99') || '%' AS ANSWER_RATE,   -- ans rate
           --begin: add by z00165355
           TO_CHAR(DECODE(NVL(SUM(AGENTOCCUPYNUM), 0), 0, 0,
               NVL(SUM(AGENTANSIN99), 0) * 100.0 / SUM(AGENTOCCUPYNUM)),'990.99') || '%' AS ANSWERRATE_IN_SERVICELEVEL,   --rate of answer in service level
           --end: add by z00165355
           NVL(SUM(AGENTNOACKNUM), 0) AS RING_OVERTIME,                                    -- ring over time
           NVL(SUM(AGENTABORTANSNUM), 0) AS RING_REJECT,                                 -- ring reject
           NVL(SUM(ABANDONINAGENTIN99), 0) AS USER_ABAN_IN_SLA,                               -- user aban in SLA
           NVL(SUM(ABANDONINAGENTNUM - ABANDONINAGENTIN99), 0) AS USER_ABAN_OVER_SLA,           -- user aban over SLA
           F_UTL_FORMATTIME(DECODE(NVL(SUM(AGENTOCCUPYNUM), 0), 0, 0,
               NVL(SUM(WAITANSTIME), 0) * 1.0 / SUM(AGENTOCCUPYNUM))) AS AVG_RINGTIME,         -- avg. ring time
           F_UTL_FORMATTIME(DECODE(NVL(SUM(AGENTCALLSUCCNUM), 0), 0, 0,
               NVL(SUM(AGENTCALLTIME), 0) * 1.0 / SUM(AGENTCALLSUCCNUM))) AS AVG_TALKTIME, -- avg. talk time
           F_UTL_FORMATTIME(NVL(MAX(MAXAGENTCALLTIME), 0)) AS MAX_TALKTIME,                -- max. tall time
           F_UTL_FORMATTIME(NVL(DECODE(MIN(MINAGENTCALLTIME), 86400, 0, MIN(MINAGENTCALLTIME)), 0))  AS MIN_TALKTIME  -- min talk time
      FROM T_TMP_AGENTTRAFFIC
     WHERE AGENTOCCUPYNUM <> 0
       AND VDNUSERNAME = v_UserName
     GROUP BY DECODE(SID, 9, 8, SID), SUBCCNO, VDN, AGENTID_GROUPID, AGENTNAME_WORKGROUP, SKILLID, TIME_SKILL
     ORDER BY DECODE(SID, 9, 8, SID), SUBCCNO, VDN, AGENTID_GROUPID, AGENTNAME_WORKGROUP,
              DECODE(SKILLID, -1, 88888, SKILLID), TIME_SKILL;

  --delete data from temporary table
  DELETE FROM T_TMP_AGENTID WHERE USERNAME = v_UserName;
  DELETE FROM T_TMP_AGENTWORKGROUP WHERE USERNAME = v_UserName;
  DELETE FROM T_TMP_SKILLHELP WHERE USERNAME = v_UserName;
  DELETE FROM T_TMP_AGENTTRAFFIC WHERE VDNUSERNAME = v_UserName;
  COMMIT;
  RETURN;

EXCEPTION
  WHEN ex_User THEN
     OPEN c_Cursor FOR
       SELECT '-30001 Access Not Authorized' AS A1, DECODE(i_Flag, 2, '-30001 Access Not Authorized', '0') AS A2,'0' AS A3,
              '0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7, '0' AS A8,'0' AS A9,'0' AS A10,
              '0' AS A11,'0' AS A12,'0' AS A13, '0' AS A14, '0' AS A15, '0' AS A16, '0' AS A17
         FROM DUAL;

  WHEN ex_InPut THEN
     OPEN c_Cursor FOR
       SELECT '-30002 Input Parameter Error' AS A1,DECODE(i_Flag, 2, '-30002 Input Parameter Error', '0') AS A2,'0' AS A3,
              '0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7, '0' AS A8,'0' AS A9,'0' AS A10,
              '0' AS A11,'0' AS A12,'0' AS A13, '0' AS A14, '0' AS A15, '0' AS A16, '0' AS A17
         FROM DUAL;

  WHEN ex_Range THEN
    OPEN c_Cursor FOR
      SELECT '-30003 Input Time Error' AS A1,DECODE(i_Flag, 2, '-30003 Input Time Error', '0') AS A2,'0' AS A3,
             '0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7, '0' AS A8,'0' AS A9,'0' AS A10,
             '0' AS A11,'0' AS A12,'0' AS A13, '0' AS A14, '0' AS A15, '0' AS A16, '0' AS A17
        FROM DUAL;

  WHEN ex_Internal THEN
    OPEN c_Cursor FOR
      SELECT '-30009 Internal Error' AS A1,DECODE(i_Flag, 2, '-30009 Internal Error', '0') AS A2,'0' AS A3,
             '0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7, '0' AS A8,'0' AS A9,'0' AS A10,
             '0' AS A11,'0' AS A12,'0' AS A13, '0' AS A14, '0' AS A15, '0' AS A16, '0' AS A17
        FROM DUAL;

  WHEN OTHERS THEN
     v_ErrCode := SQLCODE;
     v_ErrMsg := SUBSTR(SQLERRM,1,200);
     OPEN c_Cursor FOR
       SELECT TO_CHAR(v_ErrCode) || ' ' || v_ErrMsg AS A1,DECODE(i_Flag, 2, TO_CHAR(v_ErrCode) || ' ' || v_ErrMsg, '0') AS A2,'0' AS A3,
              '0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7, '0' AS A8,'0' AS A9,'0' AS A10,
              '0' AS A11,'0' AS A12,'0' AS A13, '0' AS A14, '0' AS A15, '0' AS A16, '0' AS A17
        FROM DUAL;

END P_CONSOLE_AGENTTRAFFIC;
/
CREATE OR REPLACE PROCEDURE P_CONSOLE_AGENTWORK
/*
  Description   : Agent Connection Report
  Author        : shaocan sKF55755
  Date          : 2012-05-09
  Version       :
  Caller        : F_Rpt_AgentWork_D
                  F_Rpt_AgentWork_M
                  F_Rpt_AgentWork_W

  History       :
    1.  Date         : 2012-05-09
        Author       : shaocan sKF55755
        Modification :
    2.  Date         : 2012-07-31
        Author       : zhaoling 165355
        Modification :
*/
(
  c_Cursor        OUT  xpp_DB170.T_RetDataSet,            -- cursor return value
  i_StartDate          VARCHAR2,                          -- query begin time
  i_EndDate            VARCHAR2,                          -- query end time
  i_TimeUnit           NUMBER,                            -- static time unit
  i_Flag               NUMBER,                            -- query type(1:by agent id, 2:by work group, 0:by agent id associated with work group)
  i_AgentWorkGroup     VARCHAR2,                          -- agent group
  i_AgentID            VARCHAR2,                          -- agent id
  i_VDNUserName        VARCHAR2,                          -- rpt user
  i_RptType            INT,                                -- rpt type(0:real-time, 1:interval, 2:daily, 3:monthly, 4:weekly)
  i_VDN                INT
)
AS
  v_UserName           VARCHAR2(60);
  v_StartDate          DATE ;                             -- query begin time
  v_EndDate            DATE ;                             -- query end time
  v_StartTime          DATE;                              -- query begin time for real-time daylog
  v_EndTime            DATE;                              -- query end time for real-time daylog
  v_StartTime_p        DATE;                              -- query begin time for interval daylog
  v_EndTime_p          DATE;                              -- query end time for interval daylog
  v_Ret                INT;                               -- return value
  v_Num                INT;
  v_ErrCode            INT;                               -- error code
  v_ErrMsg             VARCHAR2(200);                     -- error message
  ex_User              EXCEPTION;                         -- user exception
  ex_InPut             EXCEPTION;                         -- input excepiton
  ex_Range             EXCEPTION;                         -- range exception
  ex_Internal          EXCEPTION;
BEGIN

  -- check input time
  v_Ret := F_UTL_VALIDATEDATE(i_StartDate, i_EndDate, i_RptType, 1);
  IF v_Ret <> 0 THEN
    RAISE ex_Range;
  END IF;

  -- Format time type
  v_Ret := F_UTL_FORMATDATE(i_StartDate, i_RptType, i_TimeUnit, v_StartDate);
  IF v_Ret = 1 THEN
    RAISE ex_Range;  END IF;

  v_Ret := F_UTL_FORMATDATE(i_EndDate, i_RptType, i_TimeUnit, v_EndDate);
  IF v_Ret = 1 THEN
    RAISE ex_Range;
  END IF;

  -- check the user privilege
  v_Num := F_UTL_VALIDATEUSER(i_VDNUserName, v_UserName);
  IF v_Num = 0 THEN
    RAISE ex_User;
  END IF;

  -- split agent group and agent id for operated agent
  v_Ret := F_CONSOLE_AGENTHELP(i_AgentID, i_AgentWorkGroup, i_Flag, v_UserName, i_VDN);
  IF v_Ret <> 0 THEN
    RAISE ex_InPut;
  END IF;

  -- get login and logout time
  v_Ret := F_UTL_AGENTLOGINHELPEX(v_StartDate, v_EndDate, i_TimeUnit, i_RptType, 2, v_UserName);
  IF v_Ret <> 0 THEN
    RAISE ex_Internal;
  END IF;

  IF i_RptType = 1 THEN
    -- calculate begin time and end time
    v_Ret := F_UTL_CALCULATEQUERYTIME(v_StartDate, v_EndDate,
                                      v_StartTime, v_EndTime, v_StartTime_p, v_EndTime_p);
    IF v_Ret = 1 THEN
      RAISE ex_Internal;
    END IF;

    IF (v_StartTime IS NOT NULL) OR (v_EndTime IS NOT NULL) THEN
      v_Ret := F_RPT_AGENTWORK(v_StartTime, v_EndTime, i_TimeUnit, v_UserName);
      IF v_Ret = 1 THEN
        RAISE ex_Internal;
      END IF;
    END IF;

    IF (v_StartTime_p IS NOT NULL) OR (v_EndTime_p IS NOT NULL) THEN
      v_Ret := F_RPT_AGENTWORK_P(v_StartTime_p, v_EndTime_p, i_TimeUnit, v_UserName);
    END IF;

    UPDATE T_TMP_AGENTWORK
       SET STATTIME = F_UTL_UPDATEQUERYTIME(v_StartDate, v_EndDate, i_TimeUnit)
     WHERE i_TimeUnit = 0
       AND SID IN (8, 9)
       AND VDNUSERNAME = v_UserName;

  ELSIF i_RptType = 2 THEN  -- daily
    v_Ret := F_RPT_AGENTWORK_D(v_StartDate, v_EndDate, v_UserName);

  ELSIF i_RptType = 3 THEN  -- month
    v_Ret := F_RPT_AGENTWORK_M(v_StartDate, v_EndDate, v_UserName);

  ELSIF i_RptType = 4 THEN  -- week
    v_Ret := F_RPT_AGENTWORK_W(v_StartDate, v_EndDate, v_UserName);
  END IF;

  IF v_Ret = 1 THEN
    RAISE ex_Internal;
  END IF;

  -- cursor return value
  OPEN c_Cursor FOR
    SELECT DECODE(AgentID, 99999, '--', AgentID) AS AGENTID,                      -- agent id
           DECODE(AgentID, 99999, '--', AgentName) AS AGENT_NAME,                    -- agent name
           NVL(STATTIME,' ') AS SKILLNAME,                                          -- time segment
           NVL(SUM(LOGINCOUNT), 0) AS LOGIN_TIMES,                                    -- login times
           F_UTL_FORMATTIME(NVL(SUM(WORKTIME), 0)) AS LOGIN_DURATION,                    -- login duration
           NVL(SUM(AGENTCALLSUCCNUM), 0) AS TALK_TIMES_CALLIN,                              -- talking times of incoming calls
           F_UTL_FORMATTIME(NVL(SUM(AGENTCALLTIME), 0)) AS TALK_DURATION_CALLIN,               -- talking duration of incoming calls
           NVL(SUM(OUTAGENTCALLSUCCNUM), 0) AS TALK_TIMES_CALLOUT,                           -- talking times of outgoing calls
           F_UTL_FORMATTIME(NVL(SUM(OUTAGENTCALLTIME), 0)) AS TALK_DURATION_CALLOUT,            -- talking duration of outgoing calls
           NVL(SUM(ARRANGENUM), 0) AS ARRANGE_TIMES,                                    -- arrange times
           F_UTL_FORMATTIME(NVL(SUM(ARRANGETIME ), 0)) AS ARRANGE_DURATION,                -- arrange duration
           NVL(SUM(RestNum), 0) AS REST_TIMES,                                       -- rest times
           F_UTL_FORMATTIME(NVL(SUM(RESTTIME), 0)) AS REST_DURATION,                    -- rest duration
           NVL(SUM(HOLDNUM), 0) AS HOLD_TIMES,                                       -- hold times
           F_UTL_FORMATTIME(NVL(SUM(HOLDTIME), 0)) AS HOLD_DURATION,                    -- hold duration
           NVL(SUM(BUSYNUM), 0) AS BUSY_TIMES,                                       -- busy times
           F_UTL_FORMATTIME(NVL(SUM(BUSYTIME), 0)) AS BUSY_DURATION,                    -- busy duration
           F_UTL_FORMATTIME(NVL(SUM(WAITANSTIME), 0)) AS RING_TIME,                 -- ringing duration
           F_UTL_FORMATTIME(NVL(SUM(WORKTIME - WAITANSTIME - AGENTCALLTIME -
                                    OUTAGENTCALLTIME - ARRANGETIME - RESTTIME - BUSYTIME), 0)) AS IDEL_TIME,  -- idel time
           TO_CHAR(NVL(DECODE(SUM(WORKTIME), 0, 0,
                   SUM(AGENTCALLTIME + OUTAGENTCALLTIME + INTERCALLTIME + ARRANGETIME - HOLDTIME) * 100.0  / SUM(WORKTIME)), 0),'990.99')||'%' AS WORK_TIME_USE_RATE_WITH_ACW,      -- occupancy with ACW
           TO_CHAR(NVL(DECODE(SUM(WORKTIME), 0, 0,
                   SUM(AGENTCALLTIME + OUTAGENTCALLTIME + INTERCALLTIME - HOLDTIME) * 100.0  / SUM(WORKTIME)), 0),'990.99')||'%' AS WORK_TIME_USE_RATE_WITHOUT_ACW,           -- occupancy without ACW
           NVL(SUM(INTERTRANSFERNUM), 0) AS INTERNAL_TRANSFER_TIMES ,                              -- internal transfer times
           NVL(SUM(TRANSFEROUTNUM), 0)  AS TRANSFER_OUT_TIMES,                                -- transfer out times
           NVL(SUM(TRANSFERAUTONUM), 0) AS HANGUP_TO_IVR_TIMES,                               -- hang-up to IVR times
           NVL(SUM(TRICALLNUM), 0) AS THREE_PARTY_CALLS,                                    -- three-party calls
           NVL(SUM(INTERCALLNUM), 0) AS INTERNAL_CALLS,                                  -- internal calls
           NVL(SUM(INTERHELPNUM), 0) AS INTERNAL_HELP_TIMES                                   -- internal help times
      FROM T_TMP_AGENTWORK
     WHERE (LOGINCOUNT <> 0 OR WORKTIME <> 0 OR AGENTCALLTIME <> 0 OR OUTAGENTCALLTIME <> 0
           OR ARRANGETIME <> 0 OR RESTTIME <> 0 OR HOLDTIME <> 0 OR BUSYTIME <> 0 OR WAITANSTIME <> 0
           OR INTERTRANSFERNUM <> 0 OR TRANSFEROUTNUM  <> 0 OR TRANSFERAUTONUM <> 0
           OR TRICALLNUM <> 0 OR INTERCALLNUM <> 0 OR INTERCALLTIME <> 0 OR INTERHELPNUM <> 0)
       AND VDNUSERNAME = v_UserName
     GROUP BY DECODE(SID, 9, 8, SID), AGENTID, AGENTNAME, STATTIME
     ORDER BY DECODE(SID, 9, 8, SID), AGENTID, AGENTNAME, STATTIME;

  --delete data from temporary table
  DELETE FROM T_TMP_AGENTID WHERE USERNAME = v_UserName;
  DELETE FROM T_TMP_AGENTWORKGROUP WHERE USERNAME = v_UserName;
  DELETE FROM T_TMP_AGENTWORK WHERE VDNUSERNAME = v_UserName;
  DELETE FROM T_TMP_AGENTLOGINHELPEX WHERE USERNAME = v_UserName;
  COMMIT;

  RETURN;

EXCEPTION
  WHEN ex_User THEN
    OPEN c_Cursor FOR
      SELECT '-30001 Access Not Authorized' AS A1,'0' AS A2, '0' AS A3,'0' AS A4,'0' AS A5,
             '0' AS A6,'0' AS A7,'0' AS A8,'0' AS A9,'0' AS A10,'0' AS A11,'0' AS A12,
             '0' AS A13,'0' AS A14,'0' AS A15,'0' AS A16,'0' AS A17,'0' AS A18,'0' AS A19,
             '0' AS A20,'0' AS A21,'0' AS A22,'0' AS A23,'0' AS A24,'0' AS A25,'0' AS A26, '0' AS A27
        FROM DUAL;

  WHEN ex_InPut THEN
    OPEN c_Cursor FOR
      SELECT '-30002 Input Parameter Error' AS A1,'0' AS A2, '0' AS A3,'0' AS A4,'0' AS A5,
             '0' AS A6,'0' AS A7,'0' AS A8,'0' AS A9,'0' AS A10,'0' AS A11,'0' AS A12,
             '0' AS A13,'0' AS A14,'0' AS A15,'0' AS A16,'0' AS A17,'0' AS A18,'0' AS A19,
             '0' AS A20,'0' AS A21,'0' AS A22,'0' AS A23,'0' AS A24,'0' AS A25,'0' AS A26, '0' AS A27
        FROM DUAL;

  WHEN ex_Range THEN
    OPEN c_Cursor FOR
      SELECT '-30003 Input Time Error' AS A1,'0' AS A2, '0' AS A3,'0' AS A4,'0' AS A5,
             '0' AS A6,'0' AS A7,'0' AS A8,'0' AS A9,'0' AS A10,'0' AS A11,'0' AS A12,
             '0' AS A13,'0' AS A14,'0' AS A15,'0' AS A16,'0' AS A17,'0' AS A18,'0' AS A19,
             '0' AS A20,'0' AS A21,'0' AS A22,'0' AS A23,'0' AS A24,'0' AS A25,'0' AS A26, '0' AS A27
        FROM DUAL;

  WHEN ex_Internal THEN
    OPEN c_Cursor FOR
      SELECT '-30009 Internal Error' AS A1,'0' AS A2, '0' AS A3,'0' AS A4,'0' AS A5,
             '0' AS A6,'0' AS A7,'0' AS A8,'0' AS A9,'0' AS A10,'0' AS A11,'0' AS A12,
             '0' AS A13,'0' AS A14,'0' AS A15,'0' AS A16,'0' AS A17,'0' AS A18,'0' AS A19,
             '0' AS A20,'0' AS A21,'0' AS A22,'0' AS A23,'0' AS A24,'0' AS A25,'0' AS A26, '0' AS A27
        FROM DUAL;

  WHEN OTHERS THEN
    v_ErrCode := SQLCODE;
    v_ErrMsg := SUBSTR(SQLERRM,1,200);
    OPEN c_Cursor FOR
      SELECT TO_CHAR(v_ErrCode) || ' ' || v_ErrMsg AS A1,'0' AS A2, '0' AS A3,'0' AS A4,'0' AS A5,
             '0' AS A6,'0' AS A7,'0' AS A8,'0' AS A9,'0' AS A10,'0' AS A11,'0' AS A12,
             '0' AS A13,'0' AS A14,'0' AS A15,'0' AS A16,'0' AS A17,'0' AS A18,'0' AS A19,
             '0' AS A20,'0' AS A21,'0' AS A22,'0' AS A23,'0' AS A24,'0' AS A25,'0' AS A26, '0' AS A27
        FROM DUAL;

END P_CONSOLE_AGENTWORK;
/



CREATE OR REPLACE PROCEDURE P_CONSOLE_SKILLTRAFFIC
(
  c_Cursor       OUT  xpp_DB170.T_RetDataSet,           -- cursor return value
  i_StartDate         VARCHAR2,                         -- query begin time
  i_EndDate           VARCHAR2,                         -- query end time
  i_TimeUnit          INT,                              -- static time unit
  i_SkillID           VARCHAR2,                         -- by skill
  i_CCID              VARCHAR2,                         -- subccno
  i_Type              INT,                              -- query type(0:time, 1:skill)
  i_VDNUserName       VARCHAR2,                         -- rpt user
  i_RptType           INT,                               -- rpt type(0:real-time, 1:interval, 2:daily, 3:monthly, 4:weekly)
  i_VDN               INT
)
AS
  v_UserName          VARCHAR2(60);
  v_StartDate         DATE ;                            -- query begin time
  v_EndDate           DATE ;                            -- query end time
  v_StartTime         DATE;                             -- query begin time for real-time daylog
  v_EndTime           DATE;                             -- query end time for real-time daylog
  v_StartTime_P       DATE ;                            -- query begin time for interval daylog
  v_EndTime_p         DATE ;                            -- query end time for interval daylog
  v_Ret               NUMBER;
  v_Num               INT;
  v_ErrCode           NUMBER;
  v_ErrMsg            VARCHAR2(200);
  ex_User             EXCEPTION;                        -- user exception
  ex_InPut            EXCEPTION;                        -- input exception
  ex_Range            EXCEPTION;
  ex_Internal         EXCEPTION;
BEGIN

  -- check input time and skillid
  v_Ret := F_UTL_VALIDATEDATE(i_StartDate, i_EndDate, i_RptType, 1);
  IF v_Ret <> 0 THEN
    RAISE ex_Range;
  END IF;

  -- Format time type
  v_Ret := F_UTL_FORMATDATE(i_StartDate, i_RptType, i_TimeUnit, v_StartDate);
  IF v_Ret = 1 THEN
    RAISE ex_Range;
  END IF;

  v_Ret := F_UTL_FORMATDATE(i_EndDate, i_RptType, i_TimeUnit, v_EndDate);
  IF v_Ret = 1 THEN
    RAISE ex_Range;
  END IF;

  -- check the user privilege
  v_Num := F_UTL_VALIDATEUSER(i_VDNUserName, v_UserName);
  IF v_Num = 0 THEN
    RAISE ex_User;
  END IF;

  -- split skill
  v_Ret := F_CONSOLE_SKILLHELP(i_SkillID, v_UserName,i_VDN);
  IF v_Ret <> 0 THEN
    RAISE ex_InPut;
  END IF;

  -- by time : 1,2,3,4
  -- by skill: 2,3,4
  IF i_RptType = 1 THEN    -- interval
    -- calculate begin time and end time
    v_Ret := F_UTL_CALCULATEQUERYTIME(v_StartDate, v_EndDate,
                                      v_StartTime, v_EndTime, v_StartTime_P, v_EndTime_p);
    IF v_Ret = 1 THEN
      RAISE ex_Internal;
    END IF;

    IF (v_StartTime IS NOT NULL) OR (v_EndTime IS NOT NULL) THEN
      v_Ret := F_RPT_SKILLTRAFFIC(v_StartTime, v_EndTime, i_TimeUnit, v_UserName);
      IF v_Ret = 1 THEN
        RAISE ex_Internal;
      END IF;
    END IF;

    IF (v_StartTime_P IS NOT NULL) OR (v_EndTime_p IS NOT NULL) THEN
      v_Ret := F_RPT_SKILLTRAFFIC_P(v_StartTime_P, v_EndTime_p, i_TimeUnit, v_UserName);
      IF v_Ret = 1 THEN
        RAISE ex_Internal;
      END IF;
    END IF;

    UPDATE T_TMP_SKILLTRAFFIC
       SET TIME_SKILL = F_UTL_UPDATEQUERYTIME(v_StartDate, v_EndDate, i_TimeUnit)
     WHERE i_TimeUnit = 0
       AND SID IN (8, 9)
       AND VDNUSERNAME = v_UserName;

  ELSIF i_RptType = 2 THEN  -- daily
    v_Ret := F_RPT_SKILLTRAFFIC_D(v_StartDate, v_EndDate, v_UserName, i_Type);

  ELSIF i_RptType = 3 THEN  -- month
    v_Ret := F_RPT_SKILLTRAFFIC_M(v_StartDate, v_EndDate, v_UserName, i_Type);

  ELSIF i_RptType = 4 THEN  -- Week
    v_Ret := F_RPT_SKILLTRAFFIC_W(v_StartDate, v_EndDate, v_UserName, i_Type);
  END IF;

  IF v_Ret = 1 THEN
    RAISE ex_Internal;
  END IF;

  -- cursor return value
  OPEN c_Cursor FOR
    SELECT F_UTL_GETSKILLNAMEFORPORTAL(i_Type, SkillID, Time_Skill, SubCCNo, VDN) AS I_TYPE,                                   -- i_Type : 0:time segment, 1:skill
           NVL(SUM(AGENTOCCUPYNUM), 0) AS OFFEREDCALLS,                                                                     -- offered calls
           NVL(SUM(AGENTCALLSUCCNUM), 0) AS ANSWEREDCALLS,                                                                   -- answered calls
           TO_CHAR(DECODE(NVL(SUM(AGENTOCCUPYNUM), 0), 0, 0,
           NVL(SUM(AGENTCALLSUCCNUM), 0) * 100.0 / SUM(AGENTOCCUPYNUM)),'990.99') || '%' AS ANSWERRATE,                   -- answer rate
           NVL(SUM(AGENTOCCUPYNUM - AGENTCALLSUCCNUM), 0) AS LOSTCALLS,                                                  -- lost calls
           TO_CHAR(DECODE(NVL(SUM(AGENTOCCUPYNUM), 0), 0, 0,
           NVL(SUM(AGENTOCCUPYNUM - AGENTCALLSUCCNUM), 0) * 100.0 / SUM(AGENTOCCUPYNUM)),'990.99') || '%' AS LOSTRATE,  -- lost rate
           NVL(SUM(ABANDONINQUEUENUM), 0) AS USERABANINQUEUE,                                                                  -- user aban in queue
           -- NVL(SUM(QUEUEABORTNUM-ABANDONINQUEUENUM+QUEOVERTOIVRRELNUM), 0),                              -- sys aban calls  in queue
           NVL(SUM(AGENTOCCUPYNUM - AGENTCALLSUCCNUM - NOACKNUM - ABANDONINQUEUENUM), 0) AS  SYSABANCALLSINQUEUE,
           NVL(SUM(ABANDONINAGENTNUM), 0) AS USERABANINRING,                                                                  -- user aban in ring
           NVL(SUM(AGENTABORTANSNUM), 0) AS RINGREJECT,                                                                   -- ring reject
           NVL(SUM(NOACKNUM-ABANDONINAGENTNUM-AGENTABORTANSNUM), 0) AS SYSABANCALLSINRING,                                        -- sys aban calls in ring
           TO_CHAR(DECODE(NVL(SUM(AGENTOCCUPYNUM), 0), 0, 0,
           NVL(SUM(SUCCANSIN99), 0) * 100.0 / SUM(AGENTOCCUPYNUM)),'990.99')  || '%' AS SYSSLARATE,                       -- sys SLA rate
           NVL(SUM(NOACKNUM), 0) AS LOSTINRING,                                                                           -- lost in ring
           TO_CHAR(DECODE(NVL(SUM(WAITANSNUM), 0), 0, 0,
           NVL(SUM(WAITANSNUM - NOACKNUM), 0) * 100.0 / SUM(WAITANSNUM)),'990.99') || '%' AS RINGANSWERRATE,                  -- ring answer rate
           F_UTL_FORMATTIME(DECODE(NVL(SUM(AGENTCALLSUCCNUM), 0), 0, 0,
           NVL(SUM(SUCCQUEUEWAITTIME), 0) * 1.0 / SUM(AGENTCALLSUCCNUM)))  AS AVGANSWERQUEUETIME,                                  -- AVG. ANSWERED QUEUE TIME
           F_UTL_FORMATTIME(DECODE(NVL(SUM(AGENTOCCUPYNUM-AGENTCALLSUCCNUM), 0), 0, 0,
           NVL(SUM(FAILQUEUEWAITTIME), 0) * 1.0 / SUM(AGENTOCCUPYNUM-AGENTCALLSUCCNUM))) AS AVGLOSTQUEUETIME,                   -- AVG. LOST QUEUE TIME
           F_UTL_FORMATTIME(DECODE(NVL(SUM(AGENTCALLSUCCNUM), 0), 0, 0,
           NVL(SUM(SUCCWAITANSTIME), 0) * 1.0 / SUM(AGENTCALLSUCCNUM))) AS AVGANSWERRINGINGTIME,                                    -- AVG. ANSWERED RINGING TIME
           F_UTL_FORMATTIME(DECODE(NVL(SUM(NOACKNUM), 0), 0, 0,
           NVL(SUM(WAITANSTIME - SUCCWAITANSTIME), 0) * 1.0 / SUM(NOACKNUM))) AS AVGLOSTRINGINGTIME,                              -- Avg. Lost Ringing time
           F_UTL_FORMATTIME(DECODE(NVL(SUM(AGENTCALLSUCCNUM), 0), 0, 0,
           NVL(SUM(AGENTCALLTIME), 0) * 1.0 / SUM(AGENTCALLSUCCNUM)))  TALKTIME,                                      -- talk time
           F_UTL_FORMATTIME(DECODE(NVL(SUM(AGENTOCCUPYNUM), 0), 0, 0,
           NVL(SUM(WAITANSTIME + SUCCQUEUEWAITTIME + FAILQUEUEWAITTIME), 0) * 1.0 / SUM(AGENTOCCUPYNUM))) AS AVGWAITTIME,  -- avg wait time
           F_UTL_FORMATTIME(NVL(MAX(MAXWAITANSTIME), 0)) AS MAXQUEUETIME ,                                                   -- max queue time
           F_UTL_FORMATTIME(DECODE(NVL(MIN(MINWAITANSTIME), 0), 86400, 0, MIN(MINWAITANSTIME))) AS MINQUEUETIME ,            -- min queue time
           NVL(SUM(QUEFLOWOUTTOQUENUM), 0) AS FLOWOUTTOQUEUE,                                                                 -- flow-out to Queue
           F_UTL_FORMATTIME(DECODE(NVL(SUM(QUEFLOWOUTTOQUENUM), 0), 0, 0,
           NVL(SUM(FLOWOUTWAITTIME), 0) * 1.0 / SUM(QUEFLOWOUTTOQUENUM))) AS FLOWOUTQUEUETIME,                                  --  flow-out Queue time
           NVL(SUM(QUEFLOWOUTTOAGENTNUM), 0) AS FLOWOUTTOAGENT                                                                -- flow-out to agent
      FROM T_TMP_SKILLTRAFFIC
     WHERE VDNUSERNAME = v_UserName
     GROUP BY DECODE(SID, 9, 8, SID), SUBCCNO, VDN, SKILLID, TIME_SKILL
    HAVING (SUM(AGENTOCCUPYNUM) <> 0 OR SUM(SUCCANSIN99) <> 0)
     ORDER BY DECODE(SID, 9, 8, SID), SUBCCNO, VDN, SKILLID, TIME_SKILL;

  --delete data from temporary table
  DELETE FROM T_TMP_SKILLHELP WHERE USERNAME = v_UserName;
  DELETE FROM T_TMP_SKILLTRAFFIC WHERE VDNUSERNAME = v_UserName;
  COMMIT;

  RETURN;

EXCEPTION
  WHEN ex_User THEN
    OPEN c_Cursor FOR
      SELECT '-30001 Access Not Authorized' AS A1,'0' AS A2,
             '0' AS A3, '0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7,
             '0' AS A8, '0' AS A9, '0' AS A10,'0' AS A11,'0' AS A12,'0' AS A13,
             '0' AS A14,'0' AS A15,'0' AS A16,'0' AS A17,'0' AS A18,'0' AS A19,
             '0' AS A20,'0' AS A21,'0' AS A22,'0' AS A23,'0' AS A24,'0' AS A25
        FROM DUAL;

  WHEN ex_InPut THEN
    OPEN c_Cursor FOR
      SELECT '-30002 Input Parameter Error' AS A1,'0' AS A2,
             '0' AS A3, '0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7,
             '0' AS A8, '0' AS A9, '0' AS A10,'0' AS A11,'0' AS A12,'0' AS A13,
             '0' AS A14,'0' AS A15,'0' AS A16,'0' AS A17,'0' AS A18,'0' AS A19,
             '0' AS A20,'0' AS A21,'0' AS A22,'0' AS A23,'0' AS A24,'0' AS A25
        FROM DUAL;

  WHEN ex_Range THEN
    OPEN c_Cursor FOR
      SELECT '-30003 Input Time Error' AS A1,'0' AS A2,
             '0' AS A3, '0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7,
             '0' AS A8, '0' AS A9, '0' AS A10,'0' AS A11,'0' AS A12,'0' AS A13,
             '0' AS A14,'0' AS A15,'0' AS A16,'0' AS A17,'0' AS A18,'0' AS A19,
             '0' AS A20,'0' AS A21,'0' AS A22,'0' AS A23,'0' AS A24,'0' AS A25
        FROM DUAL;

  WHEN ex_Internal THEN
    OPEN c_Cursor FOR
      SELECT '-30009 Internal Error' AS A1,'0' AS A2,
             '0' AS A3, '0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7,
             '0' AS A8, '0' AS A9, '0' AS A10,'0' AS A11,'0' AS A12,'0' AS A13,
             '0' AS A14,'0' AS A15,'0' AS A16,'0' AS A17,'0' AS A18,'0' AS A19,
             '0' AS A20,'0' AS A21,'0' AS A22,'0' AS A23,'0' AS A24,'0' AS A25
        FROM DUAL;

  WHEN OTHERS THEN
    v_ErrCode := SQLCODE;
    v_ErrMsg := SUBSTR(SQLERRM,1,200);
    OPEN c_Cursor FOR
      SELECT TO_CHAR(v_ErrCode) || ' ' || v_ErrMsg AS A1,'0' AS A2,
             '0' AS A3, '0' AS A4, '0' AS A5, '0' AS A6, '0' AS A7,
             '0' AS A8, '0' AS A9, '0' AS A10,'0' AS A11,'0' AS A12,'0' AS A13,
             '0' AS A14,'0' AS A15,'0' AS A16,'0' AS A17,'0' AS A18,'0' AS A19,
             '0' AS A20,'0' AS A21,'0' AS A22,'0' AS A23,'0' AS A24,'0' AS A25
        FROM DUAL;

END P_CONSOLE_SKILLTRAFFIC;
/

CREATE OR REPLACE PROCEDURE P_CONSOLE_VDNTRAFFIC
/*
  Description : VDN Traffic Report
  Author      : Guanqing Chen KF71575
  Date        : 2012-05-10
  Version     :
  Callee      : F_RPT_VDNTRAFFIC
                F_RPT_VDNTRAFFIC_P
                F_RPT_VDNTRAFFIC_D
                F_RPT_VDNTRAFFIC_M
                F_RPT_VDNTRAFFIC_W
  History     :
    1.  Date         : 2012-05-10
        Author       : Guanqing Chen KF71575
        Modification : Modification of code standardization
*/
(
  c_Cursor         OUT    xpp_DB170.T_RetDataSet, -- cursor return value
  i_StartDate             VARCHAR2,               -- query begin time
  i_EndDate               VARCHAR2,               -- query end time
  i_TimeUnit              INT,
  i_CCID_VDN              VARCHAR2,               -- vdn
  i_VDNUserName           VARCHAR2,               -- rpt user
  i_RptType               INT                     -- rpt type(0:real-time, 1:interval, 2:daily, 3:monthly, 4:weekly)
 )
AS
  v_UserName              VARCHAR2(60);
  v_StartDate             DATE;                   -- query begin time
  v_EndDate               DATE;                   -- query end time
  v_StartTime             DATE;                   -- query begin time for real-time daylog
  v_EndTime               DATE;                   -- query end time for real-time daylog
  v_StartTime_p           DATE;                   -- query begin time for interval daylog
  v_EndTime_p             DATE;                   -- query end time for interval daylog
  v_Ret                   INT;
  v_Num                   INT;
  v_ErrCode               INT;
  v_ErrMsg                VARCHAR2(200);
  ex_User                 EXCEPTION;
  ex_InPut                EXCEPTION;
  ex_Range                EXCEPTION;
  ex_Internal             EXCEPTION;
BEGIN
  -- check input time
  v_Ret := F_UTL_VALIDATEDATE(i_StartDate, i_EndDate, i_RptType, 1);
  IF v_Ret <> 0 THEN
    RAISE ex_Range;
  END IF;

  -- Format time type
  v_Ret := F_UTL_FORMATDATE(i_StartDate, i_RptType, i_TimeUnit, v_StartDate);
  IF v_Ret = 1 THEN
    RAISE ex_Range;
  END IF;

  v_Ret := F_UTL_FORMATDATE(i_EndDate, i_RptType, i_TimeUnit, v_EndDate);
  IF v_Ret = 1 THEN
    RAISE ex_Range;
  END IF;

  -- check the user privilege
  v_Num := F_UTL_VALIDATEUSER(i_VDNUserName, v_UserName);
  IF v_Num = 0 THEN
    RAISE ex_User;
  END IF;

  -- split vdn
  v_Ret := F_UTL_CCVDNHELP(i_CCID_VDN, NULL, v_UserName);
  IF v_Ret <> 0 THEN
      RAISE ex_Input;
  END IF;

  IF i_RptType = 1 THEN
    -- calculate begin time and end time
    v_Ret := F_UTL_CALCULATEQUERYTIME(v_StartDate, v_EndDate,
                                      v_StartTime, v_EndTime, v_StartTime_p, v_EndTime_p);
    IF v_Ret = 1 THEN
      RAISE ex_Internal;
    END IF;

    IF (v_StartTime IS NOT NULL) OR (v_EndTime IS NOT NULL) THEN
      v_Ret := F_RPT_VDNTRAFFIC(v_StartTime, v_EndTime, i_TimeUnit, v_UserName);
      IF v_Ret = 1 THEN
        RAISE ex_Internal;
      END IF;
    END IF;

    IF (v_StartTime_p IS NOT NULL) OR (v_EndTime_p IS NOT NULL) THEN
      v_Ret := F_RPT_VDNTRAFFIC_P(v_StartTime_p, v_EndTime_p, i_TimeUnit, v_UserName);
      IF v_Ret = 1 THEN
        RAISE ex_Internal;
      END IF;
    END IF;

    UPDATE T_TMP_VDNTRAFFIC
       SET STATTIME = F_UTL_UPDATEQUERYTIME(v_StartDate, v_EndDate, i_TimeUnit)
     WHERE i_TimeUnit = 0
       AND SID IN (8, 9)
       AND VDNUSERNAME = v_UserName;

  ELSIF i_RptType = 2 THEN
    v_Ret := F_RPT_VDNTRAFFIC_D(v_StartDate, v_EndDate, v_UserName);

  ELSIF i_RptType = 3 THEN
    v_Ret := F_RPT_VDNTRAFFIC_M(v_StartDate, v_EndDate, v_UserName);

  ELSIF i_RptType = 4 THEN
    v_Ret := F_RPT_VDNTRAFFIC_W(v_StartDate, v_EndDate, v_UserName);

  END IF;

  IF v_Ret = 1 THEN
    RAISE ex_Internal;
  END IF;

  -- cursor return value
  OPEN c_Cursor FOR
    SELECT STATTIME AS STATTIME ,                                        -- time segment
           NVL(SUM(INCALLNUM) , 0) AS INBOUNDCALLS,                         -- inbound calls
           NVL(SUM(ANSNUM) , 0) AS ANSWEREDCALLS,                            -- answered calls
           TO_CHAR(DECODE(NVL(SUM(INCALLNUM), 0), 0, 0,
               NVL(SUM(ANSNUM), 0) * 100.0 / SUM(InCallNum)), '990.99') || '%' AS ANSWERRATE,            -- answer rate
           NVL(SUM(AUTOINCALLNUM) , 0) AS  IVRINBOUNDCALLS,                     -- ivr inbound calls
           NVL(SUM(AUTOANSNUM) , 0) AS IVRANSWEREDCALLS,                        -- ivr answered calls
           TO_CHAR(DECODE(NVL(SUM(AUTOINCALLNUM) , 0), 0, 0,
               NVL(SUM(AUTOANSNUM) , 0) * 100.0 / SUM(AUTOINCALLNUM)), '990.99') || '%'AS  IVRANSWERRATE,   -- ivr answer rate
           NVL(SUM(AGENTINCALLNUM) , 0) AS SKILLINBOUNDCALLS,                    -- skill inbound calls
           NVL(SUM(AGENTANSNUM) , 0) AS SKILLANSWEREDCALLS,                       -- skill answered calls
           TO_CHAR(DECODE(NVL(SUM(AGENTINCALLNUM) , 0), 0, 0,
               NVL(SUM(AGENTANSNUM) , 0) * 100.0 / SUM(AGENTINCALLNUM)), '990.99') || '%' AS SKILLANSWERRATE, -- skill answer rate
           F_UTL_FORMATTIME(DECODE(NVL(SUM(INCALLNUM), 0), 0, 0,
               NVL(SUM(INVDNTIME), 0) / SUM(INCALLNUM) )) AS AVGINBOUNDVDNTIME,  -- avg. inbound vdn time
           NVL(SUM(OUTCALLNUM) , 0) AS OUTBOUNDCALLS,                        -- outbound calls
           NVL(SUM(OUTANSNUM) , 0) AS OUTBOUNDANSWEREDCALLS,    -- outbound answered calls
           NVL(SUM(OUTAUTOANSNUM) , 0) AS OUTBOUNDIVRANSWERDCALLS,                     -- outboud ivr answered calls
           NVL(SUM(OUTAGENTANSNUM) , 0) AS OUTBOUNDSKILLANSWEREDCALLS,                    -- outbound skill answered calls
           F_UTL_FORMATTIME(DECODE(NVL(SUM(OUTCALLNUM) , 0), 0, 0,
               NVL(SUM(OUTVDNTIME) , 0) / SUM(OUTCALLNUM) )) AS AVGOUTBOUNDVDNTIME -- avg. outbound vdn time
      FROM T_TMP_VDNTRAFFIC
     WHERE VDNUSERNAME = v_UserName
     GROUP BY DECODE(SID, 9, 8, SID), STATTIME
    HAVING (SUM(INCALLNUM) <> 0 OR SUM(OUTCALLNUM) <> 0)
     ORDER BY DECODE(SID, 9, 8, SID), STATTIME;

  --delete data from temporary table
  DELETE FROM T_TMP_CCIDVDN WHERE USERNAME = v_UserName;
  DELETE FROM T_TMP_VDNTRAFFIC WHERE VDNUSERNAME = v_UserName;
  COMMIT;

  RETURN;

EXCEPTION
  WHEN ex_User THEN
    OPEN c_Cursor FOR
      SELECT '-30001 Access Not Authorized' AS A1, '0' AS A2, '0' AS A3, '0' AS A4, '0' AS A5,
             '0' AS A6, '0' AS A7, '0' AS A8, '0' AS A9, '0' AS A10, '0' AS A11,
             '0' AS A12, '0' AS A13, '0' AS A14, '0' AS A15, '0' AS A16
        FROM DUAL;
  WHEN ex_InPut THEN
    OPEN c_Cursor FOR
      SELECT '-30002 Input Parameter Error' AS A1, '0' AS A2, '0' AS A3, '0' AS A4, '0' AS A5,
             '0' AS A6, '0' AS A7, '0' AS A8, '0' AS A9, '0' AS A10, '0' AS A11,
             '0' AS A12, '0' AS A13, '0' AS A14, '0' AS A15, '0' AS A16
        FROM DUAL;

  WHEN ex_Range THEN
    OPEN c_Cursor FOR
      SELECT '-30003 Input Time Error' AS A1, '0' AS A2, '0' AS A3, '0' AS A4, '0' AS A5,
             '0' AS A6, '0' AS A7, '0' AS A8, '0' AS A9, '0' AS A10, '0' AS A11,
             '0' AS A12, '0' AS A13, '0' AS A14, '0' AS A15, '0' AS A16
        FROM DUAL;

  WHEN ex_Internal THEN
    OPEN c_Cursor FOR
      SELECT '-30009 Internal Error' AS A1, '0' AS A2, '0' AS A3, '0' AS A4, '0' AS A5,
             '0' AS A6, '0' AS A7, '0' AS A8, '0' AS A9, '0' AS A10, '0' AS A11,
             '0' AS A12, '0' AS A13, '0' AS A14, '0' AS A15, '0' AS A16
        FROM DUAL;

  WHEN OTHERS THEN
    v_ErrCode := SQLCODE;
    v_ErrMsg := SUBSTR(SQLERRM,1,200);
    OPEN c_Cursor FOR
      SELECT TO_CHAR(v_ErrCode) || ' ' || v_ErrMsg AS A1, '0' AS A2, '0' AS A3, '0' AS A4, '0' AS A5,
             '0' AS A6, '0' AS A7, '0' AS A8, '0' AS A9, '0' AS A10, '0' AS A11,
             '0' AS A12, '0' AS A13, '0' AS A14, '0' AS A15, '0' AS A16
        FROM DUAL;

END P_CONSOLE_VDNTRAFFIC;
/