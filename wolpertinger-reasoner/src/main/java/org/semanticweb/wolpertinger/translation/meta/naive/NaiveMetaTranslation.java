/*  Copyright 2015 by the International Center for Computational Logic, Technical University Dresden.

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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.wolpertinger.Configuration;
import org.semanticweb.wolpertinger.Prefixes;
import org.semanticweb.wolpertinger.structural.OWLAxioms;
import org.semanticweb.wolpertinger.structural.OWLNormalization;
import org.semanticweb.wolpertinger.translation.OWLOntologyTranslator;
import org.semanticweb.wolpertinger.translation.SignatureMapper;
import org.semanticweb.wolpertinger.translation.TranslationException;
import org.semanticweb.wolpertinger.translation.naive.ASP2CoreSymbols;

import com.google.common.base.Optional;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectComplementOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectMinCardinalityImpl;

/**
 * Implements a translation from OWL DL to ASP wrt. to a given finite domain
 * (so-called fixed-domain).
 * 
 * We call this translation meta, since we treat individuals, as well as concepts
 * and roles as constants in ASP. 
 * 
 * ...
 * 
 * @author Lukas Schweizer
 *
 */
public class NaiveMetaTranslation implements OWLOntologyTranslator {

	private Configuration configuration;
	private PrintWriter writer;
	private VariableIssuer var;
	
	private MetaTranslationIRIMapper iriMapper;
	
	private HashSet<OWLClass> auxClasses;
		
	// ------------
	// CONSTANTS and util functions
	// ------------
	public static final String TRIPLE = "triple";
	
	public static final String triple(String subject, String predicate, String object) {
		return String.format("%s(%s,%s,%s)", TRIPLE, subject, predicate, object);
	}
	
	public static final String defaultNegate(String triple) {
		return String.format("%s %s", ASPConstants.DEFAULT_NET, triple);
	}
	
	// rdf(s) constants
	private static final IRI RDF_TYPE_IRI = IRI.create(Prefixes.STANDARD_PREFIXES.getPrefixIRI("rdf:").concat("type"));

	public NaiveMetaTranslation(Configuration configuration, PrintWriter writer) {
		this.configuration = configuration;
		this.writer = writer;
		this.mapper=SignatureMapper.ASP2CoreMapping;
		this.iriMapper = new QuotedStringIriMapper();
		this.newInclusions = new LinkedList<OWLClassExpression[]>();
		this.var = new VariableIssuer();
		this.auxClasses = new HashSet<OWLClass>();
	}
	
	/**
	 * Load the root ontology and all imports and apply normalization.
	 */
	private OWLAxioms loadOntology(OWLOntology rootOntology) {
		OWLAxioms axioms = new OWLAxioms();
		
		Collection<OWLOntology> importClosure = rootOntology.getImportsClosure();

		if(configuration.getDomainIndividuals()== null) {
			configuration.setDomainIndividuals(rootOntology.getIndividualsInSignature(Imports.INCLUDED));
		}

		OWLNormalization normalization = new OWLNormalization(rootOntology.getOWLOntologyManager().getOWLDataFactory(), axioms, 0, configuration.getDomainIndividuals());

		for (OWLOntology ontology : importClosure) {
			normalization.processOntology(ontology);
		}
		return axioms;
	}
	
