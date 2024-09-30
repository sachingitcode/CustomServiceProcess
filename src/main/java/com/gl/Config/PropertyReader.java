/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gl.Config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class PropertyReader {

    private InputStream inputStream;
    Properties prop;
    static Logger logger = LogManager.getLogger(PropertyReader.class);

    public String getConfigPropValue(String key) throws IOException {

        if (System.getenv("commonConfigurationFilePath") == null) {
            prop = loadProperties(System.getenv("commonConfigurationFile"));
        } else {
            prop = loadProperties(System.getenv("commonConfigurationFilePath"));
        }
        if (Objects.nonNull(prop)) {
            return prop.getProperty(key);
        } else {
            return null;
        }
    }

    Properties loadProperties(String propFileName) {
        try {
            prop = new Properties();
            inputStream = new FileInputStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        } catch (IOException io) {
            logger.error(io.toString(), (Throwable) io);
        }
        return prop;
    }
}
