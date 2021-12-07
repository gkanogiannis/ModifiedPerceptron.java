/*
 *
 * ModifiedPerceptron.java utumno.mope.utils.hier.TreeNode
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
package utumno.mope.utils.hier;

import java.util.Arrays;
import java.util.TreeSet;

public class TreeNode implements Comparable<Object>{
	private String nodeCode;
	private TreeNode parent;
	private TreeSet<TreeNode> children;
	private Integer[] positiveExamples; 
	private Integer[] negativeExamples;
	private Integer[] positiveExamplesSubtree; 
	private Integer[] negativeExamplesSubtree;
	private boolean leaf;
	private int level;
	private int a;
	private int b;
	private int c;
	
	public TreeNode(String code){
		setNodeCode(code);
		setParent(null);
		setChildren(new TreeSet<TreeNode>());
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
	public TreeNode getParent() {
		return parent;
	}
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	public TreeSet<TreeNode> getChildren() {
		return children;
	}
	public void setChildren(TreeSet<TreeNode> children) {
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
				for(TreeNode child : children){
					if(child.isLeaf()){
						ret++;
					}
					else{
						ret += child.countAllLeafDescendants();
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
				for(TreeNode child : children){
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
	
	public TreeSet<TreeNode> getAllAncestors(){
		try{
			TreeSet<TreeNode> ret = new TreeSet<TreeNode>();
			if(parent!=null){
				ret.add(parent);
				ret.addAll(parent.getAllAncestors());
			}
			return ret;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public TreeSet<TreeNode> getAllDescendants(){
		try{
			TreeSet<TreeNode> ret = new TreeSet<TreeNode>();
			for(TreeNode child : children){
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
	
	public TreeSet<TreeNode> getCoverage(){
		try{
			TreeSet<TreeNode> ret = getAllDescendants();
			ret.add(this);
			return ret;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public TreeNode findDescendant(TreeNode nodeToFind){
		try{
			if(children.contains(nodeToFind)){
				for(TreeNode child1 : children){
					if(child1.equals(nodeToFind.getNodeCode())){
						return child1;
					}
				}
			}
			else{
				for(TreeNode child2 : children){
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
	
	public TreeSet<TreeNode> getAllSiblings(){
		try{
			TreeSet<TreeNode> ret = new TreeSet<TreeNode>();
			for(TreeNode sibling : getParent().getChildren()){
				if(this.equals(sibling)) continue;
				ret.add(sibling);
			}
			return ret;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public void computeLevels(){
		try{
			this.level = getParent().getLevel() + 1;
			for(TreeNode n : getChildren()){
				n.computeLevels();
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
		
		for(TreeNode n : getChildren()){
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
		for(TreeNode n : getChildren()){
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
			
			if(getNodeCode().equals("root")){
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
				for(TreeNode n : getCoverage()){
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
			
			
			for(TreeNode n : getChildren()){
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
		if (!(arg0 instanceof TreeNode)){
			throw new ClassCastException("A TreeNode object expected.");
		}
		return this.getNodeCode().compareToIgnoreCase(((TreeNode) arg0).getNodeCode());
	}
	
}


