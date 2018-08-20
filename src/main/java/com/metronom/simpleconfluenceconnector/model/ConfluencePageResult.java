package com.metronom.simpleconfluenceconnector.model;

public class ConfluencePageResult {

    private final ConfluencePageBody body;

    private final String id;

    private final String title;

    private final ConfluencePageVersion version;

    public ConfluencePageResult(
        final String id,
        final String title,
        final ConfluencePageVersion version,
        final ConfluencePageBody body
    ) {
        this.id = id;
        this.title = title;
        this.version = version;
        this.body = body;
    }

    public ConfluencePageBody getBody() {
        return this.body;
    }

    public String getId() {
        return this.id;
    }

    public String getStorage() {
        return this.getBody().getStorage().getValue();
    }

    public String getTitle() {
        return this.title;
    }

    public ConfluencePageVersion getVersion() {
        return this.version;
    }

    public int getVersionNumber() {
        return this.getVersion().getNumber();
    }

}
