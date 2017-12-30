package com.jukusoft.mmo.engine.service.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.jukusoft.mmo.engine.service.AfterDrawService;
import com.jukusoft.mmo.engine.service.DrawService;
import com.jukusoft.mmo.engine.service.IService;

public class SpriteBatchService implements IService, DrawService, AfterDrawService {

    //sprite batch
    protected SpriteBatch batch = null;

    /**
    * default constructor
    */
    public SpriteBatchService () {
        //
    }

    @Override
    public void onStart() {
        Gdx.app.debug("SpriteBatchService", "create sprite batch.");

        // create sprite batcher
        this.batch = new SpriteBatch();
    }

    @Override
    public void onStop() {

    }

    @Override
    public void draw() {
        //beginn rendering process
        this.batch.begin();
    }

    @Override
    public void afterDraw() {
        //flush rendering
        this.batch.end();
    }

    /**
    * get instance of sprite batch
    */
    public SpriteBatch getBatch() {
        return this.batch;
    }
}