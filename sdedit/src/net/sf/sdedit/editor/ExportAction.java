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

package net.sf.sdedit.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.sdedit.Constants;
import net.sf.sdedit.diagram.Diagram;
import net.sf.sdedit.ui.PanelPaintDevice;
import net.sf.sdedit.ui.impl.LookAndFeelManager;

import org.freehep.util.export.ExportDialog;
import org.freehep.util.export.ExportDialogListener;

public class ExportAction extends AbstractAction implements
        ExportDialogListener, Constants {
    
    private static final Set<String> vectorFormats;

    static {
        vectorFormats = new HashSet<String>();
        vectorFormats.add("pdf");
        vectorFormats.add("ps");
        vectorFormats.add("eps");
        vectorFormats.add("svg");
        vectorFormats.add("emf");
        vectorFormats.add("swf");
    }

    private ExportDialog exportDialog;

    private Editor editor;

    private Properties properties;
    
    private PanelPaintDevice exportDevice;
    
    public ExportAction (Editor editor) {
        this.editor = editor;
        properties = new Properties();
       	putValue(Action.NAME, Shortcuts.getShortcut(Shortcuts.EXPORT) + "E&xport...");
        putValue(Actions.ICON_NAME, "image");
        putValue(Action.SHORT_DESCRIPTION, "Export diagram as bitmap or vector graphics");
    }
    
    public void actionPerformed(ActionEvent e) {
    	Diagram diagram = editor.getUI().getDiagram();
    	if (diagram == null) {
    		return;
    	}
        exportDevice = (PanelPaintDevice) diagram.getPaintDevice();
        if (exportDevice.isEmpty()) {
            return;
        }
        try {
            File file = editor.getUI().getCurrentFile();
            if (exportDialog == null) {
                exportDialog = new ExportDialog("Quick Sequence Diagram Editor");
                LookAndFeelManager.instance().registerOrphan(exportDialog);
                exportDialog.setUserProperties(properties);
                exportDialog.addExportDialogListener(this);
                if (file != null) {
                    properties.setProperty(SAVE_AS_FILE_PROPERTY, file
                            .getAbsolutePath());
                }
            } else {
                String fileName = properties.getProperty(SAVE_AS_FILE_PROPERTY);
                File current = fileName != null ? new File(fileName) : null;
                if (current != null && current.exists()) {
                    File dir = current.getParentFile();
                    if (file == null) {
                        current = new File(dir, "untitled");
                    } else {
                        current = new File(dir, file.getName());
                    }
                    properties.setProperty(SAVE_AS_FILE_PROPERTY, current
                            .getAbsolutePath());
                }

            }
            
            exportDialog
                    .showExportDialog(
                            (Component) editor.getUI(),
                            "Export via FreeHEP library (see http://www.freehep.org/vectorgraphics)",
                            exportDevice.getPanel().asJComponent(), "untitled");

        } catch (Exception ex) {
            ex.printStackTrace();
            editor.getUI().errorMessage(
                    "Cannot export due to an exception\n" + "of type "
                            + ex.getClass().getSimpleName() + "\n"
                            + "with the message: " + ex.getMessage());
        } finally {
            exportDevice.setAntialiasing(true);
        }
    }

    public void writeFile(String type) {
        if (vectorFormats.contains(type)) {
            exportDevice.setAntialiasing(false);
        }
    }
}
