package utumno.mope.utils;
public class MyErrorHandler implements Thread.UncaughtExceptionHandler{
		@SuppressWarnings("rawtypes")
		private Class thread;
		
		@SuppressWarnings("rawtypes")
		public MyErrorHandler(Class thread){
			this.thread = thread;
		}

		public void uncaughtException(Thread t, Throwable e) {
			System.err.println(thread.getName());
			System.err.println("Error caught. Message is "+e.getMessage()+" "+e.getClass());
			e.printStackTrace();
			System.exit(1);
		}
	}