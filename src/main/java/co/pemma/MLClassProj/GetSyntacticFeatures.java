package co.pemma.MLClassProj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.apache.mahout.math.VectorWritable;

public class GetSyntacticFeatures {

	private static final int WORD_IDX = 2;
	private static final int POS_IDX = 3;
	private static final int DEP_IDX = 5;

	// vector indices
	private static final int SYNTAX_FEATURE_COUNT = 95;
	private static final int WORD_SHAPE = 0;
	private static final int WORD_COUNT = 5;
	private static final int LETTERS = 7;
	private static final int DIGITS = 33;
	private static final int WORD_LENGTH_FREQ = 43;
	private static final int PUNCTUATION = 63;
	private static final int SPECIAL_CHARS = 74;

	private static final int BIG_ARR_SIZE = 1000;

	private static final String OUTPUT_DIR = "output";
	private static final String FACTORIE_OUTPUT_FILE = "fac.out";
	
	private Map<String, Double> posTotals;
	private Map<String, Double> parseTotals;
	private Map<String, Double> functionTotals;

//	private static final int USER_REVIEW_THRESHOLD = 250;

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
	private static final Set<String> functionPosTags = new HashSet<>(Arrays.asList(FUNCTION_TAGS));

	private static final String[] POS_TAGS = {
		"#", "$", "''", ",", "-LRB-", "-RRB-", ".", ":", "CC", "CD", "DT", "EX",
		"FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS",
		"NNS", "PDT", "POS", "PRP", "PRP$", "PUNC", "RB", "RBR", "RBS",
		"RP", "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ",
		"WDT", "WP", "WP$", "WRB", "``", "ADD", "AFX", "HYPH", "NFP", "XX"
	};
	private static final Set<String> posTags = new HashSet<>(Arrays.asList(POS_TAGS));

	private static final String[] PARSE_LABELS = { "acomp", "advcl", "advmod",
		"agent", "amod", "appos", "attr", "aux", "auxpass", "cc", "ccomp",
		"complm", "conj", "csubj", "csubjpass", "dep", "det", "dobj",
		"expl", "hmod", "hyph", "infmod", "intj", "iobj", "mark", "meta",
		"neg", "nmod", "nn", "npadvmod", "nsubj", "nsubjpass", "num",
		"number", "oprd", "parataxis", "partmod", "pcomp", "pobj", "poss",
		"possessive", "preconj", "predet", "prep", "prt", "punct",
		"quantmod", "rcmod", "root", "xcomp" };
	private static final Set<String> parseLabels = new HashSet<>(Arrays.asList(PARSE_LABELS));
	
	private static void syntaxFeatures(String word, double[] syntaxVector)
	{
		boolean firstUpper = false;
		boolean otherUpper = false;
		boolean allUpper = true;
		int wordShape = -1;

		// word count
		syntaxVector[WORD_COUNT] ++;		
		// character count
		syntaxVector[WORD_COUNT + 1] += word.length();

		// word length frequency (lengths 1-20)
		syntaxVector[WORD_LENGTH_FREQ + Math.max(word.length(), 20) - 1] += 1;


		for (int i = 0; i < word.length(); i ++)
		{
			char c = word.charAt(i);

			// digits occurrence
			if (c >= 0 && c <= 9 )
				syntaxVector[DIGITS + c] ++;

			// punctuation
			else if (c == '.')
				syntaxVector[PUNCTUATION] ++;
			else if (c == '?')
				syntaxVector[PUNCTUATION + 1] ++;
			else if (c == '!')
				syntaxVector[PUNCTUATION + 2] ++;
			else if (c == ',')
				syntaxVector[PUNCTUATION + 3] ++;
			else if (c == ';')
				syntaxVector[PUNCTUATION + 4] ++;
			else if (c == ':')
				syntaxVector[PUNCTUATION + 5] ++;
			else if (c == '(')
				syntaxVector[PUNCTUATION + 6] ++;
			else if (c == ')')
				syntaxVector[PUNCTUATION + 7] ++;
			else if (c == '\"')
				syntaxVector[PUNCTUATION + 8] ++;
			else if (c == '-')
				syntaxVector[PUNCTUATION + 9] ++;
			else if (c == '\'')
				syntaxVector[PUNCTUATION + 10] ++;

			// special chars
			else if (c == '`')
				syntaxVector[SPECIAL_CHARS] ++;
			else if (c == '~')
				syntaxVector[SPECIAL_CHARS + 1] ++;
			else if (c == '@')
				syntaxVector[SPECIAL_CHARS + 2] ++;
			else if (c == '#')
				syntaxVector[SPECIAL_CHARS + 3] ++;
			else if (c == '$')
				syntaxVector[SPECIAL_CHARS + 4] ++;
			else if (c == '%')
				syntaxVector[SPECIAL_CHARS + 5] ++;
			else if (c == '^')
				syntaxVector[SPECIAL_CHARS + 6] ++;
			else if (c == '&')
				syntaxVector[SPECIAL_CHARS + 7] ++;
			else if (c == '*')
				syntaxVector[SPECIAL_CHARS + 8] ++;
			else if (c == '_')
				syntaxVector[SPECIAL_CHARS + 9] ++;
			else if (c == '+')
				syntaxVector[SPECIAL_CHARS + 10] ++;
			else if (c == '=')
				syntaxVector[SPECIAL_CHARS + 11] ++;
			else if (c == '[')
				syntaxVector[SPECIAL_CHARS + 12] ++;
			else if (c == ']')
				syntaxVector[SPECIAL_CHARS + 13] ++;
			else if (c == '{')
				syntaxVector[SPECIAL_CHARS + 14] ++;
			else if (c == '}')
				syntaxVector[SPECIAL_CHARS + 15] ++;
			else if (c == '\\')
				syntaxVector[SPECIAL_CHARS + 16] ++;
			else if (c == '|')
				syntaxVector[SPECIAL_CHARS + 17] ++;
			else if (c == '/')
				syntaxVector[SPECIAL_CHARS + 18] ++;
			else if (c == '<')
				syntaxVector[SPECIAL_CHARS + 19] ++;
			else if (c == '>')
				syntaxVector[SPECIAL_CHARS + 20] ++;

			// letter occurrence
			else if (c >= 'a' && c <= 'z')
			{
				syntaxVector[LETTERS + (c - 'a')] ++;
				allUpper = false;
			}
			else if (c >= 'A' && c <= 'Z')
			{
				syntaxVector[LETTERS + (c - 'A')] ++;

				// word shape
				if (i == 0)
					firstUpper = true;
				else 
				{
					if (!firstUpper && !otherUpper)
						wordShape = 3; // camelCap
					else 
						wordShape = 4; // other
					otherUpper = true;
				}
			}
		}
		// word shape 
		if (allUpper) wordShape = 0; // all upper
		else if (!firstUpper && !otherUpper) wordShape = 1; // all lower
		else if (firstUpper && !otherUpper) wordShape = 2; // only first upper
		syntaxVector[WORD_SHAPE + wordShape] += 1;
	}

