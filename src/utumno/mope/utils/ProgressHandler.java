package utumno.mope.utils;
public abstract class ProgressHandler {
	
	private int maxProgress;
	private int currentProgress;
	
	public int getCurrentProgress() {
		return currentProgress;
	}

	public int getMaxProgress() {
		return maxProgress;
	}

	public void setCurrentProgress(int currentProgress) {
		this.currentProgress = currentProgress;
	}

	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
	}

	public abstract void updateCurrentProgress(String msg);
}
