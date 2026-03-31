package com.app.mart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类
 * 处理权限认证、接口放行、JWT 过滤器等
 *
 * @author LunaMart
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()

                // 开放接口（无需登录）
                .antMatchers(
                        "/user/login",
                        "/user/register",
                        "/user/sendCode",
                        "/chat/ws/**"
                ).permitAll()

                // Swagger 接口文档
                .antMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v2/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll()

                // 商品和分类开放接口
                .antMatchers("/category/list").permitAll()
                .antMatchers("/goods/list").permitAll()
                .antMatchers("/goods/{id}").permitAll()
                .antMatchers("/goods/merchant/**").permitAll()
                .antMatchers("/merchant/{merchantId}").permitAll()
                .antMatchers("/goods/image/list/**").permitAll()

                // 超级管理员权限
                .antMatchers("/user/manage/**").hasRole("SUPER_ADMIN")
                .antMatchers("/merchant/freeze/**").hasRole("SUPER_ADMIN")
                .antMatchers("/category").hasRole("SUPER_ADMIN")
                .antMatchers("/category/{id}").hasRole("SUPER_ADMIN")

                // 管理员权限
                .antMatchers("/merchant/list", "/merchant/audit/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .antMatchers("/goods/audit/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .antMatchers("/goods/ai-audit/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .antMatchers("/order/log/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .antMatchers("/after/sale/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .antMatchers("/ai/audit/log/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // 商家权限
                .antMatchers("/order/merchant/**").hasRole("MERCHANT")
                .antMatchers("/after/sale/merchant/**").hasRole("MERCHANT")
                .antMatchers("/goods/save").hasRole("MERCHANT")
                .antMatchers("/goods/update").hasRole("MERCHANT")
                .antMatchers("/goods/submit-ai-audit/**").hasRole("MERCHANT")
                .antMatchers("/goods/offline/**").hasRole("MERCHANT")
                .antMatchers("/goods/my").hasRole("MERCHANT")
                .antMatchers("/goods/image","/goods/image/*").hasRole("MERCHANT")

                // 登录即可访问
                .antMatchers("/address/**").authenticated()
                .antMatchers("/favorite/**").authenticated()
                .antMatchers("/merchant/apply", "/merchant/my", "/merchant/update").authenticated()
                .antMatchers("/goods/comment/**").authenticated()
                .antMatchers("/cart/**").authenticated()
                .antMatchers("/order/**").authenticated()
                .antMatchers("/order/item/**").authenticated()
                .antMatchers("/after/sale/apply", "/after/sale/user/list").authenticated()
                .antMatchers("/chat/**").authenticated()
                .antMatchers("/ai/**").authenticated()

                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable();

        return http.build();
    }
}