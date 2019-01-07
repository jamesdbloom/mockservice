package org.mockserver.callback;

import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

/**
 * @author jamesdbloom
 */
public interface WebSocketRequestCallback {

    void handle(HttpRequest httpRequest);

    void handleError(HttpResponse httpResponse);

}
