package com.kdcloud.server.rest.application;

import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;

import com.kdcloud.server.entity.ServerParameter;
import com.kdcloud.server.rest.resource.WorkerServerResource;
import com.kdcloud.server.tasks.TaskQueue;

public class MainApplication extends Application {
	
	Context applicationContext = new GAEContext();
	
	Verifier verifier = new OAuthVerifier();
	
	@Override
	public Restlet createInboundRoot() {
		getLogger().setLevel(Level.INFO);

		Router router = new Router(getContext());
		
		router.attach(TaskQueue.WORKER_URI + ServerParameter.TASK_ID, WorkerServerResource.class);

		ChallengeAuthenticator guard = new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "testRealm");
		guard.setVerifier(verifier);
		guard.setNext(new KDApplication(applicationContext));
		router.attachDefault(guard);

		return router;

	}

}
