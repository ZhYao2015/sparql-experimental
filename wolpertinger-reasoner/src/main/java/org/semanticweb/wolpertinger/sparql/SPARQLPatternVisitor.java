package org.semanticweb.wolpertinger.sparql;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLabel;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpList;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProcedure;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Exists;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AggCountVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.expr.aggregate.AggMax;
import org.apache.jena.sparql.expr.aggregate.AggMaxDistinct;
import org.apache.jena.sparql.expr.aggregate.AggMin;
import org.apache.jena.sparql.expr.aggregate.AggMinDistinct;
import org.apache.jena.sparql.expr.aggregate.AggSum;
import org.apache.jena.sparql.expr.aggregate.AggSumDistinct;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.semanticweb.wolpertinger.debugLogger;
import org.semanticweb.wolpertinger.translation.TranslationException;

public class SPARQLPatternVisitor implements OpVisitor {

	private Set<String> var_set;

	private PrintWriter pw = Fixed.getPw();

	private int index = 0;


	
	
	private String ans = "";

	public String getAns() {
		return ans;
	}

	public SPARQLPatternVisitor(int index) {
		this.index = index;

		this.var_set = new HashSet<String>();
		
	}
	
	
	public Set<String> getVarSet() {
		return this.var_set;
	}

	public void visit(OpQuadPattern quadPattern) {
		// TODO Auto-generated method stub

	}

	public void visit(OpQuadBlock quadBlock) {
		// TODO Auto-generated method stub

	}

	public void visit(OpQuad opQuad) {
		// TODO Auto-generated method stub

	}

	public void visit(OpPath opPath) {
		// TODO Auto-generated method stub
		// ans=Tools.generateNonTripleAtoms("path",Tools.getMentionedVars(opPath),
		// index, -1);
		TriplePath tp = opPath.getTriplePath();
		List<String> gs=new ArrayList<String> ();
		
		String fd_subject=Tools.getProcessedNode(tp.getSubject(), gs);
		String fd_object=Tools.getProcessedNode(tp.getObject(), gs);
	
		//SPARQLPathVisitor(?fixed_subject, ?fixed_object)
		SPARQLPathVisitor vp = new SPARQLPathVisitor(fd_subject,fd_object,gs);
		tp.getPath().visit(vp);
		
		this.var_set.addAll(Tools.getMentionedVars(opPath));// ref or copy?
		List<String> sorted_vars = new ArrayList<String>(var_set);
		Collections.sort(sorted_vars);
		
		ans = Tools.generateNonTripleAtoms("ans", sorted_vars, index, -1);

		String triplePath = Tools.generateFDTriplePathAtoms(tp);
		pw.println(ans + ":-" + triplePath + ".");

		Axioms.setZeroDistPath();
	}

	public void visit(OpTriple opTriple) {
		// TODO Auto-generated method stub
		ans = Tools.generateFDTripleAtoms(opTriple.getTriple());
		Triple triple=opTriple.getTriple();
		Node subject=triple.getSubject();
		if(subject.isVariable()) {
			this.var_set.add(Tools.getProcessedNode(subject,new ArrayList<String> ()));
		}
		Node object=triple.getObject();
		if(object.isVariable()) {
			this.var_set.add(Tools.getProcessedNode(object, new ArrayList<String> ()));
		}
		Node predicate=triple.getPredicate();
		if(predicate.isVariable()) {
			this.var_set.add(Tools.getProcessedNode(predicate, new ArrayList<String> ()));
		}
	}

	public void visit(OpSequence opSequence) {
		// TODO Auto-generated method stub

		List<Op> ops = opSequence.getElements();
		String right = "";
		for (int i = 0; i < ops.size(); i++) {
			SPARQLPatternVisitor vp = new SPARQLPatternVisitor(index * 2);
			ops.get(i).visit(vp);
			right += vp.getAns();
			if (i != ops.size() - 1) {
				right += ",";
			}
			this.var_set.addAll(vp.getVarSet());
		}

		List<String> sorted_vars = new ArrayList<String>(var_set);
		Collections.sort(sorted_vars);

		ans = Tools.generateNonTripleAtoms("ans", sorted_vars, index, -1);
		pw.println(ans + ":-" + right + ".");
	}

