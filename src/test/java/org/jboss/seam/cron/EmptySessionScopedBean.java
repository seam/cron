package org.jboss.seam.cron;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;

@SuppressWarnings("serial")
@SessionScoped
public class EmptySessionScopedBean implements Serializable{

	public String sayHello() {
		return "Hello";
	}
}
