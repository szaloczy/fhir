package org.fhir;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws IOException {
        res.setContentType("text/plain");
        res.setStatus(HttpServletResponse.SC_OK);

        String code = "";
        String state = "";

        Map<String, String[]> parameters = req.getParameterMap();

        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {


            System.out.println("Parameter: " + entry.getKey());

            for (String value : entry.getValue()) {
                switch (entry.getKey()) {
                    case "code":
                        code = value;
                        break;

                    case "state":
                        state = value;
                        break;

                }
            }
        }
        Main.setAuthCode(code, state);
        res.getWriter().write("Code received, you may close this window");
    }
}
