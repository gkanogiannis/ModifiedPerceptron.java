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
import utumno.mope.datastructures.ClassVectorSimple;
import utumno.mope.datastructures.ClassVectorSparse;
import utumno.mope.datastructures.DocumentVector;
import utumno.mope.datastructures.VectorNode;
import utumno.mope.utils.DocumentClassVectorUtils;

public class HierLSHTC2_Topic_1 {
	
	private static final int numberOfTrainExamples = 394756;
	private static final int numberOfTestExamples = 104263;
	private static final int numberOfTrainTerms = 594158;
	
	private ArrayList<VectorNode>[] trainInstances;
	private Hashtable<String, HashSet<Integer>> hashTrainPositives;
	//private Hashtable<String, HashSet<Integer>> hashTrainNegatives;
	
	private Hashtable<String, TreeNode> hashCodeNodes;
	private File dataInFile;
	private TreeNode rootNode;
	private int maxLevel=1;
	
	public HierLSHTC2_Topic_1(String dataInFileName){
		try{
			trainInstances = new ArrayList[numberOfTrainExamples];
			hashTrainPositives = new Hashtable<String, HashSet<Integer>>();
			//hashTrainNegatives = new Hashtable<String, HashSet<Integer>>();
			hashCodeNodes = new Hashtable<String, TreeNode>();
			dataInFile = new File(dataInFileName);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}	
	}
	
