package com.willeeh.model;

import org.bson.types.ObjectId;

import java.util.Date;

public class Review {
	private ObjectId id;

	private ObjectId appId;

	private String userName;

	private double rating;

	private Date date;

	private String review;

	private int helpful;

	public Review() {
	}

	public Review(ObjectId appId, String userName, double rating, Date date, String review, int helpful) {
		this.appId = appId;
		this.userName = userName;
		this.rating = rating;
		this.date = date;
		this.review = review;
		this.helpful = helpful;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public ObjectId getAppId() {
		return appId;
	}

	public void setAppId(ObjectId appId) {
		this.appId = appId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public int getHelpful() {
		return helpful;
	}

	public void setHelpful(int helpful) {
		this.helpful = helpful;
	}
}
