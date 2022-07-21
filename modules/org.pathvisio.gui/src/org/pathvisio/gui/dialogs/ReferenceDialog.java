/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2022 BiGCaT Bioinformatics, WikiPathways
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.gui.dialogs;

import java.awt.Component;
import java.awt.Frame;
import java.util.List;
import org.pathvisio.libgpml.model.PathwayElement.AnnotationRef;
import org.pathvisio.libgpml.model.PathwayElement.CitationRef;
import org.pathvisio.libgpml.model.PathwayElement.EvidenceRef;
import org.pathvisio.libgpml.model.PathwayElement.InfoRef;
import org.pathvisio.libgpml.model.Referenceable.Annotatable;
import org.pathvisio.libgpml.model.Referenceable.Citable;
import org.pathvisio.libgpml.model.Referenceable.Evidenceable;

/**
 * ReferenceDialog class which contains Copy InfoRef Methods used by
 * {@link AnnotationDialog}, {@link CitationDialog}, and {@link EvidenceDialog}.
 * 
 * NB: In our implementation, when an {@link InfoRef} is "EDITED", actually a
 * new InfoRef object is created. This is in order to cleanly add Reference
 * (e.g. Annotation, Citation, Evidence) information to the PathwayModel. The
 * Copy Methods below help copy over nested InfoRef information.
 * 
 * @author finterly
 */
public abstract class ReferenceDialog extends OkCancelDialog {

	// ================================================================================
	// Constructors
	// ================================================================================
	public ReferenceDialog(Frame frame, String title, Component locationComp, boolean modal, boolean cancellable) {
		super(frame, title, locationComp, modal, cancellable);
		setSize(300, 250);// UI Design
	}

	// ================================================================================
	// Copy InfoRef Methods
	// ================================================================================
	/**
	 * @param oldC
	 * @param newC
	 */
	public void copyRefsOldToNew(CitationRef oldC, CitationRef newC) {
		if (oldC != null && newC != null) {
			copyAnnotationRefsToNew(oldC.getAnnotationRefs(), newC);
		}
	}

	/**
	 * @param oldA
	 * @param newA
	 */
	public void copyRefsOldToNew(AnnotationRef oldA, AnnotationRef newA) {
		if (oldA != null && newA != null) {
			copyCitationRefsToNew(oldA.getCitationRefs(), newA);
			copyEvidenceRefsToNew(oldA.getEvidenceRefs(), newA);
		}
	}

	/**
	 * @param citationRefs
	 * @param newC
	 */
	private void copyCitationRefsToNew(List<CitationRef> citationRefs, Citable newC) {
		for (CitationRef citationRef : citationRefs) {
			CitationRef newNestedRef = newC.addCitation(citationRef.getCitation().copyRef());
			copyAnnotationRefsToNew(citationRef.getAnnotationRefs(), newNestedRef);
		}
	}

	/**
	 * @param annotationRefs
	 * @param newA
	 */
	private void copyAnnotationRefsToNew(List<AnnotationRef> annotationRefs, Annotatable newA) {
		for (AnnotationRef annotationRef : annotationRefs) {
			AnnotationRef newNestedRef = newA.addAnnotation(annotationRef.getAnnotation().copyRef());
			copyCitationRefsToNew(annotationRef.getCitationRefs(), newNestedRef);
			copyEvidenceRefsToNew(annotationRef.getEvidenceRefs(), newNestedRef);

		}
	}

	/**
	 * @param evidenceRefs
	 * @param newE
	 */
	private void copyEvidenceRefsToNew(List<EvidenceRef> evidenceRefs, Evidenceable newE) {
		for (EvidenceRef evidenceRef : evidenceRefs) {
			newE.addEvidence(evidenceRef.getEvidence().copyRef());
		}
	}

}
