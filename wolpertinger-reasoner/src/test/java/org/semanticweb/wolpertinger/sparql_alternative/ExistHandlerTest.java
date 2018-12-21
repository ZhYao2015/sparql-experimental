package org.semanticweb.wolpertinger.sparql_alternative;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Exists;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.wolpertinger.sparql.ExistHandler;

public class ExistHandlerTest extends TestCase {

	
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
	public void testEq() {
		Set<Var> vars=new HashSet<Var>();
		vars.add(Var.alloc("X"));
		//vars.add(Var.alloc("Y"));
		BindingHashMap theta=new BindingHashMap();
		theta.add(Var.alloc("X"),Var.alloc("X1"));
		//theta.add(Var.alloc("Y"),Var.alloc("Y1"));
		//System.out.println(ExistHandler.eq(vars, theta));
		Expr result=new E_LogicalAnd(
				new E_LogicalAnd(
						new E_LogicalOr(
								new E_LogicalNot(new E_Bound(new ExprVar(Var.alloc("X")))),
								new E_Bound(new ExprVar(Var.alloc("X1")))
						),
						new E_LogicalOr(
								new E_Bound(new ExprVar(Var.alloc("X"))),
								new E_LogicalNot(new E_Bound(new ExprVar(Var.alloc("X1"))))	
						)
				),
				new E_LogicalOr(
						new E_LogicalNot(new E_Bound(new ExprVar(Var.alloc("X")))),
						new E_Equals(new ExprVar(Var.alloc("X")),new ExprVar(Var.alloc("X1")))
				)
		);
		System.out.println(result);
		assertEquals(ExistHandler.eq(vars,theta),result);
	}
	