	public void visit(OpBGP opBGP) {
		// TODO Auto-generated method stub

		List<Triple> tl = opBGP.getPattern().getList();
		String right = "";
		for (int i = 0; i < tl.size(); i++) {
			OpTriple op = new OpTriple(tl.get(i));
			SPARQLPatternVisitor v = new SPARQLPatternVisitor(index);
			op.visit(v);
			right += v.getAns();
			if (i != tl.size() - 1) {
				right += ",";
			}
			// a list does not eliminate duplicate elements, so we need to use the set
			this.var_set.addAll(v.getVarSet());
		}

		List<String> sorted_vars = new ArrayList<String>(var_set);
		Collections.sort(sorted_vars);

		ans = Tools.generateNonTripleAtoms("ans", sorted_vars, index, -1);
		pw.println(ans + ":-" + right + ".");
	}

	public void visit(OpTable opTable) {
		// TODO Auto-generated method stub

	}

	public void visit(OpNull opNull) {
		// TODO Auto-generated method stub

	}

	public void visit(OpProcedure opProc) {
		// TODO Auto-generated method stub

	}

	public void visit(OpPropFunc opPropFunc) {

		// TODO Auto-generated method stub

	}

	public Tuple normal_visit(OpFilter opFilter) {
		// TODO Auto-generated method stub
		
		Op mix = opFilter.getSubOp();
		SPARQLPatternVisitor vp = new SPARQLPatternVisitor(index * 2);
		mix.visit(vp);
		// mix? bind,not exist
		// mentioned or fixed?
		this.var_set.addAll(vp.getVarSet());
		List<String> sorted_vars = new ArrayList<String>(var_set);
		Collections.sort(sorted_vars);

		ans = Tools.generateNonTripleAtoms("ans",sorted_vars, index, -1);
		
		
		String filter_ans = "";
		String mix_ans = vp.getAns();
		Expr ex = Tools.toConjuncts(opFilter.getExprs());
		
		//handling unsafe variables in filter with a normal expression
		Set<String> var_p=vp.getVarSet();
		Set<Var> var_e=ex.getVarsMentioned();
		BindingHashMap binding=new BindingHashMap();
		Node _null=NodeFactory.createLiteral("null");
		
		for(Var v:var_e) {
			if(!var_p.contains(Tools.getProcessedNode(v,new ArrayList<String> ()))) {
				binding.add(v, _null);
			}
		}
		
		ex=Substitute.substitute(ex, binding);
		
		// issues over indices need to be re-examined
		SPARQLFilterExprVisitor vf = new SPARQLFilterExprVisitor(mix_ans, index, 1);
		ex.visit(vf);
		// test the renaming when dealing with the exist
		// ExistHandler.test(ex.get(i));

		filter_ans += vf.getAns();

		String axiom = ans + ":-" + mix_ans + "," + filter_ans + ".";
		pw.println(axiom);

		Tuple t = new Tuple(ans, this.var_set);

		return t;
	}

	// ans???
	public void visit(OpFilter opFilter) {

		Expr ex = Tools.toConjuncts(opFilter.getExprs());
		//Op sub = opFilter.getSubOp();

		SPARQLExistFinder vf = new SPARQLExistFinder(true, true);
		ex.visit(vf);
		ExprFunction efl = vf.getFound();

		// ans = Tools.generateNonTripleAtoms("ans", Tools.getMentionedVars(sub), index,
		// -1);

		if (ex instanceof E_Exists || ex instanceof E_NotExists) {
			// special handling of Filter(Exist(P),P_prime).
			if (ex instanceof E_NotExists) {
				int aux_level=Fixed.get_next_level();
				Op P1 = opFilter.getSubOp();
				Op P2 = ((E_NotExists) ex).getGraphPattern();
				Op result = OpFilter.filterDirect(
						new ExprList(new E_LogicalNot(new E_Bound(new ExprVar(Var.alloc("aux_X"+Integer.toString(aux_level)))))),
						OpLeftJoin.create(P1, OpJoin.create(P2, ExistHandler.getAuxBgp(aux_level)), Tools.getBooleanExpr(true)));
				
				SPARQLPatternVisitor v = new SPARQLPatternVisitor(index);
				result.visit(v);

				this.var_set.addAll(v.getVarSet());
				List<String> sorted_vars = new ArrayList<String>(var_set);
				Collections.sort(sorted_vars);

				ans =v.getAns();

				//pw.println(ans + ":-" + v.getAns() + ".");
			} else {
				// setMinus
				Op P1 = opFilter.getSubOp();
				Op P2 = ((E_Exists) ex).getGraphPattern();
				Op result = ExistHandler.setMinus(P1, OpFilter.filterDirect(new ExprList(new E_NotExists(P2)), P1));
				SPARQLPatternVisitor v = new SPARQLPatternVisitor(index);
				result.visit(v);

				this.var_set.addAll(v.getVarSet());
				List<String> sorted_vars = new ArrayList<String>(var_set);
				Collections.sort(sorted_vars);

				ans =v.getAns();

			}
		} else if (efl == null) {

			// this.var_set=?
			Tuple t = normal_visit(opFilter);
			ans = t.getAns();
			this.var_set.addAll(t.getVar_set());

		} else {
			// handling of Exist or Not_Exist in Expr
			Op trans = ExistHandler.trans_filter(opFilter);
			SPARQLPatternVisitor v = new SPARQLPatternVisitor(index);
			trans.visit(v);

			this.var_set.addAll(v.getVarSet());
			List<String> sorted_vars = new ArrayList<String>(var_set);
			Collections.sort(sorted_vars);

			ans =v.getAns();
		}
	}

