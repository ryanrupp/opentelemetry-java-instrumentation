/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.mongo.v3_1;

import com.mongodb.event.CommandStartedEvent;
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;

class MongoSpanNameExtractor implements SpanNameExtractor<CommandStartedEvent> {
  private static final String DEFAULT_SPAN_NAME = "DB Query";

  private final MongoDbAttributesExtractor dbAttributesExtractor;
  private final MongoAttributesExtractor attributesExtractor;

  MongoSpanNameExtractor(
      MongoDbAttributesExtractor dbAttributesExtractor,
      MongoAttributesExtractor attributesExtractor) {
    this.dbAttributesExtractor = dbAttributesExtractor;
    this.attributesExtractor = attributesExtractor;
  }

  @Override
  public String extract(CommandStartedEvent event) {
    String operation = dbAttributesExtractor.operation(event);
    String dbName = dbAttributesExtractor.name(event);
    if (operation == null) {
      return dbName == null ? DEFAULT_SPAN_NAME : dbName;
    }

    String table = attributesExtractor.collectionName(event);
    StringBuilder name = new StringBuilder(operation);
    if (dbName != null || table != null) {
      name.append(' ');
    }
    // skip db name if table already has a db name prefixed to it
    if (dbName != null && (table == null || table.indexOf('.') == -1)) {
      name.append(dbName);
      if (table != null) {
        name.append('.');
      }
    }
    if (table != null) {
      name.append(table);
    }
    return name.toString();
  }
}
