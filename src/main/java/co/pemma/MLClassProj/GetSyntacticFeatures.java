package co.pemma.MLClassProj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.VectorWritable;

public class GetSyntacticFeatures {

	private static final int WORD_IDX = 2;
	private static final int POS_IDX = 3;
	private static final int DEP_IDX = 5;

	private static final int BIG_ARR_SIZE = 1000;

	private static final String OUTPUT_DIR = "output";
	
	private static final int USER_REVIEW_THRESHOLD = 250;

	private static final String[] FUNCTION_TAGS = { 
		"CC", 	// Coordinating conjunction
		"DT", 	// Determiner
		"EX", 	// Existential there
		"IN", 	// Preposition or subordinating conjunction
		"LS", 	// List item marker
		"MD", 	// Modal
		"PDT", 	// Predeterminer
		"POS", 	// Possessive ending
		"PRP", 	// Personal pronoun
		"PRP$", // Possessive pronoun
		"RP",	// Particle
		"TO", 	// to
		"UH", 	// Interjection
		"WDT", 	// Wh-determiner
		"WP", 	// Wh-pronoun
		"WP$" 	// Possessive wh-pronoun
	};

	private static final String[] POS_TAGS = {
		"#", // In WSJ but not in Ontonotes
		"$", "''", ",", "-LRB-", "-RRB-", ".", ":", "CC", "CD", "DT", "EX",
		"FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS",
		"NNS", "PDT", "POS", "PRP", "PRP$", "PUNC", "RB", "RBR", "RBS",
		"RP", "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ",
		"WDT", "WP", "WP$", "WRB", "``", "ADD", // in Ontonotes, but not WSJ
		"AFX", // in Ontonotes, but not WSJ
		"HYPH", // in Ontonotes, but not WSJ
		"NFP", // in Ontonotes, but not WSJ
		"XX" // in Ontonotes, but not WSJ
	};

	private static final String[] PARSE_LABELS = { "acomp", "advcl", "advmod",
		"agent", "amod", "appos", "attr", "aux", "auxpass", "cc", "ccomp",
		"complm", "conj", "csubj", "csubjpass", "dep", "det", "dobj",
		"expl", "hmod", "hyph", "infmod", "intj", "iobj", "mark", "meta",
		"neg", "nmod", "nn", "npadvmod", "nsubj", "nsubjpass", "num",
		"number", "oprd", "parataxis", "partmod", "pcomp", "pobj", "poss",
		"possessive", "preconj", "predet", "prep", "prt", "punct",
		"quantmod", "rcmod", "root", "xcomp" };

