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

package net.sf.sdedit.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.event.MouseInputListener;

import net.sf.sdedit.Constants;
import net.sf.sdedit.diagram.Diagram;
import net.sf.sdedit.diagram.Lifeline;
import net.sf.sdedit.diagram.PaintDevice;
import net.sf.sdedit.drawable.Drawable;
import net.sf.sdedit.drawable.ExtensibleDrawable;
import net.sf.sdedit.drawable.Fragment;
import net.sf.sdedit.ui.components.ZoomPane;
import net.sf.sdedit.ui.components.Zoomable;

/**
 * A <tt>PanelPaintDevice</tt> is a <tt>PaintDevice</tt> implementation that
 * uses a <tt>JPanel</tt> (see {@linkplain #getPanel()}) for displaying
 * diagrams. Its main benefit is that - in contrast to other
 * <tt>PaintDevice</tt> implementations - it does not allocate extra memory.
 * 
 * @author Markus Strauch
 * 
 */
public class PanelPaintDevice extends PaintDevice implements
		MouseInputListener, Constants {
	/**
	 * FontMetrics of the normal font.
	 */
	private FontMetrics fontMetrics;

	/**
	 * FontMetrics of the bold font.
	 */
	private FontMetrics boldFontMetrics;

	/**
	 * The drawable object that the mouse move most recently over.
	 */
	private Drawable lastDrawableMovedOver;

	/**
	 * The unscaled size of the diagram.
	 */
	private Dimension size;

	/**
	 * A set of listeners.
	 */
	private final List<PanelPaintDeviceListener> listeners;

	/**
	 * The panel for displaying the diagram.
	 */
	private final Panel panel;

	/**
	 * Flag denoting if this is an interactive <tt>PanelPaintDevice</tt> that
	 * reacts to mouse movement and clicks.
	 */
	private final boolean interactive;

	private boolean antialias;

	private boolean blank;

	/**
	 * Creates a new <tt>PanelPaintDevice</tt>.
	 * 
	 * @param interactive
	 *            flag denoting if the <tt>PanelPaintDevice</tt> is
	 *            interactive, meaning that it reacts to mouse movement and
	 *            clicks
	 */
	public PanelPaintDevice(boolean interactive) {
		super();
		this.interactive = interactive;
		listeners = new LinkedList<PanelPaintDeviceListener>();

		panel = new Panel();
		ToolTipManager.sharedInstance().registerComponent(panel);
		antialias = true;
		blank = true;
	}

	public void setDiagram(Diagram diagram) {
		super.setDiagram(diagram);
		Graphics preGraphics = new BufferedImage(1, 1,
				BufferedImage.TYPE_INT_RGB).getGraphics();
		preGraphics.setFont(getFont(false));
		fontMetrics = preGraphics.getFontMetrics();
		Graphics boldGraphics = new BufferedImage(1, 1,
				BufferedImage.TYPE_INT_RGB).getGraphics();
		boldGraphics.setFont(getFont(true));
		boldFontMetrics = boldGraphics.getFontMetrics();
	}

	/**
	 * This method is called when the mouse moved over the panel. If it has
	 * exited or entered a drawable object,
	 * {@linkplain PanelPaintDeviceListener}s are notified.
	 * 
	 * @param e
	 */
	public void mouseMoved(MouseEvent e) {
		if (interactive) {
			JPanel zp = panel.getZoomPane().getPanel();
			Point point = e.getPoint();
			if (lastDrawableMovedOver != null) {

				if (!lastDrawableMovedOver.contains(point)) {
					for (PanelPaintDeviceListener listener : listeners) {
						listener.mouseExitedDrawable(lastDrawableMovedOver);
						zp.setCursor(Cursor.getDefaultCursor());
					}
				} else {
					return;
				}
			}
			lastDrawableMovedOver = null;
			for (Drawable drawable : this) {
				if (!(drawable instanceof Fragment)) {
					if (drawable.contains(point)) {
						lastDrawableMovedOver = drawable;
						for (PanelPaintDeviceListener listener : listeners) {
							if (listener.mouseEnteredDrawable(drawable)) {
								zp.setCursor(HAND_CURSOR);
							}
						}
						return;
					}
				}
			}
		}
	}

	/**
	 * Adds a <tt>PanelPaintDeviceListener</tt> that will be notified when the
	 * mouse enters, exits or clicks drawable objects
	 * 
	 * @param ppdl
	 *            a a <tt>PanelPaintDeviceListener</tt>
	 */
	public void addListener(PanelPaintDeviceListener ppdl) {
		listeners.add(ppdl);
	}

	/**
	 * @see net.sf.sdedit.diagram.PaintDevice#close()
	 */
	public void close() {
		super.close();
		size = new Dimension(getWidth(), getHeight());
	}

	/**
	 * @see net.sf.sdedit.diagram.PaintDevice#getTextHeight(boolean)
	 */
	public int getTextHeight(boolean bold) {
		return (bold ? boldFontMetrics : fontMetrics).getHeight();
	}

	/**
	 * @see net.sf.sdedit.diagram.PaintDevice#getTextWidth(java.lang.String,
	 *      boolean)
	 */
	public int getTextWidth(String text, boolean bold) {
		return (bold ? boldFontMetrics : fontMetrics).stringWidth(text);
	}

	/**
	 * Returns the panel for displaying the diagram.
	 * 
	 * @return the panel for displaying the diagram
	 */
	public Zoomable<JPanel> getPanel() {
		return panel;
	}

	public void setAntialiasing(boolean on) {
		antialias = on;
	}

	public boolean isBlank() {
		return blank;
	}

	/**
	 * Notifies all {@linkplain PanelPaintDeviceListener}s when the mouse has
	 * clicked a drawable object.
	 * 
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		if (lastDrawableMovedOver != null) {
			for (PanelPaintDeviceListener listener : listeners) {
				listener.mouseClickedDrawable(lastDrawableMovedOver);
			}
		}
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
	}

	/**
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}

	@SuppressWarnings("serial")
	private final class Panel extends JPanel implements Zoomable<JPanel> {

		Panel() {
			addMouseMotionListener(PanelPaintDevice.this);
			addMouseListener(PanelPaintDevice.this);
		}

		private ZoomPane zoomPane;

		public ZoomPane getZoomPane() {
			return zoomPane;
		}

		public void setZoomPane(ZoomPane zoomPane) {
			this.zoomPane = zoomPane;
		}

		public JPanel asJComponent() {
			return this;
		}

		public Dimension getPreferredSize() {
			return getSize();
		}

		public Dimension getSize() {
			if (size == null) {
				return super.getSize();
			}
			return size;
		}

		@Override
		public int getWidth() {
			return getAbsoluteWidth();
		}

		@Override
		public int getHeight() {
			return getAbsoluteHeight();
		}

		public int getAbsoluteWidth() {
			if (size == null) {
				return super.getWidth();
			}
			return size.width;
		}

		public int getAbsoluteHeight() {
			if (size == null) {
				return super.getHeight();
			}
			return size.height;
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			String text = null;
			Point mousePoint = e.getPoint();
			for (Drawable drawable : PanelPaintDevice.this) {
				if (drawable instanceof ExtensibleDrawable) {
					ExtensibleDrawable ed = (ExtensibleDrawable) drawable;
					if (ed.contains(mousePoint)) {
						Lifeline lifeline = ed.getLifeline();
						text = lifeline.toString();
						if (lifeline.getDiagram().getConfiguration()
								.isThreaded()
								&& !lifeline.isAlwaysActive()
								&& (ed instanceof net.sf.sdedit.drawable.Rectangle)) {
							text = text + " [thread=" + lifeline.getThread()
									+ "]";
						}
					}
				}
			}
			return text;
		}

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			Rectangle clipBounds = g2.getClipBounds();
			g2.setColor(Color.WHITE);
			g2.fill(clipBounds);
			blank = isEmpty();
			if (!isEmpty()) {
				g2.setFont(PanelPaintDevice.this.getFont(false));
				if (antialias) {
					g2.setRenderingHints(new RenderingHints(
							RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON));
				}
				g2.setColor(Color.BLACK);
				for (Drawable drawable : PanelPaintDevice.this) {
					if (drawable.intersects(clipBounds)) {
						drawable.draw(g2);
					}
				}
			}
			g2.dispose();
		}
	}
}
