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
package org.semanticweb.wolpertinger.translation.naive;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
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
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.wolpertinger.Configuration;
import org.semanticweb.wolpertinger.Prefixes;
import org.semanticweb.wolpertinger.structural.OWLAxioms;
import org.semanticweb.wolpertinger.structural.OWLAxioms.ComplexObjectPropertyInclusion;
import org.semanticweb.wolpertinger.structural.OWLAxioms.DisjunctiveRule;
import org.semanticweb.wolpertinger.structural.OWLNormalization;
import org.semanticweb.wolpertinger.translation.OWLOntologyTranslator;
import org.semanticweb.wolpertinger.translation.SignatureMapper;
import org.semanticweb.wolpertinger.translation.TranslationException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.manchester.cs.owl.owlapi.OWLAsymmetricObjectPropertyAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDisjointObjectPropertiesAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLIrreflexiveObjectPropertyAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectComplementOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectMinCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLReflexiveObjectPropertyAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubPropertyChainAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSymmetricObjectPropertyAxiomImpl;

/**
 * Implementation of the naive translation proposed in our paper, for encoding
 * a normalized OWL DL ontology into an answer set program.
 * <p>
 *
 * </p>
 *
 * @see OWLAxiomVisitor
 * @see OWLClassExpressionVisitor
 *
 * @author Lukas Schweizer
 * @author Satyadharma Tirtarasa
 */
public class NaiveTranslation implements OWLOntologyTranslator {

	private PrintWriter writer;
	private SignatureMapper mapper;
	private VariableIssuer var;

	private Set<OWLClass> auxClasses;

	// inclusions resutling from, e.g. resolving nominals
	private Collection<OWLClassExpression[]> newInclusions;
	private Configuration configuration;
	
	// 
	private OWLOntology rootOntology;
	private boolean assertionMode;
	
	/**
	 * Creates a {@link NaiveTranslation} instance
	 * @param configuration
	 * @param writer
	 */
	public NaiveTranslation(Configuration configuration, PrintWriter writer) {
		// TODO: based on config parameter instantiate the name mappers (nice,std)
		this.mapper = SignatureMapper.ASP2CoreMapping;
		this.newInclusions = new LinkedList<OWLClassExpression[]>();
		this.auxClasses = new HashSet<OWLClass>();
		this.configuration = configuration;
		this.writer = writer;
		this.assertionMode = false;
		var = new VariableIssuer();
	}

	// ----------------------
	// BEGIN MEMBER methods
	// ----------------------

	/**
	 * Load the root ontology and all imports and apply normalization.
	 */
	private OWLAxioms loadOntology(OWLOntology rootOntology) {
		this.rootOntology = rootOntology;
		
		OWLAxioms axioms = new OWLAxioms();
		Collection<OWLOntology> importClosure = rootOntology.getImportsClosure();

		if(configuration.getDomainIndividuals() == null) {
			configuration.setDomainIndividuals(rootOntology.getIndividualsInSignature(true));
		}

		OWLNormalization normalization = new OWLNormalization(rootOntology.getOWLOntologyManager().getOWLDataFactory(), axioms, 0, configuration.getDomainIndividuals());

		for (OWLOntology ontology : importClosure) {
			normalization.processOntology(ontology);
		}
		
		return axioms;
	}

	private void clearState() {
		this.newInclusions = new LinkedList<OWLClassExpression[]>();
		this.auxClasses = new HashSet<OWLClass>();
	}

	public void individualAssertionMode(OWLNamedIndividual individual) {
		this.var = new IndividualIssuer (mapper.getConstantName(individual));
		this.assertionMode = true;
	}

	/**
	 * Translate the given {@link OWLOntology}.
	 * Note that the result is written to the PrintWriter, which was given in the constructor.
	 */
	public void translateOntology(OWLOntology rootOntology) {
		this.rootOntology = rootOntology;
		translateOntology(loadOntology(rootOntology));
	}

