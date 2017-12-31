package com.jukusoft.mmo.engine.service;

public interface AfterDrawService extends IService {

    /**
    * method which should be executed after beforeDraw services to push sprite batch to gpu and so on
    */
    public void afterDraw ();

}
