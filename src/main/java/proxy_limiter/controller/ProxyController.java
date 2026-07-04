package proxy_limiter.controller;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;

@RestController //Spring Gestion des requetes web
public class ProxyController {

    // L'URL du serveur final qu'on veut protéger (notre faux serveur)
    private final String TARGET_SERVER_URL = "https://jsonplaceholder.typicode.com";

    // RestTemplate est l'outil Java pour envoyer des requêtes HTTP
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Cette méthode attrape TOUTES les requêtes (GET, POST, etc.)
     * qui commencent par n'importe quelle URL.
     */
    @RequestMapping("/**")
    public ResponseEntity<String> forwardRequest(HttpServletRequest request) {

        // 1. On récupère l'URL exacte que l'utilisateur a demandée
        // Ex: S'il tape /api/users, on récupère /api/users
        String requestURI = request.getRequestURI();

        // 2. On construit la nouvelle URL vers le vrai serveur
        // Ex: https://jsonplaceholder.typicode.com + /api/users
        String targetUrl = TARGET_SERVER_URL + requestURI;

        System.out.println("[PROXY] Transfert de la requête vers : " + targetUrl);

        try {
            // 3. On envoie la requête au vrai serveur
            ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.valueOf(request.getMethod()),
                    null, // On ne copie pas le "body" de la requête pour simplifier au début
                    String.class
            );

            // 4. On renvoie la réponse du vrai serveur à notre utilisateur
            return response;

        } catch (Exception e) {
            System.err.println("[ERREUR PROXY] Impossible de joindre le serveur : " + e.getMessage());
            return ResponseEntity.status(502).body("Erreur de passerelle (Bad Gateway)");
        }
    }
}