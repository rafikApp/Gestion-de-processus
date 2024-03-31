package server;

import static java.util.Map.entry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MathUtils {

	private static int productNumber(int number) {
		int tmp = 0;
		int product = 1;
		while (number != 0) {
			tmp = number % 10;
			number = number / 10;
			product *= tmp;
		}
		return product;
	}

	public static int computePersistance(int number) {
		int persistance = 0;
		int nbinit = number;
		while (nbinit / 10 != 0) {
			nbinit = productNumber(nbinit);
			persistance++;
		}
		return persistance;
	}

	public static String computePersistance(int from, int to) {
		String result = "";
		// CHECK FROM > To
		for (int i = from; i <= to; i++) {
			result += i + ":" + computePersistance(i) + ";";
		}
		// remove last :
		return result.substring(0, result.length() - 1);
	}

	// calcul de la moyenne (somme de toutes les persistances / nb des persistances
	// dans la map)
	public static double getAverage(Map<Integer, Integer> persistances) {
		double average = 0;
		// for (Integer number : persistances.values()) {}
		int sum = persistances.values().stream().mapToInt(Integer::intValue).sum();
		int nbElement = persistances.size();
		average = sum / nbElement;
		return average;

	}

	public static double getMediane(Map<Integer, Integer> persistances) {
		// Map<Integer, Integer> map=sortMap(persistances);
		// double mediane=0;
		// get length
		// int lengthmap=map.size();
		// System.out.println("The length of the map is: " + lengthmap);
		// on divise la liste en deux
		// int middle = lengthmap/2;
		// System.out.println("The middle of the map is: " + middle);
		ArrayList<Integer> list = new ArrayList(persistances.values());
		Collections.sort(list);
		double length = (double) list.size();

		int med = (int) Math.floor(length / 2);

		return list.get(med - 1);
		// on vérifie si c'est pair ou impair
		// si c'est impair
		// if (lengthmap%2 ==1) {
		// System.out.println(entry.getValue());
		// on retourne la valeur centrale
		// mediane= map.get(middle);
		/*
		 * mediane=map.get(lengthmap) / 2; //Tab[(n-1)//2] // } else { //mediane=
		 * ((map.get(lengthmap) -1) /2) + (map.get(lengthmap) / 2) / 2;
		 */
		// mediane= (map.get(middle) + map.get(middle - 1) )/ 2;
		// (array[mid] + array[mid - 1]) / 2
		// n = len(Tab)
		// return (Tab[n//2-1] + Tab[n//2])/2
		// return (array[mid] + array[mid - 1]) / 2
		// }
		// return mediane;
	}
	// calcul de nombre d'occurence de chaque persistance dans la map

	public static Map<Integer, Integer> nbOccOfEachPersistance(Map<Integer, Integer> persistances) { // countMap holds the count details
																					// of each element
		Map<Integer, Integer> countMap = new HashMap<Integer, Integer>();
		// entry(1, 2), entry(777, 1), entry(8, 3), entry(2, 5), entry(5, 7),entry(3, 4)
		for (Integer i : persistances.keySet()) {
			int value = persistances.get(i);
			// System.out.println(value);
			if (countMap.containsKey(value)) {
				int count = countMap.get(value);
				count++;
				countMap.put(value, count);
			} else {
				// si ca existe pas
				countMap.put(value, 1);
			}
		}
		return countMap;
		// Printing the Element and its occurrence in the array
//		for (Entry<Integer, Integer> val : countMap.entrySet()) {
//			System.out.println(val.getKey() + " occurs " + val.getValue() + " time(s)");
//		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Map<Integer, Integer> test = Map.ofEntries(entry(1, 2), entry(777, 1), entry(8, 3), entry(2, 5), entry(5, 7),
				entry(3, 4), entry(67, 3));
		double resultAverage = getAverage(test);

		// System.out.println("La moyenne est :" + resultAverage);
		//System.out.println(nbOccOfEachPersistance(test));
		double median = getMediane(test);
		System.out.println("La médiane est :" + median);

	}

}