	public static void main(String[] args) {

		Configuration config = new Configuration();
		Path path = new Path(OUTPUT_DIR + File.separator + "features");
		try(FileSystem fs = FileSystem.get(config);
			SequenceFile.Writer mahoutWriter = new SequenceFile.Writer(fs, config, path, Text.class, VectorWritable.class)){
			
			VectorWritable vec = new VectorWritable();

			long t0 = System.currentTimeMillis();
			List<User> userList = LoadYelpData.readUserReviews(USER_REVIEW_THRESHOLD);
			System.out.println(System.currentTimeMillis() - t0 + " ms");

			int startIndex = 0;
			int numToTake = userList.size();

			Set<String> functionPosTags = new HashSet<>(Arrays.asList(FUNCTION_TAGS));
			Set<String> posTags = new HashSet<>(Arrays.asList(POS_TAGS));
			Set<String> parseLabels = new HashSet<>(Arrays.asList(PARSE_LABELS));

			Map<String, Double> posTotals = Util.newMapFromKeySet(posTags, 0.0);
			Map<String, Double> parseTotals = Util.newMapFromKeySet(parseLabels, 0.0);
			Map<String, Double> functionTotals = new HashMap<>(BIG_ARR_SIZE);

			int windowSize = 3;
			int buffSize = windowSize / 2;
			List<String> wordWindow = new ArrayList<>(windowSize);
			List<String> posWindow = new ArrayList<>(windowSize);
			List<String> depWindow = new ArrayList<>(windowSize);

			int reviewCount = 1;
			String line = "";

			// Gather counts for entire dataset
			for (User user : userList.subList(startIndex, startIndex + numToTake)) {
				for (Review review : user.reviews) {

					// open connection to Factorie server (expects it's already running)
					try (Socket connection = new Socket("localhost", 3228)) {

						PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())), true);

						// write review to server
						if(reviewCount % 100 == 0){
							System.out.format("Processing review %d... ", reviewCount);
							System.out.println(review.getText());
						}
						//t0 = System.currentTimeMillis();
						writer.println(review.getText());
						connection.shutdownOutput();

						// parse result
						BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						//System.out.println((System.currentTimeMillis() - t0) + " ms");

						while ((line = reader.readLine()) != null) {
							if (!line.equals("")) {
								//System.out.println(line);
								String[] splitLine = line.split("\\s+");
								if (splitLine.length == 6) {
									String word = splitLine[WORD_IDX];
									String pos = splitLine[POS_IDX];
									String dep = splitLine[DEP_IDX];

									posTotals.put(pos, posTotals.get(pos) + 1.0);
									parseTotals.put(dep, parseTotals.get(dep) + 1.0);

									if (functionPosTags.contains(pos)) {
										String lowerWord = word.toLowerCase();
										if (functionTotals.containsKey(lowerWord)) {
											functionTotals.put(lowerWord, functionTotals.get(lowerWord) + 1.0);
										} else {
											functionTotals.put(lowerWord, 1.0);
										}
									}
								}
							}
						}

						reviewCount++;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			int numObservedFuncWords = functionTotals.keySet().size();
			reviewCount = 1;
			
			// now gather individual document counts and write to file
			for (User user : userList.subList(startIndex, startIndex + numToTake)) {
				for (Review review : user.reviews) {
					// open connection to Factorie server (expects it's already running)
					try (Socket connection = new Socket("localhost", 3228)) {

						PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())), true);

						// write review to server
						if(reviewCount % 100 == 0){
							System.out.format("Processing review %d... \n", reviewCount);
							//System.out.println(review.getText());
						}
						writer.println(review.getText());
						connection.shutdownOutput();

						// parse result
						BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						//System.out.println((System.currentTimeMillis() - t0)+ " ms");

						// initialize per-document counts using (keys of) existing data structures
						Map<String, Double> posPerDoc = Util.newMapFromKeySet(posTags, 0.0);
						Map<String, Double> parsePerDoc = Util.newMapFromKeySet(parseLabels, 0.0);
						Map<String, Double> functionPerDoc = Util.newMapFromKeySet(functionTotals.keySet(), 0.0);
						
						while ((line = reader.readLine()) != null) {
							if (!line.equals("")) {
								//System.out.println(line);
								String[] splitLine = line.split("\\s+");
								if (splitLine.length == 6) {
									
									String word = splitLine[WORD_IDX];
									String pos = splitLine[POS_IDX];
									String dep = splitLine[DEP_IDX];

									posPerDoc.put(pos, posPerDoc.get(pos) + 1.0);
									parsePerDoc.put(dep, parsePerDoc.get(dep) + 1.0);

									String lowerWord = word.toLowerCase();
									
									if (functionPosTags.contains(pos))
										functionPerDoc.put(lowerWord, functionPerDoc.get(lowerWord) + 1.0);
								}
							}
						}

						double[] posFreqs = new double[POS_TAGS.length];
						double[] depFreqs = new double[PARSE_LABELS.length];
						double[] funcFreqs = new double[numObservedFuncWords]; // make this a variable?
						double[] allFreqs = new double[POS_TAGS.length+PARSE_LABELS.length+numObservedFuncWords];

						// populate feature vector(s)
						int i = 0;
						for(Entry<String,Double> entry : posPerDoc.entrySet()){
							String key = entry.getKey();
							Double value = entry.getValue();
							Double total = posTotals.get(key);
							double freq = total != 0.0? value/total : 0.0;
							posFreqs[i] = freq;
							allFreqs[i] = freq;
							i++;
						}
						
						int j = 0;
						for(Entry<String,Double> entry : parsePerDoc.entrySet()){
							String key = entry.getKey();
							Double value = entry.getValue();
							Double total = parseTotals.get(key);
							double freq = total != 0.0? value/total : 0.0;
							depFreqs[j] = freq;
							allFreqs[i+j] = freq;
							j++;
						}
						
						int k = 0;
						for(Entry<String,Double> entry : functionPerDoc.entrySet()){
							String key = entry.getKey();
							Double value = entry.getValue();
							Double total = functionTotals.get(key);
							double freq = total != 0.0? value/total : 0.0;
							funcFreqs[k] = freq;
							allFreqs[i+j+k] = freq;
							k++;
						}

						// now write the results to Mahout-format file
						String vectorKey = user.getUserId() + "/" + reviewCount;
						NamedVector featureVector = new NamedVector(new DenseVector(allFreqs), vectorKey);
						vec.set(featureVector);
						mahoutWriter.append(new Text(vectorKey), vec);
						
						if(reviewCount % 100 == 0){
							for(int m = 0; m < allFreqs.length; ++m){
								System.out.print(allFreqs[m] + " ");
							}
							System.out.println();
						}
						
						reviewCount++;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
