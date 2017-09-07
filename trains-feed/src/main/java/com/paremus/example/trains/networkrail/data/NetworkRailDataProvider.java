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
package com.paremus.example.trains.networkrail.data;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.promise.Promise;
import org.osgi.util.pushstream.PushStream;
import org.osgi.util.pushstream.PushStreamProvider;
import org.osgi.util.pushstream.SimplePushEventSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paremus.example.trains.api.TrainInformationProvider;
import com.paremus.example.trains.api.dto.Coordinate;
import com.paremus.example.trains.api.dto.Location;
import com.paremus.example.trains.api.dto.TrainInfo;
import com.paremus.example.trains.networkrail.messages.NetworkRailDataEvent;
import com.paremus.example.trains.networkrail.messages.TrainMovementMessageBody;

@Component
public class NetworkRailDataProvider implements TrainInformationProvider {

    private PushStreamProvider psp;
    private SimplePushEventSource<NetworkRailDataEvent> spes;
    private ObjectMapper mapper;
    private Map<String, Location> stanoxToLatLong;

    @Activate
    void activate() throws Exception {

        try (InputStream is = getClass().getResourceAsStream("/ukrail_locations.csv")) {
            stanoxToLatLong = new BufferedReader(new InputStreamReader(is, UTF_8))
                    .lines()
                    .skip(1)
                    .map(s -> s.split(",")).filter(s -> !"".equals(s[6]))
                    .filter(s -> !("null".equals(s[8]) || "null".equals(s[9]))).collect(Collectors.toMap(s -> s[6], s -> {
                        Coordinate c = new Coordinate();
                        c.latitude = Double.parseDouble(s[9]);
                        c.longitude = Double.parseDouble(s[8]);
                        
                        Location loc = new Location();
                        loc.coordinates = c;
                        loc.name = s[2];
                        return loc;
                    }, (x, y) -> x));
        }
                
        mapper = new ObjectMapper();
        psp = new PushStreamProvider();
        spes = psp.createSimpleEventSource(NetworkRailDataEvent.class);
        
        spes.connectPromise().then(this::startPublishing);
    }
    
    @Deactivate
    void deactivate() {
        spes.close();
    }

    private Promise<Void> startPublishing(Promise<Void> connectPromise) {
        
        new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(getClass().getResourceAsStream("/ukrail_data"), UTF_8))) {
                    String line = reader.readLine();
                    
                    while(line != null) {
                        // The clients may all have disconnected, at which point we can stop
                        // producing data until they reconnect
                        if(!spes.isConnected()) {
                            spes.connectPromise().then(this::startPublishing);
                            return;
                        }
                        NetworkRailDataEvent event = mapper.readValue(line, NetworkRailDataEvent.class);
                        
                        Thread.sleep(NANOSECONDS.toMillis(event.time));
                        
                        spes.publish(event);
                        
                        line = reader.readLine();
                    }
                    
                    spes.endOfStream();
                } catch (Exception e) {
                    spes.error(e);
                }
            }).start();
        
        return connectPromise;
    }
    
    @Override
    public PushStream<TrainInfo> getTrainReportingData() {
        // We take the incoming batched data events, find the train location reports
        // (which have a msg_type of 0003), convert them to the right form, and send them on
        
        return psp.createStream(spes)
                .flatMap(e -> psp.streamOf(e.messages.stream()))
                .filter(m -> m.containsKey("header"))
                .filter(m -> "0003".equals(m.get("header").get("msg_type")))
                .map(m -> m.get("body"))
                .map(m -> mapper.convertValue(m, TrainMovementMessageBody.class))
                .filter(tmmb -> {
                        if(!stanoxToLatLong.containsKey(tmmb.loc_stanox)) {
                            // The train had an unknown reporting location so we cannot give it a geolocation
                            return false;
                        }
                        return true;
                    })
                .map(tmmb -> {
                        TrainInfo info = new TrainInfo();
                        info.current = stanoxToLatLong.get(tmmb.loc_stanox);
                        info.next = stanoxToLatLong.get(tmmb.next_report_stanox);
                        info.operator = tmmb.toc_id.getCompanyName();
                        info.status = tmmb.variation_status;
                        info.identifier = tmmb.train_id;
                        info.lastReported = tmmb.actual_timestamp;
                        return info;
                    });
    }
}
