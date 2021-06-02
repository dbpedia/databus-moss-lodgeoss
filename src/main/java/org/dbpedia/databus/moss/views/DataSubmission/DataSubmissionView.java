package org.dbpedia.databus.moss.views.DataSubmission;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.dbpedia.databus.moss.services.MetadataService;
import org.dbpedia.databus.moss.views.main.MainView;
import org.dbpedia.databus.utils.MossUtilityFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Route(value = "submit-data", layout = MainView.class)
@PageTitle("Metadata Submission")
@CssImport("./views/about/about-view.css")
public class DataSubmissionView extends Div {

    private final Logger log = LoggerFactory.getLogger(DataSubmissionView.class);
    TextField databusIdTF;
    MetadataService ms;
    TextArea data_area = new TextArea();
    RadioButtonGroup<String> rdf_type_selection = new RadioButtonGroup<>();

    public DataSubmissionView (@Autowired MetadataService ms) {
        this.ms = ms;
        addClassName("submit-data-view");

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

            int i = MossUtilityFunctions.checkIfValidDatabusId(event.getValue());
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

        VerticalLayout submit_selection = new VerticalLayout(rdf_type_selection, submitBTN);

        VerticalLayout vl = new VerticalLayout(databusIdTF, data_area, submit_selection);
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
}
