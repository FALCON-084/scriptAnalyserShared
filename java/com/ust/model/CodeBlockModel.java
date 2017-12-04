package com.ust.model;

import java.io.Serializable;
import java.util.*;



public class CodeBlockModel implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int cbid;
	private String lineno;
	private List<String> Query = new ArrayList<>();
	private String action;
	private String graph;
	private List<String> dbSrcTables = new ArrayList<>(); 
	private List<String> dbTargetTables = new ArrayList<>();
	private List<String> dbStAloneTables = new ArrayList<>();
	
	public CodeBlockModel() {
		super();
		// TODO Auto-generated constructor stub
	}


	public List<String> getDbSrcTables() {
		return dbSrcTables;
	}


	public void setDbSrcTables(List<String> dbSrcTables) {
		this.dbSrcTables = dbSrcTables;
	}


	public List<String> getDbTargetTables() {
		return dbTargetTables;
	}


	public void setDbTargetTables(List<String> dbTargetTables) {
		this.dbTargetTables = dbTargetTables;
	}


	public List<String> getDbStAloneTables() {
		return dbStAloneTables;
	}


	public void setDbStAloneTables(List<String> dbStAloneTables) {
		this.dbStAloneTables = dbStAloneTables;
	}


	public int getCbid() {
		return cbid;
	}


	public void setCbid(int cbid) {
		this.cbid = cbid;
	}


	public String getLineno() {
		return lineno;
	}


	public void setLineno(String lineno) {
		this.lineno = lineno;
	}


	public List<String> getQuery() {
		return Query;
	}


	public void setQuery(List<String> query) {
		this.Query = query;
	}


	public String getAction() {
		return action;
	}


	public void setAction(String action) {
		this.action = action;
	}


	public String getGraph() {
		return graph;
	}


	public void setGraph(String graph) {
		this.graph = graph;
	}


}
