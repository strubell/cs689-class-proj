package co.pemma.MLClassProj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import cc.factorie.app.nlp.parse.*;

public class GetSyntacticFeatures {

	private static final int WORD_IDX = 2;
	private static final int POS_IDX = 3;
	private static final int DEP_IDX = 5;
	
	public static void main(String[] args) {
		// for testing

		ArrayList<User> userList = LoadYelpData.readUserReviews();

		int startIndex = 0;
		int numToTake = 5;
		
		int parseDomainSize = ParseTreeLabelDomain.categories.size();
		int posDomainSize = PennPosLabelDomain.categories.size();
		
		HashMap<String,Integer> posTotals = new HashMap<>(posDomainSize);
		HashMap<String,Integer> parseTotals = new HashMap<>(parseDomainSize);
		
		int windowSize = 3;
		int buffSize = windowSize/2;
		ArrayList<String> wordWindow  = new ArrayList<String>(windowSize);
		ArrayList<String> posWindow  = new ArrayList<String>(windowSize);
		ArrayList<String> depWindow  = new ArrayList<String>(windowSize);
		
		int reviewCount = 1;
		String line = "";
		for(User user : userList.subList(startIndex, startIndex+numToTake)){
			for (Review review : user.reviews) {
	
				// open connection to Factorie server (expects it's already running)
				try(Socket connection = new Socket("localhost", 3228)){
					
					PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())), true);
		
					// write review to server
					System.out.format("Processing review %d... ", reviewCount);
					long t0 = System.currentTimeMillis();
					writer.println(review.getText());
					connection.shutdownOutput();
					
					// parse result
					BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					System.out.println((System.currentTimeMillis() - t0) + " ms");
		
					
					while((line = reader.readLine()) != null)
						System.out.println(line);
					
//					while((line = reader.readLine()) != null && wordWindow[buffSize] != ""){
//						//System.out.println(line);
//						String[] splitLine = line.split("\t");
//						String word = splitLine[WORD_IDX];
//						String pos = splitLine[POS_IDX];
//						String dep = splitLine[DEP_IDX];
//						
//						for(int i = 0; i < windowSize-1; ++i){
//							wordWindow[i] = wordWindow[i+1];
//						}
//						wordWindow[windowSize-1] = word;
//						
//					}
//					
//					// clear out window arrays for next review
//					for(int i = 0; i < windowSize; ++i){
//						wordWindow[i] = "";
//						posWindow[i] = "";
//						depWindow[i] = "";
//					}
					
					while((line = reader.readLine()) != null){
						
					}
					
					reviewCount++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
}
