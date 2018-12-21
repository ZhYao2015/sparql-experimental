package org.semanticweb.wolpertinger.sparql;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.BasicPattern;
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
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;

//how to make sure that aux_X is a fresh variable
public class ExistHandler {
	//Reform the Left(E,P1,P2) with exist
		public static Expr eq(Set<Var> vars,BindingHashMap theta) {
			Iterator<Var> it=vars.iterator();
			Var vsource=it.next();
			Var vdest;
			if(theta.get1(vsource)!=null) {
				vdest=Var.alloc(theta.get1(vsource));
			}else {
				vdest=vsource;
			}
			//if it.size()==0? we also need to consider
			if(vars.size()==1) {
				return varsRenaming(vsource,vdest);
			}else {
				Expr result=varsRenaming(vsource,vdest);
				while(it.hasNext()) {
					vsource=it.next();
					if(theta.get1(vsource)!=null) {
						vdest=Var.alloc(theta.get1(vsource));
					}else {
						vdest=vsource;
					};
					result=new E_LogicalAnd(result,varsRenaming(vsource,vdest));
				}
				return result;
			}
		}
		
		public static Op getAuxBgp(int level) {
			String lv=Integer.toString(level);
			Node n_auxy=NodeFactory.createLiteral("auxy");
			Node n_auxz=NodeFactory.createLiteral("auxz");
			
			Triple aux=Triple.create(Var.alloc("aux_X"+lv),n_auxy,n_auxz);
			BasicPattern pattern=new BasicPattern();
			pattern.add(aux);
			OpBGP Pa=new OpBGP(pattern);
			return Pa;
		}
		
		public static Expr varsRenaming(Var vsource,Var vdest) {
			Expr v=new ExprVar(vsource);
			Expr v_re=new ExprVar(vdest);
			Expr b1=new E_Bound(v);
			Expr b2=new E_Bound(v_re);
			Expr bConsistent=new E_LogicalAnd(new E_LogicalOr(new E_LogicalNot(b1),b2),
					new E_LogicalOr(b1,new E_LogicalNot(b2)));
			Expr assignConsistent=new E_LogicalOr(new E_LogicalNot(b1),new E_Equals(v,v_re));
			Expr result=new E_LogicalAnd(bConsistent,assignConsistent);
			return result;
			
		}
		
		public static BindingHashMap createVar2VarMap(Set<Var> vars,String suffix) {
			BindingHashMap theta=new BindingHashMap();
			Iterator<Var> it=vars.iterator();
			while(it.hasNext()) {
				Var v=Var.alloc(it.next());
				Var g=Var.alloc(v.getVarName()+suffix);
				theta.add(v,g);
			}
			
			return theta;
		}
		
		
		public static Op trans_leftjoin(OpLeftJoin leftjoin) {
			
			//need to be reconstructed
			Op left=leftjoin.getLeft();
			Op right=leftjoin.getRight();
		    Expr ex=Tools.toConjuncts(leftjoin.getExprs());
			
			Set<Var> vars=OpVars.fixedVars(left);
			vars.addAll(OpVars.fixedVars(right));
			
			
			int theta_lv1=Fixed.get_next_theta_level();
			
			BindingHashMap theta1=createVar2VarMap(vars,Integer.toString(theta_lv1));
			
			//Iterator<Var> it=vars.iterator();
			
			//E*theta1
			//If the bindings are not applicable?
			Expr ex_theta1=DirectSubstitute.substitute(ex,theta1);
			
			Op left_theta1=DirectSubstitute.substitute(left,theta1);
			
			Op right_theta1=DirectSubstitute.substitute(right,theta1);
			
			//P:=Filter(E_theta1,Join(Filter(eq(X,theta1),Join(P1,P1_theta1)),p2theta1)).
			Op P=OpFilter.filterDirect(
					new ExprList(ex_theta1),OpJoin.create(
							OpFilter.filterDirect(new ExprList(eq(vars,theta1)),
									OpJoin.create(left,left_theta1)),
							right_theta1
			));
			
			
			int aux_level=Fixed.get_next_level();
			Op Pa=getAuxBgp(aux_level);
			
			int theta_lv2=Fixed.get_next_theta_level();
			BindingHashMap theta2=createVar2VarMap(vars,Integer.toString(theta_lv2));
			
			//Pr:=Filter(not bound(?x),LeftJoin(eq(X,theta2),P1,P^x_theta2)),
			//P^x:=Join(P,(?x,?y,?z)).
			
			Op Px=OpJoin.createReduce(P,Pa);
			
			Op Px_theta2=DirectSubstitute.substitute(Px, theta2);
			Op Pr=OpFilter.filterDirect(new ExprList(new E_LogicalNot(
					new E_Bound(new ExprVar(Var.alloc("aux_X"+Integer.toString(aux_level)))))),
					OpLeftJoin.create(left,Px_theta2,eq(vars,theta2)));
			
			Op fP=OpUnion.create(OpFilter.filterDirect(new ExprList(ex),OpJoin.createReduce(left,right)),Pr);
			return fP;
		}
		

		

		
		//case where Exist or Not_Exist are part of Expr
		public static Op trans_filter(OpFilter filter) {
			Op sub=filter.getSubOp();
			Expr ex=Tools.toConjuncts(filter.getExprs());
			
			//just search
			SPARQLExistFinder vf=new SPARQLExistFinder(true,true);
			ex.visit(vf);
			
			ExprFunction efl=vf.getFound();
			

			
			//replace true
			SPARQLExistFinder vf_true=new SPARQLExistFinder(false,true);
			ex.visit(vf_true);
			
			Expr trueEx=vf_true.getRec();
			
			//replace false
			SPARQLExistFinder vf_false=new SPARQLExistFinder(false,false);
			ex.visit(vf_false);
			
			Expr falseEx=vf_false.getRec();
			
			Expr efl_true=null;
			Expr efl_false=null;
			if(efl instanceof E_Exists) {
				efl_true=new E_Exists(efl.getGraphPattern());
				efl_false=new E_NotExists(efl.getGraphPattern());
			}else if(efl instanceof E_NotExists) {
				efl_false=new E_Exists(efl.getGraphPattern());
				efl_true=new E_NotExists(efl.getGraphPattern());
			}else {
				
			}
			
			return OpUnion.create(OpFilter.filterDirect(new ExprList(efl_true),OpFilter.filterDirect(new ExprList(trueEx),sub)),
					OpFilter.filterDirect(new ExprList(efl_false),OpFilter.filterDirect(new ExprList(falseEx),sub)));
			
				
		}
		
		
		public static Op setMinus(Op P1,Op P2) {
			int aux_level=Fixed.get_next_level();
			Op P2_prime=OpJoin.createReduce(P2,ExistHandler.getAuxBgp(aux_level));
			//fixedVars or mentioned vars
			Set<Var> vars=OpVars.fixedVars(P1);
			vars.addAll(OpVars.fixedVars(P2));
			
			int theta_lv=Fixed.get_next_theta_level();
			
			BindingHashMap theta=createVar2VarMap(vars,Integer.toString(theta_lv));
			//the Substitute.substitute() does not work as what we expected!!!
			//DirectSubstitute.substitute need to handle the Exist or Not_Exist 
			Op P2_prime_theta=DirectSubstitute.substitute(P2_prime, theta);
			Op P3=OpLeftJoin.create(P1,P2_prime_theta,eq(vars, theta));
			Op result=OpFilter.filterDirect(new ExprList(new E_LogicalNot(new E_Bound(
					new ExprVar(Var.alloc("aux_X"+Integer.toString(aux_level)))))),P3);
			return result;
		}
}
		
