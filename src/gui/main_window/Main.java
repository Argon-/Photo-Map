package gui.main_window;

import java.awt.EventQueue;
import java.io.PrintStream;


public class Main
{

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				try {
					final MainWindow frame = new MainWindow();
					final PrintStream original = System.out;

			        System.setOut(new PrintStream(original) {
			        	public void println()         { process(System.getProperty("line.separator")); }
			            public void println(String s) { process(s + System.getProperty("line.separator")); }
			            public void print(String s)   { process(s); }

			            private void process(String s) {
			                original.print(s);
			                if (frame != null)
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