	public void visit(OpGraph opGraph) {
		// TODO Auto-generated method stub

	}

	public void visit(OpService opService) {
		// TODO Auto-generated method stub

	}

	public void visit(OpDatasetNames dsNames) {
		// TODO Auto-generated method stub

	}

	public void visit(OpLabel opLabel) {
		// TODO Auto-generated method stub

	}

	public void visit(OpAssign opAssign) {
		// TODO Auto-generated method stub

	}
	
//-----------------------------------------------------------------
	public void visit(OpExtend opExtend) {
		// TODO Auto-generated method stub
		Op sub=opExtend.getSubOp();
		VarExprList list=opExtend.getVarExprList();
		SPARQLPatternVisitor v=new SPARQLPatternVisitor(index*2);
		sub.visit(v);
		
		this.var_set.addAll(v.getVarSet());
		StringBuilder body=new StringBuilder(v.getAns());
		body.append(",");
		
		List<Var> l_vars=list.getVars();
		for(int i=0;i<l_vars.size();i++) {
			Var current=l_vars.get(i);
			SPARQLAssignExprVisitor va=new SPARQLAssignExprVisitor();
			Expr ex=list.getExpr(current);
			if(ex!=null) {
				ex.visit(va);
				String assign=Tools.getProcessedNode(current,new ArrayList<String>())+"="
						+va.getAns();
				body.append(assign);
				body.append(",");
				
				
				//---------------------------------------------------------
				//this.var_set.addAll(Tools.getMentionedExprVarNames(ex));
				//---------------------------------------------------------
			}
			this.var_set.add(Tools.getProcessedNode(current, new ArrayList<String> ()));
			body.deleteCharAt(body.length()-1);
		}
		
		List<String> sorted_vars=new ArrayList<String> (this.var_set);
		Collections.sort(sorted_vars);
		
		ans=Tools.generateNonTripleAtoms("ans",sorted_vars,index,-1);
		
		pw.println(ans+":-"+body+".");
	}
//----------------------------------------------------------------------------------
	
	public void visit(OpJoin opJoin) {
		// TODO Auto-generated method stub

		Op jl = opJoin.getLeft();
		Op jr = opJoin.getRight();
		SPARQLPatternVisitor vpL = new SPARQLPatternVisitor(index * 2);
		SPARQLPatternVisitor vpR = new SPARQLPatternVisitor(index * 2 + 1);
		jl.visit(vpL);
		jr.visit(vpR);
		// ans = Tools.generateNonTripleAtoms("ans", Tools.getMentionedVars(opJoin),
		// index, -1);

		Set<String> sl = vpL.getVarSet();
		List<String> sorted_sl = new ArrayList<String>(sl);
		Collections.sort(sorted_sl);

		Set<String> sr = vpR.getVarSet();
		List<String> sorted_sr = new ArrayList<String>(sr);
		Collections.sort(sorted_sr);

		Set<String> in = Tools.set_intersection(sl, sr);
		List<String> sorted_in = new ArrayList<String>(in);
		Collections.sort(sorted_in);

		this.var_set.addAll(sl);
		this.var_set.addAll(sr);

		List<String> sorted_vars = new ArrayList<String>(var_set);
		Collections.sort(sorted_vars);

		ans = Tools.generateNonTripleAtoms("ans", sorted_vars, index, -1);

		String axiom = ans + ":-"
				+ Tools.generateNonTripleAtoms("ans", Tools.rename_vars(sorted_sl, sorted_in, "1"), index * 2, -1) + ","
				+ Tools.generateNonTripleAtoms("ans", Tools.rename_vars(sorted_sr, sorted_in, "2"), index * 2 + 1, -1);

		String join = "";
		if (!in.isEmpty()) {
			join = Axioms.createSingleJoin(sorted_in);
			axiom += "," + join;
			int len = in.size();
			if (len <= Axioms.getJoin_length()) {

			} else {
				for (int i = Axioms.getJoin_length() + 1; i <= len; i++) {
					Axioms.setJoin_length(i);
					Axioms.setJoinAxiom(i);
				}
			}
		}
		axiom += ".";
		pw.println(axiom);
	}

