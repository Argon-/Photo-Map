package data_structures.heap;

import java.util.Arrays;



/**
 * See {@link #BinaryMinHeap(int) BinaryMinHeap}.
 */
public final class BinaryMinHeap
{
    private final double GROWTH_FACTOR = 0.5;

    private int[] heap_nid = null;
    private int[] heap_val = null;
    private int   size     = 0;


    /**
     * Initializes a new empty minimum heap with initial size {@code size}.<br>
     * This heap is specifically tailored for storing a node ID along with its value,
     * as used by {@link path.search.Dijkstra Dijkstra}.
     * The value is used for sorting.
     * <br><br>
     * The heap will increase its size upon {@link #insert(int, int) insert()} when {@link #isFull()}
     * {@code == true}. This requires copying the current heap.
     * 
     * @param size initial size
     */
    public BinaryMinHeap(int size)
    {
        heap_nid = new int[size];
        heap_val = new int[size];
        this.size = 0;
    }


    private void grow()
    {
        final int new_capacity = heap_nid.length + (int) (heap_nid.length * GROWTH_FACTOR) + 1;
        System.out.println(">>> Increasing BinaryMinHeap Capacity: " + heap_nid.length + " -> " + new_capacity + " <<<");
        heap_nid = Arrays.copyOf(heap_nid, new_capacity);
        heap_val = Arrays.copyOf(heap_val, new_capacity);
    }


    /**
     * Are there any elements stored in the heap?
     * 
     * @return {@code true} when there's no element stored
     */
    public boolean isEmpty()
    {
        return size == 0;
    }


    /**
     * Is the heap full?<br>
     * When this returns {@code true} a subsequent call to {@link #insert(int, int) insert()}
     * will increase the size of the heap.
     * 
     * @return {@code true} when full
     */
    public boolean isFull()
    {
        return size >= heap_nid.length;
    }


    /**
     * Returns the number of elements in the heap.
     * 
     * @return size
     */
    public int size()
    {
        return size;
    }


    /**
     * Insert a new heap element consisting of an arbitrary node ID and value.
     * Only the value is used for sorting, the node ID bears no relevance to the heap.
     * <br><br>
     * <b>Note:</b> this will increase the heap size in case {@link #isFull()}
     * {@code == true}, which requires to copy arrays. 
     * 
     * @param node_id
     * @param value
     */
    public void insert(int node_id, int value)
    {
        if (isFull())
            grow();

        int c = size;
        heap_nid[c] = node_id;
        heap_val[c] = value;
        ++size;

        int tmp1, tmp2;
        int parent = (c - 1) / 2;

        while ((heap_val[parent] > heap_val[c]) && parent >= 0) {
            tmp1 = heap_nid[parent];
            tmp2 = heap_val[parent];
            heap_nid[parent] = heap_nid[c];
            heap_val[parent] = heap_val[c];
            heap_nid[c] = tmp1;
            heap_val[c] = tmp2;
            c = parent;
            parent = (c - 1) / 2;
        }
    }


    /**
     * @return the min heap node ID
     */
    public int getMinID()
    {
        if (isEmpty())
            throw new RuntimeException("Empty Heap");
        else
            return heap_nid[0];
    }


    /**
     * @return the min heap node value
     */
    public int getMinValue()
    {
        if (isEmpty())
            throw new RuntimeException("Empty Heap");
        else
            return heap_val[0];
    }


    /**
     * <b>Note:</b> this will remove the node.
     * 
     * @return the min heap node
     */
    public int[] extractMin()
    {
        if (isEmpty())
            throw new RuntimeException("Empty Heap");
        final int[] min = new int[] { heap_nid[0], heap_val[0] };
        removeMin();
        return min;
    }


    /**
     * Remove the heap minimum without returning it.
     */
    public void removeMin()
    {
        if (isEmpty())
            throw new RuntimeException("Empty Heap");

        --size;
        heap_nid[0] = heap_nid[size];
        heap_val[0] = heap_val[size];
        if (size > 0) {
            heapify(0);
        }
    }


    private void heapify(int i)
    {
        int c, rc;
        while ((c = (i << 1) + 1) < size) { // c = left child
            rc = (i << 1) + 2; // right child
            // follow right child in case its value is lower
            if (rc < size && heap_val[c] > heap_val[rc]) {
                c = rc;
            }

            if (heap_val[i] > (heap_val[c])) {
                final int tmp1 = heap_nid[i];
                final int tmp2 = heap_val[i];
                heap_nid[i] = heap_nid[c];
                heap_val[i] = heap_val[c];
                heap_nid[c] = tmp1;
                heap_val[c] = tmp2;
            }
            else {
                break;
            }
            i = c;
        }
    }

}
