package com.metronom.simpleconfluenceconnector.model;

import org.w3c.dom.*;

public class ConfluencePageInformation {

    private final Document document;

    private final String id;

    private final String space;

    private final String title;

    private final int version;

    public ConfluencePageInformation(
        final String id,
        final Document document,
        final String space,
        final String title,
        final int version
    ) {
        this.id = id;
        this.document = document;
        this.space = space;
        this.title = title;
        this.version = version;
    }

    public ConfluencePageInformation advanceVersion() {
        return
            new ConfluencePageInformation(
                this.getId(),
                this.getDocument(),
                this.getSpace(),
                this.getTitle(),
                this.getVersion() + 1
            );
    }

    public Document getDocument() {
        return this.document;
    }

    public String getId() {
        return this.id;
    }

    public String getSpace() {
        return this.space;
    }

    public String getTitle() {
        return this.title;
    }

    public int getVersion() {
        return this.version;
    }

    public ConfluencePageInformation setDocument(final Document document) {
        return
            new ConfluencePageInformation(
                this.getId(),
                document,
                this.getSpace(),
                this.getTitle(),
                this.getVersion()
            );
    }

}