	/**
	 * We have the OWLOntology(ies) now (normalized) in our internal data model representation.
	 *
	 * @param normalizedOntology
	 */
	public void translateOntology(OWLAxioms normalizedOntology) throws TranslationException {
		clearState();
		
		// thing assertions for all named individuals
		for (OWLNamedIndividual individual : normalizedOntology.m_namedIndividuals) {
			// TODO: avoid adding assertions to owl:Thing when there is already real owl:Thing assertions
			assertThing(individual);
			writer.println();
			var.reset();
		}
		
		// ABox axioms
		for (OWLIndividualAxiom assertion : normalizedOntology.m_facts) {
			assertion.accept(this);
			writer.println();
			var.reset();
		}

		// TBox axioms
		for (OWLClassExpression[] inclusion : normalizedOntology.m_conceptInclusions) {
			boolean hasDataTypeClassExpression = false;
			for (int ii = 0; ii < inclusion.length; ii++) {
				OWLClassExpression c = inclusion[ii];
				// currently data properties are not supported
				if (c instanceof OWLDataSomeValuesFrom || c instanceof OWLDataAllValuesFrom ||
					c instanceof OWLDataMaxCardinality) {
					hasDataTypeClassExpression = true;
					continue;
				}
			}
			if (hasDataTypeClassExpression) continue;
			translateInclusion(inclusion);
			var.reset();
		}

		// RBox
		for (OWLObjectPropertyExpression objectPropertyExp : normalizedOntology.m_complexObjectPropertyExpressions) {
			// TODO
		}

		for	(ComplexObjectPropertyInclusion complexObjPropertyInclusion : normalizedOntology.m_complexObjectPropertyInclusions) {
			LinkedList<OWLObjectPropertyExpression> subPropertyList = new LinkedList<OWLObjectPropertyExpression> ();
			for (OWLObjectPropertyExpression e : complexObjPropertyInclusion.m_subObjectProperties) {
				subPropertyList.add(e.getObjectPropertiesInSignature().iterator().next());
			}
			OWLSubPropertyChainAxiomImpl prop = new OWLSubPropertyChainAxiomImpl(subPropertyList, complexObjPropertyInclusion.m_superObjectProperty, new LinkedList<OWLAnnotation>());
			prop.accept(this);
			var.reset();
			writer.println();
		}

		for(OWLObjectPropertyExpression objPropertyExp : normalizedOntology.m_asymmetricObjectProperties) {
			//
			OWLAsymmetricObjectPropertyAxiomImpl asyProp = new OWLAsymmetricObjectPropertyAxiomImpl(objPropertyExp, new LinkedList<OWLAnnotation>());
			asyProp.accept(this);
			var.reset();
			writer.println();
		}

		for (OWLObjectPropertyExpression objPropertyExp : normalizedOntology.m_irreflexiveObjectProperties) {
			OWLIrreflexiveObjectPropertyAxiomImpl irrProp = new OWLIrreflexiveObjectPropertyAxiomImpl(objPropertyExp, new LinkedList<OWLAnnotation>());
			irrProp.accept(this);
			var.reset();
			writer.println();
		}

		for (OWLObjectPropertyExpression objPropertyExp : normalizedOntology.m_reflexiveObjectProperties) {
			OWLReflexiveObjectPropertyAxiomImpl refProp = new OWLReflexiveObjectPropertyAxiomImpl(objPropertyExp, new LinkedList<OWLAnnotation>());
			refProp.accept(this);
			var.reset();
			writer.println();
		}

		for (OWLObjectPropertyExpression[] properties : normalizedOntology.m_disjointObjectProperties) {
			HashSet<OWLObjectPropertyExpression> props = new HashSet<OWLObjectPropertyExpression>();
			for (OWLObjectPropertyExpression property : properties) {
				props.add(property);
			}
			OWLDisjointObjectPropertiesAxiomImpl disProp = new OWLDisjointObjectPropertiesAxiomImpl(props, new LinkedList<OWLAnnotation>());
			disProp.accept(this);
			var.reset();
			writer.println();
		}

		for (OWLObjectPropertyExpression[] objectProperty : normalizedOntology.m_simpleObjectPropertyInclusions) {
			if(objectProperty[0].getInverseProperty().equals(objectProperty[1])) {
				OWLSymmetricObjectPropertyAxiomImpl prop = new OWLSymmetricObjectPropertyAxiomImpl(objectProperty[0], new LinkedList<OWLAnnotation>());
				prop.accept(this);
			} else {
				LinkedList<OWLObjectPropertyExpression> subProperty = new LinkedList<OWLObjectPropertyExpression> ();
				subProperty.add(objectProperty[0]);
				OWLSubPropertyChainAxiomImpl prop = new OWLSubPropertyChainAxiomImpl(subProperty, objectProperty[1], new LinkedList<OWLAnnotation>());
				prop.accept(this);
			}
			var.reset();
			writer.println();
		}

		// translate remaining new inclusions, mainly dealing with auxiliary classes
		for (OWLClassExpression[] inclusion : newInclusions) {
			//translateInclusion(inclusion);
			var.reset();
		}

		// add assertions of nominal guard classes
		for (OWLNamedIndividual individual : nominalGuards.keySet()) {
			OWLClass guard = nominalGuards.get(individual);
			String guardName = mapper.getPredicateName(guard);

			if (configuration.getDomainIndividuals().contains(individual)) {
				writer.write(guardName);
				writer.write(ASP2CoreSymbols.BRACKET_OPEN);
				writer.write(mapper.getConstantName(individual));
				writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
				writer.write(ASP2CoreSymbols.EOR);
				writer.println();
			} else {
				String thing = "thing";

				// 1 {guard_i_x(X):thing(X) } 1.
				writer.write("1 {");
				writer.write(guardName);
				writer.write(ASP2CoreSymbols.BRACKET_OPEN);
				writer.write(var.currentVar);
				writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
				writer.write(ASP2CoreSymbols.CONDITION);
				writer.write(thing);
				writer.write(ASP2CoreSymbols.BRACKET_OPEN);
				writer.write(var.currentVar);
				writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
				writer.write("} 1");
				writer.write(ASP2CoreSymbols.EOR);
				writer.println();
			}
			var.reset();
		}

		// SWRL Rules
		for (DisjunctiveRule rule : normalizedOntology.m_rules) {
			translateSWRLRule(rule);
			writer.println();
		}
		
		// Guessing
		Set<OWLClass> closedConcepts = configuration.getClosedConcepts();
		for (OWLClass owlClass : normalizedOntology.m_classes) {
			if(!closedConcepts.contains(owlClass)) {
				createExtensionGuess(owlClass);
				var.reset();
				writer.println();
			} else {
				// closed concept
			}
		}

		// Take care of auxiliary classes, if there are any...
		//if (null != auxClasses) {
		for (OWLClass owlClass : auxClasses) {
			if (isOneOfAuxiliaryClass(owlClass)) {

			} else {
				createExtensionGuess(owlClass);
				var.reset();
				writer.println();
			}
		}
		//}

		Set<OWLObjectProperty> closedRoles = configuration.getClosedRoles();
		for (OWLObjectProperty property : normalizedOntology.m_objectProperties) {
			if(!closedRoles.contains(property)) {
				createExtensionGuess(property);
				var.reset();
				writer.println();
			} else {
				// closed role
			}
		}

		// add #showp/n.  satements if required
		for (IRI conceptIRI : configuration.getConceptNamesToProjectOn()) {
			String conceptName = mapper.getPredicateName(new OWLClassImpl(conceptIRI));

			writer.write("#show " + conceptName + "/1.");
			writer.println();
		}
		
		if (configuration.getConceptNamesToProjectOn().isEmpty()) {
			for (OWLObjectProperty property : normalizedOntology.m_objectProperties) {
				String predicateName = mapper.getPredicateName(property);
				writer.write("#show " + predicateName + "/2.");
				writer.println();
			}
		
			for (OWLClass concept : normalizedOntology.m_classes) {
				String conceptName = mapper.getPredicateName(concept);
				writer.write("#show " + conceptName + "/1.");
				writer.println();
			}
		}

		writer.flush();
	}

	private void translateSWRLRule(DisjunctiveRule rule) {
		String owlThing = "thing";
		SWRLAtom[] head = rule.m_head;
		SWRLAtom[] body = rule.m_body;
		writer.write(ASP2CoreSymbols.IMPLICATION);
		for (SWRLAtom atom : head) {
			writer.write(ASP2CoreSymbols.NAF);
			writer.write(ASP2CoreSymbols.SPACE);
			writer.write(translateSWRLAtom(atom));
		}
		
		for (SWRLAtom atom : body) {
			writer.write(ASP2CoreSymbols.CONJUNCTION);
			writer.write(translateSWRLAtom(atom));
		}
		writer.write(ASP2CoreSymbols.EOR);
	}

