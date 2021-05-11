package org.dbpedia.databus.moss.views.search;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.dbpedia.databus.moss.views.main.MainView;

import java.util.ArrayList;
import java.util.List;

@Route(value = "search", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Search")
@CssImport("./views/helloworld/hello-world-view.css")
public class SearchView extends Div {

    private Grid<SearchResult> resultGrid = new Grid<>(SearchResult.class);

    public SearchView() {

        addClassName("hello-world-view");
        add(new H1("Databus Metadata Overlay Search System"));

        // Setting up the search field
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search the Databus for Files containing or annotated with Classes, Properties etc.");
        searchField.setMinWidth("80%");
        searchField.setClearButtonVisible(true);


        //Setting up the result layout
        List<SearchResult> resultList = new ArrayList<>();



        resultGrid.setItems(resultList);

        // Setting up the search button
        Button searchButton = new Button("Search the Databus!");
        searchButton.setIcon(new Icon(VaadinIcon.SEARCH));
        searchButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        searchButton.addClickListener(buttonClickEvent -> {
            resultList.clear();
            resultList.add(new SearchResult("EXAMPLE RESULT 1", 1));
            resultList.add(new SearchResult("EXAMPLE RESULT 2", 2));
            resultList.add(new SearchResult("EXAMPLE RESULT 3", 3));
            resultList.add(new SearchResult("EXAMPLE RESULT 4", 4));
            resultList.add(new SearchResult("EXAMPLE RESULT 5", 5));
            resultList.add(new SearchResult("EXAMPLE RESULT 6", 6));
            resultGrid.getDataProvider().refreshAll();
        });

        VerticalLayout vl = new VerticalLayout(searchField, searchButton, resultGrid);


        add(vl);
    }

    private void search() {

    }


}