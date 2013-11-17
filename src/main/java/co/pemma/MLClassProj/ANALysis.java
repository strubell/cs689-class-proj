package co.pemma.MLClassProj;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ANALysis {

	
	public static void ANALysis(String fileName)
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
