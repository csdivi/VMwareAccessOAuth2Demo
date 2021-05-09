package com.sample.oauth2;

import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Controller
public class UiController {

    @Value("${oauth2.server:}")
    private String oauth2Server;

    @Value("${oauth2.client:}")
    private String oauth2Client;

    @Value("${oauth2.secret:}")
    private String oauth2Secret;

    @Value("${oauth2.redirect.url:}")
    private String oauth2RedirectUrl;

    @GetMapping(value = "/login", produces = "text/html")
    public String index(HttpServletResponse response) throws IOException {

        String redirectUri = URLEncoder.encode(oauth2RedirectUrl, StandardCharsets.UTF_8.toString());
        response.sendRedirect(oauth2Server + "/SAAS/auth/oauth2/authorize"
            + "?response_type=code"
            + "&client_id=" + oauth2Client
            + "&redirect_uri=" + redirectUri);
        return "mylogin";
    }

    @GetMapping(value = "/verify", produces = "text/html")
    public String verify(Map<String, Object> model,
                         @RequestParam String code,
                         @RequestParam(required = false) String state) throws URISyntaxException, UnsupportedEncodingException {

        String redirectUri = URLEncoder.encode(oauth2RedirectUrl, StandardCharsets.UTF_8.toString());
        String token = verifyAuthCodeAndObtainAccessToken(code, redirectUri);

        String[] splitToken = token.split("\\.");
        String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";

        Jwt jwt = Jwts.parser().parse(unsignedToken);
        // TODO - validate JWT with private cert...

        model.put("eml", ((Claims) jwt.getBody()).get("eml"));
        model.put("prn", ((Claims) jwt.getBody()).get("prn"));
        model.put("domain", ((Claims) jwt.getBody()).get("domain"));

        return "oauth";
    }


    public String verifyAuthCodeAndObtainAccessToken(final String authCode, final String redirectUrl) throws URISyntaxException {
        RestTemplate template = new RestTemplate();
        String oauth2AccessTokenUrl = oauth2Server
            + "/SAAS/auth/oauthtoken"
            + "?grant_type=authorization_code"
            + "&code=" + authCode
            + "&redirect_uri=" + redirectUrl
            + "&client_id=" + oauth2Client
            + "&client_secret=" + oauth2Secret;

        HttpHeaders httpHeaders = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        httpHeaders.setContentType(APPLICATION_JSON);
        RequestEntity requestData = new RequestEntity(POST, new URI(oauth2AccessTokenUrl));
        ResponseEntity<JsonNode> responseEntity = template.exchange(requestData, JsonNode.class);
        System.out.println(responseEntity.getBody().toString());
        String token = responseEntity.getBody().get("access_token").asText();
        return token;
    }
}
