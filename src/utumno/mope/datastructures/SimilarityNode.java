/*
 *
 * ModifiedPerceptron.java utumno.mope.datastructures.SimilarityNode
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

public class SimilarityNode implements Comparable<Object>{
	private float similarity;
	private DocumentVector documentVector;

	public SimilarityNode(float similarity, DocumentVector documentVector){
        this.similarity = similarity;
        this.documentVector = documentVector;
    }

	public float getSimilarity() {
		return similarity;
	}

	public DocumentVector getDocumentVector() {
		return documentVector;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}

	public void setDocumentVector(DocumentVector documentVector) {
		this.documentVector = documentVector;
	}

	public int compareTo(Object o) {
		if(o instanceof SimilarityNode){
            if(getSimilarity() > ((SimilarityNode)o).getSimilarity()){
                return 1;
            }
            else if(getSimilarity() < ((SimilarityNode)o).getSimilarity()){
                return -1;
            }
        }
        return 0;
	}
	
}