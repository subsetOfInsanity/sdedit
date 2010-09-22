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

package net.sf.sdedit.editor.apple;

import java.io.File;
import java.io.IOException;

import net.sf.sdedit.editor.Editor;
import net.sf.sdedit.ui.impl.UserInterfaceImpl;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

class AppInstallerImpl extends AppInstaller implements ApplicationListener {

	private Editor editor;

	private UserInterfaceImpl ui;
	
	private File fileToLoad;
	
	AppInstallerImpl () {
		/* empty */
	}
	
	@Override
	void install(Editor editor) {
		this.editor = editor;
		this.ui = (UserInterfaceImpl) editor.getUI();
		Application app = Application.getApplication();
		if (app != null) {
			app.setEnabledPreferencesMenu(true);
			app.addApplicationListener(this);
		}
	}
	
	/**
	 * @see net.sf.sdedit.editor.apple.AppInstaller#fileToLoad()
	 */
	public File fileToLoad () {
		return fileToLoad;
	}

	/**
	 * @see com.apple.eawt.ApplicationListener#handleAbout(com.apple.eawt.ApplicationEvent)
	 */
	public void handleAbout(ApplicationEvent e) {
		ui.showAboutDialog(Editor.class
				.getResource("/net/sf/sdedit/about.html"));
		e.setHandled(true);
	}
	


	/**
	 * @see com.apple.eawt.ApplicationListener#handlePreferences(com.apple.eawt.ApplicationEvent)
	 */
	public void handlePreferences(ApplicationEvent e) {
		ui.configure(false);
		e.setHandled(true);
	}

	/**
	 * @see com.apple.eawt.ApplicationListener#handleQuit(com.apple.eawt.ApplicationEvent)
	 */
	public void handleQuit(ApplicationEvent e) {
		editor.quit();
		e.setHandled(false);
	}

	/**
	 * @see com.apple.eawt.ApplicationListener#handlePrintFile(com.apple.eawt.ApplicationEvent)
	 */
	public void handlePrintFile(com.apple.eawt.ApplicationEvent ae) {
		ae.setHandled(true);
	}

	/**
	 * @see com.apple.eawt.ApplicationListener#handleReOpenApplication(com.apple.eawt.ApplicationEvent)
	 */
	public void handleReOpenApplication(com.apple.eawt.ApplicationEvent ae) {
		ui.toFront();
		ae.setHandled(true);
	}

	/**
	 * @see com.apple.eawt.ApplicationListener#handleOpenFile(com.apple.eawt.ApplicationEvent)
	 */
	public void handleOpenFile(com.apple.eawt.ApplicationEvent ae) {
		File file = new File(ae.getFilename());
		if (!editor.isSetup()) {
			fileToLoad = file;
		} else {
			try {
				editor.loadCode(file);
			} catch (RuntimeException re) {
				throw re;
			}
			catch (Exception ex) {
				ui.errorMessage("Cannot load " + file.getAbsolutePath() + "\n"
						+ "due to an exception of type "
						+ ex.getClass().getSimpleName() + "\n"
						+ "with the message: " + ex.getMessage());
			}
		}
		ae.setHandled(true);
	}

	/**
	 * @see com.apple.eawt.ApplicationListener#handleOpenApplication(com.apple.eawt.ApplicationEvent)
	 */
	public void handleOpenApplication(ApplicationEvent event) {
		if (event.getFilename() != null) {
			handleOpenFile(event);
		}
	}

}
