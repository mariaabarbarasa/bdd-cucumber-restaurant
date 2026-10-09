package ro.tss.restaurant.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import ro.tss.restaurant.dto.MenuItemResponse;
import ro.tss.restaurant.exception.BusinessRuleException;
import ro.tss.restaurant.exception.ResourceNotFoundException;
import ro.tss.restaurant.model.Category;
import ro.tss.restaurant.service.MenuService;

/**
 * Web layer test: only MenuController + GlobalExceptionHandler are loaded;
 * the service is replaced by a mock (@MockitoBean).
 */
@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MenuService service;

    private final MenuItemResponse papanasi =
            new MenuItemResponse(1L, "Papanasi", null, Category.DESSERT, new BigDecimal("25.00"), true);

    @Test
    void createReturns201WithLocation() throws Exception {
        when(service.create(any())).thenReturn(papanasi);

        mvc.perform(post("/api/menu").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Papanasi","category":"DESSERT","price":25.00,"available":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/menu/1"))
                .andExpect(jsonPath("$.price").value(25.00));
    }

    @Test
    void invalidDataReturns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/menu").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","price":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Menu item name is required"))
                .andExpect(jsonPath("$.errors.category").value("Category is required"))
                .andExpect(jsonPath("$.errors.price").value("Price must be positive"));
    }

    @Test
    void unknownCategoryReturns400() throws Exception {
        mvc.perform(post("/api/menu").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Pizza","category":"PIZZA","price":30}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filtersByCategory() throws Exception {
        when(service.findAll(Category.DESSERT)).thenReturn(List.of(papanasi));

        mvc.perform(get("/api/menu").param("category", "DESSERT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void unknownItemReturns404() throws Exception {
        when(service.findById(99L)).thenThrow(new ResourceNotFoundException("Menu item", 99L));

        mvc.perform(get("/api/menu/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Menu item with id 99 was not found"));
    }

    @Test
    void businessRuleViolationReturns409() throws Exception {
        doThrow(new BusinessRuleException("Menu item is used in orders")).when(service).delete(1L);

        mvc.perform(delete("/api/menu/1"))
                .andExpect(status().isConflict());
    }
}
