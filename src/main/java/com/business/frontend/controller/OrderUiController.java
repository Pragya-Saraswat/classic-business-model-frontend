package com.business.frontend.controller;

import com.business.frontend.util.ApiHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
public class OrderUiController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "http://localhost:8085/api";

    // API 8 — All orders filtered by status (global)
    @GetMapping("/ui/orders")
    public String getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, HttpSession session) {

        model.addAttribute("selectedStatus", status);

        if (status == null || status.isBlank()) return "orders";

        try {
            String url = BASE_URL + "/orders?status=" + status + "&page=" + page + "&size=" + size;
            HttpEntity<Void> entity = new HttpEntity<>(ApiHelper.bearerHeaders(session));
            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<?, ?> r = resp.getBody();
            Object content = r.get("content");
            model.addAttribute("orders", content != null ? content : r);
            model.addAttribute("totalPages",  r.get("totalPages"));
            model.addAttribute("currentPage", r.get("number"));
        } catch (Exception e) {
            model.addAttribute("error", "Could not load orders: " + e.getMessage());
        }
        return "orders";
    }

    // API 9 — Order detail by ID with line items
    @GetMapping("/ui/order-lookup")
    public String orderLookup(
            @RequestParam(required = false) Integer id,
            Model model, HttpSession session) {

        if (id == null) return "order-lookup";

        model.addAttribute("orderId", id);

        try {
            HttpEntity<Void> entity = new HttpEntity<>(ApiHelper.bearerHeaders(session));
            ResponseEntity<Map> resp = restTemplate.exchange(
                    BASE_URL + "/orders/" + id,
                    HttpMethod.GET, entity, Map.class);
            model.addAttribute("order", resp.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Order #" + id + " not found: " + e.getMessage());
        }
        return "order-lookup";
    }


    @GetMapping("/ui/orders/{id}")
    public String getOrderDetail(
            @PathVariable Integer id,
            Model model, HttpSession session) {

        model.addAttribute("orderId", id);

        try {
            HttpEntity<Void> entity = new HttpEntity<>(ApiHelper.bearerHeaders(session));
            ResponseEntity<Map> resp = restTemplate.exchange(
                    BASE_URL + "/orders/" + id,
                    HttpMethod.GET, entity, Map.class);
            model.addAttribute("order", resp.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Order #" + id + " not found: " + e.getMessage());
        }
        return "order-lookup";
    }
}