	@Test
	public void testEq2() {
		Set<Var> vars=new HashSet<Var>();
		vars.add(Var.alloc("X"));
		vars.add(Var.alloc("Y"));
		BindingHashMap theta=new BindingHashMap();
		theta.add(Var.alloc("X"),Var.alloc("X1"));
		theta.add(Var.alloc("Y"),Var.alloc("Y1"));
		
		Expr resultX=new E_LogicalAnd(
				new E_LogicalAnd(
						new E_LogicalOr(
								new E_LogicalNot(new E_Bound(new ExprVar(Var.alloc("X")))),
								new E_Bound(new ExprVar(Var.alloc("X1")))
						),
						new E_LogicalOr(
								new E_Bound(new ExprVar(Var.alloc("X"))),
								new E_LogicalNot(new E_Bound(new ExprVar(Var.alloc("X1"))))	
						)
				),
				new E_LogicalOr(
						new E_LogicalNot(new E_Bound(new ExprVar(Var.alloc("X")))),
						new E_Equals(new ExprVar(Var.alloc("X")),new ExprVar(Var.alloc("X1")))
				)
		);
		
		Expr resultY=new E_LogicalAnd(
				new E_LogicalAnd(
						new E_LogicalOr(
								new E_LogicalNot(new E_Bound(new ExprVar(Var.alloc("Y")))),
								new E_Bound(new ExprVar(Var.alloc("Y1")))
						),
						new E_LogicalOr(
								new E_Bound(new ExprVar(Var.alloc("Y"))),
								new E_LogicalNot(new E_Bound(new ExprVar(Var.alloc("Y1"))))	
						)
				),
				new E_LogicalOr(
						new E_LogicalNot(new E_Bound(new ExprVar(Var.alloc("Y")))),
						new E_Equals(new ExprVar(Var.alloc("Y")),new ExprVar(Var.alloc("Y1")))
				)
		);
		Expr result=new E_LogicalAnd(resultY,resultX);
		
		assertEquals(ExistHandler.eq(vars, theta),result);
	}
	
//	@Test
//	//syntactically equivalent but not equivalent, it is true but cannot pass the test
//	//solution: LinkedHashSet
//	public void testTransLeftJoinOnlyExist() {
//		Op opl=SSE.parseOp("(prefix ((: <http://example/>)) (filter (= ?Y :k) (bgp(?X :a ?Y))) )" );
//		Op opr=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?Y :b :t))) " );
//		Op exis=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X :n ?Y))) " );
//		Op ops=OpLeftJoin.create(opl, opr, new E_NotExists(exis));
//		Op nops=ExistHandler.trans_leftjoin((OpLeftJoin)ops);
//		
//		Set<Var> vars=new LinkedHashSet<Var> ();
//		vars.add(Var.alloc("X"));
//		vars.add(Var.alloc("Y"));
//		
//		BindingHashMap theta1=new BindingHashMap();
//		theta1.add(Var.alloc("Y"),Var.alloc("Y1"));
//		theta1.add(Var.alloc("X"),Var.alloc("X1"));
//		
//		BindingHashMap theta2=new BindingHashMap();
//		theta2.add(Var.alloc("Y"),Var.alloc("Y2"));
//		theta2.add(Var.alloc("X"),Var.alloc("X2"));
//		
//		Op aux=SSE.parseOp("(bgp (?aux_X ?aux_Y ?aux_Z))");
//		
//		Op result=OpUnion.create(OpFilter.filterDirect(new ExprList(new E_NotExists(exis)),OpJoin.create(opl,opr)),
//				OpFilter.filterDirect(new ExprList(new E_LogicalNot((new E_Bound(
//						new ExprVar(Var.alloc("aux_X")))))),
//						OpLeftJoin.create(opl,DirectSubstitute.substitute(OpJoin.create(
//						OpFilter.filterDirect(new ExprList(DirectSubstitute.substitute(new E_NotExists(exis),theta1)),
//								OpJoin.create(OpFilter.filterDirect(new ExprList(ExistHandler.eq(vars, theta1)),
//										OpJoin.create(opl,DirectSubstitute.substitute(opl, theta1))),
//										DirectSubstitute.substitute(opr,theta1))),aux),theta2),
//						ExistHandler.eq(vars, theta2))));
//		
//		assertEquals(nops,result);
//	}
//	
//	@Test
//	public void testSetMinus1() {
//		Op P1=SSE.parseOp("(prefix ((: <http://example/>)) (filter (= ?Y :k) (bgp(?X :a ?Y))) )");
//		Op P2=SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X :b ?Y)) )" );
//		Op nops=ExistHandler.setMinus(P1, P2);
//		
//		Set<Var> vars=new LinkedHashSet<Var>();
//		vars.add(Var.alloc("X"));
//		vars.add(Var.alloc("Y"));
//		
//		BindingHashMap theta=new BindingHashMap();
//		theta.add(Var.alloc("X"), Var.alloc("X1"));
//		theta.add(Var.alloc("Y"), Var.alloc("Y1"));
//		
//		Op result=OpFilter.filterDirect(new ExprList(new E_LogicalNot(new E_Bound(new ExprVar(Var.alloc("aux_X"))))),
//				OpLeftJoin.create(DirectSubstitute.substitute(OpJoin.create(P2, 
//						SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?aux_X ?aux_Y ?aux_Z)) )" )),theta),P1,
//						ExistHandler.eq(vars, theta)));
//		assertEquals(nops,result);
//		
//	}
//	
//	@Test
//	public void testTransFilterExi() {
//		Op sub=SSE.parseOp("(prefix ((: <http://example/>)) (filter (= ?Y :k) (bgp(?X :a ?Y))) )" );
//		Op subb=SSE.parseOp("(prefix ((: <http://example/>)) (filter (= ?Y :k) (bgp(?X :a ?Y))) )" );
//
//		
//		Expr ex=new E_LogicalAnd(new E_NotExists(SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X :t :p)) )" )),
//				new E_Equals(new ExprVar(Var.alloc("Y")),new ExprVar(Var.alloc("X"))));
//		
//		Op ops=OpFilter.filterDirect(new ExprList(ex),sub);
//		
//		System.out.println(sub);
//				
//		Op nops=ExistHandler.trans_filter((OpFilter)ops);
//		
//		
//		Op result=OpUnion.create(
//				OpFilter.filterDirect(new ExprList(new E_NotExists(SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X :t :p)) )" ))),
//						OpFilter.filterDirect(new ExprList(new E_LogicalAnd(
//								new E_Equals(new ExprVar(Var.alloc("true")),new ExprVar(Var.alloc("true"))),
//								new E_Equals(new ExprVar(Var.alloc("Y")),new ExprVar(Var.alloc("X"))))),
//								subb)),
//				OpFilter.filterDirect(new ExprList(new E_Exists(SSE.parseOp("(prefix ((: <http://example/>)) (bgp(?X :t :p)) )" ))),
//						OpFilter.filterDirect(new ExprList(new E_LogicalAnd(new E_LogicalNot(
//								new E_Equals(new ExprVar(Var.alloc("true")),new ExprVar(Var.alloc("true")))),
//								new E_Equals(new ExprVar(Var.alloc("Y")),new ExprVar(Var.alloc("X")))))
//								,subb))
//				);
//		assertEquals(nops,result);
//	}

	
	
}
