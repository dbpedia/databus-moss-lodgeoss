package org.dbpedia.databus.moss.views.search;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.dbpedia.databus.moss.views.main.MainView;

import java.util.*;

import org.apache.jena.query.*;

@Route(value = "search", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Search")
@CssImport("./views/helloworld/hello-world-view.css")
public class SearchView extends Div {

    private final List<LookupFrontendData> suggestions = new ArrayList<>();
    private final Grid<LookupFrontendData> search_grid = new Grid<>(LookupFrontendData.class);

    private final Grid<SearchResult> result_grid = new Grid<>(SearchResult.class);

    private final String databus_sparql_endpoint = "https://databus.dbpedia.org/repo/sparql";

    private final String mods_endpoint = "https://mods.tools.dbpedia.org/sparql";

    VerticalLayout searchResultArea = new VerticalLayout();

    private List<SearchResult> result_list = new ArrayList<>();

    public SearchView() {

        addClassName("search-view");
        add(new H1("Databus Metadata Overlay Search System"));


        search_grid.setItems(suggestions);

        // Setting up the search field
        TextField search_field = new TextField();
        search_field.addValueChangeListener(event -> {
            suggestions.clear();

            updateSuggestions(event.getValue());

            search_grid.getDataProvider().refreshAll();
        });
//        search_field.addValueChangeListener(event -> {
//            updateResultArea();
//        });
        search_field.setValueChangeMode(ValueChangeMode.LAZY);
        search_field.setPlaceholder("Search the Databus for Files containing or annotated with Classes, Properties etc.");
        search_field.setMinWidth("80%");
        search_field.setClearButtonVisible(true);


        //Setting up the result layout
        List<SearchResult> result_list = new ArrayList<>();



        result_grid.setItems(result_list);

        result_grid.addColumn(new ComponentRenderer<>(searchResult -> {
            return new Anchor(searchResult.downloadUrl, searchResult.downloadUrl);
        })).setHeader("Gender");

        // Setting up the search button
        Button search_void_button = new Button("Search Content");
        search_void_button.setIcon(new Icon(VaadinIcon.SEARCH));
        search_void_button.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        search_void_button.addClickListener(buttonClickEvent -> {
            String query_string = search_field.getValue();
            if (!query_string.isBlank()) {
                result_list.clear();
                String sparql_query = buildVoidQuery(query_string);
                List<SearchResult> search_results = sendSPARQL(sparql_query, databus_sparql_endpoint);

                for (SearchResult sr : search_results) {
                    result_list.add(sr);
                }
                result_grid.getDataProvider().refreshAll();
            }
        });

        Button search_annotations_button = new Button("Search Annotations");
        search_annotations_button.setIcon(new Icon(VaadinIcon.SEARCH));
        search_annotations_button.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        search_annotations_button.addClickListener(buttonClickEvent -> {
            String query_string = search_field.getValue();
            if (!query_string.isBlank()) {
                result_list.clear();
                String sparql_query = buildAnnotationQuery(query_string);
                List<SearchResult> search_results = sendSPARQL(sparql_query, mods_endpoint);

                for (SearchResult sr : search_results) {
                    result_list.add(sr);
                }
                result_grid.getDataProvider().refreshAll();
            }
        });

        HorizontalLayout buttons_horizontal = new HorizontalLayout(search_void_button, search_annotations_button);
        //HorizontalLayout buttons_horizontal = new HorizontalLayout(search_void_button);

        VerticalLayout vl = new VerticalLayout(search_field, buttons_horizontal, search_grid, result_grid);
        add(vl);
    }

    private void search(String query) {
        if (this.getUI().isPresent()) {
            UI ui = this.getUI().get();
            result_list.clear();
            String sparql_query = buildVoidQuery(query);
            List<SearchResult> search_results = sendSPARQL(sparql_query, mods_endpoint);

            for (SearchResult sr : search_results) {
                result_list.add(sr);
            }
            result_grid.getDataProvider().refreshAll();
        }
    }

    private List<SearchResult> sendSPARQL(String query, String endpoint) {
        Query q = QueryFactory.create(query);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, q);

        ResultSet rs = qexec.execSelect();

        List<SearchResult> result_list = new ArrayList<>();

        while (rs.hasNext()) {
            QuerySolution qs = rs.next();

            SearchResult row = new SearchResult(qs.get("versionURI").toString(),
                    qs.get("downloadURL").toString(),
                    qs.get("file").toString());

            result_list.add(row);
        }

        return result_list;
    }

    private void updateSuggestions(String query) {
        suggestions.clear();
        List<LookupObject> search_result;
        try {
            search_result = LookupRequester.getResult(query);
        } catch (Exception e) {
            System.out.println("Exception:" + e);
            search_result = new ArrayList<>();
        }


        for (LookupObject lo : search_result) {
            try {
                String label;
                String definition;
                if (lo.getLabel() == null) {
                    label = "";
                } else {
                    label = lo.getLabel()[0];
                }

                if (lo.getDefinition() == null) {
                    definition = "";
                } else {
                    definition = lo.getDefinition()[0];
                }
                suggestions.add(new LookupFrontendData(lo.getResource()[0], label, definition));
            } catch (Exception e) {
                System.out.println("Exception:" + e);
            }
        }
    }

    private String buildVoidQuery(String iri) {
        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"+
                "PREFIX void: <http://rdfs.org/ns/void#>\n"+
                "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n"+
                "PREFIX dct:    <http://purl.org/dc/terms/>\n"+
                "PREFIX dcat:   <http://www.w3.org/ns/dcat#>\n"+
                "PREFIX db:     <https://databus.dbpedia.org/>\n"+
                "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n"+
                "PREFIX mods:   <http://mods.tools.dbpedia.org/>\n"+
                "\n"+
                "SELECT ?versionURI ?voidStats ?file ?downloadURL ?triples {\n"+
                " SERVICE <https://mods.tools.dbpedia.org/sparql> {\n"+
                " ?voidStats void:classPartition [\n"+
                String.format("    void:class <%s> ;\n", iri)+
                "    void:triples ?triples \n"+
                " ] .\n"+
                "  \n"+
                " ?s <http://www.w3.org/ns/prov#used> ?file . # energy file\n"+
                " ?s <http://www.w3.org/ns/prov#generated> ?voidStats . # automatic content description\n"+
                "  \n"+
                " }\n"+
                "     ?dataset dataid:group ?group .\n"+
                "     ?dataset dcat:distribution ?distribution .\n"+
                "     ?dataset dataid:version ?versionURI .\n"+
                "     ?distribution dataid:file ?file .\n"+
                "     ?distribution dcat:downloadURL ?downloadURL .\n"+
                "\n"+
                "}";
    }

    private String buildAnnotationQuery(String iri) {
        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"+
                "PREFIX void: <http://rdfs.org/ns/void#>\n"+
                "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n"+
                "PREFIX dct:    <http://purl.org/dc/terms/>\n"+
                "PREFIX dcat:   <http://www.w3.org/ns/dcat#>\n"+
                "PREFIX db:     <https://databus.dbpedia.org/>\n"+
                "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n"+
                "PREFIX mods:   <http://mods.tools.dbpedia.org/>\n"+
                "\n"+
                "\n"+
                "SELECT ?versionURI ?file ?downloadURL ?annotation {\n"+
                "  ?s a <http://mods.tools.dbpedia.org/ns/demo/DemoMod> .\n"+
                "  ?s <http://www.w3.org/ns/prov#used> ?file .\n"+
                "  ?s <http://www.w3.org/ns/prov#generated> ?g .\n"+
                "  ?g <http://purl.org/dc/elements/1.1/subject> ?annotation .\n"+
                String.format("  FILTER regex(str(?annotation), '%s')\n", iri)+
                "    \n"+
                "  SERVICE <http://databus.dbpedia.org/repo/sparql> {\n"+
                "     ?dataset dataid:group ?group .\n"+
                "     ?dataset dcat:distribution ?distribution .\n"+
                "     ?dataset dataid:version ?versionURI .\n"+
                "     ?distribution dataid:file ?file .\n"+
                "     ?distribution dcat:downloadURL ?downloadURL .\n"+
                " }\n"+
                "}";
    }


    private void updateResultArea() {
        searchResultArea.removeAll();

        for (LookupFrontendData c : this.suggestions) {
            Div d = new Div();
            d.add(new H1(c.getLabel()));
            d.add(new Anchor(c.getResource()));
            d.addClassName("resultBox");
            searchResultArea.add(d);
        }

    }

}