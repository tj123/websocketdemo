/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.samples.portfolio.web.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.charset.Charset;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.samples.portfolio.config.WebSocketConfig;
import org.springframework.samples.portfolio.vo.Trade;
import org.springframework.samples.portfolio.web.TestPrincipal;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.JsonPathExpectationsHelper;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Tests for PortfolioController that rely on the Spring TestContext framework to
 * load the actual Spring configuration.
 *
 * Tests can create a Spring {@link org.springframework.messaging.Message} that
 * represents a STOMP frame and send it directly to the "clientInboundChannel"
 * for processing. In effect this bypasses the phase where a WebSocket message
 * is received and parsed. Instead tests must set the session id and user
 * headers of the Message.
 *
 * Test ChannelInterceptor implementations are installed on the "brokerChannel"
 * and the "clientOutboundChannel" in order to detect messages sent through
 * them. Although not the case here, often a controller method will
 * not send any messages at all. In such cases it might be necessary to inject
 * the controller with "mock" services in order to assert the outcome.
 *
 * Note the (optional) use of TestConfig, which removes MessageHandler
 * subscriptions to MessageChannel's for all handlers found in the
 * ApplicationContext except the one for the one delegating to annotated message
 * handlers. This is done to reduce additional processing and additional
 * messages not related to the test.
 *
 * The test strategy here is to test the behavior of controllers using the
 * actual Spring configuration while using the TestContext framework ensures
 * that Spring configuration is loaded only once per test class. This strategy
 * is not an end-to-end test and does not replace the need for full-on
 * integration testing -- much like we can write integration tests for the
 * persistence layer, tests here ensure the web layer (including controllers
 * and Spring configuration) are well tested using tests that are a little
 * simpler and easier to write and debug than full, end-to-end integration tests.
 *
 * @author Rossen Stoyanchev
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebSocketConfig.class, ContextPortfolioControllerTests.TestConfig.class })
public class ContextPortfolioControllerTests {

	@Autowired private AbstractSubscribableChannel clientInboundChannel;

	@Autowired private AbstractSubscribableChannel clientOutboundChannel;

	@Autowired private AbstractSubscribableChannel brokerChannel;

	private TestChannelInterceptor clientOutboundChannelInterceptor;

	private TestChannelInterceptor brokerChannelInterceptor;


	@Before
	public void setUp() throws Exception {

		this.brokerChannelInterceptor = new TestChannelInterceptor(false);
		this.clientOutboundChannelInterceptor = new TestChannelInterceptor(false);

		this.brokerChannel.addInterceptor(this.brokerChannelInterceptor);
		this.clientOutboundChannel.addInterceptor(this.clientOutboundChannelInterceptor);
	}


	@Test
	public void getPositions() throws Exception {

		StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
		headers.setSubscriptionId("0");
		headers.setDestination("/app/positions");
		headers.setSessionId("0");
		headers.setUser(new TestPrincipal("fabrice"));
		Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).setHeaders(headers).build();

		this.clientOutboundChannelInterceptor.setIncludedDestinations("/positions");
		this.clientOutboundChannelInterceptor.startRecording();

		this.clientInboundChannel.send(message);

		Message<?> reply = this.clientOutboundChannelInterceptor.awaitMessage(5);
		assertNotNull(reply);

		StompHeaderAccessor replyHeaders = StompHeaderAccessor.wrap(reply);
		assertEquals("0", replyHeaders.getSessionId());
		assertEquals("0", replyHeaders.getSubscriptionId());
		assertEquals("/positions", replyHeaders.getDestination());

		String json = new String((byte[]) reply.getPayload(), Charset.forName("UTF-8"));
		new JsonPathExpectationsHelper("$[0].company").assertValue(json, "Citrix Systems, Inc.");
		new JsonPathExpectationsHelper("$[1].company").assertValue(json, "Dell Inc.");
		new JsonPathExpectationsHelper("$[2].company").assertValue(json, "Microsoft");
		new JsonPathExpectationsHelper("$[3].company").assertValue(json, "Oracle");
	}

	@Test
	public void executeTrade() throws Exception {

		Trade trade = new Trade();
		trade.setAction(Trade.TradeAction.Buy);
		trade.setTicker("DELL");
		trade.setShares(25);

		byte[] payload = new ObjectMapper().writeValueAsBytes(trade);

		StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SEND);
		headers.setDestination("/app/trade");
		headers.setSessionId("0");
		headers.setUser(new TestPrincipal("fabrice"));
		Message<byte[]> message = MessageBuilder.withPayload(payload).setHeaders(headers).build();

		this.brokerChannelInterceptor.setIncludedDestinations("/user/**");
		this.brokerChannelInterceptor.startRecording();

		this.clientInboundChannel.send(message);

		// 超时时限
		Message<?> positionUpdate = this.brokerChannelInterceptor.awaitMessage(500);
		assertNotNull(positionUpdate);

		StompHeaderAccessor positionUpdateHeaders = StompHeaderAccessor.wrap(positionUpdate);
		assertEquals("/user/fabrice/queue/position-updates", positionUpdateHeaders.getDestination());

		String json = new String((byte[]) positionUpdate.getPayload(), Charset.forName("UTF-8"));
		new JsonPathExpectationsHelper("$.ticker").assertValue(json, "DELL");
		new JsonPathExpectationsHelper("$.shares").assertValue(json, 75);
	}


	/**
	 * Configuration class that un-registers MessageHandler's it finds in the
	 * ApplicationContext from the message channels they are subscribed to...
	 * except the message handler used to invoke annotated message handling methods.
	 * The intent is to reduce additional processing and additional messages not
	 * related to the test.
	 */
	@Configuration
	static class TestConfig implements ApplicationListener<ContextRefreshedEvent> {

		@Autowired
		private List<SubscribableChannel> channels;

		@Autowired
		private List<MessageHandler> handlers;


		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			for (MessageHandler handler : handlers) {
				if (handler instanceof SimpAnnotationMethodMessageHandler) {
					continue;
				}
				for (SubscribableChannel channel :channels) {
					channel.unsubscribe(handler);
				}
			}
		}
	}
}