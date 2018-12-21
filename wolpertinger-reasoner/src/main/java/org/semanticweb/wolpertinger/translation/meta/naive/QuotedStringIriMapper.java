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

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.wolpertinger.Prefixes;

/**
 * Simply gives a quoted string representation of the IRI.
 * 
 * i.e. http://semanticweb.org/.../wolpertinger => "http://semanticweb.org/.../wolpertinger"
 * 
 * @author Lukas Schweizer
 *
 */
public class QuotedStringIriMapper implements MetaTranslationIRIMapper {

	private Prefixes prefixes;
	
	/* (non-Javadoc)
	 * @see org.semanticweb.wolpertinger.translation.meta.naive.MetaTranslationIRIMapper#setIriPrefixHandler()
	 */
	public void setIriPrefixHandler(Prefixes prefixes) {
		this.prefixes = prefixes;
	}
	
	public QuotedStringIriMapper() {
		prefixes = new Prefixes();
	}
	
	public boolean isAuxiliaryClass(IRI auxIRI) {
		return Prefixes.isInternalIRI(auxIRI.toString());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.wolpertinger.translation.meta.naive.MetaTranslationIRIMapper#getIriRepresentation(org.semanticweb.owlapi.model.IRI)
	 */
	public String getIriRepresentation(IRI iri) {
		return String.format("\"%s\"", iri.toString());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.wolpertinger.translation.meta.naive.MetaTranslationIRIMapper#getIrifromRepresentation(java.lang.String)
	 */
	public IRI getIrifromRepresentation(String iriRepresentation) {
		return IRI.create(iriRepresentation);
	}

}
