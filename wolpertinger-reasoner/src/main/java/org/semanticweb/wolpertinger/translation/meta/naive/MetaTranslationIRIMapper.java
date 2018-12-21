package org.semanticweb.wolpertinger.translation.meta.naive;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.wolpertinger.Prefixes;

public interface MetaTranslationIRIMapper {

	public void setIriPrefixHandler(Prefixes prefixes);
	
	public boolean isAuxiliaryClass(IRI auxIRI);
	
	public String getIriRepresentation(IRI iri);
	public IRI getIrifromRepresentation(String iriRepresentation);
	
}
