package com.metronom.simpleconfluenceconnector.model;

public class ConfluencePageBody {

    private final ConfluencePageStorage storage;

    public ConfluencePageBody(final ConfluencePageStorage storage) {
        this.storage = storage;
    }

    public ConfluencePageStorage getStorage() {
        return this.storage;
    }

}