	private void buildTree(){
		try{	
			//Build basic tree
			BufferedReader br = new BufferedReader(new FileReader(this.dataInFile));
			String line = null;
			while((line=br.readLine())!=null){
				String[] data = line.split("\\s+");
				String parentCode = data[0].trim();
				String childCode =  data[1].trim();
				TreeNode parentNode = hashCodeNodes.get(parentCode);
				if(parentNode==null){
					parentNode = new TreeNode(parentCode);
					hashCodeNodes.put(parentCode, parentNode);
				}
				TreeNode childNode = hashCodeNodes.get(childCode);
				if(childNode==null){
					childNode = new TreeNode(childCode);
					hashCodeNodes.put(childCode, childNode);
				}
				parentNode.getChildren().add(childNode);
				parentNode.setLeaf(false);
				childNode.setParent(parentNode);
			}
			br.close();
			
			//Attach root node
			Enumeration<TreeNode> enumer = hashCodeNodes.elements();
			rootNode = new TreeNode("root");
			rootNode.setLeaf(false);
			rootNode.setLevel(0);
			while(enumer.hasMoreElements()){
				TreeNode node = enumer.nextElement();
				if(node.getParent()==null){
					rootNode.getChildren().add(node);
					node.setParent(rootNode);
				}
			}
			hashCodeNodes.put("root", rootNode);
			
			//Compute levels
			for(TreeNode node : rootNode.getChildren()){
				node.computeLevels();				
			}
			
			//Find maxLevel
			for(TreeNode node : rootNode.getAllDescendants()){
				if(node.getLevel()>maxLevel){
					maxLevel = node.getLevel();
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void mapTermIds(){
		try{
			String baseIn  = "/windows/d/BACKUP/Datasets/lshtc2/Task-1";
			
			TreeSet<Integer> set = new TreeSet<Integer>();
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
						set.add(termId);
						while(tokenizer.hasMoreTokens()){
							tok = tokenizer.nextToken().trim();
							termId = Integer.parseInt(tok.split(":")[0]);
							set.add(termId);
						}
					}
				}
			}
			br.close();
			
			//assign
			int setSize = set.size();
			for(int i=1; i<=setSize; i++){
				maps.put(set.pollFirst(), i);
			}
			
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
			String baseIn  = "/windows/d/BACKUP/Datasets/lshtc2/Task-1";
			
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
			String baseIn  = "/windows/d/BACKUP/Datasets/lshtc2/Task-1";
			
			Float[] termDfs = new Float[numberOfTrainTerms];
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
				float f = termDfs[i];
				termDfs[i] = (float)Math.log((float)numberOfTrainExamples/f);
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
			String baseIn  = "/windows/d/BACKUP/Datasets/lshtc2/Task-1";
			String baseOut = "/windows/d/BACKUP/Datasets/lshtc2/Task-1/train";
			
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
				for(TreeNode node : rootNode.getAllDescendants()){
					if(!node.isLeaf() && node.getLevel()==level){
						HashSet<Integer> classPos = new HashSet<Integer>();
						for(TreeNode n : node.getChildren()){
							classPos.addAll(hashTrainPositives.get(n.getNodeCode()));
						}
						hashTrainPositives.put(node.getNodeCode(), classPos);
					}
				}
			}
			
			for(TreeNode node : rootNode.getAllDescendants()){
				HashSet<Integer> positiveExamples = hashTrainPositives.get(node.getNodeCode());
				System.out.println("Filling positive for "+"\t"+node.getNodeCode()+"("+node.getLevel()+")"+(node.isLeaf()?"L":""));
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
			for(TreeNode node : rootNode.getAllDescendants()){
				System.out.println("Filling negative for "+"\t"+node.getNodeCode()+"("+node.getLevel()+")"+(node.isLeaf()?"L":""));
				HashSet<Integer> negativeExamples = new HashSet<Integer>();
				for(TreeNode sibling : node.getAllSiblings()){
					HashSet<Integer> siblingPositiveExamples = hashTrainPositives.get(sibling.getNodeCode());
					negativeExamples.addAll(siblingPositiveExamples);
				}
				//if(node.getLevel()>1){
				//HashSet<Integer> parentPositiveExamples = hashTrainPositives.get(node.getParent().getNodeCode());
					//negativeExamples.addAll(parentPositiveExamples);
				//}
				negativeExamples.removeAll(hashTrainPositives.get(node.getNodeCode()));
				int neg = negativeExamples.size();
				//if(neg==0){
					//HashSet<Integer> grantParentPositiveExamples = hashTrainPositives.get(node.getParent().getParent().getNodeCode());
					//negativeExamples.addAll(grantParentPositiveExamples);
					//negativeExamples.removeAll(hashTrainPositives.get(node.getNodeCode()));
					//neg = negativeExamples.size();
				//}
				
				HashSet<Integer> pE = hashTrainPositives.get(node.getNodeCode());
				HashSet<Integer> nE = negativeExamples;
				System.out.println("Writing svmf file for "+node.getNodeCode()+"\tpos="+pE.size()+"\tneg="+nE.size());
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
			String baseOut  = "D:\\BACKUP\\Datasets\\lshtc2\\Task-1";
			
			BufferedWriter br = new BufferedWriter(new FileWriter(new File(baseOut, "train.bat")));
			
			for(TreeNode node : rootNode.getAllDescendants()){
				br.write("java -Xmx3584m -server -Xoptimize -classpath \"P:\\BACKUP\\Datasets\\rh.jar\" utumno.rothyp.cmd.ClassVectorBuilderAdjusterCmd -iter 100 -inData \"D:\\BACKUP\\Datasets\\lshtc2\\Task-1\\train\\"+node.getNodeCode()+".train.svmf\"");
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
	
	private void evaluate(){
		try{
			//String base = "D:\\BACKUP\\Datasets\\lshtc2\\Task-1";
			//String baseTrain = "D:\\BACKUP\\Datasets\\lshtc2\\Task-1\\train";
			String base = "/windows/d/BACKUP/Datasets/lshtc2/Task-1";
			String baseTrain = "/windows/d/BACKUP/Datasets/lshtc2/Task-1/train";		
			
			Hashtable<String, ClassVectorSparse> classVectors = new Hashtable<String, ClassVectorSparse>();
			
			String[] fullResults = new String[numberOfTestExamples];
			int i=0;
			for(TreeNode node : rootNode.getAllDescendants()){
				BufferedReader br = null;
				try{
					br = new BufferedReader(new FileReader(new File(baseTrain,node.getNodeCode()+".mope.model")));
				}
				catch(FileNotFoundException e){
					//continue;
					br = new BufferedReader(new FileReader(new File(baseTrain,node.getParent().getNodeCode()+".mope.model")));
				}
				ClassVectorSparse cv = DocumentClassVectorUtils.parseClassVectorSparseFromString(br.readLine());
				br.close();
				classVectors.put(node.getNodeCode(),cv);
				System.out.println((++i)+" "+node.getNodeCode()+" "+" "+cv.getName()+" "+cv.getThreshold());
			}
			
			File svmfFile = new File(base,"test.tfidf.txt");
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
					evaluateAtNode(node, classVectors, dv, results);
				}
				
				String finalResult = "";
				for(String s : results){
					finalResult += " "+s;
				}
				fullResults[docIndex] = finalResult.trim();
			}
			br.close();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(base,"results.txt")));
			for(String s : fullResults){
				if(s==null){
					bw.write("30232\n");
				}
				else if(s!=null && s.equalsIgnoreCase("")){
					bw.write("30232\n");
				}
				else{
					bw.write(s+"\n");
				}
			}
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void evaluateLevel2(){
		try{
			//String base = "D:\\BACKUP\\Datasets\\lshtc2\\Task-1";
			//String baseTrain = "D:\\BACKUP\\Datasets\\lshtc2\\Task-1\\train";
			String base = "/windows/d/BACKUP/Datasets/lshtc2/Task-1";
			String baseTrain = "/windows/d/BACKUP/Datasets/lshtc2/Task-1/train";
			
			String[] fullResults = new String[numberOfTestExamples];
			for(int i=0; i<numberOfTestExamples; i++){
				fullResults[i] = "";
			}
			int i=0;
			BufferedReader br = null;
			ClassVectorSparse cv = null;
			
			for(TreeNode firstLevelNode : rootNode.getChildren()){
				for(TreeNode secondLevelNode : firstLevelNode.getChildren()){
					System.gc();
					Hashtable<String, ClassVectorSparse> classVectors = new Hashtable<String, ClassVectorSparse>();
					br = new BufferedReader(new FileReader(new File(baseTrain,firstLevelNode.getNodeCode()+".mope.model")));
					cv = DocumentClassVectorUtils.parseClassVectorSparseFromString(br.readLine());
					br.close();
					classVectors.put(firstLevelNode.getNodeCode(),cv);
					System.out.println((++i)+" "+firstLevelNode.getNodeCode()+" "+" "+cv.getName()+" "+cv.getThreshold());
					for(TreeNode node : secondLevelNode.getCoverage()){
						try{
							br = new BufferedReader(new FileReader(new File(baseTrain,node.getNodeCode()+".mope.model")));
						}
						catch(FileNotFoundException e){
							//continue;
							br = new BufferedReader(new FileReader(new File(baseTrain,node.getParent().getNodeCode()+".mope.model")));
						}
						cv = DocumentClassVectorUtils.parseClassVectorSparseFromString(br.readLine());
						br.close();
						classVectors.put(node.getNodeCode(),cv);
						System.out.println((++i)+" "+node.getNodeCode()+" "+" "+cv.getName()+" "+cv.getThreshold());
					}
					System.out.println("Evaluating branch "+secondLevelNode.getNodeCode());
					File svmfFile = new File(base,"test.tfidf.txt");
					br = new BufferedReader(new FileReader(svmfFile));
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
									
						//System.out.println("Evaluating document "+docIndex+" on branch "+secondLevelNode.getNodeCode());
									
						ArrayList<String> results = new ArrayList<String>();
								
						for(TreeNode node : rootNode.getChildren()){
							evaluateAtNode(node, classVectors, dv, results);
						}
							
						String finalResult = "";
						if(!results.isEmpty()){
							for(String s : results){
								finalResult += " "+s;
							}
							finalResult = finalResult.trim();
						}
						
						fullResults[docIndex] = (fullResults[docIndex] + " " + finalResult).trim();
					}
					br.close();
				}
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(base,"results.txt")));
			for(String s : fullResults){
				if(s.equalsIgnoreCase("")){
					bw.write("30232\n");
				}
				else{
					bw.write(s+"\n");
				}
			}
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void evaluateLevel3(){
		try{
			//String base = "D:\\BACKUP\\Datasets\\lshtc2\\Task-1";
			//String baseTrain = "D:\\BACKUP\\Datasets\\lshtc2\\Task-1\\train";
			String base = "/windows/d/BACKUP/Datasets/lshtc2/Task-1";
			String baseTrain = "/windows/d/BACKUP/Datasets/lshtc2/Task-1/train";
			
			String[] fullResults = new String[numberOfTestExamples];
			for(int i=0; i<numberOfTestExamples; i++){
				fullResults[i] = "";
			}
			int i=0;
			BufferedReader br = null;
			ClassVectorSimple cv = null;
			
			for(TreeNode firstLevelNode : rootNode.getChildren()){
				for(TreeNode secondLevelNode : firstLevelNode.getChildren()){
					for(TreeNode thirdLevelNode : secondLevelNode.getChildren()){
						Hashtable<String, ClassVectorSimple> classVectors = new Hashtable<String, ClassVectorSimple>();
						br = new BufferedReader(new FileReader(new File(baseTrain,firstLevelNode.getNodeCode()+".mope.model")));
						cv = DocumentClassVectorUtils.parseClassVectorSimpleFromString(br.readLine());
						br.close();
						classVectors.put(firstLevelNode.getNodeCode(),cv);
						System.out.println((++i)+" "+firstLevelNode.getNodeCode()+" "+" "+cv.getName()+" "+cv.getThreshold());
						br = new BufferedReader(new FileReader(new File(baseTrain,secondLevelNode.getNodeCode()+".mope.model")));
						cv = DocumentClassVectorUtils.parseClassVectorSimpleFromString(br.readLine());
						br.close();
						classVectors.put(secondLevelNode.getNodeCode(),cv);
						System.out.println((++i)+" "+secondLevelNode.getNodeCode()+" "+" "+cv.getName()+" "+cv.getThreshold());
						for(TreeNode node : thirdLevelNode.getCoverage()){
							try{
								br = new BufferedReader(new FileReader(new File(baseTrain,node.getNodeCode()+".mope.model")));
							}
							catch(FileNotFoundException e){
								//continue;
								br = new BufferedReader(new FileReader(new File(baseTrain,node.getParent().getNodeCode()+".mope.model")));
							}
							cv = DocumentClassVectorUtils.parseClassVectorSimpleFromString(br.readLine());
							br.close();
							System.gc();
							classVectors.put(node.getNodeCode(),cv);
							System.out.println((++i)+" "+node.getNodeCode()+" "+" "+cv.getName()+" "+cv.getThreshold());
						}
						System.out.println("Evaluating branch "+thirdLevelNode.getNodeCode());
						File svmfFile = new File(base,"test.tfidf.txt");
						br = new BufferedReader(new FileReader(svmfFile));
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
									
							//System.out.println("Evaluating document "+docIndex+" on branch "+thirdLevelNode.getNodeCode());
									
							ArrayList<String> results = new ArrayList<String>();
								
							for(TreeNode node : rootNode.getChildren()){
								evaluateAtNode(node, classVectors, dv, results);
							}
							
							String finalResult = "";
							if(!results.isEmpty()){
								for(String s : results){
									finalResult += " "+s;
								}
								finalResult = finalResult.trim();
							}
							
							fullResults[docIndex] = (fullResults[docIndex] + " " + finalResult).trim();
						}
						br.close();
						classVectors.clear();
						classVectors = null;
					}
				}
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(base,"results.txt")));
			for(String s : fullResults){
				if(s.equalsIgnoreCase("")){
					bw.write("30232\n");
				}
				else{
					bw.write(s+"\n");
				}
			}
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void evaluateAtNode(TreeNode node, Hashtable<String, ClassVectorSimple> classVectors, DocumentVector dv, ArrayList<String> results){
		try{
			ClassVectorSimple cv  = classVectors.get(node.getNodeCode());
		
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
		HierLSHTC2_Topic_1 main = new HierLSHTC2_Topic_1("/windows/d/BACKUP/Datasets/lshtc2/Task-1/cat_hier.txt");
		main.buildTree();
		main.showTree();
		//main.mapTermIds();
		//main.findMaxTermId();
		//main.tfidf();
		main.generateTrainSvmfFiles();
		//main.featureSelection();
		//main.generateTrainScripts();
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
