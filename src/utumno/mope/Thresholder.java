package utumno.mope;
import java.util.ArrayList;

import utumno.mope.datastructures.BinaryHeap;
import utumno.mope.datastructures.ClassVector;
import utumno.mope.datastructures.DocumentVector;
import utumno.mope.datastructures.SimilarityNode;

public class Thresholder {
	
	private float threshold = Float.NEGATIVE_INFINITY;
	private DocumentVector posThresholdVector = null;
	private DocumentVector negThresholdVector = null;
	private ClassVector classVector;
	private ArrayList<DocumentVector> documentVectors;
	private BinaryHeap minHeap;
	private BinaryHeap maxHeap;
	private int a = 0;
	private int b = 0;
	private int c = 0;
	
	public Thresholder(ClassVector classVector, ArrayList<DocumentVector> documentVectors){
		this.classVector = classVector;
		this.documentVectors = documentVectors;
		minHeap = new BinaryHeap(true);
		maxHeap = new BinaryHeap(false);
	}
	
	public float getThreshold() {
		return threshold;
	}

	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	public ClassVector getClassVector() {
		return classVector;
	}

	public ArrayList<DocumentVector> getDocumentVectors() {
		return documentVectors;
	}

	public void setClassVector(ClassVector classVector) {
		this.classVector = classVector;
	}

	public void setDocumentVectors(ArrayList<DocumentVector> documentVectors) {
		this.documentVectors = documentVectors;
	}
	
	public void computeThreshold(int mode){//0 recall=1, 1 precison=1, 2 SCut
		if(mode==0){
			//recall=1
			try{
				clear();
				for(DocumentVector documentVector : documentVectors){
					SimilarityNode sNode = new SimilarityNode(classVector.similarityWithDocumentVector(documentVector),documentVector);
					minHeap.add(sNode);
				}
				SimilarityNode sNode = null;
				SimilarityNode previousSNode = null;
				while(!minHeap.isEmpty()){
					previousSNode = sNode;
					sNode = (SimilarityNode)minHeap.pop();
					if(sNode.getDocumentVector().isPositiveExample()){
						posThresholdVector = sNode.getDocumentVector();
						if(previousSNode != null){
							negThresholdVector = previousSNode.getDocumentVector();
							threshold = (sNode.getSimilarity()+previousSNode.getSimilarity())/2.0f;
						}
						else{
							threshold = sNode.getSimilarity();
						}
						break;
					}
				}
				classVector.setThreshold(threshold);
		        classVector.setPosThresholdVector(posThresholdVector);
		        classVector.setNegThresholdVector(negThresholdVector);
		        return;
			}
			catch(Exception e){
				throw new Error(e);
			}
		}
		else if(mode==1){
			//precision=1
			try{
				clear();
				for(DocumentVector documentVector : documentVectors){
					SimilarityNode sNode = new SimilarityNode(classVector.similarityWithDocumentVector(documentVector),documentVector);
					maxHeap.add(sNode);
				}
				SimilarityNode sNode = null;
				SimilarityNode previousSNode = null;
				while(!maxHeap.isEmpty()){
					previousSNode = sNode;
					sNode = (SimilarityNode)maxHeap.pop();
					if(!sNode.getDocumentVector().isPositiveExample()){
						posThresholdVector = sNode.getDocumentVector();
						negThresholdVector = previousSNode.getDocumentVector();
						threshold = (sNode.getSimilarity()+previousSNode.getSimilarity())/2.0f;
						break;
					}
				}
				classVector.setThreshold(threshold);
		        classVector.setPosThresholdVector(posThresholdVector);
		        classVector.setNegThresholdVector(negThresholdVector);
		        return;
			}
			catch(Exception e){
				throw new Error(e);
			}
		}
		/*
		else if(mode==2){
			//Scut
			try{
				clear();
				for(DocumentVector documentVector : documentVectors){
					SimilarityNode sNode = new SimilarityNode(classVector.similarityWithDocumentVector(documentVector),documentVector);
					minHeap.add(sNode);
					if(documentVector.isPositiveExample()){
						a++;
					}
					else{
						b++;
					}
				}
				float maxF1 = Double.NEGATIVE_INFINITY;
				SimilarityNode maxNode = (SimilarityNode)minHeap.pop();
				SimilarityNode previousNode = maxNode;
				for(int i = documentVectors.size()-1; i>0; i--){
					SimilarityNode sNode = (SimilarityNode)minHeap.pop();
					if(sNode.getSimilarity()==previousNode.getSimilarity()){
						continue;
					}
					previousNode = sNode;
					if(sNode.getDocumentVector().isPositiveExample()){
						a--;
						c++;
					}
					else{
						b--;
						d++;
					}
					float currentF1 = computeF1();
					if(currentF1>=maxF1){
						maxF1 = currentF1;
						maxNode = sNode;
					}
				}
				thresholdVector = maxNode.getDocumentVector();
				threshold = maxNode.getSimilarity();
				classVector.setThreshold(threshold);
				if(thresholdVector.isPositiveExample()){
					classVector.setPosThresholdVector(thresholdVector);
					classVector.setThresholdVector(classVector.getPosThresholdVector());
				}
				else{
					classVector.setNegThresholdVector(thresholdVector);
					classVector.setThresholdVector(classVector.getNegThresholdVector());
				}
				return;
			}
			catch(Exception e){
				throw new Error(e);
			}	
		}
		else{
			//random
			try{
				clear();
				for(DocumentVector documentVector : documentVectors){
					SimilarityNode sNode = new SimilarityNode(classVector.similarityWithDocumentVector(documentVector),documentVector);
					maxHeap.add(sNode);
				}
				int i=0;
				SimilarityNode sNode = null;
				while(i<mode){
					sNode = (SimilarityNode)maxHeap.pop();
					i++;
				}
				
				thresholdVector = sNode.getDocumentVector();
				threshold = sNode.getSimilarity();
				classVector.setThreshold(threshold);
				if(thresholdVector.isPositiveExample()){
					classVector.setPosThresholdVector(thresholdVector);
					classVector.setThresholdVector(classVector.getPosThresholdVector());
				}
				else{
					classVector.setNegThresholdVector(thresholdVector);
					classVector.setThresholdVector(classVector.getNegThresholdVector());
				}
				return;
			}
			catch(Exception e){
				throw new Error(e);
			}
		}
		*/
	}
	
	public void clear(){
		a = b = c = 0;
		minHeap = new BinaryHeap(true);
		maxHeap = new BinaryHeap(false);
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
		if(p+r!=0.0){
			F1 = 2.0f*p*r / (p+r);
		}
		return F1;
	}
	
}
