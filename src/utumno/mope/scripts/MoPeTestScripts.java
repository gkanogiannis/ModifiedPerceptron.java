/*
 *
 * ModifiedPerceptron.java utumno.mope.scripts.MoPeTestScripts
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
import java.util.ArrayList;

import utumno.mope.utils.ClassUtils;

public class MoPeTestScripts {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		String[] datasets = {"reuters-10","reuters-10-x","reuters-90","reuters-90-x",
							 "reutersBIG-103",//"reutersBIGIn-354","reutersBIGIn-262","reutersBIG-80",
							 "ohsumed-23",
							 "ohsumedBIG-28","ohsumedBIG-28-x",
							 "ohsumedBIG-49","ohsumedBIG-49-x",
							 "ohsumedBIG-96","ohsumedBIG-96-x",
	             			 "trecap-20",
	             			 "ng-20",
				             "reutersSingle-8",
				             "reutersSingle-52"};
		
		boolean linuxMode = true;
		
		//String prefix0 = "P:\\BACKUP\\scripts\\";
		String prefix0 = "/windows/p/BACKUP/scripts/";
		String prefix;
		if(linuxMode){
			prefix = "/windows/p/BACKUP/Datasets/";
		}
		else{
			prefix = "P:\\BACKUP\\Datasets\\";
		}
		
		String exe;
		if(linuxMode){
			exe = " java -Xmx3072m -server -Xoptimize -classpath \""+prefix+"mope.jar\" utumno.mope.cmd.ClassVectorEvaluatorCmd -inData ";
		}
		else{
			exe = " java -Xmx3072m -server -Xoptimize -classpath \""+prefix+"mope.jar\" utumno.mope.cmd.ClassVectorEvaluatorCmd -inData ";
		}
		
		String fileSeparator = linuxMode? "/" : "\\";
		
		BufferedWriter bw = null;
		try {
			for(String dataset : datasets){
				String prefix2;
				String[] s = dataset.split("-");
				if(s.length == 2){
					bw = new BufferedWriter(new FileWriter(prefix0+dataset+".test.mope." + (linuxMode?"sh":"bat")));
					prefix2 = s[0]+fileSeparator+"svmf"+fileSeparator+s[1]+fileSeparator;
				}
				else{
					bw = new BufferedWriter(new FileWriter(prefix0+dataset+".test.mope." + (linuxMode?"sh":"bat")));
					prefix2 = s[0]+fileSeparator+"svmf"+fileSeparator+s[1]+"-x"+fileSeparator;
				}
				
				ArrayList<String> classesNames = (ArrayList<String>)ClassUtils.class.getField(s[0]+s[1]+"Classes").get(null);
				for(String className : classesNames){
					if(linuxMode){
						String time = "\"time\" \"-fuserTime=%U sec\\nsystemTime=%S sec\\nrealTime=%e\\n\\n\" \"-o";
						String exe2 = time+prefix+prefix2+"test"+fileSeparator+className+".test.mope.txt2\" " + exe;
						String command = exe2 + "\"" + prefix + prefix2 + "test"+fileSeparator + className + ".test.svmf\" -inModel \""+prefix+prefix2+"train"+fileSeparator+className+".mope.model\"" + (linuxMode?";":"");
						bw.write(command + (linuxMode?"":"\r") +"\n");
					}
					else{
						String command = exe + "\"" + prefix + prefix2 + "test"+fileSeparator + className + ".test.svmf\" -inModel \""+prefix+prefix2+"train"+fileSeparator+className+".mope.model\"" + (linuxMode?";":"");
						bw.write(command + (linuxMode?"":"\r") +"\n");
					}
				}
				
				bw.close();
			}
		} 
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
