package com.github.xnaut97.wms.specification;

import com.github.xnaut97.wms.entity.inventory.Inventory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class InventorySpecification {

    public static Specification<Inventory> lowStock() {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(
                        root.get("quantity"),
                        root.get("material")
                                .get("minimumStock")
                );

    }

    public static Specification<Inventory> filter(
            Long warehouseId,
            Long materialId,
            String keyword
    ) {

        return (root, query, cb) -> {
            List<Predicate> predicates =
                    new ArrayList<>();
            if (warehouseId != null) {
                predicates.add(cb.equal(root.get("warehouse").get("id"), warehouseId));
            }

            if (materialId != null) {
                predicates.add(cb.equal(root.get("material").get("id"), materialId));
            }

            if (keyword != null &&
                    !keyword.isBlank()) {

                String search =
                        "%" +
                                keyword.toLowerCase() +
                                "%";

                predicates.add(

                        cb.or(

                                cb.like(

                                        cb.lower(

                                                root.get("material")
                                                        .get("name")

                                        ),

                                        search

                                ),

                                cb.like(

                                        cb.lower(

                                                root.get("material")
                                                        .get("code")

                                        ),

                                        search

                                )

                        )

                );

            }

            return cb.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );

        };

    }

}