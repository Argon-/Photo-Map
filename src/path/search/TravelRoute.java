package path.search;

import gui.overlay.OverlayImage;
import gui.overlay.OverlayImageComparator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import data_structures.graph.Graph;



public class TravelRoute
{
    /**
     * The visit order to use when searching for a route through
     * the given positions.<br>
     * {@code VISIT_ORDER_BY_USER} is default.
     */
    public static final int VISIT_ORDER_BY_USER       = 0, 
                            VISIT_ORDER_CHRONOLOGICAL = 1, 
                            VISIT_ORDER_SHORTEST      = 2;

    private final Graph graph;
    private final GeoPosition start;

    private int accDist = 0;
    private int visitOrder = VISIT_ORDER_BY_USER;
    private LinkedList<TravelRouteNode> nodes;
    private LinkedList<LinkedList<GeoPosition> > route = new LinkedList<LinkedList<GeoPosition> >();

    
    
    public TravelRoute(Graph graph, ArrayList<OverlayImage> elements, int order, GeoPosition startPos) throws NoSuchRouteOrderException
    {
        this.graph = graph;
        start = startPos;
        
        if (elements.size() < 2) {
            throw new RuntimeException("Too few elements!");
        }
        
        switch(order) {
            case VISIT_ORDER_BY_USER:
                break;
            case VISIT_ORDER_CHRONOLOGICAL:
                elements.sort(new OverlayImageComparator());
                break;
            case VISIT_ORDER_SHORTEST:
                break;
            default:
                throw new NoSuchRouteOrderException("Requested visit order is unkown!");
        }
        visitOrder = order;
        nodes = createPosList(elements, true);
    }
    
    
    public TravelRoute(Graph graph, ArrayList<OverlayImage> elements, int order) throws NoSuchRouteOrderException
    {
        this(graph, elements, order, null);
    }
    
    
    public int getOrder()
    {
        return visitOrder;
    }
            
    
    public TravelRoute calculate()
    {
        switch(visitOrder) {
            case VISIT_ORDER_BY_USER:
                // the list is already in user-defined order
                simpleRoute(nodes);
                break;
            case VISIT_ORDER_CHRONOLOGICAL:
                simpleRoute(nodes);
                break;
            case VISIT_ORDER_SHORTEST:
                shortestRouteGreedy(nodes);
                break;
        }
        return this;
    }
    
    
    private void simpleRoute(LinkedList<TravelRouteNode> nodes)
    {
        route.clear();
        Dijkstra d = new Dijkstra(graph);
        Iterator<TravelRouteNode> it = nodes.iterator();
        GeoPosition src, dst;
        int src_nid = 0, dst_nid = 0;
        
        src = it.next().getPos();
        src_nid = graph.getNearestNode(src);
        
        while (it.hasNext())
        {
            dst = it.next().getPos();
            dst_nid = graph.getNearestNode(dst);
            if (d.pathFromTo(src_nid, dst_nid)) {
                route.add(d.getRoute());
            }
            src = dst;
            src_nid = dst_nid;
        }
    }
    
    
    private void shortestRouteGreedy(LinkedList<TravelRouteNode> nodes)
    {
        route.clear();
        Dijkstra d = new Dijkstra(graph);
        
        LinkedList<TravelRouteNode> candidates = new LinkedList<TravelRouteNode>(nodes);
        nodes.clear();
        nodes.add(candidates.removeFirst());
        // we go home "manually" at the end
        candidates.removeLast();
                
        
        while (!candidates.isEmpty())
        {
            final TravelRouteNode src_trn = nodes.getLast();
            final GeoPosition src = src_trn.getPos();
            final int src_nid = graph.getNearestNode(src);
            
            //System.out.println("   src: " + (src_trn.getData() == null ? "(null)" : src_trn.getData().getLabel()));
            
            TravelRouteNode shortest_trn = null;
            LinkedList<GeoPosition> shortest = new LinkedList<GeoPosition>();
            int shortest_dist = Integer.MAX_VALUE;
            
            Iterator<TravelRouteNode> it = candidates.iterator();
            while (it.hasNext())
            {
                final TravelRouteNode cand_trn = it.next();
                final GeoPosition cand = cand_trn.getPos();
                final int cand_nid = graph.getNearestNode(cand);
                
                if (d.pathFromTo(src_nid, cand_nid)) {
                    LinkedList<GeoPosition> r = d.getRoute();
                    if (d.getDist() < shortest_dist) {
                        shortest_trn = cand_trn;
                        shortest_dist = d.getDist();
                        shortest = r;
                    }
                    //System.out.println("      cand: " + (cand_trn.getData() == null? "(null)" : cand_trn.getData().getLabel()) + " (" + d.getDist() + ")");
                }
                //else {
                //    System.out.println("      cand: no route to " + (cand_trn.getData() == null ? "(null)" : cand_trn.getData().getLabel()));
                //}

            }
            
            if (shortest_trn == null) {
                //System.out.println("   FOUND NO ROUTE");
                continue;
            }
            //System.out.println("   src: shortest candidate: " + (shortest_trn.getData() == null? "(null)" :shortest_trn.getData().getLabel()) + " (" + shortest_dist + ")");
            nodes.add(shortest_trn);
            candidates.remove(shortest_trn);
            route.add(shortest);
        }
        
        
        // go back home
        final TravelRouteNode src_trn = nodes.getLast();
        final GeoPosition src = src_trn.getPos();
        final int src_nid = graph.getNearestNode(src);

        final TravelRouteNode dst_trn = nodes.getFirst();
        final GeoPosition dst = dst_trn.getPos();
        final int dst_nid = graph.getNearestNode(dst);
        
        if (d.pathFromTo(src_nid, dst_nid)) {
            nodes.add(dst_trn);
            route.add(d.getRoute());
        }
        //else {
        //    System.out.println("FOUND NO ROUTE BACK HOME");
        //}
    }
    
    
    private LinkedList<TravelRouteNode> createPosList(ArrayList<OverlayImage> e, boolean visitAccommodationAfter)
    {
        LinkedList<TravelRouteNode> p = new LinkedList<TravelRouteNode>();
        if (start != null)
            p.add(new TravelRouteNode(start, null));
        
        for (OverlayImage oi : e) {
            if (visitAccommodationAfter)
                p.add(new TravelRouteNode(oi.getPos(), oi));
            if (oi.getAccommodation() != null)
                p.add(new TravelRouteNode(oi.getAccommodation().getPos(), oi.getAccommodation()));
            if (!visitAccommodationAfter)
                p.add(new TravelRouteNode(oi.getPos(), oi));
        }
        // back to where we came from
        p.add(p.getFirst());
        return p;
    }
    
        
    public LinkedList<TravelRouteNode> getNodes()
    {
        return nodes;
    }
    
    
    public LinkedList<LinkedList<GeoPosition> > getRoute()
    {
        return route;
    }
    
    
    public int getDist()
    {
        return accDist;
    }
    
}
