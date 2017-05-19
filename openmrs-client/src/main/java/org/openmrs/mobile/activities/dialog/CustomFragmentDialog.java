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

package org.openmrs.mobile.activities.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.joda.time.LocalDateTime;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.addeditpatient.AddEditPatientActivity;
import org.openmrs.mobile.activities.addeditpatient.SimilarPatientsRecyclerViewAdapter;
import org.openmrs.mobile.activities.addeditvisit.AddEditVisitActivity;
import org.openmrs.mobile.activities.login.LoginActivity;
import org.openmrs.mobile.activities.login.LoginFragment;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardActivity;
import org.openmrs.mobile.activities.visittasks.VisitTasksActivity;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.bundle.CustomDialogBundle;
import org.openmrs.mobile.data.DataService;
import org.openmrs.mobile.data.QueryOptions;
import org.openmrs.mobile.data.impl.EncounterDataService;
import org.openmrs.mobile.data.impl.LocationDataService;
import org.openmrs.mobile.data.impl.ObsDataService;
import org.openmrs.mobile.data.impl.VisitDataService;
import org.openmrs.mobile.models.Concept;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.EncounterType;
import org.openmrs.mobile.models.Location;
import org.openmrs.mobile.models.Observation;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.User;
import org.openmrs.mobile.models.Visit;
import org.openmrs.mobile.models.VisitPredefinedTask;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * General class for creating dialog fragment instances
 */
public class CustomFragmentDialog extends DialogFragment {
	private static final int TYPED_DIMENSION_VALUE = 10;
	protected LayoutInflater mInflater;
	protected LinearLayout mFieldsLayout;
	protected RecyclerView mRecyclerView;
	protected TextView mTextView, mTitleTextView;
	protected EditText mEditText, mEditNoteText;
	private Button mLeftButton, mRightButton;
	private CustomDialogBundle mCustomDialogBundle;
	private AutoCompleteTextView autoCompleteTextView;

