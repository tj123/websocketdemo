/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.portfolio.config;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 自定义Spring安全配置。
 *
 * @author Rob Winch
 */
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	/**
	 * 配置URL
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable() //TODO Refactor login form
			.authorizeRequests()
				.antMatchers("/assets/**").permitAll()
				.anyRequest().authenticated()
				.and()
			.logout()
				.logoutSuccessUrl("/login.html?logout")
				.logoutUrl("/logout.html")
				.permitAll()
				.and()
			.formLogin()
				.defaultSuccessUrl("/index.html")
				.loginPage("/login.html")
				.failureUrl("/login.html?error")
				.permitAll();
	}

	/**
	 * 配置账号和密码
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.inMemoryAuthentication()
				.withUser("sybn").password("111").roles("USER").and()
				.withUser("admin").password("111").roles("ADMIN","USER");
//		auth.jdbcAuthentication().
	}
}