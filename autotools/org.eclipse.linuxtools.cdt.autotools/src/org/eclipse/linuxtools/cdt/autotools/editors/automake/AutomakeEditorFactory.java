/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.editors.automake;

import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.linuxtools.cdt.autotools.AutotoolsPlugin;
import org.eclipse.linuxtools.internal.cdt.autotools.editors.automake.IMakefileDocumentProvider;
import org.eclipse.linuxtools.internal.cdt.autotools.editors.automake.WorkingCopyManager;
import org.eclipse.linuxtools.internal.cdt.autotools.ui.preferences.ColorManager;
import org.eclipse.swt.graphics.Color;


public class AutomakeEditorFactory {
	private IWorkingCopyManager workingCopyManager;
	private IMakefileDocumentProvider automakeFileDocumentProvider;
	private static AutomakeEditorFactory factory;

	/**
	 * The constructor.
	 */
	private AutomakeEditorFactory() {
		factory = this;
	}
	
	public synchronized IMakefileDocumentProvider getAutomakefileDocumentProvider() {
		if (automakeFileDocumentProvider == null) {
			automakeFileDocumentProvider=  new AutomakeDocumentProvider();
		}
		return automakeFileDocumentProvider;
	}

	public synchronized IWorkingCopyManager getWorkingCopyManager() {
		if (workingCopyManager == null) {
			IMakefileDocumentProvider provider= getAutomakefileDocumentProvider();
			workingCopyManager= new WorkingCopyManager(provider);
		}
		return workingCopyManager;
	}

	/**
	 * Returns the preference color, identified by the given preference.
	 */
	public static Color getPreferenceColor(String key) {
		//FIXME: what do we do with Makefile editor preferences?
		return ColorManager.getDefault().getColor(PreferenceConverter.getColor(AutotoolsPlugin.getDefault().getPreferenceStore(), key));
	}

	public static AutomakeEditorFactory getDefault() {
		if (factory == null)
			factory = new AutomakeEditorFactory();
		return factory;
	}
}
