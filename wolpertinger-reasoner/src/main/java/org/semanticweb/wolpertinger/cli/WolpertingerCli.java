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
package org.semanticweb.wolpertinger.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.text.BreakIterator;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.wolpertinger.Configuration;
import org.semanticweb.wolpertinger.Wolpertinger;
import org.semanticweb.wolpertinger.WolpertingerException;
import org.semanticweb.wolpertinger.translation.TranslationException;
import org.semanticweb.wolpertinger.translation.debug.DebugTranslation;
import org.semanticweb.wolpertinger.translation.meta.naive.NaiveMetaTranslation;
import org.semanticweb.wolpertinger.translation.naive.NaiveTranslation;
import org.semanticweb.wolpertinger.debugLogger;
import org.semanticweb.wolpertinger.sparql.SPARQLTranslation;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
 * Command Line Interface for Wolpertinger.
 *
 * @author Lukas Schweizer
 * @author Satyadharma Tirtarasa
 */
public class WolpertingerCli {

    protected static class StatusOutput {
        protected int level;
        public StatusOutput(int inLevel) {
            level=inLevel;
        }
        static public final int ALWAYS=0;
        static public final int STATUS=1;
        static public final int DETAIL=2;
        static public final int DEBUG=3;
        public void log(int inLevel,String message) {
            if (inLevel<=level)
                System.err.println(message);
        }
    }

    protected static final String versionString;
    static {
        String version=WolpertingerCli.class.getPackage().getImplementationVersion();
        if (version == null) {
        	version = "<no version set>";
        } else {
        	version = version + ".0.0";
        }
        versionString=version;
    }

    protected interface TranslationAction {
    	void run(OWLOntology rootOntology, Configuration configuration, StatusOutput status, PrintWriter output) throws TranslationException;
    	
    }
    
    protected interface ReasoningAction {
    	void run(Wolpertinger wolpertinger, Configuration configuration, StatusOutput status, PrintWriter output) throws WolpertingerException;
    
    }
    
    protected interface QueryAction {
    	void run(Wolpertinger wolpertinger, Configuration configuration, StatusOutput status, PrintWriter output) throws WolpertingerException;
    
    }
    
    
    //Execute the sparql query
    static protected class SPARQLAnswerAction implements QueryAction {
    	
    	String query_file;
    	
    	public SPARQLAnswerAction(String file) {
    		query_file=file;
    	}
    	
		public void run(Wolpertinger wolpertinger, Configuration configuration, StatusOutput status,
				PrintWriter output) {
			String answer=wolpertinger.computeSPARQLAnswer(query_file);
			output.println(answer);
			output.flush();
		}
    	
    }
    
    //Show the translated query
    static protected class SPARQLShowTranslation implements QueryAction {
    	String query_file;
    	
    	public SPARQLShowTranslation(String file) {
    		query_file=file;
    	}
    	
    	public void run(Wolpertinger wolpertinger, Configuration configuration, StatusOutput status,
				PrintWriter output) {
    			SPARQLTranslation sparqlTranslate=new SPARQLTranslation(output,configuration);
    			sparqlTranslate.translateQuery(query_file);
    	}
    }

    static protected class DebugTranslationAction implements TranslationAction {
    	boolean debugFlag;

    	public DebugTranslationAction(boolean debugFlag) {
    		super();
    		this.debugFlag = debugFlag;
    	}

		public void run(OWLOntology ontology, Configuration configuration,
				StatusOutput status, PrintWriter output)
				throws TranslationException {

			DebugTranslation debugTranslation = new DebugTranslation(configuration, output, debugFlag, null);
			debugTranslation.translateOntology(ontology);
		}
    }

    static protected class NaiveTranslationAction implements TranslationAction {
		public void run(OWLOntology rootOntology, Configuration configuration,
				StatusOutput status, PrintWriter output)
				throws TranslationException {
			NaiveTranslation translation = new NaiveTranslation(configuration, output);
			translation.translateOntology(rootOntology);
		}
    }
    
