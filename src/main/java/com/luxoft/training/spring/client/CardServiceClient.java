package com.luxoft.training.spring.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.view.RedirectView;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CardServiceClient {

    @Value("${auth_url}")
    String auth_url;

    @GetMapping("auth")
    public void auth(String code, String state, HttpSession session, HttpServletResponse response) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("client", "secret");
        Map<String, Object> map = new HashMap<>();

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

        Map res = restTemplate.postForObject(
                "http://localhost:8500/uaa/oauth/token?grant_type=authorization_code&code={code}&scope=read&state={state}",
                entity,
                Map.class,
                code, state);
        String access_token = (String) res.get("access_token");
        session.setAttribute("access_token", access_token);

        response.sendRedirect(state);
    }

    @GetMapping("card")
    public String getCardNumber(HttpSession session, HttpServletResponse response) throws IOException {
        String access_token = (String) session.getAttribute("access_token");
        if (access_token == null) {
            response.sendRedirect(auth_url+"&state=card");
            return null;
        }
        HttpHeaders headersBearer = new HttpHeaders();
        headersBearer.setBearerAuth(access_token);
        HttpEntity<Map<String, Object>> entityRequest =
                new HttpEntity<>(new HashMap<>(), headersBearer);

        ResponseEntity<String> result = new RestTemplate().exchange(
                "http://localhost:8080/create",
                HttpMethod.GET, entityRequest, String.class);

        return "Generated card number: "+result.getBody();
    }
}
