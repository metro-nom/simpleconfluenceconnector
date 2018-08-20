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

    private static void appendHTMLEntities(final StringBuilder toParse) {
        toParse.append("<?xml version=\"1.0\"?><!DOCTYPE confluence [");
        toParse.append("<!ENTITY exclamation \"&#33;\">");
        toParse.append("<!ENTITY quot \"&#34;\">");
        toParse.append("<!ENTITY percent \"&#37;\">");
        toParse.append("<!ENTITY amp \"&#38;\">");
        toParse.append("<!ENTITY apos \"&#39;\">");
        toParse.append("<!ENTITY add \"&#43;\">");
        toParse.append("<!ENTITY lt \"&#60;\">");
        toParse.append("<!ENTITY equal \"&#61;\">");
        toParse.append("<!ENTITY gt \"&#62;\">");
        toParse.append("<!ENTITY nbsp \"&#160;\">");
        toParse.append("<!ENTITY iexcl \"&#161;\">");
        toParse.append("<!ENTITY cent \"&#162;\">");
        toParse.append("<!ENTITY pound \"&#163;\">");
        toParse.append("<!ENTITY curren \"&#164;\">");
        toParse.append("<!ENTITY yen \"&#165;\">");
        toParse.append("<!ENTITY brvbar \"&#166;\">");
        toParse.append("<!ENTITY sect \"&#167;\">");
        toParse.append("<!ENTITY uml \"&#168;\">");
        toParse.append("<!ENTITY copy \"&#169;\">");
        toParse.append("<!ENTITY ordf \"&#170;\">");
        toParse.append("<!ENTITY laquo \"&#171;\">");
        toParse.append("<!ENTITY not \"&#172;\">");
        toParse.append("<!ENTITY shy \"&#173;\">");
        toParse.append("<!ENTITY reg \"&#174;\">");
        toParse.append("<!ENTITY macr \"&#175;\">");
        toParse.append("<!ENTITY deg \"&#176;\">");
        toParse.append("<!ENTITY plusmn \"&#177;\">");
        toParse.append("<!ENTITY sup2 \"&#178;\">");
        toParse.append("<!ENTITY sup3 \"&#179;\">");
        toParse.append("<!ENTITY acute \"&#180;\">");
        toParse.append("<!ENTITY micro \"&#181;\">");
        toParse.append("<!ENTITY para \"&#182;\">");
        toParse.append("<!ENTITY middot \"&#183;\">");
        toParse.append("<!ENTITY cedil \"&#184;\">");
        toParse.append("<!ENTITY sup1 \"&#185;\">");
        toParse.append("<!ENTITY ordm \"&#186;\">");
        toParse.append("<!ENTITY raquo \"&#187;\">");
        toParse.append("<!ENTITY frac14 \"&#188;\">");
        toParse.append("<!ENTITY frac12 \"&#189;\">");
        toParse.append("<!ENTITY frac34 \"&#190;\">");
        toParse.append("<!ENTITY iquest \"&#191;\">");
        toParse.append("<!ENTITY Agrave \"&#192;\">");
        toParse.append("<!ENTITY Aacute \"&#193;\">");
        toParse.append("<!ENTITY Acirc \"&#194;\">");
        toParse.append("<!ENTITY Atilde \"&#195;\">");
        toParse.append("<!ENTITY Auml \"&#196;\">");
        toParse.append("<!ENTITY Aring \"&#197;\">");
        toParse.append("<!ENTITY AElig \"&#198;\">");
        toParse.append("<!ENTITY Ccedil \"&#199;\">");
        toParse.append("<!ENTITY Egrave \"&#200;\">");
        toParse.append("<!ENTITY Eacute \"&#201;\">");
        toParse.append("<!ENTITY Ecirc \"&#202;\">");
        toParse.append("<!ENTITY Euml \"&#203;\">");
        toParse.append("<!ENTITY Igrave \"&#204;\">");
        toParse.append("<!ENTITY Iacute \"&#205;\">");
        toParse.append("<!ENTITY Icirc \"&#206;\">");
        toParse.append("<!ENTITY Iuml \"&#207;\">");
        toParse.append("<!ENTITY ETH \"&#208;\">");
        toParse.append("<!ENTITY Ntilde \"&#209;\">");
        toParse.append("<!ENTITY Ograve \"&#210;\">");
        toParse.append("<!ENTITY Oacute \"&#211;\">");
        toParse.append("<!ENTITY Ocirc \"&#212;\">");
        toParse.append("<!ENTITY Otilde \"&#213;\">");
        toParse.append("<!ENTITY Ouml \"&#214;\">");
        toParse.append("<!ENTITY times \"&#215;\">");
        toParse.append("<!ENTITY Oslash \"&#216;\">");
        toParse.append("<!ENTITY Ugrave \"&#217;\">");
        toParse.append("<!ENTITY Uacute \"&#218;\">");
        toParse.append("<!ENTITY Ucirc \"&#219;\">");
        toParse.append("<!ENTITY Uuml \"&#220;\">");
        toParse.append("<!ENTITY Yacute \"&#221;\">");
        toParse.append("<!ENTITY THORN \"&#222;\">");
        toParse.append("<!ENTITY szlig \"&#223;\">");
        toParse.append("<!ENTITY agrave \"&#224;\">");
        toParse.append("<!ENTITY aacute \"&#225;\">");
        toParse.append("<!ENTITY acirc \"&#226;\">");
        toParse.append("<!ENTITY atilde \"&#227;\">");
        toParse.append("<!ENTITY auml \"&#228;\">");
        toParse.append("<!ENTITY aring \"&#229;\">");
        toParse.append("<!ENTITY aelig \"&#230;\">");
        toParse.append("<!ENTITY ccedil \"&#231;\">");
        toParse.append("<!ENTITY egrave \"&#232;\">");
        toParse.append("<!ENTITY eacute \"&#233;\">");
        toParse.append("<!ENTITY ecirc \"&#234;\">");
        toParse.append("<!ENTITY euml \"&#235;\">");
        toParse.append("<!ENTITY igrave \"&#236;\">");
        toParse.append("<!ENTITY iacute \"&#237;\">");
        toParse.append("<!ENTITY icirc \"&#238;\">");
        toParse.append("<!ENTITY iuml \"&#239;\">");
        toParse.append("<!ENTITY eth \"&#240;\">");
        toParse.append("<!ENTITY ntilde \"&#241;\">");
        toParse.append("<!ENTITY ograve \"&#242;\">");
        toParse.append("<!ENTITY oacute \"&#243;\">");
        toParse.append("<!ENTITY ocirc \"&#244;\">");
        toParse.append("<!ENTITY otilde \"&#245;\">");
        toParse.append("<!ENTITY ouml \"&#246;\">");
        toParse.append("<!ENTITY divide \"&#247;\">");
        toParse.append("<!ENTITY oslash \"&#248;\">");
        toParse.append("<!ENTITY ugrave \"&#249;\">");
        toParse.append("<!ENTITY uacute \"&#250;\">");
        toParse.append("<!ENTITY ucirc \"&#251;\">");
        toParse.append("<!ENTITY uuml \"&#252;\">");
        toParse.append("<!ENTITY yacute \"&#253;\">");
        toParse.append("<!ENTITY thorn \"&#254;\">");
        toParse.append("<!ENTITY yuml \"&#255;\">");
        toParse.append("<!ENTITY OElig \"&#338;\">");
        toParse.append("<!ENTITY oelig \"&#339;\">");
        toParse.append("<!ENTITY Scaron \"&#352;\">");
        toParse.append("<!ENTITY scaron \"&#353;\">");
        toParse.append("<!ENTITY Yuml \"&#376;\">");
        toParse.append("<!ENTITY fnof \"&#402;\">");
        toParse.append("<!ENTITY circ \"&#710;\">");
        toParse.append("<!ENTITY tilde \"&#732;\">");
        toParse.append("<!ENTITY Alpha \"&#913;\">");
        toParse.append("<!ENTITY Beta \"&#914;\">");
        toParse.append("<!ENTITY Gamma \"&#915;\">");
        toParse.append("<!ENTITY Delta \"&#916;\">");
        toParse.append("<!ENTITY Epsilon \"&#917;\">");
        toParse.append("<!ENTITY Zeta \"&#918;\">");
        toParse.append("<!ENTITY Eta \"&#919;\">");
        toParse.append("<!ENTITY Theta \"&#920;\">");
        toParse.append("<!ENTITY Iota \"&#921;\">");
        toParse.append("<!ENTITY Kappa \"&#922;\">");
        toParse.append("<!ENTITY Lambda \"&#923;\">");
        toParse.append("<!ENTITY Mu \"&#924;\">");
        toParse.append("<!ENTITY Nu \"&#925;\">");
        toParse.append("<!ENTITY Xi \"&#926;\">");
        toParse.append("<!ENTITY Omicron \"&#927;\">");
        toParse.append("<!ENTITY Pi \"&#928;\">");
        toParse.append("<!ENTITY Rho \"&#929;\">");
        toParse.append("<!ENTITY Sigma \"&#931;\">");
        toParse.append("<!ENTITY Tau \"&#932;\">");
        toParse.append("<!ENTITY Upsilon \"&#933;\">");
        toParse.append("<!ENTITY Phi \"&#934;\">");
        toParse.append("<!ENTITY Chi \"&#935;\">");
        toParse.append("<!ENTITY Psi \"&#936;\">");
        toParse.append("<!ENTITY Omega \"&#937;\">");
        toParse.append("<!ENTITY alpha \"&#945;\">");
        toParse.append("<!ENTITY beta \"&#946;\">");
        toParse.append("<!ENTITY gamma \"&#947;\">");
        toParse.append("<!ENTITY delta \"&#948;\">");
        toParse.append("<!ENTITY epsilon \"&#949;\">");
        toParse.append("<!ENTITY zeta \"&#950;\">");
        toParse.append("<!ENTITY eta \"&#951;\">");
        toParse.append("<!ENTITY theta \"&#952;\">");
        toParse.append("<!ENTITY iota \"&#953;\">");
        toParse.append("<!ENTITY kappa \"&#954;\">");
        toParse.append("<!ENTITY lambda \"&#955;\">");
        toParse.append("<!ENTITY mu \"&#956;\">");
        toParse.append("<!ENTITY nu \"&#957;\">");
        toParse.append("<!ENTITY xi \"&#958;\">");
        toParse.append("<!ENTITY omicron \"&#959;\">");
        toParse.append("<!ENTITY pi \"&#960;\">");
        toParse.append("<!ENTITY rho \"&#961;\">");
        toParse.append("<!ENTITY sigmaf \"&#962;\">");
        toParse.append("<!ENTITY sigma \"&#963;\">");
        toParse.append("<!ENTITY tau \"&#964;\">");
        toParse.append("<!ENTITY upsilon \"&#965;\">");
        toParse.append("<!ENTITY phi \"&#966;\">");
        toParse.append("<!ENTITY chi \"&#967;\">");
        toParse.append("<!ENTITY psi \"&#968;\">");
        toParse.append("<!ENTITY omega \"&#969;\">");
        toParse.append("<!ENTITY thetasym \"&#977;\">");
        toParse.append("<!ENTITY upsih \"&#978;\">");
        toParse.append("<!ENTITY piv \"&#982;\">");
        toParse.append("<!ENTITY ensp \"&#8194;\">");
        toParse.append("<!ENTITY emsp \"&#8195;\">");
        toParse.append("<!ENTITY thinsp \"&#8201;\">");
        toParse.append("<!ENTITY zwnj \"&#8204;\">");
        toParse.append("<!ENTITY zwj \"&#8205;\">");
        toParse.append("<!ENTITY lrm \"&#8206;\">");
        toParse.append("<!ENTITY rlm \"&#8207;\">");
        toParse.append("<!ENTITY ndash \"&#8211;\">");
        toParse.append("<!ENTITY mdash \"&#8212;\">");
        toParse.append("<!ENTITY horbar \"&#8213;\">");
        toParse.append("<!ENTITY lsquo \"&#8216;\">");
        toParse.append("<!ENTITY rsquo \"&#8217;\">");
        toParse.append("<!ENTITY sbquo \"&#8218;\">");
        toParse.append("<!ENTITY ldquo \"&#8220;\">");
        toParse.append("<!ENTITY rdquo \"&#8221;\">");
        toParse.append("<!ENTITY bdquo \"&#8222;\">");
        toParse.append("<!ENTITY dagger \"&#8224;\">");
        toParse.append("<!ENTITY Dagger \"&#8225;\">");
        toParse.append("<!ENTITY bull \"&#8226;\">");
        toParse.append("<!ENTITY hellip \"&#8230;\">");
        toParse.append("<!ENTITY permil \"&#8240;\">");
        toParse.append("<!ENTITY prime \"&#8242;\">");
        toParse.append("<!ENTITY Prime \"&#8243;\">");
        toParse.append("<!ENTITY lsaquo \"&#8249;\">");
        toParse.append("<!ENTITY rsaquo \"&#8250;\">");
        toParse.append("<!ENTITY oline \"&#8254;\">");
        toParse.append("<!ENTITY frasl \"&#8260;\">");
        toParse.append("<!ENTITY euro \"&#8364;\">");
        toParse.append("<!ENTITY image \"&#8465;\">");
        toParse.append("<!ENTITY weierp \"&#8472;\">");
        toParse.append("<!ENTITY real \"&#8476;\">");
        toParse.append("<!ENTITY trade \"&#8482;\">");
        toParse.append("<!ENTITY alefsym \"&#8501;\">");
        toParse.append("<!ENTITY larr \"&#8592;\">");
        toParse.append("<!ENTITY uarr \"&#8593;\">");
        toParse.append("<!ENTITY rarr \"&#8594;\">");
        toParse.append("<!ENTITY darr \"&#8595;\">");
        toParse.append("<!ENTITY harr \"&#8596;\">");
        toParse.append("<!ENTITY crarr \"&#8629;\">");
        toParse.append("<!ENTITY lArr \"&#8656;\">");
        toParse.append("<!ENTITY uArr \"&#8657;\">");
        toParse.append("<!ENTITY rArr \"&#8658;\">");
        toParse.append("<!ENTITY dArr \"&#8659;\">");
        toParse.append("<!ENTITY hArr \"&#8660;\">");
        toParse.append("<!ENTITY forall \"&#8704;\">");
        toParse.append("<!ENTITY part \"&#8706;\">");
        toParse.append("<!ENTITY exist \"&#8707;\">");
        toParse.append("<!ENTITY empty \"&#8709;\">");
        toParse.append("<!ENTITY nabla \"&#8711;\">");
        toParse.append("<!ENTITY isin \"&#8712;\">");
        toParse.append("<!ENTITY notin \"&#8713;\">");
        toParse.append("<!ENTITY ni \"&#8715;\">");
        toParse.append("<!ENTITY prod \"&#8719;\">");
        toParse.append("<!ENTITY sum \"&#8721;\">");
        toParse.append("<!ENTITY minus \"&#8722;\">");
        toParse.append("<!ENTITY lowast \"&#8727;\">");
        toParse.append("<!ENTITY radic \"&#8730;\">");
        toParse.append("<!ENTITY prop \"&#8733;\">");
        toParse.append("<!ENTITY infin \"&#8734;\">");
        toParse.append("<!ENTITY ang \"&#8736;\">");
        toParse.append("<!ENTITY and \"&#8743;\">");
        toParse.append("<!ENTITY or \"&#8744;\">");
        toParse.append("<!ENTITY cap \"&#8745;\">");
        toParse.append("<!ENTITY cup \"&#8746;\">");
        toParse.append("<!ENTITY int \"&#8747;\">");
        toParse.append("<!ENTITY there4 \"&#8756;\">");
        toParse.append("<!ENTITY sim \"&#8764;\">");
        toParse.append("<!ENTITY cong \"&#8773;\">");
        toParse.append("<!ENTITY asymp \"&#8776;\">");
        toParse.append("<!ENTITY ne \"&#8800;\">");
        toParse.append("<!ENTITY equiv \"&#8801;\">");
        toParse.append("<!ENTITY le \"&#8804;\">");
        toParse.append("<!ENTITY ge \"&#8805;\">");
        toParse.append("<!ENTITY sub \"&#8834;\">");
        toParse.append("<!ENTITY sup \"&#8835;\">");
        toParse.append("<!ENTITY nsub \"&#8836;\">");
        toParse.append("<!ENTITY sube \"&#8838;\">");
        toParse.append("<!ENTITY supe \"&#8839;\">");
        toParse.append("<!ENTITY oplus \"&#8853;\">");
        toParse.append("<!ENTITY otimes \"&#8855;\">");
        toParse.append("<!ENTITY perp \"&#8869;\">");
        toParse.append("<!ENTITY sdot \"&#8901;\">");
        toParse.append("<!ENTITY lceil \"&#8968;\">");
        toParse.append("<!ENTITY rceil \"&#8969;\">");
        toParse.append("<!ENTITY lfloor \"&#8970;\">");
        toParse.append("<!ENTITY rfloor \"&#8971;\">");
        toParse.append("<!ENTITY lang \"&#9001;\">");
        toParse.append("<!ENTITY rang \"&#9002;\">");
        toParse.append("<!ENTITY loz \"&#9674;\">");
        toParse.append("<!ENTITY spades \"&#9824;\">");
        toParse.append("<!ENTITY clubs \"&#9827;\">");
        toParse.append("<!ENTITY hearts \"&#9829;\">");
        toParse.append("<!ENTITY diams \"&#9830;\">");
        toParse.append("]>");
    }

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
                final StringBuilder toParse = new StringBuilder();
                SimpleConfluenceConnector.appendHTMLEntities(toParse);
                toParse.append(page.getStorage());
                final Document document = builder.parse(new ByteArrayInputStream(toParse.toString().getBytes("UTF-8")));
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