    static protected class NaiveMetaTranslationAction implements TranslationAction {
    	public void run(OWLOntology ontology, Configuration configuration,
    			StatusOutput status, PrintWriter output) throws TranslationException {
    		NaiveMetaTranslation metaTrans = new NaiveMetaTranslation(configuration, output);
    		metaTrans.translateOntology(ontology);
    	}
    }

    static protected class DirectTranslationAction implements TranslationAction {
		public void run(OWLOntology ontology, Configuration configuration, StatusOutput status, PrintWriter output) throws TranslationException {
			
		}
    }

    static protected class EntailmentCheckingAction implements ReasoningAction {
    	private OWLOntology owlOntology;

    	public EntailmentCheckingAction(OWLOntology owlOntology) {
    		super();
    		this.owlOntology = owlOntology;
    	}

		public void run(Wolpertinger wolpertinger, Configuration configuration, StatusOutput status, PrintWriter output) {
			Set<OWLAxiom> axioms = owlOntology.getAxioms();
			System.out.println("Is entailed? : " + wolpertinger.isEntailed(axioms));
		}
    }

    /**
     * This action uses the {@link DebugTranslation} and passes an additional
     * set of axioms, for which justifications shall be computed.
     * 
     * @see DebugTranslation
     * 
     * @author Lukas Schweizer
     */
	static protected class JustificationAction implements TranslationAction {
		private OWLOntology axiomOntology;

		public JustificationAction(OWLOntology axiomOntology) {
			this.axiomOntology = axiomOntology;
		}

		public void run(OWLOntology ontology, Configuration configuration, StatusOutput status, PrintWriter output) throws TranslationException {
			DebugTranslation debugTranslation = new DebugTranslation(configuration, output, true, null);
			debugTranslation.translateOntology(ontology, axiomOntology);
		}
	}

	static protected class ConsistencyAction implements ReasoningAction {
		public ConsistencyAction() {
			super();
		}

		public void run(Wolpertinger wolpertinger, Configuration configuration,
				StatusOutput status, PrintWriter output)
				throws WolpertingerException {
			if (wolpertinger.isConsistent()) {
				output.println("Input ontologies are consistent");
			} else {
				output.println("Input ontologies are inconsistent");
			}
			output.flush();
		}
	}

    static protected class ModelEnumerationAction implements ReasoningAction {
    	int number = 0;

    	public ModelEnumerationAction(int number) {
    		super();
    		this.number = number;
    	}

		public void run(Wolpertinger wolpertinger, Configuration configuration, StatusOutput status, PrintWriter output) throws WolpertingerException {
			if (!configuration.getAboxDirectory().isEmpty()) {
				//String modelFilePattern = "model%d.owl";
				String targetDir = configuration.getAboxDirectory();
				
				OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

				Collection<Set<OWLAxiom>> aboxes = wolpertinger.enumerateModelsAsOWLAxioms(this.number);
				
				if (number == 0) {
					output.printf("Found " + aboxes.size() + " models (requested ALL): \n");
				} else {
					output.printf("Found " + aboxes.size() + " models (requested %d): \n", number);
				}
				
				int n = 1;
				for (Set<OWLAxiom> abox : aboxes) {
					if (targetDir.endsWith(File.separator))
						targetDir = targetDir.substring(0, targetDir.length()-1);
					
					IRI fileIri = IRI.create(new File(String.format("%s%s%s%d.%s", targetDir, File.separator, "model", n, "owl")).toURI());
				
					// write to file
					try {
						//OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(String.format(configuration.getAboxDirectory() + modelFilePattern, n))));
						OWLOntology owlABox = manager.createOntology(abox);
						manager.setOntologyDocumentIRI(owlABox, fileIri);
						manager.saveOntology(owlABox);
						output.println(String.format("Wrote ABox %d: %s", n, fileIri.toQuotedString()));
					} catch (OWLOntologyCreationException e) {
						System.err.println("Could not create ABox Ontology!");
					} catch (OWLOntologyStorageException e) {
						e.printStackTrace();
					} 
					n += 1;
				}
			}
			else {
				Collection<String> models = wolpertinger.enumerateModels(number);
				
				if (number == 0) {
					output.printf("Found " + models.size() + " models (requested ALL): \n");
				} else {
					output.printf("Found " + models.size() + " models (requested %d): \n", number);
				}
				for (String model : models) {
					output.println(model);
				}
			}
			output.flush();
		}
    }

