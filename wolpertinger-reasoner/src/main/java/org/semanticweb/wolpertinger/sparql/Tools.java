package org.semanticweb.wolpertinger.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AggAvgDistinct;
import org.apache.jena.sparql.expr.aggregate.AggCountDistinct;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.expr.aggregate.AggMaxDistinct;
import org.apache.jena.sparql.expr.aggregate.AggMinDistinct;
import org.apache.jena.sparql.expr.aggregate.AggSampleDistinct;
import org.apache.jena.sparql.expr.aggregate.AggSumDistinct;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.wolpertinger.Prefixes;

public class Tools {
	
	private static final IRI RDF_TYPE_IRI = IRI.create(Prefixes.STANDARD_PREFIXES.getPrefixIRI("rdf:").concat("type"));

	
	//vars in filter are all excluded from fixedvars and mentioned vars 
		
	public static Set<String> getFixedVars(Op op){
		Set<Var> l=OpVars.fixedVars(op);
		Set<String> vars=new HashSet<String> ();
		
		for(Var v:l) {
			vars.add(Tools.getProcessedNode(v, new ArrayList<String> ()));
		}
		return vars;
	}
//	
	public static Set<String> getMentionedVars(Op op){
		Collection<Var> l=OpVars.mentionedVars(op);
		Set<String> vars=new HashSet<String> ();
		for(Var v:l) {
			vars.add(Tools.getProcessedNode(v, new ArrayList<String> ()));
		}
		return vars;
	}
//	
	public static Set<String> getExprVars(Expr expr){
		Set<Var> l=ExprVars.getVarsMentioned(expr);
		Set<String> vars=new HashSet<String> ();
		for(Var v:l) {
			vars.add(Tools.getProcessedNode(v, new ArrayList<String> ()));
		}
		return vars;
	}
	
	//skip the fixed-domain issues for predicates 
	public static String processPath(Path path) {
		if(path instanceof P_Link) {
			return ((P_Link) path).getNode().getURI();
		}else {
			return path.toString();
		}
	}
	
	public static Collection<String> toVarNameWithV(Collection<Var> vars){
		Set<String> result=new HashSet<String>();
		for(Var var:vars) {
			result.add(Tools.getProcessedNode(var, new ArrayList<String> ()));
		}
		return result;
	}
	
	
	public static String getFullURI(Node node) {
		return "\""+node.getURI()+"\"";
	}
	
	public static String toHashedString(String str) {
		int code=str.hashCode();
		if(code<0) {
			return "_"+Integer.toString(code).substring(1);
		}else {
			return Integer.toString(code);
		}
	}
	
	//only for individuals
	public static String getFDNominalClassName(Node node_indv) {
			String uri=node_indv.getURI().toString();
			Set<OWLNamedIndividual> guards=Fixed.get_domain();
			boolean found=false;
			for(OWLNamedIndividual o:guards) {
				if(o.getIRI().toString().equals(node_indv.getURI().toString())) {
					found=true;
					break;
				}
			}
			if(!found) {
				return "guard_"+Tools.toHashedString(uri);
			}else {
				return node_indv.getURI();
			}
			
	}
	
	public static String getProcessedNode(Node node,List<String> gs) {
		if(node.isBlank()) {
			return "B_"+node.getBlankNodeId();
		}else if(node.isVariable()) {
			if(node.getName().startsWith(".")) {
				return "Vagg_"+node.getName().substring(1);
			}else {
				return "V_"+node.getName();
			}
			
		}else if(node.isURI()) {
			//individuals!!!
			//for anonymous individuals???
			if(gs==null) {
				//is a class
				return getFullURI(node);
			}else {
				//is an individual
				String guard=getFDNominalClassName(node);
				
				if(guard.startsWith("guard_")) {
					//is not one of the fixed-domain individual
					String str=node.getURI().toString();
					
					String tmp_var="G_"+Tools.toHashedString(str);
					gs.add(guard+"("+tmp_var+")");
					return tmp_var;
				}else {
					//is one of the fixed-domain individual
					return getFullURI(node);
				}
			}	
		}else if(node.isLiteral()) {
			//without datatype
			return node.getLiteralValue().toString();
		}else{
			return null;
		}
	}
	
	public static String generateFDTriplePathAtoms(TriplePath t) {
		String triplePath="";
		List<String> gs=new ArrayList<String> ();
	    
		String subject=getProcessedNode(t.getSubject(), gs);
	    String path="\""+t.getPath().toString()+"\"";
	    String object=getProcessedNode(t.getObject(),gs);
	    
	    triplePath+="triplePath("+subject+","+path+","+object+")";
	    if(!gs.isEmpty()) {
	       for(int i=0;i<gs.size();i++) {
	    	   triplePath+=",";
	    	   triplePath+=gs.get(i);
	       }
	    }else {
	    	
	    }
		return triplePath;
	}
	