	public static CustomFragmentDialog newInstance(CustomDialogBundle customDialogBundle) {
		CustomFragmentDialog dialog = new CustomFragmentDialog();
		dialog.setRetainInstance(true);
		Bundle bundle = new Bundle();
		bundle.putSerializable(ApplicationConstants.BundleKeys.CUSTOM_DIALOG_BUNDLE, customDialogBundle);
		dialog.setArguments(bundle);
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCustomDialogBundle =
				(CustomDialogBundle)getArguments().getSerializable(ApplicationConstants.BundleKeys.CUSTOM_DIALOG_BUNDLE);
		if (mCustomDialogBundle.hasLoadingBar()) {
			this.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.LoadingDialogTheme_DialogTheme);
		} else if (mCustomDialogBundle.hasPatientList()) {
			this.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SimilarPatients_DialogTheme);
		} else {
			this.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogTheme);
		}
		this.setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		this.mInflater = inflater;
		View dialogLayout = mInflater.inflate(R.layout.fragment_dialog_layout, null, false);
		this.mFieldsLayout = (LinearLayout)dialogLayout.findViewById(R.id.dialogForm);
		this.setRightButton(dialogLayout);
		this.setLeftButton(dialogLayout);
		getDialog().setCanceledOnTouchOutside(false);
		buildDialog();
		FontsUtil.setFont((ViewGroup)dialogLayout);
		return dialogLayout;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (isDialogAvailable()) {
			this.setBorderless();
			this.setOnBackListener();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (isDialogAvailable()) {
			this.setBorderless();
			this.setOnBackListener();
		}
	}

	@Override
	public void show(FragmentManager manager, String tag) {
		if (null == manager.findFragmentByTag(tag)) {
			manager.beginTransaction().add(this, tag).commitAllowingStateLoss();
		}
	}

	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();

	}

	public final void setOnBackListener() {
		getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && getActivity().getClass().equals(LoginActivity.class)) {
					if (OpenMRS.getInstance().getServerUrl().equals(ApplicationConstants.EMPTY_STRING)) {
						OpenMRS.getInstance().getOpenMRSLogger().d("Exit application");
						getActivity().onBackPressed();
						dismiss();
					} else {
					   /* ((FindPatientRecordFragment) getActivity()
								.getSupportFragmentManager()
                                .findFragmentById(R.id.loginContentFrame))
                                .hideURLDialog();*/
						dismiss();
					}
				}
				return false;
			}
		});
	}

	public final void setBorderless() {
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		int marginWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TYPED_DIMENSION_VALUE,
				OpenMRS.getInstance().getResources().getDisplayMetrics());

		DisplayMetrics display = this.getResources().getDisplayMetrics();
		int width = display.widthPixels;

		WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
		params.width = width - 2 * marginWidth;

		getDialog().getWindow().setAttributes(params);
	}

	private void buildDialog() {
		if (null != mCustomDialogBundle.getTitleViewMessage()) {
			mTitleTextView = addTitleBar(mCustomDialogBundle.getTitleViewMessage());
		}
		if (null != mCustomDialogBundle.getEditTextViewMessage()) {
			mEditText = addEditTextField(mCustomDialogBundle.getEditTextViewMessage());
		}
		if (null != mCustomDialogBundle.getTextViewMessage()) {
			mTextView = addTextField(mCustomDialogBundle.getTextViewMessage());
		}
		if (null != mCustomDialogBundle.getLeftButtonAction()) {
			setLeftButton(mCustomDialogBundle.getLeftButtonText());
			mLeftButton.setOnClickListener(onClickActionSolver(mCustomDialogBundle.getLeftButtonAction()));
		}
		if (null != mCustomDialogBundle.getRightButtonAction()) {
			setRightButton(mCustomDialogBundle.getRightButtonText());
			mRightButton.setOnClickListener(onClickActionSolver(mCustomDialogBundle.getRightButtonAction()));
		}
		if (mCustomDialogBundle.hasLoadingBar()) {
			addProgressBar(mCustomDialogBundle.getTitleViewMessage());
			this.setCancelable(false);
		}
		if (mCustomDialogBundle.hasProgressDialog()) {
			addProgressBar(mCustomDialogBundle.getProgressViewMessage());
			this.setCancelable(false);
		}
		if (null != mCustomDialogBundle.getPatientsList()) {
			mRecyclerView = addRecycleView(mCustomDialogBundle.getPatientsList(), mCustomDialogBundle.getNewPatient());
		}

		if (null != mCustomDialogBundle.getAutoCompleteTextView()) {
			autoCompleteTextView = addAutoCompleteTextView(mCustomDialogBundle
					.getAutoCompleteTextView());
		}

		if (null != mCustomDialogBundle.getEditNoteTextViewMessage()) {
			mEditNoteText = addEditNoteTextField(mCustomDialogBundle.getEditNoteTextViewMessage());
		}

	}

	private RecyclerView addRecycleView(List<Patient> patientsList, Patient newPatient) {
		LinearLayout field = (LinearLayout)mInflater.inflate(R.layout.openmrs_recycle_view, null);
		RecyclerView recyclerView = (RecyclerView)field.findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(new SimilarPatientsRecyclerViewAdapter((getActivity()), patientsList, newPatient));
		mFieldsLayout.addView(field);
		recyclerView.setHasFixedSize(true);
		return recyclerView;
	}

	public EditText addEditTextField(String defaultMessage) {
		LinearLayout field = (LinearLayout)mInflater.inflate(R.layout.openmrs_edit_text_field, null);
		EditText editText = (EditText)field.findViewById(R.id.openmrsEditText);
		if (null != defaultMessage) {
			editText.setText(defaultMessage);
		}
		mFieldsLayout.addView(field);
		return editText;
	}

	public AutoCompleteTextView addAutoCompleteTextView(List<VisitPredefinedTask> autoComplete) {
		LinearLayout field = (LinearLayout)mInflater.inflate(R.layout.openmrs_auto_complete_text_view_field, null);
		AutoCompleteTextView autoCompleteText = (AutoCompleteTextView)field.findViewById(R.id
				.openmrsAutoCompleteTextView);
		if (mCustomDialogBundle.isDisableAutoCompleteText()) {
			autoCompleteText.setEnabled(false);
		} else {
			autoCompleteText.setEnabled(true);
		}

		ArrayAdapter adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, autoComplete);
		autoCompleteText.setAdapter(adapter);

		autoCompleteText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (autoCompleteText.getText().length() >= autoCompleteText.getThreshold()) {
					autoCompleteText.showDropDown();
				}
				if (Arrays.asList(autoComplete).contains(autoCompleteText.getText().toString())) {
					autoCompleteText.dismissDropDown();
				}
			}
		});

		mFieldsLayout.addView(field);
		return autoCompleteText;
	}

	public EditText addEditNoteTextField(String defaultMessage) {
		LinearLayout field = (LinearLayout)mInflater.inflate(R.layout.openmrs_edit_note_text_field, null);
		EditText editText = (EditText)field.findViewById(R.id.openmrsEditNoteText);
		if (null != defaultMessage) {
			editText.setText(defaultMessage);
		}
		mFieldsLayout.addView(field);
		return editText;
	}

	public TextView addTextField(String message) {
		LinearLayout field = (LinearLayout)mInflater.inflate(R.layout.openmrs_text_view_field, null);
		TextView textView = (TextView)field.findViewById(R.id.openmrsTextView);
		textView.setText(message);
		textView.setSingleLine(false);
		FontsUtil.setFont(textView, FontsUtil.OpenFonts.OPEN_SANS_ITALIC);
		mFieldsLayout.addView(field,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		return textView;
	}

	public TextView addTitleBar(String title) {
		LinearLayout field = (LinearLayout)mInflater.inflate(R.layout.openmrs_title_view_field, null);
		TextView textView = (TextView)field.findViewById(R.id.openmrsTitleView);
		textView.setText(title);
		textView.setSingleLine(true);
		mFieldsLayout.addView(field,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		return textView;
	}

	public void setLeftButton(String text) {
		mLeftButton.setText(text);
		setViewVisible(mLeftButton, true);
	}

	public void setRightButton(String text) {
		mRightButton.setText(text);
		setViewVisible(mRightButton, true);
	}

	private void setViewVisible(View view, boolean visible) {
		if (visible) {
			view.setVisibility(View.VISIBLE);
		} else {
			view.setVisibility(View.GONE);
		}
	}

	public void setRightButton(View dialogLayout) {
		this.mRightButton = (Button)dialogLayout.findViewById(R.id.dialogFormButtonsSubmitButton);
	}

	public void setLeftButton(View dialogLayout) {
		this.mLeftButton = (Button)dialogLayout.findViewById(R.id.dialogFormButtonsCancelButton);
	}

	public void addProgressBar(String message) {
		RelativeLayout progressBarLayout = (RelativeLayout)mInflater.inflate(R.layout.dialog_progress, null);
		TextView textView = (TextView)progressBarLayout.findViewById(R.id.progressTextView);
		textView.setText(message);
		mFieldsLayout.addView(progressBarLayout,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	}

	public String getEditTextValue() {
		String value = "";
		if (mEditText != null) {
			value = mEditText.getText().toString();
		}
		return value;
	}

	public String getEditNoteTextValue() {
		String value = "";
		if (mEditNoteText != null) {
			value = mEditNoteText.getText().toString();
		}
		return value;
	}

	public String getAutoCompleteTextValue() {
		String value = "";
		if (autoCompleteTextView != null) {
			value = autoCompleteTextView.getText().toString();
		}
		return value;
	}

	private boolean isDialogAvailable() {
		return null != this && null != this.getDialog();
	}

	private View.OnClickListener onClickActionSolver(final OnClickAction action) {

		//LoginActivity activity = (LoginActivity)getActivity();
		//activity.onFinishEditDialog(mEditText.getText().toString());
		//this.dismiss();

		return new View.OnClickListener() {
			//CHECKSTYLE:OFF
			@Override
			public void onClick(View v) {
				switch (action) {
					case DISMISS:
						dismiss();
						break;
					case LOGIN:
						((LoginFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.loginContentFrame))
								.login(true);
						dismiss();
						break;
					case LOGOUT:
						((ACBaseActivity)getActivity()).logout();
						dismiss();
						break;
					case FINISH:
						getActivity().moveTaskToBack(true);
						android.os.Process.killProcess(android.os.Process.myPid());
						System.exit(1);
						break;
					case INTERNET:
						getActivity().startActivity(new Intent(Settings.ACTION_SETTINGS));
						dismiss();
						break;
					case UNAUTHORIZED:
						((ACBaseActivity)getActivity()).moveUnauthorizedUserToLoginScreen();
						dismiss();
						break;
					case END_VISIT:
						((AddEditVisitActivity)getActivity()).addEditVisitPresenter.endVisit();
						dismiss();
						break;
					case START_VISIT:
						doStartVisitAction();
						dismiss();
						break;
					case REGISTER_PATIENT:
						((AddEditPatientActivity)getActivity()).mPresenter
								.registerPatient(mCustomDialogBundle.getNewPatient());
						dismiss();
						break;
					case CANCEL_REGISTERING:
						((AddEditPatientActivity)getActivity()).mPresenter.finishAddPatientActivity();
						dismiss();
						break;
					case DELETE_PATIENT:
						PatientDashboardActivity patientDashboardActivity = (PatientDashboardActivity)getActivity();
						//activity.findPatientPresenter.deletePatient();
						dismiss();
						patientDashboardActivity.finish();
						break;
					case ADD_VISIT_TASKS:
						if (StringUtils.notEmpty(getAutoCompleteTextValue())) {
							((VisitTasksActivity)getActivity()).mPresenter
									.createVisitTasksObject(getAutoCompleteTextValue());
							dismiss();
							break;
						} else {
							dismiss();
							break;
						}
					case SAVE_VISIT_NOTE:

						Bundle bundle = mCustomDialogBundle.getArguments();
						Observation observation =
								(Observation)bundle.getSerializable(ApplicationConstants.BundleKeys.OBSERVATION);
						observation.setValue(getEditNoteTextValue());

						ObsDataService observationDataService = new ObsDataService();

						observationDataService.update(observation, new DataService.GetCallback<Observation>() {
							@Override
							public void onCompleted(Observation entity) {
								((PatientDashboardActivity)getActivity()).mPresenter
										.fetchPatientData(bundle.getString(ApplicationConstants
												.BundleKeys.PATIENT_UUID_BUNDLE));
								dismiss();
							}

							@Override
							public void onError(Throwable t) {

							}
						});
						break;
					case CREATE_VISIT_NOTE:

						bundle = mCustomDialogBundle.getArguments();
						VisitDataService visitDataService = new VisitDataService();
						visitDataService.getByUUID(bundle.getString(ApplicationConstants
										.BundleKeys.VISIT_UUID_BUNDLE), QueryOptions.LOAD_RELATED_OBJECTS,
								new DataService.GetCallback<Visit>() {
									@Override
									public void onCompleted(Visit visit) {
										saveEncounter(visit);
									}

									@Override
									public void onError(Throwable t) {

									}
								});

						break;
					case DISPLAY_URL_EDIT_FIELD:
						((LoginActivity)getActivity()).mPresenter.showEditUrlEditText(true);
						dismiss();
						break;
					default:
						break;
				}
			}
			//CHECKSTYLE:ON
		};
	}

	private void saveEncounter(Visit visit) {
		if (visit != null) {
			OpenMRS instance = OpenMRS.getInstance();

			String locationUuid = "";
			Location location = null;

			PatientDashboardActivity patientDashboardActivity = (PatientDashboardActivity)getActivity();
			Patient patient = patientDashboardActivity.mPresenter.getPatient();

			//get Location
			if (!OpenMRS.getInstance().getLocation().equalsIgnoreCase(null)) {
				locationUuid = instance.getLocation();
			}

			LocationDataService locationDataService = new LocationDataService();
			locationDataService.getByUUID
					(locationUuid, QueryOptions
							.LOAD_RELATED_OBJECTS, new DataService.GetCallback<Location>() {
						@Override
						public void onCompleted(Location location) {
							//create audit info
							User user = new User();
							user.setUuid(instance.getCurrentLoggedInUserInfo().get(ApplicationConstants.UserKeys
									.USER_UUID));


							//create concept
							Concept concept = new Concept();
							concept.setUuid("162169AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

							//create encountertType
							EncounterType mEncountertype = new EncounterType();
							mEncountertype.setUuid("d7151f82-c1f3-4152-a605-2f9ea7414a79");

							LocalDateTime localDateTime = new LocalDateTime();

							//create observation
							Observation observation = new Observation();
							observation.setConcept(concept);
							observation.setValue(getEditNoteTextValue());
							observation.setPerson(patient.getPerson());
							observation.setObsDatetime(localDateTime.toString());
							observation.setCreator(user);

							observation.setLocation(location.getUuid());

							List<Observation> observationList = new ArrayList<>();
							observationList.add(observation);

							Encounter encounter = new Encounter();
							encounter.setPatient(patient);
							encounter.setEncounterType(mEncountertype);
							encounter.setObs(observationList);
							encounter.setVisit(visit);
							encounter.setEncounterDatetime(localDateTime.toString());

							encounter.setLocation(location);

							EncounterDataService encounterDataService = new EncounterDataService();
							encounterDataService.create(encounter, new DataService.GetCallback<Encounter>() {
								@Override
								public void onCompleted(Encounter encounter) {
									((PatientDashboardActivity)getActivity()).mPresenter
											.fetchPatientData(patient.getUuid());
									dismiss();
								}

								@Override
								public void onError(Throwable t) {
									t.printStackTrace();
								}
							});
						}

						@Override
						public void onError(Throwable t) {

						}
					});

		}
	}

	private void doStartVisitAction() {
		Activity activity = getActivity();
		if (activity instanceof PatientDashboardActivity) {
			PatientDashboardActivity pda = ((PatientDashboardActivity)activity);
			/*List<Fragment> fragments = pda.getSupportFragmentManager().getFragments();
			PatientVisitsFragment fragment = null;
            for (Fragment frag : fragments) {
                if (frag instanceof PatientVisitsFragment) {
                    fragment = (PatientVisitsFragment) frag;
                    break;
                }
            }
            if (fragment != null) {
                fragment.startVisit();
            }*/
		}
	}

	public enum OnClickAction {
		SET_URL,
		SHOW_URL_DIALOG,
		DISMISS_URL_DIALOG,
		DISMISS,
		LOGOUT,
		FINISH,
		INTERNET,
		UNAUTHORIZED,
		END_VISIT,
		START_VISIT,
		LOGIN,
		REGISTER_PATIENT,
		CANCEL_REGISTERING,
		DELETE_PATIENT,
		ADD_VISIT_TASKS,
		SAVE_VISIT_NOTE,
		CREATE_VISIT_NOTE,
		DISPLAY_URL_EDIT_FIELD
	}

	public interface DialogActionClickedListener {
		void onFinish(CustomFragmentDialog.OnClickAction action);
	}
}
