package com.example.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.webapp.service.CustomUserDetailsService;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Bean
    @Profile("prod")
    public PasswordEncoder productionPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    

    @Profile("dev")
    public PasswordEncoder devPasswordEncoder() {
        return new DevPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeRequests(auth -> auth
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .antMatchers("/","/error" ,"/home", "/products", "/login", "/register", "/h2-console/**").permitAll()
                .antMatchers("/products/search").permitAll()
                .antMatchers("/products/check-availability", "/products/compare-prices", "/products/recommendations").authenticated()
                .antMatchers("/products/manage/**", "/products/add/**", "/products/edit/**", "/products/delete/**").hasAnyRole("SELLER", "ADMIN")
                .antMatchers("/users/*/preferences/**", "/users/preferences/**").hasAnyRole("USER","SELLER","ADMIN")
                .antMatchers("/users/**").hasRole("ADMIN")
                .antMatchers("/seller/revenue/**").hasRole("SELLER")
                .antMatchers("/uploads/**").hasRole("SELLER")
                .anyRequest().authenticated()
            )
            .userDetailsService(userDetailsService)
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .permitAll()
            )
            .logout(logout -> logout
                .permitAll()
                .logoutSuccessUrl("/")
            );
            
        return http.build();
    }
} 
