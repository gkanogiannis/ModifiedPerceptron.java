/*
 *
 * ModifiedPerceptron.java utumno.mope.Evaluator
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
package utumno.mope;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;

import utumno.mope.datastructures.ClassVector;
import utumno.mope.datastructures.DocumentVector;
import utumno.mope.datastructures.SimilarityNodeSimple;
import utumno.mope.utils.DocumentClassVectorUtils;
import utumno.mope.utils.MyThread;
import utumno.mope.utils.ProgressHandler;
import utumno.mope.utils.ext.PrecisionRecallEvaluation;
import utumno.mope.utils.ext.ScoredPrecisionRecallEvaluation;

public class Evaluator extends MyThread{
	//Number of train examples
	private int[] numberOfTestExamples;
	
	//Train terms
	private HashSet<Integer> testFeaturesIds;
	
	//Max train feature id.
	private int[] maxTestFeaturesId;
	
	//Input svmf location.
	private File inData;
	
	//Input cv location.
	private File inModel;
	
	//Output predictions location.
	private File outPred;
	
	//Output txt file.
	private BufferedWriter outTxt;
	
	//ArrayList holding the DocumentVectors for the current class
	private ArrayList<DocumentVector> documentVectors;
	
	//ClassVector for the current class
	private ClassVector classVector;
	
	private static NumberFormat nf = NumberFormat.getInstance();
	static{
		nf.setGroupingUsed(true);
		nf.setMaximumFractionDigits(8);
		nf.setMinimumFractionDigits(8);
		nf.setMaximumIntegerDigits(1);
		nf.setMinimumIntegerDigits(1);
	}
	
	private int TP = 0;//a
	private int FP = 0;//b
	private int FN = 0;//c
	private int TN = 0;//d
	
	public Evaluator(CommandLine options, PrintStream printstream, Thread.UncaughtExceptionHandler errorHandler, ProgressHandler progressHandler){
		this.options = options;
		this.printstream = printstream;
		this.errorHandler = errorHandler;
		this.progressHandler = progressHandler;
	}
	
	public void run() {
		try{
			long wallStartTime = System.currentTimeMillis();
			
			long userStartTimeNano   = getUserTime( );
			long systemStartTimeNano = getSystemTime( );
			long cpuStartTimeNano = getCpuTime( );
			
			initialize();
			
			printstream.println(options.getOptionValue("inData"));
			outTxt.write(options.getOptionValue("inData"));
			outTxt.write(System.getProperty("line.separator"));
			
			getClassVector();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(outPred));
			ArrayList<SimilarityNodeSimple> sNodeList = new ArrayList<SimilarityNodeSimple>();
			
			BufferedReader br = new BufferedReader(new FileReader(inData));
			String line = "";
			int i = 0;
			while((line=br.readLine())!=null){
				documentVectors = DocumentClassVectorUtils.getDocumentVectors(br, line, false, numberOfTestExamples, testFeaturesIds, maxTestFeaturesId);
				printstream.println("Pass "+(++i)+" docs="+documentVectors.size());
				for(DocumentVector documentVector : documentVectors){
					float similarity = classVector.similarityWithDocumentVector(documentVector);
					bw.write(String.valueOf(similarity));
					bw.write(System.getProperty("line.separator"));
					sNodeList.add(new SimilarityNodeSimple(similarity, documentVector.isPositiveExample()));
					if(Math.abs(similarity - classVector.getThreshold()) < Trainer.epsilon){
						 if(documentVector.isPositiveExample()){
							 TP++;
			             }
			             else{
			                 TN++;
			             }
					}
					else if(similarity > classVector.getThreshold()){
		                if(documentVector.isPositiveExample()){
		                    TP++;
		                }
		                else{
		                    FP++;
		                }
		            }
		            else if(similarity < classVector.getThreshold()){
		                if(documentVector.isPositiveExample()){
		                    FN++;
		                }
		                else{
		                    TN++;
		                }
		            }
				}
				documentVectors = null;
				System.gc();
			}
			br.close();
			bw.close();
			
			printstream.println("Number of TestExamples="+numberOfTestExamples[0]+" , Number of TestFeatures="+testFeaturesIds.size()+" , MaxTestFeaturesId="+maxTestFeaturesId[0]);
			outTxt.write("Number of TestExamples="+numberOfTestExamples[0]+" , Number of TestFeatures="+testFeaturesIds.size()+" , MaxTestFeaturesId="+maxTestFeaturesId[0]);
			outTxt.write(System.getProperty("line.separator"));
			
			printstream.println("F1="+nf.format(computeF1())+"\tTP="+TP+"\tFN="+FN+"\tFP="+FP+"\tTN="+TN);
			outTxt.write("F1="+nf.format(computeF1())+"\tTP="+TP+"\tFN="+FN+"\tFP="+FP+"\tTN="+TN);
			outTxt.write(System.getProperty("line.separator"));
			
			long wallEndTime = System.currentTimeMillis();
			long wallTime = wallEndTime - wallStartTime;
			
			long userTimeNano    = getUserTime( )   - userStartTimeNano;
			long systemTimeNano  = getSystemTime( ) - systemStartTimeNano;
			long cpuTimeNano     = getCpuTime( )    - cpuStartTimeNano;
			
			//Evaluations
			PrecisionRecallEvaluation pre = new PrecisionRecallEvaluation(TP,FN,FP,TN);
			
			ScoredPrecisionRecallEvaluation spre = new ScoredPrecisionRecallEvaluation();
			for(SimilarityNodeSimple sNode : sNodeList){
				spre.addCase(sNode.isPositiveExample(),sNode.getSimilarity());
			}
			
			printstream.println("\n\tDetailed Evaluation");
			printstream.println("---------------------------------------");
			printstream.println(pre.toString());
			printstream.println("- - - - - - - - - - - - - - - - - - - -");
			printstream.println(spre.toString());
			outTxt.write("\n\tDetailed Evaluation");
			outTxt.write(System.getProperty("line.separator"));
			outTxt.write("---------------------------------------");
			outTxt.write(System.getProperty("line.separator"));
			outTxt.write(pre.toString());
			outTxt.write(System.getProperty("line.separator"));
			outTxt.write("- - - - - - - - - - - - - - - - - - - -");
			outTxt.write(System.getProperty("line.separator"));
			outTxt.write(spre.toString());
			outTxt.write(System.getProperty("line.separator"));
			
			
			printstream.println("");
			printstream.println("WallTime="+wallTime/1000.0+" sec");
			printstream.println("");
			outTxt.write(System.getProperty("line.separator"));
			outTxt.write("WallTime="+wallTime/1000.0+" sec");
			outTxt.write(System.getProperty("line.separator"));
			outTxt.write(System.getProperty("line.separator"));
			
			printstream.println("UserTime="+userTimeNano/1000000000.0+" sec");
			printstream.println("SystemTime="+systemTimeNano/1000000000.0+" sec");
			printstream.println("CpuTime="+cpuTimeNano/1000000000.0+" sec");
			printstream.println("");
			outTxt.write("UserTime="+userTimeNano/1000000000.0+" sec");
			outTxt.write(System.getProperty("line.separator"));
			outTxt.write("SystemTime="+systemTimeNano/1000000000.0+" sec");
			outTxt.write(System.getProperty("line.separator"));
			outTxt.write("CpuTime="+cpuTimeNano/1000000000.0+" sec");
			outTxt.write(System.getProperty("line.separator"));
			
		}
		catch(Exception e){
			throw new Error(e);
		}
		finalize();
	}
	
	private void initialize(){
		try{
			inData = new File(options.getOptionValue("inData"));
			
			inModel = new File(options.getOptionValue("inModel"));
			
			if(options.getOptionValue("outPred")!=null){
				outPred = new File(options.getOptionValue("outPred"));
			}
			else{
				outPred = new File("outPred-test.pred");
			}
			outPred.delete();
			
			if(options.getOptionValue("outTxt")!=null){
				(new File(options.getOptionValue("outTxt"))).delete();
				outTxt = new BufferedWriter(new FileWriter(new File(options.getOptionValue("outTxt"))));
			}
			else{
				(new File("outTxt")).delete();
				outTxt = new BufferedWriter(new FileWriter(new File("outTxt-test.txt")));
			}
			
			testFeaturesIds = new HashSet<Integer>();
			testFeaturesIds = new HashSet<Integer>();
			numberOfTestExamples = new int[1];
			numberOfTestExamples[0] = 0;
			maxTestFeaturesId = new int[1];
			maxTestFeaturesId[0] = 0;
		}
		catch(Exception e){
			throw new Error(e);
		}
	}
	
	private void getClassVector(){
		try{
			BufferedReader br = new BufferedReader(new FileReader(inModel));
			String line = br.readLine();
			classVector = DocumentClassVectorUtils.parseClassVectorFromString(line);
			br.close();
		}
		catch(Exception e){
			throw new Error(e);
		}
	}

	public float computeF1(){
		float P = 0.0f;
		float R = 0.0f;
		float F1 = 0.0f;
		if(TP+FP!=0){
			P = (float)TP / (float)(TP+FP);
		}
		if(TP+FN!=0){
			R = (float)TP / (float)(TP+FN);
		}
		if(P+R!=0.0){
			F1 = 2.0f*P*R / (P+R);
		}
		return F1;
	}
	
	public void finalize(){
		try{
			outTxt.flush();
			outTxt.close();
		}
		catch(Exception e){
			throw new Error(e);
		}
	}
	
	
}
