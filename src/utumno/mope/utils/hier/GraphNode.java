package utumno.mope.utils.hier;

import java.util.Arrays;
import java.util.TreeSet;

public class GraphNode implements Comparable<Object>{
	private String nodeCode;
	private TreeSet<GraphNode> parents;
	private TreeSet<GraphNode> children;
	private Integer[] positiveExamples; 
	private Integer[] negativeExamples;
	private Integer[] positiveExamplesSubtree; 
	private Integer[] negativeExamplesSubtree;
	private boolean leaf;
	private int level;
	private int a;
	private int b;
	private int c;
	
	public GraphNode(String code){
		setNodeCode(code);
		setParents(new TreeSet<GraphNode>());
		setChildren(new TreeSet<GraphNode>());
		setPositiveExamples(null);
		setNegativeExamples(null);
		setLeaf(true);
		setLevel(-1);
	}
	
	public Integer[] getPositiveExamples() {
		return positiveExamples;
	}
	public void setPositiveExamples(Integer[] positiveExamples) {
		this.positiveExamples = positiveExamples;
	}
	public Integer[] getNegativeExamples() {
		return negativeExamples;
	}
	public void setNegativeExamples(Integer[] negativeExamples) {
		this.negativeExamples = negativeExamples;
	}
	public Integer[] getPositiveExamplesSubtree() {
		return positiveExamplesSubtree;
	}
	public void setPositiveExamplesSubtree(Integer[] positiveExamplesSubtree) {
		this.positiveExamplesSubtree = positiveExamplesSubtree;
	}
	public Integer[] getNegativeExamplesSubtree() {
		return negativeExamplesSubtree;
	}
	public void setNegativeExamplesSubtree(Integer[] negativeExamplesSubtree) {
		this.negativeExamplesSubtree = negativeExamplesSubtree;
	}
	public String getNodeCode() {
		return nodeCode;
	}
	public void setNodeCode(String nodeCode) {
		this.nodeCode = nodeCode;
	}
	public TreeSet<GraphNode> getParents() {
		return parents;
	}
	public void setParents(TreeSet<GraphNode> parents) {
		this.parents = parents;
	}
	public TreeSet<GraphNode> getChildren() {
		return children;
	}
	public void setChildren(TreeSet<GraphNode> children) {
		this.children = children;
	}
	public boolean isLeaf() {
		return leaf;
	}
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getA() {
		return a;
	}
	public void setA(int a) {
		this.a = a;
	}
	public int getB() {
		return b;
	}
	public void setB(int b) {
		this.b = b;
	}
	public int getC() {
		return c;
	}
	public void setC(int c) {
		this.c = c;
	}

	public int countAllLeafDescendants(){
		try{
			int ret = 0;
			if(children == null || children.isEmpty()){ 
				return ret;
			}
			else{
				for(GraphNode descendant : getAllDescendants()){
					if(descendant.isLeaf()){
						ret++;
					}
				}
			}
			return ret;
		}
		catch(Exception e){
			e.printStackTrace();
			return 0;
		}
	}
	
	public int countAllLeafChildren(){
		try{
			int ret = 0;
			if(children == null || children.isEmpty()){
				return ret;
			}
			else{
				for(GraphNode child : children){
					if(child.isLeaf()){
						ret++;
					}	
				}
			}
			return ret;
		}
		catch(Exception e){
			e.printStackTrace();
			return 0;
		}
	}
	
