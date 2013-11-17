package co.pemma.MLClassProj;

import java.util.ArrayList;

public class User {
	
	String userId;
	ArrayList<Review> reviews;
	
	public User(String userId)
	{
		this.userId = userId;
		this.reviews = new ArrayList<Review>();
	}

	public String getUserId()
	{
		return userId;
	}
	
	public void addReview(Review review)
	{
		this.reviews.add(review);
	}
	
	public ArrayList<Review> getReviews()
	{
		return reviews;
	}
	
	public int size()
	{
		return reviews.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
}
