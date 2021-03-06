/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.mobile.bundle;

import android.content.Context;
import android.os.Bundle;

import org.openmrs.mobile.activities.dialog.CustomFragmentDialog;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.Visit;
import org.openmrs.mobile.models.VisitPredefinedTask;

import java.io.Serializable;
import java.util.List;

public class CustomDialogBundle implements Serializable {

	private CustomFragmentDialog.OnClickAction leftButtonAction;
	private CustomFragmentDialog.OnClickAction rightButtonAction;
	private String textViewMessage;
	private String titleViewMessage;
	private String editTextViewMessage;
	private String leftButtonText;
	private String rightButtonText;
	private String progressViewMessage;
	private List<Patient> patientsList;
	private Patient newPatient;
	private boolean loadingBar;
	private boolean progressDialog;
	private List<VisitPredefinedTask> autoCompleteTextView;
	private Context context;
	private boolean disableAutoCompleteText;
	private String editNoteTextViewMessage;
	private Bundle arguments;
	private Visit visit;

	public boolean hasProgressDialog() {
		return progressDialog;
	}

	public void setProgressDialog(boolean progressDialog) {
		this.progressDialog = progressDialog;
	}

	public boolean hasLoadingBar() {
		return loadingBar;
	}

	public void setLoadingBar(boolean loadingBar) {
		this.loadingBar = loadingBar;
	}

	public CustomFragmentDialog.OnClickAction getLeftButtonAction() {
		return leftButtonAction;
	}

	public void setLeftButtonAction(CustomFragmentDialog.OnClickAction leftButtonAction) {
		this.leftButtonAction = leftButtonAction;
	}

	public CustomFragmentDialog.OnClickAction getRightButtonAction() {
		return rightButtonAction;
	}

	public void setRightButtonAction(CustomFragmentDialog.OnClickAction rightButtonAction) {
		this.rightButtonAction = rightButtonAction;
	}

	public String getTextViewMessage() {
		return textViewMessage;
	}

	public void setTextViewMessage(String textViewMessage) {
		this.textViewMessage = textViewMessage;
	}

	public String getLeftButtonText() {
		return leftButtonText;
	}

	public void setLeftButtonText(String leftButtonText) {
		this.leftButtonText = leftButtonText;
	}

	public String getRightButtonText() {
		return rightButtonText;
	}

	public void setRightButtonText(String rightButtonText) {
		this.rightButtonText = rightButtonText;
	}

	public String getTitleViewMessage() {
		return titleViewMessage;
	}

	public void setTitleViewMessage(String titleViewMessage) {
		this.titleViewMessage = titleViewMessage;
	}

	public String getProgressViewMessage() {
		return progressViewMessage;
	}

	public void setProgressViewMessage(String progressViewMessage) {
		this.progressViewMessage = progressViewMessage;
	}

	public String getEditTextViewMessage() {
		return editTextViewMessage;
	}

	public void setEditTextViewMessage(String editTextViewMessage) {
		this.editTextViewMessage = editTextViewMessage;
	}

	public List<Patient> getPatientsList() {
		return patientsList;
	}

	public void setPatientsList(List<Patient> patientsList) {
		this.patientsList = patientsList;
	}

	public boolean hasPatientList() {
		return patientsList != null;
	}

	public Patient getNewPatient() {
		return newPatient;
	}

	public void setNewPatient(Patient newPatient) {
		this.newPatient = newPatient;
	}

	public List<VisitPredefinedTask> getAutoCompleteTextView() {
		return autoCompleteTextView;
	}

	public void setAutoCompleteTextView(List<VisitPredefinedTask> autoCompleteTextView) {
		this.autoCompleteTextView = autoCompleteTextView;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public boolean isDisableAutoCompleteText() {
		return disableAutoCompleteText;
	}

	public void setDisableAutoCompleteText(boolean disableAutoCompleteText) {
		this.disableAutoCompleteText = disableAutoCompleteText;
	}

	public String getEditNoteTextViewMessage() {
		return editNoteTextViewMessage;
	}

	public void setEditNoteTextViewMessage(String editNoteTextViewMessage) {
		this.editNoteTextViewMessage = editNoteTextViewMessage;
	}

	public Bundle getArguments() {
		return arguments;
	}

	public void setArguments(Bundle arguments) {
		this.arguments = arguments;
	}

	public Visit getVisit() {
		return visit;
	}

	public void setVisit(Visit visit) {
		this.visit = visit;
	}
}
