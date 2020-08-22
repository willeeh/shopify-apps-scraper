package com.willeeh.model;

import org.bson.types.ObjectId;

import java.util.List;

public class App {
	private ObjectId id;

	private String name;

	private String developer;

	private List<String> categories;

	private String url;

	private int numReviews;

	private double rating;

	public App() {
	}

	public App(String name, String developer, List<String> categories, String url, int numReviews, double rating) {
		this.name = name;
		this.developer = developer;
		this.categories = categories;
		this.url = url;
		this.numReviews = numReviews;
		this.rating = rating;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeveloper() {
		return developer;
	}

	public void setDeveloper(String developer) {
		this.developer = developer;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getNumReviews() {
		return numReviews;
	}

	public void setNumReviews(int numReviews) {
		this.numReviews = numReviews;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}
}
