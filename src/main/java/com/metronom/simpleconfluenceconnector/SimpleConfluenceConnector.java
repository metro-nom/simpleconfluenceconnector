package com.metronom.simpleconfluenceconnector;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.*;

import javax.net.ssl.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.google.gson.*;
import com.metronom.simpleconfluenceconnector.model.*;

public class SimpleConfluenceConnector {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    private static final Gson GSON = new Gson();

    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private static final String UPDATE_JSON_PATTERN =
        "{\"id\":\"%s\",\"type\":\"page\",\"title\":\"%s\",\"space\":{\"key\":\"%s\"},\"body\":{\"storage\":"
        + "{\"value\":%s,\"representation\":\"storage\"}},\"version\":{\"number\":%d}}";

    public static String getStorageFormat(final Document document)
    throws TransformerException, UnsupportedEncodingException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream(131072);
        final Transformer tr = SimpleConfluenceConnector.TRANSFORMER_FACTORY.newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "no");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        tr.transform(new DOMSource(document), new StreamResult(stream));
        return stream.toString("UTF-8");
    }

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

    private HttpsURLConnection getConnection(final String url) throws MalformedURLException, IOException {
        final HttpsURLConnection connection = (HttpsURLConnection)new URL(url).openConnection();
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

    public List<ConfluencePageInformation> readPageStorage(final String space, final String title)
    throws IOException, ParserConfigurationException, SAXException, TransformerException {
        final List<ConfluencePageInformation> result = new ArrayList<ConfluencePageInformation>();
        final HttpsURLConnection connection =
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
                final DocumentBuilder builder = SimpleConfluenceConnector.DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
                final Document document = builder.parse(new ByteArrayInputStream(page.getStorage().getBytes("UTF-8")));
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

    public int writePageStorage(final ConfluencePageInformation page)
    throws MalformedURLException, IOException, TransformerException {
        final HttpsURLConnection connection =
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
                    SimpleConfluenceConnector.getStorageFormat(page.getDocument()),
                    page.getVersion()
                )
            );
        }
        return connection.getResponseCode();
    }

}
