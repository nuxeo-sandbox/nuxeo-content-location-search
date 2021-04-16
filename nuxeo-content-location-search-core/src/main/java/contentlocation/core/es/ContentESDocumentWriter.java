package contentlocation.core.es;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.elasticsearch.io.JsonESDocumentWriter;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;

public class ContentESDocumentWriter extends JsonESDocumentWriter {

    protected static final Log log = LogFactory.getLog(ContentESDocumentWriter.class);

    @Override
    protected void writeSchemas(JsonGenerator jg, DocumentModel doc, String[] schemas) throws IOException {
        super.writeSchemas(jg, doc, schemas);

        if (doc == null || doc.isProxy()) {
            return;
        }

        CoreSession coreSession = doc.getCoreSession();

        if ((doc.getType().equals("File"))) {
            // Write parent folder path
            Framework.doPrivileged(() -> {
                try {
                    DocumentModel parentDoc = coreSession.getDocument(doc.getParentRef());
                    Path path = parentDoc.getPath();
                    jg.writeStringField("assets_search:content_location", path.toString());
                } catch (IOException e) {
                    log.error(String.format("Could not index parent location for the file '%s'.", doc.getTitle()), e);
                }
            });
        }
    }
}