	public static String generateFDTripleAtoms(Triple t) {
		String triple_fd="";
		List<String> gs=new ArrayList<String> ();
	    
		String subject=getProcessedNode(t.getSubject(), gs);
	    String predicate=getProcessedNode(t.getPredicate(),null);
	    String object;
	    if(predicate.equals("\""+RDF_TYPE_IRI.toString()+"\"")) {
	    	object=getProcessedNode(t.getObject(),null);
	    }else {
	    	object=getProcessedNode(t.getObject(),gs);
	    }
	    
	    triple_fd+="triple("+subject+","+predicate+","+object+")";
	    if(!gs.isEmpty()) {
	       for(int i=0;i<gs.size();i++) {
	    	   triple_fd+=",";
	    	   triple_fd+=gs.get(i);
	       }
	    }else {
	    	
	    }
		return triple_fd;
	}
	
	//ans,filter,etc.
	public static String generateNonTripleAtoms(String pre,List<String> vars,int ix,int iy) {
		String atom="";
		if(iy==-1) {
			atom=pre+"_"+Integer.toString(ix);
			if(vars.isEmpty()) {
				
			}else {
				atom+="(";
				for(int i=0;i<vars.size();i++) {
					atom+=vars.get(i);
					if(i!=vars.size()-1) {
						atom+=",";
					}
				}
			
				atom+=")";
			}
		}else{
			atom=pre+"_"+Integer.toString(ix)+"_"+Integer.toString(iy);
			if(vars.isEmpty()) {
				
			}else {
				atom+="(";
				for(int i=0;i<vars.size();i++) {
					atom+=vars.get(i);
					if(i!=vars.size()-1) {
						atom+=",";
					}
				}
				atom+=")";
			}
		}
		return atom;
	}
	
	public static Set<String> set_intersection(Set<String> u1,Set<String> u2){
		Set<String> result=new HashSet<String> ();
		for(String s:u1) {
			if(u2.contains(s)) {
				result.add(s);
			}
		}
		return result;
	}
	
	//calculate u1 setminus u2
	public static List<String> set_difference(List<String> u1,List<String> u2){
		List<String> result=new ArrayList<String> ();
		for(String s:u1) {
			if(u2.contains(s)) {
				
			}else {
				result.add(s);
			}
		}
		return result;
	}
	
	//replace all elments in u setminus uprime by null
	public static List<String> completeNull(List<String> u,List<String> u_prime){
		List<String> result=new ArrayList<String> (u);
		for(int i=0;i<result.size();i++) {
			if(u_prime.contains(result.get(i))) {
				
			}else {
				result.set(i,"null");
			}
		}
		return result;
	}
	
	public static List<String> replaceNull(List<String> u,List<String> u_prime){
		List<String> result=new ArrayList<String>(u);
		for(int i=0;i<result.size();i++) {
			if(u_prime.contains(result.get(i))) {
				result.set(i,"null");
			}else {
				
			}
		}
		return result;
	}	
	public static List<String> rename_vars(List<String> u,List<String> o,String suffix){
		List<String> result=new ArrayList<String>(u);
		for(String s:o) {
			for(int i=0;i<result.size();i++) {
				if(s.equals(result.get(i))) {
					String str=result.get(i)+"_"+suffix;
					result.set(i,str);
				}
			}
		}
		return result;
	}
	
	public static Expr getBooleanExpr(boolean b) {
		if(b) {
			return NodeValue.TRUE;
		}else {
			return NodeValue.FALSE;
		}
		
	}
	
	
	
	
	public static Expr toConjuncts(ExprList list) {
		//if list.size()==0 exceptions!
		if(list==null) {
			return getBooleanExpr(true);
		}else {
			if(list.size()==1) {
				return list.get(0);
			}else {
				Expr result=list.get(0);
				for(int i=1;i<list.size();i++) {
					result=new E_LogicalAnd(result,list.get(i));
				}
				return result;
			}
		}
		
	}
	
	public static String appendFixedDomain(String axiom,List<String> gs) {
		for(int i=0;i<gs.size();i++) {
			axiom+=",";
			axiom+=gs.get(i);
		}
		axiom+=".";
		return axiom;
	}
	
	public static String Var_Issuer_Path(List<String> used) {
		String tmp="X";
		while(used.contains(tmp)) {
			tmp=tmp+"1";
		}
		return tmp;
	}
	
	public static boolean isDistinctAgg(Aggregator agg) {

		if(agg instanceof AggCountVarDistinct){
				return true;
		}else if(agg instanceof AggMaxDistinct) {
				return true;
		}else if(agg instanceof AggMinDistinct) {
				return true;
		}else if(agg instanceof AggSumDistinct) {
				return true;
		}else if(agg instanceof AggCountDistinct) {
				return true;
		}else if(agg instanceof AggAvgDistinct) {
				return true;
		}else if(agg instanceof AggSampleDistinct) {
				return true;
		}else {
				return false;
		}
	}
	
	public static String createCompatibility(String pre,List<String> l,int index) {
		int len=l.size();
		String comp=pre+"_"+Integer.toString(index)+"(";
		String suffix="";
		for(int i=2;i>=1;i--) {
			suffix="_"+Integer.toString(3-i);
			for(int j=0;j<len;j++) {
				comp+=l.get(j)+suffix;
				if(!(i==1&&j==len-1)) {
					comp+=",";
				}
			}
		}
		comp+=")";
		return  comp;
	}
	
}
