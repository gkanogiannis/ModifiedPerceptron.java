/*
 *
 * ModifiedPerceptron.java utumno.mope.Adjusting_old
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

public class Adjusting_old {
	
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
	//private float maxAUC;
	
	private BinaryHeap PAHeap;
	private BinaryHeap PRHeap;
	private BinaryHeap NAHeap;
	private BinaryHeap NRHeap;
	private DocumentVector PR;
	private DocumentVector NA;

	private static NumberFormat nf = NumberFormat.getInstance();
	static{
		nf.setGroupingUsed(true);
		nf.setMaximumFractionDigits(4);
		nf.setMinimumFractionDigits(4);
		nf.setMaximumIntegerDigits(1);
		nf.setMinimumIntegerDigits(1);
	}
	
	private ArrayList<String> NAList;
	@SuppressWarnings("unused")
	private ArrayList<String> NAListOld;
	private ArrayList<String> PRList;
	@SuppressWarnings("unused")
	private ArrayList<String> PRListOld;
	
	public Adjusting_old(ClassVector classVector, ArrayList<DocumentVector> documentVectors, int iterations, PrintStream printstream, BufferedWriter outTxt, boolean verbose){
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
	
	
	//Test Adjust (Rotation)
	public ClassVector executeAdjust(){
		PRList = new ArrayList<String>();
		PRListOld = new ArrayList<String>();
		NAList = new ArrayList<String>();
		NAListOld = new ArrayList<String>();
		
		@SuppressWarnings("unused")
		int PRold,NAold;
		@SuppressWarnings("unused")
		boolean posThresh;
		try{
			//Compute F1 for iteration 0 and update max
			clear();
			fillHeaps();
			if(computeF1() > maxF1){
				maxF1 = computeF1();
				maxClassVector = classVector.clone();
			}
			
			float AUC = 1.0f;
			//float AUC = computeAUC();
			//if(AUC>maxAUC){
				//maxAUC=AUC;
				//maxClassVector = classVector.clone();
			//}
			
			if(verbose){
				printstream.println("");
				printstream.println("it="+iteration+"\tF1="+nf.format(computeF1())+"\tAUC="+nf.format(AUC)+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
				outTxt.write(System.getProperty("line.separator"));
				outTxt.write("it="+iteration+"\tF1="+nf.format(computeF1())+"\tAUC="+nf.format(AUC)+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
				outTxt.write(System.getProperty("line.separator"));
			}
			
			PRold = PRHeap.size();
			NAold = NAHeap.size();
			//if(AUC==1.0){
			if(computeF1()==1.0){
				printstream.println("\r\niterations="+iteration);
				printstream.println("maxF1="+nf.format(computeF1())+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
				outTxt.write("\r\niterations="+iteration);
				outTxt.write(System.getProperty("line.separator"));
				outTxt.write("maxF1="+nf.format(computeF1())+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
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
				
				//Compute PR,NA Centroids
				computeMiscclasifiedCentroids();
				
				float beta = 0.0f;
				ClassVector dummy = null;
				DocumentVector e = null;
				
				if(!NAHeap.isEmpty() && !PRHeap.isEmpty()){
					dummy = new ClassVector();
					dummy.setNodes(new VectorNode[classVector.getNodes().length]);
					dummy.addWithDocumentVector(PR, 1.0f);
					dummy.addWithDocumentVector(NA, -1.0f);
					e = new DocumentVector(dummy.getNodes());
					
					//float b1 = classVector.similarityWithDocumentVector(PR);
					//float b2 = classVector.similarityWithDocumentVector(NA);
					//float b3 = PR.similarityWithDocumentVector(PR);
		            //float b4 = NA.similarityWithDocumentVector(NA);
		            //float b5 = 2.0 * PR.similarityWithDocumentVector(NA);
		            //beta = (b2 - b1) / (b3 + b4 - b5);
					beta = - (classVector.similarityWithDocumentVector(e))/(e.similarityWithDocumentVector(e));
					
		            //classVector.addWithDocumentVector(PR, beta);
		            //classVector.addWithDocumentVector(NA, -beta);
		            classVector.addWithDocumentVector(e, beta);
		            
		            classVector.setPosThresholdVector(PR.clone());
		            classVector.setNegThresholdVector(NA.clone());
					classVector.setThreshold(classVector.similarityWithDocumentVector(PR));
				}
				else if(PRHeap.isEmpty()){
					dummy = new ClassVector();
					dummy.setNodes(new VectorNode[classVector.getNodes().length]);
					dummy.addWithDocumentVector(classVector.getPosThresholdVector(), 1.0f);
					dummy.addWithDocumentVector(NA, -1.0f);
					e = new DocumentVector(dummy.getNodes());
					
					//float b1 = classVector.similarityWithDocumentVector(classVector.getPosThresholdVector());
					//float b2 = classVector.similarityWithDocumentVector(NA);
		            //float b3 = classVector.getPosThresholdVector().similarityWithDocumentVector(classVector.getPosThresholdVector());
		            //float b4 = NA.similarityWithDocumentVector(NA);
		            //float b5 = 2.0 * classVector.getPosThresholdVector().similarityWithDocumentVector(NA);
		            //beta = (b2 - b1) / (b3 + b4 - b5);
					beta = - (classVector.similarityWithDocumentVector(e))/(e.similarityWithDocumentVector(e));
					
		            //classVector.addWithDocumentVector(classVector.getPosThresholdVector(), beta);
		            //classVector.addWithDocumentVector(NA, -beta);
					classVector.addWithDocumentVector(e, beta);
		            
		            classVector.setNegThresholdVector(NA.clone());
					classVector.setThreshold(classVector.similarityWithDocumentVector(NA));
				}
				else if(NAHeap.isEmpty()){
					dummy = new ClassVector();
					dummy.setNodes(new VectorNode[classVector.getNodes().length]);
					dummy.addWithDocumentVector(PR, 1.0f);
					dummy.addWithDocumentVector(classVector.getNegThresholdVector(), -1.0f);
					e = new DocumentVector(dummy.getNodes());
					
					//float b1 = classVector.similarityWithDocumentVector(PR);
					//float b2 = classVector.similarityWithDocumentVector(classVector.getNegThresholdVector());
		            //float b3 = PR.similarityWithDocumentVector(PR);
		            //float b4 = classVector.getNegThresholdVector().similarityWithDocumentVector(classVector.getNegThresholdVector());
		            //float b5 = 2.0 * PR.similarityWithDocumentVector(classVector.getNegThresholdVector());
		            //beta = (b2 - b1) / (b3 + b4 - b5);
					beta = - (classVector.similarityWithDocumentVector(e))/(e.similarityWithDocumentVector(e));
					
		            //classVector.addWithDocumentVector(PR, beta);
		            //classVector.addWithDocumentVector(classVector.getNegThresholdVector(), -beta);
					classVector.addWithDocumentVector(e, beta);
					
		            classVector.setPosThresholdVector(PR.clone());
					classVector.setThreshold(classVector.similarityWithDocumentVector(PR));
				}
				
				//Compute F1 for iteration i and update max
		        clear();
		        fillHeaps();
				if(computeF1() > maxF1){
					maxF1 = computeF1();
					maxClassVector = classVector.clone();
				}
				
				AUC = 1.0f;
				//AUC = computeAUC();
				//if(AUC>maxAUC) {
					//maxAUC=AUC;
					//maxClassVector = classVector.clone();
				//}
				
				if(verbose){
					printstream.println("F1="+nf.format(computeF1())+"\tAUC="+nf.format(AUC)+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
					outTxt.write("F1="+nf.format(computeF1())+"\tAUC="+nf.format(AUC)+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
					outTxt.write(System.getProperty("line.separator"));
				}
			    /*
				if(NAHeap.size()<=NAold){
					System.out.print(" NA:+"); 
				}
				else{
					System.out.print(" NA:-");
				}
					
				boolean ok1 = true;
				int lala1 = 0;
				for(Integer i : NAList){
					if(!NAListOld.contains(i)){
						ok1 = false;
						lala1++;
					}
				}
				if(ok1){
					System.out.print("+");
				}
				else{
					System.out.print("-"+"("+lala1+")");
				}
			
				if(PRHeap.size()<=PRold){
					System.out.print(" PR:+");
				}
				else{
					System.out.print(" PR:-");
				}
					
				boolean ok2 = true;
				int lala2 = 0;
				for(Integer i : PRList){
					if(!PRListOld.contains(i)){
						ok2 = false;
						lala2++;
					}
				}
				if(ok2){
					System.out.println("+");
				}
				else{
					System.out.println("-"+"("+lala2+")");
				}
				*/
				
				PRold = PRHeap.size();
				NAold = NAHeap.size();
				
				//if(AUC==1.0){
				if(computeF1()==1.0 || (NAHeap.size()==1 && PRHeap.size()==0) || (NAHeap.size()==0 && PRHeap.size()==1) || (NAHeap.size()==1 && PRHeap.size()==1)){
					break;
				}
			}
			while(iterations>0);
			
			classVector = maxClassVector;
			//Compute F1 and heaps for maxclassvector
	        clear();
	        fillHeaps();
	        
	        AUC = 1.0f;
	        //AUC = computeAUC();
			//if(AUC>maxAUC) maxAUC=AUC;
			
			if(computeF1()>maxF1) maxF1=computeF1();
			
			float maxAUC = 1.0f;
			
			printstream.println("\r\niterations="+iteration);
			printstream.println("maxF1="+nf.format(computeF1())+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
			outTxt.write("\r\niterations="+iteration);
			outTxt.write(System.getProperty("line.separator"));
			outTxt.write("maxF1="+nf.format(computeF1())+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
			outTxt.write(System.getProperty("line.separator"));
			
			return maxClassVector;
		}
		catch(Exception e){
			throw new Error(e);
		}
	}
	
	
	/*
	//Current adjust
	public ClassVector executeAdjust(){
		PRList = new ArrayList<Integer>();
		PRListOld = new ArrayList<Integer>();
		NAList = new ArrayList<Integer>();
		NAListOld = new ArrayList<Integer>();
		
		int PRold,NAold;
		boolean posThresh;
		try{
			//Compute F1 for iteration 0 and update max
			clear();
			fillHeaps();
			if(computeF1() > maxF1){
				maxF1 = computeF1();
				maxClassVector = classVector.clone();
			}
			
			if(verbose){
				printstream.println("");
				printstream.println("it="+iteration+"\tF1="+nf.format(computeF1())+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
				outTxt.write(System.getProperty("line.separator"));
				outTxt.write("it="+iteration+"\tF1="+nf.format(computeF1())+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
				outTxt.write(System.getProperty("line.separator"));
			}
			
			PRold = PRHeap.size();
			NAold = NAHeap.size();
			if(computeF1()==1.0){
				printstream.println("\r\niterations="+iteration);
				printstream.println("maxF1="+nf.format(computeF1())+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
				outTxt.write("\r\niterations="+iteration);
				outTxt.write(System.getProperty("line.separator"));
				outTxt.write("maxF1="+nf.format(computeF1())+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
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
				
				//Compute PR,NA Centroids
				computeMiscclasifiedCentroids();
				
				float beta = 0.0;
				//ClassVector dummy = null;
				//DocumentVector e = null;
				
				if(classVector.getThresholdVector().isPositiveExample()){
					if(NAHeap.isEmpty()){
						if(classVector.getNegThresholdVector() != null){
							//System.out.println("B");
							classVector.setThresholdVector(classVector.getNegThresholdVector());
							//classVector.setThreshold(classVector.similarityWithDocumentVector(classVector.getThresholdVector()));
							continue;
						}
						else{
							break;
						}
					}
					
		            float b1 = classVector.similarityWithDocumentVector(NA);
		            float b2 = classVector.similarityWithDocumentVector(classVector.getPosThresholdVector());
		            if(b1==b2){
		            	if(classVector.getNegThresholdVector() != null){
		            		//System.out.println("b");
		            		classVector.setNegThresholdVector(NA);
		            		classVector.setThresholdVector(classVector.getNegThresholdVector());
							//classVector.setThreshold(classVector.similarityWithDocumentVector(classVector.getThresholdVector()));
							continue;
						}
						else{
							break;
						}
		            }
		            float b3 = classVector.getPosThresholdVector().similarityWithDocumentVector(classVector.getPosThresholdVector());
		            float b4 = NA.similarityWithDocumentVector(NA);
		            float b5 = 2.0 * NA.similarityWithDocumentVector(classVector.getPosThresholdVector());
		            beta = (b1 - b2) / (b3 + b4 - b5);
		            
		            classVector.addWithDocumentVector(NA, -beta);
		            classVector.addWithDocumentVector(classVector.getPosThresholdVector(), beta);
		            
		            classVector.setNegThresholdVector(NA);
					classVector.setThresholdVector(classVector.getNegThresholdVector());
					classVector.setThreshold(classVector.similarityWithDocumentVector(classVector.getThresholdVector()));

					//System.out.print("-beta="+(beta>=0.0?"+"+nf.format(beta):nf.format(beta))+"  ");
					posThresh = true;
				}
				else{
					if(PRHeap.isEmpty()){
						if(classVector.getPosThresholdVector() != null){
							//System.out.println("A");
							classVector.setThresholdVector(classVector.getPosThresholdVector());
							//classVector.setThreshold(classVector.similarityWithDocumentVector(classVector.getThresholdVector()));
							continue;
						}
						else{
							break;
						}
					}
					
					float b1 = classVector.similarityWithDocumentVector(PR);
		            float b2 = classVector.similarityWithDocumentVector(classVector.getNegThresholdVector());
		            if(b1==b2){
		            	if(classVector.getPosThresholdVector() != null){
		            		//System.out.println("a");
		            		classVector.setPosThresholdVector(PR);
		            		classVector.setThresholdVector(classVector.getPosThresholdVector());
							//classVector.setThreshold(classVector.similarityWithDocumentVector(classVector.getThresholdVector()));
							continue;
						}
						else{
							break;
						}
		            }
		            float b3 = classVector.getNegThresholdVector().similarityWithDocumentVector(classVector.getNegThresholdVector());
		            float b4 = PR.similarityWithDocumentVector(PR);
		            float b5 = 2.0 * PR.similarityWithDocumentVector(classVector.getNegThresholdVector());
		            beta = (b1 - b2) / (b3 + b4 - b5);
		            
		            classVector.addWithDocumentVector(PR, -beta);
		            classVector.addWithDocumentVector(classVector.getNegThresholdVector(), beta);
		            
		            classVector.setPosThresholdVector(PR);
					classVector.setThresholdVector(classVector.getPosThresholdVector());
					classVector.setThreshold(classVector.similarityWithDocumentVector(classVector.getThresholdVector()));
					
					//System.out.print("+beta="+(beta>=0.0?"+"+nf.format(beta):nf.format(beta))+"  ");
					posThresh = false;
				}
				
				//Compute F1 for iteration i and update max
		        clear();
		        fillHeaps();
				if(computeF1() > maxF1){
					maxF1 = computeF1();
					maxClassVector = classVector.clone();
				}
				
				if(verbose){
					printstream.println("F1="+nf.format(computeF1())+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
					outTxt.write("F1="+nf.format(computeF1())+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
					outTxt.write(System.getProperty("line.separator"));
				}
			
				/*
				if(posThresh){
					if(NAHeap.size()<=NAold){
						System.out.print(" +");
					}
					else{
						System.out.print(" -");
					}
					
					boolean ok = true;
					int lala1 = 0;
					for(Integer i : NAList){
						if(!NAListOld.contains(i)){
							ok = false;
							lala1++;
						}
					}
					if(ok){
						System.out.println(" +");
					}
					else{
						System.out.println(" -"+" "+lala1);
					}
				}
				else{
					if(PRHeap.size()<=PRold){
						System.out.print(" +");
					}
					else{
						System.out.print(" -");
					}
					
					boolean ok = true;
					int lala2 = 0;
					for(Integer i : PRList){
						if(!PRListOld.contains(i)){
							ok = false;
							lala2++;
						}
					}
					if(ok){
						System.out.println(" +");
					}
					else{
						System.out.println(" -"+" "+lala2);
					}
				}
				/
				
				PRold = PRHeap.size();
				NAold = NAHeap.size();
				
				if(computeF1()==1.0){
					break;
				}
			}
			while(iterations>0);
			
			classVector = maxClassVector;
			//Compute F1 and heaps for maxclassvector
	        clear();
	        fillHeaps();
			
			printstream.println("\r\niterations="+iteration);
			printstream.println("maxF1="+nf.format(computeF1())+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
			outTxt.write("\r\niterations="+iteration);
			outTxt.write(System.getProperty("line.separator"));
			outTxt.write("maxF1="+nf.format(computeF1())+"\tPA="+PAHeap.size()+"\tPR="+PRHeap.size()+"\tNA="+NAHeap.size()+"\tNR="+NRHeap.size());
			outTxt.write(System.getProperty("line.separator"));
			
			return maxClassVector;
		}
		catch(Exception e){
			throw new Error(e);
		}
	}
	*/
	
	/*
	//Palios tropos (W'= W+alpha*CPR -beta*CNA, margin)
	public ClassVector executeAdjust(){
		PRList = new ArrayList<Integer>();
		PRListOld = new ArrayList<Integer>();
		NAList = new ArrayList<Integer>();
		NAListOld = new ArrayList<Integer>();
		
		int PRold,NAold;
		try{
			//Compute F1 for iteration 0 and update max
			clear();
			fillHeaps();
			if(computeF1() > maxF1){
				maxF1 = computeF1();
				maxClassVector = classVector.clone();
			}
			
			System.err.println("-"+iterations+"\t                 F1="+nf.format(computeF1())+" PA="+PAHeap.size()+" PR="+PRHeap.size()+" NA="+NAHeap.size()+" NR="+NRHeap.size()+" Thresh="+(classVector.getThresholdVector().isPositiveExample()?"Pos":"Neg"));
			PRold = PRHeap.size();
			NAold = NAHeap.size();
			if(computeF1()==1.0){
				System.err.println("maxF1="+maxF1);
				return maxClassVector;
			}
			
			do{
				iterations--;
				
				System.err.print("-"+iterations+"\t");
				

				int PRx  = Math.min(a,(int)(((float)3*c) * Math.exp(-1.0* (((float)c) / ((float)a)))));
				int NAx = Math.min(d,(int)(((float)3*b) * Math.exp(-1.0* (((float)b) / ((float)d)))));
				a -= PRx;
				c += PRx;
				b += NAx;
				d -= NAx;
				
				float alpha = (float)c / (float)(a + c);
				float beta  = (float)b / (float)(a + b);
				//float alpha = (float)C / ((float)(A + C)*(float)(A + C));
				//float beta  = (float)B / ((float)(A + B)*(float)(A + B));
				
				//Compute PR,NA Centroids
				computeMiscclasifiedCentroids();
						
		            classVector.addWithDocumentVector(PR, alpha);
		            classVector.addWithDocumentVector(NA, -beta);
		            
		           
					//classVector.setThreshold(classVector.similarityWithDocumentVector(classVector.getNegThresholdVector()));
					


				
				//Compute F1 for iteration i and update max
		        clear();
		        fillHeaps();
				if(computeF1() > maxF1){
					maxF1 = computeF1();
					maxClassVector = classVector.clone();
				}
				
				System.err.println("F1="+nf.format(computeF1())+" PA="+PAHeap.size()+" PR="+PRHeap.size()+" NA="+NAHeap.size()+" NR="+NRHeap.size()+" Thresh="+(classVector.getThresholdVector().isPositiveExample()?"Pos":"Neg"));
				
				if(computeF1()==1.0){
					break;
				}
			}
			while(iterations>0);
			
			System.err.println("maxF1="+maxF1);
			
			return maxClassVector;
		}
		catch(Exception e){
			throw new Error(e);
		}
	}
	*/
	
	private void fillHeaps(){
		for(DocumentVector documentVector : documentVectors){
			float similarity = classVector.similarityWithDocumentVector(documentVector);
			
			if(similarity >= classVector.getThreshold()){	
                if(documentVector.isPositiveExample()){
                    a++;
                    PAHeap.add(new SimilarityNode(similarity,documentVector));
                }
                else{
                    b++;
                    NAHeap.add(new SimilarityNode(similarity,documentVector));
                    NAList.add(documentVector.getDocId());
                }
            }
            else if(similarity < classVector.getThreshold()){
                if(documentVector.isPositiveExample()){
                    c++;
                    PRHeap.add(new SimilarityNode(similarity,documentVector));
                    PRList.add(documentVector.getDocId());
                }
                else{
                    d++;
                    NRHeap.add(new SimilarityNode(similarity,documentVector));
                }
            }
            
		}
	}
	
	@SuppressWarnings("unchecked")
	private void computeMiscclasifiedCentroids(){
		ClassVector dummy;
		
		if(PRHeap.size()>1){
			dummy = new ClassVector();
			dummy.setNodes(new VectorNode[classVector.getNodes().length]);
		    Iterator<SimilarityNode> it1 = PRHeap.iterator();
		    while(it1.hasNext()){
		    	dummy.addWithDocumentVector(it1.next().getDocumentVector(),1.0f);
		    }
		    dummy.divide(((float)PRHeap.size()));
		    PR = new DocumentVector(dummy.getNodes());
	    	PR.setPositiveExample(true);
		}
		else if(PRHeap.size()==1){
			PR = ((SimilarityNode)PRHeap.peek()).getDocumentVector().clone();
		}
		
		if(NAHeap.size()>1){
			dummy = new ClassVector();
		    dummy.setNodes(new VectorNode[classVector.getNodes().length]);
	        Iterator<SimilarityNode> it2 = NAHeap.iterator();
	        while(it2.hasNext()){
	        	dummy.addWithDocumentVector(it2.next().getDocumentVector(),1.0f);
	        }
	        dummy.divide(((float)NAHeap.size()));
	        NA = new DocumentVector(dummy.getNodes());
	        NA.setPositiveExample(false);
		}
		else if(NAHeap.size()==1){
			NA = ((SimilarityNode)NAHeap.peek()).getDocumentVector().clone();
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
	
	/*
	@SuppressWarnings("unchecked")
	private float computeAUC(){
		ScoredPrecisionRecallEvaluation spre = new ScoredPrecisionRecallEvaluation();
		Iterator<SimilarityNode> it = PAHeap.iterator();
	    while(it.hasNext()){
	    	SimilarityNode sNode = it.next();
	    	spre.addCase(sNode.getDocumentVector().isPositiveExample(),sNode.getSimilarity());
	    }
	    it = PRHeap.iterator();
	    while(it.hasNext()){
	    	SimilarityNode sNode = it.next();
	    	spre.addCase(sNode.getDocumentVector().isPositiveExample(),sNode.getSimilarity());
	    }
	    it = NAHeap.iterator();
	    while(it.hasNext()){
	    	SimilarityNode sNode = it.next();
	    	spre.addCase(sNode.getDocumentVector().isPositiveExample(),sNode.getSimilarity());
	    }
	    it = NRHeap.iterator();
	    while(it.hasNext()){
	    	SimilarityNode sNode = it.next();
	    	spre.addCase(sNode.getDocumentVector().isPositiveExample(),sNode.getSimilarity());
	    }
	    return spre.areaUnderRocCurve(false);
	}
	*/
	
	public void clear(){
		a = b = c = d = 0;
		PAHeap = new BinaryHeap(true);
		PRHeap = new BinaryHeap(false);
		NAHeap = new BinaryHeap(true);
		NRHeap = new BinaryHeap(false);
		
		PRListOld = PRList;
		PRList = new ArrayList<String>();
		NAListOld = NAList;
		NAList = new ArrayList<String>();
	}
}
