package net.zieglejn.osm_fapra.heap;

import java.util.Arrays;
import java.util.Random;

import net.zieglejn.osm_fapra.misc.StopWatch;



public class HeapTest_Old
{
	public static final int	RND_MAX	= 99;
	public static final int	RND_MIN	= -99;
	
	
	public static String arrayToString(int[][] n)
	{
		String s = "";
		for (int i = 0; i < n.length; ++i) {
			s += n[i][1];
			
			if (i != n.length - 1)
				s += ", ";
			else
				s += " ";
		}
		return s;
	}
	
	
	public static String arrayToString(int[] n)
	{
		String s = "";
		for (int i = 0; i < n.length; ++i) {
			s += n[i];
			
			if (i != n.length - 1)
				s += ", ";
			else
				s += " ";
		}
		return s;
	}
	
	
	// insert
	// extract
	public static void insertTest1(int size)
	{
		BinaryMinHeap_Old h = new BinaryMinHeap_Old(size);
		Random rnd = new Random();
		
		int[] values = new int[size];
		for (int i = 0; i < values.length; ++i) {
			values[i] = rnd.nextInt((RND_MAX - RND_MIN) + 1) + RND_MIN;
		}
		
		for (int i = 0; i < values.length; ++i) {
			h.insert(new int[] {i, values[i]});
		}
		Arrays.sort(values);
		
		boolean error = false;
		int[] ret = new int[size];
		
		for (int i = 0; i < values.length; ++i) {
			int[] v = h.extractMin();
			ret[i] = HeapNode_Old.dist(v);
			if (HeapNode_Old.dist(v) != values[i])
				error = true;
		}
		
		if (error) {
			System.out.println("Mismatch:");
			System.out.println("   Input    : " + arrayToString(values));
			System.out.println("   Extracted: " + arrayToString(ret));
		}
	}
	
	
	// insert
	// get + remove
	public static void insertTest2(int size)
	{
		BinaryMinHeap_Old h = new BinaryMinHeap_Old(size);
		Random rnd = new Random();
		
		int[] values = new int[size];
		for (int i = 0; i < values.length; ++i) {
			values[i] = rnd.nextInt((RND_MAX - RND_MIN) + 1) + RND_MIN;
		}

		for (int i = 0; i < values.length; ++i) {
			h.insert(new int[] {i, values[i]});
		}
		Arrays.sort(values);
		
		boolean error = false;
		int[] ret = new int[size];
		
		for (int i = 0; i < values.length; ++i) {
			int[] v = h.getMin();
			h.removeMin();
			ret[i] = HeapNode_Old.dist(v);
			if (HeapNode_Old.dist(v) != values[i])
				error = true;
		}
		
		if (error) {
			System.out.println("Mismatch:");
			System.out.println("   Input    : " + arrayToString(values));
			System.out.println("   Extracted: " + arrayToString(ret));
		}
	}
	
	
	// build
	// extract
	public static void buildTest(int size)
	{
		Random rnd = new Random();
		
		int[] reference = new int[size];
		for (int i = 0; i < reference.length; ++i) {
			reference[i] = rnd.nextInt((RND_MAX - RND_MIN) + 1) + RND_MIN;
		}
		BinaryMinHeap_Old h = new BinaryMinHeap_Old(reference);		
		Arrays.sort(reference);
		
		boolean error = false;
		int[] ret = new int[size];
		
		for (int i = 0; i < reference.length; ++i) {
			int[] v = h.extractMin();
			ret[i] = HeapNode_Old.dist(v);
			if (HeapNode_Old.dist(v) != reference[i])
				error = true;
		}
		
		if (error) {
			System.out.println("Mismatch:");
			System.out.println("   Input    : " + arrayToString(reference));
			System.out.println("   Extracted: " + arrayToString(ret));
		}
	}
	
	
	// build, decreaseKey
	// extract
	public static void decreaseKeyTest(int size)
	{
		Random rnd = new Random();
		
		int[] values = new int[size];
		for (int i = 0; i < values.length; ++i) {
			values[i] = rnd.nextInt((RND_MAX - RND_MIN) + 1) + RND_MIN;
		}
		//System.out.println("   Input    : " + arrayToString(values));
		BinaryMinHeap_Old h = new BinaryMinHeap_Old(values);
		
		for (int i = 0; i < size/2; ++i) {
			int r = rnd.nextInt(size + 1);
			int m = rnd.nextInt((RND_MAX - RND_MIN) + 1) + RND_MIN;
			h.decreaseKey(r, m);
		}
		
		boolean error = false;
		int[] ret = new int[size];
		ret[0] = h.extractMin()[1];
		int last = ret[0];
		
		for (int i = 1; i < values.length; ++i) {
			ret[i] = h.extractMin()[HeapNode_Old.DIST];
			if (ret[i] < last)
				error = true;
			last = ret[i];
		}
		
		if (error) {
			System.out.println("Mismatch:");
			System.out.println("   Input    : " + arrayToString(values));
			System.out.println("   Extracted: " + arrayToString(ret));
		}
	}

	
	// build, decreaseKeyByNodeID
	// extract
	public static void decreaseKeyByNodeIDTest(int size)
	{
		Random rnd = new Random();
		
		int[] ref = new int[size];
		for (int i = 0; i < ref.length; ++i) {
			ref[i] = rnd.nextInt((RND_MAX - RND_MIN) + 1) + RND_MIN;
		}

		BinaryMinHeap_Old h = new BinaryMinHeap_Old(ref);
		
		for (int i = 0; i < size/2; ++i) {
			int m = rnd.nextInt((RND_MAX - RND_MIN) + 1) + RND_MIN;
			if (m < ref[i]) {
				//System.out.println("> Going to decrease node with ID " + i + " with current value " + ref[i] + " to value " + m);
				ref[i] = m;
				h.decreaseKeyByNodeID(i, m);
			}
			else {
				//System.out.println("Invalid m: " + m);
			}
		}
		
		//System.out.println("Input (after decrease): " + arrayToString(ref));
		
		boolean error = false;
		int[] ret = new int[size];
		Arrays.sort(ref);
		
		for (int i = 0; i < ref.length; ++i) {
			ret[i] = h.extractMin()[HeapNode_Old.DIST];
			if (ret[i] != ref[i])
				error = true;
		}
		
		//System.out.println("Should                : " + arrayToString(ref));
		//System.out.println("Output                : " + arrayToString(ret));
		
		if (error) {
			System.out.println("Mismatch:");
			System.out.println("   Input    : " + arrayToString(ref));
			System.out.println("   Extracted: " + arrayToString(ret));
		}
	}
	
	
	public static void staticTest()
	{
		// input
		//int[] v = new int[] {1, 2, 4, 3, 4, 5, 6, 7};
		int[] v = new int[] {-2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, -2};
		
				
		// heap hier mal auch ohne O(n) build bauen
		//BinaryMinHeap h = new BinaryMinHeap(v.length, true);
		BinaryMinHeap_Old h = new BinaryMinHeap_Old(v);
		//for (int i = 0; i < v.length; ++i) {
		//	h.insert(new int[] {i, v[i]});
		//}
		
		System.out.println("Input                 : " + arrayToString(v));
		
		
		// decrease tests
		h.decreaseKeyByNodeID(2, 1);
		v[2] = 1;
		h.decreaseKeyByNodeID(4, 1);
		v[4] = 1;
		h.decreaseKeyByNodeID(6, 0);
		v[6] = 0;
		
		System.out.println("Input (after decrease): " + arrayToString(v));
		
		// extract
		int[] ret = new int[v.length];
		for (int i = 0; i < ret.length; ++i)
			ret[i] = h.extractMin()[HeapNode_Old.DIST];

		Arrays.sort(v);
		System.out.println("Should                : " + arrayToString(v));
		System.out.println("Output                : " + arrayToString(ret));
	}
	

