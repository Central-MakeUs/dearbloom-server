package kr.co.dearbloom.domain.customer.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.product.entity.Product;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_saved_product_customer_product",
        columnNames = {"customer_id", "product_id"}))
public class SavedProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savedProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