	public void visit(OpUnion opUnion) {
		// TODO Auto-generated method stub

		Op ul = opUnion.getLeft();
		Op ur = opUnion.getRight();
		SPARQLPatternVisitor vpL = new SPARQLPatternVisitor(index * 2);
		SPARQLPatternVisitor vpR = new SPARQLPatternVisitor(index * 2 + 1);
		ul.visit(vpL);
		ur.visit(vpR);

		Set<String> sl = vpL.getVarSet();
		List<String> sorted_sl = new ArrayList<String>(sl);
		Collections.sort(sorted_sl);

		Set<String> sr = vpR.getVarSet();
		List<String> sorted_sr = new ArrayList<String>(sr);
		Collections.sort(sorted_sr);

		this.var_set.addAll(sl);
		this.var_set.addAll(sr);
		List<String> sorted_vars = new ArrayList<String>(var_set);
		Collections.sort(sorted_vars);

		ans = Tools.generateNonTripleAtoms("ans", sorted_vars, index, -1);

		String axiomL = Tools.generateNonTripleAtoms("ans", Tools.completeNull(sorted_vars, sorted_sl), index, -1)
				+ ":-" + vpL.getAns() + ".";
		String axiomR = Tools.generateNonTripleAtoms("ans", Tools.completeNull(sorted_vars, sorted_sr), index, -1)
				+ ":-" + vpR.getAns() + ".";
		pw.println(axiomL);
		pw.println(axiomR);
	}

	public void visit(OpDiff opDiff) {
		// TODO Auto-generated method stub
		
	}

	public void visit(OpMinus opMinus) {
		// TODO Auto-generated method stub
		Op P1=opMinus.getLeft();
		Op P2=opMinus.getRight();
		int aux_level=Fixed.get_next_level();
		Op trans=OpLeftJoin.create(P1,OpJoin.create(P2,ExistHandler.getAuxBgp(aux_level)),new ExprList(new E_LogicalNot(new E_Bound(new ExprVar(Var.alloc("aux_X"+Integer.toString(aux_level)))))));
		SPARQLPatternVisitor v=new SPARQLPatternVisitor(index);
		trans.visit(v);
		
		ans=v.getAns();
		this.var_set.addAll(v.getVarSet());
	}

	public void visit(OpConditional opCondition) {
		// TODO Auto-generated method stub
		
	}

	public void visit(OpDisjunction opDisjunction) {
		// TODO Auto-generated method stub

	}

	public void visit(OpExt opExt) {
		// TODO Auto-generated method stub

	}

	public void visit(OpList opList) {
		// TODO Auto-generated method stub

	}

	public void visit(OpOrder opOrder) {
		// TODO Auto-generated method stub

	}

	public void visit(OpProject opProject) {
		// TODO Auto-generated method stub
		SPARQLPatternVisitor vp = new SPARQLPatternVisitor(index * 2);
		opProject.getSubOp().visit(vp);
		List<String> arg=new ArrayList<String> (Tools.getFixedVars(opProject));
		
		ans = Tools.generateNonTripleAtoms("ans", arg, index, -1);
		String result = ans + ":-" + vp.getAns() + ".";
		pw.println(result);
	}

	public void visit(OpReduced opReduced) {
		// TODO Auto-generated method stub

	}

	public void visit(OpDistinct opDistinct) {
		// TODO Auto-generated method stub

	}

