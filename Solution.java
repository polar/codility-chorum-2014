import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This class is an attempt at the Codility Chorum 2014 Challenge. 
 * https://codility.com/programmers/task/tree_trip/
 * 
 * Find the maximal amount of cities less than or equal to K that are comprised
 * of cities that are more equally attractive or than ones that are not. In the
 * graph there is exactly one bidirectional route between any two distinct 
 * cities. This condition means that there are no loops between cities, which
 * essentially means that you can not travel from one city to itself in the
 * graph unless graph index is equal to itself. 
 * 
 * This solution, which I believe is correct, is specified in a recursive
 * manner. Although, some effort was put into performance, it will not 
 * be acceptable to Codility's performance tests. Further, refinement is needed.
 * @author polar
 *
 */
public class Solution {
	
	/**
	 * Represents a City in the traversal graph.
	 */
	class City {

		int index;
		int attractiveness;
		
		// Direct roads we order them in terms of next attractiveness.
		OrderedCities neighbors = new OrderedCities();
		
		City(int index, int attractiveness) {
			this.index = index;
			this.attractiveness = attractiveness;
		}
		
		void add(City city) {
			this.neighbors.add(city);
		}
		
		public String toString() {
			return String.format("City(%d-%d)", index, attractiveness);
		}
	}
	
	static class OrderedCities extends PriorityQueue<City> implements Cloneable {

		static Comparator<City> compareCity = new Comparator<City>() {
	        @Override
	        public int compare(City arg0, City arg1) {
	            return arg1.attractiveness - arg0.attractiveness;
	        }
	    };
	    
	    OrderedCities(OrderedCities o) {
	    	this();
	    	this.addAll(o);
	    }
	    
	    OrderedCities copy() {
	    	return new OrderedCities(this);
	    }
	    
		OrderedCities() {
			super(1, compareCity);
		}
		
		/**
		 * Returns the set of cities with the most attractiveness.
		 * This method does not remove them from the list;
		 */
		List<City> top() {
			List<City> ts = new ArrayList<City>();
			City t = remove();
			ts.add(t);
			while(!isEmpty() && peek().attractiveness == t.attractiveness) {
				City x = remove();
				ts.add(x);
			}
			addAll(ts);
			return ts;
		}
		
		/**
		 * Returns the set of all cities in the list that have a strictly
		 * higher attractiveness than the given attractiveness. It removes
		 * them from the list.
		 * @param attractiveness
		 * @return
		 */
		List<City> lopHigher(int attractiveness) {
			List<City> ts = new ArrayList<City>();
			while(!isEmpty() && peek().attractiveness > attractiveness) {
				ts.add(remove());
			}
			return ts;
		}
	}
	
	/**
	 * This method builds the traversal graph using the direct roads as children. 
	 * It also calculate the set of cities with the maximum attractiveness. 
	 * It returns the cities with the maximum attractiveness.
	 * @param C The City Map
	 * @param D The Attractive Map
	 * @param cities The indexed list of cities to build into.
	 * @return the cities with the maximum attractiveness.
	 */ 
	List<City> stageCities(int[] C, int[] D, City[] cities) {
		City max = null;
		List<City> maxes = new ArrayList<City>();
		
		for(int i = 0; i < C.length; i++) {
			City city1,city2;
			if (cities[i] == null) {
				cities[i] = city1 = new City(i,D[i]);
				if (max == null) {
					max = city1;
					maxes.add(city1);
				} else {
					if (max.attractiveness == city1.attractiveness) {
						maxes.add(city1);
					} else 	if (max.attractiveness < city1.attractiveness) {
						max = city1;
						maxes = new ArrayList<City>();
						maxes.add(city1);
					}
				}
			} else {
				city1 = cities[i];
			}
			
			// If C[P] = Q and P == Q, skip
			if (C[i] == i) {
				continue;
			}
			
			if (cities[C[i]] == null) {
				cities[C[i]] = city2 = new City(C[i], D[C[i]]);
				if (max == null) {
					max = city2;
					maxes.add(city2);
				} else {
					if (max.attractiveness == city2.attractiveness) {
						maxes.add(city2);
					} else 	if (max.attractiveness < city2.attractiveness) {
						max = city2;
						maxes = new ArrayList<City>();
						maxes.add(city2);
					}
				}
			} else {
				city2 = cities[C[i]];
			}
			// bidirectional roads
			city1.add(city2);
			city2.add(city1);
		}
		// There, of course, may be more than one.
		return maxes;
	}
	
	/**
	 * There is exactly one path between distinct cities. This returns it.
	 * @param start
	 * @param dest
	 * @param cities
	 * @return
	 */
	List<City> getPath(City start, City dest, City[] cities) {
		boolean[] seen = new boolean[cities.length];
		List<City> path = getPath(start, dest, seen);
		return path;
	}
	
