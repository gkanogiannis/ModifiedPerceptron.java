package utumno.mope.datastructures;
import java.io.Serializable;

@SuppressWarnings("serial")
public class VectorNodeLight implements Serializable,Comparable<Object> {
	private int termId;
    private float termWeight;
    
	public VectorNodeLight(int id, float weight) {
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
	
	public VectorNodeLight clone(){
		return new VectorNodeLight(termId, termWeight);
	}
    
	public String toString(){
		return "{" + this.getTermId() + "," + this.getTermWeight() + "}";
	}

	public int compareTo(Object o) {
		if(o instanceof VectorNodeLight){
            if(getTermId() > ((VectorNodeLight)o).getTermId()){
                return 1;
            }
            else if(getTermId() < ((VectorNodeLight)o).getTermId()){
                return -1;
            }
        }
        return 0;
	}
	
}