	static protected class CautiousConsequencesAction implements
			ReasoningAction {
		public CautiousConsequencesAction() {
			super();
		}

		public void run(Wolpertinger wolpertinger, Configuration configuration,
				StatusOutput status, PrintWriter output)
				throws TranslationException {
			String cautiousModel = wolpertinger.computeCautiousConsequences();
			output.println(cautiousModel);
			output.flush();
		}
	}
    
    static protected class AxiomatizationAction implements TranslationAction {
    	File axiomatizedOntologyFile;
    	public AxiomatizationAction(File axiomatizedOntology) {
    		this.axiomatizedOntologyFile = axiomatizedOntology;
    	}

		public void run(OWLOntology rootOntology, Configuration configuration, StatusOutput status, PrintWriter output) throws TranslationException {
			
		}
	}

    static protected class SubconceptsActions implements ReasoningAction {
    	final String conceptName;
    	final boolean direct;

    	public SubconceptsActions(String conceptName, boolean direct) {
    		this.conceptName = conceptName;
    		this.direct = direct;
    	}

		public void run(Wolpertinger wolpertinger, Configuration configuration, StatusOutput status, PrintWriter output) throws WolpertingerException {
			OWLClass owlClass = wolpertinger.getOWLClass(conceptName);
			NodeSet<OWLClass> subconcepts = wolpertinger.getSubClasses(owlClass, direct);
			for (OWLClass cl : subconcepts.getFlattened()) {
				output.println(cl);
			}
			output.flush();
		}
	}

	static protected class SuperconceptsActions implements ReasoningAction {
		final String conceptName;
		final boolean direct;

		public SuperconceptsActions(String conceptName, boolean direct) {
			this.conceptName = conceptName;
			this.direct = direct;
		}

		public void run(Wolpertinger wolpertinger, Configuration configuration,
				StatusOutput status, PrintWriter output)
				throws WolpertingerException {
			OWLClass owlClass = wolpertinger.getOWLClass(conceptName);
			NodeSet<OWLClass> subconcepts = wolpertinger.getSuperClasses(
					owlClass, direct);
			for (OWLClass cl : subconcepts.getFlattened()) {
				output.println(cl);
			}
			output.flush();
		}
	}

	static protected class EquivalentConceptsActions implements ReasoningAction {
		final String conceptName;
		final boolean direct;

		public EquivalentConceptsActions(String conceptName, boolean direct) {
			this.conceptName = conceptName;
			this.direct = direct;
		}

		public void run(Wolpertinger wolpertinger, Configuration configuration,
				StatusOutput status, PrintWriter output)
				throws WolpertingerException {
			OWLClass owlClass = wolpertinger.getOWLClass(conceptName);
			Node<OWLClass> equivalentConcepts = wolpertinger
					.getEquivalentClasses(owlClass);
			for (OWLClass cl : equivalentConcepts.getEntities()) {
				output.println(cl);
			}
			output.flush();
		}
	}
    
    static protected class ConceptInstancesAction implements ReasoningAction {
    	final String conceptName;
    	final boolean direct;

    	public ConceptInstancesAction(String conceptName, boolean direct) {
    		super();
    		this.conceptName = conceptName;
    		this.direct = direct;
    	}

		public void run(Wolpertinger wolpertinger, Configuration configuration,
				StatusOutput status, PrintWriter output)
				throws WolpertingerException {
			OWLClass owlClass = wolpertinger.getOWLClass(conceptName);
			NodeSet<OWLNamedIndividual> instances = wolpertinger.getInstances(
					owlClass, direct);
			for (OWLNamedIndividual ind : instances.getFlattened()) {
				output.println(ind);
			}
			output.flush();
		}
	}
    
    static protected class IndividualTypesAction implements ReasoningAction {
    	final String individualName;
    	final boolean direct;

    	public IndividualTypesAction(String individualName, boolean direct) {
    		super();
    		this.individualName = individualName;
    		this.direct = direct;
    	}

