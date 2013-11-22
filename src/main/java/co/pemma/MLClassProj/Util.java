package co.pemma.MLClassProj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Util {
	
	/**
	 * Creates a new Map associating values from the given keySet with the given
	 * default value.
	 * 
	 * @param keySet the keyset with which to initialize the new Map
	 * @param defaultValue default value to associate with the given keys
	 * @return new Map mapping the given keyset to the given default value
	 */
	public static <K,V> Map<K,V> newMapFromKeySet(Set<K> keySet, V defaultValue) {
		Map<K,V> newMap  = new HashMap<>(keySet.size());
		for(K key : keySet){
			newMap.put(key, defaultValue);
		}
		return newMap;
	}
	
	public static <T> Map<Double,Double> reverseMap(Map<T,Double> m) {
		Map<Double,Double> newMap  = new HashMap<>(m.size());
		for(Double value : m.values()){
			if(newMap.containsKey(value))
				newMap.put(value, newMap.get(value) + 1.0);
			else
				newMap.put(value, 1.0);
		}
		return newMap;
	}
	
	public static BufferedReader processSentence(String inputSentence) throws IOException{
		
//		try(Socket connection = new Socket("localhost", 3228)){
//			PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())), true);
//			writer.println(inputSentence);
//			connection.shutdownOutput();
//			return new BufferedReader(new InputStreamReader(connection.getInputStream()));	
//		} catch (IOException e){
//			e.printStackTrace();
//		}
//		return null;
		
		// TODO fix socket closing situation
		// ok, actually, this socket is definitely always getting closed, Eclipse just doens't know it
		Socket connection = new Socket("localhost", 3228);
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())), true);
		writer.println(inputSentence);
		connection.shutdownOutput();
		return new BufferedReader(new InputStreamReader(connection.getInputStream()));	
	}
}
