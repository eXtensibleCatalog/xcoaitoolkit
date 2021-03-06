/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the  
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/. 
  *
  */

package test.extensiblecatalog.OAIToolkit.oai;

import info.extensiblecatalog.OAIToolkit.oai.MetadataFormatUnmarshaler;
import info.extensiblecatalog.OAIToolkit.oai.MetadataFormats;
import junit.framework.TestCase;

public class MetadataFormatUnmarshalerTestCase extends TestCase {
	
	public void testUnmarshaler() {
		String dir = "E:/web_projects/ILSToolkit/resources/";
		String fileName = dir + "metadataFormats.xml";
		String mapFile = dir + "metadata-format-mapping.xml";
		MetadataFormats formats = MetadataFormatUnmarshaler.load(fileName, mapFile);
		System.out.println(formats);
	}

}
