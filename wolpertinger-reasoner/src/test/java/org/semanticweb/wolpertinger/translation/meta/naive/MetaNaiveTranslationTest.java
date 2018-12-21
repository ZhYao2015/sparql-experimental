/*  Copyright 2018 by the International Center for Computational Logic, Technical University Dresden.

    This file is part of Wolpertinger.

    Wolpertinger is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Wolpertinger is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Wolpertinger.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.wolpertinger.translation.meta.naive;

import java.io.PrintWriter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.wolpertinger.Configuration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Lukas Schweizer
 *
 */
public class MetaNaiveTranslationTest extends TestCase {

	public MetaNaiveTranslationTest(String testName) {
		super(testName);
	}
	
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
	}
	
	
	public void voidSubClassOf() throws OWLOntologyCreationException {
		//A subClassOf B
		OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
		IRI ontologyIRI=IRI.create("http://www.semanticweb.org/test");
		OWLOntology o=manager.createOntology(ontologyIRI);
		OWLDataFactory factory=manager.getOWLDataFactory();
		OWLClass classA=factory.getOWLClass(IRI.create(ontologyIRI+"#A"));
		OWLClass classB=factory.getOWLClass(IRI.create(ontologyIRI+"#B"));
		
		OWLAxiom subClassOfAxiom=factory.getOWLSubClassOfAxiom(classA, classB);
		manager.addAxiom(o, subClassOfAxiom);
		
		NaiveMetaTranslation meta=new NaiveMetaTranslation(new Configuration(),new PrintWriter(System.out));
		meta.translateOntology(o);
		
	}
	
	public void voidSomeWithEqual() throws OWLOntologyCreationException {
		
		// exists r.A subClassOf B
		//B subClassOf exists r.A
		OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
		IRI ontologyIRI=IRI.create("http://test");
		OWLOntology o=manager.createOntology(ontologyIRI);
		OWLDataFactory factory=manager.getOWLDataFactory();
		
		OWLObjectProperty propR=factory.getOWLObjectProperty(IRI.create(ontologyIRI+"#r"));
		OWLClass classA=factory.getOWLClass(IRI.create(ontologyIRI+"#A"));
		OWLClass classB=factory.getOWLClass(IRI.create(ontologyIRI+"#B"));
		
		OWLClassExpression someValuesFrom=factory.getOWLObjectSomeValuesFrom(propR,classA);
		
		OWLAxiom equivalentClassAxiom=factory.getOWLEquivalentClassesAxiom(someValuesFrom, classB);
		
		manager.addAxiom(o,equivalentClassAxiom);
		
		NaiveMetaTranslation meta=new NaiveMetaTranslation(new Configuration(),new PrintWriter(System.out));
		meta.translateOntology(o);
	}
	
	public void voidForAllwithEqual() throws OWLOntologyCreationException {
		// forall r.A subClassOf B
		// B subClassOf forall r.A
		OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
		IRI ontologyIRI=IRI.create("http://test");
		OWLOntology o=manager.createOntology(ontologyIRI);
		OWLDataFactory factory=manager.getOWLDataFactory();
		
		OWLObjectProperty propR=factory.getOWLObjectProperty(IRI.create(ontologyIRI+"#r"));
		OWLClass classA=factory.getOWLClass(IRI.create(ontologyIRI+"#A"));
		OWLClass classB=factory.getOWLClass(IRI.create(ontologyIRI+"#B"));
		
		OWLClassExpression AllValuesFrom=factory.getOWLObjectAllValuesFrom(propR,classA);
		
		OWLAxiom equivalentClassAxiom=factory.getOWLEquivalentClassesAxiom(AllValuesFrom, classB);
		
		manager.addAxiom(o,equivalentClassAxiom);
		
	
	}
	
	public void voidDomain() throws OWLOntologyCreationException {
		// exists r.T subClassOf A
		OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
		IRI ontologyIRI=IRI.create("http://test");
		OWLOntology o=manager.createOntology(ontologyIRI);
		OWLDataFactory factory=manager.getOWLDataFactory();
		
		OWLObjectProperty propR=factory.getOWLObjectProperty(IRI.create(ontologyIRI+"#r"));
		OWLClass classA=factory.getOWLClass(IRI.create(ontologyIRI+"#A"));
		
		OWLAxiom objectPropertyDomain=factory.getOWLObjectPropertyDomainAxiom(propR, classA);
		manager.addAxiom(o, objectPropertyDomain);
		
		NaiveMetaTranslation meta=new NaiveMetaTranslation(new Configuration(),new PrintWriter(System.out));
		meta.translateOntology(o);
	}
	
	public void testRange() throws OWLOntologyCreationException {
		// T subClassOf forall r.A
		OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
		IRI ontologyIRI=IRI.create("http://test");
		OWLOntology o=manager.createOntology(ontologyIRI);
		OWLDataFactory factory=manager.getOWLDataFactory();
				
		OWLObjectProperty propR=factory.getOWLObjectProperty(IRI.create(ontologyIRI+"#r"));
		OWLClass classA=factory.getOWLClass(IRI.create(ontologyIRI+"#A"));
				
		OWLAxiom objectPropertyRange=factory.getOWLObjectPropertyRangeAxiom(propR, classA);
		manager.addAxiom(o, objectPropertyRange);
				
		NaiveMetaTranslation meta=new NaiveMetaTranslation(new Configuration(),new PrintWriter(System.out));
		meta.translateOntology(o);
	}
	
	public void testDisjoint() {
		
	}
	public static Test suit() {
		return new TestSuite(MetaNaiveTranslationTest.class);
	}
	
	
	public void testSimpleABox() {
		assertTrue(true);
	}
}