	public TreeSet<GraphNode> getAllAncestors(){
		try{
			TreeSet<GraphNode> ret = new TreeSet<GraphNode>();
			if(parents!=null){
				ret.addAll(parents);
				for(GraphNode parent : parents){
					ret.addAll(parent.getAllAncestors());
				}
			}
			return ret;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public TreeSet<GraphNode> getAllDescendants(){
		try{
			TreeSet<GraphNode> ret = new TreeSet<GraphNode>();
			for(GraphNode child : children){
				ret.add(child);
				ret.addAll(child.getAllDescendants());
			}
			return ret;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public TreeSet<GraphNode> getCoverage(){
		try{
			TreeSet<GraphNode> ret = getAllDescendants();
			ret.add(this);
			return ret;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public GraphNode findDescendant(GraphNode nodeToFind){
		try{
			if(children.contains(nodeToFind)){
				for(GraphNode child1 : children){
					if(child1.equals(nodeToFind.getNodeCode())){
						return child1;
					}
				}
			}
			else{
				for(GraphNode child2 : children){
					return child2.findDescendant(nodeToFind);
				}
			}
			return null;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public TreeSet<GraphNode> getAllSiblings(){
		try{
			TreeSet<GraphNode> ret = new TreeSet<GraphNode>();
			for(GraphNode parent : getParents()){
				for(GraphNode sibling : parent.getChildren()){
					if(this.equals(sibling)) continue;
					ret.add(sibling);
				}
			}
			return ret;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public void computeLevels(int parentLevel){
		try{
			if(this.level < 0){
				this.level = parentLevel + 1;
			}
			else if(this.level > parentLevel+1){
				this.level = parentLevel + 1;
			}
			for(GraphNode n : getChildren()){
				n.computeLevels(this.level);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public String showNode(){
		String ret = "";
		for(int i=0; i<getLevel(); i++){
			ret += ".";
		}
		ret += "level="+getLevel();
		ret += " code="+getNodeCode();
		
		if(isLeaf()) ret += "(L)";
		else ret += "("+ getChildren().size() + ")";
		
		int pos = 0;
		if(getPositiveExamples()!=null){
			for(Integer i : getPositiveExamples()){
				if(i!=null) pos++;
			}
		}
		else{
			pos = 0;
		}
		int neg = 0;
		if(getNegativeExamples()!=null){
			for(Integer i : getNegativeExamples()){
				if(i!=null) neg++;
			}
		}
		else{
			neg = 0;
		}
		
		ret += " pos="+pos+" neg="+neg;
		
		ret += "\n";
		
		for(GraphNode n : getChildren()){
			ret += n.showNode();
		}
		
		return ret;
	}
	
	public String toString(){
		String ret = "";
		for(int i=0; i<level; i++){
			ret += ".";
		}
		ret += getNodeCode();
		if(isLeaf()) ret += "(L)";
		else ret += "("+ getChildren().size() + ")";
		ret += "\n";
		for(GraphNode n : getChildren()){
			ret += n.toString();
		}
		return ret;
	}
	
	public String printStatistics(){
		try{
			StringBuffer sb = new StringBuffer();
			for(int i=0; i<getLevel(); i++){
				sb.append(".");
			}
			sb.append(getNodeCode()+"("+getLevel()+")"+"("+(isLeaf()?"L":String.valueOf(getChildren().size()))+")");
			
			if(getNodeCode().equalsIgnoreCase("root")){
				sb.append("\n");
			}
			else{
				int cardinality = 0;
				
				for(int i=0; i<positiveExamples.length; i++){
					if(positiveExamples[i] != null){
						cardinality++;
					}
				}
				sb.append(" |POS|="+cardinality);
				cardinality = 0;
				
				Integer[] poscoverage = new Integer[positiveExamples.length];
				for(GraphNode n : getCoverage()){
					for(int i=0; i<positiveExamples.length; i++){
						if(n.getPositiveExamples()[i] != null){
							poscoverage[i] = n.getPositiveExamples()[i];
						}
					}
				}
				
				Integer[] postilde = Arrays.copyOf(poscoverage, positiveExamples.length);
				for(int i=0; i<positiveExamples.length; i++){
					if(positiveExamples[i] != null){
						postilde[i] = null;
					}
				}
				for(int i=0; i<positiveExamples.length; i++){
					if(postilde[i] != null){
						cardinality++;
					}
				}
				sb.append(" |POS~|="+cardinality);
				cardinality = 0;
				
				Integer[] pospostilde = Arrays.copyOf(positiveExamples, positiveExamples.length);
				for(int i=0; i<positiveExamples.length; i++){
					if(postilde[i] != null){
						pospostilde[i] = postilde[i];
					}
				}
				for(int i=0; i<positiveExamples.length; i++){
					if(pospostilde[i] != null){
						cardinality++;
					}
				}
				sb.append(" |POS U POS~|="+cardinality);
				cardinality = 0;
				
				for(int i=0; i<positiveExamples.length; i++){
					if(poscoverage[i] != null){
						cardinality++;
					}
				}
				sb.append(" |COV|="+cardinality);
				cardinality = 0;
				
				Integer[] poscovpospostilde = Arrays.copyOf(poscoverage, positiveExamples.length);
				for(int i=0; i<positiveExamples.length; i++){
					if(pospostilde[i] != null){
						poscovpospostilde[i] = null;
					}
				}
				for(int i=0; i<positiveExamples.length; i++){
					if(poscovpospostilde[i] != null){
						cardinality++;
					}
				}
				sb.append(" |COV - (POS U POS~)|="+cardinality);
				cardinality = 0;
				
				Integer[] pospostildeposcov = Arrays.copyOf(pospostilde, positiveExamples.length);
				for(int i=0; i<positiveExamples.length; i++){
					if(poscoverage[i] != null){
						pospostildeposcov[i] = null;
					}
				}
				for(int i=0; i<positiveExamples.length; i++){
					if(pospostildeposcov[i] != null){
						cardinality++;
					}
				}
				sb.append(" |(POS U POS~) - COV|="+cardinality);
				cardinality = 0;
				
				Integer[] poscovpos = Arrays.copyOf(poscoverage, positiveExamples.length);
				for(int i=0; i<positiveExamples.length; i++){
					if(positiveExamples[i] != null){
						poscovpos[i] = null;
					}
				}
				for(int i=0; i<positiveExamples.length; i++){
					if(poscovpos[i] != null){
						cardinality++;
					}
				}
				sb.append(" |COV - POS|="+cardinality);
				cardinality = 0;
				
				sb.append("\n");
			}
			
			
			for(GraphNode n : getChildren()){
				sb.append(n.printStatistics());
			}
			
			return sb.toString();
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int compareTo(Object arg0) throws ClassCastException{
		if (!(arg0 instanceof GraphNode)){
			throw new ClassCastException("A TreeNode object expected.");
		}
		return this.getNodeCode().compareToIgnoreCase(((GraphNode) arg0).getNodeCode());
	}
	
}

