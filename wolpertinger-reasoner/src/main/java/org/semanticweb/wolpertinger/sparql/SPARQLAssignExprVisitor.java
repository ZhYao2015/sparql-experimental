package org.semanticweb.wolpertinger.sparql;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Add;
import org.apache.jena.sparql.expr.E_Divide;
import org.apache.jena.sparql.expr.E_Multiply;
import org.apache.jena.sparql.expr.E_StrLength;
import org.apache.jena.sparql.expr.E_Subtract;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
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
import org.apache.jena.sparql.expr.aggregate.AggCountVar;
import org.apache.jena.sparql.expr.aggregate.AggMax;
import org.apache.jena.sparql.expr.aggregate.AggMin;
import org.apache.jena.sparql.expr.aggregate.AggSum;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.semanticweb.wolpertinger.translation.TranslationException;

public class SPARQLAssignExprVisitor implements ExprVisitor{
	
	//we use python module to express external functions
	private PrintWriter pw=Fixed.getPw();
	private List<String> guards;
	private String ans="";
	
	
	public SPARQLAssignExprVisitor() {
		
	}
	
	public String getAns() {
		return this.ans;
	}
	public List<String> getGuards() {
		return guards;
	}
	
	public void startVisit() {
		// TODO Auto-generated method stub
		
	}

	public void visit(ExprFunction0 func) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ExprFunction1 func) {
		// TODO Auto-generated method stub
		Expr e=func.getArg();
		
		
		SPARQLAssignExprVisitor v=new SPARQLAssignExprVisitor();
		e.visit(v);
		
		String sub=v.getAns();
		String fname="";
		
		if(func instanceof E_StrLength) {
			//is there any compatible functions in asp?
			fname="@strlen";
		}else {
			
		}
		
	}

	public void visit(ExprFunction2 func) {
		// TODO Auto-generated method stub
		Expr el=func.getArg1();
		Expr er=func.getArg2();
		
		SPARQLAssignExprVisitor vl=new SPARQLAssignExprVisitor();
		SPARQLAssignExprVisitor vr=new SPARQLAssignExprVisitor();
		el.visit(vl);
		er.visit(vr);
		String left=vl.getAns();
		String right=vr.getAns();
		String opr="";
		if(func instanceof E_Add) {
			opr="+";
		}else if(func instanceof E_Divide) {
			opr="\\";
		}else if(func instanceof E_Multiply) {
			opr="*";
		}else if(func instanceof E_Subtract) {
			opr="-";
		}else {
			
		}
		ans= "("+left+opr+right+")";
	}

	public void visit(ExprFunction3 func) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ExprFunctionN func) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ExprFunctionOp funcOp) {
		// TODO Auto-generated method stub
		
	}

	public void visit(NodeValue nv) {
		// TODO Auto-generated method stub
		if(nv.isNumber()) {
			if(nv.isDecimal()) {
				ans=nv.getDecimal().toEngineeringString();
			}else if(nv.isDouble()) {
				ans=Double.toString(nv.getDouble());
			}else {
				ans=Float.toString(nv.getFloat());
			}
		}
	}

	public void visit(ExprVar nv) {
		// TODO Auto-generated method stub
		Node nnv=nv.getAsNode();
		ans=Tools.getProcessedNode(nnv,(new ArrayList<String>()));
	}

	

	
	
	public void finishVisit() {
		// TODO Auto-generated method stub
		
	}

	public void visit(ExprAggregator eAgg) {
		// TODO Auto-generated method stub
		
	}

}
