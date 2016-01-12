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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Validator for {@code allOf}, {@code oneOf}, {@code anyOf} schemas.
 */
public class CombinedSchema extends Schema {

  /**
   * Builder class for {@link CombinedSchema}.
   */
  public static class Builder extends Schema.Builder<CombinedSchema> {

    private ValidationCriterion criterion;

    private Collection<Schema> subschemas = new ArrayList<Schema>();

    public Builder criterion(final ValidationCriterion criterion) {
      this.criterion = criterion;
      return this;
    }

    public Builder subschema(final Schema subschema) {
      this.subschemas.add(subschema);
      return this;
    }

    public Builder subschemas(final Collection<Schema> subschemas) {
      this.subschemas = subschemas;
      return this;
    }

    @Override
    public CombinedSchema build() {
      return new CombinedSchema(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(final Collection<Schema> subschemas) {
    return new Builder().subschemas(subschemas);
  }

  /**
   * Validation criterion.
   */
  public interface ValidationCriterion
  {
    /**
     * Throws a {@link ValidationException} if the implemented criterion is not fulfilled by the
     * {@code subschemaCount} and the {@code matchingSubschemaCount}.
     *
     * @param subschemaCount
     *          the total number of checked subschemas
     * @param matchingSubschemaCount
     *          the number of subschemas which successfully validated the subject (did not throw
     *          {@link ValidationException})
     */
    void validate(int subschemaCount, int matchingSubschemaCount);

  }

  /**
   * Validation criterion for {@code allOf} schemas.
   */
  public static final ValidationCriterion ALL_CRITERION = new ValidationCriterion()
  {
	public void validate(int subschemaCount, int matchingCount)
	{
	    if (matchingCount < subschemaCount) 
	    {
	      throw new ValidationException(String.format("only %d subschema matches out of %d",
	          matchingCount, subschemaCount));
	    }
	}
  };

  /**
   * Validation criterion for {@code anyOf} schemas.
   */
  public static final ValidationCriterion ANY_CRITERION = new ValidationCriterion()
  {
	public void validate(int subschemaCount, int matchingCount)
	{
		if (matchingCount == 0) 
		{
		      throw new ValidationException(String.format(
		          "no subschema matched out of the total %d subschemas",
		          subschemaCount));
		}
	}
  };
  
  /**
   * Validation criterion for {@code oneOf} schemas.
   */
  public static final ValidationCriterion ONE_CRITERION  = new ValidationCriterion()
  {
	public void validate(int subschemaCount, int matchingCount)
	{
		if (matchingCount != 1)
		{
		     throw new ValidationException(String.format(
		       "%d subschemas matched instead of one",
		          matchingCount));
	    }
	}
  };

  public static Builder allOf(final Collection<Schema> schemas)
  {
    return builder(schemas).criterion(ALL_CRITERION);
  }

  public static Builder anyOf(final Collection<Schema> schemas)
  {
    return builder(schemas).criterion(ANY_CRITERION);
  }

  public static Builder oneOf(final Collection<Schema> schemas) 
  {
    return builder(schemas).criterion(ONE_CRITERION);
  }

  private final Collection<Schema> subschemas;

  private final ValidationCriterion criterion;

  /**
   * Constructor.
   *
   * @param builder
   *          the builder containing the validation criterion and the subschemas to be checked
   */
  public CombinedSchema(final Builder builder) 
  {
    super(builder);

    if ((this.criterion = builder.criterion) == null)
    {
  	  throw new NullPointerException("criterion cannot be null");
    }
    if((this.subschemas = builder.subschemas) == null)
    {
    	  throw new NullPointerException("subschemas cannot be null");
    }
  }

  public ValidationCriterion getCriterion() {
    return criterion;
  }

  public Collection<Schema> getSubschemas() {
    return subschemas;
  }

  private boolean succeeds(final Schema schema, final Object subject) 
  {
    try 
    {
      schema.validate(subject);
      return true;
      
    } catch (ValidationException e) 
    {
      return false;
    }
  }

  @Override
  public void validate(final Object subject)
  {
	int matchingCount = 0;
	Iterator<Schema> iterator = subschemas.iterator();
	while(iterator.hasNext())
	{
	   matchingCount+= this.succeeds(iterator.next(), subject)?1:0;
	}
    criterion.validate(subschemas.size(), matchingCount);
  }

}
