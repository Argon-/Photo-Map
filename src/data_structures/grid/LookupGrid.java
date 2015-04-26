package data_structures.grid;

import java.awt.Color;
import java.io.Serializable;

import gui.MainWindow;
import gui.overlay.OverlayAggregate;
import gui.overlay.OverlayElement;
import util.Distance;



/**
 * See {@link #LookupGrid(double[], double[], double, double, double, double) LookupGrid}.
 */
public class LookupGrid implements Serializable
{
    private static final long serialVersionUID = 3951171285414881903L;
    
    /**
     * factor * num_of_nodes is the amount of cells in y/x direction
     */
    private final double GRID_FACTOR = 0.000015;
    private final int    GRID_LAT_CELLS;
    private final int    GRID_LON_CELLS;
    private final double LAT_CELL_SIZE;
    private final double LON_CELL_SIZE;
    private final int    ADDITIONAL_RINGS = 2;

    private final int[] grid;
    private final int[] grid_offset;
    
    private final double lat_ref[];
    private final double lon_ref[];

    private final double minLat;
    private final double maxLat;
    private final double minLon;
    private final double maxLon;
    
    private boolean visualize = false;
    private MainWindow win = null;
    private int vis_i = 0;
    
    
    /**
     * See {@link #LookupGrid(double[], double[], double, double, double, double) LookupGrid}.
     * <br>
     * Please note this: is merely a convenience constructor and will loop both passed arrays
     * two times consecutively.
     * 
     * @param lat array
     * @param lon array
     * @throws InvalidCoordinateArraysException
     */
    public LookupGrid(double[] lat, double[] lon) throws InvalidCoordinateArraysException
    {
        this(lat, lon, arrayMin(lat), arrayMax(lat), arrayMin(lon), arrayMax(lon));
    }
        
    
    /**
     * Fast lookup for "get nearest node to point (x, y)" queries.<br>
     * Creates a grid, dividing the passed locations into cells.
     * Lookups are performed by iterating these cells in circles. See 
     * {@link #getNearestNode(double, double) getNearestNode}
     * for more information.
     * <br><br>
     * This class is effectively final (except for the visualization part, but this is
     * only considered some sort of debug output anyway and disabled by default).
     *
     * @param lat
     * @param lon
     * @param minLat
     * @param maxLat
     * @param minLon
     * @param maxLon
     * @throws InvalidCoordinateArraysException when {@code lat.length != lon.lenght} or {@code lat.length < 1}
     */
    public LookupGrid(double[] lat, double[] lon, double minLat, double maxLat, double minLon, double maxLon) throws InvalidCoordinateArraysException
    {
        if (lat.length != lon.length) {
            throw new InvalidCoordinateArraysException("array length differs");
        }
        if (lat.length < 1) {
            throw new InvalidCoordinateArraysException("too few elements");
        }

        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        lat_ref = lat;
        lon_ref = lon;
        
        // determine grid sizes (amount of cells)
        final int m = (int) Math.ceil(lat_ref.length * GRID_FACTOR);
        // try to get the width and height reasonably square
        GRID_LAT_CELLS =  m < 1 ? 1 : (int) Math.ceil(m * (maxLon - minLon) / (maxLat - minLat));
        GRID_LON_CELLS =  m < 1 ? 1 : m;
        
        LAT_CELL_SIZE = (maxLat - minLat) / GRID_LAT_CELLS;
        LON_CELL_SIZE = (maxLon - minLon) / GRID_LON_CELLS;

        //System.out.println("GRID_LAT_CELLS = " + GRID_LAT_CELLS);
        //System.out.println("GRID_LON_CELLS = " + GRID_LON_CELLS);
        
        // create grid arrays and fill them
        grid = new int[lon_ref.length];
        grid_offset = new int[GRID_LAT_CELLS * GRID_LON_CELLS + 1];
        buildGrid();
    }
    
    
    /**
     * Color the nodes touched when searching for a node.<br>
     * Not necessarily thread safe.
     *
     * @param t
     * @param w
     */
    public void setVisualize(boolean t, MainWindow w)
    {
        visualize = t;
        win = w;
    }

    
    private void buildGrid()
    {
        int[] count = new int[GRID_LAT_CELLS * GRID_LON_CELLS];
        
        // first pass
        for (int i = 0; i < lon_ref.length; ++i)
        {
            int lat_cell = lat_ref[i] == maxLat ? GRID_LAT_CELLS - 1 : (int) ((lat_ref[i] - minLat) / LAT_CELL_SIZE);
            int lon_cell = lon_ref[i] == maxLon ? GRID_LON_CELLS - 1 : (int) ((lon_ref[i] - minLon) / LON_CELL_SIZE);
            count[lat_cell * GRID_LON_CELLS + lon_cell] += 1;
        }
        
        // build offset array
        for (int i = 0; i < count.length; ++i)
        {
            grid_offset[i+1] = grid_offset[i] + count[i];
        }
        
        // second pass
        for (int i = 0; i < lon_ref.length; ++i)
        {
            int lat_cell = lat_ref[i] == maxLat ? GRID_LAT_CELLS - 1 : (int) ((lat_ref[i] - minLat) / LAT_CELL_SIZE);
            int lon_cell = lon_ref[i] == maxLon ? GRID_LON_CELLS - 1 : (int) ((lon_ref[i] - minLon) / LON_CELL_SIZE);
            int pos = grid_offset[lat_cell * GRID_LON_CELLS + lon_cell] + (count[lat_cell * GRID_LON_CELLS + lon_cell]-- - 1);
            grid[pos] = i;
        }
    }
    
    
    private int searchMinInCell(double lat, double lon, int lat_cell, int lon_cell, int last_min_id)
    {
        if (lat_cell < 0 || lon_cell < 0 || lat_cell >= GRID_LAT_CELLS || lon_cell >= GRID_LON_CELLS) {
            return last_min_id;
        }
        

        int min_id = last_min_id < 0 ? -1 : last_min_id;
        double min_dist = last_min_id > -1 ? Distance.haversine(lat, lon, lat_ref[last_min_id], lon_ref[last_min_id]) : Double.MAX_VALUE;
        
        final int index = lat_cell * GRID_LON_CELLS + lon_cell;
        for (int i = 0; i < grid_offset[index + 1] - grid_offset[index]; ++i)
        {
            final int pos = grid[grid_offset[index] + i];
            final double dist = Distance.haversine(lat, lon, lat_ref[pos], lon_ref[pos]);
            
            if (dist < min_dist) {
                min_dist = dist;
                min_id = pos;
            }
        }

        
        // iterate the cell again
        if (visualize) {
            //System.out.println("Searching cell (" + lat_cell + "," + lon_cell + ")");
            final OverlayAggregate oa = new OverlayAggregate();
            final Color[] c = {Color.MAGENTA, new Color(255, 0, 144)};
            
            final int e = lat_cell * GRID_LON_CELLS + lon_cell;
            for (int i = 0; i < grid_offset[e + 1] - grid_offset[e]; ++i)
            {
                final int pos = grid[grid_offset[e] + i];
                oa.addPoint(new OverlayElement(lat_ref[pos], lon_ref[pos], c[vis_i % c.length], 2));
            }
            win.addOverlay(oa);
        }

        return min_id;
    }
    
    
    /**
     * Search for the (or rather "a"?) node closest to {@code (lat, lon)} in
     * a grid with an expanding ring, originating from the cell containing 
     * {@code (lat, lon)}.<br>
     * The ring expands by one per iteration, starting with 0 (= the cell
     * containing {@code (lat, lon)}).
     * <pre>
     * {@code
     * —————————————————————————————
     * | 3 | 3 | 3 | 3 | 3 | 3 | 3 |
     * | 3 | 2 | 2 | 2 | 2 | 2 | 3 |
     * | 3 | 2 | 1 | 1 | 1 | 2 | 3 |
     * | 3 | 2 | 1 | 0 | 1 | 2 | 3 | 
     * | 3 | 2 | 1 | 1 | 1 | 2 | 3 |
     * | 3 | 2 | 2 | 2 | 2 | 2 | 3 |
     * | 3 | 3 | 3 | 3 | 3 | 3 | 3 |
     * —————————————————————————————
     * }
     * </pre>
     * After a node was found we expand the ring {@code additional_rings} more times in order
     * to compensate for a not-exactly-square cell-size and certain edge cases.
     * <br><br>
     * 
     * @return ID of the node closest to {@code (lat, lon)}
     */
    public int getNearestNode(double lat, double lon)
    {
        if (lat < minLat || lat > maxLat || lon < minLon || lon > maxLon) {
            return -1;
        }
        
        final int lat_center = lat == maxLat ? GRID_LAT_CELLS - 1 : (int) ((lat - minLat) / LAT_CELL_SIZE);
        final int lon_center = lon == maxLon ? GRID_LON_CELLS - 1 : (int) ((lon - minLon) / LON_CELL_SIZE);
        
        /*
         * search in expanding rings, originating from (lat_center, lon_center)
         * the ring expands by one per iteration, starting with 0 (= the cell containing the clicked position)
         * after a node was found (mid_id != -1) we expand the ring additional_rings more times
         */
        
        int min_id = -1;
        int ring = 0;
        int additional_rings = ADDITIONAL_RINGS;
        
        do
        {
            // (mid_id != -1) == found a node
            if (min_id > -1 || (ring > GRID_LAT_CELLS && ring > GRID_LON_CELLS)) {
                --additional_rings;
            }
            
            // seek upwards to the current ring's starting position
            int lat_curr = lat_center + ring;
            int lon_curr = lon_center;
            
            min_id = searchMinInCell(lat, lon, lat_curr, lon_curr, min_id);
            
            // iterate right (half the side length)
            for (int i = 1; i < ring; ++i) {
                min_id = searchMinInCell(lat, lon, lat_curr, lon_curr + i, min_id);
            }
            lon_curr = lon_curr + ring;
            
            // iterate downwards
            for (int i = 0; i < ring * 2; ++i) {
                min_id = searchMinInCell(lat, lon, lat_curr - i, lon_curr, min_id);
            }
            lat_curr = lat_curr - (ring * 2);
            
            // iterate left
            for (int i = 0; i < ring * 2; ++i) {
                min_id = searchMinInCell(lat, lon, lat_curr, lon_curr - i, min_id);
            }
            lon_curr = lon_curr - (ring * 2);
            
            // iterate upwards
            for (int i = 0; i < ring * 2; ++i) {
                min_id = searchMinInCell(lat, lon, lat_curr + i, lon_curr, min_id);
            }
            lat_curr = lat_curr + (ring * 2);
            
            // iterate right (half the side length)
            for (int i = 0; i < ring; ++i) {
                min_id = searchMinInCell(lat, lon, lat_curr, lon_curr + i, min_id);
            }
            lon_curr = lon_curr + ring;
            
            ++ring; ++vis_i;
        } while (additional_rings > 0);
        //System.out.println("Searched " + (ring-1) + " rings");
        
        return min_id;
    }
    
    
    /**
     * Compute the minimum over the passed array.
     * 
     * @param arr
     */
    public static double arrayMin(double[] arr)
    {
        double r = Double.MAX_VALUE;
        for (double d : arr) {
            r = Math.min(r, d);
        }
        return r;
    }
    
    
    /**
     * Compute the maximum over the passed array.
     * 
     * @param arr
     */
    public static double arrayMax(double[] arr)
    {
        double r = -Double.MAX_VALUE;
        for (double d : arr) {
            r = Math.max(r, d);
        }
        return r;
    }

}
