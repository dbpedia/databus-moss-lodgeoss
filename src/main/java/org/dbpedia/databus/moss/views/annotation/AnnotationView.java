package org.dbpedia.databus.moss.views.annotation;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.*;
import org.dbpedia.databus.moss.views.main.MainView;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;

import java.util.*;

@Route(value = "annotate", layout = MainView.class)
@PageTitle("Annotation")
@CssImport("./views/about/about-view.css")
public class AnnotationView extends Div implements BeforeEnterObserver {

    TextField databusIdTF;

    public AnnotationView() {
        addClassName("about-view");

        //Headline
        add(new H1("Databus Mods Demo Mod - Example Metadata Overlay"));

        //Databus file id text field
        add(new com.vaadin.flow.component.html.Label("databus file identifier"));
        databusIdTF = new TextField();
        databusIdTF.setWidth("50%");
        add(new HtmlComponent("br"));
        add(databusIdTF);

        ArrayList<AnnotationURL> annotationUrls = new ArrayList<AnnotationURL>();
        ListDataProvider<AnnotationURL> annotationProvider = new ListDataProvider<AnnotationURL>(annotationUrls);
        Grid<AnnotationURL> annotationGrid = new Grid(AnnotationURL.class);

        annotationGrid.setWidth("50%");
        annotationGrid.setDataProvider(annotationProvider);
        add(annotationGrid);

        Label inputLabel = new com.vaadin.flow.component.html.Label("annotation url");
        add(inputLabel);

        TextField inputTF = new TextField();
        inputTF.setPlaceholder("http://www.w3.org/2002/07/owl#Thing");
        inputTF.setValue("http://www.w3.org/2002/07/owl#Thing");
        inputTF.setWidth("100%");

        Button inputBTN = new Button("+");
        inputBTN.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                annotationUrls.add(new AnnotationURL(inputTF.getValue()));
                annotationProvider.refreshAll();
                inputTF.setValue("");
            }
        });
        HorizontalLayout inputGroup = new HorizontalLayout(inputTF,inputBTN);
        inputGroup.setWidth("50%");
        add(inputGroup);

        Button submitBTN = new Button("submit");

        submitBTN.addClickListener(
                (ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
                    new Notification("test",3000).open();
                });
        add(submitBTN);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Location location = beforeEnterEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();
        List<String> defaultDatabusFiles = Collections.singletonList("https://databus.dbpedia.org/jj-author/mastr/bnetza-mastr/01.04.01/bnetza-mastr_rli_type=wind.nt.gz");
        String defaultDatabusFile = parametersMap.getOrDefault("dfid",defaultDatabusFiles).get(0);
        databusIdTF.setPlaceholder(defaultDatabusFile);
        databusIdTF.setValue(defaultDatabusFile);
    }
}