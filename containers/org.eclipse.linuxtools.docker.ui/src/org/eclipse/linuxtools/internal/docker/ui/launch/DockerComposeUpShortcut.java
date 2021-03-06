/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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

package org.eclipse.linuxtools.internal.docker.ui.launch;

import java.util.function.Predicate;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils;
import org.eclipse.linuxtools.internal.docker.ui.wizards.DockerComposeUpDialog;
import org.eclipse.linuxtools.internal.docker.ui.wizards.NewDockerConnection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * An {@link ILaunchShortcut} to run the {@code docker-compose up} command from
 * a specified {@code docker-compose.yml} file.
 */
public class DockerComposeUpShortcut extends BaseResourceAwareLaunchShortcut {

	@Override
	protected void launch(IResource resource, String mode) {
		// the predicate to apply on the launch configuration to find the
		// matching candidates
		final Predicate<ILaunchConfiguration> predicate = config -> {
			try {
				final String sourcePath = config.getAttribute(
						IDockerComposeLaunchConfigurationConstants.WORKING_DIR,
						""); //$NON-NLS-1$
				final boolean workspaceRelative = config.getAttribute(
						IDockerComposeLaunchConfigurationConstants.WORKING_DIR_WORKSPACE_RELATIVE_LOCATION,
						false);
				final IPath dockerfilePath = getPath(sourcePath,
						workspaceRelative);
				return dockerfilePath
						.equals(resource.getLocation().removeLastSegments(1));
			} catch (CoreException e) {
				Activator.log(e);
				return false;
			}
		};

		final ILaunchConfiguration config = findLaunchConfiguration(
				IDockerComposeLaunchConfigurationConstants.CONFIG_TYPE_ID,
				resource, predicate);

		if (config != null) {
			DebugUITools.launch(config, mode);
		} else {
			Activator.log(new DockerException(LaunchMessages
					.getString("DockerComposeUpShortcut.launchconfig.error"))); //$NON-NLS-1$
		}
	}

	/**
	 * Create a launch configuration based on a {@code docker-compose.yml}
	 * resource, and optionally save it to the underlying resource.
	 *
	 * @param dockerComposeScript
	 *            a {@code docker-compose.yml} file to use
	 * 
	 * @return a launch configuration generated for the
	 *         {@code docker-compose.yml}.
	 */
	@Override
	protected ILaunchConfiguration createConfiguration(
			final IResource dockerComposeScript) {
		try {
			if (!DockerConnectionManager.getInstance().hasConnections()) {
				Display.getDefault().asyncExec(() -> {
					final boolean confirm = MessageDialog.openQuestion(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							LaunchMessages.getString(
									"DockerComposeUpShortcut.no.connections.msg"), //$NON-NLS-1$
							LaunchMessages.getString(
									"DockerComposeUpShortcut.no.connections.desc")); //$NON-NLS-1$
					if (confirm) {
						final NewDockerConnection newConnWizard = new NewDockerConnection();
						CommandUtils.openWizard(newConnWizard,
								PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow().getShell());
					}
				});
				return null;
			} else {
				final DockerComposeUpDialog dialog = new DockerComposeUpDialog(
						getActiveWorkbenchShell());
				final int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					return LaunchConfigurationUtils
							.createDockerComposeUpLaunchConfiguration(
									dialog.getSelectedConnection(),
									dockerComposeScript);
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
		return null;
	}

}
