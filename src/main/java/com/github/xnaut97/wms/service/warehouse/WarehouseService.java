package com.github.xnaut97.wms.service.warehouse;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.warehouse.UpdateWarehouseRequest;
import com.github.xnaut97.wms.dto.warehouse.WarehouseRequest;
import com.github.xnaut97.wms.dto.warehouse.WarehouseResponse;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.enums.RoleType;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository repository;

    private final UserService userService;

    public Page<WarehouseResponse> getAll(

            String keyword,

            int page,

            int size

    ){

        Pageable pageable = PageRequest.of(page,size);

        return repository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
                        keyword,
                        keyword,
                        pageable
                )
                .map(this::map);

    }

    public WarehouseResponse getById(Long id){

        return map(
                findWarehouseById(id)
        );

    }

    @Audit(
            action = AuditAction.CREATE,
            entity = "Warehouse"
    )
    public WarehouseResponse create(WarehouseRequest request){

        if(repository.existsByCode(request.getCode())){
            throw new BusinessException("Warehouse code already exists");
        }

        User manager = null;

        if(request.getManagerId() != null){
            manager = userService.findUserById(request.getManagerId());
        }

        if (manager != null && manager.getRole().getRole() != RoleType.WAREHOUSE_MANAGER) {

            throw new BusinessException(
                    "Selected user is not a warehouse manager."
            );
        }

        Warehouse warehouse = new Warehouse();

        warehouse.setCode(request.getCode());
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setDescription(request.getDescription());
        warehouse.setManager(manager);
        warehouse.setEnabled(true);

        repository.save(warehouse);

        return map(warehouse);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "Warehouse"
    )
    public WarehouseResponse update(
            Long id,
            UpdateWarehouseRequest request
    ) {

        Warehouse warehouse = findWarehouseById(id);

        User manager = null;

        if (request.getManagerId() != null) {

            manager = userService.findUserById(request.getManagerId());

            if (manager.getRole().getRole() != RoleType.WAREHOUSE_MANAGER) {

                throw new BusinessException(
                        "Selected user is not a warehouse manager."
                );

            }

        }

        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setDescription(request.getDescription());
        warehouse.setManager(manager);
        warehouse.setEnabled(request.getEnabled());

        repository.save(warehouse);

        return map(warehouse);

    }

    @Audit(
            action = AuditAction.DELETE,
            entity = "Warehouse"
    )
    public void delete(Long id){

        Warehouse warehouse = findWarehouseById(id);

        warehouse.setEnabled(false);

        repository.save(warehouse);

    }

    private void validateWarehouseAccess(Warehouse warehouse){

        User currentUser = getCurrentUser();

        if(currentUser.getRole().getRole() == RoleType.ADMIN){
            return;
        }

        if(warehouse.getManager() == null){

            throw new BusinessException(
                    "Warehouse has no manager."
            );

        }

        if(!warehouse.getManager().getId()
                .equals(currentUser.getId())){

            throw new BusinessException(
                    "Access denied."
            );

        }

    }

    private WarehouseResponse map(Warehouse warehouse){

        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .address(warehouse.getAddress())
                .description(warehouse.getDescription())
                .managerId(
                        warehouse.getManager() == null
                                ? null
                                : warehouse.getManager().getId()
                )
                .managerName(
                        warehouse.getManager() == null
                                ? null
                                : warehouse.getManager().getUsername()
                )
                .enabled(warehouse.getEnabled())
                .build();

    }

    public Warehouse findWarehouseById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("Warehouse not found"));

    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            return userService.findByUsername("admin");
        }

        return userService.findByUsername(authentication.getName());

    }

}