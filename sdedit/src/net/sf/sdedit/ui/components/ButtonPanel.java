// Copyright (c) 2006 - 2008, Markus Strauch.
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 
// * Redistributions of source code must retain the above copyright notice, 
// this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice, 
// this list of conditions and the following disclaimer in the documentation 
// and/or other materials provided with the distribution.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
// THE POSSIBILITY OF SUCH DAMAGE.

package net.sf.sdedit.ui.components;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * A <tt>ButtonPanel</tt> is a <tt>JPanel</tt> that contains some buttons
 * associated to <tt>Action</tt>s. <tt>Action</tt> objects can be added via
 * {@linkplain #addAction(Action)}. For each of them, a new button is created.
 * Buttons will be laid out horizontally, either from the left to the right or
 * from the right to the left. All buttons have the same dimension.
 * 
 * @author Markus Strauch
 * 
 */
public class ButtonPanel extends JPanel {

	private static final long serialVersionUID = 2123185410505818217L;

	private int maxWidth;

	private int maxHeight;

	private JButton defaultButton;
	
	private static final int BORDER_WIDTH = 5;
	
	/*
	 * The width between the buttons
	 */
	private static final int GAP_WIDTH = 8;

	/**
	 * Creates a new <tt>ButtonPanel</tt> where buttons are laid out according
	 * to the given <tt>ComponentOrientation</tt>.
	 * 
	 * @param orientation
	 *            denotes the orientation in which to lay out buttons
	 *            horizontally
	 */
	public ButtonPanel(ComponentOrientation orientation) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		setComponentOrientation(orientation);
		addAncestorListener(new AncestorListener() {
			/**
			 * @see javax.swing.event.AncestorListener#ancestorAdded(javax.swing.event.AncestorEvent)
			 */
			public void ancestorAdded(AncestorEvent event) {
				setDefaultButton();
			}

			/**
			 * @see javax.swing.event.AncestorListener#ancestorMoved(javax.swing.event.AncestorEvent)
			 */
			public void ancestorMoved(AncestorEvent event) {}

			/**
			 * @see javax.swing.event.AncestorListener#ancestorRemoved(javax.swing.event.AncestorEvent)
			 */
			public void ancestorRemoved(AncestorEvent event) {}

		});
	}

	/*
	 * This method is called each time a button or an ancestor is added.
	 * So the root pane that contains this ButtonPanel will be informed
	 * about the default button (if present) in any case, whether the
	 * ButtonPanel has already been added to it as a descendant or whether
	 * it is added later.
	 */
	private void setDefaultButton() {
		if (defaultButton != null) {
			Component comp = ButtonPanel.this;
			while (comp != null && !(comp instanceof JRootPane)) {
				comp = comp.getParent();
			}
			if (comp != null) {
				((JRootPane) comp).setDefaultButton(defaultButton);
				// default button has been successfully set, "erase" it now
				defaultButton = null;
			}
		}
	}

	/**
	 * Creates a new <tt>ButtonPanel</tt> where buttons are laid out from the
	 * right to the left.
	 */
	public ButtonPanel() {
		this(ComponentOrientation.RIGHT_TO_LEFT);
	}

	/**
	 * Creates a new <tt>JButton</tt> and adds it to the panel. When it is
	 * clicked, the given action's <tt>actionPerformed</tt> method will be
	 * invoked.
	 * 
	 * @param action
	 *            an action for which a new <tt>JButton</tt> is to be added
	 */
	public void addAction(Action action) {
		addAction(action, 0, false);
	}

	/**
	 * Creates a new <tt>JButton</tt> and adds it to the panel. When it is
	 * clicked, the given action's <tt>actionPerformed</tt> method will be
	 * invoked.
	 * 
	 * @param action
	 *            an action for which a new <tt>JButton</tt> is to be added
	 * @param isDefault
	 *            flag denoting if the button is to be the default button
	 */
	public void addAction(Action action, int space, boolean isDefault) {
		JButton button = new JButton(action);
		maxWidth = Math.max(maxWidth, button.getPreferredSize().width);
		maxHeight = Math.max(maxHeight, button.getPreferredSize().height);
		add(button);
		add(Box.createRigidArea(new Dimension(GAP_WIDTH, 1)));
		if (space > 0) {
			add(Box.createRigidArea(new Dimension(space,1)));
		}
		Dimension size = new Dimension(maxWidth, maxHeight);
		for (Component comp : getComponents()) {
			if (comp instanceof JButton) {
				JButton but = (JButton) comp;
				but.setMinimumSize(size);
				but.setPreferredSize(size);
				but.setMaximumSize(size);
			}
		}
		if (isDefault) {
			defaultButton = button;
			setDefaultButton();
		}
	}
}
