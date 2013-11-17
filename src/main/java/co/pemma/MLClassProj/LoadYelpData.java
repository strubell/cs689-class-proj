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


import com.eclipsesource.json.JsonObject;

public class LoadYelpData {

	public static void main(String[] args) throws IOException 
	{	
		printReviews(readYelpReviews());
	}
	
	public static ArrayList<ReviewObject> readYelpReviews()
	{
		ArrayList<ReviewObject> reviews =  new ArrayList<ReviewObject>();
		HashMap<String, Double> userCounts = new HashMap<String, Double>();
		
		String file =  "yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json" ;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

			String line = "";
			while ((line = reader.readLine()) != null)
			{				
				ReviewObject review = new ReviewObject(JsonObject.readFrom(line));
				reviews.add(review);
				if(userCounts.containsKey(review.getUserId()))
				{
					userCounts.put(review.getUserId().toString(), userCounts.get(review) + 1.0);
				}
				else
				{
					userCounts.put(review.getUserId().toString(), 1.0);
				}
			}

			System.out.println("read in " + reviews.size() + " reviews.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return reviews;

	}
	
	public static void printReviews(ArrayList<ReviewObject> reviews)
	{
		PrintWriter writer;
		File file;
		for(ReviewObject review : reviews)
		{			
			try {					
					file = new File( "reviews/" + review.getUserId() + "/" + review.getDate() + ".txt");
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
		System.out.println("Successfully wrote data");
	}

}
