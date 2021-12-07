/*
 *
 * ModifiedPerceptron.java utumno.mope.utils.MoPeCPPMicroMacroResults
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

public class MoPeCPPMicroMacroResults {

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
			double wallTime = 0.0;
			double userTime = 0.0;
			int i = 0;
			double Acc = 0.0;
			double Pr = 0.0;
			double Re = 0.0;
			double F1 = 0.0;
			double BEP = 0.0;
			double ROCArea = 0.0;
			double AvPr = 0.0;
			int A , B , C , D;
			A=B=C=D=0;
			File[] files = new File(new File(args[0]),"test").listFiles();
			for(File f : files){
				try{
				if(f.getName().endsWith(".test.rhcpp.txt")){
					i++;
					BufferedReader br = new BufferedReader(new FileReader(f));
					
					br.readLine();br.readLine();
					
					String[] data = br.readLine().split("[ \t]");
					A += Integer.parseInt(data[1].split("=")[1].trim());
					C += Integer.parseInt(data[2].split("=")[1].trim());
					B += Integer.parseInt(data[3].split("=")[1].trim());
					D += Integer.parseInt(data[4].split("=")[1].trim());
					
					br.readLine();br.readLine();br.readLine();
					
					double Acct = Double.parseDouble(br.readLine().split("=")[1].trim());
					if(!Double.isNaN(Acct)) Acc += Acct;
					
					double Prt = Double.parseDouble(br.readLine().split("=")[1].trim());
					if(!Double.isNaN(Prt)) Pr += Prt;
					
					double Ret = Double.parseDouble(br.readLine().split("=")[1].trim());
					if(!Double.isNaN(Ret)) Re += Ret;
					
					double F1t = Double.parseDouble(br.readLine().split("=")[1].trim());
					if(!Double.isNaN(F1t)) F1 += F1t;
					
					double BEPt = Double.parseDouble(br.readLine().split("=")[1].trim());
					if(!Double.isNaN(BEPt)) BEP += BEPt;
					
					double ROCAreat = Double.parseDouble(br.readLine().split("=")[1].trim());
					if(!Double.isNaN(ROCAreat)) ROCArea += ROCAreat;
					
					br.readLine();
					
					double AvPrt = Double.parseDouble(br.readLine().split("=")[1].trim());
					if(!Double.isNaN(AvPrt)) AvPr += AvPrt;
					
					br.readLine();
					
					//wallTime   += Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim());
					br.close();
					
					br = new BufferedReader(new FileReader(f.getAbsolutePath()+"2"));
					userTime += Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim());
					br.readLine();
					wallTime += Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim());
					br.close();
				}
				}
				catch(Exception e){
					continue;
				}
			}
			
			double microAcc = 100.0 * ((double)A+(double)D)/((double)A+(double)B+(double)C+(double)D);
			if(Double.isNaN(microAcc)) microAcc = 0.0;
			double macroAcc = Acc / (double) i;
			if(Double.isNaN(macroAcc)) macroAcc = 0.0;
			double microPr = 100.0 * (double)A / ((double)A+B);
			if(Double.isNaN(microPr)) microPr = 0.0;
			double macroPr = Pr / (double) i;
			if(Double.isNaN(macroPr)) macroPr = 0.0;
			double microRe = 100.0 * (double)A / ((double)A+C);
			if(Double.isNaN(microRe)) microRe = 0.0;
			double macroRe = Re / (double) i;
			if(Double.isNaN(macroRe)) macroRe = 0.0;
			double microF1 = 2.0 * microPr * microRe / (microPr+microRe);
			if(Double.isNaN(microF1)) microF1 = 0.0;
			double macroF1 = F1 / (double) i;
			if(Double.isNaN(macroF1)) macroF1 = 0.0;
			double macroBEP = BEP / (double) i;
			if(Double.isNaN(macroBEP)) macroBEP = 0.0;
			double macroROCArea = ROCArea / (double) i;
			if(Double.isNaN(macroROCArea)) macroROCArea = 0.0;
			double macroAvPr = AvPr / (double) i;
			if(Double.isNaN(macroAvPr)) macroAvPr = 0.0;
	
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
			System.out.println("macroBEP=\t"+nf.format(macroBEP));
			System.out.println("macroRocArea=\t"+nf.format(macroROCArea));
			System.out.println("macroAvgPre=\t"+nf.format(macroAvPr));
			System.out.println("wallTime=\t"+wallTime+" sec");
			System.out.println("userTime=\t"+userTime+" sec");
			System.out.println("");
		}
		catch(Exception e){
			e.printStackTrace();
		}

		//train phase evaluation
		try{
			double wallTime = 0.0;
			double userTime = 0.0;
			int i = 0;
			double F1 = 0.0;
			int A , B , C , D;
			A=B=C=D=0;
			int iter=0;
			File[] files = new File(new File(args[0]),"train").listFiles();
			for(File f : files){
				if(f.getName().endsWith(".train.rhcpp.txt")){
					i++;
					BufferedReader br = new BufferedReader(new FileReader(f));
					br.readLine();br.readLine();br.readLine();br.readLine();br.readLine();
					iter += Integer.parseInt(br.readLine().split("=")[1].trim());
					
					String[] data = br.readLine().split("[ \t]");
					F1 += Double.parseDouble(data[0].split("=")[1].trim());
					A += Integer.parseInt(data[1].split("=")[1].trim());
					C += Integer.parseInt(data[2].split("=")[1].trim());
					B += Integer.parseInt(data[3].split("=")[1].trim());
					D += Integer.parseInt(data[4].split("=")[1].trim());
					
					br.readLine();
					//wallTime   += Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim());
					br.close();
					
					br = new BufferedReader(new FileReader(f.getAbsolutePath()+"2"));
					userTime += Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim());
					br.readLine();
					wallTime += Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim());
					br.close();
				}
			}
			double P = (double) A / ((double)A+B);
			double R = (double) A / ((double)A+C);
			
			double microF1 = 100.0 * 2.0*P*R/(P+R);
			double macroF1 = 100.0 * F1 / (double) i;
			
			System.out.println("Train phase:");
			System.out.println("i=\t\t"+i);
			System.out.println("iter=\t\t"+(iter/i));
			System.out.println("microF1=\t"+nf.format(microF1));
			System.out.println("macroF1=\t"+nf.format(macroF1));
			System.out.println("wallTime=\t"+wallTime+" sec");
			System.out.println("userTime=\t"+userTime+" sec");
			System.out.println("");
			System.out.println("");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
