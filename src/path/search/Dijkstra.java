package path.search;

import java.util.Arrays;
import java.util.LinkedList;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import data_structures.graph.Graph;
import data_structures.heap.BinaryMinHeap;



public final class Dijkstra implements Runnable
{
    private final int     HEAP_SIZE_FACTOR = 1000;
    private final int     UNSETTLED        = Integer.MAX_VALUE;
    private final int     SETTLED          = Integer.MAX_VALUE - 1;
    private final boolean weighted;

    private final Graph   g;
    private final int[]   state;
    private final int[]   pred;

    private BinaryMinHeap heap             = null;

    private int           source           = -1;
    private int           target           = -1;
    private int           accDist          = 0;


    public Dijkstra(Graph graph, boolean weighted)
    {
        g = graph;
        this.weighted = weighted;
        state = new int[g.size()];
        pred = new int[g.size()];
        Arrays.fill(pred, -1);
    }


    public Dijkstra(Graph graph)
    {
        this(graph, true);
    }


    public Dijkstra(Graph graph, boolean weighted, int from, int to)
    {
        this(graph, weighted);
        setSource(from);
        setTarget(to);
    }


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


    public boolean pathFromTo(int from, int to)
    {
        setSource(from);
        setTarget(to);
        return pathFromTo();
    }


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

        System.out.println("Found no route from " + source + " to " + target);
        return false;
    }


    public Dijkstra setSource(int from)
    {
        if (source != from) {
            source = from;
            buildNewHeap(from);
        }
        return this;
    }


    public Dijkstra setTarget(int to)
    {
        target = to;
        return this;
    }


    public void printRouteStats()
    {
        printRouteStats(source, target);
    }


    public void printRouteStats(int from, int to)
    {
        LinkedList<Integer> l = getRouteNodeIDs();
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
        // System.out.println("   > Route: " + s);
        // System.out.println("----------------------------------------------------------------------");
    }


    public LinkedList<GeoPosition> getRoute()
    {
        if (target == -1)
            throw new RuntimeException("Can't return route without target");

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


    public LinkedList<Integer> getRouteNodeIDs()
    {
        if (target == -1)
            throw new RuntimeException("Can't return route without target");

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
