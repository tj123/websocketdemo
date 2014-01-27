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
package org.springframework.samples.portfolio.vo;

import org.springframework.samples.portfolio.service.StockQuoteGenerator;

/**
 * 资产
 * @author pengfei
 *
 */
public class PortfolioPosition {

	/**
	 * 公司
	 */
	private final String company;

	/**
	 * 标签
	 */
	private final String ticker;

	/**
	 * 价格
	 */
//	private final double price;

	/**
	 * 持有份额
	 */
	private final int shares;
	
	/**
	 * 已投入资本
	 */
	private final double capital;

	/**
	 * 更新时间
	 */
	private final long updateTime;

	/**
	 * 创建一个资产
	 * @param company
	 * @param ticker
	 * @param shares
	 */
	public PortfolioPosition(String company, String ticker, int shares) {
		this.company = company;
		this.ticker = ticker;
		this.shares = shares;
		this.capital = getPrice() * shares;
		this.updateTime = System.currentTimeMillis();
	}

	/**
	 * 生成一个新的资产.
	 * @param other
	 * @param sharesToAddOrSubtract
	 */
	public PortfolioPosition(PortfolioPosition other, int sharesToAddOrSubtract) {
		this.company = other.company;
		this.ticker = other.ticker;
		this.shares = other.shares + sharesToAddOrSubtract;
		this.capital = other.capital + getPrice() * sharesToAddOrSubtract;
		this.updateTime = System.currentTimeMillis();
	}

	public String getCompany() {
		return this.company;
	}

	public String getTicker() {
		return this.ticker;
	}

	public double getPrice() {
		return StockQuoteGenerator.getPrice(ticker).doubleValue();
	}

	public int getShares() {
		return this.shares;
	}

	public long getUpdateTime() {
		return this.updateTime;
	}
	
	public double getCapital() {
		return this.capital;
	}

	@Override
	public String toString() {
		return "PortfolioPosition [company=" + this.company + ", ticker=" + this.ticker
				+ ", price=" + getPrice() + ", shares=" + this.shares + "]";
	}

}
