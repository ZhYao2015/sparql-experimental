package org.semanticweb.wolpertinger.sparql;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_SameTerm;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprFunction0;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprFunction3;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVisitor;
import org.apache.jena.sparql.expr.NodeValue;

public class SPARQLFilterExprVisitor implements ExprVisitor{

	private PrintWriter pw=Fixed.getPw();
	
	private List<String> guards;
	
	private String ans="";
	
	private int ix;
	private int iy;
	private String safe="";
	
	public SPARQLFilterExprVisitor(String safe,int ix,int iy) {
		this.safe=safe;
		this.ix=ix;
		this.iy=iy;
		this.guards=new ArrayList<String> ();
	}
	
	public List<String> getGuards() {
		return guards;
	}




	public void setGuards(List<String> guards) {
		this.guards = guards;
	}

	public String getAns() {
		return ans;
	}

	public void setAns(String ans) {
		this.ans = ans;
	}

	public void startVisit() {
		// TODO Auto-generated method stub
		
	}

	public void visit(ExprFunction0 func) {
		// TODO Auto-generated method stub
		
	}
	
	//unary predicate:!, isBlank,isLiteral...
	public void visit(ExprFunction1 func) {
		// TODO Auto-generated method stub
		
		SPARQLFilterExprVisitor ve=new SPARQLFilterExprVisitor(safe,ix,iy*2);
		Expr expr=func.getArg(1);
		expr.visit(ve);
		
		List<String> varSub=new ArrayList<String>(Tools.getExprVars(expr));
		//is it necessary to sort?
		
		ans=Tools.generateNonTripleAtoms("filter",varSub,ix,iy);
		String axiom="";
		String fn=func.getFunctionSymbol().getSymbol();
		if(fn==null) {
			
		}else {
			if(fn.equals("not")) {
				axiom=ans+":-"+safe+",not "+ve.getAns()+".";
				pw.println(axiom);
			}else {//unary functions
				String funcName=func.getFunctionSymbol().getSymbol().toString();
				
				if(funcName.equals("bound")) {
					Axioms.setBounds();
				}
				
				axiom=ans+":-"+safe+","+fn+"("+ve.getAns()+")";
				List<String> gs=ve.getGuards();
				if(!gs.isEmpty()) {
					for(int i=0;i<gs.size();i++) {
						if(i!=gs.size()-1) {
							axiom+=",";
						}
						axiom+=gs.get(i);
					}
				}
				axiom+=".";
				pw.println(axiom);
			}
		}
	}
	public void visit(ExprFunction2 func) {
		// TODO Auto-generated method stub
		Expr exprL=func.getArg(1);
		Expr exprR=func.getArg(2);
		SPARQLFilterExprVisitor veL=new SPARQLFilterExprVisitor(safe,ix,iy*2);
		SPARQLFilterExprVisitor veR=new SPARQLFilterExprVisitor(safe,ix,iy*2+1);
		exprL.visit(veL);
		exprR.visit(veR);
		
		List<String> arg=new ArrayList<String>(Tools.getExprVars(func.getExpr()));
		
		ans=Tools.generateNonTripleAtoms("filter",arg, ix, iy);
		//String fn=func.getFunctionSymbol().getSymbol();
		if(func instanceof E_LogicalOr) {
			String axiomL=ans+":-"+safe+","+veL.getAns()+".";
			String axiomR=ans+":-"+safe+","+veR.getAns()+".";
			pw.println(axiomL);
			pw.println(axiomR);
		}else if(func instanceof E_Equals) {//need to consider the fixed-domain here
			List<String> gs=new ArrayList<String> (veL.getGuards());
			gs.addAll(veR.getGuards());
			String axiom=ans+":-"+safe+","+veL.getAns()+"="+veR.getAns();
			if(!gs.isEmpty()) {
				axiom+=",";
				for(int i=0;i<gs.size();i++) {
					axiom+=gs.get(i);
					if(i!=gs.size()-1) {
						axiom+=",";
					}
				}
			}
			axiom+=".";
			pw.println(axiom);
		}else if(func instanceof E_LogicalAnd) {
			String axiom=ans+":-"+safe+","+veL.getAns()+","+veR.getAns()+".";
			pw.println(axiom);
		}else if(func instanceof E_SameTerm){
			String axiom=ans+":-"+safe+","+"sameTerm"+"("+veL.getAns()+","+veR.getAns()+").";
			pw.println(axiom);
		}else if(func instanceof E_GreaterThan) {
			String axiom=ans+":-"+safe+",("+veL.getAns()+")>("+veR.getAns()+").";
			pw.println(axiom);
		}else if(func instanceof E_LessThan) {
			String axiom=ans+":-"+safe+",("+veL.getAns()+")<("+veR.getAns()+").";
			pw.println(axiom);
		}else {
		}
	}

	public void visit(ExprFunction3 func) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ExprFunctionN func) {
		// TODO Auto-generated method stub
		//Regex,
//		String funcName=func.getFunctionSymbol().getSymbol();
//		List<Expr> ex=func.getArgs();
//		
//		List<String> arg=new ArrayList<String> (Tools.getExprVars(func.getExpr()));
//		
//		ans=Tools.generateNonTripleAtoms("filter",arg,ix,iy);
//		String axiom=ans+":-"+safe;
//		List<String> guards=new ArrayList<String>();
//		if(funcName.equals("regex")) {
//			axiom+=",regex(";
//			for(int i=0;i<ex.size();i++) {
//				SPARQLFilterExprVisitor ve=new SPARQLFilterExprVisitor(safe,-1,-1);
//				ex.get(i).visit(ve);
//				axiom+=ve.getAns();
//				guards.addAll(ve.getGuards());
//				if(i!=ex.size()-1) {
//					axiom+=",";
//				}
//			}
//			axiom+=")";
//			if(!guards.isEmpty()) {
//				for(int i=0;i<guards.size();i++) {
//					if(i!=guards.size()-1) {
//						axiom+=",";
//					}
//					axiom+=guards.get(i);
//				}
//			}
//			axiom+=".";
//			pw.println(axiom);
//		}else {
//			
//		}
		
	}

	public void visit(ExprFunctionOp funcOp) {
		// TODO Auto-generated method stub
		
	}

	public void visit(NodeValue nv) {
		// TODO Auto-generated method stub
		if(nv.isBoolean()) {
			if(nv.getBoolean()) {
				ans="filter(true)";
			}else{
				ans="not filter(true)";
			}
		}else if(nv.isNumber()){
			if(nv.isDecimal()) {
				ans=nv.getDatatypeURI()+nv.getDecimal().toEngineeringString();
			}else {
				ans=Tools.getProcessedNode(nv.asNode(), guards);
			}
		}else {
			ans=Tools.getProcessedNode(nv.asNode(), guards);
		}
		
	}

	public void visit(ExprVar nv) {
		// TODO Auto-generated method stub
		Node nnv=nv.getAsNode();
		ans=Tools.getProcessedNode(nnv,(new ArrayList<String>()));
	}

	public void visit(ExprAggregator eAgg) {
		// TODO Auto-generated method stub
		
	}

	public void finishVisit() {
		// TODO Auto-generated method stub
		
	}

}
