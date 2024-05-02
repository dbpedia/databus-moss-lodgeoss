package org.dbpedia.databus.moss.services.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;

import java.net.URI;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // private final KeycloakLogoutHandler keycloakLogoutHandler;

    // SecurityConfig(KeycloakLogoutHandler keycloakLogoutHandler) {
    //     this.keycloakLogoutHandler = keycloakLogoutHandler;
    // }


    // @Bean
    // public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
    //     http.authorizeRequests(auth -> auth.requestMatchers(
    //         new AntPathRequestMatcher("/*", HttpMethod.OPTIONS.name())
    //     )
    //         .permitAll()
    //         .requestMatchers(new AntPathRequestMatcher("/secured*"))
    //         .hasRole("user")
    //         .requestMatchers(new AntPathRequestMatcher("/"))
    //         .permitAll()
    //         .anyRequest()
    //         .authenticated());
    //     http.oauth2ResourceServer((oauth2) -> oauth2
    //             .jwt(Customizer.withDefaults()));
    //     http.oauth2Login(Customizer.withDefaults());
    //     // .logout(logout -> logout.addLogoutHandler(keycloakLogoutHandler).logoutSuccessUrl("/"));
    //     return http.build();
    // }

    // @Bean
    // public ClientRegistrationRepository clientRepository() {

    //     ClientRegistration keycloak = keycloakClientRegistration();
    //     return new InMemoryClientRegistrationRepository(keycloak);
    // }

    // private ClientRegistration keycloakClientRegistration() {

    //     return ClientRegistration.withRegistrationId("moss")
    //         .clientId("moss-api")
    //         .clientName("moss-api")
    //         .userNameAttributeName("email")
    //         .clientSecret("MxM6XmpVn8hY3XdK73TgOdIEV8MGXP2w")
    //         .redirectUri("http://localhost:2000/login/oauth2/code/moss-api")
    //         .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    //         .issuerUri("http://localhost:8180/realms/moss")
    //         .authorizationUri("http://localhost:8180/realms/moss/protocol/openid-connect/auth")
    //         .tokenUri("http://localhost:8180/realms/moss/protocol/openid-connect/token")
    //         .userInfoUri("http://localhost:8180/realms/moss/protocol/openid-connect/userinfo")
    //         .scope("openid", "profile")
    //         .jwkSetUri("http://localhost:8180/sso-auth-server/.well-known/jwks.json")
    //         .build();
    // }
}
