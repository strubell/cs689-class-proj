package co.pemma.MLClassProj;

import com.eclipsesource.json.JsonObject;

public class ReviewObject {
	String userId;
	double stars;
	String text;
	String businessId;
	String date;
	
	public ReviewObject(String user_id, String business_id, String date, String text, double stars)
	{
		this.userId = user_id;
		this.businessId = business_id;
		this.date = date;
		this.text = text;
		this.stars = stars;
	}

	public ReviewObject(JsonObject jsonObject)
	{
		this.userId = jsonObject.get("user_id").toString();
		this.businessId = jsonObject.get("business_id").toString();
		this.text = jsonObject.get("text").toString();
		this.stars = jsonObject.get("stars").asDouble();
		this.date = jsonObject.get("date").toString();
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public double getStars() {
		return stars;
	}

	public void setStars(double stars) {
		this.stars = stars;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
}
