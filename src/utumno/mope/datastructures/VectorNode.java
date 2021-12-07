/*
 *
 * ModifiedPerceptron.java utumno.mope.datastructures.VectorNode
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
public class VectorNode implements Serializable,Comparable<Object> {
	private int termId;
    private float termWeight;
    
	public VectorNode(int id, float weight) {
		termId = id;
		termWeight = weight;
	}
	
	public int getTermId() {
		return termId;
	}

	public float getTermWeight() {
		return termWeight;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public void setTermWeight(float termWeight) {
		this.termWeight = termWeight;
	}
	
	public VectorNode clone(){
		return new VectorNode(termId, termWeight);
	}
    
	public String toString(){
		return "{" + this.getTermId() + "," + this.getTermWeight() + "}";
	}

	public int compareTo(Object o) {
		if(o instanceof VectorNode){
            if(getTermId() > ((VectorNode)o).getTermId()){
                return 1;
            }
            else if(getTermId() < ((VectorNode)o).getTermId()){
                return -1;
            }
        }
        return 0;
	}
	
}