	/**
	 * This is the recursive sub function for finding the path from the
	 * start to the dest. Since there are really no loops, but our graph
	 * is bidirectional, we use a seen bobolean to make sure we don't loop.
	 * @param start
	 * @param dest
	 * @param seen
	 * @return
	 */
	List<City> getPath(City start, City dest, boolean[] seen) {
		if (start == dest) {
			List<City> list = new ArrayList<City>();
			list.add(dest);
			return list;
		}
		if (seen[start.index]) {
			return null;
		}
		seen[start.index] = true;
		for(City c : start.neighbors) {
			List<City> res = getPath(c, dest, seen);
			if (res != null) {
				res.add(start);
				return res;
			}
		}
		return null;
	}
	
	/**
	 * This class keeps the set of cities that are included in the trip plan.
	 * We use a set as to eliminate duplicate entries.
	 * It also is used to keep a hash of paths to help with efficiency. That is,
	 * if a path is added to the set, then its start and destination are 
	 * recorded here, so that it may be queried with the positive result being
	 * that the path will not have to be found, because its cities already
	 * reside in this set.
	 */
	class CitySet extends HashSet<City> {
		/**
		 * The set of paths that are included by way of addPath.
		 */
		HashSet<BigInteger> paths = new HashSet<BigInteger>();
		BigInteger primeF(int a, int b) {
			if (a < b) {
				return BigInteger.valueOf(2).pow(a).multiply(BigInteger.valueOf(3).pow(b));
			} else {
				return BigInteger.valueOf(2).pow(b).multiply(BigInteger.valueOf(3).pow(a));
			}
		}
		
		/**
		 * This method adds the path to the set. We also spend a little
		 * time in adding all the sub paths to the lookup.
		 * @param from should be the first of path.
		 * @param to should be the last of path.
		 * @param path the path from to to.
		 */
		void addPath(City from, City to, List<City> path) {
			this.addAll(path);
			int i = 0;
			while(i < path.size()-1) {
				paths.add(primeF(from.index, path.get(i+1).index));
				paths.add(primeF(path.get(i).index, path.get(i+1).index));
				paths.add(primeF(path.get(i).index, to.index));
				i++;
			}
		}
		
		boolean containsPath(City from, City to) {
			return paths.contains(primeF(from.index, to.index));
		}
		
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		
		public void remove(City a) {
			max = Integer.MIN_VALUE;
			min = Integer.MAX_VALUE;
			super.remove(a);
		}
		
		/** 
		 * Returns the maximum attractiveness of the set.
		 * @return
		 */
		int max() {
			if (max == Integer.MIN_VALUE) {
				for(City c : this) {
					max = Math.max(max, c.attractiveness);
				}
			}
			return max;
		}
		/**
		 * Returns the minimum attractiveness of the set.
		 * @return
		 */
		int min() {
			if (min == Integer.MAX_VALUE) {
				for(City c : this) {
					min = Math.min(min, c.attractiveness);
				}
			}
			return min;
		}
		
		CitySet() {
			
		}
		/**
		 * Copies a set along with its paths lookup.
		 * @param set
		 */
		CitySet(CitySet set) {
			this.paths.addAll(set.paths);
			for(City c : set) {
				super.add(c);
			}
		}
		
		/**
		 * Copies a set, along with its paths lookup.
		 * @return
		 */
		CitySet copy() {
			CitySet set = new CitySet(this);
			return set;
		}
	}
	
	/**
	 * This function is the top of the recursive definition. 
	 * We rectify a set of cities of the same attractiveness against
	 * all the cities under the maximum amount, k).
	 * 
	 * @param sameA list of cities of the same attractiveness.
	 * @param k maximum number desired
	 * @param cities the indexed list of cities.
	 * @return the maximum that is less than or equal to k. 
	 */
	int rectify(List<City> sameA, int k, City[] cities) {
		OrderedCities av = new OrderedCities();
		for(City c : cities) {
			av.add(c);
		}
		CitySet set = new CitySet();
		return rectify(sameA, k, set, av, cities);
	}
	
	/**
	 * This function is the sub-recursive function. 
	 * We rectify a set of cities of the same attractiveness against
	 * all the cities under the maximum amount, k).
	 * 
	 * @param sameA list of cities of the same attractiveness.
	 * @param k maximum number desired
	 * @param set The set of cities in the trip plan for a desired particular attractiveness
	 * @param av The list of available cities that are not in the set in order of attractiveness.
	 * @param cities The indexed list of cities.
	 * @return the maximum number of cities in a maximal valid trip plan. 
	 */
	int rectify(List<City> sameA, int k, CitySet set, OrderedCities av, City[] cities) {
		// We have a list of maximal attractiveness, with available cities not yet traversed;
		// We rectify each city with itself removed from the available, thereby leaving the
		// other cities of the same attractiveness, against the current set of selected cities;
		int max = Integer.MIN_VALUE;
		for(City c : sameA) {
			av.remove(c);
			int x = rectify(c, k, set, av, cities);
			if (x == k) {
				av.add(c);
				return k;
			}
			max = Math.max(max, x);
			av.add(c);
		}
		return max;
	}
	
