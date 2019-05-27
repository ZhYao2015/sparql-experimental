# sparql-experimental 

## Special alert

### For Windows users, please use the absolute path for reading ontologies, but for reading queries relative path can be used.
### In both platform, When building the maven project, please skip the test by using options '-DskipTests'. It does not mean that tests cannot be passed, in fact its due to the version conflict of JUnit used by different contributors.

## I apologize for the above-mentioned inconvenience.

About Files:
1. arq-mem: A query engine for Jena ARQ using the "in-memory" dataset.

2. rdf4j-demo : A query engine for RDF4J, both "in-memory" and "native" datasets are available.

3. lubm-data: Datasets which are used in the evaluation without inference.

4. lite-lubm: Ontologies which are used in the evaluation with inference.

5. queries: Queries in Appendix.

6. wolpertinger-reasoner: A fixed-domain reasoner, the translation of SPARQL query is its sub-feature.



Usage (wolpertinger-reasoner):

Installation:

cd wolpertinger-reasoner

### mvn clean install -DskipTests 

//Some out-of-date testcases are not deleted after a reconstruction
//!This step is ###necessary!

Query:
We directly call the clingo on the terminal for the purpose of evaluation.

java -jar target/wolpertinger-reasoner-1.0.jar --translate=meta --domain=DOMAIN_FILE --q_translate=QUERY_FILE  ONTOLOGY_FILE
| clingo --enum-mode=cautious

Example:

java -jar target/wolpertinger-reasoner-1.0.jar --translate=meta --q_translate=../queries/q1/q1-bgp-simple.txt   ../lubm-data/imported/imported-lubm-20.owl | clingo --enum-mode=cautious

Clingo version: 4.5.4, 64bit.