	public void visit(OpSlice opSlice) {
		// TODO Auto-generated method stub

	}


//	public void visit(OpGroup opGroup) {
//		// TODO Auto-generated method stub
//		
//		//group vars, with or without expressions
//		//might be empty?
//		VarExprList var_ex=opGroup.getGroupVars();
//		
//		//aggregators
//		//normally will not be empty
//		List<ExprAggregator> eAggs=opGroup.getAggregators();
//		
//		Op subOp=opGroup.getSubOp();
//		SPARQLPatternVisitor v=new SPARQLPatternVisitor(index*2);
//		subOp.visit(v);
//		
//		Set<String> var_set_ext=new HashSet<String> ();
//		
//		//new variables as a renaming for Expr, or old variables without Expr
//		//might be empty
//		Set<String> group_set=new HashSet<String>();
//		
//		//new variables as a renaming for aggregations
//		Set<String> agg_set=new HashSet<String> ();
//		
//		StringBuilder body=new StringBuilder();
//		
//		List<Var> vars=var_ex.getVars();
//		for(int i=0;i<vars.size();i++) {
//			Var currVar=vars.get(i);
//			SPARQLAssignExprVisitor va=new SPARQLAssignExprVisitor(new VarExprList(),v.getVarSet(),index*2);
//			
//			Expr currExpr=var_ex.getExpr(currVar);
//			String varV=Tools.getProcessedNode(currVar,new ArrayList<String>());
//			
//			if(currExpr!=null) {
//				currExpr.visit(va);
//				String assign=varV+"="+va.getAns();
//				body.append(assign);
//				body.append(",");
//			}
//			group_set.add(varV);
//			
//		}
//		
//		var_set_ext.addAll(v.getVarSet());
//		var_set_ext.addAll(group_set);
//		
//		for(int i=0;i<eAggs.size();i++) {
//			ExprAggregator eAgg=eAggs.get(i);
//			agg_set.add(Tools.getProcessedNode(eAgg.getVar(),new ArrayList<String> ()));
//			SPARQLAssignExprVisitor va=new SPARQLAssignExprVisitor(var_ex,var_set_ext,index*2);
//			eAgg.visit(va);
//			body.append(va.getAns());
//			body.append(",");
//		}
//		body.deleteCharAt(body.length()-1);
//		
//		this.var_set.addAll(group_set);
//		this.var_set.addAll(agg_set);
//		List<String> sorted_vars=new ArrayList<String> (this.var_set);
//		Collections.sort(sorted_vars);
//		
//		ans=Tools.generateNonTripleAtoms("ans",sorted_vars,index,-1);
//		
//		pw.println(ans+":-"+body.toString()+".");
//	}

	public void visit(OpTopN opTop) {
		// TODO Auto-generated method stub

	}

	public void visit(OpLeftJoin opLeftJoin) {
		// TODO Auto-generated method stub
		// //The version which handles the case with the occurring of Exist
		// Op left=opLeftJoin.getLeft();
		// Op right=opLeftJoin.getRight();
		// //we handle the case where ex=null in toConjuncts
		// Expr ex=Tools.toConjuncts(opLeftJoin.getExprs());

		// translate LeftJoin(E,P1,P2) to Union(Fiter(E,Join(P1,P2)),Pr)

		Expr ex = Tools.toConjuncts(opLeftJoin.getExprs());

		SPARQLExistFinder vf = new SPARQLExistFinder(true, true);
		ex.visit(vf);
		ExprFunction efl = vf.getFound();

		// List<String> ljVars = Tools.getMentionedVars(opLeftJoin);

		// ans = Tools.generateNonTripleAtoms("ans", ljVars, index, -1);

		if (efl == null) {

			// this.var_set=?
			//------------------------------------------------------------
			Tuple t = normal_visit(opLeftJoin);
			this.var_set.addAll(t.getVar_set());
			ans = t.getAns();
			//-------------------------------------------------------------

		} else {
			Op trans = ExistHandler.trans_leftjoin(opLeftJoin);
			SPARQLPatternVisitor vp = new SPARQLPatternVisitor(index);
			trans.visit(vp);
			
			this.var_set.addAll(vp.getVarSet());
			List<String> sorted_vars = new ArrayList<String>(var_set);
			Collections.sort(sorted_vars);

			ans =vp.getAns();

			//pw.println(ans + ":-" + vp.getAns() + ".");
		}

	}

