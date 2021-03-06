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
package org.semanticweb.wolpertinger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.Version;
import org.semanticweb.wolpertinger.clingo.ClingoModelEnumerator;
import org.semanticweb.wolpertinger.clingo.ClingoSolver;
import org.semanticweb.wolpertinger.clingo.SolverFactory;
import org.semanticweb.wolpertinger.clingo.SolvingException;
import org.semanticweb.wolpertinger.hierarchy.Hierarchy;
import org.semanticweb.wolpertinger.hierarchy.HierarchyBuilder;
import org.semanticweb.wolpertinger.hierarchy.HierarchyNode;
import org.semanticweb.wolpertinger.sparql.Fixed;
import org.semanticweb.wolpertinger.sparql.SPARQLTranslation;
import org.semanticweb.wolpertinger.structural.OWLAxioms;
import org.semanticweb.wolpertinger.structural.OWLNormalization;
import org.semanticweb.wolpertinger.structural.OWLNormalizationWithTracer;
import org.semanticweb.wolpertinger.translation.SignatureMapper;
import org.semanticweb.wolpertinger.translation.TranslationException;
import org.semanticweb.wolpertinger.translation.debug.DebugTranslation;
import org.semanticweb.wolpertinger.translation.meta.naive.NaiveMetaTranslation;
import org.semanticweb.wolpertinger.translation.naive.NaiveTranslation;

/**
 * 
 * @author Lukas Schweizer
 * @author Satyadharma Tirtarasa
 */
public class Wolpertinger implements OWLReasoner {
 
	private OWLOntology rootOntology;
	// normalized kb
	private OWLAxioms axioms;
	private OWLDataFactory dataFactory;
	private Configuration configuration;

	private NaiveTranslation naiveTranslation;

	private File baseProgramTmpFile;

	private boolean classified;
	private Hierarchy<OWLClass> classHierarchy;
	
	private ClingoModelEnumerator enumerator;
	private OWLNormalization normalization;

	private boolean satisfiableClassesComputed;
	private List<String> satisfiableClasses;

	private HashSet<OWLClass> equalsToTopClasses;
	private HashSet<OWLClass> equalsToBottomClasses;

	private SignatureMapper mapper;
	
	public Wolpertinger(OWLOntology rootOntology) throws WolpertingerException {
		this(new Configuration(), rootOntology);
	}

	public Wolpertinger(Configuration configuration, OWLOntology rootOntology) throws WolpertingerException {
		this.rootOntology = rootOntology;
		this.configuration = configuration;
		loadOntology();
		classified = false;
		dataFactory = OWLManager.getOWLDataFactory();
	}

	/**
	 * Load the root ontology and all imports and apply normalization.
	 */
	private void loadOntology() throws WolpertingerException {
		clearState();

		axioms = new OWLAxioms();

		Collection<OWLOntology> importClosure = rootOntology.getImportsClosure();
		
		if(configuration.getDomainIndividuals().isEmpty()) {
			configuration.setDomainIndividuals(rootOntology.getIndividualsInSignature(Imports.INCLUDED));
		}

		normalization = new OWLNormalization(rootOntology.getOWLOntologyManager().getOWLDataFactory(), axioms, 0, configuration.getDomainIndividuals());
		
		for (OWLOntology ontology : importClosure) {
			normalization.processOntology(ontology);
		}

		axioms.m_namedIndividuals.clear();
		axioms.m_namedIndividuals.addAll(configuration.getDomainIndividuals());
		configuration.setClosedConcepts(getClosedConcepts());
		configuration.setClosedRoles(getClosedRoles());
		
		try {
			baseProgramTmpFile = File.createTempFile("wolpertinger-base-program", ".lp");
			baseProgramTmpFile.deleteOnExit();
			PrintWriter baseProgramWriter = new PrintWriter(baseProgramTmpFile);
			naiveTranslation = new NaiveTranslation(configuration, baseProgramWriter);
			naiveTranslation.translateOntology(axioms);
			
			
			
			mapper = naiveTranslation.getSignatureMapper();
		} catch (IOException e) {
			throw new WolpertingerException(e.getMessage());
		} catch (TranslationException te) {
			throw new WolpertingerException(te.getMessage());
		}
		
		// hotfix for owl:Thing is subsumed by owl:Nothing
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		PrefixManager pManager = new DefaultPrefixManager();
		OWLClassExpression thing = factory.getOWLClass("owl:Thing", pManager);
		
		/*
		// closed concepts
		for (OWLClass closedConcept : configuration.getClosedConcepts()) {
			Set<OWLNamedIndividual> nonassertedIndividuals = configuration.getDomainIndividuals();
			Collection<OWLIndividual> assertedIndividuals = EntitySearcher.getIndividuals(closedConcept, rootOntology);
			nonassertedIndividuals.removeAll(assertedIndividuals);
			for (OWLNamedIndividual individual : nonassertedIndividuals) {
				axioms.m_facts.add(factory.getOWLClassAssertionAxiom(new OWLObjectComplementOfImpl(closedConcept), individual));
			}
		}
		
		// closed roles
		for (OWLObjectProperty closedConcept : configuration.getClosedRoles()) {
			// make a copy of domain and remove asserted individuals
			Set<OWLNamedIndividual> nonassertedIndividuals = new HashSet<OWLNamedIndividual> (configuration.getDomainIndividuals());
			Collection<OWLIndividual> assertedIndividuals = EntitySearcher.getIndividuals(closedConcept, rootOntology);
			nonassertedIndividuals.removeAll(assertedIndividuals);
			for (OWLNamedIndividual individual : nonassertedIndividuals) {
				axioms.m_facts.add(factory.getOWLClassAssertionAxiom(new OWLObjectComplementOfImpl(closedConcept), individual));
			}
		}
		*/
	
		
		for (OWLNamedIndividual individual : axioms.m_namedIndividuals) {
			OWLClassAssertionAxiom assertion = factory.getOWLClassAssertionAxiom(thing, individual);
			manager.addAxiom(rootOntology, assertion);
		}
		
		enumerator = new ClingoModelEnumerator(new String[] {baseProgramTmpFile.getAbsolutePath()});
	}

