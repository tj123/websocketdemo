/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.portfolio.service;

import org.springframework.samples.portfolio.model.Portfolio;
import org.springframework.samples.portfolio.vo.PortfolioPosition;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rob Winch 
 */
@Service
public class PortfolioServiceImpl implements PortfolioService {

	/**
	 * 用户资产表 (模拟数据库)
	 */
	// user -> Portfolio
	private final Map<String, Portfolio> portfolioLookup = new HashMap<>();
	private final Map<String, Double> fundsLookup = new HashMap<>();

	/**
	 * 构造函数,初始化资产 (模拟数据库初始数据)
	 */
	public PortfolioServiceImpl() {

		Portfolio portfolio = new Portfolio();
		portfolio.addPosition(new PortfolioPosition("Citrix Systems, Inc.", "CTXS", 0));
		portfolio.addPosition(new PortfolioPosition("Dell Inc.", "DELL", 0));
		portfolio.addPosition(new PortfolioPosition("Microsoft", "MSFT", 0));
		portfolio.addPosition(new PortfolioPosition("Oracle", "ORCL", 100));
		portfolio.setfunds(9000D);
		this.portfolioLookup.put("sybn", portfolio);
		this.fundsLookup.put("sybn", 9000D);

		portfolio = new Portfolio();
		portfolio.addPosition(new PortfolioPosition("EMC Corporation", "EMC", 10));
		portfolio.addPosition(new PortfolioPosition("Google Inc", "GOOG", 10));
		portfolio.addPosition(new PortfolioPosition("VMware, Inc.", "VMW", 10));
		portfolio.addPosition(new PortfolioPosition("Red Hat", "RHT", 100));
		portfolio.setfunds(9000D);
		this.portfolioLookup.put("admin", portfolio);
		this.fundsLookup.put("sybn", 9000D);
	}

	/**
	 * 获取某人的资产
	 */
	public Portfolio findPortfolio(String username) {
		Portfolio portfolio = portfolioLookup.get(username);
		if (portfolio == null) {
			throw new IllegalArgumentException(username);
		}
		return portfolio;
	}
	
	/**
	 * 获取账户余额
	 */
	public Double getfunds(String username) {
		Double funds = fundsLookup.get(username);
		if (fundsLookup == null) {
			throw new IllegalArgumentException(username);
		}
		return funds;
	}
	/**
	 * 设置账户余额
	 */
	public void setfunds(String username, Double funds) {
		fundsLookup.put(username, funds);
		return;
	}
	/**
	 * 设置账户余额
	 */
	public void incfunds(String username, Double margin) {
		Double funds = fundsLookup.get(username);
		if (fundsLookup == null) {
			throw new IllegalArgumentException(username);
		}
		fundsLookup.put(username, funds + margin);
		return;
	}

}
