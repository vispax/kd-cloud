package com.kdcloud.server.persistence.jdo;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.kdcloud.server.dao.UserDao;
import com.kdcloud.server.domain.datastore.DataTable;
import com.kdcloud.server.domain.datastore.User;

public class UserDaoImpl implements UserDao {
	
	PersistenceManager pm;
	
	public UserDaoImpl(PersistenceManager pm) {
		super();
		this.pm = pm;
	}

	@Override
	public User findById(String id) {
		Key k = KeyFactory.createKey(User.class.getSimpleName(), id);
		try {
			return pm.getObjectById(User.class, k);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void save(User user) {
		pm.makePersistent(user);
		setTablesKey(user);
	}
	
	//jdo should perform this automatically
	private void setTablesKey(User user) {
		DataTable e = user.getTable();
		if (e != null) {
//		for (DataTable e : user.getTables()) {
			Key k = KeyFactory.stringToKey(e.getEncodedKey());
			e.setId(k.getId());
		}
	}

	@Override
	public void delete(User user) {
//		for (DataTable e : user.getTables()) {
//			pm.deletePersistent(e);
//		}
		pm.deletePersistent(user.getTable());
		pm.deletePersistent(user);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> list() {
		Query q = pm.newQuery(User.class);
		return (List<User>) q.execute();
	}

}
