/*
 *
 * ModifiedPerceptron.java utumno.mope.datastructures.SimilarityNodeSimple
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

public class SimilarityNodeSimple implements Comparable<Object>{
	private float similarity;
	private boolean isPositiveExample;

	public SimilarityNodeSimple(float similarity, boolean isPositiveExample){
        this.similarity = similarity;
        this.isPositiveExample = isPositiveExample;
    }

	public float getSimilarity() {
		return similarity;
	}

	public boolean isPositiveExample() {
		return isPositiveExample;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}

	public void setPositiveExample(boolean isPositiveExample) {
		this.isPositiveExample = isPositiveExample;
	}

	public int compareTo(Object o) {
		if(o instanceof SimilarityNodeSimple){
            if(getSimilarity() > ((SimilarityNodeSimple)o).getSimilarity()){
                return 1;
            }
            else if(getSimilarity() < ((SimilarityNodeSimple)o).getSimilarity()){
                return -1;
            }
        }
        return 0;
	}
	
}