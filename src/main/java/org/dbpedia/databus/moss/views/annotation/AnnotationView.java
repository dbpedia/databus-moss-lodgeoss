package org.dbpedia.databus.moss.views.annotation;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.dbpedia.databus.moss.views.main.MainView;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;

@Route(value = "annotation", layout = MainView.class)
@PageTitle("Annotation")
@CssImport("./views/about/about-view.css")
public class AnnotationView extends Div {

    public AnnotationView() {
        addClassName("about-view");

        //Headline
        add(new H1("Databus Mods Demo Mod - Example Metadata Overlay"));

        //Databus file id text field
        add(new com.vaadin.flow.component.html.Label("databus file identifier"));
        TextField databusIdTF = new TextField();
        databusIdTF.setWidth("50%");
        databusIdTF.setPlaceholder("https://databus.dbpedia.org/jj-author/mastr/bnetza-mastr/01.04.01/bnetza-mastr_rli_type=wind.nt.gz");
        databusIdTF.setValue("https://databus.dbpedia.org/jj-author/mastr/bnetza-mastr/01.04.01/bnetza-mastr_rli_type=wind.nt.gz");
        add(databusIdTF);

        ArrayList<String> annotationUrls = new ArrayList<String>();
        ListDataProvider<String> annotationProvider = new ListDataProvider<String>(annotationUrls);
        Grid<String> annotationGrid = new Grid(String.class);

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
                annotationUrls.add("");
                annotationProvider.refreshAll();
                inputTF.setValue("");
            }
        });
//        inputBTN.addClickListener(new ComponentEventListener<ClickEvent<Button>> {
//            override def onComponentEvent(event: ClickEvent[Button]): Unit = {
//                    annotationUrls.add(new AnnotationURL(new URL(inputTF.getValue)))
//                    annotationProvider.refreshAll()
//                    inputTF.setValue("")
//            }
//        };)
        HorizontalLayout inputGroup = new HorizontalLayout(inputTF,inputBTN);
        inputGroup.setWidth("50%");
        add(inputGroup);

        Button submitBTN = new Button("submit");
        // TODO
        add(submitBTN);
    }

}