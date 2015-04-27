package path.search;

import java.util.Arrays;
import java.util.LinkedList;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import data_structures.graph.Graph;
import data_structures.heap.BinaryMinHeap;



/**
 * See {@link #Dijkstra(Graph)}.<br>
 * See {@link #Dijkstra(Graph, boolean)}.<br>
 * See {@link #Dijkstra(Graph, int, int)}.<br>
 * See {@link #Dijkstra(Graph, boolean, int, int)}.
 */
public final class Dijkstra implements Runnable
{
    /**
     * Factor used when allocating the heap to save some memory.
     */
    private final int     HEAP_SIZE_FACTOR = 500;
    /**
     * Magic values used internally in the {@code state} field.
     * We are in big trouble when these values are used as distances.
     */
    private final int     UNSETTLED = Integer.MAX_VALUE, SETTLED = Integer.MAX_VALUE - 1;
    private final boolean weighted;

    /**
     * The graph we are working on.
     */
    private final Graph   g;
    /**
     * This field contains the current minimum distance for every node,
     * {@code SETTLED} when the node is optimal or {@code UNSETTLED}
     * when completely untouched.
     */
    private final int[]   state;
    /**
     * Contains the predecessor node for every node.
     */
    private final int[]   pred;

    private BinaryMinHeap heap             = null;

    private int           source           = -1;
    private int           target           = -1;
    private int           accDist          = 0;


    /**
     * See {@link #Dijkstra(Graph, boolean, int, int)}.
     */
    public Dijkstra(Graph graph, boolean weighted)
    {
        g = graph;
        this.weighted = weighted;
        state = new int[g.size()];
        pred = new int[g.size()];
        Arrays.fill(pred, -1);
    }


    /**
     * Using {@code weighted == true}.<br>
     * See {@link #Dijkstra(Graph, boolean, int, int)}.
     */
    public Dijkstra(Graph graph)
    {
        this(graph, true);
    }

    
    /**
     * Search for a shortest path from {@code from} to {@code to}
     * within the given {@code graph}.<br>
     * This class is maintaining a {@link data_structures.heap.BinaryMinHeap BinaryMinHeap}
     * which will remain allocated even after a path was successfully found.
     * As long as the source {@code from} remains the same, the same heap is reused,
     * significantly speeding up subsequent queries.
     * <br><br>
     * This class implements {@code Runnable} and provides necessary means to
     * collect the calculated path after it was executed in a separate thread.
     * <br><br>
     * <b>Note</b>: for every parameter designating nodes 
     * {@link data_structures.graph.Graph Graph}-internal node IDs are used.
     * 
     * @param graph
     * @param weighted use distances weighted by street types?
     * @param from
     * @param to
     */
    public Dijkstra(Graph graph, boolean weighted, int from, int to)
    {
        this(graph, weighted);
        setSource(from);
        setTarget(to);
    }


    /**
     * Using {@code weighted == true}.<br>
     * See {@link #Dijkstra(Graph, boolean, int, int)}.
     */
    public Dijkstra(Graph graph, int from, int to)
    {
        this(graph, true, from, to);
    }


    private void buildNewHeap(int source)
    {
        Arrays.fill(state, UNSETTLED);
        heap = new BinaryMinHeap(g.size() / HEAP_SIZE_FACTOR);
        heap.insert(source, 0);
    }


    /**
     * Search for a path from {@code from} to {@code to}.<br>
     * Will re-use previously calculated information in case {@code from}
     * is the current source.
     * 
     * @param from
     * @param to
     * @return {@code false} when no path from {@code from} to {@code to} was found, otherwise {@code true}
     */
    public boolean pathFromTo(int from, int to)
    {
        setSource(from);
        setTarget(to);
        return pathFromTo();
    }


