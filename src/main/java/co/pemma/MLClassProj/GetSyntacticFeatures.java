package co.pemma.MLClassProj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.VectorWritable;

public class GetSyntacticFeatures {

	private static final int WORD_IDX = 2;
	private static final int POS_IDX = 3;
	private static final int DEP_IDX = 5;
	private static final int LEM_IDX = 6;

	private static final int BIG_ARR_SIZE = 1000;

	// define puncuation and special chars to use
	private final static char[] PUNCTUATION_TYPES = {'.', '?', '!', ',', ';', ':', '(', ')', '\"', '-', '\''};
	private final static char[] SPECIAL_CHAR_TYPES = {'`', '~', '@', '#', '$', '%', '^', '&', '*', '_', '+', '=', '[', ']', '{', '}', '\\', '|', '/', '<', '>'};

	// vector indices
	private static int SYNTAX_FEATURE_COUNT = 0;
	private static int WORD_SHAPE_START;
	private static int WORD_COUNT_START;
	private static int LETTERS_START;
	private static int DIGITS_START;
	private static int WORD_LENGTH_FREQ_START;
	private static int PUNCTUATION_START;
	private static int SPECIAL_CHARS_START;

	private static final int WORD_SHAPE_COUNT = 5;
	private static final int WORD_COUNT_COUNT = 2;
	private static final int LETTERS_COUNT = 26;
	private static final int DIGITS_COUNT = 10;
	private static final int WORD_LENGTH_FREQ_COUNT = 20;
	private static final int PUNCTUATION_COUNT = 11;
	private static final int SPECIAL_CHARS_COUNT = 21;

	// features to use
	private static boolean WORD_SHAPE_ON = false;
	private static boolean WORD_COUNT_ON = false;
	private static boolean LETTERS_ON = false;
	private static boolean DIGITS_ON = false;
	private static boolean WORD_LENGTH_FREQ_ON = false;
	private static boolean PUNCTUATION_ON = false;
	private static boolean SPECIAL_CHARS_ON = false;
	private static boolean DEP_FREQS_ON = false;
	private static boolean POS_FREQS_ON = false;
	private static boolean FUNC_FREQS_ON = false;
	private static boolean YULE_ON = false;
	
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

	public static void main(String[] args) {
		// all
		//setFeatures( new String[] {"ws", "wc", "l", "d", "wl", "p", "sc", "pos", "dp", "fw", "y"} );
		
		// just punctuation & special chars
//		setFeatures( new String[] {"d"} );
		setFeatures(args);
		
		String featuresUsed = "";
		for (int i = 0; i < args.length; i ++) featuresUsed += " " + args[i];
		
		System.out.println("Exporting feature vectors for : " + featuresUsed);
		
		GetSyntacticFeatures thisClass = new GetSyntacticFeatures();
		thisClass.functionWords();
	}
	
	private static void setFeatures(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];

