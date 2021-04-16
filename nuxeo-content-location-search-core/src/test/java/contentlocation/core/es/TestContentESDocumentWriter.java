package contentlocation.core.es;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

@RunWith(FeaturesRunner.class)
@Features({ CoreFeature.class, PlatformFeature.class, AutomationFeature.class})
@Deploy({ "com.nuxeo.sandbox.contentlocation.core.nuxeo-content-location-core", "org.nuxeo.ecm.automation.core"})
@PartialDeploy(bundle = "studio.extensions.jaldama-SANDBOX", extensions = {
        TargetExtensions.ContentModel.class, TargetExtensions.Automation.class, TargetExtensions.Automation.ContentTemplate.class })
@RepositoryConfig(cleanup = Granularity.METHOD)
public class TestContentESDocumentWriter {

    @Inject
    protected CoreSession session;

    private DocumentModel parentDir;

    private static final String[] SCHEMAS = {"assets_search"};

    @Before
    public void init() {
        parentDir = session.createDocumentModel("/", "My Folder", "Folder");
        parentDir = session.createDocument(parentDir);
    }

    @Test
    public void shouldEnhanceFileDocument() throws IOException {
        DocumentModel file = session.createDocumentModel(parentDir.getPathAsString(), "My File", "File");
        file = session.createDocument(file);

        testWrittenSchemas( "json/shouldEnhanceFileDocument.json", file);
    }

    private void testWrittenSchemas(String expectedJSONFile, DocumentModel doc) throws IOException {
        try (
                BytesStreamOutput out = new BytesStreamOutput();
                JsonGenerator jg = new JsonFactory().createGenerator(out);
        ) {
            ContentESDocumentWriter writer = new ContentESDocumentWriter();
            jg.writeStartObject();
            writer.writeSchemas(jg, doc, SCHEMAS);
            jg.writeEndObject();
            jg.flush();
            assertJSON(expectedJSONFile, out);
        }
    }

    protected void assertJSON(String expectedJSONFile, BytesStreamOutput actual) throws IOException {
        File file = FileUtils.getResourceFileFromContext(expectedJSONFile);
        String expected = org.apache.commons.io.FileUtils.readFileToString(file, UTF_8);
        assertEquals(expected, actual.bytes().utf8ToString());
    }
}
