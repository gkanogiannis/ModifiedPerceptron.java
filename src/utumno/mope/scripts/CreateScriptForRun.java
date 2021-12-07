/*
 *
 * ModifiedPerceptron.java utumno.mope.scripts.CreateScriptForRun
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
package utumno.mope.scripts;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CreateScriptForRun {

	//Rotate Windows
	/*
	public static void main(String[] args) {
		//String[] distributions = {"beta","chi2","exp","f","geo","logn","norm","wbl"};
		String[] distributions = {"norm"};
		String[] dimensions = {"2","3","10","100","1000"};
		String[] examples = {"100","1000","10000"};
		String[] margins = {"0","5","10","25","50"};
		
		String prefix1   = "D:\\PhD\\Linear Data Gen\\data\\";
		String prefix2   = "D:\\PhD\\Linear Data Gen\\test\\";
		String prefix3   = "D:\\PhD\\Linear Data Gen\\test\\";
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(prefix3+"\\"+"go-rotate.bat"));
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
		
		for(String distr : distributions){
			for(String dim : dimensions){
				for(String ex : examples){
					for(String m : margins){
						String filename = distr + "-" + dim + "-" + ex + "-" + m + "%%";
						String exe = " java -jar \""+prefix2+"centroid3single.jar\"";
						String arg1 = " -inData \"" + prefix1+filename + ".svmf\" " ;
						String arg2 = " -iter 5000 ";
						String arg3 = " 1>" + "\"" + prefix2+distr+"-"+dim+"-"+ex+"-"+m+ ".txt" +"\"";
						String command = exe+arg1+arg2+arg3;
						System.err.println(command);
						Process p;
						try {
							//p = Runtime.getRuntime().exec(command);
							//p.waitFor();
							
							bw.write(command+"\r\n");
						} 
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		try {
			bw.flush();
			bw.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
	
	/*
	//Rotate Linux
	public static void main(String[] args) {
		//String[] distributions = {"beta","chi2","exp","f","geo","logn","norm","wbl"};
		String[] distributions = {"norm"};
		String[] dimensions = {"2","3","10","100","1000"};
		String[] examples = {"100","1000","10000"};
		String[] margins = {"0","1","5","10","25","50"};
		
		String prefix1   = "/home/utumno/PhD/Linear Data Generator/data/";
		String prefix2   = "/home/utumno/PhD/Linear Data Generator/test/";
		String prefix3   = "D:\\PhD\\Linear Data Gen\\";
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(prefix3+"\\"+"go-rotate.sh"));
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
		
		for(String distr : distributions){
			for(String dim : dimensions){
				for(String ex : examples){
					for(String m : margins){
						String filename = distr + "-" + dim + "-" + ex + "-" + m + "%";
						String exe = " java -jar \"/home/utumno/PhD/Linear Data Generator/centroid3single.jar\"";
						String arg1 = " -inData \"" + prefix1+filename + ".svmf\" " ;
						String arg2 = " -iter 10000 ";
						String arg3 = " 1>" + "\"" + prefix2+distr+"-"+dim+"-"+ex+"-"+m+ ".txt" +"\";";
						String command = exe+arg1+arg2+arg3;
						System.err.println(command);
						try {
							bw.write(command+"\n");
						} 
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		try {
			bw.flush();
			bw.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
	
	
	//svmperf Linux
	public static void main(String[] args) {
		//String[] distributions = {"beta","chi2","exp","f","geo","logn","norm","wbl"};
		String[] distributions = {"norm"};
		String[] dimensions = {"2","3","10","100","1000"};
		String[] examples = {"100","1000","10000"};
		String[] margins = {"0","1","5","10","25","50"};
		
		String prefix1   = "/home/utumno/PhD/Linear Data Generator/data/";
		String prefix2   = "/home/utumno/PhD/Linear Data Generator/test/";
		String prefix3   = "D:\\PhD\\Linear Data Gen\\";
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(prefix3+"\\"+"go-svmperf.sh"));
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
		
		for(String distr : distributions){
			for(String dim : dimensions){
				for(String ex : examples){
					for(String m : margins){
						String filename = distr + "-" + dim + "-" + ex + "-" + m + "%";
						String exe = "./svmperf";
						String arg1 = " -c 100 \"" + prefix1+filename + ".svmf\" " ;
						String arg2 = " 1>" + "\"" + prefix2+distr+"-"+dim+"-"+ex+"-"+m+ "-svmperf.txt" +"\";";
						String command = exe+arg1+arg2;
						System.err.println(command);
						try {
							bw.write(command+"\n");
						} 
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		try {
			bw.flush();
			bw.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
