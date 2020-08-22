package com.willeeh.scraping;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.willeeh.Scraper;
import com.willeeh.model.App;
import com.willeeh.model.Review;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import static com.willeeh.util.ScraperUtil.lastVal;
import static com.willeeh.util.ScraperUtil.val;

public class ReviewScraping implements Callable<Boolean> {
	private static final DateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
	private final MongoCollection<App> appCollection;
	private final MongoCollection<Review> reviewCollection;
	private final String reviewUrl;

	public ReviewScraping(MongoCollection<App> appCollection,
						  MongoCollection<Review> reviewCollection,
						  String reviewUrl) {
		super();
		this.appCollection = appCollection;
		this.reviewCollection = reviewCollection;
		this.reviewUrl = reviewUrl;
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	}


	@Override
	public Boolean call() throws Exception {
		try {
			final Document doc = Jsoup.connect(reviewUrl).get();
			final String appUrl = reviewUrl.substring(0, reviewUrl.lastIndexOf("/"));
			final App app = appCollection.find(Filters.eq("url", appUrl)).first();
			final Elements elements = doc.select(".review-listing");

			elements.forEach(element -> {
				try {
					final String userName = val(element, ".review-listing-header__text");
					final int rating = Integer.parseInt(element.selectFirst(".ui-star-rating").attr("data-rating"));
					final String dateFormatted = lastVal(element, ".review-metadata__item-value").trim();
					final Date date = formatter.parse(dateFormatted);
					final String reviewText = val(element,".truncate-content-copy p");
					final int helpful = Integer.parseInt(val(element, ".review-helpfulness__helpful-count"));
					final Review review = new Review(
						app.getId(),
						userName,
						rating,
						date,
						reviewText,
						helpful
					);
					reviewCollection.insertOne(review);
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});

			final Elements nextPage = doc.select(".search-pagination__next-page-text");
			if (nextPage.size() > 0) {
				final String reviewLink = Scraper.BASE + nextPage.first().attr("href");
				Scraper.executorService.submit(new ReviewScraping(appCollection, reviewCollection, reviewLink));
			}

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