	/**
	 * Propose a set that contains the given city and rectify it against the
	 * trip plan it represents. This city may already be in the set due to
	 * path traversal of two other cities in the trip plan. However it still
	 * needs to be checked as its path between other cities in the trip plan
	 * may not be included. We take new cities from the available cities list.
	 * @param start
	 * @param k
	 * @param set
	 * @param av
	 * @param cities
	 * @return
	 */
	int rectify(City start, int k, CitySet set, OrderedCities av, City[] cities) {
		HashSet<City> removed = new HashSet<City>();
		OrderedCities avail = av.copy();
		CitySet proposed = set.copy();
		proposed.add(start);
		
		// If this city is to be in the included set, due to the requirements,
		// then all its higher attractiveness cities must also be on the set,
		// regardless of whether we have paths for them yet..
		List<City> higher = avail.lopHigher(start.attractiveness);
		proposed.addAll(higher);
		
		// Short circuit.
		if (proposed.size() > k) {
			return -1;
		}
		
		// Now we search for other cities. We try to find all the cities between
		// the start and the other cities in the set. If there are any new
		// cities introduced along the path, we need to add their more 
		// attractive cities. The are added to the working list if found.
		List<City> working = new ArrayList<City>();
		working.addAll(proposed);
		while(!working.isEmpty()) {
			City c = working.remove(0);
			if (start != c && !proposed.containsPath(start, c)) {
				List<City> need = getPath(start, c,cities);
				proposed.addPath(start, c, need);
				if (proposed.size() > k) {
					return set.size();
				}
				avail.removeAll(need);
				
				// We may have added cities with lower attractiveness.
				// If we did, we have to add them and check the paths to them.
				List<City> h2 = avail.lopHigher(proposed.min());
				proposed.addAll(h2);
				// Short circuit.
				if (proposed.size() > k) {
					return set.size();
				}
				// And any cities introduced that are of higher attractiveness.
				working.addAll(h2);
			}
		}
		int size = proposed.size();
		// We have a valid trip plan. However, it may be too large, or not
		// maximal. Advance, which will check the current proposed state.
		int res = advance(proposed, k, avail, cities);
		if (res < 0) {
			return size;
		}
		return res;
	}
	
	/**
	 * Check and advance the trip plan. The valid trip plan will be of a 
	 * certain attractiveness, but may be too large or may not be maximal.
	 * If it is not too large but not quite k, we advance using the next
	 * level of attractiveness and rectify with that set. If it only returns
	 * -1 (no valid trip plan found) then we are done.
	 * @param set The valid trip plan of a certain attractiveness.
	 * @param k
	 * @param av
	 * @param cities
	 * @return
	 */
	int advance(CitySet set, int k, OrderedCities av, City[] cities) {
		int size = set.size();
		if (size > k) {
			return -1;
		}
		if (size == k) {
			return k;
		}
		// size < k
		if (av.isEmpty()) {
			return size;
		}
		// We get the next set of maximal attractiveness from the available list.
		List<City> top = av.top();
		int res =  rectify(top, k, set, av, cities);
		if (res < 0) {
			return size;
		}
		return res;
	}
	
	int solution(int K, int[] C, int[] D) {
		City[] cities = new City[C.length];
		List<City> maxes = stageCities(C, D, cities);
		int m = Integer.MIN_VALUE;
		m = Math.max(m, rectify(maxes, K, cities));
		return m;
	}
	
	static void print(String name, List<Integer> array) {
		System.out.print(String.format("%s[%d] = [", name, array.size()));
		for(int a : array) {
			System.out.print(String.format("%d,",a));
		}
		System.out.println();
	}
	
	static void print(String name, int[] array) {
		System.out.print(String.format("%s[%d] = [", name, array.length));
		for(int a : array) {
			System.out.print(String.format("%d,",a));
		}
		System.out.println();
	}
	
	static void print(List<City> cities) {
		System.out.print("[");
		for(City c : cities) {
			System.out.format("%s,", c);
		}
		System.out.println("]");
	}

	static int[] C = new int[] {1, 3, 0, 3, 2, 4, 4};
	static int[] D = new int[] {6, 2, 7, 5, 6, 5, 2};

	static int[] straight(int len) {
		int[] C = new int[len];
		for(int i = 1; i < len; i++) {
			C[i] = i - 1;
		}
		C[0] = 0;
		return C;
	}
	
	static int[] star(int len) {
		int[] C = new int[len];
		for(int i = 1; i < len; i++) {
			C[i] = 0;
		}
		C[0] = 0;
		return C;
	}
	static int[] bad(int[] C) {
		int[] D = new int[C.length];
		for(int i = D.length-1; i >=0  ; i--) {
			D[i] = 4;
		}
		D[0] = 0;
		return D;
	}
	public static void main(String[] args) {
		int K = 5;
		Solution sol = new Solution();
		int[] map = C;
		int[] att = D;
		long time1 = System.currentTimeMillis();
		int res = sol.solution(K, map, att);
		long time2 = System.currentTimeMillis();
		System.out.format("Answer %d  %dms",  res, time2-time1);
	}
	

}
