/*
 *
 * ModifiedPerceptron.java utumno.mope.utils.LGMicroMacroResults
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
import java.io.File;
import java.io.FileReader;
import java.text.NumberFormat;

public class LGMicroMacroResults {

	private static NumberFormat nf = NumberFormat.getInstance();
	static{
		nf.setGroupingUsed(true);
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setMaximumIntegerDigits(3);
		nf.setMinimumIntegerDigits(2);
	}
	
	public static void main(String[] args) {
		
		//test phase evaluation
		try{
			int i = 0;
			double Acc = 0.0;
			double Pr = 0.0;
			double Re = 0.0;
			double F1 = 0.0;
			int A , B , C , D;
			A=B=C=D=0;
			File[] files = new File(new File(args[0]),"test").listFiles();
			for(File f : files){
				if(f.getName().endsWith(".test.lg.txt")){
					i++;
					BufferedReader br = new BufferedReader(new FileReader(f));
					
					String line = null;
					while(!(line=br.readLine()).startsWith("Cls +1:")){}
					
					String[] data = line.split("\\s+");
					A += Integer.parseInt(data[2].split("=")[1].trim());
					C += Integer.parseInt(data[3].split("=")[1].trim());
					B += Integer.parseInt(data[4].split("=")[1].trim());
					String temp = data[5].split("=")[1].trim();
					D += Integer.parseInt(temp.substring(0, temp.indexOf(";")));
				
					temp = data[7].trim();
					double Acct = Double.parseDouble(temp.substring(0, 1)+"."+temp.substring(2, temp.length()));
					if(!Double.isNaN(Acct)) Acc += Acct;
					
					temp = data[11].trim();
					double Ret = Double.parseDouble(temp.substring(0, 1)+"."+temp.substring(2, temp.length()));
					if(!Double.isNaN(Ret)) Re += Ret;
					
					temp = data[9].trim();
					double Prt = Double.parseDouble(temp.substring(0, 1)+"."+temp.substring(2, temp.length()));
					if(!Double.isNaN(Prt)) Pr += Prt;
					
					temp = data[13].trim();
					double F1t = Double.parseDouble(temp.substring(0, 1)+"."+temp.substring(2, temp.length()));
					if(!Double.isNaN(F1t)) F1 += F1t;
					
					br.close();
				}
			}
			
			double microAcc = 100.0 * ((double)A+(double)D)/((double)A+(double)B+(double)C+(double)D);
			if(Double.isNaN(microAcc)) microAcc = 0.0;
			double macroAcc = 100.0 * Acc / (double) i;
			if(Double.isNaN(macroAcc)) macroAcc = 0.0;
			double microPr = 100.0 * (double)A / ((double)A+B);
			if(Double.isNaN(microPr)) microPr = 0.0;
			double macroPr = 100.0 * Pr / (double) i;
			if(Double.isNaN(macroPr)) macroPr = 0.0;
			double microRe = 100.0 * (double)A / ((double)A+C);
			if(Double.isNaN(microRe)) microRe = 0.0;
			double macroRe = 100.0 * Re / (double) i;
			if(Double.isNaN(macroRe)) macroRe = 0.0;
			double microF1 = 2.0 * microPr * microRe / (microPr+microRe);
			if(Double.isNaN(microF1)) microF1 = 0.0;
			double macroF1 = 100.0 * F1 / (double) i;
			if(Double.isNaN(macroF1)) macroF1 = 0.0;
				
			System.out.println("Test phase:");
			System.out.println("i=\t\t"+i);
			System.out.println("microAccuracy=\t"+nf.format(microAcc));
			System.out.println("macroAccuracy=\t"+nf.format(macroAcc));
			System.out.println("microPrecision=\t"+nf.format(microPr));
			System.out.println("macroPrecision=\t"+nf.format(macroPr));
			System.out.println("microRecall=\t"+nf.format(microRe));
			System.out.println("macroRecall=\t"+nf.format(macroRe));
			System.out.println("microF1=\t"+nf.format(microF1));
			System.out.println("macroF1=\t"+nf.format(macroF1));
			System.out.println("");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
