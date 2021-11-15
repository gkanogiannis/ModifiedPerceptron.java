package utumno.mope.scripts;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class SVMPERFMicroMacroScripts {

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

		String exe    = " java -classpath \""+prefix+"rh.jar\" utumno.rothyp.utils.SVMPERFMicroMacroResults ";
		
		String fileSeparator = linuxMode? "/" : "\\";
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(prefix0+"svmperf-mm."+(linuxMode?"sh":"bat")));
			for(String dataset : datasets){
				String prefix2;
				String[] s = dataset.split("-");
				if(s.length == 2){
					prefix2 = s[0]+fileSeparator+"svmf"+fileSeparator+s[1];
				}
				else{
					prefix2 = s[0]+fileSeparator+"svmf"+fileSeparator+s[1]+"-x";
				}
				
				String command = exe + "\"" + prefix + prefix2 + "\" 1>\""+prefix + prefix2 + fileSeparator + dataset +".svmperf.mm.txt\""+ (linuxMode?";":"");
				bw.write(command + (linuxMode?"":"\r") +"\n");
			}
			bw.close();
		} 
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