	private static void normalizeSyntaxVector(double[] syntaxVector) 
	{

	}


	//	// Yules K, frequency of once used, twice used etc words [11]
	//	private double[] vocabularyRichness(String word)

	//	/*  frequency of syntax pairs 		[789]
	//	/	(A,B) where A is parent of B
	//	 */
	//	private double[] syntaxCategoryPairs(String word)

	public static void main(String[] args) {
		//		double[] syntaxVector = new double[SYNTAX_FEATURE_COUNT];
		//
		//		syntaxFeatures("Pop.", syntaxVector);
		//
		//
		//		for (int i = 0; i < syntaxVector.length; i++)
		//		{
		//			System.out.print(syntaxVector[i] +", ");
		//		}

		GetSyntacticFeatures thisClass = new GetSyntacticFeatures();
		thisClass.functionWords();
	}
	
	public BufferedReader getFactorieReader() throws FileNotFoundException{
		
		// TODO move these somewhere less hard-coded
		int startIndex = 0;
		int numToTake = 1;
		
		File factorieFile = new File(FACTORIE_OUTPUT_FILE);
		
		// if file doesn't exist
		if(!factorieFile.exists() || this.functionTotals == null){
			try(PrintWriter output = new PrintWriter(new FileWriter(factorieFile))){
				List<User> userList = LoadYelpData.getYelpReviews();
				
				this.posTotals = Util.newMapFromKeySet(posTags, 0.0);
				this.parseTotals = Util.newMapFromKeySet(parseLabels, 0.0);
				this.functionTotals = new HashMap<>(BIG_ARR_SIZE);
				
				// Gather counts for entire dataset while writing results to file for later use
				int totalReviewCount = 1;
				for (User user : userList.subList(startIndex, startIndex + numToTake)) {
					int userReviewCount = 1;
					for (Review review : user.reviews) {
						// write review to server occasionally
						if (totalReviewCount % 100 == 0) {
							System.out.format("Processing review %d... ", totalReviewCount);
							System.out.println(review.getText());
						}
						
						try(BufferedReader reader = Util.processSentence(review.getText())){
							String line;
							while ((line = reader.readLine()) != null) {
								if (!line.equals("")) {
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
												functionTotals.put(lowerWord,functionTotals.get(lowerWord) + 1.0);
											} else {
												functionTotals.put(lowerWord, 1.0);
											}

										}
									}
								}
								output.println(user.getUserId() + "\t" + userReviewCount + "\t" + line);
							}
						}
						totalReviewCount++;
						userReviewCount++;
					}
				}
			} catch (IOException e){
				// TODO auto-generated catch block
				e.printStackTrace();
			}
		}
		return new BufferedReader(new FileReader(factorieFile));
	}
	

	public void functionWords(){
		try(BufferedReader factorieOutput = getFactorieReader()){
			// initialize per-document counts using (keys of) existing data structures
			Map<String, Double> posPerDoc = Util.newMapFromKeySet(posTags, 0.0);
			Map<String, Double> parsePerDoc = Util.newMapFromKeySet(parseLabels, 0.0);
			Map<String, Double> functionPerDoc = Util.newMapFromKeySet(this.functionTotals.keySet(), 0.0);
			double[] syntaxVector;

			String line;
			while((line = factorieOutput.readLine()) != null){
				// create vector to store syntactic features
				syntaxVector = new double[SYNTAX_FEATURE_COUNT];
				
				String lastUser = "";
				while ((line = factorieOutput.readLine()) != null) {
					if (!line.equals("")) {
						//System.out.println(line);
						String[] splitLine = line.split("\\s+");
						if (splitLine.length == 8) {
	
							String user = splitLine[0];
							String review = splitLine[1];
							if(!user.equals(lastUser)){
								
								// write results
								double[] allFreqs = populateFeatureVector(syntaxVector, posPerDoc, parsePerDoc, functionPerDoc);
								String vectorKey = user + "/" + review;
								printMahoutVector(vectorKey, allFreqs);
								
								// re-initialize data structures
								posPerDoc = Util.newMapFromKeySet(posTags, 0.0);
								parsePerDoc = Util.newMapFromKeySet(parseLabels, 0.0);
								functionPerDoc = Util.newMapFromKeySet(functionTotals.keySet(), 0.0);
								syntaxVector = new double[SYNTAX_FEATURE_COUNT];

							}
							
							String word = splitLine[WORD_IDX+2];
							String pos = splitLine[POS_IDX+2];
							String dep = splitLine[DEP_IDX+2];
							
							// update syntax vector with current word
							syntaxFeatures(word, syntaxVector);
	
							posPerDoc.put(pos, posPerDoc.get(pos) + 1.0);
							parsePerDoc.put(dep, parsePerDoc.get(dep) + 1.0);
	
							String lowerWord = word.toLowerCase();
	
							if (functionPosTags.contains(pos))
								functionPerDoc.put(lowerWord, functionPerDoc.get(lowerWord) + 1.0);
							
							lastUser = user;
						}
					}
				}	
			} 
		} catch (IOException e){
			//TODO auto-generated catch block
			e.printStackTrace();
		}
	}

	private double[] populateFeatureVector(double[] syntaxVector, Map<String, Double> posPerDoc,
			Map<String, Double> parsePerDoc, Map<String, Double> functionPerDoc) {
		
		int numObservedFuncWords = this.functionTotals.keySet().size();
		
		double[] posFreqs = new double[POS_TAGS.length];
		double[] depFreqs = new double[PARSE_LABELS.length];
		double[] funcFreqs = new double[numObservedFuncWords]; // make this a variable?
		double[] allFreqs = new double[POS_TAGS.length + PARSE_LABELS.length + numObservedFuncWords + syntaxVector.length];

		// populate feature vector(s)
		int i = 0;
		for(Entry<String,Double> entry : posPerDoc.entrySet()){
			String key = entry.getKey();
			Double value = entry.getValue();
			Double total = this.posTotals.get(key);
			double freq = total != 0.0? value/total : 0.0;
			posFreqs[i] = freq;
			allFreqs[i] = freq;
			i++;
		}

		int j = 0;
		for(Entry<String,Double> entry : parsePerDoc.entrySet()){
			String key = entry.getKey();
			Double value = entry.getValue();
			Double total = this.parseTotals.get(key);
			double freq = total != 0.0? value/total : 0.0;
			depFreqs[j] = freq;
			allFreqs[i+j] = freq;
			j++;
		}

		int k = 0;
		for(Entry<String,Double> entry : functionPerDoc.entrySet()){
			String key = entry.getKey();
			Double value = entry.getValue();
			Double total = this.functionTotals.get(key);
			double freq = total != 0.0? value/total : 0.0;
			funcFreqs[k] = freq;
			allFreqs[i+j+k] = freq;
			k++;
		}

		for(int l = 0; l < syntaxVector.length; l++)
		{
			allFreqs[i+j+k+l] = syntaxVector[l];
		}
		return allFreqs;
	}
	
	private static void printMahoutVector(String vectorKey, double[] vector)
	{
		Configuration config = new Configuration();
		Path path = new Path(OUTPUT_DIR + File.separator + "features");
		try(FileSystem fs = FileSystem.get(config);
			SequenceFile.Writer mahoutWriter = new SequenceFile.Writer(fs, config, path, Text.class, VectorWritable.class)){
			VectorWritable vec = new VectorWritable();
			vec.set(new DenseVector(vector));
			mahoutWriter.append(new Text(vectorKey), vec);
			
		} catch (IOException e){
			//TODO auto-generated catch block
			e.printStackTrace();
		}

	}
}
