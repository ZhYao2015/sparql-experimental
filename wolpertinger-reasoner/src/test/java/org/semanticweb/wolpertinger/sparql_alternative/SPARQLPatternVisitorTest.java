package org.semanticweb.wolpertinger.sparql_alternative;


import java.io.PrintWriter;

import junit.framework.TestCase;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.sse.SSE;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.wolpertinger.sparql.Fixed;
import org.semanticweb.wolpertinger.sparql.SPARQLPatternVisitor;


public class SPARQLPatternVisitorTest extends TestCase {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

//	@Ignore
//	public void test() {
//		fail("Not yet implemented");
//	}

	@Ignore
	public void testFilter1() {
		Fixed.setPw(new PrintWriter(System.out));
		Op opss=SSE.parseOp("(prefix ((: <http://example/>)) (filter (= ?Y :k) (bgp(?X :a ?Y))) )");
		Op ops=OpFilter.filterDirect(new ExprList(new E_LogicalNot(new E_Equals(
				new ExprVar(Var.alloc("X")),new ExprVar(Var.alloc("Y"))))),opss);
		System.out.println(ops);
		SPARQLPatternVisitor v=new SPARQLPatternVisitor(1);
		ops.visit(v);
		Fixed.getPw().flush();
	}
	
	@Ignore
	public void testLeftJoinWithoutFilter() {
		Fixed.setPw(new PrintWriter(System.out));
		Op ops=SSE.parseOp("(prefix ((: <http://example/>)) (leftjoin (bgp(?X :a ?Y)) (bgp(?Y :b ?Z)) ) )");
		SPARQLPatternVisitor v=new SPARQLPatternVisitor(1);
		ops.visit(v);
		Fixed.getPw().flush();
	}
	
	
	@Test
	public void testLeftJoinWithFilter() {
		Fixed.setPw(new PrintWriter(System.out));
		Op opl=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X :a ?Y)) )");
		Op opr=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?Y :b ?Z)) )");
		Op lj=OpLeftJoin.create(opl, opr, new E_Bound(new ExprVar(Var.alloc("Z"))));;
		SPARQLPatternVisitor v=new SPARQLPatternVisitor(1);
		lj.visit(v);
		Fixed.getPw().flush();
	}
}
