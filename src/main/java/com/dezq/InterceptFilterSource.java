package com.dezq;

import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class InterceptFilterSource extends HttpFiltersSourceAdapter {
    private Main gui;

    public InterceptFilterSource(Main gui) {
        this.gui = gui;
    }

    @Override
    public HttpFiltersAdapter filterRequest(HttpRequest originalRequest) {
        return new HttpFiltersAdapter(originalRequest) {
            @Override
public HttpResponse clientToProxyRequest(HttpObject httpObject) {
    if (httpObject instanceof HttpRequest) {
        HttpRequest req = (HttpRequest) httpObject;
        String uri = req.getUri();
        String method = req.getMethod().name();

        // 1. SILENTLY PASS the 'CONNECT' noise and background services
        // This lets the background tunnels happen without bothering you
        if (method.equals("CONNECT") || uri.contains("mozgcp.net") || uri.contains("mozilla")) {
            return null; 
        }

        // 2. ONLY PAUSE for the requests you actually want to see
        gui.updateUI("INTERCEPTED REQUEST:\n" + method + " " + uri);
        
        try {
            // This is the "Brake Pedal"
            Main.interceptQueue.take(); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    return null;
}
        };
    }
}