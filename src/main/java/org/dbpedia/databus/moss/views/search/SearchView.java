package org.dbpedia.databus.moss.views.search;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.apache.jena.query.*;
import org.dbpedia.databus.moss.services.DatabusUtilService;
import org.dbpedia.databus.moss.views.main.MainView;
import org.dbpedia.databus.utils.LookupFrontendData;
import org.dbpedia.databus.utils.LookupObject;
import org.dbpedia.databus.utils.LookupRequester;
import org.dbpedia.databus.utils.MossUtilityFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

@Route(value = "search", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Search")
@CssImport("./views/helloworld/hello-world-view.css")
public class SearchView extends Div {

    private final List<LookupFrontendData> suggestions = new ArrayList<>();
    private final List<LookupFrontendData> selected_objects = new ArrayList<>();
    private final Grid<LookupFrontendData> selected_grid = new Grid<>();
    private final Grid<LookupFrontendData> suggestion_grid = new Grid<>();
    private final Select<String> selectDatabus = new Select<>();

    private final Grid<SearchResult> result_grid = new Grid<>();

    private final String databus_sparql_endpoint;

    private final String databus_mods_endpoint;

    private final List<SearchResult> result_list = new ArrayList<>();

    private final Logger log = LoggerFactory.getLogger(SearchView.class);

    @Autowired
    public SearchView(@Value("${databus.file.endpoint}") String databus_file_endpoint, @Value("${databus.mods.endpoint}") String databus_mods_endpoint) {

        this.databus_mods_endpoint = databus_mods_endpoint;
        this.databus_sparql_endpoint = databus_file_endpoint;

        addClassName("search-view");
        add(new H1("Databus Metadata Overlay Search System"));
        //TODO
//        RadioButtonGroup<String> type_radio_group = new RadioButtonGroup<>();
//        type_radio_group.setLabel("Select type");
//        type_radio_group.setItems("owl:Class", "owl:ObjectProperty", "any");
//        type_radio_group.setValue("owl:Class");
//
//        RadioButtonGroup<String> logical_radio_group = new RadioButtonGroup<>();
//        logical_radio_group.setLabel("Logical Conjunction");
//        logical_radio_group.setItems("AND", "OR");
//        logical_radio_group.setValue("AND");

        RadioButtonGroup<String> search_type_radio_group = new RadioButtonGroup<>();
        search_type_radio_group.setLabel("Search Type");
        search_type_radio_group.setItems("VOID", "Annotations");
        search_type_radio_group.setValue("VOID");

        VerticalLayout radio_buttons_vl = new VerticalLayout();

        //radio_buttons_vl.add(type_radio_group, logical_radio_group, search_type_radio_group);
        radio_buttons_vl.add(search_type_radio_group);

        suggestion_grid.setItems(suggestions);
        suggestion_grid.setWidth("40%");

        selected_grid.setItems(selected_objects);
        selected_grid.setWidth("40%");
        // Setting up the search field
        TextField search_field = new TextField();
        search_field.addValueChangeListener(event -> {
            suggestions.clear();

            updateSuggestions(event.getValue());

            suggestion_grid.getDataProvider().refreshAll();
        });

        suggestion_grid.addColumn(new ComponentRenderer<>(frontend_data -> {
            HorizontalLayout cell = new HorizontalLayout();
            Button add_button = new Button();
            add_button.setIcon(VaadinIcon.PLUS_CIRCLE.create());
            add_button.addClickListener(event -> {
                selected_objects.add(frontend_data);
                selected_grid.getDataProvider().refreshAll();
            });
            cell.add(add_button);
            cell.add(frontend_data.generate_html_repr());
            return cell;
        })).setHeader("Suggestions");

        selected_grid.addColumn(new ComponentRenderer<>(frontend_data -> {
            HorizontalLayout cell = new HorizontalLayout();
            Button remove_button = new Button();
            remove_button.setIcon(VaadinIcon.TRASH.create());
            remove_button.addClickListener(event -> {
                selected_objects.remove(frontend_data);
                selected_grid.getDataProvider().refreshAll();
            });
            cell.add(remove_button);
            cell.add(frontend_data.generate_html_repr());
            return cell;
        })).setHeader("Selected Search Terms");

//        result_grid.addColumn(new ComponentRenderer<>(search_result -> {
//            HorizontalLayout cell = new HorizontalLayout();
//            Html cellText = new Html(
//                    "<div style=\"background-color:" + search_result.getColorCode() + "\">" +
//                            "<u>" + search_result.getTitle() + "</u>" +
//                            "<br>" +
//                            "<a href=\""+search_result.getDatabusFileUri() + "\">"+ search_result.getDatabusFileUri() + "</a>" +
//                            "<br>" +
//                            search_result.getComment() + "</div>");
//            cell.add(cellText);
//            return cell;
//        })).setHeader("Results");

        result_grid.addColumn(SearchResult::getTitle).setSortable(true).setHeader("Title");
        result_grid.addColumn(TemplateRenderer.<SearchResult>of("<a href=\"[[item.idUri]]\" target='_top'>[[item.idUri]]</a>").withProperty("idUri", SearchResult::getDatabusFileUri)).setHeader("Download");
        result_grid.addColumn(SearchResult::getComment).setHeader("Comment");
        result_grid.addColumn(searchResult -> searchResult.getType().toString()).setSortable(true).setHeader("Databus ID Type");

        search_field.setValueChangeMode(ValueChangeMode.LAZY);
        search_field.setPlaceholder("Search the Databus for Files containing or annotated with Classes, Properties etc.");
        search_field.setMinWidth("60%");
        search_field.setClearButtonVisible(true);



        result_grid.setItems(result_list);

        Button search_button = new Button("Search");
        search_button.setIcon(new Icon(VaadinIcon.SEARCH));
        search_button.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        search_button.addClickListener(buttonClickEvent -> {
            List<String> iris = new ArrayList<>();
            for (LookupFrontendData lfd : selected_objects) {
                iris.add(lfd.getResource());
            }
            if (!iris.isEmpty()) {
                result_list.clear();
                String query;
                String endpoint;
                switch (search_type_radio_group.getValue()) {
                    case "Annotations":
                        query = buildAnnotationQuery(iris, DatabusUtilService.getFinalRedirectionURI(selectDatabus.getValue() + "/sparql"));
                        endpoint = this.databus_mods_endpoint;
                        break;
                    case "VOID":
                    default:
                        query = buildVoidQuery(iris);
                        endpoint = DatabusUtilService.getFinalRedirectionURI(selectDatabus.getValue() + "/sparql");
                        break;
                }
                log.debug(query);
                List<SearchResult> search_results = sendSPARQL(query, endpoint);

                result_list.addAll(search_results);
                result_grid.getDataProvider().refreshAll();
            }
        });

        Button clear_selected_button = new Button("Clear Selection");
        clear_selected_button.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        clear_selected_button.addClickListener(buttonClickEvent -> {
            selected_objects.clear();
            selected_grid.getDataProvider().refreshAll();
        });

        selectDatabus.setLabel("Choose the Databus:");
        selectDatabus.setItems("https://databus.dbpedia.org", "https://energy.databus.dbpedia.org",
                "https://dev.databus.dbpedia.org", "https://d8lr.tools.dbpedia.org");
        selectDatabus.setValue("https://databus.dbpedia.org");
        selectDatabus.setWidth("100%");

        HorizontalLayout buttons = new HorizontalLayout(search_button, clear_selected_button);

        HorizontalLayout search_select_hl = new HorizontalLayout(selected_grid, suggestion_grid);
        search_select_hl.setWidth("100%");

        HorizontalLayout searchGroup = new HorizontalLayout();
        searchGroup.setWidth("100%");
        searchGroup.add(buttons, radio_buttons_vl, selectDatabus);
        searchGroup.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        VerticalLayout vl = new VerticalLayout(search_field, search_select_hl, searchGroup, result_grid);
        add(vl);
    }

    private List<SearchResult> sendSPARQL(String query, String endpoint) {
        Query q = QueryFactory.create(query);
        //log.debug("Send query to :" + endpoint + "\n" + q.toString());
        List<SearchResult> result_list = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, q)) {
            ResultSet rs = qexec.execSelect();

            while (rs.hasNext()) {
                QuerySolution qs = rs.next();

                SearchResult row = SearchResult.createFromQuerySolution(qs);

                result_list.add(row);
            }
        }
        log.debug("Got result sized:\n" + result_list.size());
        return result_list;
    }

    private void updateSuggestions(String query) {
        suggestions.clear();
        List<LookupObject> search_result;
        try {
            search_result = LookupRequester.getResult(query);
        } catch (Exception e) {
            log.error("Exception:" + e);
            search_result = new ArrayList<>();
        }


        for (LookupObject lo : search_result) {
            try {
                String label = MossUtilityFunctions.getValFromArray(lo.getLabel());
                String definition = MossUtilityFunctions.getValFromArray(lo.getDefinition());
                String comment = MossUtilityFunctions.getValFromArray(lo.getComment());
                suggestions.add(new LookupFrontendData(lo.getResource()[0], label, definition, comment));
            } catch (Exception e) {
                log.error("Exception:" + e);
            }
        }
    }

    private String buildVoidQuery(List<String> iris) {

        StringBuilder builder = new StringBuilder();

        for (int i= 0; i < iris.size(); i++) {
            builder.append(" ?voidStats ?partition").append(i).append(" [\n").append("   ?p").append(i).append(" <").append(iris.get(i)).append("> ;\n").append("    void:triples ?triples").append(i).append(" \n").append(" ] .");
        }

        //log.info(queryString);

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
                "SELECT ?type ?title ?comment ?databusPage ?voidStats ?id {\n"+
                " SERVICE <https://mods.tools.dbpedia.org/sparql> {\n"+
                builder +
                "  \n"+
                " ?s <http://www.w3.org/ns/prov#used> ?id . # energy file\n"+
                " ?s <http://www.w3.org/ns/prov#generated> ?voidStats . # automatic content description\n"+
                "  \n"+
                " }\n"+
                "     ?dataset a ?type .\n" +
                "     ?dataset dataid:group ?group .\n"+
                "     ?dataset dcat:distribution ?distribution .\n"+
                "     ?dataset dataid:version ?databusPage .\n"+
                "     ?dataset dct:title ?title .\n"+
                "     ?dataset rdfs:comment ?comment .\n"+
                "     ?distribution dataid:file ?id .\n"+
                "\n"+
                "}";
    }

    private String buildAnnotationQuery(List<String> iris, String databusEndpoint) {
        StringBuilder builder = new StringBuilder();

        for (String iri : iris) {
            builder.append("  ?file <http://purl.org/dc/elements/1.1/subject> <").append(iri).append("> .\n");
        }

        // adds https://databus.dbpedia.org/system/voc/Collection to possible values for backward compatibility with Databus1.0

        // log.info(query);
        return "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
                "PREFIX dct:    <http://purl.org/dc/terms/>\n" +
                "PREFIX dcat:   <http://www.w3.org/ns/dcat#>\n" +
                "PREFIX db:     <https://databus.dbpedia.org/>\n" +
                "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX mods:   <http://mods.tools.dbpedia.org/>\n" +
                "\n" +
                "\n" +
                "SELECT ?type ?title ?comment ?id ?databusPage {\n" +
                "  GRAPH ?g {\n" +
                "    ?s a <http://mods.tools.dbpedia.org/ns/demo#AnnotationMod> .\n" +
                "    ?s <http://www.w3.org/ns/prov#used> ?id .\n" +
                builder +
                "  }  \n" +
                "  SERVICE <" + databusEndpoint + "> {\n" +
                "    {\n" +
                "    \t?dataset a ?type .\n" +
                "    \t#OPTIONAL { ?dataset dataid:group ?group . }\n" +
                "    \tOPTIONAL { ?dataset dataid:version ?databusPage . }\n" +
                "        ?dataset dcat:distribution ?distribution . \n" +
                "    \t?distribution dataid:file ?id .\n" +
                "    \t?dataset dct:title ?title .\n" +
                "    \t?dataset dct:abstract|rdfs:comment ?comment .\n" +
                "    } UNION {\n" +
                "\t\tVALUES ?type { dataid:Group dataid:Artifact dataid:Version <https://databus.dbpedia.org/system/voc/Collection> dataid:Collection }\n" +
                "      \t\n" +
                "      \t?id a ?type .\n" +
                "      \t?id dct:title ?title .\n" +
                "      \t?id dct:abstract ?comment .\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

}