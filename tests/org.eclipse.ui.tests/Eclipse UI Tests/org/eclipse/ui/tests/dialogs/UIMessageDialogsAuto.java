/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.tests.util.DialogCheck;

import org.eclipse.ui.internal.WorkbenchMessages;

public class UIMessageDialogsAuto extends TestCase {
    private static final String DUMMY_RESOURCE = "Dummy.resource";

    private static final String DUMMY_PROJECT = "DummyProject";

    private static final String DUMMY_ABSOLUTE_PATH = "C:\\Dummypath\\Dummy.resource";

    private static final String DUMMY_RELATIVE_PATH = "\\" + DUMMY_PROJECT
            + "\\" + DUMMY_RESOURCE;

    public UIMessageDialogsAuto(String name) {
        super(name);
    }

    private Shell getShell() {
        return DialogCheck.getShell();
    }

    /*
     * Convenience method simliar to org.eclipse.jface.dialogs.MessageDialog::openConfirm.
     * The method will return the dialog instead of opening.
     * @param title the dialog's title, or <code>null</code> if none.
     * @param message the message.
     * @return Dialog the confirm dialog.
     */
    private MessageDialog getConfirmDialog(String title, String message) {
        return new MessageDialog(getShell(), title, null, message,
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.OK_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
    }

    /*
     * Convenience method simliar to org.eclipse.jface.dialogs.MessageDialog::openError.
     * The method will return the dialog instead of opening.
     * @param title the dialog's title, or <code>null</code> if none.
     * @param message the message.
     * @return MessageDialog the error confirm dialog.
     */
    private MessageDialog getErrorDialog(String title, String message) {
        return new MessageDialog(getShell(), title, null, message,
                MessageDialog.ERROR,
                new String[] { IDialogConstants.OK_LABEL }, 0);
    }

    /*
     * Convenience method simliar to org.eclipse.jface.dialogs.MessageDialog::openInformation.
     * The method will return the dialog instead of opening.
     * @param title the dialog's title, or <code>null</code> if none.
     * @param message the message.
     * @return MessageDialog the information dialog.
     */
    private MessageDialog getInformationDialog(String title, String message) {
        return new MessageDialog(getShell(), title, null, message,
                MessageDialog.INFORMATION,
                new String[] { IDialogConstants.OK_LABEL }, 0);
    }

    /*
     * Convenience method simliar to org.eclipse.jface.dialogs.MessageDialog::openQuestion.
     * The method will return the dialog instead of opening.
     * @param title the dialog's title, or <code>null</code> if none.
     * @param message the message.
     * @return MessageDialog the question dialog.
     */
    private MessageDialog getQuestionDialog(String title, String message) {
        return new MessageDialog(getShell(), title, null, message,
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL }, 0);
    }

    /*
     * Convenience method simliar to org.eclipse.jface.dialogs.MessageDialog::getWarningDialog.
     * The method will return the dialog instead of opening.
     * @param title the dialog's title, or <code>null</code> if none.
     * @param message the message.
     * @return MessageDialog the confirm dialog.
     */
    private MessageDialog getWarningDialog(String title, String message) {
        return new MessageDialog(getShell(), title, null, message,
                MessageDialog.WARNING,
                new String[] { IDialogConstants.OK_LABEL }, 0);
    }

