package com.kdcloud.server.rest.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import weka.core.Instances;

import com.kdcloud.engine.KDEngine;
import com.kdcloud.engine.Worker;
import com.kdcloud.engine.embedded.node.UserDataReader;
import com.kdcloud.lib.rest.api.EngineResource;
import com.kdcloud.lib.rest.api.TaskResource;
import com.kdcloud.lib.rest.ext.InstancesRepresentation;
import com.kdcloud.lib.rest.ext.LinkRepresentation;
import com.kdcloud.server.entity.DataTable;
import com.kdcloud.server.entity.Task;
import com.kdcloud.server.entity.User;
import com.kdcloud.server.persistence.EntityMapper;
import com.kdcloud.server.persistence.InstancesMapper;
import com.kdcloud.server.rest.application.MainApplication;
import com.kdcloud.server.rest.application.TaskQueue;
import com.kdcloud.server.rest.application.UrlHelper;
import com.kdcloud.server.rest.application.UserNotifier;

public class EngineServerResource extends KDServerResource implements
		EngineResource {
	
	private static final String QUERY_QUEUE = "queue";
	private static final String QUERY_TASK = "task";
	
	TaskQueue taskQueue;
	KDEngine engine;
	UserNotifier notifier;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		engine =  inject(KDEngine.class);
		taskQueue = inject(TaskQueue.class);
		notifier = inject(UserNotifier.class);
	}

	public Instances execute(InputStream input, Map<String, String> parameters, User applicant) throws IOException {
		Worker worker = engine.getWorker(input);
		worker.setParameter(EntityMapper.class.getName(), getEntityMapper());
		worker.setParameter(InstancesMapper.class.getName(), getInstancesMapper());
		worker.setParameter(UserDataReader.APPLICANT, applicant);
		for (String param : worker.getParameters()) {
			String value = parameters.get(param);
			getLogger().info(
					"setting parameter: " + param + "=" + value);
			worker.setParameter(param, value);
		}
		if (worker.configure())
			worker.run();
		if (worker.getStatus() == Worker.STATUS_JOB_COMPLETED) {
			return worker.getOutput();
		} else {
			throw new ResourceException(Status.CLIENT_ERROR_PRECONDITION_FAILED);
		}
	}

	public Instances execute(Form form, User applicant) {
		try {
			InputStream workflow = wrapWorkflowServerResource().get().getStream();
			return execute(workflow, form.getValuesMap(), applicant);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, e.getMessage(), e);
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
	
	public ClientResource wrapWorkflowServerResource() {
		if (!getProtocol().equals(Protocol.HTTP) && !getProtocol().equals(Protocol.HTTPS))
			throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
		String uri = getRequest().getResourceRef().getIdentifier().replace("/engine", "");
		getLogger().info("forwarding request to " + uri);
		ClientResource cr = new ClientResource(uri);
		cr.setChallengeResponse(getChallengeResponse());
		return cr;
	}

	@Override
	public Representation putWorkflow(Representation representation) {
		ClientResource cr = wrapWorkflowServerResource();
		Representation wrappedRep = cr.put(representation);
		setStatus(cr.getStatus());
		return wrappedRep;
	}

	@Override
	public Document getWorkflow() {
		ClientResource cr = wrapWorkflowServerResource();
		Document workflow = (Document) cr.get(Document.class);
		setStatus(cr.getStatus());
		return workflow;
	}

	@Override
	public void deleteWorkflow() {
		ClientResource cr = wrapWorkflowServerResource();
		cr.delete();
		setStatus(cr.getStatus());
	}
	
	public Representation createTask(Form form) {
		Task t = new Task(user);
		getEntityMapper().save(t);
		String url = UrlHelper.replaceId(MainApplication.WORKER_URI, getResourceIdentifier());
		Reference ref = new Reference(url);
		ref.addQueryParameter(QUERY_TASK, t.getUUID());
		Request req = new Request(Method.POST, ref);
		req.setEntity(form.getWebRepresentation());
		taskQueue.push(req);
		setStatus(Status.SUCCESS_ACCEPTED);
		return new LinkRepresentation(QUERY_TASK, UrlHelper.replaceId(TaskResource.URI, t.getUUID()));
	}
	
	public void consumeTask(String uuid, Form form) {
		Task t = (Task) getEntityMapper().findByUUID(uuid);
		Instances output = execute(form, t.getApplicant());
		if (output != null && !output.isEmpty()) {
			DataTable table = new DataTable();
			getInstancesMapper().save(output, table);
			t.setResult(table);
		}
		t.setCompleted(true);
		getEntityMapper().save(t);
		notifier.notify(t.getApplicant());
	}
	
	public Representation execute(Form form) {
		Instances output = execute(form, user);
		if (output == null || output.isEmpty()) {
			setStatus(Status.SUCCESS_NO_CONTENT);
			return null;
		}
		return new InstancesRepresentation(MediaType.TEXT_CSV, output);
	}

	@Override
	@Post
	public Representation handleTask(Form form) {
		String queue = getQueryValue(QUERY_QUEUE);
		String task = getQueryValue(QUERY_TASK);
		if (queue != null && queue.equals("yes")) {
			return createTask(form);
			
		} else if (task != null) {
			consumeTask(task, form);
			return null;
			
		} else {
			return execute(form);
		}
	}

}
