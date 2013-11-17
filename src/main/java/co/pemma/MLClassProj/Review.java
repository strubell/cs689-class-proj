package co.pemma.MLClassProj;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class Review {
	JsonValue stars;
	JsonValue text;
	JsonValue businessId;
	JsonValue date;


	public Review(JsonObject jsonObject)
	{
		this.businessId = jsonObject.get("business_id");
		this.text = jsonObject.get("text");
		this.stars = jsonObject.get("stars");
		this.date = jsonObject.get("date");
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
