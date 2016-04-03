package com.amun.id;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bson.Document;

import com.amun.id.annotation.ProcessorManager;
import com.amun.id.exception.CommandNotFoundException;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.statics.F;
import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.Message;
import com.mario.entity.message.impl.HttpMessage;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.encrypt.rsa.KeyPairHelper;
import com.nhb.common.encrypt.rsa.SignatureHelper;
import com.nhb.common.utils.FileSystemUtils;

public class UserHandler extends BaseMessageHandler {
	private ProcessorManager manager;
	private MongoDatabase database;
	private SignatureHelper signatureHelper;

	@Override
	public void init(PuObjectRO initParams) {
		if (initParams.variableExists(F.MONGODB)) {
			initMongoDatabase(getApi().getMongoClient(initParams.getString(F.MONGODB)).getDatabase("amun_id"));
		}
		String privateKeyPath = initParams.getString(F.PRIVATE_KEY);

		KeyPairHelper keyPairHelper = new KeyPairHelper();
		try {
			keyPairHelper.loadPrivateKey(new File(
					FileSystemUtils.createPathFrom(FileSystemUtils.getBasePathForClass(getClass()), privateKeyPath)));
		} catch (IOException | GeneralSecurityException e1) {
			throw new RuntimeException("load private key error", e1);
		}

		signatureHelper = new SignatureHelper();
		signatureHelper.setKeyPairHelper(keyPairHelper);

		manager = new ProcessorManager();
		try {
			manager.init(this);
		} catch (Exception e) {
			getLogger().error("error while init procesors", e);
		}
	}

	private void createDatabaseIndexes(MongoCollection<Document> collection, List<Document> tobeIndexed) {
		for (Document index : collection.listIndexes()) {
			index = (Document) index.get(F.KEY);
			List<Integer> markToRemove = new ArrayList<>();
			for (int i = 0; i < tobeIndexed.size(); i++) {
				if (tobeIndexed.get(i).equals(index)) {
					markToRemove.add(i);
				}
			}
			if (markToRemove.size() > 0) {
				while (markToRemove.size() > 0) {
					tobeIndexed.remove(markToRemove.remove(markToRemove.size() - 1).intValue());
				}
			}
			if (tobeIndexed.size() == 0) {
				break;
			}
		}
		for (Document index : tobeIndexed) {
			getLogger().debug("create index: " + index);
			IndexOptions options = new IndexOptions();
			options.unique(true);
			collection.createIndex(index, options);
		}
	}

	private void initMongoDatabase(MongoDatabase database) {
		this.database = database;

		createDatabaseIndexes(this.database.getCollection(F.USER),
				new ArrayList<>(Arrays.asList(new Document().append(F.USER_ID, 1))));

		createDatabaseIndexes(this.database.getCollection(F.USER),
				new ArrayList<>(Arrays.asList(new Document().append(F.USERNAME, 1))));
		
		createDatabaseIndexes(this.database.getCollection(F.AUTHENTICATOR),
				new ArrayList<>(Arrays.asList(new Document().append(F.PARTNER_NAME, 1))));
		
		createDatabaseIndexes(this.database.getCollection(F.AUTHENTICATOR),
				new ArrayList<>(Arrays.asList(new Document().append(F.AUTHENTICATOR_ID, 1))));
	}

	@Override
	public PuElement handle(Message message) {
		PuElement requestParams = message.getData();
		getLogger().debug("handing request: {}", requestParams);
		if (requestParams instanceof PuObjectRO) {
			PuObject request = (PuObject) requestParams;

			if (message instanceof HttpMessage) {
				HttpMessage httpMessage = (HttpMessage) message;
				HttpServletRequest servletRequest = (HttpServletRequest) httpMessage.getRequest();
				String ipAddress = null;
				if (servletRequest != null) {
					ipAddress = servletRequest.getHeader("X-Forwarded-For");
				}

				if (ipAddress == null) {
					ipAddress = httpMessage.getContext().getRequest().getRemoteAddr();
				}

				request.setString(F.IP_ADDRESS, ipAddress);
			}

			if (request.variableExists(F.COMMAND)) {
				String command = request.getString(F.COMMAND);
				try {
					return manager.processCommand(command, request);
				} catch (CommandNotFoundException e) {
					return new PuValue("not found processor for command `" + command + "`");
				} catch (ExecuteProcessorException e) {
					getLogger().error("execute processor exception", e);
					return PuObject
							.fromObject(new MapTuple<>(F.STATUS, 1, F.EXCEPTION, ExceptionUtils.getFullStackTrace(e)));
				}
			}
		}
		return null;
	}

	public MongoDatabase getDatabase() {
		return this.database;
	}

	public SignatureHelper getSignatureHelper() {
		return this.signatureHelper;
	}
}