package org.semanticweb.wolpertinger.sparql;

import java.util.HashSet;
import java.util.Set;

public class Tuple {
	private String ans;
	private Set<String> vars_set=new HashSet<String>();
	
	public Tuple(String ans,Set<String> vars_set) {
		this.ans=ans;
		this.vars_set.addAll(vars_set);
	}
	
	public String getAns() {
		return ans;
	}
	
	public Set<String> getVar_set() {
		return vars_set;
	}
	
}
