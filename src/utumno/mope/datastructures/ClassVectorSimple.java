package utumno.mope.datastructures;
import java.io.Serializable;

@SuppressWarnings("serial")
public class ClassVectorSimple implements Serializable, Comparable<Object> {
	private String name;
	private int size = 0;
	private float threshold = Float.NEGATIVE_INFINITY;
	private float[] nodes;

	public ClassVectorSimple() {
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

	public float[] getNodes() {
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
	
	public void setNodes(float[] nodes) {
		this.nodes = nodes;
	}

	public int compareTo(Object o) {
		if (o instanceof ClassVectorSimple) {
			return (this.name.compareTo(((ClassVectorSimple) o).getName()));
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
			if(thatNode.getTermId() > nodes.length) continue;
			float thisNode = nodes[thatNode.getTermId()-1];
			similarity += thisNode*thatNode.getTermWeight();
			norm += thatNode.getTermWeight() * thatNode.getTermWeight();
		}
		norm = (float) Math.sqrt(norm);
		similarity = similarity / norm;
		return similarity;
	}
	
}