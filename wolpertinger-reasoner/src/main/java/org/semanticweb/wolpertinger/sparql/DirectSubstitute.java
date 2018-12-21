package org.semanticweb.wolpertinger.sparql;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.pfunction.PropFuncArg;

public class DirectSubstitute extends Substitute {
	
	public static Op substitute(Op op, Binding binding)
    {
        // Want to avoid cost if the binding is empty 
        // but the empty test is not zero-cost on non-empty things.
     
        if ( isNotNeeded(binding) ) return op ;
        return DirectTransformer.transform(new DirectOpSubstituteWorker(binding), op) ;
    }
	
	 private static boolean isNotNeeded(Binding b)
	 {
	        return b == null || b.isEmpty() ; 
	  
	 }
	
	private static class DirectOpSubstituteWorker extends TransformCopy
    {
        private Binding binding ;

        public DirectOpSubstituteWorker(Binding binding) 
        {
            super(TransformCopy.COPY_ALWAYS) ;
            this.binding = binding ;
        }

        @Override
        public Op transform(OpBGP opBGP)
        {
            BasicPattern bgp = opBGP.getPattern() ;
            bgp = substitute(bgp, binding) ;
            return new OpBGP(bgp) ;
        }

        @Override
        public Op transform(OpQuadPattern quadPattern)
        {
            Node gNode = quadPattern.getGraphNode() ;
            Node g = substitute(gNode, binding) ;

            BasicPattern triples = new BasicPattern() ;
            for ( Triple triple : quadPattern.getBasicPattern() )
            {
                Node s = substitute(triple.getSubject(), binding) ;
                Node p = substitute(triple.getPredicate(), binding) ;
                Node o = substitute(triple.getObject(), binding) ;
                Triple t = new Triple(s, p, o) ;
                triples.add(t) ;
            }
            
            // Pure quading.
//            for ( Iterator iter = quadPattern.getQuads().iterator() ; iter.hasNext() ; )
//            {
//                Quad quad = (Quad)iter.next() ;
//                if ( ! quad.getGraph().equals(gNode) )
//                    throw new ARQInternalErrorException("Internal error: quads block is not uniform over the graph node") ;
//                Node s = substitute(quad.getSubject(), binding) ;
//                Node p = substitute(quad.getPredicate(), binding) ;
//                Node o = substitute(quad.getObject(), binding) ;
//                Triple t = new Triple(s, p, o) ;
//                triples.add(t) ;
//            }

            return new OpQuadPattern(g, triples) ;
        }

        @Override
        public Op transform(OpPath opPath)
        {
            return new OpPath(substitute(opPath.getTriplePath(), binding)) ;
        }

        @Override
        public Op transform(OpPropFunc opPropFunc, Op subOp)
        {
            PropFuncArg sArgs = opPropFunc.getSubjectArgs() ;
            PropFuncArg oArgs = opPropFunc.getObjectArgs() ;
            
            PropFuncArg sArgs2 = substitute(sArgs, binding) ;
            PropFuncArg oArgs2 = substitute(oArgs, binding) ;
            
            if ( sArgs2 == sArgs && oArgs2 == oArgs && opPropFunc.getSubOp() == subOp)
                return super.transform(opPropFunc, subOp) ;
            return new OpPropFunc(opPropFunc.getProperty(), sArgs2, oArgs2, subOp) ; 
        }
        
        @Override
        public Op transform(OpFilter filter, Op op)
        {
            ExprList exprs = filter.getExprs().copySubstitute(binding) ;
            if ( exprs == filter.getExprs() )
                return filter ;
            return OpFilter.filterDirect(exprs, op) ; 
        }

        @Override
        public Op transform(OpAssign opAssign, Op subOp)
        { 
            VarExprList varExprList2 = transformVarExprList(opAssign.getVarExprList()) ;
            if ( varExprList2.isEmpty() )
                return subOp ;
            return OpAssign.assign(subOp, varExprList2) ;
        }
        
        @Override
        public Op transform(OpExtend opExtend, Op subOp)
        { 
            VarExprList varExprList2 = transformVarExprList(opExtend.getVarExprList()) ;
            if ( varExprList2.isEmpty() )
                return subOp ;
            
            return OpExtend.create(subOp, varExprList2) ;
        }
        
        private  VarExprList transformVarExprList(VarExprList varExprList)
        {
            VarExprList varExprList2 = new VarExprList() ;
            for ( Var v : varExprList.getVars() )
            {
//                if ( binding.contains(v))
//                    // Already bound. No need to do anything because the 
//                    // logical assignment will test value.  
//                    continue ;
                Expr expr = varExprList.getExpr(v) ;
                expr = expr.copySubstitute(binding) ;
                varExprList2.add(v, expr) ;
            }
            return varExprList2 ;
        }
        

        // The expression?
        //public Op transform(OpLeftJoin opLeftJoin, Op left, Op right)   { return xform(opLeftJoin, left, right) ; }
        
        @Override
        public Op transform(OpGraph op, Op sub)
        {
            Node n = substitute(op.getNode(), binding) ;
            return new OpGraph(n, sub) ;
        }

        @Override
        public Op transform(OpService op, Op sub)
        {
            Node n = substitute(op.getService(), binding) ;
            return new OpService(n, sub, op.getSilent()) ;
        }
    }
}
