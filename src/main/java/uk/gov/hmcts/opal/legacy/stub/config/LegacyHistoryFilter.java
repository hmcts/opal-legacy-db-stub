package uk.gov.hmcts.opal.legacy.stub.config;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.xml.Xml;
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Applies the legacy history API's inclusive date and item-type filters to fixture responses. */
public class LegacyHistoryFilter implements ResponseTransformerV2 {

    @Override
    public String getName() {
        return "legacy-history-filters";
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    @Override
    public Response transform(Response response, ServeEvent serveEvent) {
        Map<?, ?> filters = Json.read(serveEvent.getRequest().getBodyAsString(), Map.class);
        LocalDate from = date(filters.get("from_date"));
        LocalDate to = date(filters.get("to_date"));
        Set<String> types = filters.get("item_types") instanceof List<?> selectedTypes
            ? selectedTypes.stream().map(Object::toString).map(type -> type.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet()) : Set.of();

        Document document = Xml.read(response.getBodyAsString());
        NodeList items = document.getElementsByTagName("history_items_element");
        for (int index = items.getLength() - 1; index >= 0; index--) {
            Element item = (Element) items.item(index);
            LocalDate posted = LocalDate.parse(text(item, "posted_date").substring(0, 10));
            String type = text(item, "type").toLowerCase(Locale.ROOT);
            if ((from != null && posted.isBefore(from)) || (to != null && posted.isAfter(to))
                || (!types.isEmpty() && !types.contains(type))) {
                item.getParentNode().removeChild(item);
            }
        }
        return Response.Builder.like(response).body(Xml.prettyPrint(document)).build();
    }

    private static LocalDate date(Object value) {
        return value == null ? null : LocalDate.parse(value.toString());
    }

    private static String text(Element element, String name) {
        return element.getElementsByTagName(name).item(0).getTextContent();
    }
}
