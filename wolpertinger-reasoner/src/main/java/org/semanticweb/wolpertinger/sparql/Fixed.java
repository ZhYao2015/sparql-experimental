package org.semanticweb.wolpertinger.sparql;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.shared.PrefixMapping;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

public class Fixed {
	private static PrintWriter pw;

	private static PrefixMapping prefixMap;
	
	private static Set<OWLNamedIndividual> domain=new HashSet<OWLNamedIndividual> ();
	
	private static int ans_length=0;
	
	private static int aux_level=0;
	
	private static int theta_level=0;
	
	public static int get_aux_level() {
		return aux_level;
	}
	
	public static int get_next_theta_level() {
		int current_level=theta_level++;
		return current_level;
	}
	
	public static int get_next_level() {
		aux_level++;
		return aux_level;
	}
	
	public static void setAns_length(int length) {
		ans_length=length;
	}
	
	public static int getAns_length() {
		return ans_length;
	}
	
	public static Set<OWLNamedIndividual> get_domain() {
		return domain;
	}

	public static void reset_domain() {
		domain=new HashSet<OWLNamedIndividual> ();
	}
	
	public static void set_domain(Set<OWLNamedIndividual> _domain) {
		reset_domain();
		domain.addAll(_domain);
	}

	public static PrefixMapping getPrefixMap() {
		return prefixMap;
	}

	public static void setPrefixMap(PrefixMapping _prefixMap) {
		prefixMap = _prefixMap;
	}

	public static PrintWriter getPw() {
		return pw;
	}

	public static void setPw(PrintWriter _pw) {
		pw = _pw;
	}
	
	
	
	
}
