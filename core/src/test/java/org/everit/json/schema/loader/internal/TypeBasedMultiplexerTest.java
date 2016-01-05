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
package org.everit.json.schema.loader.internal;

import org.everit.json.schema.SchemaException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

public class TypeBasedMultiplexerTest {

  @Test
  public void dispatchesIdChangeEvent() {
    JSONObject scopeChangingObj = objectWithId("changedId");
    TypeBasedMultiplexer subject = new TypeBasedMultiplexer(null, scopeChangingObj, "orig");
    ResolutionScopeChangeListener mockListener = Mockito.mock(ResolutionScopeChangeListener.class);
    subject.addResolutionScopeChangeListener(mockListener);
    subject.ifObject().then(o -> {
    }).requireAny();
    Mockito.verify(mockListener).resolutionScopeChanged("origchangedId");
    Mockito.verify(mockListener).resolutionScopeChanged("orig");
  }

  private void expectScopeChanges(final JSONObject subjectOfMultiplexing, final String newScope,
      final String origScope) {
    TypeBasedMultiplexer subject = new TypeBasedMultiplexer(null, subjectOfMultiplexing,
        origScope);
    ResolutionScopeChangeListener mockListener = Mockito.mock(ResolutionScopeChangeListener.class);
    subject.addResolutionScopeChangeListener(mockListener);
    subject.ifObject().then(o -> {
    }).requireAny();
    Mockito.verify(mockListener).resolutionScopeChanged(newScope);
    Mockito.verify(mockListener).resolutionScopeChanged(origScope);
  }

  @Test
  public void fragmentIdOccurence() {
    JSONObject objWithFragment = objectWithId("#foo");
    expectScopeChanges(objWithFragment, "http://x.y.z/rootschema.json#foo",
        "http://x.y.z/rootschema.json");
  }

  @Test
  @Ignore
  public void nonFragmentRelativePath() {
    expectScopeChanges(objectWithId("otherschema.json"), "http://x.y.z/otherschema.json",
        "http://x.y.z/rootschema.json");
  }

  private JSONObject objectWithId(final String idAttribute) {
    JSONObject scopeChangingObj = new JSONObject();
    scopeChangingObj.put("id", idAttribute);
    return scopeChangingObj;
  }

  @Test(expected = SchemaException.class)
  public void typeBasedMultiplexerFailure() {
    new TypeBasedMultiplexer("foo")
        .ifObject().then(o -> {
        })
        .ifIs(JSONArray.class).then(o -> {
        })
        .requireAny();
  }

  @Test
  public void typeBasedMultiplexerTest() {
    new TypeBasedMultiplexer(new JSONObject())
        .ifObject().then(jsonObj -> {
        })
        .ifIs(JSONArray.class).then(jsonArr -> {
        })
        .orElse(obj -> {
        });

    new TypeBasedMultiplexer(new JSONObject())
        .ifObject().then(jsonObj -> {
        })
        .ifIs(JSONArray.class).then(jsonArr -> {
        })
        .requireAny();
  }

}
