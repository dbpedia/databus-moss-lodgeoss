package org.dbpedia.databus.moss.views.metadata;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.*;
import org.dbpedia.databus.moss.views.main.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Route(value = "metadata/:account?/:group?/:artifact?/:version?/:file?/:metadata?", layout = MainView.class)
public class MetadataView extends Div implements BeforeEnterObserver {

    private Logger log = LoggerFactory.getLogger(MetadataView.class);

    public MetadataView() {
        add(new H1("Databus Mods Demo Mod - Example Metadata Overlay"));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        String account = beforeEnterEvent.getRouteParameters().get("account").orElse(null);
        String group = beforeEnterEvent.getRouteParameters().get("group").orElse(null);
        String artifact = beforeEnterEvent.getRouteParameters().get("artifact").orElse(null);
        String version = beforeEnterEvent.getRouteParameters().get("version").orElse(null);
        String file = beforeEnterEvent.getRouteParameters().get("file").orElse(null);
        String metadata = beforeEnterEvent.getRouteParameters().get("metadata").orElse(null);
        List<String> values = Arrays.asList(account,group,artifact,version,file,metadata);
        log.info(String.join("/",values));
    }
}