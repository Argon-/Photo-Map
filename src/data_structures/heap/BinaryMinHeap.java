package data_structures.heap;

import java.util.Arrays;



public final class BinaryMinHeap
{
	private final double growthFactor = 0.5;
	
	private int[]		heap_nid	= null;
	private int[]		heap_val	= null;

	private int			size		= 0;
	private boolean[]	touched		= null;
	

	/**
	 * Initializes a new empty heap with fixed size.
	 * 
	 * @param size
	 * @param <strike>buildHeap</strike>
	 */
	public BinaryMinHeap(int size)
	{
		this.heap_nid = new int[size];
		this.heap_val = new int[size];
		this.size = 0;
	}
	
	
	public void cleanup()
	{
		// touched-Liste mit anschließenden Bereinigung nur dieser
		for (int i = 0; i < touched.length; ++i)
		{
			
		}
	}
	
	
	private void grow()
	{
		int new_capacity = this.heap_nid.length + ((int) (this.heap_nid.length * growthFactor) + 1);
		System.out.println("   >>> Increasing BinaryMinHeap Capacity: " + this.heap_nid.length + " -> " + new_capacity + " <<<");
		this.heap_nid = Arrays.copyOf(this.heap_nid, new_capacity);
		this.heap_val = Arrays.copyOf(this.heap_val, new_capacity);
	}
	

	public boolean isEmpty()
	{
		return this.size == 0;
	}
	
	
	public boolean isFull()
	{
		return this.size >= this.heap_nid.length;
	}
	
	
	public int size()
	{
		return this.size;
	}


	public void insert(int node_id, int value)
	{
		if (this.isFull())
			this.grow();

		int c = this.size;
		this.heap_nid[c] = node_id;
		this.heap_val[c] = value;
		++this.size;

		int tmp1, tmp2;
		int parent = (c - 1) / 2;

		while ((this.heap_val[parent] > this.heap_val[c]) && parent >= 0)
		{
			tmp1 = this.heap_nid[parent];
			tmp2 = this.heap_val[parent];
			this.heap_nid[parent] = this.heap_nid[c];
			this.heap_val[parent] = this.heap_val[c];
			this.heap_nid[c] = tmp1;
			this.heap_val[c] = tmp2;
			c = parent;
			parent = (c - 1) / 2;
		}
	}
	
	
	/**
	 * @return the min heap node ID.
	 */
	public int getMinID()
	{
		if (this.isEmpty())
			throw new RuntimeException("Empty Heap");
		else
			return this.heap_nid[0];
	}
	
	
	/**
	 * @return the min heap node value.
	 */
	public int getMinValue()
	{
		if (this.isEmpty())
			throw new RuntimeException("Empty Heap");
		else
			return this.heap_val[0];
	}


	/** 
	 * This will remove the node.
	 * @return the min heap node.
	 */
	public int[] extractMin()
	{
		if (this.isEmpty())
			throw new RuntimeException("Empty Heap");
		int[] min = new int[] {this.heap_nid[0], this.heap_val[0]};
		this.removeMin();
		return min;
	}


	public void removeMin()
	{
		if (this.isEmpty())
			throw new RuntimeException("Empty Heap");

		--this.size;
		this.heap_nid[0] = this.heap_nid[this.size];
		this.heap_val[0] = this.heap_val[this.size];
		if (this.size > 0) {
			this.heapify(0);
		}
	}


	private void heapify(int i)
	{
		int c, rc;
		while ((c = (i << 1) + 1) < this.size) {	// left child
			rc = (i << 1) + 2;						// right child
			if (rc < this.size && this.heap_val[c] > this.heap_val[rc]) {
				c = rc;
			}

			if (this.heap_val[i] > (this.heap_val[c])) {
				int tmp1 = this.heap_nid[i];
				int tmp2 = this.heap_val[i];
				this.heap_nid[i] = this.heap_nid[c];
				this.heap_val[i] = this.heap_val[c];
				this.heap_nid[c] = tmp1;
				this.heap_val[c] = tmp2;
			}
			else {
				break;
			}
			i = c;
		}
	}
	
		
	public int getNodeDist(int n)
	{
		if (n > this.size || n < 0) {
			throw new RuntimeException("hurr");
		}
		return this.heap_val[n];
	}
	
	
	public int getSize()
	{
		return this.size;
	}
	
}
