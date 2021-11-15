package utumno.mope.utils.hier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import utumno.mope.datastructures.ClassVector;
import utumno.mope.datastructures.DocumentVector;
import utumno.mope.utils.ClassUtils;
import utumno.mope.utils.DocumentClassVectorUtils;

public class HierReutersBIGInMain {
	
	private static final int numberOfTrainExamples = 23149;
	private static final int numberOfTestExamples = 781265;
	private static final double epsilon = Double.parseDouble("1.0E-16");
	
	private int totalNumberOfClassifiers = 0;
	
	private Hashtable<String, TreeNode> hashNameNodes;
	private Hashtable<String, TreeNode> hashCodeNodes;
	private File dataInFile;
	private TreeNode rootNode;
	private int maxLevel=1;
	
	public HierReutersBIGInMain(String dataInFileName){
		try{
			hashNameNodes = new Hashtable<String, TreeNode>();
			hashCodeNodes = new Hashtable<String, TreeNode>();
			dataInFile = new File(dataInFileName);
			rootNode = new TreeNode("root");
			rootNode.setNodeNameString("root");
			rootNode.setLeaf(false);
			rootNode.setLevel(0);
			hashNameNodes.put("root", rootNode);
			hashCodeNodes.put("root", rootNode);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}	
	}
	
