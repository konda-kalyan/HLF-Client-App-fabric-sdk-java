package com.example.hlfclient;

import java.io.Serializable;
import java.util.Set;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

public class MyUser_Not_Needed implements User, Serializable {
	
	protected String name;
	protected Set<String> roles;
	protected String account;
	protected String affiliation;
	protected Enrollment enrollment;
	protected String mspId;
	
	MyUser_Not_Needed() {
		this.name = null;
		this.mspId = null;
		
	}
	
	MyUser_Not_Needed(String name, String mspId) {
		this.name = name;
		this.mspId = mspId;
		
	}
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
		return name;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public String getAccount() {
		return account;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public Enrollment getEnrollment() {
		return enrollment;
	}

	public String getMspId() {
		return mspId;
	}
}

