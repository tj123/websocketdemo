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
package org.springframework.samples.portfolio.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.portfolio.service.PortfolioService;
import org.springframework.stereotype.Service;


/**
 * 用户资产表 (瞬时对象)
 * @author pengfei
 *
 */
@Service
public class PortfolioHelper {
	private static PortfolioService portfolioService;
	@Autowired 
	public PortfolioHelper(PortfolioService service) {
		portfolioService = service;
	}
	/**
	 * 获取账户余额
	 * @return 
	 */
	public static Double getfunds(String username) {
		return portfolioService.getfunds(username);
	}
	/**
	 * 设置账户余额
	 */
	public static void setfunds(String username, Double funds) {
		portfolioService.setfunds(username, funds);
	}
}
