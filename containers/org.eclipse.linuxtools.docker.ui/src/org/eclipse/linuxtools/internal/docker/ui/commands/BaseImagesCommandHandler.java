/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.commands;

import static org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils.getCurrentConnection;
import static org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils.getSelectedImages;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Base handler for commands on {@link IDockerImage}
 * 
 * @author jjohnstn
 *
 */
public abstract class BaseImagesCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final List<IDockerImage> selectedImages = getSelectedImages(activePart);
		final IDockerConnection connection = getCurrentConnection(activePart);
		if (connection == null || selectedImages.isEmpty()) {
			Activator.log(new DockerException(CommandMessages
					.getString("Command.missing.selection.failure"))); //$NON-NLS-1$
			return null;
		}
		final Job job = new Job(getJobName(selectedImages)) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				if (confirmed(selectedImages)) {
					monitor.beginTask(getJobName(selectedImages),
							selectedImages.size());
					for (final IDockerImage image : selectedImages) {
						monitor.setTaskName(getTaskName(image));
						executeInJob(image, connection);
						monitor.worked(1);
					}
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.LONG);
		job.setUser(true);
		job.schedule();
		return null;
	}

	void openError(final String errorMessage, final Exception e) {
		Display.getDefault().syncExec(() -> MessageDialog.openError(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				errorMessage, e.getMessage()));
	}

	// allow commands to add confirmation dialog
	boolean confirmed(
			@SuppressWarnings("unused") List<IDockerImage> selectedImages) {
		return true;
	}
	
	abstract String getJobName(final List<IDockerImage> selectedImages);

	abstract String getTaskName(final IDockerImage image);
	
	abstract void executeInJob(final IDockerImage image,
			final IDockerConnection connection);
}