	private void buildTree(){
		try{	
			BufferedReader br = new BufferedReader(new FileReader(this.dataInFile));
			String line = br.readLine();
			while((line=br.readLine())!=null){
				TreeNode node = new TreeNode(null);
				String[] data0 = line.split("cd:");
				String[] data = data0[0].split("\\s+");
				if(!ClassUtils.isReutersBIGInHier354Class(data[5].toLowerCase().trim())){
					continue;
				}
				node.setNodeNameString(data0[1].toLowerCase().trim());
				node.setNodeCodeString(data[5].toLowerCase().trim());
				String parentCode = data[1].toLowerCase().trim();
				TreeNode parentNode = hashCodeNodes.get(parentCode);
				parentNode.getChildren().add(node);
				parentNode.setLeaf(false);
				node.setParent(parentNode);
				node.setLevel(parentNode.getLevel()+1);
				if(node.getLevel()>maxLevel) 
					maxLevel = node.getLevel();
				hashCodeNodes.put(node.getNodeCodeString(), node);
				hashNameNodes.put(node.getNodeNameString(), node);
			}
			br.close();
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void generateTrainSvmfFiles(){
		try{
			String baseIn  = "P:\\BACKUP\\Datasets\\reutersBIGIn\\svmf\\354\\train";
			String baseOut = "P:\\BACKUP\\Datasets\\reutersBIGIn\\svmf\\354\\hier\\train";
			
			//Fill level 2-5 positive
			for(TreeNode node : rootNode.getAllDescendants()){
				if(node.getLevel()==1){
					continue;
				}
				Integer[] positiveExamples = new Integer[numberOfTrainExamples];
				System.out.print("Filling positive for "+node.getNodeNameString()+"\t"+node.getNodeCodeString()+"("+node.getLevel()+")"+(node.isLeaf()?"L":""));
				BufferedReader br = new BufferedReader(new FileReader(new File(baseIn, node.getNodeCodeString()+".train.svmf")));
				int pos = 0;
				for(int index=0; index<numberOfTrainExamples; index++){
					if(Double.parseDouble(br.readLine().split("\\s+")[0]) > 0){
						positiveExamples[index] = index;
					}
				}
				br.close();
				for(Integer k : positiveExamples){
					if(k!=null){
						pos++;
					}
				}
				System.out.println("\tpos="+pos);
				node.setPositiveExamples(positiveExamples);
			}
			
			//Fill level 1 positive
			for(int i=0; i<10; i++){
				Integer[] firstLevelNodePositiveExamples = new Integer[numberOfTrainExamples];
				TreeNode firstLevelNode = rootNode.findDescendant(new TreeNode("i"+i));
				System.out.print("Filling positive for "+firstLevelNode.getNodeNameString()+"\t"+firstLevelNode.getNodeCodeString()+"("+firstLevelNode.getLevel()+")"+(firstLevelNode.isLeaf()?"L":""));
				int pos = 0;
				for(TreeNode descendant : firstLevelNode.getAllDescendants()){
					Integer[] descendantPositiveExamples = descendant.getPositiveExamples();
					for(int index=0; index<numberOfTrainExamples; index++){
						if(descendantPositiveExamples[index]!=null){
							firstLevelNodePositiveExamples[index] = descendantPositiveExamples[index];
						}	
					}
				}
				for(Integer k : firstLevelNodePositiveExamples){
					if(k!=null){
						pos++;
					}
				}
				System.out.println("\tpos="+pos);
				firstLevelNode.setPositiveExamples(firstLevelNodePositiveExamples);
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
			/*
			//Fill negative 2 == Pos(Siblings(Node(i))+Pos(Parent(Node(i)))
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
				Integer[] parentPositiveExamples = node.getParent().getPositiveExamples();
				if(node.getLevel()>1){
					for(int k=0; k<numberOfTrainExamples; k++){
						if(parentPositiveExamples[k]!=null){
							negativeExamples[k] = parentPositiveExamples[k];
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
			/*
			//Fill negative 3 == FirstLevelNodePos(Node(i))
			for(TreeNode node : rootNode.getAllDescendants()){
				Integer[] negativeExamples= new Integer[numberOfTrainExamples];
				int neg = 0;
				
				if(node.getLevel()!=1){
					System.out.print("Filling negative for "+node.getNodeNameString()+"\t"+node.getNodeCodeString()+"("+node.getLevel()+")"+(node.isLeaf()?"L":""));
					Integer[] positiveExamples= node.getPositiveExamples();
					
					for(TreeNode ancestor : node.getAllAncestors()){
						if(ancestor.getLevel()==1){
							Integer[] firstLevelAncestorPositiveExamples = ancestor.getPositiveExamples();
							for(int k=0; k<numberOfTrainExamples; k++){
								if(firstLevelAncestorPositiveExamples[k]!=null && positiveExamples[k]==null){
									negativeExamples[k] = firstLevelAncestorPositiveExamples[k];
								}
							}
						}
					}
				}
				else{
					for(TreeNode sibling : node.getAllSiblings()){
						Integer[] siblingPositiveExamples = sibling.getPositiveExamples();
						for(int k=0; k<numberOfTrainExamples; k++){
							if(siblingPositiveExamples[k]!=null){
								negativeExamples[k] = siblingPositiveExamples[k];
							}
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
			
			
			//Generate SVMF files
			for(TreeNode node : rootNode.getAllDescendants()){
				Integer[] pE = node.getPositiveExamples();
				Integer[] nE = node.getNegativeExamples();
				System.out.println("Writing svmf file for "+node.getNodeCodeString());
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(baseOut,node.getNodeCodeString()+".train.svmf")));
				BufferedReader br = new BufferedReader(new FileReader(new File(baseIn, "i50000.train.svmf")));
				for(int index=0; index<numberOfTrainExamples; index++){
					String[] data = br.readLine().split("\\s+");
					if(pE[index]==null && nE[index]==null) {
						continue;
					}
					else if(pE[index]!=null && nE[index]!=null) {
						bw.write("+1 ");
					}
					else if(pE[index]!=null) bw.write("+1 ");
					else if(nE[index]!=null) bw.write("-1 ");
					
					for(int k=1; k<data.length; k++){
						bw.write(data[k]+" ");
					}
					bw.write("\n");
				}
				br.close();
				bw.close();
				totalNumberOfClassifiers++;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void generateQrelsFiles(){
		try{
			String baseTest  = "P:\\BACKUP\\Datasets\\reutersBIGIn\\svmf\\354\\test";
			
			ArrayList<String> classes = ClassUtils.reutersBIGInHier354Classes;
			int j = 0;
			for(String cat : classes){
				System.out.println("Generating qrels file for "+cat+" "+(++j)+"/"+classes.size());
				if(cat.equalsIgnoreCase("i0") || cat.equalsIgnoreCase("i1") || cat.equalsIgnoreCase("i2") ||
				   cat.equalsIgnoreCase("i3") || cat.equalsIgnoreCase("i4") || cat.equalsIgnoreCase("i5") || cat.equalsIgnoreCase("i6") ||
				   cat.equalsIgnoreCase("i7") || cat.equalsIgnoreCase("i8") || cat.equalsIgnoreCase("i9"))
					continue;
				BufferedReader br = new BufferedReader(new FileReader(new File(baseTest,cat+".test.svmf")));
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(baseTest,cat+".test.qrels")));
				String line = null;
				while((line=br.readLine())!=null){
					String[] data = line.split("\\s+");
					Double value = Double.parseDouble(data[0]);
					if(value>0) bw.write("+1\n");
					else		bw.write("-1\n");
				}
				bw.close();
				br.close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void evaluate(){
		try{
			String baseTrain = "P:\\BACKUP\\Datasets\\reutersBIGIn\\svmf\\354\\hier\\train-1";
			String baseTest  = "P:\\BACKUP\\Datasets\\reutersBIGIn\\svmf\\354\\hier\\test";
			
			Hashtable<String, ClassVector> classVectors = new Hashtable<String, ClassVector>();
			ArrayList<String> classes = ClassUtils.reutersBIGInHier354Classes;
			for(String cat : classes){
				BufferedReader br = new BufferedReader(new FileReader(new File(baseTrain,cat+".rh.model")));
				ClassVector cv = DocumentClassVectorUtils.parseClassVectorFromString(br.readLine());
				br.close();
				classVectors.put(cat,cv);
				System.out.println(cat+" "+" "+cv.getName()+" "+cv.getThreshold());
			}
			
			Hashtable<String, Boolean[]> trueClassLabels = new Hashtable<String, Boolean[]>();
			int j = 0;
			for(String cat : classes){
				System.out.println("Loading true labels for "+cat+" "+(++j)+"/"+classes.size());
				if(cat.equalsIgnoreCase("i0") || cat.equalsIgnoreCase("i1") || cat.equalsIgnoreCase("i2") ||
				   cat.equalsIgnoreCase("i3") || cat.equalsIgnoreCase("i4") || cat.equalsIgnoreCase("i5") || cat.equalsIgnoreCase("i6") ||
				   cat.equalsIgnoreCase("i7") || cat.equalsIgnoreCase("i8") || cat.equalsIgnoreCase("i9"))
					continue;
				
				Boolean[] trueLabels = new Boolean[numberOfTestExamples];
				BufferedReader br = new BufferedReader(new FileReader(new File(baseTest,cat+".test.qrels")));
				String line = null;
				int counter = 0;
				while((line=br.readLine())!=null){
					if(Double.parseDouble(line) > 0){
						trueLabels[counter++] = true;
					}
					else{
						trueLabels[counter++] = false;
					}
				}
				br.close();
				trueClassLabels.put(cat, trueLabels);
			}
			
			//Evaluation
			File svmfFile = new File(baseTest,"i01000.test.svmf");
			BufferedReader br = new BufferedReader(new FileReader(svmfFile));
			String line = null;
			int docIndex = -1;
			Object[] Nn = new Object[2];
			Nn[0] = 0;
			Nn[1] = Integer.MIN_VALUE;
			int nonpd = 0;
			while((line=br.readLine())!=null){
				docIndex++;
				DocumentVector dv = DocumentClassVectorUtils.parseDocumentVectorFromString(line,Nn);
				if(dv.getDocId() == null){
					dv.setDocId(String.valueOf(docIndex));
				}
				
				System.out.println("Evaluating document "+docIndex);
				for(TreeNode firstLevelNode : rootNode.getChildren()){
					nonpd += evaluateAtNode(firstLevelNode, classVectors, trueClassLabels, dv, docIndex);
				}
			}
			br.close();
			
			//microF1
			int a,b,c;
			a=b=c=0;
			for(TreeNode node : rootNode.getAllDescendants()){
				if(node.getLevel()==1) continue;
				a += node.getA();
				b += node.getB();
				c += node.getC();
			}
			System.out.println(a+" "+b+" "+c);
			double p = (double)a/(double)(a+b);
			double r = (double)a/(double)(a+c);
			double microF1 = (double)2.0*p*r/(p+r);
			System.out.println("microp="+p);
			System.out.println("micror="+r);
			System.out.println("microF1="+microF1);
			
			//macroF1
			double P,R,macroF1;
			P=R=macroF1=0.0;
			int classCounter = 0;
			for(TreeNode node : rootNode.getAllDescendants()){
				if(node.getLevel()==1) continue;
				double localP = (double)node.getA()/(double)(node.getA()+node.getB());
				double localR = (double)node.getA()/(double)(node.getA()+node.getC());
				if(node.getA()+node.getB()==0) localP = 0.0;
				if(node.getA()+node.getC()==0) localR = 0.0;
				P += localP;
				R += localR;
				classCounter++;
			}
			macroF1 = ((double)2.0*P*R)/(P+R);
			macroF1 = macroF1 / (double) classCounter;
			System.out.println("macroP="+(P/(double) classCounter));
			System.out.println("macroR="+(R/(double) classCounter));
			System.out.println("macroF1="+macroF1);
			System.out.println("anonpd="+((double)nonpd/(double)numberOfTestExamples));
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void evaluateFlat(){
		try{
			String baseTrain = "P:\\BACKUP\\Datasets\\reutersBIGIn\\svmf\\354\\hier\\train-4";
			String baseTest  = "P:\\BACKUP\\Datasets\\reutersBIGIn\\svmf\\354\\hier\\test";
			
			Hashtable<String, ClassVector> classVectors = new Hashtable<String, ClassVector>();
			ArrayList<String> classes = ClassUtils.reutersBIGInHier354Classes;
			for(String cat : classes){
				BufferedReader br = new BufferedReader(new FileReader(new File(baseTrain,cat+".rh.model")));
				ClassVector cv = DocumentClassVectorUtils.parseClassVectorFromString(br.readLine());
				br.close();
				classVectors.put(cat,cv);
				System.out.println(cat+" "+" "+cv.getName()+" "+cv.getThreshold());
			}
			
			Hashtable<String, Boolean[]> trueClassLabels = new Hashtable<String, Boolean[]>();
			int j = 0;
			for(String cat : classes){
				System.out.println("Loading true labels for "+cat+" "+(++j)+"/"+classes.size());
				if(cat.equalsIgnoreCase("i0") || cat.equalsIgnoreCase("i1") || cat.equalsIgnoreCase("i2") ||
				   cat.equalsIgnoreCase("i3") || cat.equalsIgnoreCase("i4") || cat.equalsIgnoreCase("i5") || cat.equalsIgnoreCase("i6") ||
				   cat.equalsIgnoreCase("i7") || cat.equalsIgnoreCase("i8") || cat.equalsIgnoreCase("i9"))
					continue;
				
				Boolean[] trueLabels = new Boolean[numberOfTestExamples];
				BufferedReader br = new BufferedReader(new FileReader(new File(baseTest,cat+".test.qrels")));
				String line = null;
				int counter = 0;
				while((line=br.readLine())!=null){
					if(Double.parseDouble(line) > 0){
						trueLabels[counter++] = true;
					}
					else{
						trueLabels[counter++] = false;
					}
				}
				br.close();
				trueClassLabels.put(cat, trueLabels);
			}
			
			//Evaluation
			File svmfFile = new File(baseTest,"i01000.test.svmf");
			BufferedReader br = new BufferedReader(new FileReader(svmfFile));
			String line = null;
			int docIndex = -1;
			Object[] Nn = new Object[2];
			Nn[0] = 0;
			Nn[1] = Integer.MIN_VALUE;
			while((line=br.readLine())!=null){
				docIndex++;
				DocumentVector dv = DocumentClassVectorUtils.parseDocumentVectorFromString(line,Nn);
				if(dv.getDocId() == null){
					dv.setDocId(String.valueOf(docIndex));
				}
				
				System.out.println("Evaluating document "+docIndex);
				for(TreeNode node : rootNode.getAllDescendants()){
					evaluateAtNodeFlat(node, classVectors, trueClassLabels, dv, docIndex);
				}
			}
			br.close();
			
			//microF1
			int a,b,c;
			a=b=c=0;
			for(TreeNode node : rootNode.getAllDescendants()){
				if(node.getLevel()==1) continue;
				a += node.getA();
				b += node.getB();
				c += node.getC();
			}
			System.out.println(a+" "+b+" "+c);
			double p = (double)a/(double)(a+b);
			double r = (double)a/(double)(a+c);
			double microF1 = (double)2.0*p*r/(p+r);
			System.out.println("microp="+p);
			System.out.println("micror="+r);
			System.out.println("microF1="+microF1);
			
			//macroF1
			double P,R,macroF1;
			P=R=macroF1=0.0;
			int classCounter = 0;
			for(TreeNode node : rootNode.getAllDescendants()){
				if(node.getLevel()==1) continue;
				double localP = (double)node.getA()/(double)(node.getA()+node.getB());
				double localR = (double)node.getA()/(double)(node.getA()+node.getC());
				if(node.getA()+node.getB()==0) localP = 0.0;
				if(node.getA()+node.getC()==0) localR = 0.0;
				P += localP;
				R += localR;
				classCounter++;
			}
			macroF1 = ((double)2.0*P*R)/(P+R);
			macroF1 = macroF1 / (double) classCounter;
			System.out.println("macroP="+(P/(double) classCounter));
			System.out.println("macroR="+(R/(double) classCounter));
			System.out.println("macroF1="+macroF1);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private int evaluateAtNode(TreeNode node, Hashtable<String, ClassVector> classVectors, Hashtable<String, Boolean[]> trueClassLabels, DocumentVector dv, int docIndex){
		int nonpd = 1;
		ClassVector cv  = classVectors.get(node.getNodeCodeString());
		
		double similarity = cv.similarityWithDocumentVector(dv);
		
		if(similarity < cv.getThreshold()){
			if(node.getLevel()!=1 && trueClassLabels.get(node.getNodeCodeString())[docIndex]==true){
				node.setC(node.getC()+1);
			}
			for(TreeNode descendant : node.getAllDescendants()){
				if(node.getLevel()!=1 && trueClassLabels.get(descendant.getNodeCodeString())[docIndex]==true){
					descendant.setC(descendant.getC()+1);
				}
			}
		}
		else if(similarity > cv.getThreshold() || Math.abs(similarity-cv.getThreshold()) < epsilon){
			if(node.getLevel()!=1 && trueClassLabels.get(node.getNodeCodeString())[docIndex]==true){
				node.setA(node.getA()+1);	
			}
			else if(node.getLevel()!=1 && trueClassLabels.get(node.getNodeCodeString())[docIndex]==false){
				node.setB(node.getB()+1);
			}
			for(TreeNode child : node.getChildren()){
				nonpd += evaluateAtNode(child, classVectors, trueClassLabels, dv, docIndex);
			}
		}
		return nonpd;
	}
	
	private void evaluateAtNodeFlat(TreeNode node, Hashtable<String, ClassVector> classVectors, Hashtable<String, Boolean[]> trueClassLabels, DocumentVector dv, int docIndex){
		ClassVector cv  = classVectors.get(node.getNodeCodeString());
		
		double similarity = cv.similarityWithDocumentVector(dv);
		
		if(similarity < cv.getThreshold()){
			if(node.getLevel()!=1 && trueClassLabels.get(node.getNodeCodeString())[docIndex]==true){
				node.setC(node.getC()+1);
			}
		}
		else if(similarity > cv.getThreshold() || Math.abs(similarity-cv.getThreshold()) < epsilon){
			if(node.getLevel()!=1 && trueClassLabels.get(node.getNodeCodeString())[docIndex]==true){
				node.setA(node.getA()+1);	
			}
			else if(node.getLevel()!=1 && trueClassLabels.get(node.getNodeCodeString())[docIndex]==false){
				node.setB(node.getB()+1);
			}
		}
	}
	
	private void showTree(){
		try{
			System.out.println(rootNode.showNode());
			System.out.println();
			System.out.print("Nodes="+rootNode.getAllDescendants().size());
			System.out.print("\tLeaves="+rootNode.countAllLeafDescendants());
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		HierReutersBIGInMain main = new HierReutersBIGInMain(args[0]);
		main.buildTree();
		main.showTree();
		//main.generateTrainSvmfFiles();
		//main.showTree();
		//main.generateQrelsFiles();
		main.evaluate();
		//main.evaluateFlat();
	}

}
