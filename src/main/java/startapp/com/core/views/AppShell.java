package startapp.com.core.views;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

@Push
@Theme(value = "lumo")
@PWA(name = "Startapp Chat Core", shortName = "Chat Core", iconPath = "icons/icon.png")
public class AppShell implements AppShellConfigurator {

}
