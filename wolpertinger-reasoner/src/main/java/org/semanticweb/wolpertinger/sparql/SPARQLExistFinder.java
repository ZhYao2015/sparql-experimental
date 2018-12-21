package org.semanticweb.wolpertinger.sparql;

import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_FunctionDynamic;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprFunction0;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprFunction3;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVisitor;
import org.apache.jena.sparql.expr.NodeValue;

//Find the first Exist or Not_Exist in Expr
public class SPARQLExistFinder implements ExprVisitor {

	private ExprFunction found;
	private Expr rec;
	private boolean mode;
	private boolean replace;
	
	
	public ExprFunction getFound() {
		return this.found;
	}
	
	public Expr getRec() {
		return this.rec;
	}
	
	public SPARQLExistFinder(boolean mode,boolean replace) {
		this.found=null;
		this.rec=null;
		this.mode=mode;
		this.replace=replace;
	}
	
	
	
	public void startVisit() {
		// TODO Auto-generated method stub
		
	}

	public void visit(ExprFunction0 func) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ExprFunction1 func) {
		// TODO Auto-generated method stub
		Expr ex=func.getArg(1);
		SPARQLExistFinder vf=new SPARQLExistFinder(mode,replace);
		ex.visit(vf);
		this.rec=remake(func,vf.rec);
		this.found=vf.found;
	}

	public ExprFunction2 remake(ExprFunction2 func,Expr left,Expr right) {
		if(func instanceof E_LogicalAnd) {
			return new E_LogicalAnd(left,right);
		}else if(func instanceof E_LogicalOr) {
			return new E_LogicalOr(left,right);
		}else if(func instanceof E_Equals) {
			return new E_Equals(left,right);
		}else if(func instanceof E_NotEquals) {
			return new E_NotEquals(left,right);
		}else {
			return func;
		}
	}
	
	public ExprFunction1 remake(ExprFunction1 func,Expr sub) {
		if(func instanceof E_LogicalNot) {
			return new E_LogicalNot(sub);
		}else {
			return func;
		}
	}
	
	public void visit(ExprFunction2 func) {
		// TODO Auto-generated method stub
		Expr exprL=func.getArg(1);
		Expr exprR=func.getArg(2);
		SPARQLExistFinder vf=new SPARQLExistFinder(mode,replace);
		exprL.visit(vf);
		if(vf.found!=null) {
			this.rec=remake(func,vf.rec,exprR);
			this.found=vf.found;
		}else {
			SPARQLExistFinder vff=new SPARQLExistFinder(mode,replace);
			exprR.visit(vff);
			
			this.rec=remake(func,exprL,vff.rec);
			this.found=vff.found;
		}
	}

	public void visit(ExprFunction3 func) {
		// TODO Auto-generated method stub

	}

	public void visit(ExprFunctionN func) {
		// TODO Auto-generated method stub

	}

	public void visit(ExprFunctionOp funcOp) {
		// TODO Auto-generated method stub
		this.found=funcOp;
		if(mode) {
			//search mode
			this.rec=funcOp;
		}else {
			this.rec=Tools.getBooleanExpr(replace);
		}
		
	}

	public void visit(NodeValue nv) {
		// TODO Auto-generated method stub

	}

	public void visit(ExprVar nv) {
		// TODO Auto-generated method stub

	}

	public void visit(ExprAggregator eAgg) {
		// TODO Auto-generated method stub

	}

	public void finishVisit() {
		// TODO Auto-generated method stub

	}

}
