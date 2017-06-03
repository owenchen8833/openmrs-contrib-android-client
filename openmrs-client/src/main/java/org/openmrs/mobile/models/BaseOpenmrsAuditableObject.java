package org.openmrs.mobile.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;

import java.util.Date;

public class BaseOpenmrsAuditableObject extends BaseOpenmrsObject {
	@SerializedName("creator")
	@Expose
	@ForeignKey(stubbedRelationship = true)
	private User creator;

	@SerializedName("dateCreated")
	@Expose
	@Column
	private Date dateCreated;

	@SerializedName("changedBy")
	@Expose
	@ForeignKey(stubbedRelationship = true)
	private User changedBy;

	@SerializedName("dateChanged")
	@Expose
	@Column
	private Date dateChanged;

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public User getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
}
