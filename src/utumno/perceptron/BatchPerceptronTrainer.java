/*
 *
 * ModifiedPerceptron.java utumno.perceptron.BatchPerceptronTrainer
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
package utumno.perceptron;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;

import utumno.mope.Thresholder;
import utumno.mope.datastructures.ClassVector;
import utumno.mope.datastructures.DocumentVector;
import utumno.mope.datastructures.VectorNode;
import utumno.mope.utils.DocumentClassVectorUtils;
import utumno.mope.utils.MyThread;
import utumno.mope.utils.ProgressHandler;

public class BatchPerceptronTrainer extends MyThread{
	//Number of train examples
	private int[] numberOfTrainExamples;
	
	//Train terms
	private HashSet<Integer> trainFeaturesIds;
	
	//Max train feature id.
	private int[] maxTrainFeaturesId;
	
	//Input svmf location.
	private File inData;
	
	//Output model location.
	private File outModel;
	
	//Output txt file.
	private BufferedWriter outTxt;
	
	//Number of iterations.
	private int iterations;
	
	//Verbose messages
	private boolean verbose;
	
	//ArrayList holding the DocumentVectors for the current class
	private ArrayList<DocumentVector> documentVectors;
	
	//ClassVector for the current class
	private ClassVector classVector;
	
	public BatchPerceptronTrainer(CommandLine options, PrintStream printstream, Thread.UncaughtExceptionHandler errorHandler, ProgressHandler progressHandler){
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
			
			BufferedReader br = new BufferedReader(new FileReader(inData));
			//read all dovuments vectors at once
			documentVectors = DocumentClassVectorUtils.getDocumentVectors(br,"",true,numberOfTrainExamples,trainFeaturesIds,maxTrainFeaturesId);
			br.close();
			
			printstream.println("Number of TrainExamples="+numberOfTrainExamples[0]+" , Number of TrainFeatures="+trainFeaturesIds.size()+" , MaxTrainFeaturesId="+maxTrainFeaturesId[0]);
			outTxt.write("Number of TrainExamples="+numberOfTrainExamples[0]+" , Number of TrainFeatures="+trainFeaturesIds.size()+" , MaxTrainFeaturesId="+maxTrainFeaturesId[0]);
			outTxt.write(System.getProperty("line.separator"));
			
			classVector = new ClassVector();
			classVector.setName(options.getOptionValue("inData"));
			classVector.setNodes(new VectorNode[maxTrainFeaturesId[0]]);
			
			ClassVector dummy = new ClassVector();
			dummy.setName("dummy");
			dummy.setNodes(new VectorNode[maxTrainFeaturesId[0]]);
			
			for(DocumentVector documentVector : documentVectors){
				if(documentVector.isPositiveExample()){
					classVector.addWithDocumentVector(documentVector, 1.0f);
					classVector.setSize(classVector.getSize()+1);
				}
				else{
					dummy.addWithDocumentVector(documentVector, 1.0f);
					dummy.setSize(dummy.getSize()+1);
				}
			}
			
			printstream.println("Positive examples="+classVector.getSize());
			outTxt.write("Positive examples="+classVector.getSize());
			outTxt.write(System.getProperty("line.separator"));
			printstream.println("Negative examples="+dummy.getSize());
			outTxt.write("Negative examples="+dummy.getSize());
			outTxt.write(System.getProperty("line.separator"));
			
			
			classVector.divide(classVector.getSize());
			dummy.divide(numberOfTrainExamples[0]-classVector.getSize());
				
			classVector.addWithClassVector(dummy, -1.0f);
			
			classVector.divide(classVector.computeNorm());
			
			if(iterations > 0){
				ClassVector maxClassVector = classVector.clone();
				
				Thresholder thresholder = new Thresholder(maxClassVector,documentVectors);
				
				//Hyperplane for recall=1
				thresholder.computeThreshold(0);
				
				BatchPerceptron bape = new BatchPerceptron(maxClassVector,documentVectors,iterations,printstream,outTxt,verbose);
				
				maxClassVector = bape.executeAdjust();
							
				if(maxClassVector==null){
					maxClassVector = classVector;
				}
				
				DocumentClassVectorUtils.saveClassVector(maxClassVector, outModel);
			}
			else{
				(new Thresholder(classVector,documentVectors)).computeThreshold(0);
				DocumentClassVectorUtils.saveClassVector(classVector, outModel);
			}
			
			long wallEndTime = System.currentTimeMillis();
			long wallTime = wallEndTime - wallStartTime;
			
			long userTimeNano    = getUserTime( )   - userStartTimeNano;
			long systemTimeNano  = getSystemTime( ) - systemStartTimeNano;
			long cpuTimeNano     = getCpuTime( )    - cpuStartTimeNano;
			
			
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
			
			if(options.getOptionValue("outModel")!=null){
				outModel = new File(options.getOptionValue("outModel"));
			}
			else{
				outModel = new File("outModel-train.model");
			}
			outModel.delete();
			
			if(options.getOptionValue("outTxt")!=null){
				(new File(options.getOptionValue("outTxt"))).delete();
				outTxt = new BufferedWriter(new FileWriter(new File(options.getOptionValue("outTxt"))));
			}
			else{
				(new File("outTxt")).delete();
				outTxt = new BufferedWriter(new FileWriter(new File("outTxt-train.txt")));
			}
			
			iterations = Integer.parseInt(options.getOptionValue("iter"));
			
			verbose = options.hasOption("verbose");
			
			trainFeaturesIds = new HashSet<Integer>();
			numberOfTrainExamples = new int[1];
			numberOfTrainExamples[0] = 0;
			maxTrainFeaturesId = new int[1];
			maxTrainFeaturesId[0] = 0;
		}
		catch(Exception e){
			throw new Error(e);
		}
	}
	
	public void finalize(){
		try{
			outTxt.close();
		}
		catch(Exception e){
			throw new Error(e);
		}
	}
	
}