			if (arg.equals("wordshape") || arg.equals("ws"))
			{
				WORD_SHAPE_ON = true;
				WORD_SHAPE_START = SYNTAX_FEATURE_COUNT;
				SYNTAX_FEATURE_COUNT += WORD_SHAPE_COUNT;
			}
			else if (arg.equals("wordcount") || arg.equals("wc"))
			{
				WORD_COUNT_ON = true;
				WORD_COUNT_START = SYNTAX_FEATURE_COUNT;
				SYNTAX_FEATURE_COUNT += WORD_COUNT_COUNT;
			}
			else if (arg.equals("letters") || arg.equals("l"))
			{
				LETTERS_ON = true;
				LETTERS_START = SYNTAX_FEATURE_COUNT;
				SYNTAX_FEATURE_COUNT += LETTERS_COUNT;
			}
			else if (arg.equals("digits") || arg.equals("d"))
			{
				DIGITS_ON = true;
				DIGITS_START = SYNTAX_FEATURE_COUNT;
				SYNTAX_FEATURE_COUNT += DIGITS_COUNT;
			}
			else if (arg.equals("wordlength") || arg.equals("wl"))
			{
				WORD_LENGTH_FREQ_ON = true;
				WORD_LENGTH_FREQ_START = SYNTAX_FEATURE_COUNT;
				SYNTAX_FEATURE_COUNT += WORD_LENGTH_FREQ_COUNT;
			}
			else if (arg.equals("punctuation") || arg.equals("p"))
			{
				PUNCTUATION_ON = true;
				PUNCTUATION_START = SYNTAX_FEATURE_COUNT;
				SYNTAX_FEATURE_COUNT += PUNCTUATION_COUNT;
			}
			else if (arg.equals("specialchars") || arg.equals("sc"))
			{
				SPECIAL_CHARS_ON = true;
				SPECIAL_CHARS_START = SYNTAX_FEATURE_COUNT;
				SYNTAX_FEATURE_COUNT += SPECIAL_CHARS_COUNT;
			}
			else if (arg.equals("posfreqs") || arg.equals("pos"))
			{
				POS_FREQS_ON = true;
			}
			else if (arg.equals("dpfreqs") || arg.equals("dp"))
			{
				DEP_FREQS_ON = true;
			}
			else if (arg.equals("funcwords") || arg.equals("fw"))
			{
				FUNC_FREQS_ON = true;
			}
			else if (arg.equals("yule") || arg.equals("y"))
			{
				YULE_ON = true;
			}
		}
	}

	private static void syntaxFeatures(String word, double[] syntaxVector)
	{
		boolean firstUpper = false;
		boolean otherUpper = false;
		boolean allUpper = true;
		int wordShape = -1;

		// word count
		if (WORD_COUNT_ON)
		{
			syntaxVector[WORD_COUNT_START] ++;		
			// character count
			syntaxVector[WORD_COUNT_START + 1] += word.length();
		}

		// word length frequency (lengths 1-20)
		if (WORD_LENGTH_FREQ_ON)
			syntaxVector[WORD_LENGTH_FREQ_START + Math.min(word.length(), WORD_LENGTH_FREQ_COUNT) - 1] ++;


		for (int i = 0; i < word.length(); i++)
		{
			char c = word.charAt(i);

			// digits occurrence
			if (DIGITS_ON)
				if (c >= '0' && c <= '9' )
					syntaxVector[DIGITS_START + (c - '0')] ++;

			// punctuation
			if (PUNCTUATION_ON)
				for (int j = 0; j < PUNCTUATION_TYPES.length; j++)
					if (c == PUNCTUATION_TYPES[j]){
						syntaxVector[PUNCTUATION_START + j] ++;
						break;
					}

			// special chars
			if (SPECIAL_CHARS_ON)
				for (int j = 0; j < SPECIAL_CHAR_TYPES.length; j++)
					if (c == SPECIAL_CHAR_TYPES[j]){
						syntaxVector[SPECIAL_CHARS_START + j] ++;
						break;
					}

			// letter occurrence
			if (c >= 'a' && c <= 'z')
			{
				if (LETTERS_ON)
					syntaxVector[LETTERS_START + (c - 'a')] ++;
				allUpper = false;
			}

			if (c >= 'A' && c <= 'Z')
			{
				if (LETTERS_ON)
					syntaxVector[LETTERS_START + (c - 'A')] ++;

				if (WORD_SHAPE_ON)
				{
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
		}

		// word shape 
		if (WORD_SHAPE_ON)
		{
			if (allUpper) wordShape = 0; // all upper
			else if (!firstUpper && !otherUpper) wordShape = 1; // all lower
			else if (firstUpper && !otherUpper) wordShape = 2; // only first upper
			syntaxVector[WORD_SHAPE_START + wordShape] += 1;
		}
	}

	private static void normalizeSyntaxVector(double[] syntaxVector, double words, double chars){
		
		if (WORD_SHAPE_ON)
			// normalize word shapes
			for (int i = 0; i < WORD_SHAPE_COUNT; i++)
				syntaxVector[WORD_SHAPE_START + i] = syntaxVector[WORD_SHAPE_START + i] / words;

		if (WORD_LENGTH_FREQ_ON)
			// normalize word length frequency
			for (int i = 0; i < WORD_LENGTH_FREQ_COUNT; i++)
				syntaxVector[WORD_LENGTH_FREQ_START + i] = syntaxVector[WORD_LENGTH_FREQ_START + i] / words;

		if (DIGITS_ON)
			// normalize digit counts
			for (int i = 0; i < DIGITS_COUNT; i++)
				syntaxVector[DIGITS_START + i] = syntaxVector[DIGITS_START + i] / chars;

		if (LETTERS_ON)
			// normalize letter counts
			for (int i = 0; i < LETTERS_COUNT; i++)
				syntaxVector[LETTERS_START + i] = syntaxVector[LETTERS_START + i] / chars;

		if (PUNCTUATION_ON)
			// normalize punctuation counts
			for (int i = 0; i < PUNCTUATION_COUNT; i++)
				syntaxVector[PUNCTUATION_START + i] = syntaxVector[PUNCTUATION_START + i] / chars;

		if (SPECIAL_CHARS_ON)
			// normalize special char counts
			for (int i = 0; i < SPECIAL_CHARS_COUNT; i++)
				syntaxVector[SPECIAL_CHARS_START + i] = syntaxVector[SPECIAL_CHARS_START + i] / chars;
	}

	public BufferedReader getFactorieReader() throws FileNotFoundException{

		List<User> userList = LoadYelpData.getYelpReviews();

		// TODO move these somewhere less hard-coded
		int startIndex = 0;

		int numToTake = userList.size();

		File factorieFile = new File(FACTORIE_OUTPUT_FILE);

		// if file doesn't exist
		if(!factorieFile.exists() || this.functionTotals == null){
			try(PrintWriter output = new PrintWriter(new FileWriter(factorieFile))){

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
									if (splitLine.length == 7) {
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

			// TODO move this somewhere else
			Configuration config = new Configuration();
			Path path = new Path(OUTPUT_DIR + File.separator + "features");
			FileSystem fs = FileSystem.get(config);
			SequenceFile.Writer mahoutWriter = new SequenceFile.Writer(fs, config, path, Text.class, VectorWritable.class);

			PrintWriter textWriter = Util.getPrintWriter(OUTPUT_DIR + File.separator + "textFeatures");
			
			// initialize per-document counts using (keys of) existing data structures
			Map<String, Double> posPerDoc = Util.newMapFromKeySet(posTags, 0.0);
			Map<String, Double> parsePerDoc = Util.newMapFromKeySet(parseLabels, 0.0);
			Map<String, Double> functionPerDoc = Util.newMapFromKeySet(this.functionTotals.keySet(), 0.0);
			Map<String, Double> lemmasPerDoc = new HashMap<>(100);
			double[] syntaxVector = new double[SYNTAX_FEATURE_COUNT];

			String line;
			String user = null;
			String lastUser = null;
			String lastReview = "1";
			String review = null;
			int vectorClass = 0;
			double wordCount = 0;
			double charCount = 0;
			while ((line = factorieOutput.readLine()) != null) {
				if (!line.equals("")) {
					String[] splitLine = line.split("\\s+");
					if (splitLine.length == 9) {

						user = splitLine[0];
						review = splitLine[1];
						
						if(!review.equals(lastReview)){
							// write results before tabulating new results
							double[] allFreqs = populateFeatureVector(syntaxVector, posPerDoc, parsePerDoc, functionPerDoc, lemmasPerDoc, wordCount, charCount);
							String vectorKey = lastUser + "/" + lastReview;
							printMahoutVector(vectorKey, allFreqs, mahoutWriter);
							printTextVector(vectorClass, allFreqs, textWriter);
							
							// re-initialize data structures
							posPerDoc = Util.newMapFromKeySet(posTags, 0.0);
							parsePerDoc = Util.newMapFromKeySet(parseLabels, 0.0);
							functionPerDoc = Util.newMapFromKeySet(functionTotals.keySet(), 0.0);
							lemmasPerDoc = new HashMap<>(100);
							syntaxVector = new double[SYNTAX_FEATURE_COUNT];

							wordCount = 0;
							charCount = 0;
							vectorClass++;
						}

						String word = splitLine[WORD_IDX+2];
						String pos = splitLine[POS_IDX+2];
						String dep = splitLine[DEP_IDX+2];
						String lem = splitLine[LEM_IDX+2];

						wordCount += 1;
						charCount += word.length();
						
						// update syntax vector with current word
						syntaxFeatures(word, syntaxVector);

						posPerDoc.put(pos, posPerDoc.get(pos) + 1.0);
						parsePerDoc.put(dep, parsePerDoc.get(dep) + 1.0);
						
						if(lemmasPerDoc.containsKey(lem))
							lemmasPerDoc.put(lem, lemmasPerDoc.get(lem) + 1.0);
						else
							lemmasPerDoc.put(lem, 1.0);

						String lowerWord = word.toLowerCase();
						if (functionPosTags.contains(pos))
							functionPerDoc.put(lowerWord, functionPerDoc.get(lowerWord) + 1.0);

						lastReview = review;
						lastUser = user;
					}
				}
			}
			// write final results
			//			double[] allFreqs = populateFeatureVector(syntaxVector, posPerDoc, parsePerDoc, functionPerDoc);
			//			String vectorKey = user + "/" + review;
			//			printMahoutVector(vectorKey, allFreqs, mahoutWriter);

			mahoutWriter.close();
		} catch (IOException e){
			//TODO auto-generated catch block
			e.printStackTrace();
		}
	}

	private double[] populateFeatureVector(double[] syntaxVector, Map<String, Double> posPerDoc,
			Map<String, Double> parsePerDoc, Map<String, Double> functionPerDoc, Map<String, Double> lemmasPerDoc,
			double wordCount, double charCount) {

		// POS_TAGS.length + PARSE_LABELS.length + numObservedFuncWords + syntaxVector.length + 1
		int numFeatures = syntaxVector.length;
		if(POS_FREQS_ON)
			numFeatures += POS_TAGS.length;
		if(DEP_FREQS_ON)
			numFeatures += PARSE_LABELS.length;
		if(FUNC_FREQS_ON)
			numFeatures += this.functionTotals.keySet().size();
		if(YULE_ON)
			numFeatures += 1;
			
		double[] allFreqs = new double[numFeatures];

		String key;
		Double value, total;
		double freq;
		
		// populate feature vector(s)
		int i = 0;
		if(POS_FREQS_ON){
			for(Entry<String,Double> entry : posPerDoc.entrySet()){
				key = entry.getKey();
				value = entry.getValue();
				total = this.posTotals.get(key);
				freq = total != 0.0? value/total : 0.0;
				allFreqs[i] = freq;
				i++;
			}
		}

		if(DEP_FREQS_ON){
			for(Entry<String,Double> entry : parsePerDoc.entrySet()){
				key = entry.getKey();
				value = entry.getValue();
				total = this.parseTotals.get(key);
				freq = total != 0.0? value/total : 0.0;
				allFreqs[i] = freq;
				i++;
			}
		}

		if(FUNC_FREQS_ON){
			for(Entry<String,Double> entry : functionPerDoc.entrySet()){
				key = entry.getKey();
				value = entry.getValue();
				total = this.functionTotals.get(key);
				freq = total != 0.0? value/total : 0.0;
				allFreqs[i] = freq;
				i++;
			}
		}
		
		if(YULE_ON){
			allFreqs[i] = calculateYuleK(lemmasPerDoc);
			i++;
		}

		normalizeSyntaxVector(syntaxVector, wordCount, charCount);
		for(int l = 0; l < syntaxVector.length; l++)
		{
			allFreqs[i+l] = syntaxVector[l];
		}
		syntaxVector = new double[SYNTAX_FEATURE_COUNT];
		
		return allFreqs;
	}
	
	private double calculateYuleK(Map<String, Double> lemmasPerDoc) {
		// m1 = number of different lemmas in document (review)
		double m1 = lemmasPerDoc.keySet().size();
		
		Map<Double, Double> yuleMap = Util.reverseMap(lemmasPerDoc);
		
		// m2 = sum of products of each observed frequency squared and number of 
		// words with that frequency
		double m2 = 0.0;
		for(Entry<Double,Double> e : yuleMap.entrySet()){
			Double freq = e.getKey();
			Double wordCount = e.getValue();
			m2 += freq*freq*wordCount;
		}
		
		return 10000*(m2-m1)/(m1*m1);
	}
	
	private void printTextVector(int vectorKey, double[] vector, PrintWriter textWriter) {
		
		textWriter.print(vectorKey  + " ");
		for(int i = 0; i < vector.length; ++i){
			double val = vector[i];
			if(val != 0.0)
				textWriter.print(i + ":" + vector[i] + " ");
		}
		textWriter.println();
		
	}

	private static void printMahoutVector(String vectorKey, double[] vector, SequenceFile.Writer mahoutWriter)
	{
		// print poopler
//		System.out.print(vectorKey + ":");
//		for(int i = 0; i < vector.length; ++i){
//			System.out.print(vector[i] + " ");
//		}
//		System.out.println();

		VectorWritable vec = new VectorWritable();
		vec.set(new NamedVector(new DenseVector(vector), vectorKey));
		try {
			mahoutWriter.append(new Text(vectorKey), vec);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