	private Set<OWLClass> getClosedConcepts () {
		Set<OWLClass> closedConcepts = new HashSet<OWLClass> ();
		for (OWLClass cl : rootOntology.getClassesInSignature()) {
			for (OWLAnnotation annotation : EntitySearcher.getAnnotations(cl, rootOntology)) {
				if (annotation.getProperty().getIRI().getShortForm().equals("closedConcept") && annotation.getValue().toString().startsWith("\"true\"")) {
					closedConcepts.add(cl);
				}
			}
		}
		return closedConcepts;
	}
	
	private Set<OWLObjectProperty> getClosedRoles () {
		Set<OWLObjectProperty> closedRoles = new HashSet<OWLObjectProperty> ();
		for (OWLObjectProperty property : rootOntology.getObjectPropertiesInSignature()) {
			for (OWLAnnotation annotation : EntitySearcher.getAnnotations(property, rootOntology)) {
				//TODO define the annotation properly
				if (annotation.getProperty().getIRI().getShortForm().equals("closedRole")) {
					closedRoles.add(property);
				}
			}
		}
		return closedRoles;
	}
	
	private void clearState() {
		this.axioms = null;
	}

	private OWLDataFactory getOWLDataFactory() {
		return rootOntology.getOWLOntologyManager().getOWLDataFactory();
	}
	
	public void naiveTranslate(PrintWriter output) {
		NaiveTranslation translation = new NaiveTranslation(configuration, output);
		translation.translateOntology(axioms);
	}
	
	public void metaTranslate(PrintWriter writer) {
		NaiveMetaTranslation translation = new NaiveMetaTranslation(configuration, writer);
		translation.translateOntology(axioms);
	}

	public void naffTranslate(PrintWriter output, boolean debugFlag, OWLOntology axiomsToJustify) throws WolpertingerException {
		clearState();

		OWLAxioms axioms = new OWLAxioms();

		Collection<OWLOntology> importClosure = rootOntology.getImportsClosure();
		if(configuration.getDomainIndividuals() == null) {
			configuration.setDomainIndividuals(rootOntology.getIndividualsInSignature(Imports.INCLUDED));
		}

		OWLNormalizationWithTracer normalization = new OWLNormalizationWithTracer(rootOntology.getOWLOntologyManager().getOWLDataFactory(), axioms, 0, configuration.getDomainIndividuals());

		for (OWLOntology ontology : importClosure) {
			normalization.processOntology(ontology);
		}

		axioms.m_namedIndividuals.clear();
		axioms.m_namedIndividuals.addAll(configuration.getDomainIndividuals());
		
		DebugTranslation translation = new DebugTranslation(configuration, output, debugFlag, normalization);
		translation.translateOntology(axioms);
		
		if(axiomsToJustify != null) {
			OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
			OWLOntology processedAxiomOntology;
			try {
				processedAxiomOntology = ontologyManager.createOntology();
				for(OWLAxiom axiom : axiomsToJustify.getAxioms()) {
					if (axiom instanceof OWLDeclarationAxiom) {
						continue;
					} else if (axiom instanceof OWLSubClassOfAxiom) {
						// transform into A and ~B
						OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
						LinkedHashSet<OWLClassExpression> intersectionSet = new LinkedHashSet<OWLClassExpression> ();
						OWLClassExpression negatedSuperClass = dataFactory.getOWLObjectComplementOf(subClassOfAxiom.getSuperClass());
						intersectionSet.add(negatedSuperClass);
						intersectionSet.add(subClassOfAxiom.getSubClass());
						OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(intersectionSet);
						OWLClass thing = rootOntology.getOWLOntologyManager().getOWLDataFactory().getOWLThing();
						OWLSubClassOfAxiom convertedSubClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(thing, intersection, new HashSet<OWLAnnotation> ());
						ontologyManager.addAxiom(processedAxiomOntology, convertedSubClassOfAxiom);
					} else if (axiom instanceof OWLClassAssertionAxiom) {
						OWLClassAssertionAxiom classAssertionAxiom = (OWLClassAssertionAxiom) axiom;
						OWLIndividual individual = classAssertionAxiom.getIndividual();
						OWLClassExpression classExp = classAssertionAxiom.getClassExpression();
						OWLClassExpression negClassExp = dataFactory.getOWLObjectComplementOf(classExp);
						OWLClassAssertionAxiom negatedClassAsssertionAxiom = dataFactory.getOWLClassAssertionAxiom(negClassExp, individual);
						ontologyManager.addAxiom(processedAxiomOntology, negatedClassAsssertionAxiom);
					} else {
						throw new WolpertingerException("Complex axioms not yet supported!");
					}
				}
				OWLNormalizationWithTracer axiomNormalization = normalization.copy();
				axiomNormalization.processOntology(processedAxiomOntology);
				DebugTranslation axiomTranslation = new DebugTranslation(configuration, output, false, axiomNormalization);
				axiomTranslation.translateOntologyAxioms(axiomNormalization.getM_axioms());
			} catch (OWLOntologyCreationException e) {
				throw new WolpertingerException(e.getMessage());
			}
		}
	}

