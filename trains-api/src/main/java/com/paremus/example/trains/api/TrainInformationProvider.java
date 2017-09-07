/*
 * Paremus licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.  You may obtain a copy of the 
 * License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIESOR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.paremus.example.trains.api;

import org.osgi.util.pushstream.PushStream;

import com.paremus.example.trains.api.dto.TrainInfo;

public interface TrainInformationProvider {

    /**
     * Returns a stream of train location data, indicating the
     * live positions of trains.
     * 
     * @return An asynchronous stream of location data
     */
    PushStream<TrainInfo> getTrainReportingData();
    
}
