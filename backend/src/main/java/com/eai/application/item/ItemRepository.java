package com.eai.application.item;

import com.eai.domain.item.Item;
import com.eai.domain.item.Vehicle;

import java.util.Optional;
import java.util.UUID;

public interface ItemRepository {

    ItemWithVehicle save(Item item, Vehicle vehicle);

    Optional<ItemWithVehicle> findById(UUID id);

    record ItemWithVehicle(Item item, Vehicle vehicle) {
    }
}