	public static void main(String[] args)
	{
		//System.out.println("# Static Test");
		//staticTest();
		System.out.println("\n# Random Tests");
		
		
		final int FROM = 1;
		final int TO   = 100;
		
		System.out.println("Insert Test 1");
		StopWatch.lap();
		for (int i = FROM; i < TO; ++i) {
			insertTest1(i);
		}
		System.out.println("...took " + StopWatch.lapSec() + " sec");
		
		System.out.println("Insert Test 2");
		StopWatch.lap();
		for (int i = FROM; i < TO; ++i) {
			insertTest2(i);
		}
		System.out.println("...took " + StopWatch.lapSec() + " sec");
		
		System.out.println("Build Test");
		StopWatch.lap();
		for (int i = FROM; i < TO; ++i) {
			buildTest(i);
		}
		System.out.println("...took " + StopWatch.lapSec() + " sec");
		
		System.out.println("DecreaseKey Test");
		StopWatch.lap();
		for (int i = FROM; i < TO; ++i) {
			decreaseKeyTest(i);
		}
		System.out.println("...took " + StopWatch.lapSec() + " sec");
		
		System.out.println("DecreaseKeyByNodeID Test");
		StopWatch.lap();
		for (int i = FROM; i < TO; ++i) {
			decreaseKeyByNodeIDTest(i);
		}
		System.out.println("...took " + StopWatch.lapSec() + " sec");
		
	}
}
