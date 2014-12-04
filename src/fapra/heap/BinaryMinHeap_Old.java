package fapra.heap;

import java.util.Arrays;



public final class BinaryMinHeap_Old
{
	private int[][]	heap	= null;
	//private int[] heap_nid = null;
	//private int[] heap_val = null;
	
	private int[]	map		= null;
	private int		size	= 0;
	

	/**
	 * Initializes a new empty heap with fixed size.
	 * <br><br>
	 * <strike>Depending on <code>buildHeap</code>, additionally builds and maintains 
	 * a mapping from <code>Node_ID</code> (first dimension in <code>int[][] 
	 * newHeap</code>) and the node's current position within the heap.</strike>
	 * 
	 * @param size
	 * @param <strike>buildHeap</strike>
	 */
	public BinaryMinHeap_Old(int size/*, boolean buildMap*/)
	{
		this.heap = new int[size][];
		//this.heap_nid = new int[size];
		//this.heap_val = new int[size];
		//if (buildMap) {
			this.map  = new int[size];
			Arrays.fill(this.map, -1);
		//}
		this.size = 0;
	}
	
	
	/**
	 * Builds a heap by cloning <code>newHeap</code> (deep).
	 * <br><br>
	 * <strike>Depending on <code>buildHeap</code>, additionally builds and maintains 
	 * a mapping from <code>Node_ID</code> (first dimension in <code>int[][] 
	 * newHeap</code>) and the node's current position within the heap.</strike>
	 * 
	 * @param values
	 * @param <strike>buildHeap</strike>
	 * @throws InvalidNodeIDConstraintException 
	 */
	public BinaryMinHeap_Old(int[] values/*, boolean buildMap*/)
	{
		this.heap = new int[values.length][];
		this.size = values.length;
		for (int i = 0; i < values.length; ++i) {
			this.heap[i] = new int[HeapNode_Old.SIZE];
			this.heap[i][HeapNode_Old.NODE_ID] = i;
			this.heap[i][HeapNode_Old.DIST]    = values[i];
		}
		
		//if (buildMap) {
			this.map = new int[this.heap.length];
		//}
		
		for (int i = (this.size / 2) - 1; i >= 0; --i) {
			this.heapify(i);
		}
		
		// 'consistency' check
		for (int i = 0; i < this.heap.length; ++i) {
			if (this.heap[i][HeapNode_Old.NODE_ID] < 0 || this.heap[i][HeapNode_Old.NODE_ID] >= this.heap.length) {
				throw new RuntimeException("Invalid Node ID Constraint");
			}
			//if (this.map != null)
				this.map[this.heap[i][HeapNode_Old.NODE_ID]] = i;
		}
	}


	public boolean isEmpty()
	{
		return this.size == 0;
	}
	
	
	public boolean isUninitialized()
	{
		return this.heap == null;
	}



	public boolean isFull()
	{
		return !this.isUninitialized() && !(this.size < this.heap.length);
	}


	private int leftChild(int i)
	{
		return (2 * i) + 1;
	}


	private int rightChild(int i)
	{
		return (2 * i) + 2;
	}


	private int parent(int i)
	{
		return (i - 1) / 2;
	}


	private boolean hasLeftChild(int i)
	{
		return leftChild(i) < this.size;
	}


	private boolean hasRightChild(int i)
	{
		return rightChild(i) < this.size;
	}