		public void run(Wolpertinger wolpertinger, Configuration configuration,
				StatusOutput status, PrintWriter output)
				throws WolpertingerException {
			OWLNamedIndividual owlIndividual = OWLManager
					.createOWLOntologyManager().getOWLDataFactory()
					.getOWLNamedIndividual(IRI.create(individualName));
			NodeSet<OWLClass> types = wolpertinger.getTypes(owlIndividual,
					direct);
			for (OWLClass cl : types.getFlattened()) {
				output.println(cl);
			}
			output.flush();
		}
	}
    
	@SuppressWarnings("serial")
	protected static class UsageException extends IllegalArgumentException {
		public UsageException(String inMessage) {
			super(inMessage);
		}
	}

	protected static final String usageString = "java -jar wolpertinger.jar [OPTIONS]... IRI...";

	protected static final String 	groupActions = "Actions",
									groupMisc = "Miscellaneous",
									groupDebug = "Debugging",
									groupOptimize = "Optimization",
									groupUtility = "Utility Functions";

	protected static final Option[] options = new Option[] {
			// misc options
			new Option('h', "help", groupMisc, "display this help and exit"),
			new Option('V', "version", groupMisc, "display Wolpertinger's built version and exit"),

			// optimization options
			new Option('p', "project", groupOptimize,true, "IRI1,..,IRI2", "project on concept "),
			// debug options
			new Option('v', "verbose", groupDebug, true, "AMOUNT", "increase verbosity by AMOUNT levels (default 1)"),
			// actions
			//new Option('N', "normalize", groupActions, "normalize the input ontology (structural transformation), optionally writing it back to file (via --output)"),
			new Option('T', "translate", groupActions, true, "TARGET", "translate the ontology to TARGET language, optionally writing it back to file (via --output); supported values are 'naive', 'naff', and 'meta'"),
			new Option('O', "output", groupActions, true, "FILE", "output non-debug informations to FILE"),
			new Option('e', "entail", groupActions, true, "FILE", "check whether ontology FILE is entailed by input ontology"),
			new Option('D', "domain", groupActions, true, "FILE", "get fixed domain from FILE. if this option is not provided, the domain is the implicit set of individuals in the input ontology"),
			new Option('d', "direct", groupActions, "apply direct sub/superclasses for next query"),
			new Option('f', "filter", groupActions, true, "MODE", "filter what predicates are to be shown in the model; supported values are 'positive' and 'negative'"),
			new Option('m', "model", groupActions, true, "NUMBER", "enumerate NUMBER many models; NUMBER=0 means asking for ALL models"),
			new Option('A', "abox", groupActions, true, "DIRECTORY", "write models as proper assertions in TTL syntax to DIRECTORY"),
			new Option('C', "cautious", groupActions, "write the cautious model of the ontology"),
			new Option('c', "consistent", groupActions, "ask whether input ontology(-ies) is consistent"),
			new Option('j', "justification", groupActions, false, "FILE", "compute justifications for the axioms in FILE. if no argument provided, justifications are computed for inconsistency"),
			new Option('s', "subs", groupActions, true, "CLASS", "output classes subsumed by CLASS"),
			new Option('S', "supers", groupActions, true, "CLASS", "output classes subsuming by CLASS"),
			new Option('E', "equi", groupActions, true, "CLASS", "output classes equivalent to CLASS"),
			new Option('i', "instances", groupActions, true, "CLASS", "output instances of the CLASS"),
			new Option('t', "types", groupActions, true, "INDIVIDUAL", "output types of the INDIVIDUAL"),
			//sparql-related options
			new Option('Q',"query",groupActions,true,"FILE","a sparql query for the Ontology"),
			new Option('q',"q_translate",groupActions,true,"FILE","test the query function"),
			
			new Option('a', "axiomatize", groupUtility, true, "FILE", "For the ontology given, generate axioms that axiomatize the fixed-domain semantics and write the axiomatized ontolgy to FILE.")
			
	};


	//-----------log4j logger--------------------
	//static Logger logger=Logger.getLogger(WolpertingerCli.class);
	//-------------------------------------------
	static Logger logger=debugLogger.logger;
	
