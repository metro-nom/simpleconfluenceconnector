package com.metronom.simpleconfluenceconnector.model;

import java.util.*;

public class ConfluencePageJSON {

    private final List<ConfluencePageResult> results;

    public ConfluencePageJSON(final List<ConfluencePageResult> results) {
        this.results = results;
    }

    public List<ConfluencePageResult> getResults() {
        return this.results;
    }

}
