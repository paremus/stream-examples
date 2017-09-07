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
package com.paremus.example.trains.ui;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.pushstream.PushStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paremus.example.trains.api.TrainInformationProvider;
import com.paremus.example.trains.api.dto.TrainInfo;

@Component(
    service=Servlet.class,
    property = { "osgi.http.whiteboard.servlet.pattern=/paremus/example/trains/data",
        "osgi.http.whiteboard.servlet.asyncSupported=true",
        "osgi.http.whiteboard.resource.pattern=/paremus/example/trains/static/*",
        "osgi.http.whiteboard.resource.prefix=/static",
        "com.paremus.app.endpoint=/paremus/example/trains/static/index.html"})
public class TrainLocationUIBroadcast extends HttpServlet {

    private static final long serialVersionUID = 198372497365L;

    @Reference
    private TrainInformationProvider trainInfoProvider;
    
    private final List<AsyncContext> contexts = new CopyOnWriteArrayList<>();
    
    private final AtomicReference<PushStream<TrainInfo>> activeStream = new AtomicReference<>();

    private final ObjectMapper mapper = new ObjectMapper();    
    
    @Deactivate
    void stop() {
        PushStream<TrainInfo> stream = activeStream.get();
        if(stream != null) {
            stream.close();
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        AsyncContext asyncContext = req.startAsync();
        contexts.add(asyncContext);

        asyncContext.setTimeout(MINUTES.toMillis(30));
        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");
        
        PushStream<TrainInfo> stream = trainInfoProvider.getTrainReportingData();
        
        if(activeStream.compareAndSet(null, stream)) {
            stream.map(this::toServerSentEvent)
                .forEach(this::deliver)
                .onResolve(this::complete);
        }
        
        asyncContext.addListener(new AsyncListener() {
            
            @Override
            public void onTimeout(AsyncEvent arg0) throws IOException {
                try {
                    arg0.getSuppliedResponse().getWriter().append("event: end\n" + "data: Timed out");
                } finally {
                    close(arg0.getAsyncContext());
                }
            }

            @Override
            public void onStartAsync(AsyncEvent arg0) throws IOException {}
            
            @Override
            public void onError(AsyncEvent arg0) throws IOException {
                try {
                    arg0.getSuppliedResponse().getWriter().append("event: end\n" + "data: " + arg0.getThrowable().getMessage());
                } finally {
                    close(arg0.getAsyncContext());
                }
            }
            
            @Override
            public void onComplete(AsyncEvent arg0) throws IOException {}
        });
    }

    private String toServerSentEvent(TrainInfo ti) {
        try {
            return "data: " + mapper.writeValueAsString(ti) + "\n\n";
        } catch (JsonProcessingException e) {
            return "event: dataerror\n" + 
                    "data: " + e.getMessage() + "\n\n";
        }
    }
    
    private void complete() {
        activeStream.set(null);
        deliver("event: end\n" + "data: End of Stream\n\n");
        List<AsyncContext> toComplete = new ArrayList<>(contexts);
        contexts.removeAll(toComplete);
        toComplete.forEach(AsyncContext::complete);
    }

    private void deliver(String json) {
        contexts.stream()
            .forEach(ctx -> {
                    try {
                        ctx.getResponse().getWriter().append(json).flush();
                    } catch (Exception e) { 
                        close(ctx);
                    }
                });
    }
    
    private void close(AsyncContext context) {
        PushStream<?> toClose = null;
        
            contexts.remove(context);
            if(contexts.isEmpty()) {
                toClose = activeStream.getAndSet(null);
            }
            
            context.complete();
    
        if(toClose != null) {
            try {
                toClose.close();
            } catch (Exception e) {}
        }
    }
}
