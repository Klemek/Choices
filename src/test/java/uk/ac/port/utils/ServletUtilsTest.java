package uk.ac.port.utils;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.ac.port.TestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"java.*", "javax.*", "org.*"})
public class ServletUtilsTest {
    @Test
    public void testMatchingURI() {
        assertTrue(ServletUtils.matchingURI("/api/test/{}/test", "/api2/test/bla/test", 2));
        assertFalse(ServletUtils.matchingURI("/api", "/api2", 1));
        assertFalse(ServletUtils.matchingURI("/api/group/{}", "/api/group", 2));
    }

    @Test
    public void testMapRequestSuccess() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/test/bla", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        LinkedHashMap<String, Runnable> map = new LinkedHashMap<>();
        map.put("PUT /api/test/{}", () -> {
            fail("Invalid mapping");
        });
        map.put("DELETE /api/test/{}", () -> {
            Integer.parseInt("a");
        });

        try {
            ServletUtils.mapRequest(request, response, map);
            fail("Invalid mapping");
        } catch (NumberFormatException e) {
        }
    }

    @Test
    public void testMapRequestSuccess2() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/vm/templates", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        LinkedHashMap<String, Runnable> map = new LinkedHashMap<>();
        map.put("GET /api/vm/templates", () -> {
            Integer.parseInt("a");
        });
        map.put("GET /api/vm/{}", () -> {
            fail("Invalid mapping");
        });

        try {
            ServletUtils.mapRequest(request, response, map);
            fail("Invalid mapping");
        } catch (NumberFormatException e) {
        }
    }

    @Test
    public void testMapRequestWrongMethod() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/test/bla", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        LinkedHashMap<String, Runnable> map = new LinkedHashMap<>();
        map.put("PUT /api/test/{}", () -> {
            fail("Invalid mapping");
        });
        map.put("DELETE /api/test/{}", () -> {
            fail("Invalid mapping");
        });
        map.put("GET /api/test/bla/{}", () -> {
            fail("Invalid mapping");
        });

        ServletUtils.mapRequest(request, response, map);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, res.getInt("code"));
    }

    @Test
    public void testMapRequestNotFound() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/test/test2", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        LinkedHashMap<String, Runnable> map = new LinkedHashMap<>();
        map.put("GET /api/test/test", () -> {
            fail("Invalid mapping");
        });
        map.put("GET /api/test/test3", () -> {
            fail("Invalid mapping");
        });

        ServletUtils.mapRequest(request, response, map);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, res.getInt("code"));
    }

    @Test
    public void testHandleCrossOrigin() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("OPTIONS", "/api/test/test2", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        assertTrue(ServletUtils.handleCrossOrigin(request, response));
    }

    @Test
    public void testHandleCrossOrigin2() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/test/test2", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        assertFalse(ServletUtils.handleCrossOrigin(request, response));
    }
}
