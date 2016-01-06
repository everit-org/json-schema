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
import org.junit.Test;
import org.mockito.Mockito;

public class TypeBasedMultiplexerTest {

  @Test
  public void differentPortNum() {
    expectScopeChanges(objectWithId("otherschema.json"), "http://x.y.z:8080/otherschema.json",
        "http://x.y.z:8080/rootschema.json");
  }

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
  public void newRoot() {
    expectScopeChanges(objectWithId("http://otherserver.com"), "http://otherserver.com",
        "http://x.y.z:8080/rootschema.json");
  }

  @Test
  public void nonFragmentRelativePath() {
    expectScopeChanges(objectWithId("otherschema.json"), "http://x.y.z/otherschema.json",
        "http://x.y.z/rootschema.json");
  }

  private JSONObject objectWithId(final String idAttribute) {
    JSONObject scopeChangingObj = new JSONObject();
    scopeChangingObj.put("id", idAttribute);
    return scopeChangingObj;
  }

  @Test
  public void relpathThenFragment() {
    JSONObject outerObj = objectWithId("otherschema.json");
    JSONObject innerObj = objectWithId("#bar");
    outerObj.put("innerObj", innerObj);
    TypeBasedMultiplexer outerMultiplexer = new TypeBasedMultiplexer(null, outerObj,
        "http://x.y.z/rootschema.json");
    ResolutionScopeChangeListener outerListener = Mockito.mock(ResolutionScopeChangeListener.class);
    ResolutionScopeChangeListener innerListener = Mockito.mock(ResolutionScopeChangeListener.class);
    outerMultiplexer.addResolutionScopeChangeListener(outerListener);
    outerMultiplexer.ifObject().then(obj -> {
      TypeBasedMultiplexer innerMultiplexer = new TypeBasedMultiplexer(null, obj.get("innerObj"),
          "http://x.y.z/otherschema.json");
      innerMultiplexer.addResolutionScopeChangeListener(innerListener);
      innerMultiplexer.ifObject().then(o -> {
      }).requireAny();
    }).requireAny();
    Mockito.verify(outerListener).resolutionScopeChanged("http://x.y.z/otherschema.json");
    Mockito.verify(innerListener).resolutionScopeChanged("http://x.y.z/otherschema.json#bar");
    Mockito.verify(innerListener).resolutionScopeChanged("http://x.y.z/otherschema.json");
    Mockito.verify(outerListener).resolutionScopeChanged("http://x.y.z/rootschema.json");
  }

  @Test
  public void relpathWithFragment() {
    expectScopeChanges(objectWithId("t/inner.json#a"), "http://x.y.z:8080/t/inner.json#a",
        "http://x.y.z:8080/rootschema.json");
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
