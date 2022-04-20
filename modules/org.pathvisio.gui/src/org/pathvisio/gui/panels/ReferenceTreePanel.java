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
package org.pathvisio.gui.panels;

import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import org.pathvisio.libgpml.model.PathwayElement.AnnotationRef;
import org.pathvisio.libgpml.model.PathwayElement.CitationRef;
import org.pathvisio.libgpml.model.PathwayElement.EvidenceRef;

/**
 * Parent class for CitationTreePanel and AnnotationTreePanel.
 * 
 * NB: EvidenceRefs do not have nested InfoRefs.
 * 
 * @author finterly
 */
public abstract class ReferenceTreePanel extends PathwayElementPanel implements ActionListener {

	protected void addCitationRefNodes(DefaultMutableTreeNode node, List<CitationRef> citationRefs) {
		for (CitationRef cr : citationRefs) {
			DefaultMutableTreeNode crNode = new DefaultMutableTreeNode(cr);
			addAnnotationRefNodes(crNode, cr.getAnnotationRefs());
			node.add(crNode);
		}
	}

	protected void addAnnotationRefNodes(DefaultMutableTreeNode node, List<AnnotationRef> annotationRefs) {
		for (AnnotationRef ar : annotationRefs) {
			DefaultMutableTreeNode arNode = new DefaultMutableTreeNode(ar);
			addCitationRefNodes(arNode, ar.getCitationRefs());
			addEvidenceRefNodes(arNode, ar.getEvidenceRefs());
			node.add(arNode);
		}
	}

	protected void addEvidenceRefNodes(DefaultMutableTreeNode node, List<EvidenceRef> evidenceRefs) {
		for (EvidenceRef er : evidenceRefs) {
			DefaultMutableTreeNode erNode = new DefaultMutableTreeNode(er);
			node.add(erNode);
		}
	}

}
