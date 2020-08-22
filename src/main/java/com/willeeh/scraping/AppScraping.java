package com.willeeh.scraping;

import com.mongodb.client.MongoCollection;
import com.willeeh.model.App;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.willeeh.util.ScraperUtil.val;

public class AppScraping implements Callable<Boolean> {

	private final MongoCollection<App> appCollection;
	private final String appUrl;

	public AppScraping(MongoCollection<App> appCollection, String appUrl) {
		super();
		this.appCollection = appCollection;
		this.appUrl = appUrl;
	}

	@Override
	public Boolean call() throws Exception {
		try {
			final Document doc = Jsoup.connect(appUrl).get();

			final String name = val(doc, ".ui-app-store-hero__header__app-name");
			final String developer = val(doc, ".ui-app-store-hero__header__subscript a");
			final List<String> categories = doc.selectFirst(".ui-app-store-hero__kicker").select("a").stream().map(s -> s.childNode(0).outerHtml()).collect(Collectors.toList());
			final String numReviewsFormatted = val(doc, ".ui-review-count-summary a"); //(132 reviews)
			final int numReviews = numReviewsFormatted != null ? Integer.parseInt(numReviewsFormatted.substring(1, numReviewsFormatted.indexOf(" "))) : 0;
			final String ratingsFormatted = val(doc, ".ui-star-rating__rating");
			final double rating = ratingsFormatted != null ? Double.parseDouble(ratingsFormatted) : 0d;

			final App app = new App(
				name,
				developer,
				categories,
				appUrl,
				numReviews,
				rating
			);
			appCollection.insertOne(app);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
