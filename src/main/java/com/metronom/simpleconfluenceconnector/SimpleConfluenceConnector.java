package com.metronom.simpleconfluenceconnector;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.google.gson.*;
import com.metronom.simpleconfluenceconnector.model.*;

public class SimpleConfluenceConnector {



    private static final Gson GSON = new Gson();

    private static final String UPDATE_JSON_PATTERN =
        "{\"id\":\"%s\",\"type\":\"page\",\"title\":\"%s\",\"space\":{\"key\":\"%s\"},\"body\":{\"storage\":"
        + "{\"value\":\"%s\",\"representation\":\"storage\"}},\"version\":{\"number\":%d}}";

    private final Supplier<String> base;

    private final Supplier<String> password;

    private final Supplier<String> user;

    public SimpleConfluenceConnector(
        final Supplier<String> user,
        final Supplier<String> password,
        final Supplier<String> base
    ) {
        this.user = user;
        this.password = password;
        this.base = base;
    }

    private HttpURLConnection getConnection(final String url) throws MalformedURLException, IOException {
        final HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
        final StringBuilder login = new StringBuilder();
        login.append(this.user.get());
        login.append(":");
        login.append(this.password.get());
        connection.setRequestProperty(
            "Authorization",
            "Basic " + new String(Base64.getEncoder().encode(login.toString().getBytes()))
        );
        return connection;
    }

    public List<ConfluencePageInformation> getPageStorage(final String space, final String title)
    throws IOException, ParserConfigurationException, SAXException, TransformerException {
        final List<ConfluencePageInformation> result = new ArrayList<ConfluencePageInformation>();
        final HttpURLConnection connection =
            this.getConnection(
                String.format(
                    "%srest/api/content?title=%s&spaceKey=%s&expand=version,body.storage",
                    this.base.get(),
                    title,
                    space
                )
            );
        try (final Reader reader = new InputStreamReader(connection.getInputStream())) {
            final ConfluencePageJSON pageResult =
                SimpleConfluenceConnector.GSON.fromJson(reader, ConfluencePageJSON.class);
            for (final ConfluencePageResult page : pageResult.getResults()) {
                final Document document = StorageTransformer.parseDocumentFromStorage(page.getStorage());
                result.add(
                    new ConfluencePageInformation(
                        page.getId(),
                        document,
                        space,
                        page.getTitle(),
                        page.getVersionNumber()
                    )
                );
            }
        }
        return result;
    }

    public int updatePageStorage(final ConfluencePageInformation page)
    throws MalformedURLException, IOException, TransformerException {
        final HttpURLConnection connection =
            this.getConnection(String.format("%srest/api/content/%s", this.base.get(), page.getId()));
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        try (Writer writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(
                String.format(
                    SimpleConfluenceConnector.UPDATE_JSON_PATTERN,
                    page.getId(),
                    page.getTitle(),
                    page.getSpace(),
                    StorageTransformer.getStorageFormat(page.getDocument()),
                    page.getVersion()
                )
            );
        }
        return connection.getResponseCode();
    }

}
