package com.centrale.util;

import java.io.InputStream;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static volatile SessionFactory sessionFactory;

    private HibernateUtil() {
        // Private constructor to prevent instantiation
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null) {
                    sessionFactory = buildSessionFactory();
                }
            }
        }
        return sessionFactory;
    }

    private static SessionFactory buildSessionFactory() {
        try {
            logger.info("Building Hibernate SessionFactory");
            
            // Load properties from application.properties
            Properties properties = new Properties();
            InputStream inputStream = HibernateUtil.class.getClassLoader().getResourceAsStream("application.properties");
            properties.load(inputStream);

            // Create configuration
            Configuration configuration = new Configuration();
            
            // Load hibernate.cfg.xml
            configuration.configure("hibernate.cfg.xml");
            
            // Override properties with values from application.properties
            configuration.addProperties(properties);

            configuration.setProperty("hibernate.hbm2ddl.auto", "update");

            return configuration.buildSessionFactory();
        } catch (Exception ex) {
            logger.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void shutdown() {
        logger.info("Closing Hibernate SessionFactory");
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
