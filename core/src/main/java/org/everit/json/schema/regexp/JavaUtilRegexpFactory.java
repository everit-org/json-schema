package org.everit.json.schema.regexp;



public class JavaUtilRegexpFactory implements RegexpFactory {
    @Override public Regexp createHandler(String regexp) {
        return new JavaUtilRegexp(regexp);
    }
}
