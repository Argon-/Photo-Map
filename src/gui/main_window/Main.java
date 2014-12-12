package gui.main_window;

import java.awt.EventQueue;
import java.io.PrintStream;

import data_structures.graph.GraphFactory;

public class Main
{

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				try {
					//final MainWindow frame = new MainWindow(GraphFactory.loadArrayRepresentation("/Users/Julian/Documents/Dropbox/_Semester 9/Fapra OSM/1/15000.txt"));
					final MainWindow frame = new MainWindow(GraphFactory.loadArrayRepresentation("./15000K.bin"));
					
					final PrintStream original = System.out;

			        System.setOut(new PrintStream(original) {
			            public void println(String s) {
			                process(s + "\n");
			            }

			            public void print(String s) {
			                process(s);
			            }

			            private void process(String s) {
			                // Fill some JEditorPane
			                original.print(s);
			                frame.log(s);
			            }
			        });

					
					frame.setVisible(true);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

}
