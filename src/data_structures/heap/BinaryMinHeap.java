package data_structures.heap;

import java.util.Arrays;



public final class BinaryMinHeap
{
    private final double growthFactor = 0.5;

    private int[] heap_nid = null;
    private int[] heap_val = null;
    private int   size     = 0;


    /**
     * Initializes a new empty heap with fixed size.
     * 
     * @param size
     * @param <strike>buildHeap</strike>
     */
    public BinaryMinHeap(int size)
    {
        heap_nid = new int[size];
        heap_val = new int[size];
        this.size = 0;
    }


    private void grow()
    {
        int new_capacity = heap_nid.length + ((int) (heap_nid.length * growthFactor) + 1);
        System.out.println(">>> Increasing BinaryMinHeap Capacity: " + heap_nid.length + " -> " + new_capacity + " <<<");
        heap_nid = Arrays.copyOf(heap_nid, new_capacity);
        heap_val = Arrays.copyOf(heap_val, new_capacity);
    }


    public boolean isEmpty()
    {
        return size == 0;
    }


    public boolean isFull()
    {
        return size >= heap_nid.length;
    }


    public int size()
    {
        return size;
    }


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
     * @return the min heap node ID.
     */
    public int getMinID()
    {
        if (isEmpty())
            throw new RuntimeException("Empty Heap");
        else
            return heap_nid[0];
    }


    /**
     * @return the min heap node value.
     */
    public int getMinValue()
    {
        if (isEmpty())
            throw new RuntimeException("Empty Heap");
        else
            return heap_val[0];
    }


    /**
     * This will remove the node.
     * 
     * @return the min heap node.
     */
    public int[] extractMin()
    {
        if (isEmpty())
            throw new RuntimeException("Empty Heap");
        int[] min = new int[] { heap_nid[0], heap_val[0] };
        removeMin();
        return min;
    }


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
        while ((c = (i << 1) + 1) < size) { // left child
            rc = (i << 1) + 2; // right child
            if (rc < size && heap_val[c] > heap_val[rc]) {
                c = rc;
            }

            if (heap_val[i] > (heap_val[c])) {
                int tmp1 = heap_nid[i];
                int tmp2 = heap_val[i];
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


    public int getNodeDist(int n)
    {
        if (n > size || n < 0) {
            throw new RuntimeException("hurr");
        }
        return heap_val[n];
    }


    public int getSize()
    {
        return size;
    }

}
