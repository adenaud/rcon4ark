package com.anthonydenaud.rconark.dao;

import com.google.inject.Inject;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import com.anthonydenaud.rconark.model.Server;

import java.util.List;

/**
 * Created by Anthony on 09/10/2015.
 */
public class ServerDAO {

    private RuntimeExceptionDao<Server,Integer> dao;

    @Inject
    public ServerDAO(DatabaseHelper databaseHelper){
        dao = databaseHelper.getRuntimeExceptionDao(Server.class);
    }

    public List<Server> findAll(){
        return dao.queryForAll();
    }

    public void save(Server server){
        dao.createOrUpdate(server);
    }

    public void delete(Server server){
        dao.delete(server);
    }
}
