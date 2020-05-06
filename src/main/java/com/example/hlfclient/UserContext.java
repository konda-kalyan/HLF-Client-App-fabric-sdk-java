package com.example.hlfclient;

import java.io.Serializable;
import java.util.Set;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

public class UserContext implements User, Serializable {
	private static final long serialVersionUID = 1L;
	protected String name;
	protected Set<String> roles;
	protected String account;
	protected String affiliation;
	protected Enrollment enrollment;
	protected String mspId;

	public void setName(String name) {
		this.name = name;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public void setEnrollment(Enrollment enrollment) {
		this.enrollment = enrollment;
	}

	public void setMspId(String mspId) {
		this.mspId = mspId;
	}

	public String getName() {
		return this.name;
	}

	public Set<String> getRoles() {
		return this.roles;
	}

	public String getAccount() {
		return this.account;
	}

	public String getAffiliation() {
		return this.affiliation;
	}

	public Enrollment getEnrollment() {
		return this.enrollment;
	}

	public String getMspId() {
		return this.mspId;
	}
}