/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.phoenix.schema.tuple;

import static org.apache.phoenix.query.QueryConstants.ENCODED_CQ_COUNTER_INITIAL_VALUE;

import java.util.Collections;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.hbase.index.util.GenericKeyValueBuilder;
import org.apache.phoenix.query.QueryConstants;
import org.apache.phoenix.schema.SortOrder;
import org.apache.phoenix.schema.types.PInteger;
import org.apache.phoenix.util.KeyValueUtil;

/**
 * 
 * Wrapper around {@link Result} that implements Phoenix's {@link Tuple} interface.
 *
 */
public class ResultTuple extends BaseTuple {
    private final Result result;
    public static final ResultTuple EMPTY_TUPLE = new ResultTuple(Result.create(Collections.<Cell>emptyList()));
    
    //TODO: samarth see if we can get rid of this constructor altogether.
    public ResultTuple(Result result) {
        this.result = result;
    }
    
//    public ResultTuple(Result result, boolean useQualifierAsIndex) {
//        this.result = result;
//        this.useQualifierAsIndex = useQualifierAsIndex;
//    }
    
    public Result getResult() {
        return this.result;
    }

    @Override
    public void getKey(ImmutableBytesWritable ptr) {
        ptr.set(result.getRow());
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public KeyValue getValue(byte[] family, byte[] qualifier) {
//        if (useQualifierAsIndex) {
//            int index = PInteger.INSTANCE.getCodec().decodeInt(qualifier, 0, SortOrder.ASC);
//            //TODO: samarth this seems like a hack here at this place. Think more. Maybe we should use a new tuple here?
//            index = index >= ENCODED_CQ_COUNTER_INITIAL_VALUE ? (index - ENCODED_CQ_COUNTER_INITIAL_VALUE) : index;
//            return org.apache.hadoop.hbase.KeyValueUtil.ensureKeyValue(result.rawCells()[index]);
//        }
        Cell cell = KeyValueUtil.getColumnLatest(GenericKeyValueBuilder.INSTANCE, 
          result.rawCells(), family, qualifier);
        return org.apache.hadoop.hbase.KeyValueUtil.ensureKeyValue(cell);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("keyvalues=");
      if(this.result == null || this.result.isEmpty()) {
        sb.append("NONE");
        return sb.toString();
      }
      sb.append("{");
      boolean moreThanOne = false;
      for(Cell kv : this.result.listCells()) {
        if(moreThanOne) {
          sb.append(", \n");
        } else {
          moreThanOne = true;
        }
        sb.append(kv.toString()+"/value="+Bytes.toString(kv.getValueArray(), 
          kv.getValueOffset(), kv.getValueLength()));
      }
      sb.append("}\n");
      return sb.toString();
    }

    @Override
    public int size() {
        return result.size();
    }

    @Override
    public KeyValue getValue(int index) {
        return  org.apache.hadoop.hbase.KeyValueUtil.ensureKeyValue(
          result.rawCells()[index]);
    }

    @Override
    public boolean getValue(byte[] family, byte[] qualifier,
            ImmutableBytesWritable ptr) {
        KeyValue kv = getValue(family, qualifier);
        if (kv == null)
            return false;
        ptr.set(kv.getValueArray(), kv.getValueOffset(), kv.getValueLength());
        return true;
    }
}