	public static void main(String[] args) {
		try {
			//initialize log4j settings
			PropertyConfigurator.configure("log4j.properties");
			
			Configuration configuration = new Configuration();
			Getopt getopt = new Getopt("", args, Option.formatOptionsString(options), Option.createLongOpts(options));
			
			// by default print to System.out
			PrintWriter output = new PrintWriter(System.out);
			boolean direct = false;

//			URI base;
//			try {
//				base = new URI("file", System.getProperty("user.dir") + "/", null);
//			} catch (java.net.URISyntaxException e) {
//				throw new RuntimeException("unable to create default IRI base");
//			}

			URI base=(new File(System.getProperty("user.dir"))).toURI();
			
			Collection<IRI> ontologies = new LinkedList<IRI>();
			Collection<ReasoningAction> reasoningActions = new LinkedList<ReasoningAction>();
			Collection<TranslationAction> transActions = new LinkedList<WolpertingerCli.TranslationAction>();
			Collection<QueryAction> queryActions = new LinkedList<WolpertingerCli.QueryAction>();
			
			OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();

			int option;
			int verbosity=1;
			//int debug = 1;

			while ((option = getopt.getopt()) != -1) {
				switch (option) {
				// misc
				case 'h': {
					System.out.println(usageString);
					System.out.println();
					System.out.println("For example, to get the naive translation of example.owl:");
					System.out.println("java -jar Wolpertinger.jar --translate=naive example.owl");
					System.out.println(Option.formatOptionHelp(options));
					System.exit(0);
				}
					break;

				case 'v': {
					String arg = getopt.getOptarg();
					if (arg == null) {
						verbosity += 1;
					} else
						try {
							verbosity += Integer.parseInt(arg, 10);
						} catch (NumberFormatException e) {
							throw new UsageException("argument to --verbose must be a number");
						}
				}
					break;
				case 'V': {
					System.out.println("Wolpertinger Version : " + versionString);
				}
					break;
				case 'p': {
					String arg = getopt.getOptarg();
					HashSet<IRI> iris = new HashSet<IRI>();
					for (String sIRI : arg.split(",")) {
						iris.add(IRI.create(sIRI));
					}
					configuration.setConceptsToProjectOn(iris);
				}
					break;
				case 'f': {
					String arg = getopt.getOptarg();
					configuration.setFilter(arg);
				}
					break;
				case 'd': {
					direct = true;
				}
					break;
				// ACTIONS

				// ABox
				case 'A': {
					String arg = getopt.getOptarg();

					configuration.setAboxDirectory(arg);
				}
					break;

				// translate
				case 'T': {
					String arg = getopt.getOptarg();
					TranslationAction action = null;

					if (arg.toLowerCase().equals("naive")) {
						action = new NaiveTranslationAction();
					} else if (arg.toLowerCase().equals("direct")) {
						action = new DirectTranslationAction();
					} else if (arg.toLowerCase().equals("naff")) {
						action = new DebugTranslationAction(false);
					} else if (arg.toLowerCase().equals("meta")) {
						action = new NaiveMetaTranslationAction();
					} else {
						throw new UsageException(
								"Unknown value for TARGET argument");
					}
					transActions.add(action);
				}
					break;

				// ontology entailment
				case 'e': {
					String arg = getopt.getOptarg();
					IRI domainIRI = null;
					try {
						domainIRI = IRI.create(base.resolve(arg));
						OWLOntologyManager domainOntologyManager = OWLManager.createOWLOntologyManager();
						OWLOntology checkedOntology = domainOntologyManager.loadOntology(domainIRI);
						ReasoningAction action = new EntailmentCheckingAction(checkedOntology);
						reasoningActions.add(action);
					} catch (IllegalArgumentException e) {
						throw new UsageException(arg
								+ " is not a valid ontology name");
					} catch (OWLOntologyCreationException e) {
						throw new UsageException("Failed to load ontology");
					}
				}
					break;

				// output
				case 'O': {
					String arg = getopt.getOptarg();

					if (arg == null) {
						throw new UsageException(
								"Empty value for argument --output");
					} else {
						File fOut = new File(arg);

						try {
							output = new PrintWriter(
									new FileWriter(fOut, false));
						} catch (IOException e) {
							throw new UsageException("Cannot open file: "
									+ fOut.getAbsolutePath());
						}
					}
				}
					break;

				// domain file
				case 'D': {
					String arg = getopt.getOptarg();
					IRI domainIRI = null;
					try {
						domainIRI = IRI.create(base.resolve(arg));
						OWLOntologyManager domainOntologyManager = OWLManager
								.createOWLOntologyManager();
						OWLOntology domainOntology = domainOntologyManager
								.loadOntology(domainIRI);
						Set<OWLNamedIndividual> domainIndividuals = domainOntology
								.getIndividualsInSignature(Imports.EXCLUDED);
						configuration.setDomainIndividuals(domainIndividuals);
					} catch (IllegalArgumentException e) {
						throw new UsageException(arg
								+ " is not a valid ontology name");
					} catch (OWLOntologyCreationException e) {
						throw new UsageException("Failed to load ontology");
					}
				}
					break;

				// consistency
				case 'c': {
					reasoningActions.add(new ConsistencyAction());
				}
					break;

				// enumerate models
				case 'm': {
					String arg = getopt.getOptarg();
					int number = Integer.parseInt(arg);
					ReasoningAction action = new ModelEnumerationAction(number);
					reasoningActions.add(action);
				}
					break;
				case 'C': {
					ReasoningAction action = new CautiousConsequencesAction();
					reasoningActions.add(action);
				}
					break;
				// justification
				case 'j': {
					String arg = getopt.getOptarg();
					IRI domainIRI = null;
					if (arg != null) {
						try {
							domainIRI = IRI.create(base.resolve(arg));
							OWLOntologyManager justificationontologyManager = OWLManager
									.createOWLOntologyManager();
							OWLOntology axiomOntology = justificationontologyManager
									.loadOntology(domainIRI);
							TranslationAction action = new JustificationAction(
									axiomOntology);
							transActions.add(action);
						} catch (IllegalArgumentException e) {
							throw new UsageException(arg
									+ " is not a valid ontology name");
						} catch (OWLOntologyCreationException e) {
							throw new UsageException("Failed to load ontology");
						}
					} else {
						TranslationAction action = new DebugTranslationAction(false);
						transActions.add(action);
					}
				}
					break;
				case 'a': {
					String arg = getopt.getOptarg();
					File axiomatizedOntology;
					if (arg == null) {
						throw new UsageException(
								"Empty value for argument --axiomatize");
					} else {
						axiomatizedOntology = new File(arg);
						TranslationAction action = new AxiomatizationAction(
								axiomatizedOntology);
						transActions.add(action);
					}
				}
					break;
				case 's': {
					String arg = getopt.getOptarg();
					ReasoningAction action = new SubconceptsActions(arg,
							direct);
					reasoningActions.add(action);
				}
					break;
				case 'S': {
					String arg = getopt.getOptarg();
					ReasoningAction action = new SuperconceptsActions(arg,
							direct);
					reasoningActions.add(action);
				}
					break;
				case 'E': {
					String arg = getopt.getOptarg();
					ReasoningAction action = new EquivalentConceptsActions(
							arg, direct);
					reasoningActions.add(action);
				}
					break;
				case 't': {
					String arg = getopt.getOptarg();
					ReasoningAction action = new IndividualTypesAction(arg,
							direct);
					reasoningActions.add(action);
				}
					break;
				case 'i': {
					String arg = getopt.getOptarg();
					ReasoningAction action = new ConceptInstancesAction(arg,
							direct);
					reasoningActions.add(action);
				}
					break;
				// Options:SPARQL Query
				case 'Q': {
					String arg = getopt.getOptarg();

					QueryAction action = new SPARQLAnswerAction(base.resolve(arg).toString());
					queryActions.add(action);
				}
					break;
				// Options: test
				case 'q': {
					String arg = getopt.getOptarg();

					QueryAction action = new SPARQLShowTranslation(base.resolve(arg)
							.toString());
					queryActions.add(action);
				}
					break;
				default:
					if (getopt.getOptopt() != 0) {
						throw new UsageException("invalid option -- "
								+ (char) getopt.getOptopt());
					}
					throw new UsageException("invalid option");
				} // END switch options
			} // END while options loop
			if(args.length == 0) {
				System.out.println("No input ontologies given.");
				System.out.println("Usage : " + usageString);
				System.out.println("Try -h or --help for more information");
			}

			for (int i = getopt.getOptind(); i < args.length; ++i) {
				try {
					ontologies.add(IRI.create(base.resolve(args[i])));
				} catch (IllegalArgumentException e) {
					throw new UsageException(args[i] + " is not a valid ontology name");
				}
			}
			StatusOutput status = new StatusOutput(verbosity);
			
			
			//a stand alone sparql translation cannot be involked inside this loop
			
			for (IRI iriOntology : ontologies) {
				status.log(2,"Processing "+iriOntology.toString());
				
				//just for the evaluation
                logger.debug("Processing"+iriOntology.toString());
				status.log(2,String.valueOf(reasoningActions.size())+" actions");
				
                try {
                    long startTime=System.currentTimeMillis();

                    if (iriOntology.isAbsolute()) {
                        URI uri = URI.create(iriOntology.getNamespace());
                        String scheme = uri.getScheme();
                        if (scheme != null && scheme.equalsIgnoreCase("file")) {
                            File file = new File(URI.create(iriOntology.getNamespace()));
                            if (file.isDirectory()) {
                                OWLOntologyIRIMapper mapper = new AutoIRIMapper(file, false);
                                ontologyManager.addIRIMapper(mapper);                                
                            }
                        }
                    }
                    

                    OWLOntology ontology=ontologyManager.loadOntology(iriOntology);
                    
                    long parseTime = System.currentTimeMillis()-startTime;
                    status.log(2,"Ontology parsed in " + String.valueOf(parseTime) + " msec.");
                    
                    //just for the evaluation
                    logger.debug("Ontology loaded in "+String.valueOf(parseTime)+ "msec.");
                    
                    startTime = System.currentTimeMillis();
                    
                    long loadTime = System.currentTimeMillis() - startTime;
                    status.log(2, "Reasoner created in " + String.valueOf(loadTime) + " msec.");


                    
                    for (TranslationAction action : transActions) {
                        status.log(2, "Doing action...");
                        logger.debug("Translating ontology...");
                        startTime = System.currentTimeMillis();
                        action.run(ontology, configuration, status, output);
                        long actionTime = System.currentTimeMillis() - startTime;
                        status.log(2, "...action completed in " + String.valueOf(actionTime) + " msec.");
                        logger.debug("Ontology translated in "+String.valueOf(actionTime)+ "msec.");
                    }
                    
                    Wolpertinger wolpertinger = null;
                    if (!reasoningActions.isEmpty())
                    	wolpertinger = new Wolpertinger(configuration, ontology);
                    
                    for (ReasoningAction action : reasoningActions) {
                    	status.log(2, "Doing reasoning action ...");
                    	startTime = System.currentTimeMillis();
                    	action.run(wolpertinger, configuration, status, output);
                    	long actionTime = System.currentTimeMillis() - startTime;
                    	status.log(2,  "...action completed in " + String.valueOf(actionTime) + " msec.");
                    }
                    
                    
                } catch (Throwable e) {
                	System.err.println(e.getMessage());
                	e.printStackTrace(System.err);
                } 
			}
//make a QueryAction handler at the moment, need to be formaly reconstructed in the future
			
			
			if(!queryActions.isEmpty()) {
				Wolpertinger wolpertinger=null;
				
				for(QueryAction action:queryActions) {
					try {
						long _startTime=System.currentTimeMillis();
						action.run(wolpertinger, configuration, status, output);
						long _translateTime=System.currentTimeMillis()-_startTime;
						logger.debug("query translated in:"+Long.toString(_translateTime)+"msec.");


					} catch (WolpertingerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally {
					}
				}
			
			
				
			}
		} // END try
		catch (UsageException e) {
			System.err.println(e.getMessage());
			System.err.println(usageString);
			System.err.println("try '--help' for more information.");
		} 
	}

}

enum Arg {
	NONE, OPTIONAL, REQUIRED
}

/**
 * Represents a single CL Option.
 */
class Option {
	protected int optChar;
	protected String longStr;
	protected String group;
	protected Arg arg;
	protected String metavar;
	protected String help;