	public Tuple normal_visit(OpLeftJoin opLeftJoin) {
		// TODO Auto-generated method stub
		
		Op ljl = opLeftJoin.getLeft();
		Op ljr = opLeftJoin.getRight();
		ExprList ex = opLeftJoin.getExprs();
		SPARQLPatternVisitor vpL = new SPARQLPatternVisitor(index * 2);
		SPARQLPatternVisitor vpR = new SPARQLPatternVisitor(index * 2 + 1);
		ljl.visit(vpL);
		ljr.visit(vpR);

		if(index==10) {
			debugLogger.logger.debug("unsafe variables in:\n");
			debugLogger.logger.debug(opLeftJoin);
		}
		
		
		Set<String> sl = vpL.getVarSet();
		List<String> sorted_sl = new ArrayList<String>(sl);
		Collections.sort(sorted_sl);

		Set<String> sr = vpR.getVarSet();
		List<String> sorted_sr = new ArrayList<String>(sr);
		Collections.sort(sorted_sr);

		Set<String> in = Tools.set_intersection(sl, sr);
		List<String> sorted_in = new ArrayList<String>(in);
		Collections.sort(sorted_in);

		this.var_set.addAll(sl);
		this.var_set.addAll(sr);
		List<String> sorted_vars = new ArrayList<String>(var_set);
		Collections.sort(sorted_vars);

		ans = Tools.generateNonTripleAtoms("ans", sorted_vars, index, -1);
		
		
		List<String> var_sl_r=Tools.rename_vars(sorted_sl,new ArrayList<String>(in),"1");
		List<String> var_sr_r=Tools.rename_vars(sorted_sr,new ArrayList<String>(in),"2");
		
		
		String ans_p=Tools.generateNonTripleAtoms("ans_p",sorted_vars, index,-1);
		
		
		List<String> lj_ans_pp = Tools.replaceNull(sorted_vars, Tools.set_difference(sorted_sr, sorted_sl));
		
		String ans_pp=Tools.generateNonTripleAtoms("ans_pp",sorted_sl, index, -1);
		
		String ans2=Tools.generateNonTripleAtoms("ans", lj_ans_pp, index, -1);
		
		String join = "";

		if (!in.isEmpty()) {
			join = Axioms.createSingleJoin(sorted_in);
			int len = in.size();
			if (len <= Axioms.getJoin_length()) {

			} else {
				for (int i = Axioms.getJoin_length() + 1; i <= len; i++) {
					Axioms.setJoin_length(i);
					Axioms.setJoinAxiom(i);
				}
			}
		} else {
			join = "filter(true)";
		}

		String filter = "filter(true)";
		
		String ans_safe=Tools.generateNonTripleAtoms("ans",var_sl_r, index * 2, -1) + ","
				+ Tools.generateNonTripleAtoms("ans",var_sr_r, index * 2 + 1, -1)+","+join;
		//null and default true as filter expressions
		if (ex != null) {
			//handling unsafe variables in filter with normal expressions: 
			//leftjoin with filter
			
			Set<String> var_p=new HashSet<String>(sl);
			var_p.addAll(sr);
			
			Set<Var> var_e=ex.getVarsMentioned();
			Node _null=NodeFactory.createLiteral("null");
			BindingHashMap binding=new BindingHashMap();
			for(Var v:var_e) {
				if(!var_p.contains(Tools.getProcessedNode(v, new ArrayList<String> ()))) {
					binding.add(v, _null);
				}
			}
			ex=Substitute.substitute(ex,binding);
			
			filter = "";
			for (int i = 0; i < ex.size(); i++) {
				SPARQLFilterExprVisitor evb = new SPARQLFilterExprVisitor(ans_safe, index, 1);
				ex.get(i).visit(evb);
				filter += evb.getAns();
				if (i != ex.size() - 1) {
					filter += ",";
				}

			}
		} else {

		}
		
		String axiom1= ans_p+":-"+ ans_safe + ","+filter+".";
		
		String axiom2 =ans + ":-"+ ans_p+".";

		String axiom3=ans_pp+":-"+ans_p+".";
		
		String axiom4=ans2+":-"+Tools.generateNonTripleAtoms("ans",sorted_sl,index*2, -1)+","
				+"not "+ans_pp+".";
		//{µ1,M1(µ1)|for all µ2 with M2(µ2) > 0:µ1 and µ2 are incompatible or J(µ1 ∪ µ2)(F) = false
		
		
		pw.println(axiom1);
		pw.println(axiom2);
		pw.println(axiom3);
		pw.println(axiom4);

		

		Tuple t = new Tuple(ans, this.var_set);
		return t;
	}

	//-------------------------------------------------------------------------
	public void visit(OpGroup opGroup) {
		// TODO Auto-generated method stub
		Op opSub=opGroup.getSubOp();
		VarExprList var_ex_list=opGroup.getGroupVars();
		List<ExprAggregator> list_aggs=opGroup.getAggregators();
		
//		SPARQLPatternVisitor v=new SPARQLPatternVisitor(index*2);
//		opSub.visit(v);
		
		//what about case where the list is empty
		if(list_aggs.isEmpty()) {

		}else {
			SPARQLPatternVisitor v=new SPARQLPatternVisitor(index*2);
			opSub.visit(v);
			
			//we simulate aggregateJoin here
			String right="";
			String agg_ans="";
			Set<String> s_agg_joined=new HashSet<String> ();
			for(int i=0;i<list_aggs.size();i++) {
				ExprAggregator expr_agg=list_aggs.get(i);
				
				Set<String> v_set=new HashSet<String>();
				agg_ans=Aggregation(expr_agg,var_ex_list,v,i,v_set);
				this.var_set.addAll(v_set);
				
				right+=agg_ans;
				if(i!=list_aggs.size()-1) {
					right+=",";
				}
			}
			
			List<String> sorted_var_set=new ArrayList<String>(this.var_set);
			Collections.sort(sorted_var_set);
			ans=Tools.generateNonTripleAtoms("ans",sorted_var_set,index,-1);
			String axiom_agg_Join=ans+":-"+right+".";
			pw.println(axiom_agg_Join);
		}
		
	}

	
	//simulate the Aggregation(expr,func,Group(exprlist,opSub))
	public String Aggregation(ExprAggregator expr_agg,VarExprList var_ex_list,SPARQLPatternVisitor v,int agg_dex,Set<String> v_set) {
		String result="";
		if(var_ex_list.isEmpty()) {
			result=AggregationDefaultGroup(expr_agg,v,agg_dex,v_set);
		}else {
			result=AggregationSpecifiedGroup(expr_agg,var_ex_list,v,agg_dex,v_set);
		}
		this.var_set.add(Tools.getProcessedNode(expr_agg.getAggVar().asVar(),new ArrayList<String>()));
		for(Var var:var_ex_list.getVars()) {
			this.var_set.add(Tools.getProcessedNode(var,new ArrayList<String> ()));
		}
		return result;
	}

