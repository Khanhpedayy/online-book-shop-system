package com.example.onlinebookshop.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ManagerOpenApiConfig {

    @Bean
    public OpenAPI bookShopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Online ManagerBook Shop â€” Inventory & Catalog API")
                        .description("""
                                Full API documentation for the Online ManagerBook Shop system.
                                Covers 47 workflows across Inventory, Catalog, Pricing, Reports, and more.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Admin")
                                .email("admin@bookshop.vn")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Dev")))
                .tags(List.of(
                        new Tag().name("1. Inventory Overview")
                                .description("View stock levels by title/variant, lot, condition + alerts"),
                        new Tag().name("2. ManagerBook Management")
                                .description("CRUD books with metadata, images, authors"),
                        new Tag().name("3. Variant & Pricing")
                                .description("Variant CRUD, base pricing, condition pricing, per-copy override"),
                        new Tag().name("4. Supplier Management")
                                .description("CRUD suppliers and purchase history"),
                        new Tag().name("5. Lot / Goods Receipt")
                                .description("Lot CRUD, generate copies, lock/unlock lots"),
                        new Tag().name("6. Copy Registry")
                                .description("Search copies, lifecycle, condition, location, status, photos"),
                        new Tag().name("7. Inventory Adjustments")
                                .description("Create stock adjustments (+/-) with reasons"),
                        new Tag().name("8. Cycle Count (Stocktaking)")
                                .description("Create sessions, record counts, apply adjustments"),
                        new Tag().name("9. Allocation Settings")
                                .description("FIFO rules, reservation TTL, override policy"),
                        new Tag().name("10. Returns Decision")
                                .description("Restock, reprice, mark damaged, supplier return"),
                        new Tag().name("11. Reports")
                                .description("Sales, slow movers, lot aging, inventory value, shrinkage")));
    }
}

