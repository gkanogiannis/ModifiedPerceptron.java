/*
 *
 * ModifiedPerceptron.java utumno.mope.utils.DocumentClassVectorUtils
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
package utumno.mope.utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;

import utumno.mope.datastructures.ClassVector;
import utumno.mope.datastructures.ClassVectorSimple;
import utumno.mope.datastructures.ClassVectorSparse;
import utumno.mope.datastructures.DocumentVector;
import utumno.mope.datastructures.VectorNode;
import utumno.mope.datastructures.VectorNodeLight;

public class DocumentClassVectorUtils {

	public static DocumentVector parseDocumentVectorFromString(String line, HashSet<Integer> featuresIds, int[] maxFeaturesId){
		try{
			DocumentVector documentVector = new DocumentVector();
			StringTokenizer tokenizer = new StringTokenizer(line);
			String sid = tokenizer.nextToken().trim();
			if(Float.parseFloat(sid) > 0.0f){
				documentVector.setPositiveExample(true);
			}
			else{
				documentVector.setPositiveExample(false);
			}
			
			ArrayList<VectorNode> nodes = new ArrayList<VectorNode>();
			while(tokenizer.hasMoreTokens()){
				String value = tokenizer.nextToken().trim();
				//Read doc id and exit.
				if(value.equals("#")){
					documentVector.setDocId(tokenizer.nextToken().trim());
					break;
				}
				//Read a termId:weight pair.
				else{
					int index = value.indexOf(':');
					if(index == -1){
						continue;
					}
					String stermId = value.substring(0,index);
					int termId = Integer.parseInt(stermId);
					if(termId > maxFeaturesId[0]){
						maxFeaturesId[0] = termId;
					}
					featuresIds.add(termId);
					String sweight = value.substring(index+1);
					float weight = Float.parseFloat(sweight);
					VectorNode node = new VectorNode(termId, weight);
					nodes.add(node);
				}
			}
			nodes.trimToSize();
			documentVector.setNodes(nodes);
			return documentVector;
		}
		catch (Exception e) {
			throw new Error(e);
		}			
	}
	
	private static DocumentVector parseDocumentVectorFromString2(String line, Object Nn[]){
		try{
			DocumentVector documentVector = new DocumentVector();
			ArrayList<VectorNode> nodes = new ArrayList<VectorNode>();
			line = line.substring(1,line.length()-1);
			String[] data = line.split(",");
			String pair = data[0].trim();
			String[] data2 = pair.split("\\s+");
			if(data2[0].trim().equals("0")){
				if(data2[1].trim().equalsIgnoreCase("positive"))
					documentVector.setPositiveExample(true);
				else if(data2[1].trim().equalsIgnoreCase("negative"))
					documentVector.setPositiveExample(false);
			}
			else{
				documentVector.setPositiveExample(false);
				int termId = Integer.parseInt(data2[0].trim());
				if(termId > (Integer)Nn[1]) Nn[1]=termId;
				float weight = Float.parseFloat(data2[1].trim());
				VectorNode node = new VectorNode(termId, weight);
				nodes.add(node);
			}
			for(int j=1; j<data.length; j++){
				pair = data[j].trim();
				data2 = pair.split("\\s+");
				int termId = Integer.parseInt(data2[0].trim());
				if(termId > (Integer)Nn[1]) Nn[1]=termId;
				float weight = Float.parseFloat(data2[1].trim());
				VectorNode node = new VectorNode(termId, weight);
				nodes.add(node);
			}
			nodes.trimToSize();
			documentVector.setNodes(nodes);
			return documentVector;
		}
		catch (Exception e) {
			throw new Error(e);
		}			
	}
	
	public static ClassVector parseClassVectorFromString(String line){
		try{
			ClassVector ret = new ClassVector();
			StringTokenizer tokenizer = new StringTokenizer(line);
			String spaceSize = tokenizer.nextToken().trim();
			VectorNode[] nodes = new VectorNode[Integer.parseInt(spaceSize)];
			while(tokenizer.hasMoreTokens()){
				String value = tokenizer.nextToken().trim();
				//Read class name, size, threshold and exit.
				if(value.equals("#")){
					String className = tokenizer.nextToken().trim();
					ret.setName(className.replace("%", "-").replace("$",",").replace("_", " "));
					ret.setSize(Integer.parseInt(tokenizer.nextToken().trim()));
					ret.setThreshold(Float.parseFloat(tokenizer.nextToken().trim()));
					break;
				}
				//Read a termId:weight pair.
				else{
					String stermId = value.substring(0,value.indexOf(':'));
					String sweight = value.substring(value.indexOf(':')+1);
					VectorNode node = new VectorNode(Integer.parseInt(stermId),Float.parseFloat(sweight));
					nodes[node.getTermId()-1] = node;
				}
			}
			ret.setNodes(nodes);
			return ret;
		}
		catch (Exception e) {
			throw new Error(e);
		}			
	}
	
	public static ClassVectorSparse parseClassVectorSparseFromString(String line){
		try{
			ClassVectorSparse ret = new ClassVectorSparse();
			StringTokenizer tokenizer = new StringTokenizer(line);
			String spaceSize = tokenizer.nextToken().trim();
			Hashtable<Integer, VectorNode> nodes = new Hashtable<Integer, VectorNode>();
			while(tokenizer.hasMoreTokens()){
				String value = tokenizer.nextToken().trim();
				//Read class name, size, threshold and exit.
				if(value.equals("#")){
					String className = tokenizer.nextToken().trim();
					ret.setName(className.replace("%", "-").replace("$",",").replace("_", " "));
					ret.setSize(Integer.parseInt(tokenizer.nextToken().trim()));
					ret.setThreshold(Float.parseFloat(tokenizer.nextToken().trim()));
					break;
				}
				//Read a termId:weight pair.
				else{
					VectorNode node = new VectorNode(Integer.parseInt(value.substring(0,value.indexOf(':'))),
													 Float.parseFloat(value.substring(value.indexOf(':')+1)));
					nodes.put(node.getTermId(), node);
				}
			}
			ret.setNodes(nodes);
			return ret;
		}
		catch (Exception e) {
			throw new Error(e);
		}			
	}
	
	public static ClassVectorSimple parseClassVectorSimpleFromString(String line){
		try{
			ClassVectorSimple ret = new ClassVectorSimple();
			StringTokenizer tokenizer = new StringTokenizer(line);
			String spaceSize = tokenizer.nextToken().trim();
			float[] nodes = new float[Integer.parseInt(spaceSize)];
			while(tokenizer.hasMoreTokens()){
				String value = tokenizer.nextToken().trim();
				//Read class name, size, threshold and exit.
				if(value.equals("#")){
					String className = tokenizer.nextToken().trim();
					ret.setName(className.replace("%", "-").replace("$",",").replace("_", " "));
					ret.setSize(Integer.parseInt(tokenizer.nextToken().trim()));
					ret.setThreshold(Float.parseFloat(tokenizer.nextToken().trim()));
					break;
				}
				//Read a termId:weight pair.
				else{
					String stermId = value.substring(0,value.indexOf(':'));
					String sweight = value.substring(value.indexOf(':')+1);
					nodes[Integer.parseInt(stermId)-1] = Float.parseFloat(sweight);
				}
			}
			ret.setNodes(nodes);
			return ret;
		}
		catch (Exception e) {
			throw new Error(e);
		}			
	}
	
	public static void saveClassVector(ClassVector classVector, File out){
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(out),"utf-8");
			BufferedWriter bw = new BufferedWriter(osw);

			int vectorSize = classVector.getNodes().length;
	        bw.write(vectorSize+"\t");
			for(int i=0;i<vectorSize;i++){
				VectorNode node = classVector.getNodes()[i];
				int termId = i+1;
				float termWeight;
				if(node!=null){
					termWeight = node.getTermWeight();
				}
				else{
					continue;
				}
				bw.write(termId+":"+termWeight+"\t");
			}
			bw.write("\t# "+classVector.getName().replace(" ", "_").replace(",", "$").replace("-", "%")+" "+classVector.getSize()+" "+classVector.getThreshold());
			bw.flush();
			bw.close();
		} 
		catch (Exception e) {
			throw new Error(e);
		}
    }
	
	public static void saveClassVectorPlain(ClassVector classVector, File out){
		try {
			File f = new File(out, classVector.getName()+".cvp");
			f.createNewFile();
			FileWriter fw = new FileWriter(f);
			int vectorSize = classVector.getNodes().length;
			for(int i=0;i<vectorSize;i++){
				VectorNode node = classVector.getNodes()[i];
				float termWeight;
				if(node!=null){
					termWeight = node.getTermWeight();
				}
				else{
					continue;
				}
				fw.write(termWeight+"\t");
			}
			fw.flush();
			fw.close();
		} 
		catch (Exception e) {
			throw new Error(e);
		}
	}
	
	public static ArrayList<DocumentVector> getDocumentVectors(BufferedReader brSvmfFile, String bufLine, boolean allAtOnce, int[] numberOfExamples, HashSet<Integer> featuresIds, int[] maxFeaturesId) {
		if(allAtOnce){
			try{
				ArrayList<DocumentVector> ret = new ArrayList<DocumentVector>();
				String line = "";
				while((line=brSvmfFile.readLine()) != null){
					numberOfExamples[0] = numberOfExamples[0] + 1;
					DocumentVector dv = DocumentClassVectorUtils.parseDocumentVectorFromString(line,featuresIds,maxFeaturesId);
					if(dv.getDocId() == null){
						dv.setDocId(String.valueOf(numberOfExamples));
					}
					dv.normalize();
					ret.add(dv);
				}
				ret.trimToSize();
				return ret;
			}
			catch(Exception e){
				throw new Error(e);
			}
		}
		else{
			try{
				ArrayList<DocumentVector> ret = new ArrayList<DocumentVector>();
				if(bufLine != null){
					numberOfExamples[0] = numberOfExamples[0] + 1;
					DocumentVector dv = DocumentClassVectorUtils.parseDocumentVectorFromString(bufLine,featuresIds,maxFeaturesId);
					if(dv.getDocId() == null){
						dv.setDocId(String.valueOf(numberOfExamples));
					}
					dv.normalize();
					ret.add(dv);
				}
				Runtime runtime = Runtime.getRuntime(); 
				String line = "";
				while((runtime.freeMemory()+(runtime.maxMemory()-runtime.totalMemory()))/1024 > Long.parseLong("400000") && (line=brSvmfFile.readLine())!=null){
					numberOfExamples[0] = numberOfExamples[0] + 1;
					DocumentVector dv = DocumentClassVectorUtils.parseDocumentVectorFromString(line,featuresIds,maxFeaturesId);
					if(dv.getDocId() == null){
						dv.setDocId(String.valueOf(numberOfExamples));
					}
					dv.normalize();
					ret.add(dv);
				}
				ret.trimToSize();
				return ret;
			}
			catch(Exception e){
				throw new Error(e);
			}
		}
	}
	
}
