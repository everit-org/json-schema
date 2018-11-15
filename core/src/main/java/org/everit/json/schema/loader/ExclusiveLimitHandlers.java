package org.everit.json.schema.loader;

public class ExclusiveLimitHandlers {
    public static ExclusiveLimitHandler ofSpecVersion(SpecificationVersion specVersion) {
        switch (specVersion) {
            case DRAFT_4: return new V4ExclusiveLimitHandler();
            case DRAFT_6:
            case DRAFT_7: return new V6ExclusiveLimitHandler();
            default: throw new RuntimeException("unknown spec version: " + specVersion);
        }
    }
}
