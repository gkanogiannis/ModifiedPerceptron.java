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