    public void testAbortPageFlipping() {
        Dialog dialog = getWarningDialog(JFaceResources
                .getString("AbortPageFlippingDialog.title"), JFaceResources
                .getString("AbortPageFlippingDialog.message"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testCloseFileDeleted() {
        Dialog dialog = null;
        ResourceBundle bundle = ResourceBundle
                .getBundle("org.eclipse.ui.texteditor.EditorMessages");
        if (bundle != null) {
            dialog = getConfirmDialog(
                    bundle
                            .getString("Editor.error.activated.deleted.close.title"),
                    bundle
                            .getString("Editor.error.activated.deleted.close.message"));
        }
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testCopyOverwrite() {
        Dialog dialog = getQuestionDialog("Exists","");
         DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testDeleteProject() {
        String title = "Project";
        String msg ="";
        Dialog dialog = new MessageDialog(getShell(), title, null, // accept the default window icon
                msg, MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testDeleteReadOnlyCheck() {
        Dialog dialog = new MessageDialog(getShell(), "Delete", null,
              "Exists",
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testDeleteResource() {
        Dialog dialog = getQuestionDialog("Delete","");
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testDeleteResources() {
		  Dialog dialog = getQuestionDialog("Delete","");
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testDropOverwrite() {
        Dialog dialog = new MessageDialog(
                getShell(),
                ResourceNavigatorMessagesCopy.getString("DropAdapter.question"),
                null, ResourceNavigatorMessagesCopy.format(
                        "DropAdapter.overwriteQuery",
                        new Object[] { DUMMY_RELATIVE_PATH }),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testErrorClosing() {
        Dialog dialog = getQuestionDialog(WorkbenchMessages.Error,
                WorkbenchMessages.ErrorClosingNoArg);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testFileChanged() {
        MessageDialog dialog = null;
        ResourceBundle bundle = ResourceBundle
                .getBundle("org.eclipse.ui.texteditor.EditorMessages");
        if (bundle != null) {
            dialog = getQuestionDialog(
                    bundle.getString("Editor.error.activated.outofsync.title"),
                    bundle
                            .getString("Editor.error.activated.outofsync.message"));
        }
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testFileExtensionEmpty() {
        Dialog dialog = getInformationDialog(
                "Empty",
                "ExtensionEmptyMessage");
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testFileNameWrong() {
        Dialog dialog = getInformationDialog(
                "InvalidTitle",
               "InvalidMessage");
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testFileTypeExists() {
        Dialog dialog = getInformationDialog(WorkbenchMessages.FileEditorPreference_existsTitle,
                WorkbenchMessages.FileEditorPreference_existsMessage);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testInvalidType_1() {
        Dialog dialog = getWarningDialog("invalidTitle","invalidMessage");
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testInvalidType_2() {
        Dialog dialog = getWarningDialog("invalidType", "invalidTypeMessage");
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testMoveReadOnlyCheck() {
        Dialog dialog = new MessageDialog(getShell(), "Move_title", null, ".MoveResourceAction",
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testNoBuilders() {
        Dialog dialog = getWarningDialog("BuildAction_warning", "noBuilders");
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testNoGlobalBuildersDialog() {
        Dialog dialog = getWarningDialog("GlobalBuildAction_warning","GlobalBuildAction_noBuilders");
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testNoPropertyPage() {
        Dialog dialog = getInformationDialog(WorkbenchMessages.PropertyDialog_messageTitle, NLS.bind(WorkbenchMessages.PropertyDialog_noPropertyMessage, (new Object[] { "DummyPropertyPage" })));
        DialogCheck.assertDialogTexts(dialog, this);
    }


    public void testOperationNotAvailable() {
        Dialog dialog = getInformationDialog(WorkbenchMessages.Information, "operationNotAvailableMessage");
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testOverwritePerspective() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages.SavePerspective_overwriteTitle, null,
                NLS.bind(WorkbenchMessages.SavePerspective_overwriteQuestion, (new Object[] { "Dummy Perspective" })),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testRefreshDeleteProject() {
        Dialog dialog = new MessageDialog(getShell(), "RefreshAction_dialogTitle", null,
               "c:\\dummypath\\" + DUMMY_PROJECT,
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testRenameOverwrite() {
        Dialog dialog = getQuestionDialog(".RenameResourceAction_resourceExist",DUMMY_RELATIVE_PATH);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testResetPerspective() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages.ResetPerspective_title, null, NLS.bind(WorkbenchMessages.ResetPerspective_message, (new Object[] { "Dummy Perspective" })),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.OK_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testSaveAsOverwrite() {
        Dialog dialog = new MessageDialog(getShell(), "WorkbenchMessages.Question", null,DUMMY_RELATIVE_PATH, MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testSaveChanges() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages.Save_Resource, null, NLS.bind(WorkbenchMessages.EditorManager_saveChangesQuestion, (new Object[] { DUMMY_RESOURCE })), MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testSaveFileDeleted() {
        MessageDialog dialog = null;
        ResourceBundle bundle = ResourceBundle
                .getBundle("org.eclipse.ui.texteditor.EditorMessages");
        if (bundle != null) {
            dialog = new MessageDialog(
                    getShell(),
                    bundle
                            .getString("Editor.error.activated.deleted.save.title"),
                    null,
                    bundle
                            .getString("Editor.error.activated.deleted.save.message"),
                    MessageDialog.QUESTION,
                    new String[] {
                            bundle
                                    .getString("Editor.error.activated.deleted.save.button.save"),
                            bundle
                                    .getString("Editor.error.activated.deleted.save.button.close") },
                    0);
        }
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testUpdateConflict() {
        MessageDialog dialog = null;
        ResourceBundle bundle = ResourceBundle
                .getBundle("org.eclipse.ui.texteditor.EditorMessages");
        if (bundle != null) {
            dialog = getQuestionDialog(bundle
                    .getString("Editor.error.save.outofsync.title"), bundle
                    .getString("Editor.error.save.outofsync.message"));
        }
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testWizardClosing() {
        Dialog dialog = new MessageDialog(getShell(), JFaceResources
                .getString("WizardClosingDialog.title"), null, JFaceResources
                .getString("WizardClosingDialog.message"),
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.OK_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testWizardOverwrite() {
        Dialog dialog = new MessageDialog(getShell(), "WorkbenchMessages.Question", null, "WizardDataTransfer_existsQuestion", MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

}