	public String AggregationSpecifiedGroup(ExprAggregator expr_agg,VarExprList var_ex_list,SPARQLPatternVisitor v,int agg_dex,Set<String> v_set) {
		String result="";
	
		//vars as a renaming for group
		Set<String> set_gVars=new HashSet<String>();
			
		StringBuilder body=new StringBuilder();
			
		List<Var> gVars=var_ex_list.getVars();
			
		String axiom_add_gp="";
		String axiom_add_agg="";
		String axiom_calc_agg="";
			
		String right_add_gp=v.getAns();

//-------------------add--group-----------ans^group------------------------------
		for(int i=0;i<gVars.size();i++) {
			Var currVar=gVars.get(i);
			SPARQLAssignExprVisitor va=new SPARQLAssignExprVisitor();
			Expr currExpr=var_ex_list.getExpr(currVar);
			String varV=Tools.getProcessedNode(currVar,new ArrayList<String>());
			set_gVars.add(varV);
				
			if(currExpr!=null) {
				currExpr.visit(va);
					if(i!=gVars.size()-1) {
						right_add_gp+=",";
					}
					right_add_gp=right_add_gp+varV+"="+va.getAns();
				}
			}
			
			Set<String> set_add_gp=new HashSet<String>(v.getVarSet());
			set_add_gp.addAll(set_gVars);
			List<String> sorted_add_gp=new ArrayList<String>(set_add_gp);
			Collections.sort(sorted_add_gp);
			
			String h_add_gp=Tools.generateNonTripleAtoms("ans_gp",sorted_add_gp,index*2,-1);
			
			axiom_add_gp=h_add_gp+":-"+right_add_gp+".";
			pw.println(axiom_add_gp);
//--------------------------------------------------------------------------	
			
//---------------add--aggregates----------------------------------------------
			Aggregator agg=expr_agg.getAggregator();
			ExprList expr_list=agg.getExprList();
			String right_add_agg=h_add_gp;
			String agg_var="";
			
			//consider the case where expr_list.size()==1
			Expr expr=expr_list.get(0);
			if(expr instanceof ExprVar) {
				agg_var=Tools.getProcessedNode(expr.asVar(),new ArrayList<String> ());
			}else {
				agg_var="V_temp";
				SPARQLAssignExprVisitor va=new SPARQLAssignExprVisitor();
				expr.visit(va);
				right_add_agg=right_add_agg+","+agg_var+"="+va.getAns();
			}
			Set<String> s_add_agg=new HashSet<String> (set_gVars);
			s_add_agg.add(agg_var);
			if(Tools.isDistinctAgg(agg)) {
				s_add_agg.addAll(v.getVarSet());
			}
			
			List<String> sorted_add_agg=new ArrayList<String> (s_add_agg);
			Collections.sort(sorted_add_agg);
			String h_add_agg=Tools.generateNonTripleAtoms("ans_ext",sorted_add_agg,index*2,agg_dex);
			
			axiom_add_agg=h_add_agg+":-"+right_add_agg+".";
			pw.println(axiom_add_agg);
			
//---------------------calc--aggregates---------------------------------------------
			String var_as_agg=Tools.getProcessedNode(expr_agg.getVar(),new ArrayList<String>());
			
			result=calc_agg(var_as_agg,agg_var,agg,sorted_add_agg,set_gVars,agg_dex,v_set);	
		return result;
	}
	
