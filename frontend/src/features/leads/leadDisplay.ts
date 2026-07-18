import type { Lead } from '../../types/lead';

export function vehicleLabel(lead: Lead) {
  if (lead.item?.vehicle) {
    const value = [lead.item.vehicle.name, lead.item.vehicle.model, lead.item.vehicle.year?.toString()].filter(Boolean).join(' ');
    if (value) {
      return value;
    }
  }
  return lead.vehicleInterest ?? '-';
}
