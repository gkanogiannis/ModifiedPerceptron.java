/*
 *
 * ModifiedPerceptron.java utumno.mope.ModifiedPerceptron_F1
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
package utumno.mope;

import java.io.BufferedWriter;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

import utumno.mope.datastructures.BinaryHeap;
import utumno.mope.datastructures.ClassVector;
import utumno.mope.datastructures.DocumentVector;
import utumno.mope.datastructures.SimilarityNode;
import utumno.mope.datastructures.VectorNode;

public class ModifiedPerceptron_F1 {
	
	private ClassVector classVector;
	
	private ArrayList<DocumentVector> documentVectors;
	
	private int iteration;
	private int iterations;
	
	private PrintStream printstream;
	
	private BufferedWriter outTxt;
	
	private boolean verbose;
	
	private int a = 0;
	private int b = 0;
	private int c = 0;
	private int d = 0;
	
	private ClassVector maxClassVector;
	private float maxF1 = Float.NEGATIVE_INFINITY;

	private BinaryHeap TPHeap;
	private BinaryHeap FNHeap;
	private BinaryHeap FPHeap;
	private BinaryHeap TNHeap;
	private DocumentVector FN;
	private DocumentVector FP;

	private static NumberFormat nf = NumberFormat.getInstance();
	static{
		nf.setGroupingUsed(true);
		nf.setMaximumFractionDigits(4);
		nf.setMinimumFractionDigits(4);
		nf.setMaximumIntegerDigits(1);
		nf.setMinimumIntegerDigits(1);
	}
	
	public ModifiedPerceptron_F1(ClassVector classVector, ArrayList<DocumentVector> documentVectors, int iterations, PrintStream printstream, BufferedWriter outTxt, boolean verbose){
		this.classVector = classVector;
		this.documentVectors = documentVectors;
		this.iterations = iterations;
		this.printstream = printstream;
		this.outTxt = outTxt;
		this.verbose = verbose;
	}
	
	public ClassVector getMaxClassVector() {
		return maxClassVector;
	}

	public float getMaxF1() {
		return maxF1;
	}
	
	public ClassVector executeAdjust(){
		try{
			//Compute F1 for iteration 0 and update max
			clear();
			fillHeaps();
			float F1 = computeF1();
			if(F1 > maxF1){
				maxF1 = F1;
				maxClassVector = classVector.clone();
			}
			
			if(verbose){
				printstream.println("");
				printstream.println("it="+iteration+"\tF1="+nf.format(F1)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
				outTxt.write(System.getProperty("line.separator"));
				outTxt.write("it="+iteration+"\tF1="+nf.format(F1)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
				outTxt.write(System.getProperty("line.separator"));
			}
			
			if(F1 == 1.0f){
				printstream.println("\r\niterations="+iteration);
				printstream.println("maxF1="+nf.format(F1)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
				outTxt.write("\r\niterations="+iteration);
				outTxt.write(System.getProperty("line.separator"));
				outTxt.write("maxF1="+nf.format(F1)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
				outTxt.write(System.getProperty("line.separator"));
			
				return maxClassVector;
			}
			
			do{
				iteration++;
				iterations--;
				
				if(verbose){
					printstream.print("it="+iteration+"\t");
					outTxt.write("it="+iteration+"\t");
				}
				else{
					printstream.print("*");
				}
				
				//Compute FN,FP Centroids
				computeMiscclasifiedCentroids();
				
				float heta = 0.0f;
				ClassVector dummy = null;
				DocumentVector e = null;
				
				if(!FNHeap.isEmpty() && !FPHeap.isEmpty()){
					dummy = new ClassVector();
					dummy.setNodes(new VectorNode[classVector.getNodes().length]);
					dummy.addWithDocumentVector(FN, 1.0f);
					dummy.addWithDocumentVector(FP, -1.0f);
					e = new DocumentVector(dummy.getNodes());
					dummy = null;
					
					heta = - (classVector.similarityWithDocumentVector(e))/(e.similarityWithDocumentVector(e));
					
		            classVector.addWithDocumentVector(e, heta);
		            
					classVector.setPosThresholdVector(FN.clone());
					classVector.setNegThresholdVector(FP.clone());
		            
					classVector.setThreshold(classVector.similarityWithDocumentVector(FN));
				}
				else if(FNHeap.isEmpty()){
					dummy = new ClassVector();
					dummy.setNodes(new VectorNode[classVector.getNodes().length]);
					dummy.addWithDocumentVector(classVector.getPosThresholdVector(), 1.0f);
					dummy.addWithDocumentVector(FP, -1.0f);
					e = new DocumentVector(dummy.getNodes());
					dummy = null;
					
					heta = - (classVector.similarityWithDocumentVector(e))/(e.similarityWithDocumentVector(e));
					
					classVector.addWithDocumentVector(e, heta);
		            
		            classVector.setNegThresholdVector(FP.clone());
					
					classVector.setThreshold(classVector.similarityWithDocumentVector(FP));
				}
				else if(FPHeap.isEmpty()){
					dummy = new ClassVector();
					dummy.setNodes(new VectorNode[classVector.getNodes().length]);
					dummy.addWithDocumentVector(FN, 1.0f);
					dummy.addWithDocumentVector(classVector.getNegThresholdVector(), -1.0f);
					e = new DocumentVector(dummy.getNodes());
					dummy = null;
					
					heta = - (classVector.similarityWithDocumentVector(e))/(e.similarityWithDocumentVector(e));
					
					classVector.addWithDocumentVector(e, heta);
					
					classVector.setPosThresholdVector(FN.clone());
					
					classVector.setThreshold(classVector.similarityWithDocumentVector(FN));
				}
				
				//Compute F1 for iteration i and update max
		        clear();
		        fillHeaps();
		        F1 = computeF1();
				if(F1 > maxF1){
					maxF1 = F1;
					maxClassVector = classVector.clone();
				}
				
		        if(verbose){
					printstream.println("\tF1="+nf.format(F1)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
					outTxt.write("\tF1="+nf.format(F1)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
					outTxt.write(System.getProperty("line.separator"));
				}
			    	
		        //System.out.println("b="+classVector.getThreshold()+" pT="+classVector.getPosThresholdVector().computeNorm()+" nT="+classVector.getNegThresholdVector().computeNorm());
		        
		        //If perfect classification
				if(F1 == 1.0f){// || (NAHeap.size()==1 && PRHeap.size()==0) || (NAHeap.size()==0 && PRHeap.size()==1) || (NAHeap.size()==1 && PRHeap.size()==1)){
					maxF1 = F1;
					maxClassVector = classVector.clone();
					break;
				}
			}
			while(iterations>0);
			
			classVector = maxClassVector;
			//Compute F1 and heaps for maxclassvector
	        clear();
	        fillHeaps();
	        F1 = computeF1();
			
			printstream.println("\r\niterations="+iteration);
			printstream.println("maxF1="+nf.format(F1)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
			outTxt.write("\r\niterations="+iteration);
			outTxt.write(System.getProperty("line.separator"));
			outTxt.write("maxF1="+nf.format(F1)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
			outTxt.write(System.getProperty("line.separator"));
			
			return maxClassVector;
		}
		catch(Exception e){
			throw new Error(e);
		}
	}
		
	private void fillHeaps(){
		for(DocumentVector documentVector : documentVectors){
			float similarity = classVector.similarityWithDocumentVector(documentVector);
			
			if(Math.abs(similarity - classVector.getThreshold()) < Trainer.epsilon){
				if(documentVector.isPositiveExample()){
                    a++;
                    TPHeap.add(new SimilarityNode(similarity,documentVector));
                }
                else{
                    d++;
                    TNHeap.add(new SimilarityNode(similarity,documentVector));
                }
			}
			else if(similarity > classVector.getThreshold()){	
                if(documentVector.isPositiveExample()){
                    a++;
                    TPHeap.add(new SimilarityNode(similarity,documentVector));
                }
                else{
                    b++;
                    FPHeap.add(new SimilarityNode(similarity,documentVector));
                }
            }
            else if(similarity < classVector.getThreshold()){
                if(documentVector.isPositiveExample()){
                    c++;
                    FNHeap.add(new SimilarityNode(similarity,documentVector));
                }
                else{
                    d++;
                    TNHeap.add(new SimilarityNode(similarity,documentVector));
                }
            }
		}
	}
	
	@SuppressWarnings("unchecked")
	private void computeMiscclasifiedCentroids(){
		ClassVector dummy;
		
		if(FNHeap.size() > 0){
			dummy = new ClassVector();
			dummy.setNodes(new VectorNode[classVector.getNodes().length]);
		    Iterator<SimilarityNode> it1 = FNHeap.iterator();
		    while(it1.hasNext()){
		    	dummy.addWithDocumentVector(it1.next().getDocumentVector(),1.0f);
		    }
		    dummy.divide(((float)FNHeap.size()));
		    FN = new DocumentVector(dummy.getNodes());
	    	FN.setPositiveExample(true);
		}
		
		if(FPHeap.size() > 0){
			dummy = new ClassVector();
		    dummy.setNodes(new VectorNode[classVector.getNodes().length]);
	        Iterator<SimilarityNode> it2 = FPHeap.iterator();
	        while(it2.hasNext()){
	        	dummy.addWithDocumentVector(it2.next().getDocumentVector(),1.0f);
	        }
	        dummy.divide(((float)FPHeap.size()));
	        FP = new DocumentVector(dummy.getNodes());
	        FP.setPositiveExample(false);
		}
		
        dummy = null;
	}
	
	public float computeF1(){
		float p = 0.0f;
		float r = 0.0f;
		float F1 = 0.0f;
		if(a+b!=0){
			p = (float)a / (float)(a+b);
		}
		if(a+c!=0){
			r = (float)a / (float)(a+c);
		}
		if(p+r!=0.0f){
			F1 = 2.0f*p*r / (p+r);
		}
		return F1;
	}
	
	public void clear(){
		a = b = c = d = 0;
		TPHeap = new BinaryHeap(true);
		FNHeap = new BinaryHeap(false);
		FPHeap = new BinaryHeap(true);
		TNHeap = new BinaryHeap(false);
	}
}
