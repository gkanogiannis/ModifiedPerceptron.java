/*
 *
 * ModifiedPerceptron.java utumno.mope.utils.MyThread
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
package utumno.mope.utils;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.apache.commons.cli.CommandLine;

public abstract class MyThread implements Runnable{

	private volatile Thread thisThread;
	private volatile boolean paused = false;
	protected PrintStream printstream;
	protected Thread.UncaughtExceptionHandler errorHandler;
	protected ProgressHandler progressHandler;
	protected CommandLine options;
	
	public void startOperation(){
        thisThread = new Thread(this);
        thisThread.setUncaughtExceptionHandler(this.errorHandler);
        thisThread.start();
    }
	
	public synchronized void stopOperation(){
        thisThread = null;
    }
	
	public synchronized void pauseOperation(boolean pause){
        paused = pause;
        if(!paused){
            notify();
        }
    }
	
	public boolean checkOperation(){
        if(paused){
            synchronized(this){
                try{
                    while(paused){
                        wait();
                    }
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }

        if(thisThread == null){
            return false;
        }
        return true;
    }
	
	/** Get CPU time in nanoseconds. */
	public long getCpuTime( ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        bean.getCurrentThreadCpuTime( ) : 0L;
	}
	 
	/** Get user time in nanoseconds. */
	public long getUserTime( ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        bean.getCurrentThreadUserTime( ) : 0L;
	}

	/** Get system time in nanoseconds. */
	public long getSystemTime( ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        (bean.getCurrentThreadCpuTime( ) - bean.getCurrentThreadUserTime( )) : 0L;
	}
}
