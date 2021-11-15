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