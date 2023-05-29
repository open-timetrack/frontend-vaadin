package open.timetrack.frontend.vaadin;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("open/timetrack/frontend/vaadin")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
class ApplicationTest {

}