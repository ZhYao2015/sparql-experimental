package org.semanticweb.wolpertinger.sparql;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Distinct;
import org.apache.jena.sparql.path.P_FixedLength;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Mod;
import org.apache.jena.sparql.path.P_Multi;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_OneOrMoreN;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_Shortest;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.P_ZeroOrMoreN;
import org.apache.jena.sparql.path.P_ZeroOrOne;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.path.PathVisitor;

public class SPARQLPathVisitor implements PathVisitor {

	private PrintWriter pw = Fixed.getPw();




	private String ans = "";

	//private boolean zero = true;

	//fixed-domain[a-->>G_a]
	//If fd_subject is a variable? we need to eliminate the quotes
	private String fd_subject="";
	
	private String fd_object="";
	
	private List<String> gs=new ArrayList<String>();
	
//	public boolean getZero() {
//		return zero;
//	}




	public SPARQLPathVisitor(String subject,String object,List<String> gs) {
		this.fd_subject=subject;
		this.fd_object=object;
		this.gs.addAll(gs);
	}


	public void set_Ans(String ans) {
		this.ans = ans;
	}

	public String getAns() {
		return this.ans;
	}

	public void visit(P_Link pathNode) {
		// TODO Auto-generated method stub
		// System.out.println("This is a P_Link:"+pathNode.toString());
		String str_plink = Tools.processPath(pathNode);
		//zero = false;
		String axiom="triplePath("+this.fd_subject+"," + "\"" + str_plink + "\"" + "," + this.fd_object+"):-" + ""
				+ "triple("+this.fd_subject+"," + "\"" + str_plink + "\"" + ","
				+this.fd_object+")";

		pw.println(Tools.appendFixedDomain(axiom, this.gs));
	}

	public void visit(P_ReverseLink pathNode) {
		// TODO Auto-generated method stub
		System.out.println("This is a P_ReverseLink:" + pathNode.toString());
	}

	public void visit(P_NegPropSet pathNotOneOf) {
		// TODO Auto-generated method stub
		System.out.println("This is a P_NegPropSet:" + pathNotOneOf.toString());
	}

	public void visit(P_Inverse inversePath) {
		// TODO Auto-generated method stub
		//System.out.println("This is a P_Inverse:" + inversePath.toString());
		Path pSource=inversePath.getSubPath();
		
		String str_inv=Tools.processPath(inversePath);
		String str_pS=Tools.processPath(pSource);
		SPARQLPathVisitor vp=new SPARQLPathVisitor(this.fd_object,this.fd_subject,this.gs);
		pSource.visit(vp);
		
		String axiom="triplePath("+this.fd_subject+"," + "\"" + str_inv + "\"" + "," + this.fd_object+"):-" + ""
				+ "triplePath("+this.fd_object+"," + "\"" + str_pS + "\"" + ","
				+this.fd_subject+").";

		pw.println(axiom);
	}

	public void visit(P_Mod pathMod) {
		// TODO Auto-generated method stub
		System.out.println("This is a P_Mod:" + pathMod.toString());
	}

	public void visit(P_FixedLength pFixedLength) {
		// TODO Auto-generated method stub
		System.out.println("This is a P_FixedLength:" + pFixedLength.toString());
	}

	public void visit(P_Distinct pathDistinct) {
		// TODO Auto-generated method stub
		System.out.println("This is a P_Distinct:" + pathDistinct.toString());
	}

	public void visit(P_Multi pathMulti) {
		// TODO Auto-generated method stub
		System.out.println("This is a P_Multi:" + pathMulti.toString());
	}

	public void visit(P_Shortest pathShortest) {
		// TODO Auto-generated method stub
		System.out.println("This is a P_Shortest:" + pathShortest.toString());
	}

	public void visit(P_ZeroOrOne path) {
		// TODO Auto-generated method stub
		//zero = true;
		System.out.println("This is a P_ZeroOrOne:" + path.toString());
		String str_p01=Tools.processPath(path);
		Path pSub=path.getSubPath();
		String str_pSub =Tools.processPath(pSub);
		
		SPARQLPathVisitor vp = new SPARQLPathVisitor(this.fd_subject,this.fd_object,this.gs);
		String axiom1="triplePath("+this.fd_subject+"," + "\"" + str_p01 + "\","+this.fd_object+"):-" + "triple0("+this.fd_subject+","
				+ ""+this.fd_object+")";		
		pw.println(Tools.appendFixedDomain(axiom1, this.gs));

		String axiom2="triplePath("+this.fd_subject+"," + "\"" + str_p01 + "\","+this.fd_object+"):-" + "triplePath("+this.fd_subject+","
				+ str_pSub +","+ this.fd_object+")";
		pw.println(Tools.appendFixedDomain(axiom2, this.gs));
		pSub.visit(vp);
	}

	public void visit(P_ZeroOrMore1 path) {
		// TODO Auto-generated method stub
		//zero = true;
		//System.out.println("This is a P_ZeroOrMore1:" + path.toString());
		
		String str_p0X=Tools.processPath(path);
		Path pSub = path.getSubPath();
		
		Path pTrans = PathFactory.pathOneOrMore1(pSub);
		String str_pTrans=Tools.processPath(pTrans);
		
		SPARQLPathVisitor vp = new SPARQLPathVisitor(this.fd_subject,this.fd_object,this.gs);
		pTrans.visit(vp);
		
		
		String axiom1="triplePath("+this.fd_subject +","+"\""+str_p0X+"\","+this.fd_object+"):-triple0("+this.fd_subject+","
				+ ""+this.fd_object+")";
		
		pw.println(Tools.appendFixedDomain(axiom1, this.gs));

		String axiom2="triplePath("+this.fd_subject +","+"\""+str_p0X+"\","+this.fd_object+"):-triplePath("+this.fd_subject+","
				+ "\""+str_pTrans+"\","+this.fd_object+")";
		pw.println(Tools.appendFixedDomain(axiom2, this.gs));

		
	}

