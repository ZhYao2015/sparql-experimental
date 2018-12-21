package org.semanticweb.wolpertinger.sparql;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.Path;

//This Class is for generating query-independent axioms
//like: background knowledges
public class Axioms {
	
	private static PrintWriter pw=Fixed.getPw();
	
	private static boolean set_bound=false;
	private static boolean set_basejoin=false;
	private static boolean set_zero_dist_path=false;
	private static boolean set_spo=false;
	
	private static int join_length=0;
	
	private static List<Path> path_plus_list=new ArrayList<Path>();

	public static boolean checkInPathPlusList(Path path) {
		if(path_plus_list.contains(path)){
			return true;
		}
		return false;
	}
	
	public static int getJoin_length() {
		return join_length;
	}

	
	
	public static void setJoin_length(int join_length) {
		Axioms.join_length = join_length;
	}
	
	public static void resetJoin_length() {
		Axioms.join_length=0;
	}
	
	public static void setBaseJoin() {
		setSPO();
		if(set_basejoin==false) {
			pw.println("join(X,X,X):-spo(X).\njoin(X,null,X):-spo(X).\njoin(null,X,X):-spo(X).\njoin(null,null,null).");
			set_basejoin=true;
		}
	}
	
	public static void setSPO() {
		if(set_spo==false) {
			pw.println("spo(X):-domain(X).\nspo(X):-class(X).\nspo(X):-property(X).");
			set_spo=true;
		}
		
	}
	
	public static void setTerms() {
		//pw.println("term(X):-triple(X,_,_).\nterm(X):-triple(_,_,X).");
	}

	public static void setJoinAxiom(int length) {
		setBaseJoin();
		
		if(length==1) {
			pw.println("join_1(X_1,X_2,X):-join(X_1,X_2,X).");
		}else {
			List<String> l1=new ArrayList<String>();
			for(int i=1;i<=length;i++) {
				l1.add("X"+Integer.toString(i));
			}
			String join=Axioms.createSingleJoin(l1);
			String th=l1.get(length-1);
			l1.remove(length-1);
			String left=Axioms.createSingleJoin(l1);
			String right="join("+th+"_1,"+th+"_2,"+th+")";
			pw.println(join+":-"+left+","+right+".");
		}
	}
	
	public static String createSingleJoin(List<String> l) {
			int len=l.size();
			String join="join_"+Integer.toString(len)+"(";
			String suffix="";
			for(int i=2;i>=0;i--) {
				suffix="_"+Integer.toString(i);
				if(i==0) {
					suffix="";
				}
				for(int j=0;j<len;j++) {
					join+=l.get(j)+suffix;
					if(!(i==0&&j==len-1)) {
						join+=",";
					}
				}
			}
			join+=")";
			return  join;
		}

	
	
	public static void setTrivialFilter(){
		pw.println("filter(true).");
	}
	
	public static void setAuxTriple() {
		pw.println("triple(auxx,auxy,auxz).");
		pw.println("bound(auxx).");
		pw.println("bound(auxy).");
		pw.println("bound(auxz).");
	}
	
	public static void setBackgroundKnowledges() {
		setTerms();
		showAns();
		setTrivialFilter();
		//setBounds();
		//setZeroDistPath();
		setAuxTriple();
	}
	
	
	
	public static void setBounds() {
		if(!set_bound) {
			set_bound=true;
			pw.println("bound(X):-spo(X).");
		}
	}
	
	public static void setZeroDistPath() {
		if(!set_zero_dist_path) {
			pw.println("triple0(S,S):-domain(S).");
			set_zero_dist_path=true;
		}
	}
		
	
	public static void showAns() {
		//pw.println("#show ans_0/1.");
		pw.println("#show ans_0/"+Integer.toString(Fixed.getAns_length())+".");
	}
	
	public static void setPathPlusAxioms(Path path) {
		if(path instanceof P_OneOrMore1) {
			Path pSub=((P_OneOrMore1)path).getSubPath();
			
			String str_path=Tools.processPath(path);
			String str_pSub=Tools.processPath(pSub);
			
			String axiom_base="triplePath(X,"+"\""+str_path+"\""+",Y):-triplePath(X,"+"\""
					+ str_pSub+"\",Y).";
			String axiom_deductive="triplePath(X,"+"\""+str_path+"\""+",Y):-triplePath(X,"+"\""
					+ str_path+"\",Z), triplePath(Z,"+"\""+str_pSub+"\""+",Y).";
			
			pw.println(axiom_base);
			pw.println(axiom_deductive);
			
			path_plus_list.add(pSub);
		}
	}
}