	private String translateSWRLAtom(SWRLAtom atom) {
		String translation = null;
		if (atom instanceof SWRLClassAtom) {
			SWRLClassAtom classAtom = (SWRLClassAtom) atom;
			SWRLArgument argument =  classAtom.getArgument();
			String conceptName = classAtom.getPredicate().asOWLClass().getIRI().getShortForm().toLowerCase();
			String fillerName = "";
			if (argument instanceof SWRLVariable) {
				SWRLVariable variable = (SWRLVariable) argument;
				fillerName = variable.getIRI().getShortForm();
			} else if (argument instanceof SWRLIndividualArgument) {
				SWRLIndividualArgument individualArgument = (SWRLIndividualArgument) argument;
				fillerName = "i_" + individualArgument.getIndividual().asOWLNamedIndividual().getIRI().getShortForm().toLowerCase();
			}
			translation = String.format("%s(%s)", conceptName, fillerName);
		} else if (atom instanceof SWRLObjectPropertyAtom) {
			SWRLObjectPropertyAtom objectPropertyAtom = (SWRLObjectPropertyAtom) atom;
			Collection<SWRLArgument> arguments = objectPropertyAtom.getAllArguments();
			String roleName = objectPropertyAtom.getPredicate().asOWLObjectProperty().getIRI().getShortForm().toLowerCase();
			String[] fillerName = new String[2];
			int counter = 0;
			for (SWRLArgument argument : arguments) {
				if (argument instanceof SWRLVariable) {
					SWRLVariable variable = (SWRLVariable) argument;
					fillerName[counter] = variable.getIRI().getShortForm();					
				} else if (argument instanceof SWRLIndividualArgument) {
					SWRLIndividualArgument individualArgument = (SWRLIndividualArgument) argument;
					fillerName[counter] = "i_" + individualArgument.getIndividual().asOWLNamedIndividual().getIRI().getShortForm().toLowerCase();
				}
				counter++;
			}
			translation = String.format("%s(%s,%s)", roleName, fillerName[0], fillerName[1]);
		} else {
			translation = " soon ";
		}
		return translation;
	}
	
	/**
	 * 
	 */
	public Set<OWLAxiom> retranslateSolution(String answerSet) {
		HashSet<OWLAxiom> assertions = new HashSet<OWLAxiom>();
		
		for (String fact : answerSet.split(" ")) {
			OWLAxiom assertionAxiom = getAssertionAxiom(fact);
		
			// check if axiom does not already exists
			if (null != assertionAxiom) {
				assertions.add(assertionAxiom);
			}
		}
		
		return assertions;
	}
	
	/**
	 * Given an assertion as string, the corresponding OWL axiom is created.
	 * Note: if the input ontology does not contain the predicate, null is returned.
	 * 
	 * @param _assertion Assertions as string, e.g. "color(red)".
	 * @return {@link OWLAxiom} representing the assertion. In case of an class assertion 
	 * this is {@link OWLClassAssertionAxiom}, in case of an role assertion it is {@link OWLObjectPropertyAssertionAxiom}.
	 * <br /><b>Returns null if the assertion is an internal / auxiliary construct not present in the input ontology. </b>
	 */
	private OWLIndividualAxiom getAssertionAxiom(String _assertion) {
		String[] predParts = _assertion.split("\\(|\\)");
		if (predParts.length == 2) {
			String predicateName = predParts[0];
			String argumentString = predParts[1];
			
			String[] arguments = argumentString.split(",");
			if (arguments.length == 1) { // concept assertion
				String argument = arguments[0];							
				OWLClass concept = mapper.getOWLClass(predicateName);
				
				if (null == concept)
					return null;
				
				OWLNamedIndividual individual = mapper.getOWLIndividual(argument);
				
				OWLDataFactory factory = OWLManager.getOWLDataFactory();

				// TODO: Somehow this contains-check fails !?
//				if (rootOntology.containsEntityInSignature(concept.getIRI()) && 
//						!concept.getIRI().toString().startsWith("internal:") &&
//						!concept.getIRI().toString().startsWith("urn:monum")) {
						OWLClassAssertionAxiom owlClassAssertion = factory.getOWLClassAssertionAxiom(concept, individual);
						
						return owlClassAssertion;
				//}
			
			}
			// now the binary predicates == properties
			else if (arguments.length == 2) { // role assertion
				OWLNamedIndividual indiv1 = mapper.getOWLIndividual(arguments[0]);
				OWLNamedIndividual indiv2 = mapper.getOWLIndividual(arguments[1]);
			
				OWLDataFactory factory = OWLManager.getOWLDataFactory();
				OWLObjectProperty property = mapper.getOWLObjectProperty(predicateName);
				OWLObjectPropertyAssertionAxiom propAss = factory.getOWLObjectPropertyAssertionAxiom(property, indiv1, indiv2);
				
				return propAss;
			}
		}
		return null;
	}
	
	private void createShowStatementForClasses(OWLAxioms normalizedOntology) {
		for (OWLClass owlClass : normalizedOntology.m_classes) {
			createShowStatement(owlClass);
			writer.println();
		}
	}

	private void createShowStatementForProperties(OWLAxioms normalizedOntology) {
		for (OWLObjectProperty property : normalizedOntology.m_objectProperties) {
			createShowStatement(property);
			writer.println();
		}

	}
	private void createShowStatement(OWLClass owlClass) {
		String className = mapper.getPredicateName(owlClass);

		writer.write("#show " + className + "/1.");
	}

	private void createShowStatement(OWLObjectProperty property) {
		String propertyName = mapper.getPredicateName(property);

		writer.write("#show " + propertyName + "/2.");
	}

