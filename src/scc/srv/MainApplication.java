package scc.srv;

import jakarta.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

public class MainApplication extends Application {
	
	private final Set<Object> singletons = new HashSet<>();
	private final Set<Class<?>> resources = new HashSet<>();
	
	public MainApplication() {
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		singletons.add(new MediaResource());
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}
	
	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
	
}
