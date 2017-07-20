/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ssamples.avro;

import java.io.File;
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;

public class ReaderMain {
  public static void main(String[] args) throws IOException {
    Schema schema = new Parser().parse(new File("./src/main/java/com/ssamples/avro/protodef/user-reader.avsc"));

    File file = new File("users.avro");
    // Deserialize users from disk
    DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
    DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(file, datumReader);
    GenericRecord user = null;
    while (dataFileReader.hasNext()) {
      // Reuse user object by passing it to next(). This saves us from
      // allocating and garbage collecting many objects for files with
      // many items.
      user = dataFileReader.next(user);
      System.out.println(user);
    }
    dataFileReader.close();
  }
}