	public void insert(int[] value)
	{
		if (this.heap == null)
			throw new RuntimeException("Uninitialized Heap");
		if (this.isFull())
			throw new RuntimeException("Heap Full");
		if (value[HeapNode_Old.NODE_ID] < 0 || value[HeapNode_Old.NODE_ID] >= this.heap.length) {
			throw new RuntimeException("Invalid Node ID Constraint");
		}

		int c = this.size;
		this.heap[c] = value.clone();
		if (this.map != null) {
			this.map[this.heap[c][HeapNode_Old.NODE_ID]] = c;
		}
		++this.size;

		int[] tmp;
		int p = this.parent(c);

		while ((this.heap[p][HeapNode_Old.DIST] > this.heap[c][HeapNode_Old.DIST]) && p >= 0)
		{
			//if (this.map != null) {
				this.map[this.heap[c][HeapNode_Old.NODE_ID]] = p;
				this.map[this.heap[p] [HeapNode_Old.NODE_ID]] = c;
			//}
			tmp = this.heap[p];
			this.heap[p] = this.heap[c];
			this.heap[c] = tmp;
			c = p;
			p = this.parent(c);
		}
	}
	
	
	/**
	 * @return a <u>reference</u> to the min HeapNode.
	 */
	public int[] getMin()
	{
		if (this.isEmpty())
			throw new RuntimeException("Empty Heap");
		else
			return this.heap[0];
	}


	/**
	 * @return a <u>reference</u> to the min HeapNode.
	 */
	public int[] extractMin()
	{
		if (this.isEmpty())
			throw new RuntimeException("Empty Heap");
		int[] min = this.heap[0];
		this.removeMin();
		return min;
	}


	public void removeMin()
	{
		if (this.isEmpty())
			throw new RuntimeException("Empty Heap");

		--this.size;
		//if (this.map != null) {
			this.map[this.heap[0][HeapNode_Old.NODE_ID]] = -1;
		//}
		this.heap[0] = this.heap[this.size];
		if (this.size > 0) {
			this.heapify(0);
		}
	}


	private void heapify(int i)
	{
		while (hasLeftChild(i)) {
			int c = leftChild(i);
			if (this.hasRightChild(i) && this.heap[this.leftChild(i)][HeapNode_Old.DIST] > this.heap[this.rightChild(i)][HeapNode_Old.DIST]) {
				c = this.rightChild(i);
			}

			if (this.heap[i][HeapNode_Old.DIST] > (this.heap[c][HeapNode_Old.DIST])) {
				//if (this.map != null) {
					this.map[this.heap[i][HeapNode_Old.NODE_ID]] = c;
					this.map[this.heap[c][HeapNode_Old.NODE_ID]] = i;
				//}
				int[] tmp = this.heap[i];
				this.heap[i] = this.heap[c];
				this.heap[c] = tmp;
			}
			else {
				break;
			}
			i = c;
		}
	}
	
	
	public void decreaseKey(int pos, int new_dist)
	{
		if (pos < 0 || pos >= this.size) {
			throw new RuntimeException("ERROR pos: " + pos);
			//return;
		}
		// ignore silently
		if (new_dist > this.heap[pos][HeapNode_Old.DIST]) {
			return;
		}
		
		this.heap[pos][HeapNode_Old.DIST] = new_dist;
		int p = parent(pos);
		
		while (pos > 0 && this.heap[pos][HeapNode_Old.DIST] < this.heap[p][HeapNode_Old.DIST]) {
			//if (this.map != null) {
				this.map[this.heap[pos][HeapNode_Old.NODE_ID]] = p;
				this.map[this.heap[p][HeapNode_Old.NODE_ID]] = pos;
			//}
			int[] t = this.heap[pos];
			this.heap[pos] = this.heap[p];
			this.heap[p] = t;
			pos = p;
			p = parent(pos);
		}
	}
	
	
	public boolean decreaseKeyByNodeID(int n, int new_dist)
	{
		if (this.map[n] == -1) {
			return false;
		}
		if (n < 0 || n >= this.map.length) {
			throw new RuntimeException("Invalid Node ID Constraint");
		}
		this.decreaseKey(this.map[n], new_dist);
		return true;
	}
	
	
	public int getNodeDist(int n)
	{
		if (this.map[n] == -1) {
			return Integer.MAX_VALUE;
		}
		return this.heap[this.map[n]][HeapNode_Old.DIST];
	}
	
	
	public boolean contains(int n)
	{
		return this.map[n] != -1;
	}
	
	
	public int getSize()
	{
		return this.size;
	}
	
}
