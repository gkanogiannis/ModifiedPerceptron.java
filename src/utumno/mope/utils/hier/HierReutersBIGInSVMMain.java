/*
 *
 * ModifiedPerceptron.java utumno.mope.utils.hier.HierReutersBIGInSVMMain
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
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import utumno.mope.datastructures.ClassVector;
import utumno.mope.datastructures.DocumentVector;
import utumno.mope.utils.ClassUtils;
import utumno.mope.utils.DocumentClassVectorUtils;

public class HierReutersBIGInSVMMain {
	
	private static final int numberOfTrainExamples = 23149;
	private static final int numberOfTestExamples = 781265;
	private static final double epsilon = Double.parseDouble("1.0E-16");
	
	private int totalNumberOfClassifiers = 0;
	
	private Hashtable<String, TreeNode> hashNameNodes;
	private Hashtable<String, TreeNode> hashCodeNodes;
	private File dataInFile;
	private TreeNode rootNode;
	private int maxLevel=1;
	
	public HierReutersBIGInSVMMain(String dataInFileName){
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
		
	private void evaluate(){
		try{
			String baseTrain = "P:\\BACKUP\\Datasets\\reutersBIGIn\\svmf\\354\\hier\\train-1";
			String baseTest  = "P:\\BACKUP\\Datasets\\reutersBIGIn\\svmf\\354\\hier\\test";
			
			Hashtable<String, Double> classThresholds = new Hashtable<String, Double>();
			ArrayList<String> classes = ClassUtils.reutersBIGInHier354Classes;
			int j = 0;
			for(String cat : classes){
				System.out.println("Loading threshold for "+cat+" "+(++j)+"/"+classes.size());
				BufferedReader br = new BufferedReader(new FileReader(new File(baseTrain,cat+".svmperf.model")));
				String line = null;
				while((line=br.readLine())!=null){
					if(line.contains("threshold b")){
						double b = Double.parseDouble(line.split("#")[0].trim());
						classThresholds.put(cat, b);
						break;
					}
				}
				br.close();
			}
			
			Hashtable<String, Boolean[]> systemClassLabels = new Hashtable<String, Boolean[]>();
			j=0;
			for(String cat : classes){
				System.out.println("Loading system labels for "+cat+" "+(++j)+"/"+classes.size());
				Boolean[] systemLabels = new Boolean[numberOfTestExamples];
				BufferedReader br = new BufferedReader(new FileReader(new File(baseTrain,cat+".svmperf.pred")));
				String line = null;
				int counter = 0;
				while((line=br.readLine())!=null){
					if(Double.parseDouble(line) > classThresholds.get(cat) || Math.abs(Double.parseDouble(line)-classThresholds.get(cat)) < epsilon){
						systemLabels[counter++] = true;
					}
					else{
						systemLabels[counter++] = false;
					}
				}
				br.close();
				systemClassLabels.put(cat, systemLabels);
			}
			
			Hashtable<String, Boolean[]> trueClassLabels = new Hashtable<String, Boolean[]>();
			j = 0;
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
			int nonpd = 0;
			for(int docIndex=0; docIndex<numberOfTestExamples; docIndex++){
				System.out.println("Evaluating document "+docIndex);
				for(TreeNode node : rootNode.getChildren()){
					nonpd += evaluateAtNode(node, systemClassLabels, trueClassLabels, docIndex);
				}
			}
			
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
			String baseTrain = "P:\\BACKUP\\Datasets\\reutersBIGIn\\svmf\\354\\hier\\train-1";
			String baseTest  = "P:\\BACKUP\\Datasets\\reutersBIGIn\\svmf\\354\\hier\\test";
			
			ArrayList<String> classes = ClassUtils.reutersBIGInHier354Classes;
			
			Hashtable<String, Boolean[]> systemClassLabels = new Hashtable<String, Boolean[]>();
			int j=0;
			for(String cat : classes){
				System.out.println("Loading system labels for "+cat+" "+(++j)+"/"+classes.size());
				Boolean[] systemLabels = new Boolean[numberOfTestExamples];
				BufferedReader br = new BufferedReader(new FileReader(new File(baseTrain,cat+".svmperf.pred")));
				String line = null;
				int counter = 0;
				while((line=br.readLine())!=null){
					if(Double.parseDouble(line) >= 0 ){
						systemLabels[counter++] = true;						
					}
					else{
						systemLabels[counter++] = false;
					}
				}
				br.close();
				systemClassLabels.put(cat, systemLabels);
			}
			
			Hashtable<String, Boolean[]> trueClassLabels = new Hashtable<String, Boolean[]>();
			j = 0;
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
			for(int docIndex=0; docIndex<numberOfTestExamples; docIndex++){
				System.out.println("Evaluating document "+docIndex);
				for(TreeNode node : rootNode.getAllDescendants()){
					evaluateAtNodeFlat(node, systemClassLabels, trueClassLabels, docIndex);
				}
			}
			
			//microF1
			int a,b,c;
			a=0;
		    b=0;
		    c=0;
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
	
	private int evaluateAtNode(TreeNode node, Hashtable<String, Boolean[]> systemClassVectors, Hashtable<String, Boolean[]> trueClassLabels, int docIndex){
		int nonpd = 1;
		
		if(systemClassVectors.get(node.getNodeCodeString())[docIndex]==false){
			if(node.getLevel()!=1 && trueClassLabels.get(node.getNodeCodeString())[docIndex]==true){
				node.setC(node.getC()+1);
			}
			for(TreeNode descendant : node.getAllDescendants()){
				if(node.getLevel()!=1 && trueClassLabels.get(descendant.getNodeCodeString())[docIndex]==true){
					descendant.setC(descendant.getC()+1);
				}
			}
		}
		else if(systemClassVectors.get(node.getNodeCodeString())[docIndex]==true){
			if(node.getLevel()!=1 && trueClassLabels.get(node.getNodeCodeString())[docIndex]==true){
				node.setA(node.getA()+1);	
			}
			else if(node.getLevel()!=1 && trueClassLabels.get(node.getNodeCodeString())[docIndex]==false){
				node.setB(node.getB()+1);
			}
			for(TreeNode child : node.getChildren()){
				nonpd += evaluateAtNode(child, systemClassVectors, trueClassLabels, docIndex);
			}
		}
		return nonpd;
	}
	
	private void evaluateAtNodeFlat(TreeNode node, Hashtable<String, Boolean[]> systemClassLabels, Hashtable<String, Boolean[]> trueClassLabels, int docIndex){
		if(systemClassLabels.get(node.getNodeCodeString())[docIndex]==false){
			if(node.getLevel()!=1 && trueClassLabels.get(node.getNodeCodeString())[docIndex]==true){
				node.setC(node.getC()+1);
			}
		}
		else if(systemClassLabels.get(node.getNodeCodeString())[docIndex]==true){
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
		HierReutersBIGInSVMMain main = new HierReutersBIGInSVMMain(args[0]);
		main.buildTree();
		main.showTree();
		main.evaluate();
		//main.evaluateFlat();
	}

}
