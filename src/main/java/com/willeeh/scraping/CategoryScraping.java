package com.willeeh.scraping;

import com.willeeh.Scraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.Callable;

import static com.willeeh.Scraper.BASE;

public class CategoryScraping implements Callable<Boolean> {

	private final String categoryUrl;

	public CategoryScraping(String categoryUrl) {
		super();
		this.categoryUrl = categoryUrl;
	}

	@Override
	public Boolean call() throws Exception {
		scrapCategoryPage(this.categoryUrl);
		return true;
	}

	private void scrapCategoryPage(String url) throws IOException {
		try  {
			final Document doc = Jsoup.connect(url).get();
			final Elements appCards = doc.select(".ui-app-card");
			for (Element appCard : appCards) {
				final String appLinkWithParams = appCard.attr("data-target-href");
				final String appLink = appLinkWithParams.substring(0, appLinkWithParams.indexOf("?"));
				Scraper.appLinks.add(appLink);
			}
			final Elements nextPage = doc.select(".search-pagination__next-page-text");
			if (nextPage.size() > 0) {
				final String nextLink = nextPage.first().attr("href");
				if (!nextLink.isEmpty()) {
					final String categoryUrl = nextLink.contains(BASE) ? nextLink : BASE + nextLink;	//Because a bug in Shopify website
					scrapCategoryPage(categoryUrl);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
