package com.vmware.processor;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

//    @Bean
//    public static PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Configuration
//    @Slf4j
//    public static class DataConfig extends WebSecurityConfigurerAdapter {
//
//        @Override
//        protected void configure(HttpSecurity http) throws Exception {
//            log.info("configure(http)");
//
//            http
//                    .cors().disable()
//                    .csrf().disable()
//                    .mvcMatcher("/data").authorizeRequests().anyRequest().authenticated()
//                    .and().oauth2ResourceServer().jwt();
//        }
//    }
}