    /**
     * Is using the currently designated source and destination values.<br>
     * See {@link #pathFromTo(int, int)}.
     */
    public boolean pathFromTo()
    {
        if (state[target] == SETTLED) {
            return true;
        }

        loop : while (!heap.isEmpty()) {
            int u_id = heap.getMinID();
            int u_dist = heap.getMinValue();
            heap.removeMin();

            state[u_id] = SETTLED;
            int i = 0;
            int neighbor = -1;

            if (target == u_id) {
                break loop;
            }

            while ((neighbor = g.getIthNeighbor(u_id, i++)) != -1) {

                if (state[neighbor] == SETTLED) {
                    continue;
                }

                final int inc = g.getIthEdgeDistFor(u_id, i - 1, weighted);
                if (inc < 0)
                    continue;
                final int new_dist = u_dist + inc;

                if (state[neighbor] > new_dist) {
                    heap.insert(neighbor, new_dist);
                    state[neighbor] = new_dist;
                    pred[neighbor] = u_id;
                }

                if (target == neighbor) {
                    state[neighbor] = SETTLED;
                    pred[neighbor] = u_id;
                    break loop;
                }
            }
        }

        if (state[target] == SETTLED) {
            return true;
        }

        return false;
    }


    /**
     * New source, the start of a path.
     */
    public Dijkstra setSource(int from)
    {
        if (source != from) {
            source = from;
            buildNewHeap(from);
        }
        return this;
    }


    /**
     * New target, the destination of a path.
     */
    public Dijkstra setTarget(int to)
    {
        target = to;
        return this;
    }


    /**
     * Using the current source and target.<br>
     * See {@link #printPathStats(int, int)}.
     */
    public void printPathStats()
    {
        printPathStats(source, target);
    }


    /**
     * Print some debugging stats for a path from {@code from} to {@code to}.
     */
    public void printPathStats(int from, int to)
    {
        LinkedList<Integer> l = getPathNodeIDs();
        int last = l.pop();
        int curr;
        int dist = 0;
        int hops = l.size();
        int line_count = 0;

        String s = "" + last;
        while (!l.isEmpty()) {
            curr = l.pop();
            if (g.getDist(last, curr) == -1) {
                System.out.println("NO EDGE FOUND FROM " + curr + " TO " + last);
            }
            else {
                if (s.length() / 80 > line_count) {
                    ++line_count;
                    s += System.getProperty("line.separator") + "           ";
                }
                dist += g.getDist(last, curr);
                s += " -> " + curr;
            }
            last = curr;
        }

        // System.out.println("----------------------------------------------------------------------");
        System.out.println("Shortest path from " + source + " to " + target);
        System.out.println("   >  Hops: " + hops);
        System.out.println("   >  Dist: " + dist);
        // System.out.println("   > Path: " + s);
        // System.out.println("----------------------------------------------------------------------");
    }


    /**
     * @return list of locations ({@link GeoPosition}) on the shortest path from current source to destination
     * @throws RuntimeException when the current target is {@code -1}
     */
    public LinkedList<GeoPosition> getPath()
    {
        if (target == -1)
            throw new RuntimeException("Can't return path without target");

        LinkedList<GeoPosition> l = new LinkedList<GeoPosition>();
        l.addFirst(g.getPosition(target));

        accDist = 0;
        int last = target;
        int i = pred[last];
        while (i != -1) {
            accDist += g.getDist(i, last);
            l.addFirst(g.getPosition(i));
            if (i == source) {
                break;
            }
            last = i;
            i = pred[last];
        }
        return l;
    }


    /**
     * @return accumulated distance of all edges in the shortest path
     */
    public int calculateDist()
    {
        accDist = 0;
        int last = target;
        int i = pred[last];
        while (i != -1) {
            accDist += g.getDist(i, last);
            if (i == source) {
                break;
            }
            last = i;
            i = pred[last];
        }        
        return accDist;
    }


    public int getDist()
    {
        return accDist;
    }


    /**
     * @return list of node IDs on the shortest path from current source to destination
     * @throws RuntimeException when the current target is {@code -1}
     */
    public LinkedList<Integer> getPathNodeIDs()
    {
        if (target == -1)
            throw new RuntimeException("Can't return path without target");

        LinkedList<Integer> l = new LinkedList<Integer>();
        l.addFirst(target);

        accDist = 0;
        int last = target;
        int i = pred[last];
        while (i != -1) {
            accDist += g.getDist(i, last);
            l.addFirst(i);
            if (i == source) {
                break;
            }
            last = i;
            i = pred[last];
        }
        return l;
    }


    @Override
    public void run()
    {
        pathFromTo();
    }

}
