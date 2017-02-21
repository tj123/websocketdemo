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

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.samples.portfolio.vo.Quote;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 报价服务
 * @author pengfei
 *
 */
@Service
public class QuoteService implements ApplicationListener<BrokerAvailabilityEvent> {

	private static Log logger = LogFactory.getLog(QuoteService.class);

	private final MessageSendingOperations<String> messagingTemplate;

	private AtomicBoolean brokerAvailable = new AtomicBoolean();


	@Autowired
	public QuoteService(MessageSendingOperations<String> messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	@Override
	public void onApplicationEvent(BrokerAvailabilityEvent event) {
		this.brokerAvailable.set(event.isBrokerAvailable());
	}

	/**
	 * 定时执行(3秒一次)
	 * 
	 */
	@Scheduled(fixedDelay=3000)
	public void sendQuotes() {
		for (Quote quote : StockQuoteGenerator.generateQuotes()) {
			if (logger.isTraceEnabled()) {
				logger.trace("Sending quote " + quote);
			}
			if (this.brokerAvailable.get()) {
				this.messagingTemplate.convertAndSend("/topic/price.stock." + quote.getTicker(), quote);
			}
		}
		logger.error("Sending quote end");
	}

}
