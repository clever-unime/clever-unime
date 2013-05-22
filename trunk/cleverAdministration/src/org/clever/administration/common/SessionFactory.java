package org.clever.administration.common;

public interface SessionFactory {
	/**
	 * Ritorna la sessione da cui si potra' invocare le api (isolate per thread)
	 * @return
	 */
	public Session getSession();

}
