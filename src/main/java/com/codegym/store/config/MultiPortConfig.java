package com.codegym.store.config;

import org.apache.catalina.connector.Connector;

import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultiPortConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return factory -> {
            Connector connector1 = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            connector1.setPort(9090);

            Connector connector2 = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            connector2.setPort(9091);

            factory.addAdditionalConnectors(connector1, connector2);
        };
    }
}
