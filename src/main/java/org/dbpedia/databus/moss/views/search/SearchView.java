package org.dbpedia.databus.moss.views.search;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.dbpedia.databus.moss.views.main.MainView;

@Route(value = "search", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Search")
@CssImport("./views/helloworld/hello-world-view.css")
public class SearchView extends Div {

    public SearchView() {
        addClassName("hello-world-view");
        add(new H1("Databus Metadata Overlay Search System"));
    }

}