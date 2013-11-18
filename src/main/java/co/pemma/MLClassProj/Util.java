package co.pemma.MLClassProj;

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
}
