package scc.srv;

import jakarta.ws.rs.core.Application;
import scc.utils.GenericExceptionMapper;

import java.util.HashSet;
import java.util.Set;

public class MainApplication extends Application {
	
	private final Set<Object> singletons = new HashSet<>();
	private final Set<Class<?>> resources = new HashSet<>();
	
	public MainApplication() {
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		resources.add(UserResource.class);
		resources.add(AuctionResource.class);
		resources.add(AccessControl.class);
		resources.add(GenericExceptionMapper.class);
		singletons.add(new MediaResource());
		singletons.add(new UserResource());
		singletons.add(new AuctionResource());
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