	public void visit(P_ZeroOrMoreN path) {
		// TODO Auto-generated method stub
		//zero = true;

		System.out.println("This is a P_ZeroOrMoreN:" + path.toString());

	}

	//circling?!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//consider the reachability!
	public void visit(P_OneOrMore1 path) {
		// TODO Auto-generated method stub
		// System.out.println("This is a P_OneOrMore1:"+path.toString());
		Path pSub = path.getSubPath();
		
		String str_path = Tools.processPath(path);
		
		//generate fresh variable
		List<String> used=new ArrayList<String> ();
		used.add(this.fd_subject);
		used.add(this.fd_object);
		String f_var=Tools.Var_Issuer_Path(used);
		used.add(f_var);
		String f_var1=Tools.Var_Issuer_Path(used);
		
		//(s,path,o)
		SPARQLPathVisitor vp1 = new SPARQLPathVisitor("X","Y",this.gs);
		pSub.visit(vp1);
		
		if(Axioms.checkInPathPlusList(pSub)==false) {
			Axioms.setPathPlusAxioms(path);
		}
		
		String axiom="triplePath("+this.fd_subject+","+"\""+str_path+"\""+","+this.fd_object+"):-"
				+ "triplePath("+f_var+","+"\""+str_path+"\""+","+f_var1+"),"
				+ ""+f_var+"="+this.fd_subject+","+f_var1+"="+this.fd_object;
		pw.println(Tools.appendFixedDomain(axiom,this.gs));
	}

	public void visit(P_OneOrMoreN path) {
		// TODO Auto-generated method stub
		//zero = true;
		System.out.println("This is a P_OneOrMoreN:" + path.toString());
	}

	// similar to the path seq
	public void visit(P_Alt pathAlt) {
		// TODO Auto-generated method stub
		// System.out.println("This is a P_Alt:"+pathAlt.toString());

		String str_pAlt = Tools.processPath(pathAlt);
		
		Path pLeft = pathAlt.getLeft();
		Path pRight = pathAlt.getRight();
		
		String str_pL = Tools.processPath(pLeft);
		String str_pR = Tools.processPath(pRight);

		// judge whether left or right is a type of prop*
		SPARQLPathVisitor vpl = new SPARQLPathVisitor(this.fd_subject,this.fd_object,this.gs);
		SPARQLPathVisitor vpr = new SPARQLPathVisitor(this.fd_subject,this.fd_object,this.gs);
		pLeft.visit(vpl);
		pRight.visit(vpr);
		
		//zero = (vpl.getZero() || vpr.getZero());
		String axiomL = "triplePath("+this.fd_subject + ",\"" + str_pAlt + "\","+this.fd_object+"):-"
				+ "triplePath("+this.fd_subject + ",\"" + str_pL + "\","+this.fd_object+")";
		String axiomR = "triplePath("+this.fd_subject + ",\"" + str_pAlt + "\","+this.fd_object+"):-"
				+ "triplePath("+this.fd_subject + ",\"" + str_pR + "\","+this.fd_object+")";

		pw.println(Tools.appendFixedDomain(axiomL, this.gs));
		pw.println(Tools.appendFixedDomain(axiomR, this.gs));

	}

	// hard to deal with ans:-p1*,p2*,p3.

	// public boolean test_zero(Path path) {
	// boolean result=false;
	// if(path instanceof P_ZeroOrMoreN) {
	// result=true;
	// }
	// if(path instanceof P_ZeroOrMore1) {
	// result=true;
	// }
	// if(path instanceof P_ZeroOrOne) {
	// result=true;
	// }
	// return result;
	// }

	public void visit(P_Seq pathSeq) {
		// TODO Auto-generated method stub
		// System.out.println("This is a P_Seq:"+pathSeq.toString());
		String str_pSeq = Tools.processPath(pathSeq);
		
		Path pLeft = pathSeq.getLeft();
		Path pRight = pathSeq.getRight();
		
		String str_pL = Tools.processPath(pLeft);
		String str_pR = Tools.processPath(pRight);

		//generate fresh variable
		List<String> used=new ArrayList<String> ();
		used.add(this.fd_subject);
		used.add(this.fd_object);
		String f_var=Tools.Var_Issuer_Path(used);
		
		SPARQLPathVisitor vpl = new SPARQLPathVisitor(this.fd_subject,f_var,this.gs);
		SPARQLPathVisitor vpr = new SPARQLPathVisitor(f_var,this.fd_object,this.gs);
		pLeft.visit(vpl);
		pRight.visit(vpr);
		//zero = (vpl.getZero() && vpr.getZero());
		
		String axiom="triplePath("+this.fd_subject+","+"\""+str_pSeq+"\""+","+this.fd_object+")"
				+ ":-triplePath("+this.fd_subject+","+"\""+str_pL+"\""+","+f_var+"),"
				+ "triplePath("+f_var+","+"\""+str_pR+"\""+","+this.fd_object+")";
		
		pw.println(Tools.appendFixedDomain(axiom, this.gs));
	}
}
