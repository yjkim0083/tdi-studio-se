// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2007 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.repository.preference;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.model.properties.SpagoBiServer;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryPreferenceStore;

/**
 * Preference for the SpagoBiServer values.
 * 
 * $Id: SpagoBiPreferencePage.java 2738 2007-04-26 13:12:27Z cantoine $
 * 
 */
public class SpagoBiPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    protected static Logger log = Logger.getLogger(SpagoBiPreferencePage.class);

    /**
     * SpagoBiPreferencePage.
     * 
     * $Id: SpagoBiPreferencePage.java 2738 2007-04-26 13:12:27Z cantoine $
     * 
     */

    public SpagoBiPreferencePage() {
        super(FLAT);
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        RepositoryPreferenceStore preferenceStore = new RepositoryPreferenceStore(ProxyRepositoryFactory.getInstance());
        try {
            preferenceStore.load();
        } catch (PersistenceException e) {
            String detailError = e.getMessage();
            new ErrorDialogWidthDetailArea(new Shell(), RepositoryPlugin.PLUGIN_ID, Messages
                    .getString("CommonWizard.persistenceException"), detailError); //$NON-NLS-1$
            log.error(Messages.getString("CommonWizard.persistenceException") + "\n" + detailError); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return preferenceStore;
    }

    @Override
    protected void createFieldEditors() {
        addField(new SpagoBiServerEditor(SpagoBiServer.SPAGOBI_SERVER,
                Messages.getString("SpagoBiPreferencePage.technicalStatusLabel"), getFieldEditorParent())); //$NON-NLS-1$
    }

    @Override
    protected void initialize() {
        try {
            super.initialize();
        } catch (RuntimeException e) {
            e.printStackTrace();
            setErrorMessage(e.getMessage());
            log.error(e);
        }
    }

    @Override
    protected void checkState() {
        if (getErrorMessage() == null) {
            super.checkState();
        } else {
            setValid(false);
        }
    }

    public void init(IWorkbench workbench) {
    }

}
