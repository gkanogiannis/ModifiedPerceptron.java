/*
 *
 * ModifiedPerceptron.java utumno.mope.utils.hier.HierLSHTC2_Topic_2
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;

import utumno.mope.Trainer;
import utumno.mope.datastructures.ClassVector;
import utumno.mope.datastructures.ClassVectorSparse;
import utumno.mope.datastructures.DocumentVector;
import utumno.mope.datastructures.VectorNode;
import utumno.mope.utils.DocumentClassVectorUtils;

public class HierLSHTC2_Topic_2 {
	
	private static final int numberOfTrainExamples = 456886;
	private static final int numberOfTestExamples = 81262;
	private static final int numberOfTrainTerms = 346300;
	
	private ArrayList<VectorNode>[] trainInstances;
	private Hashtable<String, HashSet<Integer>> hashTrainPositives;
	//private Hashtable<String, HashSet<Integer>> hashTrainNegatives;
	
	private Hashtable<String, GraphNode> hashCodeNodes;
	private File dataInFile;
	private GraphNode rootNode;
	private int maxLevel=1;
	
	public HierLSHTC2_Topic_2(String dataInFileName){
		try{
			trainInstances = new ArrayList[numberOfTrainExamples];
			hashTrainPositives = new Hashtable<String, HashSet<Integer>>();
			//hashTrainNegatives = new Hashtable<String, HashSet<Integer>>();
			hashCodeNodes = new Hashtable<String, GraphNode>();
			dataInFile = new File(dataInFileName);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}	
	}
	
	private void buildTree(){
		try{	
			//Build basic graph
			BufferedReader br = new BufferedReader(new FileReader(this.dataInFile));
			String line = null;
			while((line=br.readLine())!=null){
				String[] data = line.split("\\s+");
				String parentCode = data[0].trim();
				String childCode =  data[1].trim();
				GraphNode parentNode = hashCodeNodes.get(parentCode);
				if(parentNode==null){
					parentNode = new GraphNode(parentCode);
					hashCodeNodes.put(parentCode, parentNode);
				}
				GraphNode childNode = hashCodeNodes.get(childCode);
				if(childNode==null){
					childNode = new GraphNode(childCode);
					hashCodeNodes.put(childCode, childNode);
				}
				parentNode.getChildren().add(childNode);
				parentNode.setLeaf(false);
				childNode.getParents().add(parentNode);
			}
			br.close();
			
			//Attach root node
			Enumeration<GraphNode> enumer = hashCodeNodes.elements();
			rootNode = new GraphNode("root");
			rootNode.setLeaf(false);
			rootNode.setLevel(0);
			while(enumer.hasMoreElements()){
				GraphNode node = enumer.nextElement();
				if(node.getParents().isEmpty()){
					rootNode.getChildren().add(node);
					node.getParents().add(rootNode);
				}
			}
			hashCodeNodes.put("root", rootNode);
			
			//Compute levels
			for(GraphNode node : rootNode.getChildren()){
				node.computeLevels(rootNode.getLevel());				
			}
			
			//Find maxLevel
			for(GraphNode node : rootNode.getAllDescendants()){
				if(node.getLevel()>maxLevel){
					maxLevel = node.getLevel();
				}
			}
			
			System.out.println("maxLevel="+maxLevel);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void mapTermIds(){
		try{
			String baseIn  = "/windows/d/BACKUP/Datasets/lshtc2/Task-2";
			
			HashMap<Integer, Integer> maps = new HashMap<Integer, Integer>();
			
			BufferedReader br = new BufferedReader(new FileReader(new File(baseIn,"train.original.txt")));
			String line = null;
			while((line=br.readLine())!=null){
				StringTokenizer tokenizer = new StringTokenizer(line);
				while(tokenizer.hasMoreTokens()){
					String tok = tokenizer.nextToken().trim();
					if(!tok.contains(":")){
						continue;
					}
					else{
						int termId = Integer.parseInt(tok.split(":")[0]);
						maps.put(termId, (maps.size()+1));
						while(tokenizer.hasMoreTokens()){
							tok = tokenizer.nextToken().trim();
							termId = Integer.parseInt(tok.split(":")[0]);
							maps.put(termId, (maps.size()+1));
						}
					}
				}
			}
			br.close();
			
			//Rewrite train.txt
			br = new BufferedReader(new FileReader(new File(baseIn,"train.original.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(baseIn,"train.txt")));
			line = null;
			while((line=br.readLine())!=null){
				StringTokenizer tokenizer = new StringTokenizer(line);
				while(tokenizer.hasMoreTokens()){
					String tok = tokenizer.nextToken().trim();
					if(!tok.contains(":")){
						bw.write(tok);
					}
					else{
						int termId = Integer.parseInt(tok.split(":")[0]);
						float termWeight = Float.parseFloat(tok.split(":")[1]);
						bw.write(" "+maps.get(termId)+":"+termWeight);
						while(tokenizer.hasMoreTokens()){
							tok = tokenizer.nextToken().trim();
							termId = Integer.parseInt(tok.split(":")[0]);
							termWeight = Float.parseFloat(tok.split(":")[1]);
							bw.write(" "+maps.get(termId)+":"+termWeight);
						}
					}
				}
				bw.write("\n");
			}
			br.close();
			bw.close();
			
			//Rewrite test.txt
			br = new BufferedReader(new FileReader(new File(baseIn,"test.original.txt")));
			bw = new BufferedWriter(new FileWriter(new File(baseIn,"test.txt")));
			line = null;
			while((line=br.readLine())!=null){
				StringTokenizer tokenizer = new StringTokenizer(line);
				while(tokenizer.hasMoreTokens()){
					String tok = tokenizer.nextToken().trim();
					if(!tok.contains(":")){
						bw.write(tok);
					}
					else{
						int termId = Integer.parseInt(tok.split(":")[0]);
						float termWeight = Float.parseFloat(tok.split(":")[1]);
						if(maps.get(termId)!=null){
							bw.write(" "+maps.get(termId)+":"+termWeight);
						}
						while(tokenizer.hasMoreTokens()){
							tok = tokenizer.nextToken().trim();
							termId = Integer.parseInt(tok.split(":")[0]);
							termWeight = Float.parseFloat(tok.split(":")[1]);
							if(maps.get(termId)!=null){
								bw.write(" "+maps.get(termId)+":"+termWeight);
							}
						}
					}
				}
				bw.write("\n");
			}
			br.close();
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void findMaxTermId(){
		try{
			String baseIn  = "/windows/d/BACKUP/Datasets/lshtc2/Task-2";
			
			int maxTermId = 0;
			BufferedReader br = new BufferedReader(new FileReader(new File(baseIn,"train.txt")));
			String line = null;
			while((line=br.readLine())!=null){
				StringTokenizer tokenizer = new StringTokenizer(line);
				while(tokenizer.hasMoreTokens()){
					String tok = tokenizer.nextToken().trim();
					if(!tok.contains(":")){
						continue;
					}
					else{
						int termId = Integer.parseInt(tok.split(":")[0]);
						if(termId > maxTermId){
							maxTermId = termId;
						}
						while(tokenizer.hasMoreTokens()){
							tok = tokenizer.nextToken().trim();
							termId = Integer.parseInt(tok.split(":")[0]);
							if(termId > maxTermId){
								maxTermId = termId;
							}
						}
					}
				}
			}
			br.close();
			
			System.err.println("maxTermId="+maxTermId);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void tfidf(){
		try{
			String baseIn  = "/windows/d/BACKUP/Datasets/lshtc2/Task-2";
			
			float[] termDfs = new float[numberOfTrainTerms];
			for(int i=0; i<numberOfTrainTerms; i++){
				termDfs[i] = 1.0f;
			}
			
			//Count DFs
			BufferedReader br = new BufferedReader(new FileReader(new File(baseIn,"train.txt")));
			String line = null;
			while((line=br.readLine())!=null){
				StringTokenizer tokenizer = new StringTokenizer(line);
				while(tokenizer.hasMoreTokens()){
					String tok = tokenizer.nextToken().trim();
					if(!tok.contains(":")){
						continue;
					}
					else{
						int termId = Integer.parseInt(tok.split(":")[0]);
						termDfs[termId-1] = termDfs[termId-1] + 1.0f;
						while(tokenizer.hasMoreTokens()){
							tok = tokenizer.nextToken().trim();
							termId = Integer.parseInt(tok.split(":")[0]);
							termDfs[termId-1] = termDfs[termId-1] + 1.0f;
						}
					}
				}
			}
			br.close();
			
			//Compute IDFs
			for(int i=0; i<numberOfTrainTerms; i++){
				termDfs[i] = (float)Math.log((float)numberOfTrainExamples/termDfs[i]);
			}
			
			//Convert train.txt
			br = new BufferedReader(new FileReader(new File(baseIn,"train.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(baseIn,"train.tfidf.txt")));
			line = null;
			int i = 0;
			while((line=br.readLine())!=null){
				System.out.println("train "+(++i));
				String data = "";
				StringTokenizer tokenizer = new StringTokenizer(line);
				while(tokenizer.hasMoreTokens()){
					String tok = tokenizer.nextToken().trim();
					if(!tok.contains(":")){
						data += tok;
					}
					else{
						String[] termIdTf = tok.split(":");
						int termId = Integer.parseInt(termIdTf[0]);
						float termTf = Float.parseFloat(termIdTf[1]);
						data += " " + termId + ":" + (termTf*termDfs[termId-1]);
						while(tokenizer.hasMoreTokens()){
							tok = tokenizer.nextToken().trim();
							termIdTf = tok.split(":");
							termId = Integer.parseInt(termIdTf[0]);
							termTf = Float.parseFloat(termIdTf[1]);
							data += " " + termId + ":" + (termTf*termDfs[termId-1]);
						}
					}
				}
				bw.write(data+"\n");
			}
			br.close();
			bw.close();
			
			//Convert test.txt
			br = new BufferedReader(new FileReader(new File(baseIn,"test.txt")));
			bw = new BufferedWriter(new FileWriter(new File(baseIn,"test.tfidf.txt")));
			line = null;
			i =0 ;
			while((line=br.readLine())!=null){
				System.out.println("test "+(++i));
				String data = "";
				StringTokenizer tokenizer = new StringTokenizer(line);
				while(tokenizer.hasMoreTokens()){
					String tok = tokenizer.nextToken().trim();
					if(!tok.contains(":")){
						data += tok;
					}
					else{
						String[] termIdTf = tok.split(":");
						int termId = Integer.parseInt(termIdTf[0]);
						float termTf = Float.parseFloat(termIdTf[1]);
						data += " " + termId + ":" + (termTf*termDfs[termId-1]);
						while(tokenizer.hasMoreTokens()){
							tok = tokenizer.nextToken().trim();
							termIdTf = tok.split(":");
							termId = Integer.parseInt(termIdTf[0]);
							termTf = Float.parseFloat(termIdTf[1]);
							data += " " + termId + ":" + (termTf*termDfs[termId-1]);
						}
					}
				}
				bw.write(data+"\n");
			}
			br.close();
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void generateTrainSvmfFiles(){
		try{
			String baseIn  = "/windows/d/BACKUP/Datasets/lshtc2/Task-2";
			String baseOut = "/windows/d/BACKUP/Datasets/lshtc2/Task-2/train";
			
			//Fill leaf positive
			BufferedReader br = new BufferedReader(new FileReader(new File(baseIn, "train.tfidf.txt")));
			int index = 0;
			String line = null;
			while((line=br.readLine())!=null){
				StringTokenizer tokenizer = new StringTokenizer(line);
				while(tokenizer.hasMoreTokens()){
					String tok = tokenizer.nextToken().trim();
					if(!tok.contains(":")){
						String[] labels = tok.split(",");
						for(String label : labels){
							HashSet<Integer> classPos = hashTrainPositives.get(label);
							if(classPos==null){
								classPos = new HashSet<Integer>();
								hashTrainPositives.put(label, classPos);
							}
							classPos.add(index);
						}
					}
					else{
						ArrayList<VectorNode> al = new ArrayList<VectorNode>();
						trainInstances[index] = al;
						
						String[] data = tok.split(":");
						al.add(new VectorNode(Integer.parseInt(data[0]), Float.parseFloat(data[1])));
						while(tokenizer.hasMoreTokens()){
							data = tokenizer.nextToken().trim().split(":");
							al.add(new VectorNode(Integer.parseInt(data[0]), Float.parseFloat(data[1])));
						}
					}
				}
				index++;
			}
			br.close();
			
			//Fill internal positive
			for(int level=maxLevel; level>=1; level--){
				for(GraphNode node : rootNode.getAllDescendants()){
					if(!node.isLeaf() && node.getLevel()==level){
						HashSet<Integer> classPos = new HashSet<Integer>();
						for(GraphNode n : node.getChildren()){
							classPos.addAll(hashTrainPositives.get(n.getNodeCode()));
						}
						hashTrainPositives.put(node.getNodeCode(), classPos);
					}
				}
			}
			
			for(GraphNode node : rootNode.getAllDescendants()){
				HashSet<Integer> positiveExamples = hashTrainPositives.get(node.getNodeCode());
				System.out.print("Filling positive for "+"\t"+node.getNodeCode()+"("+node.getLevel()+")"+(node.isLeaf()?"L":""));
				int pos = positiveExamples.size();
				System.out.println("\tpos="+pos);
			}
			
			/*
			//Fill negative 1 == Pos(Siblings(Node(i))
			for(TreeNode node : rootNode.getAllDescendants()){
				System.out.print("Filling negative for "+node.getNodeNameString()+"\t"+node.getNodeCodeString()+"("+node.getLevel()+")"+(node.isLeaf()?"L":""));
				Integer[] negativeExamples= new Integer[numberOfTrainExamples];
				int neg = 0;
				for(TreeNode sibling : node.getAllSiblings()){
					Integer[] siblingPositiveExamples = sibling.getPositiveExamples();
					for(int k=0; k<numberOfTrainExamples; k++){
						if(siblingPositiveExamples[k]!=null){
							negativeExamples[k] = siblingPositiveExamples[k];
						}
					}
				}
				for(Integer k : negativeExamples){
					if(k!=null){
						neg++;
					}
				}
				System.out.println("\tneg="+neg);
				node.setNegativeExamples(negativeExamples);
			}
			*/
			
			//Fill negative 2 == Pos(Siblings(Node(i))+Pos(Parent(Node(i)))
			for(GraphNode node : rootNode.getAllDescendants()){
				System.out.print("Filling negative for "+"\t"+node.getNodeCode()+"("+node.getLevel()+")"+(node.isLeaf()?"L":""));
				HashSet<Integer> negativeExamples = new HashSet<Integer>();
				for(GraphNode sibling : node.getAllSiblings()){
					HashSet<Integer> siblingPositiveExamples = hashTrainPositives.get(sibling.getNodeCode());
					negativeExamples.addAll(siblingPositiveExamples);
				}
				if(node.getLevel()>1){
					for(GraphNode p : node.getParents()){
						HashSet<Integer> parentPositiveExamples = hashTrainPositives.get(p.getNodeCode());
						negativeExamples.addAll(parentPositiveExamples);
					}
				}
				negativeExamples.removeAll(hashTrainPositives.get(node.getNodeCode()));
				int neg = negativeExamples.size();
				//if(neg==0){
					//HashSet<Integer> grantParentPositiveExamples = hashTrainPositives.get(node.getParent().getParent().getNodeCode());
					//negativeExamples.addAll(grantParentPositiveExamples);
					//negativeExamples.removeAll(hashTrainPositives.get(node.getNodeCode()));
					//neg = negativeExamples.size();
				//}
				System.out.println("\tneg="+neg);
				
				HashSet<Integer> pE = hashTrainPositives.get(node.getNodeCode());
				HashSet<Integer> nE = negativeExamples;
				System.out.println("Writing svmf file for "+node.getNodeCode());
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(baseOut,node.getNodeCode()+".train.svmf")));
				Iterator<Integer> pI = pE.iterator();
				while(pI.hasNext()){
					Integer i = pI.next();
					bw.write("+1");
					ArrayList<VectorNode> al = trainInstances[i];
					for(VectorNode vn : al){
						bw.write(" "+vn.getTermId()+":"+vn.getTermWeight());
					}
					bw.write("\n");
				}
				Iterator<Integer> nI = nE.iterator();
				while(nI.hasNext()){
					Integer i = nI.next();
					bw.write("-1");
					ArrayList<VectorNode> al = trainInstances[i];
					for(VectorNode vn : al){
						bw.write(" "+vn.getTermId()+":"+vn.getTermWeight());
					}
					bw.write("\n");
				}
				bw.close();
			}
			
			/*
			//Fill negative 3 == FirstLevelNodePos(Node(i))
			for(TreeNode node : rootNode.getAllDescendants()){
				System.out.print("Filling negative for "+"\t"+node.getNodeCode()+"("+node.getLevel()+")"+(node.isLeaf()?"L":""));
				HashSet<Integer> negativeExamples = new HashSet<Integer>();
				if(node.getLevel()!=1){
					for(TreeNode ancestor : node.getAllAncestors()){
						if(ancestor.getLevel()==1){
							negativeExamples.addAll(hashTrainPositives.get(ancestor.getNodeCode()));
							break;
						}
					}
				}
				else{
					for(TreeNode sibling : node.getAllSiblings()){
						negativeExamples.addAll(hashTrainPositives.get(sibling.getNodeCode()));
					}
				}
				negativeExamples.removeAll(hashTrainPositives.get(node.getNodeCode()));
				int neg = negativeExamples.size();
				System.out.println("\tneg="+neg);
				
				HashSet<Integer> pE = hashTrainPositives.get(node.getNodeCode());
				HashSet<Integer> nE = negativeExamples;
				System.out.println("Writing svmf file for "+node.getNodeCode());
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(baseOut,node.getNodeCode()+".train.svmf")));
				Iterator<Integer> pI = pE.iterator();
				while(pI.hasNext()){
					Integer i = pI.next();
					bw.write("+1");
					ArrayList<VectorNode> al = trainInstances[i];
					for(VectorNode vn : al){
						bw.write(" "+vn.getTermId()+":"+vn.getTermWeight());
					}
					bw.write("\n");
				}
				Iterator<Integer> nI = nE.iterator();
				while(nI.hasNext()){
					Integer i = nI.next();
					bw.write("-1");
					ArrayList<VectorNode> al = trainInstances[i];
					for(VectorNode vn : al){
						bw.write(" "+vn.getTermId()+":"+vn.getTermWeight());
					}
					bw.write("\n");
				}
				bw.close();
			}			
			*/
			/*
			//Fill negative 4 == All-Pos(i)
			for(TreeNode node : rootNode.getAllDescendants()){
				System.out.print("Filling negative for "+node.getNodeNameString()+"\t"+node.getNodeCodeString()+"("+node.getLevel()+")"+(node.isLeaf()?"L":""));
				Integer[] positiveExamples= node.getPositiveExamples();
				Integer[] negativeExamples= new Integer[numberOfTrainExamples];
				int neg = 0;
				for(int k=0; k<numberOfTrainExamples; k++){
					if(positiveExamples[k]==null){
						negativeExamples[k] = k;
					}
				}
				for(Integer k : negativeExamples){
					if(k!=null){
						neg++;
					}
				}
				System.out.println("\tneg="+neg);
				node.setNegativeExamples(negativeExamples);
			}
			*/
			
			/*
			//Generate SVMF files
			for(TreeNode node : rootNode.getAllDescendants()){
				HashSet<Integer> pE = hashTrainPositives.get(node.getNodeCode());
				HashSet<Integer> nE = hashTrainNegatives.get(node.getNodeCode());
				System.out.println("Writing svmf file for "+node.getNodeCode());
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(baseOut,node.getNodeCode()+".train.svmf")));
				Iterator<Integer> pI = pE.iterator();
				while(pI.hasNext()){
					Integer i = pI.next();
					bw.write("+1");
					ArrayList<VectorNode> al = trainInstances[i];
					for(VectorNode vn : al){
						bw.write(" "+vn.getTermId()+":"+vn.getTermWeight());
					}
					bw.write("\n");
				}showTree
				Iterator<Integer> nI = nE.iterator();
				while(nI.hasNext()){
					Integer i = nI.next();
					bw.write("-1");
					ArrayList<VectorNode> al = trainInstances[i];
					for(VectorNode vn : al){
						bw.write(" "+vn.getTermId()+":"+vn.getTermWeight());
					}
					bw.write("\n");
				}
				bw.close();
			}
			*/
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void showTree(){
		try{
			System.out.println(rootNode.showNode());
			System.out.println();
			System.out.print("Nodes="+rootNode.getAllDescendants().size());
			System.out.println("\tLeaves="+rootNode.countAllLeafDescendants());
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void generateTrainScripts(){
		try{
			String baseOut  = "/windows/d/BACKUP/Datasets/lshtc2/Task-2";
			
			BufferedWriter br = new BufferedWriter(new FileWriter(new File(baseOut, "train.sh")));
			
			for(GraphNode node : rootNode.getAllDescendants()){
				br.write("java -Xmx3584m -server -Xoptimize -classpath \"/windows/p/BACKUP/Datasets/mope.jar\" utumno.mope.cmd.ClassVectorBuilderAdjusterCmd -iter 100 -inData \"/windows/d/BACKUP/Datasets/lshtc2/Task-2/train/"+node.getNodeCode()+".train.svmf\"");
				br.write("\n");
			}
			br.close();
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void featureSelection(){
		try{
			float percentage = 0.10f;
			
			//String baseTrain = "D:\\BACKUP\\Datasets\\lshtc2\\Task-1\\train";
			//String baseTrainNOFS = "D:\\BACKUP\\Datasets\\lshtc2\\Task-1\\train.NOFS";
			String baseTrain = "/windows/d/BACKUP/Datasets/lshtc2/Task-1/train";
			String baseTrainNOFS = "/windows/d/BACKUP/Datasets/lshtc2/Task-1/train.tfidf.NOFS";
			
			for(TreeNode node : rootNode.getAllDescendants()){
				BufferedReader br = null;
				try{
					br = new BufferedReader(new FileReader(new File(baseTrainNOFS,node.getNodeCode()+".mope.model")));
				}
				catch(FileNotFoundException e){
					continue;
				}
				ClassVectorSparse cv = DocumentClassVectorUtils.parseClassVectorSparseFromString(br.readLine());
				br.close();
				System.out.print(node.getNodeCode());
			
				int numberOfTerms = cv.getNodes().size();
				int numberOfKeptTerms = (int)((float)numberOfTerms * percentage);
				System.out.println(" terms="+numberOfTerms+" keep="+numberOfKeptTerms);
				
				TreeSet<VectorNode> terms = new TreeSet<VectorNode>(new VectorNodeWeightComparator());
				Enumeration<VectorNode> enumer = cv.getNodes().elements();
				while(enumer.hasMoreElements()){
					VectorNode vn = enumer.nextElement();
					terms.add(new VectorNode(vn.getTermId(), Math.abs(vn.getTermWeight())));
				}
				
				HashSet<Integer> kept = new HashSet<Integer>();
				
				int i = 0;
				Iterator<VectorNode> iter = terms.descendingIterator();
				while(iter.hasNext()){
					if(i > numberOfKeptTerms){
						break;
					}
					VectorNode vn = iter.next();
					kept.add(vn.getTermId());
					i++;
				}
				
				br = new BufferedReader(new FileReader(new File(baseTrainNOFS,node.getNodeCode()+".train.svmf")));
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(baseTrain,node.getNodeCode()+".train.svmf")));
				String line = null;
				while((line=br.readLine())!=null){
					String data = "";
					StringTokenizer tokenizer = new StringTokenizer(line);
					data += tokenizer.nextToken();
					while(tokenizer.hasMoreTokens()){
						String tok = tokenizer.nextToken();
						String[] tw = tok.split(":");
						if(kept.contains(Integer.parseInt(tw[0]))){
							data += " " + tok;
						}
					}
					bw.write(data+"\n");
				}
				
				br.close();
				bw.close();
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void evaluateSlow(){
		try{
			//String base = "D:\\BACKUP\\Datasets\\lshtc2\\Task-1";
			//String baseTrain = "D:\\BACKUP\\Datasets\\lshtc2\\Task-1\\train";
			String base = "/windows/d/BACKUP/Datasets/lshtc2/Task-1";
			String baseTrain = "/windows/d/BACKUP/Datasets/lshtc2/Task-1/train";		
			
			Thread t0 = new Thread(new EvaluatorThread(baseTrain, new File(base,"test.tfidf.txt"), new File(base,"results.txt")));
			
			Thread t1 = new Thread(new EvaluatorThread(baseTrain, new File(base,"test.tfidf.txt_aa"), new File(base,"results.txt_aa")));
			Thread t2 = new Thread(new EvaluatorThread(baseTrain, new File(base,"test.tfidf.txt_ab"), new File(base,"results.txt_ab")));
			Thread t3 = new Thread(new EvaluatorThread(baseTrain, new File(base,"test.tfidf.txt_ac"), new File(base,"results.txt_ac")));
			Thread t4 = new Thread(new EvaluatorThread(baseTrain, new File(base,"test.tfidf.txt_ad"), new File(base,"results.txt_ad")));
			
			//t0.start();
			t1.start();
			t2.start();
			t3.start();
			t4.start();
				
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void evaluateAtNodeSlow(TreeNode node, String baseTrain, DocumentVector dv, ArrayList<String> results){
		try{
			BufferedReader br = null;
			try{
				br = new BufferedReader(new FileReader(new File(baseTrain,node.getNodeCode()+".mope.model")));
			}
			catch(FileNotFoundException e){
				//continue;
				br = new BufferedReader(new FileReader(new File(baseTrain,node.getParent().getNodeCode()+".mope.model")));
			}
			ClassVectorSparse cv = DocumentClassVectorUtils.parseClassVectorSparseFromString(br.readLine());
			//ClassVector cv = DocumentClassVectorUtils.parseClassVectorFromString(br.readLine());
			br.close();
		
			if(cv == null){
				return;
			}
			
			float similarity = cv.similarityWithDocumentVector(dv);
		
			if( Math.abs(similarity-cv.getThreshold())<Trainer.epsilon || similarity > cv.getThreshold()){
				if(node.isLeaf()){
					results.add(node.getNodeCode());
				}
				for(TreeNode child : node.getChildren()){
					evaluateAtNodeSlow(child, baseTrain, dv, results);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void evaluateAtNode(TreeNode node, Hashtable<String, ClassVectorSparse> classVectors, DocumentVector dv, ArrayList<String> results){
		try{
			ClassVectorSparse cv  = classVectors.get(node.getNodeCode());
		
			if(cv == null){
				return;
			}
			
			float similarity = cv.similarityWithDocumentVector(dv);
		
			if( Math.abs(similarity-cv.getThreshold())<Trainer.epsilon || similarity > cv.getThreshold()){
				if(node.isLeaf()){
					results.add(node.getNodeCode());
				}
				for(TreeNode child : node.getChildren()){
					evaluateAtNode(child, classVectors, dv, results);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		HierLSHTC2_Topic_2 main = new HierLSHTC2_Topic_2("/windows/d/BACKUP/Datasets/lshtc2/Task-2/cat_hier.txt");
		main.buildTree();
		main.showTree();
		//main.mapTermIds();
		//main.findMaxTermId();
		//main.tfidf();
		//main.generateTrainSvmfFiles();
		//main.featureSelection();
		main.generateTrainScripts();
		//main.evaluate();
		//main.evaluateLevel2();
		//main.evaluateLevel3();
		//main.evaluateSlow();
	}
	
	private class EvaluatorThread implements Runnable{

		private String baseTrain;
		private File svmfFile;
		private File outTxt;
		
		public EvaluatorThread(String baseTrain, File svmfFile, File outTxt){
			this.baseTrain = baseTrain;
			this.svmfFile = svmfFile;
			this.outTxt = outTxt;
		}
		
		public void run() {
			try{
				BufferedWriter bw = new BufferedWriter(new FileWriter(outTxt));
				BufferedReader br = new BufferedReader(new FileReader(svmfFile));
				String line = null;
				int docIndex = -1;
				HashSet<Integer> featuresIds = new HashSet<Integer>();
				int[] maxFeaturesId = new int[1];
				maxFeaturesId[0] = 0;
				while((line=br.readLine())!=null){
					docIndex++;
					DocumentVector dv = DocumentClassVectorUtils.parseDocumentVectorFromString(line,featuresIds,maxFeaturesId);
					if(dv.getDocId() == null){
						dv.setDocId(String.valueOf(docIndex));
					}
							
					System.out.println("Evaluating document "+docIndex);
							
					ArrayList<String> results = new ArrayList<String>();
						
					for(TreeNode node : rootNode.getChildren()){
						evaluateAtNodeSlow(node, baseTrain, dv, results);
					}
					
					String finalResult = "";
					for(String s : results){
						finalResult += " "+s;
					}
					finalResult = finalResult.trim();
			
					if(finalResult.equalsIgnoreCase("")){
						bw.write("30232\n");
					}
					else{
						bw.write(finalResult+"\n");
					}
					
					bw.flush();
				}
				bw.close();
				br.close();
			}
			catch(Exception e){
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	private class VectorNodeWeightComparator implements Comparator<VectorNode>{

		public int compare(VectorNode o1, VectorNode o2) {
			if(o1.getTermWeight() > o2.getTermWeight()){
                return 1;
            }
            else if(o1.getTermWeight() < o2.getTermWeight()){
                return -1;
            }
            else{
            	return 0;
            }
		}
		
	}
	/*
	private class VectorNodeLightWeightComparator implements Comparator<VectorNodeLight>{

		public int compare(VectorNodeLight o1, VectorNodeLight o2) {
			if(o1.getTermWeight() > o2.getTermWeight()){
                return 1;
            }
            else if(o1.getTermWeight() < o2.getTermWeight()){
                return -1;
            }
            else{
            	return 0;
            }
		}
		
	}
	*/
	
}
