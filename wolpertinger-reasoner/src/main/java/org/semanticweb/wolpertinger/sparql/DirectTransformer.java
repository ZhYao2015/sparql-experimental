package org.semanticweb.wolpertinger.sparql;

import java.util.List;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.Transformer.ApplyTransformVisitor;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformer;

public class DirectTransformer extends Transformer {
	
	private static DirectTransformer singleton = new DirectTransformer();
	
	   /** Set the current transformer - use with care */
    public static void set(DirectTransformer value) { DirectTransformer.singleton = value; }
    
    /** Transform an algebra expression */
    public static Op transform(Transform transform, Op op)
    { return get().transformation(transform, op, null, null) ; }
    
    /** Transform an algebra expression and the expressions */
    public static Op transform(Transform transform, ExprTransform exprTransform, Op op)
    { return get().transformation(transform, exprTransform, op, null, null) ; }

    /** Transformation with specific Transform and default ExprTransform (apply transform inside pattern expressions like NOT EXISTS) */ 
    public static Op transform(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        return get().transformation(transform, op, beforeVisitor, afterVisitor) ;
    }
    
    /** Transformation with specific Transform and ExprTransform applied */
    public static Op transform(Transform transform, ExprTransform exprTransform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        return get().transformation(transform, exprTransform, op, beforeVisitor, afterVisitor) ;
    }
	
	protected Op transformation(Transform transform, ExprTransform exprTransform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        DirectApplyTransformVisitor v = new DirectApplyTransformVisitor(transform, exprTransform) ;
        return transformation(v, op, beforeVisitor, afterVisitor) ;
    }
	
	 /** Get the current transformer */
    public static DirectTransformer get() { return singleton; }
    
    
	public static class DirectApplyTransformVisitor extends Transformer.ApplyTransformVisitor{

		private  ExprTransform exprTransform ;
		
		public DirectApplyTransformVisitor(Transform transform, ExprTransform exprTransform) {
			super(transform, exprTransform);
			// TODO Auto-generated constructor stub
		}
		
		
		private static ExprList transform(ExprList exprList, ExprTransform exprTransform)
        {
            if ( exprList == null || exprTransform == null )
                return exprList ;
            return ExprTransformer.transform(exprTransform, exprList) ;
        }

        private static Expr transform(Expr expr, ExprTransform exprTransform)
        {
            if ( expr == null || exprTransform == null )
                return expr ;
            return ExprTransformer.transform(exprTransform, expr) ;
        }
		
		private static VarExprList process(VarExprList varExpr, ExprTransform exprTransform)
        {
            List<Var> vars = varExpr.getVars() ;
            VarExprList varExpr2 = new VarExprList() ;
            boolean changed = false ;
            for ( Var v : vars )
            {
                Expr e = varExpr.getExpr(v) ;
                Expr e2 =  e ;
                if ( e != null )
                    e2 = transform(e, exprTransform) ;
                if ( e2 == null )
                    varExpr2.add(v) ;
                else
                    varExpr2.add(v, e2) ; 
                if ( e != e2 )
                    changed = true ;
            }
            if ( ! changed ) 
                return varExpr ;
            return varExpr2 ;
        }

        private static ExprList process(ExprList exprList, ExprTransform exprTransform)
        {
            if ( exprList == null )
                return null ;
            ExprList exprList2 = new ExprList() ;
            boolean changed = false ;
            for ( Expr e : exprList )
            {
                Expr e2 = process(e, exprTransform) ;
                exprList2.add(e2) ; 
                if ( e != e2 )
                    changed = true ;
            }
            if ( ! changed ) 
                return exprList ;
            return exprList2 ;
        }
        
        private static Expr process(Expr expr, ExprTransform exprTransform)
        {
            Expr e = expr ;
            Expr e2 =  e ;
            if ( e != null )
                e2 = transform(e, exprTransform) ;
            if ( e == e2 ) 
                return expr ;
            return e2 ;
        }
		
		@Override
        protected void visitFilter(OpFilter opFilter)
        {
            Op subOp = null ;
            if ( opFilter.getSubOp() != null )
                subOp = pop() ;
            boolean changed = ( opFilter.getSubOp() != subOp ) ;

            ExprList ex = opFilter.getExprs() ;
			ExprList ex2 = process(ex, exprTransform) ;
            OpFilter f = opFilter ;
            if ( ex != ex2 )
                f = (OpFilter)OpFilter.filterDirect(ex2, subOp) ;
            push(f.apply(transform, subOp)) ;
        }
		
	}
}
