package startapp.com.core.views.helper;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import startapp.com.core.views.users.UserFormView;

/**
 * Утилита для навигации между представлениями
 */
@NoArgsConstructor(access = PRIVATE)
public class NavigationHelper {

    /**
     * Переход к указанному представлению
     */
    public static void navigateTo(Class<?> view) {
        String route = view.getSimpleName().toLowerCase()
            .replace("view", "")
            .replace("form", "");

        com.vaadin.flow.component.UI.getCurrent().navigate(route);
    }

    /**
     * Переход к указанному представлению с ID параметром
     */
    public static void navigateTo(Class<?> view, Long id) {
        String route;

        if (view instanceof Class<?> viewClass && UserFormView.class.isAssignableFrom(viewClass)) {
            route = "users/edit/" + id;
        } else {
            route = view.getSimpleName().toLowerCase()
                        .replace("view", "")
                        .replace("form", "edit") + "/" + id;
        }

        com.vaadin.flow.component.UI.getCurrent().navigate(route);
    }

    /**
     * Возврат назад
     */
    public static void navigateBack() {
        com.vaadin.flow.component.UI.getCurrent().getPage().getHistory().back();
    }
}
