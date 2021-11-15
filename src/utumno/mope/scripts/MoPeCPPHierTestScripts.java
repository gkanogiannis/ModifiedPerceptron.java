package utumno.mope.scripts;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import utumno.mope.utils.ClassUtils;

public class MoPeCPPHierTestScripts {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		String[] datasets = {"reutersBIGHier-103","reutersBIGInHier-354"};
		
		boolean linuxMode = true;//args[0].trim().equalsIgnoreCase("linux")? true : false;
		
		//String prefix0 = "D:\\PhD\\Datasets\\scripts\\";
		String prefix0 = "/home/utumno/PhD/Datasets/scripts/";
		String prefix;
		if(linuxMode){
			prefix = "/home/utumno/PhD/Datasets/";
		}
		else{
			prefix = "D:\\PhD\\Datasets\\";
		}
		String exe;
		if(linuxMode){
			exe = prefix +"rhcpp-test-InHier.bin --inData ";
		}
		else{
			exe = prefix +"rhcpp-test-InHier.exe --inData ";
		}
		
		String fileSeparator;
		if(linuxMode){
			fileSeparator = "/";
		}
		else{
			fileSeparator = "\\";
		}
		
		BufferedWriter bw = null;
		try {
			for(String dataset : datasets){
				String prefix2;
				String[] s = dataset.split("-");
				if(s.length == 2){
					bw = new BufferedWriter(new FileWriter(prefix0+dataset+".test.rhcpp." + (linuxMode?"sh":"bat")));
					prefix2 = s[0]+fileSeparator+"svmf"+fileSeparator+s[1]+fileSeparator+"hier"+fileSeparator;
				}
				else{
					bw = new BufferedWriter(new FileWriter(prefix0+dataset+".test.rhcpp." + (linuxMode?"sh":"bat")));
					prefix2 = s[0]+fileSeparator+"svmf"+fileSeparator+s[1]+"-x"+fileSeparator+"hier"+fileSeparator;
				}
				
				ArrayList<String> classesNames = (ArrayList<String>)ClassUtils.class.getField(s[0]+s[1]+"Classes").get(null);
				for(String className : classesNames){
					if(linuxMode){
						String time = "\"time\" \"-fuserTime=%U sec\\nsystemTime=%S sec\\nrealTime=%e\\n\\n\" \"-o";
						String exe2 = time+prefix+prefix2+"test"+fileSeparator+className+".test.rhcpp.txt2\" " + exe;
						String command = exe2 + "\"" + prefix + prefix2 + "test"+fileSeparator + className + ".test.svmf\" --inModel \""+prefix+prefix2+"train"+fileSeparator+className+".rhcpp.model\"" + (linuxMode?";":"");
						bw.write(command + (linuxMode?"":"\r") +"\n");
					}
					else{
						String command = exe + "\"" + prefix + prefix2 + "test"+fileSeparator + className + ".test.svmf\" --inModel \""+prefix+prefix2+"train"+fileSeparator+className+".rhcpp.model\"" + (linuxMode?";":"");
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
