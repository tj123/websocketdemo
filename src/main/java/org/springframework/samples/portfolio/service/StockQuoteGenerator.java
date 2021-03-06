package org.springframework.samples.portfolio.service;

import java.math.BigDecimal;
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
		double doubleValue = seedPrice.doubleValue();
		double rangeLimit = doubleValue * 0.10;
		double range = (random.nextDouble() - random.nextDouble()) * random.nextDouble();
		if (doubleValue < 0.1) {
			// 如果故价过低，则强制提高一次股价，防止股价清零。相当于上市公司重组
			range = range + random.nextDouble() * random.nextDouble() * random.nextDouble() * 10;
		}
		BigDecimal priceChange = new BigDecimal(String.valueOf(range * rangeLimit));
		// 补偿因1.1*0.9=0.99 造成的股价下跌趋势，将下跌行为的0.9修正为0.909，故重置10%的跌幅为0
		if (range <0 && random.nextDouble() >= 0.9) {
			return seedPrice;
		}
		BigDecimal newSeedPrice = seedPrice.add(priceChange).setScale(2, BigDecimal.ROUND_DOWN);
		prices.put(ticker, newSeedPrice.toString());
		return newSeedPrice;
	}

	public static BigDecimal getPrice(String ticker) {
		if (prices == null || prices.isEmpty()) {
			initPrices();
		}
		String string = prices.get(ticker);
		// 为了保证精度建议使用字符串存储并构造BigDecimal
		BigDecimal seedPrice = new BigDecimal(string).setScale(2, BigDecimal.ROUND_DOWN);
		return seedPrice;
	}

}