	public Collection<String> enumerateAllModels() throws WolpertingerException {
		try {
			return enumerator.enumerateModels(0);
		} catch (SolvingException se) {
			throw new WolpertingerException(se.getMessage());
		}
	}

	public Collection<String> enumerateModels(int number) throws WolpertingerException {
		Collection<String> models = null;
		
		try{
			models = enumerator.enumerateModels(number);
		} catch (SolvingException se) {
			throw new WolpertingerException(se.getMessage());
		}
		
		String filter = configuration.getFilter();
		LinkedList<String> stringList = new LinkedList<String> ();
		if (filter.equals("positive")) {
			for (String model : models) {
				StringTokenizer st = new StringTokenizer(model);
				StringBuilder strBuilder = new StringBuilder();
				while(st.hasMoreTokens()) {
					String token = st.nextToken();
					if(token.startsWith("-")) {
						continue;
					} else {
						strBuilder.append(token + " ");
					}
				}
				stringList.add(strBuilder.toString());
			}
			return stringList;
		}
		return models;
	}
	
	public Collection<Set<OWLAxiom>> enumerateAllModelsAsOWLAxioms() throws WolpertingerException {
		return enumerateModelsAsOWLAxioms(0);
	}
	
	public Collection<Set<OWLAxiom>> enumerateModelsAsOWLAxioms(int number) throws WolpertingerException {
		Collection<String> models = enumerateModels(number);
		LinkedList<Set<OWLAxiom>> modelsAsAxioms = new LinkedList<Set<OWLAxiom>>();
		
		for (String model : models) {
			modelsAsAxioms.add(naiveTranslation.retranslateSolution(model));
		}
		
		return modelsAsAxioms;
	}
	
	//invoke the sparql module here?
	public String computeSPARQLAnswer(String query_file) {
		ClingoSolver cautiousSolver = SolverFactory.INSTANCE.createClingoCautiousSolver();
		Collection<String> models = null;
		try {
			File baseMetaTmpFile=File.createTempFile("wolpertinger-triple", ".lp");
			baseMetaTmpFile.deleteOnExit();
			PrintWriter tripleWriter=new PrintWriter(new FileOutputStream(baseMetaTmpFile));
			metaTranslate(tripleWriter);
			SPARQLTranslation sparqlTranslate=new SPARQLTranslation(tripleWriter,configuration);
			sparqlTranslate.translateQuery(query_file);
			models = cautiousSolver.solve(new String[] {baseMetaTmpFile.getAbsolutePath()}, 0);
		} catch (SolvingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		String[] resultsArray = new String[models.size()];
		resultsArray = models.toArray(resultsArray);
		return resultsArray[models.size() - 1];
	}
	
	

	public String computeCautiousConsequences () {
		ClingoSolver cautiousSolver = SolverFactory.INSTANCE.createClingoCautiousSolver();
		Collection<String> consequences = null;
		try {
			consequences = cautiousSolver.solve(new String[] {baseProgramTmpFile.getAbsolutePath()}, 0);
		} catch (SolvingException e) {
			return null;
		}
		String[] resultsArray = new String[consequences.size()];
		resultsArray = consequences.toArray(resultsArray);
		return resultsArray[consequences.size() - 1];
	}
	
	/**
	 * For the ontology the wolpertinger instance was initialized with,
	 * axiomatize the fd semantics and write the axiomaized ontology to fdAxiomatizedOntologyFile.
	 * 
	 * @param fdAxiomatizedOntologyFile The file to write the axiomatized ontology to.
	 * @throws WolpertingerException 
	 */
	public void axiomatizeFDSemantics(File fdAxiomatizedOntologyFile) throws WolpertingerException {
		Set<OWLNamedIndividual> individuals = rootOntology.getIndividualsInSignature(Imports.INCLUDED);
		
 		if (individuals.isEmpty()) {
 			System.out.println("No named individuals in given ontology!");
 			return;
 		}

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLDifferentIndividualsAxiom allDiff = factory.getOWLDifferentIndividualsAxiom(individuals);
		PrefixManager pManager = new DefaultPrefixManager();
		OWLClassExpression thing = factory.getOWLClass("owl:Thing", pManager);
		OWLObjectOneOf oneof = factory.getOWLObjectOneOf(individuals);
		OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(thing, oneof);

		manager.addAxiom(rootOntology, axiom);
		manager.addAxiom(rootOntology, allDiff);

		fdAxiomatizedOntologyFile = fdAxiomatizedOntologyFile.getAbsoluteFile();
	    BufferedOutputStream outputStream;
		try {
			outputStream = new BufferedOutputStream(new FileOutputStream(fdAxiomatizedOntologyFile));
			manager.saveOntology(rootOntology, new OWLXMLDocumentFormat(), outputStream);
		} catch (FileNotFoundException e1) {
			throw new WolpertingerException("There is something wrong with the given filename: " + fdAxiomatizedOntologyFile.toString());
		} catch (OWLOntologyStorageException e1) {
			throw new WolpertingerException("Something went wrong when saving the ontology: " + e1.getMessage());
		}
	}

//	public void outputNormalizedOntology(PrintWriter writer) {
//		NiceAxiomPrinter ontoPrinter = new NiceAxiomPrinter(writer);
//		for (OWLIndividualAxiom ia : axioms.m_facts) {
//			ia.accept(ontoPrinter);
//		}
//		for (OWLClassExpression[] inclusionAxiom : axioms.m_conceptInclusions) {
//			boolean isFirst=true;
//			for (OWLClassExpression expression : inclusionAxiom) {
//				if (!isFirst)
//					writer.print(", ");
//				expression.accept(ontoPrinter);
//				isFirst=false;
//			}
//			writer.print(".\n");
//		}
//	}

	public OWLClass getOWLClass(String conceptName) {
		OWLClass owlThing = OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLThing();
		OWLClass owlNothing = OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLNothing();
		if (conceptName.equals(owlThing.toString())) {
			return owlThing;
		} else if (conceptName.equals(owlNothing.toString())) {
			return owlNothing;
		} else {
			return OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(conceptName));
		}
	}
	
