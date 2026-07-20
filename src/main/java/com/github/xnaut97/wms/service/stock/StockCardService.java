package com.github.xnaut97.wms.service.stock;

import com.github.xnaut97.wms.dto.inventory.StockCardItemResponse;
import com.github.xnaut97.wms.dto.inventory.StockCardResponse;
import com.github.xnaut97.wms.entity.inventory.InventoryTransaction;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.inventory.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockCardService {

    private final InventoryTransactionRepository transactionRepository;

    @Transactional
    public StockCardResponse getStockCard(

            Long warehouseId,

            Long materialId,

            LocalDate fromDate,

            LocalDate toDate

    ) {

        List<InventoryTransaction> transactions =
                transactionRepository.findStockCard(

                        warehouseId,

                        materialId,

                        fromDate == null ? null : fromDate.atStartOfDay(),

                        toDate == null ? null : toDate.atTime(23,59,59)

                );

        BigDecimal balance = BigDecimal.ZERO;

        List<StockCardItemResponse> items = new ArrayList<>();

        for (InventoryTransaction transaction : transactions) {

            BigDecimal in = BigDecimal.ZERO;

            BigDecimal out = BigDecimal.ZERO;

            if (transaction.getType() == InventoryTransactionType.IN) {

                in = transaction.getQuantity();

                balance = balance.add(in);

            } else {

                out = transaction.getQuantity();

                balance = balance.subtract(out);

            }

            items.add(

                    StockCardItemResponse.builder()

                            .transactionDate(transaction.getCreatedAt())

                            .referenceNo(transaction.getReferenceNo())

                            .transactionType(transaction.getType().name())

                            .quantityIn(in)

                            .quantityOut(out)

                            .balance(balance)

                            .build()

            );

        }

        if (transactions.isEmpty()) {

            throw new BusinessException(
                    "Không tìm thấy giao dịch."
            );

        }

        InventoryTransaction first = transactions.getFirst();

        return StockCardResponse.builder()

                .warehouse(
                        first.getWarehouse().getName()
                )

                .materialCode(
                        first.getMaterial().getCode()
                )

                .materialName(
                        first.getMaterial().getName()
                )

                .transactions(items)

                .build();

    }

}
