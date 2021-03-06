/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.DockerImageBuildFailedException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerProgressHandler;
import org.eclipse.linuxtools.docker.core.IDockerProgressMessage;

public class DefaultImageBuildProgressHandler implements IDockerProgressHandler {

	private final static String IMAGE_BUILD_COMPLETE = "ImageBuildComplete.msg"; //$NON-NLS-1$
	private final static String IMAGE_BUILDING_JOBNAME = "ImageBuildingJobName.msg"; //$NON-NLS-1$
	private final static String IMAGE_BUILDING = "ImageBuilding.msg"; //$NON-NLS-1$
	private final static String IMAGE_BUILD_STEP = "ImageBuildStep.msg"; //$NON-NLS-1$

	private String image;
	private DockerConnection connection;
	private int lines;

	ProgressJob progressJob;

	/**
	 * Create a progress handler to watch the progress of building an image
	 * 
	 * @param connection
	 *            - docker connection
	 * @param image
	 *            - image being built
	 * @param lines
	 *            - number of lines in the Dockerfile
	 */
	public DefaultImageBuildProgressHandler(IDockerConnection connection,
			String image, int lines) {
		this.image = image;
		this.connection = (DockerConnection) connection;
		this.lines = lines;
	}

	@Override
	public void processMessage(IDockerProgressMessage message)
			throws DockerException {
		if (message.error() != null) {
			stopAllJobs();
			throw new DockerImageBuildFailedException(image, message.error());
		}
		if (progressJob == null) {
			// For image build, all the data is in the stream.
			String status = message.stream();
			if (status != null
					&& status.startsWith(DockerMessages
							.getString(IMAGE_BUILD_COMPLETE))) {
				// refresh images
				connection.getImages(true);
			} else {
				ProgressJob newJob = new ProgressJob(
						DockerMessages.getFormattedString(
								IMAGE_BUILDING_JOBNAME,
								image),
						DockerMessages.getString(IMAGE_BUILDING));
				newJob.setUser(true);
				newJob.setPriority(Job.LONG);
				newJob.schedule();
				progressJob = newJob;
			}

		} else {
			String status = message.stream();
			if (status != null
					&& status.startsWith(DockerMessages
							.getString(IMAGE_BUILD_COMPLETE))) {
				progressJob.setPercentageDone(100);
				// refresh images
				connection.getImages(true);
			} else if (status != null
					&& status
							.startsWith(DockerMessages
									.getString(IMAGE_BUILD_STEP))) {
				// Step number follows
				String stepNumber = status.substring(
						DockerMessages.getString(
						IMAGE_BUILD_STEP).length());
				// Need to separate step # from actual message.
				String[] tokens = stepNumber.split(" ");
				if (lines > 0) {
					long percentage = (Long.valueOf(tokens[0]) + 1) / lines;
					progressJob.setPercentageDone((int) percentage);
				}
			}
		}
	}

	private void stopAllJobs() {
		progressJob.cancel();
	}

}
