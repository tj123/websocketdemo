/*
 * Copyright 2002-2014 the original author or authors.
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
package org.springframework.samples.portfolio.service;

import org.springframework.samples.portfolio.model.Portfolio;


/**
 * 资产处理服务
 * @author pengfei
 *
 */
public interface PortfolioService {

	/**
	 * 获取某人的资产
	 * @param username
	 * @return
	 */
	Portfolio findPortfolio(String username);
	/**
	 * 修改某人的资产
	 */
	void setPortfolio(String username, Portfolio portfolio);
	/**
	 * 获取账户余额
	 */
	Double getfunds(String username);
	/**
	 * 设置账户余额
	 */
	void setfunds(String username, Double funds);
	/**
	 * 设置账户余额
	 */
	void incfunds(String username, Double margin);
}
