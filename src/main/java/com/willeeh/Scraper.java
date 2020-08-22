package com.willeeh;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.willeeh.model.App;
import com.willeeh.model.Review;
import com.willeeh.scraping.AppScraping;
import com.willeeh.scraping.CategoryScraping;
import com.willeeh.scraping.ReviewScraping;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.*;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


/**
 * Scrapes Shopify App Store and stores apps and reviews in a local Mongo
 *
 * MQL to detect apps with negative reviews but high helpful reviews ordered by more criticized
 *
 * db.reviews.aggregate([
 *  {$match: {helpful:{$gte:2},rating:{$lte:3}}},
 *  {$group: {_id:"$appId", reviewsWithLessThan3StarsAndHelpful:{$sum:1}, helpful:{$sum:"$helpful"}}},
 *  {$sort: {reviewsWithLessThan3StarsAndHelpful:-1, helpful:-1}},
 *  {$lookup: {from:"apps", localField:"_id",foreignField:"_id",as:"app"}},
 *  {$replaceRoot: { newRoot: { $mergeObjects: [ { $arrayElemAt: [ "$app", 0 ] }, "$$ROOT" ] } }},
 *  {$project: {_id:1, name:1, rating:1, numReviews:1, reviewsWithLessThan3StarsAndHelpful:1, helpful:1, url:1}}
 *  ]).pretty()
 */
public class Scraper {
	private static final String MONGO_URI = "mongodb://127.0.0.1:27017";

	public static final String BASE = "https://apps.shopify.com";
	public static final ExecutorService executorService = Executors.newFixedThreadPool(10);
	public static final Set<String> appLinks = Collections.synchronizedSet(new HashSet<>());

	private final MongoCollection<App> appCollection;
	private final MongoCollection<Review> reviewCollection;

	public Scraper() {
		final CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
		final CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);


		final MongoClientSettings clientSettings = MongoClientSettings.builder()
			.applyConnectionString(new ConnectionString(MONGO_URI))
			.codecRegistry(codecRegistry)
			.build();

		final MongoDatabase database = MongoClients.create(clientSettings).getDatabase("shopify");

		this.appCollection = database.getCollection("apps", App.class);
		this.reviewCollection = database.getCollection("reviews", Review.class);

		this.appCollection.createIndex(new org.bson.Document("url", 1));
	}

	public static void main(String[] args) {
		new Scraper().run();
	}

	private void run() {

		try {
			final Document doc = Jsoup.connect(BASE).get();
			final Elements menu = doc.select("#DrawerNavPrimaryAccordion");
			final Element categoriesMenu = menu.get(0);
			final Elements categoriesSubmenu = categoriesMenu.select(".accordion-item");
			final List<Future<Boolean>> futures = new ArrayList<>();
			for (Element submenu : categoriesSubmenu) {
				final Element lastElementSubmenu = submenu.select(".drawer__item").last();
				final String categoryHref = lastElementSubmenu.select("a").attr("href");
				final String categoryUrl = categoryHref.contains(BASE) ? categoryHref : BASE + categoryHref;	//Because a bug in Shopify website
				futures.add( executorService.submit(new CategoryScraping(categoryUrl)) );
			}

			//Wait for all category futures to have appLinks
			for (Future<Boolean> future : futures) {
				future.get();
			}

			for (String appLink : appLinks) {
				final Future<Boolean> futureAppScraping = executorService.submit(new AppScraping(appCollection, appLink));
				futureAppScraping.get();	//We need to wait until it's done because ReviewScraping need it
				final String reviewLink = appLink + "/reviews";
				executorService.submit(new ReviewScraping(appCollection, reviewCollection, reviewLink));
			}

			executorService.shutdown();
			try {
				executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);

	}
}
