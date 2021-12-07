/*
 *
 * ModifiedPerceptron.java utumno.mope.datastructures.ClassVector
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

@SuppressWarnings("serial")
public class ClassVector implements Serializable, Comparable<Object> {
	private String name;
	private int size = 0;
	private float threshold = Float.NEGATIVE_INFINITY;
    private DocumentVector posThresholdVector = null;
	private DocumentVector negThresholdVector = null;
	private VectorNode[] nodes;

	public ClassVector() {
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public float getThreshold() {
		return threshold;
	}
        
	public DocumentVector getPosThresholdVector() {
		return posThresholdVector;
	}
	
	public DocumentVector getNegThresholdVector() {
		return negThresholdVector;
	}

	public VectorNode[] getNodes() {
		return nodes;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}
        
	public void setPosThresholdVector(DocumentVector posThresholdVector) {
		this.posThresholdVector = posThresholdVector;
	}
	
	public void setNegThresholdVector(DocumentVector negThresholdVector) {
		this.negThresholdVector = negThresholdVector;
	}

	public void setNodes(VectorNode[] nodes) {
		this.nodes = nodes;
	}

	public ClassVector clone() {
		ClassVector ret = new ClassVector();
		ret.setName(name);
		ret.setSize(size);
		ret.setThreshold(threshold);
		if(posThresholdVector!=null){
			ret.setPosThresholdVector(posThresholdVector.clone());
		}
		if(negThresholdVector!=null){
			ret.setNegThresholdVector(negThresholdVector.clone());
		}
		if(nodes!=null){
			VectorNode[] thatNodes = new VectorNode[this.nodes.length];
			for(int i = nodes.length-1; i>=0; i--){
				if(nodes[i]!=null){
					thatNodes[i] = nodes[i].clone();
				}
			}
			ret.setNodes(thatNodes);
		}
		return ret;
	}
	
	public int compareTo(Object o) {
		if (o instanceof ClassVector) {
			return (this.name.compareTo(((ClassVector) o).getName()));
		}
		return 0;
	}
	
	public void addWithDocumentVector(DocumentVector documentVector, float multiply){
		if(documentVector == null || documentVector.getNodes() == null || documentVector.getNodes().isEmpty()){
			return;
		}
		if(nodes==null){
			return;
		}
		for(VectorNode thatNode : documentVector.getNodes()){
			VectorNode thisNode = nodes[thatNode.getTermId()-1];
			if(thisNode!=null){
				thisNode.setTermWeight(thisNode.getTermWeight() + thatNode.getTermWeight()*multiply);
			}
			else{
				nodes[thatNode.getTermId()-1] = new VectorNode(thatNode.getTermId(),thatNode.getTermWeight()*multiply);
			}
		}
	}
	
	public void addWithClassVector(ClassVector classVector, float multiply){
		if(classVector.getNodes() == null){
			return;
		}
		if(nodes==null){
			return;
		}
		for(VectorNode thatNode : classVector.getNodes()){
			if(thatNode==null){
				continue;
			}
			VectorNode thisNode = nodes[thatNode.getTermId()-1];
			if(thisNode!=null){
				thisNode.setTermWeight(thisNode.getTermWeight()+thatNode.getTermWeight()*multiply);
			}
			else{
				nodes[thatNode.getTermId()-1] = new VectorNode(thatNode.getTermId(),thatNode.getTermWeight()*multiply);
			}
		}
	}
	
	public float computeNorm(){
		float norm = 0.0f;
		if(nodes==null){
			return 0.0f;
		}
		for(VectorNode node : nodes){
			if(node==null){
				continue;
			}
			norm += node.getTermWeight() * node.getTermWeight();
		}
		return (float)Math.sqrt(norm);
	}
	
	public void divide(float div){
		if(nodes==null){
			return;
		}
		if(div==0.0f){
			return;
		}
		for(VectorNode node : nodes){
			if(node!=null){
				node.setTermWeight(node.getTermWeight()/div);
			}
		}
	}
	
	public void multiply(float mul){
		if(nodes==null){
			return;
		}
		for(VectorNode node : nodes){
			if(node!=null){
				node.setTermWeight(node.getTermWeight()*mul);
			}
		}
	}
	
	public float similarityWithDocumentVector(DocumentVector documentVector){
		float similarity = 0.0f;
		if(documentVector.getNodes() == null || documentVector.getNodes().isEmpty()){
			return 0.0f;
		}
		if(nodes==null){
			return 0.0f;
		}
		for(VectorNode thatNode : documentVector.getNodes()){
			if(thatNode.getTermId() < 1) continue;
			if(thatNode.getTermId() > nodes.length) continue;
			VectorNode thisNode = nodes[thatNode.getTermId()-1];
			if(thisNode!=null){
				similarity += thisNode.getTermWeight()*thatNode.getTermWeight();
			}
		}
		return similarity;
	}
		
	public float similarityWithClassVector(ClassVector classVector){
		float similarity = 0.0f;
		if(classVector.getNodes() == null){
			return 0.0f;
		}
		if(nodes==null){
			return 0.0f;
		}
		for(VectorNode thatNode : classVector.getNodes()){
			if(thatNode==null){
				continue;
			}
			VectorNode thisNode = nodes[thatNode.getTermId()-1];
			if(thisNode!=null){
				similarity += thisNode.getTermWeight()*thatNode.getTermWeight();
			}
		}
		return similarity;
	}
}