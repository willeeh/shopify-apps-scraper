package com.willeeh.util;

import org.jsoup.nodes.Element;

public class ScraperUtil {
	public static String val(Element base, String select) {
		final Element element = base.selectFirst(select);
		return getValue(element);
	}

	public static String lastVal(Element base, String select) {
		final Element element = base.select(select).last();
		return getValue(element);
	}

	private static String getValue(Element element) {
		return element != null && element.childNodeSize() > 0 ? element.childNode(0).outerHtml() : null;
	}
}
