package org.jboss.seam.cron;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;

@SuppressWarnings("serial")
@RequestScoped
public class EmptyRequestScopedBean implements Serializable{

	public String sayHello() {
		return "Hello";
	}
}
