package org.everit.json.schema.listener;

import org.everit.json.schema.ValidationException;

public interface MismatchEvent {

    ValidationException getFailure();

}
