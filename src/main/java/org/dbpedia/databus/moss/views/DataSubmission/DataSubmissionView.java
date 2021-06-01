package org.dbpedia.databus.moss.views.DataSubmission;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.jena.riot.RDFLanguages;
import org.dbpedia.databus.moss.services.MetadataService;
import org.dbpedia.databus.moss.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "submit-data", layout = MainView.class)
@PageTitle("Daat Submission")
@CssImport("./views/about/about-view.css")
public class DataSubmissionView extends Div {

    TextField databusIdTF;
    MetadataService ms;
    TextArea data_area = new TextArea();

    public DataSubmissionView (@Autowired MetadataService ms) {
        this.ms = ms;
        addClassName("submit-data-view");

        //Headline
        add(new H1("Submit Metadata to a Databus File"));

        databusIdTF = new TextField();
        databusIdTF.setWidth("50%");

        RadioButtonGroup<String> rdf_type_selection = new RadioButtonGroup<>();
        rdf_type_selection.setLabel("Data Format");
        rdf_type_selection.setItems(RDFLanguages.JSONLD.toString(), RDFLanguages.NTRIPLES.toString());
        rdf_type_selection.setValue("VOID");

    }
}
