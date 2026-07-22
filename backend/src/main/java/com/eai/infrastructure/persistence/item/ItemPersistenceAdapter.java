package com.eai.infrastructure.persistence.item;

import com.eai.application.item.ItemRepository;
import com.eai.domain.item.Item;
import com.eai.domain.item.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ItemPersistenceAdapter implements ItemRepository {

    private final SpringDataItemRepository itemRepository;
    private final SpringDataVehicleRepository vehicleRepository;

    @Override
    public ItemWithVehicle save(Item item, Vehicle vehicle) {
        ItemJpaEntity savedItemEntity = itemRepository.save(toEntity(item));
        Vehicle savedVehicle = vehicle == null ? null : toDomain(vehicleRepository.save(toEntity(vehicle)));
        Item savedItem = toDomain(savedItemEntity, savedVehicle);
        return new ItemWithVehicle(savedItem, savedVehicle);
    }

    @Override
    public Optional<ItemWithVehicle> findById(UUID id) {
        return itemRepository.findById(id)
                .map(item -> {
                    Vehicle vehicle = vehicleRepository.findByItemId(id).map(this::toDomain).orElse(null);
                    return new ItemWithVehicle(toDomain(item, vehicle), vehicle);
                });
    }

    private Item toDomain(ItemJpaEntity entity) {
        return toDomain(entity, null);
    }

    private Item toDomain(ItemJpaEntity entity, Vehicle vehicle) {
        return new Item(entity.getId(), entity.getOwnerUserId(), entity.getName(), vehicle, entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private Vehicle toDomain(VehicleJpaEntity entity) {
        return new Vehicle(entity.getId(), entity.getItemId(), entity.getName(), entity.getYear(), entity.getModel(), entity.getValue(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private ItemJpaEntity toEntity(Item item) {
        ItemJpaEntity entity = new ItemJpaEntity();
        entity.setId(item.getId());
        entity.setOwnerUserId(item.getOwnerUserId());
        entity.setName(item.getName());
        entity.setCreatedAt(item.getCreatedAt());
        entity.setUpdatedAt(item.getUpdatedAt());
        return entity;
    }

    private VehicleJpaEntity toEntity(Vehicle vehicle) {
        VehicleJpaEntity entity = new VehicleJpaEntity();
        entity.setId(vehicle.getId());
        entity.setItemId(vehicle.getItemId());
        entity.setName(vehicle.getName());
        entity.setYear(vehicle.getYear());
        entity.setModel(vehicle.getModel());
        entity.setValue(vehicle.getValue());
        entity.setCreatedAt(vehicle.getCreatedAt());
        entity.setUpdatedAt(vehicle.getUpdatedAt());
        return entity;
    }
}
