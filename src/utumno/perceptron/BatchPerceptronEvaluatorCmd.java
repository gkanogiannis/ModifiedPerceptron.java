package utumno.perceptron;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import utumno.mope.Evaluator;
import utumno.mope.utils.MyErrorHandler;
import utumno.mope.utils.MyProgressHandler;

public class BatchPerceptronEvaluatorCmd {

	@SuppressWarnings("static-access")
	private Options generateOptions(){
		Options options = new Options();
		Option inData = OptionBuilder.withArgName("inData").hasArg().isRequired()
									 .withDescription("Test data location(test svmf file).")
									 .create("inData");
		Option inModel = OptionBuilder.withArgName("inModel").hasArg().isRequired()
									 .withDescription("Train data location(train model file).")
									 .create("inModel");
		Option outPred = OptionBuilder.withArgName("outPred").hasArg()
		 							 .withDescription("Output data location(predictions file).")
		 							 .create("outPred");
		Option outTxt = OptionBuilder.withArgName("outTxt").hasArg()
		     						 .withDescription("Text file for the results.")
		     						 .create("outTxt");
		Option help = new Option( "help", "Print this message." );
				
		options.addOption(inData);
		options.addOption(inModel);
		options.addOption(outPred);
		options.addOption(outTxt);
		options.addOption(help);
		return options;
	}
	
	public static void main(String[] args) {
		BatchPerceptronEvaluatorCmd builder = new BatchPerceptronEvaluatorCmd();
		Options options = builder.generateOptions();
		CommandLineParser parser = new PosixParser();
		CommandLine cmd =null;
		try{
			cmd = parser.parse(options, args);
		}
		catch(ParseException e){
			System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
			HelpFormatter f = new HelpFormatter();
			f.printHelp("ClassVectorEvaluatorCmd", options);
			System.exit(1);
		}
		if(cmd.hasOption("help")){
			HelpFormatter f = new HelpFormatter();
			f.printHelp("A", options);
			System.exit(1);
		}
		if(!cmd.hasOption("outPred")){
			String[] temp = new String[args.length+2];
			System.arraycopy(args, 0, temp, 0, args.length);
			temp[args.length] = "-outPred";
			if(cmd.getOptionValue("inData").endsWith(".test.svmf")){
				temp[args.length+1] = cmd.getOptionValue("inData").substring(0,cmd.getOptionValue("inData").indexOf(".test.svmf")) + ".bape.pred";
			}
			else if(cmd.getOptionValue("inData").endsWith(".svmf")){
				temp[args.length+1] = cmd.getOptionValue("inData").substring(0,cmd.getOptionValue("inData").indexOf(".svmf")) + ".bape.pred";
			}
			else{
				temp[args.length+1] = cmd.getOptionValue("inData") + ".bape.pred";
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
			if(cmd.getOptionValue("inData").endsWith(".test.svmf")){
				temp[args.length+1] = cmd.getOptionValue("inData").substring(0,cmd.getOptionValue("inData").indexOf(".test.svmf")) + ".test.bape.txt";
			}
			else if(cmd.getOptionValue("inData").endsWith(".svmf")){
				temp[args.length+1] = cmd.getOptionValue("inData").substring(0,cmd.getOptionValue("inData").indexOf(".svmf")) + ".test.bape.txt";
			}
			else{
				temp[args.length+1] = cmd.getOptionValue("inData") + ".test.bape.txt";
			}
			args = temp;
			try{
				cmd = parser.parse(options, args);
			}
			catch(ParseException e){
				System.exit(1);
			}
		}
		
		MyErrorHandler meh = new MyErrorHandler(Evaluator.class);
		MyProgressHandler mph = new MyProgressHandler(Evaluator.class);
		Evaluator evaluator = new Evaluator(cmd,System.out,meh,mph);
		evaluator.startOperation();
	}

}
