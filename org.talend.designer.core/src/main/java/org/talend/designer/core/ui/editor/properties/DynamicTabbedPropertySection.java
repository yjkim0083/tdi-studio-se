// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
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
package org.talend.designer.core.ui.editor.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.eclipse.core.runtime.IPath;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.colorstyledtext.ColorManager;
import org.talend.commons.ui.swt.colorstyledtext.ColorStyledText;
import org.talend.commons.ui.swt.proposal.ContentProposalAdapterExtended;
import org.talend.commons.ui.swt.proposal.TextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator.LAYOUT_MODE;
import org.talend.commons.ui.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.utils.ControlUtils;
import org.talend.commons.ui.utils.TypedTextCommandExecutor;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.commons.utils.data.container.Content;
import org.talend.commons.utils.data.container.ContentList;
import org.talend.commons.utils.data.container.RootContainer;
import org.talend.core.CorePlugin;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.perl.ICodeSyntaxChecker;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.PositionalFileConnection;
import org.talend.core.model.metadata.builder.connection.RegexpFileConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.temp.ECodeLanguage;
import org.talend.core.ui.images.EImage;
import org.talend.core.ui.images.ImageProvider;
import org.talend.core.ui.metadata.dialog.MetadataDialog;
import org.talend.core.ui.proposal.ProcessProposalProvider;
import org.talend.core.ui.proposal.ProcessProposalUtils;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.components.ExternalUtilities;
import org.talend.designer.core.model.context.ContextManager;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.editor.TalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeActivateStatusNodeCommand;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.cmd.ExternalNodeChangeCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.cmd.SchemaPropertyChangeCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionLabel;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeLabel;
import org.talend.designer.core.ui.editor.outline.NodeReturnsTreeEditPart;
import org.talend.designer.core.ui.editor.outline.NodeTreeEditPart;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorModel;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorView;
import org.talend.designer.runprocess.language.SyntaxCheckerFactory;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.utils.RepositoryPathProvider;
import org.talend.sqlbuilder.ui.SQLBuilderDialog;
import org.talend.sqlbuilder.util.ConnectionParameters;

/**
 * Dynamic node's property section. This allow the tabbed property to be dynamic dependings on node's parameters. <br/>
 * 
 * $Id$
 * 
 */
public class DynamicTabbedPropertySection extends AbstractPropertySection {

    protected MultiPageTalendEditor part;

    protected Element elem;

    protected Composite composite;

    protected BidiMap hashCurControls;

    protected String currentComponent;

    protected EComponentCategory section;

    protected int curRowSize;

    private String oldSchemaType;

    private String oldProcessType;

    private Map<String, IMetadataTable> repositoryTableMap;

    private Map<String, ConnectionItem> repositoryConnectionItemMap;

    private Map<String, IRepositoryObject> processMap;

    private static final int STANDARD_HEIGHT = 20;

    private static final int STANDARD_LABEL_WIDTH = 100;

    private static final int STANDARD_BUTTON_WIDTH = 25;

    private static final int MAX_PERCENT = 100;

    private static final String NAME = "NAME"; //$NON-NLS-1$

    private static final String VER_MAJ = "MajVerUp"; //$NON-NLS-1$

    private static final String VER_MIN = "MinVerUp"; //$NON-NLS-1$

    private static final String FILE = "FILE"; //$NON-NLS-1$

    private static final String DIRECTORY = "DIRECTORY"; //$NON-NLS-1$

    private static final String PROPERTY = "PROPERTY"; //$NON-NLS-1$

    private static final String EXTERNAL = "EXTERNAL"; //$NON-NLS-1$

    private static final String SCHEMA = "SCHEMA"; //$NON-NLS-1$

    private static final String COLUMN = "COLUMN"; //$NON-NLS-1$

    private static final String RESET_COLUMNS = "RESET_COLUMNS"; //$NON-NLS-1$

    private static final String VARIABLE_TOOLTIP = "The variable attached to this parameter is : ";

    private static final String DOTS_BUTTON = "icons/dots_button.gif"; //$NON-NLS-1$

    private static final String REFRESH_BUTTON = "icons/refresh.gif"; //$NON-NLS-1$

    private IMetadataTable repositoryMetadata = new MetadataTable();

    private org.talend.core.model.metadata.builder.connection.Connection repositoryConnection;

    private String oldPropertyType;

    private EditionControlHelper editionControlHelper;

    // Added by ftang.
    private static final String SQLEDITOR = "SQLEDITOR";

    // Ends

    /**
     * Get the command stack of the Gef editor.
     * 
     * @return
     */
    protected CommandStack getCommandStack() {
        TalendEditor talendEditor = part.getTalendEditor();
        Object adapter = talendEditor.getAdapter(CommandStack.class);
        return (CommandStack) adapter;
    }

    /**
     * Modified by ftang.
     * 
     * Gets value base on repository name.
     * 
     * @param repositoryName a string representing repository name
     * 
     * @return a string representing value
     */
    private String getValueFromRepositoryName(String repositoryName) {
        for (IElementParameter param : (List<IElementParameter>) elem.getElementParameters()) {
            if (param.getRepositoryValue() != null) {
                if (param.getRepositoryValue().equals(repositoryName)) {
                    return (String) param.getValue();
                }
            }
        }
        return null;
    }// Ends

