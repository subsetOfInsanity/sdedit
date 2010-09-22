//Copyright (c) 2006 - 2008, Markus Strauch.
//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without
//modification, are permitted provided that the following conditions are met:
//
//* Redistributions of source code must retain the above copyright notice, 
//this list of conditions and the following disclaimer.
//* Redistributions in binary form must reproduce the above copyright notice, 
//this list of conditions and the following disclaimer in the documentation 
//and/or other materials provided with the distribution.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
//ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
//LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
//CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
//THE POSSIBILITY OF SUCH DAMAGE.

package net.sf.sdedit.message;

import net.sf.sdedit.diagram.Diagram;
import net.sf.sdedit.diagram.Lifeline;
import net.sf.sdedit.diagram.MessageData;
import net.sf.sdedit.drawable.Arrow;
import net.sf.sdedit.drawable.ArrowStroke;
import net.sf.sdedit.drawable.LoopArrow;
import net.sf.sdedit.util.Direction;

public class AnswerToSelf extends Answer
{

    public AnswerToSelf(Lifeline caller, Lifeline callee, Diagram diagram,
            MessageData data, ForwardMessage forward) {
        super(caller, callee, diagram, data, forward);
    }
    
    public void updateView() {
    	
    	getDiagram().getPaintDevice().announce(getConfiguration().getSpaceBeforeAnswerToSelf()+
    			Arrow.getInnerHeight(this) + diagram.arrowSize / 2);
    	
        getDiagram().getFragmentManager().finishFragmentsNotIncluding(this);
        extendLifelines(getConfiguration().getSpaceBeforeAnswerToSelf());
        Direction align = getCaller().getDirection();
        Arrow arrow = new LoopArrow(this, ArrowStroke.DASHED,
                align, v());
        arrow.setVisible(getText().length()>0 || diagram.returnArrowVisible);
        setArrow(arrow);
        getDiagram().getPaintDevice().addSequenceElement(arrow);
        
        terminate();
        
        extendLifelines(arrow.getInnerHeight());

    }
}


