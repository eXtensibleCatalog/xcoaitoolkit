/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the  
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/. 
  *
  */

package info.extensiblecatalog.OAIToolkit.db;

import info.extensiblecatalog.OAIToolkit.db.managers.PrototypeMgr;

/**
 * The manager of records in the sets table
 * @author Peter Kiraly
 */
public class ResumptionTokensMgr extends PrototypeMgr {

	public ResumptionTokensMgr() {
		super("resumption_tokens");
	}

}
