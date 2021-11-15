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