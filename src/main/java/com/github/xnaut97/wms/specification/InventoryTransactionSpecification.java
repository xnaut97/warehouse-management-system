package com.github.xnaut97.wms.specification;

import com.github.xnaut97.wms.entity.inventory.InventoryTransaction;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryTransactionSpecification {

    public static Specification<InventoryTransaction> filter(

            Long warehouseId,

            Long materialId,

            InventoryTransactionType type,

            LocalDate fromDate,

            LocalDate toDate

    ){

        return (root, query, cb)->{

            List<Predicate> predicates =
                    new ArrayList<>();

            if(warehouseId != null){

                predicates.add(

                        cb.equal(
                                root.get("warehouse").get("id"),
                                warehouseId
                        )

                );

            }

            if(materialId != null){

                predicates.add(

                        cb.equal(
                                root.get("material").get("id"),
                                materialId
                        )

                );

            }

            if(type != null){

                predicates.add(

                        cb.equal(
                                root.get("type"),
                                type
                        )

                );

            }

            if(fromDate != null){

                predicates.add(

                        cb.greaterThanOrEqualTo(

                                root.get("createdAt"),

                                fromDate.atStartOfDay()

                        )

                );

            }

            if(toDate != null){

                predicates.add(

                        cb.lessThanOrEqualTo(

                                root.get("createdAt"),

                                toDate.atTime(23,59,59)

                        )

                );

            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );

        };

    }

}