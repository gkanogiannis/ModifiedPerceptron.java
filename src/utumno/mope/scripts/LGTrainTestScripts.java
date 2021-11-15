package utumno.mope.scripts;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import utumno.mope.utils.ClassUtils;

public class LGTrainTestScripts {

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
		
		
		String prefix0 = "C:\\phd\\datasets\\scripts\\";
		String prefix  = "P:\\BACKUP\\Datasets\\";
		String exe     = " java -Xmx2500m -server -Xoptimize -classpath \""+prefix+"lg.jar\" edu.stanford.nlp.classify.ColumnDataClassifier -trainFromSVMLight -testFromSVMLight -realValued ";
		
		String fileSeparator = "\\";
		
		BufferedWriter bw = null;
		try {
			for(String dataset : datasets){
				String prefix2;
				String[] s = dataset.split("-");
				if(s.length == 2){
					bw = new BufferedWriter(new FileWriter(prefix0+dataset+".traintest.lg.bat"));
					prefix2 = s[0]+fileSeparator+"svmf"+fileSeparator+s[1]+fileSeparator;
				}
				else{
					bw = new BufferedWriter(new FileWriter(prefix0+dataset+".traintest.lg.bat"));
					prefix2 = s[0]+fileSeparator+"svmf"+fileSeparator+s[1]+"-x"+fileSeparator;
				}
				
				ArrayList<String> classesNames = (ArrayList<String>)ClassUtils.class.getField(s[0]+s[1]+"Classes").get(null);
				for(String className : classesNames){
					/*
					if(linuxMode){
						String time = "\"time\" \"-fuserTime=%U sec\\nsystemTime=%S sec\\nrealTime=%e\\n\\n\" \"-o";
						String exe2 = time+prefix+prefix2+"train"+fileSeparator+className+".train.rh.txt2\" " + exe;
						String command = exe2 + "\"" + prefix + prefix2 + "train"+fileSeparator + className + ".train.svmf\"" + (linuxMode?";":"");
						bw.write(command + (linuxMode?"":"\r") +"\n");
					}
					*/
					//else{
					String command = exe +" -trainFile " + "\"" + prefix + prefix2 + "train" + fileSeparator + className + ".train.svmf\"";
					command       +=      " -testFile "  + "\"" + prefix + prefix2 + "test"  + fileSeparator + className + ".test.svmf\"";
					command       +=      " 2> "          + "\"" + prefix + prefix2 + "test"  + fileSeparator + className + ".test.lg.txt";          
					bw.write(command + "\r" +"\n");
					//}
				}
				
				bw.close();
			}
		} 
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
