/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the  
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/. 
  *
  */

package test.extensiblecatalog.OAIToolkit.utils;

import junit.framework.TestCase;

public class MemoryTest extends TestCase {
	
	public void testMemory() throws Exception {
		//Runtime runtime = Runtime.getRuntime();  
		System.out.println("max memory: " + (Runtime.getRuntime().maxMemory()/2.0) / (double)(1024*1024));
	}
}
