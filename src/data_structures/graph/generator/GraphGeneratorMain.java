package data_structures.graph.generator;

public class GraphGeneratorMain
{

    public static void main(String[] args)
    {
        String node_file = "/Users/Julian/Documents/Uni/_Fapra OSM/3/file-generation/files-de/nodes.txt";
        String way_file  = "/Users/Julian/Documents/Uni/_Fapra OSM/3/file-generation/files-de/ways.txt";
        String out_file  = "/Users/Julian/Documents/Uni/_Fapra OSM/3/file-generation/out.txt";

        if (args.length > 2) {
            node_file = args[0];
            way_file  = args[1];
            out_file  = args[2];
        }
        
        GraphGenerator gg = new GraphGenerator(node_file, way_file);
        gg.generate();
        gg.writeOut(out_file);
    }

}
