package startapp.com.core.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

/**
 * Главная страница - перенаправляет на Dashboard
 */
@Route("")
@SuppressWarnings("unused")
public class HomeView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Перенаправляем на Dashboard
        event.forwardTo("dashboard");
    }
}
