/*
 *
 * ModifiedPerceptron.java utumno.mope.utils.MyProgressHandler
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