package co.pemma.MLClassProj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.eclipsesource.json.JsonObject;

public class LoadYelpData {

	public static final int MIN_REVIEW_COUNT = 5;

	public static void main(String[] args) throws IOException 
	{	

		ArrayList<User> allUsers = readUserReviews();
		ArrayList<User> subsetUsers = new ArrayList<User>();

		for (User user: allUsers)
		{
			if ( user.size() >= MIN_REVIEW_COUNT)
				subsetUsers.add(user);
		}

		System.out.println(subsetUsers.size() + " Users with atleast " + MIN_REVIEW_COUNT + " reviews");
	}

	public static ArrayList<User> readUserReviews()
	{
		ArrayList<User> users = new ArrayList<User>();		
		String file =  "yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json" ;

		User user;
		JsonObject jsonObj;
		String id;


		System.out.print("Reading in data...");
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

			String line = "";
			while ((line = reader.readLine()) != null)
			{				
				jsonObj = JsonObject.readFrom(line);
				id = jsonObj.get("user_id").toString();
				user = new User(id);

				if(users.contains(user))
				{
					user = users.get(users.indexOf(user));
					user.addReview(new Review(jsonObj));
				}
				else
				{
					user.addReview(new Review(jsonObj));
					users.add(user);
				}
			}

			System.out.println("read in reviews of " + users.size() + " users");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return users;
	}

	public static void printReviews(ArrayList<User> users)
	{
		String dir = "/run/media/pv/NTFS";
		PrintWriter writer;
		File file;
		for(User user : users)
		{			
			for (Review review : user.getReviews())
			{
				try {					
					file = new File( dir + "/reviews/" + user.getUserId() + "/" + review.getDate() + ".txt");
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
