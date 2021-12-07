/*
 *
 * ModifiedPerceptron.java utumno.mope.scripts.POI_title
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.NumberFormat;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class POI_title {
	
	private static NumberFormat nf = NumberFormat.getInstance();
	static{
		nf.setGroupingUsed(true);
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setMaximumIntegerDigits(2);
		nf.setMinimumIntegerDigits(2);
	}

	public static void main(String[] args) {
		String[] datasets = {"reuters-10-title","reuters-10-x-title","reuters-90-title","reuters-90-x-title",
				 "null",
	             "ohsumedBIG-28-title","ohsumedBIG-28-x-title",
	             "ohsumedBIG-49-title","ohsumedBIG-49-x-title",
	             "ohsumedBIG-96-title","ohsumedBIG-96-x-title"
				 };

		//boolean linuxMode = args[0].trim().equalsIgnoreCase("linux")? true : false;

		String prefix0   = args[0];
		String prefix1   = args[1];
		
		HSSFWorkbook wb = null;
		HSSFSheet sheet = null;
		
		FileOutputStream fileOut = null;
		
		int i;
		
		
		//RHCPP
		try {
			FileInputStream fis = new FileInputStream(new File(prefix0,"results-TC(RHCPP-RH-SVMPERF-BaPe) - title.xls"));
			wb = new HSSFWorkbook(fis);
			sheet = wb.getSheet("Sheet5");
			fis.close();
		} 
		catch (Exception e1) {
			e1.printStackTrace();
		}
		i=-1;
		for(String dataset : datasets){
			i++;
			if(dataset.equalsIgnoreCase("null")) continue;
			try{
				BufferedReader br = new BufferedReader(new FileReader(new File(prefix1,dataset+".rhcpp.mm.txt")));
				
				HSSFRow row = sheet.getRow(4 +  i);
				
				//HSSFCell classes = row.getCell(2)==null? row.createCell(2):row.getCell(2);
				
				int j = 4;
				
				HSSFCell TecellmicroF1 = row.getCell(j)==null? row.createCell(j):row.getCell(j);
				HSSFCell TecellmacroF1 = row.getCell(j+4)==null? row.createCell(j+4):row.getCell(j+4);
				HSSFCell TecellBEP = row.getCell(j+8)==null? row.createCell(j+8):row.getCell(j+8);
				HSSFCell TecellROCArea = row.getCell(j+12)==null? row.createCell(j+12):row.getCell(j+12);
				HSSFCell TecellAvgPre = row.getCell(j+16)==null? row.createCell(j+16):row.getCell(j+16);
				HSSFCell TecellsystemTime = row.getCell(j+20)==null? row.createCell(j+20):row.getCell(j+20);
				HSSFCell TecellwallTime = row.getCell(j+24)==null? row.createCell(j+24):row.getCell(j+24);
				
				HSSFCell TrcellmicroF1 = row.getCell(j+29)==null? row.createCell(j+29):row.getCell(j+29);
				HSSFCell TrcellmacroF1 = row.getCell(j+33)==null? row.createCell(j+33):row.getCell(j+33);
				HSSFCell Trcelliter = row.getCell(j+37)==null? row.createCell(j+37):row.getCell(j+37);
				HSSFCell TrcellsystemTime = row.getCell(j+41)==null? row.createCell(j+41):row.getCell(j+41);
				HSSFCell TrcellwallTime = row.getCell(j+45)==null? row.createCell(j+45):row.getCell(j+45);
				
				br.readLine();
				br.readLine();
				//classes.setCellValue(Integer.parseInt(br.readLine().split("=")[1].trim()));
				
				br.readLine();br.readLine();br.readLine();br.readLine();br.readLine();br.readLine();
				TecellmicroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellmacroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellBEP.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellROCArea.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellAvgPre.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellwallTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				TecellsystemTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				br.readLine();br.readLine();br.readLine();

				Trcelliter.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TrcellmicroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TrcellmacroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TrcellwallTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				TrcellsystemTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				br.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		try {
			fileOut = new FileOutputStream(new File(prefix0,"results-TC(RHCPP-RH-SVMPERF-BaPe) - title.xls"));
			wb.write(fileOut);
		    fileOut.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//RH
		try {
			FileInputStream fis = new FileInputStream(new File(prefix0,"results-TC(RHCPP-RH-SVMPERF-BaPe) - title.xls"));
			wb = new HSSFWorkbook(fis);
			sheet = wb.getSheet("Sheet5");
			fis.close();
		} 
		catch (Exception e1) {
			e1.printStackTrace();
		}
		i=-1;
		for(String dataset : datasets){
			i++;
			if(dataset.equalsIgnoreCase("null")) continue;
			try{
				BufferedReader br = new BufferedReader(new FileReader(new File(prefix1,dataset+".rh.mm.txt")));
				
				HSSFRow row = sheet.getRow(4 +  i);
				
				HSSFCell classes = row.createCell(2);
				
				int j = 5;
				
				HSSFCell TecellmicroF1 = row.getCell(j)==null? row.createCell(j):row.getCell(j);
				HSSFCell TecellmacroF1 = row.getCell(j+4)==null? row.createCell(j+4):row.getCell(j+4);
				HSSFCell TecellBEP = row.getCell(j+8)==null? row.createCell(j+8):row.getCell(j+8);
				HSSFCell TecellROCArea = row.getCell(j+12)==null? row.createCell(j+12):row.getCell(j+12);
				HSSFCell TecellAvgPre = row.getCell(j+16)==null? row.createCell(j+16):row.getCell(j+16);
				HSSFCell TecellsystemTime = row.getCell(j+20)==null? row.createCell(j+20):row.getCell(j+20);
				HSSFCell TecellwallTime = row.getCell(j+24)==null? row.createCell(j+24):row.getCell(j+24);
				
				HSSFCell TrcellmicroF1 = row.getCell(j+29)==null? row.createCell(j+29):row.getCell(j+29);
				HSSFCell TrcellmacroF1 = row.getCell(j+33)==null? row.createCell(j+33):row.getCell(j+33);
				HSSFCell Trcelliter = row.getCell(j+37)==null? row.createCell(j+37):row.getCell(j+37);
				HSSFCell TrcellsystemTime = row.getCell(j+41)==null? row.createCell(j+41):row.getCell(j+41);
				HSSFCell TrcellwallTime = row.getCell(j+45)==null? row.createCell(j+45):row.getCell(j+45);
				
				br.readLine();
				classes.setCellValue(Integer.parseInt(br.readLine().split("=")[1].trim()));
				
				br.readLine();br.readLine();br.readLine();br.readLine();br.readLine();br.readLine();
				TecellmicroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellmacroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellBEP.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellROCArea.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellAvgPre.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellwallTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				TecellsystemTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				br.readLine();br.readLine();br.readLine();
				
				Trcelliter.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TrcellmicroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TrcellmacroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TrcellwallTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				TrcellsystemTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				br.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		try {
			fileOut = new FileOutputStream(new File(prefix0,"results-TC(RHCPP-RH-SVMPERF-BaPe) - title.xls"));
			wb.write(fileOut);
		    fileOut.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}


		//SVMPERF
		try {
			FileInputStream fis = new FileInputStream(new File(prefix0,"results-TC(RHCPP-RH-SVMPERF-BaPe) - title.xls"));
			wb = new HSSFWorkbook(fis);
			sheet = wb.getSheet("Sheet5");
			fis.close();
		} 
		catch (Exception e1) {
			e1.printStackTrace();
		}
		i=-1;
		for(String dataset : datasets){
			i++;
			if(dataset.equalsIgnoreCase("null")) continue;
			try{
				BufferedReader br = new BufferedReader(new FileReader(new File(prefix1,dataset+".svmperf.mm.txt")));
				
				HSSFRow row = sheet.getRow(4 +  i);
				
				//HSSFCell classes = row.createCell(2);
				
				int j = 6;
				
				HSSFCell TecellmicroF1 = row.getCell(j)==null? row.createCell(j):row.getCell(j);
				HSSFCell TecellmacroF1 = row.getCell(j+4)==null? row.createCell(j+4):row.getCell(j+4);
				HSSFCell TecellBEP = row.getCell(j+8)==null? row.createCell(j+8):row.getCell(j+8);
				HSSFCell TecellROCArea = row.getCell(j+12)==null? row.createCell(j+12):row.getCell(j+12);
				HSSFCell TecellAvgPre = row.getCell(j+16)==null? row.createCell(j+16):row.getCell(j+16);
				HSSFCell TecellsystemTime = row.getCell(j+20)==null? row.createCell(j+20):row.getCell(j+20);
				HSSFCell TecellwallTime = row.getCell(j+24)==null? row.createCell(j+24):row.getCell(j+24);
				
				//HSSFCell TrcellmicroF1 = row.getCell(j+29)==null? row.createCell(j+29):row.getCell(j+29);
				//HSSFCell TrcellmacroF1 = row.getCell(j+33)==null? row.createCell(j+33):row.getCell(j+33);
				HSSFCell Trcelliter = row.getCell(j+37)==null? row.createCell(j+37):row.getCell(j+37);
				HSSFCell TrcellsystemTime = row.getCell(j+41)==null? row.createCell(j+41):row.getCell(j+41);
				HSSFCell TrcellwallTime = row.getCell(j+45)==null? row.createCell(j+45):row.getCell(j+45);
				
				br.readLine();
				br.readLine();
				//classes.setCellValue(Integer.parseInt(br.readLine().split("=")[1].trim()));
				
				br.readLine();br.readLine();br.readLine();br.readLine();br.readLine();br.readLine();
				TecellmicroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellmacroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellBEP.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellROCArea.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellAvgPre.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellwallTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				TecellsystemTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				br.readLine();br.readLine();br.readLine();

				Trcelliter.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				//TrcellmicroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				//TrcellmacroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TrcellwallTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				TrcellsystemTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				br.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		try {
			fileOut = new FileOutputStream(new File(prefix0,"results-TC(RHCPP-RH-SVMPERF-BaPe) - title.xls"));
			wb.write(fileOut);
		    fileOut.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//BatchPerceptron
		try {
			FileInputStream fis = new FileInputStream(new File(prefix0,"results-TC(RHCPP-RH-SVMPERF-BaPe) - title.xls"));
			wb = new HSSFWorkbook(fis);
			sheet = wb.getSheet("Sheet5");
			fis.close();
		} 
		catch (Exception e1) {
			e1.printStackTrace();
		}
		i=-1;
		for(String dataset : datasets){
			i++;
			if(dataset.equalsIgnoreCase("null")) continue;
			try{
				BufferedReader br = new BufferedReader(new FileReader(new File(prefix1,dataset+".bape.mm.txt")));
				
				HSSFRow row = sheet.getRow(4 +  i);
				
				//HSSFCell classes = row.getCell(2)==null? row.createCell(2):row.getCell(2);
				
				int j = 7;
				
				HSSFCell TecellmicroF1 = row.getCell(j)==null? row.createCell(j):row.getCell(j);
				HSSFCell TecellmacroF1 = row.getCell(j+4)==null? row.createCell(j+4):row.getCell(j+4);
				HSSFCell TecellBEP = row.getCell(j+8)==null? row.createCell(j+8):row.getCell(j+8);
				HSSFCell TecellROCArea = row.getCell(j+12)==null? row.createCell(j+12):row.getCell(j+12);
				HSSFCell TecellAvgPre = row.getCell(j+16)==null? row.createCell(j+16):row.getCell(j+16);
				HSSFCell TecellsystemTime = row.getCell(j+20)==null? row.createCell(j+20):row.getCell(j+20);
				HSSFCell TecellwallTime = row.getCell(j+24)==null? row.createCell(j+24):row.getCell(j+24);
				
				HSSFCell TrcellmicroF1 = row.getCell(j+29)==null? row.createCell(j+29):row.getCell(j+29);
				HSSFCell TrcellmacroF1 = row.getCell(j+33)==null? row.createCell(j+33):row.getCell(j+33);
				HSSFCell Trcelliter = row.getCell(j+37)==null? row.createCell(j+37):row.getCell(j+37);
				HSSFCell TrcellsystemTime = row.getCell(j+41)==null? row.createCell(j+41):row.getCell(j+41);
				HSSFCell TrcellwallTime = row.getCell(j+45)==null? row.createCell(j+45):row.getCell(j+45);
				
				br.readLine();
				br.readLine();
				//classes.setCellValue(Integer.parseInt(br.readLine().split("=")[1].trim()));
				
				br.readLine();br.readLine();br.readLine();br.readLine();br.readLine();br.readLine();
				TecellmicroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellmacroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellBEP.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellROCArea.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellAvgPre.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TecellwallTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				TecellsystemTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				br.readLine();br.readLine();br.readLine();

				Trcelliter.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TrcellmicroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TrcellmacroF1.setCellValue(Double.parseDouble(br.readLine().split("=")[1].trim()));
				
				TrcellwallTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				TrcellsystemTime.setCellValue(Double.parseDouble(br.readLine().split("=")[1].split(" ")[0].trim()));
				
				br.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		try {
			fileOut = new FileOutputStream(new File(prefix0,"results-TC(RHCPP-RH-SVMPERF-BaPe) - title.xls"));
			wb.write(fileOut);
		    fileOut.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

