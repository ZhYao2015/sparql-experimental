package org.semanticweb.wolpertinger.sparql;

import java.io.PrintWriter;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.semanticweb.wolpertinger.Configuration;

public class SPARQLTranslation {
	
	private PrintWriter writer;
	private Configuration configuration;
	
	public SPARQLTranslation(PrintWriter writer,Configuration configuration) {
		this.writer=writer;
		this.configuration=configuration;
	}
	
	public void translateQuery(String file) {
		if(configuration.getDomainIndividuals()==null) {
			Fixed.reset_domain();
		}else {
			Fixed.set_domain(configuration.getDomainIndividuals());
		}
		Query query=QueryFactory.read(file);
		Fixed.setPw(writer);
		writer.println("%%---------------------------%%");
		writer.println("%%<-------SPARQL Query------>%%");
		writer.println("%%---------------------------%%");
		SPARQLQueryVisitor vq=new SPARQLQueryVisitor();
		Fixed.setPrefixMap(query.getPrefixMapping());
		query.visit(vq);
		//Axioms.setBaseJoin();
		Axioms.setBackgroundKnowledges();
		writer.close();
	}
	
	public void showAlgebra(PrintWriter pw,String file) {
		Query query=QueryFactory.read(file);
		Op op=Algebra.compile(query);
		pw.println(op);
		pw.close();
	}
	
	
	public static void main(String args[]) {
		String file="examples/aggr1.txt";
		
//		triple("http://test#james","http://test#hasID","http://test#n23").
//		triple("http://test#james","http://test#takes","http://test#lakers").
//		triple("http://test#cr7","http://test#hasID","http://test#n7").
//		triple("http://test#cr7","http://test#takes","http://test#juventus").
//		triple("http://test#enrique","http://test#hasID","http://test#c0").
//		triple("http://test#xavi","http://test#hasID","http://test#n6").
//		domain(X):-triple(X,_,_).
//		domain(Y):-triple(_,Y,_).
//		domain(Z):-triple(_,_,Z).
		
		PrintWriter pw=new PrintWriter(System.out);
		//translate(pw,file);
		//showAlgebra(pw,file);
	}
	
	
}