	public void translateOntology(OWLAxioms normalizedOntology) {
	
		// assertions
		//In this step, ontology.m_facts does not include all types of assertions 
		//e.g. the AnnotationAssertions
		
		//Abox
		for (OWLIndividualAxiom assertion : normalizedOntology.m_facts) {
			assertion.accept(this);
			writer.println(ASPConstants.EOR);
			var.reset();
		}
		
//		//so, we need to introduce m_annotations (in OWLNormalization, OWLAxioms)
//		for(OWLAnnotationAxiom assertion: normalizedOntology.m_annotations) {
//			assertion.accept(this);
//			writer.println(ASPConstants.EOR);
//			var.reset();
//		}
		
		
		//TBox
		for (OWLClassExpression[] axiom : normalizedOntology.m_conceptInclusions) {
			// recall: axioms are of the form C1 or C2 or ..or Cn  where Ci is might be negated and of form ::= A,\forall r.A, \exists r.A ..
			translateAxiom(axiom);
			writer.println(ASPConstants.EOR);
			var.reset();
		}
		
		// RBox
		for (OWLObjectPropertyExpression objectPropertyExp : normalizedOntology.m_complexObjectPropertyExpressions) {
				// TODO
		}

		//----------------------------------------------------
		//--ClosedConcepts and ClosedRoles--
		//----------------------------------------------------
				
		// declare vocabulary
		for (OWLClass owlClass : normalizedOntology.m_classes) {
			writer.println(String.format("class(%s).", iriMapper.getIriRepresentation(owlClass.getIRI())));
		}
		
		//IS IT NECESSARY SINCE AUXTRIPLES ARE ALL SUPPORTED?
		for (OWLClass auxclass : auxClasses) {
			writer.println(String.format("aux(%s).", iriMapper.getIriRepresentation(auxclass.getIRI())));
		}
		
		for (OWLObjectProperty property : normalizedOntology.m_objectProperties) {
			// also aux properties?
			writer.println(String.format("property(%s).", iriMapper.getIriRepresentation(property.getIRI())));
		}
		//------------------------------------------------------
		//here m_namedIndividuals should be replaced by m_domain
		//-----------------------------------------------------_
//		for (OWLNamedIndividual namedIndiv : normalizedOntology.m_namedIndividuals) {
//			writer.println(String.format("domain(%s).", iriMapper.getIriRepresentation(namedIndiv.getIRI())));
//		}
		
		for(OWLNamedIndividual namedIndiv : configuration.getDomainIndividuals()) {
			writer.println(String.format("domain(%s).", iriMapper.getIriRepresentation(namedIndiv.getIRI())));
		}
		
		// translate remaining new inclusions, mainly dealing with auxiliary classes
		for (OWLClassExpression[] inclusion : newInclusions) {
			//translateInclusion(inclusion);
			var.reset();
		}
		
		//add assertions of nominal guard classes
		for (OWLNamedIndividual individual : nominalGuards.keySet()) {
			OWLClass guard = nominalGuards.get(individual);
			String guardName = mapper.getPredicateName(guard);

			//guard_a(a).
			if (configuration.getDomainIndividuals().contains(individual)) {
				writer.write(guardName);
				writer.write(ASP2CoreSymbols.BRACKET_OPEN);
				writer.write(iriMapper.getIriRepresentation(individual.getIRI()));
				writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
				writer.write(ASP2CoreSymbols.EOR);
				writer.println();
			} else {
				String domain = "domain";

				// 1 {guard_i_x(X):domain(X) } 1.
				writer.write("1 {");
				writer.write(guardName);
				writer.write(ASP2CoreSymbols.BRACKET_OPEN);
				writer.write(var.currentVar);
				writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
				writer.write(ASP2CoreSymbols.CONDITION);
				writer.write(domain);
				writer.write(ASP2CoreSymbols.BRACKET_OPEN);
				writer.write(var.currentVar);
				writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
				writer.write("} 1");
				writer.write(ASP2CoreSymbols.EOR);
				writer.println();
			}
			var.reset();
		}
		
		
		
		if(normalizedOntology.m_conceptInclusions.isEmpty()) {
			
		}else {
			// add interpretation guessing
			writer.println("{triple(S,P,O) : domain(S), property(P), domain(O), not -triple(S,P,O)}.");
			writer.println("{triple(S,"+iriMapper.getIriRepresentation(RDF_TYPE_IRI)+",O) : domain(S), class(O), "
					+ "not -triple(S,"+iriMapper.getIriRepresentation(RDF_TYPE_IRI)+",O)}.");
			if(!auxClasses.isEmpty()) {
				writer.println("{triple(S,"+iriMapper.getIriRepresentation(RDF_TYPE_IRI)+",O) : domain(S), aux(O), "
					+ "not -triple(S,"+iriMapper.getIriRepresentation(RDF_TYPE_IRI)+",O)}.");
			}
			
			//writer.println("#show triple/3.");
			
		}
		writer.flush();
	}
	
	private void translateAxiom(OWLClassExpression[] axiom) {
		boolean first=true;
		
		if (1 <= axiom.length)
			writer.write(ASPConstants.IMPLICATION);
		
		for (OWLClassExpression disjunct : axiom) {
			if (!first) 
				writer.write(ASPConstants.AND);
						
			disjunct.accept(this);
						
			first=false;
		}
	}
	

