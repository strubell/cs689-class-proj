package co.pemma.MLClassProj;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class ReviewObject {
	JsonValue userId;
	JsonValue stars;
	JsonValue text;
	JsonValue businessId;
	JsonValue date;
	
//	public ReviewObject(String user_id, String business_id, String date, String text, double stars)
//	{
//		this.userId = user_id;
//		this.businessId = business_id;
//		this.date = date;
//		this.text = text;
//		this.stars = stars;
//	}

	public ReviewObject(JsonObject jsonObject)
	{
		this.userId = jsonObject.get("user_id");
		this.businessId = jsonObject.get("business_id");
		this.text = jsonObject.get("text");
		this.stars = jsonObject.get("stars");
		this.date = jsonObject.get("date");
	}

	public JsonValue getUserId() {
		return userId;
	}

	public void setUserId(JsonValue userId) {
		this.userId = userId;
	}

	public JsonValue getStars() {
		return stars;
	}

	public void setStars(JsonValue stars) {
		this.stars = stars;
	}

	public JsonValue getText() {
		return text;
	}

	public void setText(JsonValue text) {
		this.text = text;
	}

	public JsonValue getBusinessId() {
		return businessId;
	}

	public void setBusinessId(JsonValue businessId) {
		this.businessId = businessId;
	}

	public JsonValue getDate() {
		return date;
	}

	public void setDate(JsonValue date) {
		this.date = date;
	}
	
}
