package org.semanticweb.wolpertinger.sparql_alternative;


import junit.framework.TestCase;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.sse.SSE;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.wolpertinger.sparql.DirectSubstitute;


public class DirectSubstituteTest extends TestCase{

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
	
	@Test
	public void testDirectSubstitute1() {
		Op triple=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X :a :b)))");
		Op exis=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X :b :p)))");
		//System.out.println(triple);
		Op ops=OpFilter.filter(new E_NotExists(exis),triple);
		BindingHashMap map=new BindingHashMap();
		map.add(Var.alloc("X"),Var.alloc("X1"));
		Op nops=DirectSubstitute.substitute(ops,map);
		Op ntriple=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X1 :a :b)))");
		Op nexis=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X1 :b :p)))");
		Op result=OpFilter.filter(new E_NotExists(nexis), ntriple);
		assertEquals(nops,result);
	}

	@Test
	public void testDirectSubstitute2() {
		Op triple=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X :a ?Y)))");
		Op exis=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X :b :p)))");
		//System.out.println(triple);
		Op ops=OpFilter.filter(new E_LogicalAnd(new E_NotExists(exis),
				new E_Equals(new ExprVar(Var.alloc("X")),new ExprVar(Var.alloc("Y")))),triple);
		BindingHashMap map=new BindingHashMap();
		map.add(Var.alloc("X"),Var.alloc("X1"));
		map.add(Var.alloc("Y"),Var.alloc("Y1"));
		Op nops=DirectSubstitute.substitute(ops,map);
		Op ntriple=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X1 :a ?Y1)))");
		Op nexis=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X1 :b :p)))");
		Op result=OpFilter.filter(new E_LogicalAnd(new E_NotExists(nexis),
				new E_Equals(new ExprVar(Var.alloc("X1")),new ExprVar(Var.alloc("Y1")))), ntriple);
		assertEquals(nops,result);
	}
	
	@Test
	public void testDirectSubstituteEmbeddedFilter() {
		Op opsp=SSE.parseOp("(prefix ((: <http://example/>)) (filter (= ?Y :k) (bgp(?X :a ?Y))) ) ");
		Op ops=OpFilter.filterDirect(new ExprList(new E_LogicalNot(new E_Equals(
				new ExprVar(Var.alloc("X")),new ExprVar(Var.alloc("Y"))))),opsp);
		BindingHashMap map=new BindingHashMap();
		map.add(Var.alloc("X"),Var.alloc("X1"));
		map.add(Var.alloc("Y"),Var.alloc("Y1"));
		Op nops=DirectSubstitute.substitute(ops,map);
		Op nopsp=SSE.parseOp("(prefix ((: <http://example/>)) (filter (= ?Y1 :k) (bgp(?X1 :a ?Y1))) ) ");
		Op result=OpFilter.filterDirect(new ExprList(new E_LogicalNot(new E_Equals(
				new ExprVar(Var.alloc("X1")),new ExprVar(Var.alloc("Y1"))))),nopsp);
		
		assertEquals(nops,result);
	}
}
