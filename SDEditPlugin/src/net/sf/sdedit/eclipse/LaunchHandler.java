package net.sf.sdedit.eclipse;

import java.io.File;
import java.io.PrintStream;

import javax.swing.SwingUtilities;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;


public class LaunchHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			StackTree.getTopNode().writeSDEditCommands(
					new PrintStream(new File ("tmp.sdx")));
			net.sf.sdedit.Main.main(new String[] {"tmp.sdx"});
			//net.sf.sdedit.Main.main(new String[] {"-e", "tmp.sdx"});
			//TODO:  Need to figure out best way to run in embedded mode

/*  The swing interface is misbehaving with this setting
 *  (Couldn't adjust sub windows, border bars, etc)
 *  so I'm disabling this for now and using the main method.
 *  TODO: Should figure out why the main method works, but this doesn't ev
 *  
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					boolean loaded=false;
					Editor editor = Editor.getEditor();//TODO:  make the Editor Class configurable so it won't kill the JVM on exit
					editor.getUI().showUI();
					File sdFile = new File("tmp.sdx");
					if (sdFile.exists() && sdFile.canRead()
							&& !sdFile.isDirectory()) {
						loaded = true;
						try {
							editor.loadCode(sdFile);
						} catch (Exception e) {
							editor.error(e.getMessage());
						}
					} else {
						System.err.println("Warning: ignoring file "
								+ "tmp.sdx");
					}
					if (!loaded) {
						editor.getUI().addTab(
								"untitled",
								ConfigurationManager
										.createNewDefaultConfiguration());
					}
				}
			});
*/

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
