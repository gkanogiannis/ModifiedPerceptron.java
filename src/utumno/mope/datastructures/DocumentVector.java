/*
 *
 * ModifiedPerceptron.java utumno.mope.datastructures.DocumentVector
 *
 * Copyright (C) 2021 Anestis Gkanogiannis <anestis@gkanogiannis.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */
package utumno.mope.datastructures;
import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class DocumentVector implements Serializable {
	private String docId = "-1";
	private boolean positiveExample;
	private ArrayList<VectorNode> nodes;

	public DocumentVector() {
		nodes = new ArrayList<VectorNode>();
	}
	
	public DocumentVector(VectorNode[] nodesArray){
		ArrayList<VectorNode> nodes = new ArrayList<VectorNode>();
		for(VectorNode node : nodesArray){
			if(node == null){
				continue;
			}
			nodes.add(node.clone());
		}
		nodes.trimToSize();
		this.nodes = nodes;
	}
	
	public DocumentVector(ArrayList<VectorNode> nodesArray){
		ArrayList<VectorNode> nodes = new ArrayList<VectorNode>();
		for(VectorNode node : nodesArray){
			nodes.add(node.clone());
		}
		nodes.trimToSize();
		this.nodes = nodes;
	}

	public String getDocId() {
		return docId;
	}

	public boolean isPositiveExample() {
		return positiveExample;
	}

	public ArrayList<VectorNode> getNodes() {
		return nodes;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public void setPositiveExample(boolean positiveExample) {
		this.positiveExample = positiveExample;
	}

	public void setNodes(ArrayList<VectorNode> nodes) {
		this.nodes = nodes;
	}

	public float computeNorm(){
		float norm = 0.0f;
		if(nodes==null || nodes.isEmpty()){
			return 0.0f;
		}
		for(VectorNode node : nodes){
			norm += node.getTermWeight() * node.getTermWeight();
		}
		return (float)Math.sqrt(norm);
	}
	
	public void normalize(){
		float norm = computeNorm();
		if(nodes==null || nodes.isEmpty()){
			return;
		}
		for(VectorNode node : nodes){
			node.setTermWeight(node.getTermWeight() / norm);
		}
		return;
	}

	public DocumentVector clone() {
		DocumentVector ret = new DocumentVector(getNodes());
		ret.setDocId(getDocId());
		ret.setPositiveExample(isPositiveExample());
		
		return ret;
	}

	public float similarityWithDocumentVector(DocumentVector thatDocumentVector) {
		float similarity = 0.0f;
		if(thatDocumentVector.getNodes() == null || thatDocumentVector.getNodes().isEmpty() || nodes == null || nodes.isEmpty()){
			return 0.0f;
		}
		int i=0;
		int j=0;
		while(i<nodes.size() && j<thatDocumentVector.getNodes().size()){
			VectorNode thisNode = nodes.get(i);
			VectorNode thatNode = thatDocumentVector.getNodes().get(j);
			if(thisNode.getTermId() > thatNode.getTermId()){
				j++;
			}
			else if(thisNode.getTermId() < thatNode.getTermId()){
				i++;
			}
			else{
				similarity += thisNode.getTermWeight() * thatNode.getTermWeight();
				i++;
				j++;
			}
		}
		
		return similarity;
	}
}
