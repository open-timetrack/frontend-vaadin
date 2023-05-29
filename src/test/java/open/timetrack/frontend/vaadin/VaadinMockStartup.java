package open.timetrack.frontend.vaadin;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.SpringServlet;
import io.cucumber.java.*;
import kotlin.jvm.functions.Function0;
import open.timetrack.frontend.vaadin.data.service.TimeTrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
public class VaadinMockStartup {

    @Autowired
    protected ApplicationContext ctx;
    private static final Routes routes = new Routes().autoDiscoverViews("open.timetrack.frontend.vaadin.views.timetracks");

    @Autowired
    TimeTrackRepository repository;

    @Before
    public void setupVaadin() {
        final Function0<UI> uiFactory = UI::new;
        final SpringServlet servlet = new MockSpringServlet(routes, ctx, uiFactory);
        MockVaadin.setup(uiFactory, servlet);
    }


    @After
    public void tearDown() {
        repository.deleteAll();
    }

    @After
    public void tearDownVaadin() {
        MockVaadin.tearDown();
    }
}
