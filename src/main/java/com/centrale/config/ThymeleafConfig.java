package com.centrale.config;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;

import javax.servlet.ServletContext;

public class ThymeleafConfig {
    private static TemplateEngine templateEngine;

    public static TemplateEngine getTemplateEngine(ServletContext servletContext) {
        if (templateEngine == null) {
            ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
            templateResolver.setTemplateMode(TemplateMode.HTML);
            templateResolver.setPrefix("/WEB-INF/templates/");
            templateResolver.setSuffix(".html");
            templateResolver.setCacheTTLMs(3600000L);

            TemplateEngine engine = new TemplateEngine();
            engine.addDialect(new Java8TimeDialect());
            engine.setTemplateResolver(templateResolver);
            templateEngine = engine;
        }
        return templateEngine;
    }
}
