package utumno.perceptron;

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

public class BatchPerceptron {
	
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
	private float maxF1;
	
	private BinaryHeap PAHeap;
	private BinaryHeap PRHeap;
	private BinaryHeap NAHeap;
	private BinaryHeap NRHeap;
	private DocumentVector PR;
	private DocumentVector NA;

	private static NumberFormat nf = NumberFormat.getInstance();
	static{
		nf.setGroupingUsed(true);
		nf.setMaximumFractionDigits(8);
		nf.setMinimumFractionDigits(8);
		nf.setMaximumIntegerDigits(1);
		nf.setMinimumIntegerDigits(1);
	}
	
	//private ArrayList<Integer> NAList;
	//private ArrayList<Integer> NAListOld;
	//private ArrayList<Integer> PRList;
	//private ArrayList<Integer> PRListOld;
	
	public BatchPerceptron(ClassVector classVector, ArrayList<DocumentVector> documentVectors, int iterations, PrintStream printstream, BufferedWriter outTxt, boolean verbose){
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

	//BatchPerceptron
	public ClassVector executeAdjust(){
		//PRList = new ArrayList<Integer>();
		//PRListOld = new ArrayList<Integer>();
		//NAList = new ArrayList<Integer>();
		//NAListOld = new ArrayList<Integer>();
		
		//int PRold,NAold;
		//boolean posThresh;
		
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
			
			//PRold = PRHeap.size();
			//NAold = NAHeap.size();
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
				
				//////
				float heta = 1.0f;
				//float heta = 1.0 / (float) iteration;
				
				classVector.addWithDocumentVector(PR, heta);
		        classVector.addWithDocumentVector(NA, -heta);
		            
		        classVector.setThreshold(classVector.getThreshold() - heta * ((float)PRHeap.size() - (float)NAHeap.size()));
				//////
				
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
				
				//PRold = PRHeap.size();
				//NAold = NAHeap.size();
				
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
                    //NAList.add(documentVector.getDocId());
                }
            }
            else if(similarity < classVector.getThreshold()){
                if(documentVector.isPositiveExample()){
                    c++;
                    PRHeap.add(new SimilarityNode(similarity,documentVector));
                    //PRList.add(documentVector.getDocId());
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
		PR = null;
		NA = null;
		
		if(PRHeap.size()>1){
			dummy = new ClassVector();
			dummy.setNodes(new VectorNode[classVector.getNodes().length]);
		    Iterator<SimilarityNode> it1 = PRHeap.iterator();
		    while(it1.hasNext()){
		    	dummy.addWithDocumentVector(it1.next().getDocumentVector(),1.0f);
		    }
		    //dummy.divide(((float)PRHeap.size()));
		    PR = new DocumentVector(dummy.getNodes());
	    	PR.setPositiveExample(true);
		}
		else if(PRHeap.size()==1){
			PR = ((SimilarityNode)PRHeap.peek()).getDocumentVector();
		}
		
		if(NAHeap.size()>1){
			dummy = new ClassVector();
		    dummy.setNodes(new VectorNode[classVector.getNodes().length]);
	        Iterator<SimilarityNode> it2 = NAHeap.iterator();
	        while(it2.hasNext()){
	        	dummy.addWithDocumentVector(it2.next().getDocumentVector(),1.0f);
	        }
	        //dummy.divide(((float)NAHeap.size()));
	        NA = new DocumentVector(dummy.getNodes());
	        NA.setPositiveExample(false);
		}
		else if(NAHeap.size()==1){
			NA = ((SimilarityNode)NAHeap.peek()).getDocumentVector();
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
		PAHeap = new BinaryHeap(true);
		PRHeap = new BinaryHeap(false);
		NAHeap = new BinaryHeap(true);
		NRHeap = new BinaryHeap(false);
		
		//PRListOld = PRList;
		//PRList = new ArrayList<Integer>();
		//NAListOld = NAList;
		//NAList = new ArrayList<Integer>();
	}
}
