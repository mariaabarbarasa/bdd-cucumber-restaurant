package ro.tss.restaurant.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ro.tss.restaurant.dto.OrderResponse;
import ro.tss.restaurant.model.OrderStatus;
import ro.tss.restaurant.service.OrderService;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OrderService service;

    @Test
    void createReturnsOrderWithTotal() throws Exception {
        OrderResponse response = new OrderResponse(1L, 5, OrderStatus.NEW,
                List.of(new OrderResponse.Line("Tripe Soup", 2, new BigDecimal("22.50"), new BigDecimal("45.00"))),
                new BigDecimal("45.00"));
        when(service.create(any())).thenReturn(response);

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tableId":1,"items":[{"menuItemId":7,"quantity":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.total").value(45.00))
                .andExpect(jsonPath("$.items[0].subtotal").value(45.00));
    }

    @Test
    void invalidQuantityInLineReturns400() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tableId":1,"items":[{"menuItemId":7,"quantity":0}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['items[0].quantity']").value("Quantity must be at least 1"));
    }

    @Test
    void forbiddenTransitionReturns409() throws Exception {
        when(service.changeStatus(eq(1L), eq(OrderStatus.PAID)))
                .thenThrow(new IllegalStateException("Order cannot change from NEW to PAID"));

        mvc.perform(put("/api/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"PAID"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Order cannot change from NEW to PAID"));
    }
}
