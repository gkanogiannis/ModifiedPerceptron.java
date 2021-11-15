package utumno.mope.utils;

import utumno.mope.Trainer;
import utumno.mope.Evaluator;

public class MyProgressHandler extends ProgressHandler{
	@SuppressWarnings("rawtypes")
	private Class thread;
	
	@SuppressWarnings("rawtypes")
	public MyProgressHandler(Class thread){
		this.thread = thread;
	}
	
	public void updateCurrentProgress(String msg) {
		if(thread.getName().equals(Trainer.class.getName())){
			System.err.println("Class="+msg);
		}
		if(thread.getName().equals(Evaluator.class.getName())){
			System.err.println("Using ClassVectorEvaluator");
		}
	}
	
}