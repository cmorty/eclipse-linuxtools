/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.preference;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Project property page for configuring the delta options for a repository.
 */
public class CreaterepoDeltaPropertyPage extends CreaterepoPropertyPage {

    private Composite composite;
    private BooleanFieldEditor bfeEnableDeltas;
    private Group optionsGroup;
    private IntegerFieldEditor ifeNumDeltas;
    private IntegerFieldEditor ifeMaxSizeDeltas;

    private Group dirGroup;
    private CreaterepoPathEditor peDirectories;

    /**
     * Default Constructor. Sets the description of the property page.
     */
    public CreaterepoDeltaPropertyPage() {
        super(Messages.CreaterepoDeltaPropertyPage_description);
    }

    @Override
    protected Composite addContents(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);

        bfeEnableDeltas = new BooleanFieldEditor(
                CreaterepoPreferenceConstants.PREF_DELTA_ENABLE,
                Messages.CreaterepoDeltaPropertyPage_booleanEnableLabel, composite);
        bfeEnableDeltas.setPreferenceStore(Activator.getDefault().getPreferenceStore());
        bfeEnableDeltas.load();
        bfeEnableDeltas.setPropertyChangeListener(this);

        optionsGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);

        layout = new GridLayout(2, false);
        optionsGroup.setLayout(layout);
        optionsGroup.setText(Messages.CreaterepoDeltaPropertyPage_groupLabel);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, false).applyTo(optionsGroup);

        // max deltas.
        ifeNumDeltas = new IntegerFieldEditor(
                CreaterepoPreferenceConstants.PREF_NUM_DELTAS,
                Messages.CreaterepoDeltaPropertyPage_maxNumberOfDeltas, optionsGroup);
        ifeNumDeltas.setPreferenceStore(preferenceStore);
        ifeNumDeltas.setValidRange(0, Integer.MAX_VALUE);
        ifeNumDeltas.load();
        ifeNumDeltas.setPropertyChangeListener(this);
        ifeNumDeltas.setTextLimit(String.valueOf(Integer.MAX_VALUE).length());

        // max delta size. stored in megabytes for convenience to user
        ifeMaxSizeDeltas = new IntegerFieldEditor(
                CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE,
                Messages.CreaterepoDeltaPropertyPage_maxDeltaSize, optionsGroup);
        ifeMaxSizeDeltas.setPreferenceStore(preferenceStore);
        ifeMaxSizeDeltas.setValidRange(0, Integer.MAX_VALUE);
        ifeMaxSizeDeltas.load();
        ifeMaxSizeDeltas.setPropertyChangeListener(this);
        ifeMaxSizeDeltas.setTextLimit(String.valueOf(Integer.MAX_VALUE).length());

        // spaces around the group
        layout = (GridLayout) optionsGroup.getLayout();
        layout.marginWidth = 5;
        layout.marginHeight = 5;

        // the group for directories to package against
        dirGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        layout = new GridLayout(2, false);
        dirGroup.setLayout(layout);
        dirGroup.setText(Messages.CreaterepoDeltaPropertyPage_groupDirectoryLabel);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, false).applyTo(dirGroup);

        peDirectories = new CreaterepoPathEditor(CreaterepoPreferenceConstants.PREF_OLD_PACKAGE_DIRS,
                Messages.CreaterepoDeltaPropertyPage_directoryDescription,
                Messages.CreaterepoDeltaPropertyPage_directoryDialogLabel, dirGroup);
        peDirectories.setPreferenceStore(preferenceStore);
        peDirectories.load();
        peDirectories.setPropertyChangeListener(this);

        layout = (GridLayout) dirGroup.getLayout();
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        toggleEnabled();
        return composite;
    }

    @Override
    public void performDefaults() {
        Activator.getDefault().getPreferenceStore().setToDefault(CreaterepoPreferenceConstants.PREF_DELTA_ENABLE);
        bfeEnableDeltas.loadDefault();
        ifeNumDeltas.loadDefault();
        ifeMaxSizeDeltas.loadDefault();
        peDirectories.loadDefault();
    }

    @Override
    public boolean performOk() {
        Activator.getDefault().getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_DELTA_ENABLE,
                bfeEnableDeltas.getBooleanValue());
        getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_NUM_DELTAS,
                ifeNumDeltas.getIntValue());
        getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE,
                ifeMaxSizeDeltas.getIntValue());
        peDirectories.store();
        return true;
    }

    @Override
    protected void checkState() {
        if (!ifeNumDeltas.isValid() || !ifeMaxSizeDeltas.isValid()) {
            bfeEnableDeltas.setEnabled(false, composite);
            setErrorMessage(ifeMaxSizeDeltas.getErrorMessage());
            setValid(false);
        } else {
            bfeEnableDeltas.setEnabled(true, composite);
            setErrorMessage(null);
            setValid(true);
        }
        toggleEnabled();
    }

    /**
     * Toggle the enabled status of the field editors dependending on the
     * enabled status of the button.
     */
    private void toggleEnabled() {
        boolean enabled = bfeEnableDeltas.getBooleanValue();
        ifeNumDeltas.setEnabled(enabled, optionsGroup);
        ifeMaxSizeDeltas.setEnabled(enabled, optionsGroup);
        peDirectories.setEnabled(enabled, dirGroup);
    }

}
