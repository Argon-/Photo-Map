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
					MainWindow frame = new MainWindow();
					
					
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
