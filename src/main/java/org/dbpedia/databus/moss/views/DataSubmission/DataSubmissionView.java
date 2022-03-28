package org.dbpedia.databus.moss.views.DataSubmission;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.dbpedia.databus.moss.services.DatabusUtilService;
import org.dbpedia.databus.moss.services.MetadataService;
import org.dbpedia.databus.moss.views.main.MainView;
import org.dbpedia.databus.utils.MossUtilityFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Route(value = "submit-data", layout = MainView.class)
@PageTitle("Metadata Submission")
@CssImport("./views/about/about-view.css")
public class DataSubmissionView extends Div implements BeforeEnterObserver {

    private final Logger log = LoggerFactory.getLogger(DataSubmissionView.class);
    TextField databusIdTF;
    MetadataService ms;
    TextArea data_area = new TextArea();
    RadioButtonGroup<String> rdf_type_selection = new RadioButtonGroup<>();
    Button refreshBTN = new Button();
    DatabusUtilService dbFileUtil;

    public DataSubmissionView (@Autowired MetadataService ms, @Autowired DatabusUtilService dbFileUtil) {
        this.ms = ms;
        this.dbFileUtil = dbFileUtil;
        addClassName("submit-data-view");

        refreshBTN.setIcon(VaadinIcon.REFRESH.create());
        refreshBTN.addClickListener(event -> refresh_content());

        // rdf type selection

        rdf_type_selection.setLabel("Data Format");
        rdf_type_selection.setItems("JSON-LD", "JSON", "Turtle", "RDFXML", "Ntriples");
        rdf_type_selection.setValue("JSON-LD");
        rdf_type_selection.addValueChangeListener(event -> update_data_area());

        //Headline
        add(new H1("Submit Metadata to a Databus File"));
        // define data area
        data_area.setWidth("60%");
        data_area.setHeight("700px");
        data_area.addValueChangeListener(event -> update_data_area());
        data_area.setValueChangeMode(ValueChangeMode.LAZY);
        data_area.setPlaceholder("Paste in your data and select a RDF serialisation");

        // databus file id selection
        databusIdTF = new TextField();
        databusIdTF.setWidth("50%");
        databusIdTF.addValueChangeListener(event -> {
            String identifier = event.getValue();
            int i = dbFileUtil.checkIfValidDatabusId(identifier, MossUtilityFunctions.extractBaseFromURL(identifier));
            if (i == 1) {
                databusIdTF.setInvalid(false);
            } else if (i == 0) {
                databusIdTF.setInvalid(true);
            }
        });
        databusIdTF.setValueChangeMode(ValueChangeMode.LAZY);
        databusIdTF.setValue("https://databus.dbpedia.org/jj-author/mastr/bnetza-mastr/01.04.01/bnetza-mastr_rli_type=wind.nt.gz");

        // submit data
        Button submitBTN = new Button("submit");

        submitBTN.addClickListener(
                (ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> handle_submission());


        HorizontalLayout search_layout = new HorizontalLayout(databusIdTF, refreshBTN);
        VerticalLayout submit_selection = new VerticalLayout(rdf_type_selection, submitBTN);
        search_layout.setWidth("100%");
        VerticalLayout vl = new VerticalLayout(search_layout, data_area, submit_selection);
        add(vl);
    }

    private Lang get_rdf_lang_from_string(String rdf_lang_string) {

        switch (rdf_lang_string) {
            case "JSON":
            case "JSON-LD":
                return RDFLanguages.JSONLD;
            case "Ntriples":
                return RDFLanguages.NTRIPLES;
            case "Turtle":
                return RDFLanguages.TURTLE;
            case "RDFXML":
                return RDFLanguages.RDFXML;
            default:
                return RDFLanguages.NTRIPLES;

        }
    }

    private void update_data_area() {
        Model model = ModelFactory.createDefaultModel();
        Lang rdf_lang = get_rdf_lang_from_string(rdf_type_selection.getValue());
        try {
            RDFParser.create().fromString(data_area.getValue()).lang(rdf_lang).parse(model);
            data_area.setInvalid(false);
        } catch (Exception e) {
            data_area.setInvalid(true);
        }
    }

    private void handle_submission() {
        String rdf_string;
        Lang rdf_lang;

        String rdf_type_id = rdf_type_selection.getValue();

        // catch json input and convert it
        if (rdf_type_id.equals("JSON")) {
            rdf_string = MossUtilityFunctions.get_ntriples_from_json(data_area.getValue());
            rdf_lang = RDFLanguages.NTRIPLES;
        } else {
            rdf_string = data_area.getValue();
            rdf_lang = get_rdf_lang_from_string(rdf_type_selection.getValue());
        }

        Model model = ModelFactory.createDefaultModel();
        try {
            RDFParser.create().fromString(rdf_string).lang(rdf_lang).parse(model);
        } catch (Exception e) {
            log.warn("Exception during parsing: ", e);
            Notification.show("invalid input or wrong serialisation type", 2000, Notification.Position.MIDDLE);
            return;
        }
        try {
            ms.submit_model(databusIdTF.getValue(), model);
        } catch (IOException ioex) {
            log.warn("Exception during submission: ", ioex);
            Notification.show("Error during submission", 2000, Notification.Position.MIDDLE);
        }
        Notification.show("Successfully Submitted Data", 2000, Notification.Position.MIDDLE);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Location location = beforeEnterEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();
        List<String> defaultDatabusFiles = Collections.singletonList("https://databus.dbpedia.org/jj-author/mastr/bnetza-mastr/01.04.01/bnetza-mastr_rli_type=wind.nt.gz");
        String databusFile = parametersMap.getOrDefault("dfid", defaultDatabusFiles).get(0);
        databusIdTF.setPlaceholder(databusFile);
        databusIdTF.setValue(databusFile);

        String turtle_string = ms.get_api_data(databusFile);
        data_area.setValue(turtle_string);
        rdf_type_selection.setValue("Turtle");
    }

    private void refresh_content() {
        String turtle_string = ms.get_api_data(databusIdTF.getValue());
        data_area.setValue(turtle_string);
        rdf_type_selection.setValue("Turtle");
    }
}
