package com.gl.P5Process;

import java.sql.Connection;

import static com.gl.P5Process.QueryExecuter.runQuery;

public class CustomImeiPairProcess {

    public static void p5(Connection conn) {
        var status = "1";
        var source = "AUTO";
        var table = "gdce_data";
        var remark = "GDCE_TAX_PAID";
        var lastRunTime = "gdce_register_imei_update_last_run_time";
        updateNwl(conn, table, lastRunTime, status);
        startService(conn, table, lastRunTime, remark, source, status);
    }


    public static void startService(Connection conn, String table, String lastRunTime, String remark, String source, String status) {
        // gdce_data
        var subQuery = " select a.imei  from " + table + " a, imei_pair_detail b " +
                " where a.imei=b.imei and a.created_on >= ( select IFNULL(value, '2000-01-01') from sys_param where tag ='" + lastRunTime + "' ) ";

        insertNwlFromGdceOnPairRecordTime(conn, lastRunTime, status);
        insertInImeiPairHis(conn, subQuery, remark);

        insertInExceptionListHis(conn, subQuery, remark);
        deleteFromEXceptionList(conn, subQuery);

        insertInBlackListHis(conn, subQuery, remark, source);
        deleteFromBlackList(conn, subQuery, source);
        removeAutoFromBlackList(conn, subQuery, source);
        deleteFromImeiPair(conn, " select imei from " + table + " ");
        updateGdceDateTime(conn, lastRunTime);

    }

    public static void updateNwl(Connection conn, String table, String lastRunTime, String status) {
        var q = "update app.national_whitelist set gdce_imei_status = " + status + " , gdce_modified_time =CURRENT_TIMESTAMP" +
                "  where gdce_imei_status in (0,3)   and imei in( select imei from app." + table + " where created_on >= ( select IFNULL(value, '2000-01-01') from sys_param where tag ='" + lastRunTime + "' )  )  ";
        runQuery(conn, q);
    }

    private static void insertNwlFromGdceOnPairRecordTime(Connection conn, String lastRunTime, String status) {
        String a = "insert into  app.national_whitelist(action,actual_imei,actual_operator,created_filename,created_on_date,failed_rule_date, " +
                "failed_rule_id,failed_rule_name,feature_name,  imei,imei_arrival_time,imsi,is_used_device_imei,mobile_operator, " +
                "msisdn,period, raw_cdr_file_name,record_time,record_type,server_origin,source,system_type,tac,tax_paid, is_test_imei, " +
                "updated_filename,update_imei_arrival_time,update_raw_cdr_file_name,update_source ,gdce_imei_status,gdce_modified_time) " +
                "select action,actual_imei,actual_operator,create_filename,created_on,failed_rule_date, " +
                "failed_rule_id,failed_rule_name,feature_name, imei,imei_arrival_time,imsi, is_used,mobile_operator, " +
                "msisdn,period,raw_cdr_file_name,record_time,record_type,server_origin,source,system_type,tac,tax_paid,is_test_imei, " +
                "update_filename,update_imei_arrival_time,update_raw_cdr_file_name,update_source , " + status + ", CURRENT_TIMESTAMP" +
                " from active_unique_imei  where imei in(select distinct imei from imei_pair_detail where imei in " +
                "(SELECT  gdce_data.imei FROM gdce_data  LEFT JOIN national_whitelist on gdce_data.imei = national_whitelist.imei WHERE  national_whitelist.imei IS NULL" +
                " and  imei_pair_detail.record_time is not null and gdce_data.created_on >= ( select IFNULL(value, '2000-01-01') from sys_param where tag ='" + lastRunTime + "' )  ))   ";
        runQuery(conn, a);
    }

    private static void updateGdceDateTime(Connection conn, String lastRunTime) {
        String a = "update sys_param set value =CURRENT_TIMESTAMP where tag ='" + lastRunTime + "' ";
        runQuery(conn, a);
    }

    private static void insertInExceptionListHis(Connection conn, String subquery, String remark) { //GDCE_TAX_PAID
        String q = "insert into exception_list_his (actual_imei, imei,imsi , msisdn ,operator_id , operator_name, complaint_type, expiry_date , mode_type , request_type , txn_id , user_id , user_type ,tac ,remark,source ,action ,action_remark ,operation )" +
                " select actual_imei, imei,imsi , msisdn ,operator_id , operator_name, complaint_type, expiry_date , mode_type , request_type , txn_id , user_id , user_type ,tac ,remark,'" + remark + "' ,'DELETE','" + remark + "' , 0 from exception_list " +
                " where imei in( " + subquery + " )";
        runQuery(conn, q);
    }

    private static void deleteFromEXceptionList(Connection conn, String subquery) {
        String q = "delete from exception_list  where imei in (" + subquery + ")  ";
        runQuery(conn, q);
    }

    private static void insertInImeiPairHis(Connection conn, String subquery, String remark) {
        String q = "insert into imei_pair_detail_his ( allowed_days,imei ,imsi,msisdn,pairing_date ,record_time,file_name,gsma_status,pair_mode, operator,expiry_date , action,action_remark) " +
                " select allowed_days,imei ,imsi,msisdn, pairing_date ,record_time, file_name,gsma_status, pair_mode, operator,expiry_date ,'DELETE','" + remark + "' from  imei_pair_detail " +
                "where imei in( " + subquery + " )";
        runQuery(conn, q);
    }

    private static void deleteFromImeiPair(Connection conn, String subquery) {
        String q = "delete from  imei_pair_detail  where imei in (" + subquery + ")  ";
        runQuery(conn, q);
    }

    private static void removeAutoFromBlackList(Connection conn, String subquery, String source) {
        String q = "UPDATE black_list SET source = TRIM(BOTH ',' FROM REPLACE(CONCAT(',', source, ','), '," + source + ",', ','))WHERE source LIKE '%" + source + "%'  and  imei in(" + subquery + ")  ";
        runQuery(conn, q);
    }

    private static void insertInBlackListHis(Connection conn, String subquery, String remark, String source) {
        String q = "insert into black_list_his (actual_imei, imei,imsi , msisdn ,operator_id , operator_name, complaint_type, expiry_date , mode_type , request_type , txn_id , user_id , user_type ,tac ,remark,source ,action ,action_remark ) " +
                " select actual_imei, imei,imsi , msisdn ,operator_id , operator_name, complaint_type, expiry_date , mode_type , request_type , txn_id , user_id , user_type ,tac ,remark, source ,'DELETE','" + remark + "' from black_list" +
                " where imei in(" + subquery + ") and source='" + source + "' ";
        runQuery(conn, q);
    }

    private static void deleteFromBlackList(Connection conn, String subquery, String source) {
        String q = "delete from  black_list  where imei in (" + subquery + ") and source='" + source + "' ";
        runQuery(conn, q);
    }
}
