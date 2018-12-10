package org.everit.json.schema.event;

import org.everit.json.schema.ValidationException;

public interface MismatchEvent {

    ValidationException getFailure();

}
