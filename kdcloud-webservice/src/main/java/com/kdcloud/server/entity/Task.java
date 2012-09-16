package com.kdcloud.server.entity;

import java.io.InputStream;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.datanucleus.annotations.Unowned;
import com.kdcloud.lib.domain.Report;

@PersistenceCapable
public class Task {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
	private String encodedKey;

	@Persistent
    @Extension(vendorName="datanucleus", key="gae.pk-id", value="true")
	Long id;
	
	@Persistent
	@Unowned
	DataTable workingTable;
	
	@Persistent
	@Unowned
	User applicant;
	
	
	@Persistent
	@Unowned
	Workflow workflow;
	
	@Persistent(serialized = "true")
	Report report;
	
	public Task(DataTable table, Workflow workflow) {
		this.workingTable = table;
		this.workflow = workflow;
	}
	
	public Task() {
		// TODO Auto-generated constructor stub
	}

	public String getEncodedKey() {
		return encodedKey;
	}

	public void setEncodedKey(String encodedKey) {
		this.encodedKey = encodedKey;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DataTable getWorkingTable() {
		return workingTable;
	}

	public void setWorkingTable(DataTable workingTable) {
		this.workingTable = workingTable;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}

	public User getApplicant() {
		return applicant;
	}

	public void setApplicant(User applicant) {
		this.applicant = applicant;
	}

	public InputStream getStream() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
