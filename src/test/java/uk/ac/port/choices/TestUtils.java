package uk.ac.port.choices;

import org.json.JSONObject;
import org.mockito.Mockito;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.mockito.Mockito.*;

public final class TestUtils {

    public static final Level LOG_LEVEL = Level.INFO;

    private TestUtils() {

    }

    public static HttpServletRequest createMockRequest(String method, String URI, Map<String, String> parameters,
                                                       Map<String, String> headers, Map<String,Object> session) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(URI);
        StringBuilder data = new StringBuilder();
        if (parameters != null) {
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                when(request.getParameter(param.getKey())).thenReturn(param.getValue());
                data.append(param.getKey()).append("=").append(param.getValue()).append("&");
            }
        }
        HttpSession sessionMock = mock(HttpSession.class);
        when(request.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute(Mockito.anyString())).thenReturn(null);
        if (session != null){
            for (Map.Entry<String, Object> param : session.entrySet()) {
                when(sessionMock.getAttribute(param.getKey())).thenReturn(param.getValue());
            }
        }
        try {
            doReturn(new ServletInputStream() {
                private final ByteArrayInputStream stream = new ByteArrayInputStream(data.toString().getBytes());

                @Override
                public boolean isFinished() {
                    return stream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return stream.available() > 0;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                }

                @Override
                public int read() {
                    return stream.read();
                }
            }).when(request).getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        when(request.getParameterMap()).thenReturn(new HashMap<>());
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                when(request.getHeader(header.getKey())).thenReturn(header.getValue());
            }
        }
        return request;
    }

    public static HttpServletResponse createMockResponse(StringWriter stringWriter) {
        HttpServletResponse response = mock(HttpServletResponse.class);
        try {
            when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
        } catch (IOException ignored) {
        }
        return response;
    }

    public static JSONObject getResponseAsJSON(StringWriter stringWriter) {
        return new JSONObject(stringWriter.toString());
    }

}
