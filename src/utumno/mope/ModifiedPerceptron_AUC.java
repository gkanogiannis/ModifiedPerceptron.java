/*
 *
 * ModifiedPerceptron.java utumno.mope.ModifiedPerceptron_AUC
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
import utumno.mope.utils.ext.ScoredPrecisionRecallEvaluation;

public class ModifiedPerceptron_AUC {
	
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
	private float maxAUC = Float.NEGATIVE_INFINITY;
	
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
	
	public ModifiedPerceptron_AUC(ClassVector classVector, ArrayList<DocumentVector> documentVectors, int iterations, PrintStream printstream, BufferedWriter outTxt, boolean verbose){
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

	public float getMaxAUC() {
		return maxAUC;
	}
		
	//Test Adjust (Rotation)
	public ClassVector executeAdjust(){
		try{
			//Compute AUC for iteration 0 and update max
			clear();
			fillHeaps();
			float AUC = computeAUC();
			if(AUC > maxAUC){
				maxAUC = AUC;
				maxClassVector = classVector.clone();
			}
			
			if(verbose){
				printstream.println("");
				printstream.println("it="+iteration+"\tAUC="+nf.format(AUC)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
				outTxt.write(System.getProperty("line.separator"));
				outTxt.write("it="+iteration+"\tAUC="+nf.format(AUC)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
				outTxt.write(System.getProperty("line.separator"));
			}
			
			if(FNHeap.size() == 0 && FPHeap.size() == 0){
				printstream.println("\r\niterations="+iteration);
				printstream.println("maxAUC="+nf.format(AUC)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
				outTxt.write("\r\niterations="+iteration);
				outTxt.write(System.getProperty("line.separator"));
				outTxt.write("maxAUC="+nf.format(AUC)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
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
				
				//Compute AUC for iteration i and update max
		        clear();
		        fillHeaps();
		        AUC = computeAUC();
				if(AUC > maxAUC){
					maxAUC = AUC;
					maxClassVector = classVector.clone();
				}
				
				if(verbose){
					printstream.println("\tAUC="+nf.format(AUC)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
					outTxt.write("\tAUC="+nf.format(AUC)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
					outTxt.write(System.getProperty("line.separator"));
				}
				
				//If perfect classification
				if(FNHeap.size() == 0 && FPHeap.size() == 0){// || (NAHeap.size()==1 && PRHeap.size()==0) || (NAHeap.size()==0 && PRHeap.size()==1) || (NAHeap.size()==1 && PRHeap.size()==1)){
					maxAUC = AUC;
					maxClassVector = classVector.clone();
					break;
				}
			}
			while(iterations>0);
			
			classVector = maxClassVector;
			//Compute AUC and heaps for maxclassvector
	        clear();
	        fillHeaps();
	        AUC = computeAUC();
			
			printstream.println("\r\niterations="+iteration);
			printstream.println("maxAUC="+nf.format(AUC)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
			outTxt.write("\r\niterations="+iteration);
			outTxt.write(System.getProperty("line.separator"));
			outTxt.write("maxAUC="+nf.format(AUC)+"\tTP="+TPHeap.size()+"\tFP="+FPHeap.size()+"\tFN="+FNHeap.size()+"\tTN="+TNHeap.size());
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
			
			if(similarity >= classVector.getThreshold()){	
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
	
	private float computeAUC(){
		ScoredPrecisionRecallEvaluation spre = new ScoredPrecisionRecallEvaluation();
		Iterator<SimilarityNode> it = TPHeap.iterator();
	    while(it.hasNext()){
	    	SimilarityNode sNode = it.next();
	    	spre.addCase(sNode.getDocumentVector().isPositiveExample(),sNode.getSimilarity());
	    }
	    it = FNHeap.iterator();
	    while(it.hasNext()){
	    	SimilarityNode sNode = it.next();
	    	spre.addCase(sNode.getDocumentVector().isPositiveExample(),sNode.getSimilarity());
	    }
	    it = FPHeap.iterator();
	    while(it.hasNext()){
	    	SimilarityNode sNode = it.next();
	    	spre.addCase(sNode.getDocumentVector().isPositiveExample(),sNode.getSimilarity());
	    }
	    it = TNHeap.iterator();
	    while(it.hasNext()){
	    	SimilarityNode sNode = it.next();
	    	spre.addCase(sNode.getDocumentVector().isPositiveExample(),sNode.getSimilarity());
	    }
	    return (float)spre.areaUnderRocCurve(false);
	}
	
	public void clear(){
		a = b = c = d = 0;
		TPHeap = new BinaryHeap(true);
		FNHeap = new BinaryHeap(false);
		FPHeap = new BinaryHeap(true);
		TNHeap = new BinaryHeap(false);
	}
}