	// --------------
	// OWLReasoner implementations up from here
	// --------------

	////////////////////////////
	// Reasoner-related methods
	////////////////////////////

	public void dispose() {
		System.out.println("Wolpertinger: dispose()");
	}

	public void flush() {
		System.out.println("Woolpertinger: flush");
	}

	public String getReasonerName() {
		return getClass().getPackage().getImplementationTitle();
	}

	public Version getReasonerVersion() {
		String versionString = getClass().getPackage().getImplementationVersion();
        String[] splitted;
        int version[]=new int[4];
        if (versionString!=null) {
            splitted=versionString.split("\\.");
            for (int ii = 0; ii < 4; ii++) {
            	if (ii < splitted.length) {
            		version[ii]=Integer.parseInt(splitted[ii]);
            	} else {
            		version[ii]=0;
            	}
            }
        }
        return new Version(version[0],version[1],version[2],version[3]);
	}
	
	public BufferingMode getBufferingMode() {
		return BufferingMode.NON_BUFFERING;
	}

	public OWLOntology getRootOntology() {
		return this.rootOntology;
	}

	////////////////////////////
	// Utility methods
	////////////////////////////
	public FreshEntityPolicy getFreshEntityPolicy() {
		return FreshEntityPolicy.DISALLOW;
	}

	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
		return IndividualNodeSetPolicy.BY_NAME;
	}

	public Set<OWLAxiom> getPendingAxiomAdditions() {
		return new HashSet<OWLAxiom>();
	}

	public Set<OWLAxiom> getPendingAxiomRemovals() {
		return new HashSet<OWLAxiom>();
	}

	public List<OWLOntologyChange> getPendingChanges() {
		return new LinkedList<OWLOntologyChange>();
	}

	public Set<InferenceType> getPrecomputableInferenceTypes() {
		return new HashSet<InferenceType>();
	}

	public long getTimeOut() {
		return 600000;
	}

	public void interrupt() {
		// yet, we can not interrupt the solving
	}

	////////////////////////////
	// Ontology-related methods
	////////////////////////////

	public boolean isConsistent() {
        try {
        	Collection<String> models = enumerateModels(1);
        	return !models.isEmpty();
        } catch (WolpertingerException we) {
        	throw new OWLReasonerRuntimeException(we.getMessage());
        }
		
	}

	public boolean isEntailed(OWLAxiom axiom) {
		HashSet<OWLAxiom> wrapper = new HashSet<OWLAxiom> ();
		wrapper.add(axiom);
		return isEntailed(wrapper);
	}

	public boolean isEntailed(Set<? extends OWLAxiom> axiomSet) {
		for (OWLAxiom axiom : axiomSet) {
			OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
			
			OWLOntology entailmentOntology = null;
			try {
				entailmentOntology = ontologyManager.copyOntology(rootOntology, OntologyCopy.SHALLOW);
			} catch (OWLOntologyCreationException e) {
				throw new OWLReasonerRuntimeException(e);
			}
			
			File tmpEntailmentFile = null;
			PrintWriter entailmentOutput = null;
			// write the constraints
			try {
				
				tmpEntailmentFile = File.createTempFile("wolpertinger-entailment-program", ".lp");
				tmpEntailmentFile.deleteOnExit();
				entailmentOutput = new PrintWriter(tmpEntailmentFile);
				if (axiom instanceof OWLDeclarationAxiom) {
					continue;
					// skip all declaration axioms
				} else if (axiom instanceof OWLSubClassOfAxiom) {
					// transform into A and ~B
					OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
					LinkedHashSet<OWLClassExpression> intersectionSet = new LinkedHashSet<OWLClassExpression> ();
					OWLClassExpression negatedSuperClass = dataFactory.getOWLObjectComplementOf(subClassOfAxiom.getSuperClass());
					intersectionSet.add(negatedSuperClass);
					intersectionSet.add(subClassOfAxiom.getSubClass());
					OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(intersectionSet);
					OWLClass thing = rootOntology.getOWLOntologyManager().getOWLDataFactory().getOWLThing();
					OWLSubClassOfAxiom convertedSubClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(thing, intersection, new HashSet<OWLAnnotation> ());
					ontologyManager.addAxiom(entailmentOntology, convertedSubClassOfAxiom);
				} else if (axiom instanceof OWLClassAssertionAxiom) {
					OWLAxioms tempAxioms = new OWLAxioms ();
					Collection<OWLAxiom> wrapper = new HashSet<OWLAxiom> ();
					OWLNormalization tempNormalization = new OWLNormalization(rootOntology.getOWLOntologyManager().getOWLDataFactory(), tempAxioms, 0, configuration.getDomainIndividuals());	
					OWLClassAssertionAxiom classAssertionAxiom = (OWLClassAssertionAxiom) axiom;
					OWLClassExpression classExpression = classAssertionAxiom.getClassExpression();
					OWLClass thing = rootOntology.getOWLOntologyManager().getOWLDataFactory().getOWLThing();
					OWLSubClassOfAxiom subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(thing, classExpression, new HashSet<OWLAnnotation> ());
					wrapper.add(subClassOfAxiom);
					tempNormalization.processAxioms(wrapper);
					NaiveTranslation axiomTranslation = new NaiveTranslation(configuration, entailmentOutput);
					axiomTranslation.individualAssertionMode((OWLNamedIndividual) classAssertionAxiom.getIndividual());
					axiomTranslation.translateEntailment(tempAxioms);
				} else {
					throw new OWLReasonerRuntimeException("Complex axioms are not yet suppoerted.");
				}
				
				Wolpertinger entailmentWolpertinger = new Wolpertinger(entailmentOntology);
				if (entailmentWolpertinger.isConsistent()) {
					return false;
				}
			} catch (IOException e) {
				throw new OWLReasonerRuntimeException(e);
			} catch (WolpertingerException we) {
				throw new OWLReasonerRuntimeException(we);
			} finally {
				if (entailmentOutput != null) entailmentOutput.close();
			}
		}
		return true;
	}

	public boolean isEntailmentCheckingSupported(AxiomType<?> arg0) {
		return arg0 == AxiomType.SUBCLASS_OF ? true : false;
	}
	
	public boolean isPrecomputed(InferenceType arg0) {
		return false;
	}

	public void precomputeInferences(InferenceType... arg0) {
	}

	////////////////////////////
	// Class-related methods
	////////////////////////////
	
	public Node<OWLClass> getBottomClassNode() {
		classifyClasses();
		return owlClassHierarchyNodeToNode(classHierarchy.getBottomNode());
	}

	public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression arg0) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("getDisjointClasses is not supported yet.");
	}

	public Node<OWLClass> getEquivalentClasses(OWLClassExpression queryClassExpression) {
		classifyClasses();
		if (queryClassExpression.isOWLNothing()) {
            return getBottomClassNode();
        } else if (queryClassExpression.isOWLThing()) {
        	return getTopClassNode();
        } else if (queryClassExpression instanceof OWLClass ){
        	OWLClass queryClass = (OWLClass) queryClassExpression;
        	return owlClassHierarchyNodeToNode(classHierarchy.getNodeForElement(queryClass));
        }
		// not supported yet
		return new OWLClassNode();
	}

	public NodeSet<OWLClass> getSubClasses(OWLClassExpression queryClassExpression, boolean direct) {
		if (queryClassExpression instanceof OWLClass) {
			classifyClasses();
			Set<HierarchyNode<OWLClass>> result;
			OWLClass queryClass = (OWLClass) queryClassExpression;
			HierarchyNode<OWLClass> hierarchyNode = classHierarchy.getNodeForElement(queryClass);
			if (direct) {
				result = hierarchyNode.getChildNodes();
			} else {
				result=new HashSet<HierarchyNode<OWLClass>>(hierarchyNode.getDescendantNodes());
	            result.remove(hierarchyNode);
	        }
			return owlClassHierarchyNodesToNodeSet(result);
		}
		return null;
	}

	public NodeSet<OWLClass> getSuperClasses(OWLClassExpression queryClassExpression, boolean direct) {
		if (queryClassExpression instanceof OWLClass) {
			classifyClasses();
			Set<HierarchyNode<OWLClass>> result;
			OWLClass queryClass = (OWLClass) queryClassExpression;
			HierarchyNode<OWLClass> hierarchyNode = classHierarchy.getNodeForElement(queryClass);
			if (direct) {
				result = hierarchyNode.getParentNodes();
			} else {
				result=new HashSet<HierarchyNode<OWLClass>>(hierarchyNode.getAncestorNodes());
	            result.remove(hierarchyNode);
	        }
			return owlClassHierarchyNodesToNodeSet(result);
		}
		return null;
	}

	// taken from HermiT. instead of using AtomicConcept, we directly use OWLClass
	protected NodeSet<OWLClass> owlClassHierarchyNodesToNodeSet(Collection<HierarchyNode<OWLClass>> hierarchyNodes) {
        Set<Node<OWLClass>> result=new HashSet<Node<OWLClass>>();
        for (HierarchyNode<OWLClass> hierarchyNode : hierarchyNodes) {
            Node<OWLClass> node=owlClassHierarchyNodeToNode(hierarchyNode);
            if (node.getSize()!=0)
                result.add(node);
        }
        return new OWLClassNodeSet(result);
    }

	protected Node<OWLClass> owlClassHierarchyNodeToNode(HierarchyNode<OWLClass> hierarchyNode) {
        Set<OWLClass> result=new HashSet<OWLClass>();
        for (OWLClass concept : hierarchyNode.getEquivalentElements()) {
            result.add(concept);
        }
        return new OWLClassNode(result);
    }

	public Node<OWLClass> getTopClassNode() {
		classifyClasses();
		return owlClassHierarchyNodeToNode(classHierarchy.getTopNode());
	}

	public Node<OWLClass> getUnsatisfiableClasses() {
		return getBottomClassNode();
	}

	public boolean isSatisfiable(OWLClassExpression classExpression) {
		if(!satisfiableClassesComputed) {
			ClingoSolver solver = SolverFactory.INSTANCE.createClingoBraveSolver();
			try {
				File satisfiableClassFile = File.createTempFile("wolpertinger-satisfiable-program", ".lp");
				satisfiableClassFile.deleteOnExit();
				PrintWriter satisfiableOutput = new PrintWriter(satisfiableClassFile);

				for (OWLClass c : rootOntology.getClassesInSignature(Imports.INCLUDED)) {
					String className = mapper.getPredicateName(c);
					satisfiableOutput.write(String.format("%s :- %s(X).", className.toLowerCase(), className.toLowerCase()));
					satisfiableOutput.println();
					satisfiableOutput.write(String.format("not_%s :- -%s(X).", className.toLowerCase(), className.toLowerCase()));
					satisfiableOutput.println();
					satisfiableOutput.write("#show not_" + className.toLowerCase() + "/0.");
					satisfiableOutput.write("#show " + className.toLowerCase() + "/0.");
				}
				satisfiableOutput.close();
				Collection<String> results = solver.solve(new String[] {baseProgramTmpFile.getAbsolutePath(), satisfiableClassFile.getAbsolutePath()}, 0);
				String[] resultsArray = new String[results.size()];
				resultsArray = results.toArray(resultsArray);
				satisfiableClasses = new ArrayList<String>(Arrays.asList(resultsArray[results.size() - 1].split(" ")));
				satisfiableClassesComputed = true;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SolvingException e) {
				e.printStackTrace();
			}
		}
		if (classExpression instanceof OWLClass) {
			String className = mapper.getPredicateName((OWLClass) classExpression);
			if (satisfiableClasses.contains(className)) {
				return true;
			}
		} else if (classExpression instanceof OWLObjectComplementOf &&
				   ((OWLObjectComplementOf) classExpression).getOperand() instanceof OWLClass) {
			OWLClassExpression cl = ((OWLObjectComplementOf) classExpression).getOperand();
			String className = mapper.getPredicateName((OWLClass) cl);
			if (satisfiableClasses.contains("not_" + className)) {
				return true;
			}
		}
		return false;
	}

	////////////////////////////
	// Object Property-related methods
	////////////////////////////

	public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
		return new OWLObjectPropertyNode();
	}

	public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
			OWLObjectPropertyExpression arg0) {
		return new OWLObjectPropertyNodeSet();
	}

	public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
			OWLObjectPropertyExpression arg0) {
		return new OWLObjectPropertyNode();
	}

	public Node<OWLObjectPropertyExpression> getInverseObjectProperties(
			OWLObjectPropertyExpression arg0) {
		return new OWLObjectPropertyNode();
	}

	public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(
			OWLObjectPropertyExpression arg0, boolean arg1) {
		return new OWLObjectPropertyNodeSet();
	}

	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
			OWLNamedIndividual arg0, OWLObjectPropertyExpression arg1) {
		return new OWLNamedIndividualNodeSet();
	}

	public NodeSet<OWLClass> getObjectPropertyDomains(
			OWLObjectPropertyExpression arg0, boolean arg1) {
		return new OWLClassNodeSet();
	}

	public NodeSet<OWLClass> getObjectPropertyRanges(
			OWLObjectPropertyExpression arg0, boolean arg1) {
		return new OWLClassNodeSet();
	}

	public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
			OWLObjectPropertyExpression arg0, boolean arg1) {
		return new OWLObjectPropertyNodeSet();
	}

	public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
		return new OWLObjectPropertyNode();
	}

	////////////////////////////
	// Data Property-related methods
	////////////////////////////

	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		return null;
	}

	public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty arg0,
			boolean arg1) {
		return null;
	}

	public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual arg0,
			OWLDataProperty arg1) {
		return null;
	}

	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
			OWLNamedIndividual arg0) {
		return null;
	}

	public NodeSet<OWLDataProperty> getDisjointDataProperties(
			OWLDataPropertyExpression arg0) {
		return null;
	}

	public Node<OWLDataProperty> getEquivalentDataProperties(
			OWLDataProperty arg0) {
		return null;
	}

	public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty arg0,
			boolean arg1) {
		return null;
	}

	public NodeSet<OWLDataProperty> getSuperDataProperties(
			OWLDataProperty arg0, boolean arg1) {
		return null;
	}

	public Node<OWLDataProperty> getTopDataPropertyNode() {
		return null;
	}

	////////////////////////////
	// Individual-related methods
	////////////////////////////

	public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression classExpression, boolean arg1) {
		//TODO Indirect and Arbitrary Class Expr
		Set<OWLNamedIndividual> individuals = rootOntology.getIndividualsInSignature(Imports.INCLUDED);
		OWLNamedIndividualNodeSet result = new OWLNamedIndividualNodeSet ();
		for (OWLNamedIndividual individual : individuals) {
			OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(classExpression, individual, new HashSet<OWLAnnotation> ());
			if(isEntailed(classAssertion)) {
				result.addEntity(individual);
			}
		}
		return result;
	}

	public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual arg0) {
		return null;
	}

	public NodeSet<OWLClass> getTypes(OWLNamedIndividual individual, boolean arg1) {
		//TODO Indirect
		Set<OWLClass> classes = rootOntology.getClassesInSignature();
		OWLClassNodeSet result = new OWLClassNodeSet ();
		for (OWLClass cl : classes) {
			OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom (cl, individual, new HashSet<OWLAnnotation> ());
			if(isEntailed(classAssertion)) {
				result.addEntity(cl);
			}
		}
		return result;
	}
	
	///////////////////////////////
	// Private utility methods
	///////////////////////////////
	
	
	private void classifyClasses() {
		if (classified) {
			return;
		}
		
		equalsToTopClasses = new HashSet<OWLClass> ();
		equalsToBottomClasses = new HashSet<OWLClass> ();
		Collection<OWLClass> allClasses = rootOntology.getClassesInSignature(Imports.INCLUDED);
		HashMap<OWLClass,HashSet<OWLClass>> superClassHierarchy = new HashMap<OWLClass,HashSet<OWLClass>> ();

		for (OWLClass cl : allClasses) {
			OWLObjectComplementOf negated = dataFactory.getOWLObjectComplementOf(cl);
			if (!isSatisfiable(cl)) {
				equalsToBottomClasses.add(cl);
			}
			if (!isSatisfiable(negated)) {
				equalsToTopClasses.add(cl);
			} else {

			}
		}

		HashSet<OWLClass> processedClasses = new HashSet<OWLClass> ();
		for (OWLClass cl : allClasses) {
			if (equalsToTopClasses.contains(cl) || equalsToBottomClasses.contains(cl) ||
				cl.isTopEntity() || cl.isBottomEntity()) {
				continue;
			}
			processedClasses.add(cl);
			superClassHierarchy.put(cl, new HashSet<OWLClass> ());
		}
	
		for (OWLClassExpression[] inclusion : axioms.m_conceptInclusions) {
			// check for defined simple subsumptions
			if (inclusion.length == 2 && 
				inclusion[0] instanceof OWLClass && inclusion[1] instanceof OWLObjectComplementOf) {
				OWLObjectComplementOf complementOf = (OWLObjectComplementOf) inclusion[1];
				if (complementOf.getOperand() instanceof OWLClass) {					
					OWLClass superClass = (OWLClass) inclusion[0];
					OWLClass subClass = (OWLClass) complementOf.getOperand();
					// check for auxiliary and non-processed concepts
					if (!processedClasses.contains(superClass) || !processedClasses.contains(subClass)) {
						continue;
					}
					superClassHierarchy.get(subClass).add(superClass);
				}
			}			
		}

		
		ClingoSolver solver = SolverFactory.INSTANCE.createClingoBraveSolver();
		try {
			File classifyClassFile = createClassifyingFile(processedClasses, superClassHierarchy);
			Collection<String> results = solver.solve(new String[] {baseProgramTmpFile.getAbsolutePath(), classifyClassFile.getAbsolutePath()}, 0);
			 
			String[] resultsArray = new String[results.size()];
			resultsArray = results.toArray(resultsArray);
			ArrayList<String> notSubclasses = new ArrayList<String>(Arrays.asList(resultsArray[results.size() - 1].split(" ")));
			HashMap<String, HashSet<String>> tmpNotSubclasses = new HashMap<String, HashSet<String>> ();

			for (String str : notSubclasses) {
				// hotfix, has to be optimized
				if(str.equals("") || !str.startsWith("not_subClass")) {
					// no subsumption holds
					continue;
				}
				str = str.substring(13,str.length() - 1);
				String[] split = str.split(",");
				if(!tmpNotSubclasses.containsKey(split[0])) {
					tmpNotSubclasses.put(split[0], new HashSet<String> ());
				}
				tmpNotSubclasses.get(split[0]).add(split[1]);
			}
			
			for (OWLClass subClassCandidate : processedClasses) {
				HashSet<OWLClass> superClasses = superClassHierarchy.get(subClassCandidate);
				for (OWLClass superClassCandidate : processedClasses) {
					if (subClassCandidate.equals(superClassCandidate)) {
						// same class, no need to check
						continue;
					}
					
					String subClassName = mapper.getPredicateName(subClassCandidate);
					String superClassName = mapper.getPredicateName(superClassCandidate);
					if(tmpNotSubclasses.get(subClassName) != null &&
					   !tmpNotSubclasses.get(subClassName).contains(superClassName)) {
						superClasses.add(superClassCandidate);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SolvingException e) {
			e.printStackTrace();
		}		

		// check equivalent classes
		HashMap<OWLClass, OWLClass> classRepresentative = new HashMap<OWLClass, OWLClass> ();
		HashMap<OWLClass, HashSet<OWLClass>> classRepresented = new HashMap<OWLClass, HashSet<OWLClass>> ();
		for (OWLClass cl : processedClasses) {
			HashSet<OWLClass> superClasses = superClassHierarchy.get(cl);
			for (OWLClass equivalentCandidate : superClasses) {
				if (classRepresentative.keySet().contains(cl)) {
					continue;
				}
				if (superClassHierarchy.get(equivalentCandidate).contains(cl)) {
					classRepresentative.put(equivalentCandidate, cl);
					if(!classRepresented.containsKey(cl)) {
						HashSet<OWLClass> newSet = new HashSet<OWLClass> ();
						newSet.add(equivalentCandidate);
						classRepresented.put(cl, newSet);
					} else {
						HashSet<OWLClass> existedSet = classRepresented.get(cl);
						existedSet.add(equivalentCandidate);
					}

				}
			}
		}

		Set<OWLClass> representedClasses = classRepresentative.keySet();
		for (OWLClass cl : processedClasses) {
			HashSet<OWLClass> superClasses = superClassHierarchy.get(cl);
			superClasses.removeAll(representedClasses);
		}

		for (OWLClass cl : representedClasses) {
			superClassHierarchy.remove(cl);
		}

		computeTransitiveReduction(superClassHierarchy);
		OWLClass thing = getOWLDataFactory().getOWLThing();
		OWLClass nothing = getOWLDataFactory().getOWLNothing();
		
		classHierarchy = HierarchyBuilder.buildHierarchy(superClassHierarchy, classRepresentative, thing, nothing, equalsToTopClasses, equalsToBottomClasses);
		classified = true;
	}
	
	private File createClassifyingFile (HashSet<OWLClass> processedClasses, HashMap<OWLClass,HashSet<OWLClass>> superClassHierarchy) throws IOException {
		File classifyClassFile = File.createTempFile("wolpertinger-classify-program", ".lp");
		classifyClassFile.deleteOnExit();
		PrintWriter classifyOutput = new PrintWriter(classifyClassFile);

		for (OWLClass subClassCandidate : processedClasses) {
			HashSet<OWLClass> superClasses = superClassHierarchy.get(subClassCandidate);
			for (OWLClass superClassCandidate : processedClasses) {					
				if (superClasses.contains(superClassCandidate)) {
					// listed in the axioms
					continue;
				}
				if (subClassCandidate.equals(superClassCandidate)) {
					// same class, no need to check
					continue;
				}
				String subClassName = mapper.getPredicateName(subClassCandidate);
				String superClassName = mapper.getPredicateName(superClassCandidate);
				classifyOutput.write(String.format("not_subClass(%1$s, %2$s) :- %1$s(X), -%2$s(X).", subClassName.toLowerCase(), superClassName.toLowerCase()));					
				classifyOutput.println();
			}
		}
		classifyOutput.write("#show not_subClass/2.");				
		classifyOutput.close();
		return classifyClassFile;
	}
	
	private void computeTransitiveReduction (HashMap<OWLClass,HashSet<OWLClass>> superClassHierarchy) {
		// compute transitive reduction of dag
		Collection<OWLClass> allClasses = rootOntology.getClassesInSignature(Imports.INCLUDED);
		for (OWLClass cl : allClasses) {
			Collection<OWLClass> superClasses = superClassHierarchy.get(cl);
			if(superClasses == null) {
				continue;
			}
			OWLClass[] tmpSuperClasses = new OWLClass[superClasses.size()];

			for (OWLClass superClass : superClasses.toArray(tmpSuperClasses)) {
				if (!superClassHierarchy.get(cl).contains(superClass)){
					// has been removed
				}
				HashSet<OWLClass> marked = new HashSet<OWLClass> ();

		        // depth-first search using an explicit stack
		        Stack<OWLClass> stack = new Stack<OWLClass>();
		        marked.add(superClass);
		        stack.push(superClass);
		        while (!stack.isEmpty()) {
		            OWLClass v = stack.peek();
		            HashSet<OWLClass> superSuperClasses = superClassHierarchy.get(v);
		            for (OWLClass superSuperClass : superSuperClasses) {
		            	if (!marked.contains(superSuperClass)) {
		            		marked.add(superSuperClass);
		            		stack.add(superSuperClass);
		            		continue;
		            	}

		            }
            		stack.pop();
		        }
		        HashSet<OWLClass> directSuperClasses = superClassHierarchy.get(cl);
		        for (OWLClass indirectSuperClass : marked) {
		        	if (indirectSuperClass.equals(superClass)) {

		        	} else {
		        		directSuperClasses.remove(indirectSuperClass);
		        	}
		        }
			}
		}
	}
}
