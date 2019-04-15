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

package com.ssamples.hbase.stochasticbalancer;

import org.apache.hadoop.hbase.CompatibilitySingletonFactory;
import org.apache.yetus.audience.InterfaceAudience;
import org.apache.hadoop.hbase.master.balancer.MetricsBalancerSource;

/**
 * Faced for exposing metrics about the balancer.
 */
@InterfaceAudience.Private
public class MetricsBalancer {

  private MetricsBalancerSource source = null;

  public MetricsBalancer() {
    initSource();
  }
  
  /**
   * A function to instantiate the metrics source. This function can be overridden in its 
   * subclasses to provide extended sources
   */
  protected void initSource() {
    source = CompatibilitySingletonFactory.getInstance(MetricsBalancerSource.class);
  }

  public void balanceCluster(long time) {
    source.updateBalanceCluster(time);
  }

  public void incrMiscInvocations() {
    source.incrMiscInvocations();
  }

  public void balancerStatus(boolean status) {
    //source.updateBalancerStatus(status);
  }
}
