/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

public class StringSchemaTest {

  @Test
  public void formatFailure() {
    StringSchema subject = StringSchema.builder()
        .formatValidator(subj -> Optional.of("violation"))
        .build();
    TestSupport.expectFailure(subject, "string");
  }

  @Test
  public void formatSuccess() {
    StringSchema subject = StringSchema.builder().formatValidator(subj -> Optional.empty()).build();
    subject.validate("string");
  }

  @Test
  public void maxLength() {
    StringSchema subject = StringSchema.builder().maxLength(3).build();
    TestSupport.expectFailure(subject, "foobar");
  }

  @Test
  public void minLength() {
    StringSchema subject = StringSchema.builder().minLength(2).build();
    TestSupport.expectFailure(subject, "a");
  }

  @Test
  public void multipleViolations() {
    try {
      StringSchema.builder().minLength(3).maxLength(1).pattern("^b.*").build().validate("ab");
      Assert.fail();
    } catch (ValidationException e) {
      Assert.assertEquals(3, e.getCausingExceptions().size());
    }
  }

  @Test
  public void notRequiresString() {
    StringSchema.builder().requiresString(false).build().validate(2);
  }

  @Test
  public void patternFailure() {
    StringSchema subject = StringSchema.builder().pattern("^a*$").build();
    TestSupport.expectFailure(subject, "abc");
  }

  @Test
  public void patternSuccess() {
    StringSchema.builder().pattern("^a*$").build().validate("aaaa");
  }

  @Test
  public void success() {
    StringSchema.builder().build().validate("foo");
  }

  @Test
  public void typeFailure() {
    TestSupport.expectFailure(StringSchema.builder().build(), null);
  }
}
