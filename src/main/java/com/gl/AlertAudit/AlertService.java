/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.AlertAudit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class AlertService {

    static Logger logger = LogManager.getLogger(AlertService.class);

    public void raiseAnAlert(String alertCode, String alertMessage, String alertProcess, int userId, Connection conn) {
        try (Statement stmt = conn.createStatement();) {
            String sql = "select description from app.cfg_feature_alert where alert_id='" + alertCode + "'";
            logger.info("Fetching alert message by alert id from alertDb " + sql);
            ResultSet rs = stmt.executeQuery(sql);
            String description = "";
            while (rs.next()) {
                description = rs.getString("description");
            }
            description = description.replaceAll("<process_name>", alertProcess)
                    .replaceAll("<e>", alertMessage);
            logger.info("alert message: " + description);
            String sqll = "Insert into app.sys_generated_alert (alert_id,description,status,user_id) values('"  + alertCode + "' , '" + description + "', 0, " + userId + " ) ";
            logger.info("Inserting alert into running alert db" + sqll);
            stmt.executeUpdate(sqll);
        } catch (Exception e) {
            logger.error("Error in raising Alert. So, doing nothing." + e);
        }
    }
}
