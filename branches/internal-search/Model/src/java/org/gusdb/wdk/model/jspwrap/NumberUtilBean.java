package org.gusdb.wdk.model.jspwrap;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class provides a variety of methods to generate
 * numbers to be used in JSP pages via JSTL/EL calls.
 * 
 * @author rdoherty
 */
public class NumberUtilBean {

	public static AtomicInteger ID = new AtomicInteger(0);
	
	public int getNextId() {
		return ID.incrementAndGet();
	}
	
}
