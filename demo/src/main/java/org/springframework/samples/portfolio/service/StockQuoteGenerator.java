package org.springframework.samples.portfolio.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.samples.portfolio.vo.Quote;

/**
 * 报价工厂
 * @author pengfei
 *
 */
public class StockQuoteGenerator {

	/**
	 * 两位小数
	 */
	private static final MathContext mathContext = new MathContext(2);

	/**
	 * 随机数
	 */
	private static Random random = new Random();
	
	/**
	 * 报价表
	 */
	private static Map<String, String> prices = new ConcurrentHashMap<>();

	/**
	 * 初始报价
	 */
	private static void initPrices() {
		prices.put("CTXS", "10.00");
		prices.put("DELL", "10.00");
		prices.put("EMC", "10.00");
		prices.put("GOOG", "10.00");
		prices.put("MSFT", "10.00");
		prices.put("ORCL", "10.00");
		prices.put("RHT", "10.00");
		prices.put("VMW", "10.00");
	}

	/**
	 * 生成一次报价
	 * @return
	 */
	public static Set<Quote> generateQuotes() {
		if (prices == null || prices.isEmpty()) {
			initPrices();
		}
		Set<Quote> quotes = new HashSet<>();
		for (String ticker : prices.keySet()) {
			BigDecimal price = getNewPrice(ticker);
			quotes.add(new Quote(ticker, price));
		}
		return quotes;
	}

	/**
	 * 随机变动一下价格
	 * @param ticker
	 * @return
	 */
	private static BigDecimal getNewPrice(String ticker) {
		BigDecimal seedPrice = getPrice(ticker);
		double range = seedPrice.multiply(new BigDecimal(0.10)).doubleValue();
		BigDecimal priceChange = new BigDecimal(String.valueOf(random.nextDouble() * random.nextDouble() * random.nextDouble() * range));
		if (random.nextDouble() > 0.8) {
			return seedPrice;
		}
		seedPrice = seedPrice.add(priceChange).setScale(2, BigDecimal.ROUND_DOWN);
		prices.put(ticker, seedPrice.toPlainString());
		return seedPrice;
	}

	public static BigDecimal getPrice(String ticker) {
		if (prices == null || prices.isEmpty()) {
			initPrices();
		}
		String string = prices.get(ticker);
		BigDecimal seedPrice = new BigDecimal(string, mathContext);
		return seedPrice;
	}

}