package co.pemma.MLClassProj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.eclipsesource.json.JsonObject;

public class LoadYelpData {

	public static final int MIN_REVIEW_COUNT = 250;
	public static final String OUTPUT_DIR = "/NTFS";

	private static List<User> yelpReviews;
	
	public static void main(String[] args) throws IOException 
	{	
		ArrayList<User> subsetUsers = readUserReviews(MIN_REVIEW_COUNT);

		System.out.println(subsetUsers.size() + " Users with at least " + MIN_REVIEW_COUNT + " reviews");
		
		printReviews(subsetUsers);

	}
	
	public static ArrayList<User> readUserReviews(){
		return readUserReviews(0);
	}
	
	/**
	 * Return a list of Yelp reviews by users who have written > MIN_REVIEW_COUNT
	 * reviews, reading from the data file if necessary.
	 * 
	 * @return List of Yelp reviews (by User)
	 */
	public static List<User> getYelpReviews(){
		if(yelpReviews == null)
			yelpReviews = LoadYelpData.readUserReviews(MIN_REVIEW_COUNT);
		return yelpReviews;
	}

	public static ArrayList<User> readUserReviews(int minReviewCount)
	{
		HashMap<String, User> users = new HashMap<String, User>();
		String file =  "yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json" ;

		System.out.print("Reading in data...");
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			User user;
			JsonObject jsonObj;
			String id;
			String line = "";
			while ((line = reader.readLine()) != null)
			{				
				jsonObj = JsonObject.readFrom(line);
				id = jsonObj.get("user_id").toString();

				if(users.containsKey(id))
				{
					user = users.get(id);
					user.addReview(new Review(jsonObj));
				}
				else
				{
					user = new User(id);
					user.addReview(new Review(jsonObj));
					users.put(id, user);
				}
			}

			System.out.print(" read in reviews of " + users.size() + " users...");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		if(minReviewCount > 0){
			ArrayList<User> thresholdUserList = new ArrayList<>();
			for(User user : users.values()){
				if(user.size() >= minReviewCount)
					thresholdUserList.add(user);
			}
			System.out.println(" keeping " + thresholdUserList.size() + " users with at least " + minReviewCount + " reviews.");
			return thresholdUserList;
		}
		else
			return new ArrayList<User>(users.values());
	}

	public static void printReviews(ArrayList<User> users)
	{
		PrintWriter writer;
		File file;
		for(User user : users)
		{			
			for (Review review : user.getReviews())
			{
				try {					
					file = new File( OUTPUT_DIR + "/reviews_" + MIN_REVIEW_COUNT + "/" + user.getUserId() + "/" + review.getDate() + ".txt");
					file.getParentFile().mkdirs();
					writer = new PrintWriter(file, "UTF-8");
					writer.println(review.getText());
					writer.close();

				} catch (FileNotFoundException e) {
					System.out.println("io failure" + e);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} 
			}
		}
		System.out.println("Successfully wrote data");
	}

}
