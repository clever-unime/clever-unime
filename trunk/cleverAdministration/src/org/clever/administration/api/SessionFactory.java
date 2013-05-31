package org.clever.administration.api;

public interface SessionFactory {
	/**
	 * Ritorna la sessione da cui si potra' invocare le api (isolate per thread)
	 * @return
	 */
	public Session getSession();

}
