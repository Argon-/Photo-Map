package gui;

import java.awt.EventQueue;
import java.io.PrintStream;


public class GUIMain
{

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				try {
					MainWindow w = new MainWindow();
					final PrintStream original = System.out;

					System.out.println("Mirroring stdout to GUI");
			        System.setOut(new PrintStream(original) {
			        	public void println()         { original.print(System.getProperty("line.separator")); if (w != null) w.log(System.getProperty("line.separator")); }
			            public void println(String s) { original.print(s + System.getProperty("line.separator")); if (w != null) w.log(s + System.getProperty("line.separator")); }
			            public void print(String s)   { original.print(s); if (w != null) w.log(s); }
			        });
			        
			        w.init();
					w.setVisible(true);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

}
