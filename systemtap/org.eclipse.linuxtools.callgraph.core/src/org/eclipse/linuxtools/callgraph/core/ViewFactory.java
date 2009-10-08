/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.callgraph.core;

import java.util.ArrayList;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


/**
 * Class to create / manipulate Views
 */
public class ViewFactory {
	
	private static ArrayList<IViewPart> views;
	
	/**
	 * Create a view of type designated by the viewID argument
	 * @param viewID : A string corresponding to a type of View
	 * @return : The view object that corresponds to the viewID
	 */
	public static void createView(String viewID) {
		try {
			PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.showView(viewID);
			return;
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return;
	}
	
	/**
	 * Adds a view to the factory's list of active SystemTapViews.
	 */
	public static void addView(SystemTapView view) {
		if (views == null)
			views = new ArrayList<IViewPart>();
		views.add(view);
	}
	
	/**
	 * Returns the first view added to the views list that is of type SystemTapView.
	 */
	public static IViewPart getView() {
		for (IViewPart view : views) {
			if (view instanceof SystemTapView)
				return view;
		}
		return null;
	}
	
	/**
	 * Returns the list of views
	 * @return
	 */
	public static SystemTapView[] getViews() {
		return (SystemTapView[]) views.toArray();
	}
	
}