	/**
	 * Provides a sequence of variables X,Y,Y1,Y2,...
	 */
	private class VariableIssuer {
		int counter = 0;
		String currentVar = "X";

		public void reset() {
			counter = 0;
			currentVar = "X";
		}

		public String currentVar() {
			return currentVar;
		}

		public String nextVariable() {
			String nvar = "X";

			counter++;
			if (counter == 1) nvar = "Y";
			else if (counter > 1) nvar = "Y".concat(String.valueOf(counter-1));

			return currentVar = nvar;
		}

	}
	
	// -- OWLOntologyTranslator Implementation 
	
	public void translateOntology(OWLOntology rootOntology) {
		translateOntology(loadOntology(rootOntology));
	}
	
	public void visit(OWLDeclarationAxiom arg0) {
		visitUnsupported(arg0);
	}

	public void visit(OWLDatatypeDefinitionAxiom arg0) {
		visitUnsupported(arg0);
	}

	
	
	public void visit(OWLAnnotationAssertionAxiom arg0) {
		OWLAnnotationSubject subject=arg0.getSubject();
		OWLAnnotationProperty property=arg0.getProperty();
		OWLAnnotationValue value=arg0.getValue();
		//well subject do not have any method related to IRI
		writer.write(triple("\""+subject.toString()+"\"",iriMapper.getIriRepresentation(property.getIRI()),processAnnotationValue(value)));
	}

	public String processAnnotationValue(OWLAnnotationValue value) {
		Optional<IRI> opt_IRI=value.asIRI();
		IRI iri;
		if(opt_IRI.isPresent()) {
			iri=opt_IRI.get();
			return iriMapper.getIriRepresentation(iri);
		}else {
			//not an IRI, may be a literal
			Optional<OWLLiteral> opt_literal=value.asLiteral();
			OWLLiteral literal;
			if(opt_literal.isPresent()) {
				literal=opt_literal.get();
				if(literal.isInteger()) {
					return literal.getLiteral();
				}else if(literal.isFloat()||literal.isDouble()) {
					return "\""+literal.getLiteral()+"\"";
				}else if(literal.isRDFPlainLiteral()) {
					return "\""+literal.getLiteral()+"\"";
				}else {
					return literal.getLiteral();
				}
			}else {
				return value.toString();
			}
		}		
	}
	
	
	public void visit(OWLSubAnnotationPropertyOfAxiom arg0) {
		// TODO Auto-generated method stub

	}

	public void visit(OWLAnnotationPropertyDomainAxiom arg0) {
		// TODO Auto-generated method stub

	}

	public void visit(OWLAnnotationPropertyRangeAxiom arg0) {
		// TODO Auto-generated method stub
	}

	public void visit(OWLSubClassOfAxiom arg0) {
		throw new TranslationException("Unsupported Axiom. Subclassof Axioms should have been normalized first!");
	}

	public void visit(OWLNegativeObjectPropertyAssertionAxiom axmNegPropAss) {
		writer.write(ASPConstants.STRONG_NEG);
		
		String subject = iriMapper.getIriRepresentation(axmNegPropAss.getSubject().asOWLNamedIndividual().getIRI());
		String predicate = iriMapper.getIriRepresentation(axmNegPropAss.getProperty().asOWLObjectProperty().getIRI());
		String object = iriMapper.getIriRepresentation(axmNegPropAss.getObject().asOWLNamedIndividual().getIRI());
		
		writer.write(triple(subject, predicate, object));		
	}

	public void visit(OWLAsymmetricObjectPropertyAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLReflexiveObjectPropertyAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLDisjointClassesAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLDataPropertyDomainAxiom arg0) {
		visitUnsupported(arg0);
	}

	public void visit(OWLObjectPropertyDomainAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLEquivalentObjectPropertiesAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLNegativeDataPropertyAssertionAxiom arg0) {
		// TODO Auto-generated method stub

	}

	public void visit(OWLDifferentIndividualsAxiom arg0) {
		// TODO Auto-generated method stub

	}

	public void visit(OWLDisjointDataPropertiesAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLDisjointObjectPropertiesAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLObjectPropertyRangeAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLObjectPropertyAssertionAxiom axmPropAss) {
		String subject = iriMapper.getIriRepresentation(axmPropAss.getSubject().asOWLNamedIndividual().getIRI());
		String predicate= iriMapper.getIriRepresentation(axmPropAss.getProperty().asOWLObjectProperty().getIRI());
		String object = iriMapper.getIriRepresentation(axmPropAss.getObject().asOWLNamedIndividual().getIRI());
		
		writer.write(triple(subject, predicate, object));
	}

	public void visit(OWLFunctionalObjectPropertyAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLSubObjectPropertyOfAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLDisjointUnionAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLSymmetricObjectPropertyAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLDataPropertyRangeAxiom arg0) {
		visitUnsupported(arg0);
	}

	public void visit(OWLFunctionalDataPropertyAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLEquivalentDataPropertiesAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	// Pos. & Neg. Class Assertions
	// TODO: Nominal Guards
	
	public void visit(OWLClassAssertionAxiom axmClassAssertion) {
		OWLClassExpression classExpr = axmClassAssertion.getClassExpression();
		OWLNamedIndividual namedIndiv = axmClassAssertion.getIndividual().asOWLNamedIndividual();
		OWLClass namedClass;
		
		if (classExpr instanceof OWLObjectComplementOf) {
			// -A(a)
			writer.write(ASPConstants.STRONG_NEG);
			namedClass = ((OWLObjectComplementOf)classExpr).getOperand().asOWLClass();
			
			//notice--18.26--25.10.2018
			if(isAuxiliaryClass(namedClass)) {
				auxClasses.add(namedClass);
			}
			
			writer.write(triple(iriMapper.getIriRepresentation(namedIndiv.getIRI()), iriMapper.getIriRepresentation(RDF_TYPE_IRI),
					iriMapper.getIriRepresentation(namedClass.getIRI())));
		}else {
			OWLClass owlClass = classExpr.asOWLClass();
			OWLClass guard = getNominalGuard(namedIndiv);

			if (isOneOfAuxiliaryClass(owlClass)) {
				writer.print(triple(var.currentVar(),iriMapper.getIriRepresentation(RDF_TYPE_IRI), 
						iriMapper.getIriRepresentation(owlClass.getIRI())));
				writer.print(ASP2CoreSymbols.IMPLICATION);
				
				writer.print(mapper.getPredicateName(guard));
				writer.print(ASP2CoreSymbols.BRACKET_OPEN);
				writer.print(var.currentVar());
				writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
			} else {
				// A(a).
				writer.print(triple(iriMapper.getIriRepresentation(namedIndiv.getIRI()),
						iriMapper.getIriRepresentation(RDF_TYPE_IRI), 
						iriMapper.getIriRepresentation(owlClass.getIRI())));
			}
		}

	}
	
	private void visitUnsupported(OWLAxiom axiom) {
		
//		writer.write("======================================");
//		writer.write("Not yet supported: "+axiom.toString());
//		writer.write("======================================");
		
		//throw new TranslationException("Not yet supported: " + axiom.toString());
		writer.println("%% Not yet supported: "+axiom.toString());
	}
	
	private void visitUnsupported(OWLClassExpression classExpr) {
		//throw new TranslationException("Not yet supported: " + classExpr.toString());
		writer.println("%% Not yet supported :"+classExpr.toString());
	}

	public void visit(OWLEquivalentClassesAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLDataPropertyAssertionAxiom dataPropAssertion) {
		OWLLiteral value =dataPropAssertion.getObject();
		
		OWLNamedIndividual subj = dataPropAssertion.getSubject().asOWLNamedIndividual();
		OWLDataProperty property = dataPropAssertion.getProperty().asOWLDataProperty();

		if (value.isInteger()) {
			writer.write(triple(iriMapper.getIriRepresentation(subj.getIRI()), 
					iriMapper.getIriRepresentation(property.getIRI()), value.getLiteral()));
			writer.write(triple(iriMapper.getIriRepresentation(subj.getIRI()), iriMapper.getIriRepresentation(property.getIRI()), value.getLiteral()));
		}else if(value.isFloat()||value.isDouble()) {
			writer.write(triple(iriMapper.getIriRepresentation(subj.getIRI()), iriMapper.getIriRepresentation(property.getIRI()), "\""+value.getLiteral()+"\""));
		}else if(value.isRDFPlainLiteral()) {
			writer.write(triple(iriMapper.getIriRepresentation(subj.getIRI()), iriMapper.getIriRepresentation(property.getIRI()), "\""+value.getLiteral()+"\""));
		}else {
			throw new TranslationException("Unsupported datatype " + value);
		}
	}

	public void visit(OWLTransitiveObjectPropertyAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLIrreflexiveObjectPropertyAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLSubDataPropertyOfAxiom arg0) {
		visitUnsupported(arg0);
	}

	public void visit(OWLInverseFunctionalObjectPropertyAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLSameIndividualAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLSubPropertyChainOfAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLInverseObjectPropertiesAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLHasKeyAxiom arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(SWRLRule arg0) {
		throw new TranslationException("Rules are not supported yet. " + arg0.toString());
	}

	public void visit(OWLClass owlClass) {
		visit(owlClass, true);
	}
	
	
	public void visit(OWLClass owlClass, boolean polarity) {
		String subjectVar = var.currentVar();
		String object = iriMapper.getIriRepresentation(owlClass.getIRI());
		
		if (polarity)
			writer.write(defaultNegate(triple(subjectVar, iriMapper.getIriRepresentation(RDF_TYPE_IRI), object)));
		else 
			writer.write(triple(subjectVar, iriMapper.getIriRepresentation(RDF_TYPE_IRI), object));
		
		if (isAuxiliaryClass(owlClass))
			auxClasses.add(owlClass);
	}

	public void visit(OWLObjectIntersectionOf arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLObjectUnionOf arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLObjectComplementOf objComp) {
		 OWLClassExpression classExpr = objComp.getOperand();

		 if (classExpr instanceof OWLClass) {
			 visit(classExpr.asOWLClass(), false);
		 } else if (classExpr instanceof OWLObjectHasSelf) {
			 // e.g. r(X,X)
			 visit((OWLObjectHasSelf) classExpr);
		 }else if(classExpr instanceof OWLObjectOneOf) {
			 visit((OWLObjectOneOf)classExpr);
		 }
	}

	public void visit(OWLObjectSomeValuesFrom owlExists) {
		// we require normalized axioms, therefore we can do the following
		OWLObjectPropertyExpression property = owlExists.getProperty();
		OWLClassExpression fillerClass = owlExists.getFiller();

		OWLObjectMinCardinality minCard = new OWLObjectMinCardinalityImpl(property, 1, fillerClass);
		visit(minCard);
	}

	public void visit(OWLObjectAllValuesFrom allValFrom) {
		OWLObjectProperty property;
		boolean isInverseOf = false;
 
		if(allValFrom.getProperty() instanceof OWLObjectInverseOf) {
			isInverseOf = true;
			property = ((OWLObjectInverseOf) allValFrom.getProperty()).getInverse().asOWLObjectProperty();
		} else {
			property = allValFrom.getProperty().asOWLObjectProperty();
		}

		OWLClassExpression filler = allValFrom.getFiller();
		String predicate = iriMapper.getIriRepresentation(property.getIRI());

		//String className = mapper.getPredicateName(fillerClass);
		
		String roleVar1 = var.currentVar();
		String roleVar2 = var.nextVariable();
		
		if (isInverseOf) {
			String temp = roleVar1;
			roleVar1 = roleVar2;
			roleVar2 = temp;
		}

		// r(X,Y) = triple(X,r,Y)
		writer.write(triple(roleVar1, predicate, roleVar2));
		//Without the comment, there will be syntax error in ASP Program?Why?
		//if (!filler.isOWLNothing() && !filler.isBottomEntity())
				writer.write(ASPConstants.AND);
		// now the filler, this might be
		// A,-A,{a},-{a}
		if (filler instanceof OWLObjectComplementOf) {
			visit((OWLObjectComplementOf) filler);
		} else if (filler instanceof OWLClass) {
			visit((OWLClass) filler);
		} else if (filler instanceof OWLObjectOneOf) {
			visit((OWLObjectOneOf) filler);
		}
		// Note: filler might also be OWLNothing :), but then we simply do 
		// nothing here which is fine according to the semantics
		var.reset();
	}

	public void visit(OWLObjectHasValue arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLObjectMinCardinality arg0) {
		visitCardinalityRestriction(arg0);
	}

	public void visit(OWLObjectExactCardinality arg0) {
		visitCardinalityRestriction(arg0);
	}

	public void visit(OWLObjectMaxCardinality arg0) {
		visitCardinalityRestriction(arg0);
	}
	

	public void visit(OWLObjectHasSelf arg0) {
		//throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
		OWLObjectHasSelf owlHasSelf = (OWLObjectHasSelf) arg0;
		OWLObjectProperty property = owlHasSelf.getProperty().asOWLObjectProperty();
		String propertyName = mapper.getPredicateName(property);
		String cVar = var.currentVar();

		// r(X,X)
		writer.print(propertyName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(cVar);
		writer.print(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.print(cVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
	}


	public void visit(OWLObjectOneOf arg0) {
		boolean isFirst=true;
		for (OWLIndividual individual : arg0.getIndividuals()) {
			if (individual.isNamed()){
				if (!isFirst)
					writer.write(ASP2CoreSymbols.CONJUNCTION);

				OWLClass guard = getNominalGuard(individual.asOWLNamedIndividual());
				visit(guard);
				isFirst=false;
			}
		}
	}

	public void visit(OWLDataSomeValuesFrom arg0) {
		visitUnsupported(arg0);

	}

	public void visit(OWLDataAllValuesFrom arg0) {
		visitUnsupported(arg0);
	}

	public void visit(OWLDataHasValue arg0) {
		visitUnsupported(arg0);
	}

	public void visit(OWLDataMinCardinality arg0) {
		visitUnsupported(arg0);
	}

	public void visit(OWLDataExactCardinality arg0) {
		visitUnsupported(arg0);
	}

	public void visit(OWLDataMaxCardinality arg0) {
		visitUnsupported(arg0);
	}

	public void visit(OWLObjectProperty arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLObjectInverseOf arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	public void visit(OWLDataProperty arg0) {
		throw new TranslationException("Something went wrong, should be normalized: " + arg0.toString());
	}

	
	
	//Properties which are not be specified as objprop or dataprop will be 
	//recognized as AnnotaionProperties
	//e.g. <ub:name> in LUBM/University0_x.owl 
	public void visit(OWLAnnotationProperty arg0) {
		throw new TranslationException("Not implemented yet: " + arg0.toString());
	}

	
	
	public Set<OWLAxiom> retranslateSolution(String solution) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void visitCardinalityRestriction(OWLObjectCardinalityRestriction cardinalityRestriction) {
			OWLClassExpression filler=cardinalityRestriction.getFiller();
			OWLObjectPropertyExpression property=cardinalityRestriction.getProperty();
			
			String fillerTriple="",comparator="",fillerName="";
			
			if(cardinalityRestriction instanceof OWLObjectMinCardinality) {
				comparator="<";
			}else if(cardinalityRestriction instanceof OWLObjectMaxCardinality) {
				comparator=">";
			}else {
				comparator="=";
			}
			
			String currentVar=var.currentVar();
			String nextVar=var.nextVariable();
			nextVar=var.nextVariable();
			String classVar=nextVar;
			
			assert property instanceof OWLObjectProperty;
			
			boolean isInverseOf=false;
			
			if(property instanceof OWLObjectInverseOf) {
				isInverseOf=true;
				property=((OWLObjectInverseOf)property).getInverse();
			}else {
				
			}
			
			if(isInverseOf) {
				String temp=currentVar;
				currentVar=nextVar;
				nextVar=temp;
			}
			
			if(filler instanceof OWLObjectComplementOf) {
				OWLClassExpression operand=((OWLObjectComplementOf)filler).getOperand();
				fillerName=iriMapper.getIriRepresentation(operand.asOWLClass().getIRI());
				fillerTriple="not triple("+classVar+","+iriMapper.getIriRepresentation(RDF_TYPE_IRI)+","+fillerName+")";
				
				if(isAuxiliaryClass(operand.asOWLClass())) {
					auxClasses.add(operand.asOWLClass());
				}
			}else if(filler instanceof OWLObjectOneOf) {
				System.out.println("should'n be here anymore");
				
				OWLObjectOneOf oneOf = (OWLObjectOneOf) filler;
				OWLClass auxOneOf= getOneOfAuxiliaryClass(oneOf);

				fillerName = mapper.getPredicateName(auxOneOf);
			}else {
				assert filler instanceof OWLClass;
				
				fillerName=iriMapper.getIriRepresentation(filler.asOWLClass().getIRI());
				fillerTriple="triple("+classVar+","+iriMapper.getIriRepresentation(RDF_TYPE_IRI)+","+fillerName+")";
				if(isAuxiliaryClass(filler.asOWLClass())) {
					auxClasses.add(filler.asOWLClass());
				}
			}
			
		
			
			String propertyName=iriMapper.getIriRepresentation(property.asOWLObjectProperty().getIRI());
			
			writer.print("#count{");
			writer.print(classVar+":");
			writer.print("triple(");
			writer.print(currentVar);
			writer.print(",");
			writer.print(propertyName);
			writer.print(",");
			writer.print(nextVar);
			writer.print(")");
			writer.print(",");
			writer.print(fillerTriple);
			writer.print("}"+comparator+cardinalityRestriction.getCardinality());
			
			var.reset();
	}
	//------------------------
	//implementation of guards
	//------------------------
	private SignatureMapper mapper;
	
	private static final String INTERNAL_IRI_PREFIX = "http://www.semanticweb.org/wolpertinger/internal";

	private HashMap<OWLNamedIndividual, OWLClass> nominalGuards = new HashMap<OWLNamedIndividual, OWLClass>();

	//get access to the nominal_list, in order to be used by SPARQL Module
	public HashMap<OWLNamedIndividual,OWLClass> getNominalGuards(){
		return nominalGuards;
	}
	
	private String getHashedName(OWLNamedIndividual individual) {
		int code=individual.getIRI().toString().hashCode();
		String parsedCode=Integer.toString(code);
		if(parsedCode.charAt(0)=='-') {
			parsedCode="_"+parsedCode.substring(1);
		}else {
			
		}
		String s_guard="guard_"+parsedCode;
		return s_guard;
	}
	
	private OWLClass getNominalGuard(OWLNamedIndividual individual) {
		if (nominalGuards.containsKey(individual))
			return nominalGuards.get(individual);
		String className =getHashedName(individual);
		OWLClass guard = new OWLClassImpl(IRI.create(INTERNAL_IRI_PREFIX + "#" + className));
		nominalGuards.put(individual, guard);
		return guard;
	}

	/**
	 * Determines, whether the given {@link OWLClass} is an auxiliary class name introduced
	 * while normalizing.
	 *
	 * @param auxClass
	 * @return
	 */
	
	private boolean isAuxiliaryClass(OWLClass auxClass) {
		return iriMapper.isAuxiliaryClass(auxClass.getIRI());
	}
	
	
	private boolean isOneOfAuxiliaryClass(OWLClass owlClass) {
		if (isAuxiliaryClass(owlClass)) {
			String iriString = owlClass.getIRI().toString();
			boolean isOneOf = iriString.substring(iriString.lastIndexOf(":") + 1, iriString.lastIndexOf("#")).equals("nnq");
			return isOneOf;
		} else {
			return false;
		}
	}

	
	private HashMap<OWLObjectOneOf, OWLClass> oneOfAuxClasses = new HashMap<OWLObjectOneOf, OWLClass>();

	
	/**
	 * For a One-of Object {a,b,c,...} create and auxiliary class oo1, and
	 * and axiom <code>oo1 subSetOf guard_i_a or guard_i_b or ...</code>
	 * @param objectOneOf
	 * @return
	 */
	// inclusions resutling from, e.g. resolving nominals
	private Collection<OWLClassExpression[]> newInclusions;
	
	private OWLClass getOneOfAuxiliaryClass(OWLObjectOneOf objectOneOf) {
		if (oneOfAuxClasses.containsKey(objectOneOf))
			return oneOfAuxClasses.get(objectOneOf);

		OWLClass auxOneOf = new OWLClassImpl(IRI.create(INTERNAL_IRI_PREFIX + "#oneofaux" + (oneOfAuxClasses.size()+1)));
		OWLClassExpression[] inclusion = new OWLClassExpression[2];

		inclusion[0] = new OWLObjectComplementOfImpl(auxOneOf);
		inclusion[1] = objectOneOf;

		//translateInclusion(inclusion);
		newInclusions.add(inclusion);

		// add to the set of class which needs to be guessed
		// auxClasses.add(auxOneOf);
		oneOfAuxClasses.put(objectOneOf, auxOneOf);
		return auxOneOf;
	}
	
}
