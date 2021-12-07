/*
 *
 * ModifiedPerceptron.java utumno.mope.datastructures.ClassVectorSparse
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
import java.util.Hashtable;

@SuppressWarnings("serial")
public class ClassVectorSparse implements Serializable, Comparable<Object> {
	private String name;
	private int size = 0;
	private float threshold = Float.NEGATIVE_INFINITY;
	private Hashtable<Integer, VectorNode> nodes;

	public ClassVectorSparse() {
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

	public Hashtable<Integer, VectorNode> getNodes() {
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

	public void setNodes(Hashtable<Integer, VectorNode> nodes) {
		this.nodes = nodes;
	}
	
	public int compareTo(Object o) {
		if (o instanceof ClassVectorSparse) {
			return (this.name.compareTo(((ClassVectorSparse) o).getName()));
		}
		return 0;
	}
	
	public float similarityWithDocumentVector(DocumentVector documentVector){
		float similarity = 0.0f;
		if(documentVector.getNodes() == null || documentVector.getNodes().isEmpty()){
			return 0.0f;
		}
		if(nodes==null){
			return 0.0f;
		}
		float norm = 0.0f;
		for(VectorNode thatNode : documentVector.getNodes()){
			if(thatNode.getTermId() < 1) continue;
			VectorNode thisNode = nodes.get(thatNode.getTermId());
			if(thisNode!=null){
				similarity += thisNode.getTermWeight() * thatNode.getTermWeight();
				norm += thatNode.getTermWeight() * thatNode.getTermWeight();
			}
		}
		norm = (float) Math.sqrt(norm);
		similarity = similarity / norm;
		return similarity;
	}

}