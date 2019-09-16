package org.eclipse.swt.examples.paint;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */


public class PaintSurface {
	private final Canvas      paintCanvas;
	public PaintSurface(Canvas paintCanvas, Text statusText) {
	 * Sets the current paint session.
	 * If oldPaintSession != paintSession calls oldPaintSession.end()
	 * @param paintSession the paint session to activate; null to disable all sessions
	 */
	public void setPaintSession(PaintSession paintSession) {
		if (this.paintSession != null) {
			this.paintSession.endSession();
		this.paintSession = paintSession;
		clearStatus();

	/**
	 * Returns the current paint session.
	 * 
	 * @return the current paint session, null if none is active
	 */
	public PaintSession getPaintSession() {
		return paintSession;
	}