	public Option(int inChar, String inLong, String inGroup, String inHelp) {
		optChar = inChar;
		longStr = inLong;
		group = inGroup;
		arg = Arg.NONE;
		help = inHelp;
	}

	public Option(int inChar, String inLong, String inGroup,
			boolean argRequired, String inMetavar, String inHelp) {
		optChar = inChar;
		longStr = inLong;
		group = inGroup;
		arg = (argRequired ? Arg.REQUIRED : Arg.OPTIONAL);
		metavar = inMetavar;
		help = inHelp;
	}

	public static LongOpt[] createLongOpts(Option[] opts) {
		LongOpt[] out = new LongOpt[opts.length];
		for (int i = 0; i < opts.length; ++i) {
			out[i] = new LongOpt(
					opts[i].longStr,
					(opts[i].arg == Arg.NONE ? LongOpt.NO_ARGUMENT
							: opts[i].arg == Arg.OPTIONAL ? LongOpt.OPTIONAL_ARGUMENT
									: LongOpt.REQUIRED_ARGUMENT), null,
					opts[i].optChar);
		}
		return out;
	}

	public String getLongOptExampleStr() {
		if (longStr == null || longStr.equals(""))
			return "";
		return new String("--"
				+ longStr
				+ (arg == Arg.NONE ? "" : arg == Arg.OPTIONAL ? "[=" + metavar
						+ "]" : "=" + metavar));
	}

