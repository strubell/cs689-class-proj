package co.pemma.MLClassProj;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.eclipsesource.json.JsonObject;

public class LoadYelpData {

	public static void main(String[] args) throws IOException {
		String file =  "yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json" ;
		try(BufferedReader reader = new BufferedReader(new FileReader(file))){

			String line ="";
			while ((line = reader.readLine()) != null)
			{				
				JsonObject jsonObject = JsonObject.readFrom(line);
				System.out.println(jsonObject.get( "stars" ) + " " + jsonObject.get( "text" ).asString());
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
