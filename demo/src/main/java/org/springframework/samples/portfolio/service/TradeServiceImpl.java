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
package org.springframework.samples.portfolio.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.samples.portfolio.model.Portfolio;
import org.springframework.samples.portfolio.vo.PortfolioPosition;
import org.springframework.samples.portfolio.vo.Trade;
import org.springframework.samples.portfolio.vo.Trade.TradeAction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 交易服务
 * @author pengfei
 *
 */
@Service
public class TradeServiceImpl implements TradeService {

	private static final Log logger = LogFactory.getLog(TradeServiceImpl.class);

	private final SimpMessageSendingOperations messagingTemplate;

	public final PortfolioService portfolioService;

	private final List<TradeResult> tradeResults = new CopyOnWriteArrayList<>();


	@Autowired
	public TradeServiceImpl(SimpMessageSendingOperations messagingTemplate, PortfolioService portfolioService) {
		this.messagingTemplate = messagingTemplate;
		this.portfolioService = portfolioService;
	}

	/**
	 * 执行一笔交易.
	 * 在真实的应用程序中执行贸易可能是外部系统,或异步行为。
	 */
	public void executeTrade(Trade trade) {

		String username = trade.getUsername();
		Portfolio portfolio = this.portfolioService.findPortfolio(username);
		String ticker = trade.getTicker();
		int sharesToTrade = trade.getShares();

		PortfolioPosition newPosition = (trade.getAction() == TradeAction.Buy) ?
				portfolio.buy(ticker, sharesToTrade) : portfolio.sell(ticker, sharesToTrade);

		if (newPosition == null) {
			String payload = "Rejected trade " + trade;
			this.messagingTemplate.convertAndSendToUser(username, "/queue/errors", payload);
			return;
		}

		Double funds = this.portfolioService.getfunds(username);
		this.messagingTemplate.convertAndSendToUser(username, "/queue/funds-updates", funds);
		this.tradeResults.add(new TradeResult(username, newPosition));
	}

	/**
	 * 设定交易通知
	 */
	@Scheduled(fixedDelay=1500)
	public void sendTradeNotifications() {

		for (TradeResult result : this.tradeResults) {
			if (System.currentTimeMillis() >= (result.timestamp + 1500)) {
				logger.debug("Sending position update: " + result.position);
				this.messagingTemplate.convertAndSendToUser(result.user, "/queue/position-updates", result.position);
				this.tradeResults.remove(result);
			}
		}
	}

	/**
	 * 交易结果
	 * @author pengfei
	 *
	 */
	private static class TradeResult {
		/**
		 * 用户
		 */
		private final String user;
		/**
		 * 资产
		 */
		private final PortfolioPosition position;
		/**
		 * 余额
		 */
		private final double funds;
		/**
		 * 时间戳
		 */
		private final long timestamp;

		public TradeResult(String user, double funds, PortfolioPosition position) {
			this.user = user;
			this.funds = funds;
			this.position = position;
			this.timestamp = System.currentTimeMillis();
		}
		public TradeResult(String user, PortfolioPosition position) {
			this.user = user;
			this.funds = 0;
			this.position = position;
			this.timestamp = System.currentTimeMillis();
		}
	}

}