	public static String formatOptionHelp(Option[] opts) {
		StringBuffer out = new StringBuffer();
		int fieldWidth = 0;
		for (Option o : opts) {
			int curWidth = o.getLongOptExampleStr().length();
			if (curWidth > fieldWidth)
				fieldWidth = curWidth;
		}
		String curGroup = null;
		for (Option o : opts) {
			if (o.group != curGroup) {
				curGroup = o.group;
				out.append(System.getProperty("line.separator"));
				if (o.group != null) {
					out.append(curGroup + ":");
					out.append(System.getProperty("line.separator"));
				}
			}
			if (o.optChar < 256) {
				out.append("  -");
				out.appendCodePoint(o.optChar);
				if (o.longStr != null && o.longStr != "") {
					out.append(", ");
				} else {
					out.append("  ");
				}
			} else {
				out.append("      ");
			}
			int fieldLeft = fieldWidth + 1;
			if (o.longStr != null && o.longStr != "") {
				String s = o.getLongOptExampleStr();
				out.append(s);
				fieldLeft -= s.length();
			}
			for (; fieldLeft > 0; --fieldLeft)
				out.append(' ');
			out.append(breakLines(o.help, 80, 6 + fieldWidth + 1));
			out.append(System.getProperty("line.separator"));
		}
		return out.toString();
	}

	public static String formatOptionsString(Option[] opts) {
		StringBuffer out = new StringBuffer();
		for (Option o : opts) {
			if (o.optChar < 256) {
				out.appendCodePoint(o.optChar);
				switch (o.arg) {
				case REQUIRED:
					out.append(":");
					break;
				case OPTIONAL:
					out.append("::");
					break;
				case NONE:
					break;
				}
			}
		}
		return out.toString();
	}

	protected static String breakLines(String str, int lineWidth, int indent) {
		StringBuffer out = new StringBuffer();
		BreakIterator i = BreakIterator.getLineInstance();
		i.setText(str);
		int curPos = 0;
		int curLinePos = indent;
		int next = i.first();
		while (next != BreakIterator.DONE) {
			String curSpan = str.substring(curPos, next);
			if (curLinePos + curSpan.length() > lineWidth) {
				out.append(System.getProperty("line.separator"));
				for (int j = 0; j < indent; ++j)
					out.append(" ");
				curLinePos = indent;
			}
			out.append(curSpan);
			curLinePos += curSpan.length();
			curPos = next;
			next = i.next();
		}
		return out.toString();
	}
}
