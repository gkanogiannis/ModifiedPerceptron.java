package utumno.mope.cmd;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import utumno.mope.Trainer;
import utumno.mope.utils.MyErrorHandler;
import utumno.mope.utils.MyProgressHandler;

public class ClassVectorBuilderAdjusterCmd {

	@SuppressWarnings("static-access")
	private Options generateOptions(){
		Options options = new Options();
		Option inData = OptionBuilder.withArgName("inData").hasArg().isRequired()
									.withDescription("Train data location(train svmf file).")
									.create("inData");
		Option outModel = OptionBuilder.withArgName("outModel").hasArg()
									 .withDescription("Output data location(model file).")
									 .create("outModel");
		Option outTxt = OptionBuilder.withArgName("outTxt").hasArg()
	      						     .withDescription("Text file for the results.")
	      						     .create("outTxt");
		Option iter = OptionBuilder.withArgName("iter").hasArg().isRequired()
		  						   .withDescription("Number of iterations.")
		  						   .create("iter");
		Option verbose = new Option( "verbose", "Verbose messages." );
		Option help = new Option( "help", "Print this message." );
				
		options.addOption(inData);
		options.addOption(outModel);
		options.addOption(outTxt);
		options.addOption(iter);
		options.addOption(verbose);
		options.addOption(help);
		return options;
	}
	
	public static void main(String[] args) {
		ClassVectorBuilderAdjusterCmd builder = new ClassVectorBuilderAdjusterCmd();
		Options options = builder.generateOptions();
		CommandLineParser parser = new PosixParser();
		CommandLine cmd =null;
		try{
			cmd = parser.parse(options, args);
		}
		catch(ParseException e){
			System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
			HelpFormatter f = new HelpFormatter();
			f.printHelp("ClassVectorBuilderAdjusterCmd", options);
			System.exit(1);
		}
		if(cmd.hasOption("help")){
			HelpFormatter f = new HelpFormatter();
			f.printHelp("A", options);
			System.exit(1);
		}
		if(!cmd.hasOption("outModel")){
			String[] temp = new String[args.length+2];
			System.arraycopy(args, 0, temp, 0, args.length);
			temp[args.length] = "-outModel";
			if(cmd.getOptionValue("inData").endsWith(".train.svmf")){
				temp[args.length+1] = cmd.getOptionValue("inData").substring(0,cmd.getOptionValue("inData").indexOf(".train.svmf")) + ".mope.model";
			}
			else if(cmd.getOptionValue("inData").endsWith(".svmf")){
				temp[args.length+1] = cmd.getOptionValue("inData").substring(0,cmd.getOptionValue("inData").indexOf(".svmf")) + ".mope.model";
			}
			else{
				temp[args.length+1] = cmd.getOptionValue("inData") + ".mope.model";
			}
			args = temp;
			try{
				cmd = parser.parse(options, args);
			}
			catch(ParseException e){
				System.exit(1);
			}
		}
		if(!cmd.hasOption("outTxt")){
			String[] temp = new String[args.length+2];
			System.arraycopy(args, 0, temp, 0, args.length);
			temp[args.length] = "-outTxt";
			if(cmd.getOptionValue("inData").endsWith(".train.svmf")){
				temp[args.length+1] = cmd.getOptionValue("inData").substring(0,cmd.getOptionValue("inData").indexOf(".train.svmf")) + ".train.mope.txt";
			}
			else if(cmd.getOptionValue("inData").endsWith(".svmf")){
				temp[args.length+1] = cmd.getOptionValue("inData").substring(0,cmd.getOptionValue("inData").indexOf(".svmf")) + ".train.mope.txt";
			}
			else{
				temp[args.length+1] = cmd.getOptionValue("inData") + ".train.mope.txt";
			}
			args = temp;
			try{
				cmd = parser.parse(options, args);
			}
			catch(ParseException e){
				System.exit(1);
			}
		}
		
		MyErrorHandler meh = new MyErrorHandler(Trainer.class);
		MyProgressHandler mph = new MyProgressHandler(Trainer.class);
		Trainer trainer = new Trainer(cmd,System.out,meh,mph);
		trainer.startOperation();
	}

}
