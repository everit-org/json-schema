package org.everit.json.schema.facade;

import org.everit.json.schema.facade.orgjson.JsonOrg;

import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;

public final class Facade {
    private static final ServiceLoader<FacadeImpl> LOADER = ServiceLoader.load(FacadeImpl.class);
    private static FacadeImpl instance;

    private Facade() { // Utility Class
        throw new Error();
    }

    // TODO: Document
    // Allows overriding when ServiceLoader is not feasible.
    public static void setInstance(FacadeImpl instance) {
        Facade.instance = instance;
    }

    public static FacadeImpl getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    private static void load() {
        LOADER.reload();
        Iterator<FacadeImpl> it = LOADER.iterator();
        if (!it.hasNext()) {
            // TODO: Remove
            instance = new JsonOrg();
            return;
            // throw new IllegalStateException("Missing Json Implementation. Are you sure it is on the classpath?");
        }
        instance = it.next();
    }

    public interface FacadeImpl {
        Object NULL();

        JsonObject object();

        JsonArray array(Collection<Object> elements);

        JsonWriter writer(Writer writer);

        JsonComparator comparator();
    }
}
