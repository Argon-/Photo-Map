package data_structures.graph.generator;

public class GraphGeneratorMain
{

    public static void main(String[] args)
    {
        if (args.length < 3) {
            System.out.println("Parameters: <nodes file> <ways file> <out file>");
            return;
        }
        
        String node_file = args[0];
        String way_file  = args[1];
        String out_file  = args[2];

        
        GraphGenerator gg = new GraphGenerator(node_file, way_file);
        gg.generate();
        gg.writeOut(out_file);
    }

}
