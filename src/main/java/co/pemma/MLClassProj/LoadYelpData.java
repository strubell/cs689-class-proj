package co.pemma.MLClassProj;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.eclipsesource.json.JsonObject;

public class LoadYelpData {

	public static void main(String[] args) throws IOException {
		
		ArrayList<ReviewObject> reviews =  new ArrayList<ReviewObject>();
		
		String file =  "yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json" ;
		try(BufferedReader reader = new BufferedReader(new FileReader(file))){

			String line ="";
			while ((line = reader.readLine()) != null)
			{				
				reviews.add(new ReviewObject(JsonObject.readFrom(line)));
			}

			System.out.println("read in " + reviews.size() + " reviews.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
