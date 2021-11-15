package utumno.mope.datastructures;
import java.io.Serializable;
import java.util.Hashtable;

@SuppressWarnings("serial")
public class ClassVectorSparse implements Serializable, Comparable<Object> {
	private String name;
	private int size = 0;
	private float threshold = Float.NEGATIVE_INFINITY;
	private Hashtable<Integer, VectorNode> nodes;

	public ClassVectorSparse() {
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public float getThreshold() {
		return threshold;
	}

	public Hashtable<Integer, VectorNode> getNodes() {
		return nodes;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	public void setNodes(Hashtable<Integer, VectorNode> nodes) {
		this.nodes = nodes;
	}
	
	public int compareTo(Object o) {
		if (o instanceof ClassVectorSparse) {
			return (this.name.compareTo(((ClassVectorSparse) o).getName()));
		}
		return 0;
	}
	
	public float similarityWithDocumentVector(DocumentVector documentVector){
		float similarity = 0.0f;
		if(documentVector.getNodes() == null || documentVector.getNodes().isEmpty()){
			return 0.0f;
		}
		if(nodes==null){
			return 0.0f;
		}
		float norm = 0.0f;
		for(VectorNode thatNode : documentVector.getNodes()){
			if(thatNode.getTermId() < 1) continue;
			VectorNode thisNode = nodes.get(thatNode.getTermId());
			if(thisNode!=null){
				similarity += thisNode.getTermWeight() * thatNode.getTermWeight();
				norm += thatNode.getTermWeight() * thatNode.getTermWeight();
			}
		}
		norm = (float) Math.sqrt(norm);
		similarity = similarity / norm;
		return similarity;
	}

}