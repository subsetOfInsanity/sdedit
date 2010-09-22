package net.sf.sdedit.eclipse;

import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;


public class DebugListener implements IDebugEventSetListener {
	
	@Override
	public void handleDebugEvents(DebugEvent[] events) {

		//add a stacktrace to the SDEditAdapter on breakpoints
		for (DebugEvent event : events) {
			if (    event.getKind()    == DebugEvent.SUSPEND &&
					event.getDetail()  == DebugEvent.BREAKPOINT) {
				
				System.out.println("TESTING:");
				System.out.println(event.getSource()+": "+event.getSource().getClass() );

				if (event.getSource() instanceof IThread){
					try {
						if(((IThread)event.getSource()).hasStackFrames()){
							//TODO: pass the IStackFrame[] to a data object to cast and build a tree.
							StackTreeBuilder.addStack( StackTree.getTopNode(), 
									((IThread)event.getSource()).getStackFrames());
						}
					} catch (DebugException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} 
				System.out.println("END TEST");
			}

			
		}
		
	}

}