    private SelectionListener listenerSelection = new SelectionListener() {

        public void widgetDefaultSelected(final SelectionEvent e) {
        }

        @SuppressWarnings("unchecked")
        private void specificButtonSelected(String info, Button button) {
            if (info.equals(EXTERNAL)) {
                Node node = (Node) elem;
                IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen(node);

                if (externalNode == null) {
                    MessageBox mBox = new MessageBox(composite.getShell(), SWT.ICON_ERROR);
                    mBox.setText("Error");
                    mBox.setMessage("Component plugin not found: " + node.getPluginFullName());
                    mBox.open();
                } else {
                    if (externalNode.open(composite.getDisplay()) == SWT.OK) {
                        Command cmd = new ExternalNodeChangeCommand(node, externalNode);
                        getCommandStack().execute(cmd);
                    }
                }
            }
            if (info.equals(FILE)) {
                FileDialog dial = new FileDialog(composite.getShell(), SWT.NONE);
                String file = dial.open();
                if (file != null) {
                    if (!file.equals("")) { //$NON-NLS-1$
                        String propertyName = (String) button.getData(PROPERTY);
                        if (!elem.getPropertyValue(propertyName).equals(file)) {
                            Command cmd = new PropertyChangeCommand(elem, propertyName, "'" + file + "'");
                            getCommandStack().execute(cmd);
                        }
                    }
                }

            }
            if (info.equals(DIRECTORY)) {
                DirectoryDialog dial = new DirectoryDialog(composite.getShell(), SWT.NONE);
                String directory = dial.open();
                if (directory != null) {
                    if (!directory.equals("")) { //$NON-NLS-1$
                        String propertyName = (String) button.getData(PROPERTY);
                        if (!elem.getPropertyValue(propertyName).equals(directory)) {
                            Command cmd = new PropertyChangeCommand(elem, propertyName, "'" + directory + "'");
                            getCommandStack().execute(cmd);
                        }
                    }
                }

            }
            // TODO SML Still in use ??
            // if (info.equals(VER_MAJ)) {
            // MessageBox mb = new MessageBox(composite.getShell(), SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
            // mb.setText(Messages.getString("DynamicTabbedPropertySection.6")); //$NON-NLS-1$
            // mb.setMessage(Messages.getString("DynamicTabbedPropertySection.7")); //$NON-NLS-1$
            // int ret = mb.open();
            //
            // if (ret == SWT.OK) {
            // Version version = (Version) elem.getPropertyValue(EParameterFieldType.VERSION.getName());
            // Version newVersion = new Version(version.getMajor(), version.getMinor());
            // newVersion.upMajor();
            // Command cmd = new PropertyChangeCommand(elem, EParameterFieldType.VERSION.getName(),
            // (Object) newVersion);
            // getCommandStack().execute(cmd);
            // }
            // }
            // if (info.equals(VER_MIN)) {
            // MessageBox mb = new MessageBox(composite.getShell(), SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
            // mb.setText(Messages.getString("DynamicTabbedPropertySection.8")); //$NON-NLS-1$
            // mb.setMessage(Messages.getString("DynamicTabbedPropertySection.9")); //$NON-NLS-1$
            // int ret = mb.open();
            //
            // if (ret == SWT.OK) {
            // Version version = (Version) elem.getPropertyValue(EParameterFieldType.VERSION.getName());
            // Version newVersion = new Version(version.getMajor(), version.getMinor());
            // newVersion.upMinor();
            // Command cmd = new PropertyChangeCommand(elem, EParameterFieldType.VERSION.getName(),
            // (Object) newVersion);
            // getCommandStack().execute(cmd);
            // }
            // }
            if (info.equals(SCHEMA)) {
                Node node;
                if (elem instanceof Node) {
                    node = (Node) elem;
                } else { // else instanceof Connection
                    node = ((Connection) elem).getSource();
                }
                IMetadataTable inputMetadata = null, inputMetaCopy = null;
                Connection inputConec = null;
                IODataComponentContainer inAndOut = new IODataComponentContainer();
                IODataComponent input = null;
                for (Connection connec : (List<Connection>) node.getIncomingConnections()) {
                    // if (connec.isActivate() && connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                    inputMetadata = connec.getMetadataTable();
                    // inputMetaCopy = inputMetadata.clone();
                    inputConec = connec;

                    input = new IODataComponent(connec);
                    inputMetaCopy = input.getTable();
                    // }
                }
                if (input != null) {
                    inAndOut.getInputs().add(input);
                }

                IMetadataTable originaleOutputTable = (IMetadataTable) node.getMetadataList().get(0);
                IMetadataTable outputMetaCopy = originaleOutputTable.clone();

                for (Connection connec : (List<Connection>) node.getOutgoingConnections()) {
                    // if (connec.isActivate() && connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                    IODataComponent dataComponent = new IODataComponent(connec, outputMetaCopy);
                    inAndOut.getOuputs().add(dataComponent);
                    // }
                }

                MetadataDialog metaDialog;
                if (inputMetadata != null) {
                    metaDialog = new MetadataDialog(composite.getShell(), inputMetaCopy, inputMetadata.getTableName(), outputMetaCopy, node
                            .getUniqueName(), getCommandStack());
                } else {
                    metaDialog = new MetadataDialog(composite.getShell(), outputMetaCopy, node.getUniqueName(), getCommandStack());
                }
                metaDialog.setText("Schema of " + node.getLabel());

                if (metaDialog.open() == MetadataDialog.OK) {
                    inputMetaCopy = metaDialog.getInputMetaData();
                    outputMetaCopy = metaDialog.getOutputMetaData();
                    boolean modified = false;
                    if (!outputMetaCopy.sameMetadataAs(originaleOutputTable)) {
                        modified = true;
                    } else {
                        if (inputMetadata != null) {
                            if (!inputMetaCopy.sameMetadataAs(inputMetadata)) {
                                modified = true;
                            }
                        }
                    }

                    if (modified) {
                        Node inputNode = null;
                        if (inputConec != null) {
                            inputNode = inputConec.getSource();
                        }
                        ChangeMetadataCommand cmd = new ChangeMetadataCommand(node, inputNode, inputMetadata, inputMetaCopy,
                                originaleOutputTable, outputMetaCopy, inAndOut);
                        getCommandStack().execute(cmd);
                        updateColumnList();
                        refresh();
                    }
                }

            }
            if (info.equals(COLUMN)) {
                updateColumnList();
            }
            if (info.equals(RESET_COLUMNS)) {
                Node node = (Node) elem;
                IMetadataTable meta = (IMetadataTable) node.getMetadataList().get(0);
                IMetadataTable metaCopy = meta.clone();
                metaCopy.setListColumns(new ArrayList<IMetadataColumn>());

                ChangeMetadataCommand cmd = new ChangeMetadataCommand(node, meta, metaCopy, null);
                getCommandStack().execute(cmd);
                updateColumnList();
            }

            // Added by ftang.
            // Used for opening a sql builder dialog.
            if (info.equals(SQLEDITOR)) {
                ConnectionParameters connParameters = new ConnectionParameters();
                String selectedComponentName = (String) elem.getPropertyValue(EParameterName.UNIQUE_NAME.getName());
                connParameters.setSelectedComponentName(selectedComponentName);
                String repositoryType = (String) elem.getPropertyValue(EParameterName.PROPERTY_TYPE.getName());
                String propertyName = (String) button.getData(PROPERTY);
                String query = (String) elem.getPropertyValue(propertyName);
                // boolean status = true;
                if (repositoryType.equals(EmfComponent.BUILTIN)) {

                    String userName = getValueFromRepositoryName("USERNAME");

                    connParameters.setUserName(userName);

                    String password = (String) getValueFromRepositoryName("PASSWORD");
                    connParameters.setPassword(password);

                    String host = (String) getValueFromRepositoryName("SERVER_NAME");
                    connParameters.setHost(host);

                    String port = (String) getValueFromRepositoryName("PORT");
                    connParameters.setPort(port);

                    String dbName = getValueFromRepositoryName("SID");
                    connParameters.setDbName(dbName);

                    String type = getRepositoryItemFromRepositoryName("TYPE");
                    connParameters.setDbType(type);
                    connParameters.setQuery(query);
                    OpenSQLBuilderDialogJob openDialogJob = new OpenSQLBuilderDialogJob(connParameters, composite, elem, propertyName,
                            getCommandStack());

                    IWorkbenchSiteProgressService siteps = (IWorkbenchSiteProgressService) part.getSite().getAdapter(
                            IWorkbenchSiteProgressService.class);
                    siteps.showInDialog(composite.getShell(), openDialogJob);
                    openDialogJob.schedule();
                    // SQLBuilderRepositoryNodeManager manager = new SQLBuilderRepositoryNodeManager();

                    // connParameters.setRepositoryNodeBuiltIn(
                    // manager.getRepositoryNodeByBuildIn(null, connParameters));
                } else if (repositoryType.equals(EmfComponent.REPOSITORY)) {
                    String repositoryName = "";
                    for (IElementParameter param : (List<IElementParameter>) elem.getElementParameters()) {
                        // System.out.println(param.toString());
                        if (param.getName().equals(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())) {
                            String value = (String) param.getValue();
                            String[] valuesList = (String[]) param.getListItemsValue();
                            String[] displayList = param.getListItemsDisplayName();
                            for (int i = 0; i < valuesList.length; i++) {
                                if (valuesList[i].equals(value)) {

                                    repositoryName = displayList[i];
                                }
                            }
                        }
                    }

                    // When no repository avaiable on "Repository" mode, open a MessageDialog.
                    if (repositoryName == null || repositoryName.length() == 0) {
                        MessageDialog.openError(composite.getShell(), Messages.getString("NoRepositoryDialog.Title"), Messages
                                .getString("NoRepositoryDialog.Text"));
                        return;
                    }
                    connParameters.setRepositoryName(repositoryName);

                    Shell parentShell = new Shell(composite.getShell().getDisplay());

                    SQLBuilderDialog dial = new SQLBuilderDialog(parentShell);
                    connParameters.setQuery(query);
                    dial.setConnParameters(connParameters);
                    if (Window.OK == dial.open()) {
                        if (!composite.isDisposed()) {
                            String sql = connParameters.getQuery();
                            Command cmd = new PropertyChangeCommand(elem, propertyName, "'" + sql + "'");
                            getCommandStack().execute(cmd);
                        }
                    }
                }

                // if (connParameters.isStatus()) {
                // Shell parentShell = new Shell(composite.getShell().getDisplay());
                //                	
                // SQLBuilderDialog dial = new SQLBuilderDialog(parentShell);
                // connParameters.setQuery(query);
                // dial.setConnParameters(connParameters);
                // if (Window.OK == dial.open()) {
                // if (!composite.isDisposed()) {
                // String sql = connParameters.getQuery();
                // Command cmd = new PropertyChangeCommand(elem, propertyName,
                // "'" + sql + "'");
                // getCommandStack().execute(cmd);
                // }
                // }
                // } else {
                // //connection failure
                // String pid = DesignerPlugin.ID;
                // String mainMsg = Messages.getString("ConnectionError.Message");
                // new ErrorDialogWidthDetailArea(composite.getShell(), pid, mainMsg,
                // connParameters.getConnectionComment());
                // }

            }// Ends
        }

        public void widgetSelected(final SelectionEvent e) {
            Set<String> elementsName;
            Control ctrl;
            String info = null;

            ctrl = (Control) e.getSource();
            if (ctrl instanceof Button) {
                Button button = (Button) ctrl;
                info = (String) button.getData(NAME);

                if (info != null) {
                    specificButtonSelected(info, button);
                }
            }
            if (info == null) {
                elementsName = hashCurControls.keySet();
                for (String name : elementsName) {
                    Object o = hashCurControls.get(name);
                    if (o instanceof Control) {
                        ctrl = (Control) o;
                        if (ctrl == null) {
                            hashCurControls.remove(name);
                            return;
                        }
                        if (ctrl.equals(e.getSource())) {
                            if (ctrl instanceof Button) {
                                // only for checkbox, other buttons must be checked before
                                if (!elem.getPropertyValue(name).equals(new Boolean(((Button) ctrl).getSelection()))) {
                                    Command cmd;
                                    Boolean value = new Boolean(((Button) ctrl).getSelection());
                                    if (name.equals(EParameterName.ACTIVATE.getName())) {
                                        cmd = new ChangeActivateStatusNodeCommand((Node) elem);
                                    } else {
                                        cmd = new PropertyChangeCommand(elem, name, value);
                                    }
                                    getCommandStack().execute(cmd);
                                }
                            }

                            if (ctrl instanceof CCombo) {
                                if (!elem.getPropertyValue(name).equals(((CCombo) ctrl).getText())) {
                                    String value = new String(""); //$NON-NLS-1$
                                    for (int i = 0; i < elem.getElementParameters().size(); i++) {
                                        IElementParameter param = elem.getElementParameters().get(i);
                                        if (param.getName().equals(name)) {
                                            for (int j = 0; j < param.getListItemsValue().length; j++) {
                                                if (((CCombo) ctrl).getText().equals(param.getListItemsDisplayName()[j])) {
                                                    value = (String) param.getListItemsValue()[j];
                                                }
                                            }
                                        }
                                    }

                                    if (name.equals(EParameterName.REPOSITORY_SCHEMA_TYPE.getName())) {
                                        updateRepositoryList();
                                        if (elem instanceof Node) {
                                            if (repositoryTableMap.containsKey(value)) {
                                                repositoryMetadata = repositoryTableMap.get(value);
                                            } else {
                                                repositoryMetadata = new MetadataTable();
                                            }
                                            Command cmd = new SchemaPropertyChangeCommand((Node) elem, name, value, repositoryMetadata);
                                            getCommandStack().execute(cmd);
                                        }
                                    } else if (name.equals(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())) {
                                        updateRepositoryList();
                                        if (repositoryConnectionItemMap.containsKey(value)) {
                                            repositoryConnection = (org.talend.core.model.metadata.builder.connection.Connection) repositoryConnectionItemMap
                                                    .get(value).getConnection();
                                        } else {
                                            repositoryConnection = null;
                                        }

                                        if (repositoryConnection != null) {
                                            Command cmd = new ChangeValuesFromRepository(elem, repositoryConnection, name, value);
                                            getCommandStack().execute(cmd);
                                        }
                                    } else if (name.equals(EParameterName.PROPERTY_TYPE.getName())) {
                                        updateRepositoryList();

                                        String connectionSelected;
                                        connectionSelected = (String) elem.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE
                                                .getName());

                                        if (repositoryConnectionItemMap.containsKey(connectionSelected)) {
                                            repositoryConnection = (org.talend.core.model.metadata.builder.connection.Connection) repositoryConnectionItemMap
                                                    .get(connectionSelected).getConnection();
                                        } else {
                                            repositoryConnection = null;
                                        }

                                        if (repositoryConnection != null) {
                                            Command cmd = new ChangeValuesFromRepository(elem, repositoryConnection, name, value);
                                            getCommandStack().execute(cmd);
                                        } else {
                                            Command cmd = new PropertyChangeCommand(elem, name, value);
                                            getCommandStack().execute(cmd);
                                        }
                                    } else if (name.equals(EParameterName.SCHEMA_TYPE.getName())) {
                                        if (elem instanceof Node) {
                                            updateRepositoryList();
                                            String schemaSelected;
                                            schemaSelected = (String) elem
                                                    .getPropertyValue(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
                                            if (repositoryTableMap.containsKey(schemaSelected)) {
                                                repositoryMetadata = repositoryTableMap.get(schemaSelected);
                                            } else {
                                                repositoryMetadata = new MetadataTable();
                                            }
                                            Command cmd = new SchemaPropertyChangeCommand((Node) elem, name, value, repositoryMetadata);
                                            getCommandStack().execute(cmd);
                                            updateColumnList();
                                        }
                                    } else {
                                        Command cmd = new PropertyChangeCommand(elem, name, value);
                                        getCommandStack().execute(cmd);
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

    };

    private void showSchemaRepositoryList(boolean show) {
        for (int i = 0; i < elem.getElementParameters().size(); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getName().equals(EParameterName.REPOSITORY_SCHEMA_TYPE.getName())) {
                param.setShow(show);
            }
        }
    }

    private void showPropertyRepositoryList(boolean show) {
        for (int i = 0; i < elem.getElementParameters().size(); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getName().equals(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())) {
                param.setShow(show);
            }
        }
    }

    private List<String> getColumnList() {
        List<String> columnList = new ArrayList<String>();

        if (elem instanceof Node) {
            Node node = (Node) elem;
            IMetadataTable table = node.getMetadataList().get(0);
            for (IMetadataColumn column : table.getListColumns()) {
                columnList.add(column.getLabel());
            }
        }

        return columnList;
    }

    private List<String> getPrevColumnList() {
        List<String> columnList = new ArrayList<String>();

        if (elem instanceof Node) {
            Node node = (Node) elem;
            IConnection connection = null;
            boolean found = false;
            for (int i = 0; i < node.getIncomingConnections().size() && !found; i++) {
                IConnection curConnec = node.getIncomingConnections().get(i);
                if (curConnec.getLineStyle() == EConnectionType.FLOW_MAIN) {
                    connection = curConnec;
                    found = true;
                }
            }
            if (connection != null) {
                IMetadataTable table = connection.getMetadataTable();
                for (IMetadataColumn column : table.getListColumns()) {
                    columnList.add(column.getLabel());
                }
            }
        }

        return columnList;
    }

    private void updateColumnList() {
        List<String> columnList = getColumnList();
        List<String> prevColumnList = getPrevColumnList();

        List<String> curColumnList = null;

        String[] columnNameList = (String[]) columnList.toArray(new String[0]);
        String[] prevColumnNameList = (String[]) prevColumnList.toArray(new String[0]);

        String[] curColumnNameList = null;

        for (int i = 0; i < elem.getElementParameters().size(); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getField() == EParameterFieldType.COLUMN_LIST) {
                curColumnList = columnList;
                curColumnNameList = columnNameList;
            }
            if (param.getField() == EParameterFieldType.PREV_COLUMN_LIST) {
                curColumnList = prevColumnList;
                curColumnNameList = prevColumnNameList;
            }
            if (param.getField() == EParameterFieldType.COLUMN_LIST || param.getField() == EParameterFieldType.PREV_COLUMN_LIST) {
                param.setListItemsDisplayName(curColumnNameList);
                param.setListItemsValue(curColumnNameList);
                CCombo combo = (CCombo) hashCurControls.get(param.getName());
                combo.setItems(curColumnNameList);
                if (!curColumnList.contains(param.getValue())) {
                    if (curColumnNameList.length > 0) {
                        elem.setPropertyValue(param.getName(), curColumnNameList[0]);
                        combo.setText(curColumnNameList[0]);
                    }
                } else {
                    combo.setText((String) param.getValue());
                }
            }
            if (param.getField() == EParameterFieldType.TABLE) {
                TableViewerCreator tableViewerCreator = (TableViewerCreator) hashCurControls.get(param.getName());
                Object[] itemsValue = (Object[]) param.getListItemsValue();
                List colList = tableViewerCreator.getColumns();
                for (int j = 0; j < itemsValue.length; j++) {
                    if (itemsValue[j] instanceof IElementParameter) {
                        IElementParameter tmpParam = (IElementParameter) itemsValue[j];
                        if (tmpParam.getField() == EParameterFieldType.COLUMN_LIST) {
                            curColumnList = columnList;
                            curColumnNameList = columnNameList;
                        }
                        if (tmpParam.getField() == EParameterFieldType.PREV_COLUMN_LIST) {
                            curColumnList = prevColumnList;
                            curColumnNameList = prevColumnNameList;
                        }
                        if (tmpParam.getField() == EParameterFieldType.COLUMN_LIST
                                || tmpParam.getField() == EParameterFieldType.PREV_COLUMN_LIST) {
                            tmpParam.setListItemsDisplayCodeName(curColumnNameList);
                            tmpParam.setListItemsDisplayName(curColumnNameList);
                            tmpParam.setListItemsValue(curColumnNameList);
                            if (curColumnNameList.length > 0) {
                                tmpParam.setDefaultClosedListValue(curColumnNameList[0]);
                            } else {
                                tmpParam.setDefaultClosedListValue("");
                            }
                            TableViewerCreatorColumn column = (TableViewerCreatorColumn) colList.get(j);

                            CCombo combo = (CCombo) column.getCellEditor().getControl();
                            String[] oldItems = combo.getItems();
                            combo.setItems(curColumnNameList);

                            List<Map<String, Object>> paramValues = (List<Map<String, Object>>) param.getValue();
                            String[] items = param.getListItemsDisplayCodeName();

                            for (int currentIndex = 0; currentIndex < paramValues.size(); currentIndex++) {
                                Map<String, Object> currentLine = paramValues.get(currentIndex);
                                Object o = currentLine.get(items[j]);
                                if (o instanceof Integer) {
                                    Integer nb = (Integer) o;
                                    if ((nb >= oldItems.length) || (nb == -1)) {
                                        nb = new Integer(tmpParam.getIndexOfItemFromList((String) tmpParam.getDefaultClosedListValue()));
                                        currentLine.put(items[j], nb);
                                    } else {
                                        nb = new Integer(tmpParam.getIndexOfItemFromList(oldItems[nb]));
                                        currentLine.put(items[j], nb);
                                    }
                                }
                            }
                        }
                    }
                }
                if (param.isBasedOnSchema()) {
                    List<Map<String, Object>> paramValues = (List<Map<String, Object>>) param.getValue();
                    List<Map<String, Object>> newParamValues = new ArrayList<Map<String, Object>>();
                    for (int j = 0; j < columnNameList.length; j++) {
                        String columnName = columnNameList[j];
                        String[] codes = param.getListItemsDisplayCodeName();

                        Map<String, Object> newLine = null;
                        boolean found = false;
                        for (int k = 0; k < paramValues.size() && !found; k++) {
                            Map<String, Object> currentLine = (Map<String, Object>) paramValues.get(k);
                            if (currentLine.get(codes[0]).equals(columnName)) {
                                found = true;
                                newLine = currentLine;
                            }
                        }
                        if (!found) {
                            newLine = createNewLine(param);
                            newLine.put(codes[0], columnName);
                        }
                        newParamValues.add(j, newLine);
                    }
                    paramValues.clear();
                    paramValues.addAll(newParamValues);
                }

                tableViewerCreator.getTableViewer().refresh();
            }
        }
    }

    private void updateProcessList() {
        List<String> processNameList = new ArrayList<String>();
        List<String> processValueList = new ArrayList<String>();
        processMap = new HashMap<String, IRepositoryObject>();
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        try {
            RootContainer<String, IRepositoryObject> processContainer = factory.getProcess();
            ContentList<String, IRepositoryObject> processAbsoluteMembers = processContainer.getAbsoluteMembers();

            for (Content<String, IRepositoryObject> object : processAbsoluteMembers.values()) {
                IRepositoryObject process = (IRepositoryObject) object.getContent();
                if (factory.getStatus(process) != ERepositoryStatus.DELETED) {
                    String path = object.getParent().getPath().toString();
                    String name;
                    if (path.equals("")) {
                        name = IPath.SEPARATOR + process.getLabel();
                    } else {
                        name = IPath.SEPARATOR + path + IPath.SEPARATOR + process.getLabel();
                    }

                    processNameList.add(name);
                    processMap.put(name, process);
                    processValueList.add("'" + process.getLabel() + "'");
                }
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        String[] processTableNameList = (String[]) processNameList.toArray(new String[0]);
        String[] processTableValueList = (String[]) processValueList.toArray(new String[0]);

        for (int i = 0; i < elem.getElementParameters().size(); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getName().equals(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
                param.setListItemsDisplayName(processTableNameList);
                param.setListItemsValue(processTableValueList);
                if (param.getValue().equals("")) {
                    if (!processMap.keySet().contains(param.getValue())) {
                        if (processTableNameList.length > 0) {
                            elem.setPropertyValue(EParameterName.PROCESS_TYPE_PROCESS.getName(), processTableValueList[0]);
                        }
                    }
                }
            }
        }
    }

    private void updateContextList() {
        List<String> contextNameList = new ArrayList<String>();
        List<String> contextValueList = new ArrayList<String>();
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        String selectedProcess = null;

        for (int i = 0; (i < elem.getElementParameters().size()) && (selectedProcess == null); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getName().equals(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
                selectedProcess = (String) param.getValue();
            }
        }

        try {
            List<IRepositoryObject> list = factory.getAll(ERepositoryObjectType.PROCESS);

            for (IRepositoryObject process : list) {
                String id = "'" + process.getLabel() + "'";
                if (selectedProcess.equals(id)) {
                    if (process.getProperty().getItem() instanceof ProcessItem) {
                        ProcessItem processItem = (ProcessItem) process.getProperty().getItem();
                        IContextManager contextManager = ContextManager
                                .getContextManagerFromXmlProcess(processItem);
                        for (IContext context : contextManager.getListContext()) {
                            contextNameList.add(context.getName());
                            contextValueList.add("'" + context.getName() + "'");
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        String[] contextTableNameList = (String[]) contextNameList.toArray(new String[0]);
        String[] contextTableValueList = (String[]) contextValueList.toArray(new String[0]);

        for (int i = 0; i < elem.getElementParameters().size(); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getName().equals(EParameterName.PROCESS_TYPE_CONTEXT.getName())) {
                param.setListItemsDisplayName(contextTableNameList);
                param.setListItemsValue(contextTableValueList);
                if (!contextValueList.contains(param.getValue())) {
                    if (contextTableNameList.length > 0) {
                        elem.setPropertyValue(EParameterName.PROCESS_TYPE_CONTEXT.getName(), contextTableValueList[0]);
                    }
                }
            }
        }

    }

    private void updateRepositoryList() {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        List<ConnectionItem> metadataConnectionsItem = null;
        String[] repositoryTableNameList = new String[] {};
        String[] repositoryTableValueList = new String[] {};
        String[] repositoryConnectionNameList = new String[] {};
        String[] repositoryConnectionValueList = new String[] {};
        try {
            metadataConnectionsItem = factory.getMetadataConnectionsItem();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
        if (metadataConnectionsItem != null) {
            repositoryTableMap.clear();
            repositoryConnectionItemMap.clear();
            List<String> tableNamesList = new ArrayList<String>();
            List<String> tableValuesList = new ArrayList<String>();
            for (ConnectionItem connectionItem : metadataConnectionsItem) {
                org.talend.core.model.metadata.builder.connection.Connection connection = (org.talend.core.model.metadata.builder.connection.Connection) connectionItem
                        .getConnection();
                if (!connection.isReadOnly()) {
                    repositoryConnectionItemMap.put(connectionItem.getProperty().getId() + "", connectionItem);
                    for (Object tableObj : connection.getTables()) {
                        org.talend.core.model.metadata.builder.connection.MetadataTable table;
                        table = (org.talend.core.model.metadata.builder.connection.MetadataTable) tableObj;
                        String name = connectionItem.getProperty().getLabel() + " - " + table.getLabel();
                        String value = connectionItem.getProperty().getId() + " - " + table.getLabel();
                        repositoryTableMap.put(value, ConvertionHelper.convert(table));
                        tableNamesList.add(name);
                        tableValuesList.add(value);
                    }
                }
            }
            repositoryTableNameList = (String[]) tableNamesList.toArray(new String[0]);
            repositoryTableValueList = (String[]) tableValuesList.toArray(new String[0]);
        }

        for (int i = 0; i < elem.getElementParameters().size(); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getName().equals(EParameterName.REPOSITORY_SCHEMA_TYPE.getName())) {

                param.setListItemsDisplayName(repositoryTableNameList);
                param.setListItemsValue(repositoryTableValueList);
                if (!repositoryTableMap.keySet().contains(param.getValue())) {
                    if (repositoryTableNameList.length > 0) {
                        elem.setPropertyValue(EParameterName.REPOSITORY_SCHEMA_TYPE.getName(), repositoryTableValueList[0]);
                    }
                }
            }
            if (param.getName().equals(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())) {

                String repositoryValue = elem.getElementParameter(EParameterName.PROPERTY_TYPE.getName()).getRepositoryValue();
                if (repositoryValue != null) {
                    List<String> connectionNamesList = new ArrayList<String>();
                    List<String> connectionValuesList = new ArrayList<String>();
                    for (String key : repositoryConnectionItemMap.keySet()) {
                        ConnectionItem connectionItem = repositoryConnectionItemMap.get(key);
                        org.talend.core.model.metadata.builder.connection.Connection connection = (org.talend.core.model.metadata.builder.connection.Connection) connectionItem
                                .getConnection();
                        if ((connection instanceof DelimitedFileConnection) && (repositoryValue.equals("DELIMITED"))) {
                            connectionNamesList.add(connectionItem.getProperty().getLabel());
                            connectionValuesList.add(key);
                        }
                        if ((connection instanceof PositionalFileConnection) && (repositoryValue.equals("POSITIONAL"))) {
                            connectionNamesList.add(connectionItem.getProperty().getLabel());
                            connectionValuesList.add(key);
                        }
                        if ((connection instanceof RegexpFileConnection) && (repositoryValue.equals("REGEX"))) {
                            connectionNamesList.add(connectionItem.getProperty().getLabel());
                            connectionValuesList.add(key);
                        }
                        if ((connection instanceof XmlFileConnection) && (repositoryValue.equals("XML"))) {
                            connectionNamesList.add(connectionItem.getProperty().getLabel());
                            connectionValuesList.add(key);
                        }

                        if ((connection instanceof DatabaseConnection) && (repositoryValue.equals("DATABASE"))) {
                            connectionNamesList.add(connectionItem.getProperty().getLabel());
                            connectionValuesList.add(key);
                        }

                    }
                    repositoryConnectionNameList = (String[]) connectionNamesList.toArray(new String[0]);
                    repositoryConnectionValueList = (String[]) connectionValuesList.toArray(new String[0]);
                } else {
                    List<String> connectionValuesList = new ArrayList<String>();
                    List<String> connectionStringList = new ArrayList<String>();
                    for (String key : repositoryConnectionItemMap.keySet()) {
                        ConnectionItem connectionItem = repositoryConnectionItemMap.get(key);
                        connectionStringList.add(connectionItem.getProperty().getLabel());
                        connectionValuesList.add(key);
                    }
                    repositoryConnectionNameList = (String[]) connectionStringList.toArray(new String[0]);
                    repositoryConnectionValueList = (String[]) connectionValuesList.toArray(new String[0]);
                }

                param.setListItemsDisplayName(repositoryConnectionNameList);
                param.setListItemsValue(repositoryConnectionValueList);
                if (!repositoryConnectionItemMap.keySet().contains(param.getValue())) {
                    if (repositoryConnectionNameList.length > 0) {
                        elem.setPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), repositoryConnectionValueList[0]);
                    }
                }
            }
        }
    }

    public Element getElement() {
        return elem;
    }

    private void addDragAndDropTarget(final Text labelText) {
        DropTargetListener dropTargetListener = new DropTargetListener() {

            String propertyName = null;

            public void dragEnter(final DropTargetEvent event) {
            }

            public void dragLeave(final DropTargetEvent event) {
            }

            public void dragOperationChanged(final DropTargetEvent event) {
            }

            public void dragOver(final DropTargetEvent event) {
                if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    propertyName = getParameterName(labelText);
                    for (int i = 0; i < elem.getElementParameters().size(); i++) {
                        IElementParameter param = elem.getElementParameters().get(i);
                        if (param.getName().equals(propertyName)) {
                            if (param.isReadOnly()) {
                                event.detail = DND.ERROR_INVALID_DATA;
                            }
                        }
                    }
                }
            }

            public void drop(final DropTargetEvent event) {
                if (propertyName != null) {
                    String text = labelText.getText() + (String) event.data;
                    Command cmd = new PropertyChangeCommand(elem, propertyName, (Object) text);
                    getCommandStack().execute(cmd);
                }
            }

            public void dropAccept(final DropTargetEvent event) {
            }
        };

        DropTarget target = new DropTarget(labelText, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
        Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };
        target.setTransfer(transfers);
        target.addDropListener(dropTargetListener);

    }

    private Control addVersion(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl) {
        Button btnUp, btnDown;
        Text labelText;

        btnUp = getWidgetFactory().createButton(subComposite, "M", SWT.PUSH); //$NON-NLS-1$
        btnUp.setData(NAME, VER_MAJ);
        btnUp.setEnabled(!param.isReadOnly());
        btnUp.addSelectionListener(listenerSelection);
        Point btnUpSize = btnUp.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        btnDown = getWidgetFactory().createButton(subComposite, "m", SWT.PUSH); //$NON-NLS-1$
        Point btnDownSize = btnDown.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point btnSize = new Point(Math.max(btnUpSize.x, btnDownSize.x), Math.max(btnUpSize.y, btnDownSize.y));

        FormData data = new FormData();
        data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -btnSize.x);
        data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), 0);
        data.top = new FormAttachment(0, top);
        data.height = btnSize.y;
        btnDown.setLayoutData(data);
        btnDown.setData(NAME, VER_MIN);
        btnDown.setEnabled(!param.isReadOnly());
        btnDown.addSelectionListener(listenerSelection);

        data = new FormData();
        data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -((btnSize.x * 2) + ITabbedPropertyConstants.HSPACE));
        data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -(btnSize.x + ITabbedPropertyConstants.HSPACE));
        data.top = new FormAttachment(0, top);
        data.height = btnSize.y;
        btnUp.setLayoutData(data);

        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new TextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }

        Control cLayout = dField.getLayoutControl();

        labelText = (Text) dField.getControl();
        cLayout.setBackground(subComposite.getBackground());
        labelText.setEditable(false);
        if (elem instanceof Node) {
            labelText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName()); //$NON-NLS-1$
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }

        // *********************
        data = new FormData();
        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, STANDARD_LABEL_WIDTH);
            } else {
                data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), STANDARD_LABEL_WIDTH);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment(btnUp, 0);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), labelText);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (btnSize.y > initialSize.y) {
            curRowSize = btnSize.y + ITabbedPropertyConstants.VSPACE;
        } else {
            curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;
        }
        return btnDown;
    }

    private Control addDirectory(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl) {
        Button btnEdit;
        Text labelText;

        btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        FormData data;

        btnEdit.setImage(CorePlugin.getImageDescriptor(DOTS_BUTTON).createImage());

        data = new FormData();
        data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -STANDARD_BUTTON_WIDTH);
        data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), 0);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT - 2;
        btnEdit.setLayoutData(data);
        btnEdit.setData(NAME, DIRECTORY);
        btnEdit.setData(PROPERTY, param.getName());
        btnEdit.setEnabled(!param.isReadOnly());
        btnEdit.addSelectionListener(listenerSelection);

        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new TextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }

        Control cLayout = dField.getLayoutControl();
        labelText = (Text) dField.getControl();

        cLayout.setBackground(subComposite.getBackground());
        labelText.setEditable(!param.isReadOnly());

        editionControlHelper.register(param.getName(), labelText, true);

        addDragAndDropTarget(labelText);
        if (elem instanceof Node) {
            labelText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName()); //$NON-NLS-1$
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // **************************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }
        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment(btnEdit, 0);
        data.top = new FormAttachment(btnEdit, 0, SWT.CENTER);
        cLayout.setLayoutData(data);

        hashCurControls.put(param.getName(), labelText);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;

        return btnEdit;
    }

    private Control addFile(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl) {
        Button btnEdit;
        Text labelText;

        btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        FormData data;

        btnEdit.setImage(CorePlugin.getImageDescriptor(DOTS_BUTTON).createImage());

        data = new FormData();
        data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -STANDARD_BUTTON_WIDTH);
        data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), 0);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT - 2;
        btnEdit.setLayoutData(data);
        btnEdit.setData(NAME, FILE);
        btnEdit.setData(PROPERTY, param.getName());
        btnEdit.setEnabled(!param.isReadOnly());
        btnEdit.addSelectionListener(listenerSelection);

        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new TextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription("Content taken from the repository");
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        labelText = (Text) dField.getControl();
        cLayout.setBackground(subComposite.getBackground());
        labelText.setEditable(!param.isReadOnly());

        editionControlHelper.register(param.getName(), labelText, true);

        addDragAndDropTarget(labelText);
        if (elem instanceof Node) {
            labelText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        hashCurControls.put(param.getName(), labelText);

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName()); //$NON-NLS-1$
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // **************************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }
        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment(btnEdit, 0);
        data.top = new FormAttachment(btnEdit, 0, SWT.CENTER);
        cLayout.setLayoutData(data);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;

        return btnEdit;
    }

    private Control addExternal(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl) {
        Button btnEdit;

        btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$

        btnEdit.setImage(CorePlugin.getImageDescriptor(DOTS_BUTTON).createImage());
        FormData data;
        btnEdit.setData(NAME, EXTERNAL);
        btnEdit.setData(PROPERTY, param.getName());
        btnEdit.setEnabled(!param.isReadOnly());
        btnEdit.addSelectionListener(listenerSelection);
        if (elem instanceof Node) {
            btnEdit.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName()); //$NON-NLS-1$
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // **************************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
                data.right = new FormAttachment(lastControl, currentLabelWidth + STANDARD_BUTTON_WIDTH);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
                data.right = new FormAttachment(0, currentLabelWidth + STANDARD_BUTTON_WIDTH);
            }
        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
            data.right = new FormAttachment(labelLabel, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        btnEdit.setLayoutData(data);
        // **************************
        hashCurControls.put(param.getName(), btnEdit);

        Point initialSize = btnEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;
        return btnEdit;
    }

    protected Control addSchemaType(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl) {

        FormData data;
        Control lastControlUsed;
        Button resetBtn = null;

        Point btnSize;

        Button btn;
        btn = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnSize = btn.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        btn.setImage(CorePlugin.getImageDescriptor(DOTS_BUTTON).createImage());

        btn.addSelectionListener(listenerSelection);
        btn.setData(NAME, SCHEMA);
        btn.setData(PROPERTY, param.getName());
        btn.setEnabled(!param.isReadOnly());

        lastControlUsed = btn;

        if (elem instanceof Node) {
            Node node = (Node) elem;
            boolean flowMainInput = false;
            for (IConnection connec : node.getIncomingConnections()) {
                if (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                    flowMainInput = true;
                }
            }
            if (flowMainInput) {
                resetBtn = getWidgetFactory().createButton(subComposite, "Sync columns", SWT.PUSH);
                resetBtn.setToolTipText("This will take automatically the columns of the previous component");

                Point resetBtnSize = resetBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT);

                resetBtn.addSelectionListener(listenerSelection);
                data = new FormData();
                data.left = new FormAttachment(btn, 0);
                data.right = new FormAttachment(btn, resetBtnSize.x + ITabbedPropertyConstants.HSPACE, SWT.RIGHT);
                data.top = new FormAttachment(0, top);
                data.height = resetBtnSize.y;
                resetBtn.setLayoutData(data);
                resetBtn.setData(NAME, RESET_COLUMNS);
                resetBtn.setData(PROPERTY, param.getName());
                resetBtn.setEnabled(!param.isReadOnly());

                if (resetBtnSize.y > btnSize.y) {
                    btnSize.y = resetBtnSize.y;
                }

                lastControlUsed = btn;
            }
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, "Edit schema");
        data = new FormData();
        data.left = new FormAttachment(lastControl, 0);
        data.right = new FormAttachment(lastControl, labelLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x
                + (ITabbedPropertyConstants.HSPACE * 2), SWT.RIGHT);
        if (resetBtn != null) {
            data.top = new FormAttachment(resetBtn, 0, SWT.CENTER);
        } else {
            data.top = new FormAttachment(0, top);
        }
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }

        data = new FormData();
        data.left = new FormAttachment(labelLabel, 0);
        data.right = new FormAttachment(labelLabel, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        if (resetBtn != null) {
            data.top = new FormAttachment(resetBtn, 0, SWT.CENTER);
        } else {
            data.top = new FormAttachment(0, top);
        }
        data.height = STANDARD_HEIGHT - 2;
        btn.setLayoutData(data);

        curRowSize = btnSize.y + ITabbedPropertyConstants.VSPACE;
        return lastControlUsed;
    }

    private Control addCheck(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl) {
        Button checkBtn;

        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new IControlCreator() {

            public Control createControl(Composite parent, int style) {
                return getWidgetFactory().createButton(parent, param.getDisplayName(), SWT.CHECK);
            }

        });
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription("Value taken from the repository");
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        cLayout.setBackground(subComposite.getBackground());
        checkBtn = (Button) dField.getControl();

        FormData data = new FormData();
        data.top = new FormAttachment(0, top);
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        cLayout.setLayoutData(data);
        hashCurControls.put(param.getName(), checkBtn);
        checkBtn.setEnabled(!param.isReadOnly());
        checkBtn.addSelectionListener(listenerSelection);
        if (elem instanceof Node) {
            checkBtn.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        Point initialSize = checkBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;
        return cLayout;
    }

    private Control addImage(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl) {

        String fileName = (String) param.getValue();
        String filePath = RepositoryPathProvider.getPathFileName(RepositoryConstants.IMG_DIRECTORY, fileName).toPortableString();

        if (filePath != null) {
            File fileOrFolder = new java.io.File(filePath);
            if (!fileOrFolder.isFile() || !fileOrFolder.canRead()) {
                return lastControl;
            }
            final Composite compositeImage = new Composite(subComposite, SWT.BORDER);
            final Image image = new Image(subComposite.getDisplay(), filePath);
            compositeImage.addDisposeListener(new DisposeListener() {

                public void widgetDisposed(DisposeEvent e) {
                    image.dispose();
                    compositeImage.removeDisposeListener(this);
                }

            });

            CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName() + " :");
            FormData formDataLabel = new FormData();
            labelLabel.setVisible(true);
            if (numInRow != 1) {
                labelLabel.setAlignment(SWT.RIGHT);
            }
            if (lastControl != null) {
                formDataLabel.left = new FormAttachment(lastControl, 0);
            } else {
                formDataLabel.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
                formDataLabel.top = new FormAttachment(0, top);
            }
            labelLabel.setLayoutData(formDataLabel);

            compositeImage.setToolTipText(param.getDisplayName());
            Point size = new Point(image.getImageData().width, image.getImageData().height);
            FormData formData = new FormData(size.x, size.y);
            formData.top = new FormAttachment(0, top);
            formData.left = new FormAttachment(labelLabel);
            compositeImage.setBackgroundImage(image);
            compositeImage.setLayoutData(formData);
            return compositeImage;
        } else {
            return lastControl;
        }
    }

    private Control addText(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl) {
        final Text labelText;
        FormData data;

        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new TextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription("Value taken from the repository");
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }
        Control cLayout = dField.getLayoutControl();
        labelText = (Text) dField.getControl();

        editionControlHelper.register(param.getName(), labelText, true);

        cLayout.setBackground(subComposite.getBackground());
        labelText.setEditable(!param.isReadOnly());
        if (elem instanceof Node) {
            labelText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }
        addDragAndDropTarget(labelText);

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        Point labelSize = new GC(labelLabel).stringExtent(param.getDisplayName());

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************

        hashCurControls.put(param.getName(), labelText);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;
        return null;
    }

    private Control addCombo(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl) {
        CCombo combo;

        IControlCreator cbCtrl = new IControlCreator() {

            public Control createControl(final Composite parent, final int style) {
                CCombo cb = new CCombo(parent, style);
                return cb;
            }
        };
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription("Value taken from the repository");
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        combo = (CCombo) dField.getControl();
        FormData data;
        String[] originalList = param.getListItemsDisplayName();
        List<String> stringToDisplay = new ArrayList<String>();
        String[] itemsShowIf = param.getListItemsShowIf();
        if (itemsShowIf != null) {
            String[] itemsNotShowIf = param.getListItemsNotShowIf();
            for (int i = 0; i < originalList.length; i++) {
                if (param.isShow(itemsShowIf[i], itemsNotShowIf[i], elem.getElementParameters())) {
                    stringToDisplay.add(originalList[i]);
                }
            }
        } else {
            for (int i = 0; i < originalList.length; i++) {
                stringToDisplay.add(originalList[i]);
            }
        }
        combo.setItems(stringToDisplay.toArray(new String[0]));
        combo.setEditable(false);
        cLayout.setBackground(subComposite.getBackground());
        combo.setEnabled(!param.isReadOnly());
        combo.addSelectionListener(listenerSelection);
        if (elem instanceof Node) {
            combo.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), combo);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;
        return cLayout;
    }

    private Control addColumnList(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl) {
        CCombo combo;

        if (param.getField() == EParameterFieldType.COLUMN_LIST) {
            param.setDisplayName(EParameterName.COLUMN_LIST.getDisplayName());
        } else {
            param.setDisplayName(EParameterName.PREV_COLUMN_LIST.getDisplayName());
        }

        Button refreshBtn;
        refreshBtn = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        refreshBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        refreshBtn.setImage(CorePlugin.getImageDescriptor(REFRESH_BUTTON).createImage());

        refreshBtn.addSelectionListener(listenerSelection);
        refreshBtn.setData(NAME, COLUMN);
        refreshBtn.setData(PROPERTY, param.getName());
        refreshBtn.setEnabled(!param.isReadOnly());

        IControlCreator cbCtrl = new IControlCreator() {

            public Control createControl(final Composite parent, final int style) {
                CCombo cb = new CCombo(parent, style);
                return cb;
            }
        };
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }

        Control cLayout = dField.getLayoutControl();
        combo = (CCombo) dField.getControl();
        FormData data;
        combo.setEditable(false);
        cLayout.setBackground(subComposite.getBackground());
        combo.setEnabled(!param.isReadOnly());
        combo.addSelectionListener(listenerSelection);
        if (elem instanceof Node) {
            combo.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        data = new FormData();
        data.left = new FormAttachment(cLayout, 0, SWT.RIGHT);
        data.top = new FormAttachment(0, top);
        data.height = initialSize.y;

        refreshBtn.setLayoutData(data);

        // **********************
        hashCurControls.put(param.getName(), combo);
        updateColumnList();

        curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;
        return refreshBtn;
    }

    private Control addMemo(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl) {
        Text text;
        int nbLines = param.getNbLines();

        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
                new TextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        Control cLayout = dField.getLayoutControl();
        text = (Text) dField.getControl();

        editionControlHelper.register(param.getName(), text, true);

        FormData d = (FormData) text.getLayoutData();
        d.height = text.getLineHeight() * nbLines;
        FormData data;
        text.getParent().setSize(subComposite.getSize().x, text.getLineHeight() * nbLines);
        cLayout.setBackground(subComposite.getBackground());
        text.setEnabled(!param.isReadOnly());
        Font font = new Font(subComposite.getDisplay(), "courier", 8, SWT.NONE);
        text.setFont(font);
        if (elem instanceof Node) {
            text.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), text);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;
        return null;
    }

    private Control addLanguageMemo(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl, final String language) {

        ColorStyledText text;
        int nbLines = param.getNbLines();

        IControlCreator txtCtrl = new IControlCreator() {

            public Control createControl(final Composite parent, final int style) {
                ColorManager colorManager = new ColorManager(CorePlugin.getDefault().getPreferenceStore());
                ColorStyledText colorText = new ColorStyledText(parent, style, colorManager, language);
                Font font = new Font(parent.getDisplay(), "courier", 8, SWT.NONE);
                colorText.setFont(font);
                return colorText;
            }
        };
        DecoratedField dField = null;
        if (param.getNbLines() != 1) {
            dField = new DecoratedField(subComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, txtCtrl);
        } else {
            dField = new DecoratedField(subComposite, SWT.BORDER, txtCtrl);
        }
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        Control cLayout = dField.getLayoutControl();
        text = (ColorStyledText) dField.getControl();

        editionControlHelper.register(param.getName(), text, true);

        FormData d = (FormData) text.getLayoutData();
        d.height = text.getLineHeight() * nbLines;
        FormData data;
        text.getParent().setSize(subComposite.getSize().x, text.getLineHeight() * nbLines);
        cLayout.setBackground(subComposite.getBackground());
        text.setEnabled(!param.isReadOnly());
        if (elem instanceof Node) {
            text.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), text);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;
        return null;
    }

    /**
     * 
     * Adds a button for opening an sql builder dialog, by ftang.
     * 
     * @param subComposite
     * @param param
     * @param numInRow
     * @param nbInRow
     * @param top
     * @param lastControl
     * @param language
     * @return
     */
    private Control addSqlMemo(final Composite subComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControl, final String language) {

        final Button openSQLEditorButton;
        final DecoratedField dField1 = new DecoratedField(subComposite, SWT.PUSH, new IControlCreator() {

            public Control createControl(Composite parent, int style) {
                return new Button(parent, style);
            }
        });

        Control buttonControl = dField1.getLayoutControl();
        openSQLEditorButton = (Button) dField1.getControl();
        openSQLEditorButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        openSQLEditorButton.setImage(CorePlugin.getImageDescriptor(DOTS_BUTTON).createImage());
        buttonControl.setBackground(subComposite.getBackground());
        openSQLEditorButton.setEnabled(!param.isReadOnly());
        openSQLEditorButton.setData(NAME, SQLEDITOR);
        openSQLEditorButton.setData(PROPERTY, param.getName());

        openSQLEditorButton.addSelectionListener(listenerSelection);

        FormData data1 = new FormData();
        data1.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
        data1.left = new FormAttachment(100, -(ITabbedPropertyConstants.HSPACE + STANDARD_BUTTON_WIDTH));
        data1.top = new FormAttachment(0, top);

        buttonControl.setLayoutData(data1);

        int nbLines = param.getNbLines();

        IControlCreator txtCtrl = new IControlCreator() {

            public Control createControl(final Composite parent, final int style) {
                ColorManager colorManager = new ColorManager(CorePlugin.getDefault().getPreferenceStore());
                ColorStyledText colorText = new ColorStyledText(parent, style, colorManager, language);
                Font font = new Font(parent.getDisplay(), "courier", 8, SWT.NONE);
                colorText.setFont(font);
                return colorText;
            }
        };
        DecoratedField dField = null;
        if (param.getNbLines() != 1) {
            dField = new DecoratedField(subComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, txtCtrl);
        } else {
            dField = new DecoratedField(subComposite, SWT.BORDER, txtCtrl);
        }
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        Control cLayout = dField.getLayoutControl();
        ColorStyledText queryText = (ColorStyledText) dField.getControl();

        editionControlHelper.register(param.getName(), queryText, true);

        FormData d = (FormData) queryText.getLayoutData();
        d.height = queryText.getLineHeight() * nbLines;
        FormData data;
        queryText.getParent().setSize(subComposite.getSize().x, queryText.getLineHeight() * nbLines);
        cLayout.setBackground(subComposite.getBackground());
        queryText.setEnabled(!param.isReadOnly());
        if (elem instanceof Node) {
            queryText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment(buttonControl, -5, SWT.LEFT);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), queryText);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;

        return null;
    }// Ends

    @SuppressWarnings("unchecked")
    private Control addTable(final Composite parentComposite, final IElementParameter param, final int numInRow, final int nbInRow,
            final int top, final Control lastControlPrm) {

//        Composite container = parentComposite;// new Composite(subComposite, SWT.NONE);
//
//        PropertiesTableEditorModel<Map<String, Object>> tableEditorModel = new PropertiesTableEditorModel<Map<String, Object>>();
//        tableEditorModel.setData(elem, param, part.getTalendEditor().getProcess(), getColumnList(), getPrevColumnList());
//
//        PropertiesTableEditorView<Map<String, Object>> tableEditorView = new PropertiesTableEditorView<Map<String, Object>>(
//                parentComposite, SWT.NONE, tableEditorModel);
//        // PropertiesTableEditorView tableEditorView = new PropertiesTableEditorView(parentComposite, SWT.NONE);
//        tableEditorView.getExtendedTableViewer().setCommandStack(getCommandStack());
//
//        Composite mainComposite = tableEditorView.getMainComposite();
//
//        List<Map<String, Object>> values = copyTableValue(param);
//        tableEditorModel.registerDataList(values);
//
//        FormData formData = new FormData();
//
//        // formData.left = new FormAttachment(lastControlPrm);
//        // formData.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
//        // formData.right = new FormAttachment(lastControlPrm);
//        // formData.top = new FormAttachment(lastControlPrm);
//        // int ySize2 = tableEditor.getHeaderHeight() + (param.getNbLines() * tableEditor.getItemHeight()) +
//        // (tableEditor.getItemHeight() / 2);
//
//        if (lastControlPrm != null) {
//            formData.left = new FormAttachment(lastControlPrm, 0);
//        } else {
//            formData.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
//        }
//        formData.top = new FormAttachment(0, top);
//        // formData.bottom = new FormAttachment(0, top + ySize2);
//        mainComposite.setLayoutData(formData);
//        // curRowSize = ySize2 + ITabbedPropertyConstants.VSPACE;
//
//        final Control lastControl = mainComposite;

        final Control lastControl = lastControlPrm;
        final Composite container = parentComposite;
        if (true) {
            boolean dynamicData = false;

            final TableViewerCreator tableViewerCreator = new TableViewerCreator(container);
            tableViewerCreator.setBorderVisible(true);
            tableViewerCreator.setCheckboxInFirstColumn(false);
            tableViewerCreator.setAllColumnsResizable(true);
            tableViewerCreator.setLayoutMode(LAYOUT_MODE.DEFAULT);
            final Table table = tableViewerCreator.createTable();
            table.setEnabled(!param.isReadOnly());
            if (elem instanceof Node) {
                table.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
            }

            String[] titles = param.getListItemsDisplayName();
            final String[] items = (String[]) param.getListItemsDisplayCodeName();
            final Object[] itemsValue = (Object[]) param.getListItemsValue();
            String[] itemsShowIf = (String[]) param.getListItemsShowIf();
            String[] itemsNotShowIf = (String[]) param.getListItemsNotShowIf();

            List<String> columnList = getColumnList();
            String[] columnArrayList = columnList.toArray(new String[0]);
            List<String> prevColumnList = getPrevColumnList();
            String[] prevColumnArrayList = prevColumnList.toArray(new String[0]);

            if (param.isBasedOnSchema()) {
                dynamicData = true;
            }

            for (int i = 0; i < itemsValue.length; i++) {
                if (itemsValue[i] instanceof IElementParameter) {
                    IElementParameter tmpParam = (IElementParameter) itemsValue[i];
                    if (tmpParam.getField() == EParameterFieldType.COLUMN_LIST) {
                        dynamicData = true;
                        tmpParam.setListItemsDisplayCodeName(columnArrayList);
                        tmpParam.setListItemsDisplayName(columnArrayList);
                        tmpParam.setListItemsValue(columnArrayList);
                        if (columnArrayList.length != 0) {
                            tmpParam.setDefaultClosedListValue(columnArrayList[0]);
                        } else {
                            tmpParam.setDefaultClosedListValue("");
                        }
                    }
                    if (tmpParam.getField() == EParameterFieldType.PREV_COLUMN_LIST) {
                        dynamicData = true;
                        tmpParam.setListItemsDisplayCodeName(prevColumnArrayList);
                        tmpParam.setListItemsDisplayName(prevColumnArrayList);
                        tmpParam.setListItemsValue(prevColumnArrayList);
                        if (prevColumnArrayList.length != 0) {
                            tmpParam.setDefaultClosedListValue(prevColumnArrayList[0]);
                        } else {
                            tmpParam.setDefaultClosedListValue("");
                        }
                    }
                }
            }
            // there's two lists of values, one that will be in the table
            // and the other will be stored as the current value in the property
            // there is two lists because of the undo / redo capabilities
            ProcessProposalProvider processProposalProvider = new ProcessProposalProvider(part.getTalendEditor().getProcess());
            for (int i = 0; i < titles.length; i++) {
                if (param.isShow(itemsShowIf[i], itemsNotShowIf[i], elem.getElementParameters())) {
                    TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
                    column.setTitle(titles[i]);
                    column.setModifiable(true);
                    column.setMinimumWidth(100);
                    column.setWeight(20);

                    if (itemsValue[i] instanceof IElementParameter) {
                        IElementParameter tmpParam = (IElementParameter) itemsValue[i];

                        ComboBoxCellEditor cellEditor = new ComboBoxCellEditor(table, tmpParam.getListItemsDisplayName());
                        ((CCombo) cellEditor.getControl()).setEditable(false);
                        column.setCellEditor(cellEditor, new CellEditorValueAdapter() {

                            public String getColumnText(CellEditor cellEditor, Object cellEditorValue) {
                                return (String) cellEditorValue;
                            }

                            public Object getOriginalTypedValue(CellEditor cellEditor, Object cellEditorTypedValue) {
                                if (cellEditorTypedValue instanceof Integer) {
                                    return cellEditorTypedValue;
                                }
                                return new Integer(0);
                            };

                            public Object getCellEditorTypedValue(CellEditor cellEditor, Object originalTypedValue) {
                                boolean found = false;
                                int nb = 0;

                                String[] namesSet = ((CCombo) cellEditor.getControl()).getItems();

                                for (int j = 0; j < namesSet.length && !found; j++) {
                                    if (namesSet[j].equals(originalTypedValue)) {
                                        found = true;
                                        nb = j;
                                    }
                                }
                                return new Integer(nb);
                            };
                        });
                    } else {
                        TextCellEditorWithProposal textCellEditor = new TextCellEditorWithProposal(table, column);
                        textCellEditor.setContentProposalProvider(processProposalProvider);
                        if ((i == 0) && (param.isBasedOnSchema())) {
                            Text text = (Text) textCellEditor.getControl();
                            text.setEditable(false);
                        }
                        column.setCellEditor(textCellEditor);
                    }
                    final int curCol = i;
                    column.setBeanPropertyAccessors(new IBeanPropertyAccessors<Map<String, Object>, Object>() {

                        public String get(Map<String, Object> bean) {
                            Object value = bean.get(items[curCol]);
                            if (value == null) {
                                return "";
                            }
                            if (itemsValue[curCol] instanceof IElementParameter) {
                                IElementParameter tmpParam = (IElementParameter) itemsValue[curCol];
                                String[] namesSet = tmpParam.getListItemsDisplayName();
                                if (namesSet.length == 0) {
                                    return (String) tmpParam.getDefaultClosedListValue();
                                }
                                if (value instanceof String) {
                                    boolean found = false;
                                    int index = 0;
                                    Object[] items = ((IElementParameter) itemsValue[curCol]).getListItemsValue();
                                    for (int j = 0; j < items.length && !found; j++) {
                                        if (items[j].equals(value)) {
                                            found = true;
                                            index = j;
                                        }
                                    }
                                    value = new Integer(index);
                                }
                                return namesSet[(Integer) value];
                            }
                            return (String) value;
                        }

                        public void set(Map<String, Object> bean, Object value) {
                            List<Map<String, Object>> tableValues = new ArrayList<Map<String, Object>>();
                            List<Map<String, Object>> paramValues = (List<Map<String, Object>>) param.getValue();
                            int currentBeanIndex = table.getSelectionIndex();
                            for (int currentIndex = 0; currentIndex < paramValues.size(); currentIndex++) {
                                Map<String, Object> currentLine = paramValues.get(currentIndex);

                                Map<String, Object> newLine = new HashMap<String, Object>();
                                for (int i = 0; i < items.length; i++) {
                                    newLine.put(items[i], currentLine.get(items[i]));
                                }

                                if (currentIndex == currentBeanIndex) {
                                    newLine.put(items[curCol], value);
                                }
                                tableValues.add(newLine);
                            }
                            Command cmd = new PropertyChangeCommand(elem, param.getName(), tableValues);
                            getCommandStack().execute(cmd);
                            refresh();
                        }
                    });
                }
            }
            param.setValue(copyTableValue(param));
            tableViewerCreator.init((List) param.getValue());

            final Listener addLineListener = new Listener() {

                @SuppressWarnings("unchecked")
                public void handleEvent(final Event event) {
                    List<Map<String, Object>> tableValues = copyTableValue(param);

                    Map<String, Object> line = createNewLine(param);

                    int index = table.getSelectionIndex();
                    if (index == -1) {
                        tableValues.add(line);
                        index = tableValues.size() - 1;
                    } else {
                        tableValues.add(index, line);
                    }
                    Command cmd = new PropertyChangeCommand(elem, param.getName(), tableValues);
                    getCommandStack().execute(cmd);
                    refresh();
                    table.setSelection(index);
                }
            };
            final Listener removeLineListener = new Listener() {

                public void handleEvent(final Event event) {
                    List<Map<String, Object>> tableValues = copyTableValue(param);
                    int selectionIndex = table.getSelectionIndex();
                    if (selectionIndex >= 0) {
                        tableValues.remove(selectionIndex);
                        Command cmd = new PropertyChangeCommand(elem, param.getName(), tableValues);
                        getCommandStack().execute(cmd);
                        refresh();
                        if (selectionIndex >= tableValues.size()) {
                            selectionIndex--;
                        }
                        table.setSelection(selectionIndex);
                    }
                }
            };

            final Listener copyLineListener = new Listener() {

                public void handleEvent(final Event event) {
                    List<Map<String, Object>> tableValues = copyTableValue(param);
                    if (table.getSelectionIndex() >= 0) {
                        Map<String, Object> map = tableValues.get(table.getSelectionIndex());
                        if (map != null) {
                            copyToCliboard(map, param);
                        }
                    }
                }
            };
            final Listener pasteLineListener = new Listener() {

                public void handleEvent(final Event event) {
                    if (clipboard != null) {
                        List<Map<String, Object>> tableValues = copyTableValue(param);

                        int index = table.getSelectionIndex();
                        if (index == -1) {
                            tableValues.add(clipboard);
                            index = tableValues.size() - 1;
                        } else {
                            tableValues.add(index, clipboard);
                        }

                        Command cmd = new PropertyChangeCommand(elem, param.getName(), tableValues);
                        getCommandStack().execute(cmd);
                        refresh();
                        table.setSelection(index);
                    }
                }
            };
            final Listener upLineListener = new Listener() {

                public void handleEvent(final Event event) {
                    List<Map<String, Object>> tableValues = copyTableValue(param);

                    if (table.getSelectionIndex() > 0) {
                        Collections.swap(tableValues, table.getSelectionIndex(), table.getSelectionIndex() - 1);

                        Command cmd = new PropertyChangeCommand(elem, param.getName(), tableValues);
                        getCommandStack().execute(cmd);
                        refresh();
                    }
                }
            };

            final Listener downLineListener = new Listener() {

                public void handleEvent(final Event event) {
                    List<Map<String, Object>> tableValues = copyTableValue(param);

                    if (table.getSelectionIndex() < tableValues.size() - 1) {
                        Collections.swap(tableValues, table.getSelectionIndex(), table.getSelectionIndex() + 1);

                        Command cmd = new PropertyChangeCommand(elem, param.getName(), tableValues);
                        getCommandStack().execute(cmd);
                        refresh();
                    }
                }
            };

            if (!param.isReadOnly()) {
                final Menu menuTable = new Menu(table);
                table.setMenu(menuTable);
                hashCurControls.put(param.getName(), tableViewerCreator);
            }
            CLabel labelLabel = getWidgetFactory().createCLabel(container, param.getDisplayName());
            FormData data = new FormData();
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, 0);
            } else {
                data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
            }
            data.top = new FormAttachment(0, top);
            labelLabel.setLayoutData(data);
            if (numInRow != 1) {
                labelLabel.setAlignment(SWT.RIGHT);
            }
            // *********************
            data = new FormData();
            int currentLabelWidth = STANDARD_LABEL_WIDTH;
            GC gc = new GC(labelLabel);
            Point labelSize = gc.stringExtent(param.getDisplayName());
            gc.dispose();

            if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
                currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
            }
            if (numInRow == 1) {
                if (lastControl != null) {
                    data.left = new FormAttachment(lastControl, currentLabelWidth);
                } else {
                    data.left = new FormAttachment(0, currentLabelWidth);
                }
            } else {
                data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
            }
            data.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
            data.top = new FormAttachment(0, top);
            int ySize = table.getHeaderHeight() + (param.getNbLines() * table.getItemHeight()) + (table.getItemHeight() / 2);
            data.bottom = new FormAttachment(0, top + ySize);
            table.setLayoutData(data);
            curRowSize = ySize + ITabbedPropertyConstants.VSPACE;

            Button addButton;
            addButton = getWidgetFactory().createButton(container, "", SWT.PUSH); //$NON-NLS-1$
            addButton.setImage(ImageProvider.getImage(EImage.ADD_ICON));
            addButton.setToolTipText("Add");
            data = new FormData();
            data.top = new FormAttachment(table, -3);
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }
            addButton.setLayoutData(data);
            addButton.addListener(SWT.Selection, addLineListener);
            addButton.setEnabled(!param.isBasedOnSchema());
            addButton.setVisible(!param.isReadOnly());

            Button removeButton;
            removeButton = getWidgetFactory().createButton(container, "", SWT.PUSH); //$NON-NLS-1$
            removeButton.setImage(ImageProvider.getImage(EImage.DELETE_ICON));
            removeButton.setToolTipText("Remove");
            data = new FormData();
            data.top = new FormAttachment(table, -3);
            data.left = new FormAttachment(addButton, -3);
            removeButton.setLayoutData(data);
            removeButton.addListener(SWT.Selection, removeLineListener);
            removeButton.setEnabled(!param.isBasedOnSchema());
            removeButton.setVisible(!param.isReadOnly());

            Button copyButton;
            copyButton = getWidgetFactory().createButton(container, "", SWT.PUSH); //$NON-NLS-1$
            copyButton.setImage(ImageProvider.getImage(EImage.COPY_ICON));
            copyButton.setToolTipText("Copy");
            data = new FormData();
            data.top = new FormAttachment(table, -3);
            data.left = new FormAttachment(removeButton, -3);
            copyButton.setLayoutData(data);
            copyButton.addListener(SWT.Selection, copyLineListener);
            copyButton.setEnabled(!param.isBasedOnSchema());
            copyButton.setVisible(!param.isReadOnly());

            Button pasteButton;
            pasteButton = getWidgetFactory().createButton(container, "", SWT.PUSH); //$NON-NLS-1$
            pasteButton.setImage(ImageProvider.getImage(EImage.PASTE_ICON));
            pasteButton.setToolTipText("Paste");
            data = new FormData();
            data.top = new FormAttachment(table, -3);
            data.left = new FormAttachment(copyButton, -3);
            pasteButton.setLayoutData(data);
            pasteButton.addListener(SWT.Selection, pasteLineListener);
            pasteButton.setEnabled(!param.isBasedOnSchema());
            pasteButton.setVisible(!param.isReadOnly());

            Button upButton;
            upButton = getWidgetFactory().createButton(container, "", SWT.PUSH); //$NON-NLS-1$
            upButton.setImage(ImageProvider.getImage(EImage.UP_ICON));
            upButton.setToolTipText("Up");
            data = new FormData();
            data.top = new FormAttachment(table, -3);
            data.left = new FormAttachment(pasteButton, -3);
            upButton.setLayoutData(data);
            upButton.addListener(SWT.Selection, upLineListener);
            upButton.setEnabled(!param.isBasedOnSchema());
            upButton.setVisible(!param.isReadOnly());

            Button downButton;
            downButton = getWidgetFactory().createButton(container, "", SWT.PUSH); //$NON-NLS-1$
            downButton.setImage(ImageProvider.getImage(EImage.DOWN_ICON));
            downButton.setToolTipText("Down");
            data = new FormData();
            data.top = new FormAttachment(table, -3);
            data.left = new FormAttachment(upButton, -3);
            downButton.setLayoutData(data);
            downButton.addListener(SWT.Selection, downLineListener);
            downButton.setEnabled(!param.isBasedOnSchema());
            downButton.setVisible(!param.isReadOnly());

            if (dynamicData) {
                Button refreshButton;
                refreshButton = getWidgetFactory().createButton(container, "", SWT.PUSH); //$NON-NLS-1$
                refreshButton.setImage(ImageProvider.getImage(EImage.REFRESH_ICON));
                refreshButton.setToolTipText("Refresh dynamic datas");
                data = new FormData();
                data.top = new FormAttachment(table, -3);
                data.left = new FormAttachment(downButton, -3);
                refreshButton.setLayoutData(data);
                refreshButton.addSelectionListener(listenerSelection);
                refreshButton.setVisible(!param.isReadOnly());
                refreshButton.setData(NAME, COLUMN);
                refreshButton.setData(PROPERTY, param.getName());
            }

        }

        curRowSize += 23 + ITabbedPropertyConstants.VSPACE * 2;

        return null;
    }

    private List<Map<String, Object>> copyTableValue(final IElementParameter param) {
        List<Map<String, Object>> paramValues = (List<Map<String, Object>>) param.getValue();
        List<Map<String, Object>> tableValues = new ArrayList<Map<String, Object>>();

        int numLine = 0;
        for (Map<String, Object> currentLine : paramValues) {
            tableValues.add(copyLine(currentLine, param, numLine));
            numLine++;
        }
        return tableValues;
    }

    private Map<String, Object> copyLine(final Map<String, Object> currentLine, final IElementParameter param, final int numLine) {
        Map<String, Object> newLine = new HashMap<String, Object>();
        String[] items = param.getListItemsDisplayCodeName();
        for (int i = 0; i < items.length; i++) {
            Object value = currentLine.get(items[i]);
            Object[] tmpObjects = param.getListItemsValue();
            if (tmpObjects[i] instanceof IElementParameter) {
                if (value instanceof String) { // only when the job is just loaded
                    boolean found = false;
                    int index = 0;
                    Object[] itemsValue = ((IElementParameter) tmpObjects[i]).getListItemsValue();
                    for (int j = 0; j < itemsValue.length && !found; j++) {
                        if (itemsValue[j].equals(value)) {
                            found = true;
                            index = j;
                        }
                    }
                    value = new Integer(index);
                }
            } else {
                if (i == 0 && param.isBasedOnSchema() && value == null && numLine >= 0) {
                    value = getColumnList().get(numLine);
                }
            }
            newLine.put(items[i], value);
        }
        return newLine;
    }

    private Map<String, Object> createNewLine(IElementParameter param) {
        Map<String, Object> line = new HashMap<String, Object>();
        String[] items = (String[]) param.getListItemsDisplayCodeName();
        Object[] itemsValue = (Object[]) param.getListItemsValue();

        if (itemsValue[0] instanceof IElementParameter) {
            IElementParameter tmpParam = (IElementParameter) itemsValue[0];
            line.put(items[0], new Integer(tmpParam.getIndexOfItemFromList((String) tmpParam.getDefaultClosedListValue())));
        } else {
            line.put(items[0], new String("'newLine'"));
        }
        for (int i = 1; i < items.length; i++) {
            if (itemsValue[i] instanceof IElementParameter) {
                IElementParameter tmpParam = (IElementParameter) itemsValue[i];
                line.put(items[i], new Integer(tmpParam.getIndexOfItemFromList((String) tmpParam.getDefaultClosedListValue())));
            } else {
                line.put(items[i], new String(""));
            }
        }
        return line;
    }

    private Map<String, Object> clipboard;

    private boolean checkErrorsWhenViewRefreshed;

    private void copyToCliboard(Map<String, Object> line, final IElementParameter param) {
        clipboard = copyLine(line, param, -1);
    }

    /**
     * Initialize all components for the defined section for this node.
     */
    public void addComponents() {
        checkErrorsWhenViewRefreshed = true;
        int heightSize = 0, maxRowSize = 0, nbInRow, numInRow;
        int maxRow;
        List<? extends IElementParameter> listParam = elem.getElementParameters();

        oldSchemaType = (String) elem.getPropertyValue(EParameterName.SCHEMA_TYPE.getName());
        if (oldSchemaType != null) {
            if (oldSchemaType.equals(EmfComponent.REPOSITORY)) {
                showSchemaRepositoryList(true);
                updateRepositoryList();
            } else {
                showSchemaRepositoryList(false);
            }
            if (elem instanceof Node) {
                ((Process) ((Node) elem).getProcess()).checkProcess();
            }
        }

        oldPropertyType = (String) elem.getPropertyValue(EParameterName.PROPERTY_TYPE.getName());
        if (oldPropertyType != null) {
            if (oldPropertyType.equals(EmfComponent.REPOSITORY)) {
                showPropertyRepositoryList(true);
                updateRepositoryList();
            } else {
                showPropertyRepositoryList(false);
            }
            if (elem instanceof Node) {
                ((Process) ((Node) elem).getProcess()).checkProcess();
            }
        }

        oldProcessType = (String) elem.getPropertyValue(EParameterName.PROCESS_TYPE_PROCESS.getName());
        if (oldProcessType != null) {
            String[] list = elem.getElementParameter(EParameterName.PROCESS_TYPE_PROCESS.getName()).getListItemsDisplayName();
            if ((oldProcessType.equals("") || (list.length == 0))) {
                updateProcessList();
                updateContextList();
            }
        }

        Control lastControl = null;

        if (currentComponent != null) {
            Control[] ct = composite.getChildren();
            for (int i = 0; i < ct.length; i++) {
                ct[i].dispose();
            }
        }

        hashCurControls = new DualHashBidiMap();

        maxRow = 0;
        for (int i = 0; i < listParam.size(); i++) {
            if (listParam.get(i).getCategory() == section) {
                if (listParam.get(i).getNumRow() > maxRow && listParam.get(i).isShow(listParam)) {
                    maxRow = listParam.get(i).getNumRow();
                }
            }
        }

        curRowSize = 0;
        for (int curRow = 1; curRow <= maxRow; curRow++) {
            maxRowSize = 0;
            nbInRow = 0;
            for (int i = 0; i < listParam.size(); i++) {
                if (listParam.get(i).getCategory() == section) {
                    if (listParam.get(i).getNumRow() == curRow && listParam.get(i).isShow(listParam)) {
                        nbInRow++;
                    }
                }
            }
            numInRow = 0;
            lastControl = null;
            for (int i = 0; i < listParam.size(); i++) {
                if (listParam.get(i).getCategory() == section) {
                    if (listParam.get(i).getNumRow() == curRow && listParam.get(i).isShow(listParam)) {
                        numInRow++;
                        switch (listParam.get(i).getField()) {
                        case EXTERNAL:
                            lastControl = addExternal(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;
                        case SCHEMA_TYPE:
                            lastControl = addSchemaType(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;
                        case TEXT:
                            lastControl = addText(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;
                        case CLOSED_LIST:
                            lastControl = addCombo(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;
                        case MEMO:
                            lastControl = addMemo(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;
                        case COLUMN_LIST:
                            lastControl = addColumnList(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;
                        case PREV_COLUMN_LIST:
                            lastControl = addColumnList(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;

                        // Modified by ftang, used for opening a sql builder dialog.
                        case MEMO_SQL:
                            lastControl = addSqlMemo(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl, "tsql");
                            break;
                        // Ends.

                        case MEMO_PERL:
                            lastControl = addLanguageMemo(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl, "perl");
                            break;
                        case CHECK:
                            lastControl = addCheck(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;
                        case FILE:
                            lastControl = addFile(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;
                        case DIRECTORY:
                            lastControl = addDirectory(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;
                        case VERSION:
                            lastControl = addVersion(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;
                        case TABLE:
                            lastControl = addTable(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;
                        case IMAGE:
                            lastControl = addImage(composite, listParam.get(i), numInRow, nbInRow, heightSize, lastControl);
                            break;
                        default:
                            break;
                        }
                        if (curRowSize > maxRowSize) {
                            maxRowSize = curRowSize;
                        }
                    }
                }
            }
            heightSize += maxRowSize;
        }
        composite.pack();
    }

    @Override
    public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);

        composite = parent;

        FormLayout layout = new FormLayout();
        layout.marginWidth = ITabbedPropertyConstants.HSPACE + 2;
        layout.marginHeight = ITabbedPropertyConstants.VSPACE;
        layout.spacing = ITabbedPropertyConstants.VMARGIN + 1;
        composite.setLayout(layout);
    }

    @Override
    public void refresh() {
        Object object;
        Iterator<? extends IElementParameter> it;
        IElementParameter param;
        if (elem == null) {
            return;
        }
        List<? extends IElementParameter> listParam = elem.getElementParameters();
        it = listParam.iterator();

        if (oldSchemaType != null) {
            String newSchemaType = (String) elem.getPropertyValue(EParameterName.SCHEMA_TYPE.getName());
            if (!oldSchemaType.equals(newSchemaType)) {
                addComponents();
            }
        }

        if (oldPropertyType != null) {
            String newPropertyType = (String) elem.getPropertyValue(EParameterName.PROPERTY_TYPE.getName());
            if (!oldPropertyType.equals(newPropertyType)) {
                addComponents();
            }
        }

        if (oldProcessType != null) {
            String newProcessType = (String) elem.getPropertyValue(EParameterName.PROCESS_TYPE_PROCESS.getName());
            if (!oldProcessType.equals(newProcessType)) {
                updateProcessList();
                updateContextList();
                addComponents();
            }
        }

        Boolean updateNeeded = (Boolean) elem.getPropertyValue(EParameterName.UPDATE_COMPONENTS.getName());
        if (updateNeeded != null) {
            if (updateNeeded) {
                addComponents();
                elem.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), new Boolean(false));
            }
        }

        while (it.hasNext()) {
            param = it.next();
            if (param.getCategory() == section) {
                if (hashCurControls.containsKey(param.getName())) {
                    object = hashCurControls.get(param.getName());
                    if ((param.getField() == EParameterFieldType.TEXT) || (param.getField() == EParameterFieldType.MEMO)
                            || (param.getField() == EParameterFieldType.FILE) || (param.getField() == EParameterFieldType.DIRECTORY)) {
                        Text t = (Text) object;
                        // editionControlHelper.unregister(t);
                        Object value = elem.getPropertyValue(param.getName());
                        if (value == null) {
                            t.setText("");
                        } else {
                            if (!value.equals(t.getText())) {
                                t.setText((String) value);
                            }
                        }
                        if (checkErrorsWhenViewRefreshed) {
                            checkErrorsForPropertiesOnly(t);
                        }
                    }
                    if (param.getField() == EParameterFieldType.VERSION) {
                        Text t = (Text) object;
                        t.setText((String) elem.getPropertyValue(param.getName()));
                    }
                    if ((param.getField() == EParameterFieldType.MEMO_SQL) || (param.getField() == EParameterFieldType.MEMO_PERL)) {
                        ColorStyledText t = (ColorStyledText) object;
                        // editionControlHelper.unregister(t);
                        String value = (String) elem.getPropertyValue(param.getName());
                        if (value == null) {
                            t.setText("");
                        } else {
                            if (!value.equals(t.getText())) {
                                t.setText(value);
                            }
                        }
                        if (checkErrorsWhenViewRefreshed) {
                            checkErrorsForPropertiesOnly(t);
                        }
                    }
                    if (param.getField() == EParameterFieldType.CLOSED_LIST || param.getField() == EParameterFieldType.COLUMN_LIST
                            || param.getField() == EParameterFieldType.PREV_COLUMN_LIST) {
                        CCombo c = (CCombo) object;
                        String value = new String(""); //$NON-NLS-1$
                        int nbInList = 0, nbMax = param.getListItemsValue().length;
                        String name = (String) elem.getPropertyValue(param.getName());
                        while (value.equals(new String("")) && nbInList < nbMax) { //$NON-NLS-1$
                            if (name.equals(param.getListItemsValue()[nbInList])) {
                                value = param.getListItemsDisplayName()[nbInList];
                            }
                            nbInList++;
                        }
                        c.setText(value);
                    }
                    if (param.getField() == EParameterFieldType.CHECK) {
                        Button b = (Button) object;
                        b.removeSelectionListener(listenerSelection);
                        b.setSelection((Boolean) elem.getPropertyValue(param.getName()));
                        b.addSelectionListener(listenerSelection);
                    }
                    if (param.getField() == EParameterFieldType.TABLE) {
                        TableViewerCreator tableViewerCreator = (TableViewerCreator) object;
                        tableViewerCreator.init((List) param.getValue());
                        // tableViewerCreator.getTableViewer().refresh();
                    }
                }
            }
        }
        composite.getParent().layout(true, true);
        checkErrorsWhenViewRefreshed = false;
    }

    /**
     * DOC amaumont Comment method "checkErrors".
     * 
     * @param control must be or extends <code>Text</code> or <code>StyledText</code>
     */
    private void checkErrorsForPropertiesOnly(Control control) {
        if (this.section == EComponentCategory.PROPERTY) {
            editionControlHelper.checkErrors(control);
        }
    }

    private ISelection lastSelection;

    @Override
    public void setInput(final IWorkbenchPart workbenchPart, final ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) {
            return;
        }
        if (lastSelection != null) { // added to correct a bug of the TabbedProperties
            if (lastSelection.equals(selection)) {
                return;
            }
        }
        lastSelection = selection;

        if (workbenchPart instanceof MultiPageTalendEditor) {
            part = (MultiPageTalendEditor) workbenchPart;
        } else {
            part = (MultiPageTalendEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        }
        super.setInput(part, selection);
        Assert.isTrue(selection instanceof IStructuredSelection);
        Object input = ((IStructuredSelection) selection).getFirstElement();
        if (input instanceof NodeContainerPart) {
            NodeContainerPart nContainer = (NodeContainerPart) input;
            elem = (Element) nContainer.getParent().getModel();
        } else if (input instanceof NodeTreeEditPart) {
            NodeTreeEditPart nTreePart = (NodeTreeEditPart) input;
            elem = (Element) nTreePart.getModel();
        } else if (!(input instanceof NodeReturnsTreeEditPart)) {
            Assert.isTrue(input instanceof EditPart);
            EditPart editPart = (EditPart) input;
            elem = (Element) editPart.getModel();
        }
        if (elem instanceof NodeLabel) {
            elem = ((NodeLabel) elem).getNode();
        }

        if (elem instanceof ConnectionLabel) {
            elem = ((ConnectionLabel) elem).getConnection();
        }

        if (currentComponent == null || elem instanceof Connection) {
            addComponents();
        } else {
            if (!currentComponent.equals(elem.getElementName())) {
                addComponents();
            }
        }
        currentComponent = elem.getElementName();
    }

    /**
     * DOC amaumont Comment method "getParameterName".
     * 
     * @param control
     * @return
     */
    private String getParameterName(Control control) {
        String name = (String) hashCurControls.getKey(control);
        if (name == null) {
            throw new IllegalStateException("parameterName shouldn't be null or you call this method too early ! (control value : '"
                    + ControlUtils.getText(control) + "')");
        }
        return name;
    }

    /**
     * Set the section of the tabbed property.
     * 
     * @param section
     */
    public DynamicTabbedPropertySection(final EComponentCategory section) {
        super();
        this.section = section;
        // currentLanguage = ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
        // .getProject().getLanguage();

        repositoryConnectionItemMap = new HashMap<String, ConnectionItem>();

        repositoryTableMap = new HashMap<String, IMetadataTable>();

        this.editionControlHelper = new EditionControlHelper();
    }

    /**
     * 
     * DOC amaumont DynamicTabbedPropertySection class global comment. Detailled comment <br/>
     * 
     * @author amaumont $Id: DynamicTabbedPropertySection.java 344 2006-11-08 14:29:42 +0000 (mer., 08 nov. 2006)
     * smallet $
     * 
     */
    class EditionControlHelper {

        private CheckErrorsHelper checkErrorsHelper;

        private UndoRedoHelper undoRedoHelper;

        private ContentProposalAdapterExtended extendedProposal;

        /**
         * DOC amaumont EditionListenerManager constructor comment.
         */
        public EditionControlHelper() {
            super();
            this.checkErrorsHelper = new CheckErrorsHelper();
            this.undoRedoHelper = new UndoRedoHelper();
        }

        /**
         * DOC amaumont Comment method "checkErrors".
         * 
         * @param t
         * @param b
         */
        public void checkErrors(Control control) {
            this.checkErrorsHelper.checkErrors(control);
        }

        /**
         * DOC amaumont Comment method "register".
         * 
         * @param parameterName
         * @param control
         * @param checkSyntax
         */
        public void register(String parameterName, Control control, boolean checkSyntax) {
            if (parameterName == null || control == null) {
                throw new NullPointerException();
            }
            if (!elem.getElementParameter(parameterName).isReadOnly()) {
                IProcess process = part.getTalendEditor().getProcess();
                this.extendedProposal = ProcessProposalUtils.installOn(control, process);
                this.checkErrorsHelper.register(control, extendedProposal);
                // this.checkErrorsHelper.checkErrors(control, false);
                ContextParameterExtractor.installOn(control, (Process) process);
            }

            this.undoRedoHelper.register(control);
        }

        /**
         * DOC amaumont Comment method "register".
         * 
         * @param control
         */
        public void unregister(Control control) {
            this.checkErrorsHelper.unregister(control);
            this.undoRedoHelper.unregister(control);
        }

    }

    /**
     * 
     * DOC amaumont DynamicTabbedPropertySection class global comment. Detailled comment <br/>
     * 
     * @author amaumont
     * 
     * $Id$
     * 
     */
    class UndoRedoHelper {

        private TypedTextCommandExecutor typedTextCommandExecutor;

        /**
         * DOC amaumont Comment method "unregister".
         * 
         * @param control
         */
        public void unregister(Control control) {
            // ControlUtils.removeModifyListener(control, modifyListenerForUndoRedo);
            typedTextCommandExecutor.unregister(control);
        }

        public UndoRedoHelper() {
            this.typedTextCommandExecutor = new TypedTextCommandExecutor() {

                @Override
                public void addNewCommand(Control control) {
                    String name = getParameterName(control);
                    String text = ControlUtils.getText(control);

                    Command cmd = new PropertyChangeCommand(elem, name, text);

                    getCommandStack().execute(cmd);
                    // System.out.println("##############################################");
                    // System.out.println("NEW COMMAND : " + text);
                }

                @Override
                public void updateCommand(Control control) {
                    Object[] commands = getCommandStack().getCommands();
                    if (commands.length == 0) {
                        addNewCommand(control);
                    } else {
                        Object lastCommandObject = commands[commands.length - 1];
                        String name = getParameterName(control);
                        if (lastCommandObject instanceof PropertyChangeCommand) {
                            PropertyChangeCommand lastCommand = (PropertyChangeCommand) lastCommandObject;
                            if (name.equals(lastCommand.getPropName())) {
                                String text = ControlUtils.getText(control);
                                lastCommand.modifyValue(text);
                                // System.out.println("--------------------------------------------");
                                // System.out.println("UPDATED COMMAND : " + text);
                            }
                        }
                    }
                }

            };

        }

        /**
         * DOC amaumont Comment method "register".
         * 
         * @param control
         */
        private void register(Control control) {
            // ControlUtils.addModifyListener(control, modifyListenerForUndoRedo);
            typedTextCommandExecutor.register(control);
        }

    }

    /**
     * 
     * DOC amaumont DynamicTabbedPropertySection class global comment. Detailled comment <br/>
     * 
     * @author amaumont $Id: DynamicTabbedPropertySection.java 344 2006-11-08 14:29:42 +0000 (mer., 08 nov. 2006)
     * smallet $
     * 
     */
    class CheckErrorsHelper {

        private Map<Control, ControlProperties> controlToProp = new HashMap<Control, ControlProperties>();

        /**
         * DOC amaumont CheckSyntaxHelper constructor comment.
         */
        public CheckErrorsHelper() {
            super();
        }

        private FocusListener focusListenerForCheckingError = new FocusListener() {

            public void focusGained(FocusEvent event) {
                focusGainedExecute((Control) event.widget);
            }

            public void focusLost(FocusEvent event) {
                if (!extendedProposal.isProposalOpened()) {
                    Control control = (Control) event.widget;
                    checkErrorsForPropertiesOnly(control);
                }
            }

        };

        private KeyListener keyListenerForCheckingError = new KeyListener() {

            public void keyPressed(KeyEvent event) {
                Control control = (Control) event.widget;
                resetErrorState(control);
            }

            public void keyReleased(KeyEvent e) {
            }

        };

        private ContentProposalAdapterExtended extendedProposal;

        public void register(Control control, ContentProposalAdapterExtended extendedProposal) {
            control.addFocusListener(focusListenerForCheckingError);
            control.addKeyListener(keyListenerForCheckingError);
            this.extendedProposal = extendedProposal;
        }

        /**
         * DOC amaumont Comment method "unregister".
         * 
         * @param control
         */
        public void unregister(Control control) {
            control.removeFocusListener(focusListenerForCheckingError);
            control.removeKeyListener(keyListenerForCheckingError);
        }

        private void focusGainedExecute(Control control) {
            resetErrorState(control);
        }

        /**
         * DOC amaumont Comment method "checkSyntax".
         * 
         * @param control
         * @param modifying
         */
        public void checkErrors(final Control control) {

            boolean isReadonly = elem.getElementParameter(getParameterName(control)).isReadOnly();
            if (isReadonly) {
                return;
            }

            final Color bgColorError = control.getDisplay().getSystemColor(SWT.COLOR_RED);
            final Color fgColorError = control.getDisplay().getSystemColor(SWT.COLOR_WHITE);

            final ECodeLanguage language = ((RepositoryContext) org.talend.core.CorePlugin.getContext().getProperty(
                    org.talend.core.context.Context.REPOSITORY_CONTEXT_KEY)).getProject().getLanguage();
            final ICodeSyntaxChecker syntaxChecker = SyntaxCheckerFactory.getInstance().getSyntaxChecker(language);

            final String valueFinal = ControlUtils.getText(control);

            ControlProperties existingControlProperties = controlToProp.get(control);

            Problem problem = null;
            if (valueFinal != null) {
                problem = syntaxChecker.checkSyntax(valueFinal);
            }

            boolean isRequired = elem.getElementParameter(getParameterName(control)).isRequired();
            if (problem == null) {
                if (isRequired && (valueFinal == null || valueFinal.trim().length() == 0)) {
                    problem = new Problem(null, "This field is required.", ProblemStatus.ERROR);
                }
            }

            if (problem != null) {
                if (existingControlProperties == null) {
                    ControlProperties properties = new ControlProperties();
                    controlToProp.put(control, properties);
                    // store original properties to restore them when error will be corrected
                    properties.originalBgColor = control.getBackground();
                    properties.originalFgColor = control.getForeground();
                    properties.originalToolTip = control.getToolTipText();
                }

                control.setBackground(bgColorError);
                control.setForeground(fgColorError);
                control.setToolTipText("Syntax error: " + problem.getDescription());
            } else {
                resetErrorState(control);
            }
        }

        /**
         * DOC amaumont Comment method "resetErrorState".
         * 
         * @param control
         * @param previousProblem
         */
        private void resetErrorState(final Control control) {
            ControlProperties existingControlProperties = controlToProp.get(control);
            if (existingControlProperties != null) {
                control.setToolTipText(existingControlProperties.originalToolTip);
                control.setBackground(existingControlProperties.originalBgColor);
                control.setForeground(existingControlProperties.originalFgColor);
                controlToProp.remove(control);
            }
        }

        /**
         * 
         * Container of original properties of Control. <br/>
         * 
         * $Id$
         * 
         */
        class ControlProperties {

            public Color originalBgColor;

            public Color originalFgColor;

            public String originalToolTip;

            public Problem previousProblem;

            /**
             * DOC amaumont ControlProperties constructor comment.
             */
            public ControlProperties() {
                super();
            }

        }

    }

    /**
     * 
     * Added by ftang.
     * 
     * Gets repository item base on repository name.
     * 
     * @param repositoryName a string representing repository name
     * 
     * @return a string representing repository item
     * 
     */
    private String getRepositoryItemFromRepositoryName(String repositoryName) {
        for (IElementParameter param : (List<IElementParameter>) elem.getElementParameters()) {
            if (param.getRepositoryValue() != null) {
                if (param.getRepositoryValue().equals(repositoryName)) {
                    String value = (String) param.getValue();
                    Object[] valuesList = (Object[]) param.getListItemsValue();
                    String[] originalList = param.getListItemsDisplayName();
                    for (int i = 0; i < valuesList.length; i++) {
                        if (valuesList[i].equals(value)) {
                            return originalList[i];
                        }
                    }

                }
            }
        }
        return null;
    }// Ends
}
