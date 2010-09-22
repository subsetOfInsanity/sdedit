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

package net.sf.sdedit.drawable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import net.sf.sdedit.config.Configuration;
import net.sf.sdedit.message.AnswerToSelf;
import net.sf.sdedit.message.Message;
import net.sf.sdedit.util.Direction;


public class LoopArrow extends Arrow
{
    private boolean isAnswer;

    private int xExtent;

    private Drawable from;

    private Drawable to;

    private Point[] pts;

    private Point textPosition;

    /**
     * Creates a new LoopArrow.
     * 
     * @param msg
     *            a self-message or answer to self
     * @param stroke
     *            the stroke to be used for drawing the arrow
     * @param align
     *            Align.LEFT if the message is to be drawn on the left of a
     *            lifeline, otherwise Align.RIGHT
     * @param y
     *            the vertical position where to start drawing
     */
    public LoopArrow(Message msg, ArrowStroke stroke, Direction align, int y) {
        super(msg, stroke, align, y);
        init();
    }

    private void init() {
        Message message = getMessage();
        Configuration conf = message.getConfiguration();
        xExtent = conf.getSelfMessageHorizontalSpace();
        setWidth(diagram.messagePadding + xExtent + diagram.subLifelineWidth + textWidth());
        isAnswer = message instanceof AnswerToSelf;
        from = message.getCaller().getView();
        to = message.getCallee().getView();
        if (getAlign() == Direction.LEFT) {
            // loop arrows on the left must have a left neighbour
            setLeftEndpoint(message.getCallee().getLeftNeighbour().getView());
            if (message.getCaller().getSideLevel() < message.getCallee()
                    .getSideLevel()) {
                setRightEndpoint(message.getCaller().getView());
            } else {
                setRightEndpoint(message.getCallee().getView());
            }
        } else {
            int p = message.getCallee().getPosition();

            if (p < message.getDiagram().getNumberOfLifelines() - 1
                    && message.getDiagram().getLifelineAt(p + 1).isAlive()) {
                setRightEndpoint(message.getCallee().getRightNeighbour()
                        .getView());
            } else {
                setRightEndpoint(message.getDiagram().getPaintDevice()
                        .getRightBound());
            }
            if (message.getCaller().getSideLevel() < message.getCallee()
                    .getSideLevel()) {
                setLeftEndpoint(message.getCaller().getView());
            } else {
                setLeftEndpoint(message.getCallee().getView());
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        int t = getMessage().getData().getThread();
        Color back = !configuration().isOpaqueMessageText() ||
            t == -1 ? null : THREAD_COLORS[t];
        drawMultilineString(g, textPosition.x, textPosition.y, isAnswer, back);
        
        g.setColor(Color.BLACK);
        overrideColor(g);
        int sgn = getAlign() == Direction.RIGHT ? 1 : -1;

        g.setStroke(getStroke() == ArrowStroke.SOLID ? solid : dashed);

        g.drawLine(pts[0].x, pts[0].y, pts[1].x, pts[1].y);
        g.drawLine(pts[1].x, pts[1].y, pts[2].x, pts[2].y);
        g.drawLine(pts[2].x, pts[2].y, pts[3].x, pts[3].y);

        drawArrowHead(g, pts[3].x, pts[3].y, sgn);

        g.setStroke(solid);
    }

    public int getInnerHeight() {
        return textHeight();
    }

    /**
     * Returns an array of four points, representing the positions of the points
     * that are connected by the lines of which the loop arrow is made up.
     * 
     * @return an array of four points, representing the positions of the points
     *         that are connected by the lines of which the loop arrow is made
     *         up
     */
    public Point[] getLinePoints() {
        return pts;
    }
    
    public Point getAnchor () {
        return pts[1];
    }

    /**
     * Returns the position of the left bottom of the label of the loop
     * arrow.
     * 
     * @return the position of the left bottom of the label of the loop
     * arrow
     */
    public Point getTextPosition() {
        return textPosition;
    }

    @Override
    public void computeLayoutInformation() {
        int y_to = getTop() + textHeight();
        int y_from = getTop();

        int x_from, x_to;

        if (getAlign() == Direction.RIGHT) {
            x_from = from.getLeft() + from.getWidth();
            x_to = to.getLeft() + to.getWidth();
            setLeft(Math.min(x_from, x_to));
        } else {
            x_from = from.getLeft();
            x_to = to.getLeft();
            setLeft(Math.min(x_from - getWidth(), x_to - getWidth()));
        }

        int outer_x = getAlign() == Direction.RIGHT ? Math.max(x_to + xExtent,
                x_from + xExtent) : Math.min(x_to - xExtent, x_from - xExtent);

        pts = new Point[4];

        pts[0] = new Point(x_from, y_from);
        pts[1] = new Point(outer_x, y_from);
        pts[2] = new Point(outer_x, y_to);
        pts[3] = new Point(x_to, y_to);

        int textOffset = getAlign() == Direction.RIGHT ? diagram.messagePadding : -textWidth()
                - diagram.messagePadding;
        int textY = isAnswer ? y_to : y_from + textHeight();
        textPosition = new Point(outer_x + textOffset, textY);
    }
}
