/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gl.Config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.util.text.BasicTextEncryptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;


public class MySQLConnection {

    Logger logger = LogManager.getLogger(MySQLConnection.class);

    public static PropertyReader propertyReader;

    public Connection getConnection() {
        if (Objects.isNull(propertyReader)) {
            propertyReader = new PropertyReader();
        }
        Connection conn = null;
        try {
            String jdbcDriver = propertyReader.getConfigPropValue("jdbc_driver").trim();
            String dbURL = propertyReader.getConfigPropValue("db_url").trim();
            String username = propertyReader.getConfigPropValue("dbUsername").trim();
            String password = decryptor(propertyReader.getConfigPropValue("dbEncyptPassword").trim());
            Class.forName(jdbcDriver);
            conn = DriverManager.getConnection(dbURL, username, password);
            return conn;
        } catch (Exception e) {
            try {
                conn.close();
            } catch (Exception ex) {
                logger.error(" SQLException : " + ex + " :  " + java.time.LocalDateTime.now());
            }
            System.exit(0);
            return null;
        }
    }

    public String decryptor(String encryptedText) {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(System.getenv("JASYPT_ENCRYPTOR_PASSWORD"));
        return encryptor.decrypt(encryptedText);
    }
}