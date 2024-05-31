package com.mike.securingweb;

import com.mike.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "/announcement", "/En/**", "/registration", "/fee/**", "/publications/**", "/program/**", "/contacts/**", "/activate/*", "/passwordReset",
                        "/passwordChange/*", "/passwordChange", "/2021/**", "/2022/**", "/2023/**", "/static**", "/static/**").permitAll()
                .antMatchers("/admin/**").not().hasAuthority("USER")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/personal", true)
                .and()
                .rememberMe()
                .and()
                .logout()
                .logoutSuccessUrl("/")
                .permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService)
                .passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/css/**");
        web.ignoring().antMatchers("/js/**");
        web.ignoring().antMatchers("/img/**");
        web.ignoring().antMatchers("/templates/meso2021/**");
        web.ignoring().antMatchers("/templates/meso2022/**");
        web.ignoring().antMatchers("/templates/meso2023/**");
        web.ignoring().antMatchers("/thesis/**");
    }
}
