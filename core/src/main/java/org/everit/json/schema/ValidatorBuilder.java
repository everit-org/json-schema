package org.everit.json.schema;

public class ValidatorBuilder {

    public static ValidatorBuilder builder() {
        return new ValidatorBuilder();
    }

    private boolean failEarly = false;

    private ReadWriteContext readWriteContext;

    public ValidatorBuilder failEarly() {
        this.failEarly = true;
        return this;
    }

    public ValidatorBuilder readWriteContext(ReadWriteContext readWriteContext) {
        this.readWriteContext = readWriteContext;
        return this;
    }

    public Validator build() {
        return new DefaultValidator(failEarly, readWriteContext);
    }
}
