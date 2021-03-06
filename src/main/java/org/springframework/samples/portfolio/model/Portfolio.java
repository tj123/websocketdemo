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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.portfolio.service.PortfolioService;
import org.springframework.samples.portfolio.vo.PortfolioPosition;


/**
 * 用户资产表 (瞬时对象)
 * @author pengfei
 *
 */
public class Portfolio {
	/**
	 * 资产组合
	 */
	private final Map<String,PortfolioPosition> positionLookup = new LinkedHashMap<String,PortfolioPosition>();
	private Double funds = 0D;
	private String userName;

	/**
	 * 获取资产
	 * @return
	 */
	public List<PortfolioPosition> getPositions() {
		return new ArrayList<PortfolioPosition>(positionLookup.values());
	}

	/**
	 * 增加资产
	 * @param position
	 */
	public void addPosition(PortfolioPosition position) {
		this.positionLookup.put(position.getTicker(), position);
	}

	/**
	 * 获取指定资产
	 * @param ticker
	 * @return
	 */
	public PortfolioPosition getPortfolioPosition(String ticker) {
		return this.positionLookup.get(ticker);
	}

	/**
	 * 购入资产
	 * @return the updated position or null
	 */
	public PortfolioPosition buy(String ticker, int sharesToBuy) {
		PortfolioPosition position = this.positionLookup.get(ticker);
		if ((position == null) || (sharesToBuy < 1)) {
			return null;
		}
		double balance = - position.getPrice() * sharesToBuy;
		boolean deductionResults = incfunds(balance);
		if (deductionResults) {
			position = new PortfolioPosition(position, sharesToBuy);
			this.positionLookup.put(ticker, position);
			return position;
		}
		return null;
	}

	/**
	 * 卖出资产
	 * @return the updated position or null
	 */
	public PortfolioPosition sell(String ticker, int sharesToSell) {
		PortfolioPosition position = this.positionLookup.get(ticker);
		if ((position == null) || (sharesToSell < 1) || (position.getShares() < sharesToSell)) {
			return null;
		}
		incfunds(position.getPrice() * sharesToSell);
		position = new PortfolioPosition(position, -sharesToSell);
		this.positionLookup.put(ticker, position);
		return position;
	}

	/**
	 * 获取账户余额
	 */
	public Double getfunds() {
		return this.funds;
	}
	/**
	 * 设置账户余额
	 */
	public void setfunds(Double funds) {
		this.funds = funds;
	}
	/**
	 * 设置账户余额
	 */
	public boolean incfunds(Double balance) {
		this.funds = PortfolioHelper.getfunds(userName);
		if (funds + balance >=0 ) {
			this.funds = getfunds() + balance;
			PortfolioHelper.setfunds(userName, funds);
			return true;
		} else {
			return false;
		}
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
