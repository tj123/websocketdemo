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

import java.util.*;


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
		position = new PortfolioPosition(position, sharesToBuy);
		this.positionLookup.put(ticker, position);
		return position;
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
		position = new PortfolioPosition(position, -sharesToSell);
		this.positionLookup.put(ticker, position);
		return position;
	}

}
