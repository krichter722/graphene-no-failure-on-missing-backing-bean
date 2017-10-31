package richtercloud.graphene.no.failure.on.missing.backing.bean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Runs all functional tests which can be seen as integration tests in a strict
 * unit-integration test separation.
 *
 * All functional tests run in this class until it's clarified who deployments
 * can be reused and the database directory be removed after all tests ran
 * without causing the embedded database server to shutdown due to
 * {@code Shutting down due to severe error.} (realisation of improvement is in
 * progress at https://issues.jboss.org/browse/ARQ-197).
 *
 * Until it's clarified how to run {@code @AfterClass} methods to delete the
 * test database of the embedded data source of GlassFish which is executed in
 * different order than the default JUnit runner, all test routines go into one
 * {@code @Test} method with a try-finally-block deleting the database. If
 * entities (users, offers, etc.) which are created in the routine need to be
 * deleted before the finally-block is reached, use the REST API.
 *
 * @author richter
 */
@RunWith(Arquillian.class)
public class FunctionalTestIT {
    private final static Logger LOGGER = LoggerFactory.getLogger(FunctionalTestIT.class);
    public final static File DATABASE_DIR = new File("project1");
    private static final String WEBAPP_SRC = "src/main/webapp";

    @Deployment(testable = false)
    public static Archive<?> createDeployment0() throws TransformerException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        assert !DATABASE_DIR.exists(): String.format("database test directory %s mustn't exist (delete manually)", DATABASE_DIR.getAbsolutePath());
        WebArchive retValue = ShrinkWrap.create(WebArchive.class)
                .add(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(MyBackingBean.class
//                        ,ConstantsBean.class
                )
                .addAsWebInfResource(
                        new StringAsset("<faces-config version=\"2.0\"/>"),
                        "faces-config.xml");
        //add all webapp resources
        retValue.merge(ShrinkWrap.create(GenericArchive.class)
                .as(ExplodedImporter.class)
                .importDirectory(WEBAPP_SRC)
                .as(GenericArchive.class), "/", Filters.include(".*\\.(xhtml|css|js|png)$"));

        ByteArrayOutputStream archiveContentOutputStream = new ByteArrayOutputStream();
        retValue.writeTo(archiveContentOutputStream, Formatters.VERBOSE);
        LOGGER.info(archiveContentOutputStream.toString());
        return retValue;
    }

    @Drone
    private WebDriver browser;
    @ArquillianResource
    private URL deploymentUrl;
    @FindBy(id = "mainForm:mainButton")
    private WebElement mainButton;
    @FindBy(id = "keySetLabel")
    private WebElement keySetLabel;
    @FindBy(id = "keyNotSetLabel")
    private WebElement keyNotSetLabel;

    /**
     * The only test method (see class comment for an explanation why there're
     * no other test methods).
     *
     * @throws OfferSaveException
     * @throws UserAlreadyRegisteredException
     * @throws IOException
     */
    @Test
    public void testAll() {
        browser.get(deploymentUrl.toExternalForm()+"index.xhtml");
        Assert.assertTrue(keyNotSetLabel.isDisplayed());
        try {
            keySetLabel.isDisplayed();
            Assert.fail();
        }catch(NoSuchElementException expected) {
        }
        Graphene.guardHttp(mainButton).click();
                Assert.assertTrue(keyNotSetLabel.isDisplayed());
        Assert.assertTrue(keySetLabel.isDisplayed());
        try {
            keyNotSetLabel.isDisplayed();
            Assert.fail();
        }catch(NoSuchElementException expected) {
        }
    }
}