	private void translateInclusion(OWLClassExpression[] inclusion) {
		writer.print(ASP2CoreSymbols.IMPLICATION);

		boolean isFirst=true;
		for (OWLClassExpression classExp : inclusion) {
			if (!isFirst) {
				writer.print(ASP2CoreSymbols.CONJUNCTION);
			}
			classExp.accept(this);
			isFirst=false;
		}
		var.reset();
		String currentVar = var.currentVar();
		if(!isFirst) {
			writer.print(ASP2CoreSymbols.CONJUNCTION);
		}
		writer.print("thing");
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(currentVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print(ASP2CoreSymbols.EOR);
		writer.println();
	}

	public void translateEntailment (OWLAxioms normalizedOntology) {
		for (OWLClassExpression[] inclusion : normalizedOntology.m_conceptInclusions) {
			translateEntailmentInclusion(inclusion);
		}
	}

	private void translateEntailmentInclusion(OWLClassExpression[] inclusion) {
		writer.print(ASP2CoreSymbols.IMPLICATION);

		boolean isFirst=true;
		for (OWLClassExpression classExp : inclusion) {
			if (!isFirst) {
				writer.print(ASP2CoreSymbols.CONJUNCTION);
			}
			classExp.accept(this);
			isFirst=false;
		}
		var.reset();
		if(!assertionMode){
			if(!isFirst) {
				writer.print(ASP2CoreSymbols.CONJUNCTION);
			}
			writer.print("thing");
			writer.print(ASP2CoreSymbols.BRACKET_OPEN);
			writer.print("X"); //var issuer problem
			writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		}
		writer.print(ASP2CoreSymbols.EOR);
		writer.println();
		var.reset();
	}

	/**
	 * For the given class, the extension guess of the form
	 * <code>
	 * -c(X) :- not c(X), thing(X).
	 * c(X) :- not -c(X), thing(X).
	 * </code>
	 *
	 * TODO: Improve OWLThing handling
	 * @param owlClass
	 */
	private void createExtensionGuess(OWLClass owlClass) {
		String owlThing = "thing";

		String className = mapper.getPredicateName(owlClass);
		String negClassName = "-" + className;

		writer.print(className);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(var.currentVar());
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);

		writer.print(ASP2CoreSymbols.IMPLICATION);
		writer.print(ASP2CoreSymbols.NAF + " ");
		writer.print(negClassName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(var.currentVar());
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);

		writer.print(ASP2CoreSymbols.CONJUNCTION);

		writer.print(owlThing);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(var.currentVar());
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print(ASP2CoreSymbols.EOR);

		writer.println();

		writer.print(negClassName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(var.currentVar());
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);

		writer.print(ASP2CoreSymbols.IMPLICATION);
		writer.print(ASP2CoreSymbols.NAF + " ");
		writer.print(className);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(var.currentVar());
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);

		writer.print(ASP2CoreSymbols.CONJUNCTION);

		writer.print(owlThing);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(var.currentVar());
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print(ASP2CoreSymbols.EOR);
	}

	/**
	 * For the given property, create the extension guess of the form
	 * <code>
	 * r(X,Y) :- not -r(X,Y), thing(X), thing(Y).
	 * -r(X,Y) :- not r(X,Y), thing(X), thing(Y).
	 * </code>
	 * @param property
	 */
	private void createExtensionGuess(OWLObjectProperty property) {
		String owlThing = "thing";
		String propertyName = mapper.getPredicateName(property);
		String negPropertyName = "-" + propertyName;

		String currentVar = var.currentVar();
		String nextVar = var.nextVariable();

		// r(X,Y) :- not -r(X,Y), thing(X), thing(Y).
		writer.print(propertyName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(currentVar);
		writer.print(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.print(nextVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);

		writer.print(ASP2CoreSymbols.IMPLICATION);

		writer.print(ASP2CoreSymbols.NAF);
		writer.print(" ");
		writer.print(negPropertyName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(currentVar);
		writer.print(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.print(nextVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print(ASP2CoreSymbols.CONJUNCTION);

		writer.print(owlThing);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(currentVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print(ASP2CoreSymbols.CONJUNCTION);

		writer.print(owlThing);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(nextVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		//writer.print(ASP2CoreSymbols.CONJUNCTION);
		writer.print(ASP2CoreSymbols.EOR);

		writer.println();

		//-r(X,Y) :- not r(X,Y), thing(X), thing(Y).
		writer.print(negPropertyName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(currentVar);
		writer.print(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.print(nextVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);

		writer.print(ASP2CoreSymbols.IMPLICATION);

		writer.print(ASP2CoreSymbols.NAF);
		writer.print(" ");
		writer.print(propertyName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(currentVar);
		writer.print(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.print(nextVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print(ASP2CoreSymbols.CONJUNCTION);

		writer.print(owlThing);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(currentVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print(ASP2CoreSymbols.CONJUNCTION);

		writer.print(owlThing);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(nextVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print(ASP2CoreSymbols.EOR);
	}

	private void createComplexInclusion(ComplexObjectPropertyInclusion complexObjPropertyInclusion) {
		LinkedList<String> chainPredicateNameList = new LinkedList<String> ();
		for (OWLObjectPropertyExpression e : complexObjPropertyInclusion.m_subObjectProperties) {
			chainPredicateNameList.add(mapper.getPredicateName(e.getObjectPropertiesInSignature().iterator().next()));
		}
		String superPropertyName = (mapper.getPredicateName(complexObjPropertyInclusion.m_superObjectProperty.getNamedProperty()));

		int counter = 1;
		writer.print(":-");
		for (String subPropertyName : chainPredicateNameList) {
			writer.print(String.format("%s(X%d,X%d),", subPropertyName, counter, ++counter));
		}
		writer.print(String.format("not %s(X%d,X%d).", superPropertyName, 1, counter));
	}

	private void assertThing(OWLNamedIndividual individual) {
		String owlThing = "thing";
		String individualName = mapper.getConstantName(individual);

		// thing(a).
		writer.print(owlThing);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(individualName);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print(ASP2CoreSymbols.EOR);
	}

	public SignatureMapper getSignatureMapper () {
		return this.mapper;
	}
	/**
	 * Provides a sequence of variables X,Y,Y1,Y2,...
	 */
	private class VariableIssuer {
		int counter = 0;
		String currentVar = "X";

		public VariableIssuer() {

		}

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

	private class IndividualIssuer extends VariableIssuer {
		String individualName = null;

		public IndividualIssuer(String individualName) {
			this.individualName = individualName;
		}

		
		public String currentVar () {
			return individualName;
		}

		
		public String nextVariable() {
			return individualName;
		}


	}

	// ----------------------
	// BEGIN OWLAxiomVisitor methods
	// ----------------------

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAnnotationAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom)
	 */
	
	public void visit(OWLAnnotationAssertionAxiom arg0) {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAnnotationAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom)
	 */
	
	public void visit(OWLSubAnnotationPropertyOfAxiom arg0) {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAnnotationAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom)
	 */
	
	public void visit(OWLAnnotationPropertyDomainAxiom arg0) {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAnnotationAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom)
	 */
	
	public void visit(OWLAnnotationPropertyRangeAxiom arg0) {
		throw new NotImplementedException();
	}

	/**
	 * Determines, whether the given {@link OWLClass} is an auxiliary class name introduced
	 * while normalizing.
	 *
	 * @param auxClass
	 * @return
	 */
	private boolean isAuxiliaryClass(OWLClass auxClass) {
		return Prefixes.isInternalIRI(auxClass.getIRI().toString());
	}

	private boolean isOneOfAuxiliaryClass(OWLClass owlClass) {
		if (Prefixes.isInternalIRI(owlClass.getIRI().toString())) {
			String iriString = owlClass.getIRI().toString();
			boolean isOneOf = iriString.substring(iriString.lastIndexOf(":") + 1, iriString.lastIndexOf("#")).equals("nnq");
			return isOneOf;
		} else {
			return false;
		}
	}

	/**
	 * According to the naive translation:<br/>
	 * <br/>
	 * <code>naive(A) := not A(X)</code>
	 *
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLClass)
	 */
	
	public void visit(OWLClass owlClass) {
		String predicateName = mapper.getPredicateName(owlClass);
		writer.print(ASP2CoreSymbols.NAF + " ");
		writer.print(predicateName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(var.currentVar());
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);

		if (isAuxiliaryClass(owlClass)) auxClasses.add(owlClass);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectIntersectionOf)
	 */
	
	public void visit(OWLObjectIntersectionOf arg0) {
		// should not occur since axioms are normalized
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectUnionOf)
	 */
	
	public void visit(OWLObjectUnionOf arg0) {
		// should not occur since axioms are normalized
		throw new NotImplementedException();
	}

	/**
	 * We assume that we deal with a normalized axioms, i.e. they are in NNF and structural transformation took place.
	 *
	 * Thereofre we test here whether the operand
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectComplementOf)
	 */
	
	public void visit(OWLObjectComplementOf objComplementOf) {
		OWLClassExpression operand = objComplementOf.getOperand();
		if (operand instanceof OWLClass) {
			OWLClass owlClass = operand.asOWLClass();
			String predicateName = mapper.getPredicateName(owlClass);

			writer.print(predicateName);
			writer.print(ASP2CoreSymbols.BRACKET_OPEN);
			writer.print(var.currentVar());
			writer.print(ASP2CoreSymbols.BRACKET_CLOSE);

			if (isAuxiliaryClass(owlClass)) auxClasses.add(owlClass);
		}
		else if (operand instanceof OWLObjectHasSelf) {
			OWLObjectHasSelf owlHasSelf = (OWLObjectHasSelf) operand;
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
		else if (operand instanceof OWLObjectOneOf) {
			throw new NotImplementedException();
		} 
	}

	/**
	 * According to the naive translation:<br/>
	 * <br/>
	 * <code>naive(Exists r.A) := not r(X,Y), A(Y)</code>
	 *
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom)
	 */
	
	public void visit(OWLObjectSomeValuesFrom objExistential) {
		// we require normalized axioms, therefore we can do the following
		OWLObjectPropertyExpression property = objExistential.getProperty();
		OWLClassExpression fillerClass = objExistential.getFiller();

		OWLObjectMinCardinality minCard = new OWLObjectMinCardinalityImpl(property, 1, fillerClass);
		visit(minCard);
	}

	/**
	 * According to the naive translation:<br/>
	 * <br/>
	 * <code>naive(ForAll r.A) := r(X,Y), not A(Y)</code>
	 *
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectAllValuesFrom)
	 */
	
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
		String propertyName = mapper.getPredicateName(property);

		//String className = mapper.getPredicateName(fillerClass);
		
		String roleVar1 = var.currentVar();
		String roleVar2 = var.nextVariable();
		String conceptVar = var.currentVar();
		
		if (isInverseOf) {
			String temp = roleVar1;
			roleVar1 = roleVar2;
			roleVar2 = temp;
		}

		// r(X,Y),
		writer.print(propertyName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(roleVar1);
		writer.print(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.print(roleVar2);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);

		// distinguish -A or A
		// complex fillers are not possible anymore at this stage
		if (filler instanceof OWLObjectComplementOf) {
			OWLClassExpression expr = ((OWLObjectComplementOf) filler).getOperand();
			if (expr instanceof OWLObjectOneOf) {
				OWLObjectOneOf oneOf = (OWLObjectOneOf) expr;
				OWLClass auxOneOf = getOneOfAuxiliaryClass(oneOf);

				String auxOneOfName = mapper.getPredicateName(auxOneOf);
				writer.print(ASP2CoreSymbols.CONJUNCTION);
				writer.write(auxOneOfName);
				writer.write(ASP2CoreSymbols.BRACKET_OPEN);
				writer.write(conceptVar);
				writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
			} else {
				assert !expr.isAnonymous();

				OWLClass owlClass = expr.asOWLClass();
				String predicateName = mapper.getPredicateName(owlClass);

				// A(X)
				writer.print(ASP2CoreSymbols.CONJUNCTION);
				writer.print(predicateName);
				writer.print(ASP2CoreSymbols.BRACKET_OPEN);
				writer.print(conceptVar);
				writer.print(ASP2CoreSymbols.BRACKET_CLOSE);

				if (isAuxiliaryClass(owlClass)) auxClasses.add(owlClass);
			}
		}
		else if (filler instanceof OWLObjectOneOf) {
			OWLObjectOneOf oneOf = (OWLObjectOneOf) filler;
			OWLClass auxOneOf = getOneOfAuxiliaryClass(oneOf);

			String auxOneOfName = mapper.getPredicateName(auxOneOf);
			writer.print(ASP2CoreSymbols.CONJUNCTION);
			writer.print(ASP2CoreSymbols.NAF + " ");
			writer.write(auxOneOfName);
			writer.write(ASP2CoreSymbols.BRACKET_OPEN);
			writer.write(conceptVar);
			writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
		}
		else if (filler.isOWLNothing()) {
			// do nothing
		}
		else {
			assert filler instanceof OWLClass;
			String predicateName = mapper.getPredicateName(filler.asOWLClass());

			// not A(X).
			writer.print(ASP2CoreSymbols.CONJUNCTION);
			writer.print(ASP2CoreSymbols.NAF + " ");
			writer.print(predicateName);
			writer.print(ASP2CoreSymbols.BRACKET_OPEN);
			writer.print(conceptVar);
			writer.print(ASP2CoreSymbols.BRACKET_CLOSE);

			if (isAuxiliaryClass(filler.asOWLClass()))
				auxClasses.add(filler.asOWLClass());
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectHasValue)
	 */
	
	public void visit(OWLObjectHasValue arg0) {
		// TODO Auto-generated method stub

	}

	private HashMap<OWLObjectOneOf, OWLClass> oneOfAuxClasses = new HashMap<OWLObjectOneOf, OWLClass>();

	/**
	 * For a One-of Object {a,b,c,...} create and auxiliary class oo1, and
	 * and axiom <code>oo1 subSetOf guard_i_a or guard_i_b or ...</code>
	 * @param objectOneOf
	 * @return
	 */
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

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectMinCardinality)
	 */
	
	public void visit(OWLObjectMinCardinality minCardinality) {
		visitCardinalityRestriction(minCardinality);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectExactCardinality)
	 */
	
	public void visit(OWLObjectExactCardinality exactCardinality) {
		visitCardinalityRestriction(exactCardinality);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectMaxCardinality)
	 */
	
	public void visit(OWLObjectMaxCardinality maxCardinality) {
		visitCardinalityRestriction(maxCardinality);
	}

	private void visitCardinalityRestriction(OWLObjectCardinalityRestriction cardinalityRestriction) {
		OWLClassExpression filler = cardinalityRestriction.getFiller();
		OWLObjectPropertyExpression property = cardinalityRestriction.getProperty();

		String fillerName, comperator;

		if (cardinalityRestriction instanceof OWLObjectMinCardinality)
			comperator = "<";
		else if (cardinalityRestriction instanceof OWLObjectMaxCardinality)
			comperator = ">";
		else
			comperator = "=";

		if (filler instanceof OWLObjectComplementOf){
			OWLClassExpression operand = ((OWLObjectComplementOf) filler).getOperand();
			fillerName = mapper.getPredicateName(((OWLObjectComplementOf) filler).getOperand().asOWLClass());
			fillerName = ASP2CoreSymbols.NAF + " " + fillerName;
			if (isAuxiliaryClass(operand.asOWLClass()))
				auxClasses.add(operand.asOWLClass());
		}
		else if (filler instanceof OWLObjectOneOf) {
			System.out.println("Shouldn't be here anymore");
			//TODO: in case of a max-cardinality we will never end up within here,
			// since the normalization for max-cardinality uses an "optimization".
			OWLObjectOneOf oneOf = (OWLObjectOneOf) filler;
			OWLClass auxOneOf= getOneOfAuxiliaryClass(oneOf);

			fillerName = mapper.getPredicateName(auxOneOf);
		}
		else {
			assert filler instanceof OWLClass;

			fillerName = mapper.getPredicateName(filler.asOWLClass());

			if (isAuxiliaryClass(filler.asOWLClass()))
				auxClasses.add(filler.asOWLClass());
		}

		assert property instanceof OWLObjectProperty;

		boolean isInverseOf = false;

		if(property instanceof OWLObjectInverseOf) {
			isInverseOf = true;
			property = ((OWLObjectInverseOf) property).getInverse();
		} else {

		}

		String propertyName = mapper.getPredicateName(property.asOWLObjectProperty());

		String currentVar = var.currentVar();
		String nextVar = var.nextVariable();
		String classVar = nextVar;

		if (isInverseOf) {
			String temp = currentVar;
			currentVar = nextVar;
			nextVar = temp;
		}

		writer.print("#count{");
		writer.print(classVar + "," + propertyName + ":");
		writer.print(propertyName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(currentVar);
		writer.print(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.print(nextVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print(ASP2CoreSymbols.CONJUNCTION);
		writer.print(fillerName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(classVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print("}" + comperator + cardinalityRestriction.getCardinality());

		var.reset();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectHasSelf)
	 */
	
	public void visit(OWLObjectHasSelf owlHasSelf) {
		OWLObjectProperty property = owlHasSelf.getProperty().asOWLObjectProperty();
		String propertyName = mapper.getPredicateName(property);
		String cVar = var.currentVar();

		// not r(X,X)
		writer.print(ASP2CoreSymbols.NAF + " ");
		writer.print(propertyName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(cVar);
		writer.print(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.print(cVar);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
	}

	/**
	 *
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectOneOf)
	 */
	
	public void visit(OWLObjectOneOf owlOneOf) {
		boolean isFirst=true;
		for (OWLIndividual individual : owlOneOf.getIndividuals()) {
			if (individual.isNamed()){
				if (!isFirst)
					writer.write(ASP2CoreSymbols.CONJUNCTION);

				OWLClass guard = getNominalGuard(individual.asOWLNamedIndividual());
				visit(guard);
				isFirst=false;
			}
		}
	}

	private static final String INTERNAL_IRI_PREFIX = "http://www.semanticweb.org/wolpertinger/internal";

	private HashMap<OWLNamedIndividual, OWLClass> nominalGuards = new HashMap<OWLNamedIndividual, OWLClass>();

	//get access to the nominal_list, in order to be used by SPARQL Module
	public HashMap<OWLNamedIndividual,OWLClass> getNominalGuards(){
		return nominalGuards;
	}
	
	private OWLClass getNominalGuard(OWLNamedIndividual individual) {
		if (nominalGuards.containsKey(individual))
			return nominalGuards.get(individual);
		String className = "guard_" + mapper.getConstantName(individual);
		OWLClass guard = new OWLClassImpl(IRI.create(INTERNAL_IRI_PREFIX + "#" + className));
		nominalGuards.put(individual, guard);
		return guard;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataSomeValuesFrom)
	 */
	
	public void visit(OWLDataSomeValuesFrom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataAllValuesFrom)
	 */
	
	public void visit(OWLDataAllValuesFrom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataHasValue)
	 */
	
	public void visit(OWLDataHasValue arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataMinCardinality)
	 */
	
	public void visit(OWLDataMinCardinality arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataExactCardinality)
	 */
	
	public void visit(OWLDataExactCardinality arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataMaxCardinality)
	 */
	
	public void visit(OWLDataMaxCardinality arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDeclarationAxiom)
	 */
	
	public void visit(OWLDeclarationAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSubClassOfAxiom)
	 */
	
	public void visit(OWLSubClassOfAxiom arg0) {
		throw new IllegalStateException("At this stage OWLSubClassOfAxioms should have been normalized and replaced by OWLObjectUnionOf");
	}

	/**
	 *
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom)
	 */
	
	public void visit(OWLNegativeObjectPropertyAssertionAxiom negObjPropertyAssertion) {
		//assert !negObjPropertyAssertion.getProperty().isAnonymous();

		OWLObjectProperty property = negObjPropertyAssertion.getProperty().asOWLObjectProperty();
		String propertyName = mapper.getPredicateName(property);

		OWLNamedIndividual subject = negObjPropertyAssertion.getSubject().asOWLNamedIndividual();
		OWLNamedIndividual object = negObjPropertyAssertion.getObject().asOWLNamedIndividual();

		String subjectName = mapper.getConstantName(subject);
		String objectName = mapper.getConstantName(object);

		writer.print(ASP2CoreSymbols.CLASSICAL_NEGATION);
		writer.print(propertyName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(subjectName);
		writer.print(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.print(objectName);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print(ASP2CoreSymbols.EOR);
	}

	/**
	 * Asymetric Role Assertion <i>Asy(r)</i> is translated to:</br>
	 * </br>
	 * :- r(X,Y),r(Y,X).
	 *
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom)
	 */
	
	public void visit(OWLAsymmetricObjectPropertyAxiom asymetricProperty) {
		writer.write(ASP2CoreSymbols.IMPLICATION);

		OWLObjectPropertyExpression property = asymetricProperty.getProperty();

		String propertyName = mapper.getPredicateName(property.getNamedProperty());
		String cVar = var.currentVar();
		String nVar = var.nextVariable();

		writer.write(propertyName);
		writer.write(ASP2CoreSymbols.BRACKET_OPEN);
		writer.write(cVar);
		writer.write(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.write(nVar);
		writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.write(ASP2CoreSymbols.CONJUNCTION);
		//writer.write(ASP2CoreSymbols.NAF);
		//writer.write(ASP2CoreSymbols.SPACE);
		writer.write(propertyName);
		writer.write(ASP2CoreSymbols.BRACKET_OPEN);
		writer.write(nVar);
		writer.write(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.write(cVar);
		writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.write(ASP2CoreSymbols.EOR);
	}

	/**
	 * A reflexive role assertion<i>Ref(r)</i> is translated into the constraint:</br></br>
	 *
	 * <code>
	 * :- not r(X,X), thing(X).
	 * </code>
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom)
	 */
	
	public void visit(OWLReflexiveObjectPropertyAxiom refPropertyAxiom) {
		OWLObjectPropertyExpression property = refPropertyAxiom.getProperty();
		String propertyName = mapper.getPredicateName(property.getNamedProperty());
		String cVar = var.currentVar();

		writer.write(ASP2CoreSymbols.IMPLICATION);
		writer.write(ASP2CoreSymbols.NAF);
		writer.write(" ");
		writer.write(propertyName);
		writer.write(ASP2CoreSymbols.BRACKET_OPEN);
		writer.write(cVar);
		writer.write(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.write(cVar);
		writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.write(ASP2CoreSymbols.CONJUNCTION);
		writer.write("thing(");
		writer.write(cVar);
		writer.write(")"+ASP2CoreSymbols.EOR);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDisjointClassesAxiom)
	 */
	
	public void visit(OWLDisjointClassesAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom)
	 */
	
	public void visit(OWLDataPropertyDomainAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom)
	 */
	
	public void visit(OWLObjectPropertyDomainAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom)
	 */
	
	public void visit(OWLEquivalentObjectPropertiesAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom)
	 */
	
	public void visit(OWLNegativeDataPropertyAssertionAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom)
	 */
	
	public void visit(OWLDifferentIndividualsAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom)
	 */
	
	public void visit(OWLDisjointDataPropertiesAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Disjoint Properties are translated into constraints:</br>
	 * <code>:- r(X,Y), s(X,Y), ...</code>
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom)
	 */
	
	public void visit(OWLDisjointObjectPropertiesAxiom disProperties) {
		writer.write(ASP2CoreSymbols.IMPLICATION);

		String cVar = var.currentVar();
		String nVar = var.nextVariable();

		boolean isFirst=true;
		for (OWLObjectPropertyExpression property : disProperties.getProperties()) {
			String propertyName = mapper.getPredicateName(property.getNamedProperty());

			if (!isFirst) {
				writer.write(ASP2CoreSymbols.CONJUNCTION);
			}

			isFirst = false;
			writer.write(propertyName);
			writer.write(ASP2CoreSymbols.BRACKET_OPEN);
			writer.write(cVar);
			writer.write(ASP2CoreSymbols.ARG_SEPERATOR);
			writer.write(nVar);
			writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
		}

		writer.write(ASP2CoreSymbols.EOR);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom)
	 */
	
	public void visit(OWLObjectPropertyRangeAxiom arg0) {
		throw new IllegalStateException("OWLObjectRangeAxiom should have been normalized (rewritten) already.");
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom)
	 */
	
	public void visit(OWLObjectPropertyAssertionAxiom objPropertyAssertion) {
		OWLObjectProperty property = objPropertyAssertion.getProperty().asOWLObjectProperty();
		OWLNamedIndividual subject = objPropertyAssertion.getSubject().asOWLNamedIndividual();
		OWLNamedIndividual object = objPropertyAssertion.getObject().asOWLNamedIndividual();

		String propertyName = mapper.getPredicateName(property);
		String subjectName = mapper.getConstantName(subject);
		String objectName = mapper.getConstantName(object);

		writer.print(propertyName);
		writer.print(ASP2CoreSymbols.BRACKET_OPEN);
		writer.print(subjectName);
		writer.print(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.print(objectName);
		writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.print(ASP2CoreSymbols.EOR);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom)
	 */
	
	public void visit(OWLFunctionalObjectPropertyAxiom arg0) {
		throw new IllegalStateException("OWLFunctionalObjectPropertyAxiom should have been normalized (rewritten) already.");
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom)
	 */
	
	public void visit(OWLSubObjectPropertyOfAxiom arg0) {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDisjointUnionAxiom)
	 */
	
	public void visit(OWLDisjointUnionAxiom arg0) {
		throw new IllegalStateException("OWLDisjointUnionAxiom should have been normalized (rewritten) already.");
	}

	/**
	 * Symetric Role Asstertion <i>Sym(r)</i>is translated as:
	 * </br></br>
	 * :- r(X,Y), not r(Y,X).</br>
	 * And thereby ruling out solutions with symetric paris (X,Y)and (Y,X).
	 *
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom)
	 */
	
	public void visit(OWLSymmetricObjectPropertyAxiom symmetricProperty) {
		writer.write(ASP2CoreSymbols.IMPLICATION);
		OWLObjectPropertyExpression property = symmetricProperty.getProperty();

		String propertyName = mapper.getPredicateName(property.getNamedProperty());
		String cVar = var.currentVar();
		String nVar = var.nextVariable();

		writer.write(propertyName);
		writer.write(ASP2CoreSymbols.BRACKET_OPEN);
		writer.write(cVar);
		writer.write(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.write(nVar);
		writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.write(ASP2CoreSymbols.CONJUNCTION);
		writer.write(ASP2CoreSymbols.NAF);
		writer.write(ASP2CoreSymbols.SPACE);
		writer.write(propertyName);
		writer.write(ASP2CoreSymbols.BRACKET_OPEN);
		writer.write(nVar);
		writer.write(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.write(cVar);
		writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.write(ASP2CoreSymbols.EOR);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom)
	 */
	
	public void visit(OWLDataPropertyRangeAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom)
	 */
	
	public void visit(OWLFunctionalDataPropertyAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom)
	 */
	
	public void visit(OWLEquivalentDataPropertiesAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 *
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLClassAssertionAxiom)
	 */
	
	public void visit(OWLClassAssertionAxiom classAssertion) {
		OWLClassExpression classExpression = classAssertion.getClassExpression();
		OWLNamedIndividual individual = classAssertion.getIndividual().asOWLNamedIndividual();

		if (classExpression instanceof OWLObjectComplementOf) {
			OWLObjectComplementOf complementOf = (OWLObjectComplementOf) classExpression;
			OWLClass owlClass = complementOf.getOperand().asOWLClass();

			// not A(a).
			writer.print(ASP2CoreSymbols.CLASSICAL_NEGATION);
			writer.print(mapper.getPredicateName(owlClass));
			writer.print(ASP2CoreSymbols.BRACKET_OPEN);
			writer.print(mapper.getConstantName(individual));
			writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
			writer.print(ASP2CoreSymbols.EOR);
		} else {
			OWLClass owlClass = classExpression.asOWLClass();
			OWLClass guard = getNominalGuard(individual);

			if (isOneOfAuxiliaryClass(owlClass)) {
				writer.print(mapper.getPredicateName(owlClass));
				writer.print(ASP2CoreSymbols.BRACKET_OPEN);
				writer.print(var.currentVar());
				writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
				writer.print(ASP2CoreSymbols.IMPLICATION);
				writer.print(mapper.getPredicateName(guard));
				writer.print(ASP2CoreSymbols.BRACKET_OPEN);
				writer.print(var.currentVar());
				writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
				writer.print(ASP2CoreSymbols.EOR);
			} else {
				// A(a).
				writer.print(mapper.getPredicateName(owlClass));
				writer.print(ASP2CoreSymbols.BRACKET_OPEN);
				writer.print(mapper.getConstantName(individual));
				writer.print(ASP2CoreSymbols.BRACKET_CLOSE);
				writer.print(ASP2CoreSymbols.EOR);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom)
	 */
	
	public void visit(OWLEquivalentClassesAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom)
	 */
	
	public void visit(OWLDataPropertyAssertionAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom)
	 */
	
	public void visit(OWLTransitiveObjectPropertyAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * An irreflexive role assertion <i>Irr(r)</i> is tranlated into the constrained:</br>
	 * </br>
	 * <code>
	 * :- r(X,X).
	 * </code>
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom)
	 */
	
	public void visit(OWLIrreflexiveObjectPropertyAxiom irrPropertyAxiom) {
		OWLObjectPropertyExpression property = irrPropertyAxiom.getProperty();
		String propertyName = mapper.getPredicateName(property.getNamedProperty());
		String cVar = var.currentVar();
		//String nVar = var.nextVariable();

		writer.write(ASP2CoreSymbols.IMPLICATION);
		writer.write(propertyName);
		writer.write(ASP2CoreSymbols.BRACKET_OPEN);
		writer.write(cVar);
		writer.write(ASP2CoreSymbols.ARG_SEPERATOR);
		writer.write(cVar);
		writer.write(ASP2CoreSymbols.BRACKET_CLOSE);
		writer.write(ASP2CoreSymbols.EOR);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom)
	 */
	
	public void visit(OWLSubDataPropertyOfAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom)
	 */
	
	public void visit(OWLInverseFunctionalObjectPropertyAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSameIndividualAxiom)
	 */
	
	public void visit(OWLSameIndividualAxiom arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom)
	 */
	
	public void visit(OWLSubPropertyChainOfAxiom arg0) {
		// TODO Auto-generated method stub
		String superPropertyName = null;
		OWLObjectPropertyExpression superPropertyExpression = arg0.getSuperProperty();

		int counter = 1;
		writer.print(":-");

		for (OWLObjectPropertyExpression subPropertyExpression : arg0.getPropertyChain()) {
			int firstCounter = counter;
			int secondCounter = ++counter;
			String subPropertyName = null;

			if (subPropertyExpression instanceof OWLObjectInverseOf) {
				int temp = firstCounter;
				firstCounter = secondCounter;
				secondCounter = temp;
				subPropertyName = mapper.getPredicateName(((OWLObjectInverseOf) subPropertyExpression).getInverse().asOWLObjectProperty());
			} else {
				subPropertyName = mapper.getPredicateName(subPropertyExpression.asOWLObjectProperty());
			}

			writer.print(String.format("%s(X%d,X%d),", subPropertyName, firstCounter, secondCounter));
		}
		if (superPropertyExpression instanceof OWLObjectInverseOf) {
			superPropertyName = mapper.getPredicateName(((OWLObjectInverseOf) superPropertyExpression).getInverse().asOWLObjectProperty());
			writer.print(String.format("not %s(X%d,X%d).", superPropertyName, counter, 1));
		} else {
			superPropertyName = mapper.getPredicateName(arg0.getSuperProperty().asOWLObjectProperty());
			writer.print(String.format("not %s(X%d,X%d).", superPropertyName, 1, counter));
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom)
	 */
	
	public void visit(OWLInverseObjectPropertiesAxiom arg0) {

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLHasKeyAxiom)
	 */
	
	public void visit(OWLHasKeyAxiom arg0) {

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom)
	 */
	
	public void visit(OWLDatatypeDefinitionAxiom arg0) {

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.SWRLRule)
	 */
	
	public void visit(SWRLRule arg0) {
		
	}
	
	public void visit(OWLObjectProperty arg0) {
	}

	
	public void visit(OWLObjectInverseOf arg0) {
	}

	
	public void visit(OWLDataProperty arg0) {
	}

	public void visit(OWLAnnotationProperty arg0) {		
	}

}
