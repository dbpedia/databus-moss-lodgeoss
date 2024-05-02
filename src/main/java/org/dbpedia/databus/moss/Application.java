package org.dbpedia.databus.moss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.core.SpringVersion;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.core.SpringVersion;

/**
 * The entry point of the Spring Boot application.
 */

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    

    // @EnableGlobalMethodSecurity(prePostEnabled = true)
    // public static class SecurityConfig extends WebSecurityConfigurerAdapter {
    //     protected void configure(final HttpSecurity http) throws Exception {
    //         http.authorizeRequests()
    //             .anyRequest().authenticated()
    //             .and()
    //             .oauth2ResourceServer().;
    //     }
    // }

}
