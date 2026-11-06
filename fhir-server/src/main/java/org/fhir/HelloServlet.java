package org.fhir;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws IOException {
        res.setContentType("text/plain");
        res.setStatus(HttpServletResponse.SC_OK);

        res.getWriter().write("Hello");
    }
}
