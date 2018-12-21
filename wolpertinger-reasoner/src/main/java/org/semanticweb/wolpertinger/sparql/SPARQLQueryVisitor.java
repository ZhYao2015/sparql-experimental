package org.semanticweb.wolpertinger.sparql;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryVisitor;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Var;

public class SPARQLQueryVisitor implements QueryVisitor{
	
	private PrintWriter pw=Fixed.getPw();
	
	
	public void startVisit(Query query) {
		// TODO Auto-generated method stub
		
	}

	public void visitPrologue(Prologue prologue) {
		// TODO Auto-generated method stub
		
	}

	public void visitResultForm(Query query) {
		// TODO Auto-generated method stub
		
		
	}

	public void visitSelectResultForm(Query query) {
		// TODO Auto-generated method stub
		Op op=Algebra.compile(query);
		SPARQLPatternVisitor vp=new SPARQLPatternVisitor(1);
		op.visit(vp);
		Set<Var> s_args=OpVars.fixedVars(op);
		List<String> arg=new ArrayList<String>();
		
		for(Var v:s_args) {
			arg.add(Tools.getProcessedNode(v,new ArrayList<String>()));
		}		
		String left=Tools.generateNonTripleAtoms("ans",arg,0, -1);
		Fixed.setAns_length(Tools.getFixedVars(op).size());
		String right=vp.getAns();
		String result=left+":-"+right+".";
		pw.println(result);
		
	}

	public void visitConstructResultForm(Query query) {
		// TODO Auto-generated method stub
		
	}

	public void visitDescribeResultForm(Query query) {
		// TODO Auto-generated method stub
		
	}

	public void visitAskResultForm(Query query) {
		// TODO Auto-generated method stub
		
	}

	public void visitDatasetDecl(Query query) {
		// TODO Auto-generated method stub
		
	}

	public void visitQueryPattern(Query query) {
		// TODO Auto-generated method stub
		
	}

	public void visitGroupBy(Query query) {
		// TODO Auto-generated method stub
		
	}

	public void visitHaving(Query query) {
		// TODO Auto-generated method stub
		
	}

	public void visitOrderBy(Query query) {
		// TODO Auto-generated method stub
		
	}

	public void visitLimit(Query query) {
		// TODO Auto-generated method stub
		
	}

	public void visitOffset(Query query) {
		// TODO Auto-generated method stub
		
	}

	public void visitValues(Query query) {
		// TODO Auto-generated method stub
		
	}

	public void finishVisit(Query query) {
		// TODO Auto-generated method stub
		
	}
	
}