	public String AggregationDefaultGroup(ExprAggregator expr_agg,SPARQLPatternVisitor v,int agg_dex,Set<String> v_set) {
		String result="";

		//vars as a renaming for group
		Set<String> set_gVars=new HashSet<String>();
			
		StringBuilder body=new StringBuilder();
			
		String axiom_add_agg="";
		String axiom_calc_agg="";
			
		String right_add_agg=v.getAns();
			
//---------------add--aggregates----------------------------------------------
		Aggregator agg=expr_agg.getAggregator();
		ExprList expr_list=agg.getExprList();
		String agg_var="";
		
		//consider the case where expr_list.size()==1
		Expr expr=expr_list.get(0);
		if(expr instanceof ExprVar) {
			agg_var=Tools.getProcessedNode(expr.asVar(),new ArrayList<String> ());
		}else {
			agg_var="V_temp";
			SPARQLAssignExprVisitor va=new SPARQLAssignExprVisitor();
			expr.visit(va);
			right_add_agg=right_add_agg+","+agg_var+"="+va.getAns();
		}
		Set<String> s_add_agg=new HashSet<String> (v.getVarSet());
		s_add_agg.add(agg_var);

		List<String> sorted_add_agg=new ArrayList<String> (s_add_agg);
		Collections.sort(sorted_add_agg);
		String h_add_agg=Tools.generateNonTripleAtoms("ans_ext",sorted_add_agg,index*2,agg_dex);
			
		axiom_add_agg=h_add_agg+":-"+right_add_agg+".";
		pw.println(axiom_add_agg);
			
//---------------------calc--aggregates---------------------------------------------
		String var_as_agg=Tools.getProcessedNode(expr_agg.getVar(),new ArrayList<String>());
			
		result=calc_agg(var_as_agg,agg_var,agg,sorted_add_agg,set_gVars,agg_dex,v_set);	
		return result;
	}

	public String calc_agg(String var_as_agg,String agg_var,Aggregator agg,List<String> sorted_add_agg,Set<String> set_gVars,int agg_dex,Set<String> v_set) {
		// TODO Auto-generated method stub
		
		StringBuilder weight=new StringBuilder(agg_var);
		weight.append(",");
		List<String> sorted_copy=new ArrayList<String>(sorted_add_agg);
		sorted_copy.removeAll(set_gVars);
		if(Tools.isDistinctAgg(agg)) {
			for(int i=0;i<sorted_copy.size();i++) {
				if(sorted_copy.get(i).equals(agg_var)) {
					
				}else {
					weight.append(sorted_copy.get(i));
					weight.append(",");
				}
			}
		}else {
			
		}
		
		weight.deleteCharAt(weight.length()-1);
		
		//right
		
		//do not need to sort again
		List<String> sorted_vars_ctrl=new ArrayList<String>(sorted_add_agg);
		for(int i=0;i<sorted_vars_ctrl.size();i++) {
			if(!set_gVars.contains(sorted_vars_ctrl.get(i))) {
				sorted_vars_ctrl.set(i, "_");
			}
		}
		Set<String> s_calc_agg=new HashSet<String>(set_gVars);
		s_calc_agg.add(var_as_agg);
		List<String> sorted_calc_agg=new ArrayList<String>(s_calc_agg);
		Collections.sort(sorted_calc_agg);
		
		//we need an index pair for agg_(i,j)
		String h_calc_agg=Tools.generateNonTripleAtoms("ans_agg",sorted_calc_agg,index*2,agg_dex);
		
		String subOp_in_agg=Tools.generateNonTripleAtoms("ans_ext",sorted_add_agg,index*2,agg_dex);
		
		String controller="";
		if(!set_gVars.isEmpty()) {
			controller=","+Tools.generateNonTripleAtoms("ans_ext",sorted_vars_ctrl,index*2,agg_dex);
		}
		
		String right="";
		String agg_label="undefined";
		if(agg instanceof AggCountVar) {
				agg_label="#count";
		}else if(agg instanceof AggMax) {
				agg_label="#max";
		}else if(agg instanceof AggMin) {
				agg_label="#min";
		}else if(agg instanceof AggSum) {
				agg_label="#sum";
		}else if(agg instanceof AggCountVarDistinct){
				agg_label="#count";
		}else if(agg instanceof AggMaxDistinct) {
				agg_label="#max";
		}else if(agg instanceof AggMinDistinct) {
				agg_label="#min";
		}else if(agg instanceof AggSumDistinct) {
				agg_label="#sum";
		}else {
			throw new TranslationException("Unimplemented aggregates: "+agg.getName());
		}
		right=right+var_as_agg+"="+agg_label+"{"+weight+":"+subOp_in_agg+"}"+controller;
		pw.println(h_calc_agg+":-"+right+".");
		
		v_set.addAll(s_calc_agg);
		return h_calc_agg;
	}
}
