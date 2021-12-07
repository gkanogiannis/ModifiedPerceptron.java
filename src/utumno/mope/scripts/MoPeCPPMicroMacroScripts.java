/*
 *
 * ModifiedPerceptron.java utumno.mope.scripts.MoPeCPPMicroMacroScripts
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

public class MoPeCPPMicroMacroScripts {

	public static void main(String[] args) {
		String[] datasets = {"reuters-10","reuters-10-x","reuters-90","reuters-90-x",
							 "reutersBIG-103","reutersBIGIn-354","reutersBIGIn-262","reutersBIG-80",
				             "ohsumed-23",
				             "ohsumedBIG-28","ohsumedBIG-28-x",
				             "ohsumedBIG-49","ohsumedBIG-49-x",
				             "ohsumedBIG-96","ohsumedBIG-96-x",
				             "trecap-20",
				             "ng-20",
				             "reutersSingle-8",
				             "reutersSingle-52"};
		
		boolean linuxMode = true;//args[0].trim().equalsIgnoreCase("linux")? true : false;
		
		//String prefix0 = "D:\\PhD\\Datasets\\scripts\\";
		String prefix0 = "/home/utumno/Desktop/scripts/";
		String prefix;
		if(linuxMode){
			prefix = "/home/utumno/PhD/Datasets/";
		}
		else{
			prefix = "D:\\PhD\\Datasets\\";
		}

		String exe    = " java -classpath \""+prefix+"rh.jar\" utumno.rothyp.utils.RHCPPMicroMacroResults ";
		
		String fileSeparator = args[0].trim().equalsIgnoreCase("linux")? "/" : "\\";
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(prefix0+"rhcpp-mm."+(linuxMode?"sh":"bat")));
			for(String dataset : datasets){
				String prefix2;
				String[] s = dataset.split("-");
				if(s.length == 2){
					prefix2 = s[0]+fileSeparator+"svmf"+fileSeparator+s[1];
				}
				else{
					prefix2 = s[0]+fileSeparator+"svmf"+fileSeparator+s[1]+"-x";
				}
				
				String command = exe + "\"" + prefix + prefix2 + "\" 1>\""+prefix + prefix2 + fileSeparator + dataset +".rhcpp.mm.txt\""+ (linuxMode?";":"");
				bw.write(command + (linuxMode?"":"\r") +"\n");
			}
			bw.close();
		} 
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
