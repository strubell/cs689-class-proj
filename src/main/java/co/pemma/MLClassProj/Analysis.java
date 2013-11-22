package co.pemma.MLClassProj;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class Analysis {


	public static void main(String[] args)
	{
		mahoutSimilarityParser("results/");		
	}
	public static void mahoutSimilarityParser(String path)
	{
		HashMap<String, String> labels = mahoutLabels(path, false);
		String line, key, label;
		String[] values, parts;
		ArrayList<Double> classSimilarity = new ArrayList<Double>();
		ArrayList<Double> allSimilarity = new ArrayList<Double>();

		double currentVal, classCount = 0, classSim = 0, otherCount = 0, otherSim = 0;
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(path + "data.txt"));		
			int i = 0;
			while ((line = reader.readLine()) != null)
			{
//				if ( ++i % 100 == 0 ) System.out.println("Reading line : " + i);
				if (line.startsWith("Key:")){
					parts = line.split("\\{");
					key = parts[0].split(":")[1].trim();
					label = labels.get(key);
					parts = parts[1].split("\\}");
					if (parts .length > 0)
					{
						values = parts[0].split(",");

						for(String pair : values)
						{
							parts = pair.split(":");
							// dont compare the docuemnt to itself or an unlabled document
							if ( !parts[0].equals(key))
							{
								currentVal = Double.parseDouble(parts[1]);

								// documents in the same class, not no label class (-1)
								if (label.equals(labels.get(parts[0])))
								{								
									classSimilarity.add(currentVal);			
									classCount++;
									classSim += currentVal;
								}
								else
								{
									allSimilarity.add(currentVal);
									otherCount++;
									otherSim += currentVal;
								}
							}
						}
					}
				}
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		

		System.out.println("Intra Cluster similarity : " + (classSim / classCount));
		System.out.println("Inter Cluster similarity : " + (otherSim / otherCount));

	}

	private static HashMap<String, String> mahoutLabels(String path, boolean getId)
	{
		HashMap<String, String> labels = new HashMap<String, String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path + "labels.txt"));			
			String line;
			String[] fileParts;
			String key;
			String[] parts;
			String label;
			String id;
			while ((line = reader.readLine()) != null)
			{
				if (line.startsWith("Key:"))
				{				
					parts = line.split(" ");
					key = parts[1].replace(":", "").trim();
					fileParts = parts[3].split("\\/");
					label = fileParts[0];
					id = fileParts[1];
					if (getId)
						labels.put(id, key);
					else
						labels.put(key, label);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return labels;
	}

	public static void computeScores(String fileName)
	{
		BufferedReader reader = null;
		HashMap<Integer, Integer> classCount = new HashMap<Integer, Integer>();
		ArrayList< HashMap<Integer, Integer>> clusterMap = new ArrayList<HashMap<Integer, Integer>>();
		HashMap<Integer, Integer> clusterPoints = null;
		int point;

		try {
			reader = new BufferedReader(new FileReader(fileName));			
			String line;
			String[] parts;
			while ((line = reader.readLine()) != null)
			{
				// new cluster
				if (line.startsWith("VL"))
				{
					if (clusterPoints != null)
						clusterMap.add(clusterPoints);
					clusterPoints = new HashMap<Integer, Integer>();
				}
				else
				{
					parts = line.split("/");
					point = Integer.parseInt(parts[1]);
					if ( point != -1)
					{
						if (clusterPoints.containsKey(point))
							clusterPoints.put(point, clusterPoints.get(point) + 1);
						else
							clusterPoints.put(point, 1);
						// count the total files from each class
						if (classCount.containsKey(point))
							classCount.put(point, classCount.get(point) + 1);
						else
							classCount.put(point, 1);
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		double clusterCount;
		int maxClassLabel;
		double maxClassCount;
		double clustersUsed = 0;
		double precision = 0;
		double recall = 0;
		double clusterP;
		double clusterR;
		for(HashMap<Integer, Integer> clusterPoint : clusterMap)
		{
			if (clusterPoint.size() == 0)
				continue;
			clustersUsed++;
			clusterCount = 0;
			maxClassCount = 0;
			maxClassLabel = -2;
			// find the count of the most occurring class in this cluster
			for (Integer doc : clusterPoint.keySet())
			{

				if (clusterPoint.get(doc) > maxClassCount)
				{
					maxClassLabel = doc;
					maxClassCount = clusterPoint.get(doc);
				}
				clusterCount += clusterPoint.get(doc);

			}
			clusterP = maxClassCount / clusterCount;
			clusterR = maxClassCount / classCount.get(maxClassLabel);
			System.out.println(clusterP + "," + clusterR + "," + (2 * ((clusterP * clusterR) / (clusterP + clusterR))));
			precision += clusterP;
			recall += clusterR;
		}
		precision = precision / clustersUsed;
		recall = recall / clustersUsed;

		System.out.println("\n\n\n total clusters, non-empty clusters, Precision, Recall, F Score"); 
		System.out.println((clusterMap.size() + 1) + "," + clustersUsed + "," + precision + "," + recall 
				+ "," + (2 * ((precision*recall) / (precision+recall)))  + "\n\n\n");
	}
}
