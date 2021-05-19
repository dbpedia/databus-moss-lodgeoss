package org.dbpedia.databus.moss.views.annotation;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import org.dbpedia.databus.moss.services.MetadataService;
import org.dbpedia.databus.moss.views.main.MainView;
import org.dbpedia.databus.moss.views.search.LookupFrontendData;
import org.dbpedia.databus.moss.views.search.LookupObject;
import org.dbpedia.databus.moss.views.search.LookupRequester;
import org.dbpedia.databus.utils.DatabusFileUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Route(value = "annotate", layout = MainView.class)
@PageTitle("Annotation")
@CssImport("./views/about/about-view.css")
public class AnnotationView extends Div implements BeforeEnterObserver {

    TextField databusIdTF;

    ArrayList<AnnotationURL> annotationUrls;
    ListDataProvider<AnnotationURL> annotationProvider;
    Grid<AnnotationURL> annotationGrid;

    List<LookupFrontendData> suggestions = new ArrayList<>();
    Grid<LookupFrontendData> suggestion_grid = new Grid<>();

    MetadataService ms;

    public AnnotationView(@Autowired MetadataService ms) {
        this.ms = ms;
        addClassName("about-view");

        //Headline
        add(new H1("Annotate Databus File"));

        //Databus file id text field
        databusIdTF = new TextField();
        databusIdTF.setWidth("50%");

        annotationUrls = new ArrayList<AnnotationURL>();
        annotationProvider = new ListDataProvider<AnnotationURL>(annotationUrls);
        annotationGrid = new Grid();

        annotationGrid.setWidth("50%");
        annotationGrid.setDataProvider(annotationProvider);

        annotationGrid.addColumn(new ComponentRenderer<>(e -> {
            HorizontalLayout cell = new HorizontalLayout();
            Button removeBTN = new Button();
            removeBTN.setIcon(VaadinIcon.TRASH.create());
            removeBTN.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
                annotationUrls.remove(e);
                annotationProvider.refreshAll();
            });
            cell.add(removeBTN);
            cell.add(new Anchor(e.getUri(), e.getUri()));
            cell.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
            return cell;
        })).setHeader("Annotations");

        suggestion_grid.setItems(suggestions);
        suggestion_grid.setWidth("50%");

        TextField search_field = new TextField();
        search_field.setWidth("50%");
        search_field.addValueChangeListener(event -> {
            System.out.println(event.getValue());
            suggestions.clear();
            updateSuggestions(event.getValue());
            suggestion_grid.getDataProvider().refreshAll();
        });

        search_field.setValueChangeMode(ValueChangeMode.LAZY);
        search_field.setPlaceholder("Search for annotation terms (Classes and Properties)");
        search_field.setMinWidth("50%");
        search_field.setClearButtonVisible(true);

        suggestion_grid.addColumn(new ComponentRenderer<>(frontend_data -> {
            HorizontalLayout cell = new HorizontalLayout();
            Html cellText = new Html(
                    "<span>" +
                            "<u>" + frontend_data.getLabel() + "</u>" +
                            "<br>" +
                            "<a href=\"" + frontend_data.getResource() + "\">" + frontend_data.getResource() + "</a>" +
                            "<br>" +
                            frontend_data.getDefinition() + "</span>");
            Button add_button = new Button();
            add_button.setIcon(VaadinIcon.PENCIL.create());
            add_button.addClickListener(event -> {
                annotationUrls.add(new AnnotationURL(frontend_data.getResource()));
                annotationGrid.getDataProvider().refreshAll();
            });
            cell.add(add_button);
            cell.add(cellText);
            return cell;
        })).setHeader("Suggestions");

        Label inputLabel = new com.vaadin.flow.component.html.Label("annotation url");

        TextField inputTF = new TextField();
        inputTF.setPlaceholder("http://www.w3.org/2002/07/owl#Thing");
        inputTF.setValue("http://www.w3.org/2002/07/owl#Thing");
        inputTF.setWidth("100%");
        // TODO [ENTER] key

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

        Button submitBTN = new Button("submit");

        submitBTN.addClickListener(
                (ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
                    if (DatabusFileUtil.validate(databusIdTF.getValue())
                    && ! annotationUrls.isEmpty()) {
                        ms.createAnnotation(databusIdTF.getValue(),annotationUrls);
                        Notification.show("submitted",2000, Notification.Position.MIDDLE);
                    } else {
                        Notification.show("invalid input",2000, Notification.Position.MIDDLE);
                    }
                });

        Button refreshBTN = new Button();
        refreshBTN.setIcon(VaadinIcon.REFRESH.create());
        refreshBTN.addClickListener(
                (ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
                    List<AnnotationURL> list = ms.getAnnotations(databusIdTF.getValue());
                    annotationUrls.clear();
                    annotationUrls.addAll(list);
                    annotationProvider.refreshAll();
                });

        HorizontalLayout buttonGroup = new HorizontalLayout(submitBTN, refreshBTN);

        HorizontalLayout grids = new HorizontalLayout(annotationGrid, suggestion_grid);
        grids.setWidth("100%");

        HorizontalLayout inputs = new HorizontalLayout(databusIdTF, search_field);
        inputs.setWidth("100%");

        VerticalLayout vl = new VerticalLayout(
                new com.vaadin.flow.component.html.Label("Paste Databus file identifier of the file you like to annotate"),
                inputs,
                grids,
                buttonGroup
        );

        add(vl);
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

        List<AnnotationURL> list = ms.getAnnotations(databusIdTF.getValue());
        annotationUrls.clear();
        annotationUrls.addAll(list);
        annotationProvider.refreshAll();
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
}