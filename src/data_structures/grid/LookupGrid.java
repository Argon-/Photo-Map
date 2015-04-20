package data_structures.grid;

import java.awt.Color;

import gui.MainWindow;
import gui.overlay.OverlayAggregate;
import gui.overlay.OverlayElement;
import util.Distance;



public class LookupGrid
{
    // factor * num_of_nodes is the amount of cells in y/x direction
    private final double GRID_FACTOR = 0.00002;
    private int GRID_LAT_CELLS;
    private int GRID_LON_CELLS;

    private double LAT_CELL_SIZE;
    private double LON_CELL_SIZE;

    private int[] grid        = null;
    private int[] grid_offset = null;
    
    private double lat_ref[] = null;
    private double lon_ref[] = null;

    private double minLat =  Double.MAX_VALUE;
    private double maxLat = -Double.MAX_VALUE;
    private double minLon =  Double.MAX_VALUE;
    private double maxLon = -Double.MAX_VALUE;
    
    boolean visualize = false;
    MainWindow win = null;
    int vis_i = 0;
    
    
    public LookupGrid(double[] lat, double[] lon) throws InvalidCoordinateArraysException
    {        
        if (lat.length != lon.length) {
            throw new InvalidCoordinateArraysException("array length differs");
        }
        if (lat.length < 2) {
            throw new InvalidCoordinateArraysException("too few elements");
        }
        
        this.lat_ref = lat;
        this.lon_ref = lon;
        determineMinMax();
        determineCellSize();
    }
    
    
    public LookupGrid(double[] lat, double[] lon, double minLat, double maxLat, double minLon, double maxLon) throws InvalidCoordinateArraysException
    {
        if (lat.length != lon.length) {
            throw new InvalidCoordinateArraysException("array length differs");
        }
        if (lat.length < 2) {
            throw new InvalidCoordinateArraysException("too few elements");
        }

        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        
        this.lat_ref = lat;
        this.lon_ref = lon;
        determineCellSize();
    }
    
    
    public void setVisualize(boolean t, MainWindow w)
    {
        visualize = t;
        win = w;
    }
    
    
    private void determineMinMax()
    {
        for (double d : lat_ref) {
            this.minLat = Math.min(minLat, d);
            this.maxLat = Math.max(maxLat, d);
        }
        for (double d : lon_ref) {
            this.minLon = Math.min(minLon, d);
            this.maxLon = Math.max(maxLon, d);
        }
    }
    
    
    private void determineCellSize()
    {
        final int m = (int) Math.ceil(this.lat_ref.length * GRID_FACTOR);
        GRID_LAT_CELLS =  m < 1 ? 1 : (int) Math.ceil(m * (this.maxLon - this.minLon) / (this.maxLat - this.minLat));
        GRID_LON_CELLS =  m < 1 ? 1 : m;
        
        LAT_CELL_SIZE = (this.maxLat - this.minLat) / GRID_LAT_CELLS;
        LON_CELL_SIZE = (this.maxLon - this.minLon) / GRID_LON_CELLS;
        
        System.out.println("GRID_LAT_CELLS = " + GRID_LAT_CELLS);
        System.out.println("GRID_LON_CELLS = " + GRID_LON_CELLS);
    }

    
    public void buildGrid()
    {
        this.grid = new int[this.lon_ref.length];
        this.grid_offset = new int[GRID_LAT_CELLS * GRID_LON_CELLS + 1];
        int[] count = new int[GRID_LAT_CELLS * GRID_LON_CELLS];
        
        // first pass
        for (int i = 0; i < this.lon_ref.length; ++i)
        {
            int lat_cell = this.lat_ref[i] == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((this.lat_ref[i] - this.minLat) / LAT_CELL_SIZE);
            int lon_cell = this.lon_ref[i] == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((this.lon_ref[i] - this.minLon) / LON_CELL_SIZE);
            count[lat_cell * GRID_LON_CELLS + lon_cell] += 1;
        }
        
        // build offset array
        for (int i = 0; i < count.length; ++i)
        {
            grid_offset[i+1] = grid_offset[i] + count[i];
        }
        
        // second pass
        for (int i = 0; i < this.lon_ref.length; ++i)
        {
            int lat_cell = this.lat_ref[i] == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((this.lat_ref[i] - this.minLat) / LAT_CELL_SIZE);
            int lon_cell = this.lon_ref[i] == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((this.lon_ref[i] - this.minLon) / LON_CELL_SIZE);
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
        double min_dist = last_min_id > -1 ? Distance.haversine(lat, lon, this.lat_ref[last_min_id], this.lon_ref[last_min_id]) : Double.MAX_VALUE;
        
        int index = lat_cell * GRID_LON_CELLS + lon_cell;
        for (int i = 0; i < grid_offset[index + 1] - grid_offset[index]; ++i)
        {
            final int pos = this.grid[grid_offset[index] + i];
            final double dist = Distance.haversine(lat, lon, this.lat_ref[pos], this.lon_ref[pos]);
            
            if (dist < min_dist) {
                min_dist = dist;
                min_id = pos;
            }
        }

        
        if (visualize) {
            //System.out.println("Searching cell (" + lat_cell + "," + lon_cell + ")");
            OverlayAggregate oa = new OverlayAggregate();
            Color[] c = {Color.MAGENTA, new Color(255, 0, 144)};
            
            int index1 = lat_cell * GRID_LON_CELLS + lon_cell;
            for (int i = 0; i < grid_offset[index1 + 1] - grid_offset[index1]; ++i)
            {
                final int pos = this.grid[grid_offset[index1] + i];
                oa.addPoint(new OverlayElement(this.lat_ref[pos], this.lon_ref[pos], c[vis_i % c.length], 3));
            }
            win.addOverlayAggregate(oa);
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
     * After a node was found we expand the ring {@code additional_rings} more times.
     * <br>
     * <br>
     * @return ID of the node closest to {@code (lat, lon)}
     * 
     */
    public int getNearestNode(double lat, double lon)
    {
        if (lat < this.minLat || lat > this.maxLat || lon < this.minLon || lon > this.maxLon) {
            return -1;
        }
        
        final int lat_center = lat == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((lat - this.minLat) / LAT_CELL_SIZE);
        final int lon_center = lon == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((lon - this.minLon) / LON_CELL_SIZE);
        
        /*
         * search in expanding rings, originating from (lat_center, lon_center)
         * the ring expands by one per iteration, starting with 0 (= the cell containing the clicked position)
         * after a node was found (mid_id != -1) we expand the ring additional_rings more time
         * —————————————————————————————
         * | 3 | 3 | 3 | 3 | 3 | 3 | 3 |
         * | 3 | 2 | 2 | 2 | 2 | 2 | 3 |
         * | 3 | 2 | 1 | 1 | 1 | 2 | 3 |
         * | 3 | 2 | 1 | 0 | 1 | 2 | 3 | 
         * | 3 | 2 | 1 | 1 | 1 | 2 | 3 |
         * | 3 | 2 | 2 | 2 | 2 | 2 | 3 |
         * | 3 | 3 | 3 | 3 | 3 | 3 | 3 |
         * —————————————————————————————
         */
        
        int min_id = -1;
        int ring = 0;
        int additional_rings = 2;
        
        do
        {
            // (mid_id != -1) == found a node
            if (min_id > -1 || (ring > GRID_LAT_CELLS && ring > GRID_LON_CELLS)) {
                --additional_rings;
            }
            
            // seek upwards to this ring's starting position
            int lat_curr = lat_center + ring;
            int lon_curr = lon_center;
            
            min_id = searchMinInCell(lat, lon, lat_curr, lon_curr, min_id);
            
            // iterate right
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
            
            // iterate right
            for (int i = 0; i < ring; ++i) {
                min_id = searchMinInCell(lat, lon, lat_curr, lon_curr + i, min_id);
            }
            lon_curr = lon_curr + ring;
            
            ++ring; ++vis_i;
        } while (additional_rings > 0);
        //System.out.println("Searched " + (ring-1) + " rings");
        
        return min_id;
    }

}
