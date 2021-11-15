package utumno.mope.scripts;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import utumno.mope.utils.ClassUtils;

public class MoPeCPPHierTrainScripts {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		String[] datasets = {"reutersBIGHier-103","reutersBIGInHier-354"};
		
		boolean linuxMode = false;//args[0].trim().equalsIgnoreCase("linux")? true : false;
		
		String prefix0 = "P:\\BACKUP\\scripts\\";
		//String prefix0 = "/home/utumno/Desktop/scripts/";
		String prefix;
		if(linuxMode){
			prefix = "/home/utumno/PhD/Datasets/";
		}
		else{
			prefix = "P:\\BACKUP\\Datasets\\";
		}
		String exe;
		if(linuxMode){
			exe = prefix +"rhcpp-train.bin --iter 50 --inData ";
		}
		else{
			exe = prefix + "rhcpp-train.exe --iter 50 --inData ";
		}
		
		String fileSeparator = "/";//args[0].trim().equalsIgnoreCase("linux")? "/" : "\\";
		
		BufferedWriter bw = null;
		try {
			for(String dataset : datasets){
				String prefix2;
				String[] s = dataset.split("-");
				if(s.length == 2){
					bw = new BufferedWriter(new FileWriter(prefix0+dataset+".train.rhcpp."+ (linuxMode?"sh":"bat")));
					prefix2 = s[0]+fileSeparator+"svmf"+fileSeparator+s[1]+fileSeparator+"hier"+fileSeparator;
				}
				else{
					bw = new BufferedWriter(new FileWriter(prefix0+dataset+".train.rhcpp."+ (linuxMode?"sh":"bat")));
					prefix2 = s[0]+fileSeparator+"svmf"+fileSeparator+s[1]+"-x"+fileSeparator+"hier"+fileSeparator;
				}
				
				ArrayList<String> classesNames = (ArrayList<String>)ClassUtils.class.getField(s[0]+s[1]+"Classes").get(null);
				for(String className : classesNames){
					if(linuxMode){
						String time = "\"time\" \"-fuserTime=%U sec\\nsystemTime=%S sec\\nrealTime=%e\\n\\n\" \"-o";
						String exe2 = time+prefix+prefix2+"train"+fileSeparator+className+".train.rhcpp.txt2\" " + exe;
						String command = exe2 + "\"" + prefix + prefix2 + "train"+fileSeparator + className + ".train.svmf\"" + (linuxMode?";":"");
						bw.write(command + (linuxMode?"":"\r") +"\n");
					}
					else{
						String command = exe + "\"" + prefix + prefix2 + "train"+fileSeparator + className + ".train.svmf\"" + (linuxMode?